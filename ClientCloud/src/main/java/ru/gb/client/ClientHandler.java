package ru.gb.client;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import ru.gb.core.*;

import java.io.File;
import java.io.FileOutputStream;




public class ClientHandler extends SimpleChannelInboundHandler<Message> {

    private LocalFIle lf;
    private byte[] buf = new byte[1024 * 1024];
    private DataSet tmp;
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) throws Exception {
        if(msg instanceof AuthentcationRequest){
            AuthentcationRequest aMsg=(AuthentcationRequest) msg;
            if(aMsg.isStat()){
                System.out.println("userID: "+aMsg.getId());
                System.out.println("Authentcation: ok!");
//                System.out.println("Просим файл");
//                Command com=new Command("upload","D:\\testProject\\in\\1.pdf");
//                ctx.writeAndFlush(com);
//                ctx.flush();
            }else{
                System.out.println("Authentcation: Err!");
                ctx.channel().close();
            }
        }
        if (msg instanceof Answer){
            Answer ans = (Answer) msg;
            if (ans.isSuccess()){
                System.out.println("Ok");
                System.out.println(ans.getMessage());
            }else{
                System.out.println("Error!");
                System.out.println(ans.getMessage());
            }
        }
        if (msg instanceof DataSet) {
                DataSet dMsg = (DataSet) msg;
                File outFile=new File(dMsg.getPathFile());
                try (FileOutputStream outF = new FileOutputStream(outFile, true)) {
                    outF.write(dMsg.getData());
                    outF.flush();
                }
                if (dMsg.getAllPart()== dMsg.getTpart()){
                    outFile.setLastModified(dMsg.getDateMod());
                }
            }

    }
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
