package ru.gb.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;

import java.util.List;

public class MyJsonEncoder extends MessageToMessageEncoder<Message> {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public void encode(ChannelHandlerContext channelHandlerContext, Message msg, List<Object> list) throws Exception {
        list.add(MAPPER.writeValueAsString(msg));
    }
}
