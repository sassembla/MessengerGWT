package com.kissaki.client.MessengerGWTCore.MessageCenter;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import com.google.gwt.user.client.Window;
import com.kissaki.client.subFrame.debug.Debug;



public class MessageMasterHub {
	static List <Object> invokeList = null;
	static MessageMasterHub hub = new MessageMasterHub();
	static MessageChecker checker = null;
	static Debug debug;
	
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
		//ここは、各一回しか通らない
		if (invokeList != null) {
		
		} else {
			invokeList = new ArrayList<Object>();
		}
		
		if (checker != null) {
			
		} else {
			checker = new MessageChecker();
		}
		
		debug.trace("invokeObject_"+invokeObject);
		debug.trace("invokeList_size"+invokeList.size());
		
		invokeList.add(invokeObject);
		checker.addMessageReceivedEventHandler((MessageReceivedEventHandler)invokeObject);
	}
	
	/**
	 * メッセージの行使を行う
	 * ここで、イベントが発行される
	 * @param message
	 */
	public static void get (String message) {
		checker.newMessageReceived(message);
	}
}
