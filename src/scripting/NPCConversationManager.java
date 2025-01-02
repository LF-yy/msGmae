package scripting;
// 汉化方法库
import abc.Game;
import bean.*;
import client.*;
import client.inventory.*;
import com.alibaba.druid.util.StringUtils;
import constants.tzjc;
import fumo.FumoSkill;
import gui.LtMS;
import gui.服务端输出信息;
import handling.channel.handler.*;
import merchant.merchant_main;

import java.awt.*;
import java.io.*;
import java.sql.*;
import java.sql.Date;
import java.text.DecimalFormat;
import java.util.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import server.*;

import constants.ServerConfig;
import database.DatabaseConnection;
import server.Timer;
import server.maps.*;
import snail.*;
import server.custom.capture.capture_yongfa;
import server.gashapon.GashaponFactory;
import server.gashapon.Gashapon;
import server.life.*;
import server.movement.LifeMovementFragment;
import server.shops.HiredMerchant;
import handling.channel.MapleGuildRanking.JobRankingInfo;
import tools.*;
import server.Timer.EventTimer;
import handling.world.World.Family;
import server.Timer.CloneTimer;

import handling.world.guild.MapleGuild;
import handling.world.MapleParty;
import handling.world.World.Alliance;

import tools.packet.MobPacket;
import tools.packet.PacketHelper;
import tools.packet.PlayerShopPacket;

import database.DBConPool;
import handling.channel.MapleGuildRanking;
import handling.world.World.Guild;
import handling.channel.ChannelServer;
import handling.world.MaplePartyCharacter;

import java.util.Map.Entry;

import java.util.stream.Collectors;

import server.quest.MapleQuest;
import handling.world.World.Broadcast;
import constants.GameConstants;
import handling.world.World;

import javax.script.Invocable;

import tools.packet.UIPacket;
import util.ListUtil;

public class NPCConversationManager extends AbstractPlayerInteraction
{
    protected MapleClient c;
    private final int npc;
    private final int questid;
    private int mode;
    protected String script;
    private String getText;
    private final byte type;
    private byte lastMsg = -1;
    public boolean pendingDisposal = false;
    private final Invocable iv;
    private int wh = 0;
//    public NPCConversationManager(final MapleClient c, final int npc, final int questid, final int mode, final String npcscript, final byte type, final Invocable iv) {
//        super(c);
//        this.lastMsg = -1;
//        this.pendingDisposal = false;
//        this.p = 0;
//        this.c = c;
//        this.npc = npc;
//        this.questid = questid;
//        this.mode = mode;
//        this.type = type;
//        this.iv = iv;
//        this.script = npcscript;
//    }
    public NPCConversationManager(MapleClient c, int npc, int questid, int mode, String npcscript, byte type, Invocable iv) {
        super(c);
        this.c = c;
        this.npc = npc;
        this.questid = questid;
        this.mode = mode;
        this.type = type;
        this.iv = iv;
        this.script = npcscript;
    }

    public NPCConversationManager(MapleClient c, int npc, int questid, byte type, Invocable iv, int wh) {
        super(c);
        this.c = c;
        this.npc = npc;
        this.questid = questid;
        this.type = type;
        this.iv = iv;
        this.wh = wh;
    }


    public String getDiabloEquipmentsDisplay(){

        StringBuilder output = new StringBuilder();

        for (LtDiabloEquipments equipment : Start.ltDiabloEquipments) {
            StringBuilder attributes = new StringBuilder();
            if (equipment.getStr() > 0) {
                attributes.append("力量:").append(equipment.getStr()).append(", ");
            }
            if (equipment.getDex() > 0) {
                attributes.append("敏捷:").append(equipment.getDex()).append(", ");
            }
            if (equipment.get_int() > 0) {
                attributes.append("智力:").append(equipment.get_int()).append(", ");
            }
            if (equipment.getLuk() > 0) {
                attributes.append("运气:").append(equipment.getLuk()).append(", ");
            }
            if (equipment.getLuk() > 0) {
                attributes.append("物理攻击:").append(equipment.getLuk()).append(", ");
            }
            if (equipment.getLuk() > 0) {
                attributes.append("运气:").append(equipment.getLuk()).append(", ");
            }
            if (equipment.getLuk() > 0) {
                attributes.append("运气:").append(equipment.getLuk()).append(", ");
            }
            if (equipment.getLuk() > 0) {
                attributes.append("运气:").append(equipment.getLuk()).append(", ");
            }
            if (equipment.getLuk() > 0) {
                attributes.append("运气:").append(equipment.getLuk()).append(", ");
            }
            if (equipment.getLuk() > 0) {
                attributes.append("运气:").append(equipment.getLuk()).append(", ");
            }
            if (equipment.getLuk() > 0) {
                attributes.append("运气:").append(equipment.getLuk()).append(", ");
            }
            if (equipment.getLuk() > 0) {
                attributes.append("运气:").append(equipment.getLuk()).append(", ");
            }
            if (equipment.getLuk() > 0) {
                attributes.append("运气:").append(equipment.getLuk()).append(", ");
            }
            if (equipment.getLuk() > 0) {
                attributes.append("运气:").append(equipment.getLuk()).append(", ");
            }
            if (equipment.getLuk() > 0) {
                attributes.append("运气:").append(equipment.getLuk()).append(", ");
            }
            // 可以继续添加其他属性的检查

            // 移除最后的逗号和空格
            if (attributes.length() > 0) {
                attributes.setLength(attributes.length() - 2); // 删除最后的 ", "
                output.append("词条名：").append(equipment.getEntryName()).append(" => ").append(attributes.toString()).append("\n");
            }
        }
        return output.toString();
    }


    //召唤兽
    public void callUserMapleMonster(int mobId) {
        MapleMonster mainb = MapleLifeFactory.getMonster(mobId);
        if(mainb != null) {
            mainb.setPosition(new Point(c.getPlayer().getPosition().x - 200, c.getPlayer().getPosition().y));
            mainb.setFake(true);
            mainb.setOwner(c.getPlayer().getId());
            mainb.setDuration(600000L);
            c.getPlayer().getMap().spawnFakeMonster(mainb);
            c.getPlayer().getMap().setHaveStone(true);
        }
    }

    /**
     * 开启暗黑模式
     */
    public void openTeamDark() {
        c.getPlayer().setOpenEnableDarkMode(true);
    }
    /**
     * 团队开启暗黑模式
     */
    public void TDOpenTeamDark() {
        if (c.getPlayer().getParty() == null || c.getPlayer().getParty().getMembers().size() == 1) {
             c.getPlayer().setOpenEnableDarkMode(true);
             return;
        }
        for (final MaplePartyCharacter chr : c.getPlayer().getParty().getMembers()) {
            final MapleCharacter curChar = c.getPlayer().getMap().getCharacterById(chr.getId());
            if (curChar != null ) {
                curChar.setOpenEnableDarkMode(true);
            }
        }
    }
    /**
     * 获取套装属性
     */
    public List<MyPackageX> anyList(Integer id) {
        Collection<IItem> hasEquipped = c.getPlayer().getHasEquipped();
        if (hasEquipped==null || hasEquipped.size()==0){
            return null;
        }
        List<MyPackageX> list = new ArrayList<>();
        List<PackageOfEquipments.MyPackage> packageList = PackageOfEquipments.getInstance().getPackageList();
        for (PackageOfEquipments.MyPackage myPackage : packageList) {
            MyPackageX myPackageX = new MyPackageX(myPackage);
            for (Integer integer : myPackage.getItemIdList()) {
                if (hasEquipped.stream().noneMatch(item ->  item.getItemId() == integer)){
                    myPackageX.setComplete(1);
                }
            }
            list.add(myPackageX);
        }
        return list;
    }

    /**
     * 屏蔽特效
     */
    public void hideEffect() {
        c.getPlayer().屏蔽特效 = true;
    }


    /**
     * 检查商城
     */
    public void dropItemShop (int type ) {
        if (type == 1) {
            try {
                List<CashShopModifiedItems> shopItem = getShopItem();
                for (CashShopModifiedItems cashShopModifiedItems : shopItem) {
                    int itemId = cashShopModifiedItems.getItemid();
                    MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
                    if (GameConstants.isPet(itemId)) {
                    } else if (!ii.itemExists(itemId)) {
                        //物品不存在更新商城数据库表
                        if (cashShopModifiedItems.getItemid()!=0) {
                            updateShopItem(cashShopModifiedItems.getItemid(), cashShopModifiedItems.getSerial());
                            FileoutputUtil.print("logs/商城处理结果.txt", "物品id：" + cashShopModifiedItems.getItemid() + "");
                        }
                    } else {
                        IItem toDrop;
                        if (GameConstants.getInventoryType(itemId) == MapleInventoryType.EQUIP) {
                            toDrop = ii.randomizeStats((Equip) ii.getEquipById(itemId));
                        } else {
                            toDrop = new Item(itemId, (short) 0, (short) 1, (byte) 0);
                        }
                        toDrop.setGMLog(c.getPlayer().getName());
                        c.getPlayer().getMap().spawnItemDrop((MapleMapObject) c.getPlayer(), c.getPlayer(), toDrop, c.getPlayer().getPosition(), true, true);
                    }
                }
            } catch (Exception e) {
                //e.printStackTrace();
                System.out.println("商城物品检查异常");
            }
        }else{

            try {
                List<DropData> shopItem = getDropItem();
                for (DropData cashShopModifiedItems : shopItem) {
                    int itemId = cashShopModifiedItems.getItemid();
                    MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
                    if (GameConstants.isPet(itemId)) {
                    } else if (!ii.itemExists(itemId)) {
                        //物品不存在删除爆率
                        if (cashShopModifiedItems.getItemid()!=0) {
                            deleteDropItem(cashShopModifiedItems.getItemid());
                            FileoutputUtil.print("logs/爆率处理结果.txt", "物品id：" + cashShopModifiedItems.getItemid() + "");
                        }
                    } else {
                        IItem toDrop;
                        if (GameConstants.getInventoryType(itemId) == MapleInventoryType.EQUIP) {
                            toDrop = ii.randomizeStats((Equip) ii.getEquipById(itemId));
                        } else {
                            toDrop = new Item(itemId, (short) 0, (short) 1, (byte) 0);
                        }
                        toDrop.setGMLog(c.getPlayer().getName());
                        c.getPlayer().getMap().spawnItemDrop((MapleMapObject) c.getPlayer(), c.getPlayer(), toDrop, c.getPlayer().getPosition(), true, true);
                    }
                }
            } catch (Exception e) {
                //e.printStackTrace();
                System.out.println("商城物品检查异常");
            }
        }

    }

    public void 鱼转积分(List<Map<String, Integer>> itemIds){
        int 积分 = 0;
        for (Map<String, Integer> itemId : itemIds) {
            积分 += c.getPlayer().itemQuantity(itemId.get("itemId")) * itemId.get("itemNum") ;
        }
        c.getPlayer().setBossLog1("鱼积分" , 1,积分);
    }

    public void 装备转强化点(List<Map<String, Integer>> itemIds){
        int 强化点 = 0;
        for (Map<String, Integer> itemId : itemIds) {
            强化点 += c.getPlayer().itemQuantity(itemId.get("itemId")) * itemId.get("itemNum") ;
        }
        c.getPlayer().setBossLog1("强化点" , 1,强化点);
    }


    public List<CashShopModifiedItems> getShopItem(){
        List<CashShopModifiedItems> list = new ArrayList<>();
        try {
            Connection con = (Connection) DBConPool.getInstance().getDataSource().getConnection();
            PreparedStatement ps = null;
            ResultSet rs = null;
            ps = con.prepareStatement("SELECT * FROM cashshop_modified_items order by serial desc");
            rs = ps.executeQuery();
            while (rs.next()) {
                CashShopModifiedItems cashShopModifiedItems = new CashShopModifiedItems();
                cashShopModifiedItems.setItemid(rs.getInt("itemid"));
                cashShopModifiedItems.setSerial(rs.getInt("serial"));
                cashShopModifiedItems.setShowup(rs.getInt("showup"));
                list.add(cashShopModifiedItems);
            }
            rs.close();
            ps.close();
            con.close();
        } catch (SQLException ex) {
            System.err.println("[" + FileoutputUtil.CurrentReadable_Time() + "]有错误!\r\n" + ex);
        }
        return list;
    }
    public List<DropData> getDropItem(){
        List<DropData> list = new ArrayList<>();
        try {
            Connection con = (Connection) DBConPool.getInstance().getDataSource().getConnection();
            PreparedStatement ps = null;
            ResultSet rs = null;
            ps = con.prepareStatement("SELECT * FROM drop_data group by itemid");
            rs = ps.executeQuery();
            while (rs.next()) {
                DropData cashShopModifiedItems = new DropData();
                cashShopModifiedItems.setItemid(rs.getInt("itemid"));
                list.add(cashShopModifiedItems);
            }
            rs.close();
            ps.close();
            con.close();
        } catch (SQLException ex) {
            System.err.println("[" + FileoutputUtil.CurrentReadable_Time() + "]有错误!\r\n" + ex);
        }
        return list;
    }
    public void deleteDropItem(int itemId ){
        try {
            Connection con = (Connection) DBConPool.getInstance().getDataSource().getConnection();
            PreparedStatement ps = null;
            ps = con.prepareStatement("delete from  drop_data where itemid = ?");
            ps.setInt(1, itemId);
            ps.execute();
            ps.close();
            con.close();
        } catch (SQLException ex) {
            System.err.println("[" + FileoutputUtil.CurrentReadable_Time() + "]有错误!\r\n" + ex);
        }
    }
    public void updateShopItem(int itemId,int ser ){
        try {
            Connection con = (Connection) DBConPool.getInstance().getDataSource().getConnection();
            PreparedStatement ps = null;
            ps = con.prepareStatement("UPDATE cashshop_modified_items set showup = ? where serial = ? and itemid = ?");
            ps.setInt(1, 0);
            ps.setInt(2, ser);
            ps.setInt(3, itemId);//1上架
            ps.executeUpdate();
            ps.close();
            con.close();
        } catch (SQLException ex) {
            System.err.println("[" + FileoutputUtil.CurrentReadable_Time() + "]有错误!\r\n" + ex);
        }
    }




    public void setC(MapleClient c) {
        this.c = c;
    }


    public void setMode(int mode) {
        this.mode = mode;
    }

    public void setScript(String script) {
        this.script = script;
    }

    public void setPendingDisposal(boolean pendingDisposal) {
        this.pendingDisposal = pendingDisposal;
    }


    public void setTemp(int temp) {
        this.temp = temp;
    }

    //重载单个事件
    public void setEventSM(String script) {
        for (ChannelServer instance : ChannelServer.getAllInstances()) {
            if (instance != null) {
                instance.reloadEvent(script);
            }
        }
        sendOk("事件已刷新");
        this.dispose();
    }

    public List<LtMxdPrize> getLtMxdPrize(int type) {
        return Start.ltMxdPrize.stream().filter(lt -> lt.getType() == type).collect(Collectors.toList());
    }
    public List<LtZlTask> getLtZlTask() {
        return Start.ltZlTask;
    }

    public Map<Integer,List<LtZlTask>> getLtZlTaskMap() {
        return Start.ltZlTask.stream().collect(Collectors.groupingBy(LtZlTask::getTime_code));
    }
    public List<LtZlTask> getLtZlTaskMList(int timeCode) {
        return Start.ltZlTask.stream().collect(Collectors.groupingBy(LtZlTask::getTime_code)).get(timeCode);
    }
    public long getDs伤害() {
       return this.c.getPlayer().get最高伤害();
    }
    public int etDrops() {
        return this.c.getPlayer().get地图缓存1();
    }
    public int getDropMap() {
        return this.c.getPlayer().get地图缓存2();
    }
    public void setDrops(int drop ) {
        this.c.getPlayer().set地图缓存1(drop);
    }
    public void setDropMap(int mapId ) {
        this.c.getPlayer().set地图缓存2(mapId);
    }
    //accurateRankMap  段伤
    //enhancedRankMap  赋能
    //dropRankMap   爆率




    /**
     * 赋能查询
     * @return
     */
    public int getUserFl() {
        if (c.getPlayer() == null) {
            return 0;
        }
        MapleGuildRanking.SponsorRank sponsorRank = Start.enhancedRankMap.get(c.getPlayer().getId());
        if (sponsorRank!=null){
           return sponsorRank.getCounts();
        }
        return 0;
    }

    /**
     * 段伤查询
     * @return
     */
    public int getUserDs() {
        if (c.getPlayer() == null) {
            return 0;
        }
        MapleGuildRanking.SponsorRank sponsorRank = Start.accurateRankMap.get(c.getPlayer().getId());
        if (sponsorRank!=null){
            return sponsorRank.getCounts();
        }
        return 0;
    }
    /**
     * 爆率查询
     * @return
     */
    public int getUserBl() {
        if (c.getPlayer() == null) {
            return 0;
        }
        MapleGuildRanking.SponsorRank sponsorRank = Start.dropRankMap.get(c.getPlayer().getId());
        if (sponsorRank!=null){
            return sponsorRank.getCounts();
        }
        return 0;
    }

    public void setDamageSkin( int param) {
        PacketHelper.showDamageSkin(c.getPlayer().getId(),param);
    }

//    //释放特殊技能
    public void autoAttack( int display,int skillId) {
       PlayerHandler.AutoAttack(display,skillId, c, c.getPlayer());
//        c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.closeRangeAttack(c.getPlayer().getId(), (int)attack2.tbyte, attack2.skill, skillLevel2, attack2.display, attack2.animation, attack2.speed, attack2.allDamage, energy, (int)c.getPlayer().getLevel(), c.getPlayer().getStat().passive_mastery(), attack2.unk, attack2.charge));
//        final ISkill skill = SkillFactory.getSkill(skillId);
//        final int skillLevel = c.getPlayer().getSkillLevel(skillId);
//        MapleStatEffect effect = skill.getEffect(skillLevel);
//        final int maxdamage2 = effect.getDamage();
//        final int attackCount2 = 1;
//        final boolean mirror = false;
//        final AttackInfo attack = DamageParse.Modify_AttackCrit(DamageParse.parseDmgM(slea), c, 1);
//        DamageParse.applyAttack(attack2, skill, c, attackCount2, maxdamage2, effect, mirror ? AttackType.NON_RANGED_WITH_MIRROR : AttackType.NON_RANGED);
    }

    public void yourIsGM() {
        if (!c.getPlayer().isGM()) {
            Broadcast.broadcastMessage(MaplePacketCreator.serverNotice(6, "[封锁密语] " + getPlayer().getName() + " 这么屌读我GM权限,走你。"));
            Broadcast.broadcastMessage(MaplePacketCreator.serverNotice(6, "[封锁密语] " + getPlayer().getName() + " 这么屌读我GM权限,走你。"));
            Broadcast.broadcastMessage(MaplePacketCreator.serverNotice(6, "[封锁密语] " + getPlayer().getName() + " 这么屌读我GM权限,走你。"));
            this.getPlayer().ban("读GM权限", true, true, false);
        }
    }


    public void 移除领域技能(int userId,int skillId) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        BreakthroughMechanism su = new BreakthroughMechanism();
        try  {
            Connection con = DatabaseConnection.getConnection();
            ps = con.prepareStatement("DELETE FROM lt_field_skills WHERE characterid = ? AND skillid = ?");
            ps.setInt(1, userId);
            ps.setInt(1, skillId);
            ps.executeUpdate();
            ps.close();
            Start.GetFieldSkills();
        } catch (SQLException ex) {
            System.out.println("移除领域技能异常：" + ex.getMessage());
        }
    }
    public void 移除超级技能(int userId) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        BreakthroughMechanism su = new BreakthroughMechanism();
        try  {
            Connection con = DatabaseConnection.getConnection();
            ps = con.prepareStatement("DELETE FROM lt_super_skills WHERE characterid = ? ");
            ps.setInt(1, userId);
            ps.executeUpdate();
            ps.close();
            Start.GetSuperSkills();
        } catch (SQLException ex) {
            System.out.println("移除超级技能异常：" + ex.getMessage());
        }
    }
    public List<FieldSkills> 查询领域技能(int userId,int skillId) {
        if (skillId>0 && ListUtil.isNotEmpty(Start.fieldSkillsMap.get(userId))){
            List<FieldSkills> fieldSkills = Start.fieldSkillsMap.get(userId);
            Map<Integer, List<FieldSkills>> collect = fieldSkills.stream().collect(Collectors.groupingBy(FieldSkills::getSkillid));
            List<FieldSkills> fieldSkills1 = collect.get(skillId);
            if (ListUtil.isNotEmpty(fieldSkills1)){
                return fieldSkills1;
            }
        }else {
            return ListUtil.isNotEmpty(Start.fieldSkillsMap.get(userId)) ? Start.fieldSkillsMap.get(userId) : null;
        }
        return null;
    }
    public List<SuperSkills> 查询超级技能(int userId,int skillId) {
        if (skillId>0 && ListUtil.isNotEmpty(Start.superSkillsMap.get(userId))){
            List<SuperSkills> superSkills = Start.superSkillsMap.get(userId);
            Map<Integer, List<SuperSkills>> collect = superSkills.stream().collect(Collectors.groupingBy(SuperSkills::getSkillid));
            List<SuperSkills> superSkills1 = collect.get(skillId);
            if (ListUtil.isNotEmpty(superSkills1)){
                return superSkills1;
            }
        }else {
            return ListUtil.isNotEmpty(Start.superSkillsMap.get(userId)) ? Start.superSkillsMap.get(userId) : null;
        }
        return null;
    }
public void 学习领域技能(int characterid,int skillid,String skillName,int skillLeve,int injuryinterval,int injurydelaytime,int damagedestructiontime,int skillLX,int skillLY,int skillRX,int skillRY,int range,double harm) {

      try{
                PreparedStatement ps1 = null;
                ResultSet rs1 = null;
                Connection con1 = DatabaseConnection.getConnection();
                ps1 = con1.prepareStatement("INSERT INTO lt_field_skills (characterid,skillid,skill_name,skill_leve,injuryinterval,injurydelaytime,damagedestructiontime,skillLX,skillLY,skillRX,skillRY,ranges,harm) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?)");
                ps1.setInt(1, characterid);
                ps1.setInt(2, skillid);
              ps1.setString(3,skillName);
                 ps1.setInt(4, skillLeve);
                 ps1.setInt(5, injuryinterval);
                 ps1.setInt(6, injurydelaytime);
                 ps1.setInt(7, damagedestructiontime);
                 ps1.setInt(8, skillLX);
                 ps1.setInt(9, skillLY);
                ps1.setInt(10, skillRX);
                ps1.setInt(11, skillRY);
                ps1.setInt(12, range);
             ps1.setDouble(13, harm);
                ps1.execute();
                ps1.close();
            Start.GetFieldSkills();
        } catch (SQLException ex) {
            System.out.println("境界提升异常：" + ex.getMessage());
        }
    }
    public void 提升超级技能(int Characterid,int Skillid,int Itemid,int Injuryinterval,int Injurydelaytime,int SkillLX,int SkillLY,int SkillRX,int SkillRY,int Damagedestructiontime,String CombinatorialCodingId,String Skill_name,int Skill_leve,int Range,int SkillCount,int StackingDistance,double Harm) {

        PreparedStatement ps = null;
        ResultSet rs = null;
        SuperSkills su = new SuperSkills();
        try  {
            Connection con = DatabaseConnection.getConnection();
            ps = con.prepareStatement("SELECT id,characterid ,skillid,itemid,injuryinterval,injurydelaytime,skillLX,skillLY,skillRX,skillRY,damagedestructiontime,combinatorialCodingId,skill_name,skill_leve,ranges,skillCount,stackingDistance,harm FROM lt_super_skills where characterid = ? and skillid = ?");
            ps.setInt(1, Characterid);
            ps.setInt(2, Skillid);
            rs = ps.executeQuery();
            if (rs.next()) {
                su.setId(rs.getLong("id"));
                su.setCharacterid(rs.getInt("characterid"));
                su.setSkillid(rs.getInt("skillid"));
                su.setItemid(rs.getInt("itemid"));
                su.setInjuryinterval(rs.getInt("injuryinterval"));
                su.setInjurydelaytime(rs.getInt("injurydelaytime"));
                su.setSkillLX(rs.getInt("skillLX"));
                su.setSkillLY(rs.getInt("skillLY"));
                su.setSkillRX(rs.getInt("skillRX"));
                su.setSkillRY(rs.getInt("skillRY"));
                su.setDamagedestructiontime(rs.getInt("damagedestructiontime"));
                su.setCombinatorialCodingId(rs.getString("combinatorialCodingId"));
                su.setSkill_name(rs.getString("skill_name"));
                su.setSkill_leve(rs.getInt("skill_leve"));
                su.setRange(rs.getInt("range"));
                su.setSkillCount(rs.getInt("skillCount"));
                su.setStackingDistance(rs.getInt("stackingDistance"));
                su.setHarm(rs.getInt("harm"));
            }
            ps.execute();
            ps.close();
            if(su.getCharacterid() >0 ){
                PreparedStatement ps1 = null;
                ResultSet rs1 = null;
                Connection con1 = DatabaseConnection.getConnection();
                ps1 = con1.prepareStatement("update lt_super_skills set  characterid = ? ,skillid = ? ,itemid = ? ,injuryinterval = ? ,injurydelaytime = ? ,skillLX = ? ,skillLY = ? ,skillRX = ? ,skillRY = ? ,damagedestructiontime = ?,combinatorialCodingId = ?,skill_name = ?,skill_leve = ?,ranges = ?,skillCount = ?,stackingDistance = ?,harm = ? FROM lt_super_skills where id = ?");
                ps1.setInt(1, Characterid);
                ps1.setInt(2, Skillid);
                ps1.setInt(3, Itemid);
                ps1.setInt(4, Injuryinterval);
                ps1.setInt(5, Injurydelaytime);
                ps1.setInt(6, SkillLX);
                ps1.setInt(7, SkillLY);
                ps1.setInt(8, SkillRX);
                ps1.setInt(9, SkillRY);
                ps1.setInt(10, Damagedestructiontime);
                ps1.setString(11, CombinatorialCodingId);
                ps1.setString(12, Skill_name);
                ps1.setInt(13, Skill_leve);
                ps1.setInt(14, Range);
                ps1.setInt(15, SkillCount);
                ps1.setInt(16, StackingDistance);
                ps1.setDouble(17, Harm);
                ps1.setLong(18, su.getId());
                ps1.execute();
                ps1.close();
            }else{
                PreparedStatement ps1 = null;
                ResultSet rs1 = null;
                Connection con1 = DatabaseConnection.getConnection();
                ps1 = con1.prepareStatement(
                        "INSERT INTO lt_super_skills (characterid ,skillid,itemid,injuryinterval,injurydelaytime,skillLX,skillLY,skillRX,skillRY,damagedestructiontime,combinatorialCodingId,skill_name,skill_leve,ranges,skillCount,stackingDistance,harm) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
                ps1.setInt(1, Characterid);
                ps1.setInt(2, Skillid);
                ps1.setInt(3, Itemid);
                ps1.setInt(4, Injuryinterval);
                ps1.setInt(5, Injurydelaytime);
                ps1.setInt(6, SkillLX);
                ps1.setInt(7, SkillLY);
                ps1.setInt(8, SkillRX);
                ps1.setInt(9, SkillRY);
                ps1.setInt(10, Damagedestructiontime);
                ps1.setString(11, CombinatorialCodingId);
                ps1.setString(12, Skill_name);
                ps1.setInt(13, Skill_leve);
                ps1.setInt(14, Range);
                ps1.setInt(15, SkillCount);
                ps1.setInt(16, StackingDistance);
                ps1.setDouble(17, Harm);
                ps1.execute();
                ps1.close();
            }
            Start.GetSuperSkills();
        } catch (SQLException ex) {
            System.out.println("境界提升异常：" + ex.getMessage());
        }
    }

    public BreakthroughMechanism 查询境界(int userId) {
        return ListUtil.isNotEmpty(Start.breakthroughMechanism.get(userId)) ? Start.breakthroughMechanism.get(userId).get(0) : null;
    }
    public void 修改境界特性(int userId,String type,int count) {

        PreparedStatement ps = null;
        ResultSet rs = null;
        BreakthroughMechanism su = new BreakthroughMechanism();
        try  {
            Connection con = DatabaseConnection.getConnection();
            ps = con.prepareStatement("SELECT name,characterid,equal_order,localstr,localdex,localluk,localint,all_quality,harm,crit,crit_harm,boss_harm,hp,mp,pad,matk,customize_attribute,customize_smash_roll FROM lt_breakthrough_mechanism where characterid = ?");
            ps.setInt(1, userId);
            rs = ps.executeQuery();
            while (rs.next()) {
                su.setName(rs.getString("name"));
                su.setCharacterid(rs.getInt("characterid"));
                su.setEqualOrder(rs.getString("equal_order"));
                su.setLocalstr(rs.getInt("localstr"));
                su.setLocaldex(rs.getInt("localdex"));
                su.setLocalluk(rs.getInt("localluk"));
                su.setLocalint(rs.getInt("localint"));
                su.setAllQuality(rs.getInt("all_quality"));
                su.setHarm(rs.getInt("harm"));
                su.setCrit(rs.getInt("crit"));
                su.setCritHarm(rs.getInt("crit_harm"));
                su.setBossHarm(rs.getInt("boss_harm"));
                su.setHp(rs.getInt("hp"));
                su.setMp(rs.getInt("mp"));
                su.setPad(rs.getInt("pad"));
                su.setMatk(rs.getInt("matk"));
                su.setCustomizeAttribute(rs.getInt("matk"));
                su.setCustomizeSmashRoll(rs.getInt("matk"));
                su.setCustomizeAttribute(rs.getInt("customize_attribute"));
                su.setCustomizeSmashRoll(rs.getInt("customize_smash_roll"));
            }
            ps.execute();
            ps.close();
            if(su.getCharacterid() >0 ){
                PreparedStatement ps1 = null;
                ResultSet rs1 = null;
                if("customizeAttribute".equals(type)){
                    su.setCustomizeAttribute(su.getCustomizeAttribute()+count);
                }
                if("customizeSmashRoll".equals(type)){
                    su.setCustomizeSmashRoll(su.getCustomizeSmashRoll()+count);
                }
                Connection con1 = DatabaseConnection.getConnection();
                ps1 = con1.prepareStatement("update lt_breakthrough_mechanism set customize_attribute = ? ,customize_smash_roll = ?  where characterid = ? ");
                ps1.setInt(1, su.getCustomizeAttribute());
                ps1.setInt(2, su.getCustomizeSmashRoll());
                ps1.setInt(3, userId);
                ps1.execute();
                ps1.close();
            }

            List<BreakthroughMechanism> list = new ArrayList<>();
            list.add(su);
            Start.breakthroughMechanism.remove(userId);
            Start.breakthroughMechanism.put(userId,list);
        } catch (SQLException ex) {
            System.out.println("消耗境界特性异常：" + ex.getMessage());
        }
    }

    public void 提升境界(int userId,String name,String equalOrder,int allQuality) {

        PreparedStatement ps = null;
        ResultSet rs = null;
        BreakthroughMechanism su = new BreakthroughMechanism();
        try  {
            Connection con = DatabaseConnection.getConnection();
            ps = con.prepareStatement("SELECT name,characterid,equal_order,localstr,localdex,localluk,localint,all_quality,harm,crit,crit_harm,boss_harm,hp,mp,pad,matk,customize_attribute,customize_smash_roll FROM lt_breakthrough_mechanism where characterid = ?");
            ps.setInt(1, userId);
            rs = ps.executeQuery();
            while (rs.next()) {
                su.setName(rs.getString("name"));
                su.setCharacterid(rs.getInt("characterid"));
                su.setEqualOrder(rs.getString("equal_order"));
                su.setLocalstr(rs.getInt("localstr"));
                su.setLocaldex(rs.getInt("localdex"));
                su.setLocalluk(rs.getInt("localluk"));
                su.setLocalint(rs.getInt("localint"));
                su.setAllQuality(rs.getInt("all_quality"));
                su.setHarm(rs.getInt("harm"));
                su.setCrit(rs.getInt("crit"));
                su.setCritHarm(rs.getInt("crit_harm"));
                su.setBossHarm(rs.getInt("boss_harm"));
                su.setHp(rs.getInt("hp"));
                su.setMp(rs.getInt("mp"));
                su.setPad(rs.getInt("pad"));
                su.setMatk(rs.getInt("matk"));
                su.setCustomizeAttribute(rs.getInt("matk"));
                su.setCustomizeSmashRoll(rs.getInt("matk"));
                su.setCustomizeAttribute(rs.getInt("customize_attribute"));
                su.setCustomizeSmashRoll(rs.getInt("customize_smash_roll"));
            }
            ps.execute();
            ps.close();
            if(su.getCharacterid() >0 ){
                PreparedStatement ps1 = null;
                ResultSet rs1 = null;
                su.setName(name);
                su.setEqualOrder(equalOrder);
                su.setAllQuality(allQuality);
                su.setCustomizeAttribute(su.getCustomizeAttribute()+1);
                su.setCustomizeSmashRoll(su.getCustomizeSmashRoll()+1);
                su.setHarm(su.getHarm()+1);
                Connection con1 = DatabaseConnection.getConnection();
                ps1 = con1.prepareStatement("update lt_breakthrough_mechanism set name = ? ,equal_order = ? ,all_quality = ? ,customize_attribute = ?,customize_smash_roll = ? ,harm = ? where characterid = ? ");
                ps1.setString(1, su.getName());
                ps1.setString(2, su.getEqualOrder());
                ps1.setInt(3, su.getAllQuality());
                ps1.setInt(4, su.getCustomizeAttribute());
                ps1.setInt(5, su.getCustomizeSmashRoll());
                ps1.setInt(6, su.getHarm());
                ps1.setInt(7, userId);
                ps1.execute();
                ps1.close();
            }else{
                PreparedStatement ps1 = null;
                ResultSet rs1 = null;
                su.setName(name);
                su.setCharacterid(userId);
                su.setEqualOrder(equalOrder);
                su.setAllQuality(allQuality);
                su.setCustomizeAttribute(1);
                su.setCustomizeSmashRoll(1);
                Connection con1 = DatabaseConnection.getConnection();
                ps1 = con1.prepareStatement("INSERT INTO lt_breakthrough_mechanism (name,characterid,equal_order,all_quality,customize_attribute,customize_smash_roll,harm) VALUES(?,?,?,?,?,?,?)");
                ps1.setString(1, su.getName());
                ps1.setInt(2, su.getCharacterid());
                ps1.setString(3, su.getEqualOrder());
                ps1.setInt(4, su.getAllQuality());
                ps1.setInt(5, su.getCustomizeAttribute()+1);
                ps1.setInt(6, su.getCustomizeSmashRoll()+1);
                ps1.setInt(7, su.getHarm()+1);
                ps1.execute();
                ps1.close();
            }

            List<BreakthroughMechanism> list = new ArrayList<>();
            list.add(su);
            Start.breakthroughMechanism.remove(userId);
            Start.breakthroughMechanism.put(userId,list);
        } catch (SQLException ex) {
            System.out.println("境界提升异常：" + ex.getMessage());
        }
    }

    public void 五转转职(final int id,final int job,final String jobName){

        PreparedStatement ps = null;
        ResultSet rs = null;
        int numbOld = 0;
        try  {
            Connection con = DatabaseConnection.getConnection();
            ps = con.prepareStatement("SELECT occupation_name,occupation_id FROM lt_five_turn where charactersid = ?  ");
            ps.setInt(1, id);
            rs = ps.executeQuery();
            while (rs.next()) {
                numbOld = rs.getInt("occupation_id");
            }
            ps.execute();
            ps.close();
            if(numbOld>0){
                PreparedStatement ps1 = null;
                ResultSet rs1 = null;
                Connection con1 = DatabaseConnection.getConnection();
                ps1 = con1.prepareStatement("update lt_five_turn set occupation_id = ?,occupation_name = ? where charactersid = ? ");
                ps1.setInt(1, job);
                ps1.setString(2, jobName);
                ps1.setInt(3, id);
                ps1.execute();
                ps1.close();
            }else{
                PreparedStatement ps1 = null;
                ResultSet rs1 = null;
                Connection con1 = DatabaseConnection.getConnection();
                ps1 = con1.prepareStatement("INSERT INTO lt_five_turn (charactersid,occupation_id,occupation_name) VALUES(?,?,?)");
                ps1.setInt(1, id);
                ps1.setInt(2, job);
                ps1.setString(3, jobName);
                ps1.execute();
                ps1.close();
            }
            FiveTurn fiveTurn = new FiveTurn();
            fiveTurn.setCharactersid(id);
            fiveTurn.setOccupationId(job);
            fiveTurn.setOccupationName(jobName);
            List<FiveTurn> list = new ArrayList<>();
            list.add(fiveTurn);
            Start.fiveTurn.remove(id);
            Start.fiveTurn.put(id,list);
        } catch (SQLException ex) {
            System.out.println("五转转职异常：" + ex.getMessage());
        }

    }
    public int 查5转职业id(final int id){

        PreparedStatement ps = null;
        ResultSet rs = null;
        int numbOld = 0;
        try  {
            Connection con = DatabaseConnection.getConnection();
            ps = con.prepareStatement("SELECT occupation_name,occupation_id FROM lt_five_turn where charactersid = ? ");
            ps.setInt(1, id);
            rs = ps.executeQuery();
            while (rs.next()) {
                numbOld = rs.getInt("occupation_id");
            }
            ps.execute();
            ps.close();

        } catch (SQLException ex) {
            System.out.println("查5转职业出错：" + ex.getMessage());
        }
        return numbOld;
    }

    public String 查5转职业名称(final int id){

        PreparedStatement ps = null;
        ResultSet rs = null;
        String numbOld = null;
        try  {
            Connection con = DatabaseConnection.getConnection();
            ps = con.prepareStatement("SELECT occupation_name,occupation_id FROM lt_five_turn where charactersid = ? ");
            ps.setInt(1, id);
            rs = ps.executeQuery();
            while (rs.next()) {
                numbOld = rs.getString("occupation_name");
            }
            ps.execute();
            ps.close();

        } catch (SQLException ex) {
            System.out.println("查5转职业出错：" + ex.getMessage());
        }
        return numbOld;
    }

    public String 移除五转(final int id){

        PreparedStatement ps = null;
        ResultSet rs = null;
        String numbOld = null;
        try  {
            Connection con = DatabaseConnection.getConnection();
            ps = con.prepareStatement("delete FROM lt_five_turn where charactersid = ? ");
            ps.setInt(1, id);
            ps.execute();
            ps.close();
            Start.GetfiveTurn();
        } catch (SQLException ex) {
            System.out.println("查5转职业出错：" + ex.getMessage());
        }
        return numbOld;
    }
    public String 自助爆率(int hours,int drop) {
        final int 原始爆率 = Integer.parseInt(ServerProperties.getProperty("LtMS.dropRate"));
        if (drop==0){
            drop = 2;
        }
        if ("1".equals(ServerProperties.getProperty("dropRate"))){
            return "已经有大佬开启倍率了";
        }
        final int 双倍爆率活动 = 原始爆率 * drop;
        final int seconds = 0;
        final int mins = 0;
        final int time = hours * 60 * 60;
        final String rate = "爆率";
        World.scheduleRateDelay("爆率", (long)time);
        ServerProperties.setProperty("dropRate","1");
        for (final ChannelServer cservs : ChannelServer.getAllInstances()) {
            cservs.setDropRate(双倍爆率活动);
        }
        Broadcast.broadcastSmega(MaplePacketCreator.serverNotice(9, 20, "[自助倍率] : 感谢 "+c.getPlayer().getName()+"大佬开启了"+drop+" 倍打怪爆率活动，将持续 " + hours + " 小时，请各位玩家狂欢吧！"));
        return "开启成功";
    }
    public String 自助经验(int hours,int drop) {
        final int 原始爆率 = Integer.parseInt(ServerProperties.getProperty("LtMS.expRate"));
        if (drop==0){
            drop = 2;
        }
        if ("1".equals(ServerProperties.getProperty("expRate"))){
            return "已经有大佬开启倍率了";
        }
        final int 双倍爆率活动 = 原始爆率 * drop;
        final int seconds = 0;
        final int mins = 0;
        final int time = hours * 60 * 60;
        final String rate = "经验";
        World.scheduleRateDelay("经验", (long)time);
        ServerProperties.setProperty("expRate","1");
        for (final ChannelServer cservs : ChannelServer.getAllInstances()) {
            cservs.setExpRate(双倍爆率活动);
        }
        Broadcast.broadcastSmega(MaplePacketCreator.serverNotice(9, 20, "[自助倍率] : 感谢  "+c.getPlayer().getName()+"大佬开启了"+drop+" 倍打怪经验活动，将持续 " + hours + " 小时，请各位玩家狂欢吧！"));
        return "开启成功";
    }

    public String 自助金币(int hours,int drop) {
        final int 原始爆率 = Integer.parseInt(ServerProperties.getProperty("LtMS.mesoRate"));
        if (drop==0){
            drop = 2;
        }
        if ("1".equals(ServerProperties.getProperty("mesoRate"))){
            return "已经有大佬开启倍率了";
        }
        final int 双倍爆率活动 = 原始爆率 * drop;
        final int seconds = 0;
        final int mins = 0;
        final int time = hours * 60 * 60;
        final String rate = "金币";
        World.scheduleRateDelay("金币", (long)time);
        ServerProperties.setProperty("mesoRate","1");
        for (final ChannelServer cservs : ChannelServer.getAllInstances()) {
            cservs.setMesoRate(双倍爆率活动);
        }
        Broadcast.broadcastSmega(MaplePacketCreator.serverNotice(9, 20, "[自助倍率] : 感谢  "+c.getPlayer().getName()+"大佬开启了"+drop+"倍打怪金币活动，将持续 " + hours + " 小时，请各位玩家狂欢吧！"));
        return "开启成功";
    }
    public int 获取配置(final String param) {
        return LtMS.ConfigValuesMap.get(param);
    }

    public int 查装备赋能(final IItem item,final String name){
        String names = "赋能"+item.getItemId()+name;

        PreparedStatement ps = null;
        ResultSet rs = null;
        int numbOld = 0;
        try  {
            Connection con = DatabaseConnection.getConnection();
            ps = con.prepareStatement("SELECT numb FROM suitdamtablenew where name = ? ");
            ps.setString(1, names);
            rs = ps.executeQuery();
            while (rs.next()) {
                numbOld = rs.getInt("numb");
            }
            ps.execute();
            ps.close();

        } catch (SQLException ex) {
            System.out.println("查询赋能装备加成表出错：" + ex.getMessage());
        }
        return numbOld;
    }
        public double 装备赋能(final IItem item,final Double numb,final String name){
        String names = "赋能"+item.getItemId()+name;

        PreparedStatement ps = null;
        ResultSet rs = null;
        double numbOld = 0.0;
        try  {
            Connection con = DatabaseConnection.getConnection();
            ps = con.prepareStatement("SELECT numb FROM suitdamtablenew where name = ? ");
            ps.setString(1, names);
            rs = ps.executeQuery();
            while (rs.next()) {
                numbOld = rs.getDouble("numb");
            }
            ps.execute();
            ps.close();
            if(numbOld>0){
                PreparedStatement ps1 = null;
                ResultSet rs1 = null;
                Connection con1 = DatabaseConnection.getConnection();
                ps1 = con1.prepareStatement("update suitdamtablenew set numb = ? where name = ? ");
                ps1.setDouble(1, numbOld+numb);
                ps1.setString(2, names);
                ps1.execute();
                ps1.close();
            }else{
                PreparedStatement ps1 = null;
                ResultSet rs1 = null;
                Connection con1 = DatabaseConnection.getConnection();
                ps1 = con1.prepareStatement("INSERT INTO suitdamtablenew (name,numb,proportion,proname) VALUES(?,?,?,?)");
                ps1.setString(1, names);
                ps1.setDouble(2, numbOld+numb);
                ps1.setInt(3, 9999);
                ps1.setString(4, name);
                ps1.execute();
                ps1.close();
            }
            tzjc.tzMap.put(names,numbOld+numb);
        } catch (SQLException ex) {
            System.out.println("赋能装备加成表出错：" + ex.getMessage());
        }
        return numbOld+numb;
    }
    /**
     * 获取吸怪
     */
    public static UserAttraction getAttractList(int id){
       return Start.吸怪集合.get(id);
    }


    public static UserAttraction getAttractList(int channel,int mapId){
        List<Entry<Integer, UserAttraction>> collect = Start.吸怪集合.entrySet().stream().filter(ua ->
                ua.getValue().getPinDao() == channel && ua.getValue().getMapId() == mapId).collect(Collectors.toList());
        return collect.size()>0 ? collect.get(0).getValue() : null;
    }
    public static UserLhAttraction getAttractLhList(int channel,int mapId){
        List<Entry<Integer, UserLhAttraction>> collect = Start.轮回集合.entrySet().stream().filter(ua ->
                ua.getValue().getPinDao() == channel && ua.getValue().getMapId() == mapId).collect(Collectors.toList());
        return collect.size()>0 ? collect.get(0).getValue() : null;
    }
    public  void gain开启吸怪(final MapleCharacter player){
        int mapId = player.getMapId();
        for ( MapleMonster monstermo : player.getMap().getAllMonster()) {
            if (monstermo.getPosition() != null && monstermo.getStats().isBoss()) {
                c.getPlayer().dropMessage(1, "该地图有BOSS不允许吸怪.");
                return;
            }
        }
        if (Start.特殊宠物吸物无法使用地图.stream().anyMatch(s-> {return mapId == Integer.parseInt(s);})){
            c.getPlayer().dropMessage(1, "该地图不允许吸怪.");
        }else {

            boolean b = Start.吸怪集合.entrySet().stream().anyMatch(ua -> {
                return ua.getValue().getPinDao() == c.getChannel() && ua.getValue().getMapId() == player.getMapId();
            });
            if (b) {
                c.getPlayer().dropMessage(1, "该地图已经有人在吸怪了.");
            } else {
                //先清除地图所有怪物
                //c.getPlayer().setLastRes(null);
                c.getPlayer().getMap().killAllMonsters(true);
                UserAttraction userAttraction = new UserAttraction(c.getChannel(), player.getMapId(), c.getPlayer().getPosition());
                Start.吸怪集合.put(player.getId(), userAttraction);
                c.getPlayer().startMobVac(userAttraction);
                //开启吸怪();
            }
        }

    }

    public  void gain开启轮回(final MapleCharacter player){
        int mapId = player.getMapId();
        for ( MapleMonster monstermo : player.getMap().getAllMonster()) {
            if (monstermo.getPosition() != null && monstermo.getStats().isBoss()) {
               // c.getPlayer().dropMessage(1, "该地图有BOSS不允许开启轮回.");
                return;
            }
        }
        if (Start.特殊宠物吸物无法使用地图.stream().anyMatch(s-> {return mapId == Integer.parseInt(s);})){
            //c.getPlayer().dropMessage(1, "该地图不允许轮回.");
        }else {

            boolean b = Start.轮回集合.entrySet().stream().anyMatch(ua -> {
                return ua.getValue().getPinDao() == c.getChannel() && ua.getValue().getMapId() == player.getMapId();
            });
            if (b) {
               // c.getPlayer().dropMessage(1, "该地图已经有人开启轮回了.");
            } else {
                //获取身边的怪物
                List<MapleMonster> list = new ArrayList<>();
                final MapleMap mapleMap = ChannelServer.getInstance(c.getChannel()).getMapFactory().getMap(c.getPlayer().getMapId());

                for (final MapleMonster monstermo : mapleMap.getAllMonster()) {
                    if (monstermo.isAlive() && !monstermo.getStats().isBoss()) {
                        list.add(monstermo);
                    }
                }

                Start.轮回怪物.put(player.getId(), list);
                UserLhAttraction userAttraction = new UserLhAttraction(c.getChannel(), player.getMapId(), c.getPlayer().getPosition());
                userAttraction.setMapMobCount(mapleMap.getAllMonster().size());
                Start.轮回集合.put(player.getId(), userAttraction);
                c.getPlayer().startMobLhVac(userAttraction);
                c.getPlayer().setLastResOld(c.getPlayer().getLastRes());
            }
        }
    }
    public static void gain关闭吸怪(int id){
        Start.吸怪集合.remove(id);
    }
    public void gain关闭吸怪(final MapleCharacter player){
        c.getPlayer().getMap().killAllMonsters(true);
        Start.吸怪集合.remove(player.getId());
        player.stopMobVac();
    }
    public static void gain关闭轮回(final MapleCharacter player,final MapleMap mapleMap ){
        try {
            mapleMap.killAllMonsters(true);
            Start.轮回集合.remove(player.getId());
            mapleMap.setHaveStone(false);
            mapleMap.setStoneLevel(-1);
            player.stopMobLhVac();
        } catch (Exception e) {
            Start.轮回集合.remove(player.getId());
        }
    }


    public  void gain开启BOSS击杀统计(final MapleCharacter player,int mobId,String mobName,String adress,String value, long hp){
        UserAttraction userAttraction = new UserAttraction(c.getChannel(), player.getMapId(), player.getPosition());
        c.getPlayer().startMobMapVac(userAttraction,mobId,mobName, adress, value,hp);
    }
    public static  void gain关闭BOSS击杀统计(final MapleCharacter player){
        if(player!=null) {
            player.stopMobMapVac();
        }
    }
    public static void setBossLog统计用(final int id,final String boss, final int type, final int count) {
        final int bossCount = getBossLog1统计用(id,boss, type);
        try {
            final PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("UPDATE bosslog2 SET count = ?, type = ?, time = CURRENT_TIMESTAMP() WHERE characterid = ? AND bossid = ?");
            ps.setInt(1, bossCount + count);
            ps.setInt(2, type);
            ps.setInt(3, id);
            ps.setString(4, boss);
            ps.executeUpdate();
            ps.close();
        }
        catch (Exception Ex) {
            FileoutputUtil.outError("logs/資料庫異常.txt", (Throwable)Ex);
        }
    }

    public static int getBossLog1统计用(final int id, final String boss, final int type) {
        try {
            int count = 0;
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT * FROM bosslog2 WHERE characterid = ? AND bossid = ?");
            ps.setInt(1, id);
            ps.setString(2, boss);
            final ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                count = rs.getInt("count");
                if (count < 0) {
                    return count;
                }
                final Timestamp bossTime = rs.getTimestamp("time");
                rs.close();
                ps.close();
                if (type == 0) {
                    final Calendar sqlcal = Calendar.getInstance();
                    if (bossTime != null) {
                        sqlcal.setTimeInMillis(bossTime.getTime());
                    }
                    if (sqlcal.get(5) + 1 <= Calendar.getInstance().get(5) || sqlcal.get(2) + 1 <= Calendar.getInstance().get(2) || sqlcal.get(1) + 1 <= Calendar.getInstance().get(1)) {
                        count = 0;
                        ps = con.prepareStatement("UPDATE bosslog2 SET count = 0, time = CURRENT_TIMESTAMP() WHERE characterid = ? AND bossid = ?");
                        ps.setInt(1, id);
                        ps.setString(2, boss);
                        ps.executeUpdate();
                    }
                }
            }
            else {
                final PreparedStatement psu = con.prepareStatement("INSERT INTO bosslog2 (characterid, bossid, count, type) VALUES (?, ?, ?, ?)");
                psu.setInt(1, id);
                psu.setString(2, boss);
                psu.setInt(3, 0);
                psu.setInt(4, type);
                psu.executeUpdate();
                psu.close();
            }
            rs.close();
            ps.close();
            return count;
        }
        catch (Exception Ex) {
            FileoutputUtil.outError("logs/資料庫異常.txt", (Throwable)Ex);
            return -1;
        }
    }


    public Invocable getIv() {
        return this.iv;
    }
    
    public int getMode() {
        return this.mode;
    }
    
    public int getNpc() {
        return this.npc;
    }
    
    public final boolean 判断轮回(final int mobid) 
    {
        for (MapleMapObject obj : c.getPlayer().getMap().getAllMonstersThreadsafe()) {
            final MapleMonster mob = (MapleMonster) obj;
            if (mob.getId() == mobid) {
                return true;
            }
        }
        return false;
    }
    public int getQuest() {
        return this.questid;
    }
    
    public String getScript() {
        return this.script;
    }
    
    public byte getType() {
        return this.type;
    }
    
    public void safeDispose() {
        this.pendingDisposal = true;
    }
    
    public void dispose() {
        NPCScriptManager.getInstance().dispose(this.c);
    }
    
    public void askMapSelection(final String sel) {
        if (this.lastMsg > -1) {
            return;
        }
        this.c.sendPacket(MaplePacketCreator.getMapSelection(this.npc, sel));
        this.lastMsg = 13;
    }
    
    public void sendNext(final String text) {
        if (this.lastMsg > -1) {
            return;
        }
        if (text.contains((CharSequence)"#L")) {
            this.sendSimple(text);
            return;
        }
        this.c.sendPacket(MaplePacketCreator.getNPCTalk(this.npc, (byte)0, text, "00 01", (byte)0));
        this.lastMsg = 0;
    }
    
    public void sendNextS(final String text, final byte type) {
        if (this.lastMsg > -1) {
            return;
        }
        if (text.contains((CharSequence)"#L")) {
            this.sendSimpleS(text, type);
            return;
        }
        this.c.sendPacket(MaplePacketCreator.getNPCTalk(this.npc, (byte)0, text, "00 01", type));
        this.lastMsg = 0;
    }
    
    public void sendPrev(final String text) {
        if (this.lastMsg > -1) {
            return;
        }
        if (text.contains((CharSequence)"#L")) {
            this.sendSimple(text);
            return;
        }
        this.c.sendPacket(MaplePacketCreator.getNPCTalk(this.npc, (byte)0, text, "01 00", (byte)0));
        this.lastMsg = 0;
    }
    
    public void sendPrevS(final String text, final byte type) {
        if (this.lastMsg > -1) {
            return;
        }
        if (text.contains((CharSequence)"#L")) {
            this.sendSimpleS(text, type);
            return;
        }
        this.c.sendPacket(MaplePacketCreator.getNPCTalk(this.npc, (byte)0, text, "01 00", type));
        this.lastMsg = 0;
    }
    
    public void sendNextPrev(final String text) {
        if (this.lastMsg > -1) {
            return;
        }
        if (text.contains((CharSequence)"#L")) {
            this.sendSimple(text);
            return;
        }
        this.c.sendPacket(MaplePacketCreator.getNPCTalk(this.npc, (byte)0, text, "01 01", (byte)0));
        this.lastMsg = 0;
    }
    
    public void PlayerToNpc(final String text) {
        this.sendNextPrevS(text, (byte)3);
    }
    
    public void sendNextPrevS(final String text) {
        this.sendNextPrevS(text, (byte)3);
    }
    
    public void sendNextPrevS(final String text, final byte type) {
        if (this.lastMsg > -1) {
            return;
        }
        if (text.contains((CharSequence)"#L")) {
            this.sendSimpleS(text, type);
            return;
        }
        this.c.sendPacket(MaplePacketCreator.getNPCTalk(this.npc, (byte)0, text, "01 01", type));
        this.lastMsg = 0;
    }
    
    public void sendOk(final String text) {
        if (this.lastMsg > -1) {
            return;
        }
        if (text.contains((CharSequence)"#L")) {
            this.sendSimple(text);
            return;
        }
        this.c.sendPacket(MaplePacketCreator.getNPCTalk(this.npc, (byte)0, text, "00 00", (byte)0));
        this.lastMsg = 0;
    }
    
    public void sendOkS(final String text, final byte type) {
        if (this.lastMsg > -1) {
            return;
        }
        if (text.contains((CharSequence)"#L")) {
            this.sendSimpleS(text, type);
            return;
        }
        this.c.sendPacket(MaplePacketCreator.getNPCTalk(this.npc, (byte)0, text, "00 00", type));
        this.lastMsg = 0;
    }
    
    public void sendYesNo(final String text) {
        if (this.lastMsg > -1) {
            return;
        }
        if (text.contains((CharSequence)"#L")) {
            this.sendSimple(text);
            return;
        }
        this.c.sendPacket(MaplePacketCreator.getNPCTalk(this.npc, (byte)1, text, "", (byte)0));
        this.lastMsg = 1;
    }
    
    public void sendYesNoS(final String text, final byte type) {
        if (this.lastMsg > -1) {
            return;
        }
        if (text.contains((CharSequence)"#L")) {
            this.sendSimpleS(text, type);
            return;
        }
        this.c.sendPacket(MaplePacketCreator.getNPCTalk(this.npc, (byte)1, text, "", type));
        this.lastMsg = 1;
    }
    
    public void sendAcceptDecline(final String text) {
        this.askAcceptDecline(text);
    }
    
    public void sendAcceptDeclineNoESC(final String text) {
        this.askAcceptDeclineNoESC(text);
    }
    
    public void askAcceptDecline(final String text) {
        if (this.lastMsg > -1) {
            return;
        }
        if (text.contains((CharSequence)"#L")) {
            this.sendSimple(text);
            return;
        }
        this.c.sendPacket(MaplePacketCreator.getNPCTalk(this.npc, (byte)11, text, "", (byte)0));
        this.lastMsg = 11;
    }
    
    public void askAcceptDeclineNoESC(final String text) {
        if (this.lastMsg > -1) {
            return;
        }
        if (text.contains((CharSequence)"#L")) {
            this.sendSimple(text);
            return;
        }
        this.c.sendPacket(MaplePacketCreator.getNPCTalk(this.npc, (byte)12, text, "", (byte)0));
        this.lastMsg = 12;
    }
    
    public void askAvatar(final String text, final int... args) {
        if (this.lastMsg > -1) {
            return;
        }
        this.c.sendPacket(MaplePacketCreator.getNPCTalkStyle(this.npc, text, args));
        this.lastMsg = 7;
    }
    
    public void sendSimple(final String text) {
        if (this.lastMsg > -1) {
            return;
        }
        if (!text.contains((CharSequence)"#L")) {
            this.sendNext(text);
            return;
        }
        this.c.sendPacket(MaplePacketCreator.getNPCTalk(this.npc, (byte)4, text, "", (byte)0));
        this.lastMsg = 4;
    }
    
    public void sendSimpleS(final String text, final byte type) {
        if (this.lastMsg > -1) {
            return;
        }
        if (!text.contains((CharSequence)"#L")) {
            this.sendNextS(text, type);
            return;
        }
        this.c.sendPacket(MaplePacketCreator.getNPCTalk(this.npc, (byte)4, text, "", type));
        this.lastMsg = 4;
    }
    public void sendSimple(final String text, final byte type) {
    if (this.lastMsg > -1) {
        return;
    }
    if (!text.contains((CharSequence)"#L")) {
        this.sendNextS(text, type);
        return;
    }
    this.c.sendPacket(MaplePacketCreator.getNPCTalk(this.npc, (byte)4, text, "", type));
    this.lastMsg = 4;
}
    
    public void sendStyle(final String text, final int[] styles) {
        if (this.lastMsg > -1) {
            return;
        }
        this.c.sendPacket(MaplePacketCreator.getNPCTalkStyle(this.npc, text, styles));
        this.lastMsg = 7;
    }
    public void sendStyle(String text, int a, int[] styles) {
        if (this.lastMsg <= -1) {
            this.c.sendPacket(MaplePacketCreator.getNPCTalkStyle(this.npc, text, a, styles));
            if (ServerConfig.version == 79) {
                this.lastMsg = 7;
            } else if (ServerConfig.version == 85) {
                this.lastMsg = 8;
            }

        }
    }
    public void sendGetNumber(final String text, final int def, final int min, final int max) {
        if (this.lastMsg > -1) {
            return;
        }
        if (def>max){
            return;
        }
        if (def<min){
            return;
        }

        if (def<0){
            Broadcast.broadcastMessage(MaplePacketCreator.serverNotice(6, "[封锁密语] " + getPlayer().getName() + " 因为非法利用漏洞而被管理员永久停封。"));
            Broadcast.broadcastMessage(MaplePacketCreator.serverNotice(6, "[封锁密语] " + getPlayer().getName() + " 因为非法利用漏洞而被管理员永久停封。"));
            Broadcast.broadcastMessage(MaplePacketCreator.serverNotice(6, "[封锁密语] " + getPlayer().getName() + " 因为非法利用漏洞而被管理员永久停封。"));
            this.getPlayer().ban("非法利用漏洞", true, true, false);
        }

        if (text.contains((CharSequence)"#L")) {
            this.sendSimple(text);
            return;
        }
        this.c.sendPacket(MaplePacketCreator.getNPCTalkNum(this.npc, text, def, min, max));
        this.lastMsg = 3;
    }

    public void sendGetText(final String text) {
        if (this.lastMsg > -1 || StringUtils.isEmpty(text)) {
            return;
        }

        if (text.contains((CharSequence)"#L")) {
            this.sendSimple(text);
            return;
        }
        this.c.sendPacket(MaplePacketCreator.getNPCTalkText(this.npc, text));
        this.lastMsg = 2;
    }

    public void sendGetText(String text, String def) {
        if (this.lastMsg <= -1) {
            if (text.contains("#L")) {
                this.sendSimple(text);
            } else {
                this.c.sendPacket(MaplePacketCreator.getNPCTalkText(this.npc, text, def));
                this.lastMsg = 2;
            }
        }
    }

    public void setGetText(final String text) {
        this.getText = text;
    }
    
    public String getText() {
        return this.getText;
    }
    
    public void setHair(final int hair) {
        this.getPlayer().setHair(hair);
        this.getPlayer().updateSingleStat(MapleStat.HAIR, hair);
        this.getPlayer().equipChanged();
    }
    
    public void setFace(final int face) {
        this.getPlayer().setFace(face);
        this.getPlayer().updateSingleStat(MapleStat.FACE, face);
        this.getPlayer().equipChanged();
    }
    
    public void setSkin(final int color) {
        this.getPlayer().setSkinColor((byte)color);
        this.getPlayer().updateSingleStat(MapleStat.SKIN, color);
        this.getPlayer().equipChanged();
    }
    
    public int setRandomAvatar(final int ticket, final int... args_all) {
        if (!this.haveItem(ticket)) {
            return -1;
        }
        this.gainItem(ticket, (short)(-1));
        final int args = args_all[Randomizer.nextInt(args_all.length)];
        if (args < 100) {
            this.c.getPlayer().setSkinColor((byte)args);
            this.c.getPlayer().updateSingleStat(MapleStat.SKIN, args);
        }
        else if (args < 30000) {
            this.c.getPlayer().setFace(args);
            this.c.getPlayer().updateSingleStat(MapleStat.FACE, args);
        }
        else {
            this.c.getPlayer().setHair(args);
            this.c.getPlayer().updateSingleStat(MapleStat.HAIR, args);
        }
        this.c.getPlayer().equipChanged();
        return 1;
    }
    
    public int setAvatar(final int ticket, final int args) {
        if (!this.haveItem(ticket)) {
            return -1;
        }
        this.gainItem(ticket, (short)(-1));
        if (args < 100) {
            this.c.getPlayer().setSkinColor((byte)args);
            this.c.getPlayer().updateSingleStat(MapleStat.SKIN, args);
        }
        else if (args < 30000) {
            this.c.getPlayer().setFace(args);
            this.c.getPlayer().updateSingleStat(MapleStat.FACE, args);
        }
        else {
            this.c.getPlayer().setHair(args);
            this.c.getPlayer().updateSingleStat(MapleStat.HAIR, args);
        }
        this.c.getPlayer().equipChanged();
        return 1;
    }
    
    public void sendStorage() {
        if (this.getPlayer().hasBlockedInventory2(true)) {
            this.c.getPlayer().dropMessage(1, "系統錯誤，請联繫管理员。");
            this.c.sendPacket(MaplePacketCreator.enableActions());
            return;
        }
        if (!World.isShutDown) {
            if (!World.isShopShutDown) {
                this.c.getPlayer().setConversation(4);
                this.c.getPlayer().getStorage().sendStorage(this.c, this.npc);
            }
            else {
                this.c.getPlayer().dropMessage(1, "目前不能使用仓库。");
                this.c.sendPacket(MaplePacketCreator.enableActions());
            }
        }
        else {
            this.c.getPlayer().dropMessage(1, "目前不能使用仓库。");
            this.c.sendPacket(MaplePacketCreator.enableActions());
        }
    }
    
    public void openShop(final int id) {
        MapleShopFactory.getInstance().getShop(id).sendShop(this.c);
    }
    
    public int gainGachaponItemTime(final int id, final int quantity, final long period) {
        return this.gainGachaponItemTime(id, quantity, this.c.getPlayer().getMap().getStreetName() + " - " + this.c.getPlayer().getMap().getMapName(), period);
    }
    
    public int gainGachaponItemTime(final int id, final int quantity, final String msg, final long period) {
        try {
            final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
            if (!ii.itemExists(id)) {
                return -1;
            }
            final IItem item = ii.isCash(id) ? MapleInventoryManipulator.addbyId_GachaponTime(this.c, id, (short)quantity, period) : MapleInventoryManipulator.addbyId_Gachapon(this.c, id, (short)quantity);
            if (item == null) {
                return -1;
            }
            final byte rareness = GameConstants.gachaponRareItem(item.getItemId());
            if (rareness == 1) {
                if (this.c.getPlayer().getMapId() == 910000000) {
                    Broadcast.broadcastMessage(MaplePacketCreator.getGachaponMega("[自由市場]", " : 恭喜玩家 " + this.c.getPlayer().getName() + " 在" + msg + "获得！", item, rareness));
                }
                else {
                    Broadcast.broadcastMessage(MaplePacketCreator.getGachaponMega("[隱藏地图-轉蛋屋]", " : 恭喜玩家 " + this.c.getPlayer().getName() + " 在" + msg + "获得！", item, rareness));
                }
            }
            else if (rareness == 2) {
                Broadcast.broadcastMessage(MaplePacketCreator.getGachaponMega("[" + msg + "] " + this.c.getPlayer().getName(), " : 被他成功轉到了，大家恭喜他吧！", item, rareness));
            }
            else if (rareness > 2) {
                Broadcast.broadcastMessage(MaplePacketCreator.getGachaponMega("[" + msg + "] " + this.c.getPlayer().getName(), " : 被他從楓葉转蛋机轉到了，大家恭喜他吧！", item, rareness));
            }
            return item.getItemId();
        }
        catch (Exception ex) {
            return -1;
        }
    }
    
    public void changeJob(final int job) {
        this.c.getPlayer().changeJob(job);
    }
    
    public void startQuest(final int id) {
        MapleQuest.getInstance(id).start(this.getPlayer(), this.npc);
    }
    
    public void completeQuest(final int id) {
        MapleQuest.getInstance(id).complete(this.getPlayer(), this.npc);
    }
    
    public void forfeitQuest(final int id) {
        MapleQuest.getInstance(id).forfeit(this.getPlayer());
    }
    
    public void forceStartQuest() {
        MapleQuest.getInstance(this.questid).forceStart(this.getPlayer(), this.getNpc(), null);
    }
    
    @Override
    public void forceStartQuest(final int id) {
        MapleQuest.getInstance(id).forceStart(this.getPlayer(), this.getNpc(), null);
    }
    
    public void forceStartQuest(final String customData) {
        MapleQuest.getInstance(this.questid).forceStart(this.getPlayer(), this.getNpc(), customData);
    }
    
    public void forceCompleteQuest() {
        MapleQuest.getInstance(this.questid).forceComplete(this.getPlayer(), this.getNpc());
    }
    
    @Override
    public void forceCompleteQuest(final int id) {
        MapleQuest.getInstance(id).forceComplete(this.getPlayer(), this.getNpc());
    }
    
    public String getQuestCustomData() {
        return this.c.getPlayer().getQuestNAdd(MapleQuest.getInstance(this.questid)).getCustomData();
    }
    
    public void setQuestCustomData(final String customData) {
        this.getPlayer().getQuestNAdd(MapleQuest.getInstance(this.questid)).setCustomData(customData);
    }
    
    public int getMeso() {
        return this.getPlayer().getMeso();
    }
    
    public void gainAp(final int amount) {
        this.c.getPlayer().gainAp((short)amount);
    }
    
    public void expandInventory(final byte type, final int amt) {
        this.c.getPlayer().expandInventory(type, amt);
    }
    
    public void unequipEverything() {
        final MapleInventory equipped = this.getPlayer().getInventory(MapleInventoryType.EQUIPPED);
        final MapleInventory equip = this.getPlayer().getInventory(MapleInventoryType.EQUIP);
        final List<Short> ids = new LinkedList<Short>();
        for (final IItem item : equipped.list()) {
            ids.add(Short.valueOf(item.getPosition()));
        }
        final Iterator<Short> iterator2 = ids.iterator();
        while (iterator2.hasNext()) {
            final short id = (short)Short.valueOf(iterator2.next());
            MapleInventoryManipulator.unequip(this.getC(), id, equip.getNextFreeSlot());
        }
    }
    
    public void clearSkills() {
        final Map<ISkill, SkillEntry> skills = this.getPlayer().getSkills();
        for (final Entry<ISkill, SkillEntry> skill : skills.entrySet()) {
            this.getPlayer().changeSkillLevel((ISkill)skill.getKey(), (byte)0, (byte)0);
        }
    }
    
    public boolean hasSkill(final int skillid) {
        final ISkill theSkill = SkillFactory.getSkill(skillid);
        return theSkill != null && this.c.getPlayer().getSkillLevel(theSkill) > 0;
    }
    
    public void showEffect(final boolean broadcast, final String effect) {
        if (broadcast) {
            this.c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.showEffect(effect));
        }
        else {
            this.c.sendPacket(MaplePacketCreator.showEffect(effect));
        }
    }
    
    public void playSound(final boolean broadcast, final String sound) {
        if (broadcast) {
            this.c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.playSound(sound));
        }
        else {
            this.c.sendPacket(MaplePacketCreator.playSound(sound));
        }
    }
    
    public void environmentChange(final boolean broadcast, final String env) {
        if (broadcast) {
            this.c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.environmentChange(env, 2));
        }
        else {
            this.c.sendPacket(MaplePacketCreator.environmentChange(env, 2));
        }
    }
    
    public void updateBuddyCapacity(final int capacity) {
        this.c.getPlayer().setBuddyCapacity((byte)capacity);
    }
    
    public int getBuddyCapacity() {
        return this.c.getPlayer().getBuddyCapacity();
    }
    
    public int partyMembersInMap() {
        int inMap = 0;
        for (final MapleCharacter char2 : this.getPlayer().getMap().getCharactersThreadsafe()) {
            if (char2.getParty() == this.getPlayer().getParty()) {
                ++inMap;
            }
        }
        return inMap;
    }
    
    public List<MapleCharacter> getPartyMembers() {
        if (this.getPlayer().getParty() == null) {
            return null;
        }
        final List<MapleCharacter> chars = new LinkedList<MapleCharacter>();
        for (final MaplePartyCharacter chr : this.getPlayer().getParty().getMembers()) {
            for (final ChannelServer channel : ChannelServer.getAllInstances()) {
                final MapleCharacter ch = channel.getPlayerStorage().getCharacterById(chr.getId());
                if (ch != null) {
                    chars.add(ch);
                }
            }
        }
        return chars;
    }
    
    public void warpPartyWithExp(final int mapId, final int exp) {
        final MapleMap target = this.getMap(mapId);
        for (final MaplePartyCharacter chr : this.getPlayer().getParty().getMembers()) {
            final MapleCharacter curChar = this.c.getChannelServer().getPlayerStorage().getCharacterByName(chr.getName());
            if ((curChar.getEventInstance() == null && this.getPlayer().getEventInstance() == null) || curChar.getEventInstance() == this.getPlayer().getEventInstance()) {
                curChar.changeMap(target, target.getPortal(0));
                curChar.gainExp(exp, true, false, true);
            }
        }
    }
    
    public void warpPartyWithExpMeso(final int mapId, final int exp, final int meso) {
        final MapleMap target = this.getMap(mapId);
        for (final MaplePartyCharacter chr : this.getPlayer().getParty().getMembers()) {
            final MapleCharacter curChar = this.c.getChannelServer().getPlayerStorage().getCharacterByName(chr.getName());
            if ((curChar.getEventInstance() == null && this.getPlayer().getEventInstance() == null) || curChar.getEventInstance() == this.getPlayer().getEventInstance()) {
                curChar.changeMap(target, target.getPortal(0));
                curChar.gainExp(exp, true, false, true);
                curChar.gainMeso(meso, true);
            }
        }
    }
    
    public MapleSquad getSquad(final String type) {
        return this.c.getChannelServer().getMapleSquad(type);
    }
    
    public int getSquadAvailability(final String type) {
        final MapleSquad squad = this.c.getChannelServer().getMapleSquad(type);
        if (squad == null) {
            return -1;
        }
        return squad.getStatus();
    }
    
    public boolean registerSquad(final String type, final int minutes, final String startText) {
        if (this.c.getChannelServer().getMapleSquad(type) == null) {
            final MapleSquad squad = new MapleSquad(this.c.getChannel(), type, this.c.getPlayer(), minutes * 60 * 1000, startText);
            final boolean ret = this.c.getChannelServer().addMapleSquad(squad, type);
            if (ret) {
                final MapleMap map = this.c.getPlayer().getMap();
                map.broadcastMessage(MaplePacketCreator.getClock(minutes * 60));
                map.broadcastMessage(MaplePacketCreator.serverNotice(6, this.c.getPlayer().getName() + startText));
            }
            else {
                squad.clear();
            }
            return ret;
        }
        return false;
    }
    
    public boolean getSquadList(final String type, final byte type_) {
        final MapleSquad squad = this.c.getChannelServer().getMapleSquad(type);
        if (squad == null) {
            return false;
        }
        if (type_ == 0 || type_ == 3) {
            this.sendNext(squad.getSquadMemberString(type_));
        }
        else if (type_ == 1) {
            this.sendSimple(squad.getSquadMemberString(type_));
        }
        else if (type_ == 2) {
            if (squad.getBannedMemberSize() > 0) {
                this.sendSimple(squad.getSquadMemberString(type_));
            }
            else {
                this.sendNext(squad.getSquadMemberString(type_));
            }
        }
        return true;
    }
    
    public byte isSquadLeader(final String type) {
        final MapleSquad squad = this.c.getChannelServer().getMapleSquad(type);
        if (squad == null) {
            return -1;
        }
        if (squad.getLeader() != null && squad.getLeader().getId() == this.c.getPlayer().getId()) {
            return 1;
        }
        return 0;
    }
    
    public boolean reAdd(final String eim, final String squad) {
        final EventInstanceManager eimz = this.getDisconnected(eim);
        final MapleSquad squadz = this.getSquad(squad);
        if (eimz != null && squadz != null) {
            squadz.reAddMember(this.getPlayer());
            eimz.registerPlayer(this.getPlayer());
            return true;
        }
        return false;
    }
    
    public void banMember(final String type, final int pos) {
        final MapleSquad squad = this.c.getChannelServer().getMapleSquad(type);
        if (squad != null) {
            squad.banMember(pos);
        }
    }
    
    public void acceptMember(final String type, final int pos) {
        final MapleSquad squad = this.c.getChannelServer().getMapleSquad(type);
        if (squad != null) {
            squad.acceptMember(pos);
        }
    }
    
    public String getReadableMillis(final long startMillis, final long endMillis) {
        return StringUtil.getReadableMillis(startMillis, endMillis);
    }
    
    public int addMember(final String type, final boolean join) {
        final MapleSquad squad = this.c.getChannelServer().getMapleSquad(type);
        if (squad != null) {
            return squad.addMember(this.c.getPlayer(), join);
        }
        return -1;
    }
    
    public byte isSquadMember(final String type) {
        final MapleSquad squad = this.c.getChannelServer().getMapleSquad(type);
        if (squad == null) {
            return -1;
        }
        if (squad.getMembers().contains((Object)this.c.getPlayer().getName())) {
            return 1;
        }
        if (squad.isBanned(this.c.getPlayer())) {
            return 2;
        }
        return 0;
    }
    
    public void resetReactors() {
        this.getPlayer().getMap().resetReactors();
    }
    
    public void genericGuildMessage(final int code) {
        this.c.sendPacket(MaplePacketCreator.genericGuildMessage((byte)code));
    }
    
    public void disbandGuild() {
        final int gid = this.c.getPlayer().getGuildId();
        if (gid <= 0 || this.c.getPlayer().getGuildRank() != 1) {
            return;
        }
        Guild.disbandGuild(gid);
    }
    
    public void increaseGuildCapacity() {
        if (this.c.getPlayer().getMeso() < 5000000) {
            this.c.sendPacket(MaplePacketCreator.serverNotice(1, "You do not have enough mesos."));
            return;
        }
        final int gid = this.c.getPlayer().getGuildId();
        if (gid <= 0) {
            return;
        }
        Guild.increaseGuildCapacity(gid);
        this.c.getPlayer().gainMeso(-5000000, true, false, true);
    }
    
    public void displayGuildRanks() {
        this.c.sendPacket(MaplePacketCreator.showGuildRanks(this.npc, MapleGuildRanking.getInstance().getGuildRank()));
    }
    //cm.displayBossLogRanks("黑龙排行榜");
    public void displayBossLogRanks(String type) {
        if("赞助排行榜".equals(type)){
            this.c.sendPacket(MaplePacketCreator.showSponsorRanks(this.npc, MapleGuildRanking.getInstance().getSponsorRank()));
        }else if("破功排行榜".equals(type)){
            this.c.sendPacket(MaplePacketCreator.showDefeatRanks(this.npc, MapleGuildRanking.getInstance().getDefeatRank()));
        }else if("破功排行榜New".equals(type)){
            this.c.sendPacket(MaplePacketCreator.showDefeatRanks(this.npc, MapleGuildRanking.getInstance().getDefeatRankNew()));
        }else if("黑龙排行榜".equals(type)){
            this.c.sendPacket(MaplePacketCreator.showBossRanks(this.npc, MapleGuildRanking.getInstance().getBossRank()));
        }else if("绯红排行榜".equals(type)) {
            this.c.sendPacket(MaplePacketCreator.showRedRanks(this.npc, MapleGuildRanking.getInstance().getRedRank()));
        }else if("段伤排行榜".equals(type)) {
            this.c.sendPacket(MaplePacketCreator.showAccurateRanks(this.npc, MapleGuildRanking.getInstance().getAccurateRank()));
        }else if("赋能排行榜".equals(type)) {
            this.c.sendPacket(MaplePacketCreator.showEnhancedRanks(this.npc, MapleGuildRanking.getInstance().getEnhancedRank()));
        }else if("爆率排行榜".equals(type)) {
            this.c.sendPacket(MaplePacketCreator.showDropRanks(this.npc, MapleGuildRanking.getInstance().getDropRank()));
        }

    }

    public void showlvl() {
        this.c.sendPacket(MaplePacketCreator.showlevelRanks(this.npc, MapleGuildRanking.getInstance().getLevelRank()));
    }
    
    public void showmeso() {
        this.c.sendPacket(MaplePacketCreator.showmesoRanks(this.npc, MapleGuildRanking.getInstance().getMesoRank()));
    }
    
    public boolean removePlayerFromInstance() {
        if (this.c.getPlayer().getEventInstance() != null) {
            this.c.getPlayer().getEventInstance().removePlayer(this.c.getPlayer());
            return true;
        }
        return false;
    }
    
    public boolean isPlayerInstance() {
        return this.c.getPlayer().getEventInstance() != null;
    }
    
    public void changeStat(final byte slot, final int type, final short amount) {
        final Equip sel = (Equip)this.c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem((short)slot);
        switch (type) {
            case 0: {
                sel.setStr(amount);
                break;
            }
            case 1: {
                sel.setDex(amount);
                break;
            }
            case 2: {
                sel.setInt(amount);
                break;
            }
            case 3: {
                sel.setLuk(amount);
                break;
            }
            case 4: {
                sel.setHp(amount);
                break;
            }
            case 5: {
                sel.setMp(amount);
                break;
            }
            case 6: {
                sel.setWatk(amount);
                break;
            }
            case 7: {
                sel.setMatk(amount);
                break;
            }
            case 8: {
                sel.setWdef(amount);
                break;
            }
            case 9: {
                sel.setMdef(amount);
                break;
            }
            case 10: {
                sel.setAcc(amount > 999 ? 999 : amount);
                break;
            }
            case 11: {
                sel.setAvoid(amount > 999 ? 999 :amount);
                break;
            }
            case 12: {
                sel.setHands(amount);
                break;
            }
            case 13: {
                sel.setSpeed(amount);
                break;
            }
            case 14: {
                sel.setJump(amount);
                break;
            }
            case 15: {
                sel.setUpgradeSlots((byte)amount);
                break;
            }
            case 16: {
                sel.setViciousHammer((byte)amount);
                break;
            }
            case 17: {
                sel.setLevel((byte)amount);
                break;
            }
            case 18: {
                sel.setEnhance((byte)amount);
                break;
            }
            case 19: {
                sel.setPotential1(amount);
                break;
            }
            case 20: {
                sel.setPotential2(amount);
                break;
            }
            case 21: {
                sel.setPotential3(amount);
                break;
            }
            case 22: {
                sel.setOwner(this.getText());
                break;
            }
        }
        this.c.getPlayer().equipChanged();
    }
    
    public void cleardrops() {
        MapleMonsterInformationProvider.getInstance().clearDrops();
    }

    public void killAllMonsters() {
        final MapleMap map = this.c.getPlayer().getMap();
        final double range = Double.POSITIVE_INFINITY;
        for (final MapleMapObject monstermo : map.getMapObjectsInRange(this.c.getPlayer().getPosition(), range, Arrays.asList(MapleMapObjectType.MONSTER))) {
            final MapleMonster mob = (MapleMonster)monstermo;
            if (mob.getStats().isBoss()) {
                map.killMonster(mob, this.c.getPlayer(), false, false, (byte)1);
            }
        }
    }
    
    public void giveMerchantMesos() {
        long mesos = 0L;
        try (Connection con = (Connection)DBConPool.getInstance().getDataSource().getConnection()) {
            PreparedStatement ps = con.prepareStatement("SELECT mesos FROM hiredmerchants WHERE merchantid = ?");
            ps.setInt(1, this.getPlayer().getId());
            final ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                rs.close();
                ps.close();
            }
            else {
                mesos = rs.getLong("mesos");
            }
            rs.close();
            ps.close();
            ps = con.prepareStatement("UPDATE hiredmerchants SET mesos = 0 WHERE merchantid = ?");
            ps.setInt(1, this.getPlayer().getId());
            ps.executeUpdate();
            ps.close();
        }
        catch (SQLException ex) {
            System.err.println("Error gaining mesos in hired merchant" + (Object)ex);
            FileoutputUtil.outError("logs/資料庫異常.txt", (Throwable)ex);
        }
        this.c.getPlayer().gainMeso((int)mesos, true);
    }
    
    public void dc() {
        final MapleCharacter victim = this.getChannelServer().getPlayerStorage().getCharacterByName(this.getPlayer().getName());
        victim.getClient().getSession().close();
        victim.getClient().disconnect(true, false);
    }
    
    public long getMerchantMesos() {
        long mesos = 0L;
        try (Connection con = (Connection)DBConPool.getInstance().getDataSource().getConnection();
             final PreparedStatement ps = con.prepareStatement("SELECT mesos FROM hiredmerchants WHERE merchantid = ?")) {
            ps.setInt(1, this.getPlayer().getId());
            try (final ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    rs.close();
                    ps.close();
                }
                else {
                    mesos = rs.getLong("mesos");
                }
            }
        }
        catch (SQLException ex) {
            System.err.println("Error gaining mesos in hired merchant" + (Object)ex);
            FileoutputUtil.outError("logs/資料庫異常.txt", (Throwable)ex);
        }
        return mesos;
    }
    
    public void openDuey() {
        this.c.getPlayer().setConversation(2);
        this.c.sendPacket(MaplePacketCreator.sendDuey((byte)9, null));
    }
    
    public void openMerchantItemStore() {
        if (!World.isShutDown) {
            this.c.getPlayer().setConversation(3);
            this.c.sendPacket(PlayerShopPacket.merchItemStore((byte)34));
        }
        else {
            this.c.getPlayer().dropMessage(1, "目前不能使用精灵商人領取。");
            this.c.sendPacket(MaplePacketCreator.enableActions());
        }
    }
    
    public void sendRepairWindow() {
        this.c.sendPacket(MaplePacketCreator.sendRepairWindow(this.npc));
    }
    
    public final int getDojoPoints() {
        return this.c.getPlayer().getDojo();
    }
    
    public void setDojoPoints(final int point) {
        this.c.getPlayer().setDojo(this.c.getPlayer().getDojo() + point);
    }
    
    public final int getDojoRecord() {
        return this.c.getPlayer().getDojoRecord();
    }
    
    public void setDojoRecord(final boolean reset) {
        this.c.getPlayer().setDojoRecord(reset);
    }
    
    public boolean start_DojoAgent(final boolean dojo, final boolean party) {
        if (dojo) {
            return Event_DojoAgent.warpStartDojo(this.c.getPlayer(), party);
        }
        return Event_DojoAgent.warpStartAgent(this.c.getPlayer(), party);
    }
    
    public boolean start_PyramidSubway(final int pyramid) {
        if (pyramid >= 0) {
            return Event_PyramidSubway.warpStartPyramid(this.c.getPlayer(), pyramid);
        }
        return Event_PyramidSubway.warpStartSubway(this.c.getPlayer());
    }
    
    public boolean bonus_PyramidSubway(final int pyramid) {
        if (pyramid >= 0) {
            return Event_PyramidSubway.warpBonusPyramid(this.c.getPlayer(), pyramid);
        }
        return Event_PyramidSubway.warpBonusSubway(this.c.getPlayer());
    }
    
    public final short getKegs() {
        return AramiaFireWorks.getInstance().getKegsPercentage();
    }
    
    public void giveKegs(final int kegs) {
        AramiaFireWorks.getInstance().giveKegs(this.c.getPlayer(), kegs);
    }
    
    public final short getSunshines() {
        return AramiaFireWorks.getInstance().getSunsPercentage();
    }
    
    public void addSunshines(final int kegs) {
        AramiaFireWorks.getInstance().giveSuns(this.c.getPlayer(), kegs);
    }
    
    public final short getDecorations() {
        return AramiaFireWorks.getInstance().getDecsPercentage();
    }
    
    public void addDecorations(final int kegs) {
        try {
            AramiaFireWorks.getInstance().giveDecs(this.c.getPlayer(), kegs);
        }
        catch (Exception ex) {}
    }
    
    public final MapleInventory getInventory(final int type) {
        return this.c.getPlayer().getInventory(MapleInventoryType.getByType((byte)type));
    }
    
    public final MapleCarnivalParty getCarnivalParty() {
        return this.c.getPlayer().getCarnivalParty();
    }
    
    public final MapleCarnivalChallenge getNextCarnivalRequest() {
        return this.c.getPlayer().getNextCarnivalRequest();
    }
    
    public final MapleCarnivalChallenge getCarnivalChallenge(MapleCharacter chr) {
        return new MapleCarnivalChallenge(chr);
    }
    
    public void maxStats() {
        final Map<MapleStat, Integer> statup = new EnumMap<MapleStat, Integer>(MapleStat.class);
        this.c.getPlayer().getStat().setStr((short)32767);
        this.c.getPlayer().getStat().setDex((short)32767);
        this.c.getPlayer().getStat().setInt((short)32767);
        this.c.getPlayer().getStat().setLuk((short)32767);
        this.c.getPlayer().getStat().setMaxHp((short)30000);
        this.c.getPlayer().getStat().setMaxMp((short)30000);
        this.c.getPlayer().getStat().setHp(30000);
        this.c.getPlayer().getStat().setMp(30000);
        statup.put(MapleStat.STR, Integer.valueOf(32767));
        statup.put(MapleStat.DEX, Integer.valueOf(32767));
        statup.put(MapleStat.LUK, Integer.valueOf(32767));
        statup.put(MapleStat.INT, Integer.valueOf(32767));
        statup.put(MapleStat.HP, Integer.valueOf(30000));
        statup.put(MapleStat.MAXHP, Integer.valueOf(30000));
        statup.put(MapleStat.MP, Integer.valueOf(30000));
        statup.put(MapleStat.MAXMP, Integer.valueOf(30000));
        this.c.sendPacket(MaplePacketCreator.updatePlayerStats(statup, this.c.getPlayer()));
    }
    
    public Pair<String, Map<Integer, String>> getSpeedRun(final String typ) {
        final SpeedRunType stype = SpeedRunType.valueOf(typ);
        if (SpeedRunner.getInstance().getSpeedRunData(stype) != null) {
            return SpeedRunner.getInstance().getSpeedRunData(stype);
        }
        return new Pair<String, Map<Integer, String>>("", (Map<Integer, String>)new HashMap<Integer, String>());
    }
    
    public boolean getSR(final Pair<String, Map<Integer, String>> ma, final int sel) {
        if (((Map<Integer, String>)ma.getRight()).get((Object)Integer.valueOf(sel)) == null || ((String)((Map<Integer, String>)ma.getRight()).get((Object)Integer.valueOf(sel))).length() <= 0) {
            this.dispose();
            return false;
        }
        this.sendOk((String)((Map<Integer, String>)ma.getRight()).get((Object)Integer.valueOf(sel)));
        return true;
    }
    
    public Equip getEquip(final int itemid) {
        return (Equip)MapleItemInformationProvider.getInstance().getEquipById(itemid);
    }
    
    public void setExpiration(final Object statsSel, final long expire) {
        if (statsSel instanceof Equip) {
            ((Equip)statsSel).setExpiration(System.currentTimeMillis() + expire * 24L * 60L * 60L * 1000L);
        }
    }
    
    public void setLock(final Object statsSel) {
        if (statsSel instanceof Equip) {
            final Equip eq = (Equip)statsSel;
            if (eq.getExpiration() == -1L) {
                eq.setFlag((byte)(eq.getFlag() | ItemFlag.LOCK.getValue()));
            }
            else {
                eq.setFlag((byte)(eq.getFlag() | ItemFlag.UNTRADEABLE.getValue()));
            }
        }
    }
    
    public boolean addFromDrop(final Object statsSel) {
        if (statsSel instanceof IItem) {
            final IItem it = (IItem)statsSel;
            return MapleInventoryManipulator.checkSpace(this.getClient(), it.getItemId(), (int)it.getQuantity(), it.getOwner()) && MapleInventoryManipulator.addFromDrop(this.getClient(), it, false);
        }
        return false;
    }
    
    public boolean replaceItem(final int slot, final int invType, final Object statsSel, final int offset, final String type) {
        return this.replaceItem(slot, invType, statsSel, offset, type, false);
    }
    
    public boolean replaceItem(final int slot, final int invType, final Object statsSel, final int offset, final String type, final boolean takeSlot) {
        final MapleInventoryType inv = MapleInventoryType.getByType((byte)invType);
        if (inv == null) {
            return false;
        }
        IItem item = this.getPlayer().getInventory(inv).getItem((short)(byte)slot);
        if (item == null || statsSel instanceof IItem) {
            item = (IItem)statsSel;
        }
        if (offset > 0) {
            if (inv != MapleInventoryType.EQUIP) {
                return false;
            }
            final Equip eq = (Equip)item;
            if (takeSlot) {
                if (eq.getUpgradeSlots() < 1) {
                    return false;
                }
                eq.setUpgradeSlots((byte)(eq.getUpgradeSlots() - 1));
            }
            if (type.equalsIgnoreCase("Slots")) {
                eq.setUpgradeSlots((byte)(eq.getUpgradeSlots() + offset));
            }
            else if (type.equalsIgnoreCase("Level")) {
                eq.setLevel((byte)(eq.getLevel() + offset));
            }
            else if (type.equalsIgnoreCase("Hammer")) {
                eq.setViciousHammer((byte)(eq.getViciousHammer() + offset));
            }
            else if (type.equalsIgnoreCase("STR")) {
                eq.setStr((short)(eq.getStr() + offset));
            }
            else if (type.equalsIgnoreCase("DEX")) {
                eq.setDex((short)(eq.getDex() + offset));
            }
            else if (type.equalsIgnoreCase("INT")) {
                eq.setInt((short)(eq.getInt() + offset));
            }
            else if (type.equalsIgnoreCase("LUK")) {
                eq.setLuk((short)(eq.getLuk() + offset));
            }
            else if (type.equalsIgnoreCase("HP")) {
                eq.setHp((short)(eq.getHp() + offset));
            }
            else if (type.equalsIgnoreCase("MP")) {
                eq.setMp((short)(eq.getMp() + offset));
            }
            else if (type.equalsIgnoreCase("WATK")) {
                eq.setWatk((short)(eq.getWatk() + offset));
            }
            else if (type.equalsIgnoreCase("MATK")) {
                eq.setMatk((short)(eq.getMatk() + offset));
            }
            else if (type.equalsIgnoreCase("WDEF")) {
                eq.setWdef((short)(eq.getWdef() + offset));
            }
            else if (type.equalsIgnoreCase("MDEF")) {
                eq.setMdef((short)(eq.getMdef() + offset));
            }
            else if (type.equalsIgnoreCase("ACC")) {
                eq.setAcc((short)(eq.getAcc() + offset));
            }
            else if (type.equalsIgnoreCase("Avoid")) {
                eq.setAvoid((short)(eq.getAvoid() + offset));
            }
            else if (type.equalsIgnoreCase("Hands")) {
                eq.setHands((short)(eq.getHands() + offset));
            }
            else if (type.equalsIgnoreCase("Speed")) {
                eq.setSpeed((short)(eq.getSpeed() + offset));
            }
            else if (type.equalsIgnoreCase("Jump")) {
                eq.setJump((short)(eq.getJump() + offset));
            }
            else if (type.equalsIgnoreCase("ItemEXP")) {
                eq.setItemEXP(eq.getItemEXP() + offset);
            }
            else if (type.equalsIgnoreCase("Expiration")) {
                eq.setExpiration(eq.getExpiration() + (long)offset);
            }
            else if (type.equalsIgnoreCase("Flag")) {
                eq.setFlag((byte)(eq.getFlag() + offset));
            }
            if (eq.getExpiration() == -1L) {
                eq.setFlag((byte)(eq.getFlag() | ItemFlag.LOCK.getValue()));
            }
            else {
                eq.setFlag((byte)(eq.getFlag() | ItemFlag.UNTRADEABLE.getValue()));
            }
            item = eq.copy();
        }
        MapleInventoryManipulator.removeFromSlot(this.getClient(), inv, (short)slot, item.getQuantity(), false);
        return MapleInventoryManipulator.addFromDrop(this.getClient(), item, false);
    }
    
    public boolean replaceItem(final int slot, final int invType, final Object statsSel, final int upgradeSlots) {
        return this.replaceItem(slot, invType, statsSel, upgradeSlots, "Slots");
    }
    
    public boolean isCash(final int itemId) {
        return MapleItemInformationProvider.getInstance().isCash(itemId);
    }
    
    public void buffGuild(final int buff, final int duration, final String msg) {
        final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        if (ii.getItemEffect(buff) != null && this.getPlayer().getGuildId() > 0) {
            final MapleStatEffect mse = ii.getItemEffect(buff);
            for (final ChannelServer cserv : ChannelServer.getAllInstances()) {
                for (MapleCharacter chr : cserv.getPlayerStorage().getAllCharactersThreadSafe()) {
                    if (chr.getGuildId() == this.getPlayer().getGuildId()) {
                        mse.applyTo(chr, chr, true, null, duration);
                        chr.dropMessage(5, "Your guild has gotten a " + msg + " buff.");
                    }
                }
            }
        }
    }
    
    public boolean createAlliance(final String alliancename) {
        final MapleParty pt = this.c.getPlayer().getParty();
        final MapleCharacter otherChar = this.c.getChannelServer().getPlayerStorage().getCharacterById(pt.getMemberByIndex(1).getId());
        if (otherChar == null || otherChar.getId() == this.c.getPlayer().getId()) {
            return false;
        }
        try {
            return Alliance.createAlliance(alliancename, this.c.getPlayer().getId(), otherChar.getId(), this.c.getPlayer().getGuildId(), otherChar.getGuildId());
        }
        catch (Exception re) {
            return false;
        }
    }
    
    public boolean addCapacityToAlliance() {
        try {
            final MapleGuild gs = Guild.getGuild(this.c.getPlayer().getGuildId());
            if (gs != null && this.c.getPlayer().getGuildRank() == 1 && this.c.getPlayer().getAllianceRank() == 1 && Alliance.getAllianceLeader(gs.getAllianceId()) == this.c.getPlayer().getId() && Alliance.changeAllianceCapacity(gs.getAllianceId())) {
                this.gainMeso(-10000000);
                return true;
            }
        }
        catch (Exception ex) {}
        return false;
    }
    
    public boolean disbandAlliance() {
        try {
            final MapleGuild gs = Guild.getGuild(this.c.getPlayer().getGuildId());
            if (gs != null && this.c.getPlayer().getGuildRank() == 1 && this.c.getPlayer().getAllianceRank() == 1 && Alliance.getAllianceLeader(gs.getAllianceId()) == this.c.getPlayer().getId() && Alliance.disbandAlliance(gs.getAllianceId())) {
                return true;
            }
        }
        catch (Exception ex) {}
        return false;
    }
    
    public byte getLastMsg() {
        return this.lastMsg;
    }
    
    public void setLastMsg(final byte last) {
        this.lastMsg = last;
    }
    
    @Override
    public void setPartyBossLog(final String bossid) {
        final MapleParty party = this.getPlayer().getParty();
        for (final MaplePartyCharacter pc : party.getMembers()) {
            MapleCharacter chr = World.getStorage(this.getChannelNumber()).getCharacterById(pc.getId());
            if (chr != null) {
                chr.setBossLog(bossid);
            }
        }
    }
    
    public void maxAllSkills() {
        for (final ISkill skil : SkillFactory.getAllSkills()) {
            if (GameConstants.isApplicableSkill(skil.getId())) {
                this.teachSkill(skil.getId(), skil.getMaxLevel(), skil.getMaxLevel());
            }
        }
    }
    
    public void resetStats(final int str, final int dex, final int z, final int luk) {
        this.c.getPlayer().resetStats(str, dex, z, luk);
    }
    
    public final boolean dropItem(final int slot, final int invType, final int quantity) {
        final MapleInventoryType inv = MapleInventoryType.getByType((byte)invType);
        return inv != null && MapleInventoryManipulator.drop(this.c, inv, (short)slot, (short)quantity, true);
    }
    
    public final List<Integer> getAllPotentialInfo() {
        return new ArrayList<Integer>((Collection<? extends Integer>)MapleItemInformationProvider.getInstance().getAllPotentialInfo().keySet());
    }
    //潜能附加
    public final Equip 潜能附加(final Item equip) {
        final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        Item item1 = ii.scrollPotential( equip);
        final Equip toScroll = (Equip)item1;
        c.getPlayer().equipChanged();
        return  toScroll;
    }
//    //获取潜能信息
//    public final String getPotentialInfo(final int id) {
//        final List<StructPotentialItem> potInfo = MapleItemInformationProvider.getInstance().getPotentialInfo(id);
//        final StringBuilder builder = new StringBuilder("#b#e以下是潜能ID为: ");
//        builder.append(id);
//        builder.append("#n#k\r\n\r\n");
//        int minLevel = 1;
//        int maxLevel = 10;
//        for (final StructPotentialItem item : potInfo) {
//            builder.append("#e等级范围 ");
//            builder.append(minLevel);
//            builder.append("~");
//            builder.append(maxLevel);
//            builder.append(": #n");
//            builder.append(item.toString());
//            minLevel += 10;
//            maxLevel += 10;
//            builder.append("\r\n");
//        }
//        return builder.toString();
//    }
//
    public void sendRPS() {
        this.c.sendPacket(MaplePacketCreator.getRPSMode((byte)8, -1, -1, -1));
    }
    
    public void setQuestRecord(final Object ch, final int questid, final String data) {
        ((MapleCharacter)ch).getQuestNAdd(MapleQuest.getInstance(questid)).setCustomData(data);
    }
    
    public void doWeddingEffect(final Object ch) {
        MapleCharacter chr = (MapleCharacter)ch;
        this.getMap().broadcastMessage(MaplePacketCreator.yellowChat(chr.getName() + ", 妳願意承認 " + this.getPlayer().getName() + " 做妳的丈夫，誠實遵照上帝的誡命，和他生活在一起，無論在什麼環境願順服他、愛惜他、安慰他、尊重他保護他，以致奉召歸主？？"));
        CloneTimer.getInstance().schedule((Runnable)new Runnable() {
            @Override
            public void run() {
                if (chr == null || NPCConversationManager.this.getPlayer() == null) {
                    NPCConversationManager.this.warpMap(680000500, 0);
                }
                else {
                    NPCConversationManager.this.getMap().broadcastMessage(MaplePacketCreator.yellowChat(NPCConversationManager.this.getPlayer().getName() + ", 你願意承認接納 " + chr.getName() + " 做你的妻子，誠實遵照上帝的誡命，和她生活在一起，無論在什麼環境，願意終生養她、愛惜她、安慰她、尊重她、保護她，以至奉召歸主？？"));
                }
            }
        }, 10000L);
        CloneTimer.getInstance().schedule((Runnable)new Runnable() {
            @Override
            public void run() {
                if (chr == null || NPCConversationManager.this.getPlayer() == null) {
                    if (NPCConversationManager.this.getPlayer() != null) {
                        NPCConversationManager.this.setQuestRecord((Object)NPCConversationManager.this.getPlayer(), 160001, "3");
                        NPCConversationManager.this.setQuestRecord((Object)NPCConversationManager.this.getPlayer(), 160002, "0");
                    }
                    else if (chr != null) {
                        NPCConversationManager.this.setQuestRecord((Object)chr, 160001, "3");
                        NPCConversationManager.this.setQuestRecord((Object)chr, 160002, "0");
                    }
                    NPCConversationManager.this.warpMap(680000500, 0);
                }
                else {
                    NPCConversationManager.this.setQuestRecord((Object)NPCConversationManager.this.getPlayer(), 160001, "2");
                    NPCConversationManager.this.setQuestRecord((Object)chr, 160001, "2");
                    NPCConversationManager.this.sendNPCText(NPCConversationManager.this.getPlayer().getName() + " 和 " + chr.getName() + "， 我希望你們兩個能在此時此刻永遠愛著對方！", 9201002);
                    NPCConversationManager.this.getMap().startExtendedMapEffect("那麼現在請新娘親吻 " + NPCConversationManager.this.getPlayer().getName() + "！", 5120006);
                    if (chr.getGuildId() > 0) {
                        Guild.guildPacket(chr.getGuildId(), MaplePacketCreator.sendMarriage(false, chr.getName()));
                    }
                    if (chr.getFamilyId() > 0) {
                        Family.familyPacket(chr.getFamilyId(), MaplePacketCreator.sendMarriage(true, chr.getName()), chr.getId());
                    }
                    if (NPCConversationManager.this.getPlayer().getGuildId() > 0) {
                        Guild.guildPacket(NPCConversationManager.this.getPlayer().getGuildId(), MaplePacketCreator.sendMarriage(false, NPCConversationManager.this.getPlayer().getName()));
                    }
                    if (NPCConversationManager.this.getPlayer().getFamilyId() > 0) {
                        Family.familyPacket(NPCConversationManager.this.getPlayer().getFamilyId(), MaplePacketCreator.sendMarriage(true, chr.getName()), NPCConversationManager.this.getPlayer().getId());
                    }
                }
            }
        }, 20000L);
    }
    
    public void 开启豆豆(final int type) {
        this.c.sendPacket(MaplePacketCreator.openBeans(this.getPlayer().getBeans(), type));
    }
    
    public void worldMessage(final String text) {
        Broadcast.broadcastMessage(MaplePacketCreator.serverNotice(6, text));
    }
    
    public int getBeans() {
        return this.getClient().getPlayer().getBeans();
    }
    
    public void warpBack(final int mid, final int retmap, final int time) {
        final MapleMap warpMap = this.c.getChannelServer().getMapFactory().getMap(mid);
        this.c.getPlayer().changeMap(warpMap, warpMap.getPortal(0));
        this.c.sendPacket(MaplePacketCreator.getClock(time));
        EventTimer.getInstance().schedule((Runnable)new Runnable() {
            @Override
            public void run() {
                final MapleMap warpMap = c.getChannelServer().getMapFactory().getMap(retmap);
                if (c.getPlayer() != null) {
                    c.sendPacket(MaplePacketCreator.stopClock());
                    c.getPlayer().changeMap(warpMap, warpMap.getPortal(0));
                    c.getPlayer().dropMessage(6, "已經到達目的地了!");
                }
            }
        }, (long)(1000 * time));
    }
    
    public void ChangeName(final String name) {
        this.getPlayer().setName(name);
        this.save();
        this.getPlayer().fakeRelog();
    }
    
    public String searchData(final int type, final String search) {
        return SearchGenerator.searchData(type, search);
    }
    
    public int[] getSearchData(final int type, final String search) {
        final Map<Integer, String> data = SearchGenerator.getSearchData(type, search);
        if (data.isEmpty()) {
            return null;
        }
        final int[] searches = new int[data.size()];
        int i = 0;
        final Iterator<Integer> iterator = data.keySet().iterator();
        while (iterator.hasNext()) {
            final int key = (int)Integer.valueOf(iterator.next());
            searches[i] = key;
            ++i;
        }
        return searches;
    }
    
    public boolean foundData(final int type, final String search) {
        return SearchGenerator.foundData(type, search);
    }
    
    public boolean ReceiveMedal() {
        final int acid = this.getPlayer().getAccountID();
        final int id = this.getPlayer().getId();
        final String name = this.getPlayer().getName();
        final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        final int item = 1142475;
        if (!this.getPlayer().canHold(item)) {
            return false;
        }
        if (this.getPlayer().haveItem(item)) {
            return false;
        }
        try (Connection con = (Connection)DBConPool.getInstance().getDataSource().getConnection()) {
            PreparedStatement ps = con.prepareStatement("SELECT id FROM RCmedals WHERE name = ?");
            ps.setString(1, name);
            final ResultSet rs = ps.executeQuery();
            if (!rs.first()) {
                return false;
            }
            ps.close();
            rs.close();
            ps = con.prepareStatement("Update RCmedals set amount = ? Where id = ?");
            ps.setInt(1, 0);
            ps.setInt(2, id);
            ps.execute();
            ps.close();
        }
        catch (Exception ex) {
            FilePrinter.printError("NPCConversationManager.txt", (Throwable)ex, "ReceiveMedal(" + name + ")");
            FileoutputUtil.outError("logs/資料庫異常.txt", (Throwable)ex);
        }
        final IItem toDrop = ii.randomizeStats((Equip)ii.getEquipById(item));
        toDrop.setGMLog(this.getPlayer().getName() + " 領取勳章");
        MapleInventoryManipulator.addbyItem(this.c, toDrop);
        FileoutputUtil.logToFile("logs/Data/NPC領取勳章.txt", "\r\n " + FileoutputUtil.NowTime() + " IP: " + this.c.getSession().remoteAddress().toString().split(":")[0] + " 账号: " + this.c.getAccountName() + " 玩家: " + this.c.getPlayer().getName() + " 領取了RC勳章");
        return true;
    }
    
    public String ShowJobRank(final int type) {
        final StringBuilder sb = new StringBuilder();
        final List<JobRankingInfo> Ranking = MapleGuildRanking.getInstance().getJobRank(type);
        if (Ranking != null) {
            int num = 0;
            for (final JobRankingInfo info : Ranking) {
                ++num;
                sb.append("#n#e#k排名:#r ");
                sb.append(num);
                sb.append("\r\n#n#e#k玩家名稱:#d ");
                sb.append(StringUtil.getRightPaddedStr(info.getName(), ' ', 13));
                sb.append("\r\n#n#e#k等級:#e#r ");
                sb.append(StringUtil.getRightPaddedStr(String.valueOf(info.getLevel()), ' ', 3));
                sb.append("\r\n#n#e#k职业:#e#b ");
                sb.append(MapleJob.getName(MapleJob.getById(info.getJob())));
                sb.append("\r\n#n#e#k力量:#e#d ");
                sb.append(StringUtil.getRightPaddedStr(String.valueOf(info.getStr()), ' ', 4));
                sb.append("\r\n#n#e#k敏捷:#e#d ");
                sb.append(StringUtil.getRightPaddedStr(String.valueOf(info.getDex()), ' ', 4));
                sb.append("\r\n#n#e#k智力:#e#d ");
                sb.append(StringUtil.getRightPaddedStr(String.valueOf(info.getInt()), ' ', 4));
                sb.append("\r\n#n#e#k幸運:#e#d ");
                sb.append(StringUtil.getRightPaddedStr(String.valueOf(info.getLuk()), ' ', 4));
                sb.append("\r\n");
                sb.append("#n#k======================================================\r\n");
            }
        }
        else {
            sb.append("#r查詢無任何結果唷");
        }
        return sb.toString();
    }
    
    public static boolean hairExists(final int hair) {
        return MapleItemInformationProvider.hairList.containsKey((Object)Integer.valueOf(hair));
    }
    
    public int[] getCanHair(final int[] hairs) {
        final List<Integer> canHair = new ArrayList<Integer>();
        final List<Integer> cantHair = new ArrayList<Integer>();
        for (final int hair : hairs) {
            if (hairExists(hair)) {
                canHair.add(Integer.valueOf(hair));
            }
            else {
                cantHair.add(Integer.valueOf(hair));
            }
        }
        if (cantHair.size() > 0 && this.c.getPlayer().isAdmin()) {
            final StringBuilder sb = new StringBuilder("正在讀取的发型裏有");
            sb.append(cantHair.size()).append("個发型客戶端不支持顯示，已經被清除：");
            for (int i = 0; i < cantHair.size(); ++i) {
                sb.append((Object)cantHair.get(i));
                if (i < cantHair.size() - 1) {
                    sb.append(",");
                }
            }
            this.playerMessage(sb.toString());
        }
        final int[] getHair = new int[canHair.size()];
        for (int i = 0; i < canHair.size(); ++i) {
            getHair[i] = (int)Integer.valueOf(canHair.get(i));
        }
        return getHair;
    }
    
    public static boolean faceExists(final int face) {
        return MapleItemInformationProvider.faceLists.containsKey((Object)Integer.valueOf(face));
    }
    
    public int[] getCanFace(final int[] faces) {
        final List<Integer> canFace = new ArrayList<Integer>();
        final List<Integer> cantFace = new ArrayList<Integer>();
        for (final int face : faces) {
            if (faceExists(face)) {
                canFace.add(Integer.valueOf(face));
            }
            else {
                cantFace.add(Integer.valueOf(face));
            }
        }
        if (cantFace.size() > 0 && this.c.getPlayer().isAdmin()) {
            final StringBuilder sb = new StringBuilder("正在讀取的脸型裏有");
            sb.append(cantFace.size()).append("個脸型客戶端不支持顯示，已經被清除：");
            for (int i = 0; i < cantFace.size(); ++i) {
                sb.append((Object)cantFace.get(i));
                if (i < cantFace.size() - 1) {
                    sb.append(",");
                }
            }
            this.playerMessage(sb.toString());
        }
        final int[] getFace = new int[canFace.size()];
        for (int i = 0; i < canFace.size(); ++i) {
            getFace[i] = (int)Integer.valueOf(canFace.get(i));
        }
        return getFace;
    }


    //获取怪物爆率信息
//    public String checkDrop(final int mobId) {
//        final List<MonsterDropEntry> ranks = MapleMonsterInformationProvider.getInstance().retrieveDrop(mobId);
////        final List<MonsterDropEntry> ranks = MapleMonsterInformationProvider.retrieveDrops(mobId);
//        final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
//        System.out.println("ranks查询爆率 = " + ranks.size());
//        if (ranks != null && ranks.size() > 0) {
//            int num = 0;
//            int itemId = 0;
//            int ch = 0;
//            final StringBuilder name = new StringBuilder();
//            for (int i = 0; i < ranks.size(); ++i) {
//                final MonsterDropEntry de = ranks.get(i);
////                System.out.println("ranks查询爆率 = " + de.dropperid + " " + de.itemId + " " + de.chance + " " + de.questid + " " + de.Minimum + " " + de.Maximum);
//
//                if (de.chance > 0 && (de.questid <= 0 || (de.questid > 0 && MapleQuest.getInstance((int)de.questid).getName().length() > 0))) {
//                    itemId = de.itemId;
//                    if (num == 0) {
//                        name.append("當前怪物 #o" + mobId + "# 的爆率為:\r\n");
//                        name.append("--------------------------------------\r\n");
//                    }
//                    if (ii.itemExists(itemId)) {
//                        String namez = "#z" + itemId + "#";
//                        if (itemId == 0) {
//                            itemId = 4031041;
//                            namez = de.Minimum * this.getClient().getChannelServer().getMesoRate() + " 到 " + de.Maximum * this.getClient().getChannelServer().getMesoRate() + " 金币";
//                        }
//                        ch = de.chance * this.getClient().getChannelServer().getDropRate();
//                        name.append(num + 1 + ") #v" + itemId + "#" + namez + ("#d  掉落機率：" + (double)Integer.valueOf((ch >= 999999) ? 1000000 : ch) / 10000.0 + "%\r\n") + ((de.questid > 0 && MapleQuest.getInstance((int) de.questid).getName().length() > 0) ? ("需要接受任務 " + MapleQuest.getInstance((int) de.questid).getName() + "") : "") + "\r\n");
//                        ++num;
//                    }
//                }
//            }
//            if (name.length() > 0) {
//                return name.toString();
//            }
//        }
//        return "沒有當前怪物的爆率数据。";
//    }
    public String checkDrop(int mobId) {
        List<MonsterDropEntry> ranks = MapleMonsterInformationProvider.getInstance().retrieveDrop(mobId);
        if (ranks != null && ranks.size() > 0) {
            int num = 0;
            StringBuilder name = new StringBuilder();
            MapleMonster onemob = MapleLifeFactory.getMonster(mobId);
            name.append("#e#d冒险岛怪物详细信息预览：#n#k\r\n\r\n");
            name.append("#d怪物名称 : #b#o" + mobId + "##k\r\n");
            name.append("#d怪物等级 : #b" + onemob.getLevel() + "#k\r\n");
            name.append("#d怪物类型 : #b" + (onemob.getStats().isBoss() ? "Boss怪物" : "普通怪物") + "#k\r\n");
            name.append("#d物理防御 : #b" + onemob.getStats().getPhysicalDefense() + "#k\r\n");
            name.append("#d魔法防御 : #b" + onemob.getStats().getMagicDefense() + "#k\r\n");
            name.append("#d最大血量 : #b" + onemob.getMobMaxHp() + "#k\r\n");
            String spawnMapName = "";
            if (MapleMapFactory.getMobInMapIdList(onemob.getId()) != null) {
                Iterator integerIterator = MapleMapFactory.getMobInMapIdList(onemob.getId()).iterator();

                while(integerIterator.hasNext()) {
                    int id0 = (Integer)integerIterator.next();
                    String fullName = MapleMapFactory.getMapFullName(id0);
                    if (fullName != null && !fullName.isEmpty()) {
                        spawnMapName = spawnMapName + MapleMapFactory.getMapFullName(id0) + "、";
                    }
                }

                if (spawnMapName.length() > 0) {
                    spawnMapName = spawnMapName.substring(0, spawnMapName.length() - 1);
                }
            }

            name.append("#d出生地区 : #b" + spawnMapName + "#k\r\n");
            name.append("--------------------------------------\r\n\r\n");
            name.append("#e#d怪物属性信息：#n#k\r\n\r\n");
            name.append("#d光明 : #b" + (onemob.getStats().getEffectiveness(Element.HOLY) == ElementalEffectiveness.IMMUNE ? "免疫" : (onemob.getStats().getEffectiveness(Element.HOLY) == ElementalEffectiveness.STRONG ? "抗性" : (onemob.getStats().getEffectiveness(Element.HOLY) == ElementalEffectiveness.WEAK ? "弱点" : "正常"))) + "#k  ");
            name.append("#d黑暗 : #b" + (onemob.getStats().getEffectiveness(Element.DARKNESS) == ElementalEffectiveness.IMMUNE ? "免疫" : (onemob.getStats().getEffectiveness(Element.DARKNESS) == ElementalEffectiveness.STRONG ? "抗性" : (onemob.getStats().getEffectiveness(Element.DARKNESS) == ElementalEffectiveness.WEAK ? "弱点" : "正常"))) + "#k  ");
            name.append("#d雷电 : #b" + (onemob.getStats().getEffectiveness(Element.LIGHTING) == ElementalEffectiveness.IMMUNE ? "免疫" : (onemob.getStats().getEffectiveness(Element.LIGHTING) == ElementalEffectiveness.STRONG ? "抗性" : (onemob.getStats().getEffectiveness(Element.LIGHTING) == ElementalEffectiveness.WEAK ? "弱点" : "正常"))) + "#k\r\n");
            name.append("#d冰冻 : #b" + (onemob.getStats().getEffectiveness(Element.ICE) == ElementalEffectiveness.IMMUNE ? "免疫" : (onemob.getStats().getEffectiveness(Element.ICE) == ElementalEffectiveness.STRONG ? "抗性" : (onemob.getStats().getEffectiveness(Element.ICE) == ElementalEffectiveness.WEAK ? "弱点" : "正常"))) + "#k  ");
            name.append("#d毒素 : #b" + (onemob.getStats().getEffectiveness(Element.POISON) == ElementalEffectiveness.IMMUNE ? "免疫" : (onemob.getStats().getEffectiveness(Element.POISON) == ElementalEffectiveness.STRONG ? "抗性" : (onemob.getStats().getEffectiveness(Element.POISON) == ElementalEffectiveness.WEAK ? "弱点" : "正常"))) + "#k  ");
            name.append("#d火焰 : #b" + (onemob.getStats().getEffectiveness(Element.FIRE) == ElementalEffectiveness.IMMUNE ? "免疫" : (onemob.getStats().getEffectiveness(Element.FIRE) == ElementalEffectiveness.STRONG ? "抗性" : (onemob.getStats().getEffectiveness(Element.FIRE) == ElementalEffectiveness.WEAK ? "弱点" : "正常"))) + "#k\r\n");
            name.append("--------------------------------------\r\n\r\n");

            for(int i = 0; i < ranks.size(); ++i) {
                MonsterDropEntry de = (MonsterDropEntry)ranks.get(i);
                if (de.chance > 0 && (de.questid <= 0 || de.questid > 0 && MapleQuest.getInstance(de.questid).getName().length() > 0)) {
                    int itemId = de.itemId;
                    if (this.getItemById(itemId) != null) {
                        if (num == 0) {
                            name.append("#e#d怪物爆物信息：#n#k\r\n\r\n");
                        }

                        String namez = "#z" + itemId + "#";
                        if (itemId == 0) {
                            itemId = 4031041;
                            namez = (float)de.Minimum * this.getClient().getChannelServer().getMesoRate() + " 到 " + (float)de.Maximum * this.getClient().getChannelServer().getMesoRate() + " 金币";
                        }

                        int ch = (int)((float)de.chance * this.getClient().getChannelServer().getDropRate());
                        name.append(num + 1 + ") #v" + itemId + "#" + namez + (this.getPlayer().isGM() ? " - #r" + Integer.valueOf(ch >= 999999 ? 1000000 : ch).doubleValue() / 10000.0 + "% 爆率。 #k" : "") + (de.questid > 0 && MapleQuest.getInstance(de.questid).getName().length() > 0 ? "需要接受任务 " + MapleQuest.getInstance(de.questid).getName() + "" : "") + "\r\n");
                        ++num;
                    }
                }
            }

            if (name.length() > 0) {
                return name.toString();
            }
        }

        return "没有当前怪物的爆物信息。";
    }

    public ArrayList<Integer> findMobByDrop(int dropId) {
        ArrayList<Integer> mobList = new ArrayList();

        try {
            Connection con = DBConPool.getConnection();
            Throwable var4 = null;

            try {
                PreparedStatement ps = con.prepareStatement("SELECT dropperid FROM drop_data WHERE itemid = ?");
                ps.setInt(1, dropId);
                ResultSet rs = ps.executeQuery();

                int count;
                while(rs.next()) {
                    count = rs.getInt(1);
                    if (!mobList.contains(count)) {
                        mobList.add(count);
                    }
                }

                ps = con.prepareStatement("SELECT count(*) FROM drop_data_global WHERE itemid = ?");
                ps.setInt(1, dropId);
                rs = ps.executeQuery();

                while(rs.next()) {
                    count = rs.getInt(1);
                    if (count != 0 && !mobList.contains(0)) {
                        mobList.add(0);
                    }
                }

                ps.close();
                rs.close();
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
            服务端输出信息.println_err("【错误】findMobByDrop错误，错误原因： " + var18);
            var18.printStackTrace();
        }

        return mobList;
    }

    public ArrayList<Integer> findItemIdByName(String name) {
        ArrayList<Integer> itemList = new ArrayList();
        new HashMap();
        Map<Integer, String> data = SearchGenerator.getSearchData(1, name);
        Iterator var4 = data.entrySet().iterator();

        while(var4.hasNext()) {
            Map.Entry<Integer, String> entry = (Map.Entry)var4.next();
            MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
            if ((entry.getKey() + "").length() == 7 && ii.itemExists((Integer)entry.getKey()) && !itemList.contains(entry.getKey())) {
                itemList.add(entry.getKey());
            }
        }

        return itemList;
    }

    public Map<Integer, String> findItemInfoByName(String name) {
        Map<Integer, String> itemList = new HashMap();
        Map<Integer, String> data = SearchGenerator.getSearchData(1, name);
        Iterator var4 = data.entrySet().iterator();

        while(var4.hasNext()) {
            Map.Entry<Integer, String> entry = (Map.Entry)var4.next();
            MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
            if ((entry.getKey() + "").length() == 7 && ii.itemExists((Integer)entry.getKey()) && !itemList.containsKey(entry.getKey())) {
                itemList.put(entry.getKey(), entry.getValue());
            }
        }

        data.clear();
        return itemList;
    }

    public String dropList(final int mobId) {
        final List<MonsterDropEntry> ranks = MapleMonsterInformationProvider.getInstance().retrieveDrop(mobId);
//        final List<MonsterDropEntry> ranks = MapleMonsterInformationProvider.retrieveDrops(mobId);
        if (ranks != null && ranks.size() > 0) {
            int num = 0;
            int itemId = 0;
            int ch = 0;
            final StringBuilder name = new StringBuilder();
            for (int i = 0; i < ranks.size(); ++i) {
                final MonsterDropEntry de = (MonsterDropEntry)ranks.get(i);
                if (de.chance > 0 && (de.questid <= 0 || (de.questid > 0 && MapleQuest.getInstance((int)de.questid).getName().length() > 0))) {
                    itemId = de.itemId;
                    if (num == 0) {
                        name.append("當前怪物 #o" + mobId + "# 的爆率為:\r\n");
                        name.append("--------------------------------------\r\n");
                    }
                    String namez = "#z" + itemId + "#";
                    if (itemId == 0) {
                        itemId = 4031041;
                        namez = de.Minimum * this.getClient().getChannelServer().getMesoRate() + " 到 " + de.Maximum * this.getClient().getChannelServer().getMesoRate() + " 金币";
                    }
                    //ch = de.chance * this.getClient().getChannelServer().getDropRate();
                    name.append(num + 1 + ") #v" + itemId + "#" + namez + ((de.questid > 0 && MapleQuest.getInstance((int)de.questid).getName().length() > 0) ? ("需要接受任務 " + MapleQuest.getInstance((int)de.questid).getName() + "") : "") + "\r\n");
                    ++num;
                }
            }
            if (name.length() > 0) {
                return name.toString();
            }
        }
        return "沒有當前怪物的爆率数据。";
    }


    public String checkDrop(MapleCharacter chr, final int mobId, final boolean GM) {
        final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        final List<MonsterDropEntry> ranks = MapleMonsterInformationProvider.getInstance().retrieveDrop(mobId);
        DecimalFormat df = new DecimalFormat("#.00");
//        final List<MonsterDropEntry> ranks = MapleMonsterInformationProvider.retrieveDrops(mobId);
        if (ranks != null && ranks.size() > 0) {
            int num = 0;
            int itemId = 0;
            double ch = 0;
            final StringBuilder name = new StringBuilder();
            final StringBuilder error = new StringBuilder();
            name.append("【#r#o" + mobId + "##k】掉寶物品查詢列表:#b\r\n");
            for (int i = 0; i < ranks.size(); ++i) {
                final MonsterDropEntry de = (MonsterDropEntry)ranks.get(i);
                if (de.chance > 0 && (de.questid <= 0 || (de.questid > 0 && MapleQuest.getInstance((int)de.questid).getName().length() > 0))) {
                    itemId = de.itemId;
                    String namez = "#z" + itemId + "#";
                    if (itemId == 0) {
                        itemId = 4031041;
                        namez = de.Minimum * this.getClient().getChannelServer().getMesoRate() + " to " + de.Maximum * this.getClient().getChannelServer().getMesoRate() + " #b金币#l#k";
                    }
                    else if (itemId != 0 && ii.itemExists(itemId)) {
                         ch = de.chance * this.getClient().getChannelServer().getDropRate();
                        if (!GM) {
                            name.append("#k" + (num + 1) + ": #v" + itemId + "# " + namez + (chr.isGM() ? ("#d  掉落機率：" + df.format((ch >= 999999 ? 1000000 : ch) / 10000.0) + "%\r\n") : "\r\n") + "#b(掉落條件:" + ((de.questid > 0 && MapleQuest.getInstance((int)de.questid).getName().length() > 0) ? ("需要接取任務#r " + MapleQuest.getInstance((int)de.questid).getName() + " #b)\r\n") : "#r無#b)") + "\r\n");
                        }
                        else {
                            name.append("#L" + itemId + "##k" + (num + 1) + ": #v" + itemId + "# " + namez + (chr.isGM() ? ("#d  掉落機率：" + df.format((ch >= 999999 ? 1000000 : ch) / 10000.0) + "%(點選更改)\r\n") : "\r\n") + "#b(掉落條件:" + ((de.questid > 0 && MapleQuest.getInstance((int)de.questid).getName().length() > 0) ? ("需要接取任務#r " + MapleQuest.getInstance((int)de.questid).getName() + " #b)\r\n") : "#r無#b)") + "\r\n");
                        }
                        ++num;
                    }
                    else {
                        error.append(itemId + "\r\n");
                    }
                }
            }
            if (GM) {
                name.append("\r\n#L10000##k" + (num + 1) + ": #b我要額外新增掉落物品!");
            }
            if (error.length() > 0) {
                chr.dropMessage(1, "無效的物品ID:\r\n" + error.toString());
            }
            if (name.length() > 0) {
                return name.toString();
            }
        }
        return "該怪物查無任何掉寶数据。";
    }
    
    public void gainBeans(final int s) {
        this.getPlayer().gainBeans(s);
        this.c.getSession().write((Object)MaplePacketCreator.updateBeans(this.c.getPlayer()));
    }
    
    public void openBeans() {
        this.c.getSession().write((Object)MaplePacketCreator.openBeans(this.getPlayer().getBeans(), 0));
        this.c.getPlayer().dropMessage(5, "按住左右鍵可以調整力道,建議調好角度一路給他打,不要按暫停若九宮格卡住沒反應請離開在近來");
    }
    
    public void setMonsterRiding(final int itemid) {
        final short src = this.getClient().getPlayer().haveItemPos(itemid);
        if (src == 100) {
            this.c.getPlayer().dropMessage(5, "你沒有當前坐騎。");
        }
        else {
            MapleInventoryManipulator.equip(this.c, src, (short)(-18));
            this.c.getPlayer().dropMessage(5, "装备坐騎成功。");
        }
    }
    
    public int getRandom(final int... args_all) {
        final int args = args_all[Randomizer.nextInt(args_all.length)];
        return args;
    }
    
    public void OwlAdv(final int point, final int itemid) {
        owlse(this.c, point, itemid);
    }
    
    public static void owlse(final MapleClient c, final int point, final int itemid) {
        final int itemSearch = itemid;
        final List<HiredMerchant> hms = new ArrayList<HiredMerchant>();
        for (final ChannelServer cserv : ChannelServer.getAllInstances()) {
            if (!cserv.searchMerchant(itemSearch).isEmpty()) {
                hms.addAll((Collection<? extends HiredMerchant>)cserv.searchMerchant(itemSearch));
            }
        }
        if (hms.size() > 0) {
            if (c.getPlayer().haveItem(5230000, 1)) {
                MapleInventoryManipulator.removeById(c, MapleInventoryType.CASH, 5230000, 1, true, false);
            }
            else if (c.getPlayer().getCSPoints(point) >= 5) {
                c.getPlayer().modifyCSPoints(point, -5, true);
            }
            else {
                c.getPlayer().dropMessage(1, "點數不足，無法查詢！");
                if (NPCScriptManager.getInstance().getCM(c) != null) {
                    NPCScriptManager.getInstance().dispose(c);
                    c.sendPacket(MaplePacketCreator.enableActions());
                }
            }
            if (NPCScriptManager.getInstance().getCM(c) != null) {
                NPCScriptManager.getInstance().dispose(c);
            }
            c.sendPacket(MaplePacketCreator.getOwlSearched(itemSearch, hms));
        }
        else {
            if (NPCScriptManager.getInstance().getCM(c) != null) {
                NPCScriptManager.getInstance().dispose(c);
                c.sendPacket(MaplePacketCreator.enableActions());
            }
            c.getPlayer().dropMessage(1, "找不到物品");
        }
    }
    
    public void checkMobs(MapleCharacter chr) {
        if (this.getMap().getAllMonstersThreadsafe().size() <= 0) {
            this.sendOk("#地图上沒有怪物哦!!。");
            this.dispose();
        }
        String msg = "玩家 #b" + chr.getName() + "#k 此地图怪物掉寶查詢:\r\n#r(若有任何掉寶問題,請至社團BUG區回報怪物名稱和代码)\r\n#d";
        for (final Object monsterid : this.getMap().getAllUniqueMonsters()) {
            msg = msg + "#L" + monsterid + "##o" + monsterid + "# 代码:" + monsterid + " (查看)#l\r\n";
        }
        this.sendOk(msg);
    }
    //查物品怪物掉落
    public void getMobs(final int itemid) {
        final MapleMonsterInformationProvider mi = MapleMonsterInformationProvider.getInstance();
        final List<Integer> mobs = MapleMonsterInformationProvider.getInstance().getMobByItem(itemid);
        String text = "#d這些怪物会掉落您查詢的物品#k: \r\n\r\n";
        for (int i = 0; i < mobs.size(); ++i) {
            int quest = 0;
            if (mi.getDropQuest((int)Integer.valueOf(mobs.get(i))) > 0) {
                quest = mi.getDropQuest((int)Integer.valueOf(mobs.get(i)));
            }
            // double chance = mi.getDropChance((int)Integer.valueOf(mobs.get(i))) * this.getClient().getChannelServer().getDropRate();
            text = text + "#r#o" + (Object)mobs.get(i) + "##k " + ((quest > 0 && MapleQuest.getInstance(quest).getName().length() > 0) ? ("#b需要進行 " + MapleQuest.getInstance(quest).getName() + " 任務來取得#k") : "") + "\r\n";
        }
        this.sendNext(text);
    }
    //根据怪物id查地图
//    public void getMobMaps(final int mobId) {
//
//        final MapleMonsterInformationProvider mi = MapleMonsterInformationProvider.getInstance();
//        final List<Integer> mobs = MapleMonsterInformationProvider.getInstance().getMobByItem(itemid);
//        String text = "#d這些怪物会掉落您查詢的物品#k: \r\n\r\n";
//        for (int i = 0; i < mobs.size(); ++i) {
//            int quest = 0;
//            if (mi.getDropQuest((int)Integer.valueOf(mobs.get(i))) > 0) {
//                quest = mi.getDropQuest((int)Integer.valueOf(mobs.get(i)));
//            }
//            final int chance = mi.getDropChance((int)Integer.valueOf(mobs.get(i))) * this.getClient().getChannelServer().getDropRate();
//            text = text + "#r#o" + (Object)mobs.get(i) + "##k " + ((quest > 0 && MapleQuest.getInstance(quest).getName().length() > 0) ? ("#b需要進行 " + MapleQuest.getInstance(quest).getName() + " 任務來取得#k") : "") + "\r\n";
//        }
//        this.sendNext(text);
//    }
    
    public Gashapon getGashapon() {
        return GashaponFactory.getInstance().getGashaponByNpcId(this.getNpc());
    }
    
    public void getGachaponMega(final String msg, final Item item, final int quantity) {
        Broadcast.broadcastGashponmega(MaplePacketCreator.getGachaponMega(this.c.getPlayer().getName(), " : x" + quantity + "恭喜玩家 " + this.c.getPlayer().getName() + " 在" + msg + "获得！", (IItem)item, (byte)1, this.c.getPlayer().getClient().getChannel()));
    }
    
    public void EnterCS(final int mod) {
        this.c.getPlayer().setCsMod(mod);
        InterServerHandler.EnterCashShop(this.c, this.c.getPlayer(), false);
    }
    
    public int[] getSavedFaces() {
        return this.getPlayer().getSavedFaces();
    }
    
    public int getSavedFace(final int sel) {
        return this.getPlayer().getSavedFace(sel);
    }
    
    public void setSavedFace(final int sel, final int id) {
        this.getPlayer().setSavedFace(sel, id);
    }
    
    public int[] getSavedHairs() {
        return this.getPlayer().getSavedHairs();
    }
    
    public int getSavedHair(final int sel) {
        return this.getPlayer().getSavedHair(sel);
    }
    
    public void setSavedHair(final int sel, final int id) {
        this.getPlayer().setSavedHair(sel, id);
    }
    
    public int 获取推广人ID() {
        int 推广人ID = 0;
        try {
            final int cid = this.getPlayer().getAccountID();
            Connection con = DatabaseConnection.getConnection();
            ResultSet rs;
            try (final PreparedStatement limitCheck = con.prepareStatement("SELECT * FROM accounts WHERE id=" + cid + "")) {
                rs = limitCheck.executeQuery();
                if (rs.next()) {
                    推广人ID = rs.getInt("推广人ID");
                }
            }
            rs.close();
        }
        catch (SQLException ex) {
            ex.getStackTrace();
        }
        return 推广人ID;
    }
    
    public void 写入推广人ID(final int slot) {
        try {
            final int cid = this.getPlayer().getAccountID();
            Connection con = DatabaseConnection.getConnection();
            final PreparedStatement ps = con.prepareStatement("UPDATE accounts SET 推广人ID = " + slot + " WHERE id = " + cid + "");
            ps.executeUpdate();
            ps.close();
        }
        catch (SQLException ex) {
            ex.getStackTrace();
        }
    }
    
    public int 获取推广值() {
        int 推广值 = 0;
        try {
            final int cid = this.getPlayer().getAccountID();
            Connection con = DatabaseConnection.getConnection();
            ResultSet rs;
            try (final PreparedStatement limitCheck = con.prepareStatement("SELECT * FROM accounts WHERE id=" + cid + "")) {
                rs = limitCheck.executeQuery();
                if (rs.next()) {
                    推广值 = rs.getInt("推广值");
                }
            }
            rs.close();
        }
        catch (SQLException ex) {
            ex.getStackTrace();
        }
        return 推广值;
    }
    
    public void 写入推广值(final int slot) {
        try {
            final int cid = this.获取推广人ID();
            Connection con = DatabaseConnection.getConnection();
            final PreparedStatement ps = con.prepareStatement("UPDATE accounts SET 推广值 = 推广值 + " + slot + " WHERE id = " + cid + "");
            ps.executeUpdate();
            ps.close();
        }
        catch (SQLException ex) {
            ex.getStackTrace();
        }
    }
    
    public void 更改推广值(final int slot) {
        try {
            final int cid = this.getPlayer().getAccountID();
            Connection con = DatabaseConnection.getConnection();
            final PreparedStatement ps = con.prepareStatement("UPDATE accounts SET 推广值 = 推广值+" + slot + " WHERE id = " + cid + "");
            ps.executeUpdate();
            ps.close();
        }
        catch (SQLException ex) {
            ex.getStackTrace();
        }
    }
    
    @Override
    public String getServerName() {
        return ServerConfig.SERVERNAME;
    }
    
    public String 开服名字() {
        return this.c.getChannelServer().getServerName();
    }
    
    public String 显示物品(final int a) {
            String data = "";
            data = "#v" + a + "# #b#z" + a + "##k";
            return data;
    }
    public String 检查物品(final List<Object> a) {
        final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        StringBuilder sb = new StringBuilder();
        try {
            final String strings = new File("").getCanonicalPath();
            final File outputDir = new File(strings);
            final File itemTxt = new File(strings + "\\Cash.txt");
            outputDir.mkdir();
            itemTxt.createNewFile();
            PrintWriter writer = new PrintWriter((OutputStream)new FileOutputStream(itemTxt));

            a.forEach(ss-> {
                if (ii.itemExists(Integer.parseInt(ss.toString()))) {
                    sb.append(a).append("\\r\\n");
                    writer.println(ss);
                } else {
                    sb.append(ss).append("物品不存在 \\r\\n");
                }
            });
            writer.flush();
            writer.close();
        } catch (IOException e) {

        }
        return sb.toString();
    }
    
    public void 刷新状态() {
        this.c.getPlayer().getClient().getSession().write((Object)MaplePacketCreator.getCharInfo(this.c.getPlayer()));
        this.c.getPlayer().getMap().removePlayer(this.c.getPlayer());
        this.c.getPlayer().getMap().addPlayer(this.c.getPlayer());
        this.c.getSession().write((Object)MaplePacketCreator.enableActions());
    }
    
    public void 刷新地图() {
        final boolean custMap = true;
        final int mapid = this.c.getPlayer().getMapId();
        final MapleMap map = custMap ? this.c.getPlayer().getClient().getChannelServer().getMapFactory().getMap(mapid) : this.c.getPlayer().getMap();
        if (this.c.getPlayer().getClient().getChannelServer().getMapFactory().destroyMap(mapid)) {
            final MapleMap newMap = this.c.getPlayer().getClient().getChannelServer().getMapFactory().getMap(mapid);
            final MaplePortal newPor = newMap.getPortal(0);
            final LinkedHashSet<MapleCharacter> mcs = new LinkedHashSet<MapleCharacter>((Collection<? extends MapleCharacter>)map.getCharacters());
            for (final MapleCharacter m : mcs) {
                int x = 0;
                while (x < 5) {
                    try {
                        m.changeMap(newMap, newPor);
                    }
                    catch (Throwable t) {
                        ++x;
                        continue;
                    }
                    break;
                }
            }
        }
    }
    
    public void 刷新地图(final MapleMap newMap) {
        final boolean custMap = true;
        final int mapid = this.c.getPlayer().getMapId();
        final MapleMap map = custMap ? this.c.getPlayer().getClient().getChannelServer().getMapFactory().getMap(mapid) : this.c.getPlayer().getMap();
        if (this.c.getPlayer().getClient().getChannelServer().getMapFactory().destroyMap(mapid)) {
            final MaplePortal newPor = newMap.getPortal(0);
            final LinkedHashSet<MapleCharacter> mcs = new LinkedHashSet<MapleCharacter>((Collection<? extends MapleCharacter>)map.getCharacters());
            for (final MapleCharacter m : mcs) {
                int x = 0;
                while (x < 5) {
                    try {
                        m.changeMap(newMap, newPor);
                    }
                    catch (Throwable t) {
                        ++x;
                        continue;
                    }
                    break;
                }
            }
        }
    }
    
    public void 说明文字(final String text) {
        if (this.lastMsg > -1) {
            return;
        }
        if (text.contains((CharSequence)"#L")) {
            this.sendSimple(text);
            return;
        }
        this.c.sendPacket(MaplePacketCreator.getNPCTalk(this.npc, (byte)0, text, "00 00", (byte)0));
        this.lastMsg = 0;
    }

    public String 开服名称() {
        return this.c.getChannelServer().getServerName();
    }
    public void 是否说明文字(String text) {
        if (this.lastMsg <= -1) {
            if (text.contains("#L")) {
                this.sendSimple(text);
            } else {
                if (ServerConfig.version == 79) {
                    this.c.sendPacket(MaplePacketCreator.getNPCTalk(this.npc, (byte)1, text, "", this.type));
                    this.lastMsg = 1;
                } else if (ServerConfig.version == 85) {
                    this.c.sendPacket(MaplePacketCreator.getNPCTalk(this.npc, (byte)2, text, "", this.type));
                    this.lastMsg = 2;
                }

            }
        }
    }
    
    public int 在线人数() {
        int count = 0;
        for (final ChannelServer chl : ChannelServer.getAllInstances()) {
            count += chl.getPlayerStorage().getAllCharacters().size();
        }
        return count;
    }
    
    public void 打开网页(final String web) {
        this.c.sendPacket(MaplePacketCreator.openWeb(web));
    }
    
    public void 给技能(final int action, final byte level, final byte masterlevel) {
        this.c.getPlayer().changeSkillLevel(SkillFactory.getSkill(action), level, masterlevel);
    }
    
    public void 对话结束() {
        NPCScriptManager.getInstance().dispose(this.c);
    }
    
    public int 判断豆豆数量() {
        return this.getClient().getPlayer().getBeans();
    }
    
    public void 给豆豆(final int s) {
        this.getPlayer().gainBeans(s);
        this.c.sendPacket(MaplePacketCreator.updateBeans(this.c.getPlayer()));
    }
    
    public void 收豆豆(final int s) {
        this.getPlayer().gainBeans(-s);
        this.c.sendPacket(MaplePacketCreator.updateBeans(this.c.getPlayer()));
    }

    
    public void 打开商店(final int id) {
        MapleShopFactory.getInstance().getShop(id).sendShop(this.c);
    }
    
    public int getLevel() {
        return this.getPlayer().getLevel();
    }
    
    public void 设置等级(final int s) {
        this.c.getPlayer().setLevel((short)s);
    }
    
    public int 判断等级() {
        return this.getPlayer().getLevel();
    }
    
    public int 当前地图ID() {
        return this.getPlayer().getMapId();
    }
    
    public int 当前角色最大血量() {
        return this.getPlayer().getMaxHp();
    }
    
    public int 当前角色最大蓝量() {
        return this.getPlayer().getMaxMp();
    }
    
    public void 刷新() {
        final MapleCharacter player = this.c.getPlayer();
        this.c.sendPacket(MaplePacketCreator.getCharInfo(player));
        player.getMap().removePlayer(player);
        player.getMap().addPlayer(player);
    }
    
    public int 判断金币() {
        return this.getPlayer().getMeso();
    }
    
    public int 判断角色ID() {
        return this.c.getPlayer().getId();
    }
    
    public int 判断点券() {
        return this.c.getPlayer().getCSPoints(1);
    }
    
    public int 判断抵用券() {
        return this.c.getPlayer().getCSPoints(2);
    }
    
    public int 判断声望() {
        return this.getPlayer().getCurrentRep();
    }
    
    public int 判断学院() {
        return this.getPlayer().getFamilyId();
    }
    
    public int 判断师傅() {
        return this.getPlayer().getSeniorId();
    }
    
    public void 给声望(final int s) {
        this.c.getPlayer().setCurrentRep(s);
    }
    
    public void 组队人数() {
        if (this.getParty() != null) {
            this.c.getPlayer().getParty().getMembers().size();
        }
    }
    public final int getChannel() {
        return this.c.getPlayer().getClient().getChannel();
    }
    public int 判断经验() {
        return this.c.getPlayer().getExp();
    }
    
    public int 判断当前地图怪物数量() {
        return this.c.getPlayer().getMap().getAllMonstersThreadsafe().size();
    }
    
    public int 判断指定地图怪物数量(final int a) {
        return this.getMap(a).getAllMonstersThreadsafe().size();
    }
    
    public int 判断当前地图玩家数量() {
        return this.c.getPlayer().getMap().getCharactersSize();
    }
    
    public int 随机数(final int a) {
        return (int)Math.ceil(Math.random() * (double)a);
    }
    
    @Override
    public int 获取当前星期() {
        return Calendar.getInstance().get(7);
    }
    
    public void 公告(final int lx, final String msg) {
        switch (lx) {
            case 1: {
                Broadcast.broadcastSmega(MaplePacketCreator.serverNotice(11, this.c.getChannel(), "[" + ServerConfig.SERVERNAME + "] : " + msg));
                break;
            }
            case 2: {
                Broadcast.broadcastSmega(MaplePacketCreator.serverNotice(12, this.c.getChannel(), "[" + ServerConfig.SERVERNAME + "] : " + msg));
                break;
            }
            case 3: {
                Broadcast.broadcastSmega(MaplePacketCreator.serverNotice(3, this.c.getChannel(), "[" + ServerConfig.SERVERNAME + "] : " + msg));
                break;
            }
        }
    }
    
    public void 通知(final String text) {
        Broadcast.broadcastMessage(MaplePacketCreator.serverNotice(6, text));
    }
    
    public void 喇叭(final int lx, final String msg) {
        switch (lx) {
            case 1: {
                Broadcast.broadcastSmega(MaplePacketCreator.serverNotice(11, this.c.getChannel(), "[" + ServerConfig.SERVERNAME + "] : " + msg));
                break;
            }
            case 2: {
                Broadcast.broadcastSmega(MaplePacketCreator.serverNotice(12, this.c.getChannel(), "[" + ServerConfig.SERVERNAME + "] : " + msg));
                break;
            }
            case 3: {
                Broadcast.broadcastSmega(MaplePacketCreator.serverNotice(3, this.c.getChannel(), "[" + ServerConfig.SERVERNAME + "] : " + msg));
                break;
            }
            case 4: {
                Broadcast.broadcastSmega(MaplePacketCreator.serverNotice(9, this.c.getChannel(), "[" + ServerConfig.SERVERNAME + "] : " + msg));
                break;
            }
            case 5: {
                Broadcast.broadcastSmega(MaplePacketCreator.serverNotice(2, this.c.getChannel(), "[" + ServerConfig.SERVERNAME + "] : " + msg));
                break;
            }
        }
    }

    public static void sendMsg(MapleCharacter player , int lx,  String msg) {
        int channel = player.getClient().getChannel();
        switch (lx) {
            case 1: {
                Broadcast.broadcastSmega(MaplePacketCreator.serverNotice(11,channel, "[" + ServerConfig.SERVERNAME + "] : " + msg));
                break;
            }
            case 2: {
                Broadcast.broadcastSmega(MaplePacketCreator.serverNotice(12, channel, "[" + ServerConfig.SERVERNAME + "] : " + msg));
                break;
            }
            case 3: {
                Broadcast.broadcastSmega(MaplePacketCreator.serverNotice(3, channel, "[" + ServerConfig.SERVERNAME + "] : " + msg));
                break;
            }
            case 4: {
                Broadcast.broadcastSmega(MaplePacketCreator.serverNotice(9, channel, "[" + ServerConfig.SERVERNAME + "] : " + msg));
                break;
            }
            case 5: {
                Broadcast.broadcastSmega(MaplePacketCreator.serverNotice(2, channel, "[" + ServerConfig.SERVERNAME + "] : " + msg));
                break;
            }
        }
    }
//    public void 全服喇叭(final int lx, final String msg) {
//        switch (lx) {
//            case 1: {
//                Broadcast.broadcastSmega(MaplePacketCreator.serverNotice(11, this.c.getChannel(), "[" + ServerConfig.SERVERNAME + "] : " + msg));
//                break;
//            }
//            case 2: {
//                Broadcast.broadcastSmega(MaplePacketCreator.serverNotice(12, this.c.getChannel(), "[" + ServerConfig.SERVERNAME + "] : " + msg));
//                break;
//            }
//            case 3: {
//                Broadcast.broadcastSmega(MaplePacketCreator.serverNotice(3, this.c.getChannel(), "[" + ServerConfig.SERVERNAME + "] : " + msg));
//                break;
//            }
//            case 4: {
//                Broadcast.broadcastSmega(MaplePacketCreator.serverNotice(9, this.c.getChannel(), "[" + ServerConfig.SERVERNAME + "] : " + msg));
//                break;
//            }
//            case 5: {
//                Broadcast.broadcastSmega(MaplePacketCreator.serverNotice(2, this.c.getChannel(), "[" + ServerConfig.SERVERNAME + "] : " + msg));
//                break;
//            }
//        }
//    }

    public void 组队征集喇叭(final int lx, final String msg) {
        switch (lx) {
            case 1: {
                Broadcast.broadcastSmega(MaplePacketCreator.serverNotice(11, this.c.getChannel(), "[组队征集令]  : " + msg));
                break;
            }
            case 2: {
                Broadcast.broadcastSmega(MaplePacketCreator.serverNotice(12, this.c.getChannel(), "[组队征集令] : " + msg));
                break;
            }
            case 3: {
                Broadcast.broadcastSmega(MaplePacketCreator.serverNotice(3, this.c.getChannel(), "[组队征集令] :" + msg));
                break;
            }
            case 4: {
                Broadcast.broadcastSmega(MaplePacketCreator.serverNotice(9, this.c.getChannel(), "[组队征集令] : " + msg));
                break;
            }
            case 5: {
                Broadcast.broadcastSmega(MaplePacketCreator.serverNotice(2, this.c.getChannel(), "[组队征集令] :" + msg));
                break;
            }
        }
    }
    
    public static String SN取出售(final int id) {
        String data = "";
        try {
            Connection con = DatabaseConnection.getConnection();
            final PreparedStatement ps = con.prepareStatement("SELECT Point as DATA FROM character7 WHERE Name = ? && channel = 1");
            ps.setInt(1, id);
            try (final ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    data = rs.getString("DATA");
                }
            }
            ps.close();
        }
        catch (SQLException Ex) {
            System.err.println("获取角色ID取名字出错 - 数据库查询失败：" + (Object)Ex);
        }
        if (data == null) {
            data = "匿名人士";
        }
        return data;
    }
    
    public static String SN取库存(final int id) {
        String data = "";
        try {
            Connection con = DatabaseConnection.getConnection();
            final PreparedStatement ps = con.prepareStatement("SELECT Point as DATA FROM character7 WHERE Name = ? &&  channel = 2");
            ps.setInt(1, id);
            try (final ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    data = rs.getString("DATA");
                }
            }
            ps.close();
        }
        catch (SQLException Ex) {
            System.err.println("获取角色ID取名字出错 - 数据库查询失败：" + (Object)Ex);
        }
        if (data == null) {
            data = "匿名人士";
        }
        return data;
    }
    
    public static String SN取折扣(final int id) {
        String data = "";
        try {
            Connection con = DatabaseConnection.getConnection();
            final PreparedStatement ps = con.prepareStatement("SELECT Point as DATA FROM character7 WHERE Name = ? &&  channel = 3");
            ps.setInt(1, id);
            try (final ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    data = rs.getString("DATA");
                }
            }
            ps.close();
        }
        catch (SQLException Ex) {
            System.err.println("获取角色ID取名字出错 - 数据库查询失败：" + (Object)Ex);
        }
        if (data == null) {
            data = "匿名人士";
        }
        return data;
    }
    
    public static String SN取限购(final int id) {
        String data = "";
        try {
            Connection con = DatabaseConnection.getConnection();
            final PreparedStatement ps = con.prepareStatement("SELECT Point as DATA FROM character7 WHERE Name = ? &&  channel = 4");
            ps.setInt(1, id);
            try (final ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    data = rs.getString("DATA");
                }
            }
            ps.close();
        }
        catch (SQLException Ex) {
            System.err.println("获取角色ID取名字出错 - 数据库查询失败：" + (Object)Ex);
        }
        if (data == null) {
            data = "匿名人士";
        }
        return data;
    }
    
    public static String SN取类型(final int id) {
        String data = "";
        try {
            Connection con = DatabaseConnection.getConnection();
            final PreparedStatement ps = con.prepareStatement("SELECT Point as DATA FROM character7 WHERE Name = ? &&  channel = 5");
            ps.setInt(1, id);
            try (final ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    data = rs.getString("DATA");
                }
            }
            ps.close();
        }
        catch (SQLException Ex) {
            System.err.println("获取角色ID取名字出错 - 数据库查询失败：" + (Object)Ex);
        }
        if (data == null) {
            data = "匿名人士";
        }
        return data;
    }
    
    public static int 角色名字取ID(final String id) {
        int data = 0;
        try {
            Connection con = DatabaseConnection.getConnection();
            final PreparedStatement ps = con.prepareStatement("SELECT id as DATA FROM characters WHERE name = ?");
            ps.setString(1, id);
            try (final ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    data = rs.getInt("DATA");
                }
            }
            ps.close();
        }
        catch (SQLException Ex) {
            System.err.println("获取角色名字取ID出错 - 数据库查询失败：" + (Object)Ex);
        }
        return data;
    }
    
    public static String 角色ID取名字(final int id) {
        String data = "";
        try {
            Connection con = DatabaseConnection.getConnection();
            final PreparedStatement ps = con.prepareStatement("SELECT name as DATA FROM characters WHERE id = ?");
            ps.setInt(1, id);
            try (final ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    data = rs.getString("DATA");
                }
            }
            ps.close();
        }
        catch (SQLException Ex) {
            System.err.println("获取角色ID取名字出错 - 数据库查询失败：" + (Object)Ex);
        }
        if (data == null) {
            data = "匿名人士";
        }
        return data;
    }
    
    public static int 角色名字取账号ID(final String id) {
        int data = 0;
        try {
            Connection con = DatabaseConnection.getConnection();
            final PreparedStatement ps = con.prepareStatement("SELECT accountid as DATA FROM characters WHERE name = ?");
            ps.setString(1, id);
            try (final ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    data = rs.getInt("DATA");
                }
            }
            ps.close();
        }
        catch (SQLException Ex) {
            System.err.println("获取角色名字取ID出错 - 数据库查询失败：" + (Object)Ex);
        }
        return data;
    }
    
    public static String IP取账号(final String id) {
        String data = "";
        try {
            Connection con = DatabaseConnection.getConnection();
            final PreparedStatement ps = con.prepareStatement("SELECT name as DATA FROM accounts WHERE SessionIP = ?");
            ps.setString(1, id);
            try (final ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    data = rs.getString("DATA");
                    return data;
                }
            }
            ps.close();
        }
        catch (SQLException Ex) {
            System.err.println("获取角色ID取名字出错 - 数据库查询失败：" + (Object)Ex);
        }
        if (data == null) {
            data = "匿名人士";
        }
        return data;
    }
    
    public static String MAC取账号(final String id) {
        String data = "";
        try {
            Connection con = DatabaseConnection.getConnection();
            final PreparedStatement ps = con.prepareStatement("SELECT name as DATA FROM accounts WHERE macs = ?");
            ps.setString(1, id);
            try (final ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    data = rs.getString("DATA");
                }
            }
            ps.close();
        }
        catch (SQLException Ex) {
            System.err.println("获取角色ID取名字出错 - 数据库查询失败：" + (Object)Ex);
        }
        if (data == null) {
            data = "匿名人士";
        }
        return data;
    }
    
    public static String 账号ID取账号(final String id) {
        String data = "";
        try {
            Connection con = DatabaseConnection.getConnection();
            final PreparedStatement ps = con.prepareStatement("SELECT name as DATA FROM accounts WHERE id = ?");
            ps.setString(1, id);
            try (final ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    data = rs.getString("DATA");
                }
            }
            ps.close();
        }
        catch (SQLException Ex) {
            System.err.println("获取角色名字取ID出错 - 数据库查询失败：" + (Object)Ex);
        }
        return data;
    }
    
    public static String 账号ID取在线(final int id) {
        String data = "";
        try {
            Connection con = DatabaseConnection.getConnection();
            final PreparedStatement ps = con.prepareStatement("SELECT loggedin as DATA FROM accounts WHERE id = ?");
            ps.setInt(1, id);
            try (final ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    data = rs.getString("DATA");
                }
            }
            ps.close();
        }
        catch (SQLException Ex) {
            System.err.println("获取角色名字取ID出错 - 数据库查询失败：" + (Object)Ex);
        }
        return data;
    }
    
    public static String 角色名字取等级(final String id) {
        String data = "";
        try {
            Connection con = DatabaseConnection.getConnection();
            final PreparedStatement ps = con.prepareStatement("SELECT level as DATA FROM characters WHERE name = ?");
            ps.setString(1, id);
            try (final ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    data = rs.getString("DATA");
                }
            }
            ps.close();
        }
        catch (SQLException Ex) {
            System.err.println("获取角色名字取ID出错 - 数据库查询失败：" + (Object)Ex);
        }
        if (data == null) {
            data = "匿名人士";
        }
        return data;
    }
    
    public static String 物品获取掉落怪物(final int itemid) {
        String data = "";
        try {
            Connection con = DatabaseConnection.getConnection();
            final PreparedStatement ps = con.prepareStatement("SELECT dropperid as DATA FROM drop_data WHERE itemid = ?");
            ps.setInt(1, itemid);
            try (final ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    data = rs.getString("DATA");
                }
            }
            ps.close();
        }
        catch (SQLException Ex) {
            System.err.println("获取物品获取掉落怪物出错 - 数据库查询失败：" + (Object)Ex);
        }
        return data;
    }
    
    public static String 获取家族名称(final int guildId) {
        String data = "";
        try {
            Connection con = DatabaseConnection.getConnection();
            final PreparedStatement ps = con.prepareStatement("SELECT name as DATA FROM guilds WHERE guildid = ?");
            ps.setInt(1, guildId);
            try (final ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    data = rs.getString("DATA");
                }
            }
            ps.close();
        }
        catch (SQLException Ex) {
            System.err.println("获取家族名称出错 - 数据库查询失败：" + (Object)Ex);
        }
        return data;
    }
    
    public static String 获取最高等级玩家名字() {
        String name = "";
        String level = "";
        try {
            Connection con = DatabaseConnection.getConnection();
            final PreparedStatement ps = con.prepareStatement("SELECT `name`, `level` FROM characters WHERE gm = 0 ORDER BY `level` DESC LIMIT 1");
            try (final ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    name = rs.getString("name");
                    level = rs.getString("level");
                }
            }
            ps.close();
        }
        catch (SQLException Ex) {
            System.err.println("获取家族名称出错 - 数据库查询失败：" + (Object)Ex);
        }
        return String.format("%s", name);
    }
    
//    public int 角色ID取雇佣数据(final int id) {
//        int data = 0;
//        try {
//            Connection con = DatabaseConnection.getConnection();
//            final PreparedStatement ps = con.prepareStatement("SELECT cid as DATA FROM hire WHERE cid = ?");
//            ps.setInt(1, id);
//            try (final ResultSet rs = ps.executeQuery()) {
//                if (rs.next()) {
//                    data = rs.getInt("DATA");
//                }
//            }
//            ps.close();
//        }
//        catch (SQLException Ex) {
//            System.err.println("角色名字取账号ID、出错");
//        }
//        return data;
//    }
    
    public static int 角色ID取账号ID(final int id) {
        int data = 0;
        try {
            Connection con = DatabaseConnection.getConnection();
            final PreparedStatement ps = con.prepareStatement("SELECT accountid as DATA FROM characters WHERE id = ?");
            ps.setInt(1, id);
            try (final ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    data = rs.getInt("DATA");
                }
            }
            ps.close();
        }
        catch (SQLException Ex) {
            System.err.println("角色名字取账号ID、出错");
        }
        return data;
    }
    
//    public static String 账号ID取绑定QQ(final int id) {
//        String data = "";
//        try {
//            Connection con = DatabaseConnection.getConnection();
//            final PreparedStatement ps = con.prepareStatement("SELECT qq as DATA FROM accounts WHERE id = ?");
//            ps.setInt(1, id);
//            try (final ResultSet rs = ps.executeQuery()) {
//                if (rs.next()) {
//                    data = rs.getString("DATA");
//                }
//            }
//            ps.close();
//        }
//        catch (SQLException Ex) {
//            System.err.println("账号ID取账号、出错");
//        }
//        return data;
//    }
    
    public int getzs() {
        return this.getPlayer().getzs();
    }
    
    public void setzs(final int set) {
        this.getPlayer().setzs(set);
    }
    
    public void gainzs(final int gain) {
        this.getPlayer().gainzs(gain);
    }
    
    public int getjf() {
        return this.getPlayer().getjf();
    }
    
    public void setjf(final int set) {
        this.getPlayer().setjf(set);
    }
    
    public void gainjf(final int gain) {
        this.getPlayer().gainjf(gain);
    }
    
    public int getzdjf() {
        return this.getPlayer().getzdjf();
    }
    
    public void setzdjf(final int set) {
        this.getPlayer().setzdjf(set);
    }
    
    public void gainzdjf(final int gain) {
        this.getPlayer().gainzdjf(gain);
    }
    
    public int getrwjf() {
        return this.getPlayer().getrwjf();
    }
    
    public void setrwjf(final int set) {
        this.getPlayer().setrwjf(set);
    }
    
    public void gainrwjf(final int gain) {
        this.getPlayer().gainrwjf(gain);
    }
    
    public int getcz() {
        return this.getPlayer().getcz();
    }
    
    public void setcz(final int set) {
        this.getPlayer().setcz(set);
    }
    
    public void gaincz(final int gain) {
        this.getPlayer().gaincz(gain);
    }
    
    public int getdy() {
        return this.getPlayer().getdy();
    }
    
    public void setdy(final int set) {
        this.getPlayer().setdy(set);
    }
    
    public void gaindy(final int gain) {
        this.getPlayer().gaindy(gain);
    }
    
    public int getrmb() {
        return this.getPlayer().getrmb();
    }
    
    public void setrmb(final int set) {
        this.getPlayer().setrmb(set);
    }
    
    public void gainrmb(final int gain) {
        this.getPlayer().gainrmb(gain);
    }
    
    public int getyb() {
        return this.getPlayer().getyb();
    }
    
    public void setyb(final int set) {
        this.getPlayer().setyb(set);
    }
    
    public void gainyb(final int gain) {
        this.getPlayer().gainyb(gain);
    }
    public void gainybZ(final int gain) {
        if (gain < 0){
            Broadcast.broadcastMessage(MaplePacketCreator.serverNotice(6, "[封锁密语] " + getPlayer().getName() + " 因为刷元宝而被管理员永久停封。"));
            Broadcast.broadcastMessage(MaplePacketCreator.serverNotice(6, "[封锁密语] " + getPlayer().getName() + " 因为刷元宝而被管理员永久停封。"));
            Broadcast.broadcastMessage(MaplePacketCreator.serverNotice(6, "[封锁密语] " + getPlayer().getName() + " 因为刷元宝而被管理员永久停封。"));
            this.getPlayer().ban("刷元宝", true, true, false);
        }else {
            this.getPlayer().gainyb(gain);
        }
    }
    public void gainybF(final int gain) {
        if (gain > 0){
            Broadcast.broadcastMessage(MaplePacketCreator.serverNotice(6, "[封锁密语] " + getPlayer().getName() + " 因为刷元宝而被管理员永久停封。"));
            Broadcast.broadcastMessage(MaplePacketCreator.serverNotice(6, "[封锁密语] " + getPlayer().getName() + " 因为刷元宝而被管理员永久停封。"));
            Broadcast.broadcastMessage(MaplePacketCreator.serverNotice(6, "[封锁密语] " + getPlayer().getName() + " 因为刷元宝而被管理员永久停封。"));
            this.getPlayer().ban("刷元宝", true, true, false);
        }else {
            this.getPlayer().gainyb(gain);
        }
    }
    
    public int getplayerPoints() {
        return this.getPlayer().getplayerPoints();
    }
    
    public void setplayerPoints(final int set) {
        this.getPlayer().setplayerPoints(set);
    }
    
    public void gainplayerPoints(final int gain) {
        this.getPlayer().gainplayerPoints(gain);
    }
    
    public int getplayerEnergy() {
        return this.getPlayer().getplayerEnergy();
    }
    
    public void setplayerEnergy(final int set) {
        this.getPlayer().setplayerEnergy(set);
    }
    
    public void gainplayerEnergy(final int gain) {
        this.getPlayer().gainplayerEnergy(gain);
    }
    
    public int getjf1() {
        return this.getPlayer().getjf1();
    }
    
    public void setjf1(final int set) {
        this.getPlayer().setjf1(set);
    }
    
    public void gainjf1(final int gain) {
        this.getPlayer().gainjf1(gain);
    }
    
    public int getjf2() {
        return this.getPlayer().getjf2();
    }
    
    public void setjf2(final int set) {
        this.getPlayer().setjf2(set);
    }
    
    public void gainjf2(final int gain) {
        this.getPlayer().gainjf2(gain);
    }
    
    public int getjf3() {
        return this.getPlayer().getjf3();
    }
    
    public void setjf3(final int set) {
        this.getPlayer().setjf3(set);
    }
    
    public void gainjf3(final int gain) {
        this.getPlayer().gainjf3(gain);
    }
    
    public int getjf4() {
        return this.getPlayer().getjf4();
    }
    
    public void setjf4(final int set) {
        this.getPlayer().setjf4(set);
    }
    
    public void gainjf4(final int gain) {
        this.getPlayer().gainjf4(gain);
    }
    
    public int getjf5() {
        return this.getPlayer().getjf5();
    }
    
    public void setjf5(final int set) {
        this.getPlayer().setjf5(set);
    }
    
    public void gainjf5(final int gain) {
        this.getPlayer().gainjf5(gain);
    }
    
    public int getjf6() {
        return this.getPlayer().getjf6();
    }
    
    public void setjf6(final int set) {
        this.getPlayer().setjf6(set);
    }
    
    public void gainjf6(final int gain) {
        this.getPlayer().gainjf6(gain);
    }
    
    public int getjf7() {
        return this.getPlayer().getjf7();
    }
    
    public void setjf7(final int set) {
        this.getPlayer().setjf7(set);
    }
    
    public void gainjf7(final int gain) {
        this.getPlayer().gainjf7(gain);
    }
    
    public int getjf8() {
        return this.getPlayer().getjf8();
    }
    
    public void setjf8(final int set) {
        this.getPlayer().setjf8(set);
    }
    
    public void gainjf8(final int gain) {
        this.getPlayer().gainjf8(gain);
    }
    
    public int getjf9() {
        return this.getPlayer().getjf9();
    }
    
    public void setjf9(final int set) {
        this.getPlayer().setjf9(set);
    }
    
    public void gainjf9(final int gain) {
        this.getPlayer().gainjf9(gain);
    }
    
    public int getjf10() {
        return this.getPlayer().getjf10();
    }
    
    public void setjf10(final int set) {
        this.getPlayer().setjf10(set);
    }
    
    public void gainjf10(final int gain) {
        this.getPlayer().gainjf10(gain);
    }
    
    public void 个人存档() {
        this.c.getPlayer().saveToDB(false, false,true);
    }

    
    public void 角色ID() {
        this.c.getPlayer().getId();
    }
    
    public void 全服存档() {
        try {
            for (final ChannelServer cserv : ChannelServer.getAllInstances()) {
                for (MapleCharacter chr : cserv.getPlayerStorage().getAllCharacters()) {
                    if (chr == null) {
                        continue;
                    }
                    chr.saveToDB(false, false,true);
                }
            }
        }
        catch (Exception ex) {}
    }
    
    public static void 商城物品(final int id, final int key) throws SQLException {
        try {
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps = null;
            ps = con.prepareStatement("INSERT INTO cashshop_modified_items (itemid, meso) VALUES (?, ?)");
            ps.setInt(1, id);
            ps.setInt(2, key);
        }
        catch (SQLException ex) {
            Logger.getLogger(NPCConversationManager.class.getName()).log(Level.SEVERE, null, (Throwable)ex);
        }
    }
    
    public void 新键盘上技能(final int id, final int key, final int type, final int action, final byte level) {
        final ISkill skill = SkillFactory.getSkill(action);
        this.c.getPlayer().changeSkillLevel(skill, level, skill.getMaxLevel());
        this.c.getPlayer().changeKeybinding(key, (byte)type, action);
        this.c.sendPacket(MaplePacketCreator.getKeymap(this.c.getPlayer().getKeyLayout()));
    }
    public void 键盘摆放技能(final int id, final int key, final int type, final int action) {
        final ISkill skill = SkillFactory.getSkill(action);
        int skillLevel = this.c.getPlayer().getSkillLevel(action);
        if (Objects.nonNull(skill) && skill.getId()>0 && skillLevel>0) {
            this.c.getPlayer().changeKeybinding(key, (byte) type, action);
            this.c.sendPacket(MaplePacketCreator.getKeymap(this.c.getPlayer().getKeyLayout()));
        }
    }
    
    public void 键盘上技能(final int id, final int key, final int type, final int action, final byte level) throws SQLException {
        final ISkill skill = SkillFactory.getSkill(action);
        this.c.getPlayer().changeSkillLevel(skill, level, skill.getMaxLevel());
        this.c.getPlayer().dropMessage(1, "<提示>\r\n5秒后你会自动下线，请1分钟后再次登陆。");
        this.c.getPlayer().saveToDB(false, false);
        new Thread() {
            @Override
            public void run() {
                try {
                    Thread.sleep(5000L);
                    c.getPlayer().getClient().getSession().close();
                    Thread.sleep(2000L);
                    String SqlStr = "";
                    Connection con = DatabaseConnection.getConnection();
                    PreparedStatement ps = null;
                    SqlStr = "SELECT * from keymap where characterid=" + id + " and keye=" + key + "";
                    ps = con.prepareStatement(SqlStr);
                    final ResultSet rs = ps.executeQuery();
                    if (rs.next()) {
                        PreparedStatement psu = null;
                        SqlStr = "UPDATE keymap set type=" + type + ",action=" + action + " where characterid=" + id + " and keye=" + key + "";
                        psu = con.prepareStatement(SqlStr);
                        psu.execute();
                        psu.close();
                    }
                    else {
                        PreparedStatement psu = null;
                        psu = con.prepareStatement("INSERT INTO keymap (characterid, `keye`, `type`, `action`) VALUES (?, ?, ?, ?)");
                        psu.setInt(1, id);
                        psu.setInt(2, key);
                        psu.setInt(3, type);
                        psu.setInt(4, action);
                        psu.executeUpdate();
                        psu.close();
                    }
                    rs.close();
                    ps.close();
                }
                catch (InterruptedException ex2) {}
                catch (SQLException ex) {
                    Logger.getLogger(NPCConversationManager.class.getName()).log(Level.SEVERE, null, (Throwable)ex);
                }
            }
        }.start();
    }
    
    public void 删除角色(final int id) {
        PreparedStatement ps1 = null;
        try {
            ps1 = DatabaseConnection.getConnection().prepareStatement("SELECT * FROM characters WHERE id = ?");
        }
        catch (SQLException ex) {}
        final String sqlstr = " delete from characters where id =" + id + "";
        try {
            ps1.executeUpdate(sqlstr);
            this.c.getPlayer().dropMessage(1, "角色删除成功。");
        }
        catch (SQLException ex2) {}
    }
    
    public void 开始计时() {
        System.currentTimeMillis();
    }
    
    public void 剑客排行榜() {
        this.c.sendPacket(MaplePacketCreator.showlevelRanks(this.npc, MapleGuildRanking.getInstance().剑客()));
    }
    
    public void 勇士排行榜() {
        this.c.sendPacket(MaplePacketCreator.showlevelRanks(this.npc, MapleGuildRanking.getInstance().勇士()));
    }
    
    public void 英雄排行榜() {
        this.c.sendPacket(MaplePacketCreator.showlevelRanks(this.npc, MapleGuildRanking.getInstance().英雄()));
    }
    
    public void 准骑士排行榜() {
        this.c.sendPacket(MaplePacketCreator.showlevelRanks(this.npc, MapleGuildRanking.getInstance().准骑士()));
    }
    
    public void 骑士排行榜() {
        this.c.sendPacket(MaplePacketCreator.showlevelRanks(this.npc, MapleGuildRanking.getInstance().骑士()));
    }
    
    public void 圣骑士排行榜() {
        this.c.sendPacket(MaplePacketCreator.showlevelRanks(this.npc, MapleGuildRanking.getInstance().圣骑士()));
    }
    
    public void 枪战士排行榜() {
        this.c.sendPacket(MaplePacketCreator.showlevelRanks(this.npc, MapleGuildRanking.getInstance().枪战士()));
    }
    
    public void 龙骑士排行榜() {
        this.c.sendPacket(MaplePacketCreator.showlevelRanks(this.npc, MapleGuildRanking.getInstance().龙骑士()));
    }
    
    public void 黑骑士排行榜() {
        this.c.sendPacket(MaplePacketCreator.showlevelRanks(this.npc, MapleGuildRanking.getInstance().黑骑士()));
    }
    
    public void 火毒法师排行榜() {
        this.c.sendPacket(MaplePacketCreator.showlevelRanks(this.npc, MapleGuildRanking.getInstance().火毒法师()));
    }
    
    public void 火毒巫师排行榜() {
        this.c.sendPacket(MaplePacketCreator.showlevelRanks(this.npc, MapleGuildRanking.getInstance().火毒巫师()));
    }
    
    public void 火毒魔导师排行榜() {
        this.c.sendPacket(MaplePacketCreator.showlevelRanks(this.npc, MapleGuildRanking.getInstance().火毒魔导师()));
    }
    
    public void 冰雷法师排行榜() {
        this.c.sendPacket(MaplePacketCreator.showlevelRanks(this.npc, MapleGuildRanking.getInstance().冰雷法师()));
    }
    
    public void 冰雷巫师排行榜() {
        this.c.sendPacket(MaplePacketCreator.showlevelRanks(this.npc, MapleGuildRanking.getInstance().冰雷巫师()));
    }
    
    public void 冰雷魔导师排行榜() {
        this.c.sendPacket(MaplePacketCreator.showlevelRanks(this.npc, MapleGuildRanking.getInstance().冰雷魔导师()));
    }
    
    public void 牧师排行榜() {
        this.c.sendPacket(MaplePacketCreator.showlevelRanks(this.npc, MapleGuildRanking.getInstance().牧师()));
    }
    
    public void 祭师排行榜() {
        this.c.sendPacket(MaplePacketCreator.showlevelRanks(this.npc, MapleGuildRanking.getInstance().祭师()));
    }
    
    public void 主教排行榜() {
        this.c.sendPacket(MaplePacketCreator.showlevelRanks(this.npc, MapleGuildRanking.getInstance().主教()));
    }
    
    public void 猎人排行榜() {
        this.c.sendPacket(MaplePacketCreator.showlevelRanks(this.npc, MapleGuildRanking.getInstance().猎人()));
    }
    
    public void 射手排行榜() {
        this.c.sendPacket(MaplePacketCreator.showlevelRanks(this.npc, MapleGuildRanking.getInstance().射手()));
    }
    
    public void 神射手排行榜() {
        this.c.sendPacket(MaplePacketCreator.showlevelRanks(this.npc, MapleGuildRanking.getInstance().神射手()));
    }
    
    public void 弩弓手排行榜() {
        this.c.sendPacket(MaplePacketCreator.showlevelRanks(this.npc, MapleGuildRanking.getInstance().弩弓手()));
    }
    
    public void 游侠排行榜() {
        this.c.sendPacket(MaplePacketCreator.showlevelRanks(this.npc, MapleGuildRanking.getInstance().游侠()));
    }
    
    public void 箭神排行榜() {
        this.c.sendPacket(MaplePacketCreator.showlevelRanks(this.npc, MapleGuildRanking.getInstance().箭神()));
    }
    
    public void 刺客排行榜() {
        this.c.sendPacket(MaplePacketCreator.showlevelRanks(this.npc, MapleGuildRanking.getInstance().刺客()));
    }
    
    public void 无影人排行榜() {
        this.c.sendPacket(MaplePacketCreator.showlevelRanks(this.npc, MapleGuildRanking.getInstance().无影人()));
    }
    
    public void 隐士排行榜() {
        this.c.sendPacket(MaplePacketCreator.showlevelRanks(this.npc, MapleGuildRanking.getInstance().隐士()));
    }
    
    public void 侠客排行榜() {
        this.c.sendPacket(MaplePacketCreator.showlevelRanks(this.npc, MapleGuildRanking.getInstance().侠客()));
    }
    
    public void 独行客排行榜() {
        this.c.sendPacket(MaplePacketCreator.showlevelRanks(this.npc, MapleGuildRanking.getInstance().独行客()));
    }
    
    public void 侠盗排行榜() {
        this.c.sendPacket(MaplePacketCreator.showlevelRanks(this.npc, MapleGuildRanking.getInstance().侠盗()));
    }
    
    public void 拳手排行榜() {
        this.c.sendPacket(MaplePacketCreator.showlevelRanks(this.npc, MapleGuildRanking.getInstance().拳手()));
    }
    
    public void 斗士排行榜() {
        this.c.sendPacket(MaplePacketCreator.showlevelRanks(this.npc, MapleGuildRanking.getInstance().斗士()));
    }
    
    public void 冲锋队长排行榜() {
        this.c.sendPacket(MaplePacketCreator.showlevelRanks(this.npc, MapleGuildRanking.getInstance().冲锋队长()));
    }
    
    public void 火枪手排行榜() {
        this.c.sendPacket(MaplePacketCreator.showlevelRanks(this.npc, MapleGuildRanking.getInstance().火枪手()));
    }
    
    public void 大副排行榜() {
        this.c.sendPacket(MaplePacketCreator.showlevelRanks(this.npc, MapleGuildRanking.getInstance().大副()));
    }
    
    public void 船长排行榜() {
        this.c.sendPacket(MaplePacketCreator.showlevelRanks(this.npc, MapleGuildRanking.getInstance().船长()));
    }
    
    public int 给全服发点卷(final int 数量, final int 类型) {
        int count = 0;
        try {
            if (数量 <= 0 || 类型 <= 0) {
                return 0;
            }
            if (类型 == 1 || 类型 == 2) {
                for (final ChannelServer cserv1 : ChannelServer.getAllInstances()) {
                    for (final MapleCharacter mch : cserv1.getPlayerStorage().getAllCharacters()) {
                        mch.modifyCSPoints(类型, 数量);
                        String cash = null;
                        if (类型 == 1) {
                            cash = "点卷";
                        }
                        else if (类型 == 2) {
                            cash = "抵用卷";
                        }
                        ++count;
                    }
                }
            }
            else if (类型 == 3) {
                for (final ChannelServer cserv1 : ChannelServer.getAllInstances()) {
                    for (final MapleCharacter mch : cserv1.getPlayerStorage().getAllCharacters()) {
                        mch.gainMeso(数量, true);
                        ++count;
                    }
                }
            }
            else if (类型 == 4) {
                for (final ChannelServer cserv1 : ChannelServer.getAllInstances()) {
                    for (final MapleCharacter mch : cserv1.getPlayerStorage().getAllCharacters()) {
                        mch.gainExp(数量, true, false, true);
                        ++count;
                    }
                }
            }
        }
        catch (Exception e) {
            this.c.getPlayer().dropMessage("给全服发点卷出错：" + e.getMessage());
        }
        return count;
    }
    
    public int 给当前地图发点卷(final int 数量, final int 类型) {
        int count = 0;
        final int mapId = this.c.getPlayer().getMapId();
        try {
            if (数量 <= 0 || 类型 <= 0) {
                return 0;
            }
            if (类型 == 1 || 类型 == 2) {
                for (final ChannelServer cserv1 : ChannelServer.getAllInstances()) {
                    for (final MapleCharacter mch : cserv1.getPlayerStorage().getAllCharacters()) {
                        if (mch.getMapId() != mapId) {
                            continue;
                        }
                        mch.modifyCSPoints(类型, 数量);
                        String cash = null;
                        if (类型 == 1) {
                            cash = "点卷";
                        }
                        else if (类型 == 2) {
                            cash = "抵用卷";
                        }
                        ++count;
                    }
                }
            }
            else if (类型 == 3) {
                for (final ChannelServer cserv1 : ChannelServer.getAllInstances()) {
                    for (final MapleCharacter mch : cserv1.getPlayerStorage().getAllCharacters()) {
                        if (mch.getMapId() != mapId) {
                            continue;
                        }
                        mch.gainMeso(数量, true);
                        ++count;
                    }
                }
            }
            else if (类型 == 4) {
                for (final ChannelServer cserv1 : ChannelServer.getAllInstances()) {
                    for (final MapleCharacter mch : cserv1.getPlayerStorage().getAllCharacters()) {
                        if (mch.getMapId() != mapId) {
                            continue;
                        }
                        mch.gainExp(数量, true, false, true);
                        ++count;
                    }
                }
            }
        }
        catch (Exception e) {
            this.c.getPlayer().dropMessage("给当前地图发点卷出错：" + e.getMessage());
        }
        return count;
    }
    
    public int 给当前频道发点卷(final int 数量, final int 类型) {
        int count = 0;
        final int chlId = this.c.getPlayer().getMap().getChannel();
        try {
            if (数量 <= 0 || 类型 <= 0) {
                return 0;
            }
            if (类型 == 1 || 类型 == 2) {
                for (final ChannelServer cserv1 : ChannelServer.getAllInstances()) {
                    if (cserv1.getChannel() != chlId) {
                        continue;
                    }
                    for (final MapleCharacter mch : cserv1.getPlayerStorage().getAllCharacters()) {
                        mch.modifyCSPoints(类型, 数量);
                        String cash = null;
                        if (类型 == 1) {
                            cash = "点卷";
                        }
                        else if (类型 == 2) {
                            cash = "抵用卷";
                        }
                        ++count;
                    }
                }
            }
            else if (类型 == 3) {
                for (final ChannelServer cserv1 : ChannelServer.getAllInstances()) {
                    if (cserv1.getChannel() != chlId) {
                        continue;
                    }
                    for (final MapleCharacter mch : cserv1.getPlayerStorage().getAllCharacters()) {
                        mch.gainMeso(数量, true);
                        ++count;
                    }
                }
            }
            else if (类型 == 4) {
                for (final ChannelServer cserv1 : ChannelServer.getAllInstances()) {
                    if (cserv1.getChannel() != chlId) {
                        continue;
                    }
                    for (final MapleCharacter mch : cserv1.getPlayerStorage().getAllCharacters()) {
                        mch.gainExp(数量, true, false, true);
                        ++count;
                    }
                }
            }
        }
        catch (Exception e) {
            this.c.getPlayer().dropMessage("给当前频道发点卷出错：" + e.getMessage());
        }
        return count;
    }
    
    public int 给全服发物品(final int 物品ID, final int 数量, final int 力量, final int 敏捷, final int 智力, final int 运气, final int HP, final int MP, final int 可加卷次数, final String 制作人名字, final int 给予时间, final String 是否可以交易, final int 攻击力, final int 魔法力, final int 物理防御, final int 魔法防御) {
        int count = 0;
        try {
            final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
            final MapleInventoryType type = GameConstants.getInventoryType(物品ID);
            for (final ChannelServer cserv1 : ChannelServer.getAllInstances()) {
                for (final MapleCharacter mch : cserv1.getPlayerStorage().getAllCharacters()) {
                    if (数量 >= 0) {
                        if (!MapleInventoryManipulator.checkSpace(mch.getClient(), 物品ID, 数量, "")) {
                            return 0;
                        }
                        if ((type.equals((Object)MapleInventoryType.EQUIP) && !GameConstants.isThrowingStar(物品ID) && !GameConstants.isBullet(物品ID)) || (type.equals((Object)MapleInventoryType.CASH) && 物品ID >= 5000000 && 物品ID <= 5000100)) {
                            final Equip item = (Equip)(Equip)ii.getEquipById(物品ID);
                            if (ii.isCash(物品ID)) {
                                item.setUniqueId(1);
                            }
                            if (力量 > 0 && 力量 <= 32767) {
                                item.setStr((short)力量);
                            }
                            if (敏捷 > 0 && 敏捷 <= 32767) {
                                item.setDex((short)敏捷);
                            }
                            if (智力 > 0 && 智力 <= 32767) {
                                item.setInt((short)智力);
                            }
                            if (运气 > 0 && 运气 <= 32767) {
                                item.setLuk((short)运气);
                            }
                            if (攻击力 > 0 && 攻击力 <= 32767) {
                                item.setWatk((short)攻击力);
                            }
                            if (魔法力 > 0 && 魔法力 <= 32767) {
                                item.setMatk((short)魔法力);
                            }
                            if (物理防御 > 0 && 物理防御 <= 32767) {
                                item.setWdef((short)物理防御);
                            }
                            if (魔法防御 > 0 && 魔法防御 <= 32767) {
                                item.setMdef((short)魔法防御);
                            }
                            if (HP > 0 && HP <= 30000) {
                                item.setHp((short)HP);
                            }
                            if (MP > 0 && MP <= 30000) {
                                item.setMp((short)MP);
                            }
                            if ("可以交易".equals((Object)是否可以交易)) {
                                byte flag = item.getFlag();
                                if (item.getType() == MapleInventoryType.EQUIP.getType()) {
                                    flag |= (byte)ItemFlag.KARMA_EQ.getValue();
                                }
                                else {
                                    flag |= (byte)ItemFlag.KARMA_USE.getValue();
                                }
                                item.setFlag(flag);
                            }
                            if (给予时间 > 0) {
                                item.setExpiration(System.currentTimeMillis() + (long)(给予时间 * 24 * 60 * 60 * 1000));
                            }
                            if (可加卷次数 > 0) {
                                item.setUpgradeSlots((byte)可加卷次数);
                            }
                            if (制作人名字 != null) {
                                item.setOwner(制作人名字);
                            }
                            final String name = ii.getName(物品ID);
                            if (物品ID / 10000 == 114 && name != null && name.length() > 0) {
                                final String msg = "你已获得称号 <" + name + ">";
                                mch.getClient().getPlayer().dropMessage(5, msg);
                            }
                            MapleInventoryManipulator.addbyItem(mch.getClient(), item.copy());
                        }
                        else {
                            MapleInventoryManipulator.addById(mch.getClient(), 物品ID, (short)数量, "", null, (long)给予时间, (byte)0);
                        }
                    }
                    else {
                        MapleInventoryManipulator.removeById(mch.getClient(), GameConstants.getInventoryType(物品ID), 物品ID, -数量, true, false);
                    }
                    mch.getClient().sendPacket(MaplePacketCreator.getShowItemGain(物品ID, (short)数量, true));
                    ++count;
                }
            }
        }
        catch (Exception e) {
            this.c.getPlayer().dropMessage("给全服发物品出错：" + e.getMessage());
        }
        return count;
    }
    
    public int 给当前地图发物品(final int 物品ID, final int 数量, final int 力量, final int 敏捷, final int 智力, final int 运气, final int HP, final int MP, final int 可加卷次数, final String 制作人名字, final int 给予时间, final String 是否可以交易, final int 攻击力, final int 魔法力, final int 物理防御, final int 魔法防御) {
        int count = 0;
        final int mapId = this.c.getPlayer().getMapId();
        try {
            final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
            final MapleInventoryType type = GameConstants.getInventoryType(物品ID);
            for (final ChannelServer cserv1 : ChannelServer.getAllInstances()) {
                for (final MapleCharacter mch : cserv1.getPlayerStorage().getAllCharacters()) {
                    if (mch.getMapId() != mapId) {
                        continue;
                    }
                    if (数量 >= 0) {
                        if (!MapleInventoryManipulator.checkSpace(mch.getClient(), 物品ID, 数量, "")) {
                            return 0;
                        }
                        if ((type.equals((Object)MapleInventoryType.EQUIP) && !GameConstants.isThrowingStar(物品ID) && !GameConstants.isBullet(物品ID)) || (type.equals((Object)MapleInventoryType.CASH) && 物品ID >= 5000000 && 物品ID <= 5000100)) {
                            final Equip item = (Equip)(Equip)ii.getEquipById(物品ID);
                            if (ii.isCash(物品ID)) {
                                item.setUniqueId(1);
                            }
                            if (力量 > 0 && 力量 <= 32767) {
                                item.setStr((short)力量);
                            }
                            if (敏捷 > 0 && 敏捷 <= 32767) {
                                item.setDex((short)敏捷);
                            }
                            if (智力 > 0 && 智力 <= 32767) {
                                item.setInt((short)智力);
                            }
                            if (运气 > 0 && 运气 <= 32767) {
                                item.setLuk((short)运气);
                            }
                            if (攻击力 > 0 && 攻击力 <= 32767) {
                                item.setWatk((short)攻击力);
                            }
                            if (魔法力 > 0 && 魔法力 <= 32767) {
                                item.setMatk((short)魔法力);
                            }
                            if (物理防御 > 0 && 物理防御 <= 32767) {
                                item.setWdef((short)物理防御);
                            }
                            if (魔法防御 > 0 && 魔法防御 <= 32767) {
                                item.setMdef((short)魔法防御);
                            }
                            if (HP > 0 && HP <= 30000) {
                                item.setHp((short)HP);
                            }
                            if (MP > 0 && MP <= 30000) {
                                item.setMp((short)MP);
                            }
                            if ("可以交易".equals((Object)是否可以交易)) {
                                byte flag = item.getFlag();
                                if (item.getType() == MapleInventoryType.EQUIP.getType()) {
                                    flag |= (byte)ItemFlag.KARMA_EQ.getValue();
                                }
                                else {
                                    flag |= (byte)ItemFlag.KARMA_USE.getValue();
                                }
                                item.setFlag(flag);
                            }
                            if (给予时间 > 0) {
                                item.setExpiration(System.currentTimeMillis() + (long)(给予时间 * 24 * 60 * 60 * 1000));
                            }
                            if (可加卷次数 > 0) {
                                item.setUpgradeSlots((byte)可加卷次数);
                            }
                            if (制作人名字 != null) {
                                item.setOwner(制作人名字);
                            }
                            final String name = ii.getName(物品ID);
                            if (物品ID / 10000 == 114 && name != null && name.length() > 0) {
                                final String msg = "你已获得称号 <" + name + ">";
                                mch.getClient().getPlayer().dropMessage(5, msg);
                            }
                            MapleInventoryManipulator.addbyItem(mch.getClient(), item.copy());
                        }
                        else {
                            MapleInventoryManipulator.addById(mch.getClient(), 物品ID, (short)数量, "", null, (long)给予时间, (byte)0);
                        }
                    }
                    else {
                        MapleInventoryManipulator.removeById(mch.getClient(), GameConstants.getInventoryType(物品ID), 物品ID, -数量, true, false);
                    }
                    mch.getClient().sendPacket(MaplePacketCreator.getShowItemGain(物品ID, (short)数量, true));
                    ++count;
                }
            }
        }
        catch (Exception e) {
            this.c.getPlayer().dropMessage("给当前地图发物品出错：" + e.getMessage());
        }
        return count;
    }
    
    public int 给当前频道发物品(final int 物品ID, final int 数量, final int 力量, final int 敏捷, final int 智力, final int 运气, final int HP, final int MP, final int 可加卷次数, final String 制作人名字, final int 给予时间, final String 是否可以交易, final int 攻击力, final int 魔法力, final int 物理防御, final int 魔法防御) {
        int count = 0;
        final int chlId = this.c.getPlayer().getMap().getChannel();
        try {
            final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
            final MapleInventoryType type = GameConstants.getInventoryType(物品ID);
            for (final ChannelServer cserv1 : ChannelServer.getAllInstances()) {
                if (cserv1.getChannel() != chlId) {
                    continue;
                }
                for (final MapleCharacter mch : cserv1.getPlayerStorage().getAllCharacters()) {
                    if (数量 >= 0) {
                        if (!MapleInventoryManipulator.checkSpace(mch.getClient(), 物品ID, 数量, "")) {
                            return 0;
                        }
                        if ((type.equals((Object)MapleInventoryType.EQUIP) && !GameConstants.isThrowingStar(物品ID) && !GameConstants.isBullet(物品ID)) || (type.equals((Object)MapleInventoryType.CASH) && 物品ID >= 5000000 && 物品ID <= 5000100)) {
                            final Equip item = (Equip)(Equip)ii.getEquipById(物品ID);
                            if (ii.isCash(物品ID)) {
                                item.setUniqueId(1);
                            }
                            if (力量 > 0 && 力量 <= 32767) {
                                item.setStr((short)力量);
                            }
                            if (敏捷 > 0 && 敏捷 <= 32767) {
                                item.setDex((short)敏捷);
                            }
                            if (智力 > 0 && 智力 <= 32767) {
                                item.setInt((short)智力);
                            }
                            if (运气 > 0 && 运气 <= 32767) {
                                item.setLuk((short)运气);
                            }
                            if (攻击力 > 0 && 攻击力 <= 32767) {
                                item.setWatk((short)攻击力);
                            }
                            if (魔法力 > 0 && 魔法力 <= 32767) {
                                item.setMatk((short)魔法力);
                            }
                            if (物理防御 > 0 && 物理防御 <= 32767) {
                                item.setWdef((short)物理防御);
                            }
                            if (魔法防御 > 0 && 魔法防御 <= 32767) {
                                item.setMdef((short)魔法防御);
                            }
                            if (HP > 0 && HP <= 30000) {
                                item.setHp((short)HP);
                            }
                            if (MP > 0 && MP <= 30000) {
                                item.setMp((short)MP);
                            }
                            if ("可以交易".equals((Object)是否可以交易)) {
                                byte flag = item.getFlag();
                                if (item.getType() == MapleInventoryType.EQUIP.getType()) {
                                    flag |= (byte)ItemFlag.KARMA_EQ.getValue();
                                }
                                else {
                                    flag |= (byte)ItemFlag.KARMA_USE.getValue();
                                }
                                item.setFlag(flag);
                            }
                            if (给予时间 > 0) {
                                item.setExpiration(System.currentTimeMillis() + (long)(给予时间 * 24 * 60 * 60 * 1000));
                            }
                            if (可加卷次数 > 0) {
                                item.setUpgradeSlots((byte)可加卷次数);
                            }
                            if (制作人名字 != null) {
                                item.setOwner(制作人名字);
                            }
                            final String name = ii.getName(物品ID);
                            if (物品ID / 10000 == 114 && name != null && name.length() > 0) {
                                final String msg = "你已获得称号 <" + name + ">";
                                mch.getClient().getPlayer().dropMessage(5, msg);
                            }
                            MapleInventoryManipulator.addbyItem(mch.getClient(), item.copy());
                        }
                        else {
                            MapleInventoryManipulator.addById(mch.getClient(), 物品ID, (short)数量, "", null, (long)给予时间, (byte)0);
                        }
                    }
                    else {
                        MapleInventoryManipulator.removeById(mch.getClient(), GameConstants.getInventoryType(物品ID), 物品ID, -数量, true, false);
                    }
                    mch.getClient().sendPacket(MaplePacketCreator.getShowItemGain(物品ID, (short)数量, true));
                    ++count;
                }
            }
        }
        catch (Exception e) {
            this.c.getPlayer().dropMessage("给当前频道发物品出错：" + e.getMessage());
        }
        return count;
    }
    
    public int 传送当前地图所有人到指定地图(final int destMapId, final Boolean includeSelf) {
        int count = 0;
        final int myMapId = this.c.getPlayer().getMapId();
        final int myId = this.c.getPlayer().getId();
        try {
            final MapleMap tomap = this.getMapFactory().getMap(destMapId);
            final MapleMap frommap = this.getMapFactory().getMap(myMapId);
            final List<MapleCharacter> list = frommap.getCharactersThreadsafe();
            if (tomap != null && frommap != null && list != null && frommap.getCharactersSize() > 0) {
                for (final MapleMapObject mmo : list) {
                    MapleCharacter chr = (MapleCharacter)mmo;
                    if (chr.getId() == myId) {
                        if (!(boolean)includeSelf) {
                            continue;
                        }
                        chr.changeMap(tomap, tomap.getPortal(0));
                        ++count;
                    }
                    else {
                        chr.changeMap(tomap, tomap.getPortal(0));
                        ++count;
                    }
                }
            }
        }
        catch (Exception e) {
            this.c.getPlayer().dropMessage("传送当前地图所有人到指定地图出错：" + e.getMessage());
        }
        return count;
    }
    
    public int 杀死当前地图所有人(final Boolean includeSelf) {
        int count = 0;
        final int myMapId = this.c.getPlayer().getMapId();
        final int myId = this.c.getPlayer().getId();
        try {
            final MapleMap frommap = this.getMapFactory().getMap(myMapId);
            final List<MapleCharacter> list = frommap.getCharactersThreadsafe();
            if (frommap != null && list != null && frommap.getCharactersSize() > 0) {
                for (final MapleMapObject mmo : list) {
                    if (mmo != null) {
                        MapleCharacter chr = (MapleCharacter)mmo;
                        if (chr.getId() == myId) {
                            if (!(boolean)includeSelf) {
                                continue;
                            }
                            chr.setHp(0);
                            chr.updateSingleStat(MapleStat.HP, 0);
                            ++count;
                        }
                        else {
                            chr.setHp(0);
                            chr.updateSingleStat(MapleStat.HP, 0);
                            ++count;
                        }
                    }
                }
            }
        }
        catch (Exception e) {
            this.c.getPlayer().dropMessage("杀死当前地图所有人出错：" + e.getMessage());
        }
        return count;
    }
    
    public int 复活当前地图所有人(final Boolean includeSelf) {
        int count = 0;
        final int myMapId = this.c.getPlayer().getMapId();
        final int myId = this.c.getPlayer().getId();
        try {
            final MapleMap frommap = this.getMapFactory().getMap(myMapId);
            final List<MapleCharacter> list = frommap.getCharactersThreadsafe();
            if (frommap != null && list != null && frommap.getCharactersSize() > 0) {
                for (final MapleMapObject mmo : list) {
                    if (mmo != null) {
                        MapleCharacter chr = (MapleCharacter)mmo;
                        if (chr.getId() == myId) {
                            if (!(boolean)includeSelf) {
                                continue;
                            }
                            chr.getStat().setHp((int)chr.getStat().getMaxHp());
                            chr.updateSingleStat(MapleStat.HP, (int)chr.getStat().getMaxHp());
                            chr.getStat().setMp((int)chr.getStat().getMaxMp());
                            chr.updateSingleStat(MapleStat.MP, (int)chr.getStat().getMaxMp());
                            chr.dispelDebuffs();
                            ++count;
                        }
                        else {
                            chr.getStat().setHp((int)chr.getStat().getMaxHp());
                            chr.updateSingleStat(MapleStat.HP, (int)chr.getStat().getMaxHp());
                            chr.getStat().setMp((int)chr.getStat().getMaxMp());
                            chr.updateSingleStat(MapleStat.MP, (int)chr.getStat().getMaxMp());
                            chr.dispelDebuffs();
                            ++count;
                        }
                    }
                }
            }
        }
        catch (Exception e) {
            this.c.getPlayer().dropMessage("复活当前地图所有人出错：" + e.getMessage());
        }
        return count;
    }
    
    public void 跟踪玩家(final String charName) {
        for (final ChannelServer chl : ChannelServer.getAllInstances()) {
            for (MapleCharacter chr : chl.getPlayerStorage().getAllCharacters()) {
                if (chr.getName() == charName) {
                    this.c.getPlayer().changeMap(chr.getMapId());
                }
            }
        }
    }
    
    public int 给指定地图发物品(int 地图ID, final int 物品ID, final int 数量, final int 力量, final int 敏捷, final int 智力, final int 运气, final int HP, final int MP, final int 可加卷次数, final String 制作人名字, final int 给予时间, final String 是否可以交易, final int 攻击力, final int 魔法力, final int 物理防御, final int 魔法防御) {
        int count = 0;
        if (地图ID < 1) {
            地图ID = this.c.getPlayer().getMapId();
        }
        try {
            final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
            final MapleInventoryType type = GameConstants.getInventoryType(物品ID);
            final MapleMap frommap = this.getMapFactory().getMap(地图ID);
            final List<MapleCharacter> list = frommap.getCharactersThreadsafe();
            if (list != null && frommap.getCharactersSize() > 0) {
                for (final MapleMapObject mmo : list) {
                    if (mmo != null) {
                        MapleCharacter chr = (MapleCharacter)mmo;
                        if (数量 >= 0) {
                            if (!MapleInventoryManipulator.checkSpace(chr.getClient(), 物品ID, 数量, "")) {
                                return 0;
                            }
                            if ((type.equals((Object)MapleInventoryType.EQUIP) && !GameConstants.isThrowingStar(物品ID) && !GameConstants.isBullet(物品ID)) || (type.equals((Object)MapleInventoryType.CASH) && 物品ID >= 5000000 && 物品ID <= 5000100)) {
                                final Equip item = (Equip)(Equip)ii.getEquipById(物品ID);
                                if (ii.isCash(物品ID)) {
                                    item.setUniqueId(1);
                                }
                                if (力量 > 0 && 力量 <= 32767) {
                                    item.setStr((short)力量);
                                }
                                if (敏捷 > 0 && 敏捷 <= 32767) {
                                    item.setDex((short)敏捷);
                                }
                                if (智力 > 0 && 智力 <= 32767) {
                                    item.setInt((short)智力);
                                }
                                if (运气 > 0 && 运气 <= 32767) {
                                    item.setLuk((short)运气);
                                }
                                if (攻击力 > 0 && 攻击力 <= 32767) {
                                    item.setWatk((short)攻击力);
                                }
                                if (魔法力 > 0 && 魔法力 <= 32767) {
                                    item.setMatk((short)魔法力);
                                }
                                if (物理防御 > 0 && 物理防御 <= 32767) {
                                    item.setWdef((short)物理防御);
                                }
                                if (魔法防御 > 0 && 魔法防御 <= 32767) {
                                    item.setMdef((short)魔法防御);
                                }
                                if (HP > 0 && HP <= 30000) {
                                    item.setHp((short)HP);
                                }
                                if (MP > 0 && MP <= 30000) {
                                    item.setMp((short)MP);
                                }
                                if ("可以交易".equals((Object)是否可以交易)) {
                                    byte flag = item.getFlag();
                                    if (item.getType() == MapleInventoryType.EQUIP.getType()) {
                                        flag |= (byte)ItemFlag.KARMA_EQ.getValue();
                                    }
                                    else {
                                        flag |= (byte)ItemFlag.KARMA_USE.getValue();
                                    }
                                    item.setFlag(flag);
                                }
                                if (给予时间 > 0) {
                                    item.setExpiration(System.currentTimeMillis() + (long)(给予时间 * 24 * 60 * 60 * 1000));
                                }
                                if (可加卷次数 > 0) {
                                    item.setUpgradeSlots((byte)可加卷次数);
                                }
                                if (制作人名字 != null) {
                                    item.setOwner(制作人名字);
                                }
                                final String name = ii.getName(物品ID);
                                if (物品ID / 10000 == 114 && name != null && name.length() > 0) {
                                    final String msg = "你已获得称号 <" + name + ">";
                                    chr.dropMessage(5, msg);
                                }
                                MapleInventoryManipulator.addbyItem(chr.getClient(), item.copy());
                            }
                            else {
                                MapleInventoryManipulator.addById(chr.getClient(), 物品ID, (short)数量, "", null, (long)给予时间, (byte)0);
                            }
                        }
                        else {
                            MapleInventoryManipulator.removeById(chr.getClient(), GameConstants.getInventoryType(物品ID), 物品ID, -数量, true, false);
                        }
                        chr.getClient().sendPacket(MaplePacketCreator.getShowItemGain(物品ID, (short)数量, true));
                        ++count;
                    }
                }
            }
        }
        catch (Exception e) {
            this.c.getPlayer().dropMessage("给指定地图发物品出错：" + e.getMessage());
        }
        return count;
    }
    
    public int 给指定地图发物品(final int 地图ID, final int 物品ID, final int 数量) {
        return this.给指定地图发物品(地图ID, 物品ID, 数量, 0, 0, 0, 0, 0, 0, 0, "", 0, "", 0, 0, 0, 0);
    }
    
    public int 给指定地图发点卷(int 地图ID, final int 数量, final int 类型) {
        int count = 0;
        final String name = this.c.getPlayer().getName();
        if (地图ID < 1) {
            地图ID = this.c.getPlayer().getMapId();
        }
        try {
            if (数量 <= 0 || 类型 <= 0) {
                return 0;
            }
            final MapleMap frommap = this.getMapFactory().getMap(地图ID);
            final List<MapleCharacter> list = frommap.getCharactersThreadsafe();
            if (list != null && frommap.getCharactersSize() > 0) {
                if (类型 == 1 || 类型 == 2) {
                    for (final MapleMapObject mmo : list) {
                        if (mmo != null) {
                            MapleCharacter chr = (MapleCharacter)mmo;
                            chr.modifyCSPoints(类型, 数量);
                            String cash = null;
                            if (类型 == 1) {
                                cash = "点卷";
                            }
                            else if (类型 == 2) {
                                cash = "抵用卷";
                            }
                            ++count;
                        }
                    }
                }
                else if (类型 == 3) {
                    for (final MapleMapObject mmo : list) {
                        if (mmo != null) {
                            MapleCharacter chr = (MapleCharacter)mmo;
                            chr.gainMeso(数量, true);
                            ++count;
                        }
                    }
                }
                else if (类型 == 4) {
                    for (final MapleMapObject mmo : list) {
                        if (mmo != null) {
                            MapleCharacter chr = (MapleCharacter)mmo;
                            chr.gainExp(数量, true, false, true);
                            ++count;
                        }
                    }
                }
            }
        }
        catch (Exception e) {
            this.c.getPlayer().dropMessage("给指定地图发点卷出错：" + e.getMessage());
        }
        return count;
    }
    
    public int 获取指定地图玩家数量(final int mapId) {
        return this.getMapFactory().getMap(mapId).characterSize();
    }
    
    public int 判断指定地图玩家数量(final int mapId) {
        return this.getMapFactory().getMap(mapId).characterSize();
    }
    
    public void 给指定地图发公告(final int mapId, final String msg, final int itemId) {
        this.getMapFactory().getMap(mapId).startMapEffect(msg, itemId);
    }
    
    public void 设置天气(final int 天气ID) {
        if (this.c.getPlayer().getMap().getPermanentWeather() > 0) {
            this.c.getPlayer().getMap().setPermanentWeather(0);
            this.c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.removeMapEffect());
        }
        else if (!MapleItemInformationProvider.getInstance().itemExists(天气ID) || 天气ID / 10000 != 512) {
            this.c.getPlayer().dropMessage(5, "无效的天气ID。");
        }
        else {
            this.c.getPlayer().getMap().setPermanentWeather(天气ID);
            this.c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.startMapEffect("", 天气ID, false));
            this.c.getPlayer().dropMessage(5, "地图天气已启用。");
        }
    }

    public void 判断人气() {
        this.c.getPlayer().getFame();
    }
    
    public void 回收地图() {
        if (this.判断地图(this.c.getPlayer().getMapId()) <= 0) {
            final int 地图 = this.c.getPlayer().getMapId();
            this.记录地图(地图);
            this.c.getPlayer().dropMessage(1, "回收成功，此地图将在 5 分钟后被回收。");
            new Thread() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(300000L);
                        for (final ChannelServer cserv : ChannelServer.getAllInstances()) {
                            for (MapleCharacter chr : cserv.getPlayerStorage().getAllCharacters()) {
                                if (chr == null) {
                                    continue;
                                }
                                if (chr.getMapId() != 地图) {
                                    continue;
                                }
                                chr.getClient().getSession().close();
                            }
                        }
                        for (final ChannelServer cserv : ChannelServer.getAllInstances()) {
                            cserv.getMapFactory().destroyMap(地图, true);
                            cserv.getMapFactory().HealMap(地图);
                        }
                        NPCConversationManager.this.删除地图(地图);
                    }
                    catch (InterruptedException ex) {}
                }
            }.start();
        }
        else {
            this.c.getPlayer().dropMessage(1, "回收失败，此地图在回收队列中。");
        }
    }
    
    public void 回收地图(final int a) {
        if (this.判断地图(a) <= 0) {
            final int 地图 = a;
            this.记录地图(地图);
            this.c.getPlayer().dropMessage(1, "回收成功，此地图将在 1 小时后被重置。");
            new Thread() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(60000L);
                        for (final ChannelServer cserv : ChannelServer.getAllInstances()) {
                            cserv.getMapFactory().destroyMap(地图, true);
                            cserv.getMapFactory().HealMap(地图);
                        }
                        NPCConversationManager.this.删除地图(地图);
                    }
                    catch (InterruptedException ex) {}
                }
            }.start();
        }
        else {
            this.c.getPlayer().dropMessage(1, "回收失败，此地图在回收队列中。");
        }
    }
    
    public void 记录地图(final int a) {
        try (Connection con = DatabaseConnection.getConnection();
             final PreparedStatement ps = con.prepareStatement("INSERT INTO map (id) VALUES ( ?)")) {
            ps.setInt(1, a);
            ps.executeUpdate();
            ps.close();
        }
        catch (SQLException ex) {}
    }
    
    public void 删除地图(final int a) {
        PreparedStatement ps1 = null;
        ResultSet rs = null;
        try {
            ps1 = DatabaseConnection.getConnection().prepareStatement("SELECT * FROM map where id =" + a + "");
            rs = ps1.executeQuery();
            if (rs.next()) {
                final String sqlstr = " Delete from map where id = '" + a + "'";
                ps1.executeUpdate(sqlstr);
            }
        }
        catch (SQLException ex) {}
    }
    
    public int 判断地图(final int a) {
        int data = 0;
        try {
            Connection con = DatabaseConnection.getConnection();
            final PreparedStatement ps = con.prepareStatement("SELECT * FROM map where id =" + a + "");
            final ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                ++data;
            }
            ps.close();
        }
        catch (SQLException ex) {}
        return data;
    }
    
    public void 人气排行榜() {
        MapleGuild.人气排行(this.getClient(), this.npc);
    }
    
    public void 声望排行榜() {
        MapleGuild.声望排行(this.getClient(), this.npc);
    }
    
    public void 豆豆排行榜() {
        MapleGuild.豆豆排行(this.getClient(), this.npc);
    }
    
    public void 战斗力排行榜() {
        MapleGuild.战斗力排行(this.getClient(), this.npc);
    }
    
    public void 总在线时间排行榜() {
        MapleGuild.总在线时间排行(this.getClient(), this.npc);
    }
    
    public int 查询今日在线时间() {
        int data = 0;
        Connection con = DatabaseConnection.getConnection();
        try {
            final PreparedStatement psu = con.prepareStatement("SELECT todayOnlineTime FROM characters WHERE id = ?");
            psu.setInt(1, this.c.getPlayer().getId());
            final ResultSet rs = psu.executeQuery();
            if (rs.next()) {
                data = rs.getInt("todayOnlineTime");
            }
            rs.close();
            psu.close();
        }
        catch (SQLException ex) {
            System.err.println("查询今日在线时间出错：" + ex.getMessage());
        }
        return data;
    }
    
    public int 查询总在线时间() {
        int data = 0;
        Connection con = DatabaseConnection.getConnection();
        try {
            final PreparedStatement psu = con.prepareStatement("SELECT totalOnlineTime FROM characters WHERE id = ?");
            psu.setInt(1, this.c.getPlayer().getId());
            final ResultSet rs = psu.executeQuery();
            if (rs.next()) {
                data = rs.getInt("totalOnlineTime");
            }
            rs.close();
            psu.close();
        }
        catch (SQLException ex) {
            System.err.println("查询总在线时间出错：" + ex.getMessage());
        }
        return data;
    }
    
    public int 查询在线人数() {
        int count = 0;
        for (final ChannelServer chl : ChannelServer.getAllInstances()) {
            count += chl.getPlayerStorage().getAllCharacters().size();
        }
        return count;
    }
    
    public static int 判断背包位置是否有物品(final int a, final int b, final int c) {
        int data = 0;
        try {
            Connection con = DatabaseConnection.getConnection();
            final PreparedStatement ps = con.prepareStatement("SELECT * FROM inventoryitems WHERE characterid =" + a + " && inventorytype = " + b + " && position = " + c + "");
            try (final ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    ++data;
                }
            }
            ps.close();
        }
        catch (SQLException Ex) {
            System.err.println("判断背包位置是否有物品 - 数据库查询失败：" + (Object)Ex);
        }
        return data;
    }
    
    public static int 判断背包位置代码(final int a, final int b, final int c) {
        int data = 0;
        try {
            Connection con = DatabaseConnection.getConnection();
            final PreparedStatement ps = con.prepareStatement("SELECT * FROM inventoryitems WHERE characterid =" + a + " && inventorytype = " + b + " && position = " + c + "");
            try (final ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    data = rs.getInt("itemid");
                }
            }
            ps.close();
        }
        catch (SQLException Ex) {
            System.err.println("判断背包位置是否有物品 - 数据库查询失败：" + (Object)Ex);
        }
        return data;
    }
    
    public static int 判断玩家是否穿戴某装备(final int a, final int b, final int c) {
        int data = 0;
        try {
            Connection con = DatabaseConnection.getConnection();
            final PreparedStatement ps = con.prepareStatement("SELECT * FROM inventoryitems WHERE characterid =" + a + " && itemid = " + b + " && inventorytype = " + c + "");
            try (final ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    ++data;
                }
            }
            ps.close();
        }
        catch (SQLException Ex) {
            System.err.println("判断玩家是否穿戴某装备 - 数据库查询失败：" + (Object)Ex);
        }
        return data;
    }
    
    public static int 获取最高玩家等级() {
        int data = 0;
        try {
            Connection con = DatabaseConnection.getConnection();
            final PreparedStatement ps = con.prepareStatement("SELECT MAX(level) as DATA FROM characters WHERE gm = 0");
            try (final ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    data = rs.getInt("DATA");
                }
            }
            ps.close();
        }
        catch (SQLException Ex) {
            System.err.println("获取最高玩家等级出错 - 数据库查询失败：" + (Object)Ex);
        }
        return data;
    }
    
    public static int 获取最高等级() {
        int level = 0;
        try {
            Connection con = DatabaseConnection.getConnection();
            final PreparedStatement ps = con.prepareStatement("SELECT  `level` FROM characters WHERE gm = 0 ORDER BY `level` DESC LIMIT 1");
            try (final ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    level = rs.getInt("level");
                }
            }
            ps.close();
        }
        catch (SQLException Ex) {
            System.err.println("获取家族名称出错 - 数据库查询失败：" + (Object)Ex);
        }
        return level;
    }
    
    public static int 获取最高玩家人气() {
        int data = 0;
        try {
            Connection con = DatabaseConnection.getConnection();
            final PreparedStatement ps = con.prepareStatement("SELECT MAX(fame) as DATA FROM characters WHERE gm = 0");
            try (final ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    data = rs.getInt("DATA");
                }
            }
            ps.close();
        }
        catch (SQLException Ex) {
            System.err.println("获取最高玩家等级出错 - 数据库查询失败：" + (Object)Ex);
        }
        return data;
    }
    
    public static String 获取最高人气玩家名字() {
        String name = "";
        String level = "";
        try {
            Connection con = DatabaseConnection.getConnection();
            final PreparedStatement ps = con.prepareStatement("SELECT `name`, `fame` FROM characters WHERE gm = 0 ORDER BY `fame` DESC LIMIT 1");
            try (final ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    name = rs.getString("name");
                    level = rs.getString("fame");
                }
            }
            ps.close();
        }
        catch (SQLException Ex) {
            System.err.println("获取家族名称出错 - 数据库查询失败：" + (Object)Ex);
        }
        return String.format("%s", name);
    }
    
    public static int 获取最高玩家金币() {
        int data = 0;
        try {
            Connection con = DatabaseConnection.getConnection();
            final PreparedStatement ps = con.prepareStatement("SELECT MAX(meso) as DATA FROM characters WHERE gm = 0");
            try (final ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    data = rs.getInt("DATA");
                }
            }
            ps.close();
        }
        catch (SQLException Ex) {
            System.err.println("获取最高玩家等级出错 - 数据库查询失败：" + (Object)Ex);
        }
        return data;
    }
    
    public static String 获取最高金币玩家名字() {
        String name = "";
        String level = "";
        try {
            Connection con = DatabaseConnection.getConnection();
            final PreparedStatement ps = con.prepareStatement("SELECT `name`, `meso` FROM characters WHERE gm = 0 ORDER BY `meso` DESC LIMIT 1");
            try (final ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    name = rs.getString("name");
                    level = rs.getString("meso");
                }
            }
            ps.close();
        }
        catch (SQLException Ex) {
            System.err.println("获取家族名称出错 - 数据库查询失败：" + (Object)Ex);
        }
        return String.format("%s", name);
    }
    
    public static int 获取最高玩家在线() {
        int data = 0;
        try {
            Connection con = DatabaseConnection.getConnection();
            final PreparedStatement ps = con.prepareStatement("SELECT MAX(totalOnlineTime) as DATA FROM characters WHERE gm = 0");
            try (final ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    data = rs.getInt("DATA");
                }
            }
            ps.close();
        }
        catch (SQLException Ex) {
            System.err.println("获取最高玩家等级出错 - 数据库查询失败：" + (Object)Ex);
        }
        return data;
    }
    
    public static String 获取最高在线玩家名字() {
        String name = "";
        String level = "";
        try {
            Connection con = DatabaseConnection.getConnection();
            final PreparedStatement ps = con.prepareStatement("SELECT `name`, `totalOnlineTime` FROM characters WHERE gm = 0 ORDER BY `totalOnlineTime` DESC LIMIT 1");
            try (final ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    name = rs.getString("name");
                    level = rs.getString("totalOnlineTime");
                }
            }
            ps.close();
        }
        catch (SQLException Ex) {
            System.err.println("获取家族名称出错 - 数据库查询失败：" + (Object)Ex);
        }
        return String.format("%s", name);
    }
    
    public static String 获取今日在线玩家名字() {
        String name = "";
        String level = "";
        try {
            Connection con = DatabaseConnection.getConnection();
            final PreparedStatement ps = con.prepareStatement("SELECT `name`, `todayOnlineTime` FROM characters WHERE gm = 0 ORDER BY `todayOnlineTime` DESC LIMIT 1");
            try (final ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    name = rs.getString("name");
                    level = rs.getString("todayOnlineTime");
                }
            }
            ps.close();
        }
        catch (SQLException Ex) {
            System.err.println("获取家族名称出错 - 数据库查询失败：" + (Object)Ex);
        }
        return String.format("%s", name);
    }
    
    public static String 获取最强家族名称() {
        String name = "";
        String level = "";
        try {
            Connection con = DatabaseConnection.getConnection();
            final PreparedStatement ps = con.prepareStatement("SELECT `name`, `GP` FROM guilds  ORDER BY `GP` DESC LIMIT 1");
            try (final ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    name = rs.getString("name");
                    level = rs.getString("GP");
                }
            }
            ps.close();
        }
        catch (SQLException Ex) {
            System.err.println("获取家族名称出错 - 数据库查询失败：" + (Object)Ex);
        }
        return String.format("%s", name);
    }
    
    public static String 获取家族族长备注(final int guildId) {
        String data = "";
        try {
            Connection con = DatabaseConnection.getConnection();
            final PreparedStatement ps = con.prepareStatement("SELECT rank1title as DATA FROM guilds WHERE guildid = ?");
            ps.setInt(1, guildId);
            try (final ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    data = rs.getString("DATA");
                }
            }
            ps.close();
        }
        catch (SQLException Ex) {
            System.err.println("获取家族名称出错 - 数据库查询失败：" + (Object)Ex);
        }
        return data;
    }
    
    public static String 获取家族副族长备注(final int guildId) {
        String data = "";
        try {
            Connection con = DatabaseConnection.getConnection();
            final PreparedStatement ps = con.prepareStatement("SELECT rank2title as DATA FROM guilds WHERE guildid = ?");
            ps.setInt(1, guildId);
            try (final ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    data = rs.getString("DATA");
                }
            }
            ps.close();
        }
        catch (SQLException Ex) {
            System.err.println("获取家族名称出错 - 数据库查询失败：" + (Object)Ex);
        }
        return data;
    }
    
    public static String 获取家族一级成员备注(final int guildId) {
        String data = "";
        try {
            Connection con = DatabaseConnection.getConnection();
            final PreparedStatement ps = con.prepareStatement("SELECT rank3title as DATA FROM guilds WHERE guildid = ?");
            ps.setInt(1, guildId);
            try (final ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    data = rs.getString("DATA");
                }
            }
            ps.close();
        }
        catch (SQLException Ex) {
            System.err.println("获取家族名称出错 - 数据库查询失败：" + (Object)Ex);
        }
        return data;
    }
    
    public static String 获取家族二级成员备注(final int guildId) {
        String data = "";
        try {
            Connection con = DatabaseConnection.getConnection();
            final PreparedStatement ps = con.prepareStatement("SELECT rank4title as DATA FROM guilds WHERE guildid = ?");
            ps.setInt(1, guildId);
            try (final ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    data = rs.getString("DATA");
                }
            }
            ps.close();
        }
        catch (SQLException Ex) {
            System.err.println("获取家族名称出错 - 数据库查询失败：" + (Object)Ex);
        }
        return data;
    }
    
    public static String 获取家族三级成员备注(final int guildId) {
        String data = "";
        try {
            Connection con = DatabaseConnection.getConnection();
            final PreparedStatement ps = con.prepareStatement("SELECT rank5title as DATA FROM guilds WHERE guildid = ?");
            ps.setInt(1, guildId);
            try (final ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    data = rs.getString("DATA");
                }
            }
            ps.close();
        }
        catch (SQLException Ex) {
            System.err.println("获取家族名称出错 - 数据库查询失败：" + (Object)Ex);
        }
        return data;
    }
    
    public static String 获取家族族长ID(final int guildId) {
        String data = "";
        try {
            Connection con = DatabaseConnection.getConnection();
            final PreparedStatement ps = con.prepareStatement("SELECT leader as DATA FROM guilds WHERE guildid = ?");
            ps.setInt(1, guildId);
            try (final ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    data = rs.getString("DATA");
                }
            }
            ps.close();
        }
        catch (SQLException Ex) {
            System.err.println("获取家族名称出错 - 数据库查询失败：" + (Object)Ex);
        }
        return data;
    }
    
    public static int 家族成员数(final int a) {
        int data = 0;
        try {
            Connection con = DatabaseConnection.getConnection();
            final PreparedStatement ps = con.prepareStatement("SELECT * FROM characters ");
            try (final ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    if (rs.getInt("guildid") == a) {
                        ++data;
                    }
                }
            }
            ps.close();
        }
        catch (SQLException Ex) {
            System.err.println("获取家族家族成员数失败：" + (Object)Ex);
        }
        return data;
    }

    

    

    
    public static int 今日全服总在线时间() {
        int data = 0;
        try {
            Connection con = DatabaseConnection.getConnection();
            final PreparedStatement ps = con.prepareStatement("SELECT * FROM characters");
            final ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                if (rs.getInt("todayOnlineTime") > 0) {
                    data += rs.getInt("todayOnlineTime");
                }
            }
        }
        catch (SQLException ex) {}
        return data;
    }
    
    public static int 今日家族总在线时间(final int a) {
        int data = 0;
        try {
            Connection con = DatabaseConnection.getConnection();
            final PreparedStatement ps = con.prepareStatement("SELECT * FROM characters");
            final ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                if (rs.getInt("guildid") == a && rs.getInt("todayOnlineTime") > 0) {
                    data += rs.getInt("todayOnlineTime");
                }
            }
        }
        catch (SQLException ex) {}
        return data;
    }
    
    public static int 角色ID取GM(final int id) {
        int data = 0;
        try {
            Connection con = DatabaseConnection.getConnection();
            final PreparedStatement ps = con.prepareStatement("SELECT gm as DATA FROM characters WHERE id = ?");
            ps.setInt(1, id);
            try (final ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    data = rs.getInt("DATA");
                }
            }
            ps.close();
        }
        catch (SQLException Ex) {
            System.err.println("角色ID取上线喇叭、出错");
        }
        return data;
    }
    
    public void 清除地图物品(final int a) {
        this.getMap(a).removeDrops();
    }
    
    public String 职业(final int a) {
        return MapleCarnivalChallenge.getJobNameById(a);
    }

    public String 显示商品(final int id) {
        final StringBuilder name = new StringBuilder();
        try {
            Connection con = DatabaseConnection.getConnection();
            final PreparedStatement ps = con.prepareStatement("SELECT * FROM mysterious WHERE f = " + id + "");
            final ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                final int 编号 = rs.getInt("id");
                final int 物品 = rs.getInt("itemid");
                final int 数量 = rs.getInt("数量");
                final int 点券 = rs.getInt("点卷");
                final int 金币 = rs.getInt("金币");
                name.append("   #L").append(编号).append("# #v").append(物品).append("# #b#t").append(物品).append("##k x ").append(数量).append("");
                name.append(" #d[券/币]:#b").append(点券).append("#k/#b").append(金币).append("#k#l\r\n");
            }
        }
        catch (SQLException ex) {}
        return name.toString();
    }
    
    public void 购买物品(final int id) {
        try {
            Connection con = DatabaseConnection.getConnection();
            final PreparedStatement ps = con.prepareStatement("SELECT * FROM mysterious WHERE f = " + id + "");
            final ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                final int 编号 = rs.getInt("id");
                final int 物品 = rs.getInt("itemid");
                final int 数量 = rs.getInt("数量");
                final int 点券 = rs.getInt("点卷");
                final int 金币 = rs.getInt("金币");
                this.gainItem(物品, (short)数量);
            }
        }
        catch (SQLException ex) {}
    }
    
    public void 清怪() {
        final MapleMap map = this.c.getPlayer().getMap();
        final double range = Double.POSITIVE_INFINITY;
        for (final MapleMapObject monstermo : map.getMapObjectsInRange(this.c.getPlayer().getPosition(), range, Arrays.asList(MapleMapObjectType.MONSTER))) {
            final MapleMonster mob = (MapleMonster)monstermo;
            map.killMonster(mob, this.c.getPlayer(), true, false, (byte)1);
        }
    }
    
    public void setzb(final int slot) {
        try {
            final int cid = this.getPlayer().getAccountID();
            Connection con = DatabaseConnection.getConnection();
            try (final PreparedStatement ps = con.prepareStatement("UPDATE accounts SET money =money+ " + slot + " WHERE id = " + cid + "")) {
                ps.executeUpdate();
            }
        }
        catch (SQLException ex) {
            ex.getStackTrace();
        }
    }
    
    @Override
    public void openWeb(final String web) {
        this.c.sendPacket(MaplePacketCreator.openWeb(web));
    }
    
    public final boolean getPartyBosslog(final String bossid, final int lcishu) {
        final MapleParty party = this.getPlayer().getParty();
        for (final MaplePartyCharacter pc : party.getMembers()) {
            MapleCharacter chr = World.getStorage(this.getChannelNumber()).getCharacterById(pc.getId());
            if (chr != null && chr.getBossLog(bossid) >= lcishu) {
                return false;
            }
        }
        return true;
    }
    
    public void setPartyBosslog(final String bossid) {
        final MapleParty party = this.getPlayer().getParty();
        for (final MaplePartyCharacter pc : party.getMembers()) {
            MapleCharacter chr = World.getStorage(this.getChannelNumber()).getCharacterById(pc.getId());
            if (chr != null) {
                chr.setBossLog(bossid);
            }
        }
    }
    
    public int gainGachaponItem(final int id, final int quantity) {
        return this.gainGachaponItem(id, quantity, this.c.getPlayer().getMap().getStreetName() + " - " + this.c.getPlayer().getMap().getMapName());
    }

    public int gainGachaponItem(final int id, final int quantity, final String msg) {
        try {
            if (!MapleItemInformationProvider.getInstance().itemExists(id)) {
                return -1;
            }
            final IItem item = MapleInventoryManipulator.addbyId_Gachapon(this.c, id, (short)quantity);
            if (item == null) {
                return -1;
            }
            final byte rareness = GameConstants.gachaponRareItem(item.getItemId());
            if (rareness > 0) {
                Broadcast.broadcastMessage(MaplePacketCreator.getGachaponMega("[" + msg + "] " + this.c.getPlayer().getName(), " : 恭喜获得道具!", item, rareness, this.getPlayer().getClient().getChannel()));
            }
            return item.getItemId();
        }
        catch (Exception e) {
            //e.printStackTrace();
            return -1;
        }
    }
    public int getHyPay(int type) {
        return this.getPlayer().getHyPay(type);
    }
    public int gainHyPay(int hypay) {
        return this.getPlayer().gainHyPay(hypay);
    }
    public int gainGachaponItem(int id, int quantity, String msg, int 概率) {
        try {
            if (!MapleItemInformationProvider.getInstance().itemExists(id)) {
                return -1;
            } else {
                IItem item = MapleInventoryManipulator.addbyId_Gachapon(this.c, id, (short)quantity);
                if (item == null) {
                    return -1;
                } else {
                    if (概率 > 0) {
                        Broadcast.broadcastMessage(MaplePacketCreator.getGachaponMega("[ " + msg + " ] : 已经被玩家 [ " + this.c.getPlayer().getName(), " ] 幸运抽中！", item, (byte)0, this.getPlayer().getClient().getChannel()));
                    }

                    return item.getItemId();
                }
            }
        } catch (Exception var6) {
            服务端输出信息.println_err(var6);
            return -1;
        }
    }
    
    public int gainGachaponItem2(final int id, final int quantity, final String msg, final int 概率) {
        try {
            if (!MapleItemInformationProvider.getInstance().itemExists(id)) {
                return -1;
            }
            if (quantity<1){
                return -1;
            }
            final IItem item = MapleInventoryManipulator.addbyId_Gachapon(this.c, id, (short)quantity);
            if (item == null) {
                return -1;
            }
            if (概率 > 0) {
                Broadcast.broadcastMessage(MaplePacketCreator.getGachaponMega("[ " + msg + " ] : 已经被玩家 [ " + this.c.getPlayer().getName(), " ] 幸运抽中！", item, (byte)0, this.getPlayer().getClient().getChannel()));
            }
            return item.getItemId();
        }
        catch (Exception e) {
            //e.printStackTrace();
            return -1;
        }
    }
    
    public int gainGachaponItem3(final int id, final int quantity, final String msg, final int 概率) {
        try {
            if (!MapleItemInformationProvider.getInstance().itemExists(id)) {
                return -1;
            }
            final IItem item = MapleInventoryManipulator.addbyId_Gachapon(this.c, id, (short)quantity);
            if (item == null) {
                return -1;
            }
            if (概率 > 0) {
                Broadcast.broadcastMessage(MaplePacketCreator.getGachaponMega("[ " + msg + " ] : 已经被玩家 [ " + this.c.getPlayer().getName(), " ] 幸运抽中！", item, (byte)0, this.getPlayer().getClient().getChannel()));
            }
            return item.getItemId();
        }
        catch (Exception e) {
            //e.printStackTrace();
            return -1;
        }
    }
    
    public merchant_main getMerchant_main() {
        return merchant_main.getInstance();
    }
    
    public void logToFile_chr(final String path, final String msg) {
        FileoutputUtil.logToFile(path, msg);
    }
    
    public void gainmoneym(final int slot) {
        this.gainmoneym(this.c.getPlayer().getAccountID(), slot);
    }
    
    public void gainmoneym(final int cid, final int slot) {
        try (Connection con = (Connection)DBConPool.getInstance().getDataSource().getConnection()) {
            final PreparedStatement ps = con.prepareStatement("UPDATE accounts SET moneym =moneym+" + slot + " WHERE id = " + cid + "");
            ps.executeUpdate();
            ps.close();
        }
        catch (SQLException ex) {
            ex.getStackTrace();
        }
        FileoutputUtil.logToFile("日志/商人交易系统/商人利益点更改.txt", " 更改值(" + (Object)((this.c.getPlayer().getAccountID() == cid) ? "自身" : Integer.valueOf(cid)) + ") " + slot + "");
    }
    
    public void setmoneym(final int slot) {
        this.setmoneym(this.c.getPlayer().getAccountID(), slot);
    }
    
    public void setmoneym(final int cid, final int slot) {
        try (Connection con = (Connection)DBConPool.getInstance().getDataSource().getConnection()) {
            final PreparedStatement ps = con.prepareStatement("UPDATE accounts SET moneym = " + slot + " WHERE id = " + cid + "");
            ps.executeUpdate();
            ps.close();
        }
        catch (SQLException ex) {
            ex.getStackTrace();
        }
        FileoutputUtil.logToFile("日志/商人交易系统/商人利益点更改.txt", " 设置值(" + (Object)((this.c.getPlayer().getAccountID() == cid) ? "自身" : Integer.valueOf(cid)) + ") " + slot + "");
    }
    
    public int getmoneym() {
        int moneyb = 0;
        try (Connection con = (Connection)DBConPool.getInstance().getDataSource().getConnection()) {
            final int cid = this.getPlayer().getAccountID();
            ResultSet rs;
            try (final PreparedStatement limitCheck = con.prepareStatement("SELECT * FROM accounts WHERE id=" + cid + "")) {
                rs = limitCheck.executeQuery();
                if (rs.next()) {
                    moneyb = rs.getInt("moneym");
                }
            }
            rs.close();
        }
        catch (SQLException ex) {
            ex.getStackTrace();
        }
        return moneyb;
    }
    
    public boolean checkHold(final int itemid, int quantity) {
        byte need_solt = 0;
        while (quantity > 0) {
            ++need_solt;
            if (quantity < 32767) {
                break;
            }
            quantity -= 32767;
        }
        return this.c.getPlayer().getInventory(GameConstants.getInventoryType(itemid)).getNumFreeSlot() >= need_solt;
    }
    
    public void 全服突破世界等级奖励() {
        for (final ChannelServer cserv : ChannelServer.getAllInstances()) {
            for (final MapleCharacter mch : cserv.getPlayerStorage().getAllCharacters()) {
                mch.modifyCSPoints(1, 1000);
                MapleInventoryManipulator.addById(mch.getClient(), 2340000, (short)1, "");
                MapleInventoryManipulator.addById(mch.getClient(), 4000463, (short)5, "");
                mch.dropMessage(-11, "[突破限制等级] 恭喜您获得管理员赠送给您的" + "点券" + 1000 + " 点." + "祝福卷轴1个、国庆纪念币5个.");
                mch.dropMessage(-1, "[突破限制等级] 恭喜您获得管理员赠送给您的" + "点券 " + 1000 + " 点." + "祝福卷轴1个、国庆纪念币5个.");
            }
        }
        for (final ChannelServer cserv2 : ChannelServer.getAllInstances()) {
            for (final MapleCharacter mch : cserv2.getPlayerStorage().getAllCharacters()) {
                mch.startMapEffect("[突破限制等级] ：奖励1000点券、祝福卷轴1个、国庆纪念币5个.给在线的所有玩家！", 5121006);
            }
        }
    }
    public static int getMRJF(final int id) {
        int jf = 0;
        try (Connection con = (Connection)DBConPool.getInstance().getDataSource().getConnection()) {
            final PreparedStatement ps = con.prepareStatement("select * from paymoney1 where characterid =?");
            ps.setInt(1, id);
            final ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                jf = rs.getInt("mrjf");
            }
            else {
                final PreparedStatement psu = con.prepareStatement("insert into paymoney1 (characterid,mrjf) VALUES (?,?)");
                psu.setInt(1, id);
                psu.setInt(2, 0);
                psu.executeUpdate();
                psu.close();
            }
            ps.close();
            rs.close();
        }
        catch (SQLException ex) {
            System.err.println("每日积分读取发生错误: " + ex);
        }
        return jf;
    }
    public static int 角色名字取Id(final String name) {
        int data = 0;
        try (Connection con = DatabaseConnection.getConnection()) {
            final PreparedStatement ps = con.prepareStatement("SELECT id as DATA FROM characters WHERE name = ?");
            ps.setString(1, name);
            try (final ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    data = rs.getInt("DATA");
                }
                rs.close();
            }
            ps.close();
        }
        catch (SQLException Ex) {
            System.err.println("获取角色ID取名字出错 - 数据库查询失败：" + Ex);
        }
        return data;
    }
    public  int 取限制等级() {
        int 限制等级 = 0;
        try {
            final int cid = 4001128;
            Connection con = DatabaseConnection.getConnection();
            final PreparedStatement limitCheck = con.prepareStatement("SELECT * FROM shijiexianzhidengji WHERE huoyaotongid=" + cid + "");
            final ResultSet rs = limitCheck.executeQuery();
            if (rs.next()) {
                限制等级 = rs.getInt("xianzhidengji");
            }
            limitCheck.close();
            rs.close();
        }
        catch (SQLException ex) {}
        return 限制等级;
    }
    
    public int 取火药桶数量() {
        int 火药桶 = 0;
        try {
            final int cid = 4001128;
            Connection con = DatabaseConnection.getConnection();
            final PreparedStatement limitCheck = con.prepareStatement("SELECT * FROM shijiexianzhidengji WHERE huoyaotongid=" + cid + "");
            final ResultSet rs = limitCheck.executeQuery();
            if (rs.next()) {
                火药桶 = rs.getInt("dangqianshuliang");
            }
            limitCheck.close();
            rs.close();
        }
        catch (SQLException ex) {}
        return 火药桶;
    }
    
    public int 取火药桶总数量() {
        int 火药桶总数量 = 0;
        try {
            final int cid = 4001128;
            Connection con = DatabaseConnection.getConnection();
            final PreparedStatement limitCheck = con.prepareStatement("SELECT * FROM shijiexianzhidengji WHERE huoyaotongid=" + cid + "");
            final ResultSet rs = limitCheck.executeQuery();
            if (rs.next()) {
                火药桶总数量 = rs.getInt("zongshuliang");
            }
            limitCheck.close();
            rs.close();
        }
        catch (SQLException ex) {}
        return 火药桶总数量;
    }
    
    public void 写入火药桶数量(final int slot) {
        try {
            final int cid = 4001128;
            Connection con = DatabaseConnection.getConnection();
            final PreparedStatement ps = con.prepareStatement("UPDATE shijiexianzhidengji SET dangqianshuliang =dangqianshuliang+ " + slot + " WHERE huoyaotongid = " + cid + "");
            ps.executeUpdate();
            ps.close();
        }
        catch (SQLException ex) {}
    }
    
    public void 写入火药桶总数量(final int slot) {
        try {
            final int cid = 4001128;
            Connection con = DatabaseConnection.getConnection();
            final PreparedStatement ps = con.prepareStatement("UPDATE shijiexianzhidengji SET zongshuliang =zongshuliang+ " + slot + " WHERE huoyaotongid = " + cid + "");
            ps.executeUpdate();
            ps.close();
        }
        catch (SQLException ex) {}
    }
    
    public void 写入限制等级(final int slot) {
        try {
            final int cid = 4001128;
            Connection con = DatabaseConnection.getConnection();
            final PreparedStatement ps = con.prepareStatement("UPDATE shijiexianzhidengji SET xianzhidengji =xianzhidengji+ " + slot + " WHERE huoyaotongid = " + cid + "");
            ps.executeUpdate();
            ps.close();
        }
        catch (SQLException ex) {}
    }
    public static void 写入世界等级(final int slot) {
        try {
            final int cid = 4001128;
            Connection con = DatabaseConnection.getConnection();
            final PreparedStatement ps = con.prepareStatement("UPDATE shijiexianzhidengji SET xianzhidengji =" + slot + " WHERE huoyaotongid = " + cid + "");
            ps.executeUpdate();
            ps.close();
        }
        catch (SQLException ex) {}
    }
    public static void 写入个人等级(final int slot,final int userId) {
        try {
            Connection con = DatabaseConnection.getConnection();
            final PreparedStatement ps = con.prepareStatement("UPDATE shijiexianzhidengji SET xianzhidengji =" + slot + " WHERE huoyaotongid = " + userId + "");
            ps.executeUpdate();
            ps.close();
        }
        catch (SQLException ex) {}
    }
    public void 扣除火药桶数量(final int slot) {
        try {
            final int cid = 4001128;
            Connection con = DatabaseConnection.getConnection();
            final PreparedStatement ps = con.prepareStatement("UPDATE shijiexianzhidengji SET dangqianshuliang =dangqianshuliang- " + slot + " WHERE huoyaotongid = " + cid + "");
            ps.executeUpdate();
            ps.close();
        }
        catch (SQLException ex) {}
    }
    
    public void spawnChaosZakum(final int x, final int y) {
        final MapleMap mapp = this.c.getChannelServer().getMapFactory().getMap(this.c.getPlayer().getMapId());
        mapp.spawnChaosZakum(x, y);
    }
    
    public void spawnZakum(final int x, final int y) {
        final MapleMap mapp = this.c.getChannelServer().getMapFactory().getMap(this.c.getPlayer().getMapId());
        mapp.spawnZakum(x, y);
    }

    public void 清除地图所有(final int mapId) {
        try {
            MapleMap mapleMap =this.getMap(mapId);
            mapleMap.resetFully();
            mapleMap.killAllMonsters(true);
            mapleMap.respawn(true);
        } catch (Exception e) {

        }
    }

    int temp;
    class xiguai extends Thread{
        long time = LtMS.ConfigValuesMap.get("吸怪延迟").longValue();
        public void run()
        {

        int mapId  = c.getPlayer().getMapId();
            if ( c.getPlayer()!=null) {
                int id=c.getPlayer().getId();
                UserAttraction attractList = NPCConversationManager.getAttractList(id);
                if (attractList==null){
                    return;
                }
                Point position = attractList.getPosition();
                while(Objects.nonNull(NPCConversationManager.getAttractList(id)) && c.getPlayer()!=null && mapId == c.getPlayer().getMapId()){
                    MapleMap map = c.getPlayer().getMap();
                    for (final MapleMonster mmo : map.getAllMonstersThreadsafe()) {//mmo.getPosition()
                        if (!mmo.getStats().isBoss() && c.getPlayer().getLastRes() !=null) {
                            map.broadcastMessage(MobPacket.moveMonster(false, 0, 0, mmo.getObjectId(), position, position, c.getPlayer().getLastRes()));
                            mmo.setPosition(position);
                        }
                    }
                    try{
                        sleep(time);
                    }catch(Exception e){

                    }
                }
            }
            stop();
        }
    }

    public void 开启吸怪() {
        xiguai xiguai =new xiguai();
        temp=1;
        xiguai.start();
    }

//    public String 查询吸怪信息() {
//        MapleMap map = c.getPlayer().getMap();
//        int num = 0;
//        final StringBuilder name = new StringBuilder();
//        if (num == 0) {
//            name.append("当前角色:" + c.getPlayer().getName() + "\r\n");
//            name.append("\t\t\t吸怪地图:" + map.getId() + "[" + map.getMapName() + "]\r\n");
//            name.append("\t\t\t吸怪坐标:" + map.getMonsterSpawnner().right + "\r\n");
//            name.append("\t\t\t人物坐标:" + c.getPlayer().getPosition() + "\r\n");
//        }
//        if (name.length() > 0) {
//            return name.toString();
//        } else {
//            return "暂无吸怪信息！";
//        }
//    }

    public String ms() {
        return ServerProperties.getProperty("Guai.serverName", "");
    }

    public int 召唤扎昆() {
        this.c.getPlayer().getMap().spawnFakeMonsterOnGroundBelow(MapleLifeFactory.getMonster(8800000), this.c.getPlayer().getPosition());
        for(int i = 8800003; i <= 8800010; ++i) {
            this.c.getPlayer().getMap().spawnMonsterOnGroundBelow(MapleLifeFactory.getMonster(i), this.c.getPlayer().getPosition());
        }

        return 1;
    }

    public int 召唤黑龙() {
        this.c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.musicChange("Bgm14/HonTale"));
        this.c.getPlayer().getMap().spawnMonsterOnGroundBelow(MapleLifeFactory.getMonster(8810026), this.c.getPlayer().getPosition());
        return 1;
    }

    public int 召唤闹钟() {
        MapleMonster mob0 = MapleLifeFactory.getMonster(8500001);
        this.c.getPlayer().getMap().spawnMonsterOnGroundBelow(mob0, this.c.getPlayer().getPosition());
        return 1;
    }

    public final void 全服漂浮喇叭(String msg, int itemId) {
        int ret = 0;
        Iterator var4 = ChannelServer.getAllInstances().iterator();

        while(var4.hasNext()) {
            ChannelServer cserv = (ChannelServer)var4.next();

            for(Iterator var6 = cserv.getPlayerStorage().getAllCharacters().iterator(); var6.hasNext(); ++ret) {
                MapleCharacter mch = (MapleCharacter)var6.next();
                mch.startMapEffect(msg, itemId);
            }
        }

    }

    public final MapleInventory 判断背包装备栏() {
        return this.c.getPlayer().getInventory(MapleInventoryType.getByType((byte)1));
    }

    public final MapleInventory 判断背包穿戴栏() {
        return this.c.getPlayer().getInventory(MapleInventoryType.getByType((byte)-1));
    }

    public final MapleInventory 判断背包消耗栏() {
        return this.c.getPlayer().getInventory(MapleInventoryType.getByType((byte)2));
    }

    public final MapleInventory 判断背包设置栏() {
        return this.c.getPlayer().getInventory(MapleInventoryType.getByType((byte)3));
    }

    public final MapleInventory 判断背包其他栏() {
        return this.c.getPlayer().getInventory(MapleInventoryType.getByType((byte)4));
    }

    public final MapleInventory 判断背包特殊栏() {
        return this.c.getPlayer().getInventory(MapleInventoryType.getByType((byte)5));
    }
    public void openShopNPC(int shopid) {
        MapleShopFactory.getInstance().getShop(shopid).sendShop(this.c, this.npc);
    }
    public void 完成任务(int id) {
        MapleQuest.getInstance(id).complete(this.getPlayer(), this.npc);
    }

    public void 放弃任务(int id) {
        MapleQuest.getInstance(id).forfeit(this.getPlayer());
    }

    public void 任务开始() {
        MapleQuest.getInstance(this.questid).forceStart(this.getPlayer(), this.getNpc(), (String)null);
    }

    public void 任务开始(int id) {
        MapleQuest.getInstance(id).forceStart(this.getPlayer(), this.getNpc(), (String)null);
    }

    public void 开始任务(int id) {
        MapleQuest.getInstance(id).forceStart(this.getPlayer(), this.getNpc(), (String)null);
    }

    public void 任务开始(String customData) {
        MapleQuest.getInstance(this.questid).forceStart(this.getPlayer(), this.getNpc(), customData);
    }

    public void 任务完成() {
        MapleQuest.getInstance(this.questid).forceComplete(this.getPlayer(), this.getNpc());
    }

    public void 任务完成(int id) {
        MapleQuest.getInstance(id).forceComplete(this.getPlayer(), this.getNpc());
    }

    public void 任务放弃(int id) {
        MapleQuest.getInstance(id).forfeit(this.getPlayer());
    }

    public int 给炼金经验(int s) {
        return this.setBossRankCount5("炼金经验", s);
    }

    public int 挖矿经验() {
        return this.getBossRankCount5("炼金经验");
    }


    public boolean 百分率(int q) {
        int a = (int)Math.ceil(Math.random() * 100.0);
        return a <= q;
    }

    public void 重置目标地图(int a) {
        this.getMap(a).resetFully();
    }

    public void 清除地图怪物(int a, int b) {
        double range = Double.POSITIVE_INFINITY;
        MapleMap map = this.getMap(a);
        boolean drop = false;
        if (b == 0) {
            drop = true;
        } else {
            drop = false;
        }

        Iterator var8 = map.getMapObjectsInRange(this.c.getPlayer().getPosition(), range, Arrays.asList(MapleMapObjectType.MONSTER)).iterator();

        while(var8.hasNext()) {
            MapleMapObject monstermo = (MapleMapObject)var8.next();
            MapleMonster mob = (MapleMonster)monstermo;
            map.killMonster(mob, this.c.getPlayer(), drop, false, (byte)1);
        }

    }
    public void 脱光装备() {
        MapleInventory equipped = this.getPlayer().getInventory(MapleInventoryType.EQUIPPED);
        MapleInventory equip = this.getPlayer().getInventory(MapleInventoryType.EQUIP);
        List<Short> ids = new LinkedList();
        Iterator var4 = equipped.list().iterator();

        while(var4.hasNext()) {
            IItem item = (IItem)var4.next();
            ids.add(item.getPosition());
        }

        var4 = ids.iterator();

        while(var4.hasNext()) {
            short id = (Short)var4.next();
            MapleInventoryManipulator.unequip(this.getC(), id, equip.getNextFreeSlot());
        }

    }

    public void 脱掉并且销毁装备(int x) {
        MapleInventory equipped = this.getPlayer().getInventory(MapleInventoryType.EQUIPPED);
        MapleInventory equip = this.getPlayer().getInventory(MapleInventoryType.EQUIP);
        List<Short> ids = new LinkedList();
        Iterator var5 = equipped.list().iterator();

        while(var5.hasNext()) {
            IItem item = (IItem)var5.next();
            ids.add(item.getPosition());
        }

        var5 = ids.iterator();

        while(var5.hasNext()) {
            short id = (Short)var5.next();
            if (id == x) {
                MapleInventoryManipulator.unequip(this.getC(), id, equip.getNextFreeSlot());
            }
        }

        MapleInventoryManipulator.removeFromSlot(this.getC(), MapleInventoryType.EQUIP, (short)1, (short) 1, true);
    }

    public void 全服音效(boolean broadcast, String sound) {
        Iterator var3 = ChannelServer.getAllInstances().iterator();

        while(var3.hasNext()) {
            ChannelServer cserv1 = (ChannelServer)var3.next();
            Iterator var5 = cserv1.getPlayerStorage().getAllCharacters().iterator();

            while(var5.hasNext()) {
                MapleCharacter mch = (MapleCharacter)var5.next();
                Broadcast.broadcastMessage(MaplePacketCreator.playSound(sound));
            }
        }

    }

    public void 切换频道(int id) {
        this.c.getPlayer().changeChannel(id);
    }

    public void 动画(String String) {
        this.c.sendPacket(MaplePacketCreator.showEffect(String));
    }

    public void 动画2(String String) {
        this.c.sendPacket(UIPacket.AranTutInstructionalBalloon(String));
    }

    public void 动画3(String data) {
        this.c.sendPacket(UIPacket.ShowWZEffect(data));
    }

    public void 全服公告(String text) {
        Broadcast.broadcastMessage(MaplePacketCreator.serverNotice(6, text));
    }
    public void 个人黄色字体公告(String message) {
        this.c.sendPacket(UIPacket.getTopMsg(message));
    }

    public void 全服黄色字体(String message) {
        Iterator var2 = ChannelServer.getAllInstances().iterator();

        while(var2.hasNext()) {
            ChannelServer cserv1 = (ChannelServer)var2.next();
            Iterator var4 = cserv1.getPlayerStorage().getAllCharacters().iterator();

            while(var4.hasNext()) {
                MapleCharacter mch = (MapleCharacter)var4.next();
                this.c.sendPacket(UIPacket.getTopMsg(message));
            }
        }

    }
    public void 加运气(int luk) {
        Equip item = (Equip)this.c.getPlayer().getInventory(MapleInventoryType.EQUIP).getItem((short)1).copy();
        item.setLuk((short)(item.getLuk() + luk));
        MapleInventoryManipulator.removeFromSlot(this.getC(), MapleInventoryType.EQUIP, (short)1,  (short)1, true);
        MapleInventoryManipulator.addFromDrop(this.getChar().getClient(), item, false);
    }

    public void 加智力(int Int) {
        Equip item = (Equip)this.c.getPlayer().getInventory(MapleInventoryType.EQUIP).getItem((short)1).copy();
        item.setInt((short)(item.getInt() + Int));
        MapleInventoryManipulator.removeFromSlot(this.getC(), MapleInventoryType.EQUIP, (short)1,  (short)1, true);
        MapleInventoryManipulator.addFromDrop(this.getChar().getClient(), item, false);
    }

    public void 加敏捷(int dex) {
        Equip item = (Equip)this.c.getPlayer().getInventory(MapleInventoryType.EQUIP).getItem((short)1).copy();
        item.setDex((short)(item.getDex() + dex));
        MapleInventoryManipulator.removeFromSlot(this.getC(), MapleInventoryType.EQUIP, (short)1,  (short)1, true);
        MapleInventoryManipulator.addFromDrop(this.getChar().getClient(), item, false);
    }

    public void 加力量(int str) {
        Equip item = (Equip)this.c.getPlayer().getInventory(MapleInventoryType.EQUIP).getItem((short)1).copy();
        item.setStr((short)(item.getStr() + str));
        MapleInventoryManipulator.removeFromSlot(this.getC(), MapleInventoryType.EQUIP, (short)1,  (short)1, true);
        MapleInventoryManipulator.addFromDrop(this.getChar().getClient(), item, false);
    }

    public void 加命中率(int Acc) {
        Equip item = (Equip)this.c.getPlayer().getInventory(MapleInventoryType.EQUIP).getItem((short)1).copy();
        item.setAcc((short)(item.getAcc() + Acc));
        MapleInventoryManipulator.removeFromSlot(this.getC(), MapleInventoryType.EQUIP, (short)1,  (short)1, true);
        MapleInventoryManipulator.addFromDrop(this.getChar().getClient(), item, false);
    }

    public void 加跳跃力(int Jump) {
        Equip item = (Equip)this.c.getPlayer().getInventory(MapleInventoryType.EQUIP).getItem((short)1).copy();
        item.setJump((short)(item.getJump() + Jump));
        MapleInventoryManipulator.removeFromSlot(this.getC(), MapleInventoryType.EQUIP, (short)1,  (short)1, true);
        MapleInventoryManipulator.addFromDrop(this.getChar().getClient(), item, false);
    }

    public void 加移动速度(int Speed) {
        Equip item = (Equip)this.c.getPlayer().getInventory(MapleInventoryType.EQUIP).getItem((short)1).copy();
        item.setSpeed((short)(item.getSpeed() + Speed));
        MapleInventoryManipulator.removeFromSlot(this.getC(), MapleInventoryType.EQUIP, (short)1,  (short)1, true);
        MapleInventoryManipulator.addFromDrop(this.getChar().getClient(), item, false);
    }

    public void 加闪避率(int Avoid) {
        Equip item = (Equip)this.c.getPlayer().getInventory(MapleInventoryType.EQUIP).getItem((short)1).copy();
        item.setAvoid((short)(item.getAvoid() + Avoid));
        MapleInventoryManipulator.removeFromSlot(this.getC(), MapleInventoryType.EQUIP, (short)1,  (short)1, true);
        MapleInventoryManipulator.addFromDrop(this.getChar().getClient(), item, false);
    }

    public void 加魔法攻击(int matk) {
        Equip item = (Equip)this.c.getPlayer().getInventory(MapleInventoryType.EQUIP).getItem((short)1).copy();
        item.setMatk((short)(item.getMatk() + matk));
        MapleInventoryManipulator.removeFromSlot(this.getC(), MapleInventoryType.EQUIP, (short)1,  (short)1, true);
        MapleInventoryManipulator.addFromDrop(this.getChar().getClient(), item, false);
    }

    public void 加魔法防御(int Mdef) {
        Equip item = (Equip)this.c.getPlayer().getInventory(MapleInventoryType.EQUIP).getItem((short)1).copy();
        item.setMdef((short)(item.getMdef() + Mdef));
        MapleInventoryManipulator.removeFromSlot(this.getC(), MapleInventoryType.EQUIP, (short)1,  (short)1, true);
        MapleInventoryManipulator.addFromDrop(this.getChar().getClient(), item, false);
    }

    public void 加物理攻击(int watk) {
        Equip item = (Equip)this.c.getPlayer().getInventory(MapleInventoryType.EQUIP).getItem((short)1).copy();
        item.setWatk((short)(item.getWatk() + watk));
        MapleInventoryManipulator.removeFromSlot(this.getC(), MapleInventoryType.EQUIP, (short)1,  (short)1, true);
        MapleInventoryManipulator.addFromDrop(this.getChar().getClient(), item, false);
    }

    public void 加物理防御(int Wdef) {
        Equip item = (Equip)this.c.getPlayer().getInventory(MapleInventoryType.EQUIP).getItem((short)1).copy();
        item.setWdef((short)(item.getWdef() + Wdef));
        MapleInventoryManipulator.removeFromSlot(this.getC(), MapleInventoryType.EQUIP, (short)1,  (short)1, true);
        MapleInventoryManipulator.addFromDrop(this.getChar().getClient(), item, false);
    }

    public void 加升级次数(int upgr) {
        Equip item = (Equip)this.c.getPlayer().getInventory(MapleInventoryType.EQUIP).getItem((short)1).copy();
        item.setUpgradeSlots((byte)(item.getUpgradeSlots() + upgr));
        MapleInventoryManipulator.removeFromSlot(this.getC(), MapleInventoryType.EQUIP, (short)1,  (short)1, true);
        MapleInventoryManipulator.addFromDrop(this.getChar().getClient(), item, false);
    }

    public void 加最大生命值(int hp) {
        Equip item = (Equip)this.c.getPlayer().getInventory(MapleInventoryType.EQUIP).getItem((short)1).copy();
        item.setHp((short)(item.getHp() + hp));
        MapleInventoryManipulator.removeFromSlot(this.getC(), MapleInventoryType.EQUIP, (short)1,  (short)1, true);
        MapleInventoryManipulator.addFromDrop(this.getChar().getClient(), item, false);
    }

    public void 加最大法力值(int mp) {
        Equip item = (Equip)this.c.getPlayer().getInventory(MapleInventoryType.EQUIP).getItem((short)1).copy();
        item.setMp((short)(item.getMp() + mp));
        MapleInventoryManipulator.removeFromSlot(this.getC(), MapleInventoryType.EQUIP, (short)1,  (short)1, true);
        MapleInventoryManipulator.addFromDrop(this.getChar().getClient(), item, false);
    }

    public void 装备洗练() {
        Equip item = (Equip)this.c.getPlayer().getInventory(MapleInventoryType.EQUIP).getItem((short)1).copy();
        int getSpeed;
        if (item.getUpgradeSlots() > 0) {
            getSpeed = (int)Math.ceil(Math.random() * ((double)item.getUpgradeSlots() + (double)item.getUpgradeSlots() * 0.5));
            item.setUpgradeSlots((byte)getSpeed);
        }

        if (item.getWatk() > 0) {
            getSpeed = (int)Math.ceil(Math.random() * ((double)item.getWatk() + (double)item.getWatk() * 0.5));
            item.setWatk((short)((byte)getSpeed));
        }

        if (item.getMatk() > 0) {
            getSpeed = (int)Math.ceil(Math.random() * ((double)item.getMatk() + (double)item.getMatk() * 0.5));
            item.setMatk((short)((byte)getSpeed));
        }

        if (item.getWdef() > 0) {
            getSpeed = (int)Math.ceil(Math.random() * ((double)item.getWdef() + (double)item.getWdef() * 0.5));
            item.setWdef((short)((byte)getSpeed));
        }

        if (item.getMdef() > 0) {
            getSpeed = (int)Math.ceil(Math.random() * ((double)item.getMdef() + (double)item.getMdef() * 0.5));
            item.setMdef((short)((byte)getSpeed));
        }

        if (item.getStr() > 0) {
            getSpeed = (int)Math.ceil(Math.random() * ((double)item.getStr() + (double)item.getStr() * 0.5));
            item.setStr((short)((byte)getSpeed));
        }

        if (item.getDex() > 0) {
            getSpeed = (int)Math.ceil(Math.random() * ((double)item.getDex() + (double)item.getDex() * 0.5));
            item.setDex((short)((byte)getSpeed));
        }

        if (item.getLuk() > 0) {
            getSpeed = (int)Math.ceil(Math.random() * ((double)item.getLuk() + (double)item.getLuk() * 0.5));
            item.setLuk((short)((byte)getSpeed));
        }

        if (item.getInt() > 0) {
            getSpeed = (int)Math.ceil(Math.random() * ((double)item.getInt() + (double)item.getInt() * 0.5));
            item.setInt((short)((byte)getSpeed));
        }

        if (item.getHp() > 0) {
            getSpeed = (int)Math.ceil(Math.random() * ((double)item.getHp() + (double)item.getHp() * 0.5));
            item.setHp((short)((byte)getSpeed));
        }

        if (item.getMp() > 0) {
            getSpeed = (int)Math.ceil(Math.random() * ((double)item.getMp() + (double)item.getMp() * 0.5));
            item.setMp((short)((byte)getSpeed));
        }

        if (item.getAcc() > 0) {
            getSpeed = (int)Math.ceil(Math.random() * ((double)item.getAcc() + (double)item.getAcc() * 0.5));
            item.setAcc((short)((byte)getSpeed));
        }

        if (item.getAvoid() > 0) {
            getSpeed = (int)Math.ceil(Math.random() * ((double)item.getAvoid() + (double)item.getAvoid() * 0.5));
            item.setAvoid((short)((byte)getSpeed));
        }

        if (item.getSpeed() > 0) {
            getSpeed = (int)Math.ceil(Math.random() * ((double)item.getSpeed() + (double)item.getSpeed() * 0.5));
            item.setSpeed((short)((byte)getSpeed));
        }

        MapleInventoryManipulator.removeFromSlot(this.getC(), MapleInventoryType.EQUIP, (short)1, (short) 1, true);
        MapleInventoryManipulator.addFromDrop(this.getChar().getClient(), item, false);
    }



    public void 加宝石(int a) {
        Equip item = (Equip)this.c.getPlayer().getInventory(MapleInventoryType.EQUIP).getItem((short)1).copy();
        item.setMpRR((short)(item.getMpRR() + a));
        MapleInventoryManipulator.removeFromSlot(this.getC(), MapleInventoryType.EQUIP, (short)1, (short) 1, true);
        MapleInventoryManipulator.addFromDrop(this.getChar().getClient(), item, false);
    }

    public void 加锻造等级(int a, int b) {
        Equip item = (Equip)this.c.getPlayer().getInventory(MapleInventoryType.EQUIP).getItem((short)a).copy();
        item.setHpRR((short)(item.getHpRR() + b));
        MapleInventoryManipulator.removeFromSlot(this.getC(), MapleInventoryType.EQUIP, (short)a, (short) 1, true);
        MapleInventoryManipulator.addFromDrop(this.getChar().getClient(), item, false);
    }

    public int 取个人副本通关时间最快(int a, int b) {
        int data = 0;

        try {
            Connection con = DBConPool.getConnection();
            Throwable var5 = null;

            try {
                PreparedStatement ps = con.prepareStatement("SELECT * FROM fubenjilu WHERE name = " + a + " && cid = " + b + "order by time asc");
                ResultSet rs = ps.executeQuery();
                Throwable var8 = null;

                try {
                    if (rs.next()) {
                        data = rs.getInt("time");
                    }
                } catch (Throwable var33) {
                    var8 = var33;
                    throw var33;
                } finally {
                    if (rs != null) {
                        if (var8 != null) {
                            try {
                                rs.close();
                            } catch (Throwable var32) {
                                var8.addSuppressed(var32);
                            }
                        } else {
                            rs.close();
                        }
                    }

                }

                ps.close();
            } catch (Throwable var35) {
                var5 = var35;
                throw var35;
            } finally {
                if (con != null) {
                    if (var5 != null) {
                        try {
                            con.close();
                        } catch (Throwable var31) {
                            var5.addSuppressed(var31);
                        }
                    } else {
                        con.close();
                    }
                }

            }
        } catch (SQLException var37) {
            服务端输出信息.println_err("取副本通关时间最快 - 数据库查询失败：" + var37);
        }

        return data;
    }

    public int 取副本通关最快时间(int a) {
        int data = 0;

        try {
            Connection con = DBConPool.getConnection();
            Throwable var4 = null;

            try {
                PreparedStatement ps = con.prepareStatement("SELECT * FROM fubenjilu WHERE name = " + a + " order by time asc");
                ResultSet rs = ps.executeQuery();
                Throwable var7 = null;

                try {
                    if (rs.next()) {
                        data = rs.getInt("time");
                    }
                } catch (Throwable var32) {
                    var7 = var32;
                    throw var32;
                } finally {
                    if (rs != null) {
                        if (var7 != null) {
                            try {
                                rs.close();
                            } catch (Throwable var31) {
                                var7.addSuppressed(var31);
                            }
                        } else {
                            rs.close();
                        }
                    }

                }

                ps.close();
            } catch (Throwable var34) {
                var4 = var34;
                throw var34;
            } finally {
                if (con != null) {
                    if (var4 != null) {
                        try {
                            con.close();
                        } catch (Throwable var30) {
                            var4.addSuppressed(var30);
                        }
                    } else {
                        con.close();
                    }
                }

            }
        } catch (SQLException var36) {
            服务端输出信息.println_err("取副本通关时间最快 - 数据库查询失败：" + var36);
        }

        return data;
    }

    public int 取副本通关最快玩家(int a) {
        int data = 0;

        try {
            Connection con = DBConPool.getConnection();
            Throwable var4 = null;

            try {
                PreparedStatement ps = con.prepareStatement("SELECT * FROM fubenjilu WHERE name = " + a + " order by time asc");
                ResultSet rs = ps.executeQuery();
                Throwable var7 = null;

                try {
                    if (rs.next()) {
                        data = rs.getInt("cid");
                    }
                } catch (Throwable var32) {
                    var7 = var32;
                    throw var32;
                } finally {
                    if (rs != null) {
                        if (var7 != null) {
                            try {
                                rs.close();
                            } catch (Throwable var31) {
                                var7.addSuppressed(var31);
                            }
                        } else {
                            rs.close();
                        }
                    }

                }

                ps.close();
            } catch (Throwable var34) {
                var4 = var34;
                throw var34;
            } finally {
                if (con != null) {
                    if (var4 != null) {
                        try {
                            con.close();
                        } catch (Throwable var30) {
                            var4.addSuppressed(var30);
                        }
                    } else {
                        con.close();
                    }
                }

            }
        } catch (SQLException var36) {
            服务端输出信息.println_err("取副本通关时间最快 - 数据库查询失败：" + var36);
        }

        return data;
    }

    public int 取副本通关时间(int a, int b) {
        int data = 0;

        try {
            Connection con = DBConPool.getConnection();
            Throwable var5 = null;

            try {
                PreparedStatement ps = con.prepareStatement("SELECT time as DATA FROM fubenjilu WHERE name = " + a + " && cid = " + b + "");
                ResultSet rs = ps.executeQuery();
                Throwable var8 = null;

                try {
                    if (rs.next()) {
                        data = rs.getInt("DATA");
                    }
                } catch (Throwable var33) {
                    var8 = var33;
                    throw var33;
                } finally {
                    if (rs != null) {
                        if (var8 != null) {
                            try {
                                rs.close();
                            } catch (Throwable var32) {
                                var8.addSuppressed(var32);
                            }
                        } else {
                            rs.close();
                        }
                    }

                }

                ps.close();
            } catch (Throwable var35) {
                var5 = var35;
                throw var35;
            } finally {
                if (con != null) {
                    if (var5 != null) {
                        try {
                            con.close();
                        } catch (Throwable var31) {
                            var5.addSuppressed(var31);
                        }
                    } else {
                        con.close();
                    }
                }

            }
        } catch (SQLException var37) {
            服务端输出信息.println_err("取副本通关时间 - 数据库查询失败：" + var37);
        }

        return data;
    }

    public void 动态数据(String type1) {
        LtMS.ConfigValuesMap.get(type1);
    }

    public void 全服喇叭(int type, String text) {
        if (type != 3 && type != 9 && type != 10) {
            Broadcast.broadcastMessage(MaplePacketCreator.serverNotice(type, text));
        } else {
            StringBuilder sb = new StringBuilder();
            addMedalString(this.c.getPlayer(), sb);
            sb.append(this.c.getPlayer().getName());
            sb.append(" : ");
            sb.append(text);
            Broadcast.broadcastSmega(MaplePacketCreator.serverNotice(type, this.c.getChannel(), sb.toString(), true));
        }

    }

    public void 全服喇叭(int type, String text, String head) {
        if (type != 3 && type != 9 && type != 10) {
            Broadcast.broadcastMessage(MaplePacketCreator.serverNotice(type, text));
        } else {
            StringBuilder sb = new StringBuilder();
            addMedalString(this.c.getPlayer(), sb);
            sb.append(head);
            sb.append(" : ");
            sb.append(text);
            Broadcast.broadcastSmega(MaplePacketCreator.serverNotice(type, this.c.getChannel(), sb.toString(), true));
        }

    }
    private static void addMedalString(MapleCharacter c, StringBuilder sb) {
        IItem medal = c.getInventory(MapleInventoryType.EQUIPPED).getItem((short)-21);
        if (medal != null) {
            sb.append("<");
            sb.append(MapleItemInformationProvider.getInstance().getName(medal.getItemId()));
            sb.append("> ");
        }

    }
    public void 强化加卷次数(int upgr) {
        Equip item = (Equip)this.c.getPlayer().getInventory(MapleInventoryType.EQUIP).getItem((short)1).copy();
        item.setUpgradeSlots((byte)(item.getUpgradeSlots() + upgr));
        MapleInventoryManipulator.removeFromSlot(this.getC(), MapleInventoryType.EQUIP, (short)1, 1L, true);
        MapleInventoryManipulator.addFromDrop(this.getChar().getClient(), item, false);
    }

    public void gainEquiPproperty(int upgr, int watk, int matk, int str, int dex, int Int, int luk, int hp, int mp, int acc, int avoid) {
        Equip item = (Equip)this.c.getPlayer().getInventory(MapleInventoryType.EQUIP).getItem((short)1).copy();
        item.setUpgradeSlots((byte)(item.getUpgradeSlots() + upgr));
        item.setWatk((short)(item.getWatk() + watk));
        item.setMatk((short)(item.getMatk() + matk));
        item.setStr((short)(item.getStr() + str));
        item.setDex((short)(item.getDex() + dex));
        item.setInt((short)(item.getInt() + Int));
        item.setLuk((short)(item.getLuk() + luk));
        item.setHp((short)(item.getHp() + hp));
        item.setMp((short)(item.getMp() + mp));
        item.setAcc((short)((byte)(item.getAcc() + acc)));
        item.setAvoid((short)((byte)(item.getAvoid() + avoid)));
        MapleInventoryManipulator.removeFromSlot(this.getC(), MapleInventoryType.EQUIP, (short)1, 1L, true);
        MapleInventoryManipulator.addFromDrop(this.getChar().getClient(), item, false);
    }

    public String 显示装备属性() {
        StringBuilder name = new StringBuilder();
        Equip item = (Equip)this.c.getPlayer().getInventory(MapleInventoryType.EQUIP).getItem((short)1).copy();
        if (item.getUpgradeSlots() > 0) {
            name.append("升级次数:#b" + item.getUpgradeSlots() + "#k\r\n");
        }

        if (item.getWatk() > 0) {
            name.append("物理攻击力:#b" + item.getWatk() + "#k\r\n");
        }

        if (item.getMatk() > 0) {
            name.append("魔法攻击力:#b" + item.getMatk() + "#k\r\n");
        }

        if (item.getWdef() > 0) {
            name.append("物理防御力:#b" + item.getWdef() + "#k\r\n");
        }

        if (item.getMdef() > 0) {
            name.append("魔法防御力:#b" + item.getMdef() + "#k\r\n");
        }

        if (item.getStr() > 0) {
            name.append("力量:#b" + item.getStr() + "#k\r\n");
        }

        if (item.getDex() > 0) {
            name.append("敏捷:#b" + item.getDex() + "#k\r\n");
        }

        if (item.getLuk() > 0) {
            name.append("运气:#b" + item.getLuk() + "#k\r\n");
        }

        if (item.getInt() > 0) {
            name.append("智力:#b" + item.getInt() + "#k\r\n");
        }

        if (item.getHp() > 0) {
            name.append("HP:#b" + item.getHp() + "#k\r\n");
        }

        if (item.getMp() > 0) {
            name.append("MP:#b" + item.getMp() + "#k\r\n");
        }

        if (item.getAcc() > 0) {
            name.append("命中率:#b" + item.getAcc() + "#k\r\n");
        }

        if (item.getAvoid() > 0) {
            name.append("闪避率:#b" + item.getAvoid() + "#k\r\n");
        }

        if (item.getSpeed() > 0) {
            name.append("移动速度:#b" + item.getSpeed() + "#k\r\n");
        }

        return name.toString();
    }

    public void 获取装备栏物品代码() {
        this.c.getPlayer().dropMessage(5, "" + (Equip)this.c.getPlayer().getInventory(MapleInventoryType.EQUIP).getItem((short)1) + "");
    }

    public void 强化穿戴装备(int aa, int bb, int cc, int dd) {
        Equip item = (Equip)this.c.getPlayer().getInventory(MapleInventoryType.EQUIP).getItem((short)aa).copy();
    }

    public void 强化装备1(int aa, int bb, int cc, int dd) {
    }

    public void 加最大法力值(int mp, int a) {
        double 概率 = Math.ceil(Math.random() * 100.0);
        if (概率 <= (double)a) {
            Equip item = (Equip)this.c.getPlayer().getInventory(MapleInventoryType.EQUIP).getItem((short)1).copy();
            item.setMp((short)(item.getMp() + mp));
            MapleInventoryManipulator.removeFromSlot(this.getC(), MapleInventoryType.EQUIP, (short)1, 1L, true);
            MapleInventoryManipulator.addFromDrop(this.getChar().getClient(), item, false);
        } else {
            this.c.getPlayer().dropMessage(1, "强化失败。");
        }

    }

    public void 加最大法力值2(int mp) {
        double 随机 = Math.ceil(Math.random() * (double)mp);
        Equip item = (Equip)this.c.getPlayer().getInventory(MapleInventoryType.EQUIP).getItem((short)1).copy();
        item.setMp((short)((int)((double)item.getMp() + 随机)));
        MapleInventoryManipulator.removeFromSlot(this.getC(), MapleInventoryType.EQUIP, (short)1, 1L, true);
        MapleInventoryManipulator.addFromDrop(this.getChar().getClient(), item, false);
    }

    public void 加最大法力值2(int mp, int a) {
        double 随机 = Math.ceil(Math.random() * (double)mp);
        double 概率 = Math.ceil(Math.random() * 100.0);
        if (概率 <= (double)a) {
            Equip item = (Equip)this.c.getPlayer().getInventory(MapleInventoryType.EQUIP).getItem((short)1).copy();
            item.setMp((short)((int)((double)item.getMp() + 随机)));
            MapleInventoryManipulator.removeFromSlot(this.getC(), MapleInventoryType.EQUIP, (short)1, 1L, true);
            MapleInventoryManipulator.addFromDrop(this.getChar().getClient(), item, false);
        } else {
            this.c.getPlayer().dropMessage(1, "强化失败。");
        }

    }

    public void 装备耐久(int a, int b) {
        Equip item = (Equip)this.c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem((short)a).copy();
        item.setHpR((short)(item.getHpR() + b));
        MapleInventoryManipulator.removeFromSlot(this.getC(), MapleInventoryType.EQUIPPED, (short)a, 1L, true);
        MapleInventoryManipulator.addFromDrop(this.getChar().getClient(), item, false);
    }

    public void 强化规则(int a) {
        if (this.c.getPlayer().getInventory(MapleInventoryType.EQUIP).getItem((short)1) == null) {
            this.c.getPlayer().dropMessage(1, "装备栏第一格没有道具。");
        } else {
            Equip item = (Equip)this.c.getPlayer().getInventory(MapleInventoryType.EQUIP).getItem((short)1).copy();
            byte 可升级次数 = 0;
            int HP = 0;
            int MP = 0;
            int 力量 = 0;
            int 敏捷 = 0;
            int 智力 = 0;
            int 运气 = 0;
            int 物理攻击力 = 0;
            int 物理防御力 = 0;
            int 魔法攻击力 = 0;
            int 魔法防御力 = 0;
            int 回避率 = 0;
            int 命中率 = 0;
            int 跳跃力 = 0;
            int 移动速度 = 0;
            int 成功率 = 0;
            int 随机固定值 = 0;

            try {
                Connection con = DBConPool.getConnection();
                PreparedStatement ps = con.prepareStatement("SELECT * FROM Strengthening WHERE id2 = " + a + "");

                for(ResultSet rs = ps.executeQuery(); rs.next(); 随机固定值 = rs.getInt("随机值")) {
                    可升级次数 = (byte)rs.getInt("可升级次数");
                    HP = rs.getInt("HP");
                    MP = rs.getInt("MP");
                    力量 = rs.getInt("力量");
                    敏捷 = rs.getInt("敏捷");
                    智力 = rs.getInt("智力");
                    运气 = rs.getInt("运气");
                    物理攻击力 = rs.getInt("物理攻击力");
                    物理防御力 = rs.getInt("物理防御力");
                    魔法攻击力 = rs.getInt("魔法攻击力");
                    魔法防御力 = rs.getInt("魔法防御力");
                    回避率 = rs.getInt("回避率");
                    命中率 = rs.getInt("命中率");
                    跳跃力 = rs.getInt("跳跃力");
                    移动速度 = rs.getInt("移动速度");
                    成功率 = rs.getInt("成功率");
                }
            } catch (SQLException var23) {
            }

            if ((double)成功率 <= Math.ceil(Math.random() * 100.0)) {
                if (随机固定值 == 1) {
                    item.setUpgradeSlots((byte)(item.getUpgradeSlots() + 可升级次数));
                    item.setHp((short)(item.getHp() + HP));
                    item.setMp((short)(item.getMp() + MP));
                    item.setWatk((short)(item.getWatk() + 物理攻击力));
                    item.setWdef((short)(item.getWdef() + 物理防御力));
                    item.setMdef((short)(item.getMdef() + 魔法防御力));
                    item.setMatk((short)(item.getMatk() + 魔法攻击力));
                    item.setStr((short)(item.getStr() + 力量));
                    item.setDex((short)(item.getDex() + 敏捷));
                    item.setInt((short)(item.getInt() + 智力));
                    item.setLuk((short)(item.getLuk() + 运气));
                    item.setAcc((short)((byte)(item.getAcc() + 命中率)));
                    item.setAvoid((short)((byte)(item.getAvoid() + 回避率)));
                    item.setJump((short)((byte)(item.getJump() + 跳跃力)));
                    item.setSpeed((short)((byte)(item.getSpeed() + 移动速度)));
                    this.c.getPlayer().dropMessage(1, "[强化成功]:\r\nHP + " + HP + "MP + " + MP + "力量 + " + 力量 + "敏捷 + " + 敏捷 + "智力 + " + 智力 + "运气 + " + 运气 + "命中率 + " + 命中率 + "回避率 + " + 回避率 + "跳跃力 + " + 跳跃力 + "移动速度 + " + 移动速度 + "物理攻击力 + " + 物理攻击力 + "物理防御力 + " + 物理防御力 + "魔法攻击力 + " + 魔法攻击力 + "魔法防御力 + " + 魔法防御力 + "");
                    MapleInventoryManipulator.removeFromSlot(this.getC(), MapleInventoryType.EQUIP, (short)1, 1L, true);
                    MapleInventoryManipulator.addFromDrop(this.getChar().getClient(), item, false);
                } else {
                    item.setUpgradeSlots((byte)((int)((double)item.getUpgradeSlots() + Math.ceil(Math.random() * (double)可升级次数))));
                    item.setHp((short)((int)((double)item.getHp() + Math.ceil(Math.random() * (double)HP))));
                    item.setMp((short)((int)((double)item.getMp() + Math.ceil(Math.random() * (double)MP))));
                    item.setWatk((short)((int)((double)item.getWatk() + Math.ceil(Math.random() * (double)物理攻击力))));
                    item.setWdef((short)((int)((double)item.getWdef() + Math.ceil(Math.random() * (double)物理防御力))));
                    item.setMdef((short)((int)((double)item.getMdef() + Math.ceil(Math.random() * (double)魔法防御力))));
                    item.setMatk((short)((int)((double)item.getMatk() + Math.ceil(Math.random() * (double)魔法攻击力))));
                    item.setStr((short)((int)((double)item.getStr() + Math.ceil(Math.random() * (double)力量))));
                    item.setDex((short)((int)((double)item.getDex() + Math.ceil(Math.random() * (double)敏捷))));
                    item.setInt((short)((int)((double)item.getInt() + Math.ceil(Math.random() * (double)智力))));
                    item.setLuk((short)((int)((double)item.getLuk() + Math.ceil(Math.random() * (double)运气))));
                    item.setAcc((short)((byte)((int)((double)item.getAcc() + Math.ceil(Math.random() * (double)命中率)))));
                    item.setAvoid((short)((byte)((int)((double)item.getAvoid() + Math.ceil(Math.random() * (double)回避率)))));
                    item.setJump((short)((byte)((int)((double)item.getJump() + Math.ceil(Math.random() * (double)跳跃力)))));
                    item.setSpeed((short)((byte)((int)((double)item.getSpeed() + Math.ceil(Math.random() * (double)移动速度)))));
                    this.c.getPlayer().dropMessage(1, "[强化成功]:\r\nHP + " + HP + "MP + " + MP + "力量 + " + 力量 + "敏捷 + " + 敏捷 + "智力 + " + 智力 + "运气 + " + 运气 + "命中率 + " + 命中率 + "回避率 + " + 回避率 + "跳跃力 + " + 跳跃力 + "移动速度 + " + 移动速度 + "物理攻击力 + " + 物理攻击力 + "物理防御力 + " + 物理防御力 + "魔法攻击力 + " + 魔法攻击力 + "魔法防御力 + " + 魔法防御力 + "");
                }

                MapleInventoryManipulator.removeFromSlot(this.getC(), MapleInventoryType.EQUIP, (short)1, 1L, true);
                MapleInventoryManipulator.addFromDrop(this.getChar().getClient(), item, false);
            } else {
                this.c.getPlayer().dropMessage(1, "强化失败了");
            }

        }
    }

    public void 魔方(int upgr, int watk, int matk, int str, int dex, int Int, int luk, int hp, int mp, int acc, int avoid) {
        Equip item = (Equip)this.c.getPlayer().getInventory(MapleInventoryType.EQUIP).getItem((short)1).copy();
        item.setUpgradeSlots((byte)(item.getUpgradeSlots() + upgr));
        item.setWatk((short)(item.getWatk() + watk));
        item.setMatk((short)(item.getMatk() + matk));
        item.setStr((short)(item.getStr() + str));
        item.setDex((short)(item.getDex() + dex));
        item.setInt((short)(item.getInt() + Int));
        item.setLuk((short)(item.getLuk() + luk));
        item.setHp((short)(item.getHp() + hp));
        item.setMp((short)(item.getMp() + mp));
        item.setAcc((short)((byte)(item.getAcc() + acc)));
        item.setAvoid((short)((byte)(item.getAvoid() + avoid)));
        MapleInventoryManipulator.removeFromSlot(this.getC(), MapleInventoryType.EQUIP, (short)1, 1L, true);
        MapleInventoryManipulator.addFromDrop(this.getChar().getClient(), item, false);
    }
    public void xlkc(long days) {
        MapleQuestStatus marr = this.getPlayer().getQuestNoAdd(MapleQuest.getInstance(122700));
        if (marr != null && marr.getCustomData() != null && Long.parseLong(marr.getCustomData()) >= System.currentTimeMillis()) {
            this.getPlayer().dropMessage(1, "项链扩充失败，您已经进行过项链扩充。");
        } else {
            String customData = String.valueOf(System.currentTimeMillis() + days * 24L * 60L * 60L * 1000L);
            this.getPlayer().getQuestNAdd(MapleQuest.getInstance(122700)).setCustomData(customData);
            this.getPlayer().dropMessage(1, "项链" + days + "扩充扩充成功！");
        }

    }
    public void 克隆() {
        this.c.getPlayer().cloneLook();
    }

    public void 克隆(int damagePercentage) {
        this.c.getPlayer().cloneLook(damagePercentage);
    }

    public void 增加克隆(int quantity) {
        for(int i = 0; i < quantity; ++i) {
            this.克隆();
        }

    }

    public void 增加克隆(int quantity, int damagePercentage) {
        for(int i = 0; i < quantity; ++i) {
            this.克隆(damagePercentage);
        }

    }

    public void 减少克隆(int quantity) {
        this.c.getPlayer().decreaseClone(quantity);
    }

    public void 取消克隆() {
        this.c.getPlayer().disposeClones();
    }

    public int 读取克隆数量() {
        return this.c.getPlayer().getCloneLookQuantity();
    }
    public void 个人黄色字体(String message) {
        this.c.sendPacket(UIPacket.getTopMsg(message));
    }
    public void 加载镶嵌信息() {
    Start.GetFuMoInfo();
}

    public void 加载潜能列表() {
        Start.loadPotentialMap();
    }
    public int 打孔(short equipmentPosition) {
        if (equipmentPosition >= 0) {
            return 0;
        } else {
            String mxmxdDaKongFuMo = null;
            String sqlQuery1 = "SELECT b.mxmxd_dakong_fumo FROM inventoryitems a, inventoryequipment b WHERE a.inventoryitemid = b.inventoryitemid AND a.characterid = ? AND a.inventorytype = -1 AND a.position = ?";

            try {
                Connection con = DBConPool.getConnection();
                PreparedStatement ps = con.prepareStatement(sqlQuery1);
                ps.setInt(1, this.c.getPlayer().getId());
                ps.setInt(2, equipmentPosition);
                ResultSet rs = ps.executeQuery();
                Throwable var7 = null;

                try {
                    while(rs.next()) {
                        mxmxdDaKongFuMo = rs.getString("mxmxd_dakong_fumo");
                    }
                } catch (Throwable var17) {
                    var7 = var17;
                    throw var17;
                } finally {
                    if (rs != null) {
                        if (var7 != null) {
                            try {
                                rs.close();
                            } catch (Throwable var16) {
                                var7.addSuppressed(var16);
                            }
                        } else {
                            rs.close();
                        }
                    }

                }

                ps.close();
            } catch (SQLException var19) {
                服务端输出信息.println_err("打孔：查询查询装备的打孔数据出错：" + var19.getMessage());
                return 0;
            }

            if (mxmxdDaKongFuMo == null) {
                mxmxdDaKongFuMo = "";
            }

            int dakongCount = 0;
            if (mxmxdDaKongFuMo.length() > 0) {
                dakongCount = mxmxdDaKongFuMo.split(",").length;
            }

            if (dakongCount >= 20) {
                return 0;
            } else {
                this.c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem(equipmentPosition).setDaKongFuMo(mxmxdDaKongFuMo + "0:0,");
                this.c.getPlayer().道具存档();
                return 1;
            }
        }
    }


    public int setPotentialLevel(MapleCharacter chr, short equipmentPosition, short level) {
        return Potential.setPotentialLevel(chr, equipmentPosition, level);
    }

    public int setPotentialLevel(short equipmentPosition, short level) {
        return Potential.setPotentialLevel(this.c.getPlayer(), equipmentPosition, level);
    }

    public int setPotentialLevel(IItem item, short level) {
        return Potential.setPotentialLevel(item, level);
    }

    public int getPotentialLevel(IItem item) {
        return Potential.getPotentialLevel(item);
    }

    public int getPotentialLevel(MapleCharacter chr, short equipmentPosition) {
        return Potential.getPotentialLevel(chr, equipmentPosition);
    }

    public int getPotentialLevel(short equipmentPosition) {
        return Potential.getPotentialLevel(this.c.getPlayer(), equipmentPosition);
    }

    public int getPotentialQuantity(short equipmentPosition) {
        return Potential.getPotentialQuantity(this.c.getPlayer(), equipmentPosition);
    }

    public int getPotentialQuantity(IItem item) {
        return Potential.getPotentialQuantity(item);
    }

    public int setPotential(short equipmentPosition, short potentialPosition, int potentialType, int potentialValue) {
        return Potential.setPotential(this.c.getPlayer(), equipmentPosition, potentialPosition, potentialType, potentialValue);
    }

    public int setPotential(IItem item, short potentialPosition, int potentialType, int potentialValue) {
        return Potential.setPotential(item, potentialPosition, potentialType, potentialValue);
    }

    public Map<Integer, Integer> getPotentialMap(short equipmentPosition) {
        return Potential.getPotentialMap(this.c.getPlayer(), equipmentPosition);
    }

    public Map<Integer, Integer> getPotentialMap(MapleCharacter chr) {
        return Potential.getPotentialMap(chr);
    }

    public Map<Integer, Integer> getPotentialMap() {
        return Potential.getPotentialMap(this.c.getPlayer());
    }

    public Map<Integer, Integer> getPotentialMap(IItem item) {
        return Potential.getPotentialMap(item);
    }

    public ArrayList<Pair<Integer, Integer>> getPotentialList(IItem item) {
        return Potential.getPotentialList(item);
    }

    public ArrayList<Pair<Integer, Integer>> getPotentialList(short equipmentPosition) {
        return Potential.getPotentialList(this.c.getPlayer(), equipmentPosition);
    }

    public String getPotentialName(int potentialType) {
        return Potential.getPotentialName(potentialType);
    }

    public String getPotentialInfo(int potentialType) {
        return Potential.getPotentialInfo(potentialType);
    }

    public String getPotentialInfo(int potentialType, int potentialValue) {
        return Potential.getPotentialInfo(potentialType, potentialValue);
    }

    public int getPotentialValue(int potentialType, short equipmentPosition) {
        return Potential.getPotentialValue(this.c.getPlayer(), potentialType, equipmentPosition);
    }

    public int getPotentialValue(IItem item, int potentialType) {
        return Potential.getPotentialValue(item, potentialType);
    }

    public int getPotentialValue(MapleCharacter chr, int potentialType, short equipmentPosition) {
        return Potential.getPotentialValue(chr, potentialType, equipmentPosition);
    }

    public int getPotentialValue(int potentialType) {
        return Potential.getPotentialValue(this.c.getPlayer(), potentialType);
    }

    public int getPotentialValue(MapleCharacter chr, int potentialType) {
        return Potential.getPotentialValue(chr, potentialType);
    }


    public int 打孔2(short equipmentPosition) {
        if (equipmentPosition >= 0) {
            return 0;
        } else {
            String mxmxdDaKongFuMo = null;
            String sqlQuery1 = "SELECT b.mxmxd_dakong_fumo FROM inventoryitems a, inventoryequipment b WHERE a.inventoryitemid = b.inventoryitemid AND a.characterid = ? AND a.inventorytype = -1 AND a.position = ?";

            try {
                Connection con = DBConPool.getConnection();
                PreparedStatement ps = con.prepareStatement(sqlQuery1);
                ps.setInt(1, this.c.getPlayer().getId());
                ps.setInt(2, equipmentPosition);
                ResultSet rs = ps.executeQuery();
                Throwable var7 = null;

                try {
                    while(rs.next()) {
                        mxmxdDaKongFuMo = rs.getString("mxmxd_dakong_fumo");
                    }
                } catch (Throwable var17) {
                    var7 = var17;
                    throw var17;
                } finally {
                    if (rs != null) {
                        if (var7 != null) {
                            try {
                                rs.close();
                            } catch (Throwable var16) {
                                var7.addSuppressed(var16);
                            }
                        } else {
                            rs.close();
                        }
                    }

                }

                ps.close();
            } catch (SQLException var19) {
                服务端输出信息.println_err("打孔：查询查询装备的打孔数据出错：" + var19.getMessage());
                return 0;
            }

            if (mxmxdDaKongFuMo == null) {
                mxmxdDaKongFuMo = "";
            }

            int dakongCount = 0;
            if (mxmxdDaKongFuMo.length() > 0) {
                dakongCount = mxmxdDaKongFuMo.split(",").length;
            }

            if (dakongCount >= 3) {
                return 0;
            } else {
                this.c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem(equipmentPosition).setDaKongFuMo(mxmxdDaKongFuMo + "0:0,");
                this.c.getPlayer().saveToDB(false, false, true);
                return 1;
            }
        }
    }

    private static String replaceFirst2(String source, String target, String replacement) {
        int index = source.indexOf(target);
        return index == -1 ? source : source.substring(0, index).concat(replacement).concat(source.substring(index + target.length()));
    }

    public int 镶嵌(short equipmentPosition, int fuMoType, int fuMoValue) {
        if (equipmentPosition >= 0) {
            return 0;
        } else {
            String mxmxdDaKongFuMo = null;
            String sqlQuery1 = "SELECT b.mxmxd_dakong_fumo FROM inventoryitems a, inventoryequipment b WHERE a.inventoryitemid = b.inventoryitemid AND a.characterid = ? AND a.inventorytype = -1 AND a.position = ?";

            try {
                Connection con = DBConPool.getConnection();
                PreparedStatement ps = con.prepareStatement(sqlQuery1);
                ps.setInt(1, this.c.getPlayer().getId());
                ps.setInt(2, equipmentPosition);
                ResultSet rs = ps.executeQuery();
                Throwable var9 = null;

                try {
                    while(rs.next()) {
                        mxmxdDaKongFuMo = rs.getString("mxmxd_dakong_fumo");
                    }
                } catch (Throwable var19) {
                    var9 = var19;
                    throw var19;
                } finally {
                    if (rs != null) {
                        if (var9 != null) {
                            try {
                                rs.close();
                            } catch (Throwable var18) {
                                var9.addSuppressed(var18);
                            }
                        } else {
                            rs.close();
                        }
                    }

                }

                ps.close();
            } catch (SQLException var21) {
                服务端输出信息.println_err("镶嵌：查询装备的打孔镶嵌数据出错：" + var21.getMessage());
                return 0;
            }

            if (mxmxdDaKongFuMo != null && mxmxdDaKongFuMo.length() != 0) {
                mxmxdDaKongFuMo = replaceFirst2(mxmxdDaKongFuMo, "0:0,", String.format("%s:%s,", fuMoType, fuMoValue));
                this.c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem(equipmentPosition).setDaKongFuMo(mxmxdDaKongFuMo);
                String 输出 = "";
                switch (fuMoType) {
                    case 1:
                        输出 = "[镶嵌系统] : 恭喜玩家 " + this.c.getPlayer().getName() + " 镶嵌成功。增加对普通怪物 " + fuMoValue + "% 的伤害。";
                        break;
                    case 2:
                        输出 = "[镶嵌系统] : 恭喜玩家 " + this.c.getPlayer().getName() + " 镶嵌成功。增加对高级怪物 " + fuMoValue + "% 的伤害。";
                        break;
                    case 3:
                        输出 = "[镶嵌系统] : 恭喜玩家 " + this.c.getPlayer().getName() + " 镶嵌成功。增加对所有怪物 " + fuMoValue + "% 的伤害。";
                        break;
                    case 4:
                        输出 = "[镶嵌系统] : 恭喜玩家 " + this.c.getPlayer().getName() + " 镶嵌成功。增加对普通怪物 " + fuMoValue + "% 的一击必杀率。";
                        break;
                    case 5:
                        输出 = "[镶嵌系统] : 恭喜玩家 " + this.c.getPlayer().getName() + " 镶嵌成功。增加对高级怪物 " + fuMoValue + "% 的一击必杀率。";
                        break;
                    case 6:
                        输出 = "[镶嵌系统] : 恭喜玩家 " + this.c.getPlayer().getName() + " 镶嵌成功。增加对所有怪物 " + fuMoValue + "% 的一击必杀率。";
                        break;
                    case 7:
                        输出 = "[镶嵌系统] : 恭喜玩家 " + this.c.getPlayer().getName() + " 镶嵌成功。增加 " + fuMoValue + "% 的一击必杀值。";
                        break;
                    case 8:
                        输出 = "[镶嵌系统] : 恭喜玩家 " + this.c.getPlayer().getName() + " 镶嵌成功。增加 " + fuMoValue + "% 几率让怪物血量只剩1。";
                        break;
                    case 9:
                        输出 = "[镶嵌系统] : 恭喜玩家 " + this.c.getPlayer().getName() + " 镶嵌成功。攻击怪物时，" + fuMoValue + "% 几率怪物不会有仇恨。";
                        break;
                    case 10:
                        输出 = "[镶嵌系统] : 恭喜玩家 " + this.c.getPlayer().getName() + " 镶嵌成功。攻击时附加 " + fuMoValue + " 点真实伤害。";
                        break;
                    case 21:
                        输出 = "[镶嵌系统] : 恭喜玩家 " + this.c.getPlayer().getName() + " 镶嵌成功。减少角色 " + fuMoValue + "% 受到的伤害。";
                        break;
                    case 22:
                        输出 = "[镶嵌系统] : 恭喜玩家 " + this.c.getPlayer().getName() + " 镶嵌成功。减少角色 " + fuMoValue + " 点受到的伤害。";
                        break;
                    case 23:
                        输出 = "[镶嵌系统] : 恭喜玩家 " + this.c.getPlayer().getName() + " 镶嵌成功。受到的伤害大于最大生命值 60% 时，会减少 " + fuMoValue + "% 的伤害。";
                        break;
                    case 24:
                        输出 = "[镶嵌系统] : 恭喜玩家 " + this.c.getPlayer().getName() + " 镶嵌成功。用超级药水抵消受到的伤害。";
                        break;
                    case 31:
                        输出 = "[镶嵌系统] : 恭喜玩家 " + this.c.getPlayer().getName() + " 镶嵌成功。增加 " + fuMoValue + "% 的狩猎经验获取。";
                        break;
                    case 32:
                        输出 = "[镶嵌系统] : 恭喜玩家 " + this.c.getPlayer().getName() + " 镶嵌成功。增加 " + fuMoValue + "% 的泡点经验获取。";
                        break;
                    case 33:
                        输出 = "[镶嵌系统] : 恭喜玩家 " + this.c.getPlayer().getName() + " 镶嵌成功。增加 " + fuMoValue + "% 的泡点经验获取。";
                        break;
                    case 34:
                        输出 = "[镶嵌系统] : 恭喜玩家 " + this.c.getPlayer().getName() + " 镶嵌成功。被诅咒状态下增加 5 倍狩猎经验。";
                        break;
                    case 100:
                        输出 = "[镶嵌系统] : 恭喜玩家 " + this.c.getPlayer().getName() + " 镶嵌成功。增加 " + fuMoValue + " 点异常抗性。";
                        break;
                    case 101:
                        输出 = "[镶嵌系统] : 恭喜玩家 " + this.c.getPlayer().getName() + " 镶嵌成功。增加 " + fuMoValue + " 点异常免疫。";
                        break;
                    case 200:
                        输出 = "[镶嵌系统] : 恭喜玩家 " + this.c.getPlayer().getName() + " 镶嵌成功。使用BUFF技能 " + fuMoValue + "% 几率清空所有冷却。";
                        break;
                    case 300:
                        输出 = "[镶嵌系统] : 恭喜玩家 " + this.c.getPlayer().getName() + " 镶嵌成功。升级时额外增加 " + fuMoValue + " 点 MaxHP 。";
                        break;
                    case 301:
                        输出 = "[镶嵌系统] : 恭喜玩家 " + this.c.getPlayer().getName() + " 镶嵌成功。升级时额外增加 " + fuMoValue + " 点 MaxMP。";
                        break;
                    case 302:
                        输出 = "[镶嵌系统] : 恭喜玩家 " + this.c.getPlayer().getName() + " 镶嵌成功。升级时额外增加 " + fuMoValue + " 点 MaxHP，MaxMP。";
                        break;
                    case 303:
                        输出 = "[镶嵌系统] : 恭喜玩家 " + this.c.getPlayer().getName() + " 镶嵌成功。升级时 " + fuMoValue + " % 几率获得额外1级，不获得升级属性。";
                        break;
                    case 400:
                        输出 = "[镶嵌系统] : 恭喜玩家 " + this.c.getPlayer().getName() + " 镶嵌成功。受到伤害时 " + fuMoValue + " % 几率发动 10 级稳如泰山。";
                        break;
                    case 401:
                        输出 = "[镶嵌系统] : 恭喜玩家 " + this.c.getPlayer().getName() + " 镶嵌成功。受到伤害时 " + fuMoValue + " % 几率发动 10 级愤怒之火。";
                        break;
                    case 500:
                        输出 = "[镶嵌系统] : 恭喜玩家 " + this.c.getPlayer().getName() + " 镶嵌成功。增加召唤兽 " + fuMoValue + " % 对普通怪物的伤害。";
                        break;
                    case 501:
                        输出 = "[镶嵌系统] : 恭喜玩家 " + this.c.getPlayer().getName() + " 镶嵌成功。增加召唤兽 " + fuMoValue + " % 对高怪物的伤害。";
                        break;
                    case 502:
                        输出 = "[镶嵌系统] : 恭喜玩家 " + this.c.getPlayer().getName() + " 镶嵌成功。召唤兽攻击时附加 " + fuMoValue + "  点真实伤害。";
                        break;
                    case 503:
                        输出 = "[镶嵌系统] : 恭喜玩家 " + this.c.getPlayer().getName() + " 镶嵌成功。增加召唤兽和玩家 " + fuMoValue + " % 对所有怪物的伤害。";
                        break;
                    case 1000:
                        输出 = "[镶嵌系统] : 恭喜玩家 " + this.c.getPlayer().getName() + " 镶嵌成功。全属性增加 " + fuMoValue + " 点。";
                        break;
                    case 1111002:
                        输出 = "[镶嵌系统] : 恭喜玩家 " + this.c.getPlayer().getName() + " 镶嵌成功。技能[斗气集中] 值为 (" + fuMoValue + ") ";
                        break;
                    case 1211002:
                        输出 = "[镶嵌系统] : 恭喜玩家 " + this.c.getPlayer().getName() + " 镶嵌成功。技能[属性攻击] 值为 (" + fuMoValue + ") ";
                        break;
                    case 1311005:
                        输出 = "[镶嵌系统] : 恭喜玩家 " + this.c.getPlayer().getName() + " 镶嵌成功。技能[龙之献祭] 值为 (" + fuMoValue + ") ";
                        break;
                    case 2111002:
                        输出 = "[镶嵌系统] : 恭喜玩家 " + this.c.getPlayer().getName() + " 镶嵌成功。技能[末日烈焰] 值为 (" + fuMoValue + ") ";
                        break;
                    case 2211003:
                        输出 = "[镶嵌系统] : 恭喜玩家 " + this.c.getPlayer().getName() + " 镶嵌成功。技能[落雷枪] 值为 (" + fuMoValue + ") ";
                        break;
                    case 2311004:
                        输出 = "[镶嵌系统] : 恭喜玩家 " + this.c.getPlayer().getName() + " 镶嵌成功。技能[圣光] 值为 (" + fuMoValue + ") ";
                        break;
                    case 3111006:
                        输出 = "[镶嵌系统] : 恭喜玩家 " + this.c.getPlayer().getName() + " 镶嵌成功。技能[箭扫射-弓] 值为 (" + fuMoValue + ") ";
                        break;
                    case 3211006:
                        输出 = "[镶嵌系统] : 恭喜玩家 " + this.c.getPlayer().getName() + " 镶嵌成功。技能[箭扫射-弩] 值为 (" + fuMoValue + ") ";
                        break;
                    case 4111005:
                        输出 = "[镶嵌系统] : 恭喜玩家 " + this.c.getPlayer().getName() + " 镶嵌成功。技能[多重飞镖] 值为 (" + fuMoValue + ") ";
                        break;
                    case 4211002:
                        输出 = "[镶嵌系统] : 恭喜玩家 " + this.c.getPlayer().getName() + " 镶嵌成功。技能[落叶斩] 值为 (" + fuMoValue + ") ";
                        break;
                    case 5111005:
                        输出 = "[镶嵌系统] : 恭喜玩家 " + this.c.getPlayer().getName() + " 镶嵌成功。技能[超人变身] 值为 (" + fuMoValue + ") ";
                        break;
                    case 5211004:
                        输出 = "[镶嵌系统] : 恭喜玩家 " + this.c.getPlayer().getName() + " 镶嵌成功。技能[双枪喷射] 值为 (" + fuMoValue + ") ";
                }

                Broadcast.broadcastMessage(MaplePacketCreator.serverNotice(6, 输出));
                this.c.getPlayer().道具存档();
                return 1;
            } else {
                return 0;
            }
        }
    }

    public String 显示镶嵌效果() {
        StringBuilder name = new StringBuilder();
        if (this.c.getPlayer().getEquippedFuMoMap().get(FumoSkill.FM("兵不血刃")) != null) {
            int 增加 = 10000000 + 100000 * (Integer)this.c.getPlayer().getEquippedFuMoMap().get(FumoSkill.FM("兵不血刃"));
            name.append("\t[#e#b必杀值#k#n] : #r").append(增加).append("#k\r\n");
        } else {
            name.append("\t[#e#b必杀值#k#n] : #r10000000#k\r\n");
        }

        if (this.c.getPlayer().getEquippedFuMoMap().get(FumoSkill.FM("异常抗性")) != null) {
            name.append("\t[#e#b异常抗性#k#n] : 增加对异常状态抗性，减少持续 #r").append(this.c.getPlayer().getEquippedFuMoMap().get(FumoSkill.FM("异常抗性"))).append("#k % 的异常状态持续时间\r\n");
        }

        if (this.c.getPlayer().getEquippedFuMoMap().get(1) != null) {
            name.append("\t[#e#b强攻#k#n] : 增加 #r").append(this.c.getPlayer().getEquippedFuMoMap().get(1)).append("#k % 对普通怪物的伤害\r\n");
        }

        if (this.c.getPlayer().getEquippedFuMoMap().get(2) != null) {
            name.append("\t[#e#b超强攻#k#n] : 增加 #r").append(this.c.getPlayer().getEquippedFuMoMap().get(2)).append("#k % 对高级怪物的伤害\r\n");
        }

        if (this.c.getPlayer().getEquippedFuMoMap().get(3) != null) {
            name.append("\t[#e#b战争意志#k#n] : 增加 #r").append(this.c.getPlayer().getEquippedFuMoMap().get(3)).append("#k % 对所有怪物的伤害\r\n");
        }

        if (this.c.getPlayer().getEquippedFuMoMap().get(FumoSkill.FM("鹰眼")) != null) {
            name.append("\t[#e#b鹰眼#k#n] : 对普通怪物 #r").append(this.c.getPlayer().getEquippedFuMoMap().get(FumoSkill.FM("鹰眼"))).append("#k % 几率一击必杀\r\n");
        }

        if (this.c.getPlayer().getEquippedFuMoMap().get(FumoSkill.FM("锐眼")) != null) {
            name.append("\t[#e#b锐眼#k#n] : 对高级怪物 #r").append(this.c.getPlayer().getEquippedFuMoMap().get(FumoSkill.FM("锐眼"))).append("#k % 几率一击必杀\r\n");
        }

        if (this.c.getPlayer().getEquippedFuMoMap().get(FumoSkill.FM("谢幕")) != null) {
            name.append("\t[#e#b谢幕#k#n] : 对所有怪物 #r").append(this.c.getPlayer().getEquippedFuMoMap().get(FumoSkill.FM("谢幕"))).append("#k % 几率一击必杀\r\n");
        }

        if (this.c.getPlayer().getEquippedFuMoMap().get(8) != null) {
            name.append("\t[#e#b致命打击#k#n] : 攻击怪物 #r").append(this.c.getPlayer().getEquippedFuMoMap().get(8)).append("#k % 概率让怪物血量只剩1\r\n");
        }

        if (this.c.getPlayer().getEquippedFuMoMap().get(9) != null) {
            name.append("\t[#e#b蒙蔽#k#n] : 攻击怪物时 #r").append(this.c.getPlayer().getEquippedFuMoMap().get(9)).append("#k % 几率怪物不会有仇恨\r\n");
        }

        if (this.c.getPlayer().getEquippedFuMoMap().get(10) != null) {
            name.append("\t[#e#b追击#k#n] : 攻击时附加 #r").append(this.c.getPlayer().getEquippedFuMoMap().get(10)).append("#k 点真实伤害\r\n");
        }

        if (this.c.getPlayer().getEquippedFuMoMap().get(FumoSkill.FM("坚韧")) != null) {
            name.append("\t[#e#b坚韧#k#n] : 减少 #r").append(this.c.getPlayer().getEquippedFuMoMap().get(FumoSkill.FM("坚韧"))).append("#k % 受到的伤害\r\n");
        }

        if (this.c.getPlayer().getEquippedFuMoMap().get(22) != null) {
            name.append("\t[#e#b坚不可摧#k#n] : 减少 #r").append(this.c.getPlayer().getEquippedFuMoMap().get(22)).append("#k 点受到的伤害\r\n");
        }

        if (this.c.getPlayer().getEquippedFuMoMap().get(23) != null) {
            name.append("\t[#e#b顽固#k#n] : 受到的伤害大于你最大生命值 #b60%#k 时，会减少 #r").append(this.c.getPlayer().getEquippedFuMoMap().get(23)).append("#k % 受到的伤害\r\n");
        }

        if (this.c.getPlayer().getEquippedFuMoMap().get(24) != null) {
            name.append("\t[#e#b未卜先知#k#n] : 用超级药水抵消受到的伤害\r\n");
        }

        if (this.c.getPlayer().getEquippedFuMoMap().get(FumoSkill.FM("幸运狩猎")) != null) {
            name.append("\t[#e#b幸运狩猎#k#n] : 增加 #r").append(this.c.getPlayer().getEquippedFuMoMap().get(FumoSkill.FM("幸运狩猎"))).append("#k % 狩猎经验\r\n");
        }

        if (this.c.getPlayer().getEquippedFuMoMap().get(FumoSkill.FM("苦中作乐")) != null) {
            name.append("\t[#e#b苦中作乐#k#n] : 被诅咒状态下增加 #r5#k 倍狩猎经验\r\n");
        }

        if (this.c.getPlayer().getEquippedFuMoMap().get(FumoSkill.FM("闲来好运")) != null) {
            name.append("\t[#e#b闲来好运#k#n] : 增加 #r").append(this.c.getPlayer().getEquippedFuMoMap().get(FumoSkill.FM("闲来好运"))).append("#k % 泡点经验\r\n");
        }

        if (this.c.getPlayer().getEquippedFuMoMap().get(FumoSkill.FM("财源滚滚")) != null) {
            name.append("\t[#e#b财源滚滚#k#n] : 增加 #r").append(this.c.getPlayer().getEquippedFuMoMap().get(FumoSkill.FM("财源滚滚"))).append("#k % 泡点金币\r\n");
        }

        if (this.c.getPlayer().getEquippedFuMoMap().get(FumoSkill.FM("神圣祈祷ex")) != null) {
            name.append("\t[#e#b神圣祈祷ex#k#n] : 神圣祈祷技能增加 #r").append(this.c.getPlayer().getEquippedFuMoMap().get(FumoSkill.FM("神圣祈祷ex"))).append("#k % 经验加成\r\n");
        }

        if (this.c.getPlayer().getEquippedFuMoMap().get(200) != null) {
            name.append("\t[#e#b伺机待发#k#n] : 使用BUFF技能时 #r").append(this.c.getPlayer().getEquippedFuMoMap().get(200)).append("#k % 的几率重置所有已经进入冷却的技能\r\n");
        }

        if (this.c.getPlayer().getEquippedFuMoMap().get(300) != null) {
            name.append("\t[#e#b茁壮生命#k#n] : 升级时获得额外 #r").append(this.c.getPlayer().getEquippedFuMoMap().get(300)).append("#k 点 MaxHP\r\n");
        }

        if (this.c.getPlayer().getEquippedFuMoMap().get(301) != null) {
            name.append("\t[#e#b茁壮魔力#k#n] : 升级时获得额外 #r").append(this.c.getPlayer().getEquippedFuMoMap().get(301)).append("#k 点 MaxMP\r\n");
        }

        if (this.c.getPlayer().getEquippedFuMoMap().get(302) != null) {
            name.append("\t[#e#b茁壮生长#k#n] : 升级时获得额外 #r").append(this.c.getPlayer().getEquippedFuMoMap().get(302)).append("#k 点MaxMP.MaxHP\r\n");
        }

        if (this.c.getPlayer().getEquippedFuMoMap().get(303) != null) {
            name.append("\t[#e#b拔苗助长#k#n] : 升级时 #r").append(this.c.getPlayer().getEquippedFuMoMap().get(303)).append("#k % 几率获得额外1级，不获得升级属性\r\n");
        }

        if (this.c.getPlayer().getEquippedFuMoMap().get(400) != null) {
            name.append("\t[#e#b稳如泰山#k#n] : 被攻击时 #r").append(this.c.getPlayer().getEquippedFuMoMap().get(400)).append("#k % 几率发动 10 级稳如泰山\r\n");
        }

        if (this.c.getPlayer().getEquippedFuMoMap().get(401) != null) {
            name.append("\t[#e#b愤怒之火#k#n] : 被攻击时 #r").append(this.c.getPlayer().getEquippedFuMoMap().get(401)).append("#k % 几率发动 10 级愤怒之火\r\n");
        }

        if (this.c.getPlayer().getEquippedFuMoMap().get(500) != null) {
            name.append("\t[#e#b训练有方#k#n] : 增加召唤兽 #r").append(this.c.getPlayer().getEquippedFuMoMap().get(500)).append("#k % 对普通怪物的伤害\r\n");
        }

        if (this.c.getPlayer().getEquippedFuMoMap().get(501) != null) {
            name.append("\t[#e#b训练有素#k#n] : 增加召唤兽 #r").append(this.c.getPlayer().getEquippedFuMoMap().get(501)).append("#k % 对高级怪物的伤害\r\n");
        }

        if (this.c.getPlayer().getEquippedFuMoMap().get(502) != null) {
            name.append("\t[#e#b迅捷突袭#k#n] : 召唤兽攻击时附加 #r").append(this.c.getPlayer().getEquippedFuMoMap().get(502)).append("#k 点真实伤害 \r\n");
        }

        if (this.c.getPlayer().getEquippedFuMoMap().get(503) != null) {
            name.append("\t[#e#b心有灵犀#k#n] : 增加召唤兽和玩家 #r").append(this.c.getPlayer().getEquippedFuMoMap().get(503)).append("#k % 对所有怪物的伤害 \r\n");
        }

        if (this.c.getPlayer().getEquippedFuMoMap().get(4211002) != null) {
            name.append("\t[#e#b落叶斩#k#n] : 攻击时根据拥有的枫叶数量增加基础伤害，增加率为 #r").append(this.c.getPlayer().getEquippedFuMoMap().get(4211002)).append("#k %\r\n");
        }

        if (this.c.getPlayer().getEquippedFuMoMap().get(4111005) != null) {
            name.append("\t[#e#b多重飞镖#k#n] : 攻击时根据拥有的飞镖数量增加基础伤害，增加率为 #r").append(this.c.getPlayer().getEquippedFuMoMap().get(4111005)).append("#k %\r\n");
        }

        if (this.c.getPlayer().getEquippedFuMoMap().get(1111002) != null) {
            name.append("\t[#e#b斗气集中#k#n] : 斗气状态下增加 #r").append(this.c.getPlayer().getEquippedFuMoMap().get(1111002)).append("#k % 基础伤害\r\n");
        }

        if (this.c.getPlayer().getEquippedFuMoMap().get(1211002) != null) {
            name.append("\t[#e#b属性攻击#k#n] : 属性攻击技能，根据状态调整。 烈焰状态增加基础伤害，寒冰状态增加吸血能力，雷鸣状态增加累积伤害，神圣状态增加秒杀率。参考值 #r").append(this.c.getPlayer().getEquippedFuMoMap().get(1211002)).append("#k\r\n");
        }

        if (this.c.getPlayer().getEquippedFuMoMap().get(1311005) != null) {
            name.append("\t[#e#b龙之献祭#k#n] : 根据自身目前血量，增加龙之献祭伤害，使用 5 次龙之献祭后，龙咆哮将会增加 #r").append(this.c.getPlayer().getEquippedFuMoMap().get(1311005)).append("#k %基础伤害\r\n");
        }

        if (this.c.getPlayer().getEquippedFuMoMap().get(2111002) != null) {
            name.append("\t[#e#b末日烈焰#k#n] : 增加 #r").append(this.c.getPlayer().getEquippedFuMoMap().get(2111002)).append("#k % 基础伤害，每次伤害附加上一次伤害值\r\n");
        }

        if (this.c.getPlayer().getEquippedFuMoMap().get(2211003) != null) {
            name.append("\t[#e#b落雷枪#k#n] : 增加 #r").append(this.c.getPlayer().getEquippedFuMoMap().get(2211003)).append("#k % 基础伤害，并增加一定量的秒杀率\r\n");
        }

        if (this.c.getPlayer().getEquippedFuMoMap().get(2311004) != null) {
            name.append("\t[#e#b圣光#k#n] : 增加 #r").append(this.c.getPlayer().getEquippedFuMoMap().get(2311004)).append("#k % 基础伤害，使用后获取一次概率伤害免疫，最多可储存两次\r\n");
        }

        if (this.c.getPlayer().getEquippedFuMoMap().get(3111006) != null) {
            name.append("\t[#e#b箭扫射-弓#k#n] : 增加 #r").append(this.c.getPlayer().getEquippedFuMoMap().get(3111006)).append("#k % 基础伤害\r\n");
        }

        if (this.c.getPlayer().getEquippedFuMoMap().get(3211006) != null) {
            name.append("\t[#e#b箭扫射-弩#k#n] : 增加 #r").append(this.c.getPlayer().getEquippedFuMoMap().get(3211006)).append("#k % 基础伤害。并增加一定量的秒杀率\r\n");
        }

        if (this.c.getPlayer().getEquippedFuMoMap().get(5111005) != null) {
            name.append("\t[#e#b超人变身#k#n] : 增加变身状态的下 #r").append(this.c.getPlayer().getEquippedFuMoMap().get(5111005)).append("#k % 基础伤害，最终伤害\r\n");
        }

        if (this.c.getPlayer().getEquippedFuMoMap().get(5211004) != null) {
            name.append("\t[#e#b双枪喷射#k#n] : 烈焰喷射，寒冰喷射增加 #r").append(this.c.getPlayer().getEquippedFuMoMap().get(5211004)).append("#k % 基础伤害，最终伤害\r\n");
        }

        return name.toString();
    }

    public static int appearNumber(String srcText, String findText) {
        int count = 0;

        for(int index = 0; (index = srcText.indexOf(findText, index)) != -1; ++count) {
            index += findText.length();
        }

        return count;
    }

    public int 查询身上装备已打孔数(short equipmentPosition) {
        if (equipmentPosition >= 0) {
            return 0;
        } else {
            String mxmxdDaKongFuMo = null;
            String sqlQuery1 = "SELECT b.mxmxd_dakong_fumo FROM inventoryitems a, inventoryequipment b WHERE a.inventoryitemid = b.inventoryitemid AND a.characterid = ? AND a.inventorytype = -1 AND a.position = ?";

            try {
                Connection con = DBConPool.getConnection();
                PreparedStatement ps = con.prepareStatement(sqlQuery1);
                ps.setInt(1, this.c.getPlayer().getId());
                ps.setInt(2, equipmentPosition);
                ResultSet rs = ps.executeQuery();
                Throwable var7 = null;

                try {
                    while(rs.next()) {
                        mxmxdDaKongFuMo = rs.getString("mxmxd_dakong_fumo");
                    }
                } catch (Throwable var17) {
                    var7 = var17;
                    throw var17;
                } finally {
                    if (rs != null) {
                        if (var7 != null) {
                            try {
                                rs.close();
                            } catch (Throwable var16) {
                                var7.addSuppressed(var16);
                            }
                        } else {
                            rs.close();
                        }
                    }

                }

                ps.close();
            } catch (SQLException var19) {
                服务端输出信息.println_err("查询身上装备已打孔数：查询装备的打孔数据出错：" + var19.getMessage());
                return 0;
            }

            if (mxmxdDaKongFuMo == null) {
                mxmxdDaKongFuMo = "";
            }

            return appearNumber(mxmxdDaKongFuMo, ",");
        }
    }

    public int 查询身上装备可镶嵌数(short equipmentPosition) {
        if (equipmentPosition >= 0) {
            return 0;
        } else {
            String mxmxdDaKongFuMo = null;
            String sqlQuery1 = "SELECT b.mxmxd_dakong_fumo FROM inventoryitems a, inventoryequipment b WHERE a.inventoryitemid = b.inventoryitemid AND a.characterid = ? AND a.inventorytype = -1 AND a.position = ?";

            try {
                Connection con = DBConPool.getConnection();
                PreparedStatement ps = con.prepareStatement(sqlQuery1);
                ps.setInt(1, this.c.getPlayer().getId());
                ps.setInt(2, equipmentPosition);
                ResultSet rs = ps.executeQuery();
                Throwable var7 = null;

                try {
                    while(rs.next()) {
                        mxmxdDaKongFuMo = rs.getString("mxmxd_dakong_fumo");
                    }
                } catch (Throwable var17) {
                    var7 = var17;
                    throw var17;
                } finally {
                    if (rs != null) {
                        if (var7 != null) {
                            try {
                                rs.close();
                            } catch (Throwable var16) {
                                var7.addSuppressed(var16);
                            }
                        } else {
                            rs.close();
                        }
                    }

                }

                ps.close();
            } catch (SQLException var19) {
                服务端输出信息.println_err("查询身上装备可镶嵌数：查询装备的打孔镶嵌数据出错：" + var19.getMessage());
                return 0;
            }

            if (mxmxdDaKongFuMo == null) {
                mxmxdDaKongFuMo = "";
            }

            return appearNumber(mxmxdDaKongFuMo, "0:0,");
        }
    }

    public int 清洗身上装备镶嵌(short equipmentPosition) {
        if (equipmentPosition >= 0) {
            return 0;
        } else {
            Connection con = DBConPool.getConnection();
            int dakongCount = this.查询身上装备已打孔数(equipmentPosition);
            StringBuilder sb = new StringBuilder();

            for(int i = 1; i <= dakongCount; ++i) {
                sb.append("0:0,");
            }

            this.c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem(equipmentPosition).setDaKongFuMo(sb.toString());
            this.c.getPlayer().saveToDB(false, false, true);
            return 1;
        }
    }

    public int 清洗(short equipmentPosition) {
        if (equipmentPosition >= 0) {
            return 0;
        } else {
            int dakongCount = this.查询身上装备已打孔数(equipmentPosition);
            StringBuilder sb = new StringBuilder();

            for(int i = 1; i <= dakongCount; ++i) {
                sb.append("0:0,");
            }

            this.c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem(equipmentPosition).setDaKongFuMo(sb.toString());
            this.c.getPlayer().saveToDB(false, false, true);
            return 1;
        }
    }

    public void Gainrobot(String Name, int Channale, int Piot) {
        try {
            int ret = this.Getrobot(Name, Channale);
            if (ret == -1) {
                ret = 0;
                PreparedStatement ps = null;

                try {
                    Connection con = DBConPool.getConnection();
                    ps = con.prepareStatement("INSERT INTO robot (channel, Name,Point) VALUES (?, ?, ?)");
                    ps.setInt(1, Channale);
                    ps.setString(2, Name);
                    ps.setInt(3, ret);
                    ps.execute();
                } catch (SQLException var16) {
                    服务端输出信息.println_out("xxxxxxxx:" + var16);
                } finally {
                    try {
                        if (ps != null) {
                            ps.close();
                        }
                    } catch (SQLException var15) {
                        服务端输出信息.println_out("xxxxxxxxzzzzzzz:" + var15);
                    }

                }
            }

            ret += Piot;
            Connection con = DBConPool.getConnection();
            PreparedStatement ps = con.prepareStatement("UPDATE robot SET `Point` = ? WHERE Name = ? and channel = ?");
            ps.setInt(1, ret);
            ps.setString(2, Name);
            ps.setInt(3, Channale);
            ps.execute();
            ps.close();
        } catch (SQLException var18) {
            服务端输出信息.println_err("Getrobot!!55" + var18);
        }

    }

    public int Getrobot(String Name, int Channale) {
        int ret = -1;

        try {
            Connection con = DBConPool.getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT * FROM robot WHERE channel = ? and Name = ?");
            ps.setInt(1, Channale);
            ps.setString(2, Name);
            ResultSet rs = ps.executeQuery();
            rs.next();
            ret = rs.getInt("Point");
            rs.close();
            ps.close();
        } catch (SQLException var7) {
        }

        return ret;
    }

    public void 分身2() {
        if (this.c.getPlayer().getCloneSize() <= this.Getrobot("" + this.c.getPlayer().getId() + "", 1)) {
            this.c.getPlayer().cloneLook1();
        } else {
            this.c.getPlayer().dropMessage(5, "无法继续召唤分身。");
        }

    }

    public void 分身1() {
        if (this.c.getPlayer().getCloneSize() <= this.Getrobot("" + this.c.getPlayer().getId() + "", 1)) {
            this.c.getPlayer().cloneLook1();
        } else {
            this.c.getPlayer().dropMessage(5, "无法继续召唤分身。");
        }

    }

    public void 销毁分身() {
        if (this.c.getPlayer().getCloneSize() > 0) {
            this.c.getPlayer().disposeClones();
        }

    }


    public void 豆豆机抽奖() {
        int sns = 0;

        Connection con;
        PreparedStatement ps;
        ResultSet rs;
        Throwable var5;
        try {
            con = DBConPool.getConnection();
            ps = con.prepareStatement("SELECT `id` FROM 豆豆机奖品  ORDER BY `id` DESC LIMIT 1");
            rs = ps.executeQuery();
            var5 = null;

            try {
                if (rs.next()) {
                    String SN = rs.getString("id");
                    sns = Integer.parseInt(SN);
                    ++sns;
                    ps.close();
                }
            } catch (Throwable var35) {
                var5 = var35;
                throw var35;
            } finally {
                if (rs != null) {
                    if (var5 != null) {
                        try {
                            rs.close();
                        } catch (Throwable var31) {
                            var5.addSuppressed(var31);
                        }
                    } else {
                        rs.close();
                    }
                }

            }

            ps.close();
        } catch (SQLException var37) {
            服务端输出信息.println_err("出错：" + var37.getMessage());
        }

        try {
            con = DBConPool.getConnection();
            ps = con.prepareStatement(" SELECT * FROM  豆豆机奖品 ");
            rs = ps.executeQuery();
            var5 = null;

            try {
                double 随机 = Math.ceil(Math.random() * (double)sns);

                while(rs.next()) {
                    if ((double)rs.getInt("id") == 随机) {
                        this.gainItem(rs.getInt("itemId"), (short)rs.getInt("cout"));
                    }
                }
            } catch (Throwable var32) {
                var5 = var32;
                throw var32;
            } finally {
                if (rs != null) {
                    if (var5 != null) {
                        try {
                            rs.close();
                        } catch (Throwable var30) {
                            var5.addSuppressed(var30);
                        }
                    } else {
                        rs.close();
                    }
                }

            }

            ps.close();
        } catch (SQLException var34) {
            服务端输出信息.println_err("玩具塔副本奖励2、出错");
        }

    }

    public String 显示锻造需求材料(int a) {
        StringBuilder name = new StringBuilder();
        name.append("#r#e需要材料#k#n；—————————————————————\r\n");

        try {
            Connection con = DBConPool.getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT * FROM 锻造材料表 WHERE id2 = " + a + "");
            ResultSet rs = ps.executeQuery();

            while(rs.next()) {
                int 物品代码 = rs.getInt("物品代码");
                int 物品数量 = rs.getInt("物品数量");
                name.append("\r\n#v").append(物品代码).append("# #d#t").append(物品代码).append("##k x [ #b").append(物品数量).append("#k / #r#c").append(物品代码).append("##k ]\r\n");
            }
        } catch (SQLException var8) {
        }

        return name.toString();
    }

    public int 判断材料是否足够(int a) {
        int data = 0;

        try {
            Connection con = DBConPool.getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT * FROM 锻造材料表 WHERE id2 = " + a + "");
            ResultSet rs = ps.executeQuery();

            while(rs.next()) {
                ++data;
                int 物品代码 = rs.getInt("物品代码");
                int 物品数量 = rs.getInt("物品数量");
                if (物品代码 == 0) {
                    if (this.getPlayer().getMeso() <= 物品数量) {
                        data -= 100;
                    }
                } else if (物品代码 == 1) {
                    if (this.c.getPlayer().getCSPoints(1) <= 物品数量) {
                        data -= 100;
                    }
                } else if (!this.haveItem(物品代码, 物品数量)) {
                    data -= 100;
                }
            }
        } catch (SQLException var8) {
        }

        return data;
    }

    public int 收取制作材料(int a) {
        int data = 0;

        try {
            Connection con = DBConPool.getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT * FROM 锻造材料表 WHERE id2 = " + a + "");
            ResultSet rs = ps.executeQuery();

            while(rs.next()) {
                ++data;
                int 物品代码 = rs.getInt("物品代码");
                int 物品数量 = rs.getInt("物品数量");
                if (物品代码 == 0) {
                    this.c.getPlayer().gainMeso((int) (-物品数量), true, false, true);
                } else if (物品代码 == 1) {
                    this.c.getPlayer().modifyCSPoints(1, -物品数量, true);
                } else {
                    this.gainItem(物品代码, ((short)(-物品数量)));
                }
            }
        } catch (SQLException var8) {
        }

        return data;
    }

    public String 显示锻造奖励信息(int a) {
        StringBuilder name = new StringBuilder();
        name.append("#r#e制作获得#n#k；—————————————————————\r\n");
        name.append("number: #g" + a + "#k\r\n");

        try {
            Connection con = DBConPool.getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT * FROM 锻造物品表 WHERE id2 = " + a + "");
            ResultSet rs = ps.executeQuery();

            while(rs.next()) {
                int 物品代码 = rs.getInt("物品代码");
                int 物品数量 = rs.getInt("物品数量");
                name.append("\r\n#v").append(物品代码).append("# #d#t").append(物品代码).append("##k x [ #r").append(物品数量).append("#k ]\r\n");
                if (rs.getInt("可升级次数") > 0) {
                    name.append("\r\n[升级次数:#r").append(rs.getInt("可升级次数")).append("#k]");
                }

                if (物品代码 < 2000000) {
                    switch (rs.getInt("绑定")) {
                        case 1:
                            name.append("\r\n[状态:#r").append("封印").append("#k]");
                            break;
                        case 2:
                            name.append("\r\n[状态:#r").append("防滑").append("#k]");
                            break;
                        case 3:
                            name.append("\r\n[状态:#r").append("封印,防滑").append("#k]");
                            break;
                        case 4:
                            name.append("\r\n[状态:#r").append("防寒").append("#k]");
                            break;
                        case 5:
                            name.append("\r\n[状态:#r").append("封印,防寒").append("#k]");
                            break;
                        case 6:
                            name.append("\r\n[状态:#r").append("防滑,防寒").append("#k]");
                            break;
                        case 7:
                            name.append("\r\n[状态:#r").append("封印,防滑,防寒").append("#k]");
                            break;
                        case 8:
                            name.append("\r\n[状态:#r").append("不可交换").append("#k]");
                            break;
                        case 9:
                            name.append("\r\n[状态:#r").append("封印,不可交换").append("#k]");
                            break;
                        case 10:
                            name.append("\r\n[状态:#r").append("防滑,不可交换").append("#k]");
                            break;
                        case 11:
                            name.append("\r\n[状态:#r").append("封印,防滑,不可交换").append("#k]");
                            break;
                        case 12:
                            name.append("\r\n[状态:#r").append("防寒,不可交换").append("#k]");
                            break;
                        case 13:
                            name.append("\r\n[状态:#r").append("封印,防寒,不可交换").append("#k]");
                            break;
                        case 14:
                            name.append("\r\n[状态:#r").append("防滑,防寒,不可交换").append("#k]");
                            break;
                        case 15:
                            name.append("\r\n[状态:#r").append("封印,防滑,防寒,不可交换").append("#k]");
                    }
                }

                if (rs.getInt("力量") > 0) {
                    name.append("\r\n[力量:#r").append(rs.getInt("力量")).append("#k]");
                }

                if (rs.getInt("敏捷") > 0) {
                    name.append("\r\n[敏捷:#r").append(rs.getInt("敏捷")).append("#k]");
                }

                if (rs.getInt("智力") > 0) {
                    name.append("\r\n[智力:#r").append(rs.getInt("智力")).append("#k]");
                }

                if (rs.getInt("运气") > 0) {
                    name.append("\r\n[运气:#r").append(rs.getInt("运气")).append("#k]");
                }

                if (rs.getInt("HP") > 0) {
                    name.append("\r\n[HP:#r").append(rs.getInt("HP")).append("#k]");
                }

                if (rs.getInt("MP") > 0) {
                    name.append("\r\n[MP:#r").append(rs.getInt("MP")).append("#k]");
                }

                if (rs.getInt("物理攻击力") > 0) {
                    name.append("\r\n[物理攻击力:#r").append(rs.getInt("物理攻击力")).append("#k]");
                }

                if (rs.getInt("物理防御力") > 0) {
                    name.append("\r\n[物理防御力:#r").append(rs.getInt("物理防御力")).append("#k]");
                }

                if (rs.getInt("魔法攻击力") > 0) {
                    name.append("\r\n[魔法攻击力:#r").append(rs.getInt("魔法攻击力")).append("#k]");
                }

                if (rs.getInt("魔法防御力") > 0) {
                    name.append("\r\n[魔法防御力:#r").append(rs.getInt("魔法防御力")).append("#k]");
                }

                if (rs.getInt("命中率") > 0) {
                    name.append("\r\n[命中率:#r").append(rs.getInt("命中率")).append("#k]");
                }

                if (rs.getInt("回避率") > 0) {
                    name.append("\r\n[回避率:#r").append(rs.getInt("回避率")).append("#k]");
                }

                if (rs.getInt("跳跃力") > 0) {
                    name.append("\r\n[跳跃力:#r").append(rs.getInt("跳跃力")).append("#k]");
                }

                if (rs.getInt("移动速度") > 0) {
                    name.append("\r\n[移动速度:#r").append(rs.getInt("移动速度")).append("#k]");
                }

                if (rs.getInt("限时") > 0) {
                    name.append("\r\n[限时:#r").append(rs.getInt("限时")).append("#k]");
                }
            }
        } catch (SQLException var8) {
        }

        return name.toString();
    }

    public void 给锻造奖励(int a) {
        try {
            Connection con = DBConPool.getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT * FROM 锻造物品表 WHERE id2 = " + a + "");
            ResultSet rs = ps.executeQuery();

            while(rs.next()) {
                int 物品代码 = rs.getInt("物品代码");
                int 物品数量 = rs.getInt("物品数量");
                if (物品代码 > 2000000) {
                    this.gainItem(物品代码,((short)物品数量));
                } else {
                    this.给装备(rs.getInt("物品代码"), rs.getInt("可升级次数"), rs.getInt("绑定"), rs.getInt("力量"), rs.getInt("敏捷"), rs.getInt("智力"), rs.getInt("运气"), rs.getInt("HP"), rs.getInt("MP"), rs.getInt("物理攻击力"), rs.getInt("物理防御力"), rs.getInt("魔法攻击力"), rs.getInt("魔法防御力"), rs.getInt("回避率"), rs.getInt("命中率"), rs.getInt("跳跃力"), rs.getInt("移动速度"), rs.getInt("限时"));
                }
            }
        } catch (SQLException var7) {
        }

    }


    public final void 给装备(int id, int sj, int Flag, int str, int dex, int luk, int Int, int hp, int mp, int watk, int matk, int wdef, int mdef, int hb, int mz, int ty, int yd, int period) {
        this.给装备(id, sj, Flag, str, dex, luk, Int, hp, mp, watk, matk, wdef, mdef, hb, mz, ty, yd, this.c, period);
    }

    public final void 给装备(int id, int sj, int Flag, int str, int dex, int luk, int Int, int hp, int mp, int watk, int matk, int wdef, int mdef, int hb, int mz, int ty, int yd, MapleClient cg, int period) {
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        MapleInventoryType type = GameConstants.getInventoryType(id);
        if (MapleInventoryManipulator.checkSpace(cg, id, 1, "")) {
            if (type.equals(MapleInventoryType.EQUIP) && !GameConstants.isThrowingStar(id) && !GameConstants.isBullet(id)) {
                Equip item = (Equip)((Equip)ii.getEquipById(id));
                if (period > 0) {
                    item.setExpiration(System.currentTimeMillis() + (long)(period * 60 * 60 * 1000));
                }

                String name = ii.getName(id);
                if (id / 10000 == 114 && name != null && name.length() > 0) {
                    String msg = "你已获得称号 <" + name + ">";
                    cg.getPlayer().dropMessage(5, msg);
                }

                if (sj > 0) {
                    item.setUpgradeSlots((byte)((short)sj));
                }

                if (Flag > 0) {
                    item.setFlag((byte)((short)Flag));
                }

                if (str > 0) {
                    item.setStr((short)str);
                }

                if (dex > 0) {
                    item.setDex((short)dex);
                }

                if (luk > 0) {
                    item.setLuk((short)luk);
                }

                if (Int > 0) {
                    item.setInt((short)Int);
                }

                if (hp > 0) {
                    item.setHp((short)hp);
                }

                if (mp > 0) {
                    item.setMp((short)mp);
                }

                if (watk > 0) {
                    item.setWatk((short)watk);
                }

                if (matk > 0) {
                    item.setMatk((short)matk);
                }

                if (wdef > 0) {
                    item.setWdef((short)wdef);
                }

                if (mdef > 0) {
                    item.setMdef((short)mdef);
                }

                if (hb > 0) {
                    item.setAvoid((short)hb);
                }

                if (mz > 0) {
                    item.setAcc((short)mz);
                }

                if (ty > 0) {
                    item.setJump((short)ty);
                }

                if (yd > 0) {
                    item.setSpeed((short)yd);
                }

                MapleInventoryManipulator.addbyItem(cg, item.copy());
            } else {
                MapleInventoryManipulator.addById(cg, id, (short) 1, "", (byte)0);
            }

            cg.sendPacket(MaplePacketCreator.getShowItemGain(id, (short) 1, true));
        }
    }

    public static int 取装备代码(int id) {
        int data = 0;

        try {
            Connection con = DBConPool.getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT itemid as DATA FROM inventoryitems WHERE inventoryitemid = ?");
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            Throwable var5 = null;

            try {
                if (rs.next()) {
                    data = rs.getInt("DATA");
                }
            } catch (Throwable var15) {
                var5 = var15;
                throw var15;
            } finally {
                if (rs != null) {
                    if (var5 != null) {
                        try {
                            rs.close();
                        } catch (Throwable var14) {
                            var5.addSuppressed(var14);
                        }
                    } else {
                        rs.close();
                    }
                }

            }

            ps.close();
        } catch (SQLException var17) {
            服务端输出信息.println_err("取装备代码、出错");
        }

        return data;
    }

    public static int 取装备拥有者(int id) {
        int data = 0;

        try {
            Connection con = DBConPool.getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT characterid as DATA FROM inventoryitems WHERE inventoryitemid = ?");
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            Throwable var5 = null;

            try {
                if (rs.next()) {
                    data = rs.getInt("DATA");
                }
            } catch (Throwable var15) {
                var5 = var15;
                throw var15;
            } finally {
                if (rs != null) {
                    if (var5 != null) {
                        try {
                            rs.close();
                        } catch (Throwable var14) {
                            var5.addSuppressed(var14);
                        }
                    } else {
                        rs.close();
                    }
                }

            }

            ps.close();
        } catch (SQLException var17) {
            服务端输出信息.println_err("取装备代码、出错");
        }

        return data;
    }

    public String 等级成就(int a) {
        StringBuilder name = new StringBuilder();

        try {
            Connection con = DBConPool.getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT * FROM Upgrade_career order by id desc");
            ResultSet rs = ps.executeQuery();

            while(rs.next()) {
                if (rs.getInt("id2") == a) {
                    name.append("        " + rs.getString("shijian") + "  " + rs.getString("name") + "\r\n");
                }
            }
        } catch (SQLException var6) {
        }

        return name.toString();
    }

    public static String 显示参赛() {
        return capture_yongfa.显示参赛人员();
    }

    public static void 参加(int a) {
        capture_yongfa.参加(a);
    }

    public static int 是否参加(int a) {
        return capture_yongfa.判断是否已经参加(a);
    }

    public static int 判断队伍(int a) {
        return capture_yongfa.判断队伍(a);
    }



    public final void 超时空战场() {
        Timer.WorldTimer.getInstance().register(new Runnable() {
            public void run() {
            }
        }, 60000L);
    }

    public String 黑龙远征队() {
        StringBuilder name = new StringBuilder();
        name.append("黑龙远征队：\r\n");

        try {
            Connection con = DBConPool.getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT * FROM characterz WHERE channel = 201 ");
            ResultSet rs = ps.executeQuery();

            while(rs.next()) {
                if (rs.getInt("Point") > 0) {
                    String 身份 = "";
                    if (rs.getInt("Point") == 1) {
                        身份 = "[#b队员#k]";
                    } else {
                        身份 = "[#r队长#k]";
                    }

                    name.append("" + 身份 + "  玩家: #d" + 角色ID取名字(rs.getInt("Name")) + "#k \r\n");
                }
            }
        } catch (SQLException var6) {
        }

        return name.toString();
    }

    public void 开始黑龙远征(int a, int b) {
        Iterator var3 = ChannelServer.getAllInstances().iterator();

        while(var3.hasNext()) {
            ChannelServer CS = (ChannelServer)var3.next();
            Iterator var5 = CS.getPlayerStorage().getAllCharactersThreadSafe().iterator();

            while(var5.hasNext()) {
                MapleCharacter mch = (MapleCharacter)var5.next();
                if (mch.Getcharacterz("" + mch.getId() + "", 201) > 0) {
                    if (mch.getClient().getChannel() != a) {
                        mch.changeChannel(a);
                    }

                    if (mch.getMapId() != b) {
                        mch.changeMap(b, 0);
                    }
                }
            }
        }

    }

    public String 显示邮件(int id) {
        StringBuilder name = new StringBuilder();

        try {
            Connection con = DBConPool.getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT * FROM mail");
            ResultSet rs = ps.executeQuery();

            while(rs.next()) {
                if (rs.getInt("juese") == id) {
                    int 邮件编号 = rs.getInt("number");
                    String 邮件标题 = rs.getString("biaoti");
                    name.append("    #L").append(邮件编号).append("##fUI/UIWindow.img/Delivery/icon4##b").append(邮件标题).append("#k#l\r\n");
                }
            }
        } catch (SQLException var8) {
        }

        return name.toString();
    }

    public void 领取邮件(int id) {
        try {
            Connection con = DBConPool.getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT * FROM mail");
            ResultSet rs = ps.executeQuery();

            while(rs.next()) {
                if (rs.getInt("number") == id) {
                    int 数量 = rs.getInt("shuliang1");
                    int 代码 = rs.getInt("type1");
                    if (rs.getInt("type1") != 0) {
                        switch (代码) {
                            case 1:
                                this.c.getPlayer().modifyCSPoints(1, 数量, true);
                                break;
                            case 2:
                                this.c.getPlayer().modifyCSPoints(2, 数量, true);
                                break;
                            case 3:
                                this.c.getPlayer().gainExp(数量, true, true, true);
                                break;
                            case 4:
                                this.c.getPlayer().gainMeso((int)数量, true, false, true);
                                break;
                            default:
                                this.gainItem(代码, ((short)数量));
                        }
                    }

                    int 数量2 = rs.getInt("shuliang2");
                    int 代码2 = rs.getInt("type2");
                    if (rs.getInt("type2") != 0) {
                        switch (代码2) {
                            case 1:
                                this.c.getPlayer().modifyCSPoints(1, 数量2, true);
                                break;
                            case 2:
                                this.c.getPlayer().modifyCSPoints(2, 数量2, true);
                                break;
                            case 3:
                                this.c.getPlayer().gainExp(数量2, true, true, true);
                                break;
                            case 4:
                                this.c.getPlayer().gainMeso((int)数量2, true, false, true);
                                break;
                            default:
                                this.gainItem(代码2, ((short)数量2));
                        }
                    }

                    int 数量3 = rs.getInt("shuliang3");
                    int 代码3 = rs.getInt("type3");
                    if (rs.getInt("type3") != 0) {
                        switch (代码3) {
                            case 1:
                                this.c.getPlayer().modifyCSPoints(1, 数量3, true);
                                break;
                            case 2:
                                this.c.getPlayer().modifyCSPoints(2, 数量3, true);
                                break;
                            case 3:
                                this.c.getPlayer().gainExp(数量3, true, true, true);
                                break;
                            case 4:
                                this.c.getPlayer().gainMeso((int)数量3, true, false, true);
                                break;
                            default:
                                MapleInventoryManipulator.addById(this.c, 代码3,((short)数量3), "", (MaplePet)null, 0L);
                                this.gainItem(代码3, ((short)数量3));
                        }
                    }

                    PreparedStatement ps1 = null;
                    ResultSet rs1 = null;

                    try {
                        Connection con1 = DBConPool.getConnection();
                        ps1 = con1.prepareStatement("SELECT * FROM mail WHERE number = ?");
                        ps1.setInt(1, id);
                        rs1 = ps1.executeQuery();
                        if (rs1.next()) {
                            String sqlstr = " delete from mail where number = " + id + "";
                            ps1.executeUpdate(sqlstr);
                        }
                    } catch (SQLException var15) {
                    }
                }
            }
        } catch (SQLException var16) {
        }

    }

    public String 显示邮件内容(int id) {
        StringBuilder name = new StringBuilder();

        try {
            Connection con = DBConPool.getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT * FROM mail");
            ResultSet rs = ps.executeQuery();

            while(rs.next()) {
                if (rs.getInt("number") == id) {
                    String 邮件标题 = rs.getString("biaoti");
                    String 邮件时间 = rs.getString("shijian");
                    String 邮件正文 = rs.getString("wenben");
                    name.append("标题: #b").append(邮件标题).append("#k\r\n");
                    name.append("时间: #b").append(邮件时间).append("#k\r\n");
                    name.append("发件: #b系统管理员#k\r\n");
                    name.append("正文: #b").append(邮件正文).append("#k\r\n\r\n");
                    if (rs.getInt("type1") != 0) {
                        switch (rs.getInt("type1")) {
                            case 1:
                                name.append("[附件]:#d点券 x #b").append(rs.getInt("shuliang1")).append("#k\r\n");
                                break;
                            case 2:
                                name.append("[附件]:#d抵用 x #b").append(rs.getInt("shuliang1")).append("#k\r\n");
                                break;
                            case 3:
                                name.append("[附件]:#d经验 x #b").append(rs.getInt("shuliang1")).append("#k\r\n");
                                break;
                            case 4:
                                name.append("[附件]:#d金币 x #b").append(rs.getInt("shuliang1")).append("#k\r\n");
                                break;
                            default:
                                name.append("[附件]:#v").append(rs.getInt("type1")).append("# #b#t").append(rs.getInt("type1")).append("##k x #b").append(rs.getInt("shuliang1")).append("#k\r\n");
                        }
                    }

                    if (rs.getInt("type2") != 0) {
                        switch (rs.getInt("type2")) {
                            case 1:
                                name.append("[附件]:#d点券 x #b").append(rs.getInt("shuliang2")).append("#k\r\n");
                                break;
                            case 2:
                                name.append("[附件]:#d抵用 x #b").append(rs.getInt("shuliang2")).append("#k\r\n");
                                break;
                            case 3:
                                name.append("[附件]:#d经验 x #b").append(rs.getInt("shuliang2")).append("#k\r\n");
                                break;
                            case 4:
                                name.append("[附件]:#d金币 x #b").append(rs.getInt("shuliang2")).append("#k\r\n");
                                break;
                            default:
                                name.append("[附件]:#v").append(rs.getInt("type2")).append("# #b#t").append(rs.getInt("type2")).append("##k x #b").append(rs.getInt("shuliang2")).append("#k\r\n");
                        }
                    }

                    if (rs.getInt("type3") != 0) {
                        switch (rs.getInt("type3")) {
                            case 1:
                                name.append("[附件]:#d点券 x #b").append(rs.getInt("shuliang3")).append("#k\r\n");
                                break;
                            case 2:
                                name.append("[附件]:#d抵用 x #b").append(rs.getInt("shuliang3")).append("#k\r\n");
                                break;
                            case 3:
                                name.append("[附件]:#d经验 x #b").append(rs.getInt("shuliang3")).append("#k\r\n");
                                break;
                            case 4:
                                name.append("[附件]:#d金币 x #b").append(rs.getInt("shuliang3")).append("#k\r\n");
                                break;
                            default:
                                name.append("[附件]:#v").append(rs.getInt("type3")).append("# #b#t").append(rs.getInt("type3")).append("##k x #b").append(rs.getInt("shuliang3")).append("#k\r\n");
                        }
                    }
                }
            }
        } catch (SQLException var9) {
        }

        return name.toString();
    }

    public void 删除邮件(int a) {
        PreparedStatement ps1 = null;
        ResultSet rs1 = null;

        try {
            Connection con = DBConPool.getConnection();
            ps1 = con.prepareStatement("SELECT * FROM mail WHERE number = ?");
            ps1.setInt(1, a);
            rs1 = ps1.executeQuery();
            if (rs1.next()) {
                String sqlstr = " delete from mail where number = " + a + "";
                ps1.executeUpdate(sqlstr);
            }
        } catch (SQLException var6) {
        }

    }

    public String 显示永生重生升级需要的经验() {
        StringBuilder name = new StringBuilder();
        String 等级 = "";

        for(int i = 1; i <= 30; ++i) {
            等级 = "装备等级" + i + "";
            name.append("等级 [" + 等级 + "], 需要经验 (" + LtMS.ConfigValuesMap.get(等级) + ")\r\n");
        }

        return name.toString();
    }

    public int 判断传送点x(int id, int cid) {
        int ret = -1;

        try {
            Connection con = DBConPool.getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT * FROM awarp WHERE id = ? and cid = ?");
            ps.setInt(1, id);
            ps.setInt(2, cid);
            ResultSet rs = ps.executeQuery();
            rs.next();
            ret = rs.getInt("x");
            rs.close();
            ps.close();
        } catch (SQLException var7) {
        }

        return ret;
    }

    public int 判断传送点y(int id, int cid) {
        int ret = -1;

        try {
            Connection con = DBConPool.getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT * FROM awarp WHERE id = ? and cid = ?");
            ps.setInt(1, id);
            ps.setInt(2, cid);
            ResultSet rs = ps.executeQuery();
            rs.next();
            ret = rs.getInt("y");
            rs.close();
            ps.close();
        } catch (SQLException var7) {
        }

        return ret;
    }

    public void 设置传送点x(int id, int cid, int x) {
        try {
            int ret = this.判断传送点x(id, cid);
            if (ret == -1) {
                PreparedStatement ps = null;

                try {
                    Connection con = DBConPool.getConnection();
                    ps = con.prepareStatement("INSERT INTO awarp (id, cid,x) VALUES (?, ?, ?)");
                    ps.setInt(1, id);
                    ps.setInt(2, cid);
                    ps.setInt(3, x);
                    ps.execute();
                } catch (SQLException var16) {
                    服务端输出信息.println_out("设置传送点x1:" + var16);
                } finally {
                    try {
                        if (ps != null) {
                            ps.close();
                        }
                    } catch (SQLException var15) {
                        服务端输出信息.println_out("设置传送点x2:" + var15);
                    }

                }
            } else {
                Connection con = DBConPool.getConnection();
                PreparedStatement ps = con.prepareStatement("UPDATE awarp SET `x` = ? WHERE id = ? and cid = ?");
                ps.setInt(1, x);
                ps.setInt(2, id);
                ps.setInt(3, cid);
                ps.execute();
                ps.close();
            }
        } catch (SQLException var18) {
            服务端输出信息.println_err("设置传送点x3" + var18);
        }

    }

    public void 设置传送点y(int id, int cid, int y) {
        try {
            int ret = this.判断传送点x(id, cid);
            if (ret == -1) {
                PreparedStatement ps = null;

                try {
                    Connection con = DBConPool.getConnection();
                    ps = con.prepareStatement("INSERT INTO awarp (id, cid,x) VALUES (?, ?, ?)");
                    ps.setInt(1, id);
                    ps.setInt(2, cid);
                    ps.setInt(3, y);
                    ps.execute();
                } catch (SQLException var16) {
                    服务端输出信息.println_out("设置传送点y1:" + var16);
                } finally {
                    try {
                        if (ps != null) {
                            ps.close();
                        }
                    } catch (SQLException var15) {
                        服务端输出信息.println_out("设置传送点y2:" + var15);
                    }

                }
            } else {
                Connection con = DBConPool.getConnection();
                PreparedStatement ps = con.prepareStatement("UPDATE awarp SET `y` = ? WHERE id = ? and cid = ?");
                ps.setInt(1, y);
                ps.setInt(2, id);
                ps.setInt(3, cid);
                ps.execute();
                ps.close();
            }
        } catch (SQLException var18) {
            服务端输出信息.println_err("设置传送点y3" + var18);
        }

    }

    public String 显示任务内容(int id) {
        StringBuilder name = new StringBuilder();

        Connection con;
        PreparedStatement ps;
        ResultSet rs;
        try {
            con = DBConPool.getConnection();
            ps = con.prepareStatement("SELECT * FROM mail");
            rs = ps.executeQuery();

            while(rs.next()) {
                if (rs.getInt("a") == id) {
                    String 邮件标题 = rs.getString("f");
                    String 邮件时间 = rs.getString("c");
                    String 邮件正文 = rs.getString("g");
                    String 发布人 = 角色ID取名字(rs.getInt("b"));
                    name.append("任务标题: #b").append(邮件标题).append("#k\r\n");
                    name.append("发布时间: #b").append(邮件时间).append("#k\r\n");
                    name.append("任务正文: #b").append(邮件正文).append("#k\r\n");
                    name.append("发 布 人: #b").append(发布人).append("#k\r\n\r\n");
                }
            }
        } catch (SQLException var11) {
        }

        name.append("任务需求\r\n");

        try {
            con = DBConPool.getConnection();
            ps = con.prepareStatement("SELECT * FROM mail_1");
            rs = ps.executeQuery();

            while(rs.next()) {
                if (rs.getInt("a") == id) {
                    int 需要材料 = rs.getInt("b");
                    int 需要数量 = rs.getInt("c");
                    switch (rs.getInt("需要材料")) {
                        case 1:
                            name.append("[材料]:#d点券 x #b" + 需要数量 + "#k\r\n");
                            break;
                        case 2:
                            name.append("[材料]:#d抵用 x #b" + 需要数量 + "#k\r\n");
                            break;
                        case 3:
                            name.append("[材料]:#d金币 x #b" + 需要数量 + "#k\r\n");
                            break;
                        case 4:
                            name.append("[材料]:#d经验 x #b" + 需要数量 + "#k\r\n");
                            break;
                        case 5:
                            name.append("[材料]:#d人气 x #b" + 需要数量 + "#k\r\n");
                    }
                }
            }
        } catch (SQLException var10) {
        }

        name.append("任务奖励\r\n");
        return name.toString();
    }

    public String 显示任务目录() {
        StringBuilder name = new StringBuilder();

        try {
            Connection con = DBConPool.getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT * FROM task");
            ResultSet rs = ps.executeQuery();

            while(rs.next()) {
                String 进度 = "";
                if (rs.getInt("d") == 0) {
                    进度 = "#r[未完成]#k";
                } else {
                    进度 = "#g[已完成]#k";
                }

                int 任务编号 = rs.getInt("a");
                String 任务标题 = rs.getString("f");
                name.append(" #L" + 任务编号 + "#" + 进度 + " #b" + 任务标题 + "#k#l\r\n");
            }
        } catch (SQLException var8) {
        }

        return name.toString();
    }

    public String 等级排行榜() {
        int 名次 = 1;
        StringBuilder name = new StringBuilder();

        try {
            Connection con = DBConPool.getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT * FROM characters  WHERE gm = 0 order by level desc");
            ResultSet rs = ps.executeQuery();

            label96:
            while(true) {
                while(true) {
                    do {
                        do {
                            if (!rs.next()) {
                                break label96;
                            }
                        } while(rs.getInt("level") >= (Integer)LtMS.ConfigValuesMap.get("冒险家等级上限"));
                    } while(rs.getInt("level") <= 30);

                    String 玩家名字;
                    String 职业;
                    int j;
                    if (名次 < 10) {
                        玩家名字 = rs.getString("name");
                        职业 = this.职业(rs.getInt("job"));
                        name.append("Top.#e#d").append(名次).append("#n#k   ");
                        name.append("#b").append(玩家名字).append("#k");

                        for(j = 13 - 玩家名字.getBytes().length; j > 0; --j) {
                            name.append(" ");
                        }

                        name.append("  ").append(职业).append("");

                        for(j = 15 - 职业.getBytes().length; j > 0; --j) {
                            name.append(" ");
                        }

                        name.append("  Lv.#d").append(rs.getInt("level")).append("#k\r\n");
                        ++名次;
                    } else if (名次 >= 10 && 名次 <= 99) {
                        玩家名字 = rs.getString("name");
                        职业 = this.职业(rs.getInt("job"));
                        name.append("Top.#e#d").append(名次).append("#n#k  ");
                        name.append("#b").append(玩家名字).append("#k");

                        for(j = 13 - 玩家名字.getBytes().length; j > 0; --j) {
                            name.append(" ");
                        }

                        name.append("  ").append(职业).append("");

                        for(j = 15 - 职业.getBytes().length; j > 0; --j) {
                            name.append(" ");
                        }

                        name.append("  Lv.#d").append(rs.getInt("level")).append("#k\r\n");
                        ++名次;
                    } else if (名次 > 99) {
                        玩家名字 = rs.getString("name");
                        职业 = this.职业(rs.getInt("job"));
                        name.append("Top.#e#d").append(名次).append("#n#k ");
                        name.append("#b").append(玩家名字).append("#k");

                        for(j = 13 - 玩家名字.getBytes().length; j > 0; --j) {
                            name.append(" ");
                        }

                        name.append("  ").append(职业).append("");

                        for(j = 15 - 职业.getBytes().length; j > 0; --j) {
                            name.append(" ");
                        }

                        name.append("  Lv.#d").append(rs.getInt("level")).append("#k\r\n");
                        ++名次;
                    }
                }
            }
        } catch (SQLException var9) {
        }

        name.append("\r\n\r\n");
        return name.toString();
    }

    public String 满级排行榜() {
        StringBuilder name = new StringBuilder();

        try {
            Connection con = DBConPool.getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT * FROM characters WHERE gm < 1 ORDER BY `level` DESC LIMIT 100");
            ResultSet rs = ps.executeQuery();

            label39:
            while(true) {
                do {
                    if (!rs.next()) {
                        break label39;
                    }
                } while(rs.getInt("level") != ServerConfig.maxlevel);

                String 玩家名字 = rs.getString("name");
                String 职业 = this.职业(rs.getInt("job"));
                int 家族编号 = rs.getInt("guildid");
                name.append("    ");
                name.append("#b").append(玩家名字).append("#k");

                int j;
                for(j = 13 - 玩家名字.getBytes().length; j > 0; --j) {
                    name.append(" ");
                }

                name.append("  ").append(职业).append("");

                for(j = 15 - 职业.getBytes().length; j > 0; --j) {
                    name.append(" ");
                }

                name.append("家族:#d").append(this.获取家族名称(家族编号)).append("#k\r\n");
            }
        } catch (SQLException var10) {
        }

        name.append("\r\n\r\n");
        return name.toString();
    }

    public String 怪物卡片排行榜() {
        StringBuilder name = new StringBuilder();

        try {
            Connection con = DBConPool.getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT * FROM monsterbook   order by level desc");
            ResultSet rs = ps.executeQuery();

            label39:
            while(true) {
                do {
                    if (!rs.next()) {
                        break label39;
                    }
                } while(rs.getInt("level") != (Integer)LtMS.ConfigValuesMap.get("冒险家等级上限"));

                String 玩家名字 = rs.getString("name");
                String 职业 = this.职业(rs.getInt("job"));
                int 家族编号 = rs.getInt("guildid");
                name.append("    ");
                name.append("#b").append(玩家名字).append("#k");

                int j;
                for(j = 13 - 玩家名字.getBytes().length; j > 0; --j) {
                    name.append(" ");
                }

                name.append("  ").append(职业).append("");

                for(j = 15 - 职业.getBytes().length; j > 0; --j) {
                    name.append(" ");
                }

                name.append("家族.#d").append(this.获取家族名称(家族编号)).append("#k\r\n");
            }
        } catch (SQLException var10) {
        }

        name.append("\r\n\r\n");
        return name.toString();
    }

    public String 财富排行榜() {
        int 名次 = 1;
        StringBuilder name = new StringBuilder();

        try {
            Connection con = DBConPool.getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT * FROM characters WHERE gm = 0 order by meso desc LIMIT 20 ");
            ResultSet rs = ps.executeQuery();

            label52:
            while(true) {
                while(true) {
                    do {
                        if (!rs.next()) {
                            break label52;
                        }
                    } while(rs.getInt("meso") <= 0);

                    String 玩家名字;
                    String 金币;
                    int j;
                    if (名次 < 10) {
                        玩家名字 = rs.getString("name");
                        金币 = rs.getString("meso");
                        name.append("Top.#e#d").append(名次).append("#n#k   ");
                        name.append("#b").append(玩家名字).append("#k");

                        for(j = 13 - 玩家名字.getBytes().length; j > 0; --j) {
                            name.append(" ");
                        }

                        name.append("     Meso.#d").append(金币).append("#n\r\n");
                        ++名次;
                    } else if (名次 >= 10 && 名次 <= 20) {
                        玩家名字 = rs.getString("name");
                        金币 = rs.getString("meso");
                        name.append("Top.#e#d").append(名次).append("#n#k  ");
                        name.append("#b").append(玩家名字).append("#k");

                        for(j = 13 - 玩家名字.getBytes().length; j > 0; --j) {
                            name.append(" ");
                        }

                        name.append("     Meso.#d").append(金币).append("#n\r\n");
                        ++名次;
                    }
                }
            }
        } catch (SQLException var9) {
        }

        name.append("\r\n\r\n");
        return name.toString();
    }

    public String 在线排行榜() {
        int 名次 = 1;
        StringBuilder name = new StringBuilder();

        try {
            Connection con = DBConPool.getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT * FROM characters WHERE gm = 0 order by totalOnlineTime desc LIMIT 20 ");
            ResultSet rs = ps.executeQuery();

            label52:
            while(true) {
                while(true) {
                    do {
                        if (!rs.next()) {
                            break label52;
                        }
                    } while(rs.getInt("totalOnlineTime") <= 0);

                    String 玩家名字;
                    String 总在线;
                    String 今在线;
                    int j;
                    if (名次 < 10) {
                        玩家名字 = rs.getString("name");
                        总在线 = rs.getString("totalOnlineTime");
                        今在线 = rs.getString("todayOnlineTime");
                        name.append("Top.#e#d").append(名次).append("#n#k   ");
                        name.append("#b").append(玩家名字).append("#k");

                        for(j = 13 - 玩家名字.getBytes().length; j > 0; --j) {
                            name.append(" ");
                        }

                        name.append("     (tal/day).#d[").append(总在线).append(" / ").append(今在线).append("])\r\n");
                        ++名次;
                    } else if (名次 >= 10 && 名次 <= 20) {
                        玩家名字 = rs.getString("name");
                        总在线 = rs.getString("totalOnlineTime");
                        今在线 = rs.getString("todayOnlineTime");
                        name.append("Top.#e#d").append(名次).append("#n#k  ");
                        name.append("#b").append(玩家名字).append("#k");

                        for(j = 13 - 玩家名字.getBytes().length; j > 0; --j) {
                            name.append(" ");
                        }

                        name.append("     (tal/day).#d[").append(总在线).append(" / ").append(今在线).append("])\r\n");
                        ++名次;
                    }
                }
            }
        } catch (SQLException var10) {
        }

        name.append("\r\n\r\n");
        return name.toString();
    }

    public String 永恒重生排行榜() {
        int 名次 = 1;
        StringBuilder name = new StringBuilder();

        try {
            Connection con = DBConPool.getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT * FROM inventoryequipment order by itemlevel desc LIMIT 20");
            ResultSet rs = ps.executeQuery();

            label62:
            while(true) {
                int 玩家ID;
                String 玩家名字;
                int 道具IP;
                int j;
                do {
                    while(true) {
                        do {
                            if (!rs.next()) {
                                break label62;
                            }
                        } while(rs.getInt("itemlevel") <= 0);

                        if (名次 < 10) {
                            玩家ID = 道具id获取主人(rs.getInt("inventoryitemid"));
                            玩家名字 = 角色ID取名字(玩家ID);
                            break;
                        }

                        if (名次 >= 10 && 名次 <= 20) {
                            玩家ID = 道具id获取主人(rs.getInt("inventoryitemid"));
                            玩家名字 = 角色ID取名字(玩家ID);
                            if (角色ID取GM(玩家ID) == 0) {
                                道具IP = 道具id获取道具ID(rs.getInt("inventoryitemid"));
                                name.append("Top.#e#d").append(名次).append("#n#k  ");
                                name.append("拥有者:#b").append(玩家名字).append("#k");

                                for(j = 15 - 玩家名字.getBytes().length; j > 0; --j) {
                                    name.append(" ");
                                }

                                name.append(" lv.#r").append(rs.getInt("itemlevel")).append("#k #b#t").append(道具IP).append("##k\r\n");
                                ++名次;
                            }
                        }
                    }
                } while(角色ID取GM(玩家ID) != 0);

                道具IP = 道具id获取道具ID(rs.getInt("inventoryitemid"));
                name.append("Top.#e#d").append(名次).append("#n#k   ");
                name.append("拥有者:#b").append(玩家名字).append("#k");

                for(j = 15 - 玩家名字.getBytes().length; j > 0; --j) {
                    name.append(" ");
                }

                name.append(" lv.#r").append(rs.getInt("itemlevel")).append("#k #b#t").append(道具IP).append("##k\r\n");
                ++名次;
            }
        } catch (SQLException var10) {
        }

        name.append("\r\n\r\n");
        return name.toString();
    }

    public static int 道具id获取道具ID(int a) {
        int data = 0;

        try {
            Connection con = DBConPool.getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT * FROM inventoryitems WHERE inventoryitemid = " + a + "");
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                data = rs.getInt("itemid");
            }

            ps.close();
        } catch (SQLException var5) {
        }

        return data;
    }

    public static int 道具id获取主人(int a) {
        int data = 0;

        try {
            Connection con = DBConPool.getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT * FROM inventoryitems WHERE inventoryitemid = " + a + "");
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                data = rs.getInt("characterid");
            }

            ps.close();
        } catch (SQLException var5) {
        }

        return data;
    }

    public int 显示决斗开关() {
        if (Start.个人信息设置.get("" + this.c.getPlayer().getName() + "2") == null) {
            int ID = 0;

            Connection con;
            Throwable var5;
            try {
                con = DBConPool.getConnection();
                PreparedStatement ps = con.prepareStatement("SELECT `id` FROM jiezoudashi  ORDER BY `id` DESC LIMIT 1");
                ResultSet rs = ps.executeQuery();
                var5 = null;

                try {
                    if (rs.next()) {
                        String SN = rs.getString("id");
                        int sns = Integer.parseInt(SN);
                        ++sns;
                        ID = sns;
                        ps.close();
                    }
                } catch (Throwable var61) {
                    var5 = var61;
                    throw var61;
                } finally {
                    if (rs != null) {
                        if (var5 != null) {
                            try {
                                rs.close();
                            } catch (Throwable var56) {
                                var5.addSuppressed(var56);
                            }
                        } else {
                            rs.close();
                        }
                    }

                }

                ps.close();
            } catch (SQLException var63) {
            }

            try {
                con = DBConPool.getConnection();
                Throwable var64 = null;

                try {
                    PreparedStatement ps = con.prepareStatement("INSERT INTO jiezoudashi ( id,name,Val ) VALUES ( ? ,?,?)");
                    var5 = null;

                    try {
                        ps.setInt(1, ID);
                        ps.setString(2, this.c.getPlayer().getName() + "2");
                        ps.setInt(3, 0);
                        ps.executeUpdate();
                        Start.读取技个人信息设置();
                    } catch (Throwable var55) {
                        var5 = var55;
                        throw var55;
                    } finally {
                        if (ps != null) {
                            if (var5 != null) {
                                try {
                                    ps.close();
                                } catch (Throwable var54) {
                                    var5.addSuppressed(var54);
                                }
                            } else {
                                ps.close();
                            }
                        }

                    }
                } catch (Throwable var58) {
                    var64 = var58;
                    throw var58;
                } finally {
                    if (con != null) {
                        if (var64 != null) {
                            try {
                                con.close();
                            } catch (Throwable var53) {
                                var64.addSuppressed(var53);
                            }
                        } else {
                            con.close();
                        }
                    }

                }
            } catch (SQLException var60) {
            }
        }

        return (Integer)Start.个人信息设置.get("" + this.c.getPlayer().getName() + "2");
    }

    public void 打开个人决斗() {
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            Connection con = DBConPool.getConnection();
            ps = con.prepareStatement("SELECT * FROM jiezoudashi WHERE name = ?");
            ps.setString(1, this.c.getPlayer().getName() + "2");
            rs = ps.executeQuery();
            if (rs.next()) {
                String sqlString1 = null;
                sqlString1 = "update jiezoudashi set Val =  '1' where name = '" + this.c.getPlayer().getName() + "2';";
                PreparedStatement Val = con.prepareStatement(sqlString1);
                Val.executeUpdate(sqlString1);
                Start.读取技个人信息设置();
            }
        } catch (SQLException var6) {
        }

    }

    public void 关闭个人决斗() {
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            Connection con = DBConPool.getConnection();
            ps = con.prepareStatement("SELECT * FROM jiezoudashi WHERE name = ?");
            ps.setString(1, this.c.getPlayer().getName() + "2");
            rs = ps.executeQuery();
            if (rs.next()) {
                String sqlString1 = null;
                sqlString1 = "update jiezoudashi set Val = '0' where name = '" + this.c.getPlayer().getName() + "2';";
                PreparedStatement Val = con.prepareStatement(sqlString1);
                Val.executeUpdate(sqlString1);
                Start.读取技个人信息设置();
            }
        } catch (SQLException var6) {
        }

    }

    public int 显示伤害详细() {
        if (Start.个人信息设置.get("" + this.c.getPlayer().getName() + "1") == null) {
            int ID = 0;

            Connection con;
            Throwable var5;
            try {
                con = DBConPool.getConnection();
                PreparedStatement ps = con.prepareStatement("SELECT `id` FROM jiezoudashi  ORDER BY `id` DESC LIMIT 1");
                ResultSet rs = ps.executeQuery();
                var5 = null;

                try {
                    if (rs.next()) {
                        String SN = rs.getString("id");
                        int sns = Integer.parseInt(SN);
                        ++sns;
                        ID = sns;
                        ps.close();
                    }
                } catch (Throwable var61) {
                    var5 = var61;
                    throw var61;
                } finally {
                    if (rs != null) {
                        if (var5 != null) {
                            try {
                                rs.close();
                            } catch (Throwable var56) {
                                var5.addSuppressed(var56);
                            }
                        } else {
                            rs.close();
                        }
                    }

                }

                ps.close();
            } catch (SQLException var63) {
            }

            try {
                con = DBConPool.getConnection();
                Throwable var64 = null;

                try {
                    PreparedStatement ps = con.prepareStatement("INSERT INTO jiezoudashi ( id,name,Val ) VALUES ( ? ,?,?)");
                    var5 = null;

                    try {
                        ps.setInt(1, ID);
                        ps.setString(2, this.c.getPlayer().getName() + "1");
                        ps.setInt(3, 0);
                        ps.executeUpdate();
                        Start.读取技个人信息设置();
                    } catch (Throwable var55) {
                        var5 = var55;
                        throw var55;
                    } finally {
                        if (ps != null) {
                            if (var5 != null) {
                                try {
                                    ps.close();
                                } catch (Throwable var54) {
                                    var5.addSuppressed(var54);
                                }
                            } else {
                                ps.close();
                            }
                        }

                    }
                } catch (Throwable var58) {
                    var64 = var58;
                    throw var58;
                } finally {
                    if (con != null) {
                        if (var64 != null) {
                            try {
                                con.close();
                            } catch (Throwable var53) {
                                var64.addSuppressed(var53);
                            }
                        } else {
                            con.close();
                        }
                    }

                }
            } catch (SQLException var60) {
            }
        }

        return (Integer)Start.个人信息设置.get("" + this.c.getPlayer().getName() + "1");
    }

    public void 打开伤害详细() {
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            Connection con = DBConPool.getConnection();
            ps = con.prepareStatement("SELECT * FROM jiezoudashi WHERE name = ?");
            ps.setString(1, this.c.getPlayer().getName() + "1");
            rs = ps.executeQuery();
            if (rs.next()) {
                String sqlString1 = null;
                sqlString1 = "update jiezoudashi set Val = '1' where name = '" + this.c.getPlayer().getName() + "1';";
                PreparedStatement Val = con.prepareStatement(sqlString1);
                Val.executeUpdate(sqlString1);
                Start.读取技个人信息设置();
            }
        } catch (SQLException var6) {
        }

    }

    public void 关闭伤害详细() {
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            Connection con = DBConPool.getConnection();
            ps = con.prepareStatement("SELECT * FROM jiezoudashi WHERE name = ?");
            ps.setString(1, this.c.getPlayer().getName() + "1");
            rs = ps.executeQuery();
            if (rs.next()) {
                String sqlString1 = null;
                sqlString1 = "update jiezoudashi set Val = '0' where name = '" + this.c.getPlayer().getName() + "1';";
                PreparedStatement Val = con.prepareStatement(sqlString1);
                Val.executeUpdate(sqlString1);
                Start.读取技个人信息设置();
            }
        } catch (SQLException var6) {
        }

    }

    public int 显示群聊天开关() {
        if (Start.个人信息设置.get("" + this.c.getPlayer().getName() + "10") == null) {
            int ID = 0;

            Connection con;
            Throwable var5;
            try {
                con = DBConPool.getConnection();
                PreparedStatement ps = con.prepareStatement("SELECT `id` FROM jiezoudashi  ORDER BY `id` DESC LIMIT 1");
                ResultSet rs = ps.executeQuery();
                var5 = null;

                try {
                    if (rs.next()) {
                        String SN = rs.getString("id");
                        int sns = Integer.parseInt(SN);
                        ++sns;
                        ID = sns;
                        ps.close();
                    }
                } catch (Throwable var61) {
                    var5 = var61;
                    throw var61;
                } finally {
                    if (rs != null) {
                        if (var5 != null) {
                            try {
                                rs.close();
                            } catch (Throwable var56) {
                                var5.addSuppressed(var56);
                            }
                        } else {
                            rs.close();
                        }
                    }

                }

                ps.close();
            } catch (SQLException var63) {
            }

            try {
                con = DBConPool.getConnection();
                Throwable var64 = null;

                try {
                    PreparedStatement ps = con.prepareStatement("INSERT INTO jiezoudashi ( id,name,Val ) VALUES ( ? ,?,?)");
                    var5 = null;

                    try {
                        ps.setInt(1, ID);
                        ps.setString(2, this.c.getPlayer().getName() + "10");
                        ps.setInt(3, 0);
                        ps.executeUpdate();
                        Start.读取技个人信息设置();
                    } catch (Throwable var55) {
                        var5 = var55;
                        throw var55;
                    } finally {
                        if (ps != null) {
                            if (var5 != null) {
                                try {
                                    ps.close();
                                } catch (Throwable var54) {
                                    var5.addSuppressed(var54);
                                }
                            } else {
                                ps.close();
                            }
                        }

                    }
                } catch (Throwable var58) {
                    var64 = var58;
                    throw var58;
                } finally {
                    if (con != null) {
                        if (var64 != null) {
                            try {
                                con.close();
                            } catch (Throwable var53) {
                                var64.addSuppressed(var53);
                            }
                        } else {
                            con.close();
                        }
                    }

                }
            } catch (SQLException var60) {
            }
        }

        return (Integer)Start.个人信息设置.get("" + this.c.getPlayer().getName() + "10");
    }

    public void 打开群聊显示() {
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            Connection con = DBConPool.getConnection();
            ps = con.prepareStatement("SELECT * FROM jiezoudashi WHERE name = ?");
            ps.setString(1, this.c.getPlayer().getName() + "10");
            rs = ps.executeQuery();
            if (rs.next()) {
                String sqlString1 = null;
                sqlString1 = "update jiezoudashi set Val = '1' where name = '" + this.c.getPlayer().getName() + "10';";
                PreparedStatement Val = con.prepareStatement(sqlString1);
                Val.executeUpdate(sqlString1);
                Start.读取技个人信息设置();
            }
        } catch (SQLException var6) {
        }

    }

    public void 关闭群聊显示() {
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            Connection con = DBConPool.getConnection();
            ps = con.prepareStatement("SELECT * FROM jiezoudashi WHERE name = ?");
            ps.setString(1, this.c.getPlayer().getName() + "10");
            rs = ps.executeQuery();
            if (rs.next()) {
                String sqlString1 = null;
                sqlString1 = "update jiezoudashi set Val = '0' where name = '" + this.c.getPlayer().getName() + "10';";
                PreparedStatement Val = con.prepareStatement(sqlString1);
                Val.executeUpdate(sqlString1);
                Start.读取技个人信息设置();
            }
        } catch (SQLException var6) {
        }

    }

    public int 显示群聊天开关2() {
        if (Start.个人信息设置.get("" + this.c.getPlayer().getName() + "11") == null) {
            int ID = 0;

            Connection con;
            Throwable var5;
            try {
                con = DBConPool.getConnection();
                PreparedStatement ps = con.prepareStatement("SELECT `id` FROM jiezoudashi  ORDER BY `id` DESC LIMIT 1");
                ResultSet rs = ps.executeQuery();
                var5 = null;

                try {
                    if (rs.next()) {
                        String SN = rs.getString("id");
                        int sns = Integer.parseInt(SN);
                        ++sns;
                        ID = sns;
                        ps.close();
                    }
                } catch (Throwable var61) {
                    var5 = var61;
                    throw var61;
                } finally {
                    if (rs != null) {
                        if (var5 != null) {
                            try {
                                rs.close();
                            } catch (Throwable var56) {
                                var5.addSuppressed(var56);
                            }
                        } else {
                            rs.close();
                        }
                    }

                }

                ps.close();
            } catch (SQLException var63) {
            }

            try {
                con = DBConPool.getConnection();
                Throwable var64 = null;

                try {
                    PreparedStatement ps = con.prepareStatement("INSERT INTO jiezoudashi ( id,name,Val ) VALUES ( ? ,?,?)");
                    var5 = null;

                    try {
                        ps.setInt(1, ID);
                        ps.setString(2, this.c.getPlayer().getName() + "11");
                        ps.setInt(3, 0);
                        ps.executeUpdate();
                        Start.读取技个人信息设置();
                    } catch (Throwable var55) {
                        var5 = var55;
                        throw var55;
                    } finally {
                        if (ps != null) {
                            if (var5 != null) {
                                try {
                                    ps.close();
                                } catch (Throwable var54) {
                                    var5.addSuppressed(var54);
                                }
                            } else {
                                ps.close();
                            }
                        }

                    }
                } catch (Throwable var58) {
                    var64 = var58;
                    throw var58;
                } finally {
                    if (con != null) {
                        if (var64 != null) {
                            try {
                                con.close();
                            } catch (Throwable var53) {
                                var64.addSuppressed(var53);
                            }
                        } else {
                            con.close();
                        }
                    }

                }
            } catch (SQLException var60) {
            }
        }

        return (Integer)Start.个人信息设置.get("" + this.c.getPlayer().getName() + "11");
    }

    public void 打开群聊显示2() {
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            Connection con = DBConPool.getConnection();
            ps = con.prepareStatement("SELECT * FROM jiezoudashi WHERE name = ?");
            ps.setString(1, this.c.getPlayer().getName() + "11");
            rs = ps.executeQuery();
            if (rs.next()) {
                String sqlString1 = null;
                sqlString1 = "update jiezoudashi set Val = '1' where name = '" + this.c.getPlayer().getName() + "11';";
                PreparedStatement Val = con.prepareStatement(sqlString1);
                Val.executeUpdate(sqlString1);
                Start.读取技个人信息设置();
            }
        } catch (SQLException var6) {
        }

    }

    public void 关闭群聊显示2() {
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            Connection con = DBConPool.getConnection();
            ps = con.prepareStatement("SELECT * FROM jiezoudashi WHERE name = ?");
            ps.setString(1, this.c.getPlayer().getName() + "11");
            rs = ps.executeQuery();
            if (rs.next()) {
                String sqlString1 = null;
                sqlString1 = "update jiezoudashi set Val = '0' where name = '" + this.c.getPlayer().getName() + "11';";
                PreparedStatement Val = con.prepareStatement(sqlString1);
                Val.executeUpdate(sqlString1);
                Start.读取技个人信息设置();
            }
        } catch (SQLException var6) {
        }

    }

    public void 录入手册(int a, int b, int c) {
        try {
            Connection con = DBConPool.getConnection();
            PreparedStatement ps = con.prepareStatement("INSERT INTO shouce (b,c,d) VALUES ( ?, ?, ?)");
            ps.setInt(1, a);
            ps.setInt(2, b);
            ps.setInt(3, c);
            ps.execute();
            ps.close();
        } catch (SQLException var6) {
            服务端输出信息.println_err("录入手册!!55" + var6);
        }

    }

    public String 显示手册内容(int a) {
        StringBuilder name = new StringBuilder();

        try {
            Connection con = DBConPool.getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT * FROM shouce order by c desc");
            ResultSet rs = ps.executeQuery();

            while(true) {
                do {
                    if (!rs.next()) {
                        ps.close();
                        return name.toString();
                    }
                } while(rs.getInt("d") != a);

                int 编号 = rs.getInt("a");
                int 代码 = rs.getInt("b");
                int 数量 = rs.getInt("c");
                String 物品名字 = MapleItemInformationProvider.getInstance().getName(rs.getInt("b"));
                String 物品数量 = "" + 数量 + "";
                name.append("#L").append(编号).append("##b#z").append(代码).append("##k ");

                int j;
                for(j = 21 - 物品名字.getBytes().length; j > 0; --j) {
                    name.append(" ");
                }

                name.append("│册 #r").append(数量).append("#k");

                for(j = 8 - 物品数量.getBytes().length; j > 0; --j) {
                    name.append(" ");
                }

                name.append("│包 #b#c").append(代码).append("##k#l\r\n");
            }
        } catch (SQLException var12) {
            return name.toString();
        }
    }

    public static int 手册道具代码(int id) {
        int data = 0;

        try {
            Connection con = DBConPool.getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT b as DATA FROM shouce WHERE a = ?");
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            Throwable var5 = null;

            try {
                if (rs.next()) {
                    data = rs.getInt("DATA");
                }
            } catch (Throwable var15) {
                var5 = var15;
                throw var15;
            } finally {
                if (rs != null) {
                    if (var5 != null) {
                        try {
                            rs.close();
                        } catch (Throwable var14) {
                            var5.addSuppressed(var14);
                        }
                    } else {
                        rs.close();
                    }
                }

            }

            ps.close();
        } catch (SQLException var17) {
            服务端输出信息.println_err("手册道具代码、出错");
        }

        return data;
    }

    public static int 手册道具数量(int id) {
        int data = 0;

        try {
            Connection con = DBConPool.getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT c as DATA FROM shouce WHERE a = ?");
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            Throwable var5 = null;

            try {
                if (rs.next()) {
                    data = rs.getInt("DATA");
                }
            } catch (Throwable var15) {
                var5 = var15;
                throw var15;
            } finally {
                if (rs != null) {
                    if (var5 != null) {
                        try {
                            rs.close();
                        } catch (Throwable var14) {
                            var5.addSuppressed(var14);
                        }
                    } else {
                        rs.close();
                    }
                }

            }

            ps.close();
        } catch (SQLException var17) {
            服务端输出信息.println_err("手册道具数量、出错");
        }

        return data;
    }

    public static void 修改手册道具数量(int a, int b) {
        try {
            Connection con = DBConPool.getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT * FROM shouce");
            ResultSet rs = ps.executeQuery();

            while(rs.next()) {
                if (rs.getString("a").equals("" + a + "")) {
                    String aa = null;
                    aa = "update shouce set c =" + b + " where a = " + a + ";";
                    PreparedStatement S = con.prepareStatement(aa);
                    S.executeUpdate(aa);
                }
            }

            ps.close();
        } catch (SQLException var7) {
        }

    }

    public static void 删除不存在() {
        PreparedStatement ps1 = null;
        ResultSet rs = null;

        try {
            Connection con = DBConPool.getConnection();
            ps1 = con.prepareStatement("SELECT * FROM shouce");
            rs = ps1.executeQuery();

            while(rs.next()) {
                String sqlstr = " delete from shouce where c = 0";
                ps1.executeUpdate(sqlstr);
            }

            ps1.close();
        } catch (SQLException var4) {
        }

    }

    public static void 新增手册收藏(int a, int b, int c) {
        try {
            Connection con = DBConPool.getConnection();
            Throwable var4 = null;

            try {
                PreparedStatement ps = con.prepareStatement("INSERT INTO shouce ( b,c,d ) VALUES ( ? ,?,? )");
                Throwable var6 = null;

                try {
                    ps.setInt(1, a);
                    ps.setInt(2, b);
                    ps.setInt(3, c);
                    ps.executeUpdate();
                    ps.close();
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
        }

    }

    public int OX道题活动() {
        return MapleParty.OX答题活动;
    }

    public int 雪球赛() {
        return MapleParty.雪球赛;
    }

    public String 幸运职业() {
        String 职业1 = MapleCarnivalChallenge.getJobNameById(MapleParty.幸运职业 - 1);
        String 职业2 = MapleCarnivalChallenge.getJobNameById(MapleParty.幸运职业);
        String 职业3 = MapleCarnivalChallenge.getJobNameById(MapleParty.幸运职业 + 1);
        String 职业 = 职业1 + "," + 职业2 + "," + 职业3;
        return 职业;
    }

    public String 显示修炼(int a) {
        StringBuilder name = new StringBuilder();
        if (this.c.getPlayer().getLevel() >= 160) {
            Start.读取技个人信息设置();
            DecimalFormat 精确显示 = new DecimalFormat("##0.0000");
            DecimalFormat 精确显示2 = new DecimalFormat("###");
            int 契合 = (Integer)Start.个人信息设置.get("BUFF增益" + a + "");
            int 物理攻击力 = (Integer)Start.个人信息设置.get("物理攻击力" + a + "");
            int 魔法攻击力 = (Integer)Start.个人信息设置.get("魔法攻击力" + a + "");
            String 阶级 = "";
            if (契合 > 50 && 契合 <= 100) {
                阶级 = "初级";
            } else if (契合 > 100 && 契合 <= 150) {
                阶级 = "中级";
            } else if (契合 > 150 && 契合 <= 200) {
                阶级 = "高级";
            } else {
                阶级 = "入门";
            }

            name.append("状态:#b仙人模式 : #r").append(阶级).append("#k\r\n\r\n");
            name.append("    #d开启此状态，各项属性得到巨额增涨，各属性值可通过修炼提升。#k\r\n");
            name.append("\r\n状态维持；\r\n");
            if (契合 > 50 && 契合 <= 100) {
                name.append("→ 每 #r5#k 秒消耗 #r15%#k 的最大法力值,最大生命值\r\n");
            } else if (契合 > 100 && 契合 <= 150) {
                name.append("→ 每 #r4#k 秒消耗 #r15%#k 的最大法力值,最大生命值\r\n");
            } else if (契合 > 150) {
                name.append("→ 每 #r4#k 秒消耗 #r20%#k 的最大法力值,最大生命值\r\n");
            } else {
                name.append("→ 每 #r5#k 秒消耗 #r10%#k 的最大法力值,最大生命值\r\n");
            }

            name.append("→ 每 #r20000#b(-").append(精确显示.format((double)((Integer)Start.个人信息设置.get("聪明睿智" + a + "") * (Integer)Start.个人信息设置.get("BUFF增益" + a + "")) * 1.0E-4)).append(")#k毫秒获取增益BUFF\r\n");
            name.append("→ 增加 #r30%#k 受到的伤害\r\n");
            name.append("→ 增加 #r30#b(").append(精确显示.format((double)(物理攻击力 + 魔法攻击力) * 0.5 * (double)契合 * 1.0E-5)).append(")#r%#k 对高级怪物的伤害\r\n");
            name.append("\r\n状态属性；\r\n");
            String 介绍1 = "#d能力契合#k:#r" + 契合 + "#k";
            name.append(介绍1);

            for(int j = 43 - 介绍1.getBytes().length; j > 0; --j) {
                name.append(" ");
            }

            name.append("仙人模式熟练度\r\n");
            String 介绍2 = "#d硬化皮肤#k:#b" + 精确显示.format((double)((Integer)Start.个人信息设置.get("硬化皮肤" + a + "") * 契合) * 0.01) + "#r(" + Start.个人信息设置.get("硬化皮肤" + a + "") + ")#k";
            name.append(介绍2);

            for(int j = 45 - 介绍2.getBytes().length; j > 0; --j) {
                name.append(" ");
            }

            name.append("减少受到的伤害\r\n");
            String 介绍5 = "#d聪明睿智#k:#b" + 精确显示.format((double)((Integer)Start.个人信息设置.get("聪明睿智" + a + "") * 契合) * 0.01) + "#r(" + Start.个人信息设置.get("聪明睿智" + a + "") + ")#k";
            name.append(介绍5);

            for(int j = 45 - 介绍5.getBytes().length; j > 0; --j) {
                name.append(" ");
            }

            name.append("加快增益BUFF获取\r\n");
            String 介绍6 = "#d攻击力(物理)#k:#b" + 精确显示2.format((double)((Integer)Start.个人信息设置.get("物理攻击力" + a + "") * 契合) * 0.001) + "#r(" + Start.个人信息设置.get("物理攻击力" + a + "") + ")#k";
            name.append(介绍6);

            for(int j = 45 - 介绍6.getBytes().length; j > 0; --j) {
                name.append(" ");
            }

            name.append("附加物理真实伤害\r\n");
            String 介绍7 = "#d攻击力(魔法)#k:#b" + 精确显示2.format((double)((Integer)Start.个人信息设置.get("魔法攻击力" + a + "") * 契合) * 0.001) + "#r(" + Start.个人信息设置.get("魔法攻击力" + a + "") + ")#k";
            name.append(介绍7);

            for(int j = 45 - 介绍7.getBytes().length; j > 0; --j) {
                name.append(" ");
            }

            name.append("附加魔法真实伤害\r\n");
            String 介绍8 = "#d狂暴力(物理)#k:#b" + 精确显示.format((double)((Integer)Start.个人信息设置.get("物理狂暴力" + a + "") * 契合) * 0.002) + "#r(" + Start.个人信息设置.get("物理狂暴力" + a + "") + ")#k";
            name.append(介绍8);

            for(int j = 45 - 介绍8.getBytes().length; j > 0; --j) {
                name.append(" ");
            }

            name.append("物理真实伤害暴击\r\n");
            String 介绍9 = "#d狂暴力(魔法)#k:#b" + 精确显示.format((double)((Integer)Start.个人信息设置.get("魔法狂暴力" + a + "") * 契合) * 0.002) + "#r(" + Start.个人信息设置.get("魔法狂暴力" + a + "") + ")#k";
            name.append(介绍9);

            for(int j = 45 - 介绍9.getBytes().length; j > 0; --j) {
                name.append(" ");
            }

            name.append("魔法真实伤害暴击\r\n");
            String 介绍10 = "#d吸收力(物理)#k:#b" + 精确显示.format((double)((Integer)Start.个人信息设置.get("物理吸收力" + a + "") * 契合) * 0.002) + "#r(" + Start.个人信息设置.get("物理吸收力" + a + "") + ")#k";
            name.append(介绍10);

            for(int j = 45 - 介绍10.getBytes().length; j > 0; --j) {
                name.append(" ");
            }

            name.append("攻击时恢复HP\r\n");
            String 介绍11 = "#d吸收力(魔法)#k:#b" + 精确显示.format((double)((Integer)Start.个人信息设置.get("魔法吸收力" + a + "") * 契合) * 0.002) + "#r(" + Start.个人信息设置.get("魔法吸收力" + a + "") + ")#k";
            name.append(介绍11);

            for(int j = 45 - 介绍11.getBytes().length; j > 0; --j) {
                name.append(" ");
            }

            name.append("攻击时恢复MP\r\n\r\n");
            name.append("#r#L3#修炼契合力#l\r\n");
            name.append("#r#L4#修炼硬化皮肤#l\r\n");
            name.append("#b#L1#修炼攻击力[物理]#l\r\n");
            name.append("#b#L2#修炼攻击力[魔法]#l\r\n");
        } else {
            name.append(" 角色 #b160#k 级后即可觉醒 #b仙人之力#k");
        }

        return name.toString();
    }

    public void 修炼硬化皮肤() {
        this.c.getPlayer().修炼硬化皮肤();
    }

    public void 修炼物理攻击力() {
        this.c.getPlayer().修炼物理攻击力();
    }

    public void 修炼BUFF增益() {
        this.c.getPlayer().修炼BUFF增益();
    }

    public void 修炼魔法攻击力() {
        this.c.getPlayer().修炼魔法攻击力();
    }

   // public int 挂机检测验证码() {
      //  return this.c.getPlayer().挂机检测验证码;
    //}

    public void 重置验证码() {
       // this.c.getPlayer().挂机检测验证码 = 0;
        this.c.getPlayer().b = 0;
        this.c.getPlayer().d = 0;
    }

    public void 学习仙人模式(String a, int b) {
        if (Start.个人信息设置.get(a) == null) {
            int ID = 0;

            Connection con;
            Throwable var7;
            try {
                con = DBConPool.getConnection();
                PreparedStatement ps = con.prepareStatement("SELECT `id` FROM jiezoudashi  ORDER BY `id` DESC LIMIT 1");
                ResultSet rs = ps.executeQuery();
                var7 = null;

                try {
                    if (rs.next()) {
                        String SN = rs.getString("id");
                        int sns = Integer.parseInt(SN);
                        ++sns;
                        ID = sns;
                        ps.close();
                    }
                } catch (Throwable var63) {
                    var7 = var63;
                    throw var63;
                } finally {
                    if (rs != null) {
                        if (var7 != null) {
                            try {
                                rs.close();
                            } catch (Throwable var58) {
                                var7.addSuppressed(var58);
                            }
                        } else {
                            rs.close();
                        }
                    }

                }

                ps.close();
            } catch (SQLException var65) {
            }

            try {
                con = DBConPool.getConnection();
                Throwable var66 = null;

                try {
                    PreparedStatement ps = con.prepareStatement("INSERT INTO jiezoudashi ( id,name,Val ) VALUES ( ? ,?,?)");
                    var7 = null;

                    try {
                        ps.setInt(1, ID);
                        ps.setString(2, a);
                        ps.setInt(3, b);
                        ps.executeUpdate();
                        Start.读取技个人信息设置();
                    } catch (Throwable var57) {
                        var7 = var57;
                        throw var57;
                    } finally {
                        if (ps != null) {
                            if (var7 != null) {
                                try {
                                    ps.close();
                                } catch (Throwable var56) {
                                    var7.addSuppressed(var56);
                                }
                            } else {
                                ps.close();
                            }
                        }

                    }
                } catch (Throwable var60) {
                    var66 = var60;
                    throw var60;
                } finally {
                    if (con != null) {
                        if (var66 != null) {
                            try {
                                con.close();
                            } catch (Throwable var55) {
                                var66.addSuppressed(var55);
                            }
                        } else {
                            con.close();
                        }
                    }

                }
            } catch (SQLException var62) {
            }
        }

    }

    public String 显示银行列表() {
        StringBuilder name = new StringBuilder();

        try {
            Connection con = DBConPool.getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT * FROM yinhang_1 ");
            ResultSet rs = ps.executeQuery();

            while(rs.next()) {
                int 编号 = rs.getInt("a");
                String 银行名字 = rs.getString("b");
                name.append("#L").append(编号).append("##b#z").append(银行名字).append("##k ");
            }

            ps.close();
        } catch (SQLException var7) {
        }

        return name.toString();
    }

    public String 显示银行面板(int a) {
        StringBuilder name = new StringBuilder();

        try {
            Connection con = DBConPool.getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT * FROM yinhang_1 ");
            ResultSet rs = ps.executeQuery();

            while(rs.next()) {
                int 编号 = rs.getInt("a");
                String 银行名字 = rs.getString("b");
                name.append("#L").append(编号).append("##b#z").append(银行名字).append("##k ");
            }

            ps.close();
        } catch (SQLException var8) {
        }

        return name.toString();
    }

    public String 显示雇佣商人物品() {
        StringBuilder name = new StringBuilder();
        byte state = this.checkExistance(this.c.getPlayer().getAccountID(), this.c.getPlayer().getId());
        boolean merch = World.hasMerchant(this.c.getPlayer().getAccountID());
        if (!merch && state != 1 && this.角色ID取雇佣数据(this.c.getPlayer().getId()) > 0) {
            name.append("   雇佣异常消失物品领回，请保证你背包装得下，不然会消失哦。如果发现道具数量不对，或者属性不对，请联系管理员。无法恢复镶嵌装备。\r\n\r\n");

            try {
                Connection con = DBConPool.getConnection();
                PreparedStatement ps = con.prepareStatement("SELECT * FROM hire where cid =" + this.c.getPlayer().getId() + "");
                ResultSet rs = ps.executeQuery();

                while(rs.next()) {
                    if (rs.getInt("itemid") < 2000000) {
                        name.append("#v").append(rs.getInt("itemid")).append("# #b#t").append(rs.getInt("itemid")).append("##k x 1\r\n");
                    } else {
                        name.append("#v").append(rs.getInt("itemid")).append("# #b#t").append(rs.getInt("itemid")).append("##k x ").append(rs.getInt("potential3")).append("\r\n");
                    }
                }

                ps.close();
            } catch (SQLException var7) {
            }

            name.append("\r\n#b#L1#[领回道具]#l");
        } else {
            name.append("你没有发生过异常。");
        }

        return name.toString();
    }

    public void 领回雇佣道具() {
        try {
            Connection con = DBConPool.getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT * FROM hire where cid =" + this.c.getPlayer().getId() + "");

            for(ResultSet rs = ps.executeQuery(); rs.next(); this.删除道具(this.c.getPlayer().getId())) {
                if (rs.getInt("itemid") < 2000000) {
                    MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
                    Equip item = (Equip)ii.getEquipById(rs.getInt("itemid"));
                    if (rs.getInt("upgradeslots") > 0) {
                        item.setUpgradeSlots((byte)rs.getInt("upgradeslots"));
                    }

                    if (rs.getInt("str") > 0) {
                        item.setStr((short)rs.getInt("str"));
                    }

                    if (rs.getInt("dex") > 0) {
                        item.setDex((short)rs.getInt("dex"));
                    }

                    if (rs.getInt("2int") > 0) {
                        item.setInt((short)rs.getInt("2int"));
                    }

                    if (rs.getInt("luk") > 0) {
                        item.setLuk((short)rs.getInt("luk"));
                    }

                    if (rs.getInt("watk") > 0) {
                        item.setWatk((short)rs.getInt("watk"));
                    }

                    if (rs.getInt("matk") > 0) {
                        item.setMatk((short)rs.getInt("matk"));
                    }

                    if (rs.getInt("wdef") > 0) {
                        item.setWdef((short)rs.getInt("wdef"));
                    }

                    if (rs.getInt("mdef") > 0) {
                        item.setMdef((short)rs.getInt("mdef"));
                    }

                    if (rs.getInt("hp") > 0) {
                        item.setHp((short)rs.getInt("hp"));
                    }

                    if (rs.getInt("mp") > 0) {
                        item.setMp((short)rs.getInt("mp"));
                    }

                    if (rs.getInt("acc") > 0) {
                        item.setAcc((short)rs.getInt("acc"));
                    }

                    if (rs.getInt("avoid") > 0) {
                        item.setAvoid((short)rs.getInt("avoid"));
                    }

                    if (rs.getInt("speed") > 0) {
                        item.setSpeed((short)rs.getInt("speed"));
                    }

                    if (rs.getInt("jump") > 0) {
                        item.setJump((short)rs.getInt("jump"));
                    }

                    MapleInventoryManipulator.addbyItem(this.c.getPlayer().getClient(), item.copy());
                } else {
                    this.gainItem(rs.getInt("itemid"),((short)rs.getInt("upgradeslots")));
                }
            }
        } catch (SQLException var6) {
        }

    }

    public void 删除道具(int a1) {
        try {
            ResultSet rs = null;
            PreparedStatement ps = null;
            Connection con = DBConPool.getConnection();
            ps = con.prepareStatement("SELECT * FROM hire");
            rs = ps.executeQuery();
            if (rs.next()) {
                String sqlstr = " delete from hire where cid = " + a1 + " ";
                ps.executeUpdate(sqlstr);
                ps.close();
            }
        } catch (SQLException var7) {
        }

    }

    private final byte checkExistance(int accid, int charid) {
        try {
            Connection con = DBConPool.getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT * from hiredmerch where accountid = ? OR characterid = ?");
            ps.setInt(1, accid);
            ps.setInt(2, charid);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                ps.close();
                rs.close();
                return 1;
            } else {
                rs.close();
                ps.close();
                return 0;
            }
        } catch (SQLException var6) {
            return -1;
        }
    }

    public int 角色ID取雇佣数据(int id) {
        int data = 0;

        try {
            Connection con = DBConPool.getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT cid as DATA FROM hire WHERE cid = ?");
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            Throwable var6 = null;

            try {
                if (rs.next()) {
                    data = rs.getInt("DATA");
                }
            } catch (Throwable var16) {
                var6 = var16;
                throw var16;
            } finally {
                if (rs != null) {
                    if (var6 != null) {
                        try {
                            rs.close();
                        } catch (Throwable var15) {
                            var6.addSuppressed(var15);
                        }
                    } else {
                        rs.close();
                    }
                }

            }

            ps.close();
        } catch (SQLException var18) {
            服务端输出信息.println_err("角色名字取账号ID、出错");
        }

        return data;
    }

    public void 删除雇佣缓存() {
        try {
            ResultSet rs = null;
            PreparedStatement ps = null;
            Connection con = DBConPool.getConnection();
            ps = con.prepareStatement("SELECT * FROM hiredmerch");
            rs = ps.executeQuery();
            if (rs.next()) {
                String sqlstr = " delete from hiredmerch where accountid = " + this.c.getPlayer().getAccountID() + " ";
                ps.executeUpdate(sqlstr);
                ps.close();
            }
        } catch (SQLException var6) {
        }

    }

    public String 显示镶嵌效果(String a) {
        StringBuilder name = new StringBuilder();
        String[] arr1 = a.split(",");

        for(int i = 0; i < arr1.length; ++i) {
            String pair = arr1[i];
            if (pair.contains(":")) {
                String kongInfo = "●";
                String[] arr2 = pair.split(":");
                int fumoType = Integer.parseInt(arr2[0]);
                int fumoVal = Integer.parseInt(arr2[1]);
                if (fumoType > 0 && Start.FuMoInfoMap.containsKey(fumoType)) {
                    String[] infoArr = (String[])Start.FuMoInfoMap.get(fumoType);
                    String fumoName = infoArr[0];
                    String fumoInfo = infoArr[1];
                    kongInfo = kongInfo + fumoName + " " + String.format(fumoInfo, fumoVal);
                } else {
                    kongInfo = kongInfo + "[未镶嵌]";
                }

                name.append("镶嵌 : ").append(kongInfo);
            }
        }

        return name.toString();
    }

    public String 显示在线玩家() {
        StringBuilder name = new StringBuilder();
        Iterator var2 = ChannelServer.getAllInstances().iterator();

        while(var2.hasNext()) {
            ChannelServer cs = (ChannelServer)var2.next();

            for(Iterator var4 = cs.getPlayerStorage().getAllCharacters().iterator(); var4.hasNext(); name.append("#l\r\n")) {
                MapleCharacter chr = (MapleCharacter)var4.next();
                if (!abc.Game.主城(chr.getMapId())) {
                    name.append("#b#L").append(chr.getId()).append("# 玩家: #r").append(chr.getName()).append("  #b频道: #r" + chr.getClient().getChannel()).append("  #b位置: #r" + chr.getMap().getMapName());
                } else {
                    name.append("#b#L").append(chr.getId()).append("# 玩家: #d").append(chr.getName()).append("  #b频道: #d" + chr.getClient().getChannel()).append("  #b位置: #d" + chr.getMap().getMapName());
                }

                if (chr.isGM()) {
                    name.append("  #r(GM").append(chr.getGMLevel()).append(")");
                }
            }
        }

        return name.toString();
    }

    public String 显示在线玩家详细(int a) {
        StringBuilder name = new StringBuilder();
        Iterator var3 = ChannelServer.getAllInstances().iterator();

        while(var3.hasNext()) {
            ChannelServer cs = (ChannelServer)var3.next();
            Iterator var5 = cs.getPlayerStorage().getAllCharacters().iterator();

            while(var5.hasNext()) {
                MapleCharacter chr = (MapleCharacter)var5.next();
                if (chr != null && chr.getId() == a) {
                    if (!Game.主城(chr.getMapId())) {
                        name.append("\t\t#d玩家名字:  #r").append(chr.getName()).append(" #k (").append(chr.getId()).append(")\r\n");
                        name.append("\t\t#d玩家等级:  #r").append(chr.getLevel()).append(" #k (").append(MapleCarnivalChallenge.getJobNameById(chr.job)).append(")\r\n");
                        name.append("\t\t#d最高伤害:  #r").append(chr.最高伤害).append(" #k\r\n");
                        name.append("\t\t#d被攻击次:  #r").append(chr.被触碰次数).append(" #k\r\n");
                        name.append("\t\t#d累计掉血:  #r").append(chr.累计掉血).append(" #k\r\n");
                        name.append("\t\t#d所在地图:  [野外] #r").append(chr.getMap().getMapName()).append(" #k\r\n");
                        name.append("\t\t#d打怪数量:  #r").append(chr.打怪数量).append(" #k\r\n");
                        name.append("\t\t#d坐标误差:  #r").append(chr.X坐标误差).append(" #k(X)\r\n");
                        name.append("\t\t#d坐标误差:  #r").append(chr.Y坐标误差).append(" #k(Y)\r\n");
                        name.append("\t\t#d绑定QQ号:  #r").append(账号ID取绑定QQ(this.角色ID取账号ID(chr.getId()))).append(" #k\r\n");
                        name.append("\t\t\t   #b#L").append(chr.getId()).append("#[跟踪]#l  #L").append(chr.getId() + 1000000).append("#[封号]#l\r\n\r\n");
                        if (chr.地图缓存1 > 0 && !"".equals(chr.地图缓存时间1)) {
                            name.append("\t\t#d过图记录；\r\n");
                            name.append("\t\t#d进图时间:  #r").append(chr.地图缓存时间1).append("\r\n");
                            name.append("\t\t#d地图名称:  #r#m").append(chr.地图缓存1).append("##k\r\n");
                        }

                        if (chr.地图缓存2 > 0 && !"".equals(chr.地图缓存时间2)) {
                            name.append("\t\t#d进图时间:  #r").append(chr.地图缓存时间2).append("\r\n");
                            name.append("\t\t#d地图名称:  #r#m").append(chr.地图缓存2).append("##k\r\n");
                        }

                        if (chr.地图缓存3 > 0 && !"".equals(chr.地图缓存时间3)) {
                            name.append("\t\t#d进图时间:  #r").append(chr.地图缓存时间3).append("\r\n");
                            name.append("\t\t#d地图名称:  #r#m").append(chr.地图缓存3).append("##k\r\n");
                        }

                        if (chr.地图缓存4 > 0 && !"".equals(chr.地图缓存时间4)) {
                            name.append("\t\t#d进图时间:  #r").append(chr.地图缓存时间4).append("\r\n");
                            name.append("\t\t#d地图名称:  #r#m").append(chr.地图缓存4).append("##k\r\n");
                        }

                        if (chr.地图缓存5 > 0 && !"".equals(chr.地图缓存时间5)) {
                            name.append("\t\t#d进图时间:  #r").append(chr.地图缓存时间5).append("\r\n");
                            name.append("\t\t#d地图名称:  #r#m").append(chr.地图缓存5).append("##k\r\n");
                        }

                        if (chr.地图缓存6 > 0 && !"".equals(chr.地图缓存时间6)) {
                            name.append("\t\t#d进图时间:  #r").append(chr.地图缓存时间6).append("\r\n");
                            name.append("\t\t#d地图名称:  #r#m").append(chr.地图缓存6).append("##k\r\n");
                        }

                        if (chr.地图缓存7 > 0 && !"".equals(chr.地图缓存时间7)) {
                            name.append("\t\t#d进图时间:  #r").append(chr.地图缓存时间7).append("\r\n");
                            name.append("\t\t#d地图名称:  #r#m").append(chr.地图缓存7).append("##k\r\n");
                        }

                        if (chr.地图缓存8 > 0 && !"".equals(chr.地图缓存时间8)) {
                            name.append("\t\t#d进图时间:  #r").append(chr.地图缓存时间8).append("\r\n");
                            name.append("\t\t#d地图名称:  #r#m").append(chr.地图缓存8).append("##k\r\n");
                        }

                        if (chr.地图缓存9 > 0 && !"".equals(chr.地图缓存时间9)) {
                            name.append("\t\t#d进图时间:  #r").append(chr.地图缓存时间9).append("\r\n");
                            name.append("\t\t#d地图名称:  #r#m").append(chr.地图缓存9).append("##k\r\n");
                        }

                        if (chr.地图缓存10 > 0 && !"".equals(chr.地图缓存时间10)) {
                            name.append("\t\t#d进图时间:  #r").append(chr.地图缓存时间10).append("\r\n");
                            name.append("\t\t#d地图名称:  #r#m").append(chr.地图缓存10).append("##k\r\n");
                        }
                    } else {
                        name.append("\t\t#d玩家名字:  #r").append(chr.getName()).append(" #k (").append(chr.getId()).append(")\r\n");
                        name.append("\t\t#d玩家等级:  #r").append(chr.getLevel()).append(" #k (").append(MapleCarnivalChallenge.getJobNameById(chr.job)).append(")\r\n");
                        name.append("\t\t#d最高伤害:  #r").append(chr.最高伤害).append(" #k\r\n");
                        name.append("\t\t#d被攻击次:  #r").append(chr.被触碰次数).append(" #k\r\n");
                        name.append("\t\t#d累计掉血:  #r").append(chr.累计掉血).append(" #k\r\n");
                        name.append("\t\t#d过图间隔:  #rnull  #k\r\n");
                        name.append("\t\t#d所在地图:  [主城] #r").append(chr.getMap().getMapName()).append(" #k\r\n");
                        name.append("\t\t#d打怪数量:  #r").append(chr.打怪数量).append(" #k\r\n");
                        name.append("\t\t#d坐标误差:  #r").append(chr.X坐标误差).append(" #k(X)\r\n");
                        name.append("\t\t#d坐标误差:  #r").append(chr.Y坐标误差).append(" #k(Y)\r\n");
                        name.append("\t\t#d绑定QQ号:  #r").append(账号ID取绑定QQ(this.角色ID取账号ID(chr.getId()))).append(" #k\r\n");
                        name.append("\t\t\t   #b#L").append(chr.getId()).append("#[跟踪]#l  #L").append(chr.getId() + 1000000).append("#[封号]#l\r\n\r\n");
                        if (chr.地图缓存1 > 0 && !"".equals(chr.地图缓存时间1)) {
                            name.append("\t\t#d过图记录；\r\n");
                            name.append("\t\t#d进图时间:  #r").append(chr.地图缓存时间1).append("\r\n");
                            name.append("\t\t#d地图名称:  #r#m").append(chr.地图缓存1).append("##k\r\n");
                        }

                        if (chr.地图缓存2 > 0 && !"".equals(chr.地图缓存时间2)) {
                            name.append("\t\t#d进图时间:  #r").append(chr.地图缓存时间2).append("\r\n");
                            name.append("\t\t#d地图名称:  #r#m").append(chr.地图缓存2).append("##k\r\n");
                        }

                        if (chr.地图缓存3 > 0 && !"".equals(chr.地图缓存时间3)) {
                            name.append("\t\t#d进图时间:  #r").append(chr.地图缓存时间3).append("\r\n");
                            name.append("\t\t#d地图名称:  #r#m").append(chr.地图缓存3).append("##k\r\n");
                        }

                        if (chr.地图缓存4 > 0 && !"".equals(chr.地图缓存时间4)) {
                            name.append("\t\t#d进图时间:  #r").append(chr.地图缓存时间4).append("\r\n");
                            name.append("\t\t#d地图名称:  #r#m").append(chr.地图缓存4).append("##k\r\n");
                        }

                        if (chr.地图缓存5 > 0 && !"".equals(chr.地图缓存时间5)) {
                            name.append("\t\t#d进图时间:  #r").append(chr.地图缓存时间5).append("\r\n");
                            name.append("\t\t#d地图名称:  #r#m").append(chr.地图缓存5).append("##k\r\n");
                        }

                        if (chr.地图缓存6 > 0 && !"".equals(chr.地图缓存时间6)) {
                            name.append("\t\t#d进图时间:  #r").append(chr.地图缓存时间6).append("\r\n");
                            name.append("\t\t#d地图名称:  #r#m").append(chr.地图缓存6).append("##k\r\n");
                        }

                        if (chr.地图缓存7 > 0 && !"".equals(chr.地图缓存时间7)) {
                            name.append("\t\t#d进图时间:  #r").append(chr.地图缓存时间7).append("\r\n");
                            name.append("\t\t#d地图名称:  #r#m").append(chr.地图缓存7).append("##k\r\n");
                        }

                        if (chr.地图缓存8 > 0 && !"".equals(chr.地图缓存时间8)) {
                            name.append("\t\t#d进图时间:  #r").append(chr.地图缓存时间8).append("\r\n");
                            name.append("\t\t#d地图名称:  #r#m").append(chr.地图缓存8).append("##k\r\n");
                        }

                        if (chr.地图缓存9 > 0 && !"".equals(chr.地图缓存时间9)) {
                            name.append("\t\t#d进图时间:  #r").append(chr.地图缓存时间9).append("\r\n");
                            name.append("\t\t#d地图名称:  #r#m").append(chr.地图缓存9).append("##k\r\n");
                        }

                        if (chr.地图缓存10 > 0 && !"".equals(chr.地图缓存时间10)) {
                            name.append("\t\t#d进图时间:  #r").append(chr.地图缓存时间10).append("\r\n");
                            name.append("\t\t#d地图名称:  #r#m").append(chr.地图缓存10).append("##k\r\n");
                        }
                    }
                }
            }
        }

        return name.toString();
    }

    public static String 账号ID取绑定QQ(int id) {
        String data = "";

        try {
            Connection con = DBConPool.getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT qq as DATA FROM accounts WHERE id = ?");
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            Throwable var5 = null;

            try {
                if (rs.next()) {
                    data = rs.getString("DATA");
                }
            } catch (Throwable var15) {
                var5 = var15;
                throw var15;
            } finally {
                if (rs != null) {
                    if (var5 != null) {
                        try {
                            rs.close();
                        } catch (Throwable var14) {
                            var5.addSuppressed(var14);
                        }
                    } else {
                        rs.close();
                    }
                }

            }

            ps.close();
        } catch (SQLException var17) {
            服务端输出信息.println_err("账号ID取账号、出错");
        }

        return data;
    }

    public void 根据ID跟踪玩家(int cid) {
        if (this.c.getPlayer().getId() != cid) {
            Iterator var2 = ChannelServer.getAllInstances().iterator();

            while(var2.hasNext()) {
                ChannelServer cs = (ChannelServer)var2.next();
                Iterator var4 = cs.getPlayerStorage().getAllCharacters().iterator();

                while(var4.hasNext()) {
                    MapleCharacter chr = (MapleCharacter)var4.next();
                    if (chr.getId() == cid) {
                        if (this.c.getPlayer().getClient().getChannel() != chr.getClient().getChannel()) {
                            this.c.getPlayer().dropMessage(6, "正在换频道,请等待。");
                            this.c.getPlayer().changeChannel(chr.getClient().getChannel());
                        } else if (this.c.getPlayer().getId() != chr.getMapId()) {
                            MapleMap mapp = this.c.getChannelServer().getMapFactory().getMap(chr.getMapId());
                            this.c.getPlayer().changeMap(mapp, mapp.getPortal(0));
                        }
                    }
                }
            }

        }
    }

    public void 根据ID封号玩家(int cid) {
        if (this.c.getPlayer().getId() != cid) {
            int ch = World.Find.findChannel(角色ID取名字(cid));
            MapleCharacter target = ChannelServer.getInstance(ch).getPlayerStorage().getCharacterByName(角色ID取名字(cid));
            if (target.ban("被巡查封禁/" + this.c.getPlayer().getName() + "", this.c.getPlayer().isAdmin(), false, false)) {
                String 信息 = "[系统提醒] : 玩家 " + target.getName() + " 因为使用非法插件/破坏游戏平衡，被系统永久封号。";
                Broadcast.broadcastMessage(MaplePacketCreator.serverNotice(6, 信息));
                //QQMsgServer.sendMsgToQQGroup(信息);
            }

        }
    }

    public int getwh() {
        return this.wh;
    }

    public int 读取赞助余额() {
        return this.c.getPlayer().getMoney();
    }

    public boolean 增加赞助余额(int mount) {
        return this.增加赞助余额(mount, true);
    }

    public boolean 增加赞助余额(int mount, boolean record) {
        int money = this.c.getPlayer().getMoney();
        if (mount >= 0) {
            money += mount;
                this.c.getPlayer().setMoney(money);
            if (record) {
                this.c.getPlayer().setMoneyAll(mount, "脚本充值");
            }

            this.c.getPlayer().dropMessage(5, "赞助余额增加 " + mount);
            String text = "\r\n " + FileoutputUtil.NowTime() + " IP: " + this.c.getSession().remoteAddress().toString().split(":")[0] + " 账号: " + this.c.getAccountName() + " 玩家: " + this.c.getPlayer().getName() + " 增加了 " + mount + " 元宝";
            if (record) {
                text = text + "，并增加了 " + mount + " 累计赞助。";
            } else {
                text = text + "。";
            }

            FileoutputUtil.logToFile("logs/Data/赞助记录.txt", text);
            return true;
        } else {
            服务端输出信息.println_err("【错误】NPCConversationManager.java中的“增加赞助余额”不能为负数！");
            return false;
        }
    }

    public boolean 减少赞助余额(int mount) {
        int money = this.c.getPlayer().getMoney();
        if (mount >= 0) {
            if (money >= mount) {
                money -= mount;
                this.c.getPlayer().setMoney(money);
                this.c.getPlayer().dropMessage(5, "赞助余额减少 " + mount);
                String text = "\r\n " + FileoutputUtil.NowTime() + " IP: " + this.c.getSession().remoteAddress().toString().split(":")[0] + " 账号: " + this.c.getAccountName() + " 玩家: " + this.c.getPlayer().getName() + " 扣除了 " + mount + " 元宝。";
                FileoutputUtil.logToFile("logs/Data/赞助记录.txt", text);
                return true;
            } else {
                服务端输出信息.println_err("【错误】NPCConversationManager.java中的“减少赞助余额” 账号ID：" + this.c.getPlayer().getAccountID() + "余额减少数量大于剩余数量！");
                return false;
            }
        } else {
            服务端输出信息.println_err("【错误】NPCConversationManager.java中的“减少赞助余额”不能为负数！");
            return false;
        }
    }

    public boolean 增加累计赞助(int mount) {
        if (mount >= 0) {
            this.c.getPlayer().setMoneyAll(mount, "脚本充值");
            this.c.getPlayer().dropMessage(5, "累计赞助增加 " + mount);
            String text = "\r\n " + FileoutputUtil.NowTime() + " IP: " + this.c.getSession().remoteAddress().toString().split(":")[0] + " 账号: " + this.c.getAccountName() + " 玩家: " + this.c.getPlayer().getName() + " 增加了 " + mount + " 累计赞助。";
            FileoutputUtil.logToFile("logs/Data/赞助记录.txt", text);
            return true;
        } else {
            服务端输出信息.println_err("【错误】NPCConversationManager.java中的“增加累计赞助”不能为负数！");
            return false;
        }
    }

    public int 读取累计赞助() {
        return this.c.getPlayer().getMoneyAll();
    }

    public int 读取累计赞助(int type) {
        return this.c.getPlayer().getMoneyAll(type);
    }

    public void 增加伤害上限值(long inc) {
        this.c.getPlayer().增加伤害上限值(inc);
    }

    public void 减少伤害上限值(long inc) {
        this.c.getPlayer().减少伤害上限值(inc);
    }

    public long 读取伤害上限值() {
        return this.c.getPlayer().读取伤害上限值();
    }

    public boolean 概率判定(int percent) {
        Random ra = new Random();
        double min = 0.0;
        double max = 100.0;
        double value = ra.nextDouble() * (max - min) + min;
        return value <= (double)percent;
    }

    public boolean 概率判定(double percent) {
        Random ra = new Random();
        double min = 0.0;
        double max = 100.0;
        double value = ra.nextDouble() * (max - min) + min;
        return value <= percent;
    }

    public int 抽奖(String group) {
        if (!this.判断背包装备栏().isFull() && !this.判断背包消耗栏().isFull() && !this.判断背包设置栏().isFull() && !this.判断背包其他栏().isFull() && !this.判断背包特殊栏().isFull()) {
            ArrayList<Integer> rewardIDs = (ArrayList)Start.RewardIDMap.get(group);
            ArrayList<Double> chances = new ArrayList((Collection)Start.RewardChanceMap.get(group));
            double sum = 0.0;

            int i;
            for(i = 0; i < chances.size(); ++i) {
                sum += (Double)chances.get(i);
            }

            for(i = 0; i < chances.size(); ++i) {
                chances.set(i, (Double)chances.get(i) / sum * 100.0);
                if (i > 0) {
                    chances.set(i, (Double)chances.get(i - 1) + (Double)chances.get(i));
                }
            }

            Random ra = new Random();
            double min = 0.0;
            double max = 100.0;
            double value = ra.nextDouble() * (max - min) + min;

            for(int ss = 1; ss < rewardIDs.size(); ++ss) {
                if ((Double)chances.get(ss - 1) < value && value < (Double)chances.get(ss)) {
                    return (Integer)rewardIDs.get(ss);
                }
            }

            return (Integer)rewardIDs.get(0);
        } else {
            return -1;
        }
    }

    public int 抽奖(String group, String NPC) {
        if (!this.判断背包装备栏().isFull() && !this.判断背包消耗栏().isFull() && !this.判断背包设置栏().isFull() && !this.判断背包其他栏().isFull() && !this.判断背包特殊栏().isFull()) {
            ArrayList<Integer> rewardIDs = (ArrayList)Start.RewardIDMap.get(group);
            ArrayList<Double> chances = new ArrayList((Collection)Start.RewardChanceMap.get(group));
            ArrayList<Integer> rewardannouncements = (ArrayList)Start.RewardAnnouncementMap.get(group);
            double sum = 0.0;

            int i;
            for(i = 0; i < chances.size(); ++i) {
                sum += (Double)chances.get(i);
            }

            for(i = 0; i < chances.size(); ++i) {
                chances.set(i, (Double)chances.get(i) / sum * 100.0);
                if (i > 0) {
                    chances.set(i, (Double)chances.get(i - 1) + (Double)chances.get(i));
                }
            }

            Random ra = new Random();
            double min = 0.0;
            double max = 100.0;
            double value = ra.nextDouble() * (max - min) + min;

            for(int ss = 1; ss < rewardIDs.size(); ++ss) {
                if ((Double)chances.get(ss - 1) < value && value < (Double)chances.get(ss)) {
                    if ((Integer)rewardannouncements.get(ss) > 0) {
                        MapleInventoryType type = GameConstants.getInventoryType((Integer)rewardIDs.get(ss));
                        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
                        int itemId = (Integer)rewardIDs.get(ss);
                        Object item;
                        if (!type.equals(MapleInventoryType.EQUIP)) {
                            item = new Item(itemId, (short)0, (short) 1, (byte)0);
                        } else {
                            switch (itemId) {
                                case 1112405:
                                    item = ii.randomizeStats((Equip)ii.getEquipById(itemId), itemId);
                                    break;
                                case 1112413:
                                    item = ii.randomizeStats((Equip)ii.getEquipById(itemId), itemId);
                                    break;
                                case 1112414:
                                    item = ii.randomizeStats((Equip)ii.getEquipById(itemId), itemId);
                                    break;
                                default:
                                    item = ii.randomizeStats((Equip)ii.getEquipById(itemId));
                            }
                        }

                        if (item != null) {
                            ((IItem)item).getType();
                            Broadcast.broadcastMessage(MaplePacketCreator.getGachaponMega("【系统公告】", " : 恭喜玩家 " + this.c.getPlayer().getName() + " 在" + NPC + "获得！", (IItem)item, (byte)1));
                        }
                    }

                    return (Integer)rewardIDs.get(ss);
                }
            }

            MapleInventoryType type = GameConstants.getInventoryType((Integer)rewardIDs.get(0));
            MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
            int itemId = (Integer)rewardIDs.get(0);
            Object item;
            if (!type.equals(MapleInventoryType.EQUIP)) {
                item = new Item(itemId, (short)0, (short) 1, (byte)0);
            } else {
                switch (itemId) {
                    case 1112405:
                        item = ii.randomizeStats((Equip)ii.getEquipById(itemId), itemId);
                        break;
                    case 1112413:
                        item = ii.randomizeStats((Equip)ii.getEquipById(itemId), itemId);
                        break;
                    case 1112414:
                        item = ii.randomizeStats((Equip)ii.getEquipById(itemId), itemId);
                        break;
                    default:
                        item = ii.randomizeStats((Equip)ii.getEquipById(itemId));
                }
            }

            if (item != null) {
                ((IItem)item).getType();
                Broadcast.broadcastMessage(MaplePacketCreator.getGachaponMega("【系统公告】", " : 恭喜玩家 " + this.c.getPlayer().getName() + " 在" + NPC + "获得！", (IItem)item, (byte)1));
            }

            return (Integer)rewardIDs.get(0);
        } else {
            return -1;
        }
    }

    public IItem 抽奖2(String group, String NPC) {
        if (!this.判断背包装备栏().isFull() && !this.判断背包消耗栏().isFull() && !this.判断背包设置栏().isFull() && !this.判断背包其他栏().isFull() && !this.判断背包特殊栏().isFull()) {
            ArrayList<Integer> rewardIDs = (ArrayList)Start.RewardIDMap.get(group);
            ArrayList<Double> chances = new ArrayList((Collection)Start.RewardChanceMap.get(group));
            ArrayList<Integer> rewardannouncements = (ArrayList)Start.RewardAnnouncementMap.get(group);
            double sum = 0.0;

            int i;
            for(i = 0; i < chances.size(); ++i) {
                sum += (Double)chances.get(i);
            }

            for(i = 0; i < chances.size(); ++i) {
                chances.set(i, (Double)chances.get(i) / sum * 100.0);
                if (i > 0) {
                    chances.set(i, (Double)chances.get(i - 1) + (Double)chances.get(i));
                }
            }

            Random ra = new Random();
            double min = 0.0;
            double max = 100.0;
            double value = ra.nextDouble() * (max - min) + min;

            for(int ss = 1; ss < rewardIDs.size(); ++ss) {
                if ((Double)chances.get(ss - 1) < value && value < (Double)chances.get(ss)) {
                    MapleInventoryType type = GameConstants.getInventoryType((Integer)rewardIDs.get(ss));
                    MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
                    int itemId = (Integer)rewardIDs.get(ss);
                    Object item;
                    if (!type.equals(MapleInventoryType.EQUIP)) {
                        item = new Item(itemId, (short)0, (short) 1, (byte)0);
                    } else {
                        switch (itemId) {
                            case 1112405:
                                item = ii.randomizeStats((Equip)ii.getEquipById(itemId), itemId);
                                break;
                            case 1112413:
                                item = ii.randomizeStats((Equip)ii.getEquipById(itemId), itemId);
                                break;
                            case 1112414:
                                item = ii.randomizeStats((Equip)ii.getEquipById(itemId), itemId);
                                break;
                            default:
                                item = ii.randomizeStats((Equip)ii.getEquipById(itemId));
                        }
                    }

                    if (item != null) {
                        ((IItem)item).getType();
                        if ((Integer)rewardannouncements.get(ss) > 0) {
                            Broadcast.broadcastMessage(MaplePacketCreator.getGachaponMega("【系统公告】", " : 恭喜玩家 " + this.c.getPlayer().getName() + " 在" + NPC + "获得！", (IItem)item, (byte)1));
                        }

                        return (IItem)item;
                    }
                }
            }

            MapleInventoryType type = GameConstants.getInventoryType((Integer)rewardIDs.get(0));
            MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
            int itemId = (Integer)rewardIDs.get(0);
            Object item;
            if (!type.equals(MapleInventoryType.EQUIP)) {
                item = new Item(itemId, (short)0, (short) 1, (byte)0);
            } else {
                switch (itemId) {
                    case 1112405:
                        item = ii.randomizeStats((Equip)ii.getEquipById(itemId), itemId);
                        break;
                    case 1112413:
                        item = ii.randomizeStats((Equip)ii.getEquipById(itemId), itemId);
                        break;
                    case 1112414:
                        item = ii.randomizeStats((Equip)ii.getEquipById(itemId), itemId);
                        break;
                    default:
                        item = ii.randomizeStats((Equip)ii.getEquipById(itemId));
                }
            }

            if (item != null && (Integer)rewardannouncements.get(0) > 0) {
                ((IItem)item).getType();
                Broadcast.broadcastMessage(MaplePacketCreator.getGachaponMega("【系统公告】", " : 恭喜玩家 " + this.c.getPlayer().getName() + " 在" + NPC + "获得！", (IItem)item, (byte)1));
            }

            return (IItem)item;
        } else {
            return null;
        }
    }

    public ArrayList 读取奖品ID(String group) {
        ArrayList<Integer> rewardIDs = (ArrayList)Start.RewardIDMap.get(group);
        return rewardIDs;
    }

    public String 读取奖品名称(String group, int itemid) {
        ArrayList<String> names = (ArrayList)Start.RewardNameMap.get(group);
        ArrayList<Integer> rewardIDs = (ArrayList)Start.RewardIDMap.get(group);
        String name = "";

        for(int i = 0; i < rewardIDs.size(); ++i) {
            if (itemid == (Integer)rewardIDs.get(i)) {
                name = (String)names.get(i);
            }
        }

        return name;
    }
    public void sendNone() {
        if (this.lastMsg <= -1) {
            this.lastMsg = -1;
            NPCScriptManager.getInstance().action(this.c, (byte)1, (byte)0, -1);
        }
    }
    public ArrayList 读取奖品概率(String group) {
        ArrayList<Double> chances = new ArrayList((Collection)Start.RewardChanceMap.get(group));
        double sum = 0.0;

        int i;
        for(i = 0; i < chances.size(); ++i) {
            sum += (Double)chances.get(i);
        }

        for(i = 0; i < chances.size(); ++i) {
            chances.set(i, (Double)chances.get(i) / sum * 100.0);
        }

        return chances;
    }

    public String Double转字符串(double number, int jingdu) {
        return 处理字符串.doubleFormatInteger(number, jingdu);
    }

    public void 传送至角色ID(int id) {
        MapleCharacter target_char = MapleCharacter.getCharacterById(id);
        this.c.getPlayer().changeMap(target_char.getMapId());
        String a = "222";
    }

    public void flytoID(int id) {
        MapleCharacter target_char = MapleCharacter.getCharacterById(id);
        this.c.getPlayer().changeMap(target_char.getMapId());
    }

    public void deleteOneTimeLoga(String log) {
        this.c.getPlayer().deleteOneTimeLoga(log);
    }

    public void setOneTimeLoga(String log) {
        this.c.getPlayer().setOneTimeLoga(log);
    }

    public long 获取角色战斗力() {
        return this.c.getPlayer().getPower();
    }
    public Collection<MaplePartyCharacter> 组队成员() {
        if (this.getParty() != null) {
            Collection<MaplePartyCharacter> Members = this.c.getPlayer().getParty().getMembers();
            return new LinkedList(Members);
        } else {
            return null;
        }
    }
    public int checkLevelsAndMap(int lowestlevel, int highestlevel) {
        Collection<MaplePartyCharacter> party = this.组队成员();
        int mapId = this.getMapId();
        int valid = 0;
        Iterator var6 = party.iterator();

        while(var6.hasNext()) {
            MaplePartyCharacter cPlayer = (MaplePartyCharacter)var6.next();
            if ((cPlayer.getLevel() < lowestlevel || cPlayer.getLevel() > highestlevel) && cPlayer.getJobId() != 900) {
                valid = 1;
            }

            if (cPlayer.getMapid() != mapId) {
                valid = 2;
            }
        }

        return valid;
    }

    public void 爆物开关() {
        this.c.getPlayer().getMap().toggleDrops();
    }

    public long 查看蓄力一击() {
        return (System.currentTimeMillis() - this.c.getPlayer().蓄力一击) / 10L;
    }

    public void 给人气(int r) {
        this.c.getPlayer().addFame(r);
    }

    public void 发言(String 内容) {
        Iterator var2 = ChannelServer.getAllInstances().iterator();

        while(var2.hasNext()) {
            ChannelServer cserv = (ChannelServer)var2.next();
            Iterator var4 = cserv.getPlayerStorage().getAllCharacters().iterator();

            while(var4.hasNext()) {
                MapleCharacter victim = (MapleCharacter)var4.next();
                if (victim.getId() != this.c.getPlayer().getId()) {
                    victim.getMap().broadcastMessage(MaplePacketCreator.getChatText(victim.getId(), StringUtil.joinStringFrom(new String[]{内容}, 1), victim.isGM(), 0));
                }
            }
        }

    }

    public void 战斗力排行榜_snail() {
        MapleGuild.战斗力排行_snail(this.getClient(), this.npc);
    }

    public void 力量排行榜_snail() {
        MapleGuild.力量排行_snail(this.getClient(), this.npc);
    }

    public void 敏捷排行榜_snail() {
        MapleGuild.敏捷排行_snail(this.getClient(), this.npc);
    }

    public void 智力排行榜_snail() {
        MapleGuild.智力排行_snail(this.getClient(), this.npc);
    }

    public void 运气排行榜_snail() {
        MapleGuild.运气排行_snail(this.getClient(), this.npc);
    }


    public int MarrageChecking() {
        int result = 0;
        byte gender1 = this.c.getPlayer().getGender();
        MapleRing ring1 = this.c.getPlayer().getMarriageRing(false);
        if (ring1 == null) {
            result = 6;
            return result;
        } else if (this.c.getPlayer().getParty().getMembers().size() != 2) {
            result = 1;
            return result;
        } else if (!this.allMembersHere()) {
            result = 2;
            return result;
        } else {
            Iterator var4 = this.c.getPlayer().getParty().getMembers().iterator();

            while(var4.hasNext()) {
                MaplePartyCharacter partyChr = (MaplePartyCharacter)var4.next();
                MapleCharacter chr = this.c.getChannelServer().getPlayerStorage().getCharacterById(partyChr.getId());
                if (chr.getMarriageId() != 0) {
                    result = 3;
                    return result;
                }

                if (chr != this.c.getPlayer() && chr.getGender() == gender1) {
                    result = 4;
                    return result;
                }

                MapleInventory inventory = chr.getInventory(MapleInventoryType.EQUIPPED);
                if (chr.getGender() == 0 && inventory.countById(1050122) <= 0L && inventory.countById(1051129) <= 0L && inventory.countById(1050113) <= 0L) {
                    result = 5;
                    return result;
                }

                if (chr.getGender() == 1 && inventory.countById(1051130) <= 0L && inventory.countById(1051177) <= 0L && inventory.countById(1051114) <= 0L) {
                    result = 5;
                    return result;
                }

                if (chr != this.c.getPlayer()) {
                    MapleRing ring2 = chr.getMarriageRing(false);
                    if (ring2 == null) {
                        result = 6;
                        return result;
                    }

                    if (ring1.getItemId() != ring2.getItemId()) {
                        result = 7;
                        return result;
                    }

                    if (ring1.getPartnerChrId() != chr.getId() || ring2.getPartnerChrId() != this.c.getPlayer().getId()) {
                        result = 8;
                        return result;
                    }
                }
            }

            return result;
        }
    }

    public boolean 单人强行出轨() {
        int m_id = this.c.getPlayer().getMarriageId();
        if (m_id == 0) {
            return false;
        } else {
            Iterator var2 = ChannelServer.getAllInstances().iterator();

            ChannelServer ps;
            while(var2.hasNext()) {
                ps = (ChannelServer)var2.next();
                Iterator var4 = ps.getPlayerStorage().getAllCharacters().iterator();

                while(var4.hasNext()) {
                    MapleCharacter chr = (MapleCharacter)var4.next();
                    if (chr.getId() == m_id) {
                        this.c.getPlayer().setMarriageId(0);
                        chr.setMarriageId(0);
                        return true;
                    }
                }
            }

            this.c.getPlayer().setMarriageId(0);
            Connection con = DBConPool.getConnection();
            ps = null;

            try {
                PreparedStatement ps1 = con.prepareStatement("UPDATE characters SET `marriageId` = ? WHERE id = ?");
                ps1.setInt(1, 0);
                ps1.setInt(1, m_id);
                ps1.execute();
                ps1.close();
                return true;
            } catch (SQLException var6) {
                服务端输出信息.println_err("【错误】执行 单人强行出轨() 命令失败，代码位置：NPCConversationManager，错误原因：" + var6);
                return false;
            }
        }
    }

    public boolean 收徒(int chrid) {
        Connection con = DBConPool.getConnection();
        PreparedStatement ps = null;
        PreparedStatement ps0 = null;
        boolean result = false;
        String chrName = MapleCharacter.getCharacterNameById(chrid);

        try {
            ps0 = con.prepareStatement("SELECT * FROM 徒弟列表 WHERE student_name = ?");
            ps0.setString(1, chrName);
            ResultSet rs0 = ps0.executeQuery();
            if (rs0.next()) {
                return result;
            }

            ps = con.prepareStatement("INSERT INTO 徒弟列表 (chrid, chrname, student_id, student_name, graduate) VALUES (?, ?, ?, ?, ?)");
            ps.setInt(1, this.c.getPlayer().getId());
            ps.setString(2, this.c.getPlayer().getName());
            ps.setInt(3, chrid);
            ps.setString(4, chrName);
            ps.setInt(5, 0);
            ps.executeUpdate();
            ps.close();
            ps0.close();
            result = true;
        } catch (SQLException var8) {
            服务端输出信息.println_err("【错误】执行 收徒() 命令失败，代码位置：NPCConversationManager，错误原因：" + var8);
        }

        return result;
    }

    public boolean 拜师(int studentId, int teacherId) {
        PreparedStatement ps = null;
        PreparedStatement ps0 = null;
        boolean result = false;
        String studentName = MapleCharacter.getCharacterNameById(studentId);
        String teacherName = MapleCharacter.getCharacterNameById(teacherId);
        Connection con = DBConPool.getConnection();

        try {
            ps0 = con.prepareStatement("SELECT * FROM 徒弟列表 WHERE student_id = ?");
            ps0.setInt(1, studentId);
            ResultSet rs0 = ps0.executeQuery();
            if (rs0.next()) {
                return result;
            } else {
                ps = con.prepareStatement("INSERT INTO 徒弟列表 (chrid, chrname, student_id, student_name, graduate) VALUES (?, ?, ?, ?, ?)");
                ps.setInt(1, teacherId);
                ps.setString(2, teacherName);
                ps.setInt(3, studentId);
                ps.setString(4, studentName);
                ps.setInt(5, 0);
                ps.executeUpdate();
                ps.close();
                ps0.close();
                result = true;
                return result;
            }
        } catch (SQLException var10) {
            服务端输出信息.println_err("【错误】执行 拜师() 命令失败，代码位置：NPCConversationManager，错误原因：" + var10);
            return result;
        }
    }

    public boolean 是否为徒弟(int chrId) {
        Connection con = DBConPool.getConnection();
        PreparedStatement ps = null;

        try {
            ps = con.prepareStatement("SELECT * FROM 徒弟列表 WHERE student_id = ?");
            ps.setInt(1, chrId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return true;
            } else {
                ps.close();
                return false;
            }
        } catch (SQLException var5) {
            服务端输出信息.println_err("【错误】执行 是否为徒弟() 命令失败，代码位置：NPCConversationManager，错误原因：" + var5);
            return false;
        }
    }

    public boolean 是否为徒弟() {
        return this.是否为徒弟(this.c.getPlayer().getId());
    }

    public boolean 出师(int chrid) {
        Connection con = DBConPool.getConnection();
        PreparedStatement ps = null;
        PreparedStatement ps0 = null;
        boolean result = false;
        boolean haveStudent = false;
        boolean isGraduate = false;
        String chrName = MapleCharacter.getCharacterNameById(chrid);

        try {
            ps0 = con.prepareStatement("SELECT * FROM 徒弟列表 WHERE chrid = ?");
            ps0.setInt(1, this.c.getPlayer().getId());
            ResultSet rs0 = ps0.executeQuery();

            while(rs0.next()) {
                if (rs0.getInt("student_id") == chrid) {
                    haveStudent = true;
                    if (rs0.getInt("graduate") == 1) {
                        isGraduate = true;
                    }
                    break;
                }
            }

            if (haveStudent && !isGraduate) {
                ps = con.prepareStatement("Update 徒弟列表 set graduate = ? Where  student_id = ?");
                ps.setInt(1, 1);
                ps.setInt(2, chrid);
                ps.executeUpdate();
                ps.close();
                result = true;
            }

            ps0.close();
        } catch (SQLException var10) {
            服务端输出信息.println_err("【错误】执行 出师() 命令失败，代码位置：NPCConversationManager，错误原因：" + var10);
        }

        return result;
    }

    public boolean 踢出师门(int chrid) {
        Connection con = DBConPool.getConnection();
        PreparedStatement ps = null;
        PreparedStatement ps0 = null;
        boolean result = false;
        boolean haveStudent = false;
        String chrName = MapleCharacter.getCharacterNameById(chrid);

        try {
            ps0 = con.prepareStatement("SELECT * FROM 徒弟列表 WHERE chrid = ?");
            ps0.setInt(1, this.c.getPlayer().getId());
            ResultSet rs0 = ps0.executeQuery();

            while(rs0.next()) {
                if (rs0.getInt("student_id") == chrid) {
                    haveStudent = true;
                    break;
                }
            }

            if (haveStudent) {
                ps = con.prepareStatement("Delete FROM 徒弟列表 WHERE student_id = ?");
                ps.setInt(1, chrid);
                ps.executeUpdate();
                ps.close();
                result = true;
            }

            ps0.close();
        } catch (SQLException var9) {
            服务端输出信息.println_err("【错误】执行 出师() 命令失败，代码位置：NPCConversationManager，错误原因：" + var9);
        }

        return result;
    }

    public int 获得未毕业徒弟数量() {
        return this.获得未毕业徒弟数量(this.c.getPlayer().getId());
    }

    public int 获得未毕业徒弟数量(int chrId) {
        Connection con = DBConPool.getConnection();
        PreparedStatement ps = null;
        int mount = -1;

        try {
            ps = con.prepareStatement("SELECT * FROM 徒弟列表 WHERE chrid = ?");
            ps.setInt(1, chrId);
            ResultSet rs = ps.executeQuery();

            while(rs.next()) {
                if (mount == -1) {
                    mount = 0;
                }

                if (rs.getInt("graduate") == 0) {
                    ++mount;
                }
            }

            ps.close();
        } catch (SQLException var6) {
            服务端输出信息.println_err("【错误】执行 获得未毕业徒弟数量() 命令失败，代码位置：NPCConversationManager，错误原因：" + var6);
        }

        return mount;
    }

    public int 获得毕业徒弟数量() {
        return this.获得毕业徒弟数量(this.c.getPlayer().getId());
    }

    public int 获得毕业徒弟数量(int chrId) {
        Connection con = DBConPool.getConnection();
        PreparedStatement ps = null;
        int mount = -1;

        try {
            ps = con.prepareStatement("SELECT * FROM 徒弟列表 WHERE chrid = ?");
            ps.setInt(1, chrId);
            ResultSet rs = ps.executeQuery();

            while(rs.next()) {
                if (mount == -1) {
                    mount = 0;
                }

                if (rs.getInt("graduate") == 1) {
                    ++mount;
                }
            }

            ps.close();
        } catch (SQLException var6) {
            服务端输出信息.println_err("【错误】执行 获得毕业徒弟数量() 命令失败，代码位置：NPCConversationManager，错误原因：" + var6);
        }

        return mount;
    }

    public int 获得总徒弟数量() {
        return this.获得总徒弟数量(this.c.getPlayer().getId());
    }

    public int 获得总徒弟数量(int chrId) {
        Connection con = DBConPool.getConnection();
        PreparedStatement ps = null;
        int mount = -1;

        try {
            ps = con.prepareStatement("SELECT * FROM 徒弟列表 WHERE chrid = ?");
            ps.setInt(1, chrId);

            for(ResultSet rs = ps.executeQuery(); rs.next(); ++mount) {
                if (mount == -1) {
                    mount = 0;
                }
            }

            ps.close();
        } catch (SQLException var6) {
            服务端输出信息.println_err("【错误】执行 获得总徒弟数量() 命令失败，代码位置：NPCConversationManager，错误原因：" + var6);
        }

        return mount;
    }

    public ArrayList<Integer> 获得所有徒弟id() {
        return this.获得所有徒弟id(this.c.getPlayer().getId());
    }

    public ArrayList<Integer> 获得所有师傅id() {
        Connection con = DBConPool.getConnection();
        PreparedStatement ps = null;
        ArrayList<Integer> ids = new ArrayList();
        Map<Integer, Integer> countMap = new CacheMap();

        try {
            List<TimeLogCenter.OneTimeLog> timeLogList = TimeLogCenter.getInstance().getOneTimeLogList();
            Iterator var6 = timeLogList.iterator();

            while(var6.hasNext()) {
                TimeLogCenter.OneTimeLog timeLog = (TimeLogCenter.OneTimeLog)var6.next();
                if (timeLog != null && timeLog.getLogName().equals("师傅")) {
                    int id = timeLog.getChrId();
                    if (!((Map)countMap).containsKey(id)) {
                        ((Map)countMap).put(id, 0);
                    }
                }
            }

            ps = con.prepareStatement("SELECT * FROM 徒弟列表");
            ResultSet rs = ps.executeQuery();

            while(rs.next()) {
                int id = rs.getInt("chrid");
                if (!((Map)countMap).containsKey(id)) {
                    ((Map)countMap).put(id, 1);
                } else {
                    ((Map)countMap).put(id, (Integer)((Map)countMap).get(id) + 1);
                }
            }

            ps.close();
            rs.close();
        } catch (SQLException var9) {
            服务端输出信息.println_err("【错误】执行 获得所有师傅id() 命令失败，代码位置：NPCConversationManager，错误原因：" + var9);
        }

        if (((Map)countMap).size() > 0) {
            countMap = sortTool.sortDescend((Map)countMap);
        }

        Iterator var10 = ((Map)countMap).entrySet().iterator();

        while(var10.hasNext()) {
            Map.Entry<Integer, Integer> entry = (Map.Entry)var10.next();
            ids.add(entry.getKey());
        }

        ((Map)countMap).clear();
        countMap = null;
        return ids;
    }

    public ArrayList<Integer> 获得所有徒弟id(int chrId) {
        Connection con = DBConPool.getConnection();
        PreparedStatement ps = null;
        ArrayList<Integer> ids = new ArrayList();

        try {
            ps = con.prepareStatement("SELECT * FROM 徒弟列表 WHERE chrid = ?");
            ps.setInt(1, chrId);
            ResultSet rs = ps.executeQuery();

            while(rs.next()) {
                ids.add(rs.getInt("student_id"));
            }

            ps.close();
        } catch (SQLException var6) {
            服务端输出信息.println_err("【错误】执行 获得所有徒弟id() 命令失败，代码位置：NPCConversationManager，错误原因：" + var6);
        }

        return ids;
    }

    public int 改角色名(String newName) {
        return World.changeCharName(this.c.getPlayer().getId(), newName);
    }

    private void changeNameWhereCharacterId(Connection con, String tableName, String rowName, String newChrName, String oldChrName) {
        try {
            PreparedStatement ps = con.prepareStatement("UPDATE " + tableName + " SET " + rowName + " = ? WHERE " + rowName + " = ?");
            ps.setString(1, newChrName);
            ps.setString(2, oldChrName);
            ps.executeUpdate();
            ps.close();
        } catch (Exception var7) {
            服务端输出信息.println_err("changeNameWhereCharacterId出错，错误原因：" + var7);
        }

    }

    public String 查询曾用名() {
        String names = "";
        Connection con = DBConPool.getConnection();

        try {
            PreparedStatement ps = con.prepareStatement("SELECT * FROM 曾用名 WHERE chrid = ?");
            ps.setInt(1, this.c.getPlayer().getId());
            ResultSet rs = ps.executeQuery();

            while(rs.next()) {
                String oldname = rs.getString("oldname");
                if (!oldname.equals(this.c.getPlayer().getName())) {
                    names = names + oldname + ", ";
                }
            }

            if (!names.isEmpty()) {
                names = names.substring(0, names.length() - 2);
            }

            ps.close();
            rs.close();
        } catch (SQLException var6) {
            服务端输出信息.println_err("查询曾用名出错，错误原因：" + var6);
        }

        return names;
    }

    public int 查询骑士团职业数量(int minLevel) {
        int count = 0;
        Connection con = DBConPool.getConnection();

        try {
            PreparedStatement ps = con.prepareStatement("SELECT * FROM characters WHERE accountid = ?");
            ps.setInt(1, this.c.getAccID());
            ResultSet rs = ps.executeQuery();

            while(rs.next()) {
                int jobid = rs.getInt("job");
                if (jobid >= 1000 && jobid < 2000 && rs.getInt("level") >= minLevel) {
                    ++count;
                }
            }

            ps.close();
            rs.close();
        } catch (SQLException var7) {
            服务端输出信息.println_err("查询角色骑士团职业数量 出错，错误原因：" + var7);
        }

        return count;
    }

    public int 查询link授予角色(int accountid) {
        int chrid = 0;
        Connection con = DBConPool.getConnection();

        try {
            PreparedStatement ps = con.prepareStatement("SELECT * FROM 骑士团Link WHERE accountid = ?");
            ps.setInt(1, this.c.getAccID());

            ResultSet rs;
            for(rs = ps.executeQuery(); rs.next(); chrid = rs.getInt("chrid")) {
            }

            ps.close();
            rs.close();
        } catch (SQLException var6) {
            服务端输出信息.println_err("查询角色骑士团职业数量 出错，错误原因：" + var6);
        }

        return chrid;
    }

    public int 查询linkAP(int accountid) {
        int ap = 0;
        Connection con = DBConPool.getConnection();

        try {
            PreparedStatement ps = con.prepareStatement("SELECT * FROM 骑士团Link WHERE accountid = ?");
            ps.setInt(1, this.c.getAccID());

            ResultSet rs;
            for(rs = ps.executeQuery(); rs.next(); ap = rs.getInt("ap")) {
            }

            ps.close();
            rs.close();
        } catch (SQLException var6) {
            服务端输出信息.println_err("查询角色骑士团职业数量 出错，错误原因：" + var6);
        }

        return ap;
    }

    public boolean 给予Link(int accountid, int chrid, int ap) {
        Connection con = DBConPool.getConnection();

        try {
            PreparedStatement ps = con.prepareStatement("SELECT * FROM 骑士团Link WHERE accountid = ?");
            ps.setInt(1, this.c.getAccID());
            ResultSet rs = ps.executeQuery();
            boolean isExit = false;

            int chrid0;
            do {
                if (!rs.next()) {
                    if (isExit) {
                        ps = con.prepareStatement("UPDATE 骑士团Link SET chrid = ? , ap = ? WHERE accountid = ?");
                        ps.setInt(1, chrid);
                        ps.setInt(2, ap);
                        ps.setInt(3, accountid);
                        ps.executeUpdate();
                    } else {
                        ps = con.prepareStatement("INSERT INTO 骑士团Link (accountid, chrid, ap) VALUES (?, ?, ?)");
                        ps.setInt(1, accountid);
                        ps.setInt(2, chrid);
                        ps.setInt(3, ap);
                        ps.executeUpdate();
                    }

                    ps.close();
                    rs.close();
                    return true;
                }

                isExit = true;
                chrid0 = rs.getInt("chrid");
            } while(chrid0 <= 0);

            return false;
        } catch (SQLException var9) {
            服务端输出信息.println_err("给予Link 出错，错误原因：" + var9);
            return false;
        }
    }

    public int 重置Link(int accountid) {
        int ap = 0;
        Connection con = DBConPool.getConnection();

        try {
            PreparedStatement ps = con.prepareStatement("SELECT * FROM 骑士团Link WHERE accountid = ?");
            ps.setInt(1, this.c.getAccID());
            ResultSet rs = ps.executeQuery();

            while(rs.next()) {
                if (rs.getInt("chrid") > 0) {
                    ap = rs.getInt("ap");
                }

                ps = con.prepareStatement("UPDATE 骑士团Link SET chrid = ? AND ap = ? WHERE accountid = ?");
                ps.setInt(1, 0);
                ps.setInt(2, 0);
                ps.setInt(3, accountid);
                ps.executeUpdate();
            }

            ps.close();
            rs.close();
        } catch (SQLException var6) {
            服务端输出信息.println_err("查询角色骑士团职业数量 出错，错误原因：" + var6);
        }

        return ap;
    }

    public boolean 砸装备(IItem item, short str, short dex, short _int, short luk, short watk, short matk, short wdef, short mdef, short acc, short avoid, short speed, short jump, short hp, short mp, int duration, byte flag, boolean reduce) {
        if (item.getType() == 1) {
            Equip equip = (Equip)item;
            ArrayList mods;
            if (reduce) {
                if (equip.getUpgradeSlots() < 1) {
                    return false;
                }

                equip.setStr((short)(equip.getStr() + str));
                equip.setDex((short)(equip.getDex() + dex));
                equip.setInt((short)(equip.getInt() + _int));
                equip.setLuk((short)(equip.getLuk() + luk));
                equip.setWatk((short)(equip.getWatk() + watk));
                equip.setMatk((short)(equip.getMatk() + matk));
                equip.setWdef((short)(equip.getWdef() + wdef));
                equip.setMdef((short)(equip.getMdef() + mdef));
                equip.setAcc((short)(equip.getAcc() + acc));
                equip.setAvoid((short)(equip.getAvoid() + avoid));
                equip.setSpeed((short)(equip.getSpeed() + speed));
                equip.setJump((short)(equip.getJump() + jump));
                equip.setHp((short)(equip.getHp() + hp));
                equip.setMp((short)(equip.getMp() + mp));
                if (flag > 0) {
                    equip.setFlag(flag);
                }

                if (duration > 0) {
                    if (equip.getExpiration() >= System.currentTimeMillis()) {
                        equip.setExpiration(equip.getExpiration() + (long)(duration * 60 * 60 * 1000));
                    } else {
                        equip.setExpiration(System.currentTimeMillis() + (long)(duration * 60 * 60 * 1000));
                    }
                }

                equip.setUpgradeSlots((byte)(equip.getUpgradeSlots() - 1));
                equip.setLevel((byte)(equip.getLevel() + 1));
                mods = new ArrayList();
                mods.add(new ModifyInventory(3, equip));
                mods.add(new ModifyInventory(0, equip));
                this.c.sendPacket(MaplePacketCreator.modifyInventory(true, mods));
            } else {
                equip.setStr((short)(equip.getStr() + str));
                equip.setDex((short)(equip.getDex() + dex));
                equip.setInt((short)(equip.getInt() + _int));
                equip.setLuk((short)(equip.getLuk() + luk));
                equip.setWatk((short)(equip.getWatk() + watk));
                equip.setMatk((short)(equip.getMatk() + matk));
                equip.setWdef((short)(equip.getWdef() + wdef));
                equip.setMdef((short)(equip.getMdef() + mdef));
                equip.setAcc((short)(equip.getAcc() + acc));
                equip.setAvoid((short)(equip.getAvoid() + avoid));
                equip.setSpeed((short)(equip.getSpeed() + speed));
                equip.setJump((short)(equip.getJump() + jump));
                equip.setHp((short)(equip.getHp() + hp));
                equip.setMp((short)(equip.getMp() + mp));
                if (flag > 0) {
                    equip.setFlag(flag);
                }

                if (duration > 0) {
                    if (equip.getExpiration() >= System.currentTimeMillis()) {
                        equip.setExpiration(equip.getExpiration() + (long)(duration * 60 * 60 * 1000));
                    } else {
                        equip.setExpiration(System.currentTimeMillis() + (long)(duration * 60 * 60 * 1000));
                    }
                }

                mods = new ArrayList();
                mods.add(new ModifyInventory(3, equip));
                mods.add(new ModifyInventory(0, equip));
                this.c.sendPacket(MaplePacketCreator.modifyInventory(true, mods));
            }

            return true;
        } else {
            return false;
        }
    }

    public void sendMobSkill(int skillId, int level) {
        MobSkill mobSkill = MobSkillFactory.getMobSkill(skillId, level);
        MapleCharacter chr = this.getPlayer();
        MapleMonster monster = null;
        Point chrPoint = chr.getPosition();
        int distance = 999999999;
        Iterator var8 = chr.getMap().getAllMonstersThreadsafe().iterator();

        while(var8.hasNext()) {
            MapleMonster mob = (MapleMonster)var8.next();
            int d = (int)Math.pow(Math.pow((double)(mob.getPosition().x - chrPoint.x), 2.0) + Math.pow((double)(mob.getPosition().y - chrPoint.y), 2.0), 0.5);
            if (distance > d) {
                distance = d;
                monster = mob;
            }
        }

        if (monster != null && mobSkill != null && !mobSkill.checkCurrentBuff(chr, monster)) {
            mobSkill.applyEffect(chr, monster, true);
        }

    }

    public boolean 重置角色每日() {
        return this.c.getPlayer().resetBossLog();
    }

    public boolean 重置账号每日() {
        return this.c.getPlayer().resetBossLogA();
    }

    public void saveChrSkillMapToDB() {
        服务端输出信息.println_out("【脚本命令】开始保存技能皮肤至数据库");
        long time = Calendar.getInstance().getTimeInMillis();
        int count = SkillSkin.saveChrSkillMapToDB();
        time = Calendar.getInstance().getTimeInMillis() - time;
        服务端输出信息.println_out("【脚本命令】技能皮肤保存完毕，共保存" + count + "个玩家的技能皮肤，耗时 " + time + "毫秒。");
    }

    public boolean 批量添加抽奖物品(String type, int[] items, float[] weight, int[] announcement) {
        if (items.length == weight.length && items.length == announcement.length && items.length > 0) {
            try {
                Connection con = DBConPool.getConnection();
                Throwable var6 = null;

                try {
                    PreparedStatement ps = con.prepareStatement("INSERT INTO 抽奖管理 ( groups,itemid,itemname,chance,announcement) VALUES ( ?, ?, ?, ?, ?)");

                    for(int i = 0; i < items.length; ++i) {
                        ps.setString(1, type);
                        ps.setInt(2, items[i]);
                        ps.setString(3, 处理字符串.removeSpecialCharacters(this.查询道具名称(items[i])));
                        ps.setDouble(4, (double)weight[i]);
                        ps.setInt(5, announcement[i]);
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
                服务端输出信息.println_err(var19);
                FileoutputUtil.outError("logs/资料库异常.txt", var19);
                return false;
            }
        } else {
            return false;
        }
    }

    public String 查询道具名称(int ID) {
        String str = String.valueOf(ID);
        String name = "";
        Map<Integer, String> data = SearchGenerator.getSearchData(1, str);
        if (data != null && !data.isEmpty()) {
            Iterator<Integer> iterator = data.keySet().iterator();
            if (iterator.hasNext()) {
                ID = Integer.valueOf((Integer)iterator.next());
                name = (String)data.get(ID);
                return name;
            } else {
                return name;
            }
        } else {
            name = "";
            return name;
        }
    }

    public Connection getConnection() {
        return DBConPool.getConnection();
    }

    public String 输出装备打孔镶嵌信息(String mxmxdDaKongFuMo) {
        if (mxmxdDaKongFuMo != null && mxmxdDaKongFuMo.length() == 0) {
            return "";
        } else {
            String[] arr1 = mxmxdDaKongFuMo.split(",");
            String kongInfo = "";

            for(int i = 0; i < arr1.length; ++i) {
                String pair = arr1[i];
                if (pair.contains(":")) {
                    kongInfo = kongInfo + "#b \t镶嵌:#r ●";
                    String[] arr2 = pair.split(":");
                    int fumoType = Integer.parseInt(arr2[0]);
                    int fumoVal = Integer.parseInt(arr2[1]);
                    if (fumoType > 0 && Start.FuMoInfoMap.containsKey(fumoType)) {
                        String[] infoArr = (String[])Start.FuMoInfoMap.get(fumoType);
                        String fumoName = infoArr[0];
                        String fumoInfo = infoArr[1];
                        kongInfo = kongInfo + fumoName + " " + String.format(fumoInfo, fumoVal);
                    } else {
                        kongInfo = kongInfo + "[未镶嵌]";
                    }

                    kongInfo = kongInfo + "\r\n";
                }
            }

            return kongInfo;
        }
    }

//    public boolean 离线挂机() {
//        return FakePlayer.copyChr(this.c.getPlayer());
//    }

//    public boolean 增加家族技能(int guildId, int skillType, int skillLevel, int skillVal) {
//        MapleGuild guild = Guild.getGuild(guildId);
//        return guild != null ? guild.addGuildSkill(skillType, skillLevel, skillVal) : false;
//    }

//    public boolean 删除家族技能(int guildId, int skillType) {
//        MapleGuild guild = Guild.getGuild(guildId);
//        return guild != null ? guild.removeGuildSkill(skillType) : false;
//    }

//    public boolean 清空家族技能(int guildId) {
//        MapleGuild guild = Guild.getGuild(guildId);
//        if (guild != null) {
//            guild.clearGuildSkills();
//            return true;
//        } else {
//            return false;
//        }
//    }

//    public int 查询家族技能数值(int guildId, int skillType) {
//        MapleGuild guild = Guild.getGuild(guildId);
//        return guild != null ? guild.getGuildSkillVal(skillType) : -1;
//    }
//
//    public int 查询家族技能等级(int guildId, int skillType) {
//        MapleGuild guild = Guild.getGuild(guildId);
//        return guild != null ? guild.getGuildSkillLevel(skillType) : -1;
//    }
//
//    public String 查询家族技能信息(int guildId, int skillType) {
//        MapleGuild guild = Guild.getGuild(guildId);
//        return guild != null ? guild.getGuildSkillInfo(skillType) : "";
//    }

    public String getCharacterNameById(int id) {
        String name = null;
        MapleCharacter chr = MapleCharacter.getOnlineCharacterById(id);
        if (chr != null) {
            return chr.getName();
        } else {
            try {
                Connection con = DBConPool.getConnection();
                Throwable var5 = null;

                try {
                    PreparedStatement ps = null;
                    Statement stmt = con.createStatement();
                    ps = con.prepareStatement("select name from characters where id = ?");
                    ps.setInt(1, id);

                    ResultSet rs;
                    for(rs = ps.executeQuery(); rs.next(); name = rs.getString("name")) {
                    }

                    ps.close();
                    rs.close();
                } catch (Throwable var17) {
                    var5 = var17;
                    throw var17;
                } finally {
                    if (con != null) {
                        if (var5 != null) {
                            try {
                                con.close();
                            } catch (Throwable var16) {
                                var5.addSuppressed(var16);
                            }
                        } else {
                            con.close();
                        }
                    }

                }
            } catch (Exception var19) {
                FileoutputUtil.outError("logs/资料库异常.txt", var19);
            }

            return name;
        }
    }

    public int getCharacterLevelById(int id) {
        int level = -1;
        MapleCharacter chr = MapleCharacter.getOnlineCharacterById(id);
        if (chr != null) {
            return chr.getLevel();
        } else {
            try {
                Connection con = DBConPool.getConnection();
                Throwable var5 = null;

                try {
                    PreparedStatement ps = null;
                    Statement stmt = con.createStatement();
                    ps = con.prepareStatement("select level from characters where id = ?");
                    ps.setInt(1, id);

                    ResultSet rs;
                    for(rs = ps.executeQuery(); rs.next(); level = rs.getInt("level")) {
                    }

                    ps.close();
                    rs.close();
                    return level;
                } catch (Throwable var17) {
                    var5 = var17;
                    throw var17;
                } finally {
                    if (con != null) {
                        if (var5 != null) {
                            try {
                                con.close();
                            } catch (Throwable var16) {
                                var5.addSuppressed(var16);
                            }
                        } else {
                            con.close();
                        }
                    }

                }
            } catch (Exception var19) {
                FileoutputUtil.outError("logs/资料库异常.txt", var19);
                return level;
            }
        }
    }

    public boolean 定点生怪() {
        if (this.c.getPlayer() == null) {
            return false;
        } else if (this.c.getPlayer().getOneTimeLog("定点生怪权限") <= 0) {
            this.c.getPlayer().dropMessage(1, "你的角色并未开通定点生怪权限，请联系GM开启。");
            return false;
        } else {
            MapleMap map = this.c.getPlayer().getMap();
            if (map == null) {
                this.c.getPlayer().dropMessage(5, "错误，找不到地图!");
                return false;
            } else if (!GameConstants.isBossMap(map.getId()) && !GameConstants.isTownMap(map.getId()) && !GameConstants.isActivityMap(map.getId())) {
                if (GameConstants.isBanChannel(this.c.getPlayer().getClient().getChannel())) {
                    this.c.getPlayer().dropMessage(5, "当前频道不允许定点生怪!");
                    return false;
                } else {
                    if (this.c.getPlayer().getMap().getOwnerList().size() > 0 && !this.c.getPlayer().getMap().getOwnerList().contains(this.c.getPlayer().getId())) {
                        MapleCharacter owner = this.c.getPlayer().getMap().getCharacterById((Integer)this.c.getPlayer().getMap().getOwnerList().get(0));
                        if (owner != null) {
                            this.c.getPlayer().dropMessage(5, "当前地图的所有者是 " + owner.getName() + " ，你无权使用。");
                            return false;
                        }

                        this.c.getPlayer().getMap().clearOwnerList();
                    }

                    if (map.isRespawnOnePoint()) {
                        map.setRespawnOnePoint(false);
                        map.reLoadMonsterSpawn();
                        map.loadMonsterRate(false);
                        map.removeOwner(this.c.getPlayer().getId());
                        map.killAllMonsters(true, false);
                        this.c.getPlayer().dropMessage(5, "定点生怪已关闭!");
                        return true;
                    } else {
                        float expRate = 0.0F;
                        float mesoRate = 0.0F;
                        float dropRate = 0.0F;
                        Iterator var5 = ChannelServer.getAllInstances().iterator();

                        while(var5.hasNext()) {
                            ChannelServer cs = (ChannelServer)var5.next();
                            if (cs != null && GameConstants.isBanChannel(cs.getChannel())) {
                                expRate = cs.getExpRate();
                                mesoRate = cs.getMesoRate();
                                dropRate = cs.getDropRate();
                                int expRatio = (Integer)LtMS.ConfigValuesMap.get("定点生怪经验倍率百分比");
                                int mesoRatio = (Integer)LtMS.ConfigValuesMap.get("定点生怪金币倍率百分比");
                                int dropRatio = (Integer)LtMS.ConfigValuesMap.get("定点生怪爆物倍率百分比");
                                expRate *= (float)expRatio / 100.0F;
                                mesoRate *= (float)mesoRatio / 100.0F;
                                dropRate *= (float)dropRatio / 100.0F;
                                break;
                            }
                        }

                        if (expRate <= 0.0F && mesoRate <= 0.0F && dropRate <= 0.0F) {
                            expRate = this.c.getPlayer().getClient().getChannelServer().getExpRate();
                            mesoRate = this.c.getPlayer().getClient().getChannelServer().getMesoRate();
                            dropRate = this.c.getPlayer().getClient().getChannelServer().getDropRate();
                        }

                        this.c.getPlayer().getClient().getChannelServer().setExpRate(expRate);
                        this.c.getPlayer().getClient().getChannelServer().setMesoRate(mesoRate);
                        this.c.getPlayer().getClient().getChannelServer().setDropRate(dropRate);
                        map.setRespawnOnePoint(true);
                        map.setRespawnPoint(this.c.getPlayer().getPosition());
                        map.setRespawnOnePointMoves(this.c.getPlayer().getLastRes());
                        map.addOwner(this.c.getPlayer().getId());
                        map.killAllMonsters(true, false);
                        this.c.getPlayer().dropMessage(5, "定点生怪已开启!");
                        return true;
                    }
                }
            } else {
                this.c.getPlayer().dropMessage(5, "当前地图不允许定点生怪!");
                return false;
            }
        }
    }

    public boolean 物落脚下() {
        if (this.c.getPlayer() == null) {
            return false;
        } else if (this.c.getPlayer().getOneTimeLog("物落脚下权限") <= 0) {
            this.c.getPlayer().dropMessage(1, "你的角色并未开通物落脚下权限，请联系GM开启。");
            return false;
        } else if (GameConstants.isBanChannel(this.c.getPlayer().getClient().getChannel())) {
            this.c.getPlayer().dropMessage(5, "当前频道不允许物落脚下!");
            return false;
        } else if (this.c.getPlayer().isDropOnMyFoot()) {
            this.c.getPlayer().setDropOnMyFoot(false);
            this.c.getPlayer().dropMessage(5, "物落脚下已关闭!");
            return true;
        } else {
            this.c.getPlayer().setDropOnMyFoot(true);
            this.c.getPlayer().dropMessage(5, "物落脚下已开启!");
            return true;
        }
    }

    public boolean 物落背包() {
        if (this.c.getPlayer() == null) {
            return false;
        } else if (this.c.getPlayer().getOneTimeLog("物落背包权限") <= 0) {
            this.c.getPlayer().dropMessage(1, "你的角色并未开通物落背包权限，请联系GM开启。");
            return false;
        } else if (GameConstants.isBanChannel(this.c.getPlayer().getClient().getChannel())) {
            this.c.getPlayer().dropMessage(5, "当前频道不允许物落背包!");
            return false;
        } else if (this.c.getPlayer().isDropOnMyBag()) {
            this.c.getPlayer().setDropOnMyBag(false);
            this.c.getPlayer().dropMessage(5, "物落背包已关闭!");
            return true;
        } else {
            this.c.getPlayer().setDropOnMyBag(true);
            this.c.getPlayer().dropMessage(5, "物落背包已开启!");
            return true;
        }
    }

    public boolean 吸怪(int seconds) {
        if (this.c.getPlayer() == null) {
            return false;
        } else {
            final MapleMap map = this.c.getPlayer().getMap();
            if (this.c.getPlayer().getOneTimeLog("吸怪权限") <= 0) {
                this.c.getPlayer().dropMessage(1, "你的角色并未开通吸怪权限，请联系GM开启。");
                return false;
            } else if (map == null) {
                this.c.getPlayer().dropMessage(5, "错误，找不到地图。");
                return false;
            } else if (!GameConstants.isBossMap(map.getId()) && !GameConstants.isTownMap(map.getId()) && !GameConstants.isActivityMap(map.getId())) {
                final int mapId = this.c.getPlayer().getMap().getId();
                final int channel = this.c.getChannel();
                final Point position = this.c.getPlayer().getPosition();
                final List<LifeMovementFragment> moves = this.c.getPlayer().getLastRes();
                Timer.MapTimer mapTimer = Timer.MapTimer.getInstance();
                if (this.c.getPlayer().getMap().getOwnerList().size() > 0 && !this.c.getPlayer().getMap().getOwnerList().contains(this.c.getPlayer().getId())) {
                    MapleCharacter owner = this.c.getPlayer().getMap().getCharacterById((Integer)this.c.getPlayer().getMap().getOwnerList().get(0));
                    if (owner != null) {
                        this.c.getPlayer().dropMessage(5, "当前地图的所有者是 " + owner.getName() + " ，你无权使用。");
                        return false;
                    }

                    this.c.getPlayer().getMap().clearOwnerList();
                }

                this.c.getPlayer().getMap().addOwner(this.c.getPlayer().getId());
                if (seconds <= 0 && this.c.getPlayer().getMap().MobVacSchedule != null && !this.c.getPlayer().getMap().MobVacSchedule.isCancelled()) {
                    if (this.c.getPlayer() != null) {
                        this.c.getPlayer().getMap().MobVacSchedule.cancel(false);
                        this.c.getPlayer().dropMessage(5, "吸怪已关闭。");
                        this.c.getPlayer().getMap().clearOwnerList();
                    } else {
                        map.MobVacSchedule.cancel(false);
                    }

                    return true;
                } else {
                    if (this.c.getPlayer() != null && (this.c.getPlayer().getMap().MobVacSchedule == null || this.c.getPlayer().getMap().MobVacSchedule.isCancelled())) {
                        this.c.getPlayer().getMap().MobVacSchedule = mapTimer.register(new Runnable() {
                            public void run() {
                                try {
                                    if (NPCConversationManager.this.c.getPlayer() != null && mapId == NPCConversationManager.this.c.getPlayer().getMap().getId() && channel == NPCConversationManager.this.c.getChannel()) {
                                        Iterator var1 = NPCConversationManager.this.c.getPlayer().getMap().getAllMonstersThreadsafe().iterator();

                                        while(var1.hasNext()) {
                                            MapleMapObject mmo = (MapleMapObject)var1.next();
                                            MapleMonster monster = (MapleMonster)mmo;
                                            if (monster != null && !monster.getStats().isBoss()) {
                                                NPCConversationManager.this.c.getPlayer().getMap().broadcastMessage(MobPacket.moveMonster(false, -1, 0, 0, 0, 0, monster.getObjectId(), monster.getPosition(), position, moves));
                                                monster.setPosition(NPCConversationManager.this.c.getPlayer().getPosition());
                                            }
                                        }
                                    } else {
                                        map.clearOwnerList();
                                        map.MobVacSchedule.cancel(false);
                                        if (NPCConversationManager.this.c.getPlayer() != null) {
                                            NPCConversationManager.this.c.getPlayer().dropMessage(5, "因为你离开了地图，吸怪已自动关闭。");
                                        }
                                    }
                                } catch (Exception var4) {
                                    服务端输出信息.println_err("【错误】吸怪命令子线程错误，错误原因：" + var4);
                                }

                            }
                        }, (long)(seconds * 1000));
                    }

                    this.c.getPlayer().dropMessage(5, "吸怪已开启，间隔 " + seconds + " 秒。");
                    return true;
                }
            } else {
                this.c.getPlayer().dropMessage(5, "当前地图不允许吸怪。");
                return false;
            }
        }
    }

    public int getOneTimeLogi(String log) {
        return this.c.getPlayer().getOneTimeLogi(log);
    }

    public void setOneTimeLogi(String log) {
        this.c.getPlayer().setOneTimeLogi(log);
    }

    public int getBossLogi(String bossid) {
        return this.c.getPlayer().getBossLogi(bossid);
    }

    public void setBossLogi(String bossid) {
        this.c.getPlayer().setBossLogi(bossid);
    }

    public String getKookId(int chrId) {
        return JKook.getKookId(chrId);
    }

    public boolean isKookBind(int chrId) {
        return JKook.isBind(chrId);
    }

    public boolean isKookRewarded(int chrId) {
        return JKook.isRewarded(chrId);
    }

    public int getKookRewardCount(int chrId) {
        return JKook.getRewardCount(chrId);
    }

    public int addKookRewardCount(int chrId) {
        return JKook.addRewardCount(chrId);
    }

    public boolean sqlUpdate(String text, Object... value) {
        try {
            Connection con = DBConPool.getConnection();
            Throwable var4 = null;

            try {
                PreparedStatement ps = con.prepareStatement(text);
                if (value != null && value.length > 0) {
                    for(int i = 1; i <= value.length; ++i) {
                        if (value[i - 1] instanceof Integer) {
                            ps.setInt(i, (Integer)value[i - 1]);
                        } else if (value[i - 1] instanceof String) {
                            ps.setString(i, (String)value[i - 1]);
                        } else if (value[i - 1] instanceof Double) {
                            ps.setDouble(i, (Double)value[i - 1]);
                        } else if (value[i - 1] instanceof Float) {
                            ps.setFloat(i, (Float)value[i - 1]);
                        } else if (value[i - 1] instanceof Long) {
                            ps.setLong(i, (Long)value[i - 1]);
                        } else if (value[i - 1] instanceof Boolean) {
                            ps.setBoolean(i, (Boolean)value[i - 1]);
                        } else if (value[i - 1] instanceof Date) {
                            ps.setDate(i, (Date)value[i - 1]);
                        } else {
                            boolean var7;
                            if (value[i - 1] instanceof Integer[]) {
                                if (((Integer[])((Integer[])value[i - 1])).length <= 0) {
                                    var7 = false;
                                    return var7;
                                }

                                ps.setInt(i, ((Integer[])((Integer[])value[i - 1]))[0]);
                            } else if (value[i - 1] instanceof int[]) {
                                if (((int[])((int[])value[i - 1])).length <= 0) {
                                    var7 = false;
                                    return var7;
                                }

                                ps.setInt(i, ((int[])((int[])value[i - 1]))[0]);
                            } else if (value[i - 1] instanceof String[]) {
                                if (((String[])((String[])value[i - 1])).length <= 0) {
                                    var7 = false;
                                    return var7;
                                }

                                ps.setString(i, ((String[])((String[])value[i - 1]))[0]);
                            } else if (value[i - 1] instanceof Double[]) {
                                if (((Double[])((Double[])value[i - 1])).length <= 0) {
                                    var7 = false;
                                    return var7;
                                }

                                ps.setDouble(i, ((Double[])((Double[])value[i - 1]))[0]);
                            } else if (value[i - 1] instanceof Float[]) {
                                if (((Float[])((Float[])value[i - 1])).length <= 0) {
                                    var7 = false;
                                    return var7;
                                }

                                ps.setFloat(i, ((Float[])((Float[])value[i - 1]))[0]);
                            } else if (value[i - 1] instanceof Long[]) {
                                if (((Long[])((Long[])value[i - 1])).length <= 0) {
                                    var7 = false;
                                    return var7;
                                }

                                ps.setLong(i, ((Long[])((Long[])value[i - 1]))[0]);
                            } else if (value[i - 1] instanceof Boolean[]) {
                                if (((Boolean[])((Boolean[])value[i - 1])).length <= 0) {
                                    var7 = false;
                                    return var7;
                                }

                                ps.setBoolean(i, ((Boolean[])((Boolean[])value[i - 1]))[0]);
                            } else {
                                if (!(value[i - 1] instanceof Date[])) {
                                    var7 = false;
                                    return var7;
                                }

                                if (((Date[])((Date[])value[i - 1])).length <= 0) {
                                    var7 = false;
                                    return var7;
                                }

                                ps.setDate(i, ((Date[])((Date[])value[i - 1]))[0]);
                            }
                        }
                    }
                }

                ps.executeUpdate();
                ps.close();
                boolean var31 = true;
                return var31;
            } catch (Throwable var27) {
                var4 = var27;
                throw var27;
            } finally {
                if (con != null) {
                    if (var4 != null) {
                        try {
                            con.close();
                        } catch (Throwable var26) {
                            var4.addSuppressed(var26);
                        }
                    } else {
                        con.close();
                    }
                }

            }
        } catch (SQLException var29) {
            服务端输出信息.println_err("【错误】sqlUpdate错误，原因：" + var29);
            var29.printStackTrace();
            return false;
        } catch (Exception var30) {
            服务端输出信息.println_err("【错误】sqlUpdate错误，原因：" + var30);
            var30.printStackTrace();
            return false;
        }
    }
    public void 结束对话() {
        NPCScriptManager.getInstance().dispose(this.c);
    }

    public boolean sqlUpdate(String text) {
        return this.sqlUpdate(text, (Object[])null);
    }

    public boolean sqlInsert(String text, Object... value) {
        return this.sqlUpdate(text, value);
    }

    public boolean sqlInsert(String text) {
        return this.sqlUpdate(text);
    }

    public Object[] sqlSelect(String text, Object... value) {
        try {
            Connection con = DBConPool.getConnection();
            PreparedStatement ps = con.prepareStatement(text);
            if (value != null && value.length > 0) {
                for(int i = 1; i <= value.length; ++i) {
                    if (value[i - 1] instanceof Integer) {
                        ps.setInt(i, (Integer)value[i - 1]);
                    } else if (value[i - 1] instanceof String) {
                        ps.setString(i, (String)value[i - 1]);
                    } else if (value[i - 1] instanceof Double) {
                        ps.setDouble(i, (Double)value[i - 1]);
                    } else if (value[i - 1] instanceof Float) {
                        ps.setFloat(i, (Float)value[i - 1]);
                    } else if (value[i - 1] instanceof Long) {
                        ps.setLong(i, (Long)value[i - 1]);
                    } else if (value[i - 1] instanceof Boolean) {
                        ps.setBoolean(i, (Boolean)value[i - 1]);
                    } else if (value[i - 1] instanceof Date) {
                        ps.setDate(i, (Date)value[i - 1]);
                    } else if (value[i - 1] instanceof Integer[]) {
                        if (((Integer[])((Integer[])value[i - 1])).length <= 0) {
                            return null;
                        }

                        ps.setInt(i, ((Integer[])((Integer[])value[i - 1]))[0]);
                    } else if (value[i - 1] instanceof int[]) {
                        if (((int[])((int[])value[i - 1])).length <= 0) {
                            return null;
                        }

                        ps.setInt(i, ((int[])((int[])value[i - 1]))[0]);
                    } else if (value[i - 1] instanceof String[]) {
                        if (((String[])((String[])value[i - 1])).length <= 0) {
                            return null;
                        }

                        ps.setString(i, ((String[])((String[])value[i - 1]))[0]);
                    } else if (value[i - 1] instanceof Double[]) {
                        if (((Double[])((Double[])value[i - 1])).length <= 0) {
                            return null;
                        }

                        ps.setDouble(i, ((Double[])((Double[])value[i - 1]))[0]);
                    } else if (value[i - 1] instanceof Float[]) {
                        if (((Float[])((Float[])value[i - 1])).length <= 0) {
                            return null;
                        }

                        ps.setFloat(i, ((Float[])((Float[])value[i - 1]))[0]);
                    } else if (value[i - 1] instanceof Long[]) {
                        if (((Long[])((Long[])value[i - 1])).length <= 0) {
                            return null;
                        }

                        ps.setLong(i, ((Long[])((Long[])value[i - 1]))[0]);
                    } else if (value[i - 1] instanceof Boolean[]) {
                        if (((Boolean[])((Boolean[])value[i - 1])).length <= 0) {
                            return null;
                        }

                        ps.setBoolean(i, ((Boolean[])((Boolean[])value[i - 1]))[0]);
                    } else {
                        if (!(value[i - 1] instanceof Date[])) {
                            return null;
                        }

                        if (((Date[])((Date[])value[i - 1])).length <= 0) {
                            return null;
                        }

                        ps.setDate(i, ((Date[])((Date[])value[i - 1]))[0]);
                    }
                }
            }

            ResultSet rs = ps.executeQuery();
            rs.last();
            Object[] ret = new Object[rs.getRow()];
            rs.beforeFirst();

            for(int count = 0; rs.next(); ++count) {
                int column = rs.getMetaData().getColumnCount();
                Map<String, Object> obj = new HashMap();

                for(int i = 1; i <= column; ++i) {
                    obj.put(rs.getMetaData().getColumnName(i), rs.getObject(i));
                }

                ret[count] = obj;
            }

            ps.close();
            rs.close();
            return ret;
        } catch (SQLException var11) {
            服务端输出信息.println_err("【错误】sqlSelect错误，原因：" + var11);
            var11.printStackTrace();
            return null;
        }
    }

    public Object[] sqlSelect(String text) {
        return this.sqlSelect(text, (Object[])null);
    }

    public void showlvl_s() {
        this.c.sendPacket(MaplePacketCreator.showlevelRanks(this.npc, MapleGuildRanking.getInstance().getLevelRank_s()));
    }
    public void MapleMSpvpdeaths() {
        MapleGuildRanking.MapleMSpvpdeaths(this.c, this.npc);
    }

    public void showmeso_s() {
        this.c.sendPacket(MaplePacketCreator.showmesoRanks(this.npc, MapleGuildRanking.getInstance().getMesoRank_s()));
    }

    public String showJzMeso(int mapid) {
        String a = "目前排名：\r\n\r\n";
        List<MapleGuildRanking.JzRankingInfo> all = MapleGuildRanking.getInstance().getJzRank(mapid);

        StringBuilder var10000;
        MapleGuildRanking.JzRankingInfo info;
        for(Iterator var4 = all.iterator(); var4.hasNext(); a = var10000.append(MapleCharacter.getCharacterNameById(info.getId())).append("#k : ").append(info.getMeso()).append("金币\r\n").toString()) {
            info = (MapleGuildRanking.JzRankingInfo)var4.next();
            var10000 = (new StringBuilder()).append(a).append(" #b");
            this.getPlayer();
        }

        a = a + "\r\n我不能透露现在捐赠的人数有谁，";
        a = a + "\r\n这些记录每周六凌晨23：59截止每周一凌晨00：00清空。";
        return a;
    }
    public void MapleMSpvpkills() {
        MapleGuildRanking.MapleMSpvpkills(this.c, this.npc);
    }

    public void 增加角色最大生命值(short hp) {
        Map<MapleStat, Integer> statup = new EnumMap(MapleStat.class);
        if (this.c.getPlayer().getStat().getMaxHp() + hp > 30000) {
            this.c.getPlayer().getStat().setMaxHp((short)30000);
        } else {
            this.c.getPlayer().getStat().setMaxHp((short)(this.c.getPlayer().getStat().getMaxHp() + hp));
        }

        statup.put(MapleStat.MAXHP, Integer.valueOf(this.c.getPlayer().getStat().getMaxHp()));
        this.c.sendPacket(MaplePacketCreator.updatePlayerStats(statup, this.c.getPlayer()));
    }

    public void 增加角色最大法力值(short MP) {
        Map<MapleStat, Integer> statup = new EnumMap(MapleStat.class);
        if (this.c.getPlayer().getStat().getMaxMp() + MP > 30000) {
            this.c.getPlayer().getStat().setMaxMp((short)30000);
        } else {
            this.c.getPlayer().getStat().setMaxMp((short)(this.c.getPlayer().getStat().getMaxMp() + MP));
        }

        statup.put(MapleStat.MAXMP, Integer.valueOf(this.c.getPlayer().getStat().getMaxMp()));
        this.c.sendPacket(MaplePacketCreator.updatePlayerStats(statup, this.c.getPlayer()));
    }

    public int 判断兑换卡是否存在(String id) {
        int data = 0;

        try {
            Connection con = DBConPool.getConnection();
            Throwable var4 = null;

            try {
                PreparedStatement ps = con.prepareStatement("SELECT code as DATA FROM nxcodez WHERE code = ?");
                ps.setString(1, id);
                ResultSet rs = ps.executeQuery();
                Throwable var7 = null;

                try {
                    if (rs.next()) {
                        ++data;
                    }
                } catch (Throwable var32) {
                    var7 = var32;
                    throw var32;
                } finally {
                    if (rs != null) {
                        if (var7 != null) {
                            try {
                                rs.close();
                            } catch (Throwable var31) {
                                var7.addSuppressed(var31);
                            }
                        } else {
                            rs.close();
                        }
                    }

                }
            } catch (Throwable var34) {
                var4 = var34;
                throw var34;
            } finally {
                if (con != null) {
                    if (var4 != null) {
                        try {
                            con.close();
                        } catch (Throwable var30) {
                            var4.addSuppressed(var30);
                        }
                    } else {
                        con.close();
                    }
                }

            }
        } catch (SQLException var36) {
            服务端输出信息.println_err("判断兑换卡是否存在、出错");
        }

        return data;
    }

    public int 判断兑换卡类型(String code) throws SQLException {
        int item = -1;

        try {
            Connection con = DBConPool.getConnection();
            Throwable var4 = null;

            try {
                PreparedStatement ps = con.prepareStatement("SELECT `leixing` FROM nxcodez WHERE code = ?");
                ps.setString(1, code);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    item = rs.getInt("leixing");
                }

                rs.close();
                ps.close();
            } catch (Throwable var15) {
                var4 = var15;
                throw var15;
            } finally {
                if (con != null) {
                    if (var4 != null) {
                        try {
                            con.close();
                        } catch (Throwable var14) {
                            var4.addSuppressed(var14);
                        }
                    } else {
                        con.close();
                    }
                }

            }
        } catch (SQLException var17) {
            服务端输出信息.println_err("判断兑换卡是否存在、出错");
        }

        return item;
    }

    public int 判断兑换卡数额(String code) throws SQLException {
        int item = -1;

        try {
            Connection con = DBConPool.getConnection();
            Throwable var4 = null;

            try {
                PreparedStatement ps = con.prepareStatement("SELECT `valid` FROM nxcodez WHERE code = ?");
                ps.setString(1, code);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    item = rs.getInt("valid");
                }

                rs.close();
                ps.close();
            } catch (Throwable var15) {
                var4 = var15;
                throw var15;
            } finally {
                if (con != null) {
                    if (var4 != null) {
                        try {
                            con.close();
                        } catch (Throwable var14) {
                            var4.addSuppressed(var14);
                        }
                    } else {
                        con.close();
                    }
                }

            }
        } catch (SQLException var17) {
            服务端输出信息.println_err("判断兑换卡是否存在、出错");
        }

        return item;
    }

    public int 判断兑换卡礼包(String code) throws SQLException {
        int item = -1;

        try {
            Connection con = DBConPool.getConnection();
            Throwable var4 = null;

            try {
                PreparedStatement ps = con.prepareStatement("SELECT `itme` FROM nxcodez WHERE code = ?");
                ps.setString(1, code);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    item = rs.getInt("itme");
                }

                rs.close();
                ps.close();
            } catch (Throwable var15) {
                var4 = var15;
                throw var15;
            } finally {
                if (con != null) {
                    if (var4 != null) {
                        try {
                            con.close();
                        } catch (Throwable var14) {
                            var4.addSuppressed(var14);
                        }
                    } else {
                        con.close();
                    }
                }

            }
        } catch (SQLException var17) {
            服务端输出信息.println_err("判断兑换卡是否存在、出错");
        }

        return item;
    }

    public void Deleteexchangecard(String a) {
        PreparedStatement ps1 = null;
        ResultSet rs = null;

        try {
            Connection con = DBConPool.getConnection();
            Throwable var5 = null;

            try {
                ps1 = con.prepareStatement("SELECT * FROM nxcodez ");
                rs = ps1.executeQuery();

                while(rs.next()) {
                    String sqlstr = " delete from nxcodez where code = '" + a + "' ";
                    ps1.executeUpdate(sqlstr);
                }
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
        }

    }

    public void openMerchantItemStore1() {
        MerchItemPackage pack = loadItemFrom_Database(this.c.getPlayer().getId(), this.c.getPlayer().getAccountID());
        this.c.sendPacket(PlayerShopPacket.merchItemStore_ItemData(pack));
    }

    private static final MerchItemPackage loadItemFrom_Database(int charid, int accountid) {
        try {
            Connection con = DBConPool.getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT * from hiredmerch where characterid = ? OR accountid = ?");
            ps.setInt(1, charid);
            ps.setInt(2, accountid);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                ps.close();
                rs.close();
                return null;
            } else {
                int packageid = rs.getInt("PackageId");
                MerchItemPackage pack = new MerchItemPackage();
                pack.setPackageid(packageid);
                pack.setMesos(rs.getInt("Mesos"));
                pack.setSentTime(rs.getLong("time"));
                ps.close();
                rs.close();
                Map<Long, Pair<IItem, MapleInventoryType>> items = ItemLoader.HIRED_MERCHANT.loadItems(false, new Integer[]{charid});
                if (items != null) {
                    List<IItem> iters = new ArrayList();
                    Iterator var9 = items.values().iterator();

                    while(var9.hasNext()) {
                        Pair<IItem, MapleInventoryType> z = (Pair)var9.next();
                        iters.add(z.left);
                    }

                    pack.setItems(iters);
                }

                return pack;
            }
        } catch (SQLException var11) {
            服务端输出信息.println_err(var11);
            return null;
        }
    }

}
