package gui.tools;

import database.DBConPool;
import tools.FileoutputUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

import static tools.FileoutputUtil.CurrentReadable_Date;

/**
 *
 * @author 小灰灰
 */
public class 卡密制作工具 extends javax.swing.JFrame {

    /**
     * Creates new form WinStart
     */
    public 卡密制作工具() {
        ImageIcon icon = new ImageIcon(getClass().getClassLoader().getResource("image2/logo.png"));
        setIconImage(icon.getImage());
        Properties 設定檔 = System.getProperties();
        setTitle("充值/兑换卡控制台");
        initComponents();

        JOptionPane.showMessageDialog(null, ""
                + "特别友情提示；\r\n"
                + "1.每次尽量不要生成太多充值卡\r\n"
                + "2.生成充值卡后可上架到发卡平台\r\n"
                + "3.请及时删除，避免与之前的卡号弄混\r\n");
        String chars = "1234567890aAbBcCdDeEfFgGhHiIjJkKlLmMNnOoPpQqRrSsTtUuVvWwXxYyZz1234567890";
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jPanel17 = new javax.swing.JPanel();
        jLabel221 = new javax.swing.JLabel();
        点券充值卡金额 = new javax.swing.JTextField();
        抵用券充值卡金额 = new javax.swing.JTextField();
        jLabel222 = new javax.swing.JLabel();
        生成点券充值卡1 = new javax.swing.JButton();
        生成抵用券充值卡1 = new javax.swing.JButton();
        生成点券充值卡2 = new javax.swing.JButton();
        生成抵用券充值卡2 = new javax.swing.JButton();
        刷新充值卡信息 = new javax.swing.JButton();
        刷新充值卡信息1 = new javax.swing.JButton();
        jScrollPane81 = new javax.swing.JScrollPane();
        充值卡信息 = new javax.swing.JTable();

        setResizable(false);

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("卡密制作工具"));

        jPanel17.setToolTipText("");
        jPanel17.setPreferredSize(new java.awt.Dimension(700, 410));

        jLabel221.setFont(new java.awt.Font("幼圆", 0, 14)); // NOI18N
        jLabel221.setText("点券：");

        点券充值卡金额.setMaximumSize(new java.awt.Dimension(137, 27));
        点券充值卡金额.setMinimumSize(new java.awt.Dimension(137, 27));

        抵用券充值卡金额.setMaximumSize(new java.awt.Dimension(137, 27));
        抵用券充值卡金额.setMinimumSize(new java.awt.Dimension(137, 27));

        jLabel222.setFont(new java.awt.Font("幼圆", 0, 14)); // NOI18N
        jLabel222.setText("余额：");

