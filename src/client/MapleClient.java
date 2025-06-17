package client;

import gui.LtMS;
import io.netty.util.AttributeKey;
import server.ServerProperties;
import database.DatabaseConnection;
import server.Start;
import tools.MaplePacketCreator;

import java.util.*;

import constants.ServerConstants.PlayerGMRank;
import server.Timer.PingTimer;
import tools.packet.LoginPacket;
import handling.world.family.MapleFamilyCharacter;
import handling.world.guild.MapleGuildCharacter;
import handling.world.MapleParty;
import server.maps.MapleMap;
import handling.cashshop.CashShopServer;
import handling.world.World.Find;
import handling.world.World.Family;
import handling.world.World.Guild;
import handling.world.World.Buddy;
import handling.world.World.Party;
import handling.world.PartyOperation;
import handling.world.World.Messenger;
import handling.world.MapleMessengerCharacter;
import handling.world.MaplePartyCharacter;
import server.shops.IMaplePlayerShop;
import server.quest.MapleQuest;
import database.DatabaseException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;
import tools.HexTool;
import java.security.MessageDigest;
import tools.FilePrinter;
import handling.channel.ChannelServer;
import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.sql.Connection;
import java.sql.SQLException;
import tools.FileoutputUtil;
import database.DBConPool;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.ScheduledFuture;
import javax.script.ScriptEngine;

import io.netty.channel.Channel;
import tools.MapleAESOFB;


public class MapleClient
{
    public static boolean 离线挂机;
    public static transient byte LOGIN_NOTLOGGEDIN = 0;
    public static transient byte LOGIN_SERVER_TRANSITION = 1;
    public static transient byte LOGIN_LOGGEDIN = 2;
    public static transient byte LOGIN_WAITING = 3;
    public static transient byte CASH_SHOP_TRANSITION = 4;
    public static transient byte LOGIN_CS_LOGGEDIN = 5;
    public static transient byte CHANGE_CHANNEL = 6;
    public static int DEFAULT_CHARSLOT = 3;
    public static AttributeKey<MapleClient> CLIENT_KEY;
    private final MapleAESOFB send;
    private final MapleAESOFB receive;
    private final Channel session;
    private MapleCharacter player;
    private int accountId;
    private String accountName;
    private int world;
    /**
     * 角色频道
     */
    private int channel;
    private int birthday;
    private int charslots;
    private boolean loggedIn;
    private boolean serverTransition;
    private boolean canloginpw;
    private transient Calendar tempban;
    private transient long lastPong;
    private transient long lastPing;
    private boolean monitored;
    private boolean receiving;
    private int gmLevel;
    private short vip;
    private byte bannedReason;
    private byte gender;
    public transient short loginAttempt;
    private final transient List<Integer> allowedChar;
    private final transient Set<String> macs;
    public final transient Map<String, ScriptEngine> engines;
    private transient ScheduledFuture<?> idleTask;
    private transient String secondPassword;
    private final transient Lock mutex;
    private final transient Lock npcMutex;
    private long lastNpcClick;
    private long lastLoginTime;
    private static final Lock loginMutex;
    private String loginKey;
    private String serverKey;
    private int loginKeya;
    private boolean closeseesion;
    private long lastPortalClick;
    private static boolean forceLogining;
    public MapleClient(final MapleAESOFB send, final MapleAESOFB receive, final Channel session) {
        this.accountId = 1;
        this.world = 1;
        this.channel = 1;
        this.charslots = 3;
        this.loggedIn = false;
        this.serverTransition = false;
        this.canloginpw = false;
        this.tempban = null;
        this.lastPong = 0L;
        this.lastPing = 0L;
        this.monitored = false;
        this.receiving = true;
        this.bannedReason = 1;
        this.gender = -1;
        this.loginAttempt = 0;
        this.allowedChar = new LinkedList<Integer>();
        this.macs = new HashSet<String>();
        this.engines = new HashMap<String, ScriptEngine>();
        this.idleTask = null;
        this.mutex = new ReentrantLock(true);
        this.npcMutex = new ReentrantLock();
        this.lastNpcClick = 0L;
        this.loginKeya = 0;
        this.closeseesion = false;
        this.send = send;
        this.receive = receive;
        this.session = session;
    }
    
    public final MapleAESOFB getReceiveCrypto() {
        return this.receive;
    }
    
    public final MapleAESOFB getSendCrypto() {
        return this.send;
    }
    
    public final Channel getSession() {
        return this.session;
    }
    
    public final Lock getLock() {
        return this.mutex;
    }
    
    public final Lock getNPCLock() {
        return this.npcMutex;
    }
    
    public MapleCharacter getPlayer() {
        return this.player;
    }
    
    public void setPlayer(final MapleCharacter player) {
        this.player = player;
    }
    
    public void createdChar(final int id) {
        this.allowedChar.add(Integer.valueOf(id));
    }
    
    public final boolean login_Auth(final int id) {
        return this.allowedChar.contains((Object)Integer.valueOf(id));
    }
    
    public final List<MapleCharacter> loadCharacters(final int serverId) {
        final List<MapleCharacter> chars = new LinkedList<MapleCharacter>();
        for (final CharNameAndId cni : this.loadCharactersInternal(serverId)) {
            MapleCharacter chr = MapleCharacter.loadCharFromDB(cni.id, this, false);
            chars.add(chr);
            this.allowedChar.add(Integer.valueOf(chr.getId()));
        }
        return chars;
    }
    
    public List<String> loadCharacterNames(final int serverId) {
        final List<String> chars = new LinkedList<String>();
        for (final CharNameAndId cni : this.loadCharactersInternal(serverId)) {
            chars.add(cni.name);
        }
        return chars;
    }
    
    public List<Integer> loadCharacterIds(final int serverId) {
        final List<Integer> chars = new LinkedList<Integer>();
        for (final CharNameAndId cni : this.loadCharactersInternal(serverId)) {
            chars.add(Integer.valueOf(cni.id));
        }
        return chars;
    }
    
