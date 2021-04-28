
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.io.File;


public class ClientHandler extends SimpleChannelInboundHandler<DataSet> {
    private LocalFIle out=null;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DataSet ds) throws Exception {
        System.out.println("part: "+ds.getTpart());
        if(ds.getTpart()==1){
            out=new LocalFIle("D:\\testProject\\out"+ File.separator+ds.getNameFile());
            System.out.println("[Debug]: создать файл "+ds.getTpart());
        }
        out.getFs().write(ds.getData());
        if(ds.getTpart()== ds.getAllPart()){
            out.close();
            out=null;
            System.out.println("[Debug]: закрыть файл");
        }
    }
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("Add:Client");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
