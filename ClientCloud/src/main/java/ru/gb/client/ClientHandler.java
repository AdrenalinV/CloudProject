package ru.gb.client;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import ru.gb.core.*;

import java.io.*;

public class ClientHandler extends SimpleChannelInboundHandler<Message> {
    private final MainControl mControl;

    public ClientHandler(MainControl mControl) {
        this.mControl = mControl;
    }


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) throws Exception {
        if (msg instanceof AuthentcationRequest) {
            AuthentcationRequest aMsg = (AuthentcationRequest) msg;

            if (aMsg.isStat()) {
                mControl.setStatus("Вход выполнен.");
                Command cMsg = new Command(CommandType.getList, "");
                ctx.writeAndFlush(cMsg);
                cMsg = new Command(CommandType.userFiles, "");
                ctx.writeAndFlush(cMsg);
                mControl.autOK();
            } else {
                mControl.setStatus("Внимание! Ошибка входа.");
                ctx.channel().close();
            }
        }
        if (msg instanceof Answer) {
            Answer ans = (Answer) msg;
            System.out.println("[DEBUG] command: " + ans.getCommandName().toString() +
                    "value: " + ans.getCommandValue() +
                    "message: " + ans.getMessage());
            switch (ans.getCommandName()) {
                case getList:
                    System.out.println("[DEBUG] getList command");
                    System.out.println("[DEBUG] " + ans.getMessage());
                    if (ans.getMessage() != null) {
                        mControl.setLocalList(ans.getMessage().split(","));
                    }
                    break;
                case userFiles:
                    System.out.println("[DEBUG] userFiles command");
                    System.out.println("[DEBUG] " + ans.getMessage());
                    if (ans.getMessage() != null) {
                        String[] fil = ans.getMessage().split(",");
                        mControl.setServerList(fil);
                    }
                    break;
                case getLastMod: // запрос даты последней модификации
                    System.out.println("[DEBUG] getLastMod command");
                    long sTime = Long.parseLong(ans.getMessage());
                    File file = new File(ans.getCommandValue());
                    if (file.lastModified() < sTime) {
                        Command cMsg = new Command(CommandType.upload, file.getPath());
                        ctx.writeAndFlush(cMsg);
                    } else if (file.lastModified() > sTime && sTime != 0L) {
                        System.out.println("[DEBUG] upload Task add");
                        mControl.addTask(file, TypeTask.upload, mControl.getCon().getSocketChanel());

                    } else if (sTime == 0L) {
                        file.deleteOnExit(); // удаляем т.к. нет на сервере
                    }


                    break;
                case clear:
                    //TODO client clear
                    break;
                case delete:
                    //TODO client delete
                    break;
                case undelete:
                    //TODO client undelete
                    break;
                default:
                    if (ans.isSuccess()) {
                        mControl.setStatus(ans.getMessage());
                    } else {
                        mControl.setStatus("Warning! " + ans.getMessage());
                    }
            }

        }
        if (msg instanceof DataSet) {
            DataSet dMsg = (DataSet) msg;
            System.out.println("[DEBUG] " + dMsg.getNameFile() +
                    " part:" + dMsg.getTpart() +
                    " / " + dMsg.getAllPart());
            File outFile = new File(dMsg.getPathFile());
            if (dMsg.getTpart() == 1) {
                if (!outFile.getParentFile().exists()) {
                    outFile.getParentFile().mkdirs();
                }
                try (FileOutputStream outF = new FileOutputStream(outFile, false)) {
                    outF.write(dMsg.getData());
                    outF.flush();
                }
            } else {
                try (FileOutputStream outF = new FileOutputStream(outFile, true)) {
                    outF.write(dMsg.getData());
                    outF.flush();
                }
            }
            if (dMsg.getAllPart() == dMsg.getTpart()) {
                outFile.setLastModified(dMsg.getDateMod());
            }
        }

    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        ctx.close();
    }


}
