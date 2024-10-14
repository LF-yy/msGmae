package scripting;

import java.io.InputStream;
import javax.script.*;
import java.io.IOException;

import abc.Game;
import gui.LtMS;
import gui.服务端输出信息;
import server.Start;
import tools.FilePrinter;

import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.io.Reader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.io.File;

import client.MapleClient;
import tools.MaplePacketCreator;

public abstract class AbstractScriptManager {
    private static Map<String, StringBuilder> scriptCache = new HashMap<>();
    private final static Map<Integer, Map<String, CompiledScript>> compiledCache = new HashMap<>();
    private static ScriptEngineManager sem;

    protected Invocable getInvocable(final String path, final MapleClient c) {
        return this.getInvocable(path, c, false);
    }

    protected Invocable getInvocable(String path, final MapleClient c, final boolean npc) {
        path = "脚本/" + path;
        ScriptEngine engine = null;
        if (c != null) {
            engine = c.getScriptEngine(path);
        }
        if (engine == null) {
            final File scriptFile = new File(path);
            if (!scriptFile.exists()) {
                return null;
            }
            engine = AbstractScriptManager.sem.getEngineByName("javascript");
            if (c != null) {
                c.setScriptEngine(path, engine);
            }
            InputStream in = null;
            try {
                in = new FileInputStream(scriptFile);
                final BufferedReader bf = new BufferedReader((Reader)new InputStreamReader(in, tools.EncodingDetect.getJavaEncode(scriptFile)));
                final String lines = "load('nashorn:mozilla_compat.js');" + (String)bf.lines().collect((Collector<? super String, ?, String>)Collectors.joining((CharSequence)System.lineSeparator()));
                engine.eval(lines);
            }
            catch (ScriptException ex) {}
            catch (IOException e) {
                FilePrinter.printError("AbstractScriptManager.txt", "Error executing script. Path: " + path + "\nException " + (Object)e);
                return null;
            }
            finally {
                try {
                    if (in != null) {
                        in.close();
                    }
                }
                catch (IOException ex2) {}
            }
        }
        else if (c != null && npc) {
            c.removeClickedNPC();
            NPCScriptManager.getInstance().dispose(c);
            c.getPlayer().dropMessage(5, "你现在不能攻击或不能跟npc对话,请在对话框打 @解卡 來解除异常状态");
        }
        return (Invocable)engine;
    }

    static {
        sem = new ScriptEngineManager();
    }

    public static void clearScriptCache() {
        scriptCache.clear();
        for (Map.Entry<Integer, Map<String, CompiledScript>> integerMapEntry : compiledCache.entrySet()) {
            integerMapEntry.getValue().clear();
        }
        compiledCache.clear();
        sem = new ScriptEngineManager();
    }

