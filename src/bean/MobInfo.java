package bean;

/**
 * '怪物血量重载表'
 */

public class MobInfo {
     private long id    ;//bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'ID',
     private int mobId ;//   int(11) NOT NULL   DEFAULT 0 COMMENT '怪物id',
     private String name  ;//  varchar(64) NOT NULL  DEFAULT '怪物' COMMENT '怪物名称',
     private long hp    ;//bigint(20) NOT NULL  DEFAULT 10000 COMMENT '怪物血量',
     private int mp    ;//int(11) DEFAULT 0 COMMENT '蓝量',
     private int level ;//   int(11) DEFAULT 0 COMMENT '等级',
     private int speed ;//   int(11) DEFAULT 0 COMMENT '速度',
     private int eva   ;// int(11) DEFAULT 0 COMMENT '闪避',
     private int damage;//    int(11) DEFAULT 0 COMMENT '伤害',
     private int exp   ;// int(11) DEFAULT 0 COMMENT '经验',
     private int pdd   ;// int(11) DEFAULT 0 COMMENT '防御',

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getMobId() {
        return mobId;
    }

    public void setMobId(int mobId) {
        this.mobId = mobId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getHp() {
        return hp;
    }

    public void setHp(long hp) {
        this.hp = hp;
    }

    public int getMp() {
        return mp;
    }

    public void setMp(int mp) {
        this.mp = mp;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public int getEva() {
        return eva;
    }

    public void setEva(int eva) {
        this.eva = eva;
    }

    public int getDamage() {
        return damage;
    }

    public void setDamage(int damage) {
        this.damage = damage;
    }

    public int getExp() {
        return exp;
    }

    public void setExp(int exp) {
        this.exp = exp;
    }

    public int getPdd() {
        return pdd;
    }

    public void setPdd(int pdd) {
        this.pdd = pdd;
    }
}
