package gui;

import client.MapleCharacter;
import server.life.MapleMonster;
import server.maps.MapleMap;
import tools.FileoutputUtil;
import handling.world.World.Broadcast;
import tools.MaplePacketCreator;
import server.life.MapleLifeFactory;
import handling.channel.ChannelServer;
import handling.world.MapleParty;

import java.awt.*;
import java.util.Collections;
import java.util.Date;

public class 活动野外通缉
{
    public static void 随机通缉() {
        if (new Date().getHours() >= 21) {// && (
//            final int a = (int)Math.ceil(Math.random() * 25.0);
//            final int[][] 通缉 = { { 2220000, 104000400 }, { 5220001, 110040000 }, { 7220000, 250010304 }, { 8220000, 200010300 }, { 7220002, 250010503 }, { 7220001, 222010310 }, { 6220000, 107000300 }, { 5220002, 100040105 }, { 5220003, 220050100 }, { 6220001, 221040301 }, { 8220003, 240040401 }, { 3220001, 260010201 }, { 8220002, 261030000 }, { 4220000, 230020100 }, { 6130101, 100000005 }, { 6300005, 105070002 }, { 8130100, 105090900 }, { 9400205, 800010100 }, { 9400120, 801030000 }, { 8220001, 211040101 }, { 8180000, 240020401 }, { 8180001, 240020101 }, { 8220006, 270030500 }, { 8220005, 270020500 }, { 8220004, 270010500 }, { 3220000, 101030404 } };
//            MapleParty.通缉BOSS = 通缉[a][0];
//            MapleParty.通缉地图 = 通缉[a][1];
            System.out.println("进入世界BOSS1");
            //获取地图
            final MapleMap mapleMap = ChannelServer.getInstance(2).getMapFactory().getMap(LtMS.ConfigValuesMap.get("世界BOSS地图"));
            System.out.println("进入世界BOSS2");
            for (MapleMonster mapleMonster : mapleMap.getAllMonster()) {
                if (LtMS.ConfigValuesMap.get("世界BOSS") == mapleMonster.getId()) {
                    return;
                }
            }
            System.out.println("进入世界BOSS3");
            //刷新怪物
            //final MapleMonster mobName = MapleLifeFactory.getMonster(LtMS.ConfigValuesMap.get("世界BOSS"));
            for (int i = 0; i < LtMS.ConfigValuesMap.get("世界BOSS刷新数量"); i++) {
                mapleMap.spawnMonsterOnGroundBelow(MapleLifeFactory.getMonster(LtMS.ConfigValuesMap.get("世界BOSS")), new Point(LtMS.ConfigValuesMap.get("世界BOSSX坐标"), LtMS.ConfigValuesMap.get("世界BOSSY坐标")));
            }
            //mapleMap.spawnMonsterOnGroundBelow(MapleLifeFactory.getMonster(LtMS.ConfigValuesMap.get("世界BOSS")), new Point(LtMS.ConfigValuesMap.get("世界BOSSX坐标"), LtMS.ConfigValuesMap.get("世界BOSSY坐标")));
            //mapleMap.spawnMonsterOnGroundBelow(MapleLifeFactory.getMonster(LtMS.ConfigValuesMap.get("世界BOSS")), new Point(LtMS.ConfigValuesMap.get("世界BOSSX坐标"), LtMS.ConfigValuesMap.get("世界BOSSY坐标")));
            if (mapleMap.getAllMonster().size()>0) {
//                mobName.setPosition(new Point(LtMS.ConfigValuesMap.get("世界BOSSX坐标"), LtMS.ConfigValuesMap.get("世界BOSSY坐标")));
//                mobName.setHp(LtMS.ConfigValuesMap.get( "世界BOSS血量") * 100000000);
//                mapleMap.spawnMonster(mobName, -2);
                final String 信息 = "[世界BOSS] : 系统发布了一份通缉令，请前往2频道" + mapleMap.getMapName() + "击杀 世界BOSS,获取大量宝物";
                Broadcast.broadcastMessage(MaplePacketCreator.serverNotice(6, 信息));
                Broadcast.broadcastMessage(MaplePacketCreator.serverNotice(6, 信息));
                Broadcast.broadcastMessage(MaplePacketCreator.serverNotice(6, 信息));
                Broadcast.broadcastMessage(MaplePacketCreator.serverNotice(6, 信息));
                Broadcast.broadcastMessage(MaplePacketCreator.serverNotice(6, 信息));
                System.out.println("[服务端]" + FileoutputUtil.CurrentReadable_Time() + " : " + 信息);
                System.out.println("进入世界BOSS4");
                new Thread() {
                    @Override
                    public void run() {

                        MapleMap mapleMap1 = mapleMap;
                        try {
                            boolean flag = false;
                            Thread.sleep(5000);
                            while (true) {
                                System.out.println("进入世界BOSS循环");
                                if (mapleMap1.getAllMonster().size() == 0) {
                                    System.out.println("进入世界BOSS循环无怪");
                                    for (MapleCharacter character : mapleMap1.getCharacters()) {
                                        System.out.println("进入世界BOSS即将结束");
                                        character.getClient().getPlayer().setBossLog("参与击杀世界BOSS");
                                        flag = true;
                                    }
                                    if (flag) {
                                        return;
                                    }
                                }
                                if (new Date().getHours() == 23) {
                                    mapleMap1.killAllMonsters(true);
                                    mapleMap1.清怪();
                                    return;
                                }
                                Thread.sleep(1000);
                            }
                        } catch (Exception ex) {
                        }
                    }
                }.start();
            }
        }
    }

    public static void main(String[] args) {
        System.out.println(new Date().getMonth());
        System.out.println(new Date().getMinutes());
        System.out.println(new Date().getDay());
        System.out.println(new Date().getHours());
        System.out.println(new Date().getSeconds());
    }
}
