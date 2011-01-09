package com.kissaki.client;

import java.util.ArrayList;

import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.junit.client.GWTTestCase;
import com.google.gwt.user.client.Event;
import com.kissaki.client.MessengerGWTCore.MessengerGWTImplement;
import com.kissaki.client.MessengerGWTCore.MessengerGWTInterface;
import com.kissaki.client.MessengerGWTCore.MessageCenter.MessageReceivedEvent;
import com.kissaki.client.subFrame.debug.Debug;


public class MessengerGWTImplementTest extends GWTTestCase implements MessengerGWTInterface {
	Debug debug;
	
	MessengerGWTImplement messenger;
	String TEST_MYNAME = "sender";
	String TEST_COMMAND = "testCommand";
	String TEST_RECEIVER = "receiver";
	String TEST_ANOTHERONE = "another";
	
	String TEST_TAG = "tag1";
	String TEST_VALUE = "value1";
	
	boolean NOT_FOR_LOCAL = true;
	
	/**
	 * コンストラクタ
	 */
	public MessengerGWTImplementTest () {
		debug = new Debug(this);
		debug.trace("constructed");
	}
	
	
	/**
	 * Must refer to a valid module that sources this class.
	 */
	public String getModuleName() {
		return "com.kissaki.MessengerGWT";//パッケージの中で、クライアント/サーバの前+プロジェクトプロジェクト名称(xmlでの読み出しが行われている箇所)
	}
	
	
	/**
	 * セットアップ
	 */
	public void gwtSetUp () {
		debug.trace("setup");
		messenger = new MessengerGWTImplement(TEST_MYNAME, this);
	}
	
	
	/**
	 * ティアダウン
	 */
	public void gwtTearDown () {
		messenger.removeInvoke(this);
		messenger = null;
		debug.trace("teardown");
	}
	
	/*
	 * 仮のレシーバー
	 */
	
	ReceiverClass rec;
	private void setReceiver () {
		 rec = new ReceiverClass();
	}
	
	/**
	 * 上書きしたメソッド、メッセージ受信時に呼ばれる筈。
	 */
	@Override
	public void receiveCenter(String message) {
		
	}
	
	
	/**
	 * 送信者の名前チェック
	 */
	public void testGetName() {
		String name = messenger.getName();
		assertEquals(TEST_MYNAME, name);
	}
	
	
	
	/**
	 * tagValueメソッドの生成物テスト
	 */
	public void testCreateTagValue () {
		JSONObject string = new JSONObject();
		string.put("string", new JSONString("a"));
		
		JSONObject number = new JSONObject();
		number.put("number", new JSONNumber(100));
		
		
		JSONObject array [] = new JSONObject [] {
				string,//string:a
				number,//number:100
		};
		
		JSONObject obj = new JSONObject();
		obj.put("valOf5", new JSONNumber(5));
			
		
		JSONObject sInt = messenger.tagValue("キーInt", 1);//JSONNumber
		assertEquals("{\"キーInt\":1}", sInt.toString());
		
		
		JSONObject sDouble = messenger.tagValue("キーDouble", 2.222);//JSONNumber
		assertEquals("{\"キーDouble\":2.222}", sDouble.toString());
		
		
		JSONObject sString = messenger.tagValue("キーString", "val3");//JSONString
		assertEquals("{\"キーString\":\"val3\"}", sString.toString());
		
		
		JSONObject sArray = messenger.tagValue("キーArray", array);//JSONArray
		assertEquals("{\"キーArray\":[{\"string\":\"a\"},{\"number\":100}]}", sArray.toString());
	
		
		JSONObject sObj = messenger.tagValue("キーObj", obj);//JSONObject
		assertEquals("{\"キーObj\":{\"valOf5\":5}}", sObj.toString());
	}
	
	
	/**
	 * 復号テスト
	 * ""付きのコードを格納した際、ちゃんと復号され、再度変更できるのか。
	 */
	public void testEncode () {
		
		
		JSONObject v = new JSONObject();
		v.put("a", new JSONString("A"));
		String test = v.toString();
		
		JSONObject t = JSONParser.parseStrict(test).isObject();
		String decoded_A = t.get("a").isString().stringValue();
		
		assertEquals("A", decoded_A);
		
		JSONObject root1 = messenger.getMessageObjectPreview(TEST_MYNAME, TEST_COMMAND, messenger.tagValue(TEST_TAG, "String"));
		String s1 = messenger.getValueForTag(root1.toString(), TEST_TAG).isString().stringValue();
		
		JSONObject root2 = messenger.getMessageObjectPreview(TEST_MYNAME, TEST_COMMAND, messenger.tagValue(TEST_TAG, s1));
		String s2 = messenger.getValueForTag(root2.toString(), TEST_TAG).isString().stringValue();
			
		assertEquals(s1, s2);
	}

	
	/**
	 * 一つのタグがある場合の出力テスト
	 * 
	 */
	public void testSingleTagInput () {
		if (NOT_FOR_LOCAL) return;
		messenger.call(TEST_RECEIVER, TEST_COMMAND, //この時点でロックして、ただしテストはteardownする。
			messenger.tagValue(TEST_TAG, "1")
		);
		String s = messenger.getSendLog(0);
		String expect = "{MESSENGER_exec=TestCommand, MESSENGER_messengerID=CDDED3E3, MESSENGER_tagValue={\"キー1\":\"1\"}, MESSENGER_messengerName=sender, MESSENGER_to=receiver}";
		assertEquals(expect, s);
	}
	
	
	
	
	/**
	 * 複数のタグがある場合の出力テスト
	 * 
	 */
	public void testMultiTagInput () {
		if (NOT_FOR_LOCAL) return;
		messenger.call(TEST_RECEIVER, TEST_COMMAND, 
			messenger.tagValue(TEST_TAG, 1),//JSONNumber
			messenger.tagValue("キー2", 2),//JSONNumber
			messenger.tagValue("キー3", 3),//JSONNumber
			messenger.tagValue("キー4", 4)//JSONNumber
		);
		
		String expected = "{MESSENGER_exec=TestCommand, MESSENGER_messengerID=CDDED3E3, MESSENGER_tagValue={\""+ TEST_TAG +"\":1, \"キー2\":2, \"キー3\":3, \"キー4\":4}, MESSENGER_messengerName=sender, MESSENGER_to=receiver}";
		String actual = messenger.getSendLog(0);
		debug.trace("actual_"+actual);
		assertEquals(expected, actual);
	}
	
	
	
