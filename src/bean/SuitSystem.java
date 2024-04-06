package bean;

import java.util.Hashtable;
import java.util.Map;

public class SuitSystem {
      private  long id           ;//bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'ID',
      private  String name       ;//varchar(64) DEFAULT NULL COMMENT '套装名称',
      private  String equipList;//varchar(255) NOT NULL COMMENT '装备列表',
      private  int triggerNumber;//int DEFAULT 5 COMMENT '触发数',
      private  int haveNub;//int DEFAULT 5 COMMENT '拥有件数',
      private  boolean isEffective ;//int DEFAULT 5 COMMENT '是否有效',
      private  int localstr;//int DEFAULT NULL COMMENT '力量',
      private  int localdex;//int DEFAULT NULL COMMENT '敏捷',
      private  int localluk;//int DEFAULT NULL COMMENT '运气',
      private  int localint;//int DEFAULT NULL COMMENT '智力',
      private  int allQuality;//int DEFAULT NULL COMMENT '全属性',
      private  int harm          ;//int DEFAULT NULL COMMENT '伤害',
      private  int crit          ;//int DEFAULT NULL COMMENT '暴击率',
      private  int critHarm;//int DEFAULT NULL COMMENT '暴击伤害',
      private  int bossHarm;//int DEFAULT NULL COMMENT 'BOSS伤害',
      private  int hp            ;//int DEFAULT NULL COMMENT 'hp',
      private  int mp            ;//int DEFAULT NULL COMMENT 'mp',
      private  int pad            ;//int DEFAULT NULL COMMENT 'mp',
      private  int matk            ;//int DEFAULT NULL COMMENT 'mp',
      public  static Map<Integer, Map<String,String>> SuitSystemMap;//数据集合
    {
        SuitSystemMap = new Hashtable<>();
    }
    public int Pad() {
        return pad;
    }

    public void setPad(int pad) {
        this.pad = pad;
    }

    public int getMatk() {
        return matk;
    }

    public void setMatk(int matk) {
        this.matk = matk;
    }

    public int getHaveNub() {
        return haveNub;
    }

    public void setHaveNub(int haveNub) {
        this.haveNub = haveNub;
    }

    public boolean isEffective() {
        return isEffective;
    }

    public void setEffective(boolean effective) {
        isEffective = effective;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEquipList() {
        return equipList;
    }

    public void setEquipList(String equipList) {
        this.equipList = equipList;
    }

    public int getTriggerNumber() {
        return triggerNumber;
    }

    public void setTriggerNumber(int triggerNumber) {
        this.triggerNumber = triggerNumber;
    }

    public int getLocalstr() {
        return localstr;
    }

    public void setLocalstr(int localstr) {
        this.localstr = localstr;
    }

    public int getLocaldex() {
        return localdex;
    }

    public void setLocaldex(int localdex) {
        this.localdex = localdex;
    }

    public int getLocalluk() {
        return localluk;
    }

    public void setLocalluk(int localluk) {
        this.localluk = localluk;
    }

    public int getLocalint() {
        return localint;
    }

    public void setLocalint(int localint) {
        this.localint = localint;
    }

    public int getAllQuality() {
        return allQuality;
    }

    public void setAllQuality(int allQuality) {
        this.allQuality = allQuality;
    }

    public int getHarm() {
        return harm;
    }

    public void setHarm(int harm) {
        this.harm = harm;
    }

    public int getCrit() {
        return crit;
    }

    public void setCrit(int crit) {
        this.crit = crit;
    }

    public int getCritHarm() {
        return critHarm;
    }

    public void setCritHarm(int critHarm) {
        this.critHarm = critHarm;
    }

    public int getBossHarm() {
        return bossHarm;
    }

    public void setBossHarm(int bossHarm) {
        this.bossHarm = bossHarm;
    }

    public int getHp() {
        return hp;
    }

    public void setHp(int hp) {
        this.hp = hp;
    }

    public int getMp() {
        return mp;
    }

    public void setMp(int mp) {
        this.mp = mp;
    }
}
