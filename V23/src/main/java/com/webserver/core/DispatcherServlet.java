package com.webserver.core;

import com.webserver.annotation.Controller;
import com.webserver.annotation.RequestMapping;
import com.webserver.controller.UserController;
import com.webserver.http.HttpContext;
import com.webserver.http.HttpServletRequest;
import com.webserver.http.HttpServletResponse;

import java.io.File;
import java.lang.reflect.Method;
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
         * "."定位當前字解碼文件的頂級包(com)的上一層目錄(classes)
         */
        String path = request.getRequestURI();///myweb/reg(必定)

        //判斷本次請求是否為請求業務
        /**
         * 掃描所有com.webserver.controller包下所有被@Controller修飾的類
         * 並判斷被@RequestMapping註解修飾的方法中，是否有註解傳入的參數值與
         * 當前獲取的抽象路徑path相同，如果有，就是處理該請求的方法
         * 利用反射機制調用該方法即可
         */
        try {
            //獲取controller目錄對象
            File dir = new File(
                    DispatcherServlet.class.getClassLoader().getResource(//getClassLoader()用以定位controller目錄
                            "./com/webserver/controller"
                    ).toURI()
            );
            //掃描dir中所有的.class文件，生成subs數組
            File[] subs = dir.listFiles(f -> f.getName().endsWith(".class"));
            //遍歷subs數組
            for (File sub : subs) {
                //獲取每一個.class文件的文件名
                String fileName = sub.getName();
                //由於文件名包含了後綴.class，所以需要去除
                String className = fileName.substring(0, fileName.indexOf("."));
                //讓包名和類名相拼接，生成該類的全類名
                String allName = "com.webserver.controller." + className;//controller後面的"."注意!不可使用"/"
                //根據全類名
                Class cls = Class.forName(allName);
                if (cls.isAnnotationPresent(Controller.class)) {//判斷該類是否被@Controller註解修飾
                    //走進此分支，說明該類是業務類，開始獲取該類中所有自身定義的方法
                    Method[] methods = cls.getDeclaredMethods();
                    //遍歷methods數組
                    for (Method method : methods) {
                        //獲取每個方法對象，判斷該方法是否被@RequestMapping註解修飾了
                        if (method.isAnnotationPresent(RequestMapping.class)) {
                            //獲取@RequestMapping註解對象中所傳入的參數
                            RequestMapping rm = method.getDeclaredAnnotation(RequestMapping.class);
                            //通過rm實例獲取該註解在當前方法上傳入的參數值
                            String value = rm.value();
                            //比對註解參數值和獲取的抽象路徑是否相同
                            if (path.equals(value)) {
                                //走入此分支，說明當前方法，就是我們要調用的業務方法，實例對象
                                Object o = cls.newInstance();
                                //執行此方法，此處要特別注意，記得傳入方法所需之參數...request,response
                                method.invoke(o,request,response);
                                //程序執行至此，已經完成業務方法的調用，就不需要往後執行了，直接跳出即可
                                return;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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
