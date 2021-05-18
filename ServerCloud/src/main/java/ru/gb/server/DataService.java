package ru.gb.server;
import ru.gb.core.DataSet;

import java.util.ArrayList;

public interface DataService {

    ArrayList<String> getUserFiles(String userID);
    ArrayList<String> getDelUserFiles(String userID);
    String getUserFile(String userID, String fileName);
    void uploadFile(String userId, DataSet ds);
    long getLastModTime(String userID,String fullName);
    void delFile(String userID,String fullName);
    String getFullName( String fileID);
    String getFileName( String fileID);
    String getLastMod( String fileID);
    void setLastMod(long lastMod, String fileID);
    ArrayList<String> getUserPath(String userID);
    void setUserPath(String listPaths, String userID);
    void setDelDate(String fullName, String userID);
    void unSetDelDate(String fullName, String userID);


}
