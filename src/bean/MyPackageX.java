package bean;

import snail.PackageOfEquipments;

import java.util.ArrayList;

public class MyPackageX {
    private int complete;
    private ArrayList<Integer> itemIdList;
    private short str;
    private short dex;
    private short _int;
    private short luk;
    private short all_ap;
    private short watk;
    private short matk;
    private short wdef;
    private short mdef;
    private short acc;
    private short avoid;
    private short maxhp;
    private short maxmp;
    private short speed;
    private short jump;
    private short str_percent;
    private short dex_percent;
    private short _int_percent;
    private short luk_percent;
    private short all_ap_percent;
    private short watk_percent;
    private short matk_percent;
    private short wdef_percent;
    private short mdef_percent;
    private short acc_percent;
    private short avoid_percent;
    private short maxhp_percent;
    private short maxmp_percent;
    private short normal_damage_percent;
    private short boss_damage_percent;
    private short total_damage_percent;


    public MyPackageX(PackageOfEquipments.MyPackage myPackage) {
        this.itemIdList = myPackage.getItemIdList();
        this.str = myPackage.getStr();
        this.dex = myPackage.getDex();
        this._int = myPackage.get_int();
        this.luk = myPackage.getLuk();
        this.all_ap = myPackage.getAll_ap();
        this.watk = myPackage.getWatk();
        this.matk = myPackage.getMatk();
        this.wdef = myPackage.getWdef();
        this.mdef = myPackage.getMdef();
        this.acc = myPackage.getAcc();
        this.avoid = myPackage.getAvoid();
        this.maxhp = myPackage.getMaxhp();
        this.maxmp = myPackage.getMaxmp();
        this.speed = myPackage.getSpeed();
        this.jump = myPackage.getJump();
        this.str_percent = myPackage.getStr_percent();
        this.dex_percent = myPackage.getDex_percent();
        this._int_percent = myPackage.get_int_percent();
        this.luk_percent = myPackage.getLuk_percent();
        this.all_ap_percent = myPackage.getAll_ap_percent();
        this.watk_percent = myPackage.getWatk_percent();
        this.matk_percent = myPackage.getMatk_percent();
        this.wdef_percent = myPackage.getWdef_percent();
        this.mdef_percent = myPackage.getMdef_percent();
        this.acc_percent = myPackage.getAcc_percent();
        this.avoid_percent = myPackage.getAvoid_percent();
        this.maxhp_percent = myPackage.getMaxhp_percent();
        this.maxmp_percent = myPackage.getMaxmp_percent();
        this.normal_damage_percent = myPackage.getNormal_damage_percent();
        this.boss_damage_percent = myPackage.getBoss_damage_percent();
        this.total_damage_percent = myPackage.getTotal_damage_percent();
    }
    public int getComplete() {
        return complete;
    }

    public void setComplete(int complete) {
        this.complete = complete;
    }

    public ArrayList<Integer> getItemIdList() {
        return itemIdList;
    }

    public void setItemIdList(ArrayList<Integer> itemIdList) {
        this.itemIdList = itemIdList;
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

    public short getAll_ap() {
        return all_ap;
    }

    public void setAll_ap(short all_ap) {
        this.all_ap = all_ap;
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

    public short getAcc() {
        return acc;
    }

    public void setAcc(short acc) {
        this.acc = acc;
    }

    public short getAvoid() {
        return avoid;
    }

    public void setAvoid(short avoid) {
        this.avoid = avoid;
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

    public short getSpeed() {
        return speed;
    }

    public void setSpeed(short speed) {
        this.speed = speed;
    }

    public short getJump() {
        return jump;
    }

    public void setJump(short jump) {
        this.jump = jump;
    }

    public short getStr_percent() {
        return str_percent;
    }

    public void setStr_percent(short str_percent) {
        this.str_percent = str_percent;
    }

    public short getDex_percent() {
        return dex_percent;
    }

    public void setDex_percent(short dex_percent) {
        this.dex_percent = dex_percent;
    }

    public short get_int_percent() {
        return _int_percent;
    }

    public void set_int_percent(short _int_percent) {
        this._int_percent = _int_percent;
    }

    public short getLuk_percent() {
        return luk_percent;
    }

    public void setLuk_percent(short luk_percent) {
        this.luk_percent = luk_percent;
    }

    public short getAll_ap_percent() {
        return all_ap_percent;
    }

    public void setAll_ap_percent(short all_ap_percent) {
        this.all_ap_percent = all_ap_percent;
    }

    public short getWatk_percent() {
        return watk_percent;
    }

    public void setWatk_percent(short watk_percent) {
        this.watk_percent = watk_percent;
    }

    public short getMatk_percent() {
        return matk_percent;
    }

    public void setMatk_percent(short matk_percent) {
        this.matk_percent = matk_percent;
    }

    public short getWdef_percent() {
        return wdef_percent;
    }

    public void setWdef_percent(short wdef_percent) {
        this.wdef_percent = wdef_percent;
    }

    public short getMdef_percent() {
        return mdef_percent;
    }

    public void setMdef_percent(short mdef_percent) {
        this.mdef_percent = mdef_percent;
    }

    public short getAcc_percent() {
        return acc_percent;
    }

    public void setAcc_percent(short acc_percent) {
        this.acc_percent = acc_percent;
    }

    public short getAvoid_percent() {
        return avoid_percent;
    }

    public void setAvoid_percent(short avoid_percent) {
        this.avoid_percent = avoid_percent;
    }

    public short getMaxhp_percent() {
        return maxhp_percent;
    }

    public void setMaxhp_percent(short maxhp_percent) {
        this.maxhp_percent = maxhp_percent;
    }

    public short getMaxmp_percent() {
        return maxmp_percent;
    }

    public void setMaxmp_percent(short maxmp_percent) {
        this.maxmp_percent = maxmp_percent;
    }

    public short getNormal_damage_percent() {
        return normal_damage_percent;
    }

    public void setNormal_damage_percent(short normal_damage_percent) {
        this.normal_damage_percent = normal_damage_percent;
    }

    public short getBoss_damage_percent() {
        return boss_damage_percent;
    }

    public void setBoss_damage_percent(short boss_damage_percent) {
        this.boss_damage_percent = boss_damage_percent;
    }

    public short getTotal_damage_percent() {
        return total_damage_percent;
    }

    public void setTotal_damage_percent(short total_damage_percent) {
        this.total_damage_percent = total_damage_percent;
    }
}
