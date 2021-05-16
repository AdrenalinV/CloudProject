package ru.gb.client;

import io.netty.channel.socket.SocketChannel;
import javafx.application.Platform;

import javafx.collections.ObservableList;
import javafx.scene.control.*;
import lombok.Getter;
import ru.gb.core.*;

import javax.swing.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


@Getter
public class MainControl {
    private static final ExecutorService threadPull = Executors.newCachedThreadPool();

    private Network con;
    public ListView<String> localList;
    public ListView<String> serverList;
    public Tab logInItem;
    public Tab workItem;
    public TabPane panel;
    public TextField statText;
    public TextField user;
    public TextField pass;
    public CheckBox checkSetUp;
    public TextField host;
    public TextField port;
    public Button oK;
    public Button Exit;
    private final Runnable handler;
    private final ConcurrentLinkedQueue<TaskClient> myQueue = new ConcurrentLinkedQueue<>();

    public MainControl(){
        this.handler = () -> {
            byte[] buf = new byte[1024 * 1024];
            DataSet tmp;
            int size;
            String fullName;
            String fileName;
            long lastMod;
            int allPart;
            int tPart;
            System.out.println("[DEBUG] start task handler");
            while (!myQueue.isEmpty()) {
                TaskClient task = myQueue.poll();
                if (task.getType() == TypeTask.info) {
                    if (task.getWorkFile().exists()) {
                        Command cMsg = new Command(CommandType.getLastMod, task.getWorkFile().getPath());
                        task.getSocketChanel().writeAndFlush(cMsg);
                        System.out.println("[DEBUG] command getLastMod");
                    } else {
                        Command cMsg = new Command(CommandType.upload, task.getWorkFile().getPath());
                        task.getSocketChanel().writeAndFlush(cMsg);
                        System.out.println("[DEBUG] command upload");
                    }
                } else {
                    System.out.println("[DEBUG] dataset file:" + task.getWorkFile().getName());
                    tPart = 0;
                    allPart = (int) (task.getWorkFile().length() / (1024 * 1024)) + (task.getWorkFile().length() % (1024 * 1024) != 0 ? 1 : 0);
                    fullName = task.getWorkFile().getPath();
                    fileName = task.getWorkFile().getName();
                    lastMod = task.getWorkFile().lastModified();
                    System.out.println("[DEBUG] start upload file: " + fileName);
                    try (FileInputStream inF = new FileInputStream(task.getWorkFile())) {
                        while (inF.available() != 0) {
                            size = inF.read(buf);
                            tmp = new DataSet(fullName, fileName, lastMod, allPart, ++tPart, size, buf);
                            task.getSocketChanel().writeAndFlush(tmp).sync();
                        }
                        System.out.println("[DEBUG]  finish upload file: " + fileName);
                        Command cMsg=new Command(CommandType.userFiles,"");
                        task.getSocketChanel().writeAndFlush(cMsg).sync();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                }
            }
            System.out.println("[DEBUG] stop task handler");
        };
    }


    public void openDialog() {
        JFrame myFrame = new JFrame();
        myFrame.setBounds(0, 0, 500, 500);
        JFileChooser dialog = new JFileChooser();
        dialog.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        dialog.setApproveButtonText("Выбрать");
        dialog.setDialogTitle("Выберите папку для синхронизации.");
        dialog.setDialogType(JFileChooser.OPEN_DIALOG);
        dialog.setMultiSelectionEnabled(true);
        dialog.showOpenDialog(myFrame);
        myFrame.setVisible(true);
        File[] files = dialog.getSelectedFiles();
        myFrame.dispose();
        for (File file : files) {
            localList.getItems().addAll(file.getPath());
        }
    }


    public void clearFile() {
        localList.getItems().remove(localList.getSelectionModel().getSelectedItem());
    }

    public void setList() {
        System.out.println("[DEBUG] setList command");
        String list = null;
        ObservableList<String> items = localList.getItems();
        for (String item : items) {
            if (list == null) {
                list = item;
            } else {
                list += "," + item;
            }
        }
        System.out.println("[DEBUG] list " + list);
        Command cMsg = new Command(CommandType.setList, list);
        con.getSocketChanel().writeAndFlush(cMsg);
        synchroCloud();
    }

    public void addUser() {
        if (host.getLength() > 0 && port.getLength() > 0 &&
                user.getLength() > 0 && pass.getLength() > 0) {
            con = Network.getInstance(this);
            Command cMsg = new Command(CommandType.crUser, "");
            con.getSocketChanel().writeAndFlush(cMsg);
            AuthentcationRequest aMsg = new AuthentcationRequest();
            aMsg.setUser(user.getText());
            aMsg.setPass(pass.getText());
            con.getSocketChanel().writeAndFlush(aMsg);
            pass.clear();
        } else {
            setStatus("Внимание! Заполните логин и пароль!!! ");
        }
    }

    // поднимает коннект и пытается авторизоваться
    public void Auten() {
        if (host != null &&
                host.getLength() > 0 &&
                port != null &&
                port.getLength() > 0 &&
                user != null &&
                user.getLength() > 0 &&
                pass != null &&
                pass.getLength() > 0
        ) {
            con = Network.getInstance(this);
            System.out.println("[DEBUG] up connect to server");
            con.autent(user.getText(), pass.getText());
        }
    }

    // закрытие приложения
    public void quit() {
        if (con != null) {
            con.close();
            threadPull.shutdown();
        }
        Platform.exit();
    }

    // активирует настройки подключения
    public void setUP() {
        host.setDisable(!checkSetUp.isSelected());
        port.setDisable(!checkSetUp.isSelected());
    }

    // отключаем настройки входа.
    public void autOK() {
        oK.setDisable(true);
        user.setDisable(true);
        pass.setDisable(true);
        checkSetUp.setDisable(true);
        host.setDisable(true);
        port.setDisable(true);
        panel.getSelectionModel().select(workItem);
        logInItem.setDisable(true);
        System.out.println("[DEBUG] disable btn ok");
    }

    public void setLocalList(String[] fullName) {
        localList.getItems().clear();
        localList.getItems().addAll(fullName);

    }
    public void setServerList(String[] fullName){
        serverList.getItems().clear();
        serverList.getItems().addAll(fullName);
        synchroCloud();
    }

    public void setStatus(String message) {
        if (message == null) {
            statText.clear();
        } else {
            statText.setText(message);
        }
    }

    public void synchroCloud() {
        System.out.println("[DEBUD] start synchro Cloud");
        ArrayList<File> localFiles = new ArrayList<>();        // список локальных файлов
        ArrayList<File> serverFiles = new ArrayList<>();        // список локальных файлов
        ObservableList<String> items = localList.getItems();
        for (String item : items) {
            localFiles.addAll(LocalFIle.getFiles(item));
        }
        items = serverList.getItems();
        File tmp;
        for (String item : items) {
            serverFiles.add(tmp=new File(item));
            addTask(tmp, TypeTask.info,con.getSocketChanel());
            localFiles.remove(tmp);                      // удаляем файл который есть на сервере
        }
        for (File file : localFiles) {
            addTask(file, TypeTask.upload,con.getSocketChanel()); // задачи на загрузку файлов которых нет на сервере
        }
        threadPull.submit(handler);
    }

    public void addTask(File file, TypeTask type, SocketChannel socketChanel){
        myQueue.add(new TaskClient(file,type,socketChanel));
        threadPull.submit(handler);
    }


}
