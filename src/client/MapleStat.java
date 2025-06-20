package client;

public enum MapleStat
{
    SKIN(1), 
    FACE(2), 
    HAIR(4), 
    LEVEL(64), 
    JOB(128), 
    STR(256), 
    DEX(512), 
    INT(1024), 
    LUK(2048), 
    HP(4096), 
    MAXHP(8192), 
    MP(16384), 
    MAXMP(32768), 
    AVAILABLEAP(65536), 
    AVAILABLESP(131072), 
    EXP(262144), 
    FAME(524288), 
    MESO(1048576), 
    PET(56), 
    GACHAPONEXP(2097152);
    
    private final int i;
    
    private MapleStat(final int i) {
        this.i = i;
    }
    
    public int getValue() {
        return this.i;
    }
    
    public static MapleStat getByValue(final int value) {
        for (final MapleStat stat : values()) {
            if (stat.i == value) {
                return stat;
            }
        }
        return null;
    }
    
    public enum Temp
    {
        STR(1), 
        DEX(2), 
        INT(4), 
        LUK(8), 
        WATK(16), 
        WDEF(32), //物理防御
        MATK(64), 
        MDEF(128),//魔法防御
        ACC(256), //命中
        AVOID(512), //回避
        SPEED(1024), //速度
        JUMP(2048);//跳跃
        
        private final int i;
        
        private Temp(final int i) {
            this.i = i;
        }
        
        public int getValue() {
            return this.i;
        }
    }
}
