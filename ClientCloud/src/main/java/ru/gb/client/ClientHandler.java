package ru.gb.client;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import ru.gb.core.*;

import java.io.*;
import java.util.concurrent.ConcurrentLinkedQueue;


public class ClientHandler extends SimpleChannelInboundHandler<Message> {
    private Thread t;
    private MainControl mControl;
    private ConcurrentLinkedQueue<TaskClient> myQueue=new ConcurrentLinkedQueue<>();
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
                mControl.setStatus("Вход выполнен.");
                Command cMsg = new Command(CommandType.getList, "");
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
                        mControl.addServerList(ans.getMessage().split(","));
                    }
                    break;
                case userFiles:
                    System.out.println("[DEBUG] userFiles command");
                    System.out.println("[DEBUG] " + ans.getMessage());
                    if (ans.getMessage() != null) {
                        String[] fil=ans.getMessage().split(",");
                        File tmp;
                        for (String s : fil) {
                            tmp=new File(s);
                            myQueue.add(new TaskClient(tmp,TypeTask.info));
                        }
                        if(!t.isAlive()){
                            this.t.start();
                        }
                    }
                    break;
                case getLastMod: // запрос даты последней модификации
                    System.out.println("[DEBUG] getLastMod command");
                    long sTime=Long.parseLong(ans.getMessage());
                    File file = new File(ans.getCommandValue());
                    if (file.lastModified()<sTime){
                        Command cMsg=new Command(CommandType.upload,file.getPath());
                        ctx.writeAndFlush(cMsg);
                    }else if (file.lastModified()>sTime && sTime!=-1l){
                        System.out.println("[DEBUG] upload Task add");
                        myQueue.add(new TaskClient(file,TypeTask.upload));
                        if(!t.isAlive()){
                            this.t.start();
                        }

                    }else if(sTime==-1l){
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
            if (dMsg.getTpart()==1){
                try (FileOutputStream outF = new FileOutputStream(outFile, false)) {
                    outF.write(dMsg.getData());
                    outF.flush();
                }
            }else{
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
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        t=new Thread(()->{
            byte[] buf = new byte[1024 * 1024];
            DataSet tmp;
            int size;
            String fullName;
            String fileName;
            long lastMod;
            int allPart;
            int tPart;
            System.out.println("[DEBUG] start task handler");
            while (!myQueue.isEmpty()){
                TaskClient task=myQueue.poll();
                if (task.getType()==TypeTask.info){
                    if(task.getWorkFile().exists()){
                        Command cMsg=new Command(CommandType.getLastMod,task.getWorkFile().getPath());
                        ctx.writeAndFlush(cMsg);
                        System.out.println("[DEBUG] command getLastMod");
                    }else{
                        Command cMsg=new Command(CommandType.upload,task.getWorkFile().getPath());
                        ctx.writeAndFlush(cMsg);
                        System.out.println("[DEBUG] command upload");
                    }
                }else{
                    System.out.println("[DEBUG] dataset file:"+task.getWorkFile().getName());
                    tPart = 0;
                    allPart = (int) (task.getWorkFile().length() / (1024 * 1024)) + (task.getWorkFile().length() % (1024 * 1024) != 0 ? 1 : 0);
                    fullName = task.getWorkFile().getPath();
                    fileName = task.getWorkFile().getName();
                    lastMod = task.getWorkFile().lastModified();
                    System.out.println("[DEBUG] отправляем файл: " + fileName);
                    try (FileInputStream inF = new FileInputStream(task.getWorkFile())) {
                        while (inF.available() != 0) {
                            size = inF.read(buf);
                            tmp = new DataSet(fullName, fileName, lastMod, allPart, ++tPart, size, buf);
                            ctx.writeAndFlush(tmp).sync();
                        }
                        System.out.println("[DEBUG]  отправлен файл: " + fileName);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            System.out.println("[DEBUG] stop task handler");
        });

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
    }
}
