// VIVA GUIDE: Standalone test or diagnostic program. Read fixture setup, assertions and cleanup; do not assume Maven package executes this file.
package com.queue.util;

import java.sql.Connection;

public class DBTest {

    // Standalone entry point: runs this diagnostic/setup/check explicitly; Maven packaging does not automatically execute it.
    public static void main(String[] args) {

        try {
            Connection connection = DBConnection.getConnection();

            System.out.println("Database connected successfully!");

            connection.close();

        } catch (Exception e) {
            System.out.println("Database connection failed!");
            e.printStackTrace();
        }
    }
}