	/**
	 * ログのテスト
	 * 通信発進時に、内容をログに残す
	 * 発信順に保存する。
	 */
	public void testCreateLog () {
		
		messenger.addSendLog(TEST_RECEIVER, TEST_COMMAND, 
				messenger.tagValue(TEST_TAG, 1),//JSONNumber
				messenger.tagValue("キー2", 2),//JSONNumber
				messenger.tagValue("キー3", 3),//JSONNumber
				messenger.tagValue("キー4", 4)//JSONNumber
				);
		
		String log = messenger.getSendLog(0);
		debug.trace("log_"+log);
		
		
	}
	
	
	
	
	/**
	 * Callのテスト
	 */
	public void testCreateCallMethod () {
		JSONObject string = new JSONObject();
		string.put("string", new JSONString("a"));
		
		JSONObject number = new JSONObject();
		number.put("number", new JSONNumber(100));
		
		
		JSONObject array [] = new JSONObject [] {
				string,//string:a
				number,//number:100
		};
		
		
		JSONObject obj = new JSONObject();
		obj.put("valOf5", new JSONNumber(5));
		
		if (NOT_FOR_LOCAL) return;
		messenger.call(TEST_RECEIVER, TEST_COMMAND,
			messenger.tagValue(TEST_TAG, 1),//JSONNumber
			messenger.tagValue("キー2", 2.222),//JSONNumber
			messenger.tagValue("キー3", "val3"),//JSONString
			messenger.tagValue("キー4", array),//JSONArray,,,
			messenger.tagValue("キー5", obj)//JSONObject
		);
		
		
		/*
		 * 該当のTag-Valueを含んでいるか
		 */
		String s1 = messenger.getSendLog(0);
		assertTrue(s1.contains("\""+ TEST_TAG +"\":1"));
		assertTrue(s1.contains("\"キー2\":2.222"));
		assertTrue(s1.contains("\"キー3\":\"val3\""));
		assertTrue(s1.contains("\"キー4\":[{\"string\":\"a\"},{\"number\":100}]"));
		assertTrue(s1.contains("\"キー5\":{\"valOf5\":5}}"));
		
	}
	
	
	/**
	 * 送信者のコマンド生成チェック
	 */
	public void testCommandCreate () {
		if (NOT_FOR_LOCAL) return;
		messenger.call(TEST_RECEIVER, TEST_COMMAND);
		
		//送信記録
		String s1 = messenger.getSendLog(0);
		assertTrue(s1.contains("testCommand"));
	}
	
	
	
