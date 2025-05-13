package scripting;

import javax.script.ScriptEngineManager;
import client.MapleClient;
import gui.LtMS;

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
import java.util.concurrent.ConcurrentHashMap;
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
    private final Map<String, PortalScript> scripts = new ConcurrentHashMap<>();
    private static final ScriptEngineFactory sef = (new ScriptEngineManager()).getEngineByName("nashorn").getFactory();
    private static final Map<String, StringBuilder> scriptCache = new ConcurrentHashMap<>();

    public PortalScriptManager() {
    }

    public static final PortalScriptManager getInstance() {
        return instance;
    }

    private  PortalScript getPortalScript(final String scriptName) {
        InputStream in = null;
        ScriptEngine portal = PortalScriptManager.sef.getScriptEngine();
        try {
            if (this.scripts.containsKey((Object) scriptName)) {
                return (PortalScript) this.scripts.get((Object) scriptName);
            }
            StringBuilder ret = new StringBuilder();
            File scriptFile;
            BufferedReader bf ;
            if ((Integer) LtMS.ConfigValuesMap.get("脚本缓存开关") > 0) {
                if (!scriptCache.containsKey(scriptName)) {
                    scriptFile = new File("脚本/传送/" + scriptName + ".js");
                    if (!scriptFile.exists()) {
                        this.scripts.put(scriptName, null);
                        return null;
                    }

                    in = new FileInputStream(scriptFile);
                    bf = new BufferedReader(new InputStreamReader(in, EncodingDetect.getJavaEncode(scriptFile)));
                    ret.append("load('nashorn:mozilla_compat.js');");
                    ret.append((String) bf.lines().collect(Collectors.joining(System.lineSeparator())));
                    scriptCache.put(scriptName, ret);
                } else {
                    ret = (StringBuilder) scriptCache.get(scriptName);
                }
                CompiledScript compiled = ((Compilable)portal).compile(ret.toString());
                compiled.eval();
            } else{
                  scriptFile = new File("脚本/传送/" + scriptName + ".js");
            if (!scriptFile.exists()) {
                this.scripts.put(scriptName, null);
                return null;
            }

              portal = PortalScriptManager.sef.getScriptEngine();

            in = new FileInputStream(scriptFile);
             bf = new BufferedReader((Reader) new InputStreamReader(in, EncodingDetect.getJavaEncode(scriptFile)));
             String lines = "load('nashorn:mozilla_compat.js');" + (String) bf.lines().collect((Collector<? super String, ?, String>) Collectors.joining((CharSequence) System.lineSeparator()));

                CompiledScript compiled = ((Compilable)portal).compile(lines);
                compiled.eval();
        }
        }
        catch (FileNotFoundException ex) {}
        catch (UnsupportedEncodingException ex2) {}
        catch (ScriptException e) {
            System.err.println("Error executing Portalscript: " + scriptName + ":" + (Object)e);
            FilePrinter.printError("PortalScriptManager.txt", (Throwable)e);
        }
        finally {
            try {
                if (in != null) {
                    in.close();
                }
            }
            catch (IOException ex3) {}
        }
         PortalScript script = (PortalScript)((Invocable)portal).getInterface(PortalScript.class);
        this.scripts.put(scriptName, script);
        return script;
    }



    public final void executePortalScript(final MaplePortal portal, final MapleClient c) {
        final PortalScript script = this.getPortalScript(portal.getScriptName());
        if (c != null && c.getPlayer() != null && c.getPlayer().hasGmLevel(2)) {
            c.getPlayer().dropMessage("您已经建立與傳送門腳本: " + portal.getScriptName() + ".js 的关联。");
        }
        if (c != null && c.getPlayer() != null ) {
            if (!c.canClickPortal()) {
                c.getPlayer().dropMessage(5, "您的操作过快，请稍后再传送。");
            } else if (script != null) {
                try {
                    c.setClickedPortal();
                    script.enter(new PortalPlayerInteraction(c, portal));
                }
                catch (Exception e) {
                    System.err.println("进入傳送腳本失敗: " + portal.getScriptName() + ":" + (Object)e);
                }
            }
        }
        this.clearScripts();
    }

    public final void clearScripts() {
        this.scripts.clear();
        clearScriptCache();
    }

    public static void clearScriptCache() {
        scriptCache.clear();
    }

}
