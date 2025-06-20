package scripting;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

import abc.Game2;
import abc.PNPC;
import gui.LtMS;

import server.Start;
import server.quest.MapleQuest;
import javax.script.Invocable;
import java.util.concurrent.locks.Lock;
import tools.FilePrinter;
import javax.script.ScriptException;
import javax.script.ScriptEngine;
import java.util.WeakHashMap;
import client.MapleClient;
import tools.FileoutputUtil;
import tools.MaplePacketCreator;

import java.util.Map;

public class NPCScriptManager extends AbstractScriptManager {
    private final Map<MapleClient, NPCConversationManager> cms = new WeakHashMap<MapleClient, NPCConversationManager>();
    private static final NPCScriptManager instance = new NPCScriptManager();

    public NPCScriptManager() {
    }

    public static final NPCScriptManager getInstance() {
        return instance;
    }

    void start(final MapleClient client, final int id, final int wh) {
        this.start(client, id, wh);
    }

    public void start(final MapleClient c, final int npc) {
        this.start(c, npc, null);
    }
    public final void startItem(MapleClient c, int npc, int mode) {
        this.start(c, npc, mode, (String)null);
    }
    public void start(final MapleClient c, final int npc, final String script) {
        this.start(c, npc, 0, script);
    }
    //脚本开始
    public void start( MapleClient c,  int npc,  int mode,  String script) {
        if(LtMS.ConfigValuesMap.get("新版本")>0) {
            final Lock lock = c.getNPCLock();
            lock.lock();
            try {
                if (c.getPlayer().isGM() && (Integer) LtMS.ConfigValuesMap.get("脚本显码开关") > 0) {
                    c.getPlayer().dropMessage("[系统提示]您已经建立与NPC:" + npc + (script == null ? "" : "(" + script + ")") + (mode == 0 ? "" : "型号: " + mode) + "的对话。");
                    PNPC itemjc2 = PNPC.getInstance();
                    String items2 = itemjc2.getPM();
                    if (npc != 2007) {
                        if (Game2.传送相关(npc) && mode == 0) {
                            c.getPlayer().dropMessage(5, "dialogue:[脚本/传送相关/" + npc + "]");
                        } else if (Game2.传送相关(npc) && mode > 0) {
                            c.getPlayer().dropMessage(5, "dialogue:[脚本/传送相关/" + npc + "," + mode + "]");
                        } else if (Game2.英语村(npc) && mode == 0) {
                            c.getPlayer().dropMessage(5, "dialogue:[脚本/副本-英语村/" + npc + "]");
                        } else if (Game2.英语村(npc) && mode > 0) {
                            c.getPlayer().dropMessage(5, "dialogue:[脚本/副本-英语村/" + npc + "," + mode + "]");
                        } else if (Game2.废弃都市(npc) && mode == 0) {
                            c.getPlayer().dropMessage(5, "dialogue:[脚本/副本-废弃都市/" + npc + "]");
                        } else if (Game2.废弃都市(npc) && mode > 0) {
                            c.getPlayer().dropMessage(5, "dialogue:[脚本/副本-废弃都市/" + npc + "," + mode + "]");
                        } else if (Game2.每日任务(npc) && mode == 0) {
                            c.getPlayer().dropMessage(5, "dialogue:[脚本/每日任务/" + npc + "]");
                        } else if (Game2.每日任务(npc) && mode > 0) {
                            c.getPlayer().dropMessage(5, "dialogue:[脚本/每日任务/" + npc + "," + mode + "]");
                        } else if (Game2.玩具塔(npc) && mode == 0) {
                            c.getPlayer().dropMessage(5, "dialogue:[脚本/副本-玩具塔/" + npc + "]");
                        } else if (Game2.玩具塔(npc) && mode > 0) {
                            c.getPlayer().dropMessage(5, "dialogue:[脚本/副本-玩具塔/" + npc + "," + mode + "]");
                        } else if (Game2.女神塔(npc) && mode == 0) {
                            c.getPlayer().dropMessage(5, "dialogue:[脚本/副本-女神塔/" + npc + "]");
                        } else if (Game2.女神塔(npc) && mode > 0) {
                            c.getPlayer().dropMessage(5, "dialogue:[脚本/副本-女神塔/" + npc + "," + mode + "]");
                        } else if (Game2.月妙(npc) && mode == 0) {
                            c.getPlayer().dropMessage(5, "dialogue:[脚本/副本-月妙/" + npc + "]");
                        } else if (Game2.月妙(npc) && mode > 0) {
                            c.getPlayer().dropMessage(5, "dialogue:[脚本/副本-月妙/" + npc + "," + mode + "]");
                        } else if (Game2.海盗船(npc) && mode == 0) {
                            c.getPlayer().dropMessage(5, "dialogue:[脚本/副本-海盗船/" + npc + "]");
                        } else if (Game2.海盗船(npc) && mode > 0) {
                            c.getPlayer().dropMessage(5, "dialogue:[脚本/副本-海盗船/" + npc + "," + mode + "]");
                        } else if (Game2.毒雾森林(npc) && mode == 0) {
                            c.getPlayer().dropMessage(5, "dialogue:[脚本/副本-毒雾森林/" + npc + "]");
                        } else if (Game2.毒雾森林(npc) && mode > 0) {
                            c.getPlayer().dropMessage(5, "dialogue:[脚本/副本-毒雾森林/" + npc + "," + mode + "]");
                        } else if (Game2.罗密欧与朱丽叶(npc) && mode == 0) {
                            c.getPlayer().dropMessage(5, "dialogue:[脚本/副本-罗密欧与朱丽叶/" + npc + "]");
                        } else if (Game2.罗密欧与朱丽叶(npc) && mode > 0) {
                            c.getPlayer().dropMessage(5, "dialogue:[脚本/副本-罗密欧与朱丽叶/" + npc + "," + mode + "]");
                        } else if (Game2.冒险百科(npc) && mode == 0) {
                            c.getPlayer().dropMessage(5, "dialogue:[脚本/冒险百科/" + npc + "]");
                        } else if (Game2.冒险百科(npc) && mode > 0) {
                            c.getPlayer().dropMessage(5, "dialogue:[脚本/冒险百科/" + npc + "," + mode + "]");
                        } else if (Game2.礼包NPC(npc) && mode == 0) {
                            c.getPlayer().dropMessage(5, "dialogue:[脚本/礼包相关/" + npc + "]");
                        } else if (Game2.礼包NPC(npc) && mode > 0) {
                            c.getPlayer().dropMessage(5, "dialogue:[脚本/礼包相关/" + npc + "," + mode + "]");
                        } else if (Game2.zevms(npc) && mode == 0) {
                            c.getPlayer().dropMessage(5, "dialogue:[脚本/zevms[加密执行文件/" + npc + "]");
                        } else if (Game2.zevms(npc) && mode > 0) {
                            c.getPlayer().dropMessage(5, "dialogue:[脚本/zevms[加密执行文件/" + npc + "," + mode + "]");
                        } else if (Game2.飞天猪(npc) && mode == 0) {
                            c.getPlayer().dropMessage(5, "dialogue:[脚本/飞天猪]/" + npc + "]");
                        } else if (Game2.飞天猪(npc) && mode > 0) {
                            c.getPlayer().dropMessage(5, "dialogue:[脚本/飞天猪]/" + npc + "," + mode + "]");
                        } else if (Game2.消耗箱子(npc) && mode == 0) {
                            c.getPlayer().dropMessage(5, "dialogue:[脚本/消耗箱子]/" + npc + "]");
                        } else if (Game2.消耗箱子(npc) && mode > 0) {
                            c.getPlayer().dropMessage(5, "dialogue:[脚本/消耗箱子]/" + npc + "," + mode + "]");
                        } else if (Game2.活动相关(npc) && mode > 0) {
                            c.getPlayer().dropMessage(5, "dialogue:[脚本/活动奖励]/" + npc + "," + mode + "]");
                        } else if (items2.contains("," + npc + ",") && mode == 0) {
                            c.getPlayer().dropMessage(5, "dialogue:[脚本/pnpc]/" + npc + "]");
                        } else if (items2.contains("," + npc + ",") && mode > 0) {
                            c.getPlayer().dropMessage(5, "dialogue:[脚本/pnpc/" + npc + " _ " + mode + "]");
                        } else if (mode == 0) {
                            if (script == null) {
                                c.getPlayer().dropMessage(5, "dialogue:[脚本/npc/" + npc + "]");
                            } else {
                                c.getPlayer().dropMessage(5, "dialogue:[脚本/special/" + script + "]");
                            }
                        } else {
                            c.getPlayer().dropMessage(5, "dialogue:[脚本/npc/" + npc + " _ " + mode + "]");
                        }
                    }
                }

                if (c.getPlayer().isGM()) {
                    c.getPlayer().dropMessage("[GM提示]您已经连接到脚本:" + npc + ((script == null) ? "" : ("(" + script + ")")) + ((mode == 0) ? "" : ("型号: " + mode)) + "的功能，如需修改此内容可前往服务端。");
                }
                if (!this.cms.containsKey((Object) c) && c.canClickNPC()) {
                    Invocable iv;
                    if (script == null) {
                        PNPC itemjc2 = PNPC.getInstance();
                        String items2 = itemjc2.getPM();
                        if (Game2.传送相关(npc) && mode == 0) {
                            iv = this.getInvocable("传送相关/" + npc + ".js", c, true);
                        } else if (Game2.传送相关(npc) && mode > 0) {
                            iv = this.getInvocable("传送相关/" + npc + "_" + mode + ".js", c, true);
                        } else if (Game2.英语村(npc) && mode == 0) {
                            iv = this.getInvocable("副本-英语村/" + npc + ".js", c, true);
                        } else if (Game2.英语村(npc) && mode > 0) {
                            iv = this.getInvocable("副本-英语村/" + npc + "_" + mode + ".js", c, true);
                        } else if (Game2.废弃都市(npc) && mode == 0) {
                            iv = this.getInvocable("副本-废弃都市/" + npc + ".js", c, true);
                        } else if (Game2.废弃都市(npc) && mode > 0) {
                            iv = this.getInvocable("副本-废弃都市/" + npc + "_" + mode + ".js", c, true);
                        } else if (Game2.玩具塔(npc) && mode == 0) {
                            iv = this.getInvocable("副本-玩具塔/" + npc + ".js", c, true);
                        } else if (Game2.玩具塔(npc) && mode > 0) {
                            iv = this.getInvocable("副本-玩具塔/" + npc + "_" + mode + ".js", c, true);
                        } else if (Game2.女神塔(npc) && mode == 0) {
                            iv = this.getInvocable("副本-女神塔/" + npc + ".js", c, true);
                        } else if (Game2.女神塔(npc) && mode > 0) {
                            iv = this.getInvocable("副本-女神塔/" + npc + "_" + mode + ".js", c, true);
                        } else if (Game2.月妙(npc) && mode == 0) {
                            iv = this.getInvocable("副本-月妙/" + npc + ".js", c, true);
                        } else if (Game2.月妙(npc) && mode > 0) {
                            iv = this.getInvocable("副本-月妙/" + npc + "_" + mode + ".js", c, true);
                        } else if (Game2.海盗船(npc) && mode == 0) {
                            iv = this.getInvocable("副本-海盗船/" + npc + ".js", c, true);
                        } else if (Game2.海盗船(npc) && mode > 0) {
                            iv = this.getInvocable("副本-海盗船/" + npc + "_" + mode + ".js", c, true);
                        } else if (Game2.毒雾森林(npc) && mode == 0) {
                            iv = this.getInvocable("副本-毒雾森林/" + npc + ".js", c, true);
                        } else if (Game2.毒雾森林(npc) && mode > 0) {
                            iv = this.getInvocable("副本-毒雾森林/" + npc + "_" + mode + ".js", c, true);
                        } else if (Game2.罗密欧与朱丽叶(npc) && mode == 0) {
                            iv = this.getInvocable("副本-罗密欧与朱丽叶/" + npc + ".js", c, true);
                        } else if (Game2.罗密欧与朱丽叶(npc) && mode > 0) {
                            iv = this.getInvocable("副本-罗密欧与朱丽叶/" + npc + "_" + mode + ".js", c, true);
                        } else if (Game2.每日任务(npc) && mode == 0) {
                            iv = this.getInvocable("每日任务/" + npc + ".js", c, true);
                        } else if (Game2.每日任务(npc) && mode > 0) {
                            iv = this.getInvocable("每日任务/" + npc + "_" + mode + ".js", c, true);
                        } else if (Game2.冒险百科(npc) && mode == 0) {
                            iv = this.getInvocable("冒险百科/" + npc + ".js", c, true);
                        } else if (Game2.冒险百科(npc) && mode > 0) {
                            iv = this.getInvocable("冒险百科/" + npc + "_" + mode + ".js", c, true);
                        } else if (Game2.礼包NPC(npc) && mode == 0) {
                            iv = this.getInvocable("礼包相关/" + npc + ".js", c, true);
                        } else if (Game2.礼包NPC(npc) && mode > 0) {
                            iv = this.getInvocable("礼包相关/" + npc + "_" + mode + ".js", c, true);
                        } else if (Game2.zevms(npc) && mode == 0) {
                            iv = this.getInvocable("zevms/" + npc + ".js", c, true);
                        } else if (Game2.zevms(npc) && mode > 0) {
                            iv = this.getInvocable("zevms/" + npc + "_" + mode + ".js", c, true);
                        } else if (Game2.飞天猪(npc) && mode == 0) {
                            iv = this.getInvocable("飞天猪/" + npc + ".js", c, true);
                        } else if (Game2.飞天猪(npc) && mode > 0) {
                            iv = this.getInvocable("飞天猪/" + npc + "_" + mode + ".js", c, true);
                        } else if (Game2.消耗箱子(npc) && mode == 0) {
                            iv = this.getInvocable("消耗箱子/" + npc + ".js", c, true);
                        } else if (Game2.消耗箱子(npc) && mode > 0) {
                            if ((double) mode >= 1000000.0 && !c.getPlayer().haveItem(mode)) {
                                c.getPlayer().dropMessage(5, "你没有该道具！");
                                this.dispose(c);
                                return;
                            }
                            iv = this.getInvocable("消耗箱子/" + npc + "_" + mode + ".js", c, true);
                        } else if (Game2.活动相关(npc) && mode > 0) {
                            iv = this.getInvocable("活动奖励/" + npc + "_" + mode + ".js", c, true);
                        } else if (items2.contains("," + npc + ",") && mode == 0) {
                            iv = this.getInvocable("pnpc/" + npc + ".js", c, true);
                        } else if (items2.contains("," + npc + ",") && mode > 0) {
                            iv = this.getInvocable("pnpc/" + npc + "_" + mode + ".js", c, true);
                        } else if (mode != 0) {
                            iv = this.getInvocable("npc/" + npc + "_" + mode + ".js", c, true);
                        } else {
                            iv = this.getInvocable("npc/" + npc + ".js", c, true);
                        }
                    } else {
                        iv = this.getInvocable("special/" + script + ".js", c, true);
                    }
                    if (iv == null) {
                        iv = this.getInvocable("special/notcoded.js", c, true);
                        if (iv == null) {
                            this.dispose(c);
                            return;
                        }
                    }
                    ScriptEngine scriptengine = (ScriptEngine) iv;
                    NPCConversationManager cm = new NPCConversationManager(c, npc, -1, mode, script, (byte) (-1), iv);
                    if (getInstance() == null) {
                        this.dispose(c);
                        return;
                    }
                    this.cms.put(c, cm);
                    scriptengine.put("cm", (Object) cm);
                    if (c.getPlayer() != null) {
                        c.getPlayer().setConversation(1);
                    }
                    c.setClickedNPC();
                    try {
                        iv.invokeFunction("start", new Object[0]);

                    } catch (NoSuchMethodException nsme) {
                        iv.invokeFunction("action", Byte.valueOf((byte) 1), Byte.valueOf((byte) 0), Integer.valueOf(0));
                    }
                } else if (c.getPlayer() != null) {
                    c.removeClickedNPC();
                    NPCScriptManager.getInstance().dispose(c);
                    c.sendPacket(MaplePacketCreator.enableActions());
                    c.getPlayer().dropMessage(5, "你现在不能攻击或不能跟npc对话,请在对话框打 @解卡/@ea 来解除异常状态==>start");
                }
            } catch (ScriptException ex) {
                if (c.getPlayer() != null && c.getPlayer().isGM()) {
                    c.getPlayer().dropMessage("[系统提示] NPC " + npc + "脚本错误 " + (Object) ex + "");
                }
                FilePrinter.printError("ScriptException.txt", "Error executing NPC script, NPC ID : " + npc + "." + (Object) ex);
            } catch (NoSuchMethodException e) {
                if (c.getPlayer() != null && c.getPlayer().isGM()) {
                    c.getPlayer().dropMessage("[系统提示] NPC " + npc + "脚本错误 " + (Object) e + "");
                }
                FilePrinter.printError("NPCScriptManager.txt", "Error executing NPC script, NPC ID : " + npc + "." + (Object) e);
                this.dispose(c);
            } finally {
                lock.unlock();
            }
        }else{
            //老版本脚本加载
            final Lock lock = c.getNPCLock();
            lock.lock();
            try {
                if (c.getPlayer().isGM()) {
                    c.getPlayer().dropMessage("[GM提示]您已经连接到脚本:" + npc + ((script == null) ? "" : ("(" + script + ")")) + ((mode == 0) ? "" : ("型号: " + mode)) + "的功能，如需修改此内容可前往服务端。");
                }
                if (!this.cms.containsKey((Object)c) && c.canClickNPC()) {

                    Invocable iv;
                    if (script == null) {
                        if (mode != 0) {
                            iv = this.getInvocable("npc/" + npc + "_" + mode + ".js", c, true);
                        }else {
                            iv = this.getInvocable("npc/" + npc + ".js", c, true);
                        }
                    }
                    else {
                        iv = this.getInvocable("special/" + script + ".js", c, true);
                    }
                    if (iv == null) {
                        iv = this.getInvocable("special/notcoded.js", c, true);
                        if (iv == null) {
                            this.dispose(c);
                            return;
                        }
                    }
                    final ScriptEngine scriptengine = (ScriptEngine)iv;
                    final NPCConversationManager cm = new NPCConversationManager(c, npc, -1, mode, script, (byte)(-1), iv);
                    if (getInstance() == null) {
                        this.dispose(c);
                        return;
                    }
                    this.cms.put(c, cm);
                    scriptengine.put("cm", (Object)cm);
                    if (c.getPlayer() != null) {
                        c.getPlayer().setConversation(1);
                    }
                    c.setClickedNPC();

                    try {
                        iv.invokeFunction("start", new Object[0]);
                    }
                    catch (NoSuchMethodException nsme) {
                        iv.invokeFunction("action", Byte.valueOf((byte)1), Byte.valueOf((byte)0), Integer.valueOf(0));
                    }
                }
                else if (c.getPlayer() != null) {
                    c.removeClickedNPC();
                    NPCScriptManager.getInstance().dispose(c);
                    c.sendPacket(MaplePacketCreator.enableActions());
                    c.getPlayer().dropMessage(5, "你现在不能攻击或不能跟npc对话,请在对话框打 @解卡/@ea 来解除异常状态");
                }
            }
            catch (ScriptException ex) {
                System.err.println("脚本错误, 它ID为 : " + npc + "." + (Object)ex);
                if (c.getPlayer() != null && c.getPlayer().isGM()) {
                    c.getPlayer().dropMessage("[系统提示] NPC " + npc + "脚本错误 " + (Object)ex + "");
                }
                FilePrinter.printError("ScriptException.txt", "Error executing NPC script, NPC ID : " + npc + "." + (Object)ex);
            }
            catch (NoSuchMethodException e) {
                System.err.println("NPC 脚本错误, 它ID为 : " + npc + "." + (Object)e);
                if (c.getPlayer() != null && c.getPlayer().isGM()) {
                    c.getPlayer().dropMessage("[系统提示] NPC " + npc + "脚本错误 " + (Object)e + "");
                }
                FilePrinter.printError("NPCScriptManager.txt", "Error executing NPC script, NPC ID : " + npc + "." + (Object)e);
                this.dispose(c);
            }
            finally {
                lock.unlock();
            }
        }
    }

