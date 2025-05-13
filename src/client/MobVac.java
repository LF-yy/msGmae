package client;


import bean.UserAttraction;
import scripting.NPCConversationManager;
import server.Start;
import server.maps.MapleMap;

public class MobVac  extends Thread{
    private UserAttraction object;
    private MapleMap map;
    private MapleCharacter chr;
    private final MapleClient c;
    public  int userId;
    public  int mapId;
    public  int channel;

    public MobVac(MapleCharacter chr,UserAttraction userAttraction) {
        this.chr = chr;
        this.c = chr.getClient();
        this.map = this.c.getPlayer().getMap();
        this.object = userAttraction;
        this.userId =  c.getPlayer().getId();
    }


    @Override
    public  void run() {
        try {
            mapId = object.getMapId();
            int ID = c.getPlayer().getId();
            channel = c.getChannel();
            while (!Thread.interrupted()) {
                if(c.getPlayer()!=null){
                    if (!c.isLoggedIn()){
                        NPCConversationManager.gain关闭吸怪(ID);
                        map.killAllMonsters(true);
                        Start.吸怪角色.remove(channel+"-"+map.getId());
                        Thread.currentThread().interrupt();
                        return;
                    }
                    if (c.getPlayer().getMapId() != mapId){
                        NPCConversationManager.gain关闭吸怪(ID);
                        map.killAllMonsters(true);
                        Start.吸怪角色.remove(channel+"-"+map.getId());
                        Thread.currentThread().interrupt();
                        return;
                    }
                }else{
                    NPCConversationManager.gain关闭吸怪(ID);
                    map.killAllMonsters(true);
                    Start.吸怪角色.remove(channel+"-"+map.getId());
                    Thread.currentThread().interrupt();
                    return;
                }
                Thread.sleep(1000L);
            }
        }
        catch (Exception e) {
        }
    }
}
