import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonAutoDetect
public class Answer extends Message{

    public Answer(){};
    public Answer(boolean success, String message) {
        this.success = success;
        Message = message;
    }

    private boolean success=false;
    private String Message;
}
