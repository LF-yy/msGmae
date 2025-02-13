package bean;

public class LtMonsterPosition {

    private long id    ;
    private int map_id ;
    private int monster_id ;
    private String monster_name ;
    private int position_x  ;
    private int position_y;
    private int isBOSS;
    private int level;

    public LtMonsterPosition(long id, int map_id, int monster_id, String monster_name, int position_x, int position_y) {
        this.id = id;
        this.map_id = map_id;
        this.monster_id = monster_id;
        this.monster_name = monster_name;
        this.position_x = position_x;
        this.position_y = position_y;
    }
    public LtMonsterPosition(int map_id, int monster_id, String monster_name, int position_x, int position_y) {
        this.map_id = map_id;
        this.monster_id = monster_id;
        this.monster_name = monster_name;
        this.position_x = position_x;
        this.position_y = position_y;
    }
    public LtMonsterPosition(int map_id, int monster_id, String monster_name, int position_x, int position_y,int isBOSS,int level) {
        this.map_id = map_id;
        this.monster_id = monster_id;
        this.monster_name = monster_name;
        this.position_x = position_x;
        this.position_y = position_y;
        this.isBOSS = isBOSS;
        this.level = level;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getIsBOSS() {
        return isBOSS;
    }

    public void setIsBOSS(int isBOSS) {
        this.isBOSS = isBOSS;
    }

    public int getMap_id() {
        return map_id;
    }

    public void setMap_id(int map_id) {
        this.map_id = map_id;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getMonster_id() {
        return monster_id;
    }

    public void setMonster_id(int monster_id) {
        this.monster_id = monster_id;
    }

    public String getMonster_name() {
        return monster_name;
    }

    public void setMonster_name(String monster_name) {
        this.monster_name = monster_name;
    }

    public int getPosition_x() {
        return position_x;
    }

    public void setPosition_x(int position_x) {
        this.position_x = position_x;
    }

    public int getPosition_y() {
        return position_y;
    }

    public void setPosition_y(int position_y) {
        this.position_y = position_y;
    }
}
