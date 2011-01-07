package com.kissaki.client.MessengerGWTCore.MessageCenter;

import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.HasHandlers;
import com.google.gwt.user.client.Event;
import com.kissaki.client.subFrame.debug.Debug;

public class MessageChecker implements HasHandlers {
	Debug debug;

    private HandlerManager handlerManager;//ここだけが不味いんだね、SimpleEventBusをつかえ、とある。

    
	public MessageChecker () {
		debug = new Debug(this);
		handlerManager = new HandlerManager(this);
	}

    @Override
    public void fireEvent(GwtEvent<?> event) {
        handlerManager.fireEvent(event);
    }

    public HandlerRegistration addMessageReceivedEventHandler(
            MessageReceivedEventHandler handler) {
        return handlerManager.addHandler(MessageReceivedEvent.TYPE, handler);
    }

    public void newMessageReceived(String message) {
    	MessageReceivedEvent event = new MessageReceivedEvent(message);
        fireEvent(event);
    }
    
}
