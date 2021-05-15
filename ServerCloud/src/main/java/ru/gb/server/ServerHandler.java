package ru.gb.server;

import io.netty.channel.*;


import ru.gb.core.*;

import java.io.*;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;


public class ServerHandler extends SimpleChannelInboundHandler<Message> {
    private static final String OUT_DIR = "Cloud\\";
    private final ExecutorService threadPull;
    private final ConcurrentLinkedQueue<ItemTask> qTask = new ConcurrentLinkedQueue<>();
    private final BaseAuthService bas = BaseAuthService.of();
    private final BaseDataService bds = BaseDataService.of();
    private Runnable handlerQueue;
    private Condition cond = Condition.notAuth;
    private String userID = null;
    private Channel ch = null;


    enum Condition {notAuth, okAuth, newUser}

    public ServerHandler(ExecutorService threadPull) {
        this.threadPull = threadPull;
        File outDir=new File(OUT_DIR);
        if (!outDir.exists()){
            outDir.mkdir();
            System.out.println("[DEBUG] create out dir");
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        System.out.println("error channel");
        cause.printStackTrace();
        ctx.close();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        ch = ctx.channel();
        // Обработчик задач на отправку
        handlerQueue = (() -> {
            byte[] buf = new byte[1024 * 1024];
            int size;
            int allPart;
            int tPart;
            long lastMod;
            String fullName;
            String fileName;
            DataSet tmp;
            System.out.println("start task handler");
            while (!qTask.isEmpty()) {
                ItemTask t = qTask.poll();
                File fin = new File(OUT_DIR + userID + "\\" + t.getFileID());
                tPart = 0;
                allPart = (int) (fin.length() / (1024 * 1024)) + (fin.length() % (1024 * 1024) != 0 ? 1 : 0);
                fullName = bds.getFullName(t.getFileID());
                fileName = bds.getFileName(t.getFileID());
                lastMod = Long.parseLong(bds.getLastMod(t.getFileID()));
                System.out.println("start send client file: " + fileName);
                try (FileInputStream inF = new FileInputStream(fin)) {
                    while (inF.available() != 0) {
                        size = inF.read(buf);
                        tmp = new DataSet(fullName, fileName, lastMod, allPart, ++tPart, size, buf);
                        t.getCh().writeAndFlush(tmp).sync();
                    }
                    System.out.println("start send client file: " + fileName);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
            System.out.println("stop task handler");
        });

    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) throws Exception {
        // аутентификация пользователя
        if (msg instanceof AuthentcationRequest && cond == Condition.notAuth) {
            AuthentcationRequest aMsg = (AuthentcationRequest) msg;
            this.userID = bas.getAuthByLoginPass(aMsg.getUser(), aMsg.getPass());
            aMsg.setStat(this.userID != null);
            aMsg.setId(this.userID);
            ChannelFuture f = ch.writeAndFlush(aMsg);
            f.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) {
                    assert f == future;
                }
            });
            if (this.userID != null) {
                cond = Condition.okAuth;
                System.out.println("authentication completed User: "+aMsg.getUser());
            } else {
                cond = Condition.notAuth;
                System.out.println("authentication error User: "+aMsg.getUser()+" Password: "+aMsg.getPass());
            }
        }
        // Добавляем пользователя
        if (msg instanceof AuthentcationRequest && cond == Condition.newUser) {
            AuthentcationRequest aMsg = (AuthentcationRequest) msg;
            Answer ans = new Answer();
            ans.setCommandName(CommandType.crUser);
            if (aMsg.getUser() != null && aMsg.getPass() != null) {
                if (!bas.existUser(aMsg.getUser())) {
                    bas.createUser(aMsg.getUser(), aMsg.getPass());
                    ans.setSuccess(true);
                    ans.setMessage("Successfully");
                } else {
                    ans.setMessage("The user already exists!");
                }
            } else {
                ans.setMessage("The user name and password must be filled in!");
            }
            ChannelFuture f = ch.writeAndFlush(ans);
            f.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) {
                    assert f == future;
                }
            });
            cond = Condition.notAuth;

        }
        // Пришли данные от пользователя.
        if (msg instanceof DataSet) {
            if (cond == Condition.okAuth) {
                DataSet dMsg = (DataSet) msg;
                System.out.println("received dataSet " + dMsg.getNameFile());
                String fileID = bds.getUserFile(userID, dMsg.getPathFile()); // получаем файл ID
                // загружаем файл и он есть в БД
                if (dMsg.getTpart() == 1 && fileID != null) {
                    bds.setLastMod(dMsg.getDateMod(), fileID);
                    // загружаем файл но его нет в бд
                } else if (dMsg.getTpart() == 1 && fileID == null) {
                    bds.uploadFile(userID, dMsg);
                    fileID = bds.getUserFile(userID, dMsg.getPathFile());
                    // проверяем есть ли каталог загрузки
                    File tmp=new File(OUT_DIR + userID);
                    if (!tmp.exists()){
                        tmp.mkdirs();
                    }
                }
                try (FileOutputStream outF = new FileOutputStream(OUT_DIR + userID + "\\" + fileID, true)) {
                    outF.write(dMsg.getData());
                    outF.flush();
                }
            }
        }
        // пришла команда
        if (msg instanceof Command) {
            Command cMsg = (Command) msg;
            System.out.println(("received command : " + cMsg.getCommandName() + " Значение : " + cMsg.getValue()));
            switch (cMsg.getCommandName()) {
                case crUser:  // создать пользователя
                    if (cond == Condition.notAuth) {
                        cond = Condition.newUser;
                        System.out.println("handler crUser");
                    }
                    break;
                case upload: // отправить файл клиенту
                    if (cond == Condition.okAuth) {
                        if (cMsg.getValue() != null) {
                            String fileId = bds.getUserFile(userID, cMsg.getValue());
                            qTask.add(new ItemTask(ch, fileId));
                            threadPull.submit(handlerQueue);
                            Answer ans = new Answer(true, "task add in queue", CommandType.upload);
                            ctx.writeAndFlush(ans);
                            System.out.println("handler upload");
                        }
                    }
                    break;
                case getList: // список файлов на клиенте
                    if (cond == Condition.okAuth) {
                        Answer answer = new Answer();
                        answer.setCommandName(CommandType.getList);
                        ArrayList<String> userFiles = bds.getUserPath(userID);
                        for (String userFile : userFiles) {
                            if (answer.getMessage() == null) {
                                answer.setMessage(userFile);
                            } else {
                                answer.setMessage(answer.getMessage() + "," + userFile);
                            }
                        }
                        answer.setSuccess(true);
                        ctx.writeAndFlush(answer);
                        System.out.println("handler getList");
                    }
                    break;
                case setList: // список файлов на клиенте
                    if (cond == Condition.okAuth) {
                        Answer answer = new Answer();
                        answer.setCommandName(CommandType.setList);
                        bds.setUserPath(cMsg.getValue(), userID);
                        answer.setSuccess(true);
                        ctx.writeAndFlush(answer);
                        System.out.println("handler setList");
                    }
                    break;
                case userFiles:
                    if (cond == Condition.okAuth) {
                        Answer answer = new Answer();
                        answer.setCommandName(CommandType.userFiles);
                        ArrayList<String> userFiles = bds.getUserFiles(userID);
                        for (String userFile : userFiles) {
                            if (answer.getMessage() == null) {
                                answer.setMessage(userFile);
                            } else {
                                answer.setMessage(answer.getMessage() + "," + userFile);
                            }
                        }
                        answer.setSuccess(true);
                        ctx.writeAndFlush(answer);
                        System.out.println("handler userFiles");
                    }
                    break;
                case getLastMod: // запрос последней модификации файла
                    if (cond == Condition.okAuth) {
                        long result = 0L;
                        Answer ans = new Answer();
                        ans.setCommandName(CommandType.getLastMod);
                        if (cMsg.getValue() != null) {
                            result = bds.getLastModTime(userID, cMsg.getValue());
                            ans.setCommandValue(cMsg.getValue());
                            ans.setSuccess(true);
                        }
                        ans.setMessage(String.valueOf(result));
                        ctx.writeAndFlush(ans);
                        System.out.println("handler getLastMod");
                    }
                    break;
                case clear: // удалить удаленные файла пользователя
                    if (cond == Condition.okAuth) {
                        //TODO server clear
                        System.out.println("handler clear");
                    }
                    break;
                case delete: // удалить файла пользователя
                    if (cond == Condition.okAuth) {
                        //TODO server delete.
                        System.out.println("handler delete");
                    }
                    break;
                case undelete: // востановить удаленный файла пользователя
                    if (cond == Condition.okAuth) {
                        //TODO server undelete
                        System.out.println("handler undelete");
                    }
                    break;
            }

        }
    }

}
