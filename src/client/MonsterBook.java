//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package client;

import constants.GameConstants;
import database.DBConPool;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import gui.LtMS;
import server.MapleItemInformationProvider;
import server.Start;
import snail.MonsterCardStats;
import tools.FileoutputUtil;
import tools.MaplePacketCreator;
import tools.data.MaplePacketLittleEndianWriter;
import tools.packet.MonsterBookPacket;

public class MonsterBook implements Serializable {
    private static final long serialVersionUID = 7179541993413738569L;
    private boolean changed = false;
    private int SpecialCard = 0;
    private int NormalCard = 0;
    private int BookLevel = 1;
    private final Map<Integer, Integer> cards;

    public MonsterBook(Map<Integer, Integer> cards) {
        this.cards = cards;
        Iterator var2 = cards.entrySet().iterator();

        while(var2.hasNext()) {
            Map.Entry<Integer, Integer> card = (Map.Entry)var2.next();
            if (GameConstants.isSpecialCard((Integer)card.getKey())) {
                this.SpecialCard += (Integer)card.getValue();
            } else {
                this.NormalCard += (Integer)card.getValue();
            }
        }

        this.calculateLevel();
    }

    public Map<Integer, Integer> getCards() {
        return this.cards;
    }

    public final int getTotalCards() {
        return this.SpecialCard + this.NormalCard;
    }

    public final int getLevelByCard(int cardid) {
        return this.cards.get(cardid) == null ? 0 : (Integer)this.cards.get(cardid);
    }

    public static final MonsterBook loadCards(int charid) throws SQLException {
        try {
            Connection con = DBConPool.getInstance().getDataSource().getConnection();
            Throwable var3 = null;

            Object rs;
            try {
                PreparedStatement ps = con.prepareStatement("SELECT * FROM monsterbook WHERE charid = ? ORDER BY cardid ASC");
                Throwable var5 = null;

                try {
                    ps.setInt(1, charid);
                    rs = ps.executeQuery();
                    Throwable var7 = null;

                    LinkedHashMap cards;
                    try {
                        cards = new LinkedHashMap();

                        while(((ResultSet)rs).next()) {
                            cards.put(((ResultSet)rs).getInt("cardid"), ((ResultSet)rs).getInt("level"));
                        }
                    } catch (Throwable var55) {
                        var7 = var55;
                        throw var55;
                    } finally {
                        if (rs != null) {
                            if (var7 != null) {
                                try {
                                    ((ResultSet)rs).close();
                                } catch (Throwable var54) {
                                    var7.addSuppressed(var54);
                                }
                            } else {
                                ((ResultSet)rs).close();
                            }
                        }

                    }

                    rs = new MonsterBook(cards);
                } catch (Throwable var57) {
                    rs = var57;
                    var5 = var57;
                    throw var57;
                } finally {
                    if (ps != null) {
                        if (var5 != null) {
                            try {
                                ps.close();
                            } catch (Throwable var53) {
                                var5.addSuppressed(var53);
                            }
                        } else {
                            ps.close();
                        }
                    }

                }
            } catch (Throwable var59) {
                var3 = var59;
                throw var59;
            } finally {
                if (con != null) {
                    if (var3 != null) {
                        try {
                            con.close();
                        } catch (Throwable var52) {
                            var3.addSuppressed(var52);
                        }
                    } else {
                        con.close();
                    }
                }

            }

            return (MonsterBook)rs;
        } catch (SQLException var61) {
            FileoutputUtil.outError("logs/资料库异常.txt", var61);
            return null;
        }
    }

