package ru.gb.client;

import javafx.application.Platform;

import javafx.collections.ObservableList;
import javafx.scene.control.*;
import lombok.Getter;
import ru.gb.core.Command;

import javax.swing.*;
import java.io.File;

@Getter
public class MainControl {
    public ListView<String> localList;
    public ListView<String> serverList;
    public Tab workItem;
    public TabPane panel;
    public TextField statText;
    private Network con;
    public TextField user;
    public TextField pass;
    public CheckBox checkSetUp;
    public TextField host;
    public TextField port;
    public Button oK;
    public Button Exit;

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

    enum Condition {notAuth, okAuth}

    // поднимает коннект и пытается авторизоваться
    public void Auten() {
        if (host.getLength() > 0 && port.getLength() > 0 &&
                user.getLength() > 0 && pass.getLength() > 0) {
            con = Network.getInstance(this);
            System.out.println("[DEBUG] up connect to server");
            con.autent(user.getText(), pass.getText());
        }
    }

    // закрытие приложения
    public void quit() {
        if (con != null) {
            con.close();
            Platform.exit();
        }
    }

    // активирует настройки подключения
    public void setUP() {
        host.setDisable(!checkSetUp.isSelected());
        port.setDisable(!checkSetUp.isSelected());
    }

    // отключаем настройки входа.
    public void autOK(){
        oK.setDisable(true);
        user.setDisable(true);
        pass.setDisable(true);
        checkSetUp.setDisable(true);
        host.setDisable(true);
        port.setDisable(true);
        statText.setText("Добро пожаловать.");
        System.out.println("[DEBUG] disable btn ok");
    }
    public void addServerList(String[] fullName){
        serverList.getItems().clear();
        serverList.getItems().addAll(fullName);
        ObservableList<String> items = serverList.getItems();
        for (String item : items) {
            File file=new File(item);
            if (!file.exists()){
                Command cMsg=new Command("upload",item);
                con.getSocketChanel().writeAndFlush(cMsg);
                System.out.println("[DEBUG] command upload");
            }localList.getItems().add(item);
            panel.getSelectionModel().select(workItem);
        }

    }

}
