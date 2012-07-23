package com.kissaki.client;

import com.kissaki.client.MessengerGWTCore.MessengerGWTImplement;
import com.kissaki.client.MessengerGWTCore.MessengerGWTInterface;
import com.kissaki.client.debugger.Debug;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.user.client.Window;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class MessengerGWTProject implements EntryPoint, MessengerGWTInterface {
	Debug debug;
	
	public MessengerGWTProject () {
		debug = new Debug(this);
	}


	MessengerGWTImplement messenger;
	/**
	 * This is the entry point method.
	 */
	public void onModuleLoad() {
		Foo foo = new  Foo("A");
		Foo foo2 = new Foo("B");

		Window.alert("ここで");
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
