package handling.mina;

import constants.WorldConstants;
import io.netty.channel.ChannelOption;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import tools.FileoutputUtil;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.EventLoopGroup;
import io.netty.bootstrap.ServerBootstrap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.netty.channel.Channel;

import java.util.concurrent.TimeUnit;
/**
 * 服务器连接类，用于初始化和管理服务器的网络连接
 */
public class ServerConnection {
    // 日志记录器，用于记录日志信息
    private static final Logger logger = LoggerFactory.getLogger(ServerConnection.class);
    // 日志文件路径，用于记录异常输出
    private static final String LOG_FILE_PATH = "logs/异常輸出.txt";

    // 服务器端口
    private final int port;
    // 世界ID
    private int world;
    // 频道数量
    private int channels;
    // 服务器引导对象，用于配置和启动服务器
    private ServerBootstrap boot;
    // 主事件循环组，处理连接请求
    private final EventLoopGroup bossGroup;
    // 工作事件循环组，处理数据读写
    private final EventLoopGroup workerGroup;
    // 服务器通道，用于网络通信
    private Channel channel;

    /**
     * 构造函数，初始化服务器连接
     *
     * @param port 服务器端口
     */
    public ServerConnection(final int port) {
        this(port, -1, -1);
    }

    /**
     * 构造函数，初始化服务器连接
     *
     * @param port   服务器端口
     * @param world  世界ID
     * @param channels 频道数量
     */
    public ServerConnection(final int port, final int world, final int channels) {
        this.world = -1;
        this.channels = -1;
        this.bossGroup = new NioEventLoopGroup(1);
        this.workerGroup = new NioEventLoopGroup();
        this.port = port;
        this.world = world;
        this.channels = channels;
    }

    /**
     * 启动服务器，监听指定端口
     */
    public void run() {
        try {
            // 配置服务器引导对象
            this.boot = ((new ServerBootstrap().group(this.bossGroup, this.workerGroup).channel(NioServerSocketChannel.class)).option(ChannelOption.SO_BACKLOG, WorldConstants.USER_LIMIT)).childOption(ChannelOption.TCP_NODELAY, Boolean.TRUE).childOption(ChannelOption.SO_KEEPALIVE, Boolean.TRUE).childHandler(new ServerInitializer(this.world, this.channels));
            try {
                // 绑定端口并启动服务器
                this.channel = this.boot.bind(this.port).sync().channel().closeFuture().channel();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } catch (Exception e2) {
            // 处理启动失败的情况
            System.out.printf("Connection to %s failed.", (this.channel == null) ? e2.toString() : this.channel.remoteAddress());
            FileoutputUtil.outputFileError("logs/异常輸出.txt", (Throwable) e2);
            this.close();
        }
    }

    /**
     * 关闭服务器连接
     */
    public void close() {
        if (this.channel != null) {
            try {
                // 关闭通道
                this.channel.close().sync();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.error("Interrupted while closing channel", e);
            }
        }
        // 关闭事件循环组
        this.bossGroup.shutdownGracefully(0, 5, TimeUnit.SECONDS);
        this.workerGroup.shutdownGracefully(0, 5, TimeUnit.SECONDS);
    }

    /**
     * 获取服务器端口
     *
     * @return 服务器端口
     */
    public int getPort() {
        return this.port;
    }
}
