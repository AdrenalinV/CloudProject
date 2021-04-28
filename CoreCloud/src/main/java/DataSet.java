
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.Data;

import java.util.Arrays;

@JsonAutoDetect
@Data
public class DataSet extends Message {
    String pathFile;
    String nameFile;
    long dateMod;
    int allPart;
    int tpart;
    int size;
    byte[] data;

    public DataSet(String pathFile, String nameFile, long dateMod, int allPart, int tpart, int size, byte[] data) {
        this.pathFile = pathFile;
        this.nameFile = nameFile;
        this.dateMod = dateMod;
        this.allPart = allPart;
        this.tpart = tpart;
        this.size = size;
        this.data = Arrays.copyOf(data, size);
    }
    public DataSet(){};
}
