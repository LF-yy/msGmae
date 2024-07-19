package server;

import client.MapleClient;
import java.awt.Point;

public interface MaplePortal
{
    public static int MAP_PORTAL = 2;
    public static int DOOR_PORTAL = 6;
    
    int getType();
    
    int getId();
    
    Point getPosition();
    
    String getName();
    
    String getTarget();
    
    String getScriptName();
    
    void setScriptName(final String p0);
    
    int getTargetMapId();
    
    void enterPortal(final MapleClient p0);
    
    void setPortalState(final boolean p0);
    
    boolean getPortalState();
}
