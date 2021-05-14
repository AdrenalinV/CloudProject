package ru.gb.server;

import io.netty.channel.*;
import ru.gb.core.*;

import java.io.*;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;


public class ServerHandler extends SimpleChannelInboundHandler<Message> {

    private Thread handlerQueue;
    private ConcurrentLinkedQueue<ItemTask> qTask = new ConcurrentLinkedQueue<>();
    private static final String OUT_DIR = "C:\\Cloud\\";
    private Condition cond = Condition.notAuth;
    private String userID = null;
    private Channel ch = null;
    private BaseAuthService bas = BaseAuthService.of();
    private BaseDataService bds = BaseDataService.of();
    private byte[] buf = new byte[1024 * 1024];
    private DataSet tmp = null;
    private FileInputStream in = null;

    enum Condition {notAuth, okAuth, newUser}


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.out.println("[DEBUG] ОСТАНОВКА ПОТОКА!!!");
        handlerQueue.interrupt();
        ctx.close();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ch = ctx.channel();
        // Обработчик задач на отправку
        handlerQueue = new Thread(new Runnable() {
            @Override
            public void run() {
                queueHandler();
            }
        });

    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("[DEBUG] Inactive");
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
                public void operationComplete(ChannelFuture future) throws Exception {
                    assert f == future;
                }
            });
            if (this.userID != null) {
                cond = Condition.okAuth;
            } else {
                cond = Condition.notAuth;
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
                public void operationComplete(ChannelFuture future) throws Exception {
                    assert f == future;
                }
            });
            cond = Condition.notAuth;

        }
        // Пришли данные от пользователя.
        if (msg instanceof DataSet) {
            if (cond == Condition.okAuth) {
                DataSet dMsg = (DataSet) msg;
                String fileID = bds.getUserFile(userID, dMsg.getPathFile());
                if (dMsg.getTpart() == 1 && fileID ==null) {
                    bds.uploadFile(userID, dMsg);
                    fileID = bds.getUserFile(userID, dMsg.getPathFile());
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
            System.out.println("[DEBUG] Пришла команда: " + cMsg.getCommandName() + " Значение : " + cMsg.getValue());
            switch (cMsg.getCommandName()) {
                case crUser:  // создать пользователя
                    if (cond == Condition.notAuth) {
                        cond = Condition.newUser;
                        System.out.println("[DEBUG] setStat newUser");
                    }
                    break;
                case upload: // отправить файл клиенту
                    if (cond == Condition.okAuth) {
                        if (cMsg.getValue() != null) {
                            String fileId = bds.getUserFile(userID, cMsg.getValue());
//                            System.out.println("[DEBUG] отправляем файл");
//                            File fin = new File(OUT_DIR + userID + "\\" + fileId);
//
//                            byte[] buf = new byte[1024 * 1024];
//                            DataSet tmp;
//                            int size;
//                            String fullName;
//                            String fileName;
//                            long lastMod;
//                            int allPart;
//                            int tPart;
//                            tPart = 0;
//                            allPart = (int) (fin.length() / (1024 * 1024)) + (fin.length() % (1024 * 1024) != 0 ? 1 : 0);
//                            fullName = bds.getFullName(fileId);
//                            fileName = bds.getFileName(fileId);
//                            lastMod = Long.parseLong(bds.getLastMod(fileId));
//                            System.out.println("[DEBUG] отправляем файл");
//                            try (FileInputStream inF = new FileInputStream(fin)) {
//                                while (inF.available() != 0) {
//                                    size = inF.read(buf);
//                                    tmp = new DataSet(fullName, fileName, lastMod, allPart, ++tPart, size, buf);
//                                    ChannelFuture f = ch.writeAndFlush(tmp);
//                                    f.addListener(new ChannelFutureListener() {
//                                        @Override
//                                        public void operationComplete(ChannelFuture future) throws Exception {
//                                            assert f == future;
//                                        }
//                                    });
//                                }
//                            }
                            qTask.add(new ItemTask(ch, fileId));
                            Answer ans = new Answer(true, "task add in queue", CommandType.upload);
                            ctx.writeAndFlush(ans);
                            System.out.println("[DEBUG] command upload answer");
                            if (!handlerQueue.isAlive()) {
                                handlerQueue.start();
                            }
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
                        System.out.println("[DEBUG] command getList answer");
                    }
                    break;
                case setList: // список файлов на клиенте
                    if (cond == Condition.okAuth) {
                        Answer answer = new Answer();
                        answer.setCommandName(CommandType.setList);
                        bds.setUserPath(cMsg.getValue(), userID);
                        answer.setSuccess(true);
                        ctx.writeAndFlush(answer);
                        System.out.println("[DEBUG] command setList answer");
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
                        System.out.println("[DEBUG] command getList answer");
                    }
                    break;
                case getLastMod: // запрос последней модификации файла
                    if (cond == Condition.okAuth) {
                        long result=-1l;
                        Answer ans=new Answer();
                        ans.setCommandName(CommandType.getLastMod);
                        if (cMsg.getValue()!=null){
                            result= bds.getLastModTime(userID,cMsg.getValue());
                            ans.setCommandValue(cMsg.getValue());
                            ans.setSuccess(true);
                        }
                        ans.setMessage(String.valueOf(result));
                        ctx.writeAndFlush(ans);
                        System.out.println("[DEBUG] command getLastMod answer");
                    }
                    break;
                case clear: // удалить удаленные файла пользователя
                    if (cond == Condition.okAuth) {
                        //TODO server clear
                    }
                    break;
                case delete: // удалить файла пользователя
                    if (cond == Condition.okAuth) {
                        //TODO server delete.
                    }
                    break;
                case undelete: // востановить удаленный файла пользователя
                    if (cond == Condition.okAuth) {
                        //TODO server undelete
                    }
                    break;
            }

        }
    }

    private void queueHandler() {
        byte[] buf = new byte[1024 * 1024];
        DataSet tmp;
        int size;
        String fullName;
        String fileName;
        long lastMod;
        int allPart;
        int tPart;
        System.out.println("[DEBUG] start task handler");
        while (!qTask.isEmpty()) {
            ItemTask t = qTask.poll();
            File fin = new File(OUT_DIR + userID + "\\" + t.getFileID());
            tPart = 0;
            allPart = (int) (fin.length() / (1024 * 1024)) + (fin.length() % (1024 * 1024) != 0 ? 1 : 0);
            fullName = bds.getFullName(t.getFileID());
            fileName = bds.getFileName(t.getFileID());
            lastMod = Long.parseLong(bds.getLastMod(t.getFileID()));
            System.out.println("[DEBUG] отправляем файл: " + fileName);
            try (FileInputStream inF = new FileInputStream(fin)) {
                while (inF.available() != 0) {
                    size = inF.read(buf);
                    tmp = new DataSet(fullName, fileName, lastMod, allPart, ++tPart, size, buf);
                    t.getCh().writeAndFlush(tmp).sync();
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
        System.out.println("[DEBUG] stop task handler");
    }


}