    public static void reloadScriptCache() {
        Map<String, StringBuilder> ret = new HashMap(scriptCache);
        ArrayList<String> pathList = new ArrayList();
        for (Map.Entry<String, StringBuilder> entry : ret.entrySet()) {
            String path = (String)entry.getKey();
            String s = null;
            InputStream fr = null;
            File scriptFile = new File(path);
            boolean changed = false;
            if (!scriptFile.exists()) {
                changed = true;
            } else {
                try {
                    StringBuilder sb = new StringBuilder();
                    boolean 进入start = false;
                    boolean 已有status = false;
                    fr = new FileInputStream(scriptFile);

                    for(BufferedReader bf = new BufferedReader(new InputStreamReader(fr, EncodingDetect.getJavaEncode(scriptFile))); (s = bf.readLine()) != null; sb.append(s + "\r\n")) {
                        if (s.contains("start()")) {
                            进入start = true;
                        }

                        if (进入start) {
                            if (s.contains("status") && !s.contains("//status")) {
                                已有status = true;
                            }

                            if (s.contains("action(1, 0, 0)") && !已有status) {
                                sb.append("    status = -1;\r\n");
                            }
                        }
                    }

                    if (!sb.toString().equals(((StringBuilder)entry.getValue()).toString())) {
                        changed = true;
                    }
                } catch (Exception var13) {
                    服务端输出信息.println_err("【错误】reloadScriptCache 重载脚本缓存失败，失败原因：" + var13);
                }
            }

            if (changed) {
                pathList.add(path);
            }
        }
        for (String path : pathList) {
            ret.remove(path);
            compiledCache.forEach((key, value) -> value.remove(path));
        }
        scriptCache.clear();
        scriptCache = ret;
        sem = new ScriptEngineManager();
    }
}
//
//    private static ScriptEngineManager sem = new ScriptEngineManager();
//    private static Map<String, StringBuilder> scriptCache = new HashMap();
//    private static Map<Integer, Map<String, CompiledScript>> compiledCache = new HashMap();
//
//    public AbstractScriptManager() {
//    }
//
//    protected Invocable getInvocable(String path, MapleClient c) {
//        return this.getInvocable(path, c, false);
//    }
//
//    protected Invocable getInvocable(String path, MapleClient c, boolean npc) {
//        InputStream fr = null;
//
//        Invocable var29;
//        try {
//            CompiledScript compiled;
//            try {
//                path = "scripts/" + path;
//                ScriptEngine engine = null;
//                compiled = null;
//                if (c != null && (Integer)LtMS.ConfigValuesMap.get("脚本缓存开关") > 0) {
//                    engine = c.getScriptEngine(path);
//                    if (!path.contains("event") && compiledCache.containsKey(c.getAccID())) {
//                        compiled = (CompiledScript)((Map)compiledCache.get(c.getAccID())).get(path);
//                    }
//                }
//
//                if (engine != null) {
//                    if (c != null && npc) {
//                        c.sendPacket(MaplePacketCreator.enableActions());
//                        NPCScriptManager.getInstance().dispose(c);
//                    }
//                } else {
//                    String s = null;
//                    StringBuilder sb = new StringBuilder();
//                    engine = sem.getEngineByName("javascript");
//                    File scriptFile;
//                    BufferedReader bf;
//                    boolean 进入start;
//                    boolean 已有status;
//                    if ((Integer)LtMS.ConfigValuesMap.get("脚本缓存开关") > 0) {
//                        if (!scriptCache.containsKey(path)) {
//                            scriptFile = new File(path);
//                            if (!scriptFile.exists()) {
//                                if (Game.调试2.equals("开")) {
//                                    服务端输出信息.println_err("【错误】未在目录" + path + "找到该脚本！");
//                                }
//
//                                c.getPlayer().dropMessage(1, "很抱歉，该NPC尚未投入使用。\r\n脚本路径：" + path);
//                                bf = null;
//                                return (Invocable) bf;
//                            }
//
//                            if (c != null) {
//                                c.setScriptEngine(path, engine);
//                            }
//
//                            fr = new FileInputStream(scriptFile);
//                            bf = new BufferedReader(new InputStreamReader(fr, scripting.EncodingDetect.getJavaEncode(scriptFile)));
//                            进入start = false;
//
//                            for(已有status = false; (s = bf.readLine()) != null; sb.append(s + "\r\n")) {
//                                if (s.contains("start()")) {
//                                    进入start = true;
//                                }
//
//                                if (进入start) {
//                                    if (s.contains("status") && !s.contains("//status")) {
//                                        已有status = true;
//                                    }
//
//                                    if (s.contains("action(1, 0, 0)") && !已有status) {
//                                        sb.append("    status = -1;\r\n");
//                                    }
//                                }
//                            }
//
//                            scriptCache.put(path, sb);
//                        } else {
//                            sb = (StringBuilder)scriptCache.get(path);
//                        }
//                    } else {
//                        scriptFile = new File(path);
//                        if (!scriptFile.exists()) {
//                            if (Game.调试2.equals("开")) {
//                                服务端输出信息.println_err("【错误】未在目录" + path + "找到该脚本！");
//                            }
//
//                            c.getPlayer().dropMessage(1, "很抱歉，该NPC尚未投入使用。\r\n脚本路径：" + path);
//                            bf = null;
//                            return (Invocable) bf;
//                        }
//
//                        fr = new FileInputStream(scriptFile);
//                        bf = new BufferedReader(new InputStreamReader(fr, scripting.EncodingDetect.getJavaEncode(scriptFile)));
//                        进入start = false;
//
//                        for(已有status = false; (s = bf.readLine()) != null; sb.append(s + "\r\n")) {
//                            if (s.contains("start()")) {
//                                进入start = true;
//                            }
//
//                            if (进入start) {
//                                if (s.contains("status") && !s.contains("//status")) {
//                                    已有status = true;
//                                }
//
//                                if (s.contains("action(1, 0, 0)") && !已有status) {
//                                    sb.append("    status = -1;\r\n");
//                                }
//                            }
//                        }
//                    }
//
//                    s = sb.toString();
//                    if (s.indexOf("{") == -1) {
//                        s = Class1.getInstance().decrypt(s);
//                        if (c != null && c.getPlayer() != null && c.getPlayer().isGM() && (Integer)LtMS.ConfigValuesMap.get("主要调试开关") > 0) {
//                            c.getPlayer().dropMessage(6, "[GM调试] NPC对话延时测试getInvocable，step9时刻：" + System.currentTimeMillis());
//                            服务端输出信息.println_out("[GM调试] NPC对话延时测试getInvocable，step9时刻：" + System.currentTimeMillis());
//                        }
//
//                        StringBuilder s1 = new StringBuilder();
//                        s1.append("load(\"nashorn:mozilla_compat.js\");\r\n");
//                        s1.append(s);
//                        s = s1.toString();
//                    } else {
//                        s = "load(\"nashorn:mozilla_compat.js\");\r\n" + s;
//                    }
//
//                    try {
//                        if ((Integer) LtMS.ConfigValuesMap.get("脚本缓存开关") > 0 && !path.contains("event")) {
//                            if (compiled == null) {
//                                compiled = ((Compilable)engine).compile(s);
//                                if (compiledCache.containsKey(c.getAccID())) {
//                                    ((Map)compiledCache.get(c.getAccID())).put(path, compiled);
//                                } else {
//                                    Map<String, CompiledScript> compiledMap = new HashMap();
//                                    compiledMap.put(path, compiled);
//                                    compiledCache.put(c.getAccID(), compiledMap);
//                                }
//                            }
//
//                            compiled.eval();
//                        } else {
//                            engine.eval(s);
//                        }
//                    } catch (ScriptException | NullPointerException var25) {
//                        服务端输出信息.println_err("【错误】脚本执行错误. Path（路径）: " + path + "\nScriptException（错误内容）： " + var25);
//                    }
//                }
//
//                if ((Integer)LtMS.ConfigValuesMap.get("脚本缓存开关") <= 0 || compiled == null) {
//                    var29 = (Invocable)engine;
//                    return var29;
//                }
//
//                var29 = (Invocable)compiled.getEngine();
//            } catch (Exception var26) {
//                服务端输出信息.println_err("【错误】脚本执行错误. Path（路径）: " + path + "\nException（错误内容）： " + var26);
//                compiled = null;
//                return (Invocable) compiled;
//            }
//        } finally {
//            try {
//                if (fr != null) {
//                    fr.close();
//                }
//            } catch (IOException var24) {
//            }
//
//        }
//
//        return var29;
//    }
//
//    public static void clearScriptCache() {
//        scriptCache.clear();
//        Iterator var0 = compiledCache.entrySet().iterator();
//
//        while(var0.hasNext()) {
//            Map.Entry<Integer, Map<String, CompiledScript>> entry = (Map.Entry)var0.next();
//            ((Map)entry.getValue()).clear();
//        }
//
//        compiledCache.clear();
//        sem = new ScriptEngineManager();
//    }
//
//    public static void reloadScriptCache() {
//        Map<String, StringBuilder> ret = new HashMap(scriptCache);
//        ArrayList<String> pathList = new ArrayList();
//        Iterator var2 = ret.entrySet().iterator();
//
//        while(var2.hasNext()) {
//            Map.Entry<String, StringBuilder> entry = (Map.Entry)var2.next();
//            String path = (String)entry.getKey();
//            String s = null;
//            InputStream fr = null;
//            File scriptFile = new File(path);
//            boolean changed = false;
//            if (!scriptFile.exists()) {
//                changed = true;
//            } else {
//                try {
//                    StringBuilder sb = new StringBuilder();
//                    boolean 进入start = false;
//                    boolean 已有status = false;
//                    fr = new FileInputStream(scriptFile);
//
//                    for(BufferedReader bf = new BufferedReader(new InputStreamReader(fr, EncodingDetect.getJavaEncode(scriptFile))); (s = bf.readLine()) != null; sb.append(s + "\r\n")) {
//                        if (s.contains("start()")) {
//                            进入start = true;
//                        }
//
//                        if (进入start) {
//                            if (s.contains("status") && !s.contains("//status")) {
//                                已有status = true;
//                            }
//
//                            if (s.contains("action(1, 0, 0)") && !已有status) {
//                                sb.append("    status = -1;\r\n");
//                            }
//                        }
//                    }
//
//                    if (!sb.toString().equals(((StringBuilder)entry.getValue()).toString())) {
//                        changed = true;
//                    }
//                } catch (Exception var13) {
//                    服务端输出信息.println_err("【错误】reloadScriptCache 重载脚本缓存失败，失败原因：" + var13);
//                }
//            }
//
//            if (changed) {
//                pathList.add(path);
//            }
//        }
//
//        var2 = pathList.iterator();
//
//        while(var2.hasNext()) {
//            String path = (String)var2.next();
//            ret.remove(path);
//            Iterator var15 = compiledCache.entrySet().iterator();
//
//            while(var15.hasNext()) {
//                Map.Entry<Integer, Map<String, CompiledScript>> entry1 = (Map.Entry)var15.next();
//                ((Map)entry1.getValue()).remove(path);
//            }
//        }
//
//        scriptCache.clear();
//        scriptCache = ret;
//        sem = new ScriptEngineManager();
//    }
//
//}
