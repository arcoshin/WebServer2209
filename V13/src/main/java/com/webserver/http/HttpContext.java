package com.webserver.http;

import java.util.HashMap;
import java.util.Map;

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
        mimeMapping.put("html", "text/html");
        mimeMapping.put("css", "text/css");
        mimeMapping.put("js", "application/javascript");
        mimeMapping.put("gif", "image/gif");
        mimeMapping.put("jpg", "image/jpeg");
        mimeMapping.put("png", "image/png");
    }

    public static String getMimeType(String ext) {
        return mimeMapping.get(ext);
        /**
         * 外界得以通過該方法傳入對應文件後綴，從而獲取對應的MIME值
         */


    }

}
