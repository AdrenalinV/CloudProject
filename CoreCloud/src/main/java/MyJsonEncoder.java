import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;

import java.io.StringWriter;
import java.util.List;

public class MyJsonEncoder extends MessageToMessageEncoder<Message> {
    private final ObjectMapper mapper;
    private final StringWriter writer;

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Message msg, List<Object> list) throws Exception {
        mapper.writeValue(writer,msg);
        list.add(writer.toString());
        writer.getBuffer().setLength(0);

    }

    protected MyJsonEncoder() {
        this.mapper=new ObjectMapper();
        this.writer= new StringWriter();
    }


}
