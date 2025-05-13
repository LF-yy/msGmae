//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package snail;

import database.DBConPool;

import tools.FileoutputUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;

public class RedeemCodeUtils {
    public RedeemCodeUtils() {
    }

    public static String createBigSmallLetterStrOrNumberRadom(int num) {
        StringBuilder str = new StringBuilder();

        for(int i = 1; i <= num; ++i) {
            int intVal = (int)(Math.random() * 58.0 + 65.0);
            if (intVal >= 91 && intVal <= 96) {
                --i;
            }

            if (intVal < 91 || intVal > 96) {
                if (intVal % 2 == 0) {
                    str.append((char)intVal);
                } else {
                    str.append((int)(Math.random() * 10.0));
                }

                if (i % 4 == 0) {
                    str.append("-");
                }
            }
        }

        return str.substring(0, str.length() - 1);
    }

    public static String createSmallStrOrNumberRadom(int num) {
        StringBuilder str = new StringBuilder();

        for(int i = 1; i <= num; ++i) {
            int intVal = (int)(Math.random() * 26.0 + 97.0);
            if (intVal % 2 == 0) {
                str.append((char)intVal);
            } else {
                str.append((int)(Math.random() * 10.0));
            }

            if (i % 4 == 0) {
                str.append("-");
            }
        }

        return str.substring(0, str.length() - 1);
    }

    public static String createBigStrOrNumberRadom(int num) {
        StringBuilder str = new StringBuilder();

        for(int i = 1; i <= num; ++i) {
            int intVal = (int)(Math.random() * 26.0 + 65.0);
            if (intVal % 2 == 0) {
                str.append((char)intVal);
            } else {
                str.append((int)(Math.random() * 10.0));
            }

            if (i % 4 == 0) {
                str.append("-");
            }
        }

        return str.substring(0, str.length() - 1);
    }

    public static boolean newCode(int unitNum, String type, int itemId, int itemMount, int quantity) {
        if (unitNum < 4) {
            unitNum = 4;
        }

        if (quantity < 1) {
            quantity = 1;
        }

        if (itemMount < 1) {
            itemMount = 1;
        }

        if (type.equals("")) {
            return false;
        } else {
            try {
                Connection con = DBConPool.getConnection();
                Throwable var6 = null;

                try {
                    PreparedStatement ps = con.prepareStatement("INSERT INTO snail_codelist (type, code, itemid, mount, taken, used) VALUES ( ?, ?, ?, ?, ?, ?)");

                    for(int i = 0; i < quantity; ++i) {
                        ps.setString(1, type);
                        ps.setString(2, createBigStrOrNumberRadom(unitNum * 4));
                        ps.setInt(3, itemId);
                        ps.setInt(4, itemMount);
                        ps.setShort(5, (short)0);
                        ps.setShort(6, (short)0);
                        ps.executeUpdate();
                    }

                    ps.close();
                    return true;
                } catch (Throwable var17) {
                    var6 = var17;
                    throw var17;
                } finally {
                    if (con != null) {
                        if (var6 != null) {
                            try {
                                con.close();
                            } catch (Throwable var16) {
                                var6.addSuppressed(var16);
                            }
                        } else {
                            con.close();
                        }
                    }

                }
            } catch (SQLException var19) {
                //服务端输出信息.println_err("【错误】RedeemCodeUtils.newCode执行错误，错误原因：" + var19);
                var19.printStackTrace();
                return false;
            }
        }
    }

    public static ArrayList<String> getCode(String type, int quantity) {
        ArrayList<String> code = new ArrayList();
        ArrayList<Integer> itemId = new ArrayList();
        ArrayList<Integer> mount = new ArrayList();
        if (type.equals("")) {
            return null;
        } else {
            try {
                Connection con = DBConPool.getConnection();
                Throwable var6 = null;

                try {
                    PreparedStatement ps = con.prepareStatement("SELECT * FROM snail_codelist WHERE type = ? and used = 0 and taken = 0");
                    ps.setString(1, type);
                    ResultSet rs = ps.executeQuery();

                    for(int count = 0; rs.next() && count < quantity; ++count) {
                        PreparedStatement ps2 = con.prepareStatement("UPDATE snail_codelist SET taken = 1 WHERE id = ?");
                        ps2.setInt(1, rs.getInt("id"));
                        ps2.executeUpdate();
                        ps2.close();
                        code.add(rs.getString("code"));
                        itemId.add(rs.getInt("itemid"));
                        mount.add(rs.getInt("mount"));
                    }

                    ps.close();
                    rs.close();
                } catch (Throwable var19) {
                    var6 = var19;
                    throw var19;
                } finally {
                    if (con != null) {
                        if (var6 != null) {
                            try {
                                con.close();
                            } catch (Throwable var18) {
                                var6.addSuppressed(var18);
                            }
                        } else {
                            con.close();
                        }
                    }

                }
            } catch (SQLException var21) {
                //服务端输出信息.println_err("【错误】RedeemCodeUtils.getCode执行错误，错误原因：" + var21);
                var21.printStackTrace();
            }

            if (!code.isEmpty()) {
                String text = "----------------";
                text = text + Calendar.getInstance().getTime() + "----------------\r\n";
                text = text + "                    道具ID：" + itemId.get(0) + "            道具数量:" + mount.get(0) + "\r\n";

                for(int i = 0; i < code.size(); ++i) {
                    text = text + (String)code.get(i) + "\r\n";
                }

                text = text + "------------已发放的兑换码请自行记录，避免重复发放------------\r\n\r\n";
                FileoutputUtil.logToFile("codes/" + type + ".txt", text);
            }

            return code;
        }
    }

