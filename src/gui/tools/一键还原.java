package gui.tools;

import javax.swing.UIManager.LookAndFeelInfo;
import java.awt.EventQueue;
import javax.swing.LookAndFeel;

import database.DBConPool;
import gui.服务端输出信息;
import org.jvnet.substance.skin.SubstanceBusinessBlackSteelLookAndFeel;
import javax.swing.JDialog;
import javax.swing.UnsupportedLookAndFeelException;
import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.UIManager;

import database.DatabaseConnection;
import javax.swing.JOptionPane;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Font;
import java.awt.Component;
import org.netbeans.lib.awtextra.AbsoluteConstraints;
import java.awt.LayoutManager;
import org.netbeans.lib.awtextra.AbsoluteLayout;
import java.awt.Image;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JFrame;

public class 一键还原 extends JFrame
{
    private JButton jButton1;
    private JPanel jPanel1;
    private JButton 清除个人随身仓库;
    private JButton 清除商城所有商品;
    private JButton 清除家族随身仓库;
    private JButton 清除点券拍卖行;
    private JButton 清除金币拍卖行;
    private JTextField 输入验证码;
    private JButton 还原;
    private JProgressBar 进度条1;
    private JLabel 验证码;
    private JLabel 验证码1;
    
    public 一键还原() {
        final ImageIcon icon = new ImageIcon(this.getClass().getClassLoader().getResource("image/Icon.png"));
        this.setTitle("一键还原数据库工具");
        final Image background = new ImageIcon("gui/1.png").getImage();
        this.setIconImage(icon.getImage());
        this.initComponents();
        this.生成验证码();
    }
    
    private void 生成验证码() {
        final String 生成验证码 = "1234567890aAbBcCdDeEfFgGhHiIjJkKlLmMNnOoPpQqRrSsTtUuVvWwXxYyZz1234567890";
        final char 生成1 = 生成验证码.charAt((int)(Math.random() * 62.0));
        final char 生成2 = 生成验证码.charAt((int)(Math.random() * 62.0));
        final char 生成3 = 生成验证码.charAt((int)(Math.random() * 62.0));
        final char 生成4 = 生成验证码.charAt((int)(Math.random() * 62.0));
        final String 输出验证码 = "" + 生成1 + "" + 生成2 + "" + 生成3 + "" + 生成4 + "";
        this.验证码(输出验证码);
    }
    
    public void Z(final int i) {
        this.进度条1.setValue(i);
    }
    
    private void 验证码(final String str) {
        this.验证码.setText(str);
    }
    