    public final void action(final MapleClient c, final byte mode, final byte type, final int selection) {
        if (mode != -1) {
            final NPCConversationManager cm = (NPCConversationManager) this.cms.get((Object) c);
            if (cm == null || cm.getLastMsg() > -1) {
                return;
            }
            final Lock lock = c.getNPCLock();
            lock.lock();
            try {
                if (cm.pendingDisposal) {
                    this.dispose(c);
                } else {
                    c.setClickedNPC();
                   cm.getIv().invokeFunction("action", mode, type,selection);
                }
            } catch (ScriptException ex) {
                if (c.getPlayer() != null && c.getPlayer().isGM()) {
                    c.getPlayer().dropMessage("[系统提示] NPC " + cm.getNpc() + "脚本错误 " + (Object) ex + "");
                }
                FilePrinter.printError("ScriptException.txt", "Error executing NPC script, NPC ID : " + cm.getNpc() + "." + (Object) ex);
                this.dispose(c);
            } catch (NoSuchMethodException e) {
                if (c.getPlayer() != null && c.getPlayer().isGM()) {
                    c.getPlayer().dropMessage("[系统提示] NPC " + cm.getNpc() + "脚本错误 " + (Object) e + "");
                }
                //System.err.println("NPC 脚本错误. 它ID为 : " + cm.getNpc() + ":" + (Object)e);
                FilePrinter.printError("NPCScriptManager.txt", "Error executing NPC script, NPC ID : " + cm.getNpc() + "." + (Object) e);
                this.dispose(c);
            } finally {
                lock.unlock();
            }
        }
    }

