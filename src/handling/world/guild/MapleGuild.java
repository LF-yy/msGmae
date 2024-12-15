//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package handling.world.guild;

import client.MapleCharacter;
import client.MapleCharacterUtil;
import client.MapleClient;
import database.DBConPool;
import gui.LtMS;
import gui.服务端输出信息;
import handling.world.World.Alliance;
import handling.world.World.Broadcast;
import handling.world.World.Guild;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import server.Start;
import snail.TimeLogCenter;
import tools.FileoutputUtil;
import tools.MaplePacketCreator;
import tools.data.MaplePacketLittleEndianWriter;
import tools.packet.UIPacket;

public class MapleGuild implements Serializable {
    public static final long serialVersionUID = 6322150443228168192L;
    private final List<MapleGuildCharacter> members = new CopyOnWriteArrayList();
    private final String[] rankTitles = new String[5];
    private String name;
    private String notice;
    private int id;
    private int gp;
    private int logo;
    private int logoColor;
    private int leader;
    private int capacity;
    private int logoBG;
    private int logoBGColor;
    private int signature;
    private boolean bDirty = true;
    private boolean proper = true;
    private int allianceid = 0;
    private int invitedid = 0;
    private final Map<Integer, MapleBBSThread> bbs = new HashMap();
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final Lock rL;
    private final Lock wL;
    private boolean init;
    private ArrayList<GuildSkill> guildSkills;

    public MapleGuild(int guildid) {
        this.rL = this.lock.readLock();
        this.wL = this.lock.writeLock();
        this.init = false;
        this.guildSkills = new ArrayList();

        try {
            Connection con = DBConPool.getInstance().getDataSource().getConnection();
            Throwable var3 = null;

            try {
                PreparedStatement ps = con.prepareStatement("SELECT * FROM guilds WHERE guildid = ?");
                ps.setInt(1, guildid);
                ResultSet rs = ps.executeQuery();
                if (!rs.first()) {
                    rs.close();
                    ps.close();
                    this.id = -1;
                    return;
                }

                this.id = guildid;
                this.name = rs.getString("name");
                this.gp = rs.getInt("GP");
                this.logo = rs.getInt("logo");
                this.logoColor = rs.getInt("logoColor");
                this.logoBG = rs.getInt("logoBG");
                this.logoBGColor = rs.getInt("logoBGColor");
                this.capacity = rs.getInt("capacity");
                this.rankTitles[0] = rs.getString("rank1title");
                this.rankTitles[1] = rs.getString("rank2title");
                this.rankTitles[2] = rs.getString("rank3title");
                this.rankTitles[3] = rs.getString("rank4title");
                this.rankTitles[4] = rs.getString("rank5title");
                this.leader = rs.getInt("leader");
                this.notice = rs.getString("notice");
                this.signature = rs.getInt("signature");
                this.allianceid = rs.getInt("alliance");
                rs.close();
                ps.close();
                ps = con.prepareStatement("SELECT id, name, level, job, guildrank, alliancerank FROM characters WHERE guildid = ? ORDER BY guildrank ASC, name ASC");
                ps.setInt(1, guildid);
                rs = ps.executeQuery();
                if (rs.first()) {
                    boolean leaderCheck = false;

                    do {
                        if (rs.getInt("id") == this.leader) {
                            leaderCheck = true;
                        }

                        this.members.add(new MapleGuildCharacter(rs.getInt("id"), rs.getShort("level"), rs.getString("name"), (byte)-1, rs.getInt("job"), rs.getByte("guildrank"), rs.getByte("alliancerank"), guildid, false));
                    } while(rs.next());

                    rs.close();
                    ps.close();
                    if (!leaderCheck) {
                        服务端输出信息.println_err("会长[" + this.leader + "]没有在公会ID为" + this.id + "的公会中，系统自动解散这个公会。");
                        this.writeToDB(true);
                        this.proper = false;
                        return;
                    }

                    ps = con.prepareStatement("SELECT * FROM bbs_threads WHERE guildid = ? ORDER BY localthreadid DESC");
                    ps.setInt(1, guildid);

                    MapleBBSThread thread;
                    for(rs = ps.executeQuery(); rs.next(); this.bbs.put(rs.getInt("localthreadid"), thread)) {
                        thread = new MapleBBSThread(rs.getInt("localthreadid"), rs.getString("name"), rs.getString("startpost"), rs.getLong("timestamp"), guildid, rs.getInt("postercid"), rs.getInt("icon"));
                        PreparedStatement pse = con.prepareStatement("SELECT * FROM bbs_replies WHERE threadid = ?");
                        Throwable var9 = null;

                        try {
                            pse.setInt(1, rs.getInt("threadid"));
                            ResultSet rse = pse.executeQuery();

                            while(rse.next()) {
                                thread.replies.put(thread.replies.size(), new MapleBBSThread.MapleBBSReply(thread.replies.size(), rse.getInt("postercid"), rse.getString("content"), rse.getLong("timestamp")));
                            }

                            rse.close();
                        } catch (Throwable var37) {
                            var9 = var37;
                            throw var37;
                        } finally {
                            if (pse != null) {
                                if (var9 != null) {
                                    try {
                                        pse.close();
                                    } catch (Throwable var36) {
                                        var9.addSuppressed(var36);
                                    }
                                } else {
                                    pse.close();
                                }
                            }

                        }
                    }

                    rs.close();
                    ps.close();
                    this.loadGuildSkillsFromDB();
                    return;
                }

                服务端输出信息.println_err("公会ID：" + this.id + " 没有成员，系统自动解散公会。");
                rs.close();
                ps.close();
                this.writeToDB(true);
                this.proper = false;
            } catch (Throwable var39) {
                var3 = var39;
                throw var39;
            } finally {
                if (con != null) {
                    if (var3 != null) {
                        try {
                            con.close();
                        } catch (Throwable var35) {
                            var3.addSuppressed(var35);
                        }
                    } else {
                        con.close();
                    }
                }

            }

        } catch (SQLException var41) {
            服务端输出信息.println_err("unable to read guild information from sql");
            FileoutputUtil.outError("logs/资料库异常.txt", var41);
        }
    }

    public boolean isProper() {
        return this.proper;
    }

    public static final Collection<MapleGuild> loadAll() {
        Collection<MapleGuild> ret = new ArrayList();

        try {
            Connection con = DBConPool.getInstance().getDataSource().getConnection();
            Throwable var3 = null;

            try {
                PreparedStatement ps = con.prepareStatement("SELECT guildid FROM guilds");
                Throwable var5 = null;

                try {
                    ResultSet rs = ps.executeQuery();
                    Throwable var7 = null;

                    try {
                        while(rs.next()) {
                            MapleGuild g = new MapleGuild(rs.getInt("guildid"));
                            if (g.getId() > 0) {
                                ret.add(g);
                            }
                        }
                    } catch (Throwable var54) {
                        var7 = var54;
                        throw var54;
                    } finally {
                        if (rs != null) {
                            if (var7 != null) {
                                try {
                                    rs.close();
                                } catch (Throwable var53) {
                                    var7.addSuppressed(var53);
                                }
                            } else {
                                rs.close();
                            }
                        }

                    }
                } catch (Throwable var56) {
                    var5 = var56;
                    throw var56;
                } finally {
                    if (ps != null) {
                        if (var5 != null) {
                            try {
                                ps.close();
                            } catch (Throwable var52) {
                                var5.addSuppressed(var52);
                            }
                        } else {
                            ps.close();
                        }
                    }

                }
            } catch (Throwable var58) {
                var3 = var58;
                throw var58;
            } finally {
                if (con != null) {
                    if (var3 != null) {
                        try {
                            con.close();
                        } catch (Throwable var51) {
                            var3.addSuppressed(var51);
                        }
                    } else {
                        con.close();
                    }
                }

            }
        } catch (SQLException var60) {
            服务端输出信息.println_err("unable to read guild information from sql");
            FileoutputUtil.outError("logs/资料库异常.txt", var60);
        }

        return ret;
    }

    public final int getId() {
        return this.id;
    }

