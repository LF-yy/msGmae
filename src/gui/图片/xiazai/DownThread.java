//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package gui.图片.xiazai;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import javax.swing.JProgressBar;

public class DownThread extends Thread {
    private final int BUFF_LEN = 100;
    private long start;
    private long downloadstart;
    private long end;
    private InputStream inputStream;
    private RandomAccessFile raf;
    private JProgressBar jpb;
    private String cfgFileName;

    public DownThread(String name, long start, long end, InputStream is, RandomAccessFile raf, JProgressBar jp, String fileName) {
        //服务端输出信息.println_out(start + "---->" + end);
        this.setName(name);
        this.start = start;
        this.downloadstart = start;
        this.end = end;
        this.inputStream = is;
        this.raf = raf;
        this.jpb = jp;
        this.cfgFileName = fileName;
    }

    public void run() {
        try {
            FileReader reader = new FileReader(this.cfgFileName);
            BufferedReader br = new BufferedReader(reader);
            String str = null;
            if ((str = br.readLine()) != null) {
                if (str.equals("Finish")) {
                    this.jpb.setMinimum(0);
                    this.jpb.setMaximum(1);
                    this.jpb.setValue(1);
                    return;
                }

                this.downloadstart = Long.parseLong(str);
            }

            br.close();
            reader.close();
            this.inputStream.skip(this.downloadstart);
            this.raf.seek(this.downloadstart);
            writeToFile(this.cfgFileName, Long.toString(this.downloadstart));
            byte[] buff = new byte[100];
            long contentLen = this.end - this.start;
            long times = contentLen / 100L + 4L;
            this.jpb.setMinimum(0);
            this.jpb.setMaximum((int)times);
            //服务端输出信息.println_out(times + "我来看看这是个啥");

            for(int i = 0; (long)i < times && ThreadController.start; ++i) {
                if (this.downloadstart <= this.start + (long)(i * 100)) {
                    int hasRead = this.inputStream.read(buff);
                    this.downloadstart += (long)hasRead;
                    if (hasRead < 0) {
                        writeToFile(this.cfgFileName, "Finish");
                        break;
                    }

                    this.raf.write(buff, 0, hasRead);
                    if ((long)i == times - 1L) {
                        writeToFile(this.cfgFileName, "Finish");
                    } else {
                        writeToFile(this.cfgFileName, Long.toString(this.downloadstart));
                    }

                    this.jpb.setValue(i + 1);
                }
            }
        } catch (Exception var20) {
            //服务端输出信息.println_err(var20);
        } finally {
            try {
                if (this.inputStream != null) {
                    this.inputStream.close();
                }

                if (this.raf != null) {
                    this.raf.close();
                }
            } catch (Exception var19) {
                //服务端输出信息.println_err(var19);
            }

        }

    }

    private static void writeToFile(String fileName, String content) {
        try {
            FileWriter writer = new FileWriter(fileName);
            BufferedWriter bw = new BufferedWriter(writer);
            bw.write(content);
            bw.close();
            writer.close();
        } catch (FileNotFoundException var4) {
            //服务端输出信息.println_err(var4);
        } catch (IOException var5) {
            //服务端输出信息.println_err(var5);
        }

    }
}
