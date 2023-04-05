package com.webserver.http;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static com.webserver.http.HttpContext.CR;
import static com.webserver.http.HttpContext.LF;

public class HttpServletRequest {
    private Socket socket;
    //請求行的相關訊息
    private String method;//請求方式
    private String uri;//請求路徑
    private String protocol;//協議版本
    private String requestURI;//儲存uri中"?"左側的請求部分
    private String queryString;//儲存uri中"?"右側的參數部分

    //儲存客戶端提交參數的Map
    private Map<String, String> parameters = new HashMap<>();

    //消息標題的相關訊息
    private Map<String, String> headers = new HashMap();

    public String getMethod() {
        return method;
    }

    public String getUri() {
        return uri;
    }

    public String getProtocol() {
        return protocol;
    }

    /**
     * 根據消息頭的名字獲取headers中對應的消息頭的值
     */
    public String getHeaders(String name) {//此處原本返回一個Map要特別注意
        return headers.get(name);
    }

    /**
     * 實例化請求對象的過程就是解析請求的過程
     */
    public HttpServletRequest(Socket socket) throws IOException, EmptyRequestException {
        this.socket = socket;
        //1.1解析請求行
        parseRequestLine();
        //1.2解析消息標題
        parseHeaders();
        //1.3解析消息正文
        parseContent();


    }

    /**
     * 解析請求行
     */
    private void parseRequestLine() throws IOException, EmptyRequestException {//解析請求行
        String line = readline();
        //判斷是否為空值(空請求)，空請求時省略後面步驟。
        if (line.isEmpty()) {
            throw new EmptyRequestException();
        }
        System.out.println(line);


        //根據請求行的規則，通過空格將請求行拆分為三部分，分別賦值三個變量
        String[] data = line.split("\\s");
        method = data[0];
        uri = data[1];
        protocol = data[2];

        //進行抽象路徑的二次解析
        parseURI();

        //測試路徑: http://localhost:9188/myweb/index.html
        System.out.println("請求方式: " + method);
        System.out.println("抽象路徑: " + uri);
        System.out.println("協議版本: " + protocol);
    }

    /**
     * 進一步解析抽象路徑URI
     */
    private void parseURI() {
        //1.根據"?"拆成兩部分
        String[] data = uri.split("\\?");
        //2.將data第一個元素賦值給請求部分
        requestURI = data[0];
        //3.根據data長度判斷本次URI是否包含參數部分，若不包含則解析參數
        if (data.length > 1) {
            //4.將data第二個元素賦值給參數部分
            queryString = data[1];
            parseParameters(queryString);


        }
        System.out.println("requestURI" + requestURI);
        System.out.println("queryString" + queryString);
        System.out.println("parameters" + parameters);
    }

    /**
     * 解析參數
     */
    private void parseParameters(String line) {
        String[] data;//5.將queryString根據"&"拆分成數個鍵值對
        data = line.split("&");
        //6.遍歷data中每個元素
        for (String para : data) {
            //7.將每份元素根據"="拆分
            String[] paras = para.split("=");
            //8.將paras中的數據存入parameters中(Map)
            /**
             * 如果客戶端輸入空白文件，會導致拆分時沒有後半部(如age=)
             * 這時paras[1]不存在，故服務氣會報錯數組下標越界
             * 可以使用三元運算式
             */
            parameters.put(paras[0], paras.length > 1 ? paras[1] : null);
        }
    }

    /**
     * 解析消息頭
     */
    private void parseHeaders() throws IOException {//解析消息頭
        while (true) {
            String line = readline();
            if (line.isEmpty()) {
                break;
            }
            System.out.println("消息頭: " + line);
            //將消息頭按照冒號空格的形式拆分
            String[] data = line.split(":\\s");
            //將消息頭的名字作為key，消息頭的值作為value，儲存至headers
            headers.put(data[0], data[1]);
        }
    }

    /**
     * 解析消息正文
     */
    private void parseContent() throws IOException {//解析消息正文
        System.out.println("開始解析消息正文...");
        //判斷是否含有消息正文(是否消息頭中包含Content-Length)
        if (headers.containsKey("Content-Length")) {
            int contentLength = Integer.parseInt(headers.get("Content-Length"));
            System.out.println("正文長度" + contentLength);

            //基於正文大小創建字節數組以完成塊讀
            byte[] contentData = new byte[contentLength];
            InputStream in = socket.getInputStream();
            //一次性讀取基於字節數組大小的字節內容，並儲存於該數組中
            in.read(contentData);
            //正文可能不只包含參數，需判斷正文是否為參數數據
            if ("application/x-www-form-urlencoded".equals(headers.get("Content-Type"))) {
                //將contentData字節數組，還原為字符串
                String line = new String(contentData, StandardCharsets.UTF_8);
                System.out.println("正文內容" + line);
                parseParameters(line);
            }

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
            if (pre == CR && cur == LF) {
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

    public String getRequestURI() {
        return requestURI;
    }

    public String getQueryString() {
        return queryString;
    }

    public String getParameters(String KeyName) {
        return parameters.get(KeyName);
    }
}