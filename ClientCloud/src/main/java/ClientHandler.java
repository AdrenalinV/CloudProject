
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;


public class ClientHandler extends SimpleChannelInboundHandler<DataSet> {
//    private FIleClient out=null;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DataSet ds) throws Exception {
        System.out.println("part: "+ds.getTpart());
//        if(ds.getTpart()==1){
//            out=new FIleClient("D:\\out_test"+File.separator+ds.getNameFile());
//            System.out.println("[Debug]: создать файл "+ds.getTpart());
//        }
//        out.getFs().write(ds.getData());
//        System.out.println("[Debug]: записать в файл");
//        if(ds.getTpart()== ds.getAllPart()){
//            out.close();
//            out=null;
//            System.out.println("[Debug]: закрыть файл");
//        }
    }
//    @Override
//    public void channelActive(ChannelHandlerContext ctx) throws Exception {
//        System.out.println("Add:Client");
//    }
//
//    @Override
//    protected void channelRead0(ChannelHandlerContext ctx, DataSet ds) throws Exception {
//        System.out.println("[Debug]: Чтение");
//
//        if(ds.getTpart()==1){
//            out=new FIleClient("D:\\out_test"+File.separator+ds.getNameFile());
//            System.out.println("[Debug]: создать файл "+ds.getTpart());
//        }
//        out.getFs().write(ds.getData());
//        System.out.println("[Debug]: записать в файл");
//        if(ds.getTpart()== ds.getAllPart()){
//            out.close();
//            out=null;
//            System.out.println("[Debug]: закрыть файл");
//        }
//        System.out.println("[Debug]: выход");
//
//    }
//
//
//
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
