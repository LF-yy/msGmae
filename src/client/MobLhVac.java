package client;


import bean.UserAttraction;
import bean.UserLhAttraction;
import gui.LtMS;
import handling.channel.ChannelServer;
import scripting.NPCConversationManager;
import server.Start;
import server.life.MapleLifeFactory;
import server.life.MapleMonster;
import server.maps.MapleMap;
import util.ListUtil;

import java.awt.*;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class MobLhVac extends Thread{
    private UserLhAttraction object;
    private MapleMap map;
    private MapleCharacter chr;
    private final MapleClient c;

    public MobLhVac(MapleCharacter chr, UserLhAttraction userAttraction) {
        this.chr = chr;
        this.c = chr.getClient();
        this.map = this.c.getPlayer().getMap();
        this.object = userAttraction;
    }


    @Override
    public synchronized void run() {
        int rateByStone = 0;
        long lastTime = 0 ;
       int userId = c.getPlayer().getId();
        final MapleMap mapleMap = ChannelServer.getInstance(c.getChannel()).getMapFactory().getMap(c.getPlayer().getMapId());
        try {
            while (!Thread.interrupted()) {
                if(c.getPlayer()!=null){
                    if (!c.isLoggedIn()){
                        NPCConversationManager.gain关闭轮回(c.getPlayer(),mapleMap);
                        break;
                    }
                    if (c.getPlayer().getMapId() != object.getMapId()){
                        NPCConversationManager.gain关闭轮回(c.getPlayer(),mapleMap);
                        break;
                    }
                }else{
                    NPCConversationManager.gain关闭轮回(c.getPlayer(),mapleMap);
                    break;
                }

                if (chr.getMap().getStoneLevel() == 1 && rateByStone ==0 && lastTime == 0) {
                    rateByStone = (Integer)LtMS.ConfigValuesMap.get("1级轮回碑石怪物倍数");
                    lastTime = LtMS.ConfigValuesMap.get("1级轮回频率");
                } else if (chr.getMap().getStoneLevel() >= 2 && rateByStone ==0 && lastTime == 0) {
                    rateByStone = (Integer)LtMS.ConfigValuesMap.get("2级轮回碑石怪物倍数");
                    lastTime = LtMS.ConfigValuesMap.get("2级轮回频率");
                } else if ( rateByStone ==0 && lastTime == 0){
                    rateByStone = (Integer)LtMS.ConfigValuesMap.get("轮回碑石怪物倍数");
                    lastTime = LtMS.ConfigValuesMap.get("轮回频率");
                }
                Thread.sleep(lastTime);
                if ( mapleMap.getAllMonstersThreadsafe().size()< (rateByStone/2)) {
                    int index = 0;
                    for (int i = 0; i < 30; i++) {
                        for (MapleMonster mapleMonster : mapleMap.getAllMonstersThreadsafe()) {
                            if (9900000 == mapleMonster.getId() || 9900001 == mapleMonster.getId() || 9900002 == mapleMonster.getId()) {
                                continue;
                            }
                            if (mapleMonster.getStats().isBoss()){
                                continue;
                            }
                            if (rateByStone<=index){
                                break;
                            }
                            index++;
                            mapleMap.spawnMonsterOnGroundBelow(MapleLifeFactory.getMonster(mapleMonster.getId()), new Point( object.getPosition().x, object.getPosition().y));
                        }
                    }
                }
            }
        }
        catch (Exception e) {

        }finally {
            try {
                NPCConversationManager.gain关闭轮回(c.getPlayer(),mapleMap);
                c.getPlayer().dropMessage(5, "轮回已关闭");
            } catch (Exception e) {
                Start.轮回集合.remove(userId);
            }
        }
    }

}
