package scripting;

import java.io.InputStream;
import javax.script.*;
import java.io.IOException;

import gui.LtMS;
import gui.服务端输出信息;
import server.Start;
import tools.FilePrinter;

import java.nio.file.Files;
import java.util.*;
import java.io.Reader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.io.File;
import java.util.concurrent.ConcurrentHashMap;

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
        CompiledScript compiled = null;
        if (c != null && (Integer)LtMS.ConfigValuesMap.get("脚本缓存开关") > 0 ) {
            engine = c.getScriptEngine(path);
            if (!path.contains("事件") && compiledCache.containsKey(c.getAccID())) {
                 compiled = (CompiledScript)((Map)compiledCache.get(c.getAccID())).get(path);
            }
        }
        if (engine == null) {
            if ((Integer)LtMS.ConfigValuesMap.get("脚本缓存开关") > 0) {
                //System.out.println("脚本缓存"+scriptCache.containsKey(path));
                if (scriptCache.containsKey(path)) {
                    engine = AbstractScriptManager.sem.getEngineByName("javascript");
//                    engine = AbstractScriptManager.sem.getEngineByName("javascript");
                    try {
                        if ((Integer)LtMS.ConfigValuesMap.get("脚本缓存开关") > 0 && !path.contains("事件")) {
                            if (compiled == null) {
                                compiled = ((Compilable) engine).compile("load(\"nashorn:mozilla_compat.js\");\r\n" +  scriptCache.get(path).toString());
                                if (compiledCache.containsKey(c.getAccID())) {
                                    ((Map) compiledCache.get(c.getAccID())).put(path, compiled);
                                } else {
                                    Map<String, CompiledScript> compiledMap = new HashMap<>();
                                    compiledMap.put(path, compiled);
                                    compiledCache.put(c.getAccID(), compiledMap);
                                }
                            }
                            compiled.eval();
                            return (Invocable)compiled.getEngine();
                        }else{
                            engine.eval("load(\"nashorn:mozilla_compat.js\");\r\n" + scriptCache.get(path).toString());
                            return (Invocable)engine;
                        }

                    } catch (ScriptException e) {
                        FilePrinter.printError("AbstractScriptManager.txt", "Error executing script. Path: " + path + "\nException " + (Object)e);
                        return null;
                    }
                }
            }
             File scriptFile = new File(path);
            if (!scriptFile.exists()) {
                return null;
            }
//            engine = AbstractScriptManager.sem.getEngineByName("javascript");
            engine = AbstractScriptManager.sem.getEngineByName("javascript");


            if (c != null) {
                c.setScriptEngine(path, engine);
            }
            String str = null;
            StringBuilder sb = new StringBuilder();
            boolean 进入start;
            boolean 已有status;
            InputStream in = null;
            try {
                in = Files.newInputStream(scriptFile.toPath());

                 BufferedReader bf = new BufferedReader(new InputStreamReader(in, EncodingDetect.getJavaEncode(scriptFile)));

                进入start = false;

                for(已有status = false; (str = bf.readLine()) != null; sb.append(str + "\r\n")) {
                    if (str.contains("start()")) {
                        进入start = true;
                    }

                    if (进入start) {
                        if (str.contains("status") && !str.contains("//status")) {
                            已有status = true;
                        }

                        if (str.contains("action(1, 0, 0)") && !已有status) {
                            sb.append("    status = -1;\r\n");
                        }
                    }
                }

                if ((Integer)LtMS.ConfigValuesMap.get("脚本缓存开关") > 0) {
                    scriptCache.put(path, sb);
                }
                    engine.eval("load(\"nashorn:mozilla_compat.js\");\r\n" + sb.toString());
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
            c.sendPacket(MaplePacketCreator.enableActions());
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
        Map<String, StringBuilder> ret = new ConcurrentHashMap<>(scriptCache);
        ArrayList<String> pathList = new ArrayList<>();
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
                    fr = Files.newInputStream(scriptFile.toPath());

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
                }finally {
                    try {
                        if (fr != null) {
                            fr.close();
                        }
                    } catch (IOException var12) {
                        服务端输出信息.println_err("【错误】reloadScriptCache 关闭文件流失败，失败原因：" + var12);
                    }
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
