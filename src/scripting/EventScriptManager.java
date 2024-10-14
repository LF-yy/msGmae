package scripting;

import java.util.Iterator;

import com.alibaba.fastjson.JSONObject;
import gui.服务端输出信息;
import org.apache.commons.lang.StringUtils;
import tools.FilePrinter;
import javax.script.ScriptEngine;
import javax.script.Invocable;
import client.MapleClient;
import java.util.LinkedHashMap;
import handling.channel.ChannelServer;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.Map;

public class EventScriptManager extends AbstractScriptManager {
    private final Map<String, EventEntry> events ;
    private final AtomicInteger runningInstanceMapId ;

    public final int getNewInstanceMapId() {
        return this.runningInstanceMapId.addAndGet(1);
    }

    public EventScriptManager( ChannelServer cserv,  String[] scripts) {
        this.events = new LinkedHashMap<String, EventEntry>();
        this.runningInstanceMapId = new AtomicInteger(0);
        for ( String script : scripts) {
            if (Objects.nonNull(script)) {
                 Invocable iv = this.getInvocable("事件/" + script + ".js", null);
                if (iv != null) {
                    this.events.put(script, new EventEntry(script, iv, new EventManager(cserv, iv, script)));
                }
            }
        }
    }

    public final EventManager getEventManager(String event) {
        EventEntry entry = (EventEntry)this.events.get(event);
        return entry == null ? null : entry.em;
    }

    public final void init() {
        for (final EventEntry entry : this.events.values()) {
            try {
                ((ScriptEngine)entry.iv).put("em", (Object)entry.em);
                ((ScriptEngine)entry.iv).put("ecm", (Object)entry.em);
                entry.iv.invokeFunction("init", new Object[] { null });
            }
            catch (Exception ex) {
                System.err.println("Error initiating event: " + entry.script + ":" + (Object)ex);
                FilePrinter.printError("EventScriptManager.txt", "Error initiating event: " + entry.script + ":" + (Object)ex);
            }
        }
    }

    public final boolean init(String script) {
        try {
            EventEntry entry = (EventEntry)this.events.get(script);
            if (entry != null) {
                ((ScriptEngine)entry.iv).put("em", entry.em);
                ((ScriptEngine)entry.iv).put("ecm", entry.em);
                entry.iv.invokeFunction("init", (Object) null);
                return true;
            } else {
                return false;
            }
        } catch (Exception var3) {
            服务端输出信息.println_err("Error initiating event: " + script + ":" + var3);
            FilePrinter.printError("EventScriptManager.txt", "Error initiating event: " + script + ":" + var3);
            return false;
        }
    }

    public boolean loadEntry(ChannelServer cserv, String script) {
        try {
            if (!script.equals("")) {
                if (this.events.containsKey(script)) {
                    this.events.remove(script);
                }

                Invocable iv = this.getInvocable("事件/" + script + ".js", (MapleClient)null);
                if (iv != null) {
                    this.events.put(script, new EventEntry(script, iv, new EventManager(cserv, iv, script)));
                }

                return true;
            } else {
                return false;
            }
        } catch (Exception var4) {
            服务端输出信息.println_err("【错误】loadEntry错误，错误原因：" + var4);
            var4.printStackTrace();
            return false;
        }
    }

    public EventEntry getEntry(String script) {
        Iterator var2 = this.events.values().iterator();

        while(var2.hasNext()) {
            EventEntry entry = (EventEntry)var2.next();

            try {
                if (entry != null && entry.script.equals(script)) {
                    return entry;
                }
            } catch (Exception var5) {
                服务端输出信息.println_err("Error getEntry event: " + entry.script + ":" + var5);
                FilePrinter.printError("EventScriptManager.txt", "Error getEntry event: " + entry.script + ":" + var5);
            }
        }

        return null;
    }

    public final void cancel() {
        for (final EventEntry entry : this.events.values()) {
            entry.em.cancel();
        }
    }

    public final void cancel(String script) {
        EventEntry entry = (EventEntry)this.events.get(script);
        if (entry != null) {
            entry.em.cancel();
        }

    }
    private static class EventEntry
    {
        public String script;
        public Invocable iv;
        public EventManager em;

        public EventEntry(final String script, final Invocable iv, final EventManager em) {
            this.script = script;
            this.iv = iv;
            this.em = em;
        }
    }
}