    public final void writeToDB(boolean bDisband) {
        try {
            Connection con = DBConPool.getInstance().getDataSource().getConnection();
            Throwable var3 = null;

            try {
                if (!bDisband) {
                    StringBuilder buf = new StringBuilder("UPDATE guilds SET GP = ?, logo = ?, logoColor = ?, logoBG = ?, logoBGColor = ?, ");

                    for(int i = 1; i < 6; ++i) {
                        buf.append("rank").append(i).append("title = ?, ");
                    }

                    buf.append("capacity = ?, notice = ?, alliance = ?, leader = ? WHERE guildid = ?");
                    PreparedStatement ps = con.prepareStatement(buf.toString());
                    ps.setInt(1, this.gp);
                    ps.setInt(2, this.logo);
                    ps.setInt(3, this.logoColor);
                    ps.setInt(4, this.logoBG);
                    ps.setInt(5, this.logoBGColor);
                    ps.setString(6, this.rankTitles[0]);
                    ps.setString(7, this.rankTitles[1]);
                    ps.setString(8, this.rankTitles[2]);
                    ps.setString(9, this.rankTitles[3]);
                    ps.setString(10, this.rankTitles[4]);
                    ps.setInt(11, this.capacity);
                    ps.setString(12, this.notice);
                    ps.setInt(13, this.allianceid);
                    ps.setInt(14, this.leader);
                    ps.setInt(15, this.id);
                    ps.execute();
                    ps.close();
                    ps = con.prepareStatement("DELETE FROM bbs_threads WHERE guildid = ?");
                    ps.setInt(1, this.id);
                    ps.execute();
                    ps.close();
                    ps = con.prepareStatement("DELETE FROM bbs_replies WHERE guildid = ?");
                    ps.setInt(1, this.id);
                    ps.execute();
                    ps.close();
                    ps = con.prepareStatement("INSERT INTO bbs_threads(`postercid`, `name`, `timestamp`, `icon`, `startpost`, `guildid`, `localthreadid`) VALUES(?, ?, ?, ?, ?, ?, ?)", 1);
                    ps.setInt(6, this.id);
                    Iterator var6 = this.bbs.values().iterator();

                    while(var6.hasNext()) {
                        MapleBBSThread bb = (MapleBBSThread)var6.next();
                        ps.setInt(1, bb.ownerID);
                        ps.setString(2, bb.name);
                        ps.setLong(3, bb.timestamp);
                        ps.setInt(4, bb.icon);
                        ps.setString(5, bb.text);
                        ps.setInt(7, bb.localthreadID);
                        ps.executeUpdate();
                        ResultSet rs = ps.getGeneratedKeys();
                        Throwable var9 = null;

                        try {
                            if (!rs.next()) {
                                rs.close();
                            } else {
                                PreparedStatement pse = con.prepareStatement("INSERT INTO bbs_replies (`threadid`, `postercid`, `timestamp`, `content`, `guildid`) VALUES (?, ?, ?, ?, ?)");
                                Throwable var11 = null;

                                try {
                                    pse.setInt(5, this.id);
                                    Iterator var12 = bb.replies.values().iterator();

                                    while(var12.hasNext()) {
                                        MapleBBSThread.MapleBBSReply r = (MapleBBSThread.MapleBBSReply)var12.next();
                                        pse.setInt(1, rs.getInt(1));
                                        pse.setInt(2, r.ownerID);
                                        pse.setLong(3, r.timestamp);
                                        pse.setString(4, r.content);
                                        pse.execute();
                                    }
                                } catch (Throwable var61) {
                                    var11 = var61;
                                    throw var61;
                                } finally {
                                    if (pse != null) {
                                        if (var11 != null) {
                                            try {
                                                pse.close();
                                            } catch (Throwable var60) {
                                                var11.addSuppressed(var60);
                                            }
                                        } else {
                                            pse.close();
                                        }
                                    }

                                }
                            }
                        } catch (Throwable var63) {
                            var9 = var63;
                            throw var63;
                        } finally {
                            if (rs != null) {
                                if (var9 != null) {
                                    try {
                                        rs.close();
                                    } catch (Throwable var59) {
                                        var9.addSuppressed(var59);
                                    }
                                } else {
                                    rs.close();
                                }
                            }

                        }
                    }

                    ps.close();
                    this.saveGuildSkillsToDB();
                } else {
                    PreparedStatement ps = con.prepareStatement("UPDATE characters SET guildid = 0, guildrank = 5, alliancerank = 5 WHERE guildid = ?");
                    ps.setInt(1, this.id);
                    ps.execute();
                    ps.close();
                    ps = con.prepareStatement("DELETE FROM bbs_threads WHERE guildid = ?");
                    ps.setInt(1, this.id);
                    ps.execute();
                    ps.close();
                    ps = con.prepareStatement("DELETE FROM bbs_replies WHERE guildid = ?");
                    ps.setInt(1, this.id);
                    ps.execute();
                    ps.close();
                    ps = con.prepareStatement("DELETE FROM guilds WHERE guildid = ?");
                    ps.setInt(1, this.id);
                    ps.execute();
                    ps.close();
                    this.saveGuildSkillsToDB();
                    if (this.allianceid > 0) {
                        MapleGuildAlliance alliance = Alliance.getAlliance(this.allianceid);
                        if (alliance != null) {
                            alliance.removeGuild(this.id, false);
                        }
                    }

                    this.broadcast(MaplePacketCreator.guildDisband(this.id));
                }
            } catch (Throwable var65) {
                var3 = var65;
                throw var65;
            } finally {
                if (con != null) {
                    if (var3 != null) {
                        try {
                            con.close();
                        } catch (Throwable var58) {
                            var3.addSuppressed(var58);
                        }
                    } else {
                        con.close();
                    }
                }

            }
        } catch (SQLException var67) {
            服务端输出信息.println_err("Error saving guild to SQL");
            FileoutputUtil.outError("logs/资料库异常.txt", var67);
        }

    }

    public final int getLeaderId() {
        return this.leader;
    }

    public void setLeader(int leader) {
        this.changeRank(this.leader, 2);
        MapleCharacter chr = MapleCharacter.getOnlineCharacterById(this.leader);
        Connection con;
        Throwable var4;
        PreparedStatement ps;
        if (chr != null) {
            chr.setGuildRank((byte)2);
        } else {
            try {
                con = DBConPool.getConnection();
                var4 = null;

                try {
                    ps = con.prepareStatement("UPDATE characters SET guildrank = ? WHERE id = ?");
                    ps.setInt(1, 2);
                    ps.setInt(2, this.leader);
                    ps.executeUpdate();
                    ps.close();
                } catch (Throwable var32) {
                    var4 = var32;
                    throw var32;
                } finally {
                    if (con != null) {
                        if (var4 != null) {
                            try {
                                con.close();
                            } catch (Throwable var31) {
                                var4.addSuppressed(var31);
                            }
                        } else {
                            con.close();
                        }
                    }

                }
            } catch (SQLException var36) {
                服务端输出信息.println_err("【错误】 家族 setLeader 错误1，原因:" + var36);
                var36.printStackTrace();
            }
        }

        this.leader = leader;
        this.changeRank(this.leader, 1);
        chr = MapleCharacter.getOnlineCharacterById(this.leader);
        if (chr != null) {
            chr.setGuildRank((byte)1);
        } else {
            try {
                con = DBConPool.getConnection();
                var4 = null;

                try {
                    ps = con.prepareStatement("UPDATE characters SET guildrank = ? WHERE id = ?");
                    ps.setInt(1, 1);
                    ps.setInt(2, this.leader);
                    ps.executeUpdate();
                    ps.close();
                } catch (Throwable var30) {
                    var4 = var30;
                    throw var30;
                } finally {
                    if (con != null) {
                        if (var4 != null) {
                            try {
                                con.close();
                            } catch (Throwable var29) {
                                var4.addSuppressed(var29);
                            }
                        } else {
                            con.close();
                        }
                    }

                }
            } catch (SQLException var34) {
                服务端输出信息.println_err("【错误】 家族 setLeader 错误2，原因:" + var34);
                var34.printStackTrace();
            }
        }

        this.writeToDB(false);
    }

    public final String getLeaderName() {
        Iterator var1 = this.members.iterator();

        MapleGuildCharacter gchr;
        do {
            if (!var1.hasNext()) {
                return "";
            }

            gchr = (MapleGuildCharacter)var1.next();
        } while(gchr == null || gchr.getId() != this.leader);

        return gchr.getName();
    }

    public final MapleCharacter getLeader(MapleClient c) {
        return c.getChannelServer().getPlayerStorage().getCharacterById(this.leader);
    }

    public final int getGP() {
        return this.gp;
    }

    public final int getLogo() {
        return this.logo;
    }

    public final void setLogo(int l) {
        this.logo = l;
    }

    public final int getLogoColor() {
        return this.logoColor;
    }

    public final void setLogoColor(int c) {
        this.logoColor = c;
    }

    public final int getLogoBG() {
        return this.logoBG;
    }

    public final void setLogoBG(int bg) {
        this.logoBG = bg;
    }

    public final int getLogoBGColor() {
        return this.logoBGColor;
    }

    public final void setLogoBGColor(int bgColor) {
        this.logoBGColor = bgColor;
    }

    public final String getNotice() {
        return this.notice == null ? "" : this.notice;
    }

    public final String getName() {
        return this.name;
    }

    public final int getCapacity() {
        return this.capacity;
    }

    public final int getSignature() {
        return this.signature;
    }

    public final void broadcast(byte[] packet) {
        this.broadcast(packet, -1, MapleGuild.BCOp.NONE);
    }

    public final void broadcast(byte[] packet, int exception) {
        this.broadcast(packet, exception, MapleGuild.BCOp.NONE);
    }

    public final void broadcast(byte[] packet, int exceptionId, BCOp bcop) {
        this.wL.lock();

        try {
            this.buildNotifications();
        } finally {
            this.wL.unlock();
        }

        this.rL.lock();

        try {
            Iterator var4 = this.members.iterator();

            while(var4.hasNext()) {
                MapleGuildCharacter mgc = (MapleGuildCharacter)var4.next();
                if (bcop == MapleGuild.BCOp.DISBAND) {
                    if (mgc.isOnline()) {
                        Guild.setGuildAndRank(mgc.getId(), 0, 5, 5);
                    } else {
                        saveCharacterGuildInfo(0, (byte)5, (byte)5, mgc.getId());
                    }
                } else if (mgc.isOnline() && mgc.getId() != exceptionId) {
                    if (bcop == MapleGuild.BCOp.EMBELMCHANGE) {
                        Guild.changeEmblem(this.id, mgc.getId(), new MapleGuildSummary(this));
                    } else {
                        Broadcast.sendGuildPacket(mgc.getId(), packet, exceptionId, this.id);
                    }
                }
            }
        } finally {
            this.rL.unlock();
        }

    }

    private void buildNotifications() {
        if (this.bDirty) {
            List<Integer> mem = new LinkedList();
            Iterator<MapleGuildCharacter> toRemove = this.members.iterator();

            while(true) {
                while(true) {
                    MapleGuildCharacter mgc;
                    do {
                        if (!toRemove.hasNext()) {
                            this.bDirty = false;
                            return;
                        }

                        mgc = (MapleGuildCharacter)toRemove.next();
                    } while(!mgc.isOnline());

                    if (!mem.contains(mgc.getId()) && mgc.getGuildId() == this.id) {
                        mem.add(mgc.getId());
                    } else {
                        this.members.remove(mgc);
                    }
                }
            }
        }
    }

    public final void setOnline(int cid, boolean online, int channel) {
        boolean bBroadcast = true;
        Iterator var5 = this.members.iterator();

        while(var5.hasNext()) {
            MapleGuildCharacter mgc = (MapleGuildCharacter)var5.next();
            if (mgc.getGuildId() == this.id && mgc.getId() == cid) {
                if (mgc.isOnline() == online) {
                    bBroadcast = false;
                }

                mgc.setOnline(online);
                mgc.setChannel((byte)channel);
                break;
            }
        }

        if (bBroadcast) {
            this.broadcast(MaplePacketCreator.guildMemberOnline(this.id, cid, online), cid);
            if (this.allianceid > 0) {
                Alliance.sendGuild(MaplePacketCreator.allianceMemberOnline(this.allianceid, this.id, cid, online), this.id, this.allianceid);
            }
        }

        this.bDirty = true;
        this.init = true;
    }

