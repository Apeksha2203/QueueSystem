// VIVA GUIDE: Supporting source or configuration. Check the entry point or caller, inputs, outputs and failure handling when explaining this file.
package com.queue.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnectionExample {

    private static final String URL =
            "jdbc:mysql://localhost:3306/campus_queue";

    private static final String USER = "root";

    private static final String PASSWORD =
            "YOUR_MYSQL_PASSWORD";

    // Retrieve connection for the caller; follow the SQL/service delegation to identify scope and return shape.
    public static Connection getConnection() throws SQLException {

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC Driver not found.", e);
        }

        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}