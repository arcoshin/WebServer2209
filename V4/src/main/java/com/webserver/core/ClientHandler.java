package com.webserver.core;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

/**
 * 該類是線程任務類，負責完成與指定客戶端進行http交互
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
            String line = readline();
            System.out.println(line);

            /**
             * 請求行的相關訊息*/
            String method;//請求方式
            String uri;//請求路徑
            String protocol;//協議版本

            //根據請求行的規則，通過空格將請求行拆分為三部分，分別賦值三個變量
            String[] data = line.split("\\s");
            method = data[0];
            uri = data[1];
            protocol = data[2];

            //測試路徑: http://localhost:9188/myweb/index.html
            System.out.println("請求方式: " + method);
            System.out.println("抽象路徑: " + uri);
            System.out.println("協議版本: " + protocol);

            //解析消息頭
            //消息頭的相關訊息
            final Map<String,String> headers = new HashMap();
            while (true) {
                line = readline();
                if (line.isEmpty()) {
                    break;
                }
                System.out.println("消息標題: " + line);
                //將消息頭按照冒號空格的形式拆分
                data = line.split("\\s");
                //將消息頭的名字作為key，消息頭的值作為value，儲存至headers
                headers.put(data[0],data[1]);
            }


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 每次調用該方法，自動讀取一行字符串
     */
    private String readline() throws IOException {
        InputStream in = socket.getInputStream();//獲取連接的客戶端發送的字節數據
        StringBuilder builder = new StringBuilder();
        char cur;//紀錄本次讀取的字符
        char pre = 'p';//紀錄上次讀取的字符
        int d;
        while ((d = in.read()) != -1) {
            //將本次讀取的字符賦值給cur
            cur = (char) d;
            //判斷本次讀取內容是否於上次相同
            if (pre == 13 && cur == 10) {
                break;
            }//將本次讀取的字符賦值給上次讀取的字符以利下次判斷
            pre = cur;
            //將本次讀取的字節拼接到StringBuilder
            builder.append(cur);
        }
        //將builder轉換為String
        String line = builder.toString().trim();//會發現每次末尾都多一個空格，因此調用trim方法去除
//          line = line.trim();//去除末尾空白符，也可以於上式直接調用
        return line;
    }
}
