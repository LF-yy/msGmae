package client.inventory;

public interface IItem extends Comparable<IItem>
{
    byte getType();
    
    short getPosition();
    
    byte getFlag();
    String getDaKongFuMo();
    short getQuantity();
    
    String getOwner();
    
    String getGMLog();
    
    int getItemId();
    
    void setItemId(final int p0);
    String getPotentials();
    void setPotentials(String var1);
    MaplePet getPet();
    
    int getUniqueId();
    
    boolean hasSetOnlyId();
    
    long getEquipOnlyId();
    
    long getInventoryId();
    
    IItem copy();
    
    IItem copyWithQuantity(final short p0);
    
    long getExpiration();
    
    void setFlag(final byte p0);
    
    void setUniqueId(final int p0);
    
    void setEquipOnlyId(final long p0);
    
    void setInventoryId(final long p0);
    void setUUID( String p0);
    String getUUID();

    void setPosition(final short p0);
    
    void setExpiration(final long p0);
    
    void setOwner(final String p0);
    
    void setGMLog(final String p0);
    
    void setQuantity(final short p0);
    
    void setGiftFrom(final String p0);
    
    String getGiftFrom();
    
    MapleRing getRing();
    void setDaKongFuMo(String var1);

    int getPrice();
}