    public final void guildChat(String name, int cid, String msg) {
        this.broadcast(MaplePacketCreator.multiChat(name, msg, 2), cid);
    }

    public final void allianceChat(String name, int cid, String msg) {
        this.broadcast(MaplePacketCreator.multiChat(name, msg, 3), cid);
    }

    public final String getRankTitle(int rank) {
        return this.rankTitles[rank - 1];
    }

    public int getAllianceId() {
        return this.allianceid;
    }

    public int getInvitedId() {
        return this.invitedid;
    }

    public void setInvitedId(int iid) {
        this.invitedid = iid;
    }

    public void setAllianceId(int a) {
        this.allianceid = a;

        try {
            Connection con = DBConPool.getInstance().getDataSource().getConnection();
            Throwable var3 = null;

            try {
                PreparedStatement ps = con.prepareStatement("UPDATE guilds SET alliance = ? WHERE guildid = ?");
                Throwable var5 = null;

                try {
                    ps.setInt(1, a);
                    ps.setInt(2, this.id);
                    ps.execute();
                } catch (Throwable var30) {
                    var5 = var30;
                    throw var30;
                } finally {
                    if (ps != null) {
                        if (var5 != null) {
                            try {
                                ps.close();
                            } catch (Throwable var29) {
                                var5.addSuppressed(var29);
                            }
                        } else {
                            ps.close();
                        }
                    }

                }
            } catch (Throwable var32) {
                var3 = var32;
                throw var32;
            } finally {
                if (con != null) {
                    if (var3 != null) {
                        try {
                            con.close();
                        } catch (Throwable var28) {
                            var3.addSuppressed(var28);
                        }
                    } else {
                        con.close();
                    }
                }

            }
        } catch (SQLException var34) {
            服务端输出信息.println_err("Saving allianceid ERROR" + var34);
            FileoutputUtil.outError("logs/资料库异常.txt", var34);
        }

    }

    public static final int createGuild(int leaderId, String name) {
        if (name.length() > 12) {
            return 0;
        } else {
            try {
                Connection con = DBConPool.getInstance().getDataSource().getConnection();
                Throwable var3 = null;

                int var7;
                try {
                    PreparedStatement ps = con.prepareStatement("SELECT guildid FROM guilds WHERE name = ?");
                    ps.setString(1, name);
                    ResultSet rs = ps.executeQuery();
                    if (rs.first()) {
                        rs.close();
                        ps.close();
                        byte var21 = 0;
                        return var21;
                    }

                    ps.close();
                    rs.close();
                    ps = con.prepareStatement("INSERT INTO guilds (`leader`, `name`, `signature`, `alliance`) VALUES (?, ?, ?, 0)", 1);
                    ps.setInt(1, leaderId);
                    ps.setString(2, name);
                    ps.setInt(3, (int)(System.currentTimeMillis() / 1000L));
                    ps.execute();
                    rs = ps.getGeneratedKeys();
                    int ret = 0;
                    if (rs.next()) {
                        ret = rs.getInt(1);
                    }

                    rs.close();
                    ps.close();
                    var7 = ret;
                } catch (Throwable var18) {
                    var3 = var18;
                    throw var18;
                } finally {
                    if (con != null) {
                        if (var3 != null) {
                            try {
                                con.close();
                            } catch (Throwable var17) {
                                var3.addSuppressed(var17);
                            }
                        } else {
                            con.close();
                        }
                    }

                }

                return var7;
            } catch (SQLException var20) {
                服务端输出信息.println_err("SQL THROW");
                FileoutputUtil.outError("logs/资料库异常.txt", var20);
                return 0;
            }
        }
    }

    public final int addGuildMember(MapleGuildCharacter mgc) {
        return this.addGuildMember(mgc, true);
    }

    public final int addGuildMember(MapleGuildCharacter mgc, boolean show) {
        this.wL.lock();

        try {
            if (this.members.size() >= this.capacity) {
                byte var9 = 0;
                return var9;
            }

            if ((Integer)LtMS.ConfigValuesMap.get("退出家族需等待周一才能加入家族开关") > 0 && TimeLogCenter.getInstance().getWeekLog(mgc.getId(), "退出家族记录") > 0) {
                MapleCharacter chr = MapleCharacter.getOnlineCharacterById(mgc.getId());
                if (chr != null) {
                    chr.dropMessage(5, "需要等到下周一以后才可以再次加入家族！");
                }

                byte var4 = 0;
                return var4;
            }

            for(int i = this.members.size() - 1; i >= 0; --i) {
                if (((MapleGuildCharacter)this.members.get(i)).getGuildRank() < 5 || ((MapleGuildCharacter)this.members.get(i)).getName().compareTo(mgc.getName()) < 0) {
                    this.members.add(i + 1, mgc);
                    this.bDirty = true;
                    TimeLogCenter.getInstance().setWeekLog(mgc.getId(), "加入家族记录");
                    break;
                }
            }
        } finally {
            this.wL.unlock();
        }

        if (show) {
            this.broadcast(MaplePacketCreator.newGuildMember(mgc));
        }

        if (this.allianceid > 0) {
            Alliance.sendGuild(this.allianceid);
        }

        return 1;
    }

    public final void leaveGuild(MapleGuildCharacter mgc) {
        this.wL.lock();

        try {
            Iterator<MapleGuildCharacter> itr = this.members.iterator();

            while(itr.hasNext()) {
                MapleGuildCharacter mgcc = (MapleGuildCharacter)itr.next();
                if (mgcc.getId() == mgc.getId()) {
                    this.broadcast(MaplePacketCreator.memberLeft(mgcc, true));
                    this.bDirty = true;
                    this.members.remove(mgcc);
                    if (mgc.isOnline()) {
                        Guild.setGuildAndRank(mgcc.getId(), 0, 5, 5);
                    } else {
                        saveCharacterGuildInfo(0, (byte)5, (byte)5, mgcc.getId());
                    }

                    TimeLogCenter.getInstance().setWeekLog(mgc.getId(), "退出家族记录");
                    if ((Integer)LtMS.ConfigValuesMap.get("退出家族需等待周一才能加入家族开关") > 0) {
                        MapleCharacter chr = MapleCharacter.getOnlineCharacterById(mgc.getId());
                        if (chr != null) {
                            chr.dropMessage(5, "需要等到下周一以后才可以再次加入家族！");
                        }
                    }

                    if (this.allianceid > 0) {
                        Alliance.sendGuild(this.allianceid);
                    }
                }
            }
        } finally {
            this.wL.unlock();
        }

    }

    public final void expelMember(MapleGuildCharacter initiator, String name, int cid) {
        this.wL.lock();

        try {
            Iterator<MapleGuildCharacter> itr = this.members.iterator();

            while(itr.hasNext()) {
                MapleGuildCharacter mgc = (MapleGuildCharacter)itr.next();
                if (mgc.getId() == cid && initiator.getGuildRank() < mgc.getGuildRank()) {
                    this.broadcast(MaplePacketCreator.memberLeft(mgc, true));
                    this.bDirty = true;
                    if (this.allianceid > 0) {
                        Alliance.sendGuild(this.allianceid);
                    }

                    TimeLogCenter.getInstance().setWeekLog(mgc.getId(), "退出家族记录");
                    if (mgc.isOnline()) {
                        if ((Integer)LtMS.ConfigValuesMap.get("退出家族需等待周一才能加入家族开关") > 0) {
                            MapleCharacter chr = MapleCharacter.getOnlineCharacterById(mgc.getId());
                            if (chr != null) {
                                chr.dropMessage(5, "需要等到下周一以后才可以再次加入家族！");
                            }
                        }

                        Guild.setGuildAndRank(cid, 0, 5, 5);
                    } else {
                        MapleCharacterUtil.sendNote(mgc.getName(), initiator.getName(), "你已经被公会踢出.", 0);
                        saveCharacterGuildInfo(0, (byte)5, (byte)5, cid);
                    }

                    this.members.remove(mgc);
                    break;
                }
            }
        } finally {
            this.wL.unlock();
        }

    }

    public final void changeARank() {
        this.changeARank(false);
    }

    public final void changeARank(boolean leader) {
        Iterator var2 = this.members.iterator();

        while(var2.hasNext()) {
            MapleGuildCharacter mgc = (MapleGuildCharacter)var2.next();
            if (this.leader == mgc.getId()) {
                this.changeARank(mgc.getId(), leader ? 1 : 2);
            } else {
                this.changeARank(mgc.getId(), 3);
            }
        }

    }

    public final void changeARank(int newRank) {
        Iterator var2 = this.members.iterator();

        while(var2.hasNext()) {
            MapleGuildCharacter mgc = (MapleGuildCharacter)var2.next();
            this.changeARank(mgc.getId(), newRank);
        }

    }

    public final void changeARank(int cid, int newRank) {
        if (this.allianceid > 0) {
            Iterator var3 = this.members.iterator();

            MapleGuildCharacter mgc;
            do {
                if (!var3.hasNext()) {
                    服务端输出信息.println_err("INFO: unable to find the correct id for changeRank({" + cid + "}, {" + newRank + "})");
                    return;
                }

                mgc = (MapleGuildCharacter)var3.next();
            } while(cid != mgc.getId());

            if (mgc.isOnline()) {
                Guild.setGuildAndRank(cid, this.id, mgc.getGuildRank(), newRank);
            } else {
                saveCharacterGuildInfo((short)this.id, mgc.getGuildRank(), (byte)newRank, cid);
            }

            mgc.setAllianceRank((byte)newRank);
            Alliance.sendGuild(this.allianceid);
        }
    }

    public final void changeRank(int cid, int newRank) {
        Iterator var3 = this.members.iterator();

        MapleGuildCharacter mgc;
        do {
            if (!var3.hasNext()) {
                服务端输出信息.println_err("INFO: unable to find the correct id for changeRank({" + cid + "}, {" + newRank + "})");
                return;
            }

            mgc = (MapleGuildCharacter)var3.next();
        } while(cid != mgc.getId());

        if (mgc.isOnline()) {
            Guild.setGuildAndRank(cid, this.id, newRank, mgc.getAllianceRank());
        } else {
            saveCharacterGuildInfo((short)this.id, (byte)newRank, mgc.getAllianceRank(), cid);
        }

        mgc.setGuildRank((byte)newRank);
        this.broadcast(MaplePacketCreator.changeRank(mgc));
    }