    public void startQuest(final MapleClient c, final int npc, final int quest) {
        if (!MapleQuest.getInstance(quest).canStart(c.getPlayer(), null)) {
            return;
        }
        final Lock lock = c.getNPCLock();
        lock.lock();
        try {
            if (!this.cms.containsKey((Object) c) && c.canClickNPC()) {
                final Invocable iv = this.getInvocable("任务/" + quest + ".js", c, true);
                if (iv == null) {
                    c.getPlayer().dropMessage(1, "此任务尚未建置，请通知管理员。\r\n任务编号: " + quest);
                    this.dispose(c);
                    return;
                }
                final ScriptEngine scriptengine = (ScriptEngine) iv;
                final NPCConversationManager cm = new NPCConversationManager(c, npc, quest, 0, null, (byte) 0, iv);
                this.cms.put(c, cm);
                scriptengine.put("qm", (Object) cm);
                c.getPlayer().setConversation(1);
                c.setClickedNPC();
                if (c.getPlayer().isGM()) {
                    c.getPlayer().dropMessage("[系统提示]你已经建立起任务脚本:" + quest + "的往来。");
                }
                iv.invokeFunction("start", Byte.valueOf((byte) 1), Byte.valueOf((byte) 0), Integer.valueOf(0));
            }
        } catch (ScriptException ex) {
        } catch (NoSuchMethodException e) {
            FilePrinter.printError("NPCScriptManager.txt", "Error executing Quest script. (" + quest + ")..NPCID: " + npc + ":" + (Object) e);
            this.dispose(c);
        } finally {
            lock.unlock();
        }
    }

