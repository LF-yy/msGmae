package scripting;

import javax.script.ScriptEngineManager;
import client.MapleClient;
import gui.LtMS;
import gui.服务端输出信息;
import server.MaplePortal;
import javax.script.CompiledScript;
import javax.script.ScriptEngine;
import java.io.InputStream;
import javax.script.Invocable;
import java.io.IOException;
import javax.script.ScriptException;

import server.Start;
import tools.FilePrinter;
import java.io.UnsupportedEncodingException;
import java.io.FileNotFoundException;
import javax.script.Compilable;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.io.Reader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import tools.EncodingDetect;
import java.io.FileInputStream;
import java.io.File;
import java.util.HashMap;
import javax.script.ScriptEngineFactory;
import java.util.Map;

public class PortalScriptManager
{
    private static final PortalScriptManager instance = new PortalScriptManager();
    private final Map<String, PortalScript> scripts = new HashMap();
    private static final ScriptEngineFactory sef = (new ScriptEngineManager()).getEngineByName("nashorn").getFactory();
    private static Map<String, StringBuilder> scriptCache = new HashMap();

    public PortalScriptManager() {
    }

    public static final PortalScriptManager getInstance() {
        return instance;
    }

    private final PortalScript getPortalScript(String scriptName) {
        if (this.scripts.containsKey(scriptName)) {
            return (PortalScript)this.scripts.get(scriptName);
        } else {
            InputStream in = null;
            ScriptEngine portal = sef.getScriptEngine();

            try {
                StringBuilder ret = new StringBuilder();
                File scriptFile;
                BufferedReader bf ;
                if ((Integer) LtMS.ConfigValuesMap.get("脚本缓存开关") > 0) {
                    if (!scriptCache.containsKey(scriptName)) {
                        scriptFile = new File("scripts/portal/" + scriptName + ".js");
                        if (!scriptFile.exists()) {
                            this.scripts.put(scriptName, null);
                            return null;
                        }

                        in = new FileInputStream(scriptFile);
                        bf = new BufferedReader(new InputStreamReader(in, EncodingDetect.getJavaEncode(scriptFile)));
                        ret.append("load('nashorn:mozilla_compat.js');");
                        ret.append((String)bf.lines().collect(Collectors.joining(System.lineSeparator())));
                        scriptCache.put(scriptName, ret);
                    } else {
                        ret = (StringBuilder)scriptCache.get(scriptName);
                    }
                } else {
                    scriptFile = new File("scripts/portal/" + scriptName + ".js");
                    if (!scriptFile.exists()) {
                        this.scripts.put(scriptName,null);

                        return null;
                    }

                    in = new FileInputStream(scriptFile);
                    bf = new BufferedReader(new InputStreamReader(in, EncodingDetect.getJavaEncode(scriptFile)));
                    ret.append("load('nashorn:mozilla_compat.js');");
                    ret.append((String)bf.lines().collect(Collectors.joining(System.lineSeparator())));
                }

                CompiledScript compiled = ((Compilable)portal).compile(ret.toString());
                compiled.eval();
            } catch (UnsupportedEncodingException | ScriptException | FileNotFoundException var18) {
                服务端输出信息.println_err("Error executing Portalscript: " + scriptName + ":" + var18);
                FilePrinter.printError("PortalScriptManager.txt", var18);
            } finally {
                try {
                    if (in != null) {
                        in.close();
                    }
                } catch (IOException var17) {
                }

            }

            PortalScript script = (PortalScript)((Invocable)portal).getInterface(PortalScript.class);
            this.scripts.put(scriptName, script);
            return script;
        }
    }

