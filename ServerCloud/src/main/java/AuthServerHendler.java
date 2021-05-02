import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.sql.SQLException;

public class AuthServerHendler extends SimpleChannelInboundHandler<String> {
    private String user;
    private String pass;
    private BaseAuthService bas=null;
    AuthServerHendler(){
        this.bas=BaseAuthService.of();
    }


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String s) throws Exception {
        if (s.startsWith("USER")){
            this.user=s.substring(5,s.length());
            System.out.println("USER: "+this.user);
        }else if(s.startsWith("PASS")){
            this.pass=s.substring(5,s.length());
            System.out.println("PASS: "+this.pass);
        }
        if(this.user!=null && this.pass!=null){
            if (this.bas.getAuthByLoginPass(this.user,this.pass)!=null){
                ctx.writeAndFlush("YES");
                System.out.println("AUTH: YES");
                System.out.println(ctx.channel().pipeline());
                ctx.channel().pipeline().addLast(new MyJsonEncoder());
                ctx.channel().pipeline().addLast(new MyJsonDecoder());
                ctx.channel().pipeline().remove(this);
                System.out.println(ctx.channel().pipeline());
            }else{
                System.out.println("AUTH: NO");
                this.user=null;
                this.pass=null;
                ctx.writeAndFlush("NO");
            }
        }


    }
}
