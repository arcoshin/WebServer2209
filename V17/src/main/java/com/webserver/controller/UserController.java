package com.webserver.controller;

import com.webserver.core.ClientHandler;
import com.webserver.entity.User;
import com.webserver.http.HttpServletRequest;
import com.webserver.http.HttpServletResponse;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.URISyntaxException;

/**
 * 用於封裝客戶相關業務方法的類
 * <p>
 * MVC模型---(預習用，後面階段著重介紹)
 * M : model 模塊層(亦稱數據層，用以封裝數據)
 * V : view 視圖層
 * C : controller 控制層
 */
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
     * 處理用戶註冊功能的業務方法
     */
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
            File file = new File(staticDir, "/myweb/reg_null.html");
            response.setContentFile(file);
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
            File file = new File(staticDir, "/myweb/reg_have_user.html");
            response.setContentFile(file);
            return;
        }

        //4.建立序列化流鏈接，進行序列化
        try (
                FileOutputStream fos = new FileOutputStream(userFile);
                ObjectOutputStream oos = new ObjectOutputStream(fos);
        ) {
            oos.writeObject(user);//要理解為什麼是使用user

            //註冊成功，顯示reg_success頁面
            File file = new File(staticDir, "/myweb/reg_success.html");
            response.setContentFile(file);




        } catch (IOException e) {
            e.printStackTrace();
        }


        System.out.println("用戶註冊完畢!");
    }
}
