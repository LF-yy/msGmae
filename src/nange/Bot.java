package nange;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Bot {
    // 建议这个值改为配置文件加载
    private static final String baseUrl = "http://127.0.0.1:9600";
    // 建议这个值改为配置文件加载, 是否启用
    private static boolean enable = true;
    // 建议这个值改为配置文件加载, 群号, 保留值, 现在没用到
    private static String groupId = "";
    // 纯网络 IO, 非 CPU 密集型任务, 可以多分配一下工作线程
    private static ExecutorService executorService = Executors.newFixedThreadPool(32);
    // private static ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 4);

    public static void main(String[] args) throws Exception {
        // 测试发送群消息
        sendGroupMessage("\"  123\n\n\n123");
    }

    /**
     * 异步发送消息, 不阻塞线程
     * @param content
     */
    public static void sendGroupMessage(String content) {
        try {
            if (!enable) return;
            executorService.execute(() -> {
                // 因为部分用户没有 JSON 库, 我这里实现了一个手动转义
                sendMessage(baseUrl + "/bot/api/sendGroup", String.format("{\"content\": \"%s\", \"type\": \"string\"}", escape(content)));
            });
        } catch (Exception e) {

        }
    }

    private static void sendMessage(String url, String data) {
        // 部分老项目没有 http 库, 使用原生方式
        HttpURLConnection connection = null;
        OutputStream outputStream = null;
        try {
            connection = (HttpURLConnection)new URL(url).openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);
            outputStream = connection.getOutputStream();
            outputStream.write(data.getBytes(StandardCharsets.UTF_8));
            connection.getResponseCode();
        } catch (Exception e) {
            //e.printStackTrace();
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (Exception e) {
                    //e.printStackTrace();
                }
            }
            if (connection != null) {
                try {
                    connection.disconnect();
                } catch (Exception e) {
                    //e.printStackTrace();
                }
            }
        }
    }

    private static String escape(String s) {
        StringBuilder sb = new StringBuilder();
        for (char c : s.toCharArray()) {
            if (c == '\\') {
                sb.append("\\\\");
            } else if (c == '\"') {
                sb.append("\\\"");
            } else if (c == '\t') {
                sb.append("\\t");
            } else if (c == '\r') {
                sb.append("\\r");
            } else if (c == '\b') {
                sb.append("\\b");
            } else if (c == '\n') {
                sb.append("\\n");
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}