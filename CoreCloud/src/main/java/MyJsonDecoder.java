import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;

import java.util.List;

public class MyJsonDecoder extends MessageToMessageDecoder<String> {
    private ObjectMapper mapper=null;
    protected MyJsonDecoder() {
        this.mapper=new ObjectMapper();
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, String s, List<Object> list) throws Exception {
        list.add(mapper.readValue(s,Message.class));
    }
}
