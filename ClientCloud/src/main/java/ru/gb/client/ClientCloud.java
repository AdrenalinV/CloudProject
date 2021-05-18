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

@Getter
public class ClientCloud {
    private static ClientCloud item;
    private SocketChannel socketChanel;
    private final String server;
    private final int port;

    public static ClientCloud getInstance(String server, int port)  {
        if (item == null) {
            item = new ClientCloud(server, port);
        }
        return item;
    }


    private ClientCloud(String server, int port) {
        this.server = server;
        this.port = port;
        System.out.println("[DEBUG] server: " + server + " port: " + port);
        new Thread(() -> {

            EventLoopGroup bossGroup = new NioEventLoopGroup();
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
                                socketChanel.pipeline().addLast(
                                        new LoggingHandler(LogLevel.DEBUG),
                                        new LengthFieldBasedFrameDecoder(2097152, 0, 4, 0, 4),
                                        new LengthFieldPrepender(4),
                                        new StringEncoder(StandardCharsets.UTF_8),
                                        new StringDecoder(StandardCharsets.UTF_8),
                                        new MyJsonEncoder(),
                                        new MyJsonDecoder(),
                                        new ClientHandler()
                                );
                            }
                        });
                ChannelFuture f = b.connect(server, port).sync();
                f.channel().closeFuture().sync();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                bossGroup.shutdownGracefully();
            }
        }).start();

    }

    public void autent(String user, String passw) {
        System.out.println("[DEBUG] send message atent");
        AuthentcationRequest aMsg = new AuthentcationRequest();
        aMsg.setUser(user);
        aMsg.setPass(passw);
        socketChanel.writeAndFlush(aMsg);
    }
}