    public final void saveCards(int charid) throws SQLException {
        if (this.changed && this.cards.size() != 0) {
            Connection con = DBConPool.getConnection();
            PreparedStatement ps = con.prepareStatement("DELETE FROM monsterbook WHERE charid = ?");
            ps.setInt(1, charid);
            ps.execute();
            ps.close();
            boolean first = true;
            StringBuilder query = new StringBuilder();
            Iterator var6 = this.cards.entrySet().iterator();

            while(var6.hasNext()) {
                Map.Entry<Integer, Integer> all = (Map.Entry)var6.next();
                if (first) {
                    first = false;
                    query.append("INSERT INTO monsterbook VALUES (DEFAULT,");
                } else {
                    query.append(",(DEFAULT,");
                }

                query.append(charid);
                query.append(",");
                query.append(all.getKey());
                query.append(",");
                query.append(all.getValue());
                query.append(")");
            }

            ps = con.prepareStatement(query.toString());
            ps.execute();
            ps.close();
        }
    }

    public final void saveCards(int charid, Connection con) throws SQLException {
        if (this.changed && !this.cards.isEmpty()) {
            try {
                PreparedStatement ps = con.prepareStatement("DELETE FROM monsterbook WHERE charid = ?");
                ps.setInt(1, charid);
                ps.execute();
                ps.close();
                boolean first = true;
                StringBuilder query = new StringBuilder();
                Iterator var6 = this.cards.entrySet().iterator();

                while(var6.hasNext()) {
                    Map.Entry<Integer, Integer> all = (Map.Entry)var6.next();
                    if (first) {
                        first = false;
                        query.append("INSERT INTO monsterbook VALUES (DEFAULT,");
                    } else {
                        query.append(",(DEFAULT,");
                    }

                    query.append(charid);
                    query.append(",");
                    query.append(all.getKey());
                    query.append(",");
                    query.append(all.getValue());
                    query.append(")");
                }

                ps = con.prepareStatement(query.toString());
                ps.execute();
                ps.close();
            } catch (Exception var8) {
                FileoutputUtil.outError("logs/资料库异常.txt", var8);
            }

        }
    }

    private void calculateLevel() {
        int Size = this.NormalCard + this.SpecialCard;
        this.BookLevel = 8;

        for(int i = 0; i < 8; ++i) {
            if (Size <= GameConstants.getBookLevel(i)) {
                this.BookLevel = i + 1;
                break;
            }
        }

    }

    public final void addCardPacket(MaplePacketLittleEndianWriter mplew) {
        mplew.writeShort(this.cards.size());
        Iterator var2 = this.cards.entrySet().iterator();

        while(var2.hasNext()) {
            Map.Entry<Integer, Integer> all = (Map.Entry)var2.next();
            mplew.writeShort(GameConstants.getCardShortId((Integer)all.getKey()));
            mplew.write((Integer)all.getValue());
        }

    }

    public final void addCharInfoPacket(int bookcover, MaplePacketLittleEndianWriter mplew) {
        mplew.writeInt(this.BookLevel);
        mplew.writeInt(this.NormalCard);
        mplew.writeInt(this.SpecialCard);
        mplew.writeInt(this.NormalCard + this.SpecialCard);
        mplew.writeInt(MapleItemInformationProvider.getInstance().getCardMobId(bookcover));
    }

    public final void updateCard(MapleClient c, int cardid) {
        c.sendPacket(MonsterBookPacket.changeCover(cardid));
    }

