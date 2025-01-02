package server;

import client.MapleCharacter;
import client.MapleClient;
import handling.channel.ChannelServer;
import handling.world.World.Find;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import server.Timer.EtcTimer;
import server.maps.MapleMap;
import tools.MaplePacketCreator;
import tools.Pair;

public class MapleSquad {
    private WeakReference<MapleCharacter> leader;
    private final String leaderName;
    private final String toSay;
    private final Map<String, String> members = new LinkedHashMap<>();
    private final Map<String, String> bannedMembers = new LinkedHashMap<>();
    private final int ch;
    private final long startTime;
    private final int expiration;
    private final int beginMapId;
    private final MapleSquadType type;
    private byte status = 0;
    private ScheduledFuture<?> removal;
    private MapleClient c;

    public MapleSquad(int ch, String type, MapleCharacter leader, int expiration, String toSay) {
        this.leader = new WeakReference(leader);
        this.members.put(leader.getName(), MapleCarnivalChallenge.getJobNameById(leader.getJob()));
        this.leaderName = leader.getName();
        this.ch = ch;
        this.toSay = toSay;
        this.type = MapleSquad.MapleSquadType.valueOf(type.toLowerCase());
        this.status = 1;
        this.beginMapId = leader.getMapId();
        leader.getMap().setSquad(this.type);
        if (this.type.queue.get(ch) == null) {
            this.type.queue.put(ch, new ArrayList<>());
            this.type.queuedPlayers.put(ch, new ArrayList<>());
        }

        this.startTime = System.currentTimeMillis();
        this.expiration = expiration;
    }

    public void copy() {
        while(((ArrayList)this.type.queue.get(this.ch)).size() > 0 && ChannelServer.getInstance(this.ch).getMapleSquad(this.type) == null) {
            int index = 0;
            long lowest = 0L;

            for(int i = 0; i < ((ArrayList)this.type.queue.get(this.ch)).size(); ++i) {
                if (lowest == 0L || (Long)((Pair)((ArrayList)this.type.queue.get(this.ch)).get(i)).right < lowest) {
                    index = i;
                    lowest = (Long)((Pair)((ArrayList)this.type.queue.get(this.ch)).get(i)).right;
                }
            }

            String nextPlayerId = (String)((Pair)((ArrayList)this.type.queue.get(this.ch)).remove(index)).left;
            int theirCh = Find.findChannel(nextPlayerId);
            if (theirCh <= 0) {
                this.getBeginMap().broadcastMessage(MaplePacketCreator.serverNotice(6, nextPlayerId + "'远征队已经结束了，由于有成员没有在线上"));
                ((ArrayList)this.type.queuedPlayers.get(this.ch)).add(new Pair(nextPlayerId, "没有上线"));
            } else {
                MapleCharacter lead = ChannelServer.getInstance(theirCh).getPlayerStorage().getCharacterByName(nextPlayerId);
                if (lead != null && lead.getMapId() == this.beginMapId && lead.getClient().getChannel() == this.ch) {
                    MapleSquad squad = new MapleSquad(this.ch, this.type.name(), lead, this.expiration, this.toSay);
                    if (ChannelServer.getInstance(this.ch).addMapleSquad(squad, this.type.name())) {
                        this.getBeginMap().broadcastMessage(MaplePacketCreator.getClock(this.expiration / 1000));
                        this.getBeginMap().broadcastMessage(MaplePacketCreator.serverNotice(6, nextPlayerId + this.toSay));
                        ((ArrayList)this.type.queuedPlayers.get(this.ch)).add(new Pair(nextPlayerId, "成功"));
                    } else {
                        squad.clear();
                        ((ArrayList)this.type.queuedPlayers.get(this.ch)).add(new Pair(nextPlayerId, "跳过"));
                    }
                    break;
                }

                if (lead != null) {
                    lead.dropMessage(6, "远征队已经结束了，由于没有在正确的频道里。");
                }

                this.getBeginMap().broadcastMessage(MaplePacketCreator.serverNotice(6, nextPlayerId + "远征队已经结束了，由于有成员没有在地图内"));
                ((ArrayList)this.type.queuedPlayers.get(this.ch)).add(new Pair(nextPlayerId, "不在地图內"));
            }
        }

    }

    public MapleMap getBeginMap() {
        return ChannelServer.getInstance(this.ch).getMapFactory().getMap(this.beginMapId);
    }

    public void clear() {
        if (this.removal != null) {
            this.getBeginMap().broadcastMessage(MaplePacketCreator.stopClock());
            this.removal.cancel(false);
            this.removal = null;
        }

        this.members.clear();
        this.bannedMembers.clear();
        this.leader = null;
        ChannelServer.getInstance(this.ch).removeMapleSquad(this.type);
        this.status = 0;
    }

    public MapleCharacter getChar(String name) {
        return ChannelServer.getInstance(this.ch).getPlayerStorage().getCharacterByName(name);
    }

    public long getTimeLeft() {
        return (long)this.expiration - (System.currentTimeMillis() - this.startTime);
    }

