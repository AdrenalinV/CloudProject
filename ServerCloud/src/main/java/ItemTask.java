import io.netty.channel.Channel;
import lombok.Getter;


public class ItemTask {
    private  Channel ch;
    private  String fileID;
    public ItemTask(Channel ch, String fileID){
        this.ch=ch;
        this.fileID=fileID;
    }

    public  Channel getCh() {
        return ch;
    }

    public  String getFileID() {
        return fileID;
    }
}
