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
       int userId = c.getPlayer().getId();
        final MapleMap mapleMap = ChannelServer.getInstance(c.getChannel()).getMapFactory().getMap(c.getPlayer().getMapId());
        try {
           int indexs =  0;
            Random rand = new Random();
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

                //召唤怪物
                boolean b = mapleMap.getAllMonster().stream().anyMatch(mapleMonster -> mapleMonster.getId() == 9300329);
                if (!b && indexs == 0 ){
                    mapleMap.spawnMonsterOnGroundBelow(MapleLifeFactory.getMonster(9300329), new Point(c.getPlayer().getPosition().x, c.getPlayer().getPosition().y));
                    indexs++;
                }
                if ( mapleMap.getAllMonster().size()< object.getMapMobCount()+LtMS.ConfigValuesMap.get("轮回判定怪物數量")) {
                    int index = 0;
                    Thread.sleep(LtMS.ConfigValuesMap.get("轮回频率"));
                    int i = rand.nextInt(200);
                    for (MapleMonster mapleMonster : mapleMap.getAllMonster()) {
                        if (mapleMap.getAllMonster().size() >= object.getMapMobCount()+LtMS.ConfigValuesMap.get("轮回判定怪物數量")){
                            break;
                        }
                        if (mapleMonster.getId() == 9300329){
                            continue;
                        }
                        if (mapleMonster.getStats().isBoss()){
                            continue;
                        }
                        if (LtMS.ConfigValuesMap.get("轮回判定怪物數量") <=index){
                            break;
                        }
                        index++;
                        mapleMap.spawnMonsterOnGroundBelow(MapleLifeFactory.getMonster(mapleMonster.getId()), new Point(i>100 ? object.getPosition().x + i : c.getPlayer().getPosition().x - i, object.getPosition().y));
                    }
                }
                this.wait(2000L);
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