    public void scheduleRemoval() {
        this.removal = EtcTimer.getInstance().schedule(new Runnable() {
            public void run() {
                if (MapleSquad.this.status != 0 && MapleSquad.this.leader != null && (MapleSquad.this.getLeader() == null || MapleSquad.this.status == 1)) {
                    MapleSquad.this.clear();
                    MapleSquad.this.copy();
                }

            }
        }, (long)this.expiration);
    }

    public String getLeaderName() {
        return this.leaderName;
    }

    public List<Pair<String, Long>> getAllNextPlayer() {
        return (List)this.type.queue.get(this.ch);
    }

    public String getNextPlayer() {
        StringBuilder sb = new StringBuilder("\n排队成员 : ");
        sb.append("#b").append(((ArrayList)this.type.queue.get(this.ch)).size()).append(" #k ").append("与远征队名单 : \n\r ");
        int i = 0;
        Iterator var3 = ((ArrayList)this.type.queue.get(this.ch)).iterator();

        while(var3.hasNext()) {
            Pair<String, Long> chr = (Pair)var3.next();
            ++i;
            sb.append(i).append(" : ").append((String)chr.left);
            sb.append(" \n\r ");
        }

        sb.append("你是否想要 #e当下一个#n 在远征队排队中　或者 #e移除#n 在远征队? 如果你想的话...");
        return sb.toString();
    }

    public void setNextPlayer(String i) {
        Pair<String, Long> toRemove = null;
        Iterator var3 = ((ArrayList)this.type.queue.get(this.ch)).iterator();

        while(var3.hasNext()) {
            Pair<String, Long> s = (Pair)var3.next();
            if (((String)s.left).equals(i)) {
                toRemove = s;
                break;
            }
        }

        if (toRemove != null) {
            ((ArrayList)this.type.queue.get(this.ch)).remove(toRemove);
        } else {
            var3 = this.type.queue.values().iterator();

            while(var3.hasNext()) {
                ArrayList<Pair<String, Long>> v = (ArrayList)var3.next();
                Iterator var5 = v.iterator();

                while(var5.hasNext()) {
                    Pair<String, Long> s = (Pair)var5.next();
                    if (((String)s.left).equals(i)) {
                        return;
                    }
                }
            }

            ((ArrayList)this.type.queue.get(this.ch)).add(new Pair(i, System.currentTimeMillis()));
        }
    }

    public MapleCharacter getLeader() {
        if (this.leader == null || this.leader.get() == null) {
            if (this.members.size() <= 0 || this.getChar(this.leaderName) == null) {
                if (this.status != 0) {
                    this.clear();
                }

                return null;
            }

            this.leader = new WeakReference(this.getChar(this.leaderName));
        }

        return (MapleCharacter)this.leader.get();
    }

    public boolean containsMember(MapleCharacter member) {
        Iterator var2 = this.members.keySet().iterator();

        String mmbr;
        do {
            if (!var2.hasNext()) {
                return false;
            }

            mmbr = (String)var2.next();
        } while(!mmbr.equalsIgnoreCase(member.getName()));

        return true;
    }

    public List<String> getMembers() {
        return new LinkedList(this.members.keySet());
    }

    public List<String> getBannedMembers() {
        return new LinkedList(this.bannedMembers.keySet());
    }

    public int getSquadSize() {
        return this.members.size();
    }

    public boolean isBanned(MapleCharacter member) {
        return this.bannedMembers.containsKey(member.getName());
    }

    public int addMember(MapleCharacter member, boolean join) {
        if (this.getLeader() == null) {
            return -1;
        } else {
            String job = MapleCarnivalChallenge.getJobNameById(member.getJob());
            if (join) {
                if (!this.containsMember(member) && !this.getAllNextPlayer().contains(member.getName())) {
                    if (this.members.size() <= 30) {
                        this.members.put(member.getName(), job);
                        this.getLeader().dropMessage(6, member.getName() + " (" + job + ") 加入了远征队!");
                        return 1;
                    } else {
                        return 2;
                    }
                } else {
                    return -1;
                }
            } else if (this.containsMember(member)) {
                this.members.remove(member.getName());
                this.getLeader().dropMessage(6, member.getName() + " (" + job + ") 离开了远征队.");
                return 1;
            } else {
                return -1;
            }
        }
    }

    public void acceptMember(int pos) {
        if (pos >= 0 && pos < this.bannedMembers.size()) {
            List<String> membersAsList = this.getBannedMembers();
            String toadd = (String)membersAsList.get(pos);
            if (toadd != null && this.getChar(toadd) != null) {
                this.members.put(toadd, this.bannedMembers.get(toadd));
                this.bannedMembers.remove(toadd);
                this.getChar(toadd).dropMessage(5, this.getLeaderName() + " 允许你重新回来远征队");
            }

        }
    }

    public void reAddMember(MapleCharacter chr) {
        this.removeMember(chr);
        this.members.put(chr.getName(), MapleCarnivalChallenge.getJobNameById(chr.getJob()));
    }

    public void removeMember(MapleCharacter chr) {
        if (this.members.containsKey(chr.getName())) {
            this.members.remove(chr.getName());
        }

    }

    public void removeMember(String chr) {
        if (this.members.containsKey(chr)) {
            this.members.remove(chr);
        }

    }

