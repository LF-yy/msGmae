package handling.world.family;

import tools.MaplePacketCreator;
import client.MapleCharacter;
import tools.packet.FamilyPacket;
import java.util.Map.Entry;
import handling.world.World.Broadcast;
import handling.world.World.Family;
import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.sql.Connection;
import java.sql.SQLException;
import tools.FileoutputUtil;
import database.DBConPool;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.io.Serializable;

public final class MapleFamily implements Serializable
{
    public static long serialVersionUID = 6322150443228168192L;
    private final Map<Integer, MapleFamilyCharacter> members;
    private String leadername;
    private String notice;
    private int id;
    private int leaderid;
    private int generations;
    private boolean proper;
    private boolean bDirty;
    private boolean changed;
    
    public MapleFamily(final int fid) {
        this.members = new ConcurrentHashMap<Integer, MapleFamilyCharacter>();
        this.leadername = null;
        this.generations = 0;
        this.proper = true;
        this.bDirty = false;
        this.changed = false;
        try (Connection con = (Connection)DBConPool.getInstance().getDataSource().getConnection()) {
            PreparedStatement ps = con.prepareStatement("SELECT * FROM families WHERE familyid = ?");
            ps.setInt(1, fid);
            ResultSet rs = ps.executeQuery();
            if (!rs.first()) {
                rs.close();
                ps.close();
                this.id = -1;
                return;
            }
            this.id = fid;
            this.leaderid = rs.getInt("leaderid");
            this.notice = rs.getString("notice");
            rs.close();
            ps.close();
            ps = con.prepareStatement("SELECT id, name, level, job, seniorid, junior1, junior2, currentrep, totalrep FROM characters WHERE familyid = ?");
            ps.setInt(1, fid);
            rs = ps.executeQuery();
            while (rs.next()) {
                if (rs.getInt("id") == this.leaderid) {
                    this.leadername = rs.getString("name");
                }
                this.members.put(Integer.valueOf(rs.getInt("id")), new MapleFamilyCharacter(rs.getInt("id"), (int)rs.getShort("level"), rs.getString("name"), -1, rs.getInt("job"), fid, rs.getInt("seniorid"), rs.getInt("junior1"), rs.getInt("junior2"), rs.getInt("currentrep"), rs.getInt("totalrep"), false));
            }
            rs.close();
            ps.close();
            if (this.leadername == null || this.members.size() < 2) {
                System.err.println("Leader " + this.leaderid + " isn't in family " + this.id + ".  Impossible... family is disbanding.");
                this.writeToDB(true);
                this.proper = false;
                return;
            }
            for (final MapleFamilyCharacter mfc : this.members.values()) {
                if (mfc.getJunior1() > 0 && (this.getMFC(mfc.getJunior1()) == null || mfc.getId() == mfc.getJunior1())) {
                    mfc.setJunior1(0);
                }
                if (mfc.getJunior2() > 0 && (this.getMFC(mfc.getJunior2()) == null || mfc.getId() == mfc.getJunior2() || mfc.getJunior1() == mfc.getJunior2())) {
                    mfc.setJunior2(0);
                }
                if (mfc.getSeniorId() > 0 && (this.getMFC(mfc.getSeniorId()) == null || mfc.getId() == mfc.getSeniorId())) {
                    mfc.setSeniorId(0);
                }
                if (mfc.getJunior2() > 0 && mfc.getJunior1() <= 0) {
                    mfc.setJunior1(mfc.getJunior2());
                    mfc.setJunior2(0);
                }
                if (mfc.getJunior1() > 0) {
                    final MapleFamilyCharacter mfc2 = this.getMFC(mfc.getJunior1());
                    if (mfc2.getJunior1() == mfc.getId()) {
                        mfc2.setJunior1(0);
                    }
                    if (mfc2.getJunior2() == mfc.getId()) {
                        mfc2.setJunior2(0);
                    }
                    if (mfc2.getSeniorId() != mfc.getId()) {
                        mfc2.setSeniorId(mfc.getId());
                    }
                }
                if (mfc.getJunior2() > 0) {
                    final MapleFamilyCharacter mfc2 = this.getMFC(mfc.getJunior2());
                    if (mfc2.getJunior1() == mfc.getId()) {
                        mfc2.setJunior1(0);
                    }
                    if (mfc2.getJunior2() == mfc.getId()) {
                        mfc2.setJunior2(0);
                    }
                    if (mfc2.getSeniorId() == mfc.getId()) {
                        continue;
                    }
                    mfc2.setSeniorId(mfc.getId());
                }
            }
            this.resetPedigree();
            this.resetDescendants();
            this.resetGens();
        }
        catch (SQLException se) {
            //System.err.println("unable to read family information from sql");
            FileoutputUtil.outError("logs/资料库异常.txt", (Throwable)se);
            //e.printStackTrace();
        }
    }
    
