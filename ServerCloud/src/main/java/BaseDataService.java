import java.sql.*;
import java.util.ArrayList;

public class BaseDataService implements DataService{
    private static BaseDataService bds=null;
    private static final String USER_FILES="full_name FROM CLOUD.data WHERE id_user=?";
    private static final String USER_FILE="SELECT id FROM CLOUD.data WHERE id_user=? AND full_name=?";
    private static final String ADD_FILE="INSERT INTO CLOUD.data(id_user, full_name ,name ,date_last_mod) VALUES (?, ?, ?, ?)";
    private static final String GET_DATE_MOD="SELECT date_last_mod FROM CLOUD.data WHERE id_user=? AND full_name=?";
    private static final String DEL_FILE="DELETE FROM CLOUD.data WHERE id_user=? AND full_name=?";
    private static final String GET_FULL_NAME="SELECT full_name FROM CLOUD.data WHERE id=?";
    private static final String GET_FILE_NAME="SELECT name FROM CLOUD.data WHERE id=?";
    private static final String GET_LAST_MOD="SELECT date_last_mod FROM CLOUD.data WHERE id=?";

    private BaseDataService(){};

    public static BaseDataService of(){
        if (bds==null){
            bds=new BaseDataService();
        }
        return bds;
    }

    @Override
    public ArrayList<String> getUserFiles(String userID) {
        ArrayList<String> files=new ArrayList<>();
        try (Connection connection = DataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(USER_FILES)) {
            ps.setString(1, userID);
            ResultSet rs = ps.executeQuery();
            while( rs.next()){
                files.add(rs.getString(1));
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return files;
    }

    @Override
    public String getUserFile(String userID, String fullName) {
        String res=null;
        try (Connection connection = DataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(USER_FILE)) {
            ps.setString(1, userID);
            ps.setString(2, fullName);
            ResultSet rs = ps.executeQuery();
            while( rs.next()){
                res=(rs.getString(1));
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return res;
    }

    @Override
    public void uploadFile(String userId, DataSet ds) {
        try (Connection connection = DataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(ADD_FILE)) {
            ps.setString(1, userId);
            ps.setString(2, ds.getPathFile());
            ps.setString(3, ds.getNameFile());
            ps.setString(4, String.valueOf(ds.getDateMod()));
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public long getLastModTime(String userID, String fullFileName) {
        String res="0";
        try (Connection connection = DataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(GET_DATE_MOD)) {
            ps.setString(1, userID);
            ps.setString(1, fullFileName);
            ResultSet rs = ps.executeQuery();
            while( rs.next()){
                res=(rs.getString(1));
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Long.parseLong(res);
    }

    @Override
    public void delFile(String userid, String fullName) {
        try (Connection connection = DataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(DEL_FILE)) {
            ps.setString(1, userid);
            ps.setString(2, fullName);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getFullName( String fileID) {
        String res=null;
        try (Connection connection = DataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(GET_FULL_NAME)) {
            ps.setString(1, fileID);
            ResultSet rs = ps.executeQuery();
            while( rs.next()){
                res=(rs.getString(1));
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return res;
    }

    @Override
    public String getFileName( String fileID) {
        String res=null;
        try (Connection connection = DataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(GET_FILE_NAME)) {
            ps.setString(1, fileID);
            ResultSet rs = ps.executeQuery();
            while( rs.next()){
                res=(rs.getString(1));
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return res;
    }

    @Override
    public String getLastMod(String fileID) {
        String res=null;
        try (Connection connection = DataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(GET_LAST_MOD)) {
            ps.setString(1, fileID);
            ResultSet rs = ps.executeQuery();
            while( rs.next()){
                res=(rs.getString(1));
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return res;
    }


}
