package bean;

public class LtPeakLevel {
    private long id;
    private int characterid;
    private int level;
    private long level_ex;

    public LtPeakLevel(long id, int characterid, int level, long level_ex) {
        this.id = id;
        this.characterid = characterid;
        this.level = level;
        this.level_ex = level_ex;
    }
    public LtPeakLevel( int characterid, int level, long level_ex) {
        this.characterid = characterid;
        this.level = level;
        this.level_ex = level_ex;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getCharacterid() {
        return characterid;
    }

    public void setCharacterid(int characterid) {
        this.characterid = characterid;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public long getLevel_ex() {
        return level_ex;
    }

    public void setLevel_ex(long level_ex) {
        this.level_ex = level_ex;
    }
}
