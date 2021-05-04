import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import lombok.Getter;

@Getter
public class MainControl {

    public TextField user;
    public TextField pass;
    public CheckBox checkSetUp;
    public TextField host;
    public TextField port;
    public Button oK;
    public Button Exit;

    // поднимает коннект и пытается авторизоваться
    public void Auten(ActionEvent actionEvent) throws InterruptedException {
        if (host.getLength() > 0 && port.getLength() > 0 &&
                user.getLength() > 0 && pass.getLength() > 0) {
            ClientCloud con = ClientCloud.getInstance(host.getText(), Integer.parseInt(port.getText()));
            System.out.println("[DEBUG] up connect to server");
 //           con.autent("Vadim","V1570");
        }


    }

    // закрытие приложения
    public void exit(ActionEvent actionEvent) {
    }

    // активирует настройки подключения
    public void setUP(ActionEvent actionEvent) {
        host.setDisable(!checkSetUp.isSelected());
        port.setDisable(!checkSetUp.isSelected());
    }
}
