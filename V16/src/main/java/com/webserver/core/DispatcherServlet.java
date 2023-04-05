package com.webserver.core;

import com.webserver.http.HttpContext;
import com.webserver.http.HttpServletRequest;
import com.webserver.http.HttpServletResponse;

import java.io.File;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

/**
 * 用來封裝處理請求的邏輯類
 */
public class DispatcherServlet {
    /**
     * 靜態代碼塊
     * 1.是一靜態資源，屬於類，加載類時執行
     * 2.只加載一次，節省資源
     * 3.靜態代碼塊無法拋出異常，只能捕獲
     */
    private static File staticDir;//聲明於靜態代碼塊外，後面方法才可調用

    static {//靜態代碼塊
        try {
            staticDir = new File(//已在外聲明，故內部直接賦值
                    //不可用固定路徑定位index，不利後續自動抓取新版本。可定位target模塊，往後不需隨版本更新路徑
                    ClientHandler.class.getClassLoader().getResource(
                            "./static"
                    ).toURI()//路徑"/"要特別細心一點
            );
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public void service(HttpServletRequest request, HttpServletResponse response) {
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
    }


}
