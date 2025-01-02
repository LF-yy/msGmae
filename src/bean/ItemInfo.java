package bean;

public class ItemInfo {
    private Long id;           // id
    private Integer itemId;   // item_id
    private Byte upgradeSlots; // upgradeslots
    private Short str;        // str
    private Short dex;        // dex
    private Short intValue;   // int
    private Short luk;        // luk
    private Short hp;         // hp
    private Short mp;         // mp
    private Short watk;       // watk
    private Short matk;       // matk
    private Short wdef;       // wdef
    private Short mdef;       // mdef

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getItemId() {
        return itemId;
    }

    public void setItemId(Integer itemId) {
        this.itemId = itemId;
    }

    public Byte getUpgradeSlots() {
        return upgradeSlots;
    }

    public void setUpgradeSlots(Byte upgradeSlots) {
        this.upgradeSlots = upgradeSlots;
    }

    public Short getStr() {
        return str;
    }

    public void setStr(Short str) {
        this.str = str;
    }

    public Short getDex() {
        return dex;
    }

    public void setDex(Short dex) {
        this.dex = dex;
    }

    public Short getIntValue() {
        return intValue;
    }

    public void setIntValue(Short intValue) {
        this.intValue = intValue;
    }

    public Short getLuk() {
        return luk;
    }

    public void setLuk(Short luk) {
        this.luk = luk;
    }

    public Short getHp() {
        return hp;
    }

    public void setHp(Short hp) {
        this.hp = hp;
    }

    public Short getMp() {
        return mp;
    }

    public void setMp(Short mp) {
        this.mp = mp;
    }

    public Short getWatk() {
        return watk;
    }

    public void setWatk(Short watk) {
        this.watk = watk;
    }

    public Short getMatk() {
        return matk;
    }

    public void setMatk(Short matk) {
        this.matk = matk;
    }

    public Short getWdef() {
        return wdef;
    }

    public void setWdef(Short wdef) {
        this.wdef = wdef;
    }

    public Short getMdef() {
        return mdef;
    }

    public void setMdef(Short mdef) {
        this.mdef = mdef;
    }
}
