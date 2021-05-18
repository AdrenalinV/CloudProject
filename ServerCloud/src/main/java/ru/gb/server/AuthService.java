package ru.gb.server;


public interface AuthService {

    String getAuthByLoginPass(String login, String pass);

    void createUser(String userName, String plainUserPassword);

    boolean existUser(String userName);

}

