package com.webserver.core;

import com.webserver.http.HttpServletRequest;
import com.webserver.http.HttpServletResponse;

import java.io.*;
import java.net.Socket;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;


/**
 * 該類是線程任務類，負責完成與指定客戶端進行HTTP交互
 * 1.響應請求
 * 2.處理請求
 * 3.發送響應
 */
public class ClientHandler implements Runnable {
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

            //1.解析請求
            HttpServletRequest request = new HttpServletRequest(socket);
            HttpServletResponse response = new HttpServletResponse(socket);


            //2.處理請求(先不做)

            /**
             * ClientHandler.class 開始定位當前target下的文件
             * "."定位當前字解碼文件的頂級包(com的上級包:classes)
             */
            String path = request.getUri();
            System.out.println("抽象路徑:" + path);
            /**
             * File(File parent,String sub)
             * 定位parent目錄下，定位sub目錄
             */
            File staticDir = new File(
                    //不可用固定路徑定位index，不利後續自動抓取新版本。可定位target模塊，往後不需隨版本更新路徑
                    ClientHandler.class.getClassLoader().getResource(
                            "./static"
                    ).toURI()//路徑"/"要特別細心一點
            );
            //static下根據抽象路徑定位資源
            File file = new File(staticDir, path);
            /**
             * 如果抽象路徑沒有寫 localhost:8088 定位的是static
             * 如果抽象路徑寫錯了 localhost:8088/a.html 定位是 localhost:8088/a.html 但其不存在
             * 如果抽象路徑寫對了 localhost:8088/myweb/index.html
             * 定位於static下的myweb/index.html 此時定位就是一個文件
             */
            if (file.isFile()) {
                //走此分支時，將該正確文件，作為正文返回
//                line = "HTTP/1.1 200 OK";//預設內容，可不寫
                response.setContentFile(file);//定義正文內容

            } else {//說明定位到目錄(路徑空白或者錯誤)
                /**
                 * 走此分支時，重新定位404.html，將該頁面作為正文返回
                 * HTTP協議規定，如果返回404葉面，需要將狀態行改為HTTP/1.1 404 NotFound
                 */
//                line = "HTTP/1.1 404 NotFound";
                response.setStatusCode(404);//重新定義狀態代碼
                response.setStatusReason("NotFound");//重新定義狀態描述
                file = new File(staticDir, "/root/404.html");
                response.setContentFile(file);//傳入狀態內文
            }

            //3.發送響應
            //HttpServletResponse response = new HttpServletResponse(socket);
            /**
             * 往上聲明因為餘上面決定狀態與內文
             */
            response.response();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } finally {
            //一次HTTP交互後，斷開TCP連結(HTTP協議要求的)
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


}
