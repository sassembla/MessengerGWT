package com.kissaki.client;


import java.util.List;

import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.junit.client.GWTTestCase;
import com.google.gwt.user.client.Timer;
import com.kissaki.client.MessengerGWTCore.MessengerGWTImplement;
import com.kissaki.client.MessengerGWTCore.MessengerGWTInterface;
import com.kissaki.client.MessengerGWTCore.MessageCenter.MessageMasterHub;
import com.kissaki.client.subFrame.debug.Debug;
import com.kissaki.client.uuidGenerator.UUID;
import com.sun.org.apache.bcel.internal.generic.NEW;


public class MessengerGWTImplementTest extends GWTTestCase implements MessengerGWTInterface {
	Debug debug;
	
	MessengerGWTImplement messenger;
	String TEST_MYNAME = "sender";
	String TEST_MYID = "testID";
	String TEST_PARENTNAME	= "TEST_PARENTNAME";
	String TEST_CHILDNAME	= "TEST_CHILDNAME";
	String TEST_PARENTNAME1	= "TEST_PARENTNAME1";
	String TEST_PARENTNAME2	= "TEST_PARENTNAME2";
	String TEST_ORPHANNAME	= "TEST_ORPHANNAME";
	String TEST_NOPARENTNAME	= "TEST_NOPARENTNAME";
	
	String TEST_COMMAND = "testCommand";
	String TEST_RECEIVER = "receiver";
	String TEST_ANOTHERONE = "another";
	
	String TEST_TAG = "tag1";
	String TEST_VALUE = "value1";
	
	
	String TEST_LOCKED_EXEC = "TEST_LOCKED_EXEC";
	String TEST_LOCK_EXEC	= "TEST_LOCK_EXEC";
	
	int INTERVAL_TIMEOUT_MS = 500;
	int INTERVAL_FPS = 10;
	
	
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
	
	MessageMasterHub currentMaster;
	
	/**
	 * セットアップ
	 */
	public void gwtSetUp () {
		debug.trace("setup_"+this);
		currentMaster = MessageMasterHub.getMaster();//この時点でマスターが存在すれば良い
		debug.trace("setup_2_"+currentMaster);
		messenger = new MessengerGWTImplement(TEST_MYNAME, this);
	}
	
	
	/**
	 * ティアダウン
	 */
	public void gwtTearDown () {
		currentMaster.resetAllInvocationSetting();
		currentMaster = null;
		
		rec.receiver = null;
		rec = null;
		messenger = null;
		debug.trace("teardown");
	}
	
	/*
	 * 仮のレシーバー
	 */
	ReceiverClass rec;
	private void setReceiver () {
		 rec = new ReceiverClass(TEST_RECEIVER);
	}
	
	/**
	 * メッセージ受信時に呼ばれるメソッド
	 */
	@Override
	public void receiveCenter(String message) {
		
	}
	
	public void testGetMessengerStatus () {
		int i = messenger.getMessengerStatus();
		debug.trace("status_"+i);
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
			
		JSONArray jArray = new JSONArray();
		jArray.set(0, new JSONString("array1"));
		jArray.set(1, new JSONString("array2"));
		
		
		JSONObject sInt = messenger.tagValue("キーInt", 1);//JSONNumber
		assertEquals("{\"キーInt\":1}", sInt.toString());
		
		
		JSONObject sDouble = messenger.tagValue("キーDouble", 2.222);//JSONNumber
		assertEquals("{\"キーDouble\":2.222}", sDouble.toString());
		
		
		JSONObject sString = messenger.tagValue("キーString", "val3");//JSONString
		assertEquals("{\"キーString\":\"val3\"}", sString.toString());
		
		
		JSONObject sArray = messenger.tagValue("キーArray", array);//JSONObject-Array
		assertEquals("{\"キーArray\":[{\"string\":\"a\"},{\"number\":100}]}", sArray.toString());
	
		
		JSONObject sObj = messenger.tagValue("キーObj", obj);//JSONObject
		assertEquals("{\"キーObj\":{\"valOf5\":5}}", sObj.toString());
		
		
		JSONObject jArrayObject = messenger.tagValue("キーjArray", jArray);//JSONArray
		assertEquals("{\"キーjArray\":[\"array1\",\"array2\"]}", jArrayObject.toString());
		
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
		
		JSONObject root1 = messenger.getMessageObjectPreview(0, TEST_MYNAME, TEST_MYID, TEST_COMMAND, messenger.tagValue(TEST_TAG, "String"));
		String s1 = messenger.getValueForTag(TEST_TAG, root1.toString()).isString().stringValue();
		
		JSONObject root2 = messenger.getMessageObjectPreview(0, TEST_MYNAME, TEST_MYID, TEST_COMMAND, messenger.tagValue(TEST_TAG, s1));
		String s2 = messenger.getValueForTag(TEST_TAG, root2.toString()).isString().stringValue();
			
		assertEquals(s1, s2);
	}

	
	/**
	 * 送信ログ上での内容一致テスト
	 */
	public void testSendInput () {
		if (messenger.getMessengerStatus() != MESSENGER_STATUS_OK) {
			debug.trace("test_abort_testSendInput_"+messenger.getMessengerStatus());
			assertEquals(MESSENGER_STATUS_NOT_SUPPORTED, messenger.getMessengerStatus());
			return;
		}
		setReceiver();
		rec.getMessengerForTesting().sInputParent(messenger.getName());
		
		messenger.call(TEST_RECEIVER, TEST_COMMAND,
			messenger.tagValue(TEST_TAG, "1")
		);
		
		
		String sendMessage = messenger.getSendLog(1);
		String actualCommand = messenger.getCommand(sendMessage);
		assertEquals(TEST_COMMAND, actualCommand);
		
		String actualSenderName = messenger.getSenderName(sendMessage);
		assertEquals(messenger.getName(), actualSenderName);
		
		String actualSenderID = messenger.getSenderID(sendMessage);
		assertEquals(messenger.getID(), actualSenderID);
		
		
	}
	
