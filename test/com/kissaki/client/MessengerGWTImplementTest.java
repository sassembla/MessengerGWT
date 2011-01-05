package com.kissaki.client;

import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.junit.client.GWTTestCase;
import com.kissaki.client.MessengerGWTCore.MessengerGWTImplement;
import com.kissaki.client.MessengerGWTCore.MessengerGWTInterface;
import com.kissaki.client.subFrame.debug.Debug;


public class MessengerGWTImplementTest extends GWTTestCase implements MessengerGWTInterface {
	Debug debug;
	
	MessengerGWTImplement messenger;
	String TEST_MYNAME = "sender";
	String TEST_RECEIVER = "receiver";
	
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
		messenger = new MessengerGWTImplement(TEST_MYNAME, this, 1);//テスト用に、IDを固定して構築する
	}
	
	
	/**
	 * ティアダウン
	 */
	public void gwtTearDown () {
		messenger.receiveCenter("");
		messenger = null;
		debug.trace("teardown");
	}

	
	/**
	 * 上書きしたメソッド、メッセージ受信時に呼ばれる筈。
	 */
	@Override
	public void receiveCenter(String message) {
		debug.trace("receiveCenter_message_"+message);
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
	 * 一つのタグがある場合の出力テスト
	 * 
	 */
	public void testSingleTagInput () {
		messenger.call(TEST_RECEIVER, "TestCommand", //この時点でロックして、ただしテストはteardownする。
			messenger.tagValue("キー1", "1")
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
		messenger.call(TEST_RECEIVER, "TestCommand", 
			messenger.tagValue("キー1", 1),//JSONNumber
			messenger.tagValue("キー2", 2),//JSONNumber
			messenger.tagValue("キー3", 3),//JSONNumber
			messenger.tagValue("キー4", 4)//JSONNumber
		);
		
		String expected = "{MESSENGER_exec=TestCommand, MESSENGER_messengerID=CDDED3E3, MESSENGER_tagValue={\"キー1\":1, \"キー2\":2, \"キー3\":3, \"キー4\":4}, MESSENGER_messengerName=sender, MESSENGER_to=receiver}";
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
		
		messenger.addSendLog(TEST_RECEIVER, "TestCommand", 
				messenger.tagValue("キー1", 1),//JSONNumber
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
		
		
		messenger.call(TEST_RECEIVER, "TestCommand",
			messenger.tagValue("キー1", 1),//JSONNumber
			messenger.tagValue("キー2", 2.222),//JSONNumber
			messenger.tagValue("キー3", "val3"),//JSONString
			messenger.tagValue("キー4", array),//JSONArray,,,
			messenger.tagValue("キー5", obj)//JSONObject
		);
		
		
		/*
		 * 該当のKVSを含んでいるか
		 */
		String s1 = messenger.getSendLog(0);
		assertTrue(s1.contains("\"キー1\":1"));
		assertTrue(s1.contains("\"キー2\":2.222"));
		assertTrue(s1.contains("\"キー3\":\"val3\""));
		assertTrue(s1.contains("\"キー4\":[{\"string\":\"a\"},{\"number\":100}]"));
		assertTrue(s1.contains("\"キー5\":{\"valOf5\":5}}"));
		
	}
	
	
	/**
	 * 送信者のコマンド生成チェック
	 */
	public void testCommandCreate () {
		try {
			messenger.call(TEST_RECEIVER, "testCommand", messenger.JSON_NULL);
		} catch (Exception e) {
			debug.trace("error_"+e);
			assertTrue(false);
		}
		
		
		//送信記録
		String s1 = messenger.getSendLog(0);
		assertTrue(s1.contains("testCommand"));
	}
	
	
	
	
	
	//受信者がいる場合のテスト//////////////////////////////////////////////////////////
	MessengerGWTImplement receiver;
	
	private void setReceiver () {
		ReceiverClass rec = new ReceiverClass();
		receiver = new MessengerGWTImplement(TEST_RECEIVER, rec);
	}
	
	/**
	 * 受信者のログが作成されたかどうかをチェックする
	 */
	public void testReceivedLogCreate () {
		setReceiver();
		messenger.call(TEST_RECEIVER, "testCommand", messenger.JSON_NULL);
		
		assertTrue(0 < receiver.receiveList.size());
		
	}
	
	
	/**
	 * 受信者の受信テスト
	 */
	public void testReceivedLogExist () {
		setReceiver();
		messenger.call(TEST_RECEIVER, "command", messenger.JSON_NULL);
		
		String s1 = receiver.getReceiveLog(0);
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
	
	
	/*
	 * 現在のアドレスを使って、メッセンジャーをセットする、
	 * アドレスが変わるたびに、定期的にリセットする必要がある、、のか？
	 * ほんとに？
	 * それってきついな。
	 * 
	 * 寿命がURLと同値である、ということは、もちろん、移動しちゃったらまあ効かないという事ですね。
	 * 
	 */
	
	public void memo () {

	
	}

	private class ReceiverClass {
		
	}

	
	
}
