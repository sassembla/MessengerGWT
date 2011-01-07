package com.kissaki.client.MessengerGWTCore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import com.kissaki.client.MessengerGWTCore.MessageCenter.MessageMasterHub;
import com.kissaki.client.MessengerGWTCore.MessageCenter.MessageReceivedEvent;
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
public class MessengerGWTImplement implements MessengerGWTInterface {
	
	static final String version = "0.5.0";//11/01/05 19:23:28
	
	
	public final JSONObject JSON_NULL = null;//メッセージ送信、可変長引数のラストにそのままnullて書くとエマージェンシー出るのを回避するためのコード
	
	
	Debug debug;
	public final String messengerName;
	public final String messengerID;
	public final Object invokeObject;
	
	public final String KEY_MESSENGER_NAME = "MESSENGER_messengerName";
	public final String KEY_MESSENGER_ID = "MESSENGER_messengerID";
	public final String KEY_TO_NAME = "MESSENGER_to";
	public final String KEY_MESSENGER_EXEC = "MESSENGER_exec";
	public final String KEY_MESSENGER_TAGVALUE_GROUP = "MESSENGER_tagValue"; 
	
	List <HashMap <String, String>> sendList = null;
	public List <HashMap <String, String>> receiveList = null;
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
		
		if (initializeCount == 0) setUpMessaging();
		initializeCount++;
		
		masterHub.setInvokeObject(getName(), getID(), invokeObject);
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
		setUpMessaging();
		masterHub.setInvokeObject(getName(), getID(), invokeObject);
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
	 * @param e
	 */
	public static void mtd(String e) {
		MessageMasterHub.get(e);//イベントを持っているオブジェクトを起動する
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
//		
//		//event;
//		
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
	

	
	public void log (String s) {
		debug.trace("log_"+s);
	}
	
	
	
	
	private native static void got (Event e) /*-{
		alert("here");
	}-*/;
	
	/**
	 * メッセージ受取メソッド
	 * @param event
	 */
	public void receiveMessage (MessageReceivedEvent event) {
		String rootMessage = event.getMessage();
		
		debug.trace("rootMessage_"+rootMessage);
		String message = "{\"Afooキーと\":\"fooバリューです\"}";
		
		JSONObject rootObject = null;
		String receiverName = null;
		String command = null;
		String tagValue = null;
		
		try {
			rootObject = JSONParser.parseStrict(message).isObject();
		} catch (Exception e) {
			debug.trace("receiveMessage_parseError_"+e);
		}
		
		if (rootObject == null) {
			debug.trace("rootObject = null");
			return;
		}
		
		receiverName = rootObject.get(KEY_MESSENGER_NAME).isString().toString();
		if (receiverName == null) return;
		if (!receiverName.equals(getName())) return;
		
		//宛先の名前と自分の名前が同じ
		
		command = rootObject.get(KEY_MESSENGER_EXEC).isString().toString();
		if (command == null) return;
		
		//コマンドも存在する
		
		//tagValue = rootObject.get(KEY_MESSENGER_TAGVALUE_GROUP).isString().toString();
		//if (tagValue == null) return;
		
		
		addReceiveLog(receiverName, command);
		receiveCenter(rootMessage);
	}
	
	
	/**
	 * 受信時に実行されるメソッド
	 */
	public void receiveCenter(String message) {
		((MessengerGWTInterface) getInvokeObject()).receiveCenter(message);
	}
	
	


	/**
	 * メッセージ送信メソッド
	 * @param receiverName
	 * @param command
	 * @param tagValue
	 */
	public void call(String receiverName, String command, JSONObject ... tagValue) {
		
		if (tagValue == null) {
			tagValue = new JSONObject [0];//長さ0の配列としてセット、中身は空
		}
		
		HashMap <String, String> messageMap = getMessageStructure(receiverName, command, tagValue);
		
		String href = Window.Location.getHref();
		postMessage(messageMap.toString(), href);
		
		addSendLog(receiverName, command, tagValue);//ログを残す
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
	private HashMap<String, String> getMessageStructure(String receiverName,
			String command, JSONObject[] tagValue) {
		HashMap<String, String> messageMap = new HashMap<String, String>();
		
		messageMap.put(KEY_MESSENGER_NAME, getName());
		messageMap.put(KEY_MESSENGER_ID, getID());
		messageMap.put(KEY_TO_NAME, receiverName);
		messageMap.put(KEY_MESSENGER_EXEC, command);
		
		JSONObject tagValueGroup = new JSONObject();
		
		int i = 0;
		for (JSONObject currentObject:tagValue) {
			for (Iterator<String> currentItel = currentObject.keySet().iterator(); currentItel.hasNext();) {
				String currentKey = currentItel.next();
				tagValueGroup.put(currentKey, currentObject.get(currentKey));//オブジェクトの移し替え
			}
			i++;
		}
		
		messageMap.put(KEY_MESSENGER_TAGVALUE_GROUP, tagValueGroup.toString());
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
	 * invocation取得
	 * @return
	 */
	private Object getInvokeObject() {
		debug.trace("invokator_"+invokeObject);
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
	 * 送信ログを差し出す
	 * @return
	 */
	public String getSendLog(int i) {
		debug.assertTrue(sendList != null, "sendList == null");
		debug.assertTrue(i < sendList.size(), "oversize of sendList");
		return sendList.get(i).toString();
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
		if (sendList == null) {
			sendList = new ArrayList<HashMap <String, String>>();
		}
		
		HashMap<String, String> logMap = getMessageStructure(receiverName, command, tagValue);
		
		sendList.add(logMap);		
	}

	
	

	
	private void addReceiveLog(String receiverName, String command) {
		if (receiveList == null) {
			receiveList = new ArrayList<HashMap<String,String>>();
		}
		
		HashMap<String, String> logMap = getMessageStructure(receiverName, command, new JSONObject[0]);
		
		receiveList.add(logMap);
	}

	
	/**
	 * 受け取りログを差し出す
	 * @param i
	 * @return
	 */
	public String getReceiveLog(int i) {
		return receiveList.get(i).toString();
	}
	
	
	
	
	
	
	
	
}