	/**
	 * 受信ログ上での内容一致テスト
	 */
	public void testReceiveInput () {
		if (messenger.getMessengerStatus() != MESSENGER_STATUS_OK) {
			debug.trace("test_abort_testReceiveInput_"+messenger.getMessengerStatus());
			assertEquals(MESSENGER_STATUS_NOT_SUPPORTED, messenger.getMessengerStatus());
			return;
		}
		
		setReceiver();
		rec.getMessengerForTesting().sInputParent(messenger.getName());
		
		
		messenger.call(TEST_RECEIVER, TEST_COMMAND,
			messenger.tagValue(TEST_TAG, "1")
		);
		
		delayTestFinish(INTERVAL_TIMEOUT_MS);
		
		Timer timer = new Timer() {
			@Override
			public void run() {
				if (1 < rec.getMessengerForTesting().getReceiveLogSize()) {
					cancel();
					
					//到達したら
					String receiveMessage = rec.getMessengerForTesting().getReceiveLog(1);
					String actualCommand = messenger.getCommand(receiveMessage);
					assertEquals(TEST_COMMAND, actualCommand);
					
					String actualSenderName = messenger.getSenderName(receiveMessage);
					assertEquals(messenger.getName(), actualSenderName);
					
					String actualSenderID = messenger.getSenderID(receiveMessage);
					assertEquals(messenger.getID(), actualSenderID);
					
					finishTest();
				}
			}
		};
		
		timer.scheduleRepeating(INTERVAL_FPS);
	}
	
	
	
	
	/**
	 * 一つのタグがある場合の送信ログ上での内容一致テスト
	 */
	public void testSendSingleTagInput () {
		if (messenger.getMessengerStatus() != MESSENGER_STATUS_OK) {
			debug.trace("test_abort_testSendSingleTagInput_"+messenger.getMessengerStatus());
			assertEquals(MESSENGER_STATUS_NOT_SUPPORTED, messenger.getMessengerStatus());
			return;
		}
		setReceiver();
		rec.getMessengerForTesting().sInputParent(messenger.getName());
		
		
		messenger.call(TEST_RECEIVER, TEST_COMMAND,
			messenger.tagValue(TEST_TAG, "1")
		);
		
		
		String sendMessage = messenger.getSendLog(1);
		String actualCommand = messenger.getCommand(sendMessage);
		assertEquals(TEST_COMMAND, actualCommand);
		
		String actualSenderName = messenger.getSenderName(sendMessage);
		assertEquals(messenger.getName(), actualSenderName);
		
		String actualSenderID = messenger.getSenderID(sendMessage);
		assertEquals(messenger.getID(), actualSenderID);
		
		JSONValue actualTagValue = messenger.getValueForTag(TEST_TAG, sendMessage);
		assertTrue(actualTagValue.isString() != null);
		assertEquals("1", actualTagValue.isString().stringValue());
		
	}
	
	/**
	 * 一つのタグがある場合の受信ログ上での内容一致テスト
	 */
	public void testReceiveSingleTagInput () {
		if (messenger.getMessengerStatus() != MESSENGER_STATUS_OK) {
			debug.trace("test_abort_testReceiveSingleTagInput_"+messenger.getMessengerStatus());
			assertEquals(MESSENGER_STATUS_NOT_SUPPORTED, messenger.getMessengerStatus());
			return;
		}
		
		setReceiver();
		rec.getMessengerForTesting().sInputParent(messenger.getName());
		
		
		messenger.call(TEST_RECEIVER, TEST_COMMAND,
			messenger.tagValue(TEST_TAG, "1")
		);
		
		delayTestFinish(INTERVAL_TIMEOUT_MS);
		
		Timer timer = new Timer() {
			@Override
			public void run() {
				if (1 < rec.getMessengerForTesting().getReceiveLogSize()) {
					cancel();
					
					//到達したら
					String receiveMessage = rec.getMessengerForTesting().getReceiveLog(1);
					String actualCommand = messenger.getCommand(receiveMessage);
					assertEquals(TEST_COMMAND, actualCommand);
					
					String actualSenderName = messenger.getSenderName(receiveMessage);
					assertEquals(messenger.getName(), actualSenderName);
					
					String actualSenderID = messenger.getSenderID(receiveMessage);
					assertEquals(messenger.getID(), actualSenderID);
					
					JSONValue actualTagValue = messenger.getValueForTag(TEST_TAG, receiveMessage);
					assertTrue(actualTagValue.isString() != null);
					assertEquals("1", actualTagValue.isString().stringValue());
					
					finishTest();
				}
			}
		};
		
		timer.scheduleRepeating(INTERVAL_FPS);
		
	}
	
	
	
	
	/**
	 * 複数のタグがある場合の送信ログ上での内容一致テスト
	 */
	public void testSendMultiTagInput () {
		if (messenger.getMessengerStatus() != MESSENGER_STATUS_OK) {
			debug.trace("test_abort_testSendMultiTagInput_"+messenger.getMessengerStatus());
			assertEquals(MESSENGER_STATUS_NOT_SUPPORTED, messenger.getMessengerStatus());
			
			return;
		}
		setReceiver();
		rec.getMessengerForTesting().sInputParent(messenger.getName());
		
		
		messenger.call(TEST_RECEIVER, TEST_COMMAND, 
			messenger.tagValue(TEST_TAG, 1),//JSONNumber
			messenger.tagValue("キー2", 2),//JSONNumber
			messenger.tagValue("キー3", 3),//JSONNumber
			messenger.tagValue("キー4", 4)//JSONNumber
		);
		
		String sendMessage = messenger.getSendLog(1);
		
		JSONValue actualTagValue1 = messenger.getValueForTag(TEST_TAG, sendMessage);
		assertTrue(actualTagValue1.isNumber() != null);
		assertEquals(1, (int)actualTagValue1.isNumber().doubleValue());
		
		JSONValue actualTagValue2 = messenger.getValueForTag("キー2", sendMessage);
		assertTrue(actualTagValue2.isNumber() != null);
		assertEquals(2, (int)actualTagValue2.isNumber().doubleValue());
		
		JSONValue actualTagValue3 = messenger.getValueForTag("キー3", sendMessage);
		assertTrue(actualTagValue3.isNumber() != null);
		assertEquals(3, (int)actualTagValue3.isNumber().doubleValue());
		
		JSONValue actualTagValue4 = messenger.getValueForTag("キー4", sendMessage);
		assertTrue(actualTagValue4.isNumber() != null);
		assertEquals(4, (int)actualTagValue4.isNumber().doubleValue());
		
	}
	
