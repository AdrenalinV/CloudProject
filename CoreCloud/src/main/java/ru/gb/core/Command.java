package ru.gb.core;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.Getter;
import lombok.Setter;

@JsonAutoDetect
@Getter
@Setter
public class Command extends Message {
    private CommandType commandName;
    private String value;

    public Command(CommandType commandName, String value) {
        this.commandName = commandName;
        this.value = value;
    }
}
