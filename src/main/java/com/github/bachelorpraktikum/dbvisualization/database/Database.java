package com.github.bachelorpraktikum.dbvisualization.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.xml.crypto.Data;
import java.net.URL;
import java.sql.SQLException;

public class Database implements AutoCloseable {
    private final URL url;
    private final DatabaseUser user;
    private HikariDataSource connection;

    Database(URL url, DatabaseUser user) {
        this.url = url;
        this.user = user;
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://" + this.url);
        config.setUsername(user.getUser());
        config.setPassword(user.getPassword());
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        connection = new HikariDataSource(config);
    }

    boolean testConnection() {
        try {
            connection.getConnection().getClientInfo();
        } catch (SQLException ignored) {
            return false;
        }

        return true;
    }
    
    @Override
    public void close() throws Exception {
        connection.close();
    }
}
