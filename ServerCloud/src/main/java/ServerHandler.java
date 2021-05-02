

import io.netty.channel.*;

import java.io.*;
import java.util.LinkedList;
import java.util.Queue;


public class ServerHandler extends SimpleChannelInboundHandler<Message> {

    private Thread handlerQueue;
    private Queue<ItemTask> qTask = new LinkedList<>();
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

    ;

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ch = ctx.channel();
        // Обработчик задач на отправку
        this.handlerQueue = new Thread(new Runnable (){
            @Override
            public void run() {
                byte[] buf = new byte[1024 * 1024];
                DataSet tmp;
                int size;
                String fullName;
                String fileName;
                long lastMod;
                int allPart;
                int tPart;
                while (true) {
                    if (!qTask.isEmpty()) {
                        System.out.println("[DEBUG] отправляем файл");
                        ItemTask t = qTask.poll();
                        File fin = new File(OUT_DIR + userID + "\\" + t.getFileID());

                        tPart = 0;
                        allPart = (int) (fin.length() / (1024 * 1024)) + (fin.length() % (1024 * 1024) != 0 ? 1 : 0);
                        fullName = bds.getFullName(t.getFileID());
                        fileName = bds.getFileName(t.getFileID());
                        lastMod = Long.parseLong(bds.getLastMod(t.getFileID()));
                        System.out.println("[DEBUG] отправляем файл");
                        try (FileInputStream inF = new FileInputStream(fin)) {
                            while (inF.available() != 0) {
                                size = inF.read(buf);
                                tmp = new DataSet(fullName, fileName, lastMod, allPart, ++tPart, size, buf);
                                ChannelFuture f = t.getCh().writeAndFlush(tmp);
                                f.addListener(new ChannelFutureListener() {
                                    @Override
                                    public void operationComplete(ChannelFuture future) throws Exception {
                                        assert f == future;
                                    }
                                });
                            }
                            System.out.println("[DEBUG]  файл отправлен");
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException ioException) {
                            ioException.printStackTrace();
                        }
                    }
                }
            }
        });
        this.handlerQueue.start();
        System.out.println("[DEBUG] Стартовал обработчик задач!");

    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("[DEBUG] ОСТАНОВКА ПОТОКА!!!");
        handlerQueue.interrupt();
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
            }
        }
        // Добавляем пользователя
        if (msg instanceof AuthentcationRequest && cond == Condition.newUser) {
            AuthentcationRequest aMsg = (AuthentcationRequest) msg;
            Answer answ = new Answer();
            if (aMsg.getUser() != null && aMsg.getPass() != null) {
                if (!bas.existUser(aMsg.getUser())) {
                    bas.createUser(aMsg.getUser(), aMsg.getPass());
                    answ.setSuccess(true);
                    answ.setMessage("Successfully");
                } else {
                    answ.setMessage("The user already exists!");
                }
            } else {
                answ.setMessage("The user name and password must be filled in!");
            }
            ChannelFuture f = ch.writeAndFlush(answ);
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
                if (dMsg.getTpart() == 1) {
                    bds.uploadFile(userID, dMsg);
                }
                String fileID = bds.getUserFile(userID, dMsg.getPathFile());
                try (FileOutputStream outF = new FileOutputStream(OUT_DIR + userID + "\\" + fileID, true)) {
                    outF.write(dMsg.getData());
                    outF.flush();
                }
            }
        }
        // пришла команда
        if (msg instanceof Command) {
            Command cMsg = (Command) msg;
            System.out.println("[DEBUG] Пришла команда: " + cMsg.getCommandName()+ " Значение : "+ cMsg.getValue());
            switch (cMsg.getCommandName()) {
                case "crUser":
                    if (cond == Condition.notAuth) {
                        cond = Condition.newUser;
                    }
                    break;
                case "upload":
                    if (cond == Condition.okAuth) {
                        if (cMsg.getValue() != null) {
                            String fileId = bds.getUserFile(userID, cMsg.getValue());
                            System.out.println("[DEBUG] отправляем файл");
                            File fin = new File(OUT_DIR + userID + "\\" + fileId);

                            byte[] buf = new byte[1024 * 1024];
                            DataSet tmp;
                            int size;
                            String fullName;
                            String fileName;
                            long lastMod;
                            int allPart;
                            int tPart;
                            tPart = 0;
                            allPart = (int) (fin.length() / (1024 * 1024)) + (fin.length() % (1024 * 1024) != 0 ? 1 : 0);
                            fullName = bds.getFullName(fileId);
                            fileName = bds.getFileName(fileId);
                            lastMod = Long.parseLong(bds.getLastMod(fileId));
                            System.out.println("[DEBUG] отправляем файл");
                            try (FileInputStream inF = new FileInputStream(fin)) {
                                while (inF.available() != 0) {
                                    size = inF.read(buf);
                                    tmp = new DataSet(fullName, fileName, lastMod, allPart, ++tPart, size, buf);
                                    ChannelFuture f = ch.writeAndFlush(tmp);
                                    f.addListener(new ChannelFutureListener() {
                                        @Override
                                        public void operationComplete(ChannelFuture future) throws Exception {
                                            assert f == future;
                                        }
                                    });
                                }
                            }
//                            qTask.add(new ItemTask(ch, fileId));
                            Answer ans = new Answer(true, "Задача поставлена в очередь.");
                            ctx.writeAndFlush(ans);
                        }
                    }
                    break;
                case "list":
                    if (cond == Condition.okAuth) {
                        //TODO
                    }
                    break;
                case "clear":
                    if (cond == Condition.okAuth) {
                        //TODO
                    }
                    break;
            }

        }
    }
}
