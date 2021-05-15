package ru.gb.core;

import lombok.Getter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

@Getter
public class LocalFIle {
    private final FileOutputStream fs;
    private final String outPath;

    public LocalFIle(String outPath) throws FileNotFoundException {
        if (outPath != null) {
            this.outPath = outPath;
            this.fs = new FileOutputStream(this.outPath);
        } else {
            throw new IllegalArgumentException();
        }
    }

    public void close() throws IOException {
        fs.close();
    }

    public static ArrayList<File> getFiles(String path) {
        ArrayList<File> files = new ArrayList<>();
        File tmp;
        tmp = new File(path);
        if (tmp.isDirectory()) {
            File nestTmp;
            ArrayList<String> strFiles = new ArrayList<>();
            if (tmp.list()!=null){
                Collections.addAll(strFiles, tmp.list());
                for (String strFile : strFiles) {
                    nestTmp = (new File(tmp.getPath(), strFile));
                    if (nestTmp.isDirectory()) {
                        files.addAll(getFiles(nestTmp.getPath()));
                    } else {
                        files.add(nestTmp);
                    }
                }
            }
            return files;
        }
        files.add(tmp);
        return files;
    }
}
