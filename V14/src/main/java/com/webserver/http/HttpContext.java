package com.webserver.http;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * 當前類用以定義HTTP協議所規定的內容，方便後期查找資料
 * 通常稱為數據字典類
 */
public class HttpContext {
    /**
     * 將回車換行符放入字典類，以免代碼中只見數字無法見名知義
     */
    public static final char CR = 13;//回車符
    public static final char LF = 10;//換行符


    /**
     * 此處文件類型無法確定，得視要發送的文件類型來分析
     * 也就是根據文件的後綴來確定
     * HTTP已經規定了對應的文件後綴要發送的Content-Type值:
     * html == text/html
     * css == text/css
     * js == application/javascript
     * gif == image/gif
     * jpg == image/jpeg
     * png == image/png
     */

    //做一鍵值對將以上對應關係儲存起來，實際有1000多種，切勿用if/switch
    private static Map<String, String> mimeMapping = new HashMap<>();

    static {
        /**
         * 快速封裝 : CTRL + ALT + M
         */
        initMapping();
    }

    private static void initMapping() {
//        mimeMapping.put("html", "text/html");
//        mimeMapping.put("css", "text/css");
//        mimeMapping.put("js", "application/javascript");
//        mimeMapping.put("gif", "image/gif");
//        mimeMapping.put("jpg", "image/jpeg");
//        mimeMapping.put("png", "image/png");
        /**
         * Properties工具類，是專門用來讀取properties文件的類
         * 其實properties工具類本身就是一個Map
         * 可以自動將配置文件中的每一行Key = Value的內容
         * 按照等號拆分後自動拆分到Map中
         */
        Properties properties = new Properties();
        /**
         * Properties工具類提供load()方法，可以加載所要解析的配置文件
         */
        try {
            properties.load(
                    HttpContext.class.getResourceAsStream("./web.properties")
            );
            /**
             * 此時web.properties文件中的所有文件後綴與mime值都儲存到properties.Map中
             * 遍歷
             */
            properties.forEach(
                    (k, v) -> mimeMapping.put(k.toString(), v.toString())
            );
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static String getMimeType(String ext) {
        return mimeMapping.get(ext);
        /**
         * 外界得以通過該方法傳入對應文件後綴，從而獲取對應的MIME值
         */


    }

}
