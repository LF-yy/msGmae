//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package server.bean;

import client.ISkill;
import client.MapleCharacter;
import client.SkillEntry;
import constants.SkillConstants;
import database.DBConPool;
import gui.服务端输出信息;
import handling.channel.ChannelServer;
import tools.Pair;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class SkillSkin {
    private static Map<Integer, ArrayList<Pair<Integer, Pair<Integer, Integer>>>> skillList = new HashMap();
    private static ArrayList<Integer> skillId = new ArrayList();
    private static Map<Integer, Map<Integer, Integer>> chrSkillListMap = new HashMap();
    private MapleCharacter chr;
    private Map<Integer, Integer> chrSkillList = new HashMap();

    public SkillSkin(MapleCharacter chr, int skilltype) {
        this.chr = chr;
        if (skillList == null || skillList.isEmpty() || skillId.isEmpty()) {
            loadSkillList();
        }

        if (skillList != null && !skillList.isEmpty() && !skillId.isEmpty()) {
            this.chrSkillList.clear();
            Iterator var3 = skillId.iterator();

            while(var3.hasNext()) {
                int skid = (Integer)var3.next();
                if (SkillConstants.isJobSkill(skid, chr.getJob())) {
                    this.chrSkillList.put(skid, skilltype);
                }
            }
        }

    }

    public void loadChrSkill() {
        if (chrSkillListMap.containsKey(this.chr.getId()) && !((Map)chrSkillListMap.get(this.chr.getId())).isEmpty()) {
            this.chrSkillList = new HashMap((Map)chrSkillListMap.get(this.chr.getId()));
        } else {
            this.loadChrSkillFromDB();
        }

    }

    public void loadChrSkillFromDB() {
        try {
            Connection con = DBConPool.getNewConnection();
            Throwable var2 = null;

            try {
                this.chrSkillList.clear();
                PreparedStatement ps = con.prepareStatement("SELECT * FROM snail_skillskin_chrlist WHERE characterid = ?");
                ps.setInt(1, this.chr.getId());
                ResultSet rs = ps.executeQuery();

                while(rs.next()) {
                    this.chrSkillList.put(rs.getInt("skillid"), rs.getInt("skilltype"));
                }

                if (!skillId.isEmpty()) {
                    Iterator var5 = skillId.iterator();

                    while(var5.hasNext()) {
                        int skId = (Integer)var5.next();
                        if (!this.chrSkillList.containsKey(skId)) {
                            this.chrSkillList.put(skId, 0);
                        }
                    }

                    ArrayList<Integer> keyToDelete = new ArrayList();
                    Iterator var20 = this.chrSkillList.entrySet().iterator();

                    while(var20.hasNext()) {
                        Map.Entry<Integer, Integer> entry = (Map.Entry)var20.next();
                        if (!skillId.contains(entry.getKey())) {
                            ps = con.prepareStatement("DELETE FROM snail_skillskin_chrlist WHERE characterid = ? AND skillid = ?");
                            ps.setInt(1, this.chr.getId());
                            ps.setInt(2, (Integer)entry.getKey());
                            ps.executeUpdate();
                            keyToDelete.add(entry.getKey());
                        }
                    }

                    if (!keyToDelete.isEmpty()) {
                        var20 = keyToDelete.iterator();

                        while(var20.hasNext()) {
                            int i = (Integer)var20.next();
                            this.chrSkillList.remove(i);
                        }
                    }
                }

                chrSkillListMap.put(this.chr.getId(), new HashMap(this.chrSkillList));
                ps.close();
                rs.close();
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
        } catch (SQLException var18) {
            服务端输出信息.println_err("loadChrSkillFromDB 错误，错误原因：" + var18);
            var18.printStackTrace();
        }

    }

    public static int saveChrSkillMapToDB() {
        if (chrSkillListMap.isEmpty()) {
            return 0;
        } else {
            int mount = 0;
            ArrayList<Integer> chrIds = new ArrayList();
            Map<Integer, Short> chrJobMap = new HashMap();

            try {
                Iterator var3 = ChannelServer.getAllInstances().iterator();

                while(var3.hasNext()) {
                    ChannelServer cs = (ChannelServer)var3.next();
                    Iterator var5 = cs.getPlayerStorage().getAllCharactersThreadSafe().iterator();

                    while(var5.hasNext()) {
                        MapleCharacter chr = (MapleCharacter)var5.next();
                        if (chr != null) {
                            chrIds.add(chr.getId());
                            chrJobMap.put(chr.getId(), chr.getJob());
                        }
                    }
                }
            } catch (Exception var22) {
                服务端输出信息.println_err("【错误】saveChrSkillMapToDB出错，错误原因：" + var22);
                var22.printStackTrace();
            }

            Map<Integer, Map<Integer, Integer>> chrSkillListMap0 = new HashMap(chrSkillListMap);

            try {
                for(Iterator var24 = chrSkillListMap0.entrySet().iterator(); var24.hasNext(); ++mount) {
                    Map.Entry<Integer, Map<Integer, Integer>> entry0 = (Map.Entry)var24.next();
                    int chrId = (Integer)entry0.getKey();
                    Map<Integer, Integer> chrSkillList = (Map)entry0.getValue();
                    if (!chrSkillList.isEmpty()) {
                        int batchSize1 = 1000;
                        int count1 = 0;
                        int batchSize2 = 1000;
                        int count2 = 0;
                        Connection con = DBConPool.getNewConnection();
                        PreparedStatement ps = con.prepareStatement("SELECT count(*) FROM snail_skillskin_chrlist WHERE characterid = ? AND skillid = ?");
                        PreparedStatement ps1 = con.prepareStatement("UPDATE snail_skillskin_chrlist SET skilltype = ? WHERE characterid = ? and skillid = ?");
                        PreparedStatement ps2 = con.prepareStatement("INSERT INTO snail_skillskin_chrlist (characterid, skillid, skilltype) VALUES (?, ?, ?)");
                        Iterator var16 = chrSkillList.entrySet().iterator();

                        label75:
                        while(true) {
                            Map.Entry entry;
                            int skillId;
                            do {
                                if (!var16.hasNext()) {
                                    if (count1 % batchSize1 != 0) {
                                        ps1.executeBatch();
                                    }

                                    if (count2 % batchSize2 != 0) {
                                        ps2.executeBatch();
                                    }

                                    ps1.close();
                                    ps2.close();
                                    ps.close();
                                    con.close();
                                    break label75;
                                }

                                entry = (Map.Entry)var16.next();
                                skillId = (Integer)entry.getKey();
                            } while(chrJobMap.containsKey(chrId) && !SkillConstants.isJobSkill(skillId, (Short)chrJobMap.get(chrId)));

                            int skillType = (Integer)entry.getValue();
                            ps.setInt(1, chrId);
                            ps.setInt(2, skillId);
                            ResultSet rs = ps.executeQuery();
                            if (rs.next() && rs.getInt(1) > 0) {
                                ps1.setInt(1, skillType);
                                ps1.setInt(2, chrId);
                                ps1.setInt(3, skillId);
                                ps1.addBatch();
                                ++count1;
                                if (count1 % batchSize1 == 0) {
                                    ps1.executeBatch();
                                    ps1.clearBatch();
                                }
                            } else {
                                ps2.setInt(1, chrId);
                                ps2.setInt(2, skillId);
                                ps2.setInt(3, skillType);
                                ps2.addBatch();
                                ++count2;
                                if (count2 % batchSize2 == 0) {
                                    ps2.executeBatch();
                                    ps2.clearBatch();
                                }
                            }

                            rs.close();
                        }
                    }

                    if (!chrIds.contains(chrId)) {
                        chrSkillListMap.remove(chrId);
                    }
                }
            } catch (SQLException var21) {
                服务端输出信息.println_err("saveChrSkillMapToDB 错误，错误原因：" + var21);
                var21.printStackTrace();
            }

            chrIds.clear();
            chrJobMap.clear();
            chrSkillListMap0.clear();
            return mount;
        }
    }

    public void saveChrSkill() {
        if (!this.chrSkillList.isEmpty()) {
            chrSkillListMap.put(this.chr.getId(), new HashMap(this.chrSkillList));
        }

    }

    public void saveChrSkillToDB() {
        if (!this.chrSkillList.isEmpty()) {
            try {
                Connection con = DBConPool.getNewConnection();
                Throwable var2 = null;

                try {
                    Iterator var3 = this.chrSkillList.entrySet().iterator();

                    while(var3.hasNext()) {
                        Map.Entry<Integer, Integer> entry = (Map.Entry)var3.next();
                        int skillId = (Integer)entry.getKey();
                        int skillType = (Integer)entry.getValue();
                        PreparedStatement ps = con.prepareStatement("SELECT * FROM snail_skillskin_chrlist WHERE characterid = ? AND skillid = ?");
                        ps.setInt(1, this.chr.getId());
                        ps.setInt(2, skillId);
                        ResultSet rs = ps.executeQuery();
                        if (rs.next()) {
                            ps = con.prepareStatement("UPDATE snail_skillskin_chrlist SET skilltype = ? WHERE characterid = ? and skillid = ?");
                            ps.setInt(1, skillType);
                            ps.setInt(2, this.chr.getId());
                            ps.setInt(3, skillId);
                            ps.executeUpdate();
                        } else {
                            ps = con.prepareStatement("INSERT INTO snail_skillskin_chrlist (characterid, skillid, skilltype) VALUES (?, ?, ?)");
                            ps.setInt(1, this.chr.getId());
                            ps.setInt(2, skillId);
                            ps.setInt(3, skillType);
                            ps.executeUpdate();
                        }

                        ps.close();
                        rs.close();
                    }
                } catch (Throwable var17) {
                    var2 = var17;
                    throw var17;
                } finally {
                    if (con != null) {
                        if (var2 != null) {
                            try {
                                con.close();
                            } catch (Throwable var16) {
                                var2.addSuppressed(var16);
                            }
                        } else {
                            con.close();
                        }
                    }

                }
            } catch (SQLException var19) {
                服务端输出信息.println_err("saveChrSkillToDB 错误，错误原因：" + var19);
                var19.printStackTrace();
            }

        }
    }

    public Pair<Integer, Integer> getChrSkillEff(int skillId) {
        return !this.chrSkillList.containsKey(skillId) ? null : getEffSkill(skillId, (Integer)this.chrSkillList.get(skillId));
    }

    public Map<Integer, Integer> getChrSkillList() {
        return this.chrSkillList.isEmpty() ? null : this.chrSkillList;
    }

    public Map<Integer, Integer> getChrSkillListForJob() {
        if (this.chrSkillList.isEmpty()) {
            return null;
        } else {
            Map<Integer, Integer> chrSkillListForJob = new HashMap();
            Map<ISkill, SkillEntry> chrSkills = this.chr.getSkills();
            Iterator var3 = chrSkills.entrySet().iterator();

            while(var3.hasNext()) {
                Map.Entry<ISkill, SkillEntry> entry = (Map.Entry)var3.next();
                if (SkillConstants.isJobSkill(((ISkill)entry.getKey()).getId(), this.chr.getJob()) && this.chrSkillList.containsKey(((ISkill)entry.getKey()).getId())) {
                    chrSkillListForJob.put(((ISkill)entry.getKey()).getId(), this.chrSkillList.get(((ISkill)entry.getKey()).getId()));
                }
            }

            if (!chrSkillListForJob.isEmpty()) {
                return chrSkillListForJob;
            } else {
                return null;
            }
        }
    }

    public Pair<Integer, Integer> getChrSkillEffS(int skillId) {
        if (!this.chrSkillList.containsKey(skillId)) {
            return null;
        } else {
            int skillType = (Integer)this.chrSkillList.get(skillId);
            switch (skillId) {
                case 1211004:
                case 1211006:
                case 1211008:
                case 1221004:
                    return getEffSkill(skillId, skillType + 100);
                default:
                    return getEffSkill(skillId, skillType);
            }
        }
    }

    public int getChrSkillType(int skillId) {
        return !this.chrSkillList.containsKey(skillId) ? 0 : (Integer)this.chrSkillList.get(skillId);
    }

    public boolean setChrSkillType(int skillId, int skillType) {
        if (!this.chrSkillList.containsKey(skillId)) {
            if (SkillSkin.skillId.contains(skillId)) {
                if (this.containsType(skillId, skillType)) {
                    this.chrSkillList.put(skillId, skillType);
                    return true;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        } else {
            this.chrSkillList.replace(skillId, skillType);
            return true;
        }
    }

    public void setChrSkillTypeAll(int skillType) {
        Iterator var2 = skillId.iterator();

        while(var2.hasNext()) {
            int skId = (Integer)var2.next();
            if (this.chrSkillList.containsKey(skId) && this.containsType(skId, skillType)) {
                this.chrSkillList.replace(skId, skillType);
            }
        }

    }

    public boolean containsType(int skillId, int skillType) {
        if (skillList.containsKey(skillId)) {
            ArrayList<Pair<Integer, Pair<Integer, Integer>>> effectList = (ArrayList)skillList.get(skillId);
            if (!effectList.isEmpty()) {
                if (skillType == 0) {
                    return true;
                }

                Iterator var4 = effectList.iterator();

                while(var4.hasNext()) {
                    Pair<Integer, Pair<Integer, Integer>> effPair = (Pair)var4.next();
                    if ((Integer)effPair.left == skillType) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public boolean haveSkill(int skillId) {
        return SkillSkin.skillId.contains(skillId);
    }

    public static void loadSkillList() {
        skillList.clear();
        skillId.clear();

        try {
            Connection con = DBConPool.getConnection();
            Throwable var1 = null;

            try {
                PreparedStatement ps = con.prepareStatement("SELECT * FROM snail_skillskin_list");
                ResultSet rs = ps.executeQuery();

                while(rs.next()) {
                    int id = rs.getInt("skillid");
                    if (!skillId.contains(id)) {
                        skillId.add(id);
                    }
                }

                if (!skillId.isEmpty()) {
                    Iterator var18 = skillId.iterator();

                    while(var18.hasNext()) {
                        int skId = (Integer)var18.next();
                        ArrayList<Pair<Integer, Pair<Integer, Integer>>> effskillList = new ArrayList();
                        ps = con.prepareStatement("SELECT * FROM snail_skillskin_list WHERE skillid = ? ORDER BY skilltype");
                        ps.setInt(1, skId);
                        rs = ps.executeQuery();

                        while(rs.next()) {
                            effskillList.add(new Pair(rs.getInt("skilltype"), new Pair(rs.getInt("effskillid"), rs.getInt("efftype"))));
                        }

                        skillList.put(skId, effskillList);
                    }
                }

                ps.close();
                rs.close();
            } catch (Throwable var15) {
                var1 = var15;
                throw var15;
            } finally {
                if (con != null) {
                    if (var1 != null) {
                        try {
                            con.close();
                        } catch (Throwable var14) {
                            var1.addSuppressed(var14);
                        }
                    } else {
                        con.close();
                    }
                }

            }
        } catch (SQLException var17) {
            服务端输出信息.println_err("loadSkillList 错误，错误原因：" + var17);
            var17.printStackTrace();
        }

    }

    public static Map<Integer, ArrayList<Pair<Integer, Pair<Integer, Integer>>>> getSkillList() {
        return skillList;
    }

    public static ArrayList<Pair<Integer, Pair<Integer, Integer>>> getEffSkills(int skillId) {
        return skillList != null && !skillList.isEmpty() ? (ArrayList)skillList.get(skillId) : null;
    }

    public static Pair<Integer, Integer> getEffSkill(int skillId, int skillType) {
        return findEffSkillByType(skillId, skillType);
    }

    private static Pair<Integer, Integer> findEffSkillByType(int skillId, int skillType) {
        if (skillList != null && !skillList.isEmpty()) {
            ArrayList<Pair<Integer, Pair<Integer, Integer>>> skillInfo = (ArrayList)skillList.get(skillId);
            if (skillInfo == null) {
                return null;
            } else {
                Iterator var3 = skillInfo.iterator();

                Pair p1;
                do {
                    if (!var3.hasNext()) {
                        return null;
                    }

                    p1 = (Pair)var3.next();
                } while((Integer)p1.left != skillType);

                return (Pair)p1.right;
            }
        } else {
            return null;
        }
    }
}