    public void banMember(int pos) {
        if (pos > 0 && pos < this.members.size()) {
            List<String> membersAsList = this.getMembers();
            String toban = (String)membersAsList.get(pos);
            if (toban != null && this.getChar(toban) != null) {
                this.bannedMembers.put(toban, this.members.get(toban));
                this.members.remove(toban);
                this.getChar(toban).dropMessage(5, this.getLeaderName() + " 从远征队中删除您.");
            }

        }
    }

    public void setStatus(byte status) {
        this.status = status;
        if (status == 2 && this.removal != null) {
            this.removal.cancel(false);
            this.removal = null;
        }

    }

    public int getStatus() {
        return this.status;
    }

    public int getBannedMemberSize() {
        return this.bannedMembers.size();
    }

    public String getSquadMemberString(byte type) {
        StringBuilder sb;
        Iterator var4;
        Map.Entry chr;
        int i;
        int selection;
        Iterator var9;
        switch (type) {
            case 0:
                sb = new StringBuilder("目前远征队总人数 : ");
                sb.append("#b").append(this.members.size()).append(" #k ").append("\r\n远征队名单 : \n\r ");
                i = 0;
                var4 = this.members.entrySet().iterator();

                for(; var4.hasNext(); sb.append(" \n\r ")) {
                    chr = (Map.Entry)var4.next();
                    ++i;
                    sb.append(i).append(" : ").append((String)chr.getKey()).append(" (").append((String)chr.getValue()).append(") ");
                    if (i == 1) {
                        sb.append("(远征队领袖)");
                    }
                }

                while(i < 30) {
                    ++i;
                    sb.append(i).append(" : ").append(" \n\r ");
                }

                return sb.toString();
            case 1:
                sb = new StringBuilder("目前远征队总人数 : ");
                sb.append("#b").append(this.members.size()).append(" #k ").append("\r\n远征队名单 : \n\r ");
                i = 0;
                selection = 0;

                for(var9 = this.members.entrySet().iterator(); var9.hasNext(); sb.append("#l").append(" \n\r ")) {
                    chr = (Map.Entry)var9.next();
                    ++i;
                    sb.append("#b#L").append(selection).append("#");
                    ++selection;
                    sb.append(i).append(" : ").append((String)chr.getKey()).append(" (").append((String)chr.getValue()).append(") ");
                    if (i == 1) {
                        sb.append("(远征队领袖)");
                    }
                }

                while(i < 30) {
                    ++i;
                    sb.append(i).append(" : ").append(" \n\r ");
                }

                return sb.toString();
            case 2:
                sb = new StringBuilder("目前远征队总人数 : ");
                sb.append("#b").append(this.members.size()).append(" #k ").append("\r\n远征队名单 : \n\r ");
                i = 0;
                selection = 0;
                var9 = this.bannedMembers.entrySet().iterator();

                while(var9.hasNext()) {
                    chr = (Map.Entry)var9.next();
                    ++i;
                    sb.append("#b#L").append(selection).append("#");
                    ++selection;
                    sb.append(i).append(" : ").append((String)chr.getKey()).append(" (").append((String)chr.getValue()).append(") ");
                    sb.append("#l").append(" \n\r ");
                }

                while(i < 30) {
                    ++i;
                    sb.append(i).append(" : ").append(" \n\r ");
                }

                return sb.toString();
            case 3:
                sb = new StringBuilder("职业 : ");
                Map<String, Integer> jobs = this.getJobs();
                var4 = jobs.entrySet().iterator();

                while(var4.hasNext()) {
                    chr = (Map.Entry)var4.next();
                    sb.append("\r\n").append((String)chr.getKey()).append(" : ").append(chr.getValue());
                }

                return sb.toString();
            default:
                return null;
        }
    }

    public final MapleSquadType getType() {
        return this.type;
    }

    public final Map<String, Integer> getJobs() {
        Map<String, Integer> jobs = new LinkedHashMap();
        Iterator var2 = this.members.entrySet().iterator();

        while(var2.hasNext()) {
            Map.Entry<String, String> chr = (Map.Entry)var2.next();
            if (jobs.containsKey(chr.getValue())) {
                jobs.put(chr.getValue(), (Integer)jobs.get(chr.getValue()) + 1);
            } else {
                jobs.put(chr.getValue(), 1);
            }
        }

        return jobs;
    }

    public static enum MapleSquadType {
        bossbalrog(2),
        zak(2),
        chaoszak(3),
        dragon(2),
        horntail(2),
        chaosht(3),
        pinkbean(3),
        nmm_squad(2),
        vergamot(2),
        dunas(2),
        nibergen_squad(2),
        dunas2(2),
        core_blaze(2),
        aufheben(2),
        cwkpq(10),
        vonleon(3),
        scartar(2),
        cygnus(3),
        blackmage(2);

        public int i;
        public HashMap<Integer, ArrayList<Pair<String, String>>> queuedPlayers = new HashMap();
        public HashMap<Integer, ArrayList<Pair<String, Long>>> queue = new HashMap();

        private MapleSquadType(int i) {
            this.i = i;
        }
    }
}
