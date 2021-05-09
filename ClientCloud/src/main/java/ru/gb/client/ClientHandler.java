package ru.gb.client;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import ru.gb.core.*;

import java.io.File;
import java.io.FileOutputStream;


public class ClientHandler extends SimpleChannelInboundHandler<Message> {
    private MainControl mControl;
    private LocalFIle lf;
    private byte[] buf = new byte[1024 * 1024];
    private DataSet tmp;

    public ClientHandler(MainControl mControl) {
        this.mControl = mControl;
    }


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) throws Exception {
        if (msg instanceof AuthentcationRequest) {
            AuthentcationRequest aMsg = (AuthentcationRequest) msg;
            if (aMsg.isStat()) {
                System.out.println("[DEBUG] userID: " + aMsg.getId());
                System.out.println("[DEBUG] Authentcation: ok!");
                mControl.autOK();
                Command cMsg = new Command("list", "");
                ctx.writeAndFlush(cMsg);
            } else {
                System.out.println("[DEBUG] Authentcation: Err!");
                ctx.channel().close();
            }
        }
        if (msg instanceof Answer) {
            Answer ans = (Answer) msg;
            switch (ans.getCommandName()) {
                case "list":
                    System.out.println("[DEBUG] list command");
                    if(ans.isSuccess()){
                        mControl.addServerList(ans.getMessage().split(","));
                    }
                    break;
                case "clear":

                    break;
                case "delete":

                    break;
                case "undelete":

                    break;

            }
            if (ans.isSuccess()) {
                System.out.println("[DEBUG] Ok");
                System.out.println(ans.getMessage());
            } else {
                System.out.println("[DEBUG] Error!");
                System.out.println(ans.getMessage());
            }
        }
        if (msg instanceof DataSet) {
            DataSet dMsg = (DataSet) msg;
            File outFile = new File(dMsg.getPathFile());
            outFile.deleteOnExit();
            try (FileOutputStream outF = new FileOutputStream(outFile, true)) {
                outF.write(dMsg.getData());
                outF.flush();
            }
            if (dMsg.getAllPart() == dMsg.getTpart()) {
                outFile.setLastModified(dMsg.getDateMod());
            }
        }

    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
    }
}
