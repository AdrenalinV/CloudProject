package ru.gb.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.log4j.Log4j2;
import ru.gb.core.MyJsonDecoder;
import ru.gb.core.MyJsonEncoder;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
@Log4j2
public class ServerCloud {
    private static final int DEFAULT_PORT = 8999;
    private final int port;

    public static void main(String[] args) throws Exception {
        int port = 0;
        if (args.length > 0 && args[0] != null) {
            port = Integer.parseInt(args[0]);
        }
        new ServerCloud(port).run();
    }


    public ServerCloud(int port) {
        if (port != 0) {
            this.port = port;

        } else {
            this.port = DEFAULT_PORT;
        }
    }

    public void run() throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workedGroup = new NioEventLoopGroup();
        ExecutorService threadPull = Executors.newCachedThreadPool();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workedGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ch.pipeline().addLast(
                                    new LoggingHandler(LogLevel.DEBUG),
                                    new LengthFieldBasedFrameDecoder(2097152, 0, 4, 0, 4),
                                    new LengthFieldPrepender(4),
                                    new StringDecoder(StandardCharsets.UTF_8),
                                    new StringEncoder(StandardCharsets.UTF_8),
                                    new MyJsonEncoder(),
                                    new MyJsonDecoder(),
                                    new ServerHandler(threadPull)
                            );
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);
            log.info("Start server");
            ChannelFuture f = b.bind(port).sync();
            f.channel().closeFuture().sync();

        } finally {
            workedGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
            log.info("stop server");
        }
    }
}
