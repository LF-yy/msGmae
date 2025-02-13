//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package gui.图片.shuchu;


import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

public class Listing2 {
    static PipedInputStream pipedIS = new PipedInputStream();
    static PipedOutputStream pipedOS = new PipedOutputStream();

    public Listing2() {
    }

    public static void main(String[] a) {
        try {
            pipedIS.connect(pipedOS);
        } catch (IOException var5) {
            //服务端输出信息.println_err("连接失败");
            System.exit(1);
        }

        byte[] inArray = new byte[10];
        byte[] outArray = new byte[20];

        try {
            pipedOS.write(outArray, 0, 20);
            //服务端输出信息.println_out("\t 已发送20字节...");
            int bytesRead = pipedIS.read(inArray, 0, 10);

            for(int i = 0; bytesRead != -1; bytesRead = pipedIS.read(inArray, 0, 10)) {
                pipedOS.write(outArray, 0, 20);
                //服务端输出信息.println_out("\t 已发送20字节..." + i);
                ++i;
            }
        } catch (IOException var6) {
            //服务端输出信息.println_err("读取pipedIS时出现错误: " + var6);
            System.exit(1);
        }

    }
}
