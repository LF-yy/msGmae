//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package gui.图片.xiazai;

import gui.服务端输出信息;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.JProgressBar;

public class FileDownThread extends Thread {
    List<DownloadFile> downloadFileList;
    int downThreadNum;
    ArrayList<JProgressBar> jprogressbarlist = null;
    ArrayList<JLabel> jlabel = null;

    public FileDownThread(String name, List<DownloadFile> dfList, int downThreadNum, ArrayList<JProgressBar> jps, ArrayList<JLabel> jls) {
        this.setName(name);
        this.setDfList(dfList);
        this.setDownThreadNum(downThreadNum);
        this.jprogressbarlist = jps;
        this.jlabel = jls;
    }

    public void run() {
        服务端输出信息.println_out(this.getName() + " 开始 ");

        for(int i = 0; i < this.downloadFileList.size() && ThreadController.start; ++i) {
            if (((DownloadFile)this.downloadFileList.get(i)).finished == 0) {
                ((DownloadFile)this.downloadFileList.get(i)).finished = 1;
                int intMain = Integer.parseInt(this.getName().substring(this.getName().length() - 1));
                服务端输出信息.println_out("" + intMain);

                try {
                    ((JLabel)this.jlabel.get(intMain)).setText(((DownloadFile)this.downloadFileList.get(i)).sourceFile);
                } catch (Exception var4) {
                }

                MultiDown md = new MultiDown();
                md.MultiDownload(this.getName(), ((DownloadFile)this.downloadFileList.get(i)).sourceFile, ((DownloadFile)this.downloadFileList.get(i)).targetFile, this.downThreadNum, intMain, this.jprogressbarlist);
                ((DownloadFile)this.downloadFileList.get(i)).finished = 2;
            }
        }

        服务端输出信息.println_out(this.getName() + " 结束");
    }

    public List<DownloadFile> getDfList() {
        return this.downloadFileList;
    }

    public void setDfList(List<DownloadFile> dfList) {
        this.downloadFileList = dfList;
    }

    public int getDownThreadNum() {
        return this.downThreadNum;
    }

    public void setDownThreadNum(int downThreadNum) {
        this.downThreadNum = downThreadNum;
    }
}
