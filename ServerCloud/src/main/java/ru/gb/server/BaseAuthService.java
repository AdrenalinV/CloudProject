package ru.gb.server;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;

public class BaseAuthService implements AuthService {
    private static BaseAuthService bas = null;
    private static final String INIT_DB = "CREATE SCHEMA IF NOT EXISTS CLOUD;" +
            "CREATE TABLE IF NOT EXISTS CLOUD.user (id INTEGER PRIMARY KEY AUTO_INCREMENT NOT NULL," +
            "login VARCHAR(40) UNIQUE NOT NULL," +
            "pass VARCHAR(255) NOT NULL);" +
            "CREATE TABLE IF NOT EXISTS CLOUD.data ( id INTEGER PRIMARY KEY AUTO_INCREMENT NOT NULL," +
            "id_user INTEGER NOT NULL," +
            "full_name VARCHAR(255) NOT NULL," +
            "name VARCHAR(255) NOT NULL," +
            "date_last_mod LONG NOT NULL," +
            "date_del LONG," +
            "FOREIGN KEY (id_user) REFERENCES CLOUD.user(id));" +
            "CREATE TABLE IF NOT EXISTS CLOUD.user_path ( id INTEGER PRIMARY KEY AUTO_INCREMENT NOT NULL," +
            "id_user INTEGER NOT NULL," +
            "full_name VARCHAR(255) NOT NULL," +
            "FOREIGN KEY (id_user) REFERENCES CLOUD.user(id));" +
            "COMMIT;";
    private static final String EXIST_USER = "SELECT * FROM CLOUD.user WHERE login=?";
    private static final String ADD_USER = "INSERT INTO CLOUD.user(login, pass) VALUES (?, ?)";
    private static final String GET_AUTH_BY_LOGIN_PASS = "SELECT id FROM CLOUD.user WHERE login=? AND pass=?";
    private static final String SUPER_SECRET_SALT = "MY_MOM_MAKES_COFFEE";

    private BaseAuthService() throws SQLException {
        try (Connection con = DataSource.getConnection();
             Statement st = con.createStatement()) {
            st.executeUpdate(INIT_DB);
        }

    }

    public static BaseAuthService of() {
        if (bas == null) {
            try {
                bas = new BaseAuthService();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
        return bas;
    }


    @Override
    public String getAuthByLoginPass(String login, String pass) {
        String res=null;
        try (Connection connection = DataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(GET_AUTH_BY_LOGIN_PASS)) {
            ps.setString(1, login);
            ps.setString(2, getPassword(pass));
            ResultSet rs = ps.executeQuery();
            while( rs.next()){
                res=rs.getString(1);
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return res;
    }

    @Override
    public void createUser(String userName, String plainUserPassword) {
        try (Connection connection = DataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(ADD_USER)) {
            ps.setString(1, userName);
            ps.setString(2, getPassword(plainUserPassword));
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    @Override
    public boolean existUser(String userName) {
        boolean isExist = false;
        try (Connection con = DataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(EXIST_USER)) {
            ps.setString(1, userName);
            ResultSet rs = ps.executeQuery();
            isExist = rs.next();
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return isExist;
    }

    private String getPassword(String plainPassword) {
        String hashedPassword = null;
        String passwordWithSalt = plainPassword + SUPER_SECRET_SALT;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            hashedPassword = bytesToHex(md.digest(passwordWithSalt.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return hashedPassword;
    }

    public static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
