import java.sql.SQLException;

public interface AuthService {

    void start() throws SQLException;

    boolean getAuthByLoginPass(String login, String pass);

    void createUser(String userName, String plainUserPassword);

    boolean existUser(String userName);

    void stop();
}