    public final void setGuildNotice(String notice) {
        this.notice = notice;
        this.broadcast(MaplePacketCreator.guildNotice(this.id, notice));
    }

    public final void memberLevelJobUpdate(MapleGuildCharacter mgc) {
        Iterator var2 = this.members.iterator();

        while(var2.hasNext()) {
            MapleGuildCharacter member = (MapleGuildCharacter)var2.next();
            if (member.getId() == mgc.getId()) {
                int old_level = member.getLevel();
                int old_job = member.getJobId();
                member.setJobId(mgc.getJobId());
                member.setLevel((short)mgc.getLevel());
                if (mgc.getLevel() > old_level) {
                }

                if (old_level != mgc.getLevel()) {
                    this.broadcast(MaplePacketCreator.sendLevelup(false, mgc.getLevel(), mgc.getName()), mgc.getId());
                }

                if (old_job != mgc.getJobId()) {
                    this.broadcast(MaplePacketCreator.sendJobup(false, mgc.getJobId(), mgc.getName()), mgc.getId());
                }

                this.broadcast(MaplePacketCreator.guildMemberLevelJobUpdate(mgc));
                if (this.allianceid > 0) {
                    Alliance.sendGuild(MaplePacketCreator.updateAlliance(mgc, this.allianceid), this.id, this.allianceid);
                }
                break;
            }
        }

    }

    public final void changeRankTitle(String[] ranks) {
        System.arraycopy(ranks, 0, this.rankTitles, 0, 5);
        this.broadcast(MaplePacketCreator.rankTitleChange(this.id, ranks));
    }

    public final void disbandGuild() {
        this.writeToDB(true);
        this.broadcast((byte[])null, -1, MapleGuild.BCOp.DISBAND);
    }

    public final void setGuildEmblem(short bg, byte bgcolor, short logo, byte logocolor) {
        this.logoBG = bg;
        this.logoBGColor = bgcolor;
        this.logo = logo;
        this.logoColor = logocolor;
        this.broadcast((byte[])null, -1, MapleGuild.BCOp.EMBELMCHANGE);

        try {
            Connection con = DBConPool.getInstance().getDataSource().getConnection();
            Throwable var6 = null;

            try {
                PreparedStatement ps = con.prepareStatement("UPDATE guilds SET logo = ?, logoColor = ?, logoBG = ?, logoBGColor = ? WHERE guildid = ?");
                Throwable var8 = null;

                try {
                    ps.setInt(1, logo);
                    ps.setInt(2, this.logoColor);
                    ps.setInt(3, this.logoBG);
                    ps.setInt(4, this.logoBGColor);
                    ps.setInt(5, this.id);
                    ps.execute();
                } catch (Throwable var33) {
                    var8 = var33;
                    throw var33;
                } finally {
                    if (ps != null) {
                        if (var8 != null) {
                            try {
                                ps.close();
                            } catch (Throwable var32) {
                                var8.addSuppressed(var32);
                            }
                        } else {
                            ps.close();
                        }
                    }

                }
            } catch (Throwable var35) {
                var6 = var35;
                throw var35;
            } finally {
                if (con != null) {
                    if (var6 != null) {
                        try {
                            con.close();
                        } catch (Throwable var31) {
                            var6.addSuppressed(var31);
                        }
                    } else {
                        con.close();
                    }
                }

            }
        } catch (SQLException var37) {
            服务端输出信息.println_err("Saving guild logo / BG colo ERROR");
            FileoutputUtil.outError("logs/资料库异常.txt", var37);
        }

    }

    public final MapleGuildCharacter getMGC(int cid) {
        Iterator var2 = this.members.iterator();

        MapleGuildCharacter mgc;
        do {
            if (!var2.hasNext()) {
                return null;
            }

            mgc = (MapleGuildCharacter)var2.next();
        } while(mgc.getId() != cid);

        return mgc;
    }

    public final boolean increaseCapacity() {
        if (this.capacity < 100 && this.capacity + 5 <= 100) {
            this.capacity += 5;
            this.broadcast(MaplePacketCreator.guildCapacityChange(this.id, this.capacity));

            try {
                Connection con = DBConPool.getInstance().getDataSource().getConnection();
                Throwable var2 = null;

                try {
                    PreparedStatement ps = con.prepareStatement("UPDATE guilds SET capacity = ? WHERE guildid = ?");
                    Throwable var4 = null;

                    try {
                        ps.setInt(1, this.capacity);
                        ps.setInt(2, this.id);
                        ps.execute();
                    } catch (Throwable var29) {
                        var4 = var29;
                        throw var29;
                    } finally {
                        if (ps != null) {
                            if (var4 != null) {
                                try {
                                    ps.close();
                                } catch (Throwable var28) {
                                    var4.addSuppressed(var28);
                                }
                            } else {
                                ps.close();
                            }
                        }

                    }
                } catch (Throwable var31) {
                    var2 = var31;
                    throw var31;
                } finally {
                    if (con != null) {
                        if (var2 != null) {
                            try {
                                con.close();
                            } catch (Throwable var27) {
                                var2.addSuppressed(var27);
                            }
                        } else {
                            con.close();
                        }
                    }

                }
            } catch (SQLException var33) {
                服务端输出信息.println_err("Saving guild capacity ERROR");
                FileoutputUtil.outError("logs/资料库异常.txt", var33);
            }

            return true;
        } else {
            return false;
        }
    }

    public final void gainGP(int amount) {
        this.gainGP(amount, true);
    }

    public final void gainGP(int amount, boolean broadcast) {
        if (amount != 0) {
            if (amount + this.gp < 0) {
                amount = -this.gp;
            }

            this.gp += amount;
            this.broadcast(MaplePacketCreator.updateGP(this.id, this.gp));
            if (broadcast) {
                this.broadcast(UIPacket.getGPMsg(amount));
            }

        }
    }

    public final void addMemberData(MaplePacketLittleEndianWriter mplew) {
        mplew.write(this.members.size());
        Iterator var2 = this.members.iterator();

        MapleGuildCharacter mgc;
        while(var2.hasNext()) {
            mgc = (MapleGuildCharacter)var2.next();
            mplew.writeInt(mgc.getId());
        }

        var2 = this.members.iterator();

        while(var2.hasNext()) {
            mgc = (MapleGuildCharacter)var2.next();
            mplew.writeAsciiString(mgc.getName(), 13);
            mplew.writeInt(mgc.getJobId());
            mplew.writeInt(mgc.getLevel());
            mplew.writeInt(mgc.getGuildRank());
            mplew.writeInt(mgc.isOnline() ? 1 : 0);
            mplew.writeInt(this.signature);
            mplew.writeInt(mgc.getAllianceRank());
        }

    }

    public static final MapleGuildResponse sendInvite(MapleClient c, String targetName) {
        MapleCharacter mc = c.getChannelServer().getPlayerStorage().getCharacterByName(targetName);
        if (mc == null) {
            return MapleGuildResponse.NOT_IN_CHANNEL;
        } else if (mc.getGuildId() > 0) {
            return MapleGuildResponse.ALREADY_IN_GUILD;
        } else {
            mc.getClient().sendPacket(MaplePacketCreator.guildInvite(c.getPlayer().getGuildId(), c.getPlayer().getName(), c.getPlayer().getLevel(), c.getPlayer().getJob()));
            return null;
        }
    }

    public Collection<MapleGuildCharacter> getMembers() {
        return Collections.unmodifiableCollection(this.members);
    }

    public final boolean isInit() {
        return this.init;
    }

    public final List<MapleBBSThread> getBBS() {
        List<MapleBBSThread> ret = new ArrayList(this.bbs.values());
        Collections.sort(ret, new MapleBBSThread.ThreadComparator());
        return ret;
    }

    public final int addBBSThread(String title, String text, int icon, boolean bNotice, int posterID) {
        int add = this.bbs.get(0) == null ? 1 : 0;
        int ret = bNotice ? 0 : Math.max(1, this.bbs.size() + add);
        this.bbs.put(ret, new MapleBBSThread(ret, title, text, System.currentTimeMillis(), this.id, posterID, icon));
        return ret;
    }

    public final void editBBSThread(int localthreadid, String title, String text, int icon, int posterID, int guildRank) {
        MapleBBSThread thread = (MapleBBSThread)this.bbs.get(localthreadid);
        if (thread != null && (thread.ownerID == posterID || guildRank <= 2)) {
            this.bbs.put(localthreadid, new MapleBBSThread(localthreadid, title, text, System.currentTimeMillis(), this.id, thread.ownerID, icon));
        }

    }

    public final void deleteBBSThread(int localthreadid, int posterID, int guildRank) {
        MapleBBSThread thread = (MapleBBSThread)this.bbs.get(localthreadid);
        if (thread != null && (thread.ownerID == posterID || guildRank <= 2)) {
            this.bbs.remove(localthreadid);
        }

    }

    public final void addBBSReply(int localthreadid, String text, int posterID) {
        MapleBBSThread thread = (MapleBBSThread)this.bbs.get(localthreadid);
        if (thread != null) {
            thread.replies.put(thread.replies.size(), new MapleBBSThread.MapleBBSReply(thread.replies.size(), posterID, text, System.currentTimeMillis()));
        }

    }

    public final void deleteBBSReply(int localthreadid, int replyid, int posterID, int guildRank) {
        MapleBBSThread thread = (MapleBBSThread)this.bbs.get(localthreadid);
        if (thread != null) {
            MapleBBSThread.MapleBBSReply reply = (MapleBBSThread.MapleBBSReply)thread.replies.get(replyid);
            if (reply != null && (reply.ownerID == posterID || guildRank <= 2)) {
                thread.replies.remove(replyid);
            }
        }

    }

