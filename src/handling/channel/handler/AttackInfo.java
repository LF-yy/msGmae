package handling.channel.handler;

import tools.FileoutputUtil;
import server.AutobanManager;
import client.SkillFactory;
import constants.GameConstants;
import server.MapleStatEffect;
import client.ISkill;
import client.MapleCharacter;
import java.awt.Point;
import tools.AttackPair;

import java.util.Calendar;
import java.util.List;

public class AttackInfo
{
    /**
     * 技能标识，用于区分不同的技能
     */
    public int skill;
    /**
     * 充能水平，表示技能的当前充能状态
     */
    public int charge;
    /**
     * 上次攻击时的Tick计数，用于计算攻击间隔
     */
    public int lastAttackTickCount;
    /**
     * 所有伤害数据，存储每次攻击的伤害值和位置信息
     */
    public List<AttackPair> allDamage;
    /**
     * 位置坐标，表示技能释放的位置
     */
    public Point position;
    /**
     * 位置坐标xy，强调二维平面上的坐标位置
     */
    public Point positionxy;
    /**
     * 击中次数，表示技能击中的次数
     */
    public byte hits;
    /**
     * 目标数量，技能所影响的目标数目
     */
    public byte targets;
    /**
     * 未知字节，用于未明确的字节数据，可能保留作未来使用
     */
    public byte tbyte;
    /**
     * 显示类型，表示技能在游戏中的显示方式
     */
    public byte display;
    /**
     * 动画标识，用于指定技能释放时的动画效果
     */
    public byte animation;
    /**
     * 速度，技能释放的速度或移动速度
     */
    public byte speed;
    /**
     * CS星级，可能表示技能的稀有度或星级
     */
    public byte csstar;
    /**
     * 群体伤害范围，表示技能的AOE（Area of Effect）范围
     */
    public byte AOE;
    /**
     * 插槽，技能所属的插槽，用于技能装备或选择
     */
    public byte slot;
    /**
     * 未知数据，用于未明确的字节数据，可能保留作未来使用
     */
    public byte unk;
    /**
     * 是否真实，表示技能是否是真实的或有效的
     */
    public boolean real;


    public AttackInfo() {
        this.real = true;
    }
    
    public final MapleStatEffect getAttackEffect(MapleCharacter chr, int skillLevel, final ISkill skill_) {
        if (GameConstants.isMulungSkill(this.skill) || GameConstants.isPyramidSkill(this.skill)) {
            skillLevel = 1;
        }
        else if (skillLevel <= 0) {
            return null;
        }
        if(this.skill == 5121002 || this.skill == 5121004){
            return skill_.getEffect(skillLevel);
        }
        if (GameConstants.isLinkedSkill(this.skill)) {
            final ISkill skillLink = SkillFactory.getSkill(this.skill);
            if (this.display > 80 && !skillLink.hasAction()) {
                AutobanManager.getInstance().autoban(chr.getClient(), "攻击无延迟，技能ID： : " + this.skill);
                return null;
            }
            return skillLink.getEffect(skillLevel);
        } else {
            if (this.skill != skill_.getId()) {
                FileoutputUtil.logToFile("logs/Data/AttackEffect.txt", "" + FileoutputUtil.NowTime() + " 連結技能[" + this.skill + "](" + skill_.getId() + "傳承) 連結技能等級:" + skillLevel + " 不在getLinkedkill清单內卻被觸發, 觸發者: " + chr.getName() + " 职业: " + (int)chr.getJob() + " 等級: " + (int)chr.getLevel() + "\r\n");
            }
            if (this.display > 80 && !skill_.hasAction()) {
                AutobanManager.getInstance().autoban(chr.getClient(), "攻击无延迟，技能ID： " + this.skill);
                return null;
            }
            return skill_.getEffect(skillLevel);
        }
    }
    public int getSkill() {
        return skill;
    }

    public void setSkill(int skill) {
        this.skill = skill;
    }

    public int getCharge() {
        return charge;
    }

    public void setCharge(int charge) {
        this.charge = charge;
    }

    public int getLastAttackTickCount() {
        return lastAttackTickCount;
    }

    public void setLastAttackTickCount(int lastAttackTickCount) {
        this.lastAttackTickCount = lastAttackTickCount;
    }

    public List<AttackPair> getAllDamage() {
        return allDamage;
    }

    public void setAllDamage(List<AttackPair> allDamage) {
        this.allDamage = allDamage;
    }

    public Point getPosition() {
        return position;
    }

    public void setPosition(Point position) {
        this.position = position;
    }

    public Point getPositionxy() {
        return positionxy;
    }

    public void setPositionxy(Point positionxy) {
        this.positionxy = positionxy;
    }

    public byte getHits() {
        return hits;
    }

    public void setHits(byte hits) {
        this.hits = hits;
    }

    public byte getTargets() {
        return targets;
    }

    public void setTargets(byte targets) {
        this.targets = targets;
    }

    public byte getTbyte() {
        return tbyte;
    }

    public void setTbyte(byte tbyte) {
        this.tbyte = tbyte;
    }

    public byte getDisplay() {
        return display;
    }

    public void setDisplay(byte display) {
        this.display = display;
    }

    public byte getAnimation() {
        return animation;
    }

    public void setAnimation(byte animation) {
        this.animation = animation;
    }

    public byte getSpeed() {
        return speed;
    }

    public void setSpeed(byte speed) {
        this.speed = speed;
    }

    public byte getCsstar() {
        return csstar;
    }

    public void setCsstar(byte csstar) {
        this.csstar = csstar;
    }

    public byte getAOE() {
        return AOE;
    }

    public void setAOE(byte AOE) {
        this.AOE = AOE;
    }

    public byte getSlot() {
        return slot;
    }

    public void setSlot(byte slot) {
        this.slot = slot;
    }

    public byte getUnk() {
        return unk;
    }

    public void setUnk(byte unk) {
        this.unk = unk;
    }

    public boolean isReal() {
        return real;
    }

    public void setReal(boolean real) {
        this.real = real;
    }
}
