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

import java.nio.charset.Charset;
@Getter
public class ClientCloud {

    Bootstrap b=null;
    private String server;
    private int port;

    public ClientCloud(String server, int port) {
        this.server = server;
        this.port = port;
    }
    public void run() throws InterruptedException {
        EventLoopGroup bossGroup=new NioEventLoopGroup();
        try{
            b = new Bootstrap();
            b.group(bossGroup);
            b.channel(NioSocketChannel.class);
            b.option(ChannelOption.SO_KEEPALIVE,true);
            b.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline().addLast(
                            new LoggingHandler(LogLevel.DEBUG),
                            new LengthFieldBasedFrameDecoder(2097152,0,4,0,4),
                            new LengthFieldPrepender(4),
                            new StringEncoder(Charset.forName("UTF-8")),
                            new StringDecoder(Charset.forName("UTF-8")),
                            new MyJsonEncoder(),
                            new MyJsonDecoder(),
                            new ClientHandler()
//                            new TestClientHandler()
                    );
                }
            });
            ChannelFuture f = b.connect(server,port).sync();
            f.channel().closeFuture().sync();

        }finally{
            bossGroup.shutdownGracefully();
        }
    }
}
