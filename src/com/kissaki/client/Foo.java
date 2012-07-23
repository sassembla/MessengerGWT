package com.kissaki.client;

import com.kissaki.client.MessengerGWTCore.MessengerGWTImplement;
import com.kissaki.client.MessengerGWTCore.MessengerGWTInterface;
import com.kissaki.client.debugger.Debug;

public class Foo implements MessengerGWTInterface {
	MessengerGWTImplement messenger;
	Debug debug;
	
	public Foo (String name) {
		debug = new Debug(this);
		messenger = new MessengerGWTImplement("foo", this);
		messenger.call(messenger.getName(), "fooCommand", messenger.tagValue(name+"fooキーと", "fooバリューです"));
	}
	
	
	@Override
	public void receiveCenter(String message) {
		debug.trace("Foooo_receiveCenter_"+message);
	}
}
