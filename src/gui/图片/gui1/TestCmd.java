//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package gui.图片.gui1;


import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class TestCmd {
    public TestCmd() {
    }

    public boolean getProcess() {
        boolean flag = false;
        ByteArrayOutputStream baos = null;
        InputStream os = null;
        String s = "";

        try {
            try {
                Process p = Runtime.getRuntime().exec("");
                baos = new ByteArrayOutputStream();
                os = p.getInputStream();
                byte[] b = new byte[256];

                while(os.read(b) > 0) {
                    baos.write(b);
                }

                s = baos.toString();
                if (s.indexOf("exe") >= 0) {
                    //服务端输出信息.println_out("已经运行YY");
                    flag = true;
                } else {
                    //服务端输出信息.println_out("未运行YY");
                    flag = false;
                }
            } catch (Exception var10) {
                //服务端输出信息.println_err(var10);
            }

            return flag;
        } finally {
            ;
        }
    }

    public static void main(String[] args) {
        (new TestCmd()).getProcess();
        //服务端输出信息.println_out("Hello World!");
    }
}