    private List<CharNameAndId> loadCharactersInternal(final int serverId) {
        final List<CharNameAndId> chars = new LinkedList<CharNameAndId>();
        try (Connection con = DBConPool.getConnection();
             final PreparedStatement ps = con.prepareStatement("SELECT id, name FROM characters WHERE accountid = ? AND world = ?")) {
            ps.setInt(1, this.accountId);
            ps.setInt(2, serverId);
            try (final ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    chars.add(new CharNameAndId(rs.getString("name"), rs.getInt("id")));
                }
            }
            ps.close();
        }
        catch (SQLException e) {
            System.err.println("error loading characters internal" + (Object)e);
            FileoutputUtil.outError("logs/资料库异常.txt", (Throwable)e);
        }
        return chars;
    }
    
    public boolean isLoggedIn() {
        return this.loggedIn;
    }
    
    private Calendar getTempBanCalendar(final ResultSet rs) throws SQLException {
        final Calendar lTempban = Calendar.getInstance();
        if (rs.getLong("tempban") == 0L) {
            lTempban.setTimeInMillis(0L);
            return lTempban;
        }
        final Calendar today = Calendar.getInstance();
        lTempban.setTimeInMillis(rs.getTimestamp("tempban").getTime());
        if (today.getTimeInMillis() < lTempban.getTimeInMillis()) {
            return lTempban;
        }
        lTempban.setTimeInMillis(0L);
        return lTempban;
    }
    
    public Calendar getTempBanCalendar() {
        return this.tempban;
    }
    
    public byte getBanReason() {
        return this.bannedReason;
    }
    
    public boolean isBannedIP(final String ip) {
        boolean ret = false;
        try (Connection con = DBConPool.getConnection();
             final PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM ipbans WHERE ? LIKE CONCAT(ip, '%')")) {
            ps.setString(1, ip);
            try (final ResultSet rs = ps.executeQuery()) {
                rs.next();
                if (rs.getInt(1) > 0) {
                    ret = true;
                }
            }
            ps.close();
                    con.close();

        }
        catch (SQLException ex) {
            System.err.println("Error checking ip bans" + (Object)ex);
            FileoutputUtil.outError("logs/资料库异常.txt", (Throwable)ex);
        }
        return ret;
    }
    
    public boolean hasBannedIP() {
        boolean ret = false;
        try (Connection con = DBConPool.getConnection();
             final PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM ipbans WHERE ? LIKE CONCAT(ip, '%')")) {
            ps.setString(1, this.getSessionIPAddress());
            try (final ResultSet rs = ps.executeQuery()) {
                rs.next();
                if (rs.getInt(1) > 0) {
                    ret = true;
                }
            }
ps.close();
        con.close();
        }
        catch (SQLException ex) {
            System.err.println("Error checking ip bans" + (Object)ex);
            FileoutputUtil.outError("logs/资料库异常.txt", (Throwable)ex);
        }
        return ret;
    }
    
    public int finishLogin() {
        MapleClient.loginMutex.lock();
        try {
            final byte state = this.getLoginState();
            if (state > 0 && state != 3) {
                this.loggedIn = false;
                return 7;
            }
            this.updateLoginState(2, this.getSessionIPAddress());
        }
        finally {
            MapleClient.loginMutex.unlock();
        }
        return 0;
    }
    
    public int login(final String account, final String password, final boolean isIPBanned) {
        int loginok = 5;
        try (Connection con = DBConPool.getConnection();
             final PreparedStatement ps = con.prepareStatement("SELECT id, banned, password, salt, macs, 2ndpassword, gm, vip, greason, tempban, gender, SessionIP FROM accounts WHERE name = ?")) {
            ps.setString(1, account);
            try (final ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    final int banned = rs.getInt("banned");
                    final String passhash = rs.getString("password");
                    final String salt = rs.getString("salt");
                    final String oldSession = rs.getString("SessionIP");
                    this.accountId = rs.getInt("id");
                    this.secondPassword = rs.getString("2ndpassword");
                    this.gmLevel = rs.getInt("gm");
                    this.vip = rs.getShort("vip");
                    this.bannedReason = rs.getByte("greason");
                    this.tempban = this.getTempBanCalendar(rs);
                    this.gender = rs.getByte("gender");
                    ps.close();
                    if (banned > 0 && !this.isGm()) {
                        loginok = 3;
                    }
                    else {
                        if (banned == -1) {
                            this.unban();
                        }
                        final byte loginstate = this.getLoginState();
                        boolean updatePasswordHash = false;
                        if (LoginCryptoLegacy.isLegacyPassword(passhash) && LoginCryptoLegacy.checkPassword(password, passhash)) {
                            loginok = 0;
                            updatePasswordHash = true;
                        }
                        else if (salt == null && LoginCrypto.checkSha1Hash(passhash, password)) {
                            loginok = 0;
                            updatePasswordHash = true;
                        }
                        else if (password.equals((Object)passhash)) {
                            loginok = 0;
                            updatePasswordHash = true;
                        }
                        else if (LoginCrypto.checkSaltedSha512Hash(passhash, password, salt)) {
                            loginok = 0;
                        }
                        else {
                            this.loggedIn = false;
                            loginok = 4;
                        }
                        if (loginok == 0) {
                            if ((Integer) LtMS.ConfigValuesMap.get("允许顶号开关") <= 0) {
                                if (ChannelServer.isOnlineByAccId(this, this.accountId)) {
                                    loginok = 6;
                                } else {
                                    ChannelServer.forceRemovePlayerByAccId(this, this.accountId);
                                    this.updateLoginState(0, this.getSessionIPAddress());
                                }
                            } else {
                                if (ChannelServer.isOnlineByAccId(this, this.accountId)) {
                                    Random rand = null;
                                    if (forceLogining) {
                                        return rand.nextInt();
                                    }

                                    this.getSession().writeAndFlush(MaplePacketCreator.serverNotice(1, "检测到您的账号正在游戏中，正在顶号，请耐心等候！"));
                                    forceLogining = true;
                                    rand = new Random();
                                    try {
                                        Thread.sleep((long)((Integer)LtMS.ConfigValuesMap.get("顶号等待秒数") * 1000 ));
                                    } catch (InterruptedException e) {

                                    }
                                }

                                ChannelServer.forceRemovePlayerByAccId(this, this.accountId);
                                this.updateLoginState(0, this.getSessionIPAddress());
                                forceLogining = false;
                            }
                        }
                    }
                }
            }
                    con.close();
        }
        catch (SQLException e) {
            System.err.println("ERROR" + (Object)e);
            FileoutputUtil.outError("logs/资料库异常.txt", (Throwable)e);
        }
        if (loginok == 0) {
            this.canloginpw = true;
            this.lastLoginTime = System.currentTimeMillis();
        }
        return loginok;
    }
    
    public void loadVip(final int accountID) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try (Connection con = DBConPool.getConnection()) {
            ps = con.prepareStatement("SELECT vip FROM accounts WHERE id = ?");
            ps.setInt(1, accountID);
            rs = ps.executeQuery();
            if (rs.next()) {
                this.vip = rs.getShort("vip");
                ps.close();
                rs.close();
            }
            ps.close();
            con.close();
        }
        catch (SQLException e) {
            FilePrinter.printError("MapleClient.txt", (Throwable)e);
            FileoutputUtil.outError("logs/资料库异常.txt", (Throwable)e);
            try {
                if (ps != null && !ps.isClosed()) {
                    ps.close();
                }
                if (rs != null && !rs.isClosed()) {
                    rs.close();
                }
            }
            catch (SQLException e2) {
                FileoutputUtil.outError("logs/资料库异常.txt", (Throwable)e2);
            }
        }
        finally {
            try {
                if (ps != null && !ps.isClosed()) {
                    ps.close();
                }
                if (rs != null && !rs.isClosed()) {
                    rs.close();
                }
            }
            catch (SQLException e2) {
                FileoutputUtil.outError("logs/资料库异常.txt", (Throwable)e2);
            }
        }
    }
    
    public void update2ndPassword() {
        try {
            final MessageDigest digester = MessageDigest.getInstance("SHA-1");
            digester.update(this.secondPassword.getBytes("UTF-8"), 0, this.secondPassword.length());
            final String hash = HexTool.toString(digester.digest()).replace((CharSequence)" ", (CharSequence)"").toLowerCase();
            try (Connection con = DBConPool.getConnection();
                 final PreparedStatement ps = con.prepareStatement("UPDATE `accounts` SET `2ndpassword` = ? WHERE id = ?")) {
                ps.setString(1, hash);
                ps.setInt(2, this.accountId);
                ps.executeUpdate();
                ps.close();
                con.close();

            }
            catch (SQLException ex) {
                FilePrinter.printError("MapleClient.txt", (Throwable)ex);
                FileoutputUtil.outError("logs/资料库异常.txt", (Throwable)ex);
            }
        }
        catch (NoSuchAlgorithmException | UnsupportedEncodingException ex4) {
            Logger.getLogger(MapleClient.class.getName()).log(Level.SEVERE, null, (Throwable)ex4);
            FileoutputUtil.outError("logs/资料库异常.txt", (Throwable)ex4);
        }
    }
    
    private void unban() {
        try (Connection con = DBConPool.getConnection();
             final PreparedStatement ps = con.prepareStatement("UPDATE accounts SET banned = 0 and banreason = '' WHERE id = ?")) {
            ps.setInt(1, this.accountId);
            ps.executeUpdate();
            ps.close();
            con.close();
        }
        catch (SQLException e) {
            System.err.println("Error while unbanning" + (Object)e);
            FileoutputUtil.outError("logs/资料库异常.txt", (Throwable)e);
        }
    }
    
    public static byte unban(final String charname) {
        try (Connection con = DBConPool.getConnection()) {
            PreparedStatement ps = con.prepareStatement("SELECT accountid from characters where name = ?");
            ps.setString(1, charname);
            final ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                rs.close();
                ps.close();
                return -1;
            }
            final int accid = rs.getInt(1);
            rs.close();
            ps.close();
            ps = con.prepareStatement("UPDATE accounts SET banned = 0 and banreason = '' WHERE id = ?");
            ps.setInt(1, accid);
            ps.executeUpdate();
            ps.close();
            con.close();
        }

        catch (SQLException e) {
            System.err.println("Error while unbanning" + (Object)e);
            FileoutputUtil.outError("logs/资料库异常.txt", (Throwable)e);
            return -2;
        }
        return 0;
    }
    
    public void setAccID(final int id) {
        this.accountId = id;
    }
    
    public int getAccID() {
        return this.accountId;
    }
    
    public void updateLoginState(final int newstate, final String SessionID) {
        MapleClient.loginMutex.lock();
        try {
            try (Connection con = DBConPool.getConnection()) {
                 PreparedStatement ps = con.prepareStatement("UPDATE accounts SET loggedin = ?, SessionIP = ?, lastlogin = CURRENT_TIMESTAMP() WHERE id = ?");
                ps.setInt(1, newstate);
                ps.setString(2, SessionID);
                ps.setInt(3, this.getAccID());
                ps.executeUpdate();
                ps.close();
            }
            catch (SQLException e) {
                System.err.println("更新登入状态錯誤" + (Object)e);
                FileoutputUtil.outError("logs/资料库异常.txt", (Throwable)e);
            }
            if (newstate == 0 || newstate == 3) {
                this.loggedIn = false;
                this.serverTransition = false;
            }
            else {
                this.serverTransition = (newstate == 1 || newstate == 6);
                this.loggedIn = !this.serverTransition;
            }
        }
        finally {
            MapleClient.loginMutex.unlock();
        }
    }
    
    public void updateGender() {
        try (Connection con = DBConPool.getConnection();
             final PreparedStatement ps = con.prepareStatement("UPDATE `accounts` SET `gender` = ? WHERE id = ?")) {
            ps.setInt(1, (int)this.gender);
            ps.setInt(2, this.accountId);
            ps.executeUpdate();
            ps.close();
            con.close();
        }
        catch (SQLException e) {
            System.err.println("更新性別錯誤" + (Object)e);
            FileoutputUtil.outError("logs/资料库异常.txt", (Throwable)e);
        }
    }
    
    public final byte getLoginState() {
        try (Connection con = DBConPool.getConnection()) {
            final PreparedStatement ps = con.prepareStatement("SELECT loggedin, lastlogin, `birthday` + 0 AS `bday` FROM accounts WHERE id = ?");
            ps.setInt(1, this.getAccID());
            byte state;
            try (final ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    ps.close();
                    throw new DatabaseException("Everything sucks");
                }
                this.birthday = rs.getInt("bday");
                state = rs.getByte("loggedin");
                if ((state == 1 || state == 6) && rs.getTimestamp("lastlogin").getTime() + 70000L < System.currentTimeMillis()) {
                    state = 0;
                    this.updateLoginState((int)state, this.getSessionIPAddress());
                }
            }
            ps.close();
            con.close();
            this.loggedIn = (state == 2);
            return state;
        }
        catch (SQLException e) {
            this.loggedIn = false;
            FileoutputUtil.outError("logs/资料库异常.txt", (Throwable)e);
            throw new DatabaseException("登入状态獲取失敗", (Throwable)e);
        }
    }
    
    public final boolean checkBirthDate(final int date) {
        return this.birthday == date;
    }
    
    public void removalTask(final boolean shutdown) {
        try {
            this.player.cancelAllBuffs_();
            this.player.cancelAllDebuffs();
            this.player.cancelAllSkillID();
            if (this.player.getMarriageId() > 0) {
                final MapleQuestStatus stat1 = this.player.getQuestNAdd(MapleQuest.getInstance(160001));
                final MapleQuestStatus stat2 = this.player.getQuestNAdd(MapleQuest.getInstance(160002));
                if (stat1.getCustomData() != null && (stat1.getCustomData().equals((Object)"2_") || stat1.getCustomData().equals((Object)"2"))) {
                    if (stat2.getCustomData() != null) {
                        stat2.setCustomData("0");
                    }
                    stat1.setCustomData("3");
                }
            }
            this.player.changeRemoval(true);
            if (this.player.getEventInstance() != null) {
                this.player.getEventInstance().playerDisconnected(this.player, this.player.getId());
            }
            if (this.player.getMap() != null) {
                switch (this.player.getMapId()) {
                    case 220080001:
                    case 240060000:
                    case 240060100:
                    case 240060200:
                    case 541010100:
                    case 541020800:
                    case 551030200: {
                        this.player.getMap().addDisconnected(this.player.getId());
                        break;
                    }
                }
                if (this.player.getMapId() == 910000000 && this.player.isPlayer() && !this.player.isGM() && MapleClient.离线挂机) {
                    this.player.getMap().removePlayer2(this.player);
                }
                else {
                    this.player.getMap().removePlayer(this.player);
                }
            }
            final IMaplePlayerShop shop = this.player.getPlayerShop();
            if (shop != null) {
                shop.removeVisitor(this.player);
                if (shop.isOwner(this.player)) {
                    if (shop.getShopType() == 1 && shop.isAvailable() && !shutdown) {
                        shop.setOpen(true);
                    }
                    else {
                        shop.closeShop(true, !shutdown);
                    }
                }
            }
            this.player.setMessenger(null);
//            try {
//                if (this.player.getAntiMacro().inProgress()) {
//                    this.player.getAntiMacro().end();
//                }
//            } catch (Exception e) {
//                System.out.println("测谎this.player.getAntiMacro().inProgress()"+e.getMessage());
//            }
        }
        catch (Throwable e) {
            FilePrinter.printError("AccountStuck.txt", e);
        }
    }
    
    public void disconnect(final boolean RemoveInChannelServer, final boolean fromCS) {
        this.disconnect(RemoveInChannelServer, fromCS, false);
    }
    
    public void disconnect(final boolean RemoveInChannelServer, final boolean fromCS, final boolean shutdown) {
        try {
            if (this.player != null) {
                 MapleMap map = this.player.getMap();
                 MapleParty party = this.player.getParty();
                 boolean clone = this.player.isClone();
                 String namez = this.player.getName();
                 boolean hidden = this.player.isHidden();
                 int gmLevel = this.player.getGMLevel();
                 int idz = this.player.getId();
                 int messengerid = (this.player.getMessenger() == null) ? 0 : this.player.getMessenger().getId();
                 int gid = this.player.getGuildId();
                 int fid = this.player.getFamilyId();
                 BuddyList bl = this.player.getBuddylist();
                 MaplePartyCharacter chrp = new MaplePartyCharacter(this.player);
                 MapleMessengerCharacter chrm = new MapleMessengerCharacter(this.player);
                 MapleGuildCharacter chrg = this.player.getMGC();
                 MapleFamilyCharacter chrf = this.player.getMFC();
                this.removalTask(shutdown);
                try {
                    this.player.saveToDB(true, fromCS);
                }
                catch (Exception ex) {
                    FileoutputUtil.logToFile("logs/下線保存数据异常.txt", "\r\n " + FileoutputUtil.NowTime() + " IP: " + this.getSession().remoteAddress().toString().split(":")[0] + " 账号 " + this.getAccountName() + " 账号ID " + this.getAccID() + " 角色名 " + this.player.getName() + " 角色ID " + this.player.getId());
                    FileoutputUtil.outError("logs/下線保存数据异常.txt", (Throwable)ex);
                }
                if (shutdown) {
                    this.player = null;
                    this.receiving = false;
                    return;
                }
                if (!fromCS) {
                    final ChannelServer ch = ChannelServer.getInstance((map == null) ? this.channel : map.getChannel());
                    try {
                        if (ch == null || clone || ch.isShutdown()) {
                            this.player = null;
                            return;
                        }
                        if (messengerid > 0) {
                            Messenger.leaveMessenger(messengerid, chrm);
                        }
                        if (party != null) {
                            chrp.setOnline(false);
                            Party.updateParty(party.getId(), PartyOperation.LOG_ONOFF, chrp);
                            if (map != null && party.getLeader().getId() == idz) {
                                MaplePartyCharacter lchr = null;
                                for (final MaplePartyCharacter pchr : party.getMembers()) {
                                    if (pchr != null && map.getCharacterById(pchr.getId()) != null && (lchr == null || lchr.getLevel() < pchr.getLevel())) {
                                        lchr = pchr;
                                    }
                                }
                                if (lchr != null) {
                                    Party.updateParty(party.getId(), PartyOperation.CHANGE_LEADER_DC, lchr);
                                }
                            }
                        }
                        if (bl != null) {
                            if (!this.serverTransition && this.isLoggedIn()) {
                                Buddy.loggedOff(namez, idz, this.channel, bl.getBuddiesIds(), gmLevel, hidden);
                            }
                            else {
                                Buddy.loggedOn(namez, idz, this.channel, bl.getBuddiesIds(), gmLevel, hidden);
                            }
                        }
                        if (gid > 0) {
                            Guild.setGuildMemberOnline(chrg, false, -1);
                        }
                        if (fid > 0) {
                            Family.setFamilyMemberOnline(chrf, false, -1);
                        }
                    }
                    catch (Exception e) {
                        FilePrinter.printError("AccountStuck.txt", (Throwable)e);
                    }
                    finally {
                        if (RemoveInChannelServer && ch != null) {
                            // 关闭与服务器连接的客户端会话
                            // 该操作通常在客户端不再需要与服务器保持连接时执行，例如退出登录或关闭应用程序时
                            // 目的是释放系统资源，提升系统性能
                            this.disconnect(false, false, true);
                            this.getSession().close();
                            System.out.println("注销玩家：" + namez);
                            ch.removePlayer(idz, namez);
                        }
                        this.player = null;
                    }
                }
                else {
                    final int ch2 = Find.findChannel(idz);
                    if (ch2 > 0) {
                        this.disconnect(RemoveInChannelServer, false);
                        return;
                    }
                    try {
                        if (party != null) {
                            chrp.setOnline(false);
                            Party.updateParty(party.getId(), PartyOperation.LOG_ONOFF, chrp);
                        }
                        if (!this.serverTransition && this.isLoggedIn()) {
                            Buddy.loggedOff(namez, idz, this.channel, bl.getBuddiesIds(), gmLevel, hidden);
                        }
                        else {
                            Buddy.loggedOn(namez, idz, this.channel, bl.getBuddiesIds(), gmLevel, hidden);
                        }
                        if (gid > 0) {
                            Guild.setGuildMemberOnline(chrg, false, -1);
                        }
                        if (this.player != null) {
                            this.player.setMessenger(null);
                        }
                    }
                    catch (Exception e) {
                        FilePrinter.printError("AccountStuck.txt", (Throwable)e);
                    }
                    finally {
                        if ((RemoveInChannelServer && ch2 > 0) || (RemoveInChannelServer && ch2 == -10)) {
                            // 关闭与服务器连接的客户端会话
                            // 该操作通常在客户端不再需要与服务器保持连接时执行，例如退出登录或关闭应用程序时
                            // 目的是释放系统资源，提升系统性能
                            this.disconnect(false, false, true);
                            this.getSession().close();
                            System.out.println("注销玩家：" + namez);
                            CashShopServer.getPlayerStorage().deregisterPlayer(idz, namez);
                        }
                        this.player = null;
                    }
                }
            }
            if (!this.serverTransition && this.isLoggedIn()) {
                this.updateLoginState(0, this.getSessionIPAddress());
            }
            this.engines.clear();
        }
        catch (Exception ex2) {
            FileoutputUtil.logToFile("logs/下線處理异常.txt", "\r\n " + FileoutputUtil.NowTime() + " IP: " + this.getSession().remoteAddress().toString().split(":")[0] + " 账号 " + this.getAccountName() + " 账号ID " + this.getAccID() + " 角色名 " + this.player.getName() + " 角色ID " + this.player.getId());
            FileoutputUtil.outError("logs/下線處理异常.txt", (Throwable)ex2);
        }
    }
    
    public final String getSessionIPAddress() {
        if (this.session != null && this.session.remoteAddress() != null) {
            return this.session.remoteAddress().toString().split(":")[0];
        }
        return this.getLastIPAddress();
    }
    
    public final String getLastIPAddress() {
        String sessionIP = null;
        try (Connection con = DBConPool.getConnection();
             final PreparedStatement ps = con.prepareStatement("SELECT SessionIP FROM accounts WHERE id = ?")) {
            ps.setInt(1, this.accountId);
            try (final ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    sessionIP = rs.getString("SessionIP");
                }
            }
            ps.close();
            con.close();
        }
        catch (SQLException e) {
            System.err.println("Failed in checking IP address for client.");
            FileoutputUtil.outError("logs/资料库异常.txt", (Throwable)e);
        }
        return (sessionIP == null) ? "" : sessionIP;
    }
    
    public final boolean CheckIPAddress() {
        boolean canlogin = false;
        final String sessionIP = this.getLastIPAddress();
        if (!sessionIP.isEmpty()) {
            canlogin = this.getSessionIPAddress().equals((Object)sessionIP.split(":")[0]);
        }
        return canlogin;
    }
    
    public void DebugMessage(final StringBuilder sb) {
        sb.append((Object)this.getSession().remoteAddress());
        sb.append(" Connected: ");
        sb.append(this.getSession().isActive());
        sb.append(" ClientKeySet: ");
        sb.append(this.getSession().attr((AttributeKey)MapleClient.CLIENT_KEY).get() != null);
        sb.append(" loggedin: ");
        sb.append(this.isLoggedIn());
        sb.append(" has char: ");
        sb.append(this.getPlayer() != null);
    }
    
    public final int getChannel() {
        return this.channel;
    }
    
    public final ChannelServer getChannelServer() {
        return ChannelServer.getInstance(this.channel);
    }
    
    public final int deleteCharacter(final int cid) {
        String name = null;
        try (Connection con = DBConPool.getConnection()) {
            PreparedStatement ps = null;
            ps = con.prepareStatement("select name from characters where id = ?");
            ps.setInt(1, cid);
            final ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                name = rs.getString("name");
            }
            ps.close();
            rs.close();
            con.close();
        }
        catch (Exception ex) {
            FileoutputUtil.outError("logs/资料库异常.txt", (Throwable)ex);
        }
        FileoutputUtil.logToFile("Logs/Data/角色刪除.txt", FileoutputUtil.NowTime() + " 账号: " + this.accountName + "(" + this.accountId + ") 角色: " + cid + " (" + name + ") IP: " + this.getSessionIPAddress() + " \r\n");
        final Set<Integer> channels = ChannelServer.getAllChannels();
        for (final Integer ch : channels) {
            MapleCharacter chr = ChannelServer.getInstance((int)ch).getPlayerStorage().getCharacterById(cid);
            if (chr != null) {
                ChannelServer.getInstance((int)ch).removePlayer(chr);
            }
        }
        try (Connection con2 = DBConPool.getConnection()) {
            try (final PreparedStatement ps2 = con2.prepareStatement("SELECT guildid, guildrank, familyid, name FROM characters WHERE id = ? AND accountid = ?")) {
                ps2.setInt(1, cid);
                ps2.setInt(2, this.accountId);
                try (final ResultSet rs2 = ps2.executeQuery()) {
                    if (!rs2.next()) {
                        rs2.close();
                        ps2.close();
                        return 1;
                    }
                    if (rs2.getInt("guildid") > 0) {
                        if (rs2.getInt("guildrank") == 1) {
                            rs2.close();
                            ps2.close();
                            return 1;
                        }
                        Guild.deleteGuildCharacter(rs2.getInt("guildid"), cid);
                    }
                    ps2.close();
                    rs2.close();

                    if (rs2.getInt("familyid") > 0) {
                        Family.getFamily(rs2.getInt("familyid")).leaveFamily(cid);
                    }
                }
            }
            System.out.println("删除角色");
            MapleCharacter.deleteWhereCharacterId(con2, "DELETE FROM characters WHERE id = ?", cid);
            MapleCharacter.deleteWhereCharacterId(con2, "DELETE FROM monsterbook WHERE charid = ?", cid);
            MapleCharacter.deleteWhereCharacterId(con2, "DELETE FROM mts_cart WHERE characterid = ?", cid);
            MapleCharacter.deleteWhereCharacterId(con2, "DELETE FROM mts_items WHERE characterid = ?", cid);
            MapleCharacter.deleteWhereCharacterId(con2, "DELETE FROM mountdata WHERE characterid = ?", cid);
            MapleCharacter.deleteWhereCharacterId(con2, "DELETE FROM inventoryitems WHERE characterid = ?", cid);
            MapleCharacter.deleteWhereCharacterId(con2, "DELETE FROM famelog WHERE characterid = ?", cid);
            MapleCharacter.deleteWhereCharacterId(con2, "DELETE FROM famelog WHERE characterid_to = ?", cid);
            MapleCharacter.deleteWhereCharacterId(con2, "DELETE FROM dueypackages WHERE RecieverId = ?", cid);
            MapleCharacter.deleteWhereCharacterId(con2, "DELETE FROM wishlist WHERE characterid = ?", cid);
            MapleCharacter.deleteWhereCharacterId(con2, "DELETE FROM buddies WHERE characterid = ?", cid);
            MapleCharacter.deleteWhereCharacterId(con2, "DELETE FROM buddies WHERE buddyid = ?", cid);
            MapleCharacter.deleteWhereCharacterId(con2, "DELETE FROM keymap WHERE characterid = ?", cid);
            MapleCharacter.deleteWhereCharacterId(con2, "DELETE FROM savedlocations WHERE characterid = ?", cid);
            MapleCharacter.deleteWhereCharacterId(con2, "DELETE FROM skills WHERE characterid = ?", cid);
            MapleCharacter.deleteWhereCharacterId(con2, "DELETE FROM mountdata WHERE characterid = ?", cid);
            MapleCharacter.deleteWhereCharacterId(con2, "DELETE FROM skillmacros WHERE characterid = ?", cid);
            MapleCharacter.deleteWhereCharacterId(con2, "DELETE FROM trocklocations WHERE characterid = ?", cid);
            MapleCharacter.deleteWhereCharacterId(con2, "DELETE FROM queststatus WHERE characterid = ?", cid);
            MapleCharacter.deleteWhereCharacterId(con2, "DELETE FROM inventoryslot WHERE characterid = ?", cid);
            con2.commit();
            con2.close();
            return 0;
        }
        catch (Exception e) {
            FilePrinter.printError("MapleCharacter.txt", (Throwable)e, "deleteCharacter");
            FileoutputUtil.outError("logs/资料库异常.txt", (Throwable)e);
            return 1;
        }
    }
    
    public final byte getGender() {
        return this.gender;
    }
    
    public void setGender(final byte gender) {
        this.gender = gender;
    }
    
    public final String getSecondPassword() {
        return this.secondPassword;
    }
    
    public boolean getCheckSecondPassword(final String in) {
        boolean allow = false;
        if (LoginCrypto.checkSha1Hash(this.secondPassword, in)) {
            allow = true;
        }
        return allow;
    }
    
    public void updateSecondPassword() {
        try (Connection con = DBConPool.getConnection()) {
            final PreparedStatement ps = con.prepareStatement("UPDATE `accounts` SET `2ndpassword` = ? WHERE id = ?");
            ps.setString(1, LoginCrypto.hexSha1(this.secondPassword));
            ps.setInt(2, this.accountId);
            ps.executeUpdate();
            ps.close();
        }
        catch (SQLException e) {
            FileoutputUtil.outputFileError("logs/资料库异常.txt", (Throwable)e);
            System.err.println("error updating login state" + (Object)e);
        }
    }
    
    public void setSecondPassword(final String secondPassword) {
        this.secondPassword = secondPassword;
    }
    
    public boolean check2ndPassword(final String secondPassword) {
        boolean allow = false;
        if (checkHash(this.secondPassword, "SHA-1", secondPassword)) {
            allow = true;
        }
        return allow;
    }
    
    public static boolean checkHash(final String hash, final String type, final String password) {
        try {
            final MessageDigest digester = MessageDigest.getInstance(type);
            digester.update(password.getBytes("UTF-8"), 0, password.length());
            return HexTool.toString(digester.digest()).replace((CharSequence)" ", (CharSequence)"").toLowerCase().equals((Object)hash);
        }
        catch (NoSuchAlgorithmException | UnsupportedEncodingException ex2) {
            throw new RuntimeException("Encoding the string failed", (Throwable)ex2);
        }
    }
    
    public final String getAccountName() {
        return this.accountName;
    }
    
    public void setAccountName(final String accountName) {
        this.accountName = accountName;
    }
    
    public void setChannel(final int channel) {
        this.channel = channel;
    }
    
    public final int getWorld() {
        return this.world;
    }
    
    public void setWorld(final int world) {
        this.world = world;
    }
    
    public final int getLatency() {
        return (int)(this.lastPong - this.lastPing);
    }
    
    public final long getLastPong() {
        return this.lastPong;
    }
    
    public final long getLastPing() {
        return this.lastPing;
    }
    
    public void pongReceived() {
        this.lastPong = System.currentTimeMillis();
    }
    
    public void sendPing() {
        this.lastPing = System.currentTimeMillis();
        this.session.writeAndFlush((Object)LoginPacket.getPing());
        PingTimer.getInstance().schedule((Runnable)new Runnable() {
            @Override
            public void run() {
                try {
                    if (MapleClient.this.getLatency() < 0) {
                        closeseesion = true;
                        MapleClient.this.setReceiving(false);
                        MapleClient.this.updateLoginState(0, MapleClient.this.getSessionIPAddress());
                        MapleClient.this.getSession().close();
                    }
                }
                catch (NullPointerException e) {
                    closeseesion = true;
                    MapleClient.this.getSession().close();
                }
            }
        }, 30000L);
    }
    
    public boolean canClickNPC() {
        return this.lastNpcClick + 100L < System.currentTimeMillis();
    }
    
    public void setClickedNPC() {
        this.lastNpcClick = System.currentTimeMillis();
    }
    
    public void removeClickedNPC() {
        this.lastNpcClick = 0L;
    }
    
    public static String getLogMessage(final MapleClient cfor, final String message) {
        return getLogMessage(cfor, message, new Object[0]);
    }
    
    public static String getLogMessage(final MapleCharacter cfor, final String message) {
        return getLogMessage((cfor == null) ? null : cfor.getClient(), message);
    }
    
    public static String getLogMessage(final MapleCharacter cfor, final String message, final Object... parms) {
        return getLogMessage((cfor == null) ? null : cfor.getClient(), message, parms);
    }
    
    public static String getLogMessage(final MapleClient cfor, final String message, final Object... parms) {
        final StringBuilder builder = new StringBuilder();
        if (cfor != null) {
            if (cfor.getPlayer() != null) {
                builder.append("<");
                builder.append(MapleCharacterUtil.makeMapleReadable(cfor.getPlayer().getName()));
                builder.append(" (cid: ");
                builder.append(cfor.getPlayer().getId());
                builder.append(")> ");
            }
            if (cfor.getAccountName() != null) {
                builder.append("(Account: ");
                builder.append(cfor.getAccountName());
                builder.append(") ");
            }
        }
        builder.append(message);
        for (final Object parm : parms) {
            final int start = builder.indexOf("{}");
            builder.replace(start, start + 2, parm.toString());
        }
        return builder.toString();
    }
    
    public static int findAccIdForCharacterName(final String charName) {
        try (Connection con = DBConPool.getConnection()) {
            int ret;
            try (final PreparedStatement ps = con.prepareStatement("SELECT accountid FROM characters WHERE name = ?")) {
                ps.setString(1, charName);
                try (final ResultSet rs = ps.executeQuery()) {
                    ret = -1;
                    if (rs.next()) {
                        ret = rs.getInt("accountid");
                    }
                }
            }
            return ret;
        }
        catch (SQLException e) {
            System.err.println("findAccIdForCharacterName SQL error");
            FileoutputUtil.outError("logs/资料库异常.txt", (Throwable)e);
            return -1;
        }
    }
    
    public boolean isGm() {
        return this.gmLevel > PlayerGMRank.普通玩家.getLevel();
    }
    
    public boolean isSuperGM() {
        return this.gmLevel >= PlayerGMRank.超级管理员.getLevel();
    }
    
    public boolean isGod() {
        return this.gmLevel >= PlayerGMRank.神.getLevel();
    }
    
    public int getGmLevel() {
        return this.gmLevel;
    }
    
    public void setGmLevel(final int gmLevel) {
        this.gmLevel = gmLevel;
    }
    
    public void setVip(final int x) {
        try (Connection con = DBConPool.getConnection()) {
            final PreparedStatement ps = con.prepareStatement("Update Accounts set vip = ? Where id = ?");
            ps.setInt(1, x);
            ps.setInt(2, this.getAccID());
            ps.execute();
            ps.close();
        }
        catch (SQLException ex) {
            FilePrinter.printError("MapleCharacter.txt", (Throwable)ex, "SetVip");
            System.err.println("[vip]无法连接资料库");
            FileoutputUtil.outError("logs/资料库异常.txt", (Throwable)ex);
        }
        catch (Exception ex2) {
            FilePrinter.printError("MapleCharacter.txt", (Throwable)ex2, "SetVip");
            System.err.println("[setvip]" + (Object)ex2);
            FileoutputUtil.outError("logs/资料库异常.txt", (Throwable)ex2);
        }
    }
    public void useSkill(MapleCharacter from,final int skill, final int level) {
        if (level <= 0) {
            return;
        }
        try {
            Objects.requireNonNull(SkillFactory.getSkill(skill)).getEffect(level).applyTo(from);
        } catch (Exception e) {
            //System.out.println("Error using skill " + skill + " with level " + level + " on " + from.getName() + ": " + e);
        }

    }
    public int getVip() {
        return this.vip;
    }
    
    public void setScriptEngine(final String name, final ScriptEngine e) {
        this.engines.put(name, e);
    }
    
    public final ScriptEngine getScriptEngine(final String name) {
        return (ScriptEngine)this.engines.get((Object)name);
    }
    
    public void removeScriptEngine(final String name) {
        this.engines.remove((Object)name);
    }
    
    public final ScheduledFuture<?> getIdleTask() {
        return this.idleTask;
    }
    
    public void setIdleTask(final ScheduledFuture<?> idleTask) {
        this.idleTask = idleTask;
    }
    
    public int getCharacterSlots() {
        if (this.isGm()) {
            return 15;
        }
        if (this.charslots != 3) {
            return this.charslots;
        }
        try (Connection con = DBConPool.getConnection();
             final PreparedStatement ps = con.prepareStatement("SELECT charslots FROM character_slots WHERE accid = ? AND worldid = ?")) {
            ps.setInt(1, this.accountId);
            ps.setInt(2, this.world);
            try (final ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    this.charslots = rs.getInt("charslots");
                }
                else {
                    try (final PreparedStatement psu = con.prepareStatement("INSERT INTO character_slots (accid, worldid, charslots) VALUES (?, ?, ?)")) {
                        psu.setInt(1, this.accountId);
                        psu.setInt(2, this.world);
                        psu.setInt(3, this.charslots);
                        psu.executeUpdate();
                    }
                }
            }
        }
        catch (SQLException sqlE) {
            FileoutputUtil.outError("logs/资料库异常.txt", (Throwable)sqlE);
        }
        return this.charslots;
    }
    
    public boolean gainCharacterSlot() {
        if (this.getCharacterSlots() >= 15) {
            return false;
        }
        ++this.charslots;
        try (Connection con = DBConPool.getConnection();
             final PreparedStatement ps = con.prepareStatement("UPDATE character_slots SET charslots = ? WHERE worldid = ? AND accid = ?")) {
            ps.setInt(1, this.charslots);
            ps.setInt(2, this.world);
            ps.setInt(3, this.accountId);
            ps.executeUpdate();
        }
        catch (SQLException sqlE) {
            FileoutputUtil.outError("logs/资料库异常.txt", (Throwable)sqlE);
            return false;
        }
        return true;
    }
    
    public static byte unbanIPMacs(final String charname) {
        try (Connection con = DBConPool.getConnection()) {
            PreparedStatement ps = con.prepareStatement("SELECT accountid from characters where name = ?");
            ps.setString(1, charname);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                rs.close();
                ps.close();
                return -1;
            }
            final int accid = rs.getInt(1);
            rs.close();
            ps.close();
            ps = con.prepareStatement("SELECT sessionIP, macs FROM accounts WHERE id = ?");
            ps.setInt(1, accid);
            rs = ps.executeQuery();
            if (!rs.next()) {
                rs.close();
                ps.close();
                return -1;
            }
            final String sessionIP = rs.getString("sessionIP");
            final String macs = rs.getString("macs");
            rs.close();
            ps.close();
            byte ret = 0;
            if (sessionIP != null) {
                try (final PreparedStatement psa = con.prepareStatement("DELETE FROM ipbans WHERE ip = ?")) {
                    psa.setString(1, sessionIP);
                    psa.execute();
                }
                ++ret;
            }
            if (macs != null) {
                final String[] split;
                final String[] macz = split = macs.split(", ");
                for (final String mac : split) {
                    if (!mac.equals((Object)"")) {
                        try (final PreparedStatement psa2 = con.prepareStatement("DELETE FROM macbans WHERE mac = ?")) {
                            psa2.setString(1, mac);
                            psa2.execute();
                        }
                    }
                }
                ++ret;
            }
            return ret;
        }
        catch (SQLException e) {
            FileoutputUtil.outError("logs/资料库异常.txt", (Throwable)e);
            System.err.println("Error while unbanning" + (Object)e);
            return -2;
        }
    }
    
    public static byte unbanIP(final String charname) {
        try (Connection con = DBConPool.getConnection()) {
            PreparedStatement ps = con.prepareStatement("SELECT accountid from characters where name = ?");
            ps.setString(1, charname);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                rs.close();
                ps.close();
                return -1;
            }
            final int accid = rs.getInt(1);
            rs.close();
            ps.close();
            ps = con.prepareStatement("SELECT sessionIP FROM accounts WHERE id = ?");
            ps.setInt(1, accid);
            rs = ps.executeQuery();
            if (!rs.next()) {
                rs.close();
                ps.close();
                return -1;
            }
            final String sessionIP = rs.getString("sessionIP");
            rs.close();
            ps.close();
            byte ret = 0;
            if (sessionIP != null) {
                try (final PreparedStatement psa = con.prepareStatement("DELETE FROM ipbans WHERE ip = ?")) {
                    psa.setString(1, sessionIP);
                    psa.execute();
                }
                ++ret;
            }
            return ret;
        }
        catch (SQLException e) {
            FileoutputUtil.outError("logs/资料库异常.txt", (Throwable)e);
            System.err.println("Error while unbanning" + (Object)e);
            return -2;
        }
    }
    
    public static byte unHellban(final String charname) {
        try (Connection con = DBConPool.getConnection()) {
            PreparedStatement ps = con.prepareStatement("SELECT accountid from characters where name = ?");
            ps.setString(1, charname);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                rs.close();
                ps.close();
                return -1;
            }
            final int accid = rs.getInt(1);
            rs.close();
            ps.close();
            ps = con.prepareStatement("SELECT sessionIP, email FROM accounts WHERE id = ?");
            ps.setInt(1, accid);
            rs = ps.executeQuery();
            if (!rs.next()) {
                rs.close();
                ps.close();
                return -1;
            }
            final String sessionIP = rs.getString("sessionIP");
            final String email = rs.getString("email");
            rs.close();
            ps.close();
            ps = con.prepareStatement("UPDATE accounts SET banned = 0, banreason = '' WHERE email = ?" + ((sessionIP == null) ? "" : " OR sessionIP = ?"));
            ps.setString(1, email);
            if (sessionIP != null) {
                ps.setString(2, sessionIP);
            }
            ps.execute();
            ps.close();
            return 0;
        }
        catch (SQLException e) {
            System.err.println("Error while unbanning" + (Object)e);
            FileoutputUtil.outError("logs/资料库异常.txt", (Throwable)e);
            return -2;
        }
    }
    
    public static List<Integer> getLoggedIdsFromDB(final int state) {
        final List<Integer> ret = new ArrayList<Integer>();
        try (Connection con = DBConPool.getConnection()) {
            final PreparedStatement ps = con.prepareStatement("SELECT id from accounts where loggedin = ?");
            ps.setInt(1, state);
            final ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                ret.add(Integer.valueOf(rs.getInt("id")));
            }
        }
        catch (SQLException ex) {
            FileoutputUtil.outError("logs/资料库异常.txt", (Throwable)ex);
        }
        return ret;
    }
    
    public boolean isMonitored() {
        return this.monitored;
    }
    
    public void setMonitored(final boolean m) {
        this.monitored = m;
    }
    
    public boolean isReceiving() {
        return this.receiving;
    }
    
    public void setReceiving(final boolean m) {
        this.receiving = m;
    }
    
    public void sendPacket(final byte[] packet) {
        this.getSession().writeAndFlush((Object)packet);
    }
    
    public static boolean banMacs(final String macData) {
        if (macData.equalsIgnoreCase("00-00-00-00-00-00") || macData.length() != 17) {
            return false;
        }
        try (Connection con = DBConPool.getConnection();
             final PreparedStatement ps = con.prepareStatement("INSERT INTO macbans (mac) VALUES (?)")) {
            ps.setString(1, macData);
            ps.executeUpdate();
            ps.close();
        }
        catch (SQLException e) {
            System.err.println("Error banning MACs" + (Object)e);
            FileoutputUtil.outError("logs/资料库异常.txt", (Throwable)e);
            return false;
        }
        return true;
    }
    
    public final Set<String> getMacs() {
        return this.macs;
    }
    
    public boolean isBannedMac(final String mac) {
        if (mac.equalsIgnoreCase("00-00-00-00-00-00") || mac.length() != 17) {
            return false;
        }
        boolean ret = false;
        try (Connection con = DBConPool.getConnection();
             final PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM macbans WHERE mac = ?")) {
            ps.setString(1, mac);
            try (final ResultSet rs = ps.executeQuery()) {
                rs.next();
                if (rs.getInt(1) > 0) {
                    ret = true;
                }
            }
            ps.close();
        }
        catch (SQLException ex) {
            System.err.println("Error checking mac bans" + (Object)ex);
            FileoutputUtil.outError("logs/资料库异常.txt", (Throwable)ex);
        }
        return ret;
    }
    
    public boolean hasBannedMac() {
        if (this.macs.isEmpty()) {
            return false;
        }
        boolean ret = false;
        try (Connection con = DBConPool.getConnection()) {
            final StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM macbans WHERE mac IN (");
            for (int i = 0; i < this.macs.size(); ++i) {
                sql.append("?");
                if (i != this.macs.size() - 1) {
                    sql.append(", ");
                }
            }
            sql.append(")");
            try (final PreparedStatement ps = con.prepareStatement(sql.toString())) {
                int i = 0;
                for (final String mac : this.macs) {
                    ++i;
                    ps.setString(i, mac);
                }
                try (final ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    if (rs.getInt(1) > 0) {
                        ret = true;
                    }
                }
            }
        }
        catch (SQLException ex) {
            System.err.println("Error checking mac bans" + (Object)ex);
            FileoutputUtil.outError("logs/资料库异常.txt", (Throwable)ex);
        }
        return ret;
    }
    
    private void loadMacsIfNescessary() throws SQLException {
        if (this.macs.isEmpty()) {
            try (Connection con = DBConPool.getConnection();
                 final PreparedStatement ps = con.prepareStatement("SELECT macs FROM accounts WHERE id = ?")) {
                ps.setInt(1, this.accountId);
                try (final ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        rs.close();
                        ps.close();
                        throw new RuntimeException("No valid account associated with this client.");
                    }
                    if (rs.getString("macs") != null) {
                        final String[] split;
                        final String[] macData = split = rs.getString("macs").split(", ");
                        for (final String mac : split) {
                            if (!mac.equals((Object)"")) {
                                this.macs.add(mac);
                            }
                        }
                    }
                }
            }
            catch (SQLException ex) {
                //Ex.printStackTrace();
                FileoutputUtil.outError("logs/资料库异常.txt", (Throwable)ex);
            }
        }
    }
    
    public void banMacs() {
        try {
            this.loadMacsIfNescessary();
            if (this.macs.size() > 0) {
                final String[] macBans = new String[this.macs.size()];
                int z = 0;
                for (final String mac : this.macs) {
                    macBans[z] = mac;
                    ++z;
                }
                banMacs(macBans);
            }
        }
        catch (SQLException ex) {}
    }
    
    public static void banMacs(final String[] macs) {
        try (Connection con = DBConPool.getConnection()) {
            final List<String> filtered = new LinkedList<String>();
            PreparedStatement ps = con.prepareStatement("SELECT filter FROM macfilters");
            final ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                filtered.add(rs.getString("filter"));
            }
            rs.close();
            ps.close();
            ps = con.prepareStatement("INSERT INTO macbans (mac) VALUES (?)");
            for (final String mac : macs) {
                boolean matched = false;
                for (final String filter : filtered) {
                    if (mac.matches(filter)) {
                        matched = true;
                        break;
                    }
                }
                if (!matched) {
                    ps.setString(1, mac);
                    try {
                        ps.executeUpdate();
                    }
                    catch (SQLException ex) {}
                }
            }
            ps.close();
        }
        catch (SQLException e) {
            System.err.println("Error banning MACs" + (Object)e);
            FileoutputUtil.outError("logs/资料库异常.txt", (Throwable)e);
        }
    }
    
    public List<String> loadCharacterNamesByCharId(final int charId) {
        final List<String> chars = new LinkedList<String>();
        try (Connection con = DBConPool.getConnection() ) {
            PreparedStatement ps = con.prepareStatement("SELECT id,name FROM characters WHERE accountid= (SELECT accountid FROM characters where id=?)");
            ps.setInt(1, charId);
            try (final ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    chars.add(rs.getString("name"));
                }
            }
            ps.close();
        }
        catch (SQLException e) {
            System.err.println("error loading characters internal" + (Object)e);
            FileoutputUtil.outputFileError("logs/资料库异常.txt", (Throwable)e);
        }
        return chars;
    }
    
    public int loadLogGedin(final int accountID) {
        int login = 0;
        try (Connection con = DBConPool.getConnection()) {
            final PreparedStatement ps = con.prepareStatement("SELECT loggedin FROM accounts WHERE id = ?");
            ps.setInt(1, accountID);
            final ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                login = rs.getShort("loggedin");
                ps.close();
                rs.close();
            }
        }
        catch (SQLException e) {
            FileoutputUtil.outError("logs/资料库异常.txt", (Throwable)e);
        }
        return login;
    }
    
    public void setLoginKey(final String key) {
        this.loginKey = key;
    }
    
    public int getLoginKeya() {
        return this.loginKeya;
    }
    
    public void setLoginKeya(final int keya) {
        this.loginKeya = keya;
    }
    
    public String getLoginKey() {
        return this.loginKey;
    }
    
    public String loadLoginKey() {
        String loginkey = null;
        try (Connection con = DBConPool.getConnection()) {
            final PreparedStatement ps = con.prepareStatement("SELECT loginkey FROM accounts WHERE id = ?");
            ps.setInt(1, this.getAccID());
            final ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                loginkey = rs.getString("loginkey");
                ps.close();
                rs.close();
            }
        }
        catch (SQLException e) {
            FileoutputUtil.outError("logs/资料库异常.txt", (Throwable)e);
        }
        return loginkey;
    }
    
    public void setServerKey(final String key) {
        this.serverKey = key;
    }
    
    public String getServerKey() {
        return this.serverKey;
    }
    
    public String loadServerKey() {
        String serverkey = null;
        try (Connection con = DBConPool.getConnection()) {
            final PreparedStatement ps = con.prepareStatement("SELECT serverkey FROM accounts WHERE id = ?");
            ps.setInt(1, this.getAccID());
            final ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                serverkey = rs.getString("serverkey");
                ps.close();
                rs.close();
            }
        }
        catch (SQLException e) {
            FileoutputUtil.outError("logs/资料库异常.txt", (Throwable)e);
        }
        return serverkey;
    }
    
    public void updateLoginKey(final String loginkey) {
        try (Connection con = DBConPool.getConnection()) {
            final PreparedStatement ps = con.prepareStatement("UPDATE accounts SET loginkey = ? WHERE id = ?");
            ps.setString(1, loginkey);
            ps.setInt(2, this.getAccID());
            ps.executeUpdate();
        }
        catch (SQLException e) {
            FileoutputUtil.outError("logs/资料库异常.txt", (Throwable)e);
        }
    }
    
    public void updateServerKey(final String serverkey) {
        try (Connection con = DBConPool.getConnection()) {
            final PreparedStatement ps = con.prepareStatement("UPDATE accounts SET serverkey = ? WHERE id = ?");
            ps.setString(1, serverkey);
            ps.setInt(2, this.getAccID());
            ps.executeUpdate();
        }
        catch (SQLException e) {
            FileoutputUtil.outError("logs/资料库异常.txt", (Throwable)e);
        }
    }
    
    public void updateClientKey(final String clientkey) {
        try (Connection con = DBConPool.getConnection()) {
            final PreparedStatement ps = con.prepareStatement("UPDATE accounts SET clientkey = ? WHERE id = ?");
            ps.setString(1, clientkey);
            ps.setInt(2, this.getAccID());
            ps.executeUpdate();
            ps.close();
        }
        catch (SQLException e) {
            FileoutputUtil.outError("logs/资料库异常.txt", (Throwable)e);
        }
    }
    
    public static byte setTGJF(final String charname, final int x) {
        try (Connection con = DBConPool.getConnection()) {
            PreparedStatement ps = con.prepareStatement("SELECT accountid from characters where name = ?");
            ps.setString(1, charname);
            final ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                rs.close();
                ps.close();
                return -1;
            }
            final int accid = rs.getInt(1);
            rs.close();
            ps.close();
            ps = con.prepareStatement("UPDATE accounts SET TGJF = ? WHERE id = ?");
            ps.setInt(1, x);
            ps.setInt(2, accid);
            ps.executeUpdate();
            ps.close();
        }
        catch (SQLException e) {
            System.err.println("Error while unbanning" + (Object)e);
            FileoutputUtil.outError("logs/资料库异常.txt", (Throwable)e);
            return -2;
        }
        return 0;
    }
    
    public static int getTGJF(final int accid) {
        try (Connection con = DBConPool.getConnection()) {
            final PreparedStatement ps = con.prepareStatement("SELECT TGJF FROM accounts WHERE id = ?");
            ps.setInt(1, accid);
            int ret;
            try (final ResultSet rs = ps.executeQuery()) {
                ret = -1;
                if (rs.next()) {
                    ret = rs.getInt("TGJF");
                }
            }
            return ret;
        }
        catch (SQLException e) {
            FileoutputUtil.outError("logs/资料库异常.txt", (Throwable)e);
            return -1;
        }
    }
    
    public void updateMacs(final String macs) {
        try (Connection con = DBConPool.getConnection()) {
            final PreparedStatement ps = con.prepareStatement("UPDATE accounts SET macs = ? WHERE id = ?");
            ps.setString(1, macs);
            ps.setInt(2, this.getAccID());
            ps.executeUpdate();
        }
        catch (SQLException e) {
            FileoutputUtil.outError("logs/资料库异常.txt", (Throwable)e);
        }
    }
    
    public static byte setTJJF(final String charname, final int x) {
        try (Connection con = DBConPool.getConnection()) {
            PreparedStatement ps = con.prepareStatement("SELECT accountid from characters where name = ?");
            ps.setString(1, charname);
            final ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                rs.close();
                ps.close();
                return -1;
            }
            final int accid = rs.getInt(1);
            rs.close();
            ps.close();
            ps = con.prepareStatement("UPDATE accounts SET TJJF = ? WHERE id = ?");
            ps.setInt(1, x);
            ps.setInt(2, accid);
            ps.executeUpdate();
            ps.close();
        }
        catch (SQLException e) {
            System.err.println("Error while unbanning" + (Object)e);
            FileoutputUtil.outError("logs/资料库异常.txt", (Throwable)e);
            return -2;
        }
        return 0;
    }
    
    public static int getTJJF(final int accid) {
        try (Connection con = DBConPool.getConnection()) {
            final PreparedStatement ps = con.prepareStatement("SELECT TJJF FROM accounts WHERE id = ?");
            ps.setInt(1, accid);
            int ret;
            try (final ResultSet rs = ps.executeQuery()) {
                ret = -1;
                if (rs.next()) {
                    ret = rs.getInt("TJJF");
                }
            }
            return ret;
        }
        catch (SQLException e) {
            FileoutputUtil.outError("logs/资料库异常.txt", (Throwable)e);
            return -1;
        }
    }
    
    public boolean dangerousIp(final String lip) {
        final String ip = lip.substring(1, lip.lastIndexOf(58));
        boolean ret = false;
        try (Connection con = DBConPool.getConnection();
             final PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM dangerousip WHERE ? LIKE CONCAT(ip, '%')")) {
            ps.setString(1, ip);
            try (final ResultSet rs = ps.executeQuery()) {
                rs.next();
                if (rs.getInt(1) > 0) {
                    ret = true;
                }
            }
        }
        catch (SQLException ex) {
            System.err.println("Error dangerousIp " + (Object)ex);
            FileoutputUtil.outError("logs/资料库异常.txt", (Throwable)ex);
        }
        return ret;
    }
    
    public void setDangerousIp(final String lip) {
        final String ip = lip.substring(1, lip.lastIndexOf(58));
        try (Connection con = DBConPool.getConnection()) {
            final PreparedStatement ps = con.prepareStatement("INSERT INTO dangerousip (ip) VALUES (?)");
            ps.setString(1, ip);
            ps.executeUpdate();
            ps.close();
        }
        catch (SQLException Wx) {
            FileoutputUtil.outError("logs/资料库异常.txt", (Throwable)Wx);
        }
    }
    
    public int getMacsCout(final byte loggedin, final String macs) {
        try (Connection con = DBConPool.getConnection()) {
            final PreparedStatement ps = con.prepareStatement("select count(*) from accounts where loggedin > ? and macs = ?");
            ps.setByte(1, loggedin);
            ps.setString(2, macs);
            int ret_count;
            try (final ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    ret_count = rs.getInt(1);
                }
                else {
                    ret_count = -1;
                }
            }
            ps.close();
            return ret_count;
        }
        catch (SQLException Ex) {
            FileoutputUtil.outError("logs/资料库异常.txt", (Throwable)Ex);
            return -1;
        }
    }
    
    public long getLastLoginTime() {
        return this.lastLoginTime;
    }
    
    public void setLastLoginTime(final long lastLoginTime) {
        this.lastLoginTime = lastLoginTime;
    }
    
    public void unLockDisconnect() {
        this.getSession().writeAndFlush((Object)MaplePacketCreator.serverNotice(1, "當前账号在別處登入\r\n若不是你本人操作请及時更改密碼。"));
        this.disconnect(this.serverTransition, this.getChannel() == -10);
        this.getSession().close();
        this.closeseesion = true;
        final MapleClient client = this;
        final Thread closeSession = new Thread() {
            @Override
            public void run() {
                try {
                    Thread.sleep(3000L);
                }
                catch (InterruptedException ex) {}
                client.getSession().close();
            }
        };
        try {
            closeSession.start();
        }
        catch (Exception ex) {}
    }
    
    public boolean getCloseSession() {
        return this.closeseesion;
    }
    
    public boolean isCanloginpw() {
        return this.canloginpw;
    }
    
    public void setCanloginpw(final boolean x) {
        this.canloginpw = x;
    }
    
    public List<String> loadCharacterNamesByAccId(final int accId) {
        final List<String> Acc = new LinkedList<String>();
        try (Connection con = DBConPool.getConnection()) {
            final PreparedStatement ps = con.prepareStatement("SELECT name FROM characters WHERE accountid = ?");
            ps.setInt(1, accId);
            final ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Acc.add(rs.getString("name"));
            }
        }
        catch (SQLException e) {
            System.err.println("error loading characters names by id " + (Object)e);
        }
        return Acc;
    }
    
    public List<Integer> loadCharacterIDsByAccId(final int accId) {
        final List<Integer> Acc = new LinkedList<Integer>();
        try (Connection con = DBConPool.getConnection()) {
            final PreparedStatement ps = con.prepareStatement("SELECT id FROM characters WHERE accountid = ?");
            ps.setInt(1, accId);
            final ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Acc.add(Integer.valueOf(rs.getInt("id")));
            }
        }
        catch (SQLException e) {
            System.err.println("error loading characters cids by id " + (Object)e);
        }
        return Acc;
    }
    
    public boolean hasCheck(final int accid) {
        boolean ret = false;
        Connection con = DatabaseConnection.getConnection();
        try {
            final PreparedStatement ps = con.prepareStatement("SELECT * FROM accounts WHERE id = ?");
            ps.setInt(1, accid);
            final ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                ret = (rs.getInt("check") > 0);
            }
            rs.close();
            ps.close();
            con.close();
        }
        catch (SQLException ex) {
            System.err.println("Error checking ip Check" + (Object)ex);
        }finally {
            try {
                con.close();
            } catch (SQLException e) {}
        }
        return ret;
    }
    public static void resetForceLogin() {
        forceLogining = false;
    }
    static {
        MapleClient.离线挂机 = Boolean.parseBoolean(ServerProperties.getProperty("LtMS.离线挂机"));
        CLIENT_KEY = AttributeKey.valueOf("Client");
        loginMutex = new ReentrantLock(true);
        forceLogining = false;
    }
    
    protected static final class CharNameAndId
    {
        public final String name;
        public final int id;
        
        public CharNameAndId(final String name, final int id) {
            this.name = name;
            this.id = id;
        }
    }

    public boolean canClickPortal() {
        if(this.lastPortalClick == 0L){
            return true;
        }
        return (this.lastPortalClick + 2000L) < System.currentTimeMillis();
    }



    public void setClickedPortal() {
        this.lastPortalClick = System.currentTimeMillis();
    }

    public void removeClickedPortal() {
        this.lastPortalClick = 0L;
    }

}