    public static void saveCharacterGuildInfo(int guildid, byte guildrank, byte alliancerank, int cid) {
        try {
            Connection con = DBConPool.getInstance().getDataSource().getConnection();
            Throwable var5 = null;

            try {
                PreparedStatement ps = con.prepareStatement("UPDATE characters SET guildid = ?, guildrank = ?, alliancerank = ? WHERE id = ?");
                ps.setInt(1, guildid);
                ps.setInt(2, guildrank);
                ps.setInt(3, alliancerank);
                ps.setInt(4, cid);
                ps.executeUpdate();
                ps.close();
            } catch (Throwable var15) {
                var5 = var15;
                throw var15;
            } finally {
                if (con != null) {
                    if (var5 != null) {
                        try {
                            con.close();
                        } catch (Throwable var14) {
                            var5.addSuppressed(var14);
                        }
                    } else {
                        con.close();
                    }
                }

            }
        } catch (SQLException var17) {
            服务端输出信息.println_err("SQLException: " + var17.getLocalizedMessage());
            FileoutputUtil.outError("logs/资料库异常.txt", var17);
        }

    }

    public String getPrefix(MapleCharacter chr) {
        return chr.getPrefix();
    }

    public static void 战斗力排行(MapleClient c, int npcid) {
        try {
            Connection con = DBConPool.getConnection();
            PreparedStatement ps = con.prepareStatement("select `name`, ((`level` * 15)  + (maxhp / 50) + (maxmp / 50) + str + dex + luk + `int`) * max_damage / 199999 AS `data`, `level`, meso from characters order by `data` desc LIMIT 100");
            Throwable localThrowable2 = null;

            ResultSet rs;
            try {
                rs = ps.executeQuery();
                c.sendPacket(MaplePacketCreator.showCustomRanks(npcid, rs));
            } catch (Throwable var15) {
                localThrowable2 = var15;
                throw var15;
            } finally {
                if (ps != null) {
                    if (localThrowable2 != null) {
                        try {
                            ps.close();
                        } catch (Throwable var14) {
                            localThrowable2.addSuppressed(var14);
                        }
                    } else {
                        ps.close();
                    }
                }

            }

            rs.close();
        } catch (SQLException var17) {
            服务端输出信息.println_err("战斗力排行出错！,原因：" + var17);
        }

    }

    public static void 战斗力排行_snail(MapleClient c, int npcid) {
        ArrayList<战斗力信息> players = new ArrayList();
        Connection con = DBConPool.getConnection();

        try {
            con.setTransactionIsolation(1);
            con.setAutoCommit(false);
            PreparedStatement ps = con.prepareStatement("select * from characters");
            ResultSet rs = ps.executeQuery();

            label32:
            while(true) {
                do {
                    if (!rs.next()) {
                        ps.close();
                        rs.close();
                        con.setAutoCommit(true);
                        con.setTransactionIsolation(4);
                        break label32;
                    }
                } while(rs.getInt("gm") >= 1);

                战斗力信息 player = new 战斗力信息();
                int chrid = rs.getInt("id");
                PreparedStatement ps1 = con.prepareStatement("SELECT * FROM `inventoryitems` LEFT JOIN `inventoryequipment` USING(`inventoryitemid`) WHERE `characterid` = ? AND `position` < 0");
                ps1.setInt(1, chrid);
                ResultSet rs1 = ps1.executeQuery();
                player.setPotentials("");

                while(rs1.next()) {
                    player.setE_str(player.getE_str() + rs1.getShort("str"));
                    player.setE_dex(player.getE_dex() + rs1.getShort("dex"));
                    player.setE_int(player.getE_int() + rs1.getShort("int"));
                    player.setE_luk(player.getE_luk() + rs1.getShort("luk"));
                    player.setE_hp(player.getE_hp() + rs1.getShort("hp"));
                    player.setE_mp(player.getE_mp() + rs1.getShort("mp"));
                    player.setE_watk(player.getE_watk() + rs1.getShort("watk"));
                    player.setE_matk(player.getE_matk() + rs1.getShort("matk"));
                    player.setPotentials(player.getPotentials() + rs1.getString("snail_potentials"));
                }

                player.setChrid(chrid);
                player.setName(rs.getString("name"));
                player.setLevel(rs.getInt("level"));
                player.setJob(rs.getInt("job"));
                player.setStr(rs.getInt("str"));
                player.setDex(rs.getInt("dex"));
                player.setInt(rs.getInt("int"));
                player.setLuk(rs.getInt("luk"));
                player.setMaxhp(rs.getInt("maxhp"));
                player.setMaxmp(rs.getInt("maxmp"));
                player.setMax_damage(rs.getLong("max_damage"));
                player.solveForce();
                players.add(player);
            }
        } catch (SQLException var10) {
            服务端输出信息.println_err("战斗力排行_snail出错！,原因：" + var10);
        }

        按战斗力排序(players);
        c.sendPacket(MaplePacketCreator.显示战斗力排行_snail(npcid, players));
    }

    public static void 力量排行_snail(MapleClient c, int npcid) {
        ArrayList<战斗力信息> players = new ArrayList();
        Connection con = DBConPool.getConnection();

        try {
            con.setTransactionIsolation(1);
            con.setAutoCommit(false);
            PreparedStatement ps = con.prepareStatement("select * from characters");
            ResultSet rs = ps.executeQuery();

            label32:
            while(true) {
                do {
                    if (!rs.next()) {
                        ps.close();
                        rs.close();
                        con.setAutoCommit(true);
                        con.setTransactionIsolation(4);
                        break label32;
                    }
                } while(rs.getInt("gm") >= 1);

                战斗力信息 player = new 战斗力信息();
                int chrid = rs.getInt("id");
                PreparedStatement ps1 = con.prepareStatement("SELECT * FROM `inventoryitems` LEFT JOIN `inventoryequipment` USING(`inventoryitemid`) WHERE `characterid` = ? AND `position` < 0");
                ps1.setInt(1, chrid);
                ResultSet rs1 = ps1.executeQuery();
                player.setPotentials("");

                while(rs1.next()) {
                    player.setE_str(player.getE_str() + rs1.getShort("str"));
                    player.setE_dex(player.getE_dex() + rs1.getShort("dex"));
                    player.setE_int(player.getE_int() + rs1.getShort("int"));
                    player.setE_luk(player.getE_luk() + rs1.getShort("luk"));
                    player.setE_hp(player.getE_hp() + rs1.getShort("hp"));
                    player.setE_mp(player.getE_mp() + rs1.getShort("mp"));
                    player.setE_watk(player.getE_watk() + rs1.getShort("watk"));
                    player.setE_matk(player.getE_matk() + rs1.getShort("matk"));
                    player.setPotentials(player.getPotentials() + rs1.getString("snail_potentials"));
                }

                player.setChrid(chrid);
                player.setName(rs.getString("name"));
                player.setLevel(rs.getInt("level"));
                player.setJob(rs.getInt("job"));
                player.setStr(rs.getInt("str"));
                player.setDex(rs.getInt("dex"));
                player.setInt(rs.getInt("int"));
                player.setLuk(rs.getInt("luk"));
                player.setMaxhp(rs.getInt("maxhp"));
                player.setMaxmp(rs.getInt("maxmp"));
                player.setMax_damage((long)rs.getInt("max_damage"));
                player.solveForce();
                players.add(player);
            }
        } catch (SQLException var10) {
            服务端输出信息.println_err("战斗力排行_snail出错！,原因：" + var10);
        }

        按力量排序(players);
        c.sendPacket(MaplePacketCreator.显示力量排行_snail(npcid, players));
    }

    public static void 敏捷排行_snail(MapleClient c, int npcid) {
        ArrayList<战斗力信息> players = new ArrayList();
        Connection con = DBConPool.getConnection();

        try {
            con.setTransactionIsolation(1);
            con.setAutoCommit(false);
            PreparedStatement ps = con.prepareStatement("select * from characters");
            ResultSet rs = ps.executeQuery();

            label32:
            while(true) {
                do {
                    if (!rs.next()) {
                        ps.close();
                        rs.close();
                        con.setAutoCommit(true);
                        con.setTransactionIsolation(4);
                        break label32;
                    }
                } while(rs.getInt("gm") >= 1);

                战斗力信息 player = new 战斗力信息();
                int chrid = rs.getInt("id");
                PreparedStatement ps1 = con.prepareStatement("SELECT * FROM `inventoryitems` LEFT JOIN `inventoryequipment` USING(`inventoryitemid`) WHERE `characterid` = ? AND `position` < 0");
                ps1.setInt(1, chrid);
                ResultSet rs1 = ps1.executeQuery();
                player.setPotentials("");

                while(rs1.next()) {
                    player.setE_str(player.getE_str() + rs1.getShort("str"));
                    player.setE_dex(player.getE_dex() + rs1.getShort("dex"));
                    player.setE_int(player.getE_int() + rs1.getShort("int"));
                    player.setE_luk(player.getE_luk() + rs1.getShort("luk"));
                    player.setE_hp(player.getE_hp() + rs1.getShort("hp"));
                    player.setE_mp(player.getE_mp() + rs1.getShort("mp"));
                    player.setE_watk(player.getE_watk() + rs1.getShort("watk"));
                    player.setE_matk(player.getE_matk() + rs1.getShort("matk"));
                    player.setPotentials(player.getPotentials() + rs1.getString("snail_potentials"));
                }

                player.setChrid(chrid);
                player.setName(rs.getString("name"));
                player.setLevel(rs.getInt("level"));
                player.setJob(rs.getInt("job"));
                player.setStr(rs.getInt("str"));
                player.setDex(rs.getInt("dex"));
                player.setInt(rs.getInt("int"));
                player.setLuk(rs.getInt("luk"));
                player.setMaxhp(rs.getInt("maxhp"));
                player.setMaxmp(rs.getInt("maxmp"));
                player.setMax_damage((long)rs.getInt("max_damage"));
                player.solveForce();
                players.add(player);
            }
        } catch (SQLException var10) {
            服务端输出信息.println_err("战斗力排行_snail出错！,原因：" + var10);
        }

        按敏捷排序(players);
        c.sendPacket(MaplePacketCreator.显示敏捷排行_snail(npcid, players));
    }