	/**
	 * 複数のタグがある場合の受信ログ上での内容一致テスト
	 */
	public void testReceiveMultiTagInput () {
		if (messenger.getMessengerStatus() != MESSENGER_STATUS_OK) {
			debug.trace("test_abort_testReceiveMultiTagInput_"+messenger.getMessengerStatus());
			assertEquals(MESSENGER_STATUS_NOT_SUPPORTED, messenger.getMessengerStatus());
			
			return;
		}
		
		setReceiver();
		rec.getMessengerForTesting().sInputParent(messenger.getName());
		
		
		messenger.call(TEST_RECEIVER, TEST_COMMAND, 
			messenger.tagValue(TEST_TAG, 1),//JSONNumber
			messenger.tagValue("キー2", 2),//JSONNumber
			messenger.tagValue("キー3", 3),//JSONNumber
			messenger.tagValue("キー4", 4)//JSONNumber
		);
		
		delayTestFinish(INTERVAL_TIMEOUT_MS);
		
		Timer timer = new Timer() {
			@Override
			public void run() {
				if (1 < rec.getMessengerForTesting().getReceiveLogSize()) {
					cancel();
					
					//到達したら
					String receiveMessage = rec.getMessengerForTesting().getReceiveLog(1);
					JSONValue actualTagValue1 = messenger.getValueForTag(TEST_TAG, receiveMessage);
					assertTrue(actualTagValue1.isNumber() != null);
					assertEquals(1, (int)actualTagValue1.isNumber().doubleValue());
					
					JSONValue actualTagValue2 = messenger.getValueForTag("キー2", receiveMessage);
					assertTrue(actualTagValue2.isNumber() != null);
					assertEquals(2, (int)actualTagValue2.isNumber().doubleValue());
					
					JSONValue actualTagValue3 = messenger.getValueForTag("キー3", receiveMessage);
					assertTrue(actualTagValue3.isNumber() != null);
					assertEquals(3, (int)actualTagValue3.isNumber().doubleValue());
					
					JSONValue actualTagValue4 = messenger.getValueForTag("キー4", receiveMessage);
					assertTrue(actualTagValue4.isNumber() != null);
					assertEquals(4, (int)actualTagValue4.isNumber().doubleValue());
					
					
					
					finishTest();
				}
			}
		};
		
		timer.scheduleRepeating(INTERVAL_FPS);
		debug.trace("test_abort_testReceiveMultiTagInput_到達してる");
	}
	
	
	
	
	
	
	/**
	 * Callのテスト
	 */
	public void testCreateCallMethod () {
		if (messenger.getMessengerStatus() != MESSENGER_STATUS_OK) {
			debug.trace("test_abort_testCreateCallMethod_"+messenger.getMessengerStatus());
			assertEquals(MESSENGER_STATUS_NOT_SUPPORTED, messenger.getMessengerStatus());
			
			return;
		}
		
		
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
		
		setReceiver();
		rec.getMessengerForTesting().sInputParent(messenger.getName());
		
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
		String s1 = messenger.getSendLog(1);
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
		if (messenger.getMessengerStatus() != MESSENGER_STATUS_OK) {
			debug.trace("test_abort_getMessengerStatus_"+messenger.getMessengerStatus());
			assertEquals(MESSENGER_STATUS_NOT_SUPPORTED, messenger.getMessengerStatus());
			
			return;
		}
		
		setReceiver();
		rec.getMessengerForTesting().sInputParent(messenger.getName());
		
		messenger.call(TEST_RECEIVER, TEST_COMMAND);
		
		//送信記録
		String s1 = messenger.getSendLog(1);
		assertTrue(s1.contains("testCommand"));
	}
	
	
	
//	//仮想的に受信者がいる場合のテスト//////////////////////////////////////////////////////////
//	/**
//	 * 自分~自分へのメッセージ
//	 */
////	public void testAssumeReceiveLog () {
////		
////		String message = messenger.getMessageObjectPreview(0, TEST_MYNAME, TEST_COMMAND, 0, messenger.tagValue(TEST_TAG, TEST_VALUE)).toString();
////		MessageReceivedEvent event = new MessageReceivedEvent(message);
////        
////		messenger.onMessageReceived(event);
////		String s = messenger.getReceiveLog(0);
////		
////		assertEquals(1, messenger.getReceiveLogSize());
////	}
//	
//	
//	/**
//	 * 自分-自分宛のメッセージを取得したテスト
//	 */
//	public void testGetReceiveToMyself () {
//		String message = messenger.getMessageObjectPreview(0, TEST_MYNAME, TEST_COMMAND, 0, messenger.tagValue(TEST_TAG, TEST_VALUE)).toString();
//		MessageReceivedEvent event = new MessageReceivedEvent(message);
//       
//		messenger.onMessageReceived(event);
//		String s = messenger.getReceiveLog(0);
//		
//		assertEquals(1, messenger.getReceiveLogSize());
//	}
//	
//	
//	/**
//	 * 他人宛のメッセージを取得していないテスト
//	 */
//	public void testNotReceiveTheMessageForAnyone () {
//		String message = messenger.getMessageObjectPreview(0, TEST_RECEIVER, TEST_COMMAND, 0, messenger.tagValue(TEST_TAG, TEST_VALUE)).toString();
//		MessageReceivedEvent event = new MessageReceivedEvent(message);
//       
//		messenger.onMessageReceived(event);
//		
//		assertEquals(0, messenger.getReceiveLogSize());
//	}
//	
//	
//	/**
//	 * 他人-他人間のメッセージを取得していないテスト
//	 */
//	public void testNotConcernSomeoneToSomeselfMessageNotReceive () {
//		setReceiver();
//		String message = rec.getMessengerForTesting().getMessageObjectPreview(0, TEST_RECEIVER, TEST_COMMAND, 0, rec.getMessengerForTesting().tagValue(TEST_TAG, TEST_VALUE)).toString();
//		MessageReceivedEvent event = new MessageReceivedEvent(message);
//		
//		messenger.onMessageReceived(event);
//		assertEquals(0, messenger.getReceiveLogSize());
//	}
//	
//	/**
//	 * 他人-第三者間のメッセージを取得していないテスト
//	 */
//	public void testNotConcernSomeoneToSomeoneMessageNotReceive () {
//		setReceiver();
//		String message = rec.getMessengerForTesting().getMessageObjectPreview(0, TEST_ANOTHERONE, TEST_COMMAND, 0, rec.getMessengerForTesting().tagValue(TEST_TAG, TEST_VALUE)).toString();
//		MessageReceivedEvent event = new MessageReceivedEvent(message);
//		
//		messenger.onMessageReceived(event);
//		assertEquals(0, messenger.getReceiveLogSize());
//	}
//	
//	/**
//	 * 他人-自分宛のメッセージを受け取るテスト
//	 */
//	public void testGetMessageSomeoneToMe () {
//		setReceiver();
//		String message = rec.getMessengerForTesting().getMessageObjectPreview(messenger.MS_CATEGOLY_CALLCHILD, TEST_MYNAME, TEST_COMMAND, 0, rec.getMessengerForTesting().tagValue(TEST_TAG, TEST_VALUE)).toString();
//		MessageReceivedEvent event = new MessageReceivedEvent(message);
//		
//		messenger.onMessageReceived(event);
//		assertEquals(1, messenger.getReceiveLogSize());
//	}
//	
//	/**
//	 * 受信したメッセージの送信者名を取得するメソッドの内容チェック
//	 */
//	public void testCheckSenderName () {
//		
//		setReceiver();
//		String message = rec.getMessengerForTesting().getMessageObjectPreview(messenger.MS_CATEGOLY_CALLCHILD, TEST_MYNAME, TEST_COMMAND, 0, rec.getMessengerForTesting().tagValue(TEST_TAG, TEST_VALUE)).toString();
//		MessageReceivedEvent event = new MessageReceivedEvent(message);
//		
//		messenger.onMessageReceived(event);
//		
//		assertEquals(1, messenger.getReceiveLogSize());
//		
//		String s = messenger.getReceiveLog(0);
//		
//		String senderName = messenger.getSenderName(s);
//		assertEquals(rec.getMessengerForTesting().getName(), senderName);
//	}
//	
//	
//	/**
//	 * 受信したメッセージの送信者IDを取得するメソッドの内容チェック
//	 */
//	public void testChechSenderID () {
//		setReceiver();
//		String message = rec.getMessengerForTesting().getMessageObjectPreview(messenger.MS_CATEGOLY_CALLCHILD, TEST_MYNAME, TEST_COMMAND, 0, rec.getMessengerForTesting().tagValue(TEST_TAG, TEST_VALUE)).toString();
//		debug.trace("id_message_"+message);
//		MessageReceivedEvent event = new MessageReceivedEvent(message);
//		
//		messenger.onMessageReceived(event);
//		
//		assertEquals(1, messenger.getReceiveLogSize());
//		
//		String s = messenger.getReceiveLog(0);
//		debug.trace("id_s_"+s);
//		String senderID = messenger.getSenderID(s);
//		
//		assertEquals(rec.getMessengerForTesting().getID(), senderID);
//	}
//	
//	
//	
//	/**
//	 * 受信したコマンドを分析するコマンドの内容チェック
//	 */
//	public void testCheckReceivedCommand () {
//		setReceiver();
//		String message = rec.getMessengerForTesting().getMessageObjectPreview(messenger.MS_CATEGOLY_CALLCHILD, TEST_MYNAME, TEST_COMMAND, 0, rec.getMessengerForTesting().tagValue(TEST_TAG, TEST_VALUE)).toString();
//		MessageReceivedEvent event = new MessageReceivedEvent(message);
//		
//		messenger.onMessageReceived(event);
//		String s = messenger.getReceiveLog(0);
//		
//		String command = messenger.getCommand(s);
//		assertEquals(TEST_COMMAND, command);
//	}
//	
//	
//	/**
//	 * 受信したtagValueの内容を分析するテスト
//	 */
//	public void testCheckTagValue () {
//		setReceiver();
//		String message = rec.getMessengerForTesting().getMessageObjectPreview(messenger.MS_CATEGOLY_CALLCHILD, TEST_MYNAME, TEST_COMMAND, 0, rec.getMessengerForTesting().tagValue(TEST_TAG, TEST_VALUE)).toString();
//		MessageReceivedEvent event = new MessageReceivedEvent(message);
//		
//		messenger.onMessageReceived(event);
//		String s = messenger.getReceiveLog(0);
//		String tagValue1 = messenger.getValueForTag(TEST_TAG, s).isString().stringValue();//isString, isObject, isArray, isNumber, isBoolean　とかが入るので、こんな感じ。
//		assertEquals(TEST_VALUE, tagValue1);
//	}
//	
//	
//	/**
//	 * タグ集をゲットするメソッドの内容テスト
//	 */
//	public void testCheckGetTags () {
//		setReceiver();
//		String message = rec.getMessengerForTesting().getMessageObjectPreview(messenger.MS_CATEGOLY_CALLCHILD, TEST_MYNAME, TEST_COMMAND, 0, rec.getMessengerForTesting().tagValue(TEST_TAG, TEST_VALUE)).toString();
//		MessageReceivedEvent event = new MessageReceivedEvent(message);
//		
//		messenger.onMessageReceived(event);
//		String s = messenger.getReceiveLog(0);
//		
//		ArrayList<String> tags = messenger.getTags(s);
//		assertEquals(1, tags.size());
//		assertEquals(TEST_TAG, tags.get(0));
//	}
//	
//	/**
//	 * バリュー集をゲットするメソッドの内容テスト
//	 */
//	public void testCheckGetValues () {
//		setReceiver();
//		String message = rec.getMessengerForTesting().getMessageObjectPreview(messenger.MS_CATEGOLY_CALLCHILD, TEST_MYNAME, TEST_COMMAND, 0, rec.getMessengerForTesting().tagValue(TEST_TAG, TEST_VALUE)).toString();
//		MessageReceivedEvent event = new MessageReceivedEvent(message);
//		
//		messenger.onMessageReceived(event);
//		String s = messenger.getReceiveLog(0);
//		
//		ArrayList<JSONValue> values = messenger.getValues(s);
//		
//		assertEquals(1, values.size());
//		assertEquals(TEST_VALUE, values.get(0).isString().stringValue());
//	}
	
	
	
	
	//実際に受信者がいる場合のテスト//////////////////////////////////////////////////////////
	
	
	/**
	 * 受信者のログが作成されたかどうかをチェックする
	 */
	public void testReceivedLogCreate () {
		if (messenger.getMessengerStatus() != MESSENGER_STATUS_OK) {
			debug.trace("test_abort_testReceivedLogCreate_"+messenger.getMessengerStatus());
			assertEquals(MESSENGER_STATUS_NOT_SUPPORTED, messenger.getMessengerStatus());
			
			return;
		}
		
		setReceiver();
		rec.getMessengerForTesting().sInputParent(messenger.getName());
		
		
		messenger.call(TEST_RECEIVER, "testCommand");
		delayTestFinish(INTERVAL_TIMEOUT_MS);
		
		Timer timer = new Timer() {
			@Override
			public void run() {
				if (1 < rec.getMessengerForTesting().getReceiveLogSize()) {
					cancel();
					
					assertEquals(2, rec.getMessengerForTesting().getReceiveLogSize());
					
					finishTest();
				}
			}
		};
		
		timer.scheduleRepeating(INTERVAL_FPS);
	}
	
	
	/**
	 * 受信者の受信内容テスト
	 */
	public void testReceivedLogExist () {
		if (messenger.getMessengerStatus() != MESSENGER_STATUS_OK) {
			debug.trace("test_abort_testReceivedLogExist_"+messenger.getMessengerStatus());
			assertEquals(MESSENGER_STATUS_NOT_SUPPORTED, messenger.getMessengerStatus());
			
			return;
		}
		
		setReceiver();
		rec.getMessengerForTesting().sInputParent(messenger.getName());
		
		
		messenger.call(TEST_RECEIVER, TEST_COMMAND);
		
		delayTestFinish(INTERVAL_TIMEOUT_MS);
		
		Timer timer = new Timer() {
			@Override
			public void run() {
				if (1 < rec.getMessengerForTesting().getReceiveLogSize()) {
					cancel();
					
					String s1 = rec.getMessengerForTesting().getReceiveLog(1);
					assertTrue(s1.contains(TEST_COMMAND));
					
					
					finishTest();
				}
			}
		};
		
		timer.scheduleRepeating(INTERVAL_FPS);
	}
	
