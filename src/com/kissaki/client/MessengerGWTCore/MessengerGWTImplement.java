package com.kissaki.client.MessengerGWTCore;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.Window;
import com.kissaki.client.MessengerGWTCore.MessageCenter.MessageMasterHub;
import com.kissaki.client.MessengerGWTCore.MessageCenter.MessageReceivedEvent;
//import com.kissaki.client.MessengerGWTCore.MessageCenter.MessageReceivedEventHandler;
import com.kissaki.client.MessengerGWTCore.MessageCenter.MessageReceivedHandler;
import com.kissaki.client.subFrame.debug.Debug;
import com.kissaki.client.uuidGenerator.UUID;


/**
 * MessengerGWTの実装
 * 
 * Obj-C版の実装から、親子関係を取り除いたバージョン
 * 
 * -任意時間での遅延実行：製作中
 * -クライアント間の通信：未作成
 * -クライアント-サーバ感の通信：未作成
 * 
 * @author ToruInoue
 */
public class MessengerGWTImplement extends MessageReceivedHandler implements MessengerGWTInterface {
	
	static final String version = "0.7.1";//バグフィックスとか調整中
//		"0.7.0";//11/01/18 17:50:30 Beta release
//		"0.5.2";//11/01/18 16:41:28 changed to EventBus from HasHandlers(Duplicated) 
//		"0.5.1";//11/01/09 20:55:55 String-Value-Bug fixed.
//		"0.5.0";//11/01/05 19:23:28 Alpha release
	
	
	Debug debug;
	
	public final String messengerName;
	public final String messengerID;
	public final Object invokeObject;
	
	public final String KEY_MESSENGER_NAME = "MESSENGER_messengerName";
	public final String KEY_MESSENGER_ID = "MESSENGER_messengerID";
	public final String KEY_TO_NAME = "MESSENGER_to";
	public final String KEY_MESSENGER_EXEC = "MESSENGER_exec";
	public final String KEY_MESSENGER_TAGVALUE_GROUP = "MESSENGER_tagValue"; 
	
	List <JSONObject> sendList = null;
	List <JSONObject> receiveList = null;
	private static MessageMasterHub masterHub;
	
	
	
	/**
	 * コンストラクタ
	 * メッセージの受信ハンドラを設定する
	 * @param string 
	 */
	public MessengerGWTImplement (String messengerName, Object invokeObject) {
		debug = new Debug(this);

		this.messengerName = messengerName;
		this.messengerID = UUID.uuid(8,16);
		this.invokeObject = invokeObject;
		
		if (masterHub == null) {
			masterHub = MessageMasterHub.getMaster();
		} else {
			debug.trace("not first");
		}
		
		sendList = new ArrayList<JSONObject>();
		receiveList = new ArrayList<JSONObject>();
		
		if (masterHub.getMessengerGlobalStatus() == MESSENGER_STATUS_READY_FOR_INITIALIZE) {
			debug.trace("initialize");
			setUpMessaging(masterHub);
		}
		
		masterHub.setInvokeObject(invokeObject, this);
		
		debug.trace("setInvokefinished");
	}
	
	
	/**
	 * JSNIへとmtdメソッドのJSオブジェクトを投入し、messageハンドラを初期化する。
	 * @param globalMasterHub 
	 */
	public void setUpMessaging (MessageMasterHub globalMasterHub) {
		int status = setUp(get());//Javaのメソッドのポインタ渡しが実現されている。
		debug.assertTrue(globalMasterHub.getMessengerGlobalStatus() == MESSENGER_STATUS_READY_FOR_INITIALIZE, "already initialized");
		globalMasterHub.setMessengerGlobalStatus(status);
	}
	
	/**
	 * mtdメソッドをJSOとして値渡しするためのメソッド
	 * @return
	 */
	private native JavaScriptObject get () /*-{
		return @com.kissaki.client.MessengerGWTCore.MessengerGWTImplement::mtd(Ljava/lang/String;);
	}-*/;
	

