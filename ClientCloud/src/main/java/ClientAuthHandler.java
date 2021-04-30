import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class ClientAuthHandler extends SimpleChannelInboundHandler<String> {
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ctx.writeAndFlush("USER:Vadim");
        ctx.writeAndFlush("PASS:V1570");
    }


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String s) throws Exception {
        if (s.equals("YES")){
            ctx.channel().pipeline().remove(this);
        }

    }
}