    private void initComponents() {
        this.jPanel1 = new JPanel();
        this.进度条1 = new JProgressBar();
        this.清除金币拍卖行 = new JButton();
        this.还原 = new JButton();
        this.验证码 = new JLabel();
        this.输入验证码 = new JTextField();
        this.验证码1 = new JLabel();
        this.jButton1 = new JButton();
        this.清除点券拍卖行 = new JButton();
        this.清除个人随身仓库 = new JButton();
        this.清除家族随身仓库 = new JButton();
        this.清除商城所有商品 = new JButton();
        this.getContentPane().setLayout((LayoutManager)new AbsoluteLayout());
        this.jPanel1.setLayout((LayoutManager)new AbsoluteLayout());
        this.jPanel1.add((Component)this.进度条1, (Object)new AbsoluteConstraints(10, 10, 330, 40));
        this.清除金币拍卖行.setFont(new Font("幼圆", 0, 15));
        this.清除金币拍卖行.setText("清除金币拍卖行");
        this.清除金币拍卖行.addActionListener((ActionListener)new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent evt) {
                一键还原.this.清除金币拍卖行ActionPerformed(evt);
            }
        });
        this.jPanel1.add((Component)this.清除金币拍卖行, (Object)new AbsoluteConstraints(10, 150, 160, 30));
        this.还原.setFont(new Font("幼圆", 0, 15));
        this.还原.setForeground(new Color(255, 51, 102));
        this.还原.setText("一键还原游戏数据");
        this.还原.addActionListener((ActionListener)new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent evt) {
                一键还原.this.还原ActionPerformed(evt);
            }
        });
        this.jPanel1.add((Component)this.还原, (Object)new AbsoluteConstraints(180, 120, 160, 30));
        this.验证码.setFont(new Font("宋体", 0, 18));
        this.验证码.setForeground(new Color(0, 51, 204));
        this.验证码.setText("XXXX");
        this.jPanel1.add((Component)this.验证码, (Object)new AbsoluteConstraints(80, 60, 60, 40));
        this.jPanel1.add((Component)this.输入验证码, (Object)new AbsoluteConstraints(200, 70, 90, -1));
        this.验证码1.setFont(new Font("幼圆", 0, 15));
        this.验证码1.setText("验证码:");
        this.jPanel1.add((Component)this.验证码1, (Object)new AbsoluteConstraints(20, 60, 60, 40));
        this.jButton1.setText("刷");
        this.jButton1.addActionListener((ActionListener)new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent evt) {
                一键还原.this.jButton1ActionPerformed(evt);
            }
        });
        this.jPanel1.add((Component)this.jButton1, (Object)new AbsoluteConstraints(130, 70, 60, 25));
        this.清除点券拍卖行.setFont(new Font("幼圆", 0, 15));
        this.清除点券拍卖行.setText("清除点券拍卖行");
        this.清除点券拍卖行.addActionListener((ActionListener)new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent evt) {
                一键还原.this.清除点券拍卖行ActionPerformed(evt);
            }
        });
        this.jPanel1.add((Component)this.清除点券拍卖行, (Object)new AbsoluteConstraints(10, 120, 160, 30));
        this.清除个人随身仓库.setFont(new Font("幼圆", 0, 15));
        this.清除个人随身仓库.setText("清除个人随身仓库");
        this.清除个人随身仓库.addActionListener((ActionListener)new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent evt) {
                一键还原.this.清除个人随身仓库ActionPerformed(evt);
            }
        });
        this.jPanel1.add((Component)this.清除个人随身仓库, (Object)new AbsoluteConstraints(10, 180, 160, 30));
        this.清除家族随身仓库.setFont(new Font("幼圆", 0, 15));
        this.清除家族随身仓库.setText("清除家族随身仓库");
        this.清除家族随身仓库.addActionListener((ActionListener)new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent evt) {
                一键还原.this.清除家族随身仓库ActionPerformed(evt);
            }
        });
        this.jPanel1.add((Component)this.清除家族随身仓库, (Object)new AbsoluteConstraints(10, 210, 160, 30));
        this.清除商城所有商品.setFont(new Font("幼圆", 0, 15));
        this.清除商城所有商品.setText("清除商城所有商品");
        this.清除商城所有商品.addActionListener((ActionListener)new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent evt) {
                一键还原.this.清除商城所有商品ActionPerformed(evt);
            }
        });
        this.jPanel1.add((Component)this.清除商城所有商品, (Object)new AbsoluteConstraints(10, 240, 160, 30));
        this.getContentPane().add((Component)this.jPanel1, (Object)new AbsoluteConstraints(0, 0, 350, 540));
        this.pack();
        this.setLocationRelativeTo(null);
    }
    
    private void 还原ActionPerformed(final ActionEvent evt) {
        if (this.输入验证码.getText().equals((Object)"")) {
            JOptionPane.showMessageDialog(null, (Object)"请输入验证码！");
            return;
        }
        if (!this.输入验证码.getText().equals((Object)this.验证码.getText())) {
            JOptionPane.showMessageDialog(null, (Object)"验证码错误！");
            this.生成验证码();
            return;
        }
        this.Delete("accounts", 1);
        this.Delete("accounts_info", 2);
        this.Delete("auctionitems", 3);
        this.Delete("aclog", 4);
        this.Delete("addlog", 5);
        this.Delete("alliances", 6);
        this.Delete("auth_server_channel", 7);
        this.Delete("auth_server_channel_ip", 8);
        this.Delete("auth_server_cs", 9);
        this.Delete("auth_server_login", 10);
        this.Delete("auth_server_mts", 11);
        this.Delete("auctionpoint", 12);
        this.Delete("auctionpoint1", 13);
        this.Delete("bank_item", 20);
        this.Delete("bank_item1", 21);
        this.Delete("bank_item2", 22);
        this.Delete("bbs_replies", 23);
        this.Delete("bbs_threads", 24);
        this.Delete("blocklogin", 25);
        this.Delete("bosslog", 26);
        this.Delete("bossrank", 27);
        this.Delete("bossrank1", 28);
        this.Delete("bossrank2", 29);
        this.Delete("bossrank3", 30);
        this.Delete("bossrank4", 31);
        this.Delete("bossrank5", 32);
        this.Delete("bossrank6", 33);
        this.Delete("bossrank7", 34);
        this.Delete("bossrank8", 35);
        this.Delete("bossrank9", 36);
        this.Delete("buddies", 37);
        this.Delete("capture_cs", 38);
        this.Delete("capture_jl", 39);
        this.Delete("capture_zj", 40);
        this.Delete("capture_zk", 41);
        this.Delete("character_slots", 42);
        this.Delete("cashshop_limit_sell", 43);
        this.Delete("character7", 44);
        this.Delete("charactera", 45);
        this.Delete("characters", 46);
        this.Delete("characterz", 47);
        this.Delete("cheatlog", 48);
        this.Delete("csequipment", 49);
        this.Delete("csitems", 50);
        this.Delete("dangerousacc", 55);
        this.Delete("dangerousip", 55);
        this.Delete("divine", 56);
        this.Delete("dueyequipment", 57);
        this.Delete("dueyitems", 58);
        this.Delete("dueypackages", 59);
        this.Delete("eventstats", 60);
        this.Delete("famelog", 61);
        this.Delete("families", 62);
        this.Delete("fishingjf", 63);
        this.Delete("fubenjilu", 64);
        this.Delete("fullpoint", 65);
        this.Delete("FengYeDuan", 66);
        this.Delete("forum_reply", 67);
        this.Delete("forum_section", 68);
        this.Delete("forum_thread", 69);
        this.Delete("game_poll_reply", 70);
        this.Delete("gbook_admin", 71);
        this.Delete("gbook_setting", 72);
        this.Delete("gifts", 73);
        this.Delete("gmlog", 74);
        this.Delete("guiidld", 75);
        this.Delete("guilds", 76);
        this.Delete("guildsl", 77);
        this.Delete("hiredmerch", 78);
        this.Delete("hiredmerchequipment", 79);
        this.Delete("hiredmerchitems", 80);
        this.Delete("hiredmerch", 81);
        this.Delete("htsquads", 82);
        this.Delete("inventoryequipment", 83);
        this.Delete("inventoryitems", 84);
        this.Delete("inventorylog", 85);
        this.Delete("inventoryslot", 86);
        this.Delete("invitecodedata", 87);
        this.Delete("ipbans", 88);
        this.Delete("ipcheck", 89);
        this.Delete("ipvotelog", 90);
        this.Delete("keymap", 91);
        this.Delete("loginlog", 92);
        this.Delete("lottery_info", 93);
        this.Delete("lottery_player_info", 94);
        this.Delete("macbans", 95);
        this.Delete("mapidban", 96);
        this.Delete("macfilters", 97);
        this.Delete("monsterbook", 98);
        this.Delete("mountdata", 99);
        this.Delete("mts_cart", 100);
        this.Delete("mts_items", 101);
        this.Delete("mtsequipment", 102);
        this.Delete("mtsitems", 103);
        this.Delete("mtstransfer", 104);
        this.Delete("mtstransferequipment", 105);
        this.Delete("mulungdojo", 106);
        this.Delete("notes", 107);
        this.Delete("nxcode", 108);
        this.Delete("nxcodez", 109);
        this.Delete("nxitemlist", 110);
        this.Delete("onetimelog", 111);
        this.Delete("pets", 112);
        this.Delete("pnpc", 113);
        this.Delete("playernpcs", 114);
        this.Delete("playernpcs_equip", 115);
        this.Delete("prizelog", 116);
        this.Delete("qqlog", 117);
        this.Delete("qqstem", 118);
        this.Delete("questactions", 119);
        this.Delete("questinfo", 120);
        this.Delete("questrequirements", 121);
        this.Delete("queststatus", 122);
        this.Delete("queststatusmobs", 123);
        this.Delete("rcmedals", 124);
        this.Delete("regrocklocations", 125);
        this.Delete("reports", 126);
        this.Delete("rings", 127);
        this.Delete("saiji", 128);
        this.Delete("savedlocations", 129);
        this.Delete("skillmacros", 130);
        this.Delete("skills", 131);
        this.Delete("skills_cooldowns", 132);
        this.Delete("speedruns", 133);
        this.Delete("storages", 134);
        this.Delete("stjflog", 134);
        this.Delete("stlog", 134);
        this.Delete("trocklocations", 135);
        this.Delete("uselog", 136);
        this.Delete("zaksquads", 137);
        this.Delete("z_pg", 138);
        this.Delete("bank", 139);
        this.Delete("mail", 140);
        this.Delete("jiezoudashi", 141);
        this.Delete("bosslog1", 142);
        this.Delete("bosslog2", 143);
        this.Delete("bosslog3", 144);
        this.Delete("shouce", 145);
        this.Delete("lt_breakthrough_mechanism", 146);
        this.Delete("lt_five_turn", 147);
        this.Delete("lt_public_record", 148);
        this.Delete("suitdamtablenew", 149);
        this.Delete("pqlog", 150);
        this.Delete("lt_attack_info_skills", 151);
        this.Delete("lt_field_skills", 152);
        this.Delete("lt_super_skills", 153);
        deleteDBAll();
        JOptionPane.showMessageDialog(null, (Object)"清理完成");
        System.exit(0);
    }

    public static boolean deleteDBAll() {
        Statement stmt;
        try {
            Connection con = DBConPool.getConnection();
            Throwable var41 = null;

            boolean var43;
            try {
                String[] tableList = new String[]{"accounts", "accounts_info", "admin", "auctionitems", "auctionpoint", "auctionitems1", "auctionpoint1", "awarp", "bossrank", "bossrank1", "bossrank2", "bossrank3", "bossrank4", "bossrank5", "bossrank6", "bossrank7", "bossrank8", "bossrank9", "bosslog", "bossloga", "bosslogi", "buddies", "character_slots", "characters", "characterz", "csequipment", "csitems", "dlq_ban", "donate", "famelog", "fubenjilu", "gifts", "gmlog", "guilds", "hiredmerch", "hiredmerchequipment", "hiredmerchitems", "hypay", "inventoryequipment", "inventoryitems", "inventoryequipment_copy", "inventoryitems_copy", "inventoryslot", "ipbans", "keymap", "macbans", "monsterbook", "mountdata", "notes", "onetimelog", "onetimeloga", "onetimelogi", "pets", "questinfo", "questrequirements", "queststatus", "queststatusmobs", "regrocklocations", "rings", "savedlocations", "skillmacros", "skills", "skills_cooldowns", "speedruns", "storages", "trocklocations", "wishlist", "snail_skillskin_chrlist", "snail_boss_points", "snail_chr_mount_list", "snail_world_boss", "snail_world_level", "snail_memory_flowerpot", "snail_memory_garden", "snail_memory_seedpackage", "snail_time_log", "snail_weeklog", "snail_weekloga", "snail_monthlog", "snail_monthloga", "snail_monster_damage", "snail_guild_skills", "snail_prize_ban_list", "snail_dagong", "snail_equipment_field_enhancement", "snail_drop_item_filter", "snail_sell_pickup_items", "snail_lotto", "bank_jin", "bank_mzb", "snail_lottery_prize", "bank_item", "曾用名", "抽奖屏蔽", "技能范围检测", "蘑菇战令_记录", "蘑菇战令_任务", "蘑菇战令_信息", "骑士团link", "徒弟列表", "网关_ip白名单", "网关_宠吸授权", "网关_多开授权", "网关_机器码注册限制", "网关_内挂授权"};
                stmt = con.createStatement();
                stmt.executeUpdate("SET FOREIGN_KEY_CHECKS = 5;");
                DatabaseMetaData dbm = con.getMetaData();
                String[] var5 = tableList;
                int var6 = tableList.length;

                for(int var7 = 0; var7 < var6; ++var7) {
                    String tableName = var5[var7];
                    ResultSet rs = dbm.getTables((String)null, (String)null, tableName, (String[])null);
                    if (rs.next()) {
                        stmt.executeUpdate("truncate table " + tableName + ";");
                    } else {
                        服务端输出信息.println_out("【提示】删档过程中，表 [" + tableName + "] 不存在，跳过该表");
                    }

                    rs.close();
                }

                stmt.executeUpdate("SET FOREIGN_KEY_CHECKS = 1;");
                stmt.close();
                var43 = true;
            } catch (Throwable var38) {
                var41 = var38;
                throw var38;
            } finally {
                if (con != null) {
                    if (var41 != null) {
                        try {
                            con.close();
                        } catch (Throwable var35) {
                            var41.addSuppressed(var35);
                        }
                    } else {
                        con.close();
                    }
                }

            }

            return var43;
        } catch (SQLException var40) {
            try {
                Connection con = DBConPool.getConnection();
                Throwable var2 = null;

                try {
                    stmt = con.createStatement();
                    stmt.executeUpdate("SET FOREIGN_KEY_CHECKS = 1;");
                    stmt.close();
                } catch (Throwable var34) {
                    var2 = var34;
                    throw var34;
                } finally {
                    if (con != null) {
                        if (var2 != null) {
                            try {
                                con.close();
                            } catch (Throwable var33) {
                                var2.addSuppressed(var33);
                            }
                        } else {
                            con.close();
                        }
                    }

                }
            } catch (SQLException var37) {
            }

            服务端输出信息.println_err("【错误】deleteDBAll错误，原因：" + var40);
            var40.printStackTrace();
            return false;
        }
    }
    
    private void jButton1ActionPerformed(final ActionEvent evt) {
        this.生成验证码();
    }
    
    private void Delete(final String a, final int b) {
        Connection con = DatabaseConnection.getConnection();
        try {
            final PreparedStatement ps = con.prepareStatement("Delete from " + a + "");
            ps.executeUpdate();
            ps.close();
            this.Z(b);
        }
        catch (SQLException e) {
            System.out.println("Error/" + a + ":" + (Object)e);
        }finally {
            try {
                con.close();
            } catch (SQLException e) {}
        }
    }
    
    private void 清空个人设置() {
        Connection con = DatabaseConnection.getConnection();
        try {
            final PreparedStatement ps = con.prepareStatement("Delete from characterz");
            ps.executeUpdate();
            ps.close();
        }
        catch (SQLException e) {
            System.out.println("Error " + (Object)e);
        }finally {
            try {
                con.close();
            } catch (SQLException e) {}
        }
    }
    
    private void 清空qqgame() {
        Connection con = DatabaseConnection.getConnection();
        try {
            final PreparedStatement ps = con.prepareStatement("Delete from qqstem");
            ps.executeUpdate();
            ps.close();
        }
        catch (SQLException e) {
            System.out.println("Error " + (Object)e);
        }finally {
            try {
                con.close();
            } catch (SQLException e) {}
        }
    }
    
    private void 清空qqlog() {
        Connection con = DatabaseConnection.getConnection();
        try {
            final PreparedStatement ps = con.prepareStatement("Delete from qqlog");
            ps.executeUpdate();
            ps.close();
        }
        catch (SQLException e) {
            System.out.println("Error " + (Object)e);
        }finally {
            try {
                con.close();
            } catch (SQLException e) {}
        }
    }
    
    private void 清空养殖() {
        Connection con = DatabaseConnection.getConnection();
        try {
            final PreparedStatement ps = con.prepareStatement("Delete from character7");
            ps.executeUpdate();
            ps.close();
        }
        catch (SQLException e) {
            System.out.println("Error " + (Object)e);
        }finally {
            try {
                con.close();
            } catch (SQLException e) {}
        }
    }
    
    private void 清空B1() {
        Connection con = DatabaseConnection.getConnection();
        try {
            final PreparedStatement ps = con.prepareStatement("Delete from bossrank1");
            ps.executeUpdate();
            ps.close();
        }
        catch (SQLException e) {
            System.out.println("Error " + (Object)e);
        }finally {
            try {
                con.close();
            } catch (SQLException e) {}
        }
    }
    
    private void 清空B2() {
        Connection con = DatabaseConnection.getConnection();
        try {
            final PreparedStatement ps = con.prepareStatement("Delete from bossrank2");
            ps.executeUpdate();
            ps.close();
        }
        catch (SQLException e) {
            System.out.println("Error " + (Object)e);
        }finally {
            try {
                con.close();
            } catch (SQLException e) {}
        }
    }
    
    private void 清空B3() {
        Connection con = DatabaseConnection.getConnection();
        try {
            final PreparedStatement ps = con.prepareStatement("Delete from bossrank3");
            ps.executeUpdate();
            ps.close();
        }
        catch (SQLException e) {
            System.out.println("Error " + (Object)e);
        }finally {
            try {
                con.close();
            } catch (SQLException e) {}
        }
    }
    
    private void 清空B4() {
        Connection con = DatabaseConnection.getConnection();
        try {
            final PreparedStatement ps = con.prepareStatement("Delete from bossrank4");
            ps.executeUpdate();
            ps.close();
        }
        catch (SQLException e) {
            System.out.println("Error " + (Object)e);
        }finally {
            try {
                con.close();
            } catch (SQLException e) {}
        }
    }
    
    private void 清空B5() {
        Connection con = DatabaseConnection.getConnection();
        try {
            final PreparedStatement ps = con.prepareStatement("Delete from bossrank5");
            ps.executeUpdate();
            ps.close();
        }
        catch (SQLException e) {
            System.out.println("Error " + (Object)e);
        }finally {
            try {
                con.close();
            } catch (SQLException e) {}
        }
    }
    
    private void 清空B6() {
        Connection con = DatabaseConnection.getConnection();
        try {
            final PreparedStatement ps = con.prepareStatement("Delete from bossrank6");
            ps.executeUpdate();
            ps.close();
        }
        catch (SQLException e) {
            System.out.println("Error " + (Object)e);
        }finally {
            try {
                con.close();
            } catch (SQLException e) {}
        }
    }
    
    private void 清空B7() {
        Connection con = DatabaseConnection.getConnection();
        try {
            final PreparedStatement ps = con.prepareStatement("Delete from bossrank7");
            ps.executeUpdate();
            ps.close();
        }
        catch (SQLException e) {
            System.out.println("Error " + (Object)e);
        }finally {
            try {
                con.close();
            } catch (SQLException e) {}
        }
    }
    
    private void 清空B8() {
        Connection con = DatabaseConnection.getConnection();
        try {
            final PreparedStatement ps = con.prepareStatement("Delete from bossrank8");
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            System.out.println("Error " + (Object) e);
        } finally {
            try {
                con.close();
            } catch (SQLException e) {
                System.out.println("Error " + (Object) e);
            }
        }
    }
    private void 清除金币拍卖行ActionPerformed(final ActionEvent evt) {
        this.清空拍卖b1();
        this.清空拍卖b2();
        JOptionPane.showMessageDialog(null, (Object)"金币拍卖行清理完成");
    }
    
    private void 清空拍卖b1() {
        Connection con = DatabaseConnection.getConnection();
        try {
            final PreparedStatement ps = con.prepareStatement("Delete from auctionitems1");
            ps.executeUpdate();
            ps.close();
            this.Z(50);
        }
        catch (SQLException e) {
            System.out.println("Error " + (Object)e);
        }finally {
            try {
                con.close();
            } catch (SQLException e) {}
        }
    }
    
    private void 清空拍卖b2() {
        Connection con = DatabaseConnection.getConnection();
        try {
            final PreparedStatement ps = con.prepareStatement("Delete from auctionpoint1");
            ps.executeUpdate();
            ps.close();
            this.Z(100);
        }
        catch (SQLException e) {
            System.out.println("Error " + (Object)e);
        }finally {
            try {
                con.close();
            } catch (SQLException e) {}
        }
    }
    
    private void 清除点券拍卖行ActionPerformed(final ActionEvent evt) {
        this.清空拍卖a1();
        this.清空拍卖a2();
        JOptionPane.showMessageDialog(null, (Object)"点券拍卖行清理完成");
    }
    
    private void 清除个人随身仓库ActionPerformed(final ActionEvent evt) {
        this.清空个人随身仓库();
        JOptionPane.showMessageDialog(null, (Object)"个人随身仓库清理完成");
    }
    
    private void 清除家族随身仓库ActionPerformed(final ActionEvent evt) {
        this.清空家族随身仓库();
        JOptionPane.showMessageDialog(null, (Object)"家族随身仓库清理完成");
    }
    
    private void 清除商城所有商品ActionPerformed(final ActionEvent evt) {
        this.清除商城所有商品();
        this.清除SN("sn1000", 10000000);
        this.Z(10);
        this.清除SN("sn1009", 10089999);
        this.Z(20);
        this.清除SN("sn1010", 10100000);
        this.Z(30);
        this.清除SN("sn1019", 10189999);
        this.Z(40);
        this.清除SN("sn1020", 10200000);
        this.Z(50);
        this.清除SN("sn1029", 10289999);
        this.Z(60);
        this.清除SN("sn2000", 20000000);
        this.Z(100);
        JOptionPane.showMessageDialog(null, (Object)"商城所有商品清理完成");
    }
    
    private void 清除SN(final String a, final int b) {
        PreparedStatement ps1 = null;
        ResultSet rs = null;
        try {
            ps1 = DatabaseConnection.getConnection().prepareStatement("SELECT * FROM " + a + " WHERE SN > " + b + "");
            ps1.setInt(1, b);
            rs = ps1.executeQuery();
            if (rs.next()) {
                final String sqlstr = " delete from " + a + " where SN >" + b + "";
                ps1.executeUpdate(sqlstr);
            }
        }
        catch (SQLException ex) {}finally {
            try {
                if (rs != null)
                    rs.close();

                if (ps1 != null)
                    ps1.close();
            } catch (SQLException e) {}
        }
    }
    
    private void 清除商城所有商品() {
        Connection con = DatabaseConnection.getConnection();
        try {
            final PreparedStatement ps = con.prepareStatement("Delete from cashshop_modified_items");
            ps.executeUpdate();
            ps.close();
            this.Z(100);
        }
        catch (SQLException e) {
            System.out.println("Error " + (Object)e);
        }finally {
            try {
                con.close();
            } catch (SQLException e) {}
        }
    }
    
    private void 清空个人随身仓库() {
        Connection con = DatabaseConnection.getConnection();
        try {
            final PreparedStatement ps = con.prepareStatement("Delete from bank_item");
            ps.executeUpdate();
            ps.close();
            this.Z(100);
        }
        catch (SQLException e) {
            System.out.println("Error " + (Object)e);
        }finally {
            try {
                con.close();
            } catch (SQLException e) {}
        }
    }
    
    private void 清空家族随身仓库() {
        Connection con = DatabaseConnection.getConnection();
        try {
            final PreparedStatement ps = con.prepareStatement("Delete from bank_item1");
            ps.executeUpdate();
            ps.close();
            this.Z(100);
        }
        catch (SQLException e) {
            System.out.println("Error " + (Object)e);
        }finally {
            try {
                con.close();
            } catch (SQLException e) {}
        }
    }
    
    private void 清空拍卖a1() {
        Connection con = DatabaseConnection.getConnection();
        try {
            final PreparedStatement ps = con.prepareStatement("Delete from auctionitems");
            ps.executeUpdate();
            ps.close();
            this.Z(50);
        }
        catch (SQLException e) {
            System.out.println("Error " + (Object)e);
        }finally {
            try {
                con.close();
            } catch (SQLException e) {}
        }
    }
    
    private void 清空拍卖a2() {
        Connection con = DatabaseConnection.getConnection();
        try {
            final PreparedStatement ps = con.prepareStatement("Delete from auctionpoint");
            ps.executeUpdate();
            ps.close();
            this.Z(100);
        }
        catch (SQLException e) {
            System.out.println("Error " + (Object)e);
        }finally {
            try {
                con.close();
            } catch (SQLException e) {}
        }
    }
    
    private void 清理雇佣金币() {
        Connection con = DatabaseConnection.getConnection();
        try {
            final PreparedStatement ps = con.prepareStatement("Delete from hiredmerch");
            ps.executeUpdate();
            ps.close();
        }
        catch (SQLException e) {
            System.out.println("Error " + (Object)e);
        }finally {
            try {
                con.close();
            } catch (SQLException e) {}
        }
    }
    
    private void 清空记录角色人数() {
        Connection con = DatabaseConnection.getConnection();
        try {
            final PreparedStatement ps = con.prepareStatement("Delete from z角色统计");
            ps.executeUpdate();
            ps.close();
        }
        catch (SQLException e) {
            System.out.println("Error " + (Object)e);
        }finally {
            try {
                con.close();
            } catch (SQLException e) {}
        }
    }
    
    private void 清空商城() {
        Connection con = DatabaseConnection.getConnection();
        try {
            final PreparedStatement ps = con.prepareStatement("Delete from csitems");
            ps.executeUpdate();
            ps.close();
            this.Z(56);
        }
        catch (SQLException e) {
            System.out.println("Error " + (Object)e);
        }finally {
            try {
                con.close();
            } catch (SQLException e) {}
        }
    }
    
    private void 清空技能2() {
        Connection con = DatabaseConnection.getConnection();
        try {
            final PreparedStatement ps = con.prepareStatement("Delete from skills");
            ps.executeUpdate();
            ps.close();
            this.Z(54);
        }
        catch (SQLException e) {
            System.out.println("Error " + (Object)e);
        }finally {
            try {
                con.close();
            } catch (SQLException e) {}
        }
    }
    
    private void 清空技能1() {
        Connection con = DatabaseConnection.getConnection();
        try {
            final PreparedStatement ps = con.prepareStatement("Delete from skillmacros");
            ps.executeUpdate();
            ps.close();
            this.Z(52);
        }
        catch (SQLException e) {
            System.out.println("Error " + (Object)e);
        }finally {
            try {
                con.close();
            } catch (SQLException e) {}
        }
    }
    
    private void 清空任务2() {
        Connection con = DatabaseConnection.getConnection();
        try {
            final PreparedStatement ps = con.prepareStatement("Delete from queststatusmobs");
            ps.executeUpdate();
            ps.close();
            this.Z(50);
        }
        catch (SQLException e) {
            System.out.println("Error " + (Object)e);
        }finally {
            try {
                con.close();
            } catch (SQLException e) {}
        }
    }
    
    private void 清空任务1() {
        Connection con = DatabaseConnection.getConnection();
        try {
            final PreparedStatement ps = con.prepareStatement("Delete from queststatus");
            ps.executeUpdate();
            ps.close();
            this.Z(48);
        }
        catch (SQLException e) {
            System.out.println("Error " + (Object)e);
        }finally {
            try {
                con.close();
            } catch (SQLException e) {}
        }
    }
    
    private void 清空D() {
        Connection con = DatabaseConnection.getConnection();
        try {
            final PreparedStatement ps = con.prepareStatement("Delete from mountdata");
            ps.executeUpdate();
            ps.close();
            this.Z(46);
        }
        catch (SQLException e) {
            System.out.println("Error " + (Object)e);
        }finally {
            try {
                con.close();
            } catch (SQLException e) {}
        }
    }
    
    private void 清空C() {
        Connection con = DatabaseConnection.getConnection();
        try {
            final PreparedStatement ps = con.prepareStatement("Delete from keymap");
            ps.executeUpdate();
            ps.close();
            this.Z(44);
        }
        catch (SQLException e) {
            System.out.println("Error " + (Object)e);
        }finally {
            try {
                con.close();
            } catch (SQLException e) {}
        }
    }
    
    private void 清空B() {
        Connection con = DatabaseConnection.getConnection();
        try {
            final PreparedStatement ps = con.prepareStatement("Delete from inventoryslot");
            ps.executeUpdate();
            ps.close();
            this.Z(42);
        }
        catch (SQLException e) {
            System.out.println("Error " + (Object)e);
        }finally {
            try {
                con.close();
            } catch (SQLException e) {}
        }
    }
    
    private void 清空论坛1() {
        Connection con = DatabaseConnection.getConnection();
        try {
            final PreparedStatement ps = con.prepareStatement("Delete from forum_thread");
            ps.executeUpdate();
            ps.close();
            this.Z(30);
        }
        catch (SQLException e) {
            System.out.println("Error " + (Object)e);
        }finally {
            try {
                con.close();
            } catch (SQLException e) {}
        }
    }
    
    private void 清空A() {
        Connection con = DatabaseConnection.getConnection();
        try {
            final PreparedStatement ps = con.prepareStatement("Delete from hypay");
            ps.executeUpdate();
            ps.close();
            this.Z(40);
        }
        catch (SQLException e) {
            System.out.println("Error " + (Object)e);
        }finally {
            try {
                con.close();
            } catch (SQLException e) {}
        }
    }
    
    private void 清空武器2() {
        Connection con = DatabaseConnection.getConnection();
        try {
            final PreparedStatement ps = con.prepareStatement("Delete from inventoryitems");
            ps.executeUpdate();
            ps.close();
            this.Z(38);
        }
        catch (SQLException e) {
            System.out.println("Error " + (Object)e);
        }finally {
            try {
                con.close();
            } catch (SQLException e) {}
        }
    }
    
    private void 清空武器1() {
        Connection con = DatabaseConnection.getConnection();
        try {
            final PreparedStatement ps = con.prepareStatement("Delete from inventoryequipment");
            ps.executeUpdate();
            ps.close();
            this.Z(36);
        }
        catch (SQLException e) {
            System.out.println("Error " + (Object)e);
        }finally {
            try {
                con.close();
            } catch (SQLException e) {}
        }
    }
    
    private void 清空论坛2() {
        Connection con = DatabaseConnection.getConnection();
        try {
            final PreparedStatement ps = con.prepareStatement("Delete from forum_section");
            ps.executeUpdate();
            ps.close();
            this.Z(32);
        }
        catch (SQLException e) {
            System.out.println("Error " + (Object)e);
        }finally {
            try {
                con.close();
            } catch (SQLException e) {}
        }
    }
    
    private void 清空论坛3() {
        Connection con = DatabaseConnection.getConnection();
        try {
            final PreparedStatement ps = con.prepareStatement("Delete from forum_reply");
            ps.executeUpdate();
            ps.close();
            this.Z(34);
        }
        catch (SQLException e) {
            System.out.println("Error " + (Object)e);
        }finally {
            try {
                con.close();
            } catch (SQLException e) {}
        }
    }
    
    private void 清空世界爆物() {
        Connection con = DatabaseConnection.getConnection();
        try {
            final PreparedStatement ps = con.prepareStatement("Delete from drop_data_global");
            ps.executeUpdate();
            ps.close();
            this.Z(28);
        }
        catch (SQLException e) {
            System.out.println("Error " + (Object)e);
        }finally {
            try {
                con.close();
            } catch (SQLException e) {}
        }
    }
    
    private void 清空家族表() {
        this.Z(6);
        Connection con = DatabaseConnection.getConnection();
        try {
            final PreparedStatement ps = con.prepareStatement("Delete from guilds");
            ps.executeUpdate();
            ps.close();
        }
        catch (SQLException e) {
            System.out.println("Error " + (Object)e);
        }finally {
            try {
                con.close();
            } catch (SQLException e) {}
        }
        this.Z(8);
    }
    
    private void 清空角色表() {
        this.Z(10);
        Connection con = DatabaseConnection.getConnection();
        try {
            final PreparedStatement ps = con.prepareStatement("Delete from characters");
            ps.executeUpdate();
            ps.close();
        }
        catch (SQLException e) {
            System.out.println("Error " + (Object)e);
        }finally {
            try {
                con.close();
            } catch (SQLException e) {}
        }
        this.Z(12);
    }
    
    private void 清空核心数据库() {
        this.Z(14);
        Connection con = DatabaseConnection.getConnection();
        try {
            final PreparedStatement ps = con.prepareStatement("Delete from bossrank");
            ps.executeUpdate();
            ps.close();
        }
        catch (SQLException e) {
            System.out.println("Error " + (Object)e);
        }finally {
            try {
                con.close();
            } catch (SQLException e) {}
        }
        this.Z(16);
    }
    
    private void 清空每日列表() {
        this.Z(18);
        Connection con = DatabaseConnection.getConnection();
        try {
            final PreparedStatement ps = con.prepareStatement("Delete from bosslog");
            ps.executeUpdate();
            ps.close();
        }
        catch (SQLException e) {
            System.out.println("Error " + (Object)e);
        }finally {
            try {
                con.close();
            } catch (SQLException e) {}
        }
        this.Z(20);
    }
    
    private void 清空随身仓库() {
        Connection con = DatabaseConnection.getConnection();
        try {
            final PreparedStatement ps = con.prepareStatement("Delete from bank_item");
            ps.executeUpdate();
            ps.close();
        }
        catch (SQLException e) {
            System.out.println("Error " + (Object)e);
        }finally {
            try {
                con.close();
            } catch (SQLException e) {}
        }
        this.Z(22);
    }
    
    private void 清空拍卖1() {
        Connection con = DatabaseConnection.getConnection();
        try {
            final PreparedStatement ps = con.prepareStatement("Delete from auctionitems");
            ps.executeUpdate();
            ps.close();
            this.Z(24);
        }
        catch (SQLException e) {
            System.out.println("Error " + (Object)e);
        }finally {
            try {
                con.close();
            } catch (SQLException e) {}
        }
    }
    
    private void 清空拍卖2() {
        Connection con = DatabaseConnection.getConnection();
        try {
            final PreparedStatement ps = con.prepareStatement("Delete from auctionpoint");
            ps.executeUpdate();
            ps.close();
            this.Z(26);
        }
        catch (SQLException e) {
            System.out.println("Error " + (Object)e);
        }finally {
            try {
                con.close();
            } catch (SQLException e) {}
        }
    }
    
    private void 清空账号() {
        this.Z(2);
        Connection con = DatabaseConnection.getConnection();
        try {
            final PreparedStatement ps = con.prepareStatement("Delete from accounts");
            ps.executeUpdate();
            ps.close();
        }
        catch (SQLException e) {
            System.out.println("Error " + (Object)e);
        }finally {
            try {
                con.close();
            } catch (SQLException e) {}
        }
        this.Z(4);
    }
    
    private void 清空雇佣1() {
        Connection con = DatabaseConnection.getConnection();
        try {
            final PreparedStatement ps = con.prepareStatement("Delete from hiredmerch");
            ps.executeUpdate();
            ps.close();
            this.Z(58);
        }
        catch (SQLException e) {
            System.out.println("Error " + (Object)e);
        }finally {
            try {
                con.close();
            } catch (SQLException e) {}
        }
    }
    
    private void 清空雇佣2() {
        Connection con = DatabaseConnection.getConnection();
        try {
            final PreparedStatement ps = con.prepareStatement("Delete from hiredmerchequipment");
            ps.executeUpdate();
            ps.close();
            this.Z(60);
        }
        catch (SQLException e) {
            System.out.println("Error " + (Object)e);
        }finally {
            try {
                con.close();
            } catch (SQLException e) {}
        }
    }
    
    private void 清空雇佣3() {
        Connection con = DatabaseConnection.getConnection();
        try {
            final PreparedStatement ps = con.prepareStatement("Delete from hiredmerchitems");
            ps.executeUpdate();
            ps.close();
            this.Z(62);
        }
        catch (SQLException e) {
            System.out.println("Error " + (Object)e);
        }finally {
            try {
                con.close();
            } catch (SQLException e) {}
        }
    }
    
    public static void main(final String[] args) {
        try {
            for (final LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals((Object)info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        }
        catch (ClassNotFoundException ex) {
            Logger.getLogger(一键还原.class.getName()).log(Level.SEVERE, null, (Throwable)ex);
        }
        catch (InstantiationException ex2) {
            Logger.getLogger(一键还原.class.getName()).log(Level.SEVERE, null, (Throwable)ex2);
        }
        catch (IllegalAccessException ex3) {
            Logger.getLogger(一键还原.class.getName()).log(Level.SEVERE, null, (Throwable)ex3);
        }
        catch (UnsupportedLookAndFeelException ex4) {
            Logger.getLogger(一键还原.class.getName()).log(Level.SEVERE, null, (Throwable)ex4);
        }
        JFrame.setDefaultLookAndFeelDecorated(true);
        JDialog.setDefaultLookAndFeelDecorated(true);
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            UIManager.setLookAndFeel((LookAndFeel)new SubstanceBusinessBlackSteelLookAndFeel());
        }
        catch (Exception e) {
            //e.printStackTrace();
        }
        EventQueue.invokeLater((Runnable)new Runnable() {
            @Override
            public void run() {
                new 一键还原().setVisible(true);
            }
        });
    }
    
  
}
