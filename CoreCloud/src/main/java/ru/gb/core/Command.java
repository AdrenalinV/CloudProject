package ru.gb.core;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.Getter;
import lombok.Setter;

@JsonAutoDetect
@Getter
@Setter
public class Command extends Message {
    private String commandName;
    private String value;

    public  Command(){};

    public Command(String commandName, String value) {
        this.commandName = commandName;
        this.value = value;
    }
}
