// VIVA GUIDE: Database parent-row locks. SELECT FOR UPDATE serializes operations per service; locks depend on the surrounding transaction.
package com.queue.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/** Stable parent-row locks serialize validation and writes across server instances. */
public final class TransactionLocks {
    // Initialize this object; inspect arguments/field assignments for the values it carries.
    private TransactionLocks() {}
    // Locks a stable services parent row with SELECT FOR UPDATE so concurrent reservations for the same service serialize.
    public static boolean service(Connection connection, int id) throws SQLException {
        return lock(connection, "SELECT service_id FROM services WHERE service_id=? FOR UPDATE", id);
    }
    // Locks a stable student row for transaction-scoped student operations.
    public static boolean student(Connection connection, int id) throws SQLException {
        return lock(connection, "SELECT student_id FROM students WHERE student_id=? FOR UPDATE", id);
    }
    // Binds the ID and reads the locked row; commit/rollback on the caller connection releases the lock.
    private static boolean lock(Connection connection, String sql, int id) throws SQLException {
        try (PreparedStatement statement=connection.prepareStatement(sql)) {
            statement.setInt(1,id);
            try (ResultSet rows=statement.executeQuery()) { return rows.next(); }
        }
    }
}
