package server.life;

public class MonsterDropEntry
{
    public short questid;
    public int dropperid;
    public int itemId;
    public int chance;
    public int Minimum;
    public int Maximum;
    public String droppername;
    public String itemname;
    public MonsterDropEntry( final int itemId, final int chance, final int Minimum, final int Maximum, final short questid) {
        //this.dropperid = dropperid;
        this.itemId = itemId;
        this.chance = chance;
        this.questid = questid;
        this.Minimum = Minimum;
        this.Maximum = Maximum;
    }
    public MonsterDropEntry(short questid, int dropperid, int itemId, int chance, int minimum, int maximum) {
        this.questid = questid;
        this.dropperid = dropperid;
        this.itemId = itemId;
        this.chance = chance;
        this.Minimum = minimum;
        this.Maximum = maximum;
    }
    public MonsterDropEntry(short questid, int dropperid, int itemId, int chance, int minimum, int maximum,String droppername,String itemname) {
        this.questid = questid;
        this.dropperid = dropperid;
        this.itemId = itemId;
        this.chance = chance;
        this.Minimum = minimum;
        this.Maximum = maximum;
        this.droppername = droppername;
        this.itemname = itemname;
    }
  public int getDropperid() {
      return dropperid;
  }

  public void setDropperid(int dropperid) {
      this.dropperid = dropperid;
  }

    public short getQuestid() {
        return questid;
    }

    public void setQuestid(short questid) {
        this.questid = questid;
    }

    public int getItemId() {
        return itemId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public int getChance() {
        return chance;
    }

    public void setChance(int chance) {
        this.chance = chance;
    }

    public int getMinimum() {
        return Minimum;
    }

    public void setMinimum(int minimum) {
        Minimum = minimum;
    }

    public int getMaximum() {
        return Maximum;
    }

    public void setMaximum(int maximum) {
        Maximum = maximum;
    }
}
