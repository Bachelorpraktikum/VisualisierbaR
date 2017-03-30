package com.github.bachelorpraktikum.dbvisualization.database;

import com.github.bachelorpraktikum.dbvisualization.config.ConfigFile;
import com.github.bachelorpraktikum.dbvisualization.config.ConfigKey;
import java.util.Optional;

public class DatabaseUser {

    private final String user;
    private final String password;

    public DatabaseUser(String user, String password) {
        this.user = user;
        this.password = password;
    }

    String getUser() {
        return user;
    }

    String getPassword() {
        return password;
    }

    static Optional<DatabaseUser> fromConfig() {
        String userKey = ConfigKey.databaseUsername.getKey();
        String passKey = ConfigKey.databasePassword.getKey();

        ConfigFile config = ConfigFile.getInstance();
        String username = config.getProperty(userKey);
        String password = config.getProperty(passKey);

        DatabaseUser user = null;
        if (username != null && password != null) {
            user = new DatabaseUser(username, password);
        }

        return Optional.ofNullable(user);
    }
}
