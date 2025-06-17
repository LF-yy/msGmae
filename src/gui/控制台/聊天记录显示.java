//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package gui.控制台;


import org.jvnet.substance.skin.SubstanceBusinessBlackSteelLookAndFeel;
import org.netbeans.lib.awtextra.AbsoluteConstraints;
import org.netbeans.lib.awtextra.AbsoluteLayout;
import server.Start;

import javax.swing.*;
import java.awt.*;
import java.io.PrintStream;

public class 聊天记录显示 extends JFrame {
    private JScrollPane jScrollPane2;
    private JTextPane 游戏端登录信息;

    public 聊天记录显示() {
        ImageIcon icon = new ImageIcon(this.getClass().getClassLoader().getResource("gui/图片/pp/2.png"));
        this.setIconImage(icon.getImage());
        this.setTitle("玩家聊天记录信息");
        this.initComponents();
    }

    public static final Start getInstance() {
        return Start.instance;
    }

    public void 游戏端登录信息输出(String str) {
        this.游戏端登录信息.setText(this.游戏端登录信息.getText() + str);
    }

    private void initComponents() {
        this.jScrollPane2 = new JScrollPane();
        this.游戏端登录信息 = new JTextPane();
        this.setResizable(false);
        this.getContentPane().setLayout(new AbsoluteLayout());
        this.游戏端登录信息.setFont(new Font("黑体", 0, 16));
        this.jScrollPane2.setViewportView(this.游戏端登录信息);
        this.getContentPane().add(this.jScrollPane2, new AbsoluteConstraints(0, 0, 500, 720));
        this.pack();
    }

    public static void main(String[] args) {
        setDefaultLookAndFeelDecorated(true);
        JDialog.setDefaultLookAndFeelDecorated(true);

        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            UIManager.setLookAndFeel(new SubstanceBusinessBlackSteelLookAndFeel());
        } catch (Exception var2) {
            //服务端输出信息.println_err(var2);
        }

        EventQueue.invokeLater(new Runnable() {
            public void run() {
                (new 聊天记录显示()).setVisible(true);
            }
        });
    }
}
