package bean;

/**
 * '野外BOSS刷新表'
 */
public class LtMobSpawnBoss {
          private long id    ;//    bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'ID',
          private int mapid ;//       int(11) NOT NULL DEFAULT '0' COMMENT '怪物id',
          private int mobid ;//       int(11) NOT NULL DEFAULT '0' COMMENT '怪物id',
          private String name  ;//      varchar(64) NOT NULL DEFAULT '怪物' COMMENT '怪物名称',
          private int x     ;//   int(11) NOT NULL DEFAULT '0' COMMENT '坐标x ',
          private int y     ;//   int(11) DEFAULT '0' COMMENT '坐标y',
          private int x1    ;//    int(11) DEFAULT '0' COMMENT '坐标x',
          private int y1    ;//    int(11) DEFAULT '0' COMMENT '坐标y',
          private int x2    ;//    int(11) DEFAULT '0' COMMENT '坐标x',
          private int y2    ;//    int(11) DEFAULT '0' COMMENT '坐标y',
          private int time  ;//      int(11) DEFAULT '0' COMMENT '刷新时间间隔',

    public LtMobSpawnBoss() {
    }

    public LtMobSpawnBoss(int mapid, int mobid, String name, int x, int y, int x1, int y1, int x2, int y2, int time) {
        this.mapid = mapid;
        this.mobid = mobid;
        this.name = name;
        this.x = x;
        this.y = y;
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
        this.time = time;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getMapid() {
        return mapid;
    }

    public void setMapid(int mapid) {
        this.mapid = mapid;
    }

    public int getMobid() {
        return mobid;
    }

    public void setMobid(int mobid) {
        this.mobid = mobid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getX1() {
        return x1;
    }

    public void setX1(int x1) {
        this.x1 = x1;
    }

    public int getY1() {
        return y1;
    }

    public void setY1(int y1) {
        this.y1 = y1;
    }

    public int getX2() {
        return x2;
    }

    public void setX2(int x2) {
        this.x2 = x2;
    }

    public int getY2() {
        return y2;
    }

    public void setY2(int y2) {
        this.y2 = y2;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }
}
