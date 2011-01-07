package com.kissaki.client.MessengerGWTCore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.http.client.URL;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import com.kissaki.client.MessengerGWTCore.MessageCenter.MessageMasterHub;
import com.kissaki.client.MessengerGWTCore.MessageCenter.MessageReceivedEvent;
import com.kissaki.client.MessengerGWTCore.MessageCenter.MessageReceivedEventHandler;
import com.kissaki.client.subFrame.debug.Debug;
import com.kissaki.client.uuidGenerator.UUID;


/**
 * MessengerGWTの実装
 * 
 * Obj-C版の実装から、親子関係を取り除いたバージョン
 * 
 * -クライアント間の通信：未作成
 * -クライアント-サーバ感の通信：未作成
 * 
 * @author ToruInoue
 */
public class MessengerGWTImplement implements MessageReceivedEventHandler, MessengerGWTInterface {
	
	static final String version = "0.5.0";//11/01/05 19:23:28
	
	
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
	static MessageMasterHub masterHub;
	
	static int initializeCount = 0;
	
	/**
	 * コンストラクタ
	 * メッセージの受信ハンドラを設定する
	 * @param string 
	 */
	public MessengerGWTImplement (String messengerName, Object invokeObject) {
		this.messengerName = messengerName;
		this.messengerID = UUID.uuid(8,16);
		this.invokeObject = invokeObject;
		masterHub = MessageMasterHub.getMaster();//new MessageMasterHub(getName(), getID(), invokeObject);
		debug = new Debug(this);
		
		sendList = new ArrayList<JSONObject>();
		receiveList = new ArrayList<JSONObject>();
		
		if (initializeCount == 0) setUpMessaging();
		initializeCount++;
		
		masterHub.setInvokeObject(getName(), getID(), this);
	}
	
	
	/**
	 * テスト用のコンストラクタ
	 * messengerIDを固定している
	 * @param messengerName
	 */
	public MessengerGWTImplement (String messengerName, Object invokeObject, int isTest) {
		this.messengerName = messengerName;
		this.messengerID = "CDDED3E3";//UUID.uuid(8,16);
		this.invokeObject = invokeObject;
		masterHub = MessageMasterHub.getMaster();//new MessageMasterHub(getName(), getID(), invokeObject);
		
		debug = new Debug(this);
		
		sendList = new ArrayList<JSONObject>();
		receiveList = new ArrayList<JSONObject>();
		
		if (initializeCount == 0) setUpMessaging();
		initializeCount++;
		
		masterHub.setInvokeObject(getName(), getID(), this);
	}
	
	
	
	/**
	 * JSNIへとmtdメソッドのJSオブジェクトを投入し、messageハンドラを初期化する。
	 */
	public void setUpMessaging () {
		setUp(get());
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
		MessageMasterHub.invokeReceive(message);//イベントを持っているオブジェクトを起動する
	}
	
	
	/**
	 * フィルタ構造
	 */
	public void messengerFilter() {
		debug.trace("messengerFilter_ここにいる");
	}
	
	
	/**
	 * セットアップ
	 * 
	 * Messengerの初期設定を行う
	 * Nativeのメッセージ受信部分
	 */
	private native void setUp(JavaScriptObject method) /*-{
		try {
			if (typeof window.postMessage === "undefined") { 
	    		alert("残念ですが、あなたのブラウザはメッセージAPIをサポートしていません。このアプリケーションは使えません");
	    		return;
			}
			
			window.addEventListener('message', func, false);
			
			function func (e) {
				method(e.data);
			}
		} catch (er) {
			alert("er_"+er);
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
	 * TODO イベントハンドラで呼ばれている。　イベントに登録したmessenger全てに送られているが、native実装があれば、そんなに頑張らないでいいはず。
	 * @param event
	 */
	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		String rootMessage = event.getMessage();
		
//		debug.trace("rootMessage_"+rootMessage);
		
		JSONObject rootObject = null;
		String toName = null;
		String command = null;
		JSONObject tagValue = null;
		
