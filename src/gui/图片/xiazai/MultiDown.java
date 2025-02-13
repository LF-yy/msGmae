//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package gui.图片.xiazai;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.JProgressBar;

public class MultiDown {
    public MultiDown() {
    }

    public void MultiDownload(String name, String sourceFile, String targetFile, int threadNum, int intMain, ArrayList<JProgressBar> jpList) {
        File tf = new File(targetFile);
        if (!tf.exists()) {
            int DOWN_THREAD_NUM = threadNum;
            String DL_FILE_NAME = targetFile + ".dl";
            String CFG_FILE_NAME = targetFile + ".cfg";
            InputStream[] isArr = new InputStream[threadNum];
            RandomAccessFile[] outArr = new RandomAccessFile[threadNum];

            try {
                URL url = new URL(sourceFile);
                isArr[0] = url.openStream();
                long fileLen = getFileLength(url);
                //服务端输出信息.println_out(sourceFile + "的大小" + fileLen);
                outArr[0] = new RandomAccessFile(DL_FILE_NAME, "rw");
                outArr[0].setLength(fileLen);
                long numPerThred = fileLen / (long)DOWN_THREAD_NUM;
                long left = fileLen % (long)DOWN_THREAD_NUM;
                List<DownThread> list = new ArrayList();

                File f1;
                for(int i = 0; i < DOWN_THREAD_NUM; ++i) {
                    f1 = new File(CFG_FILE_NAME + i);
                    if (!f1.exists()) {
                        f1.createNewFile();
                    }

                    if (i != 0) {
                        isArr[i] = url.openStream();
                        outArr[i] = new RandomAccessFile(DL_FILE_NAME, "rw");
                    }

                    DownThread dt;
                    if (i == DOWN_THREAD_NUM - 1) {
                        dt = new DownThread(name + "-" + i, (long)i * numPerThred, (long)(i + 1) * numPerThred + left, isArr[i], outArr[i], (JProgressBar)jpList.get(intMain * 3 + i), CFG_FILE_NAME + i);
                        dt.start();
                        list.add(dt);
                    } else {
                        dt = new DownThread(name + "-" + i, (long)i * numPerThred, (long)(i + 1) * numPerThred, isArr[i], outArr[i], (JProgressBar)jpList.get(intMain * 3 + i), CFG_FILE_NAME + i);
                        dt.start();
                        list.add(dt);
                    }
                }

                try {
                    Iterator var28 = list.iterator();

                    while(var28.hasNext()) {
                        DownThread my = (DownThread)var28.next();
                        my.join();
                    }

                    boolean finishFlg = true;

                    for(int i = 0; i < DOWN_THREAD_NUM; ++i) {
                        FileReader reader = new FileReader(CFG_FILE_NAME + i);
                        BufferedReader br = new BufferedReader(reader);
                        String str = null;

                        while((str = br.readLine()) != null) {
                            if (!str.equals("Finish")) {
                                finishFlg = false;
                                break;
                            }
                        }

                        br.close();
                        reader.close();
                    }

                    if (finishFlg) {
                        f1 = new File(DL_FILE_NAME);
                        File f2 = new File(targetFile);
                        if (f2.exists()) {
                            f2.delete();
                        }

                        if (f1.exists()) {
                            f1.renameTo(f2);
                        }

                        for(int i = 0; i < DOWN_THREAD_NUM; ++i) {
                            File fileConfig = new File(CFG_FILE_NAME + i);
                            fileConfig.delete();
                        }
                    }
                } catch (InterruptedException var26) {
                    //服务端输出信息.println_err(var26);
                }
            } catch (Exception var27) {
                //服务端输出信息.println_err(var27);
            }

        }
    }

    public static long getFileLength(URL url) throws Exception {
        long length = 0L;
        URLConnection con = url.openConnection();
        con.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
        long size = (long)con.getContentLength();
        return size;
    }
}