    public static int getCodeQuantity(String type, boolean used, boolean taken) {
        if (type.equals("")) {
            return 0;
        } else {
            int count = 0;

            try {
                Connection con = DBConPool.getConnection();
                Throwable var5 = null;

                try {
                    PreparedStatement ps = con.prepareStatement("SELECT count(*) FROM snail_codelist WHERE type = ? and used = ? and taken = ?");
                    ps.setString(1, type);
                    if (used) {
                        ps.setShort(2, (short)1);
                    } else {
                        ps.setShort(2, (short)0);
                    }

                    if (taken) {
                        ps.setShort(3, (short)1);
                    } else {
                        ps.setShort(3, (short)0);
                    }

                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) {
                        count = rs.getInt(1);
                    }

                    ps.close();
                    rs.close();
                } catch (Throwable var16) {
                    var5 = var16;
                    throw var16;
                } finally {
                    if (con != null) {
                        if (var5 != null) {
                            try {
                                con.close();
                            } catch (Throwable var15) {
                                var5.addSuppressed(var15);
                            }
                        } else {
                            con.close();
                        }
                    }

                }

                return count;
            } catch (SQLException var18) {
                //服务端输出信息.println_err("【错误】RedeemCodeUtils.getCode执行错误，错误原因：" + var18);
                var18.printStackTrace();
                return 0;
            }
        }
    }

    public static boolean checkCode(String type, String code) {
        if (type.equals("")) {
            return false;
        } else if (code.equals("")) {
            return false;
        } else {
            code = code.replaceAll("-", "");
            code = code.replaceAll("\r", "");
            code = code.replaceAll("\n", "");
            code = code.replaceAll(" ", "");
            boolean found = false;

            try {
                Connection con = DBConPool.getConnection();
                Throwable var4 = null;

                try {
                    PreparedStatement ps = con.prepareStatement("SELECT * FROM snail_codelist WHERE type = ?");
                    ps.setString(1, type);
                    ResultSet rs = ps.executeQuery();

                    while(rs.next()) {
                        String code0 = rs.getString("code");
                        code0 = code0.replaceAll("-", "");
                        code0 = code0.replaceAll("\r", "");
                        code0 = code0.replaceAll("\n", "");
                        code0 = code0.replaceAll(" ", "");
                        if (code.equals(code0)) {
                            if (rs.getShort("used") > 0) {
                                ps.close();
                                rs.close();
                                boolean var8 = false;
                                return var8;
                            }

                            found = true;
                            break;
                        }
                    }

                    ps.close();
                    rs.close();
                } catch (Throwable var19) {
                    var4 = var19;
                    throw var19;
                } finally {
                    if (con != null) {
                        if (var4 != null) {
                            try {
                                con.close();
                            } catch (Throwable var18) {
                                var4.addSuppressed(var18);
                            }
                        } else {
                            con.close();
                        }
                    }

                }

                return found;
            } catch (SQLException var21) {
                //服务端输出信息.println_err("【错误】RedeemCodeUtils.checkCode执行错误，错误原因：" + var21);
                var21.printStackTrace();
                return false;
            }
        }
    }

    public static boolean useCode(String type, String code) {
        return useCode(type, code, "");
    }

    public static boolean useCode(String type, String code, String user) {
        if (type.equals("")) {
            return false;
        } else if (code.equals("")) {
            return false;
        } else {
            code = code.replaceAll("-", "");
            code = code.replaceAll("\r", "");
            code = code.replaceAll("\n", "");
            code = code.replaceAll(" ", "");

            try {
                Connection con = DBConPool.getConnection();
                Throwable var4 = null;

                boolean var24;
                try {
                    PreparedStatement ps = con.prepareStatement("SELECT * FROM snail_codelist WHERE type = ?");
                    ps.setString(1, type);
                    ResultSet rs = ps.executeQuery();
                    int id = 0;

                    while(rs.next()) {
                        String code0 = rs.getString("code");
                        code0 = code0.replaceAll("-", "");
                        code0 = code0.replaceAll("\r", "");
                        code0 = code0.replaceAll("\n", "");
                        code0 = code0.replaceAll(" ", "");
                        if (code.equals(code0)) {
                            if (rs.getShort("used") > 0) {
                                ps.close();
                                rs.close();
                                boolean var9 = false;
                                return var9;
                            }

                            id = rs.getInt("id");
                            break;
                        }
                    }

                    if (id > 0) {
                        ps = con.prepareStatement("UPDATE snail_codelist SET used = 1 , `user` = ? WHERE `id` = ?");
                        ps.setString(1, user);
                        ps.setInt(2, id);
                        ps.executeUpdate();
                        ps.close();
                        rs.close();
                        return true;
                    }

                    var24 = false;
                } catch (Throwable var21) {
                    var4 = var21;
                    throw var21;
                } finally {
                    if (con != null) {
                        if (var4 != null) {
                            try {
                                con.close();
                            } catch (Throwable var20) {
                                var4.addSuppressed(var20);
                            }
                        } else {
                            con.close();
                        }
                    }

                }

                return var24;
            } catch (SQLException var23) {
                //服务端输出信息.println_err("【错误】RedeemCodeUtils.newCode执行错误，错误原因：" + var23);
                var23.printStackTrace();
                return false;
            }
        }
    }

