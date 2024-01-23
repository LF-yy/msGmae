package bean;

public class BossInMap
{
    private int mobid;
    private int map;
    private int x;
    private int y;
    private String msg;
    private int time;
    
    public BossInMap(final int mobid, final int map, final int x, final int y, final String msg, final int time) {
        this.mobid = mobid;
        this.map = map;
        this.x = x;
        this.y = y;
        this.msg = msg;
        this.time = time;
    }
    
    public int getMobid() {
        return this.mobid;
    }
    
    public void setMobid(final int mobid) {
        this.mobid = mobid;
    }
    
    public int getMap() {
        return this.map;
    }
    
    public void setMap(final int map) {
        this.map = map;
    }
    
    public int getX() {
        return this.x;
    }
    
    public void setX(final int x) {
        this.x = x;
    }
    
    public int getY() {
        return this.y;
    }
    
    public void setY(final int y) {
        this.y = y;
    }
    
    public String getMsg() {
        return this.msg;
    }
    
    public void setMsg(final String msg) {
        this.msg = msg;
    }
    
    public int getTime() {
        return this.time;
    }
    
    public void setTime(final int time) {
        this.time = time;
    }
}
