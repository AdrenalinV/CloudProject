package ru.gb.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.Getter;
import ru.gb.core.AuthentcationRequest;
import ru.gb.core.MyJsonDecoder;
import ru.gb.core.MyJsonEncoder;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Getter
public class Network {

    private static Network item;
    private SocketChannel socketChanel;
    private static final String SERVER = "localhost";
    private static final int PORT = 8999;

    public static Network getInstance(MainControl mControl) {
        if (item == null || !item.socketChanel.isActive()) {
            item = new Network(mControl);
        }
        return item;
    }


    private Network(MainControl mControl) {
        new Thread(() -> {
            EventLoopGroup bossGroup = new NioEventLoopGroup();
            ExecutorService threadPull = Executors.newCachedThreadPool();
            try {
                System.out.println("[DEBUG] RUN Connect");
                Bootstrap b = new Bootstrap();
                b.group(bossGroup)
                        .channel(NioSocketChannel.class)
                        .option(ChannelOption.SO_KEEPALIVE, true)
                        .handler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            protected void initChannel(SocketChannel ch) {
                                socketChanel = ch;
                                System.out.println("[DEBUG] Exist connect");
                                socketChanel.pipeline().addLast(
                                        new LoggingHandler(LogLevel.DEBUG),
                                        new LengthFieldBasedFrameDecoder(2097152, 0, 4, 0, 4),
                                        new LengthFieldPrepender(4),
                                        new StringEncoder(StandardCharsets.UTF_8),
                                        new StringDecoder(StandardCharsets.UTF_8),
                                        new MyJsonEncoder(),
                                        new MyJsonDecoder(),
                                        new ClientHandler(mControl, threadPull)
                                );
                            }
                        });
                ChannelFuture f = b.connect(SERVER, PORT).sync();
                f.channel().closeFuture().sync();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                bossGroup.shutdownGracefully();
                threadPull.shutdown();
            }
        }).start();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void autent(String user, String passw) {
        System.out.println("[DEBUG] send message atent");
        AuthentcationRequest aMsg = new AuthentcationRequest();
        aMsg.setUser(user);
        aMsg.setPass(passw);
        socketChanel.writeAndFlush(aMsg);
    }

    public void close() {
        socketChanel.close();
        System.out.println("[DEBUG] socket close");
    }

    public static boolean isLive() {
        return item != null;
    }


}
