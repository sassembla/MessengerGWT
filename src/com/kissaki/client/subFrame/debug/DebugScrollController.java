package com.kissaki.client.subFrame.debug;


import com.google.gwt.user.client.ui.HTML;

/**
 * debugStringを更新、受け入れ、部分的にHTML化するクラス。
 * 
 * 本来外から呼ばれるのだが、シングルトンにしていない。
 * 
 * 様々な箇所から、イベントを受ける先に指定している。
 * 各デバッグクラスからイベントが発行され、イベントハンドラからこのクラスにデバッグイベントが渡されてくる。
 * このクラスの初期化時、スクリーンにイベント表示用のダイアログをセットする。
 * 
 * @author sassembla
 *
 */
public class DebugScrollController {
	Debug debug = null;
	
	private StringBuffer debugStringBuffer;//デバッグ文字列を追加するストリングバッファ
	


	String CODE_HTML_RETURN = "<br>";//HTML内に入れる改行コード
	
	final int SIZE_OF_DISPLAY_LOG = 2000;//デバッグで表示を次消しする文字数設定
	
	
	/**
	 * コンストラクタ
	 */
	public DebugScrollController () {
		debug = new Debug(this);
		debug.removeTraceSetting(Debug.DEBUG_EVENT_ON);//このクラスでは、デバッグを表示しない。
		
//		debug.trace("DebugScrollController_コンストラクタ");
		
		
		debugStringBuffer = new StringBuffer();//デバッグ文字列入力、保持用のStringBuffer
	}
	
	
	
	
	DebugScrollDialog dialog = null;
	/**
	 * 外部から
	 * 受け取った文言のまとめを行う
	 * 改行コードを交えてdebugStringBufferに追加
	 * 
	 * オブジェクトにリスナをつけられればな。
	 */
	public void addDebugString(String debugString) {
		
		if (dialog == null){
			debug.trace("addDebugString_dialog == null なので作成する");
			dialog = new DebugScrollDialog();
			
//			ToDoAppDelegate delegate = ToDoAppDelegate.getDelegate();
//			delegate.fireEvent(new ScreenEvent(2, dialog));//スクリーンにこのWidgetを貼付ける
		}
		
		
		debugStringBuffer.append(debugString + CODE_HTML_RETURN);//文言を改行と一緒に追加
		try {

			dialog.addHTMLToScrollPanel(new HTML(debugStringBuffer.toString()));
			
//			System.out.println("addDebugHTML_終了");
		} catch (Throwable e) {
			System.out.println("addDebugHTML_エラー発生_"+e);
		}
	}
	
	
	
	
	
	
}