	/**
	 * 指定した親へとメッセージが届く
	 */
	public void testInputParentReach () {
		Object o = new Object();
		final MessengerGWTImplement parentMessenger = new MessengerGWTImplement("currentParent2", o);
		
		Object c = new Object();
		final MessengerGWTImplement childMessenger = new MessengerGWTImplement("currentChild", c);
		
		childMessenger.inputParent(parentMessenger.getName());
		
		delayTestFinish(INTERVAL_TIMEOUT_MS);
		assertEquals(0, parentMessenger.getReceiveLogSize());
		
		Timer timer = new Timer() {
			@Override
			public void run() {
				if (parentMessenger.getReceiveLogSize() == 1) {
					cancel();
					List<JSONObject> currentChildList = parentMessenger.childList;
				
					assertEquals(1, currentChildList.size());
					JSONObject obj = currentChildList.get(0);
					assertTrue(obj.get(messenger.CHILDLIST_KEY_CHILD_ID).isString() != null);
					String childId = obj.get(messenger.CHILDLIST_KEY_CHILD_ID).isString().stringValue();
					
					assertTrue(obj.get(messenger.CHILDLIST_KEY_CHILD_NAME).isString() != null);
					String childName = obj.get(messenger.CHILDLIST_KEY_CHILD_NAME).isString().stringValue();

					assertEquals(childMessenger.getName(), childName);
					assertEquals(childMessenger.getID(), childId);
					
					
					finishTest();
				}
			}
		};
		
		timer.scheduleRepeating(INTERVAL_FPS);
	}
	