		try {
			rootObject = JSONParser.parseStrict(rootMessage).isObject();
		} catch (Exception e) {
			debug.trace("receiveMessage_parseError_"+e);
		}
		
		if (rootObject == null) {
			debug.trace("rootObject = null");
			return;
		}
		
		
		toName = removeDoubleQuatation(rootObject.get(KEY_TO_NAME).isString().toString());
		
		if (toName == null) {
			debug.trace("receiverName = null");
			return;
		}
		
		if (!toName.equals(getName())) {
			debug.trace("!receiverName_"+toName+" /vs/ "+getName());
			return;
		}
		
		//宛先の名前と自分の名前が同じ
		
		command = rootObject.get(KEY_MESSENGER_EXEC).isString().toString();
		if (command == null) {
			debug.trace("command = null");
			return;
		}
		
		//コマンドも存在する
		
		tagValue = rootObject.get(KEY_MESSENGER_TAGVALUE_GROUP).isObject();
		if (tagValue == null) {
			debug.trace("tagValue = null");
			return;
		}
		debug.trace("tagValue_"+tagValue);
		
		
		addReceiveLog(toName, command, tagValue);
		receiveCenter(rootMessage);
	}
	
	
	/**
	 * "文字削り
	 * @param input
	 * @return
	 */
	private String removeDoubleQuatation (String input) {
		return input.substring(1, input.length()-1);
	}
	
	
	/**
	 * 内部から外部への行使
	 */
	public void receiveCenter(String rootMessage) {
		((MessengerGWTInterface) getInvokeObject()).receiveCenter(rootMessage);
	}


		

	/**
	 * 送付前のメッセージをプレビュー取得するメソッド
	 * @param receiverName
	 * @param command
	 * @param tagValue
	 * @return
	 */
	public JSONObject getMessageObjectPreview (String receiverName, String command, JSONObject ... tagValue) {
		return getMessageStructure(receiverName, command, tagValue);
	}
	
	
	
	/**
	 * メッセージ送信メソッド
	 * @param receiverName
	 * @param command
	 * @param tagValue
	 */
	public void call(String receiverName, String command, JSONObject ... tagValue) {
		
		JSONObject messageMap = getMessageStructure(receiverName, command, tagValue);
		
		String href = Window.Location.getHref();//アドレスが変わったら使えない、張り直しなどの対策が必要なところ。
		postMessage(messageMap.toString(), href);
		
		addSendLog(receiverName, command, tagValue);//ログを残す
	}
	
	
	/**
	 * メッセージ送信メソッド、
	 * tagValueが無いバージョン
	 * @param receiverName
	 * @param command
	 */
	public void call(String receiverName, String command) {
		JSONObject [] tagValue = new JSONObject [0];//長さ0の配列としてセット、中身は空
		call(receiverName, command, tagValue);
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
	 * メッセージ構造を構築する
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
	private JSONObject getMessageStructure(String receiverName,
			String command, JSONObject[] tagValue) {
		JSONObject messageMap = new JSONObject();
		
		//HashMap<String, String> messageMap = new HashMap<String, String>();
		
		messageMap.put(KEY_MESSENGER_NAME, new JSONString(getName()));
		messageMap.put(KEY_MESSENGER_ID, new JSONString(getID()));
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
	 * Array
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
	 * 送信ログをセットする
	 * 
	 * @param receiverName
	 * @param command
	 * @param tagValue
	 */
	public void addSendLog(String receiverName, String command,
			JSONObject ... tagValue) {
		JSONObject logMap = getMessageStructure(receiverName, command, tagValue);
		
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
	private void addReceiveLog(String receiverName, String command, JSONObject ... tagValue) {
//		debug.trace("addReceiveLog_tagValue_"+tagValue);
		JSONObject logMap = getMessageStructure(receiverName, command, tagValue);
		
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
	 * TODO 未実装　終了処理
	 */
	public void removeInvoke() {
		//リスナから外す、、という処理が必要かなあ
	}


	
	
	
	
	
	
	
	
	
}