    public static void 智力排行_snail(MapleClient c, int npcid) {
        ArrayList<战斗力信息> players = new ArrayList();
        Connection con = DBConPool.getConnection();

        try {
            con.setTransactionIsolation(1);
            con.setAutoCommit(false);
            PreparedStatement ps = con.prepareStatement("select * from characters");
            ResultSet rs = ps.executeQuery();

            label32:
            while(true) {
                do {
                    if (!rs.next()) {
                        ps.close();
                        rs.close();
                        con.setAutoCommit(true);
                        con.setTransactionIsolation(4);
                        break label32;
                    }
                } while(rs.getInt("gm") >= 1);

                战斗力信息 player = new 战斗力信息();
                int chrid = rs.getInt("id");
                PreparedStatement ps1 = con.prepareStatement("SELECT * FROM `inventoryitems` LEFT JOIN `inventoryequipment` USING(`inventoryitemid`) WHERE `characterid` = ? AND `position` < 0");
                ps1.setInt(1, chrid);
                ResultSet rs1 = ps1.executeQuery();
                player.setPotentials("");

                while(rs1.next()) {
                    player.setE_str(player.getE_str() + rs1.getShort("str"));
                    player.setE_dex(player.getE_dex() + rs1.getShort("dex"));
                    player.setE_int(player.getE_int() + rs1.getShort("int"));
                    player.setE_luk(player.getE_luk() + rs1.getShort("luk"));
                    player.setE_hp(player.getE_hp() + rs1.getShort("hp"));
                    player.setE_mp(player.getE_mp() + rs1.getShort("mp"));
                    player.setE_watk(player.getE_watk() + rs1.getShort("watk"));
                    player.setE_matk(player.getE_matk() + rs1.getShort("matk"));
                    player.setPotentials(player.getPotentials() + rs1.getString("snail_potentials"));
                }

                player.setChrid(chrid);
                player.setName(rs.getString("name"));
                player.setLevel(rs.getInt("level"));
                player.setJob(rs.getInt("job"));
                player.setStr(rs.getInt("str"));
                player.setDex(rs.getInt("dex"));
                player.setInt(rs.getInt("int"));
                player.setLuk(rs.getInt("luk"));
                player.setMaxhp(rs.getInt("maxhp"));
                player.setMaxmp(rs.getInt("maxmp"));
                player.setMax_damage((long)rs.getInt("max_damage"));
                player.solveForce();
                players.add(player);
            }
        } catch (SQLException var10) {
            服务端输出信息.println_err("战斗力排行_snail出错！,原因：" + var10);
        }

        按智力排序(players);
        c.sendPacket(MaplePacketCreator.显示智力排行_snail(npcid, players));
    }

    public static void 运气排行_snail(MapleClient c, int npcid) {
        ArrayList<战斗力信息> players = new ArrayList();
        Connection con = DBConPool.getConnection();

        try {
            con.setTransactionIsolation(1);
            con.setAutoCommit(false);
            PreparedStatement ps = con.prepareStatement("select * from characters");
            ResultSet rs = ps.executeQuery();

            label32:
            while(true) {
                do {
                    if (!rs.next()) {
                        ps.close();
                        rs.close();
                        con.setAutoCommit(true);
                        con.setTransactionIsolation(4);
                        break label32;
                    }
                } while(rs.getInt("gm") >= 1);

                战斗力信息 player = new 战斗力信息();
                int chrid = rs.getInt("id");
                PreparedStatement ps1 = con.prepareStatement("SELECT * FROM `inventoryitems` LEFT JOIN `inventoryequipment` USING(`inventoryitemid`) WHERE `characterid` = ? AND `position` < 0");
                ps1.setInt(1, chrid);
                ResultSet rs1 = ps1.executeQuery();
                player.setPotentials("");

                while(rs1.next()) {
                    player.setE_str(player.getE_str() + rs1.getShort("str"));
                    player.setE_dex(player.getE_dex() + rs1.getShort("dex"));
                    player.setE_int(player.getE_int() + rs1.getShort("int"));
                    player.setE_luk(player.getE_luk() + rs1.getShort("luk"));
                    player.setE_hp(player.getE_hp() + rs1.getShort("hp"));
                    player.setE_mp(player.getE_mp() + rs1.getShort("mp"));
                    player.setE_watk(player.getE_watk() + rs1.getShort("watk"));
                    player.setE_matk(player.getE_matk() + rs1.getShort("matk"));
                    player.setPotentials(player.getPotentials() + rs1.getString("snail_potentials"));
                }

                player.setChrid(chrid);
                player.setName(rs.getString("name"));
                player.setLevel(rs.getInt("level"));
                player.setJob(rs.getInt("job"));
                player.setStr(rs.getInt("str"));
                player.setDex(rs.getInt("dex"));
                player.setInt(rs.getInt("int"));
                player.setLuk(rs.getInt("luk"));
                player.setMaxhp(rs.getInt("maxhp"));
                player.setMaxmp(rs.getInt("maxmp"));
                player.setMax_damage((long)rs.getInt("max_damage"));
                player.solveForce();
                players.add(player);
            }
        } catch (SQLException var10) {
            服务端输出信息.println_err("战斗力排行_snail出错！,原因：" + var10);
        }

        按运气排序(players);
        c.sendPacket(MaplePacketCreator.显示运气排行_snail(npcid, players));
    }

    private static void 按力量排序(ArrayList<战斗力信息> players) {
        int n = players.size();
        if (n > 1) {
            for(int i = 0; i < n; ++i) {
                boolean flag = false;

                for(int j = 0; j < n - i - 1; ++j) {
                    int str1 = ((战斗力信息)players.get(j)).getStr() + ((战斗力信息)players.get(j)).getE_str();
                    int str2 = ((战斗力信息)players.get(j + 1)).getStr() + ((战斗力信息)players.get(j + 1)).getE_str();
                    if (str1 < str2) {
                        Collections.swap(players, j, j + 1);
                        flag = true;
                    }
                }

                if (!flag) {
                    break;
                }
            }

        }
    }

    private static void 按敏捷排序(ArrayList<战斗力信息> players) {
        int n = players.size();
        if (n > 1) {
            for(int i = 0; i < n; ++i) {
                boolean flag = false;

                for(int j = 0; j < n - i - 1; ++j) {
                    int dex1 = ((战斗力信息)players.get(j)).getDex() + ((战斗力信息)players.get(j)).getE_dex();
                    int dex2 = ((战斗力信息)players.get(j + 1)).getDex() + ((战斗力信息)players.get(j + 1)).getE_dex();
                    if (dex1 < dex2) {
                        Collections.swap(players, j, j + 1);
                        flag = true;
                    }
                }

                if (!flag) {
                    break;
                }
            }

        }
    }

    private static void 按智力排序(ArrayList<战斗力信息> players) {
        int n = players.size();
        if (n > 1) {
            for(int i = 0; i < n; ++i) {
                boolean flag = false;

                for(int j = 0; j < n - i - 1; ++j) {
                    int int1 = ((战斗力信息)players.get(j)).getInt() + ((战斗力信息)players.get(j)).getE_int();
                    int int2 = ((战斗力信息)players.get(j + 1)).getInt() + ((战斗力信息)players.get(j + 1)).getE_int();
                    if (int1 < int2) {
                        Collections.swap(players, j, j + 1);
                        flag = true;
                    }
                }

                if (!flag) {
                    break;
                }
            }

        }
    }

    private static void 按运气排序(ArrayList<战斗力信息> players) {
        int n = players.size();
        if (n > 1) {
            for(int i = 0; i < n; ++i) {
                boolean flag = false;

                for(int j = 0; j < n - i - 1; ++j) {
                    int luk1 = ((战斗力信息)players.get(j)).getLuk() + ((战斗力信息)players.get(j)).getE_luk();
                    int luk2 = ((战斗力信息)players.get(j + 1)).getLuk() + ((战斗力信息)players.get(j + 1)).getE_luk();
                    if (luk1 < luk2) {
                        Collections.swap(players, j, j + 1);
                        flag = true;
                    }
                }

                if (!flag) {
                    break;
                }
            }

        }
    }

    private static void 按战斗力排序(ArrayList<战斗力信息> players) {
        int n = players.size();
        if (n > 1) {
            for(int i = 0; i < n; ++i) {
                boolean flag = false;

                for(int j = 0; j < n - i - 1; ++j) {
                    if (((战斗力信息)players.get(j)).getForce() < ((战斗力信息)players.get(j + 1)).getForce()) {
                        Collections.swap(players, j, j + 1);
                        flag = true;
                    }
                }

                if (!flag) {
                    break;
                }
            }

        }
    }

    public static void 总在线时间排行(MapleClient c, int npcid) {
        try {
            Connection con = DBConPool.getConnection();
            PreparedStatement ps = con.prepareStatement("select `name`, totalOnlineTime AS `data`, `level`, meso from characters order by `data` desc LIMIT 10");
            Throwable localThrowable2 = null;

            ResultSet rs;
            try {
                rs = ps.executeQuery();
                c.sendPacket(MaplePacketCreator.showCustomRanks(npcid, rs));
            } catch (Throwable var15) {
                localThrowable2 = var15;
                throw var15;
            } finally {
                if (ps != null) {
                    if (localThrowable2 != null) {
                        try {
                            ps.close();
                        } catch (Throwable var14) {
                            localThrowable2.addSuppressed(var14);
                        }
                    } else {
                        ps.close();
                    }
                }

            }

            rs.close();
        } catch (SQLException var17) {
            服务端输出信息.println_err("总在线时间排行出错！");
        }

    }

    public static void 声望排行(MapleClient c, int npcid) {
        try {
            Connection con = DBConPool.getConnection();
            PreparedStatement ps = con.prepareStatement("select `name`, totalrep AS `data`, `level`, meso from characters order by `data` desc LIMIT 10");
            Throwable localThrowable2 = null;

            ResultSet rs;
            try {
                rs = ps.executeQuery();
                c.sendPacket(MaplePacketCreator.showCustomRanks(npcid, rs));
            } catch (Throwable var15) {
                localThrowable2 = var15;
                throw var15;
            } finally {
                if (ps != null) {
                    if (localThrowable2 != null) {
                        try {
                            ps.close();
                        } catch (Throwable var14) {
                            localThrowable2.addSuppressed(var14);
                        }
                    } else {
                        ps.close();
                    }
                }

            }

            rs.close();
        } catch (SQLException var17) {
            服务端输出信息.println_err("人气排行出错！");
        }

    }

