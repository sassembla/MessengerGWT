package com.kissaki.client;

import com.google.gwt.user.client.Window;
import com.kissaki.client.MessengerGWTCore.MessengerGWTImplement;
import com.kissaki.client.MessengerGWTCore.MessengerGWTInterface;
import com.kissaki.client.subFrame.debug.Debug;

public class ReceiverClass implements MessengerGWTInterface {
	Debug debug;
	MessengerGWTImplement receiver;
	String TEST_RECEIVER = null;

	
	public ReceiverClass (String name) {
		TEST_RECEIVER = name;
		debug = new Debug(this);
		receiver = new MessengerGWTImplement(TEST_RECEIVER, this);
	}
	
	
	public MessengerGWTImplement getMessengerForTesting () {
		return receiver;
	}
	
	
	@Override
	public void receiveCenter(String message) {
		debug.trace("receive_message_"+message);
	}
}
