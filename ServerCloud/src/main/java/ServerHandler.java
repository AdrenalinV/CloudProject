

import io.netty.channel.*;

import java.io.File;
import java.io.FileInputStream;


public class ServerHandler extends SimpleChannelInboundHandler<String> {
    byte[] buf = new byte[1024 * 1024];
    DataSet tmp = null;
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("[Dbug]: New client Add.");
        File fin=new File("D:\\test\\1.pdf");
        FileInputStream in =new FileInputStream(fin);
        System.out.println("[Dbug]: openFile.");
        int size;
        int allPart;
        int part;
        allPart = (int) (fin.length() / (1024 * 1024)) + (fin.length() % (1024 * 1024) != 0 ? 1 : 0);
        part = 0;
        while (in.available() != 0) {
            size = in.read(buf);
            tmp=new DataSet(fin.getPath(), fin.getName(),fin.lastModified(),allPart, ++part, size, buf);
            System.out.println("[Dbug]: readFile. "+ tmp.getTpart());
            ctx.writeAndFlush(tmp);
        }
        System.out.println("[Dbug]: closeFile.");
        in.close();

    }


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String s) throws Exception {
        System.out.println(s);

    }
}