    public static void 人气排行(MapleClient c, int npcid) {
        try {
            Connection con = DBConPool.getConnection();
            PreparedStatement ps = con.prepareStatement("select `name`, fame AS `data`, `level`, meso from characters order by `data` desc LIMIT 10");
            Throwable localThrowable2 = null;

            ResultSet rs;
            try {
                rs = ps.executeQuery();
                c.sendPacket(MaplePacketCreator.showCustomRanks(npcid, rs));
            } catch (Throwable var15) {
                localThrowable2 = var15;
                throw var15;
            } finally {
                if (ps != null) {
                    if (localThrowable2 != null) {
                        try {
                            ps.close();
                        } catch (Throwable var14) {
                            localThrowable2.addSuppressed(var14);
                        }
                    } else {
                        ps.close();
                    }
                }

            }

            rs.close();
        } catch (SQLException var17) {
            服务端输出信息.println_err("人气排行出错！");
        }

    }

    public static void 豆豆排行(MapleClient c, int npcid) {
        try {
            Connection con = DBConPool.getConnection();
            PreparedStatement ps = con.prepareStatement("select `name`, beans AS `data`, `level`, meso from characters order by `data` desc LIMIT 10");
            Throwable localThrowable2 = null;

            ResultSet rs;
            try {
                rs = ps.executeQuery();
                c.sendPacket(MaplePacketCreator.showCustomRanks(npcid, rs));
            } catch (Throwable var15) {
                localThrowable2 = var15;
                throw var15;
            } finally {
                if (ps != null) {
                    if (localThrowable2 != null) {
                        try {
                            ps.close();
                        } catch (Throwable var14) {
                            localThrowable2.addSuppressed(var14);
                        }
                    } else {
                        ps.close();
                    }
                }

            }

            rs.close();
        } catch (SQLException var17) {
            服务端输出信息.println_err("豆豆排行出错！");
        }

    }

