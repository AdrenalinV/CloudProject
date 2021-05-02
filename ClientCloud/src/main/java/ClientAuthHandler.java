import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class ClientAuthHandler extends SimpleChannelInboundHandler<Message> {
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ctx.writeAndFlush("USER:Vadim");
        ctx.writeAndFlush("PASS:V1570");
    }


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) throws Exception {



    }
}
