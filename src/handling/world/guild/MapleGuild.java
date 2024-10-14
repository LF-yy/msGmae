package handling.world.guild;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import client.MapleCharacter;
import client.MapleCharacterUtil;
import client.MapleClient;
import database.DBConPool;
import database.DatabaseConnection;
import gui.LtMS;
import handling.world.World.Alliance;
import handling.world.World.Broadcast;
import handling.world.World.Guild;
import handling.world.guild.MapleBBSThread.MapleBBSReply;
import handling.world.guild.MapleBBSThread.ThreadComparator;
import server.Start;
import tools.FileoutputUtil;
import tools.MaplePacketCreator;
import tools.data.MaplePacketLittleEndianWriter;
import tools.packet.UIPacket;

public class MapleGuild implements Serializable
{
    public static long serialVersionUID = 6322150443228168192L;
    private final List<MapleGuildCharacter> members;
    private final String[] rankTitles;
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
    private boolean bDirty;
    private boolean proper;
    private int allianceid;
    private int invitedid;
    private final Map<Integer, MapleBBSThread> bbs;
    private final ReentrantReadWriteLock lock;
    private final Lock rL;
    private final Lock wL;
    private boolean init;
    
    public MapleGuild(final int guildid) {
        this.members = new CopyOnWriteArrayList<MapleGuildCharacter>();
        this.rankTitles = new String[5];
        this.bDirty = true;
        this.proper = true;
        this.allianceid = 0;
        this.invitedid = 0;
        this.bbs = new HashMap<Integer, MapleBBSThread>();
        this.lock = new ReentrantReadWriteLock();
        this.rL = this.lock.readLock();
        this.wL = this.lock.writeLock();
        this.init = false;
        try (Connection con = (Connection)DBConPool.getInstance().getDataSource().getConnection()) {
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
            if (!rs.first()) {
                System.err.println("公会ID：" + this.id + " 沒有成員，系統自動解散公会。");
                rs.close();
                ps.close();
                this.writeToDB(true);
                this.proper = false;
                return;
            }
            boolean leaderCheck = false;
            do {
                if (rs.getInt("id") == this.leader) {
                    leaderCheck = true;
                }
                this.members.add(new MapleGuildCharacter(rs.getInt("id"), rs.getShort("level"), rs.getString("name"), (byte)(-1), rs.getInt("job"), rs.getByte("guildrank"), rs.getByte("alliancerank"), guildid, false));
            } while (rs.next());
            rs.close();
            ps.close();
            if (!leaderCheck) {
                System.err.println("会長[" + this.leader + "]沒有在公会ID為" + this.id + "的公会中，系統自動解散這個公会。");
                this.writeToDB(true);
                this.proper = false;
                return;
            }
            ps = con.prepareStatement("SELECT * FROM bbs_threads WHERE guildid = ? ORDER BY localthreadid DESC");
            ps.setInt(1, guildid);
            rs = ps.executeQuery();
            while (rs.next()) {
                final MapleBBSThread thread = new MapleBBSThread(rs.getInt("localthreadid"), rs.getString("name"), rs.getString("startpost"), rs.getLong("timestamp"), guildid, rs.getInt("postercid"), rs.getInt("icon"));
                try (final PreparedStatement pse = con.prepareStatement("SELECT * FROM bbs_replies WHERE threadid = ?")) {
                    pse.setInt(1, rs.getInt("threadid"));
                    final ResultSet rse = pse.executeQuery();
                    while (rse.next()) {
                        thread.replies.put(Integer.valueOf(thread.replies.size()), new MapleBBSReply(thread.replies.size(), rse.getInt("postercid"), rse.getString("content"), rse.getLong("timestamp")));
                    }
                    rse.close();
                }
                this.bbs.put(Integer.valueOf(rs.getInt("localthreadid")), thread);
            }
            rs.close();
            ps.close();
        }
        catch (SQLException se) {
            System.err.println("unable to read guild information from sql");
            FileoutputUtil.outError("logs/資料庫異常.txt", (Throwable)se);
        }
    }
    
