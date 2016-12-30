package com.github.bachelorpraktikum.dbvisualization.database;

public class DatabaseUser {
    private final String user;
    private final String password;

    DatabaseUser(String user, String password) {
        this.user = user;
        this.password = password;
    }

    String getUser() {
        return user;
    }

    String getPassword() {
        return password;
    }
}