    public void startQuest( MapleClient c,  byte mode,  byte type,  int selection) {
         Lock lock = c.getNPCLock();
         NPCConversationManager cm = (NPCConversationManager) this.cms.get((Object) c);
        if (cm == null || cm.getLastMsg() > -1) {
            return;
        }
        lock.lock();
        try {
            if (cm.pendingDisposal) {
                this.dispose(c);
            } else {
                c.setClickedNPC();
                cm.getIv().invokeFunction("start", mode, type, selection);
            }
        } catch (ScriptException ex) {
        } catch (NoSuchMethodException e) {
            if (c.getPlayer().isGM()) {
                c.getPlayer().dropMessage("[系统提示]任务脚本:" + cm.getQuest() + "错误NPC: " + cm.getNpc() + ":" + (Object) e);
            }
            //System.err.println("Error executing Quest script. (" + cm.getQuest() + ")...NPC: " + cm.getNpc() + ":" + (Object)e);
            FilePrinter.printError("NPCScriptManager.txt", "Error executing Quest script. (" + cm.getQuest() + ")..NPCID: " + cm.getNpc() + ":" + (Object) e);
            this.dispose(c);
        } finally {
            lock.unlock();
        }
    }

    public void endQuest(final MapleClient c, final int npc, final int quest, final boolean customEnd) {
        if (!customEnd && !MapleQuest.getInstance(quest).canComplete(c.getPlayer(), null)) {
            return;
        }
        final Lock lock = c.getNPCLock();
        lock.lock();
        try {
            if (!this.cms.containsKey((Object) c) && c.canClickNPC()) {
                final Invocable iv = this.getInvocable("任务/" + quest + ".js", c, true);
                if (iv == null) {
                    this.dispose(c);
                    return;
                }
                final ScriptEngine scriptengine = (ScriptEngine) iv;
                final NPCConversationManager cm = new NPCConversationManager(c, npc, quest, 0, null, (byte) 1, iv);
                this.cms.put(c, cm);
                scriptengine.put("qm", (Object) cm);
                c.getPlayer().setConversation(1);
                c.setClickedNPC();
                iv.invokeFunction("end", Byte.valueOf((byte) 1), Byte.valueOf((byte) 0), Integer.valueOf(0));
            }
        } catch (ScriptException ex) {
        } catch (NoSuchMethodException e) {
            if (c.getPlayer().isGM()) {
                c.getPlayer().dropMessage("[系统提示]任务脚本:" + quest + "错误NPC: " + quest + ":" + (Object) e);
            }
            FilePrinter.printError("NPCScriptManager.txt", "Error executing Quest script. (" + quest + ")..NPCID: " + npc + ":" + (Object) e);
            this.dispose(c);
        } finally {
            lock.unlock();
        }
    }
    public final void cleanCMS() {
        List<MapleClient> clients = new ArrayList();
        Iterator var2 = this.cms.keySet().iterator();

        while(true) {
            MapleClient c;
            do {
                if (!var2.hasNext()) {
                    var2 = clients.iterator();

                    while(var2.hasNext()) {
                        c = (MapleClient)var2.next();
                        this.cms.remove(c);
                    }

                    return;
                }

                c = (MapleClient)var2.next();
            } while(c != null && c.getSession() != null && c.getSession().isActive());

            clients.add(c);
        }
    }
    public void endQuest(final MapleClient c, final byte mode, final byte type, final int selection) {
        final Lock lock = c.getNPCLock();
        final NPCConversationManager cm = (NPCConversationManager) this.cms.get((Object) c);
        if (cm == null || cm.getLastMsg() > -1) {
            return;
        }
        lock.lock();
        try {
            if (cm.pendingDisposal) {
                this.dispose(c);
            } else {
                c.setClickedNPC();
                cm.getIv().invokeFunction("end", mode, type, selection);
            }
        } catch (ScriptException ex) {
        } catch (NoSuchMethodException e) {
            if (c.getPlayer().isGM()) {
                c.getPlayer().dropMessage("[系统提示]任务脚本:" + cm.getQuest() + "错误NPC: " + cm.getNpc() + ":" + (Object) e);
            }
            System.err.println("Error executing Quest script. (" + cm.getQuest() + ")...NPC: " + cm.getNpc() + ":" + (Object) e);
            FilePrinter.printError("NPCScriptManager.txt", "Error executing Quest script. (" + cm.getQuest() + ")..NPCID: " + cm.getNpc() + ":" + (Object) e);
            this.dispose(c);
        } finally {
            lock.unlock();
        }
    }

