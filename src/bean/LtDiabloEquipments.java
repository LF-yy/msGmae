package bean;

import client.MapleCharacter;

import java.util.List;

public class LtDiabloEquipments {

    private int id; // 自增ID
    private String entryName; // 词条名
    private short probability; // 概率
    private short str; // 力量
    private short dex; // 敏捷
    private short _int; // 智力
    private short luk; // 运气
    private short watk; // 物理攻击
    private short matk; // 魔力
    private short wdef; // 物理防御
    private short mdef; // 魔法防御
    private short maxhp; // 血量
    private short maxmp; // 蓝量
    private short resistance; // 抗性（可抵抗BOSS释放的debuff技能）
    private short dodge; // 闪避（可闪避所有）
    private short strPercent; // 力量百分比
    private short dexPercent; // 敏捷百分比
    private short intPercent; // 智力百分比
    private short lukPercent; // 运气百分比
    private short skillId; // 附加技能
    private short skillType; // 技能类型
    private short skillDamage; // 技能伤害
    private short skillDs; // 技能段数
    private short skillSl; // 技能打击数量
    private String skillTx; // 特效
    private short watkPercent; // 物理攻击百分比
    private short matkPercent; // 魔力百分比
    private short wdefPercent; // 物理防御百分比
    private short mdefPercent; // 魔法防御百分比
    private short maxhpPercent; // 血量百分比
    private short maxmpPercent; // 蓝量百分比
    private short normalDamagePercent; // 普通怪物伤害加成
    private short bossDamagePercent; // boss伤害加成
    private short totalDamagePercent; // 总伤害加成
    private short dropRate; // 掉落率
    private short dropRateCount; // 掉落率数量
    private short expRate; // 经验倍率
    private short expRateCount; // 经验倍率
    private short mesoRate; // 掉落率
    private short mesoRateCount; // 掉落率

    /**
     * 统计转换
     * @param list
     */
    public static void dataStatistical(List<LtDiabloEquipments> list, MapleCharacter character){

    }
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEntryName() {
        return entryName;
    }

    public void setEntryName(String entryName) {
        this.entryName = entryName;
    }

    public short getProbability() {
        return probability;
    }

    public void setProbability(short probability) {
        this.probability = probability;
    }

    public short getStr() {
        return str;
    }

    public void setStr(short str) {
        this.str = str;
    }

    public short getDex() {
        return dex;
    }

    public void setDex(short dex) {
        this.dex = dex;
    }

    public short get_int() {
        return _int;
    }

    public void set_int(short _int) {
        this._int = _int;
    }

    public short getLuk() {
        return luk;
    }

    public void setLuk(short luk) {
        this.luk = luk;
    }

    public short getWatk() {
        return watk;
    }

    public void setWatk(short watk) {
        this.watk = watk;
    }

    public short getMatk() {
        return matk;
    }

    public void setMatk(short matk) {
        this.matk = matk;
    }

    public short getWdef() {
        return wdef;
    }

    public void setWdef(short wdef) {
        this.wdef = wdef;
    }

    public short getMdef() {
        return mdef;
    }

    public void setMdef(short mdef) {
        this.mdef = mdef;
    }

    public short getMaxhp() {
        return maxhp;
    }

    public void setMaxhp(short maxhp) {
        this.maxhp = maxhp;
    }

    public short getMaxmp() {
        return maxmp;
    }

    public void setMaxmp(short maxmp) {
        this.maxmp = maxmp;
    }

    public short getResistance() {
        return resistance;
    }

    public void setResistance(short resistance) {
        this.resistance = resistance;
    }

    public short getDodge() {
        return dodge;
    }

    public void setDodge(short dodge) {
        this.dodge = dodge;
    }

    public short getStrPercent() {
        return strPercent;
    }

    public void setStrPercent(short strPercent) {
        this.strPercent = strPercent;
    }

    public short getDexPercent() {
        return dexPercent;
    }

