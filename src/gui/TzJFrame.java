package gui;

import com.alibaba.fastjson.JSONObject;
import constants.tzjc;
import database.DBConPool;
import server.MapleItemInformationProvider;
import server.Start;
import tools.FileoutputUtil;
import tools.Pair;
import util.GetRedisDataUtil;
import util.RedisUtil;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class TzJFrame extends JFrame
{

    private JButton 重载套装加成;
    private static JTable 套装伤害加成表;
    private static JTextField 套装个数;
    private static JTextField 套装加成比例;
    private static JTextField 套装名字;
    private JTextField 套装名字输入;
    private static JToggleButton 套装属性加成开关;
    private JTextField 套装排序输入;
    private JButton 套装查询;
    private JButton 套装查询1;
    private static JTextField 套装编码;
    private static JTextField 套装道具代码;
    private JButton 套装道具修改;
    private JButton 套装道具删除;
    private JButton 套装道具增加;
    private static JTextField 现套装代码;
    private static JTextField 原套装代码;
    private JButton 装备加成伤害列表初始化;

    private JPanel jPanel90t;
    private JPanel jPanel91t;
    private JPanel jPanel92t;
    private JPanel jPanel93t;
    private JPanel jPanel94t;
    private JLabel jLabel197t;
    private JLabel jLabel198t;
    private JLabel jLabel199t;
    private JLabel jLabel200t;
    private JLabel jLabel201t;
    private JLabel jLabel202t;
    private JLabel jLabel203t;
    private JLabel jLabel204t;
    private JLabel jLabel205t;
        private JLabel jLabel192 ;
        private JLabel jLabel192t;
        private JLabel jLabel193 ;
        private JLabel jLabel193t;
        private JLabel jLabel194;
        private JLabel jLabel194t;
        private JLabel jLabel195;
        private JLabel jLabel195t;
        private JLabel jLabel196;
        private JLabel jLabel196t;
     private JScrollPane jScrollPane19t;
    private JTabbedPane jTabbedPane2;
    public TzJFrame()
    {
        setTitle("套装赋能"); //设置显示窗口标题
        setSize(1250,700); //设置窗口显示尺寸
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); //置窗口是否可以关闭
//        JLabel jl=new JLabel("这是使用JFrame类创建的窗口"); //创建一个标签
        Container c = getContentPane(); //获取当前窗口的内容窗格
        setconfig();
        c.add(jTabbedPane2); //将标签组件添加到内容窗格上
        setVisible(true); //设置窗口是否可见


    }


    private void setconfig(){
       this.jTabbedPane2 = new JTabbedPane();
       this.jPanel90t = new JPanel();
       this.jPanel91t = new JPanel();
       this.jPanel92t = new JPanel();
       this.jPanel93t = new JPanel();
       this.jPanel94t = new JPanel();
       this.jLabel197t = new JLabel();
       this.jLabel198t = new JLabel();
       this.jLabel199t = new JLabel();
       this.jLabel200t = new JLabel();
       this.jLabel201t = new JLabel();
       this.jLabel202t = new JLabel();
       this.jLabel203t = new JLabel();
       this.jLabel204t = new JLabel();
       this.jLabel204t = new JLabel();
       this.jLabel205t = new JLabel();
       this.装备加成伤害列表初始化 = new JButton();
       this.套装伤害加成表 = new JTable();
       this.套装伤害加成表 = new JTable();
       this.套装个数 = new JTextField();
       this.套装加成比例 = new JTextField();
       this.套装名字 = new JTextField();
       this.套装名字输入 = new JTextField();
       this.套装属性加成开关 = new JToggleButton();
       this.套装排序输入 = new JTextField();
       this.套装查询 = new JButton();
       this.套装查询1 = new JButton();
       this.套装编码 = new JTextField();
       this.套装道具代码 = new JTextField();
       this.套装道具修改 = new JButton();
       this.套装道具删除 = new JButton();
       this.套装道具增加 = new JButton();
       this.现套装代码 = new JTextField();
       this.原套装代码 = new JTextField();
       this.重载套装加成 = new JButton();
        this.jScrollPane19t = new JScrollPane();
       this.jLabel192 = new JLabel();
       this.jLabel192t = new JLabel();
       this.jLabel193 = new JLabel();
       this.jLabel193t = new JLabel();
       this.jLabel194 = new JLabel();
       this.jLabel194t = new JLabel();
       this.jLabel195 = new JLabel();
       this.jLabel195t = new JLabel();
       this.jLabel196 = new JLabel();
       this.jLabel196t = new JLabel();

        jPanel91t.setBorder((Border) BorderFactory.createTitledBorder("套装散件比例加成"));
       this.套装伤害加成表.setModel((TableModel) new DefaultTableModel(new Object[0][], new String[]{"道具代码", "道具名字", "加成比例%", "套装编码", "套装名字"}) {
           boolean[] canEdit = {false, false, false, false, false};

           @Override
           public boolean isCellEditable(int rowIndex, int columnIndex) {
               return this.canEdit[columnIndex];
           }
       });
       this.jScrollPane19t.setViewportView((Component) this.套装伤害加成表);
       if (this.套装伤害加成表.getColumnModel().getColumnCount() > 0) {
           this.套装伤害加成表.getColumnModel().getColumn(0).setPreferredWidth(20);
           this.套装伤害加成表.getColumnModel().getColumn(2).setPreferredWidth(20);
       }
       GroupLayout jPanel91Layout = new GroupLayout((Container) this.jPanel91t);
       this.jPanel91t.setLayout((LayoutManager) jPanel91Layout);
       jPanel91Layout.setHorizontalGroup((GroupLayout.Group) jPanel91Layout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup((GroupLayout.Group) jPanel91Layout.createSequentialGroup().addContainerGap().addComponent((Component) this.jScrollPane19t, -1, 598, 32767).addGap(12, 12, 12)));
       jPanel91Layout.setVerticalGroup((GroupLayout.Group) jPanel91Layout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup((GroupLayout.Group) jPanel91Layout.createSequentialGroup().addComponent((Component) this.jScrollPane19t).addContainerGap()));
       this.jPanel92t.setBorder((Border) BorderFactory.createTitledBorder("显示操作"));
       this.jLabel192t.setText("道具代码:");
       this.原套装代码.setEnabled(false);
       this.jLabel193t.setText("道具代码:");
       this.jLabel194t.setText("加成比例:");
       this.套装道具增加.setText("增加");
       this.套装道具增加.addActionListener((ActionListener) new ActionListener() {
           @Override
           public void actionPerformed(ActionEvent evt) {
               套装道具增加ActionPerformed(evt);
           }
       });
       this.套装道具删除.setText("删除");
       this.套装道具删除.addActionListener((ActionListener) new ActionListener() {
           @Override
           public void actionPerformed(ActionEvent evt) {
               套装道具删除ActionPerformed(evt);
           }
       });
       this.套装道具修改.setText("修改");
       this.套装道具修改.addActionListener((ActionListener) new ActionListener() {
           @Override
           public void actionPerformed(ActionEvent evt) {
               套装道具修改ActionPerformed(evt);
           }
       });
       this.jLabel195t.setText("道具名字:");
       this.套装道具代码.setEnabled(false);
       this.装备加成伤害列表初始化.setText("初始化表单信息");
       this.装备加成伤害列表初始化.addActionListener((ActionListener) new ActionListener() {
           @Override
           public void actionPerformed(ActionEvent evt) {
               装备加成伤害列表初始化ActionPerformed(evt);
           }
       });
       this.jLabel196t.setText("套装属性加成开关:");
       this.套装属性加成开关.setIcon((Icon) new ImageIcon(this.getClass().getResource("/image/OFF2.png")));
       this.套装属性加成开关.setBorderPainted(false);
       this.套装属性加成开关.setContentAreaFilled(false);
       this.套装属性加成开关.setFocusPainted(false);
       this.套装属性加成开关.setSelectedIcon((Icon) new ImageIcon(this.getClass().getResource("/image/ON2.png")));
       this.套装属性加成开关.addChangeListener((ChangeListener) new ChangeListener() {
           @Override
           public void stateChanged(ChangeEvent evt) {
               套装属性加成开关StateChanged(evt);
           }
       });
       this.套装属性加成开关.addActionListener((ActionListener) new ActionListener() {
           @Override
           public void actionPerformed(ActionEvent evt) {
               套装属性加成开关ActionPerformed(evt);
           }
       });
       this.jPanel93t.setBorder((Border) BorderFactory.createTitledBorder("指定套装查询"));
       this.jLabel197t.setText("输入套装编码:");
       this.套装查询.setText("查询");
       this.套装查询.addActionListener((ActionListener) new ActionListener() {
           @Override
           public void actionPerformed(ActionEvent evt) {
               套装查询ActionPerformed(evt);
           }
       });
       this.jLabel204t.setText("输入套装名字:");
       this.套装查询1.setText("查询");
       this.套装查询1.addActionListener((ActionListener) new ActionListener() {
           @Override
           public void actionPerformed(ActionEvent evt) {
               套装查询1ActionPerformed(evt);
           }
       });
       GroupLayout jPanel93Layout = new GroupLayout((Container) this.jPanel93t);
       this.jPanel93t.setLayout((LayoutManager) jPanel93Layout);
       jPanel93Layout.setHorizontalGroup((GroupLayout.Group) jPanel93Layout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup((GroupLayout.Group) jPanel93Layout.createSequentialGroup().addContainerGap().addGroup((GroupLayout.Group) jPanel93Layout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup((GroupLayout.Group) jPanel93Layout.createSequentialGroup().addComponent((Component) this.jLabel197t).addContainerGap(-1, 32767)).addGroup((GroupLayout.Group) jPanel93Layout.createSequentialGroup().addComponent((Component) this.套装排序输入, -2, 100, -2).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, -1, 32767).addComponent((Component) this.套装查询)).addGroup((GroupLayout.Group) jPanel93Layout.createSequentialGroup().addComponent((Component) this.jLabel204t).addGap(0, 0, 32767)).addGroup((GroupLayout.Group) jPanel93Layout.createSequentialGroup().addComponent((Component) this.套装名字输入, -2, 100, -2).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, -1, 32767).addComponent((Component) this.套装查询1)))));
       jPanel93Layout.setVerticalGroup((GroupLayout.Group) jPanel93Layout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup((GroupLayout.Group) jPanel93Layout.createSequentialGroup().addContainerGap().addComponent((Component) this.jLabel197t).addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED).addGroup((GroupLayout.Group) jPanel93Layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent((Component) this.套装排序输入, -2, -1, -2).addComponent((Component) this.套装查询)).addGap(10, 10, 10).addComponent((Component) this.jLabel204t).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 7, 32767).addGroup((GroupLayout.Group) jPanel93Layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent((Component) this.套装名字输入, -2, -1, -2).addComponent((Component) this.套装查询1)).addContainerGap()));
       this.jLabel198t.setText("套装编码:");
       this.jLabel203t.setText("套装名字:");
       this.jLabel205t.setText("套装个数:");
       this.套装个数.addFocusListener((FocusListener) new FocusAdapter() {
           @Override
           public void focusLost(FocusEvent evt) {
               套装个数FocusLost(evt);
           }
       });
       this.重载套装加成.setText("重载套装加成");
       this.重载套装加成.addActionListener((ActionListener) new ActionListener() {
           @Override
           public void actionPerformed(ActionEvent evt) {
               重载套装加成ActionPerformed(evt);
           }
       });
       GroupLayout jPanel92Layout = new GroupLayout((Container) this.jPanel92t);
       this.jPanel92t.setLayout((LayoutManager) jPanel92Layout);
       jPanel92Layout.setHorizontalGroup((GroupLayout.Group) jPanel92Layout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(GroupLayout.Alignment.TRAILING, (GroupLayout.Group) jPanel92Layout.createSequentialGroup().addGroup((GroupLayout.Group) jPanel92Layout.createParallelGroup(GroupLayout.Alignment.TRAILING).addGroup(GroupLayout.Alignment.LEADING, (GroupLayout.Group) jPanel92Layout.createSequentialGroup().addContainerGap().addComponent((Component) this.jPanel93t, -1, -1, 32767)).addGroup(GroupLayout.Alignment.LEADING, (GroupLayout.Group) jPanel92Layout.createSequentialGroup().addContainerGap().addComponent((Component) this.套装道具增加, -2, 79, -2).addGap(136, 136, 136).addComponent((Component) this.套装道具删除, -2, 79, -2).addGap(137, 137, 137).addComponent((Component) this.套装道具修改, -2, 79, -2)).addGroup((GroupLayout.Group) jPanel92Layout.createSequentialGroup().addGap(10, 10, 10).addGroup((GroupLayout.Group) jPanel92Layout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent((Component) this.jLabel195t).addComponent((Component) this.jLabel194t).addComponent((Component) this.jLabel193t).addComponent((Component) this.jLabel192t)).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, -1, 32767).addGroup((GroupLayout.Group) jPanel92Layout.createParallelGroup(GroupLayout.Alignment.LEADING, false).addComponent((Component) this.原套装代码, -1, 110, 32767).addComponent((Component) this.现套装代码).addComponent((Component) this.套装加成比例).addComponent((Component) this.套装道具代码)))).addGap(10, 10, 10)).addGroup((GroupLayout.Group) jPanel92Layout.createSequentialGroup().addContainerGap().addGroup((GroupLayout.Group) jPanel92Layout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup((GroupLayout.Group) jPanel92Layout.createSequentialGroup().addGroup((GroupLayout.Group) jPanel92Layout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent((Component) this.jLabel198t).addComponent((Component) this.jLabel203t)).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, -1, 32767).addGroup((GroupLayout.Group) jPanel92Layout.createParallelGroup(GroupLayout.Alignment.LEADING, false).addComponent((Component) this.套装名字, -1, 110, 32767).addComponent((Component) this.套装编码))).addGroup((GroupLayout.Group) jPanel92Layout.createSequentialGroup().addComponent((Component) this.jLabel205t).addGap(346, 346, 346).addComponent((Component) this.套装个数)).addGroup((GroupLayout.Group) jPanel92Layout.createSequentialGroup().addComponent((Component) this.jLabel196t).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, -1, 32767).addComponent((Component) this.套装属性加成开关, -2, 74, -2)).addGroup((GroupLayout.Group) jPanel92Layout.createSequentialGroup().addComponent((Component) this.装备加成伤害列表初始化, -2, 160, -2).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, -1, 32767).addComponent((Component) this.重载套装加成, -2, 160, -2))).addContainerGap()));
       jPanel92Layout.setVerticalGroup((GroupLayout.Group) jPanel92Layout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup((GroupLayout.Group) jPanel92Layout.createSequentialGroup().addGap(18, 18, 18).addGroup((GroupLayout.Group) jPanel92Layout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent((Component) this.原套装代码, -2, -1, -2).addComponent((Component) this.jLabel192t)).addGap(18, 18, 18).addGroup((GroupLayout.Group) jPanel92Layout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent((Component) this.jLabel193t).addComponent((Component) this.现套装代码, -2, -1, -2)).addGap(18, 18, 18).addGroup((GroupLayout.Group) jPanel92Layout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent((Component) this.jLabel194t).addComponent((Component) this.套装加成比例, -2, -1, -2)).addGap(18, 18, 18).addGroup((GroupLayout.Group) jPanel92Layout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent((Component) this.jLabel195t).addComponent((Component) this.套装道具代码, -2, -1, -2)).addGap(18, 18, 18).addGroup((GroupLayout.Group) jPanel92Layout.createParallelGroup(GroupLayout.Alignment.CENTER).addComponent((Component) this.套装编码, -2, -1, -2).addComponent((Component) this.jLabel198t)).addGap(18, 18, 18).addGroup((GroupLayout.Group) jPanel92Layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent((Component) this.jLabel203t).addComponent((Component) this.套装名字, -2, -1, -2)).addGap(18, 18, 18).addGroup((GroupLayout.Group) jPanel92Layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent((Component) this.套装道具增加).addComponent((Component) this.套装道具删除).addComponent((Component) this.套装道具修改)).addGap(18, 18, 18).addGroup((GroupLayout.Group) jPanel92Layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent((Component) this.装备加成伤害列表初始化).addComponent((Component) this.重载套装加成)).addGap(3, 3, 3).addComponent((Component) this.jPanel93t, -2, -1, -2).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addGroup((GroupLayout.Group) jPanel92Layout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup((GroupLayout.Group) jPanel92Layout.createSequentialGroup().addGap(7, 7, 7).addComponent((Component) this.jLabel196t)).addComponent((Component) this.套装属性加成开关, -2, 30, -2)).addGap(18, 18, 18).addGroup((GroupLayout.Group) jPanel92Layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent((Component) this.jLabel205t).addComponent((Component) this.套装个数, -2, -1, -2)).addContainerGap(31, 32767)));
       GroupLayout jPanel90Layout = new GroupLayout((Container) this.jPanel90t);
       this.jPanel90t.setLayout((LayoutManager) jPanel90Layout);
       jPanel90Layout.setHorizontalGroup((GroupLayout.Group) jPanel90Layout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup((GroupLayout.Group) jPanel90Layout.createSequentialGroup().addContainerGap().addComponent((Component) this.jPanel91t, -2, -1, -2).addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED).addComponent((Component) this.jPanel92t, -2, -1, -2).addContainerGap(296, 32767)));
       jPanel90Layout.setVerticalGroup((GroupLayout.Group) jPanel90Layout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup((GroupLayout.Group) jPanel90Layout.createSequentialGroup().addContainerGap().addGroup((GroupLayout.Group) jPanel90Layout.createParallelGroup(GroupLayout.Alignment.LEADING, false).addComponent((Component) this.jPanel91t, -1, -1, 32767).addComponent((Component) this.jPanel92t, -1, -1, 32767)).addContainerGap(88, 32767)));
       this.jTabbedPane2.addTab("套装属性", (Component)this.jPanel90t);

   }
    public void 查询套装列表(int a) {
        List<Pair<Integer, Pair<String, Pair<String, Integer>>>> redisData = GetRedisDataUtil.getRedisData(RedisUtil.KEYNAMES.SET_BONUS_TABLE.getKeyName());

        for (int i = ((DefaultTableModel) (DefaultTableModel) this.套装伤害加成表.getModel()).getRowCount() - 1; i >= 0; --i) {
            ((DefaultTableModel) (DefaultTableModel) this.套装伤害加成表.getModel()).removeRow(i);
        }
        if (a == 1) {
            for (int i = 0; i < redisData.size(); ++i) {
                if ((redisData.get(i)).getLeft() == Integer.valueOf(this.套装排序输入.getText())) {
                    ((DefaultTableModel) this.套装伤害加成表.getModel()).insertRow(this.套装伤害加成表.getRowCount(), new Object[]{(((redisData.get(i)).getRight()).getRight()).getLeft(), MapleItemInformationProvider.getInstance().getName(Integer.valueOf((String) (((redisData.get(i)).getRight()).getRight()).getLeft()).intValue()), (((redisData.get(i)).getRight()).getRight()).getRight(), (redisData.get(i)).getLeft(), ((redisData.get(i)).getRight()).getLeft()});
                }
            }
        } else {
            for (int i = 0; i < redisData.size(); ++i) {
                if (((String) ((redisData.get(i)).getRight()).getLeft()).contains((CharSequence) this.套装名字输入.getText())) {
                    ((DefaultTableModel) this.套装伤害加成表.getModel()).insertRow(this.套装伤害加成表.getRowCount(), new Object[]{(((redisData.get(i)).getRight()).getRight()).getLeft(), MapleItemInformationProvider.getInstance().getName(Integer.valueOf((String) (((redisData.get(i)).getRight()).getRight()).getLeft()).intValue()), (((redisData.get(i)).getRight()).getRight()).getRight(), (redisData.get(i)).getLeft(), ((redisData.get(i)).getRight()).getLeft()});
                }
            }
        }
    }

    private void 套装查询ActionPerformed(ActionEvent evt) {
        this.查询套装列表(1);
    }

    private void 套装查询1ActionPerformed(ActionEvent evt) {
        this.查询套装列表(2);
    }

    public void 重载套装加成ActionPerformed(ActionEvent evt) {
        int n = JOptionPane.showConfirmDialog((Component) this, "你需要重载套装加成属性吗？", "信息", 0);
        if (n == 0) {
            tzjc.sr_tz();

            JOptionPane.showMessageDialog(null, "重载完成。");
        }
    }
    public void 套装个数FocusLost(FocusEvent evt) {
        if (LtMS.ConfigValuesMap.get("套装个数") != Integer.valueOf(this.套装个数.getText())) {
            this.配置更新("套装个数", Integer.valueOf(this.套装个数.getText()).intValue());
        }
    }
    public void 加载套装伤害加成表() {
        for (int i = ((DefaultTableModel) this.套装伤害加成表.getModel()).getRowCount() - 1; i >= 0; --i) {
            ((DefaultTableModel) this.套装伤害加成表.getModel()).removeRow(i);
        }
        List<Pair<Integer, Pair<String, Pair<String, Integer>>>> redisData = GetRedisDataUtil.getRedisData(RedisUtil.KEYNAMES.SET_BONUS_TABLE.getKeyName());

        for (int i = 0; i < redisData.size(); ++i) {
            ((DefaultTableModel) this.套装伤害加成表.getModel()).insertRow(this.套装伤害加成表.getRowCount(), new Object[]{(((redisData.get(i)).getRight()).getRight()).getLeft(), MapleItemInformationProvider.getInstance().getName(Integer.valueOf((String) (((redisData.get(i)).getRight()).getRight()).getLeft()).intValue()), (((redisData.get(i)).getRight()).getRight()).getRight(), (redisData.get(i)).getLeft(), ((redisData.get(i)).getRight()).getLeft()});
        }
    }
    private void 套装道具增加ActionPerformed(ActionEvent evt) {
        this.套装伤害加成表调整(0);
    }

    private void 套装道具修改ActionPerformed(ActionEvent evt) {
        this.套装伤害加成表调整(2);
    }

    private void 套装道具删除ActionPerformed(ActionEvent evt) {
        this.套装伤害加成表调整(1);
    }

    private void 装备加成伤害列表初始化ActionPerformed(ActionEvent evt) {
        this.加载套装伤害加成表();
    }

    private void 套装属性加成开关StateChanged(ChangeEvent evt) {
        this.配置更新("套装属性加成开关", (int) (this.套装属性加成开关.isSelected() ? 1 : 0));
    }

    private void 套装属性加成开关ActionPerformed(ActionEvent evt) {
    }
    public void 套装伤害加成表调整(int type) {
        if (this.现套装代码.getText().length() < 1) {
            JOptionPane.showMessageDialog(null, "错误:道具代码错误");
            return;
        }
        if (this.套装加成比例.getText().length() < 1) {
            JOptionPane.showMessageDialog(null, "错误:套装加成比例错误");
            return;
        }
        if (this.套装编码.getText().length() < 1) {
            JOptionPane.showMessageDialog(null, "错误:套装编码错误");
            return;
        }
        if (this.套装名字.getText().length() < 1) {
            JOptionPane.showMessageDialog(null, "错误:套装名字错误");
            return;
        }
        try (Connection con = (Connection) DBConPool.getInstance().getDataSource().getConnection()) {
            PreparedStatement ps = null;
            switch (type) {
                case 0: {
                    ps = con.prepareStatement("INSERT INTO suitdamtable (name,numb,proportion,proname) VALUES(?,?,?,?)");
                    ps.setString(1, this.现套装代码.getText());
                    ps.setInt(2, Integer.valueOf(this.套装加成比例.getText()).intValue());
                    ps.setInt(3, Integer.valueOf(this.套装编码.getText()).intValue());
                    ps.setString(4, this.套装名字.getText());
                    ps.execute();
                    ps.close();
                    break;
                }

                case 1: {
                    ps = con.prepareStatement("DELETE FROM suitdamtable where name = ? AND numb = ? AND proportion = ? AND proname = ? ");
                    ps.setString(1, this.原套装代码.getText());
                    ps.setInt(2, Integer.valueOf(this.套装加成比例.getText()).intValue());
                    ps.setInt(3, Integer.valueOf(this.套装编码.getText()).intValue());
                    ps.setString(4, this.套装名字.getText());
                    ps.execute();
                    ps.close();
                }
                case 2: {
                    ps = con.prepareStatement("UPDATE suitdamtable set name = ? ,numb = ?,proportion = ? ,proname = ? where name = ?");
                    ps.setString(1, this.现套装代码.getText());
                    ps.setInt(2, Integer.valueOf(this.套装加成比例.getText()).intValue());
                    ps.setInt(3, Integer.valueOf(this.套装编码.getText()).intValue());
                    ps.setString(4, this.套装名字.getText());
                    ps.setString(5, this.原套装代码.getText());
                    ps.execute();
                    ps.close();
                    break;
                }
            }
            LtMS.GetConfigValues();
            this.加载套装伤害加成表();
            JOptionPane.showMessageDialog(null, "数据处理完毕！", "套装伤害加成列表调整提示", 1);
        } catch (SQLException ex) {
            System.err.println("[" + FileoutputUtil.CurrentReadable_Time() + "]套装伤害加成列表调整出错：" + ex.getMessage());
        }
    }

    public void 配置更新(String name, int value) {
        if (LtMS.ConfigValuesMap.get(name) != null) {
            LtMS.ConfigValuesMap.put(name, value);
        }
        try (Connection con = (Connection)DBConPool.getInstance().getDataSource().getConnection()) {
            PreparedStatement ps = null;
            ps = con.prepareStatement("UPDATE configvalues SET Val = ? WHERE name = ?");
            ps.setInt(1, value);
            ps.setString(2, name);
            ps.execute();
            ps.close();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, "错误!\r\n" + ex);
        }
        LtMS.GetConfigValues();
    }
    public static void main(String[] agrs)
    {
        new TzJFrame(); //创建一个实例化对象
    }
}