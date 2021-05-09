package ru.gb.core;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonAutoDetect
public class Answer extends Message{

    public Answer(){};
    public Answer(boolean success, String message, String commandName) {
        this.success = success;
        Message = message;
        this.commandName=commandName;
    }
    private String commandName;
    private boolean success=false;
    private String Message;
}
