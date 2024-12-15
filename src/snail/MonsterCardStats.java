//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package snail;

import database.DBConPool;
import gui.服务端输出信息;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class MonsterCardStats {
    private static MonsterCardStats instance = new MonsterCardStats();
    private Map<Integer, MonsterCard> monsterCardMap = new HashMap();

    public MonsterCardStats() {
    }

    public static MonsterCardStats getInstance() {
        return instance;
    }

    public boolean loadMonsterCardStatFromDB() {
        服务端输出信息.println_out("【怪物卡片附加属性】开始加载...");
        Map<Integer, MonsterCard> monsterCardMap1 = new HashMap();

        try {
            Connection con = DBConPool.getConnection();
            Throwable var3 = null;

            try {
                PreparedStatement ps = con.prepareStatement("SELECT * FROM snail_monster_card_stats");
                ResultSet rs = ps.executeQuery();

                while(rs.next()) {
                    monsterCardMap1.put(rs.getInt("itemid"), new MonsterCard(rs.getInt("itemid"), rs.getInt("str"), rs.getInt("dex"), rs.getInt("int"), rs.getInt("luk"), rs.getInt("hp"), rs.getInt("mp")));
                }

                ps.close();
                rs.close();
                this.monsterCardMap.clear();
                this.monsterCardMap = monsterCardMap1;
                服务端输出信息.println_out("【怪物卡片附加属性】加载完毕！");
                boolean var6 = true;
                return var6;
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
            服务端输出信息.println_err("【错误】loadMonsterCardStatFromDB执行错误，原因：" + var18);
            var18.printStackTrace();
            return false;
        }
    }

    public MonsterCard getMonsterCard(int itemId) {
        return (MonsterCard)this.monsterCardMap.get(itemId);
    }

    public class MonsterCard {
        private int itemId;
        private int str;
        private int dex;
        private int _int;
        private int luk;
        private int hp;
        private int mp;

        public MonsterCard(int itemId) {
            this.itemId = itemId;
        }

        public MonsterCard(int itemId, int str, int dex, int _int, int luk, int hp, int mp) {
            this.itemId = itemId;
            this.str = str;
            this.dex = dex;
            this._int = _int;
            this.luk = luk;
            this.hp = hp;
            this.mp = mp;
        }

        public int getItemId() {
            return this.itemId;
        }

        public void setItemId(int itemId) {
            this.itemId = itemId;
        }

        public int getStr() {
            return this.str;
        }

        public void setStr(int str) {
            this.str = str;
        }

        public int getDex() {
            return this.dex;
        }

        public void setDex(int dex) {
            this.dex = dex;
        }

        public int getInt() {
            return this._int;
        }

        public void setInt(int _int) {
            this._int = _int;
        }

        public int getLuk() {
            return this.luk;
        }

        public void setLuk(int luk) {
            this.luk = luk;
        }

        public int getHp() {
            return this.hp;
        }

        public void setHp(int hp) {
            this.hp = hp;
        }

        public int getMp() {
            return this.mp;
        }

        public void setMp(int mp) {
            this.mp = mp;
        }
    }
}
