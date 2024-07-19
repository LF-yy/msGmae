package client;


import bean.UserAttraction;
import bean.UserLhAttraction;
import gui.LtMS;
import handling.channel.ChannelServer;
import scripting.NPCConversationManager;
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
        try {
            Random rand = new Random();
            int ID = c.getPlayer().getId();
            final MapleMap mapleMap = ChannelServer.getInstance(c.getChannel()).getMapFactory().getMap(c.getPlayer().getMapId());
            List<MapleMonster> mapleMonsters = NPCConversationManager.轮回怪物.get(ID);
            while (!Thread.interrupted()) {
                if(c.getPlayer()!=null){
                    if (!c.isLoggedIn()){
                        NPCConversationManager.gain关闭轮回(ID);
                        map.killAllMonsters(true);

                        break;
                    }
                    if (c.getPlayer().getMapId() != object.getMapId()){
                        NPCConversationManager.gain关闭轮回(ID);
                        map.killAllMonsters(true);
                        break;
                    }
                }else{
                    NPCConversationManager.gain关闭轮回(ID);
                    map.killAllMonsters(true);
                    break;
                }
                if (ListUtil.isEmpty(mapleMonsters)){
                    NPCConversationManager.gain关闭轮回(ID);
                    break;
                }
                //召唤怪物
                if (mapleMonsters.size() > 0 && c.getPlayer().getMap().getAllMonster().size()< (Objects.nonNull(LtMS.ConfigValuesMap.get("轮回判定怪物數量")) ? LtMS.ConfigValuesMap.get("轮回判定怪物數量") : 150)) {
                    for (MapleMonster mapleMonster : mapleMonsters) {
                        mapleMap.spawnMonsterOnGroundBelow(MapleLifeFactory.getMonster(mapleMonster.getId()), new Point(c.getPlayer().getPosition().x+rand.nextInt(300), c.getPlayer().getPosition().y));
                    }
                }
                this.wait(1000L);
            }
        }
        catch (InterruptedException e) {
            e.printStackTrace();
            System.err.println("[轮回]未知錯誤" + (Object)e);
        }
        c.getPlayer().dropMessage(5, "轮回已关闭");
    }
}
