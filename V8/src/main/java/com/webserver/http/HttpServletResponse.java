package com.webserver.http;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * 該類實現發送響應的功能，並在該類中完成組織響應訊息
 */
public class HttpServletResponse {
    private Socket socket;
    private int statusCode = 200;//狀態碼
    private String statusReason = "OK";//狀態描述
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
        String line = "Content-Type: text/html";//注意空格不可省略
        println(line);//冗餘代碼封裝成方法
        line = "Content-Length: " + contentFile.length();//注意空格不可省略
        println(line);//冗餘代碼封裝成方法
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
        out.write(13);//回車符
        out.write(10);//換行符
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
    }
}
