package com.webserver.core;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

/**
 * 該類是線程任務類，負責完成與指定客戶端進行http交互
 */
public class ClientHandler implements Runnable{
    private Socket socket;

    //創建任務類實例時，將對應的客戶端socket接收
    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    public void run() {
        /**
         * HTTP協議要求進行交互時，發送的訊息只能包含以下三種字符，
         * 英文、數字、符號(全是單字節字符)，所以此處使用單字節讀取即可
         */
        try {
            InputStream in = socket.getInputStream();//獲取連接的客戶端發送的字節數據
            int data;
            while ((data = in.read()) != -1){
                System.out.print((char)data);//注意此處使用print方法即可不需換行。
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
