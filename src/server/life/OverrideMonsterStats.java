package server.life;

public class OverrideMonsterStats
{
    private long hp;
    private int exp;
    private int mp;
    private int level;
    
    public OverrideMonsterStats() {
        this.hp = 0L;
        this.exp = 0;
        this.mp = 0;
    }
    
    public OverrideMonsterStats(final long hp, final int mp, final int exp, final boolean change) {
        this.hp = hp;
        this.mp = mp;
        this.exp = exp;
    }
    
    public OverrideMonsterStats(final long hp, final int mp, final int exp) {
        this(hp, mp, exp, true);
    }
    
    
    public int getExp() {
        return this.exp;
    }
    
    public void setOExp(final int exp) {
        this.exp = exp;
    }
    
    public long getHp() {
        return this.hp;
    }
    
    public void setOHp(final long hp) {
        this.hp = hp;
    }
    
    public int getMp() {
        return this.mp;
    }
    
    public void setOMp(final int mp) {
        this.mp = mp;
    }
    
    public int getlevel() {
        return this.level;
    }

    public void setLevel(int level) {
        this.level = level;
    }
}
