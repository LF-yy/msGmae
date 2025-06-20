package handling.channel;

import client.MapleClient;

import server.Start;
import tools.FileoutputUtil;
import database.DBConPool;
import server.Timer.WorldTimer;
import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.sql.Connection;
import java.sql.SQLException;
import database.DatabaseConnection;
import tools.MaplePacketCreator;

import java.util.*;
import java.util.stream.Collectors;

public class MapleGuildRanking
{
    int 职业排行显示;
    private static final MapleGuildRanking instance;
    private final List<GuildRankingInfo> ranks;
    private final List<levelRankingInfo> ranks1;
    private final List<mesoRankingInfo> ranks2;
    private final Map<Integer, List<JobRankingInfo>> JobRanks;
    private final List<JzRankingInfo> ranks3 = new LinkedList<>();
    private static List<levelRankingInfo> ranks_level = new LinkedList<>();
    private static List<mesoRankingInfo> ranks_meso = new LinkedList<>();
    private final List<levelRankingInfo> ranks剑客 ;
    private final List<levelRankingInfo> ranks勇士;
    private final List<levelRankingInfo> ranks英雄;
    private final List<levelRankingInfo> ranks枪战士;
    private final List<levelRankingInfo> ranks龙骑士;
    private final List<levelRankingInfo> ranks黑骑士;
    private final List<levelRankingInfo> ranks准骑士;
    private final List<levelRankingInfo> ranks骑士;
    private final List<levelRankingInfo> ranks圣骑士;
    private final List<levelRankingInfo> ranks火毒法师;
    private final List<levelRankingInfo> ranks火毒巫师;
    private final List<levelRankingInfo> ranks火毒魔导师;
    private final List<levelRankingInfo> ranks冰雷法师;
    private final List<levelRankingInfo> ranks冰雷巫师;
    private final List<levelRankingInfo> ranks冰雷魔导师;
    private final List<levelRankingInfo> ranks牧师;
    private final List<levelRankingInfo> ranks祭师;
    private final List<levelRankingInfo> ranks主教;
    private final List<levelRankingInfo> ranks猎人;
    private final List<levelRankingInfo> ranks射手;
    private final List<levelRankingInfo> ranks神射手;
    private final List<levelRankingInfo> ranks弩弓手;
    private final List<levelRankingInfo> ranks游侠;
    private final List<levelRankingInfo> ranks箭神;
    private final List<levelRankingInfo> ranks刺客;
    private final List<levelRankingInfo> ranks无影人;
    private final List<levelRankingInfo> ranks隐士;
    private final List<levelRankingInfo> ranks侠客;
    private final List<levelRankingInfo> ranks独行客;
    private final List<levelRankingInfo> ranks侠盗;
    private final List<levelRankingInfo> ranks拳手;
    private final List<levelRankingInfo> ranks斗士;
    private final List<levelRankingInfo> ranks冲锋队长;
    private final List<levelRankingInfo> ranks火枪手;
    private final List<levelRankingInfo> ranks大副;
    private final List<levelRankingInfo> ranks船长;

    private List<SponsorRank> sponsorRank;
    private List<SponsorRank> privDefeatRank;
    private List<SponsorRank> privDefeatRankNew;
    private List<SponsorRank> privBossRank;
    private List<SponsorRank> privRedRank;
    private List<SponsorRank> privAccurateRank;
    private List<SponsorRank> privEnhancedRank;
    private List<SponsorRank> privDropRank;
    public MapleGuildRanking() {
        this.职业排行显示 = 10;
        this.sponsorRank = new LinkedList<SponsorRank>();
        this.privDefeatRank = new LinkedList<SponsorRank>();
        this.privDefeatRankNew = new LinkedList<SponsorRank>();
        this.privBossRank = new LinkedList<SponsorRank>();
        this.privRedRank = new LinkedList<SponsorRank>();
        this.privAccurateRank = new LinkedList<SponsorRank>();
        this.privEnhancedRank = new LinkedList<SponsorRank>();
        this.privDropRank = new LinkedList<SponsorRank>();

        this.ranks = new LinkedList<GuildRankingInfo>();
        this.ranks1 = new LinkedList<levelRankingInfo>();
        this.ranks2 = new LinkedList<mesoRankingInfo>();
        this.JobRanks = new HashMap<Integer, List<JobRankingInfo>>();
        this.ranks剑客 = new LinkedList<levelRankingInfo>();
        this.ranks勇士 = new LinkedList<levelRankingInfo>();
        this.ranks英雄 = new LinkedList<levelRankingInfo>();
        this.ranks枪战士 = new LinkedList<levelRankingInfo>();
        this.ranks龙骑士 = new LinkedList<levelRankingInfo>();
        this.ranks黑骑士 = new LinkedList<levelRankingInfo>();
        this.ranks准骑士 = new LinkedList<levelRankingInfo>();
        this.ranks骑士 = new LinkedList<levelRankingInfo>();
        this.ranks圣骑士 = new LinkedList<levelRankingInfo>();
        this.ranks火毒法师 = new LinkedList<levelRankingInfo>();
        this.ranks火毒巫师 = new LinkedList<levelRankingInfo>();
        this.ranks火毒魔导师 = new LinkedList<levelRankingInfo>();
        this.ranks冰雷法师 = new LinkedList<levelRankingInfo>();
        this.ranks冰雷巫师 = new LinkedList<levelRankingInfo>();
        this.ranks冰雷魔导师 = new LinkedList<levelRankingInfo>();
        this.ranks牧师 = new LinkedList<levelRankingInfo>();
        this.ranks祭师 = new LinkedList<levelRankingInfo>();
        this.ranks主教 = new LinkedList<levelRankingInfo>();
        this.ranks猎人 = new LinkedList<levelRankingInfo>();
        this.ranks射手 = new LinkedList<levelRankingInfo>();
        this.ranks神射手 = new LinkedList<levelRankingInfo>();
        this.ranks弩弓手 = new LinkedList<levelRankingInfo>();
        this.ranks游侠 = new LinkedList<levelRankingInfo>();
        this.ranks箭神 = new LinkedList<levelRankingInfo>();
        this.ranks刺客 = new LinkedList<levelRankingInfo>();
        this.ranks无影人 = new LinkedList<levelRankingInfo>();
        this.ranks隐士 = new LinkedList<levelRankingInfo>();
        this.ranks侠客 = new LinkedList<levelRankingInfo>();
        this.ranks独行客 = new LinkedList<levelRankingInfo>();
        this.ranks侠盗 = new LinkedList<levelRankingInfo>();
        this.ranks拳手 = new LinkedList<levelRankingInfo>();
        this.ranks斗士 = new LinkedList<levelRankingInfo>();
        this.ranks冲锋队长 = new LinkedList<levelRankingInfo>();
        this.ranks火枪手 = new LinkedList<levelRankingInfo>();
        this.ranks大副 = new LinkedList<levelRankingInfo>();
        this.ranks船长 = new LinkedList<levelRankingInfo>();

    }
    
    public List<levelRankingInfo> 剑客() {
        if (this.ranks剑客.isEmpty()) {
            this.剑客职业排行();
        }
        return this.ranks剑客;
    }
    
