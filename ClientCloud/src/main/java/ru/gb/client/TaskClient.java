package ru.gb.client;


import io.netty.channel.socket.SocketChannel;
import lombok.Getter;

import java.io.File;

@Getter
public class TaskClient {
    private final SocketChannel socketChanel;
    private final File workFile;
    private final TypeTask type;


    public TaskClient(File workFile, TypeTask type, SocketChannel socketChannel) {
        this.workFile = workFile;
        this.type = type;
        this.socketChanel = socketChannel;
    }
}
