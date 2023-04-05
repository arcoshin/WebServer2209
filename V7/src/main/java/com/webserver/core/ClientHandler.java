package com.webserver.core;

import com.webserver.http.HttpServletRequest;

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
        try {//1.解析請求
            HttpServletRequest request = new HttpServletRequest(socket);
            //2.處理請求(先不做)
            //3.發送響應
            /**
             * ClientHandler.class 開始定位當前target下的文件
             * "."定位當前字解碼文件的頂級包(com的上級包:classes)
             *
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
            File file = new File(staticDir,path);
            /**
             * 如果抽象路徑沒有寫 localhost:8088 定位的是static
             * 如果抽象路徑寫錯了 localhost:8088/a.html 定位是 localhost:8088/a.html 但其不存在
             * 如果抽象路徑寫對了 localhost:8088/myweb/index.html
             * 定位於static下的myweb/index.html 此時定位就是一個文件
             */
            String line;
            if(file.isFile()){
                //走此分支時，將該正確文件，作為正文返回
                line = "HTTP/1.1 200 OK";

            } else {//說明定位到目錄(路徑空白或者錯誤)
                /**
                 * 走此分支時，重新定位404.html，將該頁面作為正文返回
                 * HTTP協議規定，如果返回404葉面，需要將狀態行改為HTTP/1.1 404 NotFound
                 */
                line = "HTTP/1.1 404 NotFound";
                file = new File(staticDir,"/root/404.html");
            }


            /**
             * HTTP/1.1 200 OK (CR)(LF)
             * Content-Type:(SP)text/html(CR)(LF)
             * Content-length:(SP)正文的長度(CR)(LF)
             * (CR)(LF)
             * 要發送的文件的字節數據
             */
            OutputStream out = socket.getOutputStream();
            //3.1 發送狀態行
            println(line);//冗餘代碼封裝成方法
            //3.2 發送響應頭
            line = "Content-Type: text/html";//注意空格不可省略
            println(line);//冗餘代碼封裝成方法

            line = "Content-Length: "+file.length();//注意空格不可省略
            println(line);//冗餘代碼封裝成方法

            println("");//空字符串自動輸出回車換行

            //3.3 發送響應正文
            byte[] buf = new byte[10*1024];//塊讀
            FileInputStream fis = new FileInputStream(file);
            int len;//每次讀寫的字節量
            while ((len = fis.read(buf)) != -1){
                out.write(buf,0,len);//讀多少寫多少模擬flash
            }
            System.out.println("響應發送完畢");//打樁語句

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

    /**
     * 將指定字符串發送給瀏覽器
     */
    private void println(String line) throws IOException {
        OutputStream out = socket.getOutputStream();
        byte[] data = line.getBytes(StandardCharsets.ISO_8859_1);
        out.write(data);
        out.write(13);//回車符
        out.write(10);//換行符
    }


}
