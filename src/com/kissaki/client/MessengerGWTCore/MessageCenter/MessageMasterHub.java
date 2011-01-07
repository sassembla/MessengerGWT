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
	static int count = 0;
	/**
	 * この時点で、2つずつ呼ばれている。
	 * リスナの数だけ、メッセージが増えたような挙動になる。
	 * という事は、
	 * ここにくるさらに前に、増える要素が有るんだ。
	 * 
	 * 全員宛に拡散するような行動をとる
	 * @param message
	 */
	public static void get (String message) {
//		Window.alert("message_"+message);
//		debug.trace("message_"+message);
		debug.trace("count_"+count);
//		for (Iterator<Object> currentInvokeItel = invokeList.iterator(); currentInvokeItel.hasNext();) {
//			Object currentInvokator = currentInvokeItel.next();
			checker.newMessageReceived(message);
//		}
		count++;
	}
}
