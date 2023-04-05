package com.webserver.core;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 當前類為WebServer的主類
 * WebServer是一個Web容器，模擬tomcat的基礎功能
 * Web容器主要有兩個任務:
 * 1.管理佈署在服務器內部的所有網路應用(webapp)
 * 而每個網路應用就是我們的網站，通過網站
 * 都會包含頁面，處理業務的代碼與其他資源等等
 * 2.負責與客戶端進行TCP連接，並基於Http協議進行交互，
 * 使得可以通過客戶端遠程調用容器中的某個網路應用
 */
public class WebServerApplication {
    /**
     * 相當於總機，用於接收所有客戶端的訪問
     */
    private ServerSocket serverSocket;
    private ExecutorService threadPool;
    public WebServerApplication() {
        try {
            System.out.println("正在啟動09班服務器中...");
            //實例化serverSocket,並且佔用端口
            serverSocket = new ServerSocket(9188);
            threadPool = Executors.newFixedThreadPool(50);
            System.out.println("09班服務器創建成功");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void start() {
        try {
            while (true) {
                System.out.println("等待客戶端連接");
                //accept();為一阻塞方法，當有客戶端訪問才會繼續向下進行，並且會為其生成一個專用的Socket實例
                Socket socket = serverSocket.accept();
                System.out.println("一個客戶端已連接!");
                //啟動一個線程，讓該線程負責與當前來訪的客戶端進行交互
                ClientHandler handler = new ClientHandler(socket);
                threadPool.execute(handler);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    public static void main(String[] args) {
        WebServerApplication server = new WebServerApplication();
        server.start();
    }
}
