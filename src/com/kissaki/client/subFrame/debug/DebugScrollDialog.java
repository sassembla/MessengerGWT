package com.kissaki.client.subFrame.debug;

import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.ScrollPanel;


/**
 * デバッグ用の文字表示ダイアログ。
 * す
 * @author sassembla
 *
 */
public class DebugScrollDialog extends DialogBox {

	Debug debug = null;
	ScrollPanel scrollPanel = null;//親となるスクロールパネル
	
	
	/**
	 * コンストラクタ
	 */
	public DebugScrollDialog() {
		debug = new Debug(this);
		debug.removeTraceSetting(Debug.DEBUG_EVENT_ON);
		
		setHTML("Debug");
		
		scrollPanel = new ScrollPanel();
		setWidget(scrollPanel);
		scrollPanel.setSize("374px", "194px");
	}
	
	
	
	/**
	 * HTMLを取得、スクロールにセットする。
	 * @param html
	 */
	public void addHTMLToScrollPanel (HTML html) {
//		debug.trace("addHTMLToScrollPanel_html_"+html.toString().substring(0, 20));
		scrollPanel.clear();
		scrollPanel.add(html);
	}


//	/**
//	 * 内蔵されているHTMLのリロードをおこなう
//	 */
//	public void reload(HTML html) {
//		int i = 0;
//		while (scrollPanel.iterator().hasNext()) {
////			for (HTML w : (HTML)scrollPanel.iterator().next()) {
////				debug.trace("子供が居れば出てくるはず_"+i);
//				
//				
//				i++;
//				if (100 < i) break; 
////			}
//		}
//		debug.trace("子供が居れば出てくるはず総数_"+i);
//		
////		if (0 < i) {
////			scrollPanel.remove(w)
////		}
//	}

}