	/**
	 * 正確なIDの子供へのが有る
	 */
	public void testInputParentReturned () {
		Object o_ = new Object();
		final MessengerGWTImplement parentDummyMessenger = new MessengerGWTImplement("dummyParent", o_);
		
		Object o = new Object();
		final MessengerGWTImplement parentMessenger = new MessengerGWTImplement("currentParent", o);
		
		Object c = new Object();
		final MessengerGWTImplement childMessenger = new MessengerGWTImplement("currentChild", c);
		
		Object o2 = new Object();
		final MessengerGWTImplement parentMessenger2 = new MessengerGWTImplement("currentParent", o);
		
		
		childMessenger.inputParent(parentMessenger.getName());//同名の親が存在する可能性のある状態
		
		
		delayTestFinish(INTERVAL_TIMEOUT_MS);
		assertEquals(0, parentMessenger.getReceiveLogSize());
		
		Timer timer = new Timer() {
			@Override
			public void run() {
				if (childMessenger.getReceiveLogSize() == 1) {
					cancel();
					
					String parentName = childMessenger.getParentName();
					assertEquals(parentMessenger.getName(), parentName);
					
					String paretnID = childMessenger.getParentID();
					assertEquals(parentMessenger.getID(), paretnID);
										
					
					finishTest();
				}
			}
		};
		
		timer.scheduleRepeating(INTERVAL_FPS);
	}

