package ru.gb.core;
import lombok.Getter;

import javax.management.BadAttributeValueExpException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
@Getter
public class LocalFIle {
    FileOutputStream fs=null;
    String outPath;

    public LocalFIle(String outPath) throws FileNotFoundException {
        if (outPath!=null){
            this.outPath = outPath;
            this.fs=new FileOutputStream(this.outPath);
        }else{
            throw new IllegalArgumentException();
        }
    }

    public void close() throws IOException {
        if(fs!=null){
            fs.close();
        }
    }

    public static ArrayList<File> getFiles(String path) {
        ArrayList<File> files = new ArrayList<>();
        File tmp;
        tmp = new File(path);
        if (tmp.isDirectory()) {
            File nestTmp;
            ArrayList<String> strFiles = new ArrayList<>();
            strFiles.addAll(Arrays.asList(tmp.list()));
            for (String strFile : strFiles) {
                nestTmp = (new File(tmp.getPath(), strFile));
                if (nestTmp.isDirectory()) {
                    files.addAll(getFiles(nestTmp.getPath()));
                } else {
                    files.add(nestTmp);
                }
                nestTmp = null;
            }
            return files;
        }
        files.add(tmp);
        return files;
    }
}
