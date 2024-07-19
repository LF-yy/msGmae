package bean;

public class Leveladdharm {
    private int leve;
    private int numb;

    public int getLevel() {
        return leve;
    }

    public void setLevel(int level) {
        this.leve = level;
    }

    public int getHarm() {
        return numb;
    }

    public void setHarm(int harm) {
        this.numb = harm;
    }

    public Leveladdharm(int leve, int numb) {
        this.leve = leve;
        this.numb = numb;
    }
}
