package ru.gb.client;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import lombok.extern.log4j.Log4j2;
import ru.gb.core.*;

import java.io.*;
@Log4j2
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
            log.debug("command: {} value: {} message: {}",
                    ans.getCommandName().toString(),
                    ans.getCommandValue(),
                    ans.getMessage());
            switch (ans.getCommandName()) {
                case getList:
                    log.debug("getList command");;
                    if (ans.getMessage() != null) {
                        mControl.setLocalList(ans.getMessage().split(","));
                    }
                    break;
                case delete:
                case userFiles:
                    mControl.setSync(false);
                    log.debug("userFiles or delete command");
                    if (ans.getMessage() != null) {
                        String[] fil = ans.getMessage().split(",");
                        mControl.updateFileList(fil);
                    }else {
                        mControl.updateFileList(null);
                    }
                    break;
                case undelete:
                case userDelFiles:
                    log.debug("userDelFiles or undelete command");
                    mControl.setSync(true);
                    if (ans.getMessage()!=null){
                        String[] fil = ans.getMessage().split(",");
                        mControl.updateFileList(fil);
                    }else{
                        mControl.updateFileList(null);
                    }
                    break;
                case getLastMod: // запрос даты последней модификации
                    log.debug("getLastMod command");
                    long sTime = Long.parseLong(ans.getMessage());
                    File file = new File(ans.getCommandValue());
                    if (file.lastModified() < sTime) {
                        log.debug("command upload file: {}" , file.getPath());
                        Command cMsg = new Command(CommandType.upload, file.getPath());
                        ctx.writeAndFlush(cMsg);
                    } else if (file.lastModified() > sTime && sTime != 0L) {
                        log.debug("add task upload file: {}",file.getPath());
                        mControl.addTask(file, TypeTask.upload, mControl.getCon().getSocketChanel());
                    } else if (sTime == 0L) {
                        log.debug("delete file: {}",file.getPath());
                        file.deleteOnExit(); // удаляем т.к. нет на сервере
                    }


                    break;
                case clear:
                    //TODO client clear
                    log.debug("getLastMod clear");
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
            log.debug("received dataset: {}  part: {}/{} ",
                    dMsg.getNameFile() ,
                    dMsg.getTpart() ,
                    dMsg.getAllPart());
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