	//仮想的に受信者がいる場合のテスト//////////////////////////////////////////////////////////
	/**
	 * 自分~自分へのメッセージ
	 */
	public void testAssumeReceiveLog () {
		
		String message = messenger.getMessageObjectPreview(TEST_MYNAME, TEST_COMMAND, messenger.tagValue(TEST_TAG, TEST_VALUE)).toString();
		MessageReceivedEvent event = new MessageReceivedEvent(message);
        
		messenger.onMessageReceived(event);
		String s = messenger.getReceiveLog(0);
		
		assertEquals(1, messenger.getReceiveLogSize());
	}
	
	
	/**
	 * 自分-自分宛のメッセージを取得したテスト
	 */
	public void testGetReceiveToMyself () {
		String message = messenger.getMessageObjectPreview(TEST_MYNAME, TEST_COMMAND, messenger.tagValue(TEST_TAG, TEST_VALUE)).toString();
		MessageReceivedEvent event = new MessageReceivedEvent(message);
       
		messenger.onMessageReceived(event);
		String s = messenger.getReceiveLog(0);
		
		assertEquals(1, messenger.getReceiveLogSize());
	}
	
	
	/**
	 * 他人宛のメッセージを取得していないテスト
	 */
	public void testNotReceiveTheMessageForAnyone () {
		String message = messenger.getMessageObjectPreview(TEST_RECEIVER, TEST_COMMAND, messenger.tagValue(TEST_TAG, TEST_VALUE)).toString();
		MessageReceivedEvent event = new MessageReceivedEvent(message);
       
		messenger.onMessageReceived(event);
		
		assertEquals(0, messenger.getReceiveLogSize());
	}
	
	
	/**
	 * 他人-他人間のメッセージを取得していないテスト
	 */
	public void testNotConcernSomeoneToSomeselfMessageNotReceive () {
		setReceiver();
		String message = rec.getMessengerForTesting().getMessageObjectPreview(TEST_RECEIVER, TEST_COMMAND, rec.getMessengerForTesting().tagValue(TEST_TAG, TEST_VALUE)).toString();
		MessageReceivedEvent event = new MessageReceivedEvent(message);
		
		messenger.onMessageReceived(event);
		assertEquals(0, messenger.getReceiveLogSize());
	}
	
	/**
	 * 他人-第三者間のメッセージを取得していないテスト
	 */
	public void testNotConcernSomeoneToSomeoneMessageNotReceive () {
		setReceiver();
		String message = rec.getMessengerForTesting().getMessageObjectPreview(TEST_ANOTHERONE, TEST_COMMAND, rec.getMessengerForTesting().tagValue(TEST_TAG, TEST_VALUE)).toString();
		MessageReceivedEvent event = new MessageReceivedEvent(message);
		
		messenger.onMessageReceived(event);
		assertEquals(0, messenger.getReceiveLogSize());
	}
	
	/**
	 * 他人-自分宛のメッセージを受け取るテスト
	 */
	public void testGetMessageSomeoneToMe () {
		setReceiver();
		String message = rec.getMessengerForTesting().getMessageObjectPreview(TEST_MYNAME, TEST_COMMAND, rec.getMessengerForTesting().tagValue(TEST_TAG, TEST_VALUE)).toString();
		MessageReceivedEvent event = new MessageReceivedEvent(message);
		
		messenger.onMessageReceived(event);
		assertEquals(1, messenger.getReceiveLogSize());
	}
	
	/**
	 * 受信したメッセージの送信者名を取得するメソッドの内容チェック
	 */
	public void testCheckSenderName () {
		setReceiver();
		String message = rec.getMessengerForTesting().getMessageObjectPreview(TEST_MYNAME, TEST_COMMAND, rec.getMessengerForTesting().tagValue(TEST_TAG, TEST_VALUE)).toString();
		debug.trace("name_message_"+message);
		MessageReceivedEvent event = new MessageReceivedEvent(message);
		
		messenger.onMessageReceived(event);
		
		assertEquals(1, messenger.getReceiveLogSize());
		
		String s = messenger.getReceiveLog(0);
		
		String senderName = messenger.getSenderName(s);
		assertEquals(rec.getMessengerForTesting().getName(), senderName);
	}
	
	
	/**
	 * 受信したメッセージの送信者IDを取得するメソッドの内容チェック
	 */
	public void testChechSenderID () {
		setReceiver();
		String message = rec.getMessengerForTesting().getMessageObjectPreview(TEST_MYNAME, TEST_COMMAND, rec.getMessengerForTesting().tagValue(TEST_TAG, TEST_VALUE)).toString();
		debug.trace("id_message_"+message);
		MessageReceivedEvent event = new MessageReceivedEvent(message);
		
		messenger.onMessageReceived(event);
		
		assertEquals(1, messenger.getReceiveLogSize());
		
		String s = messenger.getReceiveLog(0);
		debug.trace("id_s_"+s);
		String senderID = messenger.getSenderID(s);
		
		assertEquals(rec.getMessengerForTesting().getID(), senderID);
	}
	
	
	