	/**
	 * TODO このメソッドから、MasterHubまでがstaticでなくても反応するようにしたい。どうすればいいか。
	 * 今後の課題
	 * 
	 * このメソッドは、staticであるために、
	 * リスナに関しては最初に誰かがひとつセットすればそれでいい。
	 * 
	 * @param message
	 */
	public static void mtd(String message) {
		MessageMasterHub.invokeReceive(message);
	}
	public static void called (String message) {
		Debug debugs = new Debug("");
		debugs.trace("届かないんじゃねーかなー_"+message);
	}
	
	
	
	
	/**
	 * セットアップ
	 * 
	 * Messengerの初期設定を行う
	 * Nativeのメッセージ受信部分
	 */
	private native int setUp(JavaScriptObject method) /*-{
		try {
			if (typeof window.postMessage === "undefined") { 
	    		alert("残念ですが、あなたのブラウザはpostMessage APIをサポートしていません。");
	    		return @com.kissaki.client.MessengerGWTCore.MessengerGWTInterface::MESSENGER_STATUS_NOT_SUPPORTED;
			} else {
				window.addEventListener('message',
					function(e) {method(e.data);},
					false);
			}
			
			return @com.kissaki.client.MessengerGWTCore.MessengerGWTInterface::MESSENGER_STATUS_OK;
		} catch (er) {
			alert("messenger_undefined_error_"+er);
			return @com.kissaki.client.MessengerGWTCore.MessengerGWTInterface::MESSENGER_STATUS_FAILURE;
		}
	}-*/;
	
	
//	/**
//	 * セットアップ
//	 * 
//	 * Messengerの初期設定を行う
//	 * Nativeのメッセージ受信部分
//	 * 
//	 * TODO この部分が、staticなメソッドしか引数に取らないのが絶望的。解消せねば、、
//	 */
//	public native void setUp(String messengerName, String messengerID) /*-{
//		if (typeof window.postMessage === "undefined") { 
//    		alert("残念ですが、あなたのブラウザはメッセージAPIをサポートしていません。"); 
//		}
//		this.@com.kissaki.client.MessengerGWTCore.MessengerGWTImplement::log(Ljava/lang/String;)("セットアップ");//com.google.gwt.user.client.Event
//		
//		
//		window.addEventListener('message', 
//			center,
//			//receiver,
////			this.@com.kissaki.client.MessengerGWTCore.MessengerGWTImplement::log2(Lcom/google/gwt/user/client/Event;),//ここね。
//			false);//TODO このfalseって何
////		window.attachEvent('message', this.@com.kissaki.client.MessengerGWTCore.MessengerGWTImplement::log2(Lcom/google/gwt/user/client/Event;));
//		
//		function receiver() {//この書き方だと、内部のメソッドはstaticで無ければ行けない。それは、駄目でしょ。
////			if (e.origin == 'http://example.com') {
////				if (e.data == 'Hello world') {
////					e.source.postMessage('Hello', e.origin);
////				} else {
////					alert(e.data);
////				}
////			}
//		}
//	}-*/;
	

	/**
	 * メッセージ受取メソッド
	 * TODO イベントハンドラで呼ばれている。　native実装が来たら、調整する。　イベントに登録したmessenger全てに送られているが、native実装があれば、そんなに頑張らないでいいはず。
	 * @param event
	 */
	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		String rootMessage = event.getMessage();
		
//		debug.trace("rootMessage_"+rootMessage);
		
		JSONObject rootObject = null;

		String fromName = null;
		String fromID = null;
		
		String toName = null;
		
		String command = null;
		JSONObject tagValue = null;
		
		try {
			rootObject = JSONParser.parseStrict(rootMessage).isObject();
		} catch (Exception e) {
//			debug.trace("receiveMessage_parseError_"+e);
		}
		