        生成点券充值卡1.setFont(new java.awt.Font("幼圆", 0, 15)); // NOI18N
        生成点券充值卡1.setText("生成1张");
        生成点券充值卡1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                生成点券充值卡1ActionPerformed(evt);
            }
        });

        生成抵用券充值卡1.setFont(new java.awt.Font("幼圆", 0, 15)); // NOI18N
        生成抵用券充值卡1.setText("生成1张");
        生成抵用券充值卡1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                生成抵用券充值卡1ActionPerformed(evt);
            }
        });

        生成点券充值卡2.setFont(new java.awt.Font("幼圆", 0, 15)); // NOI18N
        生成点券充值卡2.setText("生成10张");
        生成点券充值卡2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                生成点券充值卡2ActionPerformed(evt);
            }
        });

        生成抵用券充值卡2.setFont(new java.awt.Font("幼圆", 0, 15)); // NOI18N
        生成抵用券充值卡2.setText("生成10张");
        生成抵用券充值卡2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                生成抵用券充值卡2ActionPerformed(evt);
            }
        });

        刷新充值卡信息.setFont(new java.awt.Font("幼圆", 0, 15)); // NOI18N
        刷新充值卡信息.setText("刷新充值卡信息");
        刷新充值卡信息.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                刷新充值卡信息ActionPerformed(evt);
            }
        });

        刷新充值卡信息1.setFont(new java.awt.Font("幼圆", 0, 15)); // NOI18N
        刷新充值卡信息1.setText("打开充值卡库存文件夹");
        刷新充值卡信息1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                刷新充值卡信息1ActionPerformed(evt);
            }
        });

        充值卡信息.setBorder(new javax.swing.border.MatteBorder(null));
        充值卡信息.setFont(new java.awt.Font("宋体", 0, 18)); // NOI18N
        充值卡信息.setModel(new DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "卡号", "类型", "领取", "额度"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        充值卡信息.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jScrollPane81.setViewportView(充值卡信息);

        javax.swing.GroupLayout jPanel17Layout = new javax.swing.GroupLayout(jPanel17);
        jPanel17.setLayout(jPanel17Layout);
        jPanel17Layout.setHorizontalGroup(
            jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel17Layout.createSequentialGroup()
                .addGroup(jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel17Layout.createSequentialGroup()
                        .addGap(14, 14, 14)
                        .addComponent(jLabel222))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel17Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel221)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(抵用券充值卡金额, javax.swing.GroupLayout.PREFERRED_SIZE, 169, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(点券充值卡金额, javax.swing.GroupLayout.PREFERRED_SIZE, 169, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(10, 10, 10)
                .addGroup(jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel17Layout.createSequentialGroup()
                        .addComponent(生成抵用券充值卡1, javax.swing.GroupLayout.PREFERRED_SIZE, 96, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(生成抵用券充值卡2, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(刷新充值卡信息1, javax.swing.GroupLayout.PREFERRED_SIZE, 191, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel17Layout.createSequentialGroup()
                        .addComponent(生成点券充值卡1, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(生成点券充值卡2, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(刷新充值卡信息, javax.swing.GroupLayout.PREFERRED_SIZE, 191, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(19, Short.MAX_VALUE))
            .addComponent(jScrollPane81)
        );
        jPanel17Layout.setVerticalGroup(
            jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel17Layout.createSequentialGroup()
                .addComponent(jScrollPane81, javax.swing.GroupLayout.PREFERRED_SIZE, 277, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(生成点券充值卡1, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(生成点券充值卡2, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(刷新充值卡信息)
                    .addComponent(点券充值卡金额, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel221, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(6, 6, 6)
                .addGroup(jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(生成抵用券充值卡2, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(生成抵用券充值卡1, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(刷新充值卡信息1, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(抵用券充值卡金额, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel222, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel17, javax.swing.GroupLayout.PREFERRED_SIZE, 657, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jPanel17, javax.swing.GroupLayout.PREFERRED_SIZE, 361, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 10, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void 生成点券充值卡1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_生成点券充值卡1ActionPerformed

        boolean result1 = this.点券充值卡金额.getText().matches("[0-9]+");
        if (点券充值卡金额.getText().equals("") && !result1) {
            return;
        }
        生成自定义充值卡();
    }//GEN-LAST:event_生成点券充值卡1ActionPerformed

    private void 生成抵用券充值卡1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_生成抵用券充值卡1ActionPerformed
        boolean result1 = this.抵用券充值卡金额.getText().matches("[0-9]+");
        if (抵用券充值卡金额.getText().equals("") && !result1) {
            return;
        }
        生成自定义充值卡2();
    }//GEN-LAST:event_生成抵用券充值卡1ActionPerformed

    private void 生成点券充值卡2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_生成点券充值卡2ActionPerformed
        boolean result1 = this.点券充值卡金额.getText().matches("[0-9]+");
        if (点券充值卡金额.getText().equals("") && !result1) {
            return;
        }
        生成自定义充值卡();
        生成自定义充值卡();
        生成自定义充值卡();
        生成自定义充值卡();
        生成自定义充值卡();
        生成自定义充值卡();
        生成自定义充值卡();
        生成自定义充值卡();
        生成自定义充值卡();
        生成自定义充值卡();// TODO add your handling code here:
    }//GEN-LAST:event_生成点券充值卡2ActionPerformed

    private void 生成抵用券充值卡2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_生成抵用券充值卡2ActionPerformed
        boolean result1 = this.抵用券充值卡金额.getText().matches("[0-9]+");
        if (抵用券充值卡金额.getText().equals("") && !result1) {
            return;
        }
        生成自定义充值卡2();
        生成自定义充值卡2();
        生成自定义充值卡2();
        生成自定义充值卡2();
        生成自定义充值卡2();
        生成自定义充值卡2();
        生成自定义充值卡2();
        生成自定义充值卡2();
        生成自定义充值卡2();
        生成自定义充值卡2();
    }//GEN-LAST:event_生成抵用券充值卡2ActionPerformed

    private void 刷新充值卡信息ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_刷新充值卡信息ActionPerformed
        刷新充值卡信息();
    }//GEN-LAST:event_刷新充值卡信息ActionPerformed

    private void 刷新充值卡信息1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_刷新充值卡信息1ActionPerformed
        打开充值卡库存文件夹();// TODO add your handling code here:
    }//GEN-LAST:event_刷新充值卡信息1ActionPerformed
    public void 生成自定义充值卡() {
        int 金额 = Integer.parseInt(点券充值卡金额.getText());
        String 输出 = "";
        String chars = "1234567890aAbBcCdDeEfFgGhHiIjJkKlLmMNnOoPpQqRrSsTtUuVvWwXxYyZz1234567890";
        String 充值卡 = "DQ";
        for (int i = 0; i < 13; i++) {
            充值卡 += chars.charAt((int) (Math.random() * 62));
        }
        try (Connection con = DBConPool.getInstance().getDataSource().getConnection()) {
            PreparedStatement ps = con.prepareStatement("INSERT INTO nxcode ( code,type,valid,item) VALUES ( ?, ?, ?, ?)");
            ps.setString(1, 充值卡);
            ps.setInt(2, 0);
            ps.setInt(3, 1);
            ps.setInt(4, 金额);
            ps.executeUpdate();
            FileoutputUtil.logToFile("充值卡后台库存/[" + CurrentReadable_Date() + "]" + 金额 + "点券充值卡.txt", "" + 充值卡 + "\r\n");
            刷新充值卡信息();
            输出 = "" + CurrentReadable_Date() + "/生成兑换卡成功，数额为 " + 金额 + " 点券，已经存放服务端根目录。";
            ps.close();
            con.close();
        } catch (SQLException ex) {
            //System.err.println(ex);
            FileoutputUtil.outError("logs/资料库异常.txt", ex);

        }
    }

    public void 刷新充值卡信息() {
        for (int i = ((DefaultTableModel) (this.充值卡信息.getModel())).getRowCount() - 1; i >= 0; i--) {
            ((DefaultTableModel) (this.充值卡信息.getModel())).removeRow(i);
        }
        try (Connection con = DBConPool.getInstance().getDataSource().getConnection()) {
            PreparedStatement ps = con.prepareStatement("SELECT * FROM nxcode");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String 类型 = "";
                switch (rs.getInt("type")) {
                    case 0:
                        类型 = "点券";
                        break;
                    case 1:
                        类型 = "余额";
                        break;
                    default:
                        break;
                }
                ((DefaultTableModel) 充值卡信息.getModel()).insertRow(充值卡信息.getRowCount(), new Object[]{
                    rs.getString("code"),
                    类型,
                    rs.getInt("valid"),
                    rs.getInt("item")
                });
            }
ps.close();
            con.close();
        } catch (SQLException ex) {
            //System.err.println(ex);
            FileoutputUtil.outError("logs/资料库异常.txt", ex);
        }
    }

    public void 生成自定义充值卡2() {
        int 金额 = Integer.parseInt(抵用券充值卡金额.getText());
        String 输出 = "";
        String chars = "1234567890aAbBcCdDeEfFgGhHiIjJkKlLmMNnOoPpQqRrSsTtUuVvWwXxYyZz1234567890";
        String 充值卡 = "DQ";
        for (int i = 0; i < 13; i++) {
            充值卡 += chars.charAt((int) (Math.random() * 62));
        }
        try (Connection con = DBConPool.getInstance().getDataSource().getConnection()) {
            PreparedStatement ps = con.prepareStatement("INSERT INTO nxcode ( code,type,valid,item) VALUES ( ?, ?, ?, ?)");
            ps.setString(1, 充值卡);
            ps.setInt(2, 1);
            ps.setInt(3, 1);
            ps.setInt(4, 金额);
            ps.executeUpdate();
            FileoutputUtil.logToFile("充值卡后台库存/[" + CurrentReadable_Date() + "]" + 金额 + "充余额卡.txt", "" + 充值卡 + "\r\n");
            刷新充值卡信息();
            输出 = "" + CurrentReadable_Date() + "/生成兑换卡成功，数额为 " + 金额 + " 抵用券，已经存放服务端根目录。";
            ps.close();
            con.close();
        } catch (SQLException ex) {
            //System.err.println(ex);
            FileoutputUtil.outError("logs/资料库异常.txt", ex);

        }
    }

    public static void 打开充值卡库存文件夹() {
        final Runtime runtime = Runtime.getRuntime();
        Process process = null;//  
        Properties 設定檔 = System.getProperties();
        final String cmd = "rundll32 url.dll FileProtocolHandler file:" + 設定檔.getProperty("user.dir") + "\\充值卡后台库存";
        try {
            process = runtime.exec(cmd);
        } catch (final Exception e) {
            System.out.println("Error exec!");
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel221;
    private javax.swing.JLabel jLabel222;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel17;
    private javax.swing.JScrollPane jScrollPane81;
    private javax.swing.JTable 充值卡信息;
    private javax.swing.JButton 刷新充值卡信息;
    private javax.swing.JButton 刷新充值卡信息1;
    private javax.swing.JTextField 抵用券充值卡金额;
    private javax.swing.JTextField 点券充值卡金额;
    private javax.swing.JButton 生成抵用券充值卡1;
    private javax.swing.JButton 生成抵用券充值卡2;
    private javax.swing.JButton 生成点券充值卡1;
    private javax.swing.JButton 生成点券充值卡2;
    // End of variables declaration//GEN-END:variables
}