    public final void addCard(MapleClient c, int cardid) {
        this.changed = true;
        c.getPlayer().getMap().broadcastMessage(c.getPlayer(), MonsterBookPacket.showForeginCardEffect(c.getPlayer().getId()), false);
        if (this.cards.containsKey(cardid)) {
            int levels = (Integer)this.cards.get(cardid);
            if (levels >= 5) {
                c.sendPacket(MonsterBookPacket.addCard(true, cardid, levels));
            } else {
                if (GameConstants.isSpecialCard(cardid)) {
                    ++this.SpecialCard;
                } else {
                    ++this.NormalCard;
                }

                if ((Integer)LtMS.ConfigValuesMap.get("怪物卡片附加属性开关") > 0) {
                    MonsterCardStats.MonsterCard monsterCard = MonsterCardStats.getInstance().getMonsterCard(cardid);
                    if (monsterCard != null) {
                        String text = "使用怪物卡片后增加人物属性：";
                        int a;
                        if (monsterCard.getStr() > 0) {
                            a = c.getPlayer().getStr() + monsterCard.getStr();
                            if (a > 32767) {
                                a = 32767;
                            }

                            c.getPlayer().setStr(a);
                            c.getPlayer().updateSingleStat(MapleStat.STR, a);
                            text = text + "力量+" + monsterCard.getStr() + " ";
                        }

                        if (monsterCard.getDex() > 0) {
                            a = c.getPlayer().getDex() + monsterCard.getDex();
                            if (a > 32767) {
                                a = 32767;
                            }

                            c.getPlayer().setDex(a);
                            c.getPlayer().updateSingleStat(MapleStat.DEX, a);
                            text = text + "敏捷+" + monsterCard.getDex() + " ";
                        }

                        if (monsterCard.getInt() > 0) {
                            a = c.getPlayer().getInt() + monsterCard.getInt();
                            if (a > 32767) {
                                a = 32767;
                            }

                            c.getPlayer().setInt(a);
                            c.getPlayer().updateSingleStat(MapleStat.INT, a);
                            text = text + "智力+" + monsterCard.getInt() + " ";
                        }

                        if (monsterCard.getLuk() > 0) {
                            a = c.getPlayer().getLuk() + monsterCard.getLuk();
                            if (a > 32767) {
                                a = 32767;
                            }

                            c.getPlayer().setLuk(a);
                            c.getPlayer().updateSingleStat(MapleStat.LUK, a);
                            text = text + "运气+" + monsterCard.getLuk() + " ";
                        }

                        if (monsterCard.getHp() > 0) {
                            a = c.getPlayer().getMaxHp() + monsterCard.getHp();
                            if (a > 32767) {
                                a = 32767;
                            }

                            c.getPlayer().setMaxHp(a);
                            c.getPlayer().updateSingleStat(MapleStat.MAXHP, a);
                            text = text + "maxHp+" + monsterCard.getHp() + " ";
                        }

                        if (monsterCard.getMp() > 0) {
                            a = c.getPlayer().getMaxMp() + monsterCard.getMp();
                            if (a > 32767) {
                                a = 32767;
                            }

                            c.getPlayer().setMaxMp(a);
                            c.getPlayer().updateSingleStat(MapleStat.MAXMP, a);
                            text = text + "maxMp+" + monsterCard.getMp() + " ";
                        }

                        if (!text.equals("使用怪物卡片后增加人物属性：")) {
                            c.getPlayer().dropMessage(5, text);
                        }
                    }
                }

                c.sendPacket(MonsterBookPacket.addCard(false, cardid, levels + 1));
                c.sendPacket(MonsterBookPacket.showGainCard(cardid));
                c.sendPacket(MaplePacketCreator.showSpecialEffect(15));
                this.cards.put(cardid, levels + 1);
                this.calculateLevel();
            }

        } else {
            if (GameConstants.isSpecialCard(cardid)) {
                ++this.SpecialCard;
            } else {
                ++this.NormalCard;
            }

            if ((Integer) LtMS.ConfigValuesMap.get("怪物卡片附加属性开关") > 0) {
                MonsterCardStats.MonsterCard monsterCard = MonsterCardStats.getInstance().getMonsterCard(cardid);
                if (monsterCard != null) {
                    String text = "使用怪物卡片后增加人物属性：";
                    int a;
                    if (monsterCard.getStr() > 0) {
                        a = c.getPlayer().getStr() + monsterCard.getStr();
                        if (a > 32767) {
                            a = 32767;
                        }

                        c.getPlayer().setStr(a);
                        c.getPlayer().updateSingleStat(MapleStat.STR, a);
                        text = text + "力量+" + monsterCard.getStr() + " ";
                    }

                    if (monsterCard.getDex() > 0) {
                        a = c.getPlayer().getDex() + monsterCard.getDex();
                        if (a > 32767) {
                            a = 32767;
                        }

                        c.getPlayer().setDex(a);
                        c.getPlayer().updateSingleStat(MapleStat.DEX, a);
                        text = text + "敏捷+" + monsterCard.getDex() + " ";
                    }

                    if (monsterCard.getInt() > 0) {
                        a = c.getPlayer().getInt() + monsterCard.getInt();
                        if (a > 32767) {
                            a = 32767;
                        }

                        c.getPlayer().setInt(a);
                        c.getPlayer().updateSingleStat(MapleStat.INT, a);
                        text = text + "智力+" + monsterCard.getInt() + " ";
                    }

                    if (monsterCard.getLuk() > 0) {
                        a = c.getPlayer().getLuk() + monsterCard.getLuk();
                        if (a > 32767) {
                            a = 32767;
                        }

                        c.getPlayer().setLuk(a);
                        c.getPlayer().updateSingleStat(MapleStat.LUK, a);
                        text = text + "运气+" + monsterCard.getLuk() + " ";
                    }

                    if (monsterCard.getHp() > 0) {
                        a = c.getPlayer().getMaxHp() + monsterCard.getHp();
                        if (a > 32767) {
                            a = 32767;
                        }

                        c.getPlayer().setMaxHp(a);
                        c.getPlayer().updateSingleStat(MapleStat.MAXHP, a);
                        text = text + "maxHp+" + monsterCard.getHp() + " ";
                    }

                    if (monsterCard.getMp() > 0) {
                        a = c.getPlayer().getMaxMp() + monsterCard.getMp();
                        if (a > 32767) {
                            a = 32767;
                        }

                        c.getPlayer().setMaxMp(a);
                        c.getPlayer().updateSingleStat(MapleStat.MAXMP, a);
                        text = text + "maxMp+" + monsterCard.getMp() + " ";
                    }

                    if (!text.equals("使用怪物卡片后增加人物属性：")) {
                        c.getPlayer().dropMessage(5, text);
                    }
                }
            }

            this.cards.put(cardid, 1);
            c.sendPacket(MonsterBookPacket.addCard(false, cardid, 1));
            c.sendPacket(MonsterBookPacket.showGainCard(cardid));
            c.sendPacket(MaplePacketCreator.showSpecialEffect(15));
            this.calculateLevel();
        }
    }

