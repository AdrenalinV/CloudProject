import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;

public class Main extends Application {
    public static void main(String[] args)  throws InterruptedException {
        Main.launch();
       // new ClientCloud("localhost",8999).run();
    }

    @Override
    public void start(Stage stage) throws Exception {
        MainControl mainControl=(MainControl) init(stage, "fxml/MainWindow.fxml");
//        oneControl.getFxBtnIn().setOnMouseClicked(event -> autentStage.show());
        stage.show();
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
