package ru.gb.server;

import io.netty.channel.Channel;


public class ItemTask {
    private final Channel ch;
    private final String fileID;

    public ItemTask(Channel ch, String fileID) {
        this.ch = ch;
        this.fileID = fileID;
    }

    public Channel getCh() {
        return ch;
    }

    public String getFileID() {
        return fileID;
    }
}