    public void dispose(final MapleClient c) {
        final NPCConversationManager npccm = (NPCConversationManager) this.cms.get((Object) c);
        if(LtMS.ConfigValuesMap.get("新版本")>0) {
            if (npccm != null) {
                this.cms.remove(c);
                if (npccm.getType() == -1) {
                    PNPC itemjc2 = PNPC.getInstance();
                    String items2 = itemjc2.getPM();
                    if (Game2.传送相关(npccm.getNpc()) && npccm.getMode() == 0) {
                        c.removeScriptEngine("脚本/传送相关/" + npccm.getNpc() + ".js");
                    } else if (Game2.传送相关(npccm.getNpc()) && npccm.getMode() > 0) {
                        c.removeScriptEngine("脚本/传送相关/" + npccm.getNpc() + "_" + npccm.getMode() + ".js");
                    } else if (Game2.英语村(npccm.getNpc()) && npccm.getMode() == 0) {
                        c.removeScriptEngine("脚本/副本-英语村/" + npccm.getNpc() + ".js");
                    } else if (Game2.英语村(npccm.getNpc()) && npccm.getMode() > 0) {
                        c.removeScriptEngine("脚本/副本-英语村/" + npccm.getNpc() + "_" + npccm.getMode() + ".js");
                    } else if (Game2.废弃都市(npccm.getNpc()) && npccm.getMode() == 0) {
                        c.removeScriptEngine("脚本/副本-废弃都市/" + npccm.getNpc() + ".js");
                    } else if (Game2.废弃都市(npccm.getNpc()) && npccm.getMode() > 0) {
                        c.removeScriptEngine("脚本/副本-废弃都市/" + npccm.getNpc() + "_" + npccm.getMode() + ".js");
                    } else if (Game2.玩具塔(npccm.getNpc()) && npccm.getMode() == 0) {
                        c.removeScriptEngine("脚本/副本-玩具塔/" + npccm.getNpc() + ".js");
                    } else if (Game2.玩具塔(npccm.getNpc()) && npccm.getMode() > 0) {
                        c.removeScriptEngine("脚本/副本-玩具塔/" + npccm.getNpc() + "_" + npccm.getMode() + ".js");
                    } else if (Game2.女神塔(npccm.getNpc()) && npccm.getMode() == 0) {
                        c.removeScriptEngine("脚本/副本-女神塔/" + npccm.getNpc() + ".js");
                    } else if (Game2.女神塔(npccm.getNpc()) && npccm.getMode() > 0) {
                        c.removeScriptEngine("脚本/副本-女神塔/" + npccm.getNpc() + "_" + npccm.getMode() + ".js");
                    } else if (Game2.月妙(npccm.getNpc()) && npccm.getMode() == 0) {
                        c.removeScriptEngine("脚本/副本-月妙/" + npccm.getNpc() + ".js");
                    } else if (Game2.月妙(npccm.getNpc()) && npccm.getMode() > 0) {
                        c.removeScriptEngine("脚本/副本-月妙/" + npccm.getNpc() + "_" + npccm.getMode() + ".js");
                    } else if (Game2.海盗船(npccm.getNpc()) && npccm.getMode() == 0) {
                        c.removeScriptEngine("脚本/副本-海盗船/" + npccm.getNpc() + ".js");
                    } else if (Game2.海盗船(npccm.getNpc()) && npccm.getMode() > 0) {
                        c.removeScriptEngine("脚本/副本-海盗船/" + npccm.getNpc() + "_" + npccm.getMode() + ".js");
                    } else if (Game2.毒雾森林(npccm.getNpc()) && npccm.getMode() == 0) {
                        c.removeScriptEngine("脚本/副本-毒雾森林/" + npccm.getNpc() + ".js");
                    } else if (Game2.毒雾森林(npccm.getNpc()) && npccm.getMode() > 0) {
                        c.removeScriptEngine("脚本/副本-毒雾森林/" + npccm.getNpc() + "_" + npccm.getMode() + ".js");
                    } else if (Game2.罗密欧与朱丽叶(npccm.getNpc()) && npccm.getMode() == 0) {
                        c.removeScriptEngine("脚本/副本-罗密欧与朱丽叶/" + npccm.getNpc() + ".js");
                    } else if (Game2.罗密欧与朱丽叶(npccm.getNpc()) && npccm.getMode() > 0) {
                        c.removeScriptEngine("脚本/副本-罗密欧与朱丽叶/" + npccm.getNpc() + "_" + npccm.getMode() + ".js");
                    } else if (Game2.每日任务(npccm.getNpc()) && npccm.getMode() == 0) {
                        c.removeScriptEngine("脚本/每日任务/" + npccm.getNpc() + ".js");
                    } else if (Game2.每日任务(npccm.getNpc()) && npccm.getMode() > 0) {
                        c.removeScriptEngine("脚本/每日任务/" + npccm.getNpc() + "_" + npccm.getMode() + ".js");
                    } else if (Game2.冒险百科(npccm.getNpc()) && npccm.getMode() == 0) {
                        c.removeScriptEngine("脚本/冒险百科/" + npccm.getNpc() + ".js");
                    } else if (Game2.冒险百科(npccm.getNpc()) && npccm.getMode() > 0) {
                        c.removeScriptEngine("脚本/冒险百科/" + npccm.getNpc() + "_" + npccm.getMode() + ".js");
                    } else if (Game2.礼包NPC(npccm.getNpc()) && npccm.getMode() == 0) {
                        c.removeScriptEngine("脚本/礼包相关/" + npccm.getNpc() + ".js");
                    } else if (Game2.礼包NPC(npccm.getNpc()) && npccm.getMode() > 0) {
                        c.removeScriptEngine("脚本/礼包相关/" + npccm.getNpc() + "_" + npccm.getMode() + ".js");
                    } else if (Game2.zevms(npccm.getNpc()) && npccm.getMode() == 0) {
                        c.removeScriptEngine("脚本/zevms/" + npccm.getNpc() + ".js");
                    } else if (Game2.zevms(npccm.getNpc()) && npccm.getMode() > 0) {
                        c.removeScriptEngine("脚本/zevms/" + npccm.getNpc() + "_" + npccm.getMode() + ".js");
                    } else if (Game2.飞天猪(npccm.getNpc()) && npccm.getMode() == 0) {
                        c.removeScriptEngine("脚本/飞天猪/" + npccm.getNpc() + ".js");
                    } else if (Game2.飞天猪(npccm.getNpc()) && npccm.getMode() > 0) {
                        c.removeScriptEngine("脚本/飞天猪/" + npccm.getNpc() + "_" + npccm.getMode() + ".js");
                    } else if (Game2.消耗箱子(npccm.getNpc()) && npccm.getMode() == 0) {
                        c.removeScriptEngine("脚本/消耗箱子/" + npccm.getNpc() + ".js");
                    } else if (Game2.消耗箱子(npccm.getNpc()) && npccm.getMode() > 0) {
                        c.removeScriptEngine("脚本/消耗箱子/" + npccm.getNpc() + "_" + npccm.getMode() + ".js");
                    } else if (Game2.活动相关(npccm.getNpc()) && npccm.getMode() > 0) {
                        c.removeScriptEngine("脚本/活动奖励/" + npccm.getNpc() + "_" + npccm.getMode() + ".js");
                    } else if (items2.contains("," + npccm.getNpc() + ",") && npccm.getMode() == 0) {
                        c.removeScriptEngine("脚本/pnpc/" + npccm.getNpc() + ".js");
                    } else if (items2.contains("," + npccm.getNpc() + ",") && npccm.getMode() > 0) {
                        c.removeScriptEngine("脚本/pnpc/" + npccm.getNpc() + "_" + npccm.getMode() + ".js");
                    } else if (npccm.getMode() == 0) {
                        if (npccm.getScript() == null) {
                            c.removeScriptEngine("脚本/npc/" + npccm.getNpc() + ".js");
                        } else {
                            c.removeScriptEngine("脚本/special/" + npccm.getScript() + ".js");
                        }
                    } else {
                        c.removeScriptEngine("脚本/npc/" + npccm.getNpc() + "_" + npccm.getMode() + ".js");
                    }
                } else {
                    c.removeScriptEngine("脚本/quest/" + npccm.getQuest() + ".js");
                }
            }
        }else{
            if (npccm != null) {
                this.cms.remove((Object)c);
                if (npccm.getType() == -1) {
                    c.removeScriptEngine("脚本/npc/" + npccm.getNpc() + ".js");
                    if (npccm.getMode() != 0) {
                        c.removeScriptEngine("脚本/npc/" + npccm.getNpc() + "_" + npccm.getMode() + ".js");
                    }
                    c.removeScriptEngine("脚本/special/" + npccm.getScript() + ".js");
                    c.removeScriptEngine("脚本/special/notcoded.js");
                }
                else {
                    c.removeScriptEngine("脚本/任务/" + npccm.getQuest() + ".js");
                }
            }
        }
        if (c.getPlayer() != null && c.getPlayer().getConversation() == 1) {
            c.getPlayer().setConversation(0);
        }
    }


