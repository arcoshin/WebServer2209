package com.webserver.core;

import com.webserver.http.EmptyRequestException;
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

    @Override
    public void run() {
        /**
         * HTTP協議要求進行交互時，發送的訊息只能包含以下三種字符，
         * 英文、數字、符號(全是單字節字符)，所以此處使用單字節讀取即可
         */
        try {

            //1.解析請求
            HttpServletRequest request = new HttpServletRequest(socket);
            HttpServletResponse response = new HttpServletResponse(socket);

            //2.處理請求(注意順序與單字細節)
            DispatcherServlet servlet = new DispatcherServlet();
            servlet.service(request, response);

            //3.發送響應
            //HttpServletResponse response = new HttpServletResponse(socket);
            /**
             * 往上聲明因為於上面決定狀態與內文
             */
            response.response();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (EmptyRequestException e) {
            //依照HTTP要求，對於空請求直接忽略即可，故不可與上述合併，也不回應(留白)。
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