	/**
	 * 親子関係の実装とテスト
	 */
	public void testInputParent () {
		final MessengerGWTImplement messengerParent = new MessengerGWTImplement(TEST_PARENTNAME, this);
		messenger.inputParent(TEST_PARENTNAME);
		
		//親への信号を送って、帰って来た段階で初めてOKになる筈
		delayTestFinish(INTERVAL_TIMEOUT_MS);
		
		Timer timer = new Timer() {
			@Override
			public void run() {
				if (0 < messenger.getReceiveLogSize()) {
					cancel();
					
					assertEquals(TEST_PARENTNAME, messenger.getParentName());
					
					finishTest();
				}
			}
		};
		
		timer.scheduleRepeating(INTERVAL_FPS);
	}
	
	
	/**
	 * 存在しないメッセンジャーへの送信を行った場合の処理テスト
	 * 呼べずにエラーが発生する
	 * 目的としては、指定した親が存在しない事に対して、即時的にエラーを発生させたい。
	 */
	public void testInputNotExistParentName () {
		final MessengerGWTImplement messengerOrphan = new MessengerGWTImplement(TEST_ORPHANNAME, this);
		messengerOrphan.inputParent(TEST_NOPARENTNAME);
		//親が見つからなかったとき、そのMessengerは停止すべき。その後メッセージを送ろうとしても、エラーを返すべき。その後、をどう判定するか、、
		
		//親への信号を送って、帰って来た段階で初めてOKになる筈
		delayTestFinish(INTERVAL_TIMEOUT_MS);
		
		Timer timer = new Timer() {
			int i = 0;
			@Override
			public void run() {
				i++;
				debug.trace("i_"+i);
				if (i == 3) {
					try {
						messengerOrphan.callParent("");
					} catch (Exception e) {
						cancel();
						finishTest();
					}
				}
			}
		};
		
		timer.scheduleRepeating(INTERVAL_FPS);
	}
	
	
	/**
	 * 件数ベースでの子供登録がなされているかどうかのテスト
	 * 単独の子供
	 */
	public void testInputParentSingleNumber () {
		final MessengerGWTImplement messengerParent = new MessengerGWTImplement(TEST_PARENTNAME, this);
		messengerParent.inputParent(messenger.getName());
		
		//親への信号を送って、帰って来た段階で初めてOKになる筈
		delayTestFinish(INTERVAL_TIMEOUT_MS);
		
		Timer timer = new Timer() {
			@Override
			public void run() {
				if (0 < messenger.getReceiveLogSize()) {
					cancel();
					
					assertEquals(1, messenger.childList.size());
					
					finishTest();
				}
			}
		};
		
		timer.scheduleRepeating(INTERVAL_FPS);
	}
	
	/**
	 * 件数ベースでの子供登録がなされているかどうかのテスト
	 * 複数の子供
	 */
	public void testInputParentMultiNumbers () {
		for (int i = 0; i < 10; i++) {
			final MessengerGWTImplement messengerParent = new MessengerGWTImplement(TEST_CHILDNAME+"_"+i, this);
			messengerParent.inputParent(messenger.getName());
		}
		
		//親への信号を送って、帰って来た段階で初めてOKになる筈
		delayTestFinish(INTERVAL_TIMEOUT_MS);
		
		Timer timer = new Timer() {
			@Override
			public void run() {
				if (messenger.childList.size() == 10) {
					
					for (JSONObject currentObj : messenger.childList) {
						debug.trace("currentObj_"+currentObj);
					}
					cancel();
					finishTest();
				}
			}
		};
		
		timer.scheduleRepeating(INTERVAL_FPS);
	}
	
	
	
	
	/**
	 * callParentの成立テスト
	 */
	public void testCallParent () {
		setReceiver();
		assertTrue(rec.getMessengerForTesting().getReceiveLogSize() == 0);
		messenger.inputParent(rec.getMessengerForTesting().getName());
		
		delayTestFinish(INTERVAL_TIMEOUT_MS);
		
		Timer timer = new Timer() {
			int step = 0;
			@Override
			public void run() {
				
				if (messenger.getReceiveLogSize() == 1 && step == 0) {
					step++;
					
					//子から見た親
					assertEquals(rec.getMessengerForTesting().getID(), messenger.getParentID());
					assertEquals(rec.getMessengerForTesting().getName(), messenger.getParentName());
					
					
					//親から見た子
					assertTrue(rec.getMessengerForTesting().childList.size() == 1);
					JSONObject currentChildList = rec.getMessengerForTesting().childList.get(0);
					assertEquals(messenger.getID(), currentChildList.get(messenger.CHILDLIST_KEY_CHILD_ID).isString().stringValue());
					assertEquals(messenger.getName(), currentChildList.get(messenger.CHILDLIST_KEY_CHILD_NAME).isString().stringValue());
					
					
					assertTrue(messenger.getSendLogSize() == 1);//親への登録通信の一回のみ
					messenger.callParent(TEST_COMMAND);
					assertTrue(messenger.getSendLogSize() == 2);//親への通信
				}
				
				if (rec.getMessengerForTesting().getReceiveLogSize() == 2 && step == 1) {
					step++;
					
					cancel();
					String s1 = rec.getMessengerForTesting().getReceiveLog(1);
					assertTrue(s1.contains(TEST_COMMAND));
						
					finishTest();
				}
			}
		};
		timer.scheduleRepeating(INTERVAL_FPS);
	}
	
