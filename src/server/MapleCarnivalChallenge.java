package server;

import java.util.Iterator;
import handling.world.MaplePartyCharacter;
import client.MapleCharacter;
import java.lang.ref.WeakReference;

public class MapleCarnivalChallenge
{
    WeakReference<MapleCharacter> challenger;
    String challengeinfo;
    
    public MapleCarnivalChallenge(final MapleCharacter challenger) {
        this.challengeinfo = "";
        this.challenger = new WeakReference<MapleCharacter>(challenger);
        this.challengeinfo += "#b";
        for (final MaplePartyCharacter pc : challenger.getParty().getMembers()) {
            final MapleCharacter c = challenger.getMap().getCharacterById(pc.getId());
            if (c != null) {
                this.challengeinfo = this.challengeinfo + c.getName() + " / 等級" + (int)c.getLevel() + " / " + getJobNameById((int)c.getJob()) + "\r\n";
            }
        }
        this.challengeinfo += "#k";
    }
    
    public MapleCharacter getChallenger() {
        return (MapleCharacter)this.challenger.get();
    }
    
    public String getChallengeInfo() {
        return this.challengeinfo;
    }

    public static int getIdByJobName(final String id) {
        switch (id) {
            case "新手": {
                return 0;
            }
            case "初心者": {
                return 1000;
            }
            case "战童": {
                return 2000;
            }
            case "战士": {
                return 100;
            }
            case "剑客": {
                return 110;
            }
            case "勇士": {
                return 111;
            }
            case "英雄": {
                return 112;
            }
            case "准骑士": {
                return 120;
            }
            case "骑士": {
                return 121;
            }
            case "圣骑士": {
                return 122;
            }
            case "枪战士": {
                return 130;
            }
            case "龙骑士": {
                return 131;
            }
            case "黑骑士": {
                return 132;
            }
            case "魔法师": {
                return 200;
            }
            case "巫师(火,毒)": {
                return 210;
            }
            case "魔导士(火,毒)": {
                return 211;
            }
            case "大魔导士(火,毒)": {
                return 212;
            }
            case "巫师(冰,雷)": {
                return 220;
            }
            case "魔导士(冰,雷)": {
                return 221;
            }
            case "大魔导士(冰,雷)": {
                return 222;
            }
            case "牧师": {
                return 230;
            }
            case "祭司": {
                return 231;
            }
            case "主教": {
                return 232;
            }
            case "弓箭手": {
                return 300;
            }
            case "猎人": {
                return 310;
            }
            case "射手": {
                return 311;
            }
            case "神射手": {
                return 312;
            }
            case "弩弓手": {
                return 320;
            }
            case "游侠": {
                return 321;
            }
            case "箭神": {
                return 322;
            }
            case "盜贼": {
                return 400;
            }
            case "刺客": {
                return 410;
            }
            case "无影人": {
                return 411;
            }
            case "隐士": {
                return 412;
            }
            case "海盜": {
                return 500;
            }
            case "拳手": {
                return 510;
            }
            case "斗士": {
                return 511;
            }
            case "冲锋队长": {
                return 512;
            }
            case "火枪手": {
                return 520;
            }
            case "大副": {
                return 521;
            }
            case "船长": {
                return 522;
            }
            case "魂骑士1转": {
                return 1100;
            }
            case "魂骑士2转": {
                return 1110;
            }
            case "魂骑士3转": {
                return 1111;
            }
            case "魂骑士4转": {
                return 1112;
            }
            case "炎术士1转": {
                return 1200;
            }
            case "炎术士2转": {
                return 1210;
            }
            case "炎术士3转": {
                return 1211;
            }
            case "炎术士4转": {
                return 1212;
            }
            case "风灵使者1转": {
                return 1300;
            }
            case "风灵使者2转": {
                return 1310;
            }
            case "风灵使者3转": {
                return 1311;
            }
            case "风灵使者4转": {
                return 1312;
            }
            case "夜行者1转": {
                return 1400;
            }
            case "夜行者2转": {
                return 1410;
            }
            case "夜行者3转": {
                return 1411;
            }
            case "夜行者4转": {
                return 1412;
            }
            case "奇袭者1转": {
                return 1500;
            }
            case "奇袭者2转": {
                return 1510;
            }
            case "奇袭者3转": {
                return 1511;
            }
            case "奇袭者4转": {
                return 1512;
            }
            case "战神1转": {
                return 2100;
            }
            case "战神2转": {
                return 2110;
            }
            case "战神3转": {
                return 2111;
            }
            case "战神4转": {
                return 2112;
            }
            case "巡查者": {
                return 800;
            }
            case "管理员": {
                return 900;
            }
            default: {
                return -1;
            }
        }
    }