    public final void addCard(MapleClient c, int cardid, int quantity) {
        if (quantity > 5) {
            quantity = 5;
        } else if (quantity < 1) {
            quantity = 1;
        }

        this.changed = true;
        c.getPlayer().getMap().broadcastMessage(c.getPlayer(), MonsterBookPacket.showForeginCardEffect(c.getPlayer().getId()), false);
        if (this.cards.containsKey(cardid)) {
            int levels = (Integer)this.cards.get(cardid);
            if (levels >= 5) {
                c.sendPacket(MonsterBookPacket.addCard(true, cardid, levels));
            } else {
                if (levels + quantity > 5) {
                    quantity = 5 - levels;
                }

                if (GameConstants.isSpecialCard(cardid)) {
                    this.SpecialCard += quantity;
                } else {
                    this.NormalCard += quantity;
                }

                c.sendPacket(MonsterBookPacket.addCard(false, cardid, levels));
                c.sendPacket(MonsterBookPacket.showGainCard(cardid));
                c.sendPacket(MaplePacketCreator.showSpecialEffect(15));
                this.cards.put(cardid, levels + quantity);
                this.calculateLevel();
            }

        } else {
            if (GameConstants.isSpecialCard(cardid)) {
                this.SpecialCard += quantity;
            } else {
                this.NormalCard += quantity;
            }

            this.cards.put(cardid, quantity);
            c.sendPacket(MonsterBookPacket.addCard(false, cardid, quantity));
            c.sendPacket(MonsterBookPacket.showGainCard(cardid));
            c.sendPacket(MaplePacketCreator.showSpecialEffect(15));
            this.calculateLevel();
        }
    }

