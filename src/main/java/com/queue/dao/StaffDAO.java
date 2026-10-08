// VIVA GUIDE: Staff SQL login/profile queries and password verification. Assigned service and counter originate from stored account data.
package com.queue.dao;

import com.queue.model.Staff;
import com.queue.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class StaffDAO {

    // Finds the staff account by email and verifies its password; service/counter assignment comes from stored account fields.
    public Staff loginStaff(String email, String password) {

        String sql = "SELECT * FROM staff " +
                     "WHERE email = ?";

        try (
            Connection connection = DBConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)
        ) {

            statement.setString(1, email);

            try (ResultSet resultSet = statement.executeQuery()) {

                if (resultSet.next()) {

                    String stored = resultSet.getString("password");
                    if (!com.queue.util.Passwords.matches(password, stored)) return null;
                    if (!stored.startsWith("pbkdf2$")) {
                        try (PreparedStatement migrate = connection.prepareStatement("UPDATE staff SET password=? WHERE staff_id=? AND password=?")) {
                            migrate.setString(1, com.queue.util.Passwords.hash(password));
                            migrate.setInt(2, resultSet.getInt("staff_id"));
                            migrate.setString(3, stored);
                            migrate.executeUpdate();
                        }
                    }

                    Staff staff = new Staff();

                    staff.setStaffId(
                            resultSet.getInt("staff_id")
                    );

                    staff.setStaffName(
                            resultSet.getString("staff_name")
                    );

                    staff.setEmail(
                            resultSet.getString("email")
                    );

                    staff.setPassword(
                            resultSet.getString("password")
                    );

                    staff.setServiceId(
                            resultSet.getInt("service_id")
                    );

                    staff.setCounterId(
                            resultSet.getInt("counter_id")
                    );

                    return staff;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }


    // Retrieve staff by id for the caller; follow the SQL/service delegation to identify scope and return shape.
    public Staff getStaffById(int staffId) {

        String sql = "SELECT * FROM staff WHERE staff_id = ?";

        try (
            Connection connection = DBConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)
        ) {

            statement.setInt(1, staffId);

            try (ResultSet resultSet = statement.executeQuery()) {

                if (resultSet.next()) {

                    Staff staff = new Staff();

                    staff.setStaffId(
                            resultSet.getInt("staff_id")
                    );

                    staff.setStaffName(
                            resultSet.getString("staff_name")
                    );

                    staff.setEmail(
                            resultSet.getString("email")
                    );

                    staff.setPassword(
                            resultSet.getString("password")
                    );

                    staff.setServiceId(
                            resultSet.getInt("service_id")
                    );

                    staff.setCounterId(
                            resultSet.getInt("counter_id")
                    );

                    return staff;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
