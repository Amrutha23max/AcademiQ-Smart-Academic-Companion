package com.academiq.common;

import java.sql.*;

/** Creates JDBC connections from AppConfig. Every DAO calls DatabaseConnection.get(). */
public final class DatabaseConnection {
    private DatabaseConnection() {}

    public static Connection get() throws DatabaseException {
        try {
            return DriverManager.getConnection(AppConfig.get("db.url", ""),
                    AppConfig.get("db.user", "root"), AppConfig.get("db.password", ""));
        } catch (SQLException e) {
            throw new DatabaseException("Cannot connect to the database. Check academiq.properties and that MySQL is running. (" + e.getMessage() + ")", e);
        }
    }
}
