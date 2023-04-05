package com.webserver.http;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
/**
 * 靜態導入
 */
import static com.webserver.http.HttpContext.LF;

/**
 * 該類實現發送響應的功能，並在該類中完成組織響應訊息
 */
public class HttpServletResponse {
    private Socket socket;
    private int statusCode = 200;//狀態碼
    private String statusReason = "OK";//狀態描述
    private Map<String, String> headers = new HashMap<>();//蒐集響應頭相關訊息(蒐集要發送的響應頭)
    private File contentFile;//響應正文的相關訊息

    public HttpServletResponse(Socket socket) {
        this.socket = socket;
    }

    /**
     * 發送響應
     * 將當前的響應對象內容按照(響應訊息的)標準格式發送給客戶端
     */
    public void response() throws IOException {
        //3.1 發送狀態行
        sendStatusLine();

        //3.2 發送響應頭
        sendHeaders();

        //3.3 發送響應正文
        sendContent();

        System.out.println("響應發送完畢!!");

    }

    /**
     * 發送狀態行
     */
    private void sendStatusLine() throws IOException {
        /**
         * String line = "HTTP/1.1 狀態碼 狀態描述";//HTTP/1.1冗餘，只需要狀態碼跟狀態描述
         * */
        String line = "HTTP/1.1" + " " + statusCode + " " + statusReason;//空格要記得不可忽略
        println(line);//冗餘代碼封裝成方法
    }

    /**
     * 發送響應頭
     */
    private void sendHeaders() throws IOException {
        //遍歷headers，將其中的所有響應頭的名字和值進行組裝與發送
        //將Map中的每一組鍵值對封裝為entry對象，再封裝給Set集合
        Set<Map.Entry<String, String>> entries = headers.entrySet();
        //遍歷set集合，獲取每一個鍵值對
        for (Map.Entry<String, String> entry : entries) {
            String name = entry.getKey();//獲取響應頭的名字
            String value = entry.getValue();//獲取響應頭的值
            //將名字和值按照響應頭格式重新組合
            String line = name + ": " + value;
            println(line);
            System.out.println("響應頭→" + line);
        }
        println("");//空字符串自動輸出回車換行
    }

    /**
     * 發送響應正文
     */
    private void sendContent() throws IOException {
        OutputStream out = socket.getOutputStream();
        byte[] buf = new byte[10 * 1024];//塊讀
        try (
                FileInputStream fis = new FileInputStream(contentFile);
        ) {
            int len;//每次讀寫的字節量
            while ((len = fis.read(buf)) != -1) {
                out.write(buf, 0, len);//讀多少寫多少模擬flash
            }
            System.out.println("響應發送完畢");//打樁語句
        }

    }

    /**
     * 將指定字符串發送給瀏覽器
     */
    private void println(String line) throws IOException {
        OutputStream out = socket.getOutputStream();
        byte[] data = line.getBytes(StandardCharsets.ISO_8859_1);
        out.write(data);
        out.write(HttpContext.CR);//回車符
        out.write(LF);//換行符，此處使用靜態導入
        //如果沒有註釋，會忘記13跟10分別代表的意義是什麼
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getStatusReason() {
        return statusReason;
    }

    public void setStatusReason(String statusReason) {
        this.statusReason = statusReason;
    }

    public File getContentFile() {
        return contentFile;
    }

    public void setContentFile(File contentFile) {
        this.contentFile = contentFile;
        //獲取文件名(含後綴) index.html
        String fileName = contentFile.getName();
        //根據文件名獲取其中包含的文件後綴
        /**
         * substring(int index) 根據下標擷取字符串
         * indexOf(".") 獲取第一次出現指定字符的下標(從左到右)
         * lastIndexOf(".") 獲取最後一次出現指定字符的下標(從右到左)
         */

//            fileName.substring(fileName.indexOf("."));//不建議，因為文件名可能出現兩個.
        String ext = fileName.substring(fileName.lastIndexOf(".") + 1);//+1以免因為下標取到"."

        //根據文件後綴設置對應的MIME值
        String mime = HttpContext.getMimeType(ext);
        //發送響應頭
        addHeaders("Content-Type", mime);
        addHeaders("Content-Length", contentFile.length() + "");
    }

    /**
     * 外界可以通過這個方法給headers添加響應頭的名字和值
     */
    public void addHeaders(String name, String value) {
        headers.put(name, value);
    }
}
