// VIVA GUIDE: Connection factory. Environment variables override an explicitly loaded local properties file. DriverManager opens the MySQL connection.
package com.queue.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.InputStream;
import java.util.Properties;

/** Local credentials stay outside the WAR and Git. Environment variables take precedence. */
public class DBConnection {
    // Loads explicit local properties if configured, applies environment overrides and opens a MySQL JDBC connection.
    public static Connection getConnection() throws SQLException {
        Properties config = new Properties();
        String configPath = System.getProperty("queue.config", System.getenv("QUEUE_DB_CONFIG"));
        if (configPath != null && !configPath.isBlank()) {
            try (InputStream stream = Files.newInputStream(Path.of(configPath))) { config.load(stream); }
            catch (Exception e) { throw new SQLException("Cannot read the local database configuration.", e); }
        }
        String url = value("QUEUE_DB_URL", config, "url", "jdbc:mysql://127.0.0.1:3306/campus_queue?connectionTimeZone=Asia/Kolkata");
        String user = value("QUEUE_DB_USER", config, "user", "root");
        String password = value("QUEUE_DB_PASSWORD", config, "password", "");
        try { Class.forName("com.mysql.cj.jdbc.Driver"); }
        catch (ClassNotFoundException e) { throw new SQLException("MySQL JDBC driver not found.", e); }
        return DriverManager.getConnection(url, user, password);
    }
    // Chooses environment value first, then local property, then fallback; never print credential values.
    private static String value(String env, Properties config, String key, String fallback) {
        String value = System.getenv(env);
        return value != null ? value : config.getProperty(key, fallback);
    }
}
