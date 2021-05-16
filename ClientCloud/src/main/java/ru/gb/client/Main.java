package ru.gb.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;

public class Main extends Application {
    public static void main(String[] args) {
        Main.launch();
    }

    @Override
    public void start(Stage stage) throws Exception {
        MainControl mainControl = (MainControl) init(stage, "fxml/MainWindow.fxml");
        stage.show();
        mainControl.getPanel().setTabMaxHeight(45);
        mainControl.getPanel().setTabMaxHeight(45);
        mainControl.getLogInItem().setGraphic(new ImageView("img/logIN.png"));
        mainControl.getWorkItem().setGraphic(new ImageView("img/data.png"));
        stage.setOnCloseRequest(request -> {
            mainControl.quit();
        });
    }

    private Object init(Stage stage, String source) throws IOException {
        FXMLLoader fxmlloader = new FXMLLoader();
        try (InputStream inp = getClass().getClassLoader().getResourceAsStream(source)) {
            Parent root = fxmlloader.load(inp);
            Scene scene = new Scene(root);
            stage.setScene(scene);
            return fxmlloader.getController();
        }
    }

}
