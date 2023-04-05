package com.webserver.controller;

import com.sun.org.apache.bcel.internal.generic.NEW;
import com.webserver.annotation.Controller;
import com.webserver.annotation.RequestMapping;
import com.webserver.core.ClientHandler;
import com.webserver.entity.User;
import com.webserver.http.HttpServletRequest;
import com.webserver.http.HttpServletResponse;

import java.io.*;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * 用於封裝客戶相關業務方法的類
 * <p>
 * MVC模型---(預習用，後面階段著重介紹)
 * M : model 模塊層(亦稱數據層，用以封裝數據)
 * V : view 視圖層
 * C : controller 控制層
 */
@Controller
public class UserController {
    /**
     * 用以保存所有用戶資訊文件序列化後，儲存到該目錄
     */
    private static File USER_DIR = new File("./users");
    private static File staticDir;//聲明於靜態代碼塊外，後面方法才可調用


    static {
        if (!USER_DIR.exists()) {//如目錄已存在，不需再創建
            USER_DIR.mkdirs();
        }

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

    /**
     * 專門處理用戶註冊功能的業務方法
     */
    @RequestMapping("/myweb/reg")
    public void reg(HttpServletRequest request, HttpServletResponse response) {//註冊資料來源
        System.out.println("開始處理用戶註冊...");
        //1.獲取用戶提交的數據
        String username = request.getParameters("username");
        String password = request.getParameters("password");
        String nickname = request.getParameters("nickname");
        String ageStr = request.getParameters("age");
        System.out.println(username + "," + password + "," + nickname + "," + ageStr);
        //判斷客戶資訊是否有誤
        if (username == null || password == null || nickname == null || ageStr == null || !ageStr.matches("[0-9]+")) {
//            File file = new File(staticDir, "/myweb/reg_null.html");
//            response.setContentFile(file);
            response.sendRedirect("/myweb/reg_null.html");

            //執行至此，代表其無需再進行註冊邏輯了，故直接退出即可
            return;
        }

        //2.將用戶資訊以一個User實例形式表示，並且序列化到文件中
        int age = Integer.parseInt(ageStr);
        User user = new User(username, password, nickname, age);

        //3.在user目錄中生成一個以"用戶名.obj"為文件名的文件
        File userFile = new File(USER_DIR, username + ".obj");
        //3-1 判斷是否重複註冊
        if (userFile.exists()) {
            response.sendRedirect("/myweb/reg_have_user.html");
            return;
        }

        //4.建立序列化流鏈接，進行序列化
        try (
                FileOutputStream fos = new FileOutputStream(userFile);
                ObjectOutputStream oos = new ObjectOutputStream(fos);
        ) {
            oos.writeObject(user);//要理解為什麼是使用user

            //註冊成功，顯示reg_success頁面
            response.sendRedirect("/myweb/reg_success.html");




        } catch (IOException e) {
            e.printStackTrace();
        }


        System.out.println("用戶註冊完畢!");
    }

    /**
     * 專門處理用戶登入的方法
     */
    @RequestMapping("/myweb/login")
    public void login(HttpServletRequest request, HttpServletResponse response) {
        System.out.println("開始處理用戶登入...");
        //1.獲取參數
        String username = request.getParameters("username");
        String pwd = request.getParameters("pwd");
        System.out.println(username + "," + pwd);

        //2.判斷名稱、密碼填寫是否有誤
        if (username == null || pwd == null) {
            response.sendRedirect("/myweb/login_info_error.html");
            return;
        }

        //3.執行登入邏輯

        //3-1判斷用戶名稱與密碼是否匹配
        File userFile = new File(USER_DIR, username + ".obj");
        if (userFile.exists()) {//文件需存在，說明已是註冊成功用戶
            //反序列化讀取註冊用戶資訊，比對是否匹配
            try (
                    FileInputStream fis = new FileInputStream(userFile);
                    ObjectInputStream ois = new ObjectInputStream(fis);
            ) {
                User user = (User) ois.readObject();
                System.out.println(user);
                if (user.getPassword().equals(pwd)) {//密碼是否匹配
                    //如果登入成功
                    response.sendRedirect("/myweb/login_success.html");

                    return;
                }

            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            //如果登入失敗(包含找不到用戶的註冊紀錄、用戶名或密碼錯誤)
            response.sendRedirect("myweb/login_fail.html");
            System.out.println("用戶登入處理完畢...");
        }

    }

    /**
     * 生成顯示用戶所有訊息的動態頁面
     */
    @RequestMapping("/myweb/showAllUser")
    public void showAllUser(HttpServletRequest request, HttpServletResponse response) {
        System.out.println("開始生成動態頁面...");

        //準備一個專門儲存用戶訊息的容器
        List<User> userList = new ArrayList<>();
        //將users目錄下的所有.obj文件取出
        File[] subs = USER_DIR.listFiles(f -> f.getName().endsWith(".obj"));
        //遍歷subs，獲取每一份obj文件，並將其反序列化，得到對應的user對象
        for (File userFile : subs) {
            try (
                    FileInputStream fis = new FileInputStream(userFile);
                    ObjectInputStream ois = new ObjectInputStream(fis);
            ) {
                User user = (User) ois.readObject();
                userList.add(user);
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }

            //生成動態頁面(逐行寫出)
            File file = new File("./userList.html");
            try (
                    PrintWriter pw = new PrintWriter(file, "UTF-8");
            ) {
                pw.println("<!DOCTYPE html>");
                pw.println("<html lang=\"en\">");
                pw.println("<head>");
                pw.println("<meta charset=\"UTF-8\">");
                pw.println("<title>用戶列表</title>");
                pw.println("</head>");
                pw.println("<body>");
                pw.println("<center>");
                pw.println("<h1>用戶列表</h1>");
                pw.println("<table border=\"1\">");
                pw.println("<tr>");
                pw.println("<td>用戶名</td>");
                pw.println("<td>密碼</td>");
                pw.println("<td>暱稱</td>");
                pw.println("<td>年齡</td>");
                pw.println("</tr>");
                pw.println("<tr>");
                pw.println("<td>測試人名</td>");
                pw.println("<td>測試密碼</td>");
                pw.println("<td>測試匿名</td>");
                pw.println("<td>測試年齡</td>");
                pw.println("</tr>");
                //遍歷userList，獲取每一個用戶訊息，然後拼接為html代碼
                for (User user : userList) {
                    pw.println("<tr>");
                    pw.println("<td>" + user.getUsername() + "</td>");
                    pw.println("<td>" + user.getPassword() + "</td>");
                    pw.println("<td>" + user.getNickname() + "</td>");
                    pw.println("<td>" + user.getAge() + "</td>");
                    pw.println("</tr>");
                }
                pw.println("</table>");
                pw.println("<a href='/myweb/index.html'>返回首頁</a>");
                pw.println("</center>");
                pw.println("</body>");
                pw.println("</html>");


            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            response.setContentFile(file);
        }
        System.out.println("動態頁面生成完畢");


    }
}
