package scripting;

import java.util.Iterator;

import com.alibaba.fastjson.JSONObject;

import org.apache.commons.lang.StringUtils;
import tools.FilePrinter;
import javax.script.ScriptEngine;
import javax.script.Invocable;
import client.MapleClient;
import java.util.LinkedHashMap;
import handling.channel.ChannelServer;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.Map;

public class EventScriptManager extends AbstractScriptManager {
    private final ConcurrentHashMap<String, EventEntry> events = new ConcurrentHashMap<>();    private final AtomicInteger runningInstanceMapId ;

    public final int getNewInstanceMapId() {
        return this.runningInstanceMapId.addAndGet(1);
    }

    public EventScriptManager( ChannelServer cserv,  String[] scripts) {
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
                entry.iv.invokeFunction("init", (Object) null);
                return true;
            } else {
                return false;
            }
        } catch (Exception var3) {
            //服务端输出信息.println_err("Error initiating event: " + script + ":" + var3);
            FilePrinter.printError("EventScriptManager.txt", "Error initiating event: " + script + ":" + var3);
            return false;
        }
    }

    public boolean loadEntry(ChannelServer cserv, String script) {
        try {
            // 增强空值检查
            if (script == null || script.isEmpty()) {
                return false;
            }

            // 确保线程安全性
            events.remove(script);

            // 路径配置化
            String scriptPath = "事件/" + script + ".js";
            Invocable iv = this.getInvocable(scriptPath, null);

            if (iv != null) {
                // 创建 EventEntry 并存入线程安全的集合
                EventEntry eventEntry = new EventEntry(script, iv, new EventManager(cserv, iv, script));
                events.put(script, eventEntry);
            }

            return true;
        } catch (Exception e) {
            // 改进异常处理，记录详细日志
            System.err.println("【错误】loadEntry 错误，脚本名称：" + script + "，错误原因：" + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public EventEntry getEntry(String script) {
        if (script == null) {
            FilePrinter.printError("EventScriptManager.txt", "Error getEntry: script is null");
            return null;
        }

        Iterator<EventEntry> iterator = this.events.values().iterator();

        while (iterator.hasNext()) {
            EventEntry entry = iterator.next();

            try {
                if (entry != null && areScriptsEqual(entry.script, script)) {
                    return entry;
                }
            } catch (NullPointerException e) {
                // 记录空指针异常
                FilePrinter.printError("EventScriptManager.txt", "NullPointerException in getEntry: entry.script=" + entry.script + ", script=" + script);
            } catch (Exception e) {
                // 记录其他异常
                FilePrinter.printError("EventScriptManager.txt", "Unexpected exception in getEntry: entry.script=" + entry.script + ", script=" + script);
            }
        }

        return null;
    }

    // 辅助方法：比较两个字符串是否相等，避免空指针异常
    private boolean areScriptsEqual(String script1, String script2) {
        return script1 == null ? script2 == null : script1.equals(script2);
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

    // 假设 EventEntry 和 EventManager 类已定义
    private static class EventEntry {
        private final String script;
        private final Invocable iv;
        private final EventManager em;

        public EventEntry(String script, Invocable iv, EventManager em) {
            this.script = script;
            this.iv = iv;
            this.em = em;
        }
    }
}
