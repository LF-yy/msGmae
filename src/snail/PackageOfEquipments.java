//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package snail;

import client.MapleCharacter;
import client.inventory.IItem;
import client.inventory.MapleInventoryType;
import database.DBConPool;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class PackageOfEquipments {
    private static final PackageOfEquipments packageOfEquipments = new PackageOfEquipments();
//    private static List<MyPackage> packageList = Collections.synchronizedList(new ArrayList<>());
    private static List<MyPackage> packageList = new CopyOnWriteArrayList<>();

    public PackageOfEquipments() {
    }

    public static PackageOfEquipments getInstance() {
        return packageOfEquipments;
    }

public void loadFromDB() {
    Connection con = null;
    PreparedStatement ps = null;
    ResultSet rs = null;

    try {
        con = DBConPool.getConnection();
        // 服务端输出信息.println_out("【套装系统】开始从数据库读取套装信息...");
        ps = con.prepareStatement("SELECT * FROM snail_package_equipments");
        rs = ps.executeQuery();

        int count = 0;
        ArrayList<MyPackage> packageList0 = new ArrayList<>();

        while (rs.next()) {
            String itemString0 = rs.getString("itemids");

            if (itemString0.contains("ID") || itemString0.contains("道具")) {
                continue;
            }

            String[] itemString = itemString0.split(",");
            ArrayList<Integer> itemIdList = new ArrayList<>();

            for (String a : itemString) {
                if (a.isEmpty()) {
                    continue;
                }
                try {
                    int id = Integer.parseInt(a);
                    if (id > 0) {
                        itemIdList.add(id);
                    }
                } catch (NumberFormatException e) {
                    // 记录非法 ID 格式
                    // 服务端输出信息.println_err("【警告】发现非法道具ID格式：" + a);
                }
            }

            MyPackage myPackage = new MyPackage(
                    itemIdList,
                    rs.getShort("str"),
                    rs.getShort("dex"),
                    rs.getShort("_int"),
                    rs.getShort("luk"),
                    rs.getShort("all_ap"),
                    rs.getShort("watk"),
                    rs.getShort("matk"),
                    rs.getShort("wdef"),
                    rs.getShort("mdef"),
                    rs.getShort("acc"),
                    rs.getShort("avoid"),
                    rs.getShort("maxhp"),
                    rs.getShort("maxmp"),
                    rs.getShort("speed"),
                    rs.getShort("jump"),
                    rs.getShort("str_percent"),
                    rs.getShort("dex_percent"),
                    rs.getShort("_int_percent"),
                    rs.getShort("luk_percent"),
                    rs.getShort("all_ap_percent"),
                    rs.getShort("watk_percent"),
                    rs.getShort("matk_percent"),
                    rs.getShort("wdef_percent"),
                    rs.getShort("mdef_percent"),
                    rs.getShort("acc_percent"),
                    rs.getShort("avoid_percent"),
                    rs.getShort("maxhp_percent"),
                    rs.getShort("maxmp_percent"),
                    rs.getShort("normal_damage_percent"),
                    rs.getShort("boss_damage_percent"),
                    rs.getShort("total_damage_percent")
            );
            packageList0.add(myPackage);
            ++count;
        }

        // 成功加载后再更新全局列表
        packageList.clear();
        packageList.addAll(packageList0);

        // 服务端输出信息.println_out("【套装系统】读取完毕，共读取" + count + "组套装。");

    } catch (SQLException e) {
        // 服务端输出信息.println_err("【错误】：loadFromDB错误，错误原因：" + e);
        e.printStackTrace();
    } finally {
        // 关闭所有资源
        if (rs != null) {
            try { rs.close(); } catch (SQLException ignored) {}
        }
        if (ps != null) {
            try { ps.close(); } catch (SQLException ignored) {}
        }
        if (con != null) {
            try { con.close(); } catch (SQLException ignored) {}
        }
    }
}

    public ArrayList<MyPackage> getPackages(ArrayList<Integer> itemIdList) {
        if (itemIdList == null) {
            return null;
        } else {
            ArrayList<MyPackage> ret = new ArrayList<>();
            Iterator var3 = packageList.iterator();

            while(true) {
                MyPackage myPackage;
                do {
                    if (!var3.hasNext()) {
                        return ret;
                    }

                    myPackage = (MyPackage)var3.next();
                } while(myPackage == null);

                ArrayList<Integer> itemIdList0 = myPackage.getItemIdList();
                int a = 0;
                Iterator var7 = itemIdList0.iterator();

                while(var7.hasNext()) {
                    int id = (Integer)var7.next();
                    if (itemIdList.contains(id)) {
                        ++a;
                    }
                }

                if (a >= itemIdList0.size()) {
                    ret.add(myPackage);
                }
            }
        }
    }

    public List<MyPackage> getPackageList() {
        return packageList;
    }

    public ArrayList<MyPackage> getPackage(MapleCharacter chr) {
        if (chr == null) {
            return null;
        } else {
            ArrayList<IItem> itemList = new ArrayList<>(chr.getInventory(MapleInventoryType.EQUIPPED).list());
            ArrayList<Integer> itemIdList = new ArrayList<>();
            for (IItem iItem : itemList) {
                if (iItem != null) {
                    itemIdList.add(iItem.getItemId());
                }
            }
            return this.getPackages(itemIdList);
        }
    }

    public static class MyPackage {
        private ArrayList<Integer> itemIdList;
        private short str;
        private short dex;
        private short _int;
        private short luk;
        private short all_ap;
        private short watk;
        private short matk;
        private short wdef;
        private short mdef;
        private short acc;
        private short avoid;
        private short maxhp;
        private short maxmp;
        private short speed;
        private short jump;
        private short str_percent;
        private short dex_percent;
        private short _int_percent;
        private short luk_percent;
        private short all_ap_percent;
        private short watk_percent;
        private short matk_percent;
        private short wdef_percent;
        private short mdef_percent;
        private short acc_percent;
        private short avoid_percent;
        private short maxhp_percent;
        private short maxmp_percent;
        private short normal_damage_percent;
        private short boss_damage_percent;
        private short total_damage_percent;

        private MyPackage(ArrayList<Integer> itemIdList) {
            this.itemIdList = new ArrayList<>(itemIdList);
            this.str = 0;
            this.dex = 0;
            this._int = 0;
            this.luk = 0;
            this.all_ap = 0;
            this.watk = 0;
            this.matk = 0;
            this.wdef = 0;
            this.mdef = 0;
            this.acc = 0;
            this.avoid = 0;
            this.maxhp = 0;
            this.maxmp = 0;
            this.speed = 0;
            this.jump = 0;
            this.str_percent = 0;
            this.dex_percent = 0;
            this._int_percent = 0;
            this.luk_percent = 0;
            this.all_ap_percent = 0;
            this.watk_percent = 0;
            this.matk_percent = 0;
            this.wdef_percent = 0;
            this.mdef_percent = 0;
            this.acc_percent = 0;
            this.avoid_percent = 0;
            this.maxhp_percent = 0;
            this.maxmp_percent = 0;
            this.normal_damage_percent = 0;
            this.boss_damage_percent = 0;
            this.total_damage_percent = 0;
        }

        private MyPackage(ArrayList<Integer> itemIdList, short str, short dex, short _int, short luk, short all_ap, short watk, short matk, short wdef, short mdef, short acc, short avoid, short maxhp, short maxmp, short speed, short jump, short str_percent, short dex_percent, short _int_percent, short luk_percent, short all_ap_percent, short watk_percent, short matk_percent, short wdef_percent, short mdef_percent, short acc_percent, short avoid_percent, short maxhp_percent, short maxmp_percent, short normal_damage_percent, short boss_damage_percent, short total_damage_percent) {
            this.itemIdList = new ArrayList<>(itemIdList);
            this.str = str;
            this.dex = dex;
            this._int = _int;
            this.luk = luk;
            this.all_ap = all_ap;
            this.watk = watk;
            this.matk = matk;
            this.wdef = wdef;
            this.mdef = mdef;
            this.acc = acc;
            this.avoid = avoid;
            this.maxhp = maxhp;
            this.maxmp = maxmp;
            this.speed = speed;
            this.jump = jump;
            this.str_percent = str_percent;
            this.dex_percent = dex_percent;
            this._int_percent = _int_percent;
            this.luk_percent = luk_percent;
            this.all_ap_percent = all_ap_percent;
            this.watk_percent = watk_percent;
            this.matk_percent = matk_percent;
            this.wdef_percent = wdef_percent;
            this.mdef_percent = mdef_percent;
            this.acc_percent = acc_percent;
            this.avoid_percent = avoid_percent;
            this.maxhp_percent = maxhp_percent;
            this.maxmp_percent = maxmp_percent;
            this.normal_damage_percent = normal_damage_percent;
            this.boss_damage_percent = boss_damage_percent;
            this.total_damage_percent = total_damage_percent;
        }

        public ArrayList<Integer> getItemIdList() {
            return this.itemIdList;
        }

        public void setItemIdList(ArrayList<Integer> itemIdList) {
            this.itemIdList = itemIdList;
        }

        public short getStr() {
            return this.str;
        }

        public void setStr(short str) {
            this.str = str;
        }

        public short getDex() {
            return this.dex;
        }

        public void setDex(short dex) {
            this.dex = dex;
        }

        public short get_int() {
            return this._int;
        }

        public void set_int(short _int) {
            this._int = _int;
        }

        public short getLuk() {
            return this.luk;
        }

        public void setLuk(short luk) {
            this.luk = luk;
        }

        public short getAll_ap() {
            return this.all_ap;
        }

        public void setAll_ap(short all_ap) {
            this.all_ap = all_ap;
        }

        public short getWatk() {
            return this.watk;
        }

        public void setWatk(short watk) {
            this.watk = watk;
        }

        public short getMatk() {
            return this.matk;
        }

        public void setMatk(short matk) {
            this.matk = matk;
        }

        public short getWdef() {
            return this.wdef;
        }

        public void setWdef(short wdef) {
            this.wdef = wdef;
        }

        public short getMdef() {
            return this.mdef;
        }

        public void setMdef(short mdef) {
            this.mdef = mdef;
        }

        public short getAcc() {
            return this.acc;
        }

        public void setAcc(short acc) {
            this.acc = acc;
        }

        public short getAvoid() {
            return this.avoid;
        }

        public void setAvoid(short avoid) {
            this.avoid = avoid;
        }

        public short getMaxhp() {
            return this.maxhp;
        }

        public void setMaxhp(short maxhp) {
            this.maxhp = maxhp;
        }

        public short getMaxmp() {
            return this.maxmp;
        }

        public void setMaxmp(short maxmp) {
            this.maxmp = maxmp;
        }

        public short getSpeed() {
            return this.speed;
        }

        public void setSpeed(short speed) {
            this.speed = speed;
        }

        public short getJump() {
            return this.jump;
        }

        public void setJump(short jump) {
            this.jump = jump;
        }

        public short getStr_percent() {
            return this.str_percent;
        }

        public void setStr_percent(short str_percent) {
            this.str_percent = str_percent;
        }

        public short getDex_percent() {
            return this.dex_percent;
        }

        public void setDex_percent(short dex_percent) {
            this.dex_percent = dex_percent;
        }

        public short get_int_percent() {
            return this._int_percent;
        }

        public void set_int_percent(short _int_percent) {
            this._int_percent = _int_percent;
        }

        public short getLuk_percent() {
            return this.luk_percent;
        }

        public void setLuk_percent(short luk_percent) {
            this.luk_percent = luk_percent;
        }

        public short getAll_ap_percent() {
            return this.all_ap_percent;
        }

        public void setAll_ap_percent(short all_ap_percent) {
            this.all_ap_percent = all_ap_percent;
        }

        public short getWatk_percent() {
            return this.watk_percent;
        }

        public void setWatk_percent(short watk_percent) {
            this.watk_percent = watk_percent;
        }

        public short getMatk_percent() {
            return this.matk_percent;
        }

        public void setMatk_percent(short matk_percent) {
            this.matk_percent = matk_percent;
        }

        public short getWdef_percent() {
            return this.wdef_percent;
        }

        public void setWdef_percent(short wdef_percent) {
            this.wdef_percent = wdef_percent;
        }

        public short getMdef_percent() {
            return this.mdef_percent;
        }

        public void setMdef_percent(short mdef_percent) {
            this.mdef_percent = mdef_percent;
        }

        public short getAcc_percent() {
            return this.acc_percent;
        }

        public void setAcc_percent(short acc_percent) {
            this.acc_percent = acc_percent;
        }

        public short getAvoid_percent() {
            return this.avoid_percent;
        }

        public void setAvoid_percent(short avoid_percent) {
            this.avoid_percent = avoid_percent;
        }

        public short getMaxhp_percent() {
            return this.maxhp_percent;
        }

        public void setMaxhp_percent(short maxhp_percent) {
            this.maxhp_percent = maxhp_percent;
        }

        public short getMaxmp_percent() {
            return this.maxmp_percent;
        }

        public void setMaxmp_percent(short maxmp_percent) {
            this.maxmp_percent = maxmp_percent;
        }

        public short getNormal_damage_percent() {
            return this.normal_damage_percent;
        }

        public void setNormal_damage_percent(short normal_damage_percent) {
            this.normal_damage_percent = normal_damage_percent;
        }

        public short getBoss_damage_percent() {
            return this.boss_damage_percent;
        }

        public void setBoss_damage_percent(short boss_damage_percent) {
            this.boss_damage_percent = boss_damage_percent;
        }

        public short getTotal_damage_percent() {
            return this.total_damage_percent;
        }

        public void setTotal_damage_percent(short total_damage_percent) {
            this.total_damage_percent = total_damage_percent;
        }
    }
}