	/**
	 * TagValueを含んだテスト
	 */
//	public void testCallParentWithTagValue () {
//		Object o = new Object();
//		final MessengerGWTImplement parentMessenger = new MessengerGWTImplement(TEST_PARENTNAME+"tag", o);
//		
//		messenger.inputParent(parentMessenger.getName());
//		
//		delayTestFinish(INTERVAL_TIMEOUT_MS);
//		
//		Timer timer = new Timer() {
//			int step = 0;
//			@Override
//			public void run() {
//				if (messenger.getReceiveLogSize() == 1 && step == 0) {
//					messenger.callParent(TEST_COMMAND,
//					messenger.tagValue("tag", "value")		
//					);
//					step++;
//				}
//				if (parentMessenger.getReceiveLogSize() == 2 && step == 1) {
//					cancel();
//					
//					String s1 = parentMessenger.getReceiveLog(1);
//					assertTrue(s1.contains(TEST_COMMAND));
//					finishTest();
//				}
//			}
//		};
//		timer.scheduleRepeating(INTERVAL_FPS);
//	}
	
	/**
	 * 自分へのメッセージングのテスト
	 */
	public void testCallMyself () {
		final int beforeSize = messenger.getReceiveLogSize();
		messenger.callMyself("command");
		
		delayTestFinish(INTERVAL_TIMEOUT_MS);
		
		Timer timer = new Timer() {
			@Override
			public void run() {
				if (beforeSize < messenger.getReceiveLogSize()) {
					cancel();
					
					String s1 = messenger.getReceiveLog(0);
					assertTrue(s1.contains("command"));
					finishTest();
				}
			}
		};
		
		timer.scheduleRepeating(INTERVAL_FPS);
	}
	
	
	public void testCallMyselfWithTagValue () {
		final int beforeSize = messenger.getReceiveLogSize();
		messenger.callMyself("command", messenger.tagValue("テストキー", "テストバリュー"));
		
		delayTestFinish(INTERVAL_TIMEOUT_MS);
		
		Timer timer = new Timer() {
			@Override
			public void run() {
				if (beforeSize < messenger.getReceiveLogSize()) {
					cancel();
					
					String s1 = messenger.getReceiveLog(0);
					assertTrue(s1.contains("command"));
					finishTest();
				}
			}
		};
		
		timer.scheduleRepeating(INTERVAL_FPS);
	}
	
	public void testCallMyselfWithMultipleTagValue () {
		final int beforeSize = messenger.getReceiveLogSize();
		messenger.callMyself("command", 
				messenger.tagValue("テストキー", "テストバリュー"),
				messenger.tagValue("テストキー2", "テストバリュー2"));
		
		delayTestFinish(INTERVAL_TIMEOUT_MS);
		
		Timer timer = new Timer() {
			@Override
			public void run() {
				if (beforeSize < messenger.getReceiveLogSize()) {
					cancel();
					
					String s1 = messenger.getReceiveLog(0);
					assertTrue(s1.contains("command"));
					finishTest();
				}
			}
		};
		
		timer.scheduleRepeating(INTERVAL_FPS);
	}
	
	/**
	 * 同名のメッセンジャーが存在する状態での、自分へのメッセージングの影響範囲限定のテスト
	 */
	public void testCallMyselfInMultipneSameNameSituation () {
		Object o = new Object();
		
		final MessengerGWTImplement duplicateNameMessenger = new MessengerGWTImplement(TEST_MYNAME, o);
		
		final int beforeSize = messenger.getReceiveLogSize();
		messenger.callMyself("command");
		
		
		delayTestFinish(INTERVAL_TIMEOUT_MS);
		
		Timer timer = new Timer() {
			@Override
			public void run() {
				if (beforeSize < messenger.getReceiveLogSize()) {
					cancel();
					
					//何も受け取っていない状態で無ければならない
					assertEquals(0,	duplicateNameMessenger.getReceiveLogSize());
					
					String s1 = messenger.getReceiveLog(0);
					assertTrue(s1.contains("command"));
					finishTest();
				}
			}
		};
		
		timer.scheduleRepeating(INTERVAL_FPS);
		
	}
	
	
	/**
	 * 同名の子供が一人の場合の到達テスト
	 */
	public void tescCallOneChild () {
		setReceiver();
		rec.getMessengerForTesting().sInputParent(messenger.getName());
		
		messenger.call(rec.getMessengerForTesting().getName(), TEST_COMMAND);//この時点で親
		
		
		delayTestFinish(INTERVAL_TIMEOUT_MS);
		Timer timer = new Timer() {
			@Override
			public void run() {
				if (1 < messenger.getReceiveLogSize()) {
					cancel();
					
					assertEquals(2,	rec.getMessengerForTesting().getReceiveLogSize());
					
					String s1 = rec.getMessengerForTesting().getReceiveLog(1);
					assertTrue(s1.contains("command"));
					finishTest();
				}
			}
		};
		
		timer.scheduleRepeating(INTERVAL_FPS);
	}
	

