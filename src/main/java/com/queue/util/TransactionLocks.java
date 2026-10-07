package com.queue.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/** Stable parent-row locks serialize validation and writes across server instances. */
public final class TransactionLocks {
    private TransactionLocks() {}
    public static boolean service(Connection connection, int id) throws SQLException {
        return lock(connection, "SELECT service_id FROM services WHERE service_id=? FOR UPDATE", id);
    }
    public static boolean student(Connection connection, int id) throws SQLException {
        return lock(connection, "SELECT student_id FROM students WHERE student_id=? FOR UPDATE", id);
    }
    private static boolean lock(Connection connection, String sql, int id) throws SQLException {
        try (PreparedStatement statement=connection.prepareStatement(sql)) {
            statement.setInt(1,id);
            try (ResultSet rows=statement.executeQuery()) { return rows.next(); }
        }
    }
}