		if (rootObject == null) {
//			debug.trace("rootObject = null");
			return;
		}
		
		
		fromName = rootObject.get(KEY_MESSENGER_NAME).isString().stringValue();
		if (fromName == null) {
//			debug.trace("fromName = null");
			return;
		}
		
		
		fromID = rootObject.get(KEY_MESSENGER_ID).isString().stringValue();
		if (fromID == null) {
//			debug.trace("fromID = null");
			return;
		}
		
		
		toName = rootObject.get(KEY_TO_NAME).isString().stringValue();
		if (toName == null) {
//			debug.trace("receiverName = null");
			return;
		}
		
		if (!toName.equals(getName())) {
//			debug.trace("!receiverName_"+toName+" /vs/ "+getName());
			return;
		}
		
		//宛先の名前と自分の名前が同じ
		
		command = rootObject.get(KEY_MESSENGER_EXEC).isString().stringValue();
		if (command == null) {
//			debug.trace("command = null");
			return;
		}
		
		//コマンドも存在する
		
		tagValue = rootObject.get(KEY_MESSENGER_TAGVALUE_GROUP).isObject();
		if (tagValue == null) {
//			debug.trace("tagValue = null");
			return;
		}
		
//		debug.trace("tagValue_"+tagValue);
		
		addReceiveLog(fromName, fromID, toName, command, tagValue);
		receiveCenter(rootMessage);
	}
	
	
	



	/**
	 * 内部から外部への行使
	 */
	public void receiveCenter(String rootMessage) {
		((MessengerGWTInterface) getInvokeObject()).receiveCenter(rootMessage);
	}


		

	/**
	 * 送付前のメッセージのプレビューを取得するメソッド
	 * @param receiverName
	 * @param command
	 * @param tagValue
	 * @return
	 */
	public JSONObject getMessageObjectPreview (String receiverName, String command, JSONObject ... tagValue) {
		return getMessageStructure(getName(), getID(), receiverName, command, tagValue);
	}
	
	
	
	/**
	 * vメッセージ送信メソッド
	 * @param receiverName
	 * @param command
	 * @param tagValue
	 */
	public void call(String receiverName, String command, JSONObject ... tagValue) {
		
		JSONObject messageMap = getMessageStructure(getName(), getID(), receiverName, command, tagValue);
		
		String href = Window.Location.getHref();//アドレスが変わったら使えない、張り直しなどの対策が必要なところ。
		postMessage(messageMap.toString(), href);
		
		addSendLog(receiverName, command, tagValue);//ログを残す
	}
	
	
	/**
	 * 非同期メッセージ送信メソッド、
	 * tagValueが無いバージョン
	 * @param receiverName
	 * @param command
	 */
	public void call(String receiverName, String command) {
		JSONObject [] tagValue = new JSONObject [0];//長さ0の配列としてセット、中身は空
		call(receiverName, command, tagValue);
	}
	
	/**
	 * 入力されたメッセージを元に、宛先とコマンドを変更して発信する
	 * @param receiverName
	 * @param command
	 * @param eventString
	 * @return
	 */
	public String copyOut(String newReceiverName, String newCommand, String eventString) {
		//内容チェックを行い、receiverとcommandを書き換える
		debug.assertTrue(newReceiverName != null, "newReceiverName = null");
		debug.assertTrue(newCommand != null, "newCommand = null");
		debug.assertTrue(eventString != null, "eventString = null");
		
		JSONObject eventObj = JSONParser.parseStrict(eventString).isObject();
		/*
		 * eventString_
		 * {
		 * 	"MESSENGER_messengerName":"receiver", 
		 * 	"MESSENGER_messengerID":"342E1E5C", 
		 * 	"MESSENGER_to":"sender", 
		 * 	"MESSENGER_exec":"testCommand", 
		 * 	"MESSENGER_tagValue":{"tag1":"value1"}
		 * }
		 */
		debug.assertTrue(eventObj.containsKey(KEY_MESSENGER_NAME), "not contain KEY_MESSENGER_NAME");
		debug.assertTrue(eventObj.containsKey(KEY_MESSENGER_ID), "not contain KEY_MESSENGER_ID");
		debug.assertTrue(eventObj.containsKey(KEY_MESSENGER_EXEC), "not contain KEY_MESSENGER_EXEC");
		debug.assertTrue(eventObj.containsKey(KEY_TO_NAME), "not contain KEY_TO_NAME");
		debug.assertTrue(eventObj.containsKey(KEY_MESSENGER_TAGVALUE_GROUP), "not contain KEY_MESSENGER_TAGVALUE_GROUP");
		return replaceSenderInformation(getName(), getID(), newReceiverName, newCommand, eventObj).toString();
	}
	


	/**
	 * 送信者情報を特定のものに変更する
	 * @param name
	 * @param id
	 * @param newCommand 
	 * @param newReceiverName 
	 * @param eventObj
	 */
	private JSONObject replaceSenderInformation(String name, String id,
			String newReceiverName, String newCommand, JSONObject eventObj) {
		JSONObject newObject = new JSONObject();
		
		newObject.put(KEY_MESSENGER_NAME, new JSONString(name));
		newObject.put(KEY_MESSENGER_ID, new JSONString(id));
		newObject.put(KEY_TO_NAME, new JSONString(newReceiverName));
		newObject.put(KEY_MESSENGER_EXEC, new JSONString(newCommand));
		newObject.put(KEY_MESSENGER_TAGVALUE_GROUP, eventObj.get(KEY_MESSENGER_TAGVALUE_GROUP));
		
		return newObject;
	}



	/**
	 * 秘匿されるべき関数
	 * 
	 * 特定の宛先に向けて、メッセージを送付する
	 * @param message
	 * @param uri
	 */
	private native void post (String message, String uri) /*-{
		window.postMessage(message, uri);
	}-*/;

	
	/**
	 * 送信メソッド
	 * @param message
	 * @param href
	 */
	private void postMessage (String message, String href) {
		post(message, href);
	}
	
	
	
	
	
	/**
	 * 送信メッセージ構造を構築する
	 * 
	 * KEY_MESSENGER_NAME:送信者名
	 * KEY_MESSENGER_ID:送信者ID
	 * KEY_TO_NAME:送信先
	 * KEY_MESSENGER_EXEC:実行コマンド
	 * KEY_MESSENGER_TAGVALUE_GROUP:タグとバリューのグループ
	 * 
	 * @param receiverName
	 * @param command
	 * @param tagValue
	 * @return
	 */
	private JSONObject getMessageStructure(
			String senderName,
			String senderID,
			String receiverName,
			String command, JSONObject[] tagValue) {
		JSONObject messageMap = new JSONObject();
		
//		debug.trace("getName()_"+getName());
//		debug.trace("getID()_"+getID());
		
		messageMap.put(KEY_MESSENGER_NAME, new JSONString(senderName));
		messageMap.put(KEY_MESSENGER_ID, new JSONString(senderID));
		messageMap.put(KEY_TO_NAME, new JSONString(receiverName));
		messageMap.put(KEY_MESSENGER_EXEC, new JSONString(command));
		
		JSONObject tagValueGroup = new JSONObject();
		
		int i = 0;
		for (JSONObject currentObject:tagValue) {
			for (Iterator<String> currentItel = currentObject.keySet().iterator(); currentItel.hasNext();) {
				String currentKey = currentItel.next();
				tagValueGroup.put(currentKey, currentObject.get(currentKey));//オブジェクトの移し替え
			}
			i++;
		}
		
		messageMap.put(KEY_MESSENGER_TAGVALUE_GROUP, tagValueGroup);
//		debug.trace("messageMap_"+messageMap);//しばらくin-outのテスト用にとっておこう。
		return messageMap;
	}
	
	

	

	/**
	 * 名称取得
	 * @return
	 */
	public String getName () {
		return messengerName;
	}
	
	
	/**
	 * ID取得
	 * @return
	 */
	public String getID () {
		return messengerID;
	}
	
	
	
	/**
	 * invocation元、実行者の取得
	 * @return
	 */
	private Object getInvokeObject() {
//		debug.trace("invokator_"+invokeObject);
		return invokeObject;
	}
	
	



	/**
	 * integer
	 * 
	 * タグバリュー型のJSONObjectを生成する
	 * @param key
	 * @param value
	 * @return
	 */
	public JSONObject tagValue(String key, int value) {
		JSONObject intObj = new JSONObject();
		intObj.put(key, new JSONNumber(value));
		return intObj;
	}



	/**
	 * double
	 * 
	 * タグバリュー型のJSONObjectを生成する
	 * @param key
	 * @param value
	 * @return
	 */
	public JSONObject tagValue(String key, double value) {
		JSONObject doubleObj = new JSONObject();
		doubleObj.put(key, new JSONNumber(value));
		return doubleObj;
	}



	/**
	 * String
	 * 
	 * タグバリュー型のJSONObjectを生成する
	 * @param key
	 * @param value
	 * @return
	 */
	public JSONObject tagValue(String key, String value) {
		JSONObject stringObj = new JSONObject();
		stringObj.put(key, new JSONString(value));
		return stringObj;
	}
	
	


	/**
	 * JSONObject-Array
	 * 
	 * タグバリュー型のJSONObjectを生成する
	 * @param key
	 * @param value
	 * @return
	 */
	public JSONObject tagValue(String key, JSONObject [] value) {
		JSONObject arrayObj = new JSONObject();
		JSONArray array = new JSONArray();
		
		int i = 0;
		for (JSONObject currentValue:value) {
			array.set(i++, currentValue);
		}
		arrayObj.put(key, array);
		
		return arrayObj;
	}
	


	/**
	 * JSONObject
	 * 
	 * タグバリュー型のJSONObjectを生成する
	 * @param key
	 * @param value
	 * @return
	 */
	public JSONObject tagValue(String key, JSONObject value) {
		JSONObject jsonObj = new JSONObject();
		jsonObj.put(key, value);
		return jsonObj;
	}
	
	/**
	 * JSONArray
	 * 
	 * タグバリュー型のJSONObjectを生成する
	 * @param key
	 * @param value
	 * @return
	 */
	public JSONObject tagValue(String key, JSONArray value) {
		JSONObject jsonObj = new JSONObject();
		jsonObj.put(key, value);
		return jsonObj;
	}


	
	



	/**
	 * 送信ログをセットする
	 * 
	 * @param receiverName
	 * @param command
	 * @param tagValue
	 */
	public void addSendLog(String receiverName, String command,
			JSONObject ... tagValue) {
		JSONObject logMap = getMessageStructure(getName(), getID(), receiverName, command, tagValue);
		
		sendList.add(logMap);		
	}
	

	/**
	 * 送信ログを差し出す
	 * @return
	 */
	public String getSendLog(int i) {
		debug.assertTrue(sendList != null, "sendList == null");
		debug.assertTrue(i < sendList.size(), "oversize of sendList");
		return sendList.get(i).toString();
	}
	
	
	/**
	 * 送信ログのサイズを取得する
	 * @return
	 */
	public int getSendLogSize() {
		return sendList.size();
	}

	
	

	/**
	 * 受け取りログに内容を加える
	 * @param receiverName
	 * @param command
	 */
	private void addReceiveLog(String senderName, String senderID, String toName, String command, JSONObject ... tagValue) {

		JSONObject logMap = getMessageStructure(senderName, senderID, toName, command, tagValue);
		
		receiveList.add(logMap);
	}

	
	/**
	 * 受け取りログを差し出す
	 * @param i
	 * @return
	 */
	public String getReceiveLog(int i) {
		debug.assertTrue(receiveList != null, "receiveList not yet initialize");
		return receiveList.get(i).toString();
	}
	
	
	/**
	 * 受け取りログのサイズ取得
	 * @return
	 */
	public int getReceiveLogSize() {
		return receiveList.size();
	}
	
	
	/**
	 * メッセージから、メッセージの送信者名を取得する
	 * @param message
	 * @return
	 */
	public String getSenderName(String message) {
		return JSONParser.parseStrict(message).isObject().get(KEY_MESSENGER_NAME).isString().stringValue();
	}

	/**
	 * メッセージから、メッセージの送信者のIDを取得する
	 * @param message
	 * @return
	 */
	public String getSenderID(String message) {
		return JSONParser.parseStrict(message).isObject().get(KEY_MESSENGER_ID).isString().stringValue();
	}
	
	/**
	 * メッセージから、メッセージコマンドを取得する
	 * @param message
	 * @return
	 */
	public String getCommand(String message) {
		return JSONParser.parseStrict(message).isObject().get(KEY_MESSENGER_EXEC).isString().stringValue();
	}

	/**
	 * バリューをタグから取得する
	 * 存在しない場合アサーションエラー
	 * @param message
	 * @param tag
	 * @return
	 */
	public JSONValue getValueForTag(String tag, String message) {
		JSONObject obj = JSONParser.parseStrict(message).isObject().get(KEY_MESSENGER_TAGVALUE_GROUP).isObject();
		debug.assertTrue(obj.containsKey(tag), "no-	" + tag + "	-contains");
		
		return obj.get(tag);
	}
	
	/**
	 * tagValueグループに含まれるtagをリストとして取得する
	 * @param message
	 * @return
	 */
	public ArrayList<String> getTags(String message) {
		JSONObject obj = JSONParser.parseStrict(message).isObject().get(KEY_MESSENGER_TAGVALUE_GROUP).isObject();
		
		ArrayList<String> tags = new ArrayList<String>();
		
		Set<String> currentSet = obj.keySet();
		
		for (Iterator<String> currentSetItel = currentSet.iterator(); currentSetItel.hasNext();) {
			tags.add(currentSetItel.next());
		}
		
		return tags;
	}

	/**
	 * tagValueグループに含まれるvalueをリストとして取得する
	 * @param message
	 * @return
	 */
	public ArrayList<JSONValue> getValues(String message) {
		JSONObject obj = JSONParser.parseStrict(message).isObject().get(KEY_MESSENGER_TAGVALUE_GROUP).isObject();
		
		ArrayList<JSONValue> values = new ArrayList<JSONValue>();
		
		Set<String> currentSet = obj.keySet();
		
		for (Iterator<String> currentSetItel = currentSet.iterator(); currentSetItel.hasNext();) {
			String currentKey = currentSetItel.next();
			values.add(obj.get(currentKey));
		}
		
		return values;
	}
	
	
	
	
	
	/**
	 * @return the messengerStatus
	 */
	public int getMessengerStatus() {
		return masterHub.getMessengerGlobalStatus();
	}


	


	/**
	 * 終了処理
	 */
	public void removeInvoke(Object root) {
		//setMessengerStatus(MESSENGER_STATUS_REMOVED);
		masterHub.destractInvocationClassNameList(root);
	}

	
	/**
	 * テスト中 now under testing.
	 * 
	 * 同期call
	 * tagValue無し
	 * @param receiverName
	 * @param command
	 */
	public int syncCall(String receiverName, String command) {
		/*
		 * イベントバスにロックかけてやれば出来る気がするが
		 * すなわちフレームレート的な1/fの世界をイベントハブに用意することになりそう。
		 * コードがそこで止まる、になるのかな。ヤバいな。
		 * でもこれがあると、遅延実行の正確な時間指定ができるんだよね。
		 * 
		 * ちがう。
		 * 送信前にMasterHubに問い合わせをして、宛先が存在する場合は送る、そうで無い場合はエラーを即座に返す必要が有る。
		 * かつ、送信するのはMessengerだ。つまり、
		 * 発信時刻を決定する責任と実行する責任はMessengerが負うことになる。
		 * あ、、先にEventBusにしよう。
		 */
//		while (true) {
//			boolean voo = false;
//			//送信完了が確認できるまで、ここで待つ話になる。 returnの論理を考えよう。
//			if (voo) break; 
//		}
		return -1;
	}
}
