//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package gui;

import gui.图片.xiazai.FileDownThread;
import tools.FileoutputUtil;

import javax.swing.*;
import javax.swing.GroupLayout.Alignment;
import java.awt.*;
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class 服务端输出信息 extends JFrame {
    static ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
    消息窗口监听 documentListener;
    private JPanel jPanel1;
    private JScrollPane jScrollPane12;
    private JScrollPane jScrollPane13;
    public static JTextArea 标准输出;
    private JTabbedPane 输出窗格;
    public static JTextArea 错误输出;

    public 服务端输出信息() {
        ImageIcon icon = new ImageIcon(this.getClass().getClassLoader().getResource("gui/蓝蜗牛icon.png"));
        this.setIconImage(icon.getImage());
        this.setTitle("输出信息窗口");
        this.initComponents();
        this.documentListener = new 消息窗口监听();
        this.documentListener.setView(this);
        标准输出.getDocument().addDocumentListener(this.documentListener);
        错误输出.getDocument().addDocumentListener(this.documentListener);
    }

    private void initComponents() {
        this.jPanel1 = new JPanel();
        this.输出窗格 = new JTabbedPane();
        this.jScrollPane12 = new JScrollPane();
        标准输出 = new JTextArea();
        this.jScrollPane13 = new JScrollPane();
        错误输出 = new JTextArea();
        this.setBackground(new Color(255, 255, 255));
        this.setPreferredSize(new Dimension(820, 670));
        this.jPanel1.setBackground(new Color(255, 255, 255));
        this.jPanel1.setPreferredSize(new Dimension(800, 630));
        this.输出窗格.setBackground(new Color(255, 255, 255));
        this.jScrollPane12.setBackground(new Color(255, 255, 255));
        标准输出.setColumns(20);
        标准输出.setRows(5);
        this.jScrollPane12.setViewportView(标准输出);
        this.输出窗格.addTab("标准信息", this.jScrollPane12);
        this.jScrollPane13.setBackground(new Color(255, 255, 255));
        错误输出.setColumns(20);
        错误输出.setForeground(new Color(255, 0, 51));
        错误输出.setRows(5);
        this.jScrollPane13.setViewportView(错误输出);
        this.输出窗格.addTab("错误信息", this.jScrollPane13);
        GroupLayout jPanel1Layout = new GroupLayout(this.jPanel1);
        this.jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(jPanel1Layout.createParallelGroup(Alignment.LEADING).addGap(0, 800, 32767).addGroup(jPanel1Layout.createParallelGroup(Alignment.LEADING).addGroup(jPanel1Layout.createSequentialGroup().addGap(0, 0, 0).addComponent(this.输出窗格, -2, 800, -2).addGap(0, 0, 0))));
        jPanel1Layout.setVerticalGroup(jPanel1Layout.createParallelGroup(Alignment.LEADING).addGap(0, 630, 32767).addGroup(jPanel1Layout.createParallelGroup(Alignment.LEADING).addGroup(jPanel1Layout.createSequentialGroup().addGap(0, 0, 0).addComponent(this.输出窗格, -2, 630, -2).addGap(0, 0, 0))));
        GroupLayout layout = new GroupLayout(this.getContentPane());
        this.getContentPane().setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(Alignment.LEADING).addGap(0, 800, 32767).addGroup(layout.createParallelGroup(Alignment.LEADING).addGroup(layout.createSequentialGroup().addGap(0, 0, 32767).addComponent(this.jPanel1, -2, -1, -2).addGap(0, 0, 32767))));
        layout.setVerticalGroup(layout.createParallelGroup(Alignment.LEADING).addGap(0, 631, 32767).addGroup(layout.createParallelGroup(Alignment.LEADING).addGroup(layout.createSequentialGroup().addGap(0, 0, 32767).addComponent(this.jPanel1, -2, -1, -2).addGap(0, 0, 32767))));
        this.pack();
    }

    public static void main(String[] args) {
        try {
            UIManager.LookAndFeelInfo[] lookAndFeelInfos = UIManager.getInstalledLookAndFeels();
            for(int i = 0; i < lookAndFeelInfos.length; ++i) {
                UIManager.LookAndFeelInfo info = lookAndFeelInfos[i];
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException var5) {
            Logger.getLogger(服务端输出信息.class.getName()).log(Level.SEVERE, (String)null, var5);
        } catch (InstantiationException var6) {
            Logger.getLogger(服务端输出信息.class.getName()).log(Level.SEVERE, (String)null, var6);
        } catch (IllegalAccessException var7) {
            Logger.getLogger(服务端输出信息.class.getName()).log(Level.SEVERE, (String)null, var7);
        } catch (UnsupportedLookAndFeelException var8) {
            Logger.getLogger(服务端输出信息.class.getName()).log(Level.SEVERE, (String)null, var8);
        }

        EventQueue.invokeLater(new Runnable() {
            public void run() {
                (new 服务端输出信息()).setVisible(true);
            }
        });
    }

    public static void println_err(final String message) {
        singleThreadExecutor.execute(new Runnable() {
            public void run() {
                FileoutputUtil.logToFile("logs/errors.txt", FileoutputUtil.NowTime() + ": " + message + "\r\n");
                System.err.println(message);
            }
        });
    }

    public static void println_err(final Throwable message) {
        singleThreadExecutor.execute(new Runnable() {
            public void run() {
                FileoutputUtil.logToFile("logs/errors.txt", FileoutputUtil.NowTime() + ": " + message + "\r\n");
                System.err.println(message);
            }
        });
    }

    public static void println_err(final SQLException message) {
        singleThreadExecutor.execute(new Runnable() {
            public void run() {
                FileoutputUtil.logToFile("logs/errors.txt", FileoutputUtil.NowTime() + ": " + message + "\r\n");
                System.err.println(message);
            }
        });
    }

    public static void println_err(final Exception message) {
        singleThreadExecutor.execute(new Runnable() {
            public void run() {
                FileoutputUtil.logToFile("logs/errors.txt", FileoutputUtil.NowTime() + ": " + message + "\r\n");
                System.err.println(message);
            }
        });
    }

    public static void println_out(final String message) {
        singleThreadExecutor.execute(new Runnable() {
            public void run() {
                System.out.println(message);
            }
        });
    }

    public static void println_out(final FileDownThread message) {
        singleThreadExecutor.execute(new Runnable() {
            public void run() {
                System.out.println(message);
            }
        });
    }

    public static void println_out(final SQLException message) {
        singleThreadExecutor.execute(new Runnable() {
            public void run() {
                System.out.println(message);
            }
        });
    }

    public static void println_out(final Exception message) {
        singleThreadExecutor.execute(new Runnable() {
            public void run() {
                System.out.println(message);
            }
        });
    }
}
