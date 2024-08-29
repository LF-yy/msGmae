package bean;

import java.awt.*;

public class UserLhAttraction {
    /**
     * 所在频道
     */
    private int pinDao;
    /**
     * 地图ID
     */
    private int mapId;
    /**
     * 吸怪定点位置
     */
    private Point position;
    /**
     * 吸怪定点位置
     */
    private int mapMobCount;

    public UserLhAttraction(int pinDao, int mapId, Point position) {
        this.pinDao = pinDao;
        this.mapId = mapId;
        this.position = position;
    }

    public int getPinDao() {
        return pinDao;
    }

    public void setPinDao(int pinDao) {
        this.pinDao = pinDao;
    }

    public int getMapId() {
        return mapId;
    }

    public void setMapId(int mapId) {
        this.mapId = mapId;
    }

    public Point getPosition() {
        return position;
    }

    public void setPosition(Point position) {
        this.position = position;
    }

    public int getMapMobCount() {
        return mapMobCount;
    }

    public void setMapMobCount(int mapMobCount) {
        this.mapMobCount = mapMobCount;
    }
}
