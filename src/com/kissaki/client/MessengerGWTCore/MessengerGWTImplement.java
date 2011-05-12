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
import com.google.gwt.thirdparty.streamhtmlparser.util.EntityResolver.Status;
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
	
	static final String version = "0.7.5";//親子関係設定、子からの親登録を実装。MIDでの関係しばり、子から親へのcallParentのみ完了。 callMyselfのID縛り完成。 
//		"0.7.4";//カテゴリ判別のルールを追加、callMyselfでの限定を実装。 
//		"0.7.3";//親子関係の設定/取得機能を追加、まだ制限は無し 
//		"0.7.2";///callMyself追加
//		"0.7.1";//バグフィックスとか調整中
//		"0.7.0";//11/01/18 17:50:30 Beta release
//		"0.5.2";//11/01/18 16:41:28 changed to EventBus from HasHandlers(Duplicated) 
//		"0.5.1";//11/01/09 20:55:55 String-Value-Bug fixed.
//		"0.5.0";//11/01/05 19:23:28 Alpha release


	
	
	Debug debug;
	
	public final String messengerName;
	public final String messengerID;
	public final Object invokeObject;
	
	public String parentName;
	public String parentID;
	
	public final String KEY_MESSAGE_CATEGOLY = "KEY_MESSAGE_CATEGOLY";
	public final int MS_CATEGOLY_LOCAL = 0;
	public final int MS_CATEGOLY_CALLCHILD = 1;
	public final int MS_CATEGOLY_CALLPARENT = 2;
	public final int MS_CATEGOLY_PARENTSEARCH = 3;
	public final int MS_CATEGOLY_PARENTSEARCH_RET = 4;
	public final int MS_CATEGOLY_REMOVE_PARENT = 5;
	public final int MS_CATEGOLY_REMOVE_CHILD = 6;
	
	private final String KEY_MESSENGER_NAME	= "MESSENGER_messengerName";
	private final String KEY_MESSENGER_ID	= "MESSENGER_messengerID";
	private final String KEY_TO_NAME			= "MESSENGER_to";
	private final String KEY_TO_ID			= "MESSENGER_toID";
	private final String KEY_MESSENGER_EXEC	= "MESSENGER_exec";
	private final String KEY_MESSENGER_TAGVALUE_GROUP	= "MESSENGER_tagValue"; 
	private final String KEY_LOCK_BEFORE	= "MESSENGER_lock_b";
	private final String KEY_LOCK_AFTER		= "MESSENGER_lock_a";
	private final String KEY_PARENT_NAME	= "MESSENGER_pName";
	private final String KEY_PARENT_ID		= "MESSENGER_pID"; 
	
	
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
		
		parentName = "";
		parentID = "";

		debug.timeAssert("11/05/15 9:02:09", 10000, "わざわざシングルトン使ってる。Ginとか使って祖結合に切り替えたい");
		if (masterHub == null) {
			masterHub = MessageMasterHub.getMaster();
		}
		
		sendList = new ArrayList<JSONObject>();
		receiveList = new ArrayList<JSONObject>();
		
		if (masterHub.getMessengerGlobalStatus() == MESSENGER_STATUS_READY_FOR_INITIALIZE) {
			debug.trace("initialize");
			setUpMessaging(masterHub);
		}
		
		masterHub.setInvokeObject(invokeObject, this);
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
		debugs.timeAssert("11/05/05 9:02:09", 0, "なんでしたっけコレ");
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
//			false);
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
	 * @param event
	 */
	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		String rootMessage = event.getMessage();
		
		JSONObject rootObject = null;
		
		
		try {
			rootObject = JSONParser.parseStrict(rootMessage).isObject();
		} catch (Exception e) {
//			debug.trace("receiveMessage_parseError_"+e);
			return;
		}
		
		
		if (rootObject == null) {
//			debug.trace("rootObject = null");
			return;
		}
		
		
		String toName = null;
		{
			/*
			 * 宛先チェック
			 */
			debug.assertTrue(rootObject.get(KEY_TO_NAME).isString() != null, "invalid KEY_TO_NAME");
			toName = rootObject.get(KEY_TO_NAME).isString().stringValue();
			
			if (!toName.equals(getName())) {//送信者の指定した宛先が自分か
				//			NSLog(@"MS_CATEGOLY_CALLPARENT_宛先ではないMessnegerが受け取った");
				return;
			}
		}
		
		
		String fromName = null;
		String fromID = null;
		{
			/*
			 * 送付元名前チェック
			 */
			fromName = rootObject.get(KEY_MESSENGER_NAME).isString().stringValue();
			debug.assertTrue(fromName != null, "invalid KEY_MESSENGER_NAME");
			
			/*
			 * 送付元IDチェック
			 */
			
			fromID = rootObject.get(KEY_MESSENGER_ID).isString().stringValue();
			debug.assertTrue(fromID != null, "invalid KEY_MESSENGER_ID");
		}
		
		
		int categoly;
		{
			debug.assertTrue(rootObject.get(KEY_MESSAGE_CATEGOLY).isNumber() != null, "no KEY_MESSAGE_CATEGOLY");
			categoly = (int)rootObject.get(KEY_MESSAGE_CATEGOLY).isNumber().doubleValue();
		}
		
		
		/*
		 * コマンドチェック
		 */
		String command = null;
		{
			debug.assertTrue(rootObject.get(KEY_MESSENGER_EXEC).isString() != null, "KEY_MESSENGER_EXEC = null");
			command = rootObject.get(KEY_MESSENGER_EXEC).isString().stringValue();
		}
		
		JSONObject tagValue = null;
		{
			debug.assertTrue(rootObject.get(KEY_MESSENGER_TAGVALUE_GROUP).isObject() != null, "KEY_MESSENGER_TAGVALUE_GROUP = null");
			tagValue = rootObject.get(KEY_MESSENGER_TAGVALUE_GROUP).isObject();
		}
		
		
		
		switch (categoly) {
		case MS_CATEGOLY_LOCAL:
		{
			/*
			 * 宛先IDチェック
			 */
			String toID = null;
			toID = rootObject.get(KEY_TO_ID).isString().stringValue();
			if (toID.equals(getID())) {
				addReceiveLog(categoly, fromName, fromID, toName, command, tagValue);
				receiveCenter(rootMessage);
			}
		}
			return;
			
		case MS_CATEGOLY_CALLCHILD:
//			if ([senderName isEqualToString:[self getMyParentName]]) {//送信者が自分の親の場合のみ、処理を進める
//				
//				[self saveLogForReceived:recievedLogDict];
//				
//				
//				//設定されたbodyのメソッドを実行
//				IMP func = [[self getMyBodyID] methodForSelector:[self getMyBodySelector]];
//				(*func)([self getMyBodyID], [self getMyBodySelector], notification);
//				return;
//			}
			addReceiveLog(categoly, fromName, fromID, toName, command, tagValue);
			receiveCenter(rootMessage);
			return;
			
		case MS_CATEGOLY_CALLPARENT:
			debug.assertTrue(rootObject.get(KEY_TO_ID).isString() != null, "no KEY_TO_ID");
			String calledParentMSID = rootObject.get(KEY_TO_ID).isString().stringValue();
			
			//宛先MIDが自分のIDと一致するか
			if (calledParentMSID.equals(getID())) {

//				for (id key in [self getChildDict]) {//子供リストに含まれていなければ実行しないし、受け取らない。
//					if ([[[self getChildDict] objectForKey:key] isEqualToString:senderName]) {
//						[self saveLogForReceived:recievedLogDict];
//						
//						//設定されたbodyのメソッドを実行
//						IMP func = [[self getMyBodyID] methodForSelector:[self getMyBodySelector]];
//						(*func)([self getMyBodyID], [self getMyBodySelector], notification);
//						return;
//					}
//				}
				
				addReceiveLog(categoly, fromName, fromID, toName, command, tagValue);
				receiveCenter(rootMessage);
				
			}			

			
			
			return;
			
		case MS_CATEGOLY_PARENTSEARCH:
			
			debug.assertTrue(rootObject.get(KEY_PARENT_NAME).isString() != null, "no KEY_PARENT_NAME");
			String childSearchingName = rootObject.get(KEY_PARENT_NAME).isString().stringValue();
			
			if (childSearchingName.equals(getName())) {
				sendMessage(MS_CATEGOLY_PARENTSEARCH_RET, fromName, fromID);
			}
			addReceiveLog(categoly, fromName, fromID, toName, command, tagValue);
			return;
			
			
			
		case MS_CATEGOLY_PARENTSEARCH_RET:
		{
			String toID = null;
			toID = rootObject.get(KEY_TO_ID).isString().stringValue();
			
			if (toID.equals(getID())) {
				if (parentID.equals("")) {
					parentID = fromID;
					addReceiveLog(categoly, fromName, fromID, toName, command, tagValue);
				} else {
					//すでに親が居る旨のエラー、、
				}
				
			}
			
			return;
		}
			
			
		case MS_CATEGOLY_REMOVE_CHILD:
		case MS_CATEGOLY_REMOVE_PARENT:
		default:
			debug.assertTrue(false, "not ready yet or UNKNOWN CATEGOLY");
			return;
		}
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
	public JSONObject getMessageObjectPreview (int messageCategoly, String receiverName, String command, JSONObject ... tagValue) {
		return getMessageStructure(messageCategoly, getName(), getID(), receiverName, command, tagValue);
	}
	
	
	
	/**
	 * 非同期メッセージ送信メソッド
	 * @param receiverName
	 * @param command
	 * @param tagValue
	 */
	public void sendMessage(int messageCategoly, String receiverName, String command, JSONObject ... tagValue) {
		JSONObject messageMap = getMessageStructure(messageCategoly, getName(), getID(), receiverName, command, tagValue);
		
		debug.timeAssert("11/05/12 19:04:00", 100000, "sendMessage アドレスが変わったら使えない、張り直しなどの対策が必要なところ");
		
		String href = Window.Location.getHref();
		postMessage(messageMap.toString(), href);
		
		addSendLog(messageCategoly, receiverName, command, tagValue);//ログを残す
	}
	
	/**
	 * 非同期メッセージを子供へと送信するメソッド
	 * 子供へのメッセージング
	 * @param toName
	 * @param command
	 * @param tagValue
	 */
	public void call(String toName, String command, JSONObject ... tagValue) {
		sendMessage(MS_CATEGOLY_CALLCHILD, toName, command, tagValue);
	}

	/**
	 * 非同期メッセージを自分へと送信するメソッド
	 * 自分へのメッセージング
	 * @param command
	 * @param tagValue
	 */
	public void callMyself(String command, JSONObject ... tagValue) {
		sendMessage(MS_CATEGOLY_LOCAL, getName(), command, tagValue);
	}
	
	/**
	 * 非同期メッセージを親へと送信するメソッド
	 * 親へのメッセージング
	 * @param command
	 * @param tagValue
	 */
	public void callParent(String command, JSONObject ... tagValue) {
		debug.assertTrue(parentID!="", "parentID yet applied");
		sendMessage(MS_CATEGOLY_CALLPARENT, getParentName(), command, tagValue);
	}
	

	
	
	
	
	
	
	
	
	/**
	 * 入力されたメッセージを元に、宛先とコマンドを変更したものを返す
	 * 
	 * @deprecated 0.7.4 親子関係を組み込むと、callの部分がmanualに成る為、使用不可とする。
	 * 
	 * @param receiverName
	 * @param command
	 * @param eventString
	 * @return
	 */
	public String copyOut(int messageCategoly, String newReceiverName, String newCommand, String eventString) {
		//内容チェックを行い、receiverとcommandを書き換える
		debug.assertTrue(newReceiverName != null, "newReceiverName = null");
		debug.assertTrue(newCommand != null, "newCommand = null");
		debug.assertTrue(eventString != null, "eventString = null");
		
		JSONObject eventObj = JSONParser.parseStrict(eventString).isObject();
		debug.assertTrue(eventObj.containsKey(KEY_MESSAGE_CATEGOLY), "not contain KEY_MESSAGE_CATEGOLY");
		debug.assertTrue(eventObj.containsKey(KEY_MESSENGER_NAME), "not contain KEY_MESSENGER_NAME");
		debug.assertTrue(eventObj.containsKey(KEY_MESSENGER_ID), "not contain KEY_MESSENGER_ID");
		debug.assertTrue(eventObj.containsKey(KEY_MESSENGER_EXEC), "not contain KEY_MESSENGER_EXEC");
		debug.assertTrue(eventObj.containsKey(KEY_TO_NAME), "not contain KEY_TO_NAME");
		debug.assertTrue(eventObj.containsKey(KEY_TO_ID), "not contain KEY_TO_ID");
		debug.assertTrue(eventObj.containsKey(KEY_MESSENGER_TAGVALUE_GROUP), "not contain KEY_MESSENGER_TAGVALUE_GROUP");
		
		//categolyの書き換えを行う
		
		return replaceSenderInformation(messageCategoly, getName(), getID(), newReceiverName, newCommand, eventObj).toString();
	}
	


	/**
	 * 送信者情報を特定のものに変更する
	 * @param name
	 * @param id
	 * @param newCommand 
	 * @param newReceiverName 
	 * @param eventObj
	 */
	private JSONObject replaceSenderInformation(int messageCategoly, String name, String id,
			String newReceiverName, String newCommand, JSONObject eventObj) {
		JSONObject newObject = new JSONObject();
		newObject.put(KEY_MESSAGE_CATEGOLY, new JSONNumber(messageCategoly));
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
			int messageCategoly,
			String senderName,
			String senderID,
			String receiverName,
			String command, JSONObject[] tagValue) {
		JSONObject messageMap = new JSONObject();
		
		messageMap.put(KEY_MESSENGER_NAME, new JSONString(senderName));
		messageMap.put(KEY_MESSENGER_ID, new JSONString(senderID));
		
		messageMap.put(KEY_MESSAGE_CATEGOLY, new JSONNumber(messageCategoly));
		
		switch (messageCategoly) {
		case MS_CATEGOLY_LOCAL:
			messageMap.put(KEY_TO_NAME, new JSONString(receiverName));
			messageMap.put(KEY_MESSENGER_EXEC, new JSONString(command));
			messageMap.put(KEY_TO_ID, new JSONString(senderID));
			
			break;
			
		case MS_CATEGOLY_CALLCHILD:
			messageMap.put(KEY_TO_NAME, new JSONString(receiverName));
			messageMap.put(KEY_MESSENGER_EXEC, new JSONString(command));
			
			debug.timeAssert("11/05/21 12:32:57", 0, "ブロードキャストのみに対応、個別の送付は未だ。");
			//このキーを、対象限定の場合は付ければ良い。
			messageMap.put(KEY_TO_ID, new JSONString(""));
			
			break;
			
		case MS_CATEGOLY_CALLPARENT:
			debug.assertTrue(parentID != null, "parentID not yet inputted");
			messageMap.put(KEY_TO_ID, new JSONString(parentID));
			
			
			messageMap.put(KEY_TO_NAME, new JSONString(receiverName));
			messageMap.put(KEY_MESSENGER_EXEC, new JSONString(command));
			
			break;
			
		case MS_CATEGOLY_PARENTSEARCH:
			messageMap.put(KEY_TO_NAME, new JSONString(receiverName));
			messageMap.put(KEY_MESSENGER_EXEC, new JSONString(command));
			
			messageMap.put(KEY_PARENT_NAME, new JSONString(receiverName));
			break;
			
		case MS_CATEGOLY_PARENTSEARCH_RET:
			messageMap.put(KEY_PARENT_NAME, new JSONString(getName()));
			messageMap.put(KEY_PARENT_ID, new JSONString(getID()));
			
			messageMap.put(KEY_TO_NAME, new JSONString(receiverName));
			messageMap.put(KEY_TO_ID, new JSONString(command));

			messageMap.put(KEY_MESSENGER_EXEC, new JSONString(command));
			
			break;
			
		case MS_CATEGOLY_REMOVE_CHILD:
		case MS_CATEGOLY_REMOVE_PARENT:
		default:
			debug.assertTrue(false, "not ready yet");
			break;
		}

		
		JSONObject tagValueGroup = new JSONObject();
		
		for (JSONObject currentObject:tagValue) {
			for (Iterator<String> currentItel = currentObject.keySet().iterator(); currentItel.hasNext();) {
				String currentKey = currentItel.next();
				tagValueGroup.put(currentKey, currentObject.get(currentKey));//オブジェクトの移し替え
			}
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


	
	//Lock系の実装

//	public JSONObject withLockBefore(String ... key_lockValues) {
//		JSONObject singleLockObject = new JSONObject();
//		
//		
//		
//		int i = 0;
//		for (String currentObject:key_lockValues) {
//			
//		}
//		singleLockObject.put(keyName, new JSONString(lockValue));
//		
//		//[NSDictionary dictionaryWithObject:multiLockArray forKey:MS_LOCK_AFTER];
//		return tagValue(KEY_LOCK_BEFORE, singleLockObject);
//	}
//
//	public JSONObject withLockAfter(String lockValue, String keyName) {
//		JSONObject singleLockObject = new JSONObject();
//		singleLockObject.put(keyName, new JSONString(lockValue));
//		
//		return tagValue(KEY_LOCK_AFTER, singleLockObject);
//	}
	
	
	



	/**
	 * 送信ログをセットする
	 * 
	 * @param receiverName
	 * @param command
	 * @param tagValue
	 */
	public void addSendLog(int messageCategoly, String receiverName, String command,
			JSONObject ... tagValue) {
		JSONObject logMap = getMessageStructure(messageCategoly, getName(), getID(), receiverName, command, tagValue);
		
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
	private void addReceiveLog(int messageCategoly, String senderName, String senderID, String toName, String command, JSONObject ... tagValue) {

		JSONObject logMap = getMessageStructure(messageCategoly, senderName, senderID, toName, command, tagValue);
		
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
	 * 親子関係の構築を行う
	 * @param input
	 */
	public void inputParent(String input) {
		debug.assertTrue(parentName.equals(""), "already have parentName	すでに先約があるようです");
		debug.assertTrue(parentID.equals(""), "already have parentID	すでに先約があるようです");
		
		debug.assertTrue(!input.equals(""), "空文字は親の名称として指定できません");
		
		parentName = input;
		sendMessage(MS_CATEGOLY_PARENTSEARCH, input, "");
		
	}

	/**
	 * この名称のMessengerのIDを探し、取得する
	 * @param input
	 * @return
	 */
	private void getParentID(String input) {
		
	}


	/**
	 * 入力されている親の名前を取得する
	 * @return
	 */
	public String getParentName() {
		debug.assertTrue(!parentName.equals(""), "まだ親のinputが行われていません");
		return parentName;
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