	/**
	 * 受信したコマンドを分析するコマンドの内容チェック
	 */
	public void testCheckReceivedCommand () {
		setReceiver();
		String message = rec.getMessengerForTesting().getMessageObjectPreview(TEST_MYNAME, TEST_COMMAND, rec.getMessengerForTesting().tagValue(TEST_TAG, TEST_VALUE)).toString();
		MessageReceivedEvent event = new MessageReceivedEvent(message);
		
		messenger.onMessageReceived(event);
		String s = messenger.getReceiveLog(0);
		
		String command = messenger.getCommand(s);
		assertEquals(TEST_COMMAND, command);
	}
	
	
	/**
	 * 受信したtagValueの内容を分析するテスト
	 */
	public void testCheckTagValue () {
		setReceiver();
		String message = rec.getMessengerForTesting().getMessageObjectPreview(TEST_MYNAME, TEST_COMMAND, rec.getMessengerForTesting().tagValue(TEST_TAG, TEST_VALUE)).toString();
		MessageReceivedEvent event = new MessageReceivedEvent(message);
		
		messenger.onMessageReceived(event);
		String s = messenger.getReceiveLog(0);
		String tagValue1 = messenger.getValueForTag(s, TEST_TAG).isString().stringValue();//isString, isObject, isArray, isNumber, isBoolean　とかが入るので、こんな感じ。
		assertEquals(TEST_VALUE, tagValue1);
	}
	
	
	/**
	 * タグ集をゲットするメソッドの内容テスト
	 */
	public void testCheckGetTags () {
		setReceiver();
		String message = rec.getMessengerForTesting().getMessageObjectPreview(TEST_MYNAME, TEST_COMMAND, rec.getMessengerForTesting().tagValue(TEST_TAG, TEST_VALUE)).toString();
		MessageReceivedEvent event = new MessageReceivedEvent(message);
		
		messenger.onMessageReceived(event);
		String s = messenger.getReceiveLog(0);
		
		ArrayList<String> tags = messenger.getTags(s);
		assertEquals(1, tags.size());
		assertEquals(TEST_TAG, tags.get(0));
	}
	
	/**
	 * バリュー集をゲットするメソッドの内容テスト
	 */
	public void testCheckGetValues () {
		setReceiver();
		String message = rec.getMessengerForTesting().getMessageObjectPreview(TEST_MYNAME, TEST_COMMAND, rec.getMessengerForTesting().tagValue(TEST_TAG, TEST_VALUE)).toString();
		MessageReceivedEvent event = new MessageReceivedEvent(message);
		
		messenger.onMessageReceived(event);
		String s = messenger.getReceiveLog(0);
		
		ArrayList<JSONValue> values = messenger.getValues(s);
		
		assertEquals(1, values.size());
		assertEquals(TEST_VALUE, values.get(0).isString().stringValue());
	}
	
	
	//実際に受信者がいる場合のテスト//////////////////////////////////////////////////////////
	
	
	/**
	 * 受信者のログが作成されたかどうかをチェックする
	 */
	public void testReceivedLogCreate () {
		if (NOT_FOR_LOCAL) return;
		setReceiver();
		messenger.call(TEST_RECEIVER, "testCommand");
		
		
		String s = rec.getMessengerForTesting().getReceiveLog(0);
		
	}
	
	
	/**
	 * 受信者の受信テスト
	 */
	public void testReceivedLogExist () {
		if (NOT_FOR_LOCAL) return;
		setReceiver();
		messenger.call(TEST_RECEIVER, "command");
		
		String s1 = rec.getMessengerForTesting().getReceiveLog(0);
		assertTrue(s1.contains("command"));
	}
	
	
	

	
	

	
	/*
	 * 親子関係がないので、どんなテストが必要か、列記する
	 * 
	 * 自体のメソッドテスト
	 * 
	 * 初期化
	 * 		自分の名称設定
	 * 		
	 * 
	 * 送信内容設定
	 *		送信コマンド設定
	 * 		送信元名設定
	 * 		送信先名設定
	 * 		
	 * 
	 * A-B間の送信
	 * 宛先へと届いているテスト
	 * 宛先へと届いている内容のテスト
	 * 		Bの受信コマンド設定
	 * 		Bの受信内容設定
	 * 
	 * 宛先でない場所に届いていないテスト
	 * テストログの充実
	 * 	→ログでチェックする事になると思う
	 * 
	 * 
	 */
	public class ReceiverClass implements MessengerGWTInterface {
		Debug debug;
		MessengerGWTImplement receiver;
		
		public ReceiverClass () {
			debug = new Debug(this);
			receiver = new MessengerGWTImplement(TEST_RECEIVER, this);
		}
		
		public MessengerGWTImplement getMessengerForTesting () {
			return receiver;
		}
		
		
		@Override
		public void receiveCenter(String message) {
			
		}
	}

	
	
}