    public final NPCConversationManager getCM(final MapleClient c) {
        return (NPCConversationManager) this.cms.get((Object) c);
    }

    public final void onUserEnter(MapleClient c, String scriptname) {
        try {
            if (c.getPlayer().isAdmin()) {
                c.getPlayer().dropMessage(5, "[地图脚本] 执行onUserEnter脚本：" + scriptname + " 地图：" + c.getPlayer().getMap().getMapName());
            }

            Invocable iv = this.getInvocable("map/onUserEnter/" + scriptname + ".js", c, true);
            ScriptEngine scriptEngine = (ScriptEngine) iv;
            NPCConversationManager ms = new NPCConversationManager(c, c.getPlayer().getMap().getId(), -1, 0, scriptname, (byte) -1, iv);
            if (this.cms.containsValue(ms)) {
                if (c.getPlayer().isAdmin()) {
                    c.getPlayer().dropMessage(5, "无法执行脚本:已有脚本执行 - " + this.cms.containsKey(c));
                }

                this.dispose(c);
                return;
            }

            if (iv == null || getInstance() == null) {
                if (iv == null && c.getPlayer().isAdmin()) {
                    c.getPlayer().dropMessage(5, "[系统提示]:没有找到事件:" + scriptname + "脚本在map/onUserEnter里");
                }

                this.dispose(c);
                return;
            }

            this.cms.put(c, ms);
            scriptEngine.put("ms", ms);
            c.getPlayer().setConversation(1);
            c.setClickedNPC();

            try {
                iv.invokeFunction("start", new Object[0]);
            } catch (NoSuchMethodException var7) {
                iv.invokeFunction("action", new Object[]{1, 0, 0});
            }
        } catch (ScriptException | NoSuchMethodException var8) {
            FilePrinter.printError("OnUserEnter.txt", "地图脚本 : " + scriptname + ", 型态 : onUserEnter - 地图ID " + c.getPlayer().getMapId());
            System.err.println("地图脚本 : " + scriptname + ", 型态 : onUserEnter - 地图ID " + c.getPlayer().getMapId() + ":" + var8);
            if (c.getPlayer().isGM()) {
                c.getPlayer().dropMessage("[系统提示]地图脚本:" + scriptname + "型态 : onUserEnter 错误...地图ID: " + c.getPlayer().getMap().getId() + ":" + var8);
            }

            this.dispose(c);
        }

    }

