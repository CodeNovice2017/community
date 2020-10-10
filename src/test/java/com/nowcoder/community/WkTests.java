package com.nowcoder.community;

import java.io.IOException;

public class WkTests {

    public static void main(String[] args) {
        String cmd = "wkhtmltoimage --quality 75 https://www.nowcoder.com C:/workspace/wk/1.png";
        try {
            // 运行测试的话,会直接在控制台显示ok.
            // 因为Runtime执行命令都是把命令提交给操作系统,剩下的事情都交给了操作系统,这个时候Java是不会等操作系统做一个返回的,直接就向下执行了,
            // 也就是说操作系统执行命令和Java主程序是并发的,是异步的
            Runtime.getRuntime().exec(cmd);
            System.out.println("ok.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
