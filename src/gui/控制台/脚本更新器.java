//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package gui.控制台;


import org.jvnet.substance.skin.SubstanceBusinessBlackSteelLookAndFeel;
import org.netbeans.lib.awtextra.AbsoluteConstraints;
import org.netbeans.lib.awtextra.AbsoluteLayout;

import javax.swing.*;
import java.awt.*;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class 脚本更新器 extends JFrame {
    public static int 进度 = 0;
    public static int 量 = 0;
    private JLabel jLabel1;
    private JPanel jPanel1;
    private JProgressBar 启动进度条;
    private JLabel 显示;

    public 脚本更新器() {
        ImageIcon icon = new ImageIcon(this.getClass().getClassLoader().getResource("gui/pp/2.png"));
        Image background = (new ImageIcon("gui/1.png")).getImage();
        this.setIconImage(icon.getImage());
        this.setTitle("在线更新程序");
        this.initComponents();
        PrintStream var10002 = System.out;
//        SnailMS.getInstance();
//        new GUIPrintStream(var10002, SnailMS.标准输出);
        var10002 = System.out;
//        SnailMS.getInstance();
//        new GUIPrintStream(var10002, SnailMS.错误输出);
        this.显示.setText("正在准备更新程序...");
        (new Thread() {
            public void run() {
                try {
                    Thread.sleep(5000L);
                } catch (InterruptedException var2) {
                }

            }
        }).start();
    }

    public void Z(int i) {
        this.启动进度条.setValue(i);
    }


    private void initComponents() {
        this.jPanel1 = new JPanel();
        this.启动进度条 = new JProgressBar();
        this.显示 = new JLabel();
        this.jLabel1 = new JLabel();
        this.setResizable(false);
        this.getContentPane().setLayout(new AbsoluteLayout());
        this.jPanel1.setLayout(new AbsoluteLayout());
        this.jPanel1.add(this.启动进度条, new AbsoluteConstraints(50, 70, 520, 20));
        this.显示.setFont(new Font("幼圆", 0, 24));
        this.显示.setForeground(new Color(255, 255, 255));
        this.显示.setText("ZEVMS在线更新程序");
        this.jPanel1.add(this.显示, new AbsoluteConstraints(130, 30, 370, 30));
        this.jLabel1.setIcon(new ImageIcon(this.getClass().getResource("/gui/LOGO/A1_副本.png")));
        this.jPanel1.add(this.jLabel1, new AbsoluteConstraints(0, 0, -1, -1));
        this.getContentPane().add(this.jPanel1, new AbsoluteConstraints(0, 0, 600, 170));
        this.pack();
    }

    public static void main(String[] args) {
        try {
            UIManager.LookAndFeelInfo[] var1 = UIManager.getInstalledLookAndFeels();
            int var2 = var1.length;

            for(int var3 = 0; var3 < var2; ++var3) {
                UIManager.LookAndFeelInfo info = var1[var3];
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException var6) {
            Logger.getLogger(脚本更新器.class.getName()).log(Level.SEVERE, (String)null, var6);
        } catch (InstantiationException var7) {
            Logger.getLogger(脚本更新器.class.getName()).log(Level.SEVERE, (String)null, var7);
        } catch (IllegalAccessException var8) {
            Logger.getLogger(脚本更新器.class.getName()).log(Level.SEVERE, (String)null, var8);
        } catch (UnsupportedLookAndFeelException var9) {
            Logger.getLogger(脚本更新器.class.getName()).log(Level.SEVERE, (String)null, var9);
        }

        JDialog.setDefaultLookAndFeelDecorated(true);

        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            UIManager.setLookAndFeel(new SubstanceBusinessBlackSteelLookAndFeel());
        } catch (Exception var5) {
            //服务端输出信息.println_err(var5);
        }

        EventQueue.invokeLater(new Runnable() {
            public void run() {
                (new 脚本更新器()).setVisible(true);
            }
        });
    }
}
