//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package gui.图片.xiazai;

import gui.服务端输出信息;
import java.awt.LayoutManager;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class DownloadFrame implements ActionListener {
    ArrayList<JProgressBar> jps = new ArrayList();
    ArrayList<JLabel> jls = new ArrayList();
    JButton jbutton;
    public JTextField textField;
    public JButton button;
    public JTextField textField_1;
    public JTextField textField_2;
    public JLabel label_3;
    String sourceFile = null;
    String path = null;
    String name = null;

    public DownloadFrame() {
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == this.jbutton) {
            if (!ThreadController.start) {
                this.sourceFile = this.textField.getText();
                this.path = this.textField_1.getText();
                this.name = this.textField_2.getText();
                服务端输出信息.println_out(this.textField.getText().substring(0, 3));
                if (this.textField.getText().substring(0, 3).equals("htt")) {
                    服务端输出信息.println_out(this.textField.getText().substring(0, 3));
                    StartDownload sd = new StartDownload(this.jps, this.jls, this.sourceFile, this.name, this.path, this.label_3);
                    sd.start();
                    this.jbutton.setText("暂停\t");
                    ThreadController.start = true;
                }
            } else {
                this.jbutton.setText("继续");
                ThreadController.start = false;
            }
        }

        if (e.getSource() == this.button) {
            try {
                JFileChooser jfc = new JFileChooser();
                jfc.setFileSelectionMode(2);
                jfc.showDialog(new JLabel(), "选择");
                File file = jfc.getSelectedFile();
                if (file.isDirectory()) {
                    服务端输出信息.println_out("文件夹:" + file.getAbsolutePath());
                    String m = file.getAbsolutePath();
                    this.textField_1.setText(m);
                } else if (file.isFile()) {
                    this.textField_1.setText("是文件，请输入正确的路径");
                }
            } catch (Exception var5) {
            }
        }

    }

    private void createAndShowGUI() {
        JFrame frame = new JFrame("下载界面");
        frame.setDefaultCloseOperation(3);
        Panel pn = new Panel((LayoutManager)null);
        pn.setSize(800, 600);
        this.jbutton = new JButton("开始下载");
        this.jbutton.setMnemonic(73);
        this.jbutton.addActionListener(this);
        this.jbutton.setBounds(356, 346, 88, 44);
        pn.add(this.jbutton);
        int intWidth = 0;
        JLabel label = new JLabel("请输入下载链接");
        label.setBounds(100, 10, 100, 30);
        pn.add(label);
        this.textField = new JTextField();
        this.textField.setBounds(200, 11, 500, 30);
        pn.add(this.textField);
        this.textField.setColumns(10);
        JLabel label_1 = new JLabel("选择下载路径：");
        label_1.setBounds(100, 50, 100, 30);
        pn.add(label_1);
        this.textField_1 = new JTextField();
        this.textField_1.setBounds(200, 50, 400, 30);
        pn.add(this.textField_1);
        this.textField_1.setColumns(10);
        this.button = new JButton("选择路径");
        this.button.setBounds(610, 51, 100, 30);
        pn.add(this.button);
        this.button.addActionListener(this);
        JLabel label_2 = new JLabel("文件名：");
        label_2.setBounds(100, 90, 100, 30);
        pn.add(label_2);
        this.textField_2 = new JTextField();
        this.textField_2.setBounds(200, 90, 400, 30);
        pn.add(this.textField_2);
        this.textField_2.setColumns(10);
        this.label_3 = new JLabel("");
        this.label_3.setBounds(470, 361, 54, 15);
        pn.add(this.label_3);

        for(int i = 1; i <= 3; ++i) {
            JLabel labelDown;
            if ((i + 2) % 3 == 0) {
                labelDown = new JLabel();
                labelDown.setBounds(100, 100 * (i + 2) / 3 + 20, 700, 30);
                intWidth = 100 * (i + 2) / 3 + 20;
                pn.add(labelDown);
                this.jls.add(labelDown);
            }

            labelDown = new JLabel("Thread" + ((i + 2) % 3 + 1) + ":");
            labelDown.setBounds(100, intWidth + 30, 100, 30);
            pn.add(labelDown);
            JProgressBar progress = new JProgressBar(1, 100);
            progress.setStringPainted(true);
            progress.setName("progress" + i);
            progress.setBounds(200, intWidth + 30, 500, 30);
            pn.add(progress);
            this.jps.add(progress);
            intWidth += 40;
        }

        frame.getContentPane().add(pn);
        frame.pack();
        frame.setVisible(true);
        frame.setSize(816, 496);
    }

    public static void main(String[] args) {
        if (1==1){
            return;
        }
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                DownloadFrame downloadframe = new DownloadFrame();
                downloadframe.createAndShowGUI();
            }
        });
    }
}
