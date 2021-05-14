package ru.gb.client;


import lombok.Getter;

import java.io.File;
@Getter
public class TaskClient {
    private final File workFile;
    private final TypeTask type;


    public TaskClient(File workFile, TypeTask type) {
        this.workFile = workFile;
        this.type = type;
    }
}