    public final void executePortalScript(MaplePortal portal, MapleClient c) {
        PortalScript script = this.getPortalScript(portal.getScriptName());
        if (c != null && c.getPlayer() != null && c.getPlayer().hasGmLevel(2) && (Integer) LtMS.ConfigValuesMap.get("脚本显码开关") > 0) {
            c.getPlayer().dropMessage("您已经建立与传送门脚本: " + portal.getScriptName() + ".js 的连接。");
        }

        if (c != null && c.getPlayer() != null && c.getPlayer().hasGmLevel(2) && (Integer)LtMS.ConfigValuesMap.get("封包记录开关") > 0) {
            服务端输出信息.println_out("您已经建立与传送门脚本: " + portal.getScriptName() + ".js 的连接。");
        }

        if (!c.canClickPortal()) {
            c.getPlayer().dropMessage(5, "您的操作过快，请稍后再传送。");
        } else if (script != null) {
            try {
                c.setClickedPortal();
                script.enter(new PortalPlayerInteraction(c, portal));
            } catch (Exception var5) {
                服务端输出信息.println_err("进入传送门脚本失败: " + portal.getScriptName() + ":" + var5);
            }
        }

        this.clearScripts();
    }

    public final void clearScripts() {
        this.scripts.clear();
    }

    public static void clearScriptCache() {
        scriptCache.clear();
    }
//    private static final PortalScriptManager instance;
//    private final Map<String, PortalScript> scripts;
//    private static final ScriptEngineFactory sef;
//    public PortalScriptManager() {
//        this.scripts = new HashMap<String, PortalScript>();
//    }
//    
//    public static PortalScriptManager getInstance() {
//        return PortalScriptManager.instance;
//    }
//    
//    private final PortalScript getPortalScript(final String scriptName) {
//        if (this.scripts.containsKey((Object)scriptName)) {
//            return (PortalScript)this.scripts.get((Object)scriptName);
//        }
//        final File scriptFile = new File("脚本/传送/" + scriptName + ".js");
//        if (!scriptFile.exists()) {
//            this.scripts.put(scriptName, null);
//            return null;
//        }
//        InputStream in = null;
//        final ScriptEngine portal = PortalScriptManager.sef.getScriptEngine();
//        try {
//            in = new FileInputStream(scriptFile);
//            final BufferedReader bf = new BufferedReader((Reader)new InputStreamReader(in, EncodingDetect.getJavaEncode(scriptFile)));
//            final String lines = "load('nashorn:mozilla_compat.js');" + (String)bf.lines().collect((Collector<? super String, ?, String>)Collectors.joining((CharSequence)System.lineSeparator()));
//            final CompiledScript compiled = ((Compilable)portal).compile(lines);
//            compiled.eval();
//        }
//        catch (FileNotFoundException ex) {}
//        catch (UnsupportedEncodingException ex2) {}
//        catch (ScriptException e) {
//            System.err.println("Error executing Portalscript: " + scriptName + ":" + (Object)e);
//            FilePrinter.printError("PortalScriptManager.txt", (Throwable)e);
//        }
//        finally {
//            try {
//                if (in != null) {
//                    in.close();
//                }
//            }
//            catch (IOException ex3) {}
//        }
//        final PortalScript script = (PortalScript)((Invocable)portal).getInterface(PortalScript.class);
//        this.scripts.put(scriptName, script);
//        return script;
//    }
//    
//    public void executePortalScript(final MaplePortal portal, final MapleClient c) {
//        final PortalScript script = this.getPortalScript(portal.getScriptName());
//        if (c != null && c.getPlayer() != null && c.getPlayer().hasGmLevel(2)) {
//            c.getPlayer().dropMessage("您已經建立與傳送門腳本: " + portal.getScriptName() + ".js 的关联。");
//        }
//        if (script != null) {
//            try {
//                script.enter(new PortalPlayerInteraction(c, portal));
//            }
//            catch (Exception e) {
//                System.err.println("進入傳送腳本失敗: " + portal.getScriptName() + ":" + (Object)e);
//            }
//        }
//        this.clearScripts();
//    }
//    
//    public void clearScripts() {
//        this.scripts.clear();
//    }
//    
//    static {
//        instance = new PortalScriptManager();
//        sef = new ScriptEngineManager().getEngineByName("nashorn").getFactory();
//    }
}
