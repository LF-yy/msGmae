//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package tools.packet;

import snail.GuiPlayerEntity;
import tools.data.MaplePacketLittleEndianWriter;

import java.util.Iterator;
import java.util.List;

public class GuiPacketCreator {
    public GuiPacketCreator() {
    }

    public static byte[] ShowGui(boolean isOpen) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(2007);
        mplew.write(isOpen ? 1 : 0);
        return mplew.getPacket();
    }

    public static byte[] ShowPlayer(List<GuiPlayerEntity> items) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(2008);
        mplew.writeShort(items.size());
        if (items.size() > 0) {
            Iterator var2 = items.iterator();

            while(var2.hasNext()) {
                GuiPlayerEntity entity = (GuiPlayerEntity)var2.next();
                mplew.writeInt(entity.getId());
                mplew.writeMapleAsciiString(entity.getName());
                mplew.writeMapleAsciiString(entity.getText());
            }
        }

        return mplew.getPacket();
    }

    public static byte[] UpdateDps(int playerId, String text) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(2009);
        mplew.writeInt(playerId);
        mplew.writeMapleAsciiString(text);
        return mplew.getPacket();
    }

    public static byte[] ClearDps() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(2010);
        return mplew.getPacket();
    }
}