    public final void onFirstUserEnter(MapleClient c, String scriptname) {
        try {
            if (c.getPlayer().isAdmin()) {
                c.getPlayer().dropMessage(5, "[地图脚本] 执行onFirstUserEnter脚本：" + scriptname + " 地图：" + c.getPlayer().getMap().getMapName());
            }

            if (this.cms.containsKey(c)) {
                if (c.getPlayer().isAdmin()) {
                    c.getPlayer().dropMessage(5, "无法执行脚本:已有脚本执行 - " + this.cms.containsKey(c));
                }

                this.dispose(c);
                return;
            }

            Invocable iv = this.getInvocable("map/onFirstUserEnter/" + scriptname + ".js", c, true);
            ScriptEngine scriptEngine = (ScriptEngine) iv;
            NPCConversationManager ms = new NPCConversationManager(c, c.getPlayer().getMap().getId(), -1, 0, scriptname, (byte) -1, iv);
            if (iv == null || getInstance() == null) {
                if (iv != null && c.getPlayer().isAdmin()) {
                    c.getPlayer().dropMessage(5, "[系统提示]:没有找到事件:" + scriptname + "脚本在map/onFirstUserEnter里");
                }

                this.dispose(c);
                return;
            }

            this.cms.put(c, ms);
            scriptEngine.put("ms", ms);
            c.getPlayer().setConversation(1);
            c.setClickedNPC();

            try {
                iv.invokeFunction("start", new Object[0]);
            } catch (NoSuchMethodException var7) {
                iv.invokeFunction("action", new Object[]{1, 0, 0});
            }
        } catch (ScriptException | NoSuchMethodException var8) {
            FilePrinter.printError("OnFirstUserEnter.txt", "地图脚本 : " + scriptname + ", 型态 : onFirstUserEnter - 地图ID " + c.getPlayer().getMapId() + var8);
            System.err.println("地图脚本 : " + scriptname + ", 型态 : OnFirstUserEnter - 地图ID " + c.getPlayer().getMapId() + ":" + var8);
            if (c.getPlayer().isGM()) {
                c.getPlayer().dropMessage("[系统提示]地图脚本:" + scriptname + "型态 : onFirstUserEnter 错误...地图ID: " + c.getPlayer().getMap().getId() + ":" + var8);
            }

            this.dispose(c);
        }

    }

//    public final void cleanCMS() {
//        List<MapleClient> clients = new ArrayList();
//        Iterator var2 = this.cms.keySet().iterator();
//
//        while (true) {
//            MapleClient c;
//            do {
//                if (!var2.hasNext()) {
//                    var2 = clients.iterator();
//
//                    while (var2.hasNext()) {
//                        c = (MapleClient) var2.next();
//                        this.cms.remove(c);
//                    }
//
//                    return;
//                }
//
//                c = (MapleClient) var2.next();
//            } while (c != null && c.getSession() != null && c.getSession().isActive());
//
//            clients.add(c);
//        }
//    }

}