    public static String getJobNameById(final int job) {
        switch (job) {
            case 0: {
                return "新手";
            }
            case 1000: {
                return "初心者";
            }
            case 2000: {
                return "战童";
            }
            case 100: {
                return "战士";
            }
            case 110: {
                return "剑客";
            }
            case 111: {
                return "勇士";
            }
            case 112: {
                return "英雄";
            }
            case 120: {
                return "准骑士";
            }
            case 121: {
                return "骑士";
            }
            case 122: {
                return "圣骑士";
            }
            case 130: {
                return "枪战士";
            }
            case 131: {
                return "龙骑士";
            }
            case 132: {
                return "黑骑士";
            }
            case 200: {
                return "魔法师";
            }
            case 210: {
                return "巫师(火,毒)";
            }
            case 211: {
                return "魔导士(火,毒)";
            }
            case 212: {
                return "大魔导士(火,毒)";
            }
            case 220: {
                return "巫师(冰,雷)";
            }
            case 221: {
                return "魔导士(冰,雷)";
            }
            case 222: {
                return "大魔导士(冰,雷)";
            }
            case 230: {
                return "牧师";
            }
            case 231: {
                return "祭司";
            }
            case 232: {
                return "主教";
            }
            case 300: {
                return "弓箭手";
            }
            case 310: {
                return "猎人";
            }
            case 311: {
                return "射手";
            }
            case 312: {
                return "神射手";
            }
            case 320: {
                return "弩弓手";
            }
            case 321: {
                return "游侠";
            }
            case 322: {
                return "箭神";
            }
            case 400: {
                return "盜贼";
            }
            case 410: {
                return "刺客";
            }
            case 411: {
                return "无影人";
            }
            case 412: {
                return "隐士";
            }
            case 420: {
                return "侠客";
            }
            case 421: {
                return "独行客";
            }
            case 422: {
                return "侠盗";
            }
            case 500: {
                return "海盜";
            }
            case 510: {
                return "拳手";
            }
            case 511: {
                return "斗士";
            }
            case 512: {
                return "冲锋队长";
            }
            case 520: {
                return "火枪手";
            }
            case 521: {
                return "大副";
            }
            case 522: {
                return "船长";
            }
            case 1100: {
                return "魂骑士1转";
            }
            case 1110: {
                return "魂骑士2转";
            }
            case 1111: {
                return "魂骑士3转";
            }
            case 1112: {
                return "魂骑士4转";
            }
            case 1200: {
                return "炎术士1转";
            }
            case 1210: {
                return "炎术士2转";
            }
            case 1211: {
                return "炎术士3转";
            }
            case 1212: {
                return "炎术士4转";
            }
            case 1300: {
                return "风灵使者1转";
            }
            case 1310: {
                return "风灵使者2转";
            }
            case 1311: {
                return "风灵使者3转";
            }
            case 1312: {
                return "风灵使者4转";
            }
            case 1400: {
                return "夜行者1转";
            }
            case 1410: {
                return "夜行者2转";
            }
            case 1411: {
                return "夜行者3转";
            }
            case 1412: {
                return "夜行者4转";
            }
            case 1500: {
                return "奇袭者1转";
            }
            case 1510: {
                return "奇袭者2转";
            }
            case 1511: {
                return "奇袭者3转";
            }
            case 1512: {
                return "奇袭者4转";
            }
            case 2100: {
                return "战神1转";
            }
            case 2110: {
                return "战神2转";
            }
            case 2111: {
                return "战神3转";
            }
            case 2112: {
                return "战神4转";
            }
            case 800: {
                return "巡查者";
            }
            case 900: {
                return "管理员";
            }
            default: {
                return "未知";
            }
        }
    }

    
    public static String getJobBasicNameById(final int job) {
        switch (job) {
            case 0:
            case 1000:
            case 2000:
            case 2001:
            case 3000: {
                return "初心者";
            }
            case 100:
            case 110:
            case 111:
            case 112:
            case 120:
            case 121:
            case 122:
            case 130:
            case 131:
            case 132:
            case 1100:
            case 1110:
            case 1111:
            case 1112:
            case 2100:
            case 2110:
            case 2111:
            case 2112: {
                return "劍士";
            }
            case 200:
            case 210:
            case 211:
            case 212:
            case 220:
            case 221:
            case 222:
            case 230:
            case 231:
            case 232:
            case 1200:
            case 1210:
            case 1211:
            case 1212:
            case 2200:
            case 2210:
            case 2211:
            case 2212:
            case 2213:
            case 2214:
            case 2215:
            case 2216:
            case 2217:
            case 2218:
            case 3200:
            case 3210:
            case 3211:
            case 3212: {
                return "法師";
            }
            case 300:
            case 310:
            case 311:
            case 312:
            case 320:
            case 321:
            case 322:
            case 1300:
            case 1310:
            case 1311:
            case 1312:
            case 3300:
            case 3310:
            case 3311:
            case 3312: {
                return "弓箭手";
            }
            case 400:
            case 410:
            case 411:
            case 412:
            case 420:
            case 421:
            case 422:
            case 430:
            case 431:
            case 432:
            case 433:
            case 434:
            case 1400:
            case 1410:
            case 1411:
            case 1412: {
                return "盜賊";
            }
            case 500:
            case 510:
            case 511:
            case 512:
            case 520:
            case 521:
            case 522:
            case 1500:
            case 1510:
            case 1511:
            case 1512:
            case 3500:
            case 3510:
            case 3511:
            case 3512: {
                return "海盜";
            }
            default: {
                return "未知";
            }
        }
    }


}
