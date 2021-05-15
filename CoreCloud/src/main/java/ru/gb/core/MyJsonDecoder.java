package ru.gb.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;

import java.util.List;

public class MyJsonDecoder extends MessageToMessageDecoder<String> {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public void decode(ChannelHandlerContext ctx, String s, List<Object> list) throws Exception {
        list.add(MAPPER.readValue(s, Message.class));
    }
}
