package com.kissaki.client.MessengerGWTCore.MessageCenter;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import com.google.gwt.user.client.Window;
import com.kissaki.client.subFrame.debug.Debug;



public class MessageMasterHub {
	
	static MessageMasterHub hub = new MessageMasterHub();
	static MessageChecker checker = null;
	static Debug debug;
	
	/**
	 * シングルトン取得メソッド
	 * @return
	 */
	public static MessageMasterHub getMaster () {
		return hub;
	}
	
	/**
	 * コンストラクタ、シングルトンの為に秘匿
	 */
	private MessageMasterHub() {
		debug = new Debug(this);
	}
	
	public void setInvokeObject(String name, String id, Object invokeObject) {
		
		if (checker != null) {
			
		} else {
			checker = new MessageChecker();
		}
		
		checker.addMessageReceivedEventHandler((MessageReceivedEventHandler)invokeObject);
	}
	
	/**
	 * メッセージの行使を行う
	 * ここで、イベントが発行される
	 * @param message
	 */
	public static void invokeReceive (String message) {
		checker.newMessageReceived(message);
	}
}