    public boolean loadGuildSkillsFromDB() {
        boolean find = false;

        try {
            Connection con = DBConPool.getConnection();
            Throwable var3 = null;

            try {
                this.guildSkills.clear();
                PreparedStatement ps = con.prepareStatement("SELECT * FROM `snail_guild_skills` LEFT JOIN `snail_guild_skill_info` USING(`skilltype`) WHERE `guildid` = ?");
                ps.setInt(1, this.id);

                for(ResultSet rs = ps.executeQuery(); rs.next(); find = true) {
                    this.guildSkills.add(new GuildSkill(rs.getInt("skilltype"), rs.getInt("skilllevel"), rs.getInt("skillval"), rs.getString("skillname") + " " + rs.getString("skillinfo")));
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

            return find;
        } catch (SQLException var16) {
            服务端输出信息.println_err("【错误】loadGuildSkillsFromDB错误，错误原因：" + var16);
            var16.printStackTrace();
            return false;
        }
    }

    public ArrayList<GuildSkill> getGuildSkills() {
        if (this.guildSkills.isEmpty()) {
            this.loadGuildSkillsFromDB();
        }

        return this.guildSkills;
    }

    public static String getGuildSkillInfoFromDB(int skillType, int skillVal) {
        try {
            Connection con = DBConPool.getConnection();
            Throwable var3 = null;

            try {
                PreparedStatement ps = con.prepareStatement("SELECT * FROM `snail_guild_skill_info`  WHERE `skillType` = ?");
                ps.setInt(1, skillType);
                ResultSet rs = ps.executeQuery();
                String info = "";
                if (rs.next()) {
                    info = rs.getString("skillname") + " " + rs.getString("skillinfo");
                    if (skillVal < 0) {
                        info = info.replaceFirst("%s", "?");
                    } else {
                        info = info.replaceFirst("%s", skillVal + "");
                    }

                    String var7 = info;
                    return var7;
                }
            } catch (Throwable var18) {
                var3 = var18;
                throw var18;
            } finally {
                if (con != null) {
                    if (var3 != null) {
                        try {
                            con.close();
                        } catch (Throwable var17) {
                            var3.addSuppressed(var17);
                        }
                    } else {
                        con.close();
                    }
                }

            }

            return "无";
        } catch (SQLException var20) {
            服务端输出信息.println_err("【错误】loadGuildSkillsFromDB错误，错误原因：" + var20);
            var20.printStackTrace();
            return "无";
        }
    }

    public int getGuildSkillLevel(int skillType) {
        int level = 0;
        Iterator var3 = this.getGuildSkills().iterator();

        while(var3.hasNext()) {
            GuildSkill skill = (GuildSkill)var3.next();
            if (skill != null && skill.type == skillType && level < skill.level) {
                level = skill.level;
            }
        }

        return level;
    }

    public int getGuildSkillVal(int skillType) {
        int val = 0;
        Iterator var3 = this.getGuildSkills().iterator();

        while(var3.hasNext()) {
            GuildSkill skill = (GuildSkill)var3.next();
            if (skill != null && skill.type == skillType) {
                val += skill.val;
            }
        }

        return val;
    }

    public String getGuildSkillInfo(int skillType) {
        int val = this.getGuildSkillVal(skillType);
        String msg = "";
        if (val > 0) {
            Iterator var4 = this.getGuildSkills().iterator();

            while(var4.hasNext()) {
                GuildSkill skill = (GuildSkill)var4.next();
                if (skill != null && skill.type == skillType) {
                    msg = skill.info;
                    break;
                }
            }

            if (!msg.equals("")) {
                msg = msg.replaceFirst("%s", val + "");
            }
        }

        if (msg.equals("")) {
            msg = "无";
        }

        return msg;
    }

    public boolean addGuildSkill(int skillType, int skillLevel, int skillVal) {
        try {
            Connection con = DBConPool.getConnection();
            Throwable var5 = null;

            boolean var9;
            try {
                PreparedStatement ps = con.prepareStatement("SELECT * FROM snail_guild_skill_info WHERE skilltype = ?");
                ps.setInt(1, skillType);
                ResultSet rs = ps.executeQuery();
                String info = "";
                if (rs.next()) {
                    info = rs.getString("skillname") + " " + rs.getString("skillinfo");
                }

                if (!info.equals("")) {
                    ps.close();
                    rs.close();
                    this.removeGuildSkill(skillType);
                    this.guildSkills.add(new GuildSkill(skillType, skillLevel, skillVal, info));
                    var9 = true;
                    return var9;
                }

                ps.close();
                rs.close();
                var9 = false;
            } catch (Throwable var20) {
                var5 = var20;
                throw var20;
            } finally {
                if (con != null) {
                    if (var5 != null) {
                        try {
                            con.close();
                        } catch (Throwable var19) {
                            var5.addSuppressed(var19);
                        }
                    } else {
                        con.close();
                    }
                }

            }

            return var9;
        } catch (SQLException var22) {
            服务端输出信息.println_err("【错误】addGuildSkill错误，错误原因：" + var22);
            var22.printStackTrace();
            return false;
        }
    }

    public boolean removeGuildSkill(int skillType) {
        try {
            boolean find = false;

            for(int i = 0; i < this.guildSkills.size(); ++i) {
                if (this.guildSkills.get(i) != null && ((GuildSkill)this.guildSkills.get(i)).type == skillType) {
                    this.guildSkills.remove(i);
                    --i;
                    find = true;
                }
            }

            return find;
        } catch (Exception var4) {
            服务端输出信息.println_err("【错误】removeGuildSkill错误，错误原因：" + var4);
            var4.printStackTrace();
            return false;
        }
    }

    public void clearGuildSkills() {
        this.guildSkills.clear();
    }

    public boolean saveGuildSkillsToDB() {
        try {
            Connection con = DBConPool.getConnection();
            Throwable var2 = null;

            boolean var18;
            try {
                PreparedStatement ps = con.prepareStatement("SELECT * FROM snail_guild_skills WHERE guildid = ? AND skilltype = ?");

                ResultSet rs;
                for(Iterator var4 = this.guildSkills.iterator(); var4.hasNext(); rs.close()) {
                    GuildSkill skill = (GuildSkill)var4.next();
                    ps = con.prepareStatement("SELECT * FROM snail_guild_skills WHERE guildid = ? AND skilltype = ?");
                    ps.setInt(1, this.id);
                    ps.setInt(2, skill.type);
                    rs = ps.executeQuery();
                    if (rs.next()) {
                        ps = con.prepareStatement("UPDATE snail_guild_skills SET skilllevel = ?, skillval = ? WHERE guildid = ? AND skilltype = ?");
                        ps.setInt(1, skill.level);
                        ps.setInt(2, skill.val);
                        ps.setInt(3, skill.guildId);
                        ps.setInt(4, skill.type);
                        ps.executeUpdate();
                    } else {
                        ps = con.prepareStatement("INSERT INTO snail_guild_skills (guildid,skilltype,skilllevel, skillval) VALUES ( ?, ?, ?, ?)");
                        ps.setInt(1, skill.guildId);
                        ps.setInt(2, skill.type);
                        ps.setInt(3, skill.level);
                        ps.setInt(4, skill.val);
                        ps.executeUpdate();
                    }
                }

                if (this.guildSkills.isEmpty()) {
                    ps = con.prepareStatement("DELETE FROM snail_guild_skills WHERE guildid = ?");
                    ps.setInt(1, this.id);
                    ps.executeUpdate();
                }

                ps.close();
                var18 = true;
            } catch (Throwable var15) {
                var2 = var15;
                throw var15;
            } finally {
                if (con != null) {
                    if (var2 != null) {
                        try {
                            con.close();
                        } catch (Throwable var14) {
                            var2.addSuppressed(var14);
                        }
                    } else {
                        con.close();
                    }
                }

            }

            return var18;
        } catch (SQLException var17) {
            服务端输出信息.println_err("【错误】saveGuildSkillsToDB错误，错误原因：" + var17);
            var17.printStackTrace();
            return false;
        }
    }

    public class GuildSkill {
        private int type;
        private int level;
        private int val;
        private int guildId;
        private String info;

        private GuildSkill(int type, int level, int val, String info) {
            this.type = type;
            this.level = level;
            this.val = val;
            this.guildId = MapleGuild.this.id;
            this.info = info;
        }

        public int getType() {
            return this.type;
        }

        public void setType(int type) {
            this.type = type;
        }

        public int getLevel() {
            return this.level;
        }

        public void setLevel(int level) {
            this.level = level;
        }

        public int getVal() {
            return this.val;
        }

        public void setVal(int val) {
            this.val = val;
        }

        public int getGuildId() {
            return this.guildId;
        }

        public void setGuildId(int guildId) {
            this.guildId = guildId;
        }

        public String getInfo() {
            return this.info;
        }

        public void setInfo(String info) {
            this.info = info;
        }
    }

    public static class 战斗力信息 {
        private String name;
        private int chrid;
        private int level;
        private int job;
        private int str;
        private int dex;
        private int _int;
        private int luk;
        private int maxhp;
        private int maxmp;
        private long max_damage;
        private int e_str;
        private int e_dex;
        private int e_int;
        private int e_luk;
        private int e_watk;
        private int e_matk;
        private int e_hp;
        private int e_mp;
        private String potentials;
        private Map<Integer, Integer> potentialMap = new HashMap();
        private int percentForce;
        private long force;
        private long chr_attack;

        public 战斗力信息() {
        }

        public void solveChr_attack() {
            if (this.job >= 100 && this.job < 200) {
                this.chr_attack = (long)(this.str + this.e_str + (this.dex + this.e_dex) / 4) * (long)this.e_watk / 25L;
            } else if (this.job >= 200 && this.job < 300) {
                this.chr_attack = (long)(this._int + this.e_int + (this.luk + this.e_luk) / 4) * (long)this.e_matk / 25L;
            } else if (this.job >= 300 && this.job < 400) {
                this.chr_attack = (long)(this.dex + this.e_dex + (this.str + this.e_str) / 4) * (long)this.e_watk / 25L;
            } else if (this.job >= 400 && this.job < 500) {
                this.chr_attack = (long)(this.luk + this.e_luk + (this.str + this.e_str + this.dex + this.e_dex) / 4) * (long)this.e_watk / 25L;
            } else if (this.job >= 500 && this.job < 520) {
                this.chr_attack = (long)(this.str + this.e_str + (this.dex + this.e_dex) / 4) * (long)this.e_watk / 25L;
            } else if (this.job >= 520 && this.job < 600) {
                this.chr_attack = (long)(this.dex + this.e_dex + (this.str + this.e_str) / 4) * (long)this.e_watk / 25L;
            } else if (this.job >= 1000 && this.job < 1200) {
                this.chr_attack = (long)(this.str + this.e_str + (this.dex + this.e_dex) / 4) * (long)this.e_watk / 25L;
            } else if (this.job >= 1200 && this.job < 1300) {
                this.chr_attack = (long)(this._int + this.e_int + (this.luk + this.e_luk) / 4) * (long)this.e_matk / 25L;
            } else if (this.job >= 1400 && this.job < 1500) {
                this.chr_attack = (long)(this.luk + this.e_luk + (this.str + this.e_str + this.dex + this.e_dex) / 4) * (long)this.e_watk / 25L;
            } else if (this.job >= 1500 && this.job < 1600) {
                this.chr_attack = (long)(this.str + this.e_str + (this.dex + this.e_dex) / 4) * (long)this.e_watk / 25L;
            } else if (this.job >= 2000 && this.job < 2200) {
                this.chr_attack = (long)(this.str + this.e_str + (this.dex + this.e_dex) / 4) * (long)this.e_watk / 25L;
            } else if (this.job >= 2200 && this.job < 2300) {
                this.chr_attack = (long)(this._int + this.e_int + (this.luk + this.e_luk) / 4) * (long)this.e_matk / 25L;
            }

        }

        public long getChr_attack() {
            return this.chr_attack;
        }

        public void solveForce() {
            this.solveChr_attack();
            if ((Integer) LtMS.ConfigValuesMap.get("战斗力计算包含潜能开关") > 0) {
                this.solvePotentials();
            }

            this.force = (long)(this.level * 15 + this.str + this.dex + this.luk + this._int) + this.chr_attack / 2L;
            if (this.percentForce > 0) {
                this.force += (long)this.percentForce * this.force / 100L;
            }

        }

        private void solvePotentials() {
            if (this.potentials != null) {
                this.potentialMap.clear();
                if (this.potentials.contains(",")) {
                    String[] ret1 = this.potentials.split(",");
                    for(int b = 0; b < ret1.length; ++b) {
                        String a = ret1[b];
                        if (a.contains(":")) {
                           String[] aStr = a.split(":");
                            int key = Integer.parseInt(aStr[0]);
                            int value = Integer.parseInt(aStr[1]);
                            if (this.potentialMap.containsKey(key)) {
                                b = (Integer)this.potentialMap.get(key);
                                this.potentialMap.put(key, b + value);
                            } else {
                                this.potentialMap.put(key, value);
                            }
                        }
                    }
                } else {
                    if (!this.potentials.contains(":")) {
                        return;
                    }

                   String[] potential = this.potentials.split(":");
                    int k = Integer.parseInt(potential[0]);
                    int v = Integer.parseInt(potential[1]);
                    if (this.potentialMap.containsKey(k)) {
                       int b = (Integer)this.potentialMap.get(k);
                        this.potentialMap.put(k, b + v);
                    } else {
                        this.potentialMap.put(k, v);
                    }
                }

                Iterator var10 = this.potentialMap.entrySet().iterator();

                while(var10.hasNext()) {
                    Map.Entry<Integer, Integer> entry = (Map.Entry)var10.next();
                    switch ((Integer)entry.getKey()) {
                        case 1:
                            this.str += (Integer)entry.getValue();
                            break;
                        case 2:
                            this.dex += (Integer)entry.getValue();
                            break;
                        case 3:
                            this._int += (Integer)entry.getValue();
                            break;
                        case 4:
                            this.luk += (Integer)entry.getValue();
                            break;
                        case 5:
                            this.str += (Integer)entry.getValue() * this.str / 100;
                            break;
                        case 6:
                            this.dex += (Integer)entry.getValue() * this.dex / 100;
                            break;
                        case 7:
                            this._int += (Integer)entry.getValue() * this._int / 100;
                            break;
                        case 8:
                            this.luk += (Integer)entry.getValue() * this.luk / 100;
                            break;
                        case 9:
                            this.str += (Integer)entry.getValue();
                            this.dex += (Integer)entry.getValue();
                            this._int += (Integer)entry.getValue();
                            this.luk += (Integer)entry.getValue();
                            break;
                        case 10:
                            this.str += (Integer)entry.getValue() * this.str / 100;
                            this.dex += (Integer)entry.getValue() * this.dex / 100;
                            this._int += (Integer)entry.getValue() * this._int / 100;
                            this.luk += (Integer)entry.getValue() * this.luk / 100;
                            break;
                        case 11:
                        case 12:
                            this.chr_attack += (long)(Integer)entry.getValue();
                            break;
                        case 13:
                        case 14:
                            this.chr_attack += (long)(Integer)entry.getValue() * this.chr_attack / 100L;
                        case 15:
                        case 16:
                        case 17:
                        case 18:
                        case 19:
                        case 20:
                        case 21:
                        case 22:
                        case 23:
                        case 24:
                        case 25:
                        case 26:
                        case 27:
                        case 28:
                        default:
                            break;
                        case 29:
                            this.percentForce += (Integer)entry.getValue();
                            break;
                        case 30:
                            this.percentForce = (int)((double)this.percentForce + (double)(Integer)entry.getValue() * 0.5);
                            break;
                        case 31:
                            this.percentForce = (int)((double)this.percentForce + (double)(Integer)entry.getValue() * 0.3);
                    }
                }

            }
        }

        public long getForce() {
            return this.force;
        }

        public int getChrid() {
            return this.chrid;
        }

        public String getName() {
            return this.name;
        }

        public int getLevel() {
            return this.level;
        }

        public int getJob() {
            return this.job;
        }

        public int getStr() {
            return this.str;
        }

        public int getDex() {
            return this.dex;
        }

        public int getInt() {
            return this._int;
        }

        public int getLuk() {
            return this.luk;
        }

        public int getMaxhp() {
            return this.maxhp;
        }

        public int getMaxmp() {
            return this.maxmp;
        }

        public long getMax_damage() {
            return this.max_damage;
        }

        public void setChrid(int chrid) {
            this.chrid = chrid;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setLevel(int level) {
            this.level = level;
        }

        public void setJob(int job) {
            this.job = job;
        }

        public void setStr(int str) {
            this.str = str;
        }

        public void setDex(int dex) {
            this.dex = dex;
        }

        public void setInt(int _int) {
            this._int = _int;
        }

        public void setLuk(int luk) {
            this.luk = luk;
        }

        public void setMaxhp(int maxhp) {
            this.maxhp = maxhp;
        }

        public void setMaxmp(int maxmp) {
            this.maxmp = maxmp;
        }

        public void setMax_damage(long max_damage) {
            this.max_damage = max_damage;
        }

        public void setE_str(int e_str) {
            this.e_str = e_str;
        }
        public int getE_str() {
            return this.e_str;
        }

        public void setE_dex(int e_dex) {
            this.e_dex = e_dex;
        }

        public int getE_dex() {
            return this.e_dex;
        }

        public void setE_int(int e_int) {
            this.e_int = e_int;
        }

        public int getE_int() {
            return this.e_int;
        }

        public void setE_luk(int e_luk) {
            this.e_luk = e_luk;
        }

        public int getE_luk() {
            return this.e_luk;
        }

        public void setE_watk(int e_watk) {
            this.e_watk = e_watk;
        }

        public int getE_watk() {
            return this.e_watk;
        }

        public void setE_matk(int e_matk) {
            this.e_matk = e_matk;
        }

        public int getE_matk() {
            return this.e_matk;
        }

        public void setE_hp(int e_hp) {
            this.e_hp = e_hp;
        }

        public int getE_hp() {
            return this.e_hp;
        }

        public void setE_mp(int e_mp) {
            this.e_mp = e_mp;
        }

        public int getE_mp() {
            return this.e_mp;
        }

        public String getPotentials() {
            return this.potentials;
        }

        public void setPotentials(String potentials) {
            this.potentials = potentials;
        }
    }

    private static enum BCOp {
        NONE,
        DISBAND,
        EMBELMCHANGE;

        private BCOp() {
        }
    }
}
