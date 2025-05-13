//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package gui.图片.shuchu;


import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

public class Listing3 {
    static PipedInputStream pipedIS = new PipedInputStream();
    static PipedOutputStream pipedOS = new PipedOutputStream();

    public Listing3() {
    }

    public static void main(String[] args) {
        try {
            pipedIS.connect(pipedOS);
        } catch (IOException var4) {
            //服务端输出信息.println_err("连接失败");
            System.exit(1);
        }

        byte[] inArray = new byte[10];
        startWriterThread();

        try {
            for(int bytesRead = pipedIS.read(inArray, 0, 10); bytesRead != -1; bytesRead = pipedIS.read(inArray, 0, 10)) {
                //服务端输出信息.println_out("已经读取" + bytesRead + "字节...");
            }
        } catch (IOException var5) {
            //服务端输出信息.println_err("读取输入错误.");
            System.exit(1);
        }

    }

    private static volatile boolean isRunning = true;
    //优化
    private static void startWriterThread() {
        new Thread(() -> {
            byte[] outArray = new byte[2000];
            Thread.currentThread().setName("WriterThread");

            while (isRunning) {
                try {
                    Listing3.pipedOS.write(outArray, 0, 2000);
                } catch (IOException e) {
                    // 更合理的异常处理：打印日志并退出当前线程
                    e.printStackTrace();
                    isRunning = false; // 关闭循环
                    return;
                }

                // 服务端输出信息.println_out("\t 已经发送2000字节...");
            }
        }).start();
    }
}
