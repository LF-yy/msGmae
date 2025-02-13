package bean;

import java.util.Map;

public class HideAttribute {
    public int id;
    public short totalDropRate = 0;
    public short totalDropRateCount = 0;
    public short totalExpRate = 0;
    public short totalExpRateCount = 0;
    public short totalMesoRate = 0;
    public short totalMesoRateCount = 0;
    //抗性（可抵抗BOSS释放的debuff技能）
    public short totalResistance = 0;
    //闪避
    public short totalDodge = 0;
    public short total_normal_damage_percent;
    public short total_boss_damage_percent;
    public short total_total_damage_percent;
    public Map<Integer,Double> skillDamage;

    public Map<Integer, Double> getSkillDamage() {
        return skillDamage;
    }

    public void setSkillDamage(Map<Integer, Double> skillDamage) {
        this.skillDamage = skillDamage;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public short getTotalDropRate() {
        return totalDropRate;
    }

    public void setTotalDropRate(short totalDropRate) {
        this.totalDropRate = totalDropRate;
    }

    public short getTotalDropRateCount() {
        return totalDropRateCount;
    }

    public void setTotalDropRateCount(short totalDropRateCount) {
        this.totalDropRateCount = totalDropRateCount;
    }

    public short getTotalExpRate() {
        return totalExpRate;
    }

    public void setTotalExpRate(short totalExpRate) {
        this.totalExpRate = totalExpRate;
    }

    public short getTotalExpRateCount() {
        return totalExpRateCount;
    }

    public void setTotalExpRateCount(short totalExpRateCount) {
        this.totalExpRateCount = totalExpRateCount;
    }

    public short getTotalMesoRate() {
        return totalMesoRate;
    }

    public void setTotalMesoRate(short totalMesoRate) {
        this.totalMesoRate = totalMesoRate;
    }

    public short getTotalMesoRateCount() {
        return totalMesoRateCount;
    }

    public void setTotalMesoRateCount(short totalMesoRateCount) {
        this.totalMesoRateCount = totalMesoRateCount;
    }

    public short getTotalResistance() {
        return totalResistance;
    }

    public void setTotalResistance(short totalResistance) {
        this.totalResistance = totalResistance;
    }

    public short getTotalDodge() {
        return totalDodge;
    }

    public void setTotalDodge(short totalDodge) {
        this.totalDodge = totalDodge;
    }

    public short getTotal_normal_damage_percent() {
        return total_normal_damage_percent;
    }

    public void setTotal_normal_damage_percent(short total_normal_damage_percent) {
        this.total_normal_damage_percent = total_normal_damage_percent;
    }

    public short getTotal_boss_damage_percent() {
        return total_boss_damage_percent;
    }

    public void setTotal_boss_damage_percent(short total_boss_damage_percent) {
        this.total_boss_damage_percent = total_boss_damage_percent;
    }

    public short getTotal_total_damage_percent() {
        return total_total_damage_percent;
    }

    public void setTotal_total_damage_percent(short total_total_damage_percent) {
        this.total_total_damage_percent = total_total_damage_percent;
    }
}
