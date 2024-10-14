package server.events;

public enum MapleEventType
{
    打椰子比赛("椰子比赛", new int[]{109080000}),
    打瓶盖比赛("打瓶盖", new int[]{109080010}),
    向高地比赛("向高地", new int[]{109040000, 109040001, 109040002, 109040003, 109040004}),
    上楼上楼("上楼上楼", new int[]{109030001, 109030002, 109030003}),
    OX答题比赛("答题", new int[]{109020001}),
    推雪球比赛("雪球赛", new int[]{109060000}),
    家族对抗赛("家族对抗赛", new int[]{229000000, 229000010, 229000300, 229000310, 229000030, 229000020, 229000100, 229000200, 229000220, 229000210, 229000211, 229000040, 229000311}),
    家族野外BOSS赛("家族野外BOSS赛", new int[]{209000001, 209000002, 209000003, 209000004, 209000005, 209000006, 209000007, 209000008, 209000009, 209000010, 209000011, 209000012, 209000013, 209000014, 209000015}),
    怪物攻城("怪物攻城", new int[]{100000000, 102000000, 103000000, 101000000, 104000000, 120000000}),
    寻宝("寻宝", new int[]{109010000, 109010100, 109010102, 109010103, 109010104, 109010105, 109010106, 109010107, 109010108, 109010109, 109010110, 109010200, 109010201, 109010202, 109010203, 109010204, 109010205, 109010206});


    public String command;
    public int[] mapids;

    private MapleEventType(String comm, int[] mapids) {
        this.command = comm;
        this.mapids = mapids;
    }

    public static final MapleEventType getByString(String splitted) {
        MapleEventType[] var1 = values();
        int var2 = var1.length;

        for(int var3 = 0; var3 < var2; ++var3) {
            MapleEventType t = var1[var3];
            if (t.command.equalsIgnoreCase(splitted)) {
                return t;
            }
        }

        return null;
    }
//    public String command;
//    public int[] mapids;
//
//    private MapleEventType(final String comm, final int[] mapids) {
//        this.command = comm;
//        this.mapids = mapids;
//    }
//
//    public static MapleEventType getByString(final String splitted) {
//        for (final MapleEventType t : values()) {
//            if (t.command.equalsIgnoreCase(splitted)) {
//                return t;
//            }
//        }
//        return null;
//    }
}
