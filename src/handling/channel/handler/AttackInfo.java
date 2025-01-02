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
    public int skill;
    public int charge;
    public int lastAttackTickCount;
    public List<AttackPair> allDamage;
    public Point position;
    public Point positionxy;
    public byte hits;
    public byte targets;
    public byte tbyte;
    public byte display;
    public byte animation;
    public byte speed;
    public byte csstar;
    public byte AOE;
    public byte slot;
    public byte unk;
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
                FileoutputUtil.logToFile("logs/Data/AttackEffect.txt", "" + FileoutputUtil.NowTime() + " 連結技能[" + this.skill + "](" + skill_.getId() + "傳承) 連結技能等級:" + skillLevel + " 不在getLinkedkill清單內卻被觸發, 觸發者: " + chr.getName() + " 职业: " + (int)chr.getJob() + " 等級: " + (int)chr.getLevel() + "\r\n");
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
