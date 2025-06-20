package provider;

import java.awt.Point;
import java.awt.image.BufferedImage;
import provider.WzXML.MapleDataType;

public class MapleDataTool
{
    public static String getString(final MapleData data) {
        return (String)data.getData();
    }
    
    public static String getString(final MapleData data, final String def) {
        if (data == null || data.getData() == null) {
            return def;
        }
        if (data.getType() == MapleDataType.STRING || data.getData() instanceof String) {
            return (String)data.getData();
        }
        return String.valueOf(getInt(data));
    }
    
    public static String getString(final String path, final MapleData data) {
        return getString(data.getChildByPath(path));
    }
    
    public static String getString(final String path, final MapleData data, final String def) {
        return getString((data == null || data.getChildByPath(path) == null) ? null : data.getChildByPath(path), def);
    }
    
    public static double getDouble(final MapleData data) {
        return (double)(Double)data.getData();
    }
    
    public static float getFloat(final MapleData data) {
        return (float)(Float)data.getData();
    }
    
    public static float getFloat(final MapleData data, final float def) {
        if (data == null || data.getData() == null) {
            return def;
        }
        return (float)(Float)data.getData();
    }
    
    public static int getInt(final MapleData data) {
        if (data.getType() == MapleDataType.STRING) {
            return Integer.parseInt(getString(data));
        }
        return (int)(Integer)data.getData();
    }
    public static long getLong(final MapleData data) {
        if (data.getType() == MapleDataType.STRING) {
            return Long.parseLong(getString(data));
        }
        return (long)(Long)data.getData();
    }
    public static int getInt(final MapleData data, final int def) {
        if (data == null || data.getData() == null) {
            return def;
        }
        if (data.getType() == MapleDataType.STRING) {
            return Integer.parseInt(getString(data));
        }
        if (data.getType() == MapleDataType.LONG) {
            return Integer.parseInt(getString(data));
        }
        if (data.getType() == MapleDataType.SHORT) {
            return Integer.valueOf((Short)data.getData());
        }
        if (data.getType() == MapleDataType.DOUBLE) {
            return (int) Math.abs(Double.parseDouble(String.valueOf(data.getData())));
        }
        return (int)(Integer)data.getData();
    }
    
    public static int getInt(final String path, final MapleData data) {
        return getInt(data.getChildByPath(path));
    }
    
    public static int getIntConvert(final MapleData data) {
        if (data.getType() == MapleDataType.STRING) {
            return Integer.parseInt(getString(data));
        }
        return getInt(data);
    }
    
    public static int getIntConvert(final String path, final MapleData data) {
        final MapleData d = data.getChildByPath(path);
        if (d.getType() == MapleDataType.STRING) {
            return Integer.parseInt(getString(d));
        }
        return getInt(d);
    }
    public static long getLongConvert(final String path, final MapleData data) {
        final MapleData d = data.getChildByPath(path);
        if (d.getType() == MapleDataType.STRING) {
            return Long.parseLong(getString(d));
        }
        return getLong(d);
    }
    public static int getInt(final String path, final MapleData data, final int def) {
        return getInt(data.getChildByPath(path), def);
    }
    
    public static int getIntConvert(final String path, final MapleData data, final int def) {
        if (data == null) {
            return def;
        }
        return getIntConvert(data.getChildByPath(path), def);
    }
    
    public static int getIntConvert(final MapleData d, final int def) {
        if (d == null) {
            return def;
        }
        if (d.getType() == MapleDataType.STRING) {
            String dd = getString(d);
            if (dd.endsWith("%")) {
                dd = dd.substring(0, dd.length() - 1);
            }
            try {
                return Integer.parseInt(dd);
            }
            catch (NumberFormatException nfe) {
                return def;
            }
        }
        return getInt(d, def);
    }
    
    public static BufferedImage getImage(final MapleData data) {
        return ((MapleCanvas)data.getData()).getImage();
    }
    
    public static Point getPoint(final MapleData data) {
        return (Point)data.getData();
    }
    
    public static Point getPoint(final String path, final MapleData data) {
        return getPoint(data.getChildByPath(path));
    }
    
    public static Point getPoint(final String path, final MapleData data, final Point def) {
        final MapleData pointData = data.getChildByPath(path);
        if (pointData == null) {
            return def;
        }
        return getPoint(pointData);
    }
    
    public static String getFullDataPath(final MapleData data) {
        String path = "";
        for (MapleDataEntity myData = data; myData != null; myData = myData.getParent()) {
            path = myData.getName() + "/" + path;
        }
        return path.substring(0, path.length() - 1);
    }
}
