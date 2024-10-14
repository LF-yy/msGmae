//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package gui.图片.shuchu;

import gui.服务端输出信息;
import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.InputStream;
import javax.swing.JFrame;
import javax.swing.JScrollPane;

public class AppOutputCapture {
    private static Process process;

    public AppOutputCapture() {
    }

    public static void main(String[] args) {
        if (args.length == 0) {
            服务端输出信息.println_err("用法：java AppOutputCapture <程序名字> {参数1 参数2 ...}");
            System.exit(0);
        }

        try {
            process = Runtime.getRuntime().exec(args);
        } catch (IOException var4) {
            服务端输出信息.println_err("创建进程时出错...\n" + var4);
            System.exit(1);
        }

        InputStream[] inStreams = new InputStream[]{process.getInputStream(), process.getErrorStream()};
        ConsoleTextArea cta = new ConsoleTextArea(inStreams);
        cta.setFont(Font.decode("monospaced"));
        JFrame frame = new JFrame(args[0] + "控制台输出");
        frame.getContentPane().add(new JScrollPane(cta), "Center");
        frame.setBounds(50, 50, 400, 400);
        frame.setVisible(true);
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent evt) {
                AppOutputCapture.process.destroy();

                try {
                    AppOutputCapture.process.waitFor();
                } catch (InterruptedException var3) {
                }

                System.exit(0);
            }
        });
    }
}