    public int getGens() {
        return this.generations;
    }
    
    public void resetPedigree() {
        for (final MapleFamilyCharacter mfc : this.members.values()) {
            mfc.resetPedigree(this);
        }
        this.bDirty = true;
    }
    
    public void resetGens() {
        final MapleFamilyCharacter mfc = this.getMFC(this.leaderid);
        if (mfc != null) {
            this.generations = mfc.resetGenerations(this);
        }
        this.bDirty = true;
    }
    
    public void resetDescendants() {
        final MapleFamilyCharacter mfc = this.getMFC(this.leaderid);
        if (mfc != null) {
            mfc.resetDescendants(this);
        }
        this.bDirty = true;
    }
    
    public boolean isProper() {
        return this.proper;
    }
    
    public static Collection<MapleFamily> loadAll() {
        final Collection<MapleFamily> ret = new ArrayList<MapleFamily>();
        try (Connection con = (Connection)DBConPool.getInstance().getDataSource().getConnection();
             final PreparedStatement ps = con.prepareStatement("SELECT familyid FROM families")) {
            final ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                final MapleFamily g = new MapleFamily(rs.getInt("familyid"));
                if (g.getId() > 0) {
                    ret.add(g);
                }
            }
            rs.close();
        }
        catch (SQLException se) {
            //System.err.println("unable to read family information from sql");
            FileoutputUtil.outError("logs/资料库异常.txt", (Throwable)se);
        }
        return ret;
    }
    
    public void writeToDB(final boolean bDisband) {
        try (Connection con = (Connection)DBConPool.getInstance().getDataSource().getConnection()) {
            if (!bDisband) {
                if (this.changed) {
                    try (final PreparedStatement ps = con.prepareStatement("UPDATE families SET notice = ? WHERE familyid = ?")) {
                        ps.setString(1, this.notice);
                        ps.setInt(2, this.id);
                        ps.execute();
                    }
                }
                this.changed = false;
            }
            else {
                try (final PreparedStatement ps = con.prepareStatement("DELETE FROM families WHERE familyid = ?")) {
                    ps.setInt(1, this.id);
                    ps.execute();
                }
            }
        }
        catch (SQLException se) {
            //System.err.println("Error saving family to SQL");
            FileoutputUtil.outError("logs/资料库异常.txt", (Throwable)se);
        }
    }
    
    public final int getId() {
        return this.id;
    }
    
    public final int getLeaderId() {
        return this.leaderid;
    }
    
    public final String getNotice() {
        if (this.notice == null) {
            return "";
        }
        return this.notice;
    }
    
    public final String getLeaderName() {
        return this.leadername;
    }
    
    public void broadcast(final byte[] packet, final List<Integer> cids) {
        this.broadcast(packet, -1, FCOp.NONE, cids);
    }
    
    public void broadcast(final byte[] packet, final int exception, final List<Integer> cids) {
        this.broadcast(packet, exception, FCOp.NONE, cids);
    }
    
    public void broadcast(final byte[] packet, final int exceptionId, final FCOp bcop, final List<Integer> cids) {
        this.buildNotifications();
        if (this.members.size() < 2) {
            this.bDirty = true;
            return;
        }
        for (final MapleFamilyCharacter mgc : this.members.values()) {
            if (cids == null || cids.contains((Object)Integer.valueOf(mgc.getId()))) {
                if (bcop == FCOp.DISBAND) {
                    if (mgc.isOnline()) {
                        Family.setFamily(0, 0, 0, 0, mgc.getCurrentRep(), mgc.getTotalRep(), mgc.getId());
                    }
                    else {
                        setOfflineFamilyStatus(0, 0, 0, 0, mgc.getCurrentRep(), mgc.getTotalRep(), mgc.getId());
                    }
                }
                else {
                    if (!mgc.isOnline() || mgc.getId() == exceptionId) {
                        continue;
                    }
                    Broadcast.sendFamilyPacket(mgc.getId(), packet, exceptionId, this.id);
                }
            }
        }
    }
    
    private final void buildNotifications() {
        if (!this.bDirty) {
            return;
        }
        final Iterator<Entry<Integer, MapleFamilyCharacter>> toRemove = this.members.entrySet().iterator();
        while (toRemove.hasNext()) {
            final MapleFamilyCharacter mfc = (MapleFamilyCharacter)((Entry<Integer, MapleFamilyCharacter>)toRemove.next()).getValue();
            if (mfc.getJunior1() > 0 && this.getMFC(mfc.getJunior1()) == null) {
                mfc.setJunior1(0);
            }
            if (mfc.getJunior2() > 0 && this.getMFC(mfc.getJunior2()) == null) {
                mfc.setJunior2(0);
            }
            if (mfc.getSeniorId() > 0 && this.getMFC(mfc.getSeniorId()) == null) {
                mfc.setSeniorId(0);
            }
            if (mfc.getFamilyId() != this.id) {
                toRemove.remove();
            }
        }
        if (this.members.size() < 2 && Family.getFamily(this.id) != null) {
            Family.disbandFamily(this.id);
        }
        this.bDirty = false;
    }
    
    public void setOnline(final int cid, final boolean online, final int channel) {
        final MapleFamilyCharacter mgc = this.getMFC(cid);
        if (mgc != null && mgc.getFamilyId() == this.id) {
            if (mgc.isOnline() != online) {
                this.broadcast(FamilyPacket.familyLoggedIn(online, mgc.getName()), cid, (mgc.getId() == this.leaderid) ? null : mgc.getPedigree());
            }
            mgc.setOnline(online);
            mgc.setChannel((int)(byte)channel);
        }
        this.bDirty = true;
    }
    
    public final int setRep(final int cid, int addrep, final int oldLevel) {
        final MapleFamilyCharacter mgc = this.getMFC(cid);
        if (mgc != null && mgc.getFamilyId() == this.id) {
            if (oldLevel > mgc.getLevel()) {
                addrep /= 2;
            }
            if (mgc.isOnline()) {
                final List<Integer> dummy = new ArrayList<Integer>();
                dummy.add(Integer.valueOf(mgc.getId()));
                this.broadcast(FamilyPacket.changeRep(addrep), -1, dummy);
                Family.setFamily(this.id, mgc.getSeniorId(), mgc.getJunior1(), mgc.getJunior2(), mgc.getCurrentRep() + addrep, mgc.getTotalRep() + addrep, mgc.getId());
            }
            else {
                setOfflineFamilyStatus(this.id, mgc.getSeniorId(), mgc.getJunior1(), mgc.getJunior2(), mgc.getCurrentRep() + addrep, mgc.getTotalRep() + addrep, mgc.getId());
            }
            return mgc.getSeniorId();
        }
        return 0;
    }
    
    public final MapleFamilyCharacter addFamilyMemberInfo(final MapleCharacter mc, final int seniorid, final int junior1, final int junior2) {
        final MapleFamilyCharacter ret = new MapleFamilyCharacter(mc, this.id, seniorid, junior1, junior2);
        this.members.put(Integer.valueOf(mc.getId()), ret);
        ret.resetPedigree(this);
        this.bDirty = true;
        final List<Integer> toRemove = new ArrayList<Integer>();
        for (int i = 0; i < ret.getPedigree().size(); ++i) {
            if ((int)Integer.valueOf(ret.getPedigree().get(i)) != ret.getId()) {
                final MapleFamilyCharacter mfc = this.getMFC((int)Integer.valueOf(ret.getPedigree().get(i)));
                if (mfc == null) {
                    toRemove.add(Integer.valueOf(i));
                }
                else {
                    mfc.resetPedigree(this);
                }
            }
        }
        final Iterator<Integer> iterator = toRemove.iterator();
        while (iterator.hasNext()) {
            final int j = (int)Integer.valueOf(iterator.next());
            ret.getPedigree().remove(j);
        }
        return ret;
    }
    
    public final int addFamilyMember(final MapleFamilyCharacter mgc) {
        mgc.setFamilyId(this.id);
        this.members.put(Integer.valueOf(mgc.getId()), mgc);
        mgc.resetPedigree(this);
        this.bDirty = true;
        final Iterator<Integer> iterator = mgc.getPedigree().iterator();
        while (iterator.hasNext()) {
            final int i = (int)Integer.valueOf(iterator.next());
            this.getMFC(i).resetPedigree(this);
        }
        return 1;
    }
    
    public void leaveFamily(final int id) {
        this.leaveFamily(this.getMFC(id), true);
    }
    
    public void leaveFamily(final MapleFamilyCharacter mgc, final boolean skipLeader) {
        this.bDirty = true;
        if (mgc.getId() == this.leaderid && !skipLeader) {
            this.leadername = null;
            Family.disbandFamily(this.id);
        }
        else {
            if (mgc.getJunior1() > 0) {
                final MapleFamilyCharacter j = this.getMFC(mgc.getJunior1());
                if (j != null) {
                    j.setSeniorId(0);
                    this.splitFamily(j.getId(), j);
                }
            }
            if (mgc.getJunior2() > 0) {
                final MapleFamilyCharacter j = this.getMFC(mgc.getJunior2());
                if (j != null) {
                    j.setSeniorId(0);
                    this.splitFamily(j.getId(), j);
                }
            }
            if (mgc.getSeniorId() > 0) {
                final MapleFamilyCharacter mfc = this.getMFC(mgc.getSeniorId());
                if (mfc != null) {
                    if (mfc.getJunior1() == mgc.getId()) {
                        mfc.setJunior1(0);
                    }
                    else {
                        mfc.setJunior2(0);
                    }
                }
            }
            final List<Integer> dummy = new ArrayList<Integer>();
            dummy.add(Integer.valueOf(mgc.getId()));
            this.broadcast(null, -1, FCOp.DISBAND, dummy);
            this.resetPedigree();
        }
        this.members.remove((Object)Integer.valueOf(mgc.getId()));
        this.bDirty = true;
    }
    
    public void memberLevelJobUpdate(final MapleCharacter mgc) {
        final MapleFamilyCharacter member = this.getMFC(mgc.getId());
        if (member != null) {
            final int old_level = member.getLevel();
            final int old_job = member.getJobId();
            member.setJobId((int)mgc.getJob());
            member.setLevel((int)mgc.getLevel());
            if (old_level != mgc.getLevel()) {
                this.broadcast(MaplePacketCreator.sendLevelup(true, (int)mgc.getLevel(), mgc.getName()), mgc.getId(), (mgc.getId() == this.leaderid) ? null : member.getPedigree());
            }
            if (old_job != mgc.getJob()) {
                this.broadcast(MaplePacketCreator.sendJobup(true, (int)mgc.getJob(), mgc.getName()), mgc.getId(), (mgc.getId() == this.leaderid) ? null : member.getPedigree());
            }
        }
    }
    
    public void disbandFamily() {
        this.writeToDB(true);
    }
    
    public final MapleFamilyCharacter getMFC(final int cid) {
        return (MapleFamilyCharacter)this.members.get((Object)Integer.valueOf(cid));
    }
    
    public int getMemberSize() {
        return this.members.size();
    }
    
    public static void setOfflineFamilyStatus(final int familyid, final int seniorid, final int junior1, final int junior2, final int currentrep, final int totalrep, final int cid) {
        try (Connection con = (Connection)DBConPool.getInstance().getDataSource().getConnection()) {
            final PreparedStatement ps = con.prepareStatement("UPDATE characters SET familyid = ?, seniorid = ?, junior1 = ?, junior2 = ?, currentrep = ?, totalrep = ? WHERE id = ?");
            ps.setInt(1, familyid);
            ps.setInt(2, seniorid);
            ps.setInt(3, junior1);
            ps.setInt(4, junior2);
            ps.setInt(5, currentrep);
            ps.setInt(6, totalrep);
            ps.setInt(7, cid);
            ps.execute();
            ps.close();
        }
        catch (SQLException se) {
            System.err.println("SQLException: " + se.getLocalizedMessage());
            FileoutputUtil.outError("logs/资料库异常.txt", (Throwable)se);
            //e.printStackTrace();
        }
    }
    
    public static int createFamily(final int leaderId) {
        try (Connection con = (Connection)DBConPool.getInstance().getDataSource().getConnection()) {
            final PreparedStatement ps = con.prepareStatement("INSERT INTO families (`leaderid`) VALUES (?)", 1);
            ps.setInt(1, leaderId);
            ps.executeUpdate();
            final ResultSet rs = ps.getGeneratedKeys();
            if (!rs.next()) {
                rs.close();
                ps.close();
                return 0;
            }
            final int ret = rs.getInt(1);
            rs.close();
            ps.close();
            return ret;
        }
        catch (Exception e) {
            FileoutputUtil.outError("logs/资料库异常.txt", (Throwable)e);
            return 0;
        }
    }
    
    public static void mergeFamily(final MapleFamily newfam, final MapleFamily oldfam) {
        for (final MapleFamilyCharacter mgc : oldfam.members.values()) {
            mgc.setFamilyId(newfam.getId());
            if (mgc.isOnline()) {
                Family.setFamily(newfam.getId(), mgc.getSeniorId(), mgc.getJunior1(), mgc.getJunior2(), mgc.getCurrentRep(), mgc.getTotalRep(), mgc.getId());
            }
            else {
                setOfflineFamilyStatus(newfam.getId(), mgc.getSeniorId(), mgc.getJunior1(), mgc.getJunior2(), mgc.getCurrentRep(), mgc.getTotalRep(), mgc.getId());
            }
            newfam.members.put(Integer.valueOf(mgc.getId()), mgc);
            newfam.setOnline(mgc.getId(), mgc.isOnline(), mgc.getChannel());
        }
        newfam.resetPedigree();
        Family.disbandFamily(oldfam.getId());
    }
    
    public boolean splitFamily(final int splitId, final MapleFamilyCharacter def) {
        MapleFamilyCharacter leader = this.getMFC(splitId);
        if (leader == null) {
            leader = def;
            if (leader == null) {
                return false;
            }
        }
        try {
            final List<MapleFamilyCharacter> all = leader.getAllJuniors(this);
            if (all.size() <= 1) {
                this.leaveFamily(leader, false);
                return true;
            }
            final int newId = createFamily(leader.getId());
            if (newId <= 0) {
                return false;
            }
            for (final MapleFamilyCharacter mgc : all) {
                mgc.setFamilyId(newId);
                setOfflineFamilyStatus(newId, mgc.getSeniorId(), mgc.getJunior1(), mgc.getJunior2(), mgc.getCurrentRep(), mgc.getTotalRep(), mgc.getId());
                this.members.remove((Object)Integer.valueOf(mgc.getId()));
            }
            final MapleFamily newfam = Family.getFamily(newId);
            for (final MapleFamilyCharacter mgc2 : all) {
                if (mgc2.isOnline()) {
                    Family.setFamily(newId, mgc2.getSeniorId(), mgc2.getJunior1(), mgc2.getJunior2(), mgc2.getCurrentRep(), mgc2.getTotalRep(), mgc2.getId());
                }
                newfam.setOnline(mgc2.getId(), mgc2.isOnline(), mgc2.getChannel());
            }
            if (this.members.size() <= 1) {
                Family.disbandFamily(this.id);
                return true;
            }
        }
        finally {
            if (this.members.size() <= 1) {
                Family.disbandFamily(this.id);
                return true;
            }
        }
        this.bDirty = true;
        return false;
    }
    
    public void setNotice(final String notice) {
        this.changed = true;
        this.notice = notice;
    }
    
    public enum FCOp
    {
        NONE, 
        DISBAND;
    }
}
