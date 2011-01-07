package com.kissaki.client.MessengerGWTCore.MessageCenter;

import com.google.gwt.event.shared.GwtEvent;

public class MessageReceivedEvent extends GwtEvent<MessageReceivedEventHandler> {

    public static Type<MessageReceivedEventHandler> TYPE = new Type<MessageReceivedEventHandler>();

    private final String message;

    public MessageReceivedEvent(String message) {
        this.message = message;
    }

    @Override
    public Type<MessageReceivedEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(MessageReceivedEventHandler handler) {
        handler.onMessageReceived(this);
    }

    public String getMessage() {
        return message;
    }
}