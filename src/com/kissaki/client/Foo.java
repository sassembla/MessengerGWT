package com.kissaki.client;

import com.google.gwt.user.client.Window;
import com.kissaki.client.MessengerGWTCore.MessengerGWTImplement;
import com.kissaki.client.MessengerGWTCore.MessageCenter.MessageReceivedEvent;
import com.kissaki.client.MessengerGWTCore.MessageCenter.MessageReceivedEventHandler;
import com.kissaki.client.subFrame.debug.Debug;

public class Foo implements MessageReceivedEventHandler {
	MessengerGWTImplement messenger;
	Debug debug;
	
	public Foo (String name) {
		debug = new Debug(this);
		messenger = new MessengerGWTImplement("foo", this);
		messenger.call(messenger.getName(), "fooCommand", messenger.tagValue(name+"fooキーと", "fooバリューです"));
	}

	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
//		Window.alert("message_"+event.getMessage());
		debug.trace("FooReceived_"+messenger.getID()+"_message_"+event.getMessage());
	}
}