//    public static int getItemId(String type, String code) {
//        if (type.equals("")) {
//            return -1;
//        } else if (code.equals("")) {
//            return -1;
//        } else {
//            code = code.replaceAll("-", "");
//            code = code.replaceAll("\r", "");
//            code = code.replaceAll("\n", "");
//            code = code.replaceAll(" ", "");
//            int itemId = -1;
//
//            try {
//                Connection con = DBConPool.getConnection();
//                Throwable var4 = null;
//
//                try {
//                    PreparedStatement ps = con.prepareStatement("SELECT * FROM snail_codelist WHERE type = ?");
//                    ps.setString(1, type);
//                    ResultSet rs = ps.executeQuery();
//
//                    while(true) {
//                        if (rs.next()) {
//                            String code0 = rs.getString("code");
//                            code0 = code0.replaceAll("-", "");
//                            code0 = code0.replaceAll("\r", "");
//                            code0 = code0.replaceAll("\n", "");
//                            code0 = code0.replaceAll(" ", "");
//                            if (!code0.equals(code)) {
//                                continue;
//                            }
//
//                            itemId = rs.getInt("itemid");
//                        }
//
//                        ps.close();
//                        rs.close();
//                        return itemId;
//                    }
//                } catch (Throwable var16) {
//                    var4 = var16;
//                    throw var16;
//                } finally {
//                    if (con != null) {
//                        if (var4 != null) {
//                            try {
//                                con.close();
//                            } catch (Throwable var15) {
//                                var4.addSuppressed(var15);
//                            }
//                        } else {
//                            con.close();
//                        }
//                    }
//
//                }
//            } catch (SQLException var18) {
//                //服务端输出信息.println_err("【错误】RedeemCodeUtils.getItemId执行错误，错误原因：" + var18);
//                var18.printStackTrace();
//                return -1;
//            }
//        }
//    }
public static int getItemId(String type, String code) {
    if (type == null || type.isEmpty() || code == null || code.isEmpty()) {
        return -1;
    }

    // 清理 code 字符串
    code = code.replaceAll("[-\\s]", "");

    int itemId = -1;

    String sql = "SELECT itemid FROM snail_codelist WHERE type = ? AND REPLACE(REPLACE(REPLACE(REPLACE(code, '-', ''), '\r', ''), '\n', ''), ' ', '') = ?";

    try (Connection con = DBConPool.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {

        ps.setString(1, type);
        ps.setString(2, code);

        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                itemId = rs.getInt("itemid");
            }
        }

    } catch (SQLException e) {
        // 建议使用日志框架替代 printStackTrace
        e.printStackTrace();
        return -1;
    }

    return itemId;
}

    public static int getItemMount(String type, String code) {
        if (type.equals("")) {
            return 0;
        } else if (code.equals("")) {
            return 0;
        } else {
            code = code.replaceAll("-", "");
            code = code.replaceAll("\r", "");
            code = code.replaceAll("\n", "");
            code = code.replaceAll(" ", "");
            int mount = 0;

            try {
                Connection con = DBConPool.getConnection();
                Throwable var4 = null;

                try {
                    PreparedStatement ps = con.prepareStatement("SELECT * FROM snail_codelist WHERE type = ?");
                    ps.setString(1, type);
                    ResultSet rs = ps.executeQuery();

                    while(rs.next()) {
                        String code0 = rs.getString("code");
                        code0 = code0.replaceAll("-", "");
                        code0 = code0.replaceAll("\r", "");
                        code0 = code0.replaceAll("\n", "");
                        code0 = code0.replaceAll(" ", "");
                        if (code0.equals(code)) {
                            mount = rs.getInt("mount");
                        }
                    }

                    ps.close();
                    rs.close();
                    return mount;
                } catch (Throwable var16) {
                    var4 = var16;
                    throw var16;
                } finally {
                    if (con != null) {
                        if (var4 != null) {
                            try {
                                con.close();
                            } catch (Throwable var15) {
                                var4.addSuppressed(var15);
                            }
                        } else {
                            con.close();
                        }
                    }

                }
            } catch (SQLException var18) {
                //服务端输出信息.println_err("【错误】RedeemCodeUtils.getMount执行错误，错误原因：" + var18);
                var18.printStackTrace();
                return 0;
            }
        }
    }
}
