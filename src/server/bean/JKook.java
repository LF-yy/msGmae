//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package server.bean;

import database.DBConPool;
import gui.服务端输出信息;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class JKook {
    public JKook() {
    }

    public static int addRewardCount(int chrId) {
        try {
            Connection con = DBConPool.getConnection();
            Throwable var2 = null;

            byte var5;
            try {
                PreparedStatement ps = con.prepareStatement("SELECT * FROM snail_kook_data WHERE character_id = ?");
                ps.setInt(1, chrId);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    if (rs.getInt("rewarded") > 0) {
                        ps.close();
                        rs.close();
                        var5 = -1;
                        return var5;
                    }

                    ps = con.prepareStatement("UPDATE snail_kook_data SET rewarded = 1 WHERE character_id = ?");
                    ps.setInt(1, chrId);
                    ps.executeUpdate();
                    String kookId = rs.getString("kook_id");
                    ps = con.prepareStatement("SELECT * FROM snail_kook_reward WHERE kook_id = ?");
                    ps.setString(1, kookId);
                    rs = ps.executeQuery();
                    if (rs.next()) {
                        int count = rs.getInt("rewarded_count");
                        ps = con.prepareStatement("UPDATE snail_kook_reward SET rewarded_count = ? WHERE kook_id = ?");
                        ps.setInt(1, count + 1);
                        ps.setString(2, kookId);
                        ps.executeUpdate();
                        ps = con.prepareStatement("UPDATE snail_kook_data SET reward = 1 WHERE kook_id = ?");
                        ps.setString(1, kookId);
                        ps.executeUpdate();
                    } else {
                        ps = con.prepareStatement("INSERT INTO snail_kook_reward (kook_id, rewarded_count) VALUES (?, 1)");
                        ps.setString(1, kookId);
                        ps.executeUpdate();
                    }

                    ps.close();
                    rs.close();
                    byte var22 = 1;
                    return var22;
                }

                ps.close();
                rs.close();
                var5 = -2;
            } catch (Throwable var18) {
                var2 = var18;
                throw var18;
            } finally {
                if (con != null) {
                    if (var2 != null) {
                        try {
                            con.close();
                        } catch (Throwable var17) {
                            var2.addSuppressed(var17);
                        }
                    } else {
                        con.close();
                    }
                }

            }

            return var5;
        } catch (SQLException var20) {
            var20.printStackTrace();
            服务端输出信息.println_err("【错误】JKook addReward错误，原因：" + var20);
            return 0;
        }
    }

    public static String getKookId(int chrId) {
        String kookId = "";

        try {
            Connection con = DBConPool.getConnection();
            Throwable var3 = null;

            try {
                PreparedStatement ps = con.prepareStatement("SELECT * FROM snail_kook_data WHERE character_id = ?");
                ps.setInt(1, chrId);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    kookId = rs.getString("kook_id");
                }
            } catch (Throwable var14) {
                var3 = var14;
                throw var14;
            } finally {
                if (con != null) {
                    if (var3 != null) {
                        try {
                            con.close();
                        } catch (Throwable var13) {
                            var3.addSuppressed(var13);
                        }
                    } else {
                        con.close();
                    }
                }

            }

            return kookId;
        } catch (SQLException var16) {
            var16.printStackTrace();
            服务端输出信息.println_err("【错误】JKook getKookId错误，原因：" + var16);
            return "";
        }
    }

    public static boolean isBind(int chrId) {
        return !getKookId(chrId).equals("");
    }

    public static boolean isRewarded(int chrId) {
        try {
            Connection con = DBConPool.getConnection();
            Throwable var2 = null;

            boolean var5;
            try {
                PreparedStatement ps = con.prepareStatement("SELECT * FROM snail_kook_data WHERE character_id = ?");
                ps.setInt(1, chrId);
                ResultSet rs = ps.executeQuery();
                if (!rs.next()) {
                    return false;
                }

                var5 = rs.getInt("rewarded") > 0;
            } catch (Throwable var16) {
                var2 = var16;
                throw var16;
            } finally {
                if (con != null) {
                    if (var2 != null) {
                        try {
                            con.close();
                        } catch (Throwable var15) {
                            var2.addSuppressed(var15);
                        }
                    } else {
                        con.close();
                    }
                }

            }

            return var5;
        } catch (SQLException var18) {
            var18.printStackTrace();
            服务端输出信息.println_err("【错误】JKook getKookId错误，原因：" + var18);
            return false;
        }
    }

    public static int getRewardCount(int chrId) {
        String kookId = getKookId(chrId);

        try {
            Connection con = DBConPool.getConnection();
            Throwable var3 = null;

            int var6;
            try {
                PreparedStatement ps = con.prepareStatement("SELECT * FROM snail_kook_reward WHERE kook_id = ?");
                ps.setString(1, kookId);
                ResultSet rs = ps.executeQuery();
                if (!rs.next()) {
                    return 0;
                }

                var6 = rs.getInt("rewarded_count");
            } catch (Throwable var17) {
                var3 = var17;
                throw var17;
            } finally {
                if (con != null) {
                    if (var3 != null) {
                        try {
                            con.close();
                        } catch (Throwable var16) {
                            var3.addSuppressed(var16);
                        }
                    } else {
                        con.close();
                    }
                }

            }

            return var6;
        } catch (SQLException var19) {
            var19.printStackTrace();
            服务端输出信息.println_err("【错误】JKook getKookId错误，原因：" + var19);
            return 0;
        }
    }
}
