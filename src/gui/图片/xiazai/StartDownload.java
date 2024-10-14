//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package gui.图片.xiazai;

import gui.服务端输出信息;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.JProgressBar;

public class StartDownload extends Thread {
    ArrayList<JProgressBar> jpList = null;
    ArrayList<JLabel> jlList = null;
    static String sourceFile;
    static String name1;
    static String path1;
    JLabel label1;
    static final int FILE_THREAD_NUM = 3;
    static final int DOWN_THREAD_NUM = 3;

    public static void main(String[] args) {
    }

    public StartDownload(ArrayList<JProgressBar> jps, ArrayList<JLabel> jls, String path, String name, String path2, JLabel label) {
        this.jpList = jps;
        this.jlList = jls;
        sourceFile = path;
        name1 = name;
        path1 = path2;
        this.label1 = label;
    }

    public void run() {
        List<DownloadFile> fileList = new ArrayList();
        buildList(fileList);
        服务端输出信息.println_out("下载开始");
        this.label1.setText("下载开始");
        List<FileDownThread> list = new ArrayList();

        FileDownThread my;
        for(int i = 0; i < 3; ++i) {
            my = new FileDownThread("线程 " + i, fileList, 3, this.jpList, this.jlList);
            服务端输出信息.println_out(my);
            my.start();
            list.add(my);
        }

        try {
            Iterator var6 = list.iterator();

            while(var6.hasNext()) {
                my = (FileDownThread)var6.next();
                my.join();
            }
        } catch (InterruptedException var5) {
            服务端输出信息.println_err(var5);
        }

        服务端输出信息.println_out("下载完成");
        this.label1.setText("下载结束");
    }

    private static void buildList(List<DownloadFile> fileList) {
        DownloadFile downloadfile1 = new DownloadFile();
        String a = path1 + "\\" + name1;
        downloadfile1.sourceFile = sourceFile;
        downloadfile1.targetFile = a;
        downloadfile1.finished = 0;
        fileList.add(downloadfile1);
    }
}
