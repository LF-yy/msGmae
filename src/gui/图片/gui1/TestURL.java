//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package gui.图片.gui1;


import javazoom.jl.player.Player;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;

public class TestURL {
    public TestURL() {
    }

    public static void main(String[] args) throws IOException {
        test4();
    }

    public static void test4() throws IOException {
        URL url = new URL("http://服务端控制/配置检测开关.txt");
        Reader reader = new InputStreamReader(new BufferedInputStream(url.openStream()));

        int c;
        while((c = reader.read()) != -1) {
            //服务端输出信息.println_out("" + (char)c);
        }

        reader.close();
    }

    public static void test42() throws IOException {
        URL url = new URL("http://服务端控制/配置检测开关.txt");
        Object obj = url.getContent();

        try {
            BufferedInputStream buffer = new BufferedInputStream(new FileInputStream(obj.getClass().getName()));
            Player player = new Player(buffer);
            player.play();
        } catch (Exception var4) {
        }

        //服务端输出信息.println_out(obj.getClass().getName());
    }

    public static void test3() throws IOException {
        URL url = new URL("");
        URLConnection uc = url.openConnection();
        InputStream in = uc.getInputStream();

        int c;
        while((c = in.read()) != -1) {
            System.out.print(c);
        }

        in.close();
    }

    public static void test2() throws IOException {
        URL url = new URL("");
        Reader reader = new InputStreamReader(new BufferedInputStream(url.openStream()));

        int c;
        while((c = reader.read()) != -1) {
            System.out.print((char)c);
        }

        reader.close();
    }

    public static void test() throws IOException {
        URL url = new URL("");
        InputStream in = url.openStream();

        int c;
        while((c = in.read()) != -1) {
            System.out.print(c);
        }

        in.close();
    }

    public static void 获取游戏公告1() throws IOException {
        URL url = new URL("http:///新闻.txt");
        Reader reader = new InputStreamReader(new BufferedInputStream(url.openStream()));

        int c;
        while((c = reader.read()) != -1) {
            if (c == 48) {
                System.out.print("开");
            } else if (c == 49) {
                System.out.print("关");
            } else {
                System.out.print("错误！！！");
            }
        }

        reader.close();
    }
}
