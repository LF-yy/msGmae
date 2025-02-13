//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package gui.图片.动图;


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.LayoutManager;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class JpanelForm {
    JFrame frame = new JFrame("动态panel");
    final JPanel p1 = new JPanel();
    final JPanel p2 = new JPanel();
    final JPanel p3 = new JPanel();
    JPanel contentPanel = new JPanel();
    JPanel topPanel = new JPanel();
    final JPanel centerPanel = new JPanel();

    public JpanelForm() {
        this.frame.setSize(600, 450);
        this.contentPanel.setBackground(Color.GRAY);
        this.frame.setContentPane(this.contentPanel);
        this.contentPanel.setLayout(new BorderLayout());
        this.topPanel.setBackground(Color.yellow);
        this.topPanel.setPreferredSize(new Dimension(this.contentPanel.getWidth(), 50));
        this.centerPanel.setBackground(Color.WHITE);
        this.centerPanel.setLayout((LayoutManager)null);
        this.p1.setBackground(Color.BLUE);
        this.p2.setBackground(Color.GREEN);
        this.p3.setBackground(Color.RED);
        this.p1.add(new JLabel("===============panel1======="));
        this.p2.add(new JLabel("===============panel2======="));
        this.p3.add(new JLabel("===============panel3======="));
        JButton jb1 = new JButton("panel1");
        jb1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JpanelForm.this.xiaoGuo(JpanelForm.this.p1);
            }
        });
        JButton jb2 = new JButton("panel2");
        jb2.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JpanelForm.this.xiaoGuo(JpanelForm.this.p2);
            }
        });
        JButton jb3 = new JButton("panel3");
        jb3.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JpanelForm.this.xiaoGuo(JpanelForm.this.p3);
                //服务端输出信息.println_out("panel3========================/n");
            }
        });
        JButton jb4 = new JButton("返回");
        jb4.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JpanelForm.this.returnPanel(JpanelForm.this.p1);
            }
        });
        this.topPanel.add(jb1);
        this.topPanel.add(jb2);
        this.topPanel.add(jb3);
        this.topPanel.add(jb4);
        this.contentPanel.add(this.topPanel, "North");
        this.contentPanel.add(this.centerPanel, "Center");
        this.frame.show();
        this.frame.setDefaultCloseOperation(3);
    }

    public void returnPanel(final JPanel panel) {
        panel.setBounds(0, 0, this.centerPanel.getWidth(), this.centerPanel.getHeight());
        int count = this.centerPanel.getComponentCount();
        List list = new ArrayList();
        Component[] var4 = this.centerPanel.getComponents();
        int var5 = var4.length;

        for(int var6 = 0; var6 < var5; ++var6) {
            Component comp = var4[var6];
            list.add(comp);
        }

        if (count > 0) {
            for(int i = 0; i < count; ++i) {
                Component comp = this.centerPanel.getComponent(i);
                if (comp instanceof JPanel) {
                    final JPanel currentPanel = (JPanel)comp;
                    if (currentPanel != panel) {
                        (new Thread() {
                            public void run() {
                                Rectangle rec = currentPanel.getBounds();
                                int y = JpanelForm.this.centerPanel.getWidth();

                                for(int i = 0; i >= -JpanelForm.this.centerPanel.getWidth(); i -= 10) {
                                    currentPanel.setBounds(i, 0, JpanelForm.this.centerPanel.getWidth(), JpanelForm.this.centerPanel.getHeight());
                                    panel.setBounds(y, 0, JpanelForm.this.centerPanel.getWidth(), JpanelForm.this.centerPanel.getHeight());

                                    try {
                                        Thread.sleep(5L);
                                    } catch (InterruptedException var5) {
                                        //服务端输出信息.println_err(var5);
                                    }

                                    y -= 10;
                                }

                                JpanelForm.this.centerPanel.remove(currentPanel);
                                panel.setBounds(0, 0, JpanelForm.this.centerPanel.getWidth(), JpanelForm.this.centerPanel.getHeight());
                            }
                        }).start();
                        break;
                    }
                }
            }
        }

        if (!list.contains(panel)) {
            this.centerPanel.add(panel);
        }

        this.centerPanel.validate();
        this.centerPanel.repaint();
    }

    public void xiaoGuo(final JPanel panel) {
        panel.setBounds(0, 0, this.centerPanel.getWidth(), this.centerPanel.getHeight());
        int count = this.centerPanel.getComponentCount();
        List list = new ArrayList();
        Component[] var4 = this.centerPanel.getComponents();
        int var5 = var4.length;

        for(int var6 = 0; var6 < var5; ++var6) {
            Component comp = var4[var6];
            list.add(comp);
        }

        if (count > 0) {
            for(int i = 0; i < count; ++i) {
                Component comp = this.centerPanel.getComponent(i);
                if (comp instanceof JPanel) {
                    final JPanel currentPanel = (JPanel)comp;
                    if (currentPanel != panel) {
                        (new Thread() {
                            public void run() {
                                Rectangle rec = currentPanel.getBounds();
                                int y = -JpanelForm.this.centerPanel.getWidth();

                                for(int i = 0; i <= JpanelForm.this.centerPanel.getWidth(); i += 10) {
                                    currentPanel.setBounds(i, 0, JpanelForm.this.centerPanel.getWidth(), JpanelForm.this.centerPanel.getHeight());
                                    panel.setBounds(y, 0, JpanelForm.this.centerPanel.getWidth(), JpanelForm.this.centerPanel.getHeight());

                                    try {
                                        Thread.sleep(5L);
                                    } catch (InterruptedException var5) {
                                        //服务端输出信息.println_err(var5);
                                    }

                                    y += 10;
                                }

                                JpanelForm.this.centerPanel.remove(currentPanel);
                                panel.setBounds(0, 0, JpanelForm.this.centerPanel.getWidth(), JpanelForm.this.centerPanel.getHeight());
                            }
                        }).start();
                        break;
                    }
                }
            }
        }

        if (!list.contains(panel)) {
            this.centerPanel.add(panel);
        }

        this.centerPanel.validate();
        this.centerPanel.repaint();
    }

    public static void main(String[] args) {
        new JpanelForm();
    }
}
