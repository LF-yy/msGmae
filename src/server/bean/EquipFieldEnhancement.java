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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class EquipFieldEnhancement {
    private Map<Integer, Map<Integer, EquipField>> chrEquipFieldMap = new HashMap();
    private static EquipFieldEnhancement instance = new EquipFieldEnhancement();

    public EquipFieldEnhancement() {
    }

    public static EquipFieldEnhancement getInstance() {
        return instance;
    }

    public boolean loadChrEquipFieldMapFromDB() {
        Map<Integer, Map<Integer, EquipField>> ret = new HashMap();

        try {
            Connection con = DBConPool.getConnection();
            Throwable var3 = null;

            try {
                PreparedStatement ps = con.prepareStatement("SELECT * FROM snail_equipment_field_enhancement");
                ResultSet rs = ps.executeQuery();

                while(rs.next()) {
                    int chrId = rs.getInt("characterid");
                    if (ret.containsKey(chrId)) {
                        Map<Integer, EquipField> equipFieldMap = (Map)ret.get(chrId);
                        equipFieldMap.put(rs.getInt("position"), new EquipField(rs.getInt("position"), rs.getShort("str"), rs.getShort("dex"), rs.getShort("int"), rs.getShort("luk"), rs.getShort("hp"), rs.getShort("mp"), rs.getShort("watk"), rs.getShort("matk"), rs.getShort("wdef"), rs.getShort("mdef"), rs.getShort("acc"), rs.getShort("avoid"), rs.getShort("speed"), rs.getShort("jump"), rs.getInt("str_p"), rs.getInt("dex_p"), rs.getInt("int_p"), rs.getInt("luk_p"), rs.getInt("hp_p"), rs.getInt("mp_p"), rs.getInt("watk_p"), rs.getInt("matk_p"), rs.getInt("wdef_p"), rs.getInt("mdef_p"), rs.getInt("acc_p"), rs.getInt("avoid_p"), rs.getInt("totalDamage"), rs.getInt("bossDamage"), rs.getInt("normalDamage"), rs.getInt("must_kill"), rs.getInt("invincible"), rs.getInt("strong"), rs.getInt("suck_hp"), rs.getInt("suck_mp"), rs.getInt("growable_hp"), rs.getInt("growable_mp"), rs.getInt("more_exp"), rs.getInt("more_meso"), rs.getInt("more_drop"), rs.getInt("revive"), rs.getInt("summon_mob"), rs.getInt("consume_recover")));
                        ret.put(chrId, equipFieldMap);
                    } else {
                        Map<Integer, EquipField> equipFieldMap = new HashMap();
                        equipFieldMap.put(rs.getInt("position"), new EquipField(rs.getInt("position"), rs.getShort("str"), rs.getShort("dex"), rs.getShort("int"), rs.getShort("luk"), rs.getShort("hp"), rs.getShort("mp"), rs.getShort("watk"), rs.getShort("matk"), rs.getShort("wdef"), rs.getShort("mdef"), rs.getShort("acc"), rs.getShort("avoid"), rs.getShort("speed"), rs.getShort("jump"), rs.getInt("str_p"), rs.getInt("dex_p"), rs.getInt("int_p"), rs.getInt("luk_p"), rs.getInt("hp_p"), rs.getInt("mp_p"), rs.getInt("watk_p"), rs.getInt("matk_p"), rs.getInt("wdef_p"), rs.getInt("mdef_p"), rs.getInt("acc_p"), rs.getInt("avoid_p"), rs.getInt("totalDamage"), rs.getInt("bossDamage"), rs.getInt("normalDamage"), rs.getInt("must_kill"), rs.getInt("invincible"), rs.getInt("strong"), rs.getInt("suck_hp"), rs.getInt("suck_mp"), rs.getInt("growable_hp"), rs.getInt("growable_mp"), rs.getInt("more_exp"), rs.getInt("more_meso"), rs.getInt("more_drop"), rs.getInt("revive"), rs.getInt("summon_mob"), rs.getInt("consume_recover")));
                        ret.put(chrId, equipFieldMap);
                    }
                }

                this.clearChrEquipFieldMap();
                this.chrEquipFieldMap = ret;
                rs.close();
                ps.close();
                boolean var19 = true;
                return var19;
            } catch (Throwable var16) {
                var3 = var16;
                throw var16;
            } finally {
                if (con != null) {
                    if (var3 != null) {
                        try {
                            con.close();
                        } catch (Throwable var15) {
                            var3.addSuppressed(var15);
                        }
                    } else {
                        con.close();
                    }
                }

            }
        } catch (SQLException var18) {
            服务端输出信息.println_err("【错误】loadChrEquipFieldMapFromDB错误，原因：" + var18);
            var18.printStackTrace();
            return false;
        }
    }

    public void clearChrEquipFieldMap() {
        Iterator var1 = this.chrEquipFieldMap.entrySet().iterator();

        while(var1.hasNext()) {
            Map.Entry<Integer, Map<Integer, EquipField>> entry = (Map.Entry)var1.next();
            ((Map)entry.getValue()).clear();
        }

        this.chrEquipFieldMap.clear();
    }

    public EquipField getChrEquipField(int chrId, int position) {
        if (this.chrEquipFieldMap.containsKey(chrId)) {
            EquipField equipField = (EquipField)((Map)this.chrEquipFieldMap.get(chrId)).get(position);
            if (equipField == null) {
                equipField = new EquipField(position);
                ((Map)this.chrEquipFieldMap.get(chrId)).put(position, equipField);
            }

            return equipField;
        } else {
            Map<Integer, EquipField> equipFieldMap = new HashMap();
            EquipField equipField = new EquipField(position);
            equipFieldMap.put(position, equipField);
            this.chrEquipFieldMap.put(chrId, equipFieldMap);
            return equipField;
        }
    }

    public Map<Integer, EquipField> getChrEquipFieldMap(int chrId) {
        return (Map)this.chrEquipFieldMap.get(chrId);
    }

    public boolean saveChrToDB(int chrId) {
        return this.saveChrToDB(chrId, DBConPool.getConnection());
    }

    public boolean saveChrToDB(int chrId, Connection con) {
        if (this.chrEquipFieldMap.get(chrId) == null) {
            return false;
        } else {
            boolean isConNull = false;
            Map<Integer, EquipField> equipFieldMap = new HashMap((Map)this.chrEquipFieldMap.get(chrId));
            if (equipFieldMap != null) {
                try {
                    if (con == null) {
                        con = DBConPool.getConnection();
                        isConNull = true;
                        con.setTransactionIsolation(1);
                        con.setAutoCommit(false);
                    }

                    PreparedStatement ps = con.prepareStatement("SELECT * FROM snail_equipment_field_enhancement WHERE characterid = ?");
                    ps.setInt(1, chrId);
                    ResultSet rs = ps.executeQuery();
                    int batchSize = 1000;
                    int count = 0;
                    PreparedStatement ps2 = con.prepareStatement("UPDATE snail_equipment_field_enhancement SET str = ?, dex = ?, `int` = ?, luk = ?, hp = ?, mp = ?, watk = ?, matk = ?, wdef = ?, mdef = ?, acc = ?, avoid = ?, speed = ?, jump = ?, str_p = ?, dex_p = ?, int_p = ?, luk_p = ?, hp_p = ?, mp_p = ?, watk_p = ?, matk_p = ?, wdef_p = ?, mdef_p = ?, acc_p = ?, avoid_p = ?, totalDamage = ?, bossDamage = ?, normalDamage = ?, must_kill = ?, invincible = ?, strong = ?, suck_hp = ?, suck_mp = ?, growable_hp = ?, growable_mp = ?, more_exp = ?, more_meso = ?, more_drop = ?, revive = ?, summon_mob = ?, consume_recover = ? WHERE characterid = ? AND `position` = ?");

                    while(rs.next()) {
                        int id = rs.getInt("id");
                        int position = rs.getInt("position");
                        if (equipFieldMap.containsKey(position)) {
                            EquipField equipField = (EquipField)equipFieldMap.get(position);
                            ps2.setInt(1, equipField.getStr());
                            ps2.setInt(2, equipField.getDex());
                            ps2.setInt(3, equipField.getInt());
                            ps2.setInt(4, equipField.getLuk());
                            ps2.setInt(5, equipField.getHp());
                            ps2.setInt(6, equipField.getMp());
                            ps2.setInt(7, equipField.getWatk());
                            ps2.setInt(8, equipField.getMatk());
                            ps2.setInt(9, equipField.getWdef());
                            ps2.setInt(10, equipField.getMdef());
                            ps2.setInt(11, equipField.getAcc());
                            ps2.setInt(12, equipField.getAvoid());
                            ps2.setInt(13, equipField.getSpeed());
                            ps2.setInt(14, equipField.getJump());
                            ps2.setInt(15, equipField.getStr_p());
                            ps2.setInt(16, equipField.getDex_p());
                            ps2.setInt(17, equipField.getInt_p());
                            ps2.setInt(18, equipField.getLuk_p());
                            ps2.setInt(19, equipField.getHp_p());
                            ps2.setInt(20, equipField.getMp_p());
                            ps2.setInt(21, equipField.getWatk_p());
                            ps2.setInt(22, equipField.getMatk_p());
                            ps2.setInt(23, equipField.getWdef_p());
                            ps2.setInt(24, equipField.getMdef_p());
                            ps2.setInt(25, equipField.getAcc_p());
                            ps2.setInt(26, equipField.getAvoid_p());
                            ps2.setInt(27, equipField.getTotalDamage());
                            ps2.setInt(28, equipField.getBossDamage());
                            ps2.setInt(29, equipField.getNormalDamage());
                            ps2.setInt(30, equipField.getMustKill());
                            ps2.setInt(31, equipField.getInvincible());
                            ps2.setInt(32, equipField.getStrong());
                            ps2.setInt(33, equipField.getSuckHp());
                            ps2.setInt(34, equipField.getSuckMp());
                            ps2.setInt(35, equipField.getGrowableHp());
                            ps2.setInt(36, equipField.getGrowableMp());
                            ps2.setInt(37, equipField.getMoreExp());
                            ps2.setInt(38, equipField.getMoreMeso());
                            ps2.setInt(39, equipField.getMoreDrop());
                            ps2.setInt(40, equipField.getRevive());
                            ps2.setInt(41, equipField.getSummonMob());
                            ps2.setInt(42, equipField.getConsumeRecover());
                            ps2.setInt(43, chrId);
                            ps2.setInt(44, position);
                            ps2.addBatch();
                            ++count;
                            if (count % batchSize == 0) {
                                ps2.executeBatch();
                                ps2.clearBatch();
                            }

                            equipFieldMap.remove(position);
                        } else {
                            PreparedStatement ps3 = con.prepareStatement("DELETE FROM snail_equipment_field_enhancement WHERE characterid = ? AND `position` = ?");
                            ps3.setInt(1, chrId);
                            ps3.setInt(2, position);
                            ps3.execute();
                            ps3.close();
                        }
                    }

                    if (count % batchSize != 0) {
                        ps2.executeBatch();
                        ps2.clearBatch();
                    }

                    count = 0;
                    ps2 = con.prepareStatement("INSERT INTO snail_equipment_field_enhancement (characterid, `position`, str, dex, `int`, luk, hp, mp, watk, matk, wdef, mdef, acc, avoid, speed, jump, str_p, dex_p, int_p, luk_p, hp_p, mp_p, watk_p, matk_p, wdef_p, mdef_p, acc_p, avoid_p, totalDamage, bossDamage, normalDamage, must_kill, invincible, strong, suck_hp, suck_mp, growable_hp, growable_mp, more_exp, more_meso, more_drop, revive, summon_mob, consume_recover) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
                    Iterator var15 = equipFieldMap.entrySet().iterator();

                    while(var15.hasNext()) {
                        Map.Entry<Integer, EquipField> entry = (Map.Entry)var15.next();
                        int position = (Integer)entry.getKey();
                        EquipField equipField = (EquipField)entry.getValue();
                        ps2.setInt(1, chrId);
                        ps2.setInt(2, position);
                        ps2.setInt(3, equipField.getStr());
                        ps2.setInt(4, equipField.getDex());
                        ps2.setInt(5, equipField.getInt());
                        ps2.setInt(6, equipField.getLuk());
                        ps2.setInt(7, equipField.getHp());
                        ps2.setInt(8, equipField.getMp());
                        ps2.setInt(9, equipField.getWatk());
                        ps2.setInt(10, equipField.getMatk());
                        ps2.setInt(11, equipField.getWdef());
                        ps2.setInt(12, equipField.getMdef());
                        ps2.setInt(13, equipField.getAcc());
                        ps2.setInt(14, equipField.getAvoid());
                        ps2.setInt(15, equipField.getSpeed());
                        ps2.setInt(16, equipField.getJump());
                        ps2.setInt(17, equipField.getStr_p());
                        ps2.setInt(18, equipField.getDex_p());
                        ps2.setInt(19, equipField.getInt_p());
                        ps2.setInt(20, equipField.getLuk_p());
                        ps2.setInt(21, equipField.getHp_p());
                        ps2.setInt(22, equipField.getMp_p());
                        ps2.setInt(23, equipField.getWatk_p());
                        ps2.setInt(24, equipField.getMatk_p());
                        ps2.setInt(25, equipField.getWdef_p());
                        ps2.setInt(26, equipField.getMdef_p());
                        ps2.setInt(27, equipField.getAcc_p());
                        ps2.setInt(28, equipField.getAvoid_p());
                        ps2.setInt(29, equipField.getTotalDamage());
                        ps2.setInt(30, equipField.getBossDamage());
                        ps2.setInt(31, equipField.getNormalDamage());
                        ps2.setInt(32, equipField.getMustKill());
                        ps2.setInt(33, equipField.getInvincible());
                        ps2.setInt(34, equipField.getStrong());
                        ps2.setInt(35, equipField.getSuckHp());
                        ps2.setInt(36, equipField.getSuckMp());
                        ps2.setInt(37, equipField.getGrowableHp());
                        ps2.setInt(38, equipField.getGrowableMp());
                        ps2.setInt(39, equipField.getMoreExp());
                        ps2.setInt(40, equipField.getMoreMeso());
                        ps2.setInt(41, equipField.getMoreDrop());
                        ps2.setInt(42, equipField.getRevive());
                        ps2.setInt(43, equipField.getSummonMob());
                        ps2.setInt(44, equipField.getConsumeRecover());
                        ps2.addBatch();
                        ++count;
                        if (count % batchSize == 0) {
                            ps2.executeBatch();
                            ps2.clearBatch();
                        }
                    }

                    if (count % batchSize != 0) {
                        ps2.executeBatch();
                        ps2.clearBatch();
                    }

                    ps2.close();
                    if (isConNull) {
                        con.setAutoCommit(true);
                        con.setTransactionIsolation(4);
                        con.close();
                    }
                } catch (SQLException var14) {
                    服务端输出信息.println_err("【错误】EquipFieldEnhancement.saveChrToDB错误，原因：" + var14);
                    var14.printStackTrace();
                }
            }

            return true;
        }
    }

    public boolean loadCharFromDB(int chrId) {
        return this.loadCharFromDB(chrId, DBConPool.getConnection());
    }

    public boolean loadCharFromDB(int chrId, Connection con) {
        try {
            if (con == null || con.isClosed()) {
                con = DBConPool.getConnection();
            }

            PreparedStatement ps = con.prepareStatement("SELECT * FROM snail_equipment_field_enhancement WHERE characterid = ?");
            ps.setInt(1, chrId);
            ResultSet rs = ps.executeQuery();
            Map<Integer, EquipField> equipFieldMap = new HashMap();
            if (this.chrEquipFieldMap.containsKey(chrId)) {
                ((Map)this.chrEquipFieldMap.get(chrId)).clear();
            }

            while(rs.next()) {
                equipFieldMap.put(rs.getInt("position"), new EquipField(rs.getInt("position"), rs.getShort("str"), rs.getShort("dex"), rs.getShort("int"), rs.getShort("luk"), rs.getShort("hp"), rs.getShort("mp"), rs.getShort("watk"), rs.getShort("matk"), rs.getShort("wdef"), rs.getShort("mdef"), rs.getShort("acc"), rs.getShort("avoid"), rs.getShort("speed"), rs.getShort("jump"), rs.getInt("str_p"), rs.getInt("dex_p"), rs.getInt("int_p"), rs.getInt("luk_p"), rs.getInt("hp_p"), rs.getInt("mp_p"), rs.getInt("watk_p"), rs.getInt("matk_p"), rs.getInt("wdef_p"), rs.getInt("mdef_p"), rs.getInt("acc_p"), rs.getInt("avoid_p"), rs.getInt("totalDamage"), rs.getInt("bossDamage"), rs.getInt("normalDamage"), rs.getInt("must_kill"), rs.getInt("invincible"), rs.getInt("strong"), rs.getInt("suck_hp"), rs.getInt("suck_mp"), rs.getInt("growable_hp"), rs.getInt("growable_mp"), rs.getInt("more_exp"), rs.getInt("more_meso"), rs.getInt("more_drop"), rs.getInt("revive"), rs.getInt("summon_mob"), rs.getInt("consume_recover")));
            }

            this.chrEquipFieldMap.put(chrId, equipFieldMap);
            rs.close();
            ps.close();
            return true;
        } catch (SQLException var6) {
            服务端输出信息.println_err("【错误】EquipFieldEnhancement.loadCharFromDB错误，原因：" + var6);
            var6.printStackTrace();
            return false;
        }
    }

    public class EquipField {
        private short str;
        private short dex;
        private short _int;
        private short luk;
        private short hp;
        private short mp;
        private short watk;
        private short matk;
        private short wdef;
        private short mdef;
        private short acc;
        private short avoid;
        private short speed;
        private short jump;
        private int str_p;
        private int dex_p;
        private int _int_p;
        private int luk_p;
        private int hp_p;
        private int mp_p;
        private int watk_p;
        private int matk_p;
        private int wdef_p;
        private int mdef_p;
        private int acc_p;
        private int avoid_p;
        private int totalDamage;
        private int bossDamage;
        private int normalDamage;
        private int mustKill;
        private int invincible;
        private int strong;
        private int suckHp;
        private int suckMp;
        private int growableHp;
        private int growableMp;
        private int moreExp;
        private int moreMeso;
        private int moreDrop;
        private int revive;
        private int summonMob;
        private int consumeRecover;
        private int position;

        public EquipField(int position, short str, short dex, short _int, short luk, short hp, short mp, short watk, short matk, short wdef, short mdef, short acc, short avoid, short speed, short jump, int str_p, int dex_p, int _int_p, int luk_p, int hp_p, int mp_p, int watk_p, int matk_p, int wdef_p, int mdef_p, int acc_p, int avoid_p, int totalDamage, int bossDamage, int normalDamage, int mustKill, int invincible, int strong, int suckHp, int suckMp, int growableHp, int growableMp, int moreExp, int moreMeso, int moreDrop, int revive, int summonMob, int consumeRecover) {
            this.str = str;
            this.dex = dex;
            this._int = _int;
            this.luk = luk;
            this.hp = hp;
            this.mp = mp;
            this.watk = watk;
            this.matk = matk;
            this.wdef = wdef;
            this.mdef = mdef;
            this.acc = acc;
            this.avoid = avoid;
            this.speed = speed;
            this.jump = jump;
            this.str_p = str_p;
            this.dex_p = dex_p;
            this._int_p = _int_p;
            this.luk_p = luk_p;
            this.hp_p = hp_p;
            this.mp_p = mp_p;
            this.watk_p = watk_p;
            this.matk_p = matk_p;
            this.wdef_p = wdef_p;
            this.mdef_p = mdef_p;
            this.acc_p = acc_p;
            this.avoid_p = avoid_p;
            this.totalDamage = totalDamage;
            this.bossDamage = bossDamage;
            this.normalDamage = normalDamage;
            this.position = position;
            this.mustKill = mustKill;
            this.invincible = invincible;
            this.strong = strong;
            this.suckHp = suckHp;
            this.suckMp = suckMp;
            this.growableHp = growableHp;
            this.growableMp = growableMp;
            this.moreExp = moreExp;
            this.moreMeso = moreMeso;
            this.moreDrop = moreDrop;
            this.revive = revive;
            this.summonMob = summonMob;
            this.consumeRecover = consumeRecover;
        }

        public EquipField(int position) {
            this.position = position;
        }

        public int getPosition() {
            return this.position;
        }

        public void setPosition(int position) {
            this.position = position;
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

        public short getInt() {
            return this._int;
        }

        public void setInt(short _int) {
            this._int = _int;
        }

        public short getLuk() {
            return this.luk;
        }

        public void setLuk(short luk) {
            this.luk = luk;
        }

        public short getHp() {
            return this.hp;
        }

        public void setHp(short hp) {
            this.hp = hp;
        }

        public short getMp() {
            return this.mp;
        }

        public void setMp(short mp) {
            this.mp = mp;
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

        public int getStr_p() {
            return this.str_p;
        }

        public void setStr_p(int str_p) {
            this.str_p = str_p;
        }

        public int getDex_p() {
            return this.dex_p;
        }

        public void setDex_p(int dex_p) {
            this.dex_p = dex_p;
        }

        public int getInt_p() {
            return this._int_p;
        }

        public void setInt_p(int _int_p) {
            this._int_p = _int_p;
        }

        public int getLuk_p() {
            return this.luk_p;
        }

        public void setLuk_p(int luk_p) {
            this.luk_p = luk_p;
        }

        public int getHp_p() {
            return this.hp_p;
        }

        public void setHp_p(int hp_p) {
            this.hp_p = hp_p;
        }

        public int getMp_p() {
            return this.mp_p;
        }

        public void setMp_p(int mp_p) {
            this.mp_p = mp_p;
        }

        public int getWatk_p() {
            return this.watk_p;
        }

        public void setWatk_p(int watk_p) {
            this.watk_p = watk_p;
        }

        public int getMatk_p() {
            return this.matk_p;
        }

        public void setMatk_p(int matk_p) {
            this.matk_p = matk_p;
        }

        public int getWdef_p() {
            return this.wdef_p;
        }

        public void setWdef_p(int wdef_p) {
            this.wdef_p = wdef_p;
        }

        public int getMdef_p() {
            return this.mdef_p;
        }

        public void setMdef_p(int mdef_p) {
            this.mdef_p = mdef_p;
        }

        public int getAcc_p() {
            return this.acc_p;
        }

        public void setAcc_p(int acc_p) {
            this.acc_p = acc_p;
        }

        public int getAvoid_p() {
            return this.avoid_p;
        }

        public void setAvoid_p(int avoid_p) {
            this.avoid_p = avoid_p;
        }

        public int getTotalDamage() {
            return this.totalDamage;
        }

        public void setTotalDamage(int totalDamage) {
            this.totalDamage = totalDamage;
        }

        public int getBossDamage() {
            return this.bossDamage;
        }

        public void setBossDamage(int bossDamage) {
            this.bossDamage = bossDamage;
        }

        public int getNormalDamage() {
            return this.normalDamage;
        }

        public void setNormalDamage(int normalDamage) {
            this.normalDamage = normalDamage;
        }

        public int getMustKill() {
            return this.mustKill;
        }

        public void setMustKill(int mustKill) {
            this.mustKill = mustKill;
        }

        public int getInvincible() {
            return this.invincible;
        }

        public void setInvincible(int invincible) {
            this.invincible = invincible;
        }

        public int getStrong() {
            return this.strong;
        }

        public void setStrong(int strong) {
            this.strong = strong;
        }

        public int getSuckHp() {
            return this.suckHp;
        }

        public void setSuckHp(int suckHp) {
            this.suckHp = suckHp;
        }

        public int getSuckMp() {
            return this.suckMp;
        }

        public void setSuckMp(int suckMp) {
            this.suckMp = suckMp;
        }

        public int getGrowableHp() {
            return this.growableHp;
        }

        public void setGrowableHp(int growableHp) {
            this.growableHp = growableHp;
        }

        public int getGrowableMp() {
            return this.growableMp;
        }

        public void setGrowableMp(int growableMp) {
            this.growableMp = growableMp;
        }

        public int getMoreExp() {
            return this.moreExp;
        }

        public void setMoreExp(int moreExp) {
            this.moreExp = moreExp;
        }

        public int getMoreMeso() {
            return this.moreMeso;
        }

        public void setMoreMeso(int moreMeso) {
            this.moreMeso = moreMeso;
        }

        public int getMoreDrop() {
            return this.moreDrop;
        }

        public void setMoreDrop(int moreDrop) {
            this.moreDrop = moreDrop;
        }

        public int getRevive() {
            return this.revive;
        }

        public void setRevive(int revive) {
            this.revive = revive;
        }

        public int getSummonMob() {
            return this.summonMob;
        }

        public void setSummonMob(int summonMob) {
            this.summonMob = summonMob;
        }

        public int getConsumeRecover() {
            return this.consumeRecover;
        }

        public void setConsumeRecover(int consumeRecover) {
            this.consumeRecover = consumeRecover;
        }
    }
}
