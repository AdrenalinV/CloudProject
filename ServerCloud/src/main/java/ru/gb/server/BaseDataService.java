package ru.gb.server;

import lombok.extern.log4j.Log4j2;
import ru.gb.core.DataSet;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;

@Log4j2
public class BaseDataService implements DataService {
    private static BaseDataService bds = null;
    private static final String USER_FILES = "SELECT full_name FROM CLOUD.data WHERE date_del IS NULL AND id_user=? ";
    private static final String USER_DEL_FILES = "SELECT full_name FROM CLOUD.data WHERE date_del IS NOT NULL AND id_user=? ";
    private static final String USER_FILE = "SELECT id FROM CLOUD.data WHERE id_user=? AND full_name=?";
    private static final String ADD_FILE = "INSERT INTO CLOUD.data(id_user, full_name ,name ,date_last_mod) VALUES (?, ?, ?, ?)";
    private static final String GET_DATE_MOD = "SELECT date_last_mod FROM CLOUD.data WHERE id_user=? AND full_name=?";
    private static final String SET_DATE_MOD = "UPDATE CLOUD.data SET date_last_mod=? WHERE id=?";
    private static final String UNSET_DATE_DEL = "UPDATE CLOUD.data SET date_del = NULL WHERE id_user=? AND full_name=?";
    private static final String SET_DATE_DEL = "UPDATE CLOUD.data SET date_del=? WHERE id_user=? AND full_name=?";
    private static final String DEL_FILE = "DELETE FROM CLOUD.data WHERE id_user=? AND full_name=?";
    private static final String GET_FULL_NAME = "SELECT full_name FROM CLOUD.data WHERE id=?";
    private static final String GET_FILE_NAME = "SELECT name FROM CLOUD.data WHERE id=?";
    private static final String GET_LAST_MOD = "SELECT date_last_mod FROM CLOUD.data WHERE id=?";
    private static final String GET_USER_PATH = "SELECT full_name FROM CLOUD.user_path WHERE id_user=?";
    private static final String DEL_USER_PATH = "DELETE FROM CLOUD.user_path WHERE id_user=?";
    private static final String SET_USER_PATH = "INSERT INTO CLOUD.user_path(id_user, full_name) VALUES(?,?)";

    private BaseDataService() {
        log.info("create BaseDataService");
    }

    public static BaseDataService of() {
        if (bds == null) {
            bds = new BaseDataService();
        }
        return bds;
    }

    @Override
    public ArrayList<String> getUserFiles(String userID) {
        ArrayList<String> files = new ArrayList<>();
        try (Connection connection = DataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(USER_FILES)) {
            ps.setString(1, userID);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                files.add(rs.getString(1));
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return files;
    }

    @Override
    public ArrayList<String> getDelUserFiles(String userID) {
        ArrayList<String> files = new ArrayList<>();
        try (Connection connection = DataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(USER_DEL_FILES)) {
            ps.setString(1, userID);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
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
        String res = null;
        try (Connection connection = DataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(USER_FILE)) {
            ps.setString(1, userID);
            ps.setString(2, fullName);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                res = (rs.getString(1));
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
        String res = "0";
        try (Connection connection = DataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(GET_DATE_MOD)) {
            ps.setString(1, userID);
            ps.setString(2, fullFileName);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                res = (rs.getString(1));
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
    public String getFullName(String fileID) {
        String res = null;
        try (Connection connection = DataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(GET_FULL_NAME)) {
            ps.setString(1, fileID);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                res = (rs.getString(1));
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return res;
    }

    @Override
    public String getFileName(String fileID) {
        String res = null;
        try (Connection connection = DataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(GET_FILE_NAME)) {
            ps.setString(1, fileID);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                res = (rs.getString(1));
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return res;
    }

    @Override
    public String getLastMod(String fileID) {
        String res = null;
        try (Connection connection = DataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(GET_LAST_MOD)) {
            ps.setString(1, fileID);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                res = (rs.getString(1));
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return res;
    }

    @Override
    public void setLastMod(long lastMod, String fileID) {
        try (Connection connection = DataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(SET_DATE_MOD)) {
            ps.setString(1, String.valueOf(lastMod));
            ps.setString(2, fileID);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public ArrayList<String> getUserPath(String userID) {
        ArrayList<String> files = new ArrayList<>();
        try (Connection connection = DataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(GET_USER_PATH)) {
            ps.setString(1, userID);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                files.add(rs.getString(1));
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return files;
    }

    @Override
    public void setUserPath(String listPaths, String userID) {
        try (Connection connection = DataSource.getConnection();
             PreparedStatement psD = connection.prepareStatement(DEL_USER_PATH);
             PreparedStatement psS = connection.prepareStatement(SET_USER_PATH)) {
            String[] list = listPaths.split(",");
            if (list.length > 0) {
                psD.setString(1, userID);
                psD.executeUpdate();
                for (String s : list) {
                    psS.setString(1, userID);
                    psS.setString(2, s);
                    psS.executeUpdate();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void setDelDate(String fullName, String userID) {
        try (Connection connection = DataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(SET_DATE_DEL)) {
            Date date = new Date();
            ps.setString(1, String.valueOf(date.getTime()));
            ps.setString(2, userID);
            ps.setString(3, fullName);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void unSetDelDate(String fullName, String userID) {
        try (Connection connection = DataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(UNSET_DATE_DEL)) {
            ps.setString(1, userID);
            ps.setString(2, fullName);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }


}
