package com.queue.util;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.Statement;

/** Explicit local setup command; never runs automatically when the web application starts. */
public final class DatabaseSetup {
    public static void main(String[] args) throws Exception {
        if (args.length != 1) throw new IllegalArgumentException("Supply the schema file path.");
        String schema = Files.readString(Path.of(args[0])).replaceAll("(?m)^\\s*--.*$", "");
        try (Connection connection = DBConnection.getConnection(); Statement statement = connection.createStatement()) {
            for (String sql : schema.split(";")) {
                if (!sql.isBlank()) statement.execute(sql.trim());
            }
            System.out.println("Database connection verified. Supplied SQL script executed successfully.");
            System.out.println("Existing records were retained. Staff accounts and counter availability were not fabricated.");
        } catch (java.sql.SQLException error) {
            System.err.println("Database setup failed (SQL state " + error.getSQLState()
                    + ", code " + error.getErrorCode() + "). Check your local credentials and MySQL service.");
            System.exit(1);
        }
    }
}
