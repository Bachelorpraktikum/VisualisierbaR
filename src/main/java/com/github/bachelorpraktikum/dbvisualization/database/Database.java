package com.github.bachelorpraktikum.dbvisualization.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.net.URI;
import java.sql.SQLException;

public class Database implements AutoCloseable {

    private HikariDataSource connection;

    public Database(URI uri) {
        new Database(uri, new DatabaseUser("", ""));
    }

    public Database(URI uri, DatabaseUser user) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://" + uri.toString());
        config.setUsername(user.getUser());
        config.setPassword(user.getPassword());
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        connection = new HikariDataSource(config);
    }

    public boolean testConnection() throws SQLException {
        connection.getConnection().getClientInfo();

        return true;
    }

    @Override
    public void close() throws Exception {
        connection.close();
    }
}
