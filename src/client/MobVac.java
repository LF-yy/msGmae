package client;


import bean.UserAttraction;
import scripting.NPCConversationManager;
import server.maps.MapleMap;

public class MobVac  extends Thread{
    private UserAttraction object;
    private MapleMap map;
    private final MapleCharacter chr;
    private final MapleClient c;

    public MobVac(final MapleCharacter chr,UserAttraction userAttraction) {
        this.chr = chr;
        this.c = chr.getClient();
        this.map = this.c.getPlayer().getMap();
        this.object = userAttraction;
    }


    @Override
    public synchronized void run() {
        try {
            int ID = c.getPlayer().getId();
            while (!Thread.interrupted()) {
                if(c.getPlayer()!=null){
                    if (!c.isLoggedIn()){
                        NPCConversationManager.gain关闭吸怪(ID);
                        map.killAllMonsters(true);

                        break;
                    }
                    if (c.getPlayer().getMapId() != object.getMapId()){
                        NPCConversationManager.gain关闭吸怪(ID);
                        map.killAllMonsters(true);
                        break;
                    }
                }else{
                    NPCConversationManager.gain关闭吸怪(ID);
                    map.killAllMonsters(true);
                    break;
                }

                this.wait(1000L);
            }
        }
        catch (InterruptedException e) {
            e.printStackTrace();
            System.err.println("[ItemVac]未知錯誤" + (Object)e);
        }
    }
}
