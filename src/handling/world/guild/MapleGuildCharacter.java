package handling.world.guild;

import client.MapleCharacter;
import java.io.Serializable;

public class MapleGuildCharacter implements Serializable
{
    public static long serialVersionUID = 2058609046116597760L;
    private byte channel;
    private byte guildrank;
    private byte allianceRank;
    private short level;
    private final int id;
    private int jobid;
    private int guildid;
    private  String name;
    private boolean online;

    public MapleGuildCharacter(final MapleCharacter c) {
        this.channel = -1;
        this.name = c.getName();
        this.level = c.getLevel();
        this.id = c.getId();
        this.channel = (byte)c.getClient().getChannel();
        this.jobid = c.getJob();
        this.guildrank = c.getGuildRank();
        this.guildid = c.getGuildId();
        this.allianceRank = c.getAllianceRank();
        this.online = true;
    }
    
    public MapleGuildCharacter(final int id, final short lv, final String name, final byte channel, final int job, final byte rank, final byte allianceRank, final int gid, final boolean on) {
        this.channel = -1;
        this.level = lv;
        this.id = id;
        this.name = name;
        if (on) {
            this.channel = channel;
        }
        this.jobid = job;
        this.online = on;
        this.guildrank = rank;
        this.allianceRank = allianceRank;
        this.guildid = gid;
    }
    
    public int getLevel() {
        return this.level;
    }
    
    public void setLevel(final short l) {
        this.level = l;
    }
    
    public int getId() {
        return this.id;
    }
    
    public void setChannel(final byte ch) {
        this.channel = ch;
    }
    
    public int getChannel() {
        return this.channel;
    }
    
    public int getJobId() {
        return this.jobid;
    }
    
    public void setJobId(final int job) {
        this.jobid = job;
    }
    
    public int getGuildId() {
        return this.guildid;
    }
    
    public void setGuildId(final int gid) {
        this.guildid = gid;
    }
    
    public void setGuildRank(final byte rank) {
        this.guildrank = rank;
    }
    
    public byte getGuildRank() {
        return this.guildrank;
    }
    
    public boolean isOnline() {
        return this.online;
    }
    
    public String getName() {
        return this.name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public byte getGuildrank() {
        return guildrank;
    }

    public int getJobid() {
        return jobid;
    }

    public int getGuildid() {
        return guildid;
    }

    public void setGuildrank(byte guildrank) {
        this.guildrank = guildrank;
    }

    public void setJobid(int jobid) {
        this.jobid = jobid;
    }

    public void setGuildid(int guildid) {
        this.guildid = guildid;
    }

    public void setOnline(final boolean f) {
        this.online = f;
    }
    
    public void setAllianceRank(final byte rank) {
        this.allianceRank = rank;
    }
    
    public byte getAllianceRank() {
        return this.allianceRank;
    }
}
