package com.nowcoder.community;

import java.io.IOException;

public class WkTests {

    public static void main(String[] args) {
        String cmd = "D:/work/wk/wkhtmltox/bin/wkhtmltoimage --quality 75  https://www.baidu.com D:\\work\\wk\\data\\img\\1.png";
        try {
            Runtime.getRuntime().exec(cmd);
            System.out.println("ok.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
