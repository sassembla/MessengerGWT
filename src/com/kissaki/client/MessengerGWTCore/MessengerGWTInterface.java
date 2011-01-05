package com.kissaki.client.MessengerGWTCore;

/**
 * 実装すべきメソッドについて記述されたインターフェース
 * 
 * MessengerGWTIplementsで実装すべきメソッドを実装している。
 * Messengerを使うクラスは、このインターフェースを実装する必要があるようにしたい。
 * TODO その縛り
 * @author ToruInoue
 *
 */
public interface MessengerGWTInterface {
	abstract void receiveCenter(String message);
}
