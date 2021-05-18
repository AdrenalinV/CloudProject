package ru.gb.core;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.lang.annotation.Target;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = AuthentcationRequest.class, name = "authentcation"),
        @JsonSubTypes.Type(value = DataSet.class, name = "dataSet"),
        @JsonSubTypes.Type(value = Command.class, name = "cmd"),
        @JsonSubTypes.Type(value = Answer.class, name = "answ")
})
public abstract class Message {

}
