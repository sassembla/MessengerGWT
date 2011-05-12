package com.kissaki.client.MessengerGWTCore.MessageCenter;

import java.util.ArrayList;
import com.kissaki.client.MessengerGWTCore.MessengerGWTInterface;
import com.kissaki.client.subFrame.debug.Debug;


/**
 * messageで行使されたメソッドから中継される、イベントハブ。
 * イベントを発行する手助けを行う、今回のMessengerのハブ部分。
 * 
 * staticなシングルトンオブジェクトとして所持される。
 * GINとか使って疎にしたいところだが。
 * 
 * @author ToruInoue
 */
public class MessageMasterHub implements MessengerGWTInterface {
	static Debug debug;
	
	int messageMasterStatus = MESSENGER_STATUS_NULL;
	
	private static MessageMasterHub hub = new MessageMasterHub();
	
//	static MessageChecker checker = null;
	static MessageReceivedEventBus eventBus;
	static ArrayList <String> invocationClassNameList = null; 
	
	/**
	 * シングルトン取得メソッド
	 * @return
	 */
	public static MessageMasterHub getMaster () {
//		if (checker == null) checker = new MessageChecker();
		if (eventBus == null) eventBus = new MessageReceivedEventBus(); 
		if (invocationClassNameList == null) invocationClassNameList = new ArrayList<String>();
		hub.setMessengerGlobalStatus(MESSENGER_STATUS_READY_FOR_INITIALIZE);
		return hub;
	}
	
	/**
	 * コンストラクタ、シングルトンの為に秘匿
	 */
	private MessageMasterHub() {
		debug = new Debug(this);
	}
	
	/**
	 * invoke対象の登録を行う
	 * 同じクラスに対して、二重登録を行わないようにチェックを行う
	 * (同じクラスを２つ登録すると、例えnameSpaceが別であっても、リスナのカウントが不自然に加算されるため。)
	 * e.g. Class Aのインスタンスを複数作り、messengerを持たせると、messengerの数だけ各クラスにメッセージが送られてしまう。
	 * 	Class A x 2 → 一つのメッセージが発生すると、インスタンス一つにつき2つのメッセージが送られてしまう。
	 * 	Eventの構造の問題だと思われる。登録数分だけ、同様のクラスに向けて発行されてしまう。
	 * 	
	 * 続き_11/01/13 20:29:49
	 * 通常のクラスについては、上記現象が発生しなかった。テストを書き換える必要がある。
	 * 
	 * @param name
	 * @param id
	 * @param invokeObject
	 * @param root
	 */
	public void setInvokeObject(Object root, MessageReceivedHandler messengerSelf) {
		
//		if (invocationClassNameList.contains(root.getClass().toString())) {//すでに同名のクラスが登録されていたら、登録しない。
//			debug.trace("already added_"+root.getClass());//JSの特例、同クラスの別インスタンスの所持するメソッドの区別が無い証、、、
//		} else {
//			invocationClassNameList.add(root.getClass().toString());
//			debug.trace("just added_"+root.getClass().toString());
//			checker.addMessageReceivedEventHandler((MessageReceivedEventHandler)messengerSelf);
//		}
		
		eventBus.addHandler(MessageReceivedEvent.MESSAGE_RECEIVED_EVENT_TYPE, messengerSelf);
	}
	
	/**
	 * メッセージの行使を行う
	 * ここで、イベントが発行される
	 * @param message
	 */
	public static void invokeReceive (String message) {
		eventBus.fireEvent(new MessageReceivedEvent(message));
	}
	
	/**
	 * ハンドラリセット用のメソッド
	 * 所持クラスからメッセージのハンドラを削除する
	 * 
	 * TODO テスト版のため、特定のメソッドではなく全ての記録が消える。
	 * 
	 * 同名のクラスが存在する場合、どれかひとつのハンドラを消去したタイミングで、生存しているオブジェクトへのメッセージも
	 * 到達しなくなるので、同名クラスを使う場合は注意すること。っていっても注意のしようもないのだけれど。
	 * 
	 * @param messengerSelf 
	 */
	public void destractInvocationClassNameList (Object root) {
		invocationClassNameList.remove(root.getClass().toString());
	}

	/**
	 * globalなインスタンスであるこのインスタンスが保持するMessengerSystemとしてのステータス
	 * @param setUp
	 */
	public void setMessengerGlobalStatus(int status) {
		messageMasterStatus = status;
	}

	public int getMessengerGlobalStatus() {
		return messageMasterStatus;
	}

	@Override
	public void receiveCenter(String message) {
		debug.assertTrue(false, "never call this method");
	}

	public String getExistMessengerIDFromName(String input) {
		// TODO Auto-generated method stub
		return null;
	}

}