    public int getFinishQuantity(short stage, int minLevel) {
        if (this.cards.isEmpty()) {
            return 0;
        } else {
            int count = 0;
            Iterator var4 = this.cards.entrySet().iterator();

            while(var4.hasNext()) {
                Map.Entry<Integer, Integer> entry = (Map.Entry)var4.next();
                switch (stage) {
                    case 0:
                        if ((Integer)entry.getValue() >= minLevel) {
                            ++count;
                        }
                        break;
                    case 1:
                        if ((Integer)entry.getKey() >= 2380000 && (Integer)entry.getKey() < 2381000 && (Integer)entry.getValue() >= minLevel) {
                            ++count;
                        }
                        break;
                    case 2:
                        if ((Integer)entry.getKey() >= 2381000 && (Integer)entry.getKey() < 2382000 && (Integer)entry.getValue() >= minLevel) {
                            ++count;
                        }
                        break;
                    case 3:
                        if ((Integer)entry.getKey() >= 2382000 && (Integer)entry.getKey() < 2383000 && (Integer)entry.getValue() >= minLevel) {
                            ++count;
                        }
                        break;
                    case 4:
                        if ((Integer)entry.getKey() >= 2383000 && (Integer)entry.getKey() < 2384000 && (Integer)entry.getValue() >= minLevel) {
                            ++count;
                        }
                        break;
                    case 5:
                        if ((Integer)entry.getKey() >= 2384000 && (Integer)entry.getKey() < 2385000 && (Integer)entry.getValue() >= minLevel) {
                            ++count;
                        }
                        break;
                    case 6:
                        if ((Integer)entry.getKey() >= 2385000 && (Integer)entry.getKey() < 2386000 && (Integer)entry.getValue() >= minLevel) {
                            ++count;
                        }
                        break;
                    case 7:
                        if ((Integer)entry.getKey() >= 2386000 && (Integer)entry.getKey() < 2387000 && (Integer)entry.getValue() >= minLevel) {
                            ++count;
                        }
                        break;
                    case 8:
                        if ((Integer)entry.getKey() >= 2387000 && (Integer)entry.getKey() < 2388000 && (Integer)entry.getValue() >= minLevel) {
                            ++count;
                        }
                        break;
                    default:
                        if ((Integer)entry.getKey() >= 2388000 && (Integer)entry.getKey() < 2389000 && (Integer)entry.getValue() >= minLevel) {
                            ++count;
                        }
                }
            }

            return count;
        }
    }

    public boolean isFinished(short stage, int minLevel) {
        switch (stage) {
            case 0:
                return this.isFinished((short)1, minLevel) && this.isFinished((short)2, minLevel) && this.isFinished((short)3, minLevel) && this.isFinished((short)4, minLevel) && this.isFinished((short)5, minLevel) && this.isFinished((short)6, minLevel) && this.isFinished((short)7, minLevel) && this.isFinished((short)8, minLevel) && this.isFinished((short)9, minLevel);
            case 1:
                return this.getFinishQuantity(stage, minLevel) >= 20;
            case 2:
                return this.getFinishQuantity(stage, minLevel) >= 74;
            case 3:
                return this.getFinishQuantity(stage, minLevel) >= 86;
            case 4:
                return this.getFinishQuantity(stage, minLevel) >= 58;
            case 5:
                return this.getFinishQuantity(stage, minLevel) >= 44;
            case 6:
                return this.getFinishQuantity(stage, minLevel) >= 27;
            case 7:
                return this.getFinishQuantity(stage, minLevel) >= 29;
            case 8:
                return this.getFinishQuantity(stage, minLevel) >= 20;
            default:
                return this.getFinishQuantity(stage, minLevel) >= 53;
        }
    }

    public void setCard(MapleClient c, int cardId, int level) {
        this.setCard(c, cardId, level, true);
    }