	/**
	 * 同名の子供が複数の場合の到達テスト
	 */
	public void tescCallMultiChild () {
		setReceiver();
		rec.getMessengerForTesting().sInputParent(messenger.getName());
		
		for (int i = 0; i < 10; i++) {
			Object o = new Object();
			MessengerGWTImplement messenger2 = new MessengerGWTImplement(rec.getMessengerForTesting().getName(), o);
			messenger2.inputParent(messenger.getName());
		}
		
		messenger.call(rec.getMessengerForTesting().getName(), TEST_COMMAND);//この時点で11人の子供の親
		
		
		delayTestFinish(INTERVAL_TIMEOUT_MS);
		Timer timer = new Timer() {
			@Override
			public void run() {
				if (1 < messenger.getReceiveLogSize()) {
					cancel();
					
					assertEquals(2,	rec.getMessengerForTesting().getReceiveLogSize());
					assertEquals(10+10, messenger.childList.size());
					
					String s1 = rec.getMessengerForTesting().getReceiveLog(1);
					assertTrue(s1.contains(TEST_COMMAND));
					finishTest();
				}
			}
		};
		
		timer.scheduleRepeating(INTERVAL_FPS);
	}
	
	
	
	
	
	
//	/**
//	 * ロックでの、連携動作機構のテスト
//	 * ロックされたメソッドが必ず実行されることを示す
//	 */
//	void testWithLock () {
//		if (messenger.getMessengerStatus() != MESSENGER_STATUS_OK) {
//			debug.trace("testWithLock	"+messenger.getMessengerStatus());
//			assertEquals(MESSENGER_STATUS_NOT_SUPPORTED, messenger.getMessengerStatus());
//			
//			return;
//		}
//		//自分自身に送り、かつ、送られたら特定のメッセージを発行するようにする。
//	
//		messenger.callMyself(TEST_LOCKED_EXEC);
//		messenger.callMyself(TEST_LOCK_EXEC,
//				messenger.withLockAfter(TEST_LOCKED_EXEC, "key")
//				);
//		
//		
//		
//		
//	}
	
	/**
	 * inputParentメソッドでの同期メソッドのログ件数チェック
	 */
	public void testSyncInputParentLogNum () {
		if (messenger.getMessengerStatus() != MESSENGER_STATUS_OK) {
			debug.trace("test_abort_testSyncCall_"+messenger.getMessengerStatus());
			assertEquals(MESSENGER_STATUS_NOT_SUPPORTED, messenger.getMessengerStatus());
			
			return;
		}
		
		setReceiver();
		messenger.sInputParent(rec.getMessengerForTesting().getName());
		
		assertEquals(1,messenger.getReceiveLogSize());
		assertEquals(1,messenger.getSendLogSize());
		
		assertEquals(1,rec.getMessengerForTesting().getReceiveLogSize());
	}
	
	/**
	 * 自分自身を同期メソッドで呼ぶ場合のログ件数テスト
	 */
	public void testSyncCallMyselfLogNum () {
		messenger.sCallMyself(TEST_COMMAND);
		assertEquals(1,messenger.getReceiveLogSize());
		assertEquals(1,messenger.getSendLogSize());
	}
	
	
	/**
	 * 自分自身を同期メソッドで呼ぶ場合のコマンド内容テスト
	 */
	public void testSyncCallMyselfExec () {
		messenger.sCallMyself(TEST_COMMAND);
		
		String s1 = messenger.getReceiveLog(0);
		assertTrue(s1.contains(TEST_COMMAND));
	}
	
	
	/**
	 * 同期での親子関係ログの一致
	 */
	public void testSyncInputParentLog () {
		setReceiver();
		messenger.sInputParent(rec.getMessengerForTesting().getName());
		
		assertEquals(1,messenger.getSendLogSize());
		assertEquals(1,messenger.getReceiveLogSize());
		
		assertEquals(rec.getMessengerForTesting().getName(), messenger.getParentName());
		assertEquals(rec.getMessengerForTesting().getID(), messenger.getParentID());
		
		
		assertEquals(1,rec.getMessengerForTesting().getSendLogSize());
		assertEquals(1,rec.getMessengerForTesting().getReceiveLogSize());
		
		assertEquals(1, rec.getMessengerForTesting().childList.size());
		
		JSONObject currentChild = rec.getMessengerForTesting().childList.get(0);
		String childName = currentChild.get(messenger.CHILDLIST_KEY_CHILD_NAME).isString().stringValue();
		String childID = currentChild.get(messenger.CHILDLIST_KEY_CHILD_ID).isString().stringValue();
		
		assertEquals(messenger.getName(), childName);
		assertEquals(messenger.getID(), childID);
	}
	
	
	/**
	 * 親子間の同期メソッドのコマンド伝達内容確認
	 */
	public void testSyncCallExec () {
		setReceiver();
		rec.getMessengerForTesting().sInputParent(messenger.getName());
		
		messenger.sCall(TEST_RECEIVER, TEST_COMMAND);
		
		assertEquals(2, rec.getMessengerForTesting().getReceiveLogSize());
		
		String s1 = rec.getMessengerForTesting().getReceiveLog(1);
		assertTrue(s1.contains(TEST_COMMAND));
	}
	
	
	/**
	 * 親子間の同期メソッドのコマンド+タグ付き伝達内容確認
	 */
	public void testSyncCallExecWithTag () {
		setReceiver();
		rec.getMessengerForTesting().sInputParent(messenger.getName());
		
		messenger.sCall(TEST_RECEIVER, TEST_COMMAND,
				messenger.tagValue("タグ", "バリュー")
		);
		
		assertEquals(2, rec.getMessengerForTesting().getReceiveLogSize());
		
		String s1 = rec.getMessengerForTesting().getReceiveLog(1);
		assertTrue(s1.contains(TEST_COMMAND));
		assertTrue(s1.contains("タグ"));
		assertTrue(s1.contains("バリュー"));
	}
	
	
	
}
