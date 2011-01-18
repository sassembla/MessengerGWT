package com.kissaki.client;

import com.kissaki.client.MessengerGWTCore.MessengerGWTImplement;
import com.kissaki.client.MessengerGWTCore.MessengerGWTInterface;
import com.kissaki.client.MessengerGWTCore.MessageCenter.MessageMasterHub;
import com.kissaki.client.MessengerGWTCore.MessageCenter.MessageReceivedEvent;
import com.kissaki.client.MessengerGWTCore.MessageCenter.MessageReceivedEventHandler;
import com.kissaki.client.subFrame.debug.Debug;
import com.kissaki.shared.FieldVerifier;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class MessengerGWT implements EntryPoint, MessengerGWTInterface {
	Debug debug;
	
	public MessengerGWT () {
		debug = new Debug(this);
	}
	/**
	 * The message displayed to the user when the server cannot be reached or
	 * returns an error.
	 */
	private static final String SERVER_ERROR = "An error occurred while "
			+ "attempting to contact the server. Please check your network "
			+ "connection and try again.";

	/**
	 * Create a remote service proxy to talk to the server-side Greeting service.
	 */
	private final GreetingServiceAsync greetingService = GWT
			.create(GreetingService.class);
	
	MessengerGWTImplement messenger;
	/**
	 * This is the entry point method.
	 */
	public void onModuleLoad() {
		Foo foo = new  Foo("A");
		Foo foo2 = new Foo("B");
		
		messenger = new MessengerGWTImplement("myself", this);
		
//		messenger.call(messenger.getName(), "command", messenger.tagValue("キーと", "バリューです"));
//		messenger.call(messenger.getName(), "command2", messenger.tagValue("キー2と", "バリューです2"));
		
		JSONObject fooObject = new JSONObject();
		fooObject.put("childKey", new JSONString("childValue"));
		
		messenger.call(messenger.getName(), "fooCommand", 
				messenger.tagValue("fooString", "fooValue"),
				messenger.tagValue("fooNumber", 100.0),
				messenger.tagValue("fooObject", fooObject)
				);
	}

	@Override
	public void receiveCenter(String message) {
		
		String command = messenger.getCommand(message);
		
		String fooString = messenger.getValueForTag("fooString", message).isString().stringValue();
		Window.alert("command_" + command + "	fooString_"+fooString);
		
		double fooDouble = messenger.getValueForTag("fooNumber", message).isNumber().doubleValue();
		Window.alert("command_" + command + "	fooDouble_"+fooDouble);
		
		JSONObject fooObject = messenger.getValueForTag("fooObject", message).isObject();
		Window.alert("command_" + command + "	fooObject_"+fooObject);
		
	}
}
