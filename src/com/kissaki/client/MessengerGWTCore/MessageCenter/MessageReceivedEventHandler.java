package com.kissaki.client.MessengerGWTCore.MessageCenter;

import com.google.gwt.event.shared.EventHandler;
@Deprecated
public interface MessageReceivedEventHandler extends EventHandler {
    void onMessageReceived(MessageReceivedEvent event);
}