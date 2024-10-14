//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package gui.图片.gui1;

import gui.服务端输出信息;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

public class ProgressBar extends Thread implements ActionListener {
    JProgressBar jpb = new JProgressBar();
    JLabel jl = new JLabel();

    public void actionPerformed(ActionEvent e) {
        this.start();
    }

    public void run() {
        for(int i = 0; i <= 100; ++i) {
            this.jpb.setValue(i);
            this.jpb.setStringPainted(true);
            String per = (int)(this.jpb.getPercentComplete() * 100.0) + "%";
            this.jl.setText(per);

            try {
                Thread.sleep(50L);
            } catch (Exception var4) {
                服务端输出信息.println_err(var4);
            }
        }

    }

    public ProgressBar() {
        JFrame jf = new JFrame();
        jf.setLayout(new FlowLayout());
        JButton jb = new JButton("开始");
        JPanel jp = new JPanel();
        jp.setLayout(new GridLayout(2, 1));
        jp.add(this.jpb);
        jp.add(this.jl);
        jf.add(jp);
        jf.add(jb);
        jb.addActionListener(this);
        jf.pack();
        jf.setLocation(400, 300);
        jf.setSize(200, 100);
        jf.setVisible(true);
        jf.setDefaultCloseOperation(3);
    }

    public static void main(String[] args) {
        new ProgressBar();
    }
}