    public boolean isProper() {
        return this.proper;
    }
    
    public static Collection<MapleGuild> loadAll() {
        final Collection<MapleGuild> ret = new ArrayList<MapleGuild>();
        try (Connection con = (Connection)DBConPool.getInstance().getDataSource().getConnection();
             final PreparedStatement ps = con.prepareStatement("SELECT guildid FROM guilds");
             final ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                final MapleGuild g = new MapleGuild(rs.getInt("guildid"));
                if (g.getId() > 0) {
                    ret.add(g);
                }
            }
        }
        catch (SQLException se) {
            System.err.println("unable to read guild information from sql");
            FileoutputUtil.outError("logs/資料庫異常.txt", (Throwable)se);
        }
        return ret;
    }
    
    public void writeToDB(final boolean bDisband) {
        try (Connection con = (Connection)DBConPool.getInstance().getDataSource().getConnection()) {
            if (!bDisband) {
                final StringBuilder buf = new StringBuilder("UPDATE guilds SET GP = ?, logo = ?, logoColor = ?, logoBG = ?, logoBGColor = ?, ");
                for (int i = 1; i < 6; ++i) {
                    buf.append("rank").append(i).append("title = ?, ");
                }
                buf.append("capacity = ?, notice = ?, alliance = ? WHERE guildid = ?");
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
                ps.setInt(14, this.id);
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
                for (final MapleBBSThread bb : this.bbs.values()) {
                    ps.setInt(1, bb.ownerID);
                    ps.setString(2, bb.name);
                    ps.setLong(3, bb.timestamp);
                    ps.setInt(4, bb.icon);
                    ps.setString(5, bb.text);
                    ps.setInt(7, bb.localthreadID);
                    ps.executeUpdate();
                    try (final ResultSet rs = ps.getGeneratedKeys()) {
                        if (!rs.next()) {
                            rs.close();
                        }
                        else {
                            try (final PreparedStatement pse = con.prepareStatement("INSERT INTO bbs_replies (`threadid`, `postercid`, `timestamp`, `content`, `guildid`) VALUES (?, ?, ?, ?, ?)")) {
                                pse.setInt(5, this.id);
                                for (final MapleBBSReply r : bb.replies.values()) {
                                    pse.setInt(1, rs.getInt(1));
                                    pse.setInt(2, r.ownerID);
                                    pse.setLong(3, r.timestamp);
                                    pse.setString(4, r.content);
                                    pse.execute();
                                }
                            }
                        }
                    }
                }
                ps.close();
            }
            else {
                PreparedStatement ps2 = con.prepareStatement("UPDATE characters SET guildid = 0, guildrank = 5, alliancerank = 5 WHERE guildid = ?");
                ps2.setInt(1, this.id);
                ps2.execute();
                ps2.close();
                ps2 = con.prepareStatement("DELETE FROM bbs_threads WHERE guildid = ?");
                ps2.setInt(1, this.id);
                ps2.execute();
                ps2.close();
                ps2 = con.prepareStatement("DELETE FROM bbs_replies WHERE guildid = ?");
                ps2.setInt(1, this.id);
                ps2.execute();
                ps2.close();
                ps2 = con.prepareStatement("DELETE FROM guilds WHERE guildid = ?");
                ps2.setInt(1, this.id);
                ps2.execute();
                ps2.close();
                if (this.allianceid > 0) {
                    final MapleGuildAlliance alliance = Alliance.getAlliance(this.allianceid);
                    if (alliance != null) {
                        alliance.removeGuild(this.id, false);
                    }
                }
                this.broadcast(MaplePacketCreator.guildDisband(this.id));
            }
        }
        catch (SQLException se) {
            System.err.println("Error saving guild to SQL");
            FileoutputUtil.outError("logs/資料庫異常.txt", (Throwable)se);
        }
    }
    
    public final int getId() {
        return this.id;
    }
    
    public final int getLeaderId() {
        return this.leader;
    }
    
    public final MapleCharacter getLeader(final MapleClient c) {
        return c.getChannelServer().getPlayerStorage().getCharacterById(this.leader);
    }
    
    public final int getGP() {
        return this.gp;
    }
    
    public final int getLogo() {
        return this.logo;
    }
    
    public void setLogo(final int l) {
        this.logo = l;
    }
    
    public final int getLogoColor() {
        return this.logoColor;
    }
    
    public void setLogoColor(final int c) {
        this.logoColor = c;
    }
    
    public final int getLogoBG() {
        return this.logoBG;
    }
    
    public void setLogoBG(final int bg) {
        this.logoBG = bg;
    }
    
    public final int getLogoBGColor() {
        return this.logoBGColor;
    }
    
    public void setLogoBGColor(final int bgColor) {
        this.logoBGColor = bgColor;
    }
    
    public final String getNotice() {
        if (this.notice == null) {
            return "";
        }
        return this.notice;
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
    
    public void broadcast(final byte[] packet) {
        this.broadcast(packet, -1, BCOp.NONE);
    }
    
    public void broadcast(final byte[] packet, final int exception) {
        this.broadcast(packet, exception, BCOp.NONE);
    }
    
    public void broadcast(final byte[] packet, final int exceptionId, final BCOp bcop) {
        this.wL.lock();
        try {
            this.buildNotifications();
        }
        finally {
            this.wL.unlock();
        }
        this.rL.lock();
        try {
            for (final MapleGuildCharacter mgc : this.members) {
                if (bcop == BCOp.DISBAND) {
                    if (mgc.isOnline()) {
                        Guild.setGuildAndRank(mgc.getId(), 0, 5, 5);
                    }
                    else {
                        saveCharacterGuildInfo(0, (byte)5, (byte)5, mgc.getId());
                    }
                }
                else {
                    if (!mgc.isOnline() || mgc.getId() == exceptionId) {
                        continue;
                    }
                    if (bcop == BCOp.EMBELMCHANGE) {
                        Guild.changeEmblem(this.id, mgc.getId(), new MapleGuildSummary(this));
                    }
                    else {
                        Broadcast.sendGuildPacket(mgc.getId(), packet, exceptionId, this.id);
                    }
                }
            }
        }
        finally {
            this.rL.unlock();
        }
    }
    
    private void buildNotifications() {
        if (!this.bDirty) {
            return;
        }
        final List<Integer> mem = new LinkedList<Integer>();
        for (final MapleGuildCharacter mgc : this.members) {
            if (!mgc.isOnline()) {
                continue;
            }
            if (mem.contains((Object)Integer.valueOf(mgc.getId())) || mgc.getGuildId() != this.id) {
                this.members.remove((Object)mgc);
            }
            else {
                mem.add(Integer.valueOf(mgc.getId()));
            }
        }
        this.bDirty = false;
    }
    
    public void setOnline(final int cid, final boolean online, final int channel) {
        boolean bBroadcast = true;
        for (final MapleGuildCharacter mgc : this.members) {
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
    
    public void guildChat(final String name, final int cid, final String msg) {
        this.broadcast(MaplePacketCreator.multiChat(name, msg, 2), cid);
    }
    
    public void allianceChat(final String name, final int cid, final String msg) {
        this.broadcast(MaplePacketCreator.multiChat(name, msg, 3), cid);
    }
    
    public final String getRankTitle(final int rank) {
        return this.rankTitles[rank - 1];
    }
    
    public int getAllianceId() {
        return this.allianceid;
    }
    
    public int getInvitedId() {
        return this.invitedid;
    }
    
    public void setInvitedId(final int iid) {
        this.invitedid = iid;
    }
    
    public void setAllianceId(final int a) {
        this.allianceid = a;
        try (Connection con = (Connection)DBConPool.getInstance().getDataSource().getConnection();
             final PreparedStatement ps = con.prepareStatement("UPDATE guilds SET alliance = ? WHERE guildid = ?")) {
            ps.setInt(1, a);
            ps.setInt(2, this.id);
            ps.execute();
        }
        catch (SQLException e) {
            System.err.println("Saving allianceid ERROR" + (Object)e);
            FileoutputUtil.outError("logs/資料庫異常.txt", (Throwable)e);
        }
    }
    
    public static int createGuild(final int leaderId, final String name) {
        if (name.length() > 12) {
            return 0;
        }
        try (Connection con = (Connection)DBConPool.getInstance().getDataSource().getConnection()) {
            PreparedStatement ps = con.prepareStatement("SELECT guildid FROM guilds WHERE name = ?");
            ps.setString(1, name);
            ResultSet rs = ps.executeQuery();
            if (rs.first()) {
                rs.close();
                ps.close();
                return 0;
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
            return ret;
        }
        catch (SQLException se) {
            System.err.println("SQL THROW");
            FileoutputUtil.outError("logs/資料庫異常.txt", (Throwable)se);
            return 0;
        }
    }
    
    public final int addGuildMember(final MapleGuildCharacter mgc) {
        return this.addGuildMember(mgc, true);
    }
    
    public final int addGuildMember(final MapleGuildCharacter mgc, final boolean show) {
        this.wL.lock();
        try {
            if (this.members.size() >= this.capacity) {
                return 0;
            }
            for (int i = this.members.size() - 1; i >= 0; --i) {
                if (((MapleGuildCharacter)this.members.get(i)).getGuildRank() < 5 || ((MapleGuildCharacter)this.members.get(i)).getName().compareTo(mgc.getName()) < 0) {
                    this.members.add(i + 1, mgc);
                    this.bDirty = true;
                    break;
                }
            }
        }
        finally {
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
    
    public void leaveGuild(final MapleGuildCharacter mgc) {
        this.wL.lock();
        try {
            for (final MapleGuildCharacter mgcc : this.members) {
                if (mgcc.getId() == mgc.getId()) {
                    this.broadcast(MaplePacketCreator.memberLeft(mgcc, true));
                    this.bDirty = true;
                    this.members.remove((Object)mgcc);
                    if (mgc.isOnline()) {
                        Guild.setGuildAndRank(mgcc.getId(), 0, 5, 5);
                    }
                    else {
                        saveCharacterGuildInfo(0, (byte)5, (byte)5, mgcc.getId());
                    }
                    if (this.allianceid <= 0) {
                        continue;
                    }
                    Alliance.sendGuild(this.allianceid);
                }
            }
        }
        finally {
            this.wL.unlock();
        }
    }
    
    public void expelMember(final MapleGuildCharacter initiator, final String name, final int cid) {
        this.wL.lock();
        try {
            for (final MapleGuildCharacter mgc : this.members) {
                if (mgc.getId() == cid && initiator.getGuildRank() < mgc.getGuildRank()) {
                    this.broadcast(MaplePacketCreator.memberLeft(mgc, true));
                    this.bDirty = true;
                    if (this.allianceid > 0) {
                        Alliance.sendGuild(this.allianceid);
                    }
                    if (mgc.isOnline()) {
                        Guild.setGuildAndRank(cid, 0, 5, 5);
                    }
                    else {
                        MapleCharacterUtil.sendNote(mgc.getName(), initiator.getName(), "你已經被公会踢出.", 0);
                        saveCharacterGuildInfo(0, (byte)5, (byte)5, cid);
                    }
                    this.members.remove((Object)mgc);
                    break;
                }
            }
        }
        finally {
            this.wL.unlock();
        }
    }
    
    public void changeARank() {
        this.changeARank(false);
    }
    
    public void changeARank(final boolean leader) {
        for (final MapleGuildCharacter mgc : this.members) {
            if (this.leader == mgc.getId()) {
                this.changeARank(mgc.getId(), leader ? 1 : 2);
            }
            else {
                this.changeARank(mgc.getId(), 3);
            }
        }
    }
    
    public void changeARank(final int newRank) {
        for (final MapleGuildCharacter mgc : this.members) {
            this.changeARank(mgc.getId(), newRank);
        }
    }
    
    public void changeARank(final int cid, final int newRank) {
        if (this.allianceid <= 0) {
            return;
        }
        for (final MapleGuildCharacter mgc : this.members) {
            if (cid == mgc.getId()) {
                if (mgc.isOnline()) {
                    Guild.setGuildAndRank(cid, this.id, (int)mgc.getGuildRank(), newRank);
                }
                else {
                    saveCharacterGuildInfo((int)(short)this.id, mgc.getGuildRank(), (byte)newRank, cid);
                }
                mgc.setAllianceRank((byte)newRank);
                Alliance.sendGuild(this.allianceid);
                return;
            }
        }
        System.err.println("INFO: unable to find the correct id for changeRank({" + cid + "}, {" + newRank + "})");
    }
    
    public void changeRank(final int cid, final int newRank) {
        for (final MapleGuildCharacter mgc : this.members) {
            if (cid == mgc.getId()) {
                if (mgc.isOnline()) {
                    Guild.setGuildAndRank(cid, this.id, newRank, (int)mgc.getAllianceRank());
                }
                else {
                    saveCharacterGuildInfo((int)(short)this.id, (byte)newRank, mgc.getAllianceRank(), cid);
                }
                mgc.setGuildRank((byte)newRank);
                this.broadcast(MaplePacketCreator.changeRank(mgc));
                return;
            }
        }
        System.err.println("INFO: unable to find the correct id for changeRank({" + cid + "}, {" + newRank + "})");
    }
    
    public void setGuildNotice(final String notice) {
        this.notice = notice;
        this.broadcast(MaplePacketCreator.guildNotice(this.id, notice));
    }
    
    public void memberLevelJobUpdate(final MapleGuildCharacter mgc) {
        for (final MapleGuildCharacter member : this.members) {
            if (member.getId() == mgc.getId()) {
                final int old_level = member.getLevel();
                final int old_job = member.getJobId();
                member.setJobId(mgc.getJobId());
                member.setLevel((short)mgc.getLevel());
                if (mgc.getLevel() > old_level) {}
                if (old_level != mgc.getLevel()) {
                    this.broadcast(MaplePacketCreator.sendLevelup(false, mgc.getLevel(), mgc.getName()), mgc.getId());
                }
                if (old_job != mgc.getJobId()) {
                    this.broadcast(MaplePacketCreator.sendJobup(false, mgc.getJobId(), mgc.getName()), mgc.getId());
                }
                this.broadcast(MaplePacketCreator.guildMemberLevelJobUpdate(mgc));
                if (this.allianceid > 0) {
                    Alliance.sendGuild(MaplePacketCreator.updateAlliance(mgc, this.allianceid), this.id, this.allianceid);
                    break;
                }
                break;
            }
        }
    }
    
    public void changeRankTitle(final String[] ranks) {
        System.arraycopy((Object)ranks, 0, (Object)this.rankTitles, 0, 5);
        this.broadcast(MaplePacketCreator.rankTitleChange(this.id, ranks));
    }
    
    public void disbandGuild() {
        this.writeToDB(true);
        this.broadcast(null, -1, BCOp.DISBAND);
    }
    
    public void setGuildEmblem(final short bg, final byte bgcolor, final short logo, final byte logocolor) {
        this.logoBG = bg;
        this.logoBGColor = bgcolor;
        this.logo = logo;
        this.logoColor = logocolor;
        this.broadcast(null, -1, BCOp.EMBELMCHANGE);
        try (Connection con = (Connection)DBConPool.getInstance().getDataSource().getConnection();
             final PreparedStatement ps = con.prepareStatement("UPDATE guilds SET logo = ?, logoColor = ?, logoBG = ?, logoBGColor = ? WHERE guildid = ?")) {
            ps.setInt(1, (int)logo);
            ps.setInt(2, this.logoColor);
            ps.setInt(3, this.logoBG);
            ps.setInt(4, this.logoBGColor);
            ps.setInt(5, this.id);
            ps.execute();
        }
        catch (SQLException e) {
            System.err.println("Saving guild logo / BG colo ERROR");
            FileoutputUtil.outError("logs/資料庫異常.txt", (Throwable)e);
        }
    }
    
    public final MapleGuildCharacter getMGC(final int cid) {
        for (final MapleGuildCharacter mgc : this.members) {
            if (mgc.getId() == cid) {
                return mgc;
            }
        }
        return null;
    }
    
    public final boolean increaseCapacity() {
        if (this.capacity >= 100 || this.capacity + 5 > 100) {
            return false;
        }
        this.capacity += 5;
        this.broadcast(MaplePacketCreator.guildCapacityChange(this.id, this.capacity));
        try (Connection con = (Connection)DBConPool.getInstance().getDataSource().getConnection();
             final PreparedStatement ps = con.prepareStatement("UPDATE guilds SET capacity = ? WHERE guildid = ?")) {
            ps.setInt(1, this.capacity);
            ps.setInt(2, this.id);
            ps.execute();
        }
        catch (SQLException e) {
            System.err.println("Saving guild capacity ERROR");
            FileoutputUtil.outError("logs/資料庫異常.txt", (Throwable)e);
        }
        return true;
    }
    
    public void gainGP(final int amount) {
        this.gainGP(amount, true);
    }
    
    public void gainGP(int amount, final boolean broadcast) {
        if (amount == 0) {
            return;
        }
        if (amount + this.gp < 0) {
            amount = -this.gp;
        }
        this.gp += amount;
        this.broadcast(MaplePacketCreator.updateGP(this.id, this.gp));
        if (broadcast) {
            this.broadcast(UIPacket.getGPMsg(amount));
        }
    }
    
    public void addMemberData(final MaplePacketLittleEndianWriter mplew) {
        mplew.write(this.members.size());
        for (final MapleGuildCharacter mgc : this.members) {
            mplew.writeInt(mgc.getId());
        }
        for (final MapleGuildCharacter mgc : this.members) {
            mplew.writeAsciiString(mgc.getName(), 13);
            mplew.writeInt(mgc.getJobId());
            mplew.writeInt(mgc.getLevel());
            mplew.writeInt((int)mgc.getGuildRank());
            mplew.writeInt((int)(mgc.isOnline() ? 1 : 0));
            mplew.writeInt(this.signature);
            mplew.writeInt((int)mgc.getAllianceRank());
        }
    }
    
    public static MapleGuildResponse sendInvite(final MapleClient c, final String targetName) {
        final MapleCharacter mc = c.getChannelServer().getPlayerStorage().getCharacterByName(targetName);
        if (mc == null) {
            return MapleGuildResponse.NOT_IN_CHANNEL;
        }
        if (mc.getGuildId() > 0) {
            return MapleGuildResponse.ALREADY_IN_GUILD;
        }
        mc.getClient().sendPacket(MaplePacketCreator.guildInvite(c.getPlayer().getGuildId(), c.getPlayer().getName(), (int)c.getPlayer().getLevel(), (int)c.getPlayer().getJob()));
        return null;
    }
    
    public Collection<MapleGuildCharacter> getMembers() {
        return Collections.unmodifiableCollection((Collection<? extends MapleGuildCharacter>)this.members);
    }
    
    public final boolean isInit() {
        return this.init;
    }
    
    public final List<MapleBBSThread> getBBS() {
        final List<MapleBBSThread> ret = new ArrayList<MapleBBSThread>((Collection<? extends MapleBBSThread>)this.bbs.values());
        Collections.sort(ret, (Comparator<? super MapleBBSThread>)new ThreadComparator());
        return ret;
    }
    
    public final int addBBSThread(final String title, final String text, final int icon, final boolean bNotice, final int posterID) {
        final int add = (this.bbs.get((Object)Integer.valueOf(0)) == null) ? 1 : 0;
        final int ret = bNotice ? 0 : Math.max(1, this.bbs.size() + add);
        this.bbs.put(Integer.valueOf(ret), new MapleBBSThread(ret, title, text, System.currentTimeMillis(), this.id, posterID, icon));
        return ret;
    }
    
    public void editBBSThread(final int localthreadid, final String title, final String text, final int icon, final int posterID, final int guildRank) {
        final MapleBBSThread thread = (MapleBBSThread)this.bbs.get((Object)Integer.valueOf(localthreadid));
        if (thread != null && (thread.ownerID == posterID || guildRank <= 2)) {
            this.bbs.put(Integer.valueOf(localthreadid), new MapleBBSThread(localthreadid, title, text, System.currentTimeMillis(), this.id, thread.ownerID, icon));
        }
    }
    
    public void deleteBBSThread(final int localthreadid, final int posterID, final int guildRank) {
        final MapleBBSThread thread = (MapleBBSThread)this.bbs.get((Object)Integer.valueOf(localthreadid));
        if (thread != null && (thread.ownerID == posterID || guildRank <= 2)) {
            this.bbs.remove((Object)Integer.valueOf(localthreadid));
        }
    }
    
    public void addBBSReply(final int localthreadid, final String text, final int posterID) {
        final MapleBBSThread thread = (MapleBBSThread)this.bbs.get((Object)Integer.valueOf(localthreadid));
        if (thread != null) {
            thread.replies.put(Integer.valueOf(thread.replies.size()), new MapleBBSReply(thread.replies.size(), posterID, text, System.currentTimeMillis()));
        }
    }
    
    public void deleteBBSReply(final int localthreadid, final int replyid, final int posterID, final int guildRank) {
        final MapleBBSThread thread = (MapleBBSThread)this.bbs.get((Object)Integer.valueOf(localthreadid));
        if (thread != null) {
            final MapleBBSReply reply = (MapleBBSReply)thread.replies.get((Object)Integer.valueOf(replyid));
            if (reply != null && (reply.ownerID == posterID || guildRank <= 2)) {
                thread.replies.remove((Object)Integer.valueOf(replyid));
            }
        }
    }
    
    public static void saveCharacterGuildInfo(final int guildid, final byte guildrank, final byte alliancerank, final int cid) {
        try (Connection con = (Connection)DBConPool.getInstance().getDataSource().getConnection()) {
            final PreparedStatement ps = con.prepareStatement("UPDATE characters SET guildid = ?, guildrank = ?, alliancerank = ? WHERE id = ?");
            ps.setInt(1, guildid);
            ps.setInt(2, (int)guildrank);
            ps.setInt(3, (int)alliancerank);
            ps.setInt(4, cid);
            ps.executeUpdate();
            ps.close();
        }
        catch (SQLException se) {
            System.err.println("SQLException: " + se.getLocalizedMessage());
            FileoutputUtil.outError("logs/資料庫異常.txt", (Throwable)se);
        }
    }
    
    public String getPrefix(MapleCharacter chr) {
        return chr.getPrefix();
    }
    
    public static void 战斗力排行(final MapleClient c, final int npcid) {
        try {
            Connection con = DatabaseConnection.getConnection();
            ResultSet rs;
            try (final PreparedStatement ps = con.prepareStatement("select `name`, ((`level` * 15) + CASE WHEN fame < 0 THEN 0 ELSE fame END + (maxhp / 50) + (maxmp / 50) + str + dex + luk + `int`) AS `data`, `level`, meso from characters order by `data` desc LIMIT 100")) {
                rs = ps.executeQuery();
                c.sendPacket(MaplePacketCreator.showCustomRanks(npcid, rs));
            }
            rs.close();
        }
        catch (SQLException e) {
            System.err.println("战斗力排行出错！");
        }
    }
    
    public static void 总在线时间排行(final MapleClient c, final int npcid) {
        try {
            Connection con = DatabaseConnection.getConnection();
            ResultSet rs;
            try (final PreparedStatement ps = con.prepareStatement("select `name`, totalOnlineTime AS `data`, `level`, meso from characters order by `data` desc LIMIT 10")) {
                rs = ps.executeQuery();
                c.sendPacket(MaplePacketCreator.showCustomRanks(npcid, rs));
            }
            rs.close();
        }
        catch (SQLException e) {
            System.err.println("总在线时间排行出错！");
        }
    }
    
    public static void 声望排行(final MapleClient c, final int npcid) {
        try {
            Connection con = DatabaseConnection.getConnection();
            ResultSet rs;
            try (final PreparedStatement ps = con.prepareStatement("select `name`, totalrep AS `data`, `level`, meso from characters order by `data` desc LIMIT 10")) {
                rs = ps.executeQuery();
                c.sendPacket(MaplePacketCreator.showCustomRanks(npcid, rs));
            }
            rs.close();
        }
        catch (SQLException e) {
            System.err.println("人气排行出错！");
        }
    }
    
    public static void 人气排行(final MapleClient c, final int npcid) {
        try {
            Connection con = DatabaseConnection.getConnection();
            ResultSet rs;
            try (final PreparedStatement ps = con.prepareStatement("select `name`, fame AS `data`, `level`, meso from characters order by `data` desc LIMIT 10")) {
                rs = ps.executeQuery();
                c.sendPacket(MaplePacketCreator.showCustomRanks(npcid, rs));
            }
            rs.close();
        }
        catch (SQLException e) {
            System.err.println("人气排行出错！");
        }
    }
    
    public static void 豆豆排行(final MapleClient c, final int npcid) {
        try {
            Connection con = DatabaseConnection.getConnection();
            ResultSet rs;
            try (final PreparedStatement ps = con.prepareStatement("select `name`, beans AS `data`, `level`, meso from characters order by `data` desc LIMIT 10")) {
                rs = ps.executeQuery();
                c.sendPacket(MaplePacketCreator.showCustomRanks(npcid, rs));
            }
            rs.close();
        }
        catch (SQLException e) {
            System.err.println("豆豆排行出错！");
        }
    }
    
    private enum BCOp
    {
        NONE, 
        DISBAND, 
        EMBELMCHANGE;
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
                String[] ret1;
                int v;
                int b;
                if (this.potentials.contains(",")) {
                    ret1 = this.potentials.split(",");
                    String[] var2 = ret1;
                    v = ret1.length;

                    for(b = 0; b < v; ++b) {
                        String a = var2[b];
                        if (a.contains(":")) {
                            ret1 = a.split(":");
                            int k = Integer.parseInt(ret1[0]);
                            v = Integer.parseInt(ret1[1]);
                            if (this.potentialMap.containsKey(k)) {
                                b = (Integer)this.potentialMap.get(k);
                                this.potentialMap.put(k, b + v);
                            } else {
                                this.potentialMap.put(k, v);
                            }
                        }
                    }
                } else {
                    if (!this.potentials.contains(":")) {
                        return;
                    }

                    ret1 = this.potentials.split(":");
                    int k = Integer.parseInt(ret1[0]);
                    v = Integer.parseInt(ret1[1]);
                    if (this.potentialMap.containsKey(k)) {
                        b = (Integer)this.potentialMap.get(k);
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
}
