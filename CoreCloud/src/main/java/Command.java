import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.Getter;
import lombok.Setter;
@JsonAutoDetect
@Getter
@Setter
public class Command extends Message{
    private String commandName;
    public Command(String commandName) {
        this.commandName=commandName;
    }
}