    public void setDexPercent(short dexPercent) {
        this.dexPercent = dexPercent;
    }

    public short getIntPercent() {
        return intPercent;
    }

    public void setIntPercent(short intPercent) {
        this.intPercent = intPercent;
    }

    public short getLukPercent() {
        return lukPercent;
    }

    public void setLukPercent(short lukPercent) {
        this.lukPercent = lukPercent;
    }

    public short getSkillId() {
        return skillId;
    }

    public void setSkillId(short skillId) {
        this.skillId = skillId;
    }

    public short getSkillType() {
        return skillType;
    }

    public void setSkillType(short skillType) {
        this.skillType = skillType;
    }

    public short getSkillDamage() {
        return skillDamage;
    }

    public void setSkillDamage(short skillDamage) {
        this.skillDamage = skillDamage;
    }

    public short getSkillDs() {
        return skillDs;
    }

    public void setSkillDs(short skillDs) {
        this.skillDs = skillDs;
    }

    public short getSkillSl() {
        return skillSl;
    }

    public void setSkillSl(short skillSl) {
        this.skillSl = skillSl;
    }

    public String getSkillTx() {
        return skillTx;
    }

    public void setSkillTx(String skillTx) {
        this.skillTx = skillTx;
    }

    public short getWatkPercent() {
        return watkPercent;
    }

    public void setWatkPercent(short watkPercent) {
        this.watkPercent = watkPercent;
    }

    public short getMatkPercent() {
        return matkPercent;
    }

    public void setMatkPercent(short matkPercent) {
        this.matkPercent = matkPercent;
    }

    public short getWdefPercent() {
        return wdefPercent;
    }

    public void setWdefPercent(short wdefPercent) {
        this.wdefPercent = wdefPercent;
    }

    public short getMdefPercent() {
        return mdefPercent;
    }

    public void setMdefPercent(short mdefPercent) {
        this.mdefPercent = mdefPercent;
    }

    public short getMaxhpPercent() {
        return maxhpPercent;
    }

    public void setMaxhpPercent(short maxhpPercent) {
        this.maxhpPercent = maxhpPercent;
    }

    public short getMaxmpPercent() {
        return maxmpPercent;
    }

    public void setMaxmpPercent(short maxmpPercent) {
        this.maxmpPercent = maxmpPercent;
    }

    public short getNormalDamagePercent() {
        return normalDamagePercent;
    }

    public void setNormalDamagePercent(short normalDamagePercent) {
        this.normalDamagePercent = normalDamagePercent;
    }

    public short getBossDamagePercent() {
        return bossDamagePercent;
    }

    public void setBossDamagePercent(short bossDamagePercent) {
        this.bossDamagePercent = bossDamagePercent;
    }

    public short getTotalDamagePercent() {
        return totalDamagePercent;
    }

    public void setTotalDamagePercent(short totalDamagePercent) {
        this.totalDamagePercent = totalDamagePercent;
    }

    public short getDropRate() {
        return dropRate;
    }

    public void setDropRate(short dropRate) {
        this.dropRate = dropRate;
    }

    public short getDropRateCount() {
        return dropRateCount;
    }

    public void setDropRateCount(short dropRateCount) {
        this.dropRateCount = dropRateCount;
    }

    public short getExpRate() {
        return expRate;
    }

    public void setExpRate(short expRate) {
        this.expRate = expRate;
    }

    public short getExpRateCount() {
        return expRateCount;
    }

    public void setExpRateCount(short expRateCount) {
        this.expRateCount = expRateCount;
    }

    public short getMesoRate() {
        return mesoRate;
    }

    public void setMesoRate(short mesoRate) {
        this.mesoRate = mesoRate;
    }

    public short getMesoRateCount() {
        return mesoRateCount;
    }

    public void setMesoRateCount(short mesoRateCount) {
        this.mesoRateCount = mesoRateCount;
    }
}
