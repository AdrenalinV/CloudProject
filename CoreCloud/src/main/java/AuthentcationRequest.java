import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonAutoDetect
public class AuthentcationRequest extends Message{
    private String id;
    private String user;
    private String pass;
    private boolean stat=false;
}
