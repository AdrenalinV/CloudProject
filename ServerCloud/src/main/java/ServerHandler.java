

import io.netty.channel.*;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Locale;


public class ServerHandler extends SimpleChannelInboundHandler<String> {
    private byte[] buf = new byte[1024 * 1024];
    private DataSet tmp = null;
    private FileInputStream in = null;

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ArrayList<File> files = LocalFIle.getFiles("D:\\testProject\\in");
        for (File file : files) {
            in = new FileInputStream(file);
            int size;
            int allPart;
            int part;
            allPart = (int) (file.length() / (1024 * 1024)) + (file.length() % (1024 * 1024) != 0 ? 1 : 0);
            part = 0;
            while (in.available() != 0) {
                size = in.read(buf);
                tmp = new DataSet(file.getPath(), file.getName(), file.lastModified(), allPart, ++part, size, buf);
                ChannelFuture f = ctx.writeAndFlush(tmp);
                f.addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture future) throws Exception {
                        assert f == future;
                    }
                });
            }
            in.close();
            in = null;
        }


    }


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String s) throws Exception {
        System.out.println(s);

    }
}
