import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;

public class BaseAuthService implements AuthService{
    private static final String INIT_DB = "CREATE TABLE IF NOT EXISTS users (id INTEGER  PRIMARY KEY AUTO_INCREMENT NOT NULL, login VARCHAR(40) UNIQUE NOT NULL, pass TEXT NOT NULL)";
    private static final String EXIST_USER = "SELECT * FROM users WHERE login=?";
    private static final String ADD_USER = "INSERT INTO users(login, pass) VALUES (?, ?)";
    private static final String GET_AUTH_BY_LOGIN_PASS = "SELECT id FROM users WHERE login=? AND pass=?";
    private static final String SUPER_SECRET_SALT = "MY_MOM_MAKES_COFFEE";

    @Override
    public void start() throws SQLException {
        try (Connection con = DataSource.getConnection();
             Statement st = con.createStatement()) {
            st.executeUpdate(INIT_DB);
        }

    }

    @Override
    public boolean getAuthByLoginPass(String login, String pass) {
        boolean auth=false;
        try (Connection connection = DataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(GET_AUTH_BY_LOGIN_PASS)) {
            ps.setString(1, login);
            ps.setString(2, getPassword(pass));
            ResultSet rs = ps.executeQuery();
            auth = rs.next();
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return auth;
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

    @Override
    public void stop() {

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
