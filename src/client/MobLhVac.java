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
import java.util.*;
import java.util.List;

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
    public void run() {
        int rateByStone = 0;
        long lastTime = 0;
        List<Integer> mobList = new ArrayList<>();
        int userId = c.getPlayer().getId();
        final MapleMap mapleMap = ChannelServer.getInstance(c.getChannel()).getMapFactory().getMap(c.getPlayer().getMapId());

        try {
            while (!Thread.interrupted()) {
                if (!checkPlayerStatus(c, mapleMap)) {
                    Thread.currentThread().interrupt();
                    return;
                }

                if (chr.getMap().getStoneLevel() == 1 && rateByStone == 0 && lastTime == 0) {
                    rateByStone = (Integer) LtMS.ConfigValuesMap.get("1级轮回碑石怪物倍数");
                    lastTime = LtMS.ConfigValuesMap.get("1级轮回频率");
                } else if (chr.getMap().getStoneLevel() >= 2 && rateByStone == 0 && lastTime == 0) {
                    rateByStone = (Integer) LtMS.ConfigValuesMap.get("2级轮回碑石怪物倍数");
                    lastTime =  LtMS.ConfigValuesMap.get("2级轮回频率");
                } else if (rateByStone == 0 && lastTime == 0) {
                    rateByStone = (Integer) LtMS.ConfigValuesMap.get("轮回碑石怪物倍数");
                    lastTime = LtMS.ConfigValuesMap.get("轮回频率");
                }
                Thread.sleep(lastTime);
                spawnMonstersIfNecessary(mapleMap, rateByStone, mobList);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            //log.error("Thread interrupted", e);
        } catch (Exception e) {
          //  log.error("Unexpected error occurred", e);
        } finally {
            closeCycle(c, mapleMap, userId);
        }
    }

    private boolean checkPlayerStatus(MapleClient c, MapleMap mapleMap) {
        MapleCharacter player = c.getPlayer();
        if (player == null || !c.isLoggedIn() || player.getMapId() != object.getMapId()) {
            NPCConversationManager.gain关闭轮回(player, mapleMap);
            return false;
        }
        return true;
    }

    private void spawnMonstersIfNecessary(MapleMap mapleMap, int rateByStone, List<Integer> mobList) {
        if (mapleMap.getAllMonstersThreadsafe().size() < (rateByStone)) {
            List<MapleMonster> monsters = new ArrayList<>(mapleMap.getAllMonstersThreadsafe());
            int index = 0;

            if (mobList.size() < rateByStone) {
                for (int i = 0; i < 300 && mobList.size() < rateByStone; i++) {
                    for (MapleMonster mapleMonster : monsters) {
                        if (shouldSkipMonster(mapleMonster)) {
                            continue;
                        }
                        if (rateByStone <= index) {
                            break;
                        }
                        mobList.add(mapleMonster.getId());
                        spawnMonster(mapleMap, mapleMonster, object.getPosition());
                        index++;
                    }
                }
            } else {
                for (Integer integer : mobList) {
                    if (shouldSkipMonster(integer)) {
                        continue;
                    }
                    if (rateByStone <= index) {
                        break;
                    }
                    spawnMonster(mapleMap, integer, object.getPosition());
                    index++;
                }
            }
        }
    }

    private boolean shouldSkipMonster(MapleMonster mapleMonster) {
        int id = mapleMonster.getId();
        return id == 9900000 || id == 9900001 || id == 9900002 || mapleMonster.getStats().isBoss();
    }

    private boolean shouldSkipMonster(Integer integer) {
        return integer == 9900000 || integer == 9900001 || integer == 9900002;
    }

    private void spawnMonster(MapleMap mapleMap, MapleMonster mapleMonster, Point position) {
        mapleMap.spawnMonsterOnGroundBelow(MapleLifeFactory.getMonster(mapleMonster.getId()), position);
    }

    private void spawnMonster(MapleMap mapleMap, Integer integer, Point position) {
        mapleMap.spawnMonsterOnGroundBelow(MapleLifeFactory.getMonster(integer), position);
    }

    private void closeCycle(MapleClient c, MapleMap mapleMap, int userId) {
        try {
            NPCConversationManager.gain关闭轮回(c.getPlayer(), mapleMap);
            c.getPlayer().dropMessage(5, "轮回已关闭");
        } catch (Exception e) {
            Start.轮回集合.remove(userId);
        }
    }
}
