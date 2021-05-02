import java.util.ArrayList;

public interface DataService {

    ArrayList getUserFiles(String userID);
    String getUserFile(String userID, String fileName);
    void uploadFile(String userId, DataSet ds);
    long getLastModTime(String userID,String fullName);
    void delFile(String userID,String fullName);
    String getFullName( String fileID);
    String getFileName( String fileID);
    String getLastMod( String fileID);

}
