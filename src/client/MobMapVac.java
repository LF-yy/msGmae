package client;


import bean.UserAttraction;
import handling.channel.ChannelServer;
import handling.world.MaplePartyCharacter;
import handling.world.World;
import scripting.NPCConversationManager;
import server.life.MapleMonster;
import server.maps.MapleMap;
import tools.MaplePacketCreator;

import java.util.List;
import java.util.Objects;

public class MobMapVac extends Thread{
    private UserAttraction object;
    private MapleMap map;
    private MapleCharacter chr;
    private final MapleClient c;
    private final int mobId;
    private final String mobName;
    private final String adress;
    private final String value;
    private final long hp;

    public MobMapVac(MapleCharacter chr, UserAttraction userAttraction,int mobId,String mobName,String adress,String value, long hp) {
        this.chr = chr;
        this.mobName = mobName;
        this.adress = adress;
        this.value = value;
        this.mobId = mobId;
        this.hp = hp;
        this.c = chr.getClient();
        this.map = this.c.getPlayer().getMap();
        this.object = userAttraction;
    }


    @Override
    public  void run() {
        try {
            long l = System.currentTimeMillis();
            long time = 0L;
            int ID = c.getPlayer().getMapId();
            while (!Thread.interrupted()) {
                if (map.getCharacters().size() == 0) {
                    map.killAllMonsters(true);
                    break;
                }

                Thread.sleep(1000);
                if (chr!=null && chr.getClient()!=null && chr.getClient().getPlayer() !=null ){
                    if (Objects.isNull(map.getMonsterById(mobId))) {
                        //System.out.println("检测到怪物被击杀");
                        //杀死怪物的时间毫秒
                        boolean flag = false;
                        time = System.currentTimeMillis() - l;
                        if (Objects.nonNull(chr.getClient().getPlayer().getParty()) && chr.getClient().getPlayer().getParty().getMembers().size() >= 4) {
                            // System.out.println("检测到怪物被击杀");
                            for (final MaplePartyCharacter cc : chr.getClient().getPlayer().getParty().getMembers()) {
                                if (ID == cc.getMapid()) {
                                    //统计击杀时间
                                    // System.out.println("组队统计击杀结果");
                                    NPCConversationManager.setBossLog统计用(cc.getId(), "参与"+mobName, 1, 1);

                                            long l1 = time / 1000L;
                                            long l2 = hp/ l1;
                                            String shanghai;
                                            if(l2>100000000){
                                                shanghai  = Math.ceil(l2/100000000L) + "亿";
                                            }else if(l2>10000){
                                                shanghai  = Math.ceil(l2/10000L) + "万";
                                            }else{
                                                shanghai  = Math.ceil(l2) +"";
                                            }
                                            World.Broadcast.broadcastMessage(MaplePacketCreator.serverNotice(0, adress + ": " + cc.getName() + " " + value + mobName + ",用时" + (time / 1000) + "秒==>秒伤:"+shanghai));

                                }
                            }
                            break;
                        }
                        if (ID == c.getPlayer().getMapId()) {
                            //统计击杀时间
                            NPCConversationManager.setBossLog统计用(c.getPlayer().getId(), "参与"+mobName, 1, 1);

                                    long l1 = time / 1000L;
                                    long l2 = hp/ l1;
                                    String shanghai;
                                    if(l2>100000000){
                                        shanghai  = Math.ceil(l2/100000000L) + "亿";
                                    }else if(l2>10000){
                                        shanghai  = Math.ceil(l2/10000L) + "万";
                                    }else{
                                        shanghai  = Math.ceil(l2) +"";
                                    }
                                    World.Broadcast.broadcastMessage(MaplePacketCreator.serverNotice(0, adress + ": " + c.getPlayer().getName() + " " + value + mobName + ",用时" + (time / 1000) + "秒==>秒伤:"+shanghai));
                                    break;
                        }
                    }
                }else{
                 break;
                }
            }
        }
        catch (Exception e) {
        }finally {
            NPCConversationManager.gain关闭BOSS击杀统计(c.getPlayer());
        }
    }

    public static void main(String[] args) {
        long time = 100000L;
        long hp = 567897654345678L;
        long l1 = time / 1000L;
        long l2 = hp/ l1;
        String 伤害;
        if(l2>=100000000L){
            伤害  = Math.ceil(l2/100000000L) + "亿";
        }else if(l2>=10000L){
            伤害  = Math.ceil(l2/10000L) + "万";
        }else{
            伤害  = Math.ceil(l2) +"";
        }
        System.out.println(伤害);
    }
}