    public void 剑客职业排行() {
        this.ranks剑客.clear();
        try {
            Connection con = DatabaseConnection.getConnection();
            final PreparedStatement ps = con.prepareStatement("SELECT * FROM characters WHERE job = 110  ORDER BY `level` DESC LIMIT " + this.职业排行显示 + "");
            final ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                final levelRankingInfo rank1 = new levelRankingInfo(rs.getString("name"), rs.getInt("level"), rs.getInt("str"), rs.getInt("dex"), rs.getInt("int"), rs.getInt("luk"));
                this.ranks剑客.add(rank1);
            }
            ps.close();
            rs.close();
        }
        catch (SQLException e) {
            System.err.println("剑客排行错误");
        }
    }
    
    public List<levelRankingInfo> 勇士() {
        if (this.ranks勇士.isEmpty()) {
            this.勇士职业排行();
        }
        return this.ranks勇士;
    }
    
    public void 勇士职业排行() {
        this.ranks勇士.clear();
        try {
            Connection con = DatabaseConnection.getConnection();
            final PreparedStatement ps = con.prepareStatement("SELECT * FROM characters WHERE job = 111  ORDER BY `level` DESC LIMIT " + this.职业排行显示 + "");
            final ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                final levelRankingInfo rank1 = new levelRankingInfo(rs.getString("name"), rs.getInt("level"), rs.getInt("str"), rs.getInt("dex"), rs.getInt("int"), rs.getInt("luk"));
                this.ranks勇士.add(rank1);
            }
            ps.close();
            rs.close();
        }
        catch (SQLException e) {
            System.err.println("勇士排行错误");
        }
    }
    
    public List<levelRankingInfo> 英雄() {
        if (this.ranks英雄.isEmpty()) {
            this.英雄职业排行();
        }
        return this.ranks英雄;
    }
    
    public void 英雄职业排行() {
        this.ranks英雄.clear();
        try {
            Connection con = DatabaseConnection.getConnection();
            final PreparedStatement ps = con.prepareStatement("SELECT * FROM characters WHERE job = 112  ORDER BY `level` DESC LIMIT " + this.职业排行显示 + "");
            final ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                final levelRankingInfo rank1 = new levelRankingInfo(rs.getString("name"), rs.getInt("level"), rs.getInt("str"), rs.getInt("dex"), rs.getInt("int"), rs.getInt("luk"));
                this.ranks英雄.add(rank1);
            }
            ps.close();
            rs.close();
        }
        catch (SQLException e) {
            System.err.println("英雄排行错误");
        }
    }
    
    public List<levelRankingInfo> 枪战士() {
        if (this.ranks枪战士.isEmpty()) {
            this.枪战士职业排行();
        }
        return this.ranks枪战士;
    }
    
    public void 枪战士职业排行() {
        this.ranks枪战士.clear();
        try {
            Connection con = DatabaseConnection.getConnection();
            final PreparedStatement ps = con.prepareStatement("SELECT * FROM characters WHERE job = 130  ORDER BY `level` DESC LIMIT " + this.职业排行显示 + "");
            final ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                final levelRankingInfo rank1 = new levelRankingInfo(rs.getString("name"), rs.getInt("level"), rs.getInt("str"), rs.getInt("dex"), rs.getInt("int"), rs.getInt("luk"));
                this.ranks枪战士.add(rank1);
            }
            ps.close();
            rs.close();
        }
        catch (SQLException e) {
            System.err.println("枪战士排行错误");
        }
    }
    
    public List<levelRankingInfo> 龙骑士() {
        if (this.ranks龙骑士.isEmpty()) {
            this.龙骑士职业排行();
        }
        return this.ranks龙骑士;
    }
    
    public void 龙骑士职业排行() {
        this.ranks龙骑士.clear();
        try {
            Connection con = DatabaseConnection.getConnection();
            final PreparedStatement ps = con.prepareStatement("SELECT * FROM characters WHERE job = 131  ORDER BY `level` DESC LIMIT " + this.职业排行显示 + "");
            final ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                final levelRankingInfo rank1 = new levelRankingInfo(rs.getString("name"), rs.getInt("level"), rs.getInt("str"), rs.getInt("dex"), rs.getInt("int"), rs.getInt("luk"));
                this.ranks龙骑士.add(rank1);
            }
            ps.close();
            rs.close();
        }
        catch (SQLException e) {
            System.err.println("龙骑士排行错误");
        }
    }
    
    public List<levelRankingInfo> 黑骑士() {
        if (this.ranks黑骑士.isEmpty()) {
            this.黑骑士职业排行();
        }
        return this.ranks黑骑士;
    }
    
    public void 黑骑士职业排行() {
        this.ranks黑骑士.clear();
        try {
            Connection con = DatabaseConnection.getConnection();
            final PreparedStatement ps = con.prepareStatement("SELECT * FROM characters WHERE job = 132  ORDER BY `level` DESC LIMIT " + this.职业排行显示 + "");
            final ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                final levelRankingInfo rank1 = new levelRankingInfo(rs.getString("name"), rs.getInt("level"), rs.getInt("str"), rs.getInt("dex"), rs.getInt("int"), rs.getInt("luk"));
                this.ranks黑骑士.add(rank1);
            }
            ps.close();
            rs.close();
        }
        catch (SQLException e) {
            System.err.println("黑骑士排行错误");
        }
    }
    
    public List<levelRankingInfo> 准骑士() {
        if (this.ranks准骑士.isEmpty()) {
            this.准骑士职业排行();
        }
        return this.ranks准骑士;
    }
    
    public void 准骑士职业排行() {
        this.ranks准骑士.clear();
        try {
            Connection con = DatabaseConnection.getConnection();
            final PreparedStatement ps = con.prepareStatement("SELECT * FROM characters WHERE job = 120  ORDER BY `level` DESC LIMIT " + this.职业排行显示 + "");
            final ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                final levelRankingInfo rank1 = new levelRankingInfo(rs.getString("name"), rs.getInt("level"), rs.getInt("str"), rs.getInt("dex"), rs.getInt("int"), rs.getInt("luk"));
                this.ranks准骑士.add(rank1);
            }
            ps.close();
            rs.close();
        }
        catch (SQLException e) {
            System.err.println("准骑士排行错误");
        }
    }
    
    public List<levelRankingInfo> 骑士() {
        if (this.ranks骑士.isEmpty()) {
            this.骑士职业排行();
        }
        return this.ranks骑士;
    }
    
    public void 骑士职业排行() {
        this.ranks骑士.clear();
        try {
            Connection con = DatabaseConnection.getConnection();
            final PreparedStatement ps = con.prepareStatement("SELECT * FROM characters WHERE job = 121  ORDER BY `level` DESC LIMIT " + this.职业排行显示 + "");
            final ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                final levelRankingInfo rank1 = new levelRankingInfo(rs.getString("name"), rs.getInt("level"), rs.getInt("str"), rs.getInt("dex"), rs.getInt("int"), rs.getInt("luk"));
                this.ranks骑士.add(rank1);
            }
            ps.close();
            rs.close();
        }
        catch (SQLException e) {
            System.err.println("骑士排行错误");
        }
    }
    
    public List<levelRankingInfo> 圣骑士() {
        if (this.ranks圣骑士.isEmpty()) {
            this.圣骑士职业排行();
        }
        return this.ranks圣骑士;
    }
    
    public void 圣骑士职业排行() {
        this.ranks圣骑士.clear();
        try {
            Connection con = DatabaseConnection.getConnection();
            final PreparedStatement ps = con.prepareStatement("SELECT * FROM characters WHERE job = 122  ORDER BY `level` DESC LIMIT " + this.职业排行显示 + "");
            final ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                final levelRankingInfo rank1 = new levelRankingInfo(rs.getString("name"), rs.getInt("level"), rs.getInt("str"), rs.getInt("dex"), rs.getInt("int"), rs.getInt("luk"));
                this.ranks圣骑士.add(rank1);
            }
            ps.close();
            rs.close();
        }
        catch (SQLException e) {
            System.err.println("圣骑士排行错误");
        }
    }
    
    public List<levelRankingInfo> 火毒法师() {
        if (this.ranks火毒法师.isEmpty()) {
            this.火毒法师职业排行();
        }
        return this.ranks火毒法师;
    }
    
    public void 火毒法师职业排行() {
        this.ranks火毒法师.clear();
        try {
            Connection con = DatabaseConnection.getConnection();
            final PreparedStatement ps = con.prepareStatement("SELECT * FROM characters WHERE job = 210  ORDER BY `level` DESC LIMIT " + this.职业排行显示 + "");
            final ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                final levelRankingInfo rank1 = new levelRankingInfo(rs.getString("name"), rs.getInt("level"), rs.getInt("str"), rs.getInt("dex"), rs.getInt("int"), rs.getInt("luk"));
                this.ranks火毒法师.add(rank1);
            }
            ps.close();
            rs.close();
        }
        catch (SQLException e) {
            System.err.println("火毒法师排行错误");
        }
    }
    
    public List<levelRankingInfo> 火毒巫师() {
        if (this.ranks火毒巫师.isEmpty()) {
            this.火毒巫师职业排行();
        }
        return this.ranks火毒巫师;
    }
    
    public void 火毒巫师职业排行() {
        this.ranks火毒巫师.clear();
        try {
            Connection con = DatabaseConnection.getConnection();
            final PreparedStatement ps = con.prepareStatement("SELECT * FROM characters WHERE job = 211  ORDER BY `level` DESC LIMIT " + this.职业排行显示 + "");
            final ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                final levelRankingInfo rank1 = new levelRankingInfo(rs.getString("name"), rs.getInt("level"), rs.getInt("str"), rs.getInt("dex"), rs.getInt("int"), rs.getInt("luk"));
                this.ranks火毒巫师.add(rank1);
            }
            ps.close();
            rs.close();
        }
        catch (SQLException e) {
            System.err.println("火毒巫师排行错误");
        }
    }
    
    public List<levelRankingInfo> 火毒魔导师() {
        if (this.ranks火毒魔导师.isEmpty()) {
            this.火毒魔导师职业排行();
        }
        return this.ranks火毒魔导师;
    }
    
    public void 火毒魔导师职业排行() {
        this.ranks火毒魔导师.clear();
        try {
            Connection con = DatabaseConnection.getConnection();
            final PreparedStatement ps = con.prepareStatement("SELECT * FROM characters WHERE job = 212  ORDER BY `level` DESC LIMIT " + this.职业排行显示 + "");
            final ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                final levelRankingInfo rank1 = new levelRankingInfo(rs.getString("name"), rs.getInt("level"), rs.getInt("str"), rs.getInt("dex"), rs.getInt("int"), rs.getInt("luk"));
                this.ranks火毒魔导师.add(rank1);
            }
            ps.close();
            rs.close();
        }
        catch (SQLException e) {
            System.err.println("火毒魔导师排行错误");
        }
    }
    
    public List<levelRankingInfo> 冰雷法师() {
        if (this.ranks冰雷法师.isEmpty()) {
            this.冰雷法师职业排行();
        }
        return this.ranks冰雷法师;
    }
    
    public void 冰雷法师职业排行() {
        this.ranks冰雷法师.clear();
        try {
            Connection con = DatabaseConnection.getConnection();
            final PreparedStatement ps = con.prepareStatement("SELECT * FROM characters WHERE job = 220  ORDER BY `level` DESC LIMIT " + this.职业排行显示 + "");
            final ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                final levelRankingInfo rank1 = new levelRankingInfo(rs.getString("name"), rs.getInt("level"), rs.getInt("str"), rs.getInt("dex"), rs.getInt("int"), rs.getInt("luk"));
                this.ranks冰雷法师.add(rank1);
            }
            ps.close();
            rs.close();
        }
        catch (SQLException e) {
            System.err.println("冰雷法师排行错误");
        }
    }
    
    public List<levelRankingInfo> 冰雷巫师() {
        if (this.ranks冰雷巫师.isEmpty()) {
            this.冰雷巫师职业排行();
        }
        return this.ranks冰雷巫师;
    }
    
    public void 冰雷巫师职业排行() {
        this.ranks冰雷巫师.clear();
        try {
            Connection con = DatabaseConnection.getConnection();
            final PreparedStatement ps = con.prepareStatement("SELECT * FROM characters WHERE job = 221  ORDER BY `level` DESC LIMIT " + this.职业排行显示 + "");
            final ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                final levelRankingInfo rank1 = new levelRankingInfo(rs.getString("name"), rs.getInt("level"), rs.getInt("str"), rs.getInt("dex"), rs.getInt("int"), rs.getInt("luk"));
                this.ranks冰雷巫师.add(rank1);
            }
            ps.close();
            rs.close();
        }
        catch (SQLException e) {
            System.err.println("冰雷巫师排行错误");
        }
    }
    
    public List<levelRankingInfo> 冰雷魔导师() {
        if (this.ranks冰雷魔导师.isEmpty()) {
            this.冰雷魔导师职业排行();
        }
        return this.ranks冰雷魔导师;
    }
    
    public void 冰雷魔导师职业排行() {
        this.ranks冰雷魔导师.clear();
        try {
            Connection con = DatabaseConnection.getConnection();
            final PreparedStatement ps = con.prepareStatement("SELECT * FROM characters WHERE job = 222  ORDER BY `level` DESC LIMIT " + this.职业排行显示 + "");
            final ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                final levelRankingInfo rank1 = new levelRankingInfo(rs.getString("name"), rs.getInt("level"), rs.getInt("str"), rs.getInt("dex"), rs.getInt("int"), rs.getInt("luk"));
                this.ranks冰雷魔导师.add(rank1);
            }
            ps.close();
            rs.close();
        }
        catch (SQLException e) {
            System.err.println("冰雷魔导师排行错误");
        }
    }
    
    public List<levelRankingInfo> 牧师() {
        if (this.ranks牧师.isEmpty()) {
            this.牧师职业排行();
        }
        return this.ranks牧师;
    }
    
    public void 牧师职业排行() {
        this.ranks牧师.clear();
        try {
            Connection con = DatabaseConnection.getConnection();
            final PreparedStatement ps = con.prepareStatement("SELECT * FROM characters WHERE job = 230  ORDER BY `level` DESC LIMIT " + this.职业排行显示 + "");
            final ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                final levelRankingInfo rank1 = new levelRankingInfo(rs.getString("name"), rs.getInt("level"), rs.getInt("str"), rs.getInt("dex"), rs.getInt("int"), rs.getInt("luk"));
                this.ranks牧师.add(rank1);
            }
            ps.close();
            rs.close();
        }
        catch (SQLException e) {
            System.err.println("牧师排行错误");
        }
    }
    
    public List<levelRankingInfo> 祭师() {
        if (this.ranks祭师.isEmpty()) {
            this.祭师职业排行();
        }
        return this.ranks祭师;
    }
    
    public void 祭师职业排行() {
        this.ranks祭师.clear();
        try {
            Connection con = DatabaseConnection.getConnection();
            final PreparedStatement ps = con.prepareStatement("SELECT * FROM characters WHERE job = 231  ORDER BY `level` DESC LIMIT " + this.职业排行显示 + "");
            final ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                final levelRankingInfo rank1 = new levelRankingInfo(rs.getString("name"), rs.getInt("level"), rs.getInt("str"), rs.getInt("dex"), rs.getInt("int"), rs.getInt("luk"));
                this.ranks祭师.add(rank1);
            }
            ps.close();
            rs.close();
        }
        catch (SQLException e) {
            System.err.println("祭师排行错误");
        }
    }
    
    public List<levelRankingInfo> 主教() {
        if (this.ranks主教.isEmpty()) {
            this.主教职业排行();
        }
        return this.ranks主教;
    }
    
    public void 主教职业排行() {
        this.ranks主教.clear();
        try {
            Connection con = DatabaseConnection.getConnection();
            final PreparedStatement ps = con.prepareStatement("SELECT * FROM characters WHERE job = 232  ORDER BY `level` DESC LIMIT " + this.职业排行显示 + "");
            final ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                final levelRankingInfo rank1 = new levelRankingInfo(rs.getString("name"), rs.getInt("level"), rs.getInt("str"), rs.getInt("dex"), rs.getInt("int"), rs.getInt("luk"));
                this.ranks主教.add(rank1);
            }
            ps.close();
            rs.close();
        }
        catch (SQLException e) {
            System.err.println("主教排行错误");
        }
    }
    
    public List<levelRankingInfo> 猎人() {
        if (this.ranks猎人.isEmpty()) {
            this.猎人职业排行();
        }
        return this.ranks猎人;
    }
    
    public void 猎人职业排行() {
        this.ranks猎人.clear();
        try {
            Connection con = DatabaseConnection.getConnection();
            final PreparedStatement ps = con.prepareStatement("SELECT * FROM characters WHERE job = 310  ORDER BY `level` DESC LIMIT " + this.职业排行显示 + "");
            final ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                final levelRankingInfo rank1 = new levelRankingInfo(rs.getString("name"), rs.getInt("level"), rs.getInt("str"), rs.getInt("dex"), rs.getInt("int"), rs.getInt("luk"));
                this.ranks猎人.add(rank1);
            }
            ps.close();
            rs.close();
        }
        catch (SQLException e) {
            System.err.println("猎人排行错误");
        }
    }
    
    public List<levelRankingInfo> 射手() {
        if (this.ranks射手.isEmpty()) {
            this.射手职业排行();
        }
        return this.ranks射手;
    }
    
    public void 射手职业排行() {
        this.ranks射手.clear();
        try {
            Connection con = DatabaseConnection.getConnection();
            final PreparedStatement ps = con.prepareStatement("SELECT * FROM characters WHERE job = 311  ORDER BY `level` DESC LIMIT " + this.职业排行显示 + "");
            final ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                final levelRankingInfo rank1 = new levelRankingInfo(rs.getString("name"), rs.getInt("level"), rs.getInt("str"), rs.getInt("dex"), rs.getInt("int"), rs.getInt("luk"));
                this.ranks射手.add(rank1);
            }
            ps.close();
            rs.close();
        }
        catch (SQLException e) {
            System.err.println("射手排行错误");
        }
    }
    
    public List<levelRankingInfo> 神射手() {
        if (this.ranks神射手.isEmpty()) {
            this.神射手职业排行();
        }
        return this.ranks神射手;
    }
    
    public void 神射手职业排行() {
        this.ranks神射手.clear();
        try {
            Connection con = DatabaseConnection.getConnection();
            final PreparedStatement ps = con.prepareStatement("SELECT * FROM characters WHERE job = 312  ORDER BY `level` DESC LIMIT " + this.职业排行显示 + "");
            final ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                final levelRankingInfo rank1 = new levelRankingInfo(rs.getString("name"), rs.getInt("level"), rs.getInt("str"), rs.getInt("dex"), rs.getInt("int"), rs.getInt("luk"));
                this.ranks神射手.add(rank1);
            }
            ps.close();
            rs.close();
        }
        catch (SQLException e) {
            System.err.println("神射手排行错误");
        }
    }
    
    public List<levelRankingInfo> 弩弓手() {
        if (this.ranks弩弓手.isEmpty()) {
            this.弩弓手职业排行();
        }
        return this.ranks弩弓手;
    }
    
    public void 弩弓手职业排行() {
        this.ranks弩弓手.clear();
        try {
            Connection con = DatabaseConnection.getConnection();
            final PreparedStatement ps = con.prepareStatement("SELECT * FROM characters WHERE job = 320  ORDER BY `level` DESC LIMIT " + this.职业排行显示 + "");
            final ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                final levelRankingInfo rank1 = new levelRankingInfo(rs.getString("name"), rs.getInt("level"), rs.getInt("str"), rs.getInt("dex"), rs.getInt("int"), rs.getInt("luk"));
                this.ranks弩弓手.add(rank1);
            }
            ps.close();
            rs.close();
        }
        catch (SQLException e) {
            System.err.println("弩弓手排行错误");
        }
    }
    
    public List<levelRankingInfo> 游侠() {
        if (this.ranks游侠.isEmpty()) {
            this.游侠职业排行();
        }
        return this.ranks游侠;
    }
    
    public void 游侠职业排行() {
        this.ranks游侠.clear();
        try {
            Connection con = DatabaseConnection.getConnection();
            final PreparedStatement ps = con.prepareStatement("SELECT * FROM characters WHERE job = 321  ORDER BY `level` DESC LIMIT " + this.职业排行显示 + "");
            final ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                final levelRankingInfo rank1 = new levelRankingInfo(rs.getString("name"), rs.getInt("level"), rs.getInt("str"), rs.getInt("dex"), rs.getInt("int"), rs.getInt("luk"));
                this.ranks游侠.add(rank1);
            }
            ps.close();
            rs.close();
        }
        catch (SQLException e) {
            System.err.println("游侠排行错误");
        }
    }
    
    public List<levelRankingInfo> 箭神() {
        if (this.ranks箭神.isEmpty()) {
            this.箭神职业排行();
        }
        return this.ranks箭神;
    }
    
    public void 箭神职业排行() {
        this.ranks箭神.clear();
        try {
            Connection con = DatabaseConnection.getConnection();
            final PreparedStatement ps = con.prepareStatement("SELECT * FROM characters WHERE job = 322  ORDER BY `level` DESC LIMIT " + this.职业排行显示 + "");
            final ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                final levelRankingInfo rank1 = new levelRankingInfo(rs.getString("name"), rs.getInt("level"), rs.getInt("str"), rs.getInt("dex"), rs.getInt("int"), rs.getInt("luk"));
                this.ranks箭神.add(rank1);
            }
            ps.close();
            rs.close();
        }
        catch (SQLException e) {
            System.err.println("箭神排行错误");
        }
    }
    
    public List<levelRankingInfo> 刺客() {
        if (this.ranks刺客.isEmpty()) {
            this.刺客职业排行();
        }
        return this.ranks刺客;
    }
    
    public void 刺客职业排行() {
        this.ranks刺客.clear();
        try {
            Connection con = DatabaseConnection.getConnection();
            final PreparedStatement ps = con.prepareStatement("SELECT * FROM characters WHERE job = 410  ORDER BY `level` DESC LIMIT " + this.职业排行显示 + "");
            final ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                final levelRankingInfo rank1 = new levelRankingInfo(rs.getString("name"), rs.getInt("level"), rs.getInt("str"), rs.getInt("dex"), rs.getInt("int"), rs.getInt("luk"));
                this.ranks刺客.add(rank1);
            }
            ps.close();
            rs.close();
        }
        catch (SQLException e) {
            System.err.println("刺客排行错误");
        }
    }
    
    public List<levelRankingInfo> 无影人() {
        if (this.ranks无影人.isEmpty()) {
            this.无影人职业排行();
        }
        return this.ranks无影人;
    }
    
    public void 无影人职业排行() {
        this.ranks无影人.clear();
        try {
            Connection con = DatabaseConnection.getConnection();
            final PreparedStatement ps = con.prepareStatement("SELECT * FROM characters WHERE job = 411  ORDER BY `level` DESC LIMIT " + this.职业排行显示 + "");
            final ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                final levelRankingInfo rank1 = new levelRankingInfo(rs.getString("name"), rs.getInt("level"), rs.getInt("str"), rs.getInt("dex"), rs.getInt("int"), rs.getInt("luk"));
                this.ranks无影人.add(rank1);
            }
            ps.close();
            rs.close();
        }
        catch (SQLException e) {
            System.err.println("无影人排行错误");
        }
    }
    
    public List<levelRankingInfo> 隐士() {
        if (this.ranks隐士.isEmpty()) {
            this.隐士职业排行();
        }
        return this.ranks隐士;
    }
    
    public void 隐士职业排行() {
        this.ranks隐士.clear();
        try {
            Connection con = DatabaseConnection.getConnection();
            final PreparedStatement ps = con.prepareStatement("SELECT * FROM characters WHERE job = 412  ORDER BY `level` DESC LIMIT " + this.职业排行显示 + "");
            final ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                final levelRankingInfo rank1 = new levelRankingInfo(rs.getString("name"), rs.getInt("level"), rs.getInt("str"), rs.getInt("dex"), rs.getInt("int"), rs.getInt("luk"));
                this.ranks隐士.add(rank1);
            }
            ps.close();
            rs.close();
        }
        catch (SQLException e) {
            System.err.println("隐士排行错误");
        }
    }
    
    public List<levelRankingInfo> 侠客() {
        if (this.ranks侠客.isEmpty()) {
            this.侠客职业排行();
        }
        return this.ranks侠客;
    }
    
    public void 侠客职业排行() {
        this.ranks侠客.clear();
        try {
            Connection con = DatabaseConnection.getConnection();
            final PreparedStatement ps = con.prepareStatement("SELECT * FROM characters WHERE job = 420  ORDER BY `level` DESC LIMIT " + this.职业排行显示 + "");
            final ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                final levelRankingInfo rank1 = new levelRankingInfo(rs.getString("name"), rs.getInt("level"), rs.getInt("str"), rs.getInt("dex"), rs.getInt("int"), rs.getInt("luk"));
                this.ranks侠客.add(rank1);
            }
            ps.close();
            rs.close();
        }
        catch (SQLException e) {
            System.err.println("侠客排行错误");
        }
    }
    
    public List<levelRankingInfo> 独行客() {
        if (this.ranks独行客.isEmpty()) {
            this.独行客职业排行();
        }
        return this.ranks独行客;
    }
    
    public void 独行客职业排行() {
        this.ranks独行客.clear();
        try {
            Connection con = DatabaseConnection.getConnection();
            final PreparedStatement ps = con.prepareStatement("SELECT * FROM characters WHERE job = 421  ORDER BY `level` DESC LIMIT " + this.职业排行显示 + "");
            final ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                final levelRankingInfo rank1 = new levelRankingInfo(rs.getString("name"), rs.getInt("level"), rs.getInt("str"), rs.getInt("dex"), rs.getInt("int"), rs.getInt("luk"));
                this.ranks独行客.add(rank1);
            }
            ps.close();
            rs.close();
        }
        catch (SQLException e) {
            System.err.println("独行客排行错误");
        }
    }
    
    public List<levelRankingInfo> 侠盗() {
        if (this.ranks侠盗.isEmpty()) {
            this.侠盗职业排行();
        }
        return this.ranks侠盗;
    }
    
    public void 侠盗职业排行() {
        this.ranks侠盗.clear();
        try {
            Connection con = DatabaseConnection.getConnection();
            final PreparedStatement ps = con.prepareStatement("SELECT * FROM characters WHERE job = 422  ORDER BY `level` DESC LIMIT " + this.职业排行显示 + "");
            final ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                final levelRankingInfo rank1 = new levelRankingInfo(rs.getString("name"), rs.getInt("level"), rs.getInt("str"), rs.getInt("dex"), rs.getInt("int"), rs.getInt("luk"));
                this.ranks侠盗.add(rank1);
            }
            ps.close();
            rs.close();
        }
        catch (SQLException e) {
            System.err.println("侠盗排行错误");
        }
    }
    
    public List<levelRankingInfo> 拳手() {
        if (this.ranks拳手.isEmpty()) {
            this.拳手职业排行();
        }
        return this.ranks拳手;
    }
    
    public void 拳手职业排行() {
        this.ranks拳手.clear();
        try {
            Connection con = DatabaseConnection.getConnection();
            final PreparedStatement ps = con.prepareStatement("SELECT * FROM characters WHERE job = 510  ORDER BY `level` DESC LIMIT " + this.职业排行显示 + "");
            final ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                final levelRankingInfo rank1 = new levelRankingInfo(rs.getString("name"), rs.getInt("level"), rs.getInt("str"), rs.getInt("dex"), rs.getInt("int"), rs.getInt("luk"));
                this.ranks拳手.add(rank1);
            }
            ps.close();
            rs.close();
        }
        catch (SQLException e) {
            System.err.println("拳手排行错误");
        }
    }
    
    public List<levelRankingInfo> 斗士() {
        if (this.ranks斗士.isEmpty()) {
            this.斗士职业排行();
        }
        return this.ranks斗士;
    }
    
    public void 斗士职业排行() {
        this.ranks斗士.clear();
        try {
            Connection con = DatabaseConnection.getConnection();
            final PreparedStatement ps = con.prepareStatement("SELECT * FROM characters WHERE job = 511  ORDER BY `level` DESC LIMIT " + this.职业排行显示 + "");
            final ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                final levelRankingInfo rank1 = new levelRankingInfo(rs.getString("name"), rs.getInt("level"), rs.getInt("str"), rs.getInt("dex"), rs.getInt("int"), rs.getInt("luk"));
                this.ranks斗士.add(rank1);
            }
            ps.close();
            rs.close();
        }
        catch (SQLException e) {
            System.err.println("斗士排行错误");
        }
    }
    
    public List<levelRankingInfo> 冲锋队长() {
        if (this.ranks冲锋队长.isEmpty()) {
            this.冲锋队长职业排行();
        }
        return this.ranks冲锋队长;
    }
    
    public void 冲锋队长职业排行() {
        this.ranks冲锋队长.clear();
        try {
            Connection con = DatabaseConnection.getConnection();
            final PreparedStatement ps = con.prepareStatement("SELECT * FROM characters WHERE job = 512  ORDER BY `level` DESC LIMIT " + this.职业排行显示 + "");
            final ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                final levelRankingInfo rank1 = new levelRankingInfo(rs.getString("name"), rs.getInt("level"), rs.getInt("str"), rs.getInt("dex"), rs.getInt("int"), rs.getInt("luk"));
                this.ranks冲锋队长.add(rank1);
            }
            ps.close();
            rs.close();
        }
        catch (SQLException e) {
            System.err.println("冲锋队长排行错误");
        }
    }
    
    public List<levelRankingInfo> 火枪手() {
        if (this.ranks火枪手.isEmpty()) {
            this.火枪手职业排行();
        }
        return this.ranks火枪手;
    }
    
    public void 火枪手职业排行() {
        this.ranks火枪手.clear();
        try {
            Connection con = DatabaseConnection.getConnection();
            final PreparedStatement ps = con.prepareStatement("SELECT * FROM characters WHERE job = 520  ORDER BY `level` DESC LIMIT " + this.职业排行显示 + "");
            final ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                final levelRankingInfo rank1 = new levelRankingInfo(rs.getString("name"), rs.getInt("level"), rs.getInt("str"), rs.getInt("dex"), rs.getInt("int"), rs.getInt("luk"));
                this.ranks火枪手.add(rank1);
            }
            ps.close();
            rs.close();
        }
        catch (SQLException e) {
            System.err.println("火枪手排行错误");
        }
    }
    
    public List<levelRankingInfo> 大副() {
        if (this.ranks大副.isEmpty()) {
            this.大副职业排行();
        }
        return this.ranks大副;
    }
    
    public void 大副职业排行() {
        this.ranks大副.clear();
        try {
            Connection con = DatabaseConnection.getConnection();
            final PreparedStatement ps = con.prepareStatement("SELECT * FROM characters WHERE job = 521  ORDER BY `level` DESC LIMIT " + this.职业排行显示 + "");
            final ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                final levelRankingInfo rank1 = new levelRankingInfo(rs.getString("name"), rs.getInt("level"), rs.getInt("str"), rs.getInt("dex"), rs.getInt("int"), rs.getInt("luk"));
                this.ranks大副.add(rank1);
            }
            ps.close();
            rs.close();
        }
        catch (SQLException e) {
            System.err.println("大副排行错误");
        }
    }
    
    public List<levelRankingInfo> 船长() {
        if (this.ranks船长.isEmpty()) {
            this.船长职业排行();
        }
        return this.ranks船长;
    }
    
    public void 船长职业排行() {
        this.ranks船长.clear();
        try {
            Connection con = DatabaseConnection.getConnection();
            final PreparedStatement ps = con.prepareStatement("SELECT * FROM characters WHERE job = 522  ORDER BY `level` DESC LIMIT " + this.职业排行显示 + "");
            final ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                final levelRankingInfo rank1 = new levelRankingInfo(rs.getString("name"), rs.getInt("level"), rs.getInt("str"), rs.getInt("dex"), rs.getInt("int"), rs.getInt("luk"));
                this.ranks船长.add(rank1);
            }
            ps.close();
            rs.close();
        }
        catch (SQLException e) {
            System.err.println("船长排行错误");
        }
    }

    /**
     * 赞助排行榜
     */
    public void querySponsorRank() {
        this.sponsorRank.clear();
        try {
            Connection con = DatabaseConnection.getConnection();
            final PreparedStatement ps = con.prepareStatement("select  a.name,b.moneyc,a.level,a.str,a.dex,a.int,a.luk from  characters as a " +
                    "left join accounts as b on a.accountid = b.id " +
                    "where b.moneyc!= 0 " +
                    "order by b.moneyc desc limit 10 ");
            final ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                final SponsorRank rank1 = new SponsorRank(rs.getString("name"), rs.getInt("moneyc"), rs.getInt("str"), rs.getInt("dex"), rs.getInt("int"), rs.getInt("luk"));
                this.sponsorRank.add(rank1);
            }
            ps.close();
            rs.close();
        }
        catch (SQLException e) {
            System.err.println("querySponsorRank错误");
        }
    }
    /**
     * 破功排行榜
     */
    public void queryDefeatRank() {
        this.privDefeatRank.clear();
        try {
            Connection con = DatabaseConnection.getConnection();
            final PreparedStatement ps = con.prepareStatement("select  aa.name,CONVERT(aa.PGSXDJ, UNSIGNED INTEGER) as PGSXDJ,aa.level,aa.str,aa.dex,aa.int,aa.luk   from  characters as aa" +
                    " where aa.PGSXDJ is not null " +
                    " order by aa.PGSXDJ desc limit 10 ");
            final ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                final SponsorRank rank1 = new SponsorRank(rs.getString("name"), rs.getInt("PGSXDJ"), rs.getInt("str"), rs.getInt("dex"), rs.getInt("int"), rs.getInt("luk"));
                this.privDefeatRank.add(rank1);
            }
            ps.close();
            rs.close();
        }
        catch (SQLException e) {
            System.err.println("queryDefeatRank错误");
        }
    }
    /**
     * 破功排行榜
     */
    public void queryDefeatRankNew() {
        this.privDefeatRankNew.clear();
        try {
            Connection con = DatabaseConnection.getConnection();
            final PreparedStatement ps = con.prepareStatement("select  aa.name,CONVERT(aa.max_damage, UNSIGNED LONG) as PGSXDJ,aa.level,aa.str,aa.dex,aa.int,aa.luk  from  characters as aa" +
                    " where aa.max_damage is not null " +
                    " order by aa.max_damage desc limit 10 ");
            final ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                final SponsorRank rank1 = new SponsorRank(rs.getString("name"), rs.getInt("max_damage"), rs.getInt("str"), rs.getInt("dex"), rs.getInt("int"), rs.getInt("luk"));
                this.privDefeatRankNew.add(rank1);
            }
            ps.close();
            rs.close();
        }
        catch (SQLException e) {
            System.err.println("queryDefeatRank错误");
        }
    }

    /**
     * 黑龙排行榜
     */
    public void queryBossRank() {
        this.privBossRank.clear();
        try {
            Connection con = DatabaseConnection.getConnection();
            final PreparedStatement ps = con.prepareStatement("select  c.name,a.count,c.level,c.str,c.dex,c.int,c.luk  from  bosslog2  as a " +
                    "left join characters as c on  a.characterid = c.id " +
                    "where a.bossid = '击杀黑龙' and a.type = 1 " +
                    "order by a.count desc limit 10 ");
            final ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                final SponsorRank rank1 = new SponsorRank(rs.getString("name"), rs.getInt("count"), rs.getInt("str"), rs.getInt("dex"), rs.getInt("int"), rs.getInt("luk"));
                this.privBossRank.add(rank1);
            }
            ps.close();
            rs.close();
        }
        catch (SQLException e) {
            System.err.println("queryBossRank错误");
        }
    }
    /**
     * 绯红排行榜 绯红四人帮
     */
    public void queryRedRank() {
        this.privRedRank.clear();
        try {
            Connection con = DatabaseConnection.getConnection();
            final PreparedStatement ps = con.prepareStatement("select  c.name,a.count,c.level,c.str,c.dex,c.int,c.luk  from  bosslog2  as a "+
                    "     left join characters as c on  a.characterid = c.id " +
                    "     where a.bossid = '绯红四人帮' and a.type = 1 "+
                    "     order by a.count desc limit 10 ");
            final ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                final SponsorRank rank1 = new SponsorRank(rs.getString("name"), rs.getInt("count"), rs.getInt("str"), rs.getInt("dex"), rs.getInt("int"), rs.getInt("luk"));
                this.privRedRank.add(rank1);
            }
            ps.close();
            rs.close();
        }
        catch (SQLException e) {
            System.err.println("queryRedRank错误");
        }
    }
    /**
     * 段伤排行榜
     */
    public void queryAccurateRank() {
        try {
            this.privAccurateRank.clear();
            for (Map.Entry<Integer, SponsorRank> integerSponsorRankEntry : Start.accurateRankMap.entrySet()) {
                privAccurateRank.add(integerSponsorRankEntry.getValue());
            }
            this.privAccurateRank = privAccurateRank.stream().sorted(Comparator.comparing(SponsorRank::getCounts).reversed()).collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("queryAccurateRank错误");
        }

    }
    /**
     * 赋能排行榜
     */
    public void queryEnhancedRank() {
        try {
            this.privEnhancedRank.clear();
            for (Map.Entry<Integer, SponsorRank> integerSponsorRankEntry : Start.enhancedRankMap.entrySet()) {
                privEnhancedRank.add(integerSponsorRankEntry.getValue());
            }
            //sponsorRank.stream().sorted()排序
            this.privEnhancedRank = privEnhancedRank.stream().sorted(Comparator.comparing(SponsorRank::getCounts).reversed()).collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("queryEnhancedRank错误");
        }
    }
    /**
     * 爆率排行榜
     */
    public void queryDropRank() {
        try {
            this.privDropRank.clear();
            for (Map.Entry<Integer, SponsorRank> integerSponsorRankEntry : Start.dropRankMap.entrySet()) {
                privDropRank.add(integerSponsorRankEntry.getValue());
            }
            //sponsorRank.stream().sorted()排序
            this.privDropRank = privDropRank.stream().sorted(Comparator.comparing(SponsorRank::getCounts).reversed()).collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("queryDropRank错误");
        }
    }
    public void RankingUpdate() {
        WorldTimer.getInstance().register((Runnable)new Runnable() {
            @Override
            public void run() {
                try {
                    MapleGuildRanking.this.reload();
                    MapleGuildRanking.this.showLevelRank();
                    MapleGuildRanking.this.showMesoRank();
                }
                catch (Exception ex) {
                    //Ex.printStackTrace();
                    System.err.println("Could not update rankings");
                }
            }
        }, 3600000L, 3600000L);
    }
    
    public static MapleGuildRanking getInstance() {
        return MapleGuildRanking.instance;
    }
    
    public List<GuildRankingInfo> getGuildRank() {
        //if (this.ranks.isEmpty()) {
            this.reload();
       // }
        return this.ranks;
    }
    
    public List<levelRankingInfo> getLevelRank() {
       // if (this.ranks1.isEmpty()) {
            this.showLevelRank();
      //  }
        return this.ranks1;
    }
    public List<SponsorRank> getSponsorRank() {
        //if (this.sponsorRank.isEmpty()) {
            this.querySponsorRank();
        //}
        return this.sponsorRank;
    }
    public List<SponsorRank> getDefeatRank() {
       // if (this.privDefeatRank.isEmpty()) {
            this.queryDefeatRank();
        //}
        return this.privDefeatRank;
    }
    public List<SponsorRank> getDefeatRankNew() {
        //if (this.privDefeatRankNew.isEmpty()) {
            this.queryDefeatRankNew();
       // }
        return this.privDefeatRankNew;
    }
    public List<SponsorRank> getBossRank() {
       // if (this.privBossRank.isEmpty()) {
            this.queryBossRank();
        //}
        return this.privBossRank;
    }
    public List<SponsorRank> getRedRank() {
        //if (this.privRedRank.isEmpty()) {
            this.queryRedRank();
        //}
        return this.privRedRank;
    }
    public List<SponsorRank> getAccurateRank() {
       // if (this.privAccurateRank.isEmpty()) {
            this.queryAccurateRank();
      //  }
        return this.privAccurateRank;
    }
    public List<SponsorRank> getEnhancedRank() {
        //if (this.privEnhancedRank.isEmpty()) {
            this.queryEnhancedRank();
       // }
        return this.privEnhancedRank;
    }
    public List<SponsorRank> getDropRank() {
//        if (this.privDropRank.isEmpty()) {
            this.queryDropRank();
//        }
            return this.privDropRank;
    }
    
    public List<mesoRankingInfo> getMesoRank() {
//        if (this.ranks2.isEmpty()) {
            this.showMesoRank();
//        }
        return this.ranks2;
    }
    
    public void reload() {
        this.ranks.clear();
        Connection con = DatabaseConnection.getConnection();
        try (final PreparedStatement ps = con.prepareStatement("SELECT * FROM guilds ORDER BY `GP` DESC LIMIT 10")) {
            final ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                final GuildRankingInfo rank = new GuildRankingInfo(rs.getString("name"), rs.getInt("GP"), rs.getInt("logo"), rs.getInt("logoColor"), rs.getInt("logoBG"), rs.getInt("logoBGColor"));
                this.ranks.add(rank);
            }
            rs.close();
        }
        catch (SQLException e) {
            System.err.println("家族排行错误" + (Object)e);
        }
    }
    
    public void showLevelRank() {
        this.ranks1.clear();
        try {
            Connection con = DatabaseConnection.getConnection();
            final PreparedStatement ps = con.prepareStatement("SELECT * FROM characters WHERE gm < 1 ORDER BY `level` DESC LIMIT 10");
            final ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                final levelRankingInfo rank1 = new levelRankingInfo(rs.getString("name"), rs.getInt("level"), rs.getInt("str"), rs.getInt("dex"), rs.getInt("int"), rs.getInt("luk"));
                this.ranks1.add(rank1);
            }
            ps.close();
            rs.close();
        }
        catch (SQLException e) {
            System.err.println("人物排行错误");
        }
    }
    
    public void showMesoRank() {
        this.ranks2.clear();
        Connection con = DatabaseConnection.getConnection();
        try (final PreparedStatement ps = con.prepareStatement("SELECT *, ( chr.meso + s.meso ) as money FROM `characters` as chr , `storages` as s WHERE chr.gm < 1  AND s.accountid = chr.accountid ORDER BY money DESC LIMIT 10")) {
            final ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                final mesoRankingInfo rank2 = new mesoRankingInfo(rs.getString("name"), rs.getLong("money"), rs.getInt("str"), rs.getInt("dex"), rs.getInt("int"), rs.getInt("luk"));
                this.ranks2.add(rank2);
            }
            rs.close();
        }
        catch (SQLException e) {
            System.err.println("金币排行错误");
        }
    }
    
    public List<JobRankingInfo> getJobRank(final int type) {
        if (this.JobRanks.get((Object)Integer.valueOf(type)) == null || ((List<JobRankingInfo>)this.JobRanks.get((Object)Integer.valueOf(type))).isEmpty()) {
            this.loadJobRank(type);
        }
        return (List<JobRankingInfo>)this.JobRanks.get((Object)Integer.valueOf(type));
    }
    
    public void loadJobRank(final int type) {
        if (this.JobRanks.get((Object)Integer.valueOf(type)) != null) {
            ((List<JobRankingInfo>)this.JobRanks.get((Object)Integer.valueOf(type))).clear();
        }
        String jobRange = "";
        if (type == 1) {
            jobRange = "and job >= '100' and job <= '132'";
        }
        else if (type == 2) {
            jobRange = "and job >= '200' and job <= '232'";
        }
        else if (type == 3) {
            jobRange = "and job >= '300' and job <= '322'";
        }
        else if (type == 4) {
            jobRange = "and job >= '400' and job <= '422'";
        }
        else if (type == 5) {
            jobRange = "and job >= '500' and job <= '522'";
        }
        else if (type == 6) {
            jobRange = "and job >= '2000' and job <= '2112'";
        }
        try (Connection con = (Connection)DBConPool.getInstance().getDataSource().getConnection()) {
            final PreparedStatement ps = con.prepareStatement("SELECT * FROM characters WHERE gm = 0 " + jobRange + " and accountid in (select id from accounts where banned= '0') ORDER BY `level` DESC LIMIT 10");
            final ResultSet rs = ps.executeQuery();
            final LinkedList<JobRankingInfo> JobRankList = new LinkedList<JobRankingInfo>();
            while (rs.next()) {
                final JobRankingInfo JobRank = new JobRankingInfo(rs.getString("name"), rs.getInt("level"), rs.getInt("job"), rs.getInt("str"), rs.getInt("dex"), rs.getInt("int"), rs.getInt("luk"));
                JobRankList.add(JobRank);
            }
            this.JobRanks.put(Integer.valueOf(type), JobRankList);
            ps.close();
            rs.close();
        }
        catch (SQLException e) {
            FileoutputUtil.outError("logs/资料库异常.txt", (Throwable)e);
            System.err.println("未能顯示职业" + type + "排行");
        }
    }
    
    static {
        instance = new MapleGuildRanking();
    }
    
    public static class SponsorRank {
        private final String name;
        private final int counts;
        private final int str;
        private final int dex;
        private final int _int;
        private final int luk;

        public SponsorRank(final String name, final int counts, final int str, final int dex, final int intt, final int luk) {
            this.name = name;
            this.counts = counts;
            this.str = str;
            this.dex = dex;
            this._int = intt;
            this.luk = luk;
        }

        public String getName() {
            return this.name;
        }

        public int getCounts() {
            return this.counts;
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
    }
    public static class mesoRankingInfo
    {
        private final String name;
        private final long meso;
        private final int str;
        private final int dex;
        private final int _int;
        private final int luk;

        public mesoRankingInfo(final String name, final long meso, final int str, final int dex, final int intt, final int luk) {
            this.name = name;
            this.meso = meso;
            this.str = str;
            this.dex = dex;
            this._int = intt;
            this.luk = luk;
        }
        
        public String getName() {
            return this.name;
        }
        
        public long getMeso() {
            return this.meso;
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
    }
    
    public static class JobRankingInfo
    {
        private final String name;
        private final int level;
        private final int str;
        private final int dex;
        private final int _int;
        private final int luk;
        private final int job;
        
        public JobRankingInfo(final String name, final int level, final int job, final int str, final int dex, final int intt, final int luk) {
            this.name = name;
            this.level = level;
            this.job = job;
            this.str = str;
            this.dex = dex;
            this._int = intt;
            this.luk = luk;
        }
        
        public String getName() {
            return this.name;
        }
        
        public int getLevel() {
            return this.level;
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
        
        public int getJob() {
            return this.job;
        }
    }
    
    public static class levelRankingInfo
    {
        private final String name;
        private final int level;
        private final int str;
        private final int dex;
        private final int _int;
        private final int luk;
        
        public levelRankingInfo(final String name, final int level, final int str, final int dex, final int intt, final int luk) {
            this.name = name;
            this.level = level;
            this.str = str;
            this.dex = dex;
            this._int = intt;
            this.luk = luk;
        }
        
        public String getName() {
            return this.name;
        }
        
        public int getLevel() {
            return this.level;
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
    }
    
    public static class GuildRankingInfo
    {
        private final String name;
        private final int gp;
        private final int logo;
        private final int logocolor;
        private final int logobg;
        private final int logobgcolor;
        
        public GuildRankingInfo(final String name, final int gp, final int logo, final int logocolor, final int logobg, final int logobgcolor) {
            this.name = name;
            this.gp = gp;
            this.logo = logo;
            this.logocolor = logocolor;
            this.logobg = logobg;
            this.logobgcolor = logobgcolor;
        }
        
        public String getName() {
            return this.name;
        }
        
        public int getGP() {
            return this.gp;
        }
        
        public int getLogo() {
            return this.logo;
        }
        
        public int getLogoColor() {
            return this.logocolor;
        }
        
        public int getLogoBg() {
            return this.logobg;
        }
        
        public int getLogoBgColor() {
            return this.logobgcolor;
        }
    }


    public List<mesoRankingInfo> getMesoRank_s() {
        if (ranks_meso.isEmpty()) {
            showMesoRank_snail();
        }

        return ranks_meso;
    }
    public List<JzRankingInfo> getJzRank(int mapid) {
        this.showJzRank(mapid);
        return this.ranks3;
    }
    public static class JzRankingInfo {
        private final int id;
        private final long meso;

        public JzRankingInfo(int id, long meso) {
            this.id = id;
            this.meso = meso;
        }

        public int getId() {
            return this.id;
        }

        public long getMeso() {
            return this.meso;
        }
    }
    public void showJzRank(int mapid) {
        this.ranks3.clear();

        try {
            Connection con = DBConPool.getInstance().getDataSource().getConnection();
            Throwable var4 = null;

            try {
                PreparedStatement ps = con.prepareStatement("SELECT * FROM jzlog WHERE mapid = " + mapid + " ORDER BY `meso` DESC LIMIT 10");
                Throwable var6 = null;

                try {
                    ResultSet rs = ps.executeQuery();

                    while(rs.next()) {
                        JzRankingInfo rank3 = new JzRankingInfo(rs.getInt("characterid"), rs.getLong("meso"));
                        this.ranks3.add(rank3);
                    }

                    rs.close();
                } catch (Throwable var31) {
                    var6 = var31;
                    throw var31;
                } finally {
                    if (ps != null) {
                        if (var6 != null) {
                            try {
                                ps.close();
                            } catch (Throwable var30) {
                                var6.addSuppressed(var30);
                            }
                        } else {
                            ps.close();
                        }
                    }

                }
            } catch (Throwable var33) {
                var4 = var33;
                throw var33;
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
        } catch (SQLException var35) {
            //服务端输出信息.println_err("未能捐赠排行");
            FileoutputUtil.outError("logs/资料库异常.txt", var35);
        }

    }

    public static void showMesoRank_snail() {
        ranks_meso.clear();
        ArrayList<Integer> accIDList = new ArrayList();

        try {
            Connection con = DBConPool.getInstance().getDataSource().getConnection();
            Throwable var3 = null;

            try {
                PreparedStatement ps = con.prepareStatement("SELECT *, ( chr.meso + s.meso ) as money FROM `characters` as chr , `storages` as s WHERE chr.gm < 1  AND s.accountid = chr.accountid ORDER BY money DESC LIMIT 41");
                Throwable var5 = null;

                try {
                    ResultSet rs = ps.executeQuery();
                    ArrayList<mesoRankingInfo> chrRankList = new ArrayList();

                    while(rs.next()) {
                        long money = 0L;
                        if (accIDList.contains(rs.getInt("accountid"))) {
                            money = rs.getLong("meso");
                        } else {
                            money = rs.getLong("money");
                            accIDList.add(rs.getInt("accountid"));
                        }

                        mesoRankingInfo rank2 = new mesoRankingInfo(rs.getString("name"), money, rs.getInt("str"), rs.getInt("dex"), rs.getInt("int"), rs.getInt("luk"));
                        chrRankList.add(rank2);
                    }

                    int n = chrRankList.size();
                    if (n > 1) {
                        int i;
                        for(i = 0; i < n; ++i) {
                            boolean flag = false;

                            for(int j = 0; j < n - i - 1; ++j) {
                                long meso1 = ((mesoRankingInfo)chrRankList.get(j)).getMeso();
                                long meso2 = ((mesoRankingInfo)chrRankList.get(j + 1)).getMeso();
                                if (meso1 < meso2) {
                                    Collections.swap(chrRankList, j, j + 1);
                                    flag = true;
                                }
                            }

                            if (!flag) {
                                break;
                            }
                        }

                        for(i = 0; i < chrRankList.size() && i < 20; ++i) {
                            ranks_meso.add(chrRankList.get(i));
                        }

                        rs.close();
                        accIDList.clear();
                        chrRankList.clear();
                    }
                } catch (Throwable var41) {
                    var5 = var41;
                    throw var41;
                } finally {
                    if (ps != null) {
                        if (var5 != null) {
                            try {
                                ps.close();
                            } catch (Throwable var40) {
                                var5.addSuppressed(var40);
                            }
                        } else {
                            ps.close();
                        }
                    }

                }
            } catch (Throwable var43) {
                var3 = var43;
                throw var43;
            } finally {
                if (con != null) {
                    if (var3 != null) {
                        try {
                            con.close();
                        } catch (Throwable var39) {
                            var3.addSuppressed(var39);
                        }
                    } else {
                        con.close();
                    }
                }

            }
        } catch (Exception var45) {
            //服务端输出信息.println_err("未能显示财产排行," + var45);
            FileoutputUtil.outError("logs/资料库异常.txt", var45);
        }
    }
    public List<levelRankingInfo> getLevelRank_s() {
        if (ranks_level.isEmpty()) {
            showLevelRank_s();
        }

        return ranks_level;
    }
    public static void showLevelRank_s() {
        ranks_level.clear();

        try {
            Connection con = DBConPool.getInstance().getDataSource().getConnection();
            Throwable var1 = null;

            try {
                PreparedStatement ps = con.prepareStatement("SELECT * FROM characters WHERE gm < 1 ORDER BY `level` DESC LIMIT 100");
                ResultSet rs = ps.executeQuery();

                while(rs.next()) {
                    levelRankingInfo rank1 = new levelRankingInfo(rs.getString("name"), rs.getInt("level"), rs.getInt("str"), rs.getInt("dex"), rs.getInt("int"), rs.getInt("luk"));
                    ranks_level.add(rank1);
                }

                ps.close();
                rs.close();
            } catch (Throwable var13) {
                var1 = var13;
                throw var13;
            } finally {
                if (con != null) {
                    if (var1 != null) {
                        try {
                            con.close();
                        } catch (Throwable var12) {
                            var1.addSuppressed(var12);
                        }
                    } else {
                        con.close();
                    }
                }

            }
        } catch (SQLException var15) {
            //服务端输出信息.println_err("未能显示等级排行");
            FileoutputUtil.outError("logs/资料库异常.txt", var15);
        }

    }

    public static void MapleMSpvpdeaths(MapleClient c, int npcid) {
        try {
            Connection con = DBConPool.getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT `name`, `pvpdeaths`, `str`, `dex`, `int`, `luk` FROM characters ORDER BY `pvpdeaths` DESC LIMIT 10");
            ResultSet rs = ps.executeQuery();
            c.sendPacket(MaplePacketCreator.MapleMSpvpdeaths(npcid, rs));
            ps.close();
            rs.close();
        } catch (Exception var5) {
            //服务端输出信息.println_out("failed to display guild ranks." + var5);
        }

    }

    public static void MapleMSpvpkills(MapleClient c, int npcid) {
        try {
            Connection con = DBConPool.getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT `name`, `pvpkills`, `str`, `dex`, `int`, `luk` FROM characters ORDER BY `pvpkills` WHERE gm < 1  DESC LIMIT 10");
            ResultSet rs = ps.executeQuery();
            c.sendPacket(MaplePacketCreator.MapleMSpvpkills(npcid, rs));
            ps.close();
            rs.close();
        } catch (Exception var5) {
            //服务端输出信息.println_out("failed to display guild ranks." + var5);
        }

    }

}