    public void setCard(MapleClient c, int cardId, int level, boolean showEffect) {
        if (level < 0) {
            level = 0;
        } else if (level > 5) {
            level = 5;
        }

        this.changed = true;
        if (showEffect) {
            c.getPlayer().getMap().broadcastMessage(c.getPlayer(), MonsterBookPacket.showForeginCardEffect(c.getPlayer().getId()), false);
        }

        if (this.cards.containsKey(cardId)) {
            int levels = (Integer)this.cards.get(cardId);
            if (GameConstants.isSpecialCard(cardId)) {
                this.SpecialCard += level - levels;
            } else {
                this.NormalCard += level - levels;
            }

            c.sendPacket(MonsterBookPacket.addCard(false, cardId, level));
            if (showEffect) {
                c.sendPacket(MonsterBookPacket.showGainCard(cardId));
                c.sendPacket(MaplePacketCreator.showSpecialEffect(15));
            }

            this.cards.put(cardId, level);
            this.calculateLevel();
        } else {
            if (GameConstants.isSpecialCard(cardId)) {
                this.SpecialCard += level;
            } else {
                this.NormalCard += level;
            }

            this.cards.put(cardId, level);
            c.sendPacket(MonsterBookPacket.addCard(false, cardId, level));
            if (showEffect) {
                c.sendPacket(MonsterBookPacket.showGainCard(cardId));
                c.sendPacket(MaplePacketCreator.showSpecialEffect(15));
            }

            this.calculateLevel();
        }
    }

    public void setCardsByStage(MapleClient c, short stage, int level) {
        int i;
        switch (stage) {
            case 0:
                for(i = 2380000; i < 2389000; ++i) {
                    this.setCard(c, i, level, false);
                }

                c.sendPacket(MonsterBookPacket.showGainCard(2380000));
                break;
            case 1:
                for(i = 2380000; i < 2381000; ++i) {
                    this.setCard(c, i, level, false);
                }

                c.sendPacket(MonsterBookPacket.showGainCard(2380000));
                break;
            case 2:
                for(i = 2381000; i < 2382000; ++i) {
                    this.setCard(c, i, level, false);
                }

                c.sendPacket(MonsterBookPacket.showGainCard(2381000));
                break;
            case 3:
                for(i = 2382000; i < 2383000; ++i) {
                    this.setCard(c, i, level, false);
                }

                c.sendPacket(MonsterBookPacket.showGainCard(2382000));
                break;
            case 4:
                for(i = 2383000; i < 2384000; ++i) {
                    this.setCard(c, i, level, false);
                }

                c.sendPacket(MonsterBookPacket.showGainCard(2383000));
                break;
            case 5:
                for(i = 2384000; i < 2385000; ++i) {
                    this.setCard(c, i, level, false);
                }

                c.sendPacket(MonsterBookPacket.showGainCard(2384000));
                break;
            case 6:
                for(i = 2385000; i < 2386000; ++i) {
                    this.setCard(c, i, level, false);
                }

                c.sendPacket(MonsterBookPacket.showGainCard(2385000));
                break;
            case 7:
                for(i = 2386000; i < 2387000; ++i) {
                    this.setCard(c, i, level, false);
                }

                c.sendPacket(MonsterBookPacket.showGainCard(2386000));
                break;
            case 8:
                for(i = 2387000; i < 2388000; ++i) {
                    this.setCard(c, i, level, false);
                }

                c.sendPacket(MonsterBookPacket.showGainCard(2387000));
                break;
            default:
                for(i = 2388000; i < 2389000; ++i) {
                    this.setCard(c, i, level, false);
                }

                c.sendPacket(MonsterBookPacket.showGainCard(2388000));
        }

        c.getPlayer().getMap().broadcastMessage(c.getPlayer(), MonsterBookPacket.showForeginCardEffect(c.getPlayer().getId()), false);
        c.sendPacket(MaplePacketCreator.showSpecialEffect(15));
    }
}
