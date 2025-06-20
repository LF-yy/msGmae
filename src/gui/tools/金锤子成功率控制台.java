/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gui.tools;

import database.DBConPool;
import gui.LtMS;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Administrator
 */
public class 金锤子成功率控制台 extends javax.swing.JFrame {

    /**
     * Creates new form 锻造控制台
     */
    public 金锤子成功率控制台() {
        ImageIcon icon = new ImageIcon(getClass().getClassLoader().getResource("image2/logo.png"));
        setIconImage(icon.getImage());
        setTitle("金锤子成功率控制台");
        initComponents();
        LtMS.GetConfigValues();
        刷新金锤子();
        刷新金锤子上限();
    }

    public void 按键开关(String a, int b) {
        int 检测开关 = LtMS.ConfigValuesMap.get(a);
        PreparedStatement ps = null;
        PreparedStatement ps1 = null;
        ResultSet rs = null;
        if (检测开关 > 0) {
            try (Connection con = DBConPool.getInstance().getDataSource().getConnection()) {
                ps = con.prepareStatement("UPDATE configvalues SET Val = ? WHERE id = ?");
                ps1 = con.prepareStatement("SELECT * FROM configvalues WHERE id = ?");
                ps1.setInt(1, b);
                rs = ps1.executeQuery();
                if (rs.next()) {
                    String sqlString2 = null;
                    String sqlString3 = null;
                    String sqlString4 = null;
                    sqlString2 = "update configvalues set Val= '0' where id= '" + b + "';";
                    PreparedStatement dropperid = con.prepareStatement(sqlString2);
                    dropperid.executeUpdate(sqlString2);
                }
                ps.close();
                ps1.close();
                rs.close();
                con.close();
            } catch (SQLException ex) {
                Logger.getLogger(金锤子成功率控制台.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            try (Connection con = DBConPool.getInstance().getDataSource().getConnection()) {
                ps = con.prepareStatement("UPDATE configvalues SET Val = ? WHERE id = ?");
                ps1 = con.prepareStatement("SELECT * FROM configvalues WHERE id = ?");
                ps1.setInt(1, b);
                rs = ps1.executeQuery();
                if (rs.next()) {
                    String sqlString2 = null;
                    String sqlString3 = null;
                    String sqlString4 = null;
                    sqlString2 = "update configvalues set Val= '1' where id='" + b + "';";
                    PreparedStatement dropperid = con.prepareStatement(sqlString2);
                    dropperid.executeUpdate(sqlString2);
                }
                ps.close();
                ps1.close();
                rs.close();
                con.close();
            } catch (SQLException ex) {
                Logger.getLogger(金锤子成功率控制台.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        LtMS.GetConfigValues();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        金锤子 = new javax.swing.JPanel();
        jScrollPane137 = new javax.swing.JScrollPane();
        金锤子表 = new javax.swing.JTable();
        金锤子序号 = new javax.swing.JTextField();
        金锤子类型 = new javax.swing.JTextField();
        金锤子数值 = new javax.swing.JTextField();
        金锤子修改 = new javax.swing.JButton();
        jLabel332 = new javax.swing.JLabel();
        jLabel334 = new javax.swing.JLabel();
        jLabel335 = new javax.swing.JLabel();
        jLabel337 = new javax.swing.JLabel();
        金锤子提升上限 = new javax.swing.JButton();

        setResizable(false);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        金锤子.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "金锤子成功率设置", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.TOP, new java.awt.Font("幼圆", 0, 24))); // NOI18N
        金锤子.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        金锤子表.setFont(new java.awt.Font("幼圆", 0, 20)); // NOI18N
        金锤子表.setForeground(new java.awt.Color(255, 255, 255));
        金锤子表.setModel(new DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "序号", "类型", "成功率/%"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        金锤子表.getTableHeader().setReorderingAllowed(false);
        jScrollPane137.setViewportView(金锤子表);

        金锤子.add(jScrollPane137, new org.netbeans.lib.awtextra.AbsoluteConstraints(130, 150, 480, 160));

        金锤子序号.setEditable(false);
        金锤子.add(金锤子序号, new org.netbeans.lib.awtextra.AbsoluteConstraints(130, 350, 100, -1));

        金锤子类型.setEditable(false);
        金锤子.add(金锤子类型, new org.netbeans.lib.awtextra.AbsoluteConstraints(230, 350, 170, -1));
        金锤子.add(金锤子数值, new org.netbeans.lib.awtextra.AbsoluteConstraints(400, 350, 100, -1));

        金锤子修改.setFont(new java.awt.Font("幼圆", 0, 15)); // NOI18N
        金锤子修改.setText("修改");
        金锤子修改.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                金锤子修改ActionPerformed(evt);
            }
        });
        金锤子.add(金锤子修改, new org.netbeans.lib.awtextra.AbsoluteConstraints(520, 340, 80, 40));

        jLabel332.setFont(new java.awt.Font("幼圆", 0, 14)); // NOI18N
        jLabel332.setText("成功率%;");
        金锤子.add(jLabel332, new org.netbeans.lib.awtextra.AbsoluteConstraints(400, 330, -1, -1));

        jLabel334.setFont(new java.awt.Font("幼圆", 0, 14)); // NOI18N
        jLabel334.setText("类型；");
        金锤子.add(jLabel334, new org.netbeans.lib.awtextra.AbsoluteConstraints(230, 330, -1, -1));

        jLabel335.setFont(new java.awt.Font("幼圆", 0, 24)); // NOI18N
        jLabel335.setForeground(new java.awt.Color(204, 0, 0));
        jLabel335.setText("提示:金锤子可以无限制提高升级次数，谨慎调试成功率");
        金锤子.add(jLabel335, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 110, -1, -1));

        jLabel337.setFont(new java.awt.Font("幼圆", 0, 14)); // NOI18N
        jLabel337.setText("序号；");
        金锤子.add(jLabel337, new org.netbeans.lib.awtextra.AbsoluteConstraints(130, 330, -1, -1));

        金锤子提升上限.setFont(new java.awt.Font("幼圆", 0, 15)); // NOI18N
        金锤子提升上限.setText("提升上限");
        金锤子提升上限.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                金锤子提升上限ActionPerformed(evt);
            }
        });
        金锤子.add(金锤子提升上限, new org.netbeans.lib.awtextra.AbsoluteConstraints(270, 400, 150, 60));

        getContentPane().add(金锤子, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 790, 540));

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void 金锤子修改ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_金锤子修改ActionPerformed
        PreparedStatement ps = null;
        PreparedStatement ps1 = null;
        ResultSet rs = null;
        boolean result1 = this.金锤子序号.getText().matches("[0-9]+");
        if (result1) {
            try (Connection con = DBConPool.getInstance().getDataSource().getConnection()) {
                ps = con.prepareStatement("UPDATE configvalues SET Val = ? WHERE id = ?");
                ps1 = con.prepareStatement("SELECT * FROM configvalues WHERE id = ?");
                ps1.setInt(1, Integer.parseInt(this.金锤子序号.getText()));
                rs = ps1.executeQuery();
                if (rs.next()) {
                    String sqlString1 = null;
                    sqlString1 = "update configvalues set Val = '" + this.金锤子数值.getText() + "' where id= " + this.金锤子序号.getText() + ";";
                    PreparedStatement Val = con.prepareStatement(sqlString1);
                    Val.executeUpdate(sqlString1);
                    刷新金锤子();
                    LtMS.GetConfigValues();
                    JOptionPane.showMessageDialog(null, "修改成功已经生效");
                }
                ps.close();
                ps1.close();
                rs.close();
                con.close();
            } catch (SQLException ex) {
                Logger.getLogger(金锤子成功率控制台.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            JOptionPane.showMessageDialog(null, "请选择你要修改的值");
        }

    }//GEN-LAST:event_金锤子修改ActionPerformed

    private void 金锤子提升上限ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_金锤子提升上限ActionPerformed
        按键开关("金锤子提升上限", 601);
        刷新金锤子上限();
    }//GEN-LAST:event_金锤子提升上限ActionPerformed
    private void 金锤子提升上限(String str) {
        金锤子提升上限.setText(str);
    }

    public void 刷新金锤子() {
        for (int i = ((DefaultTableModel) (this.金锤子表.getModel())).getRowCount() - 1; i >= 0; i--) {
            ((DefaultTableModel) (this.金锤子表.getModel())).removeRow(i);
        }
        try (Connection con = DBConPool.getInstance().getDataSource().getConnection()) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            ps = con.prepareStatement("SELECT * FROM configvalues WHERE id = 600  ");
            rs = ps.executeQuery();
            while (rs.next()) {
                ((DefaultTableModel) 金锤子表.getModel()).insertRow(金锤子表.getRowCount(), new Object[]{rs.getString("id"), rs.getString("name"), rs.getString("Val")});
            }
            ps.close();
            rs.close();
            con.close();
        } catch (SQLException ex) {
            Logger.getLogger(金锤子成功率控制台.class.getName()).log(Level.SEVERE, null, ex);
        }
        金锤子表.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int i = 金锤子表.getSelectedRow();
                String a = 金锤子表.getValueAt(i, 0).toString();
                String a1 = 金锤子表.getValueAt(i, 1).toString();
                String a2 = 金锤子表.getValueAt(i, 2).toString();
                金锤子序号.setText(a);
                金锤子类型.setText(a1);
                金锤子数值.setText(a2);
            }
        });
    }

    private void 刷新金锤子上限() {
        String 金锤子提升上限显示 = "";
        int 金锤子提升上限 = LtMS.ConfigValuesMap.get("金锤子提升上限");
        if (金锤子提升上限 >= 1) {
            金锤子提升上限显示 = "提升上限:开启";
        } else {
            金锤子提升上限显示 = "提升上限:关闭";
        }
        金锤子提升上限(金锤子提升上限显示);
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel332;
    private javax.swing.JLabel jLabel334;
    private javax.swing.JLabel jLabel335;
    private javax.swing.JLabel jLabel337;
    private javax.swing.JScrollPane jScrollPane137;
    private javax.swing.JPanel 金锤子;
    private javax.swing.JButton 金锤子修改;
    private javax.swing.JTextField 金锤子序号;
    private javax.swing.JButton 金锤子提升上限;
    private javax.swing.JTextField 金锤子数值;
    private javax.swing.JTextField 金锤子类型;
    private javax.swing.JTable 金锤子表;
    // End of variables declaration//GEN-END:variables
}
