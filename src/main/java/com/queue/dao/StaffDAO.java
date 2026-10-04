package com.queue.dao;

import com.queue.model.Staff;
import com.queue.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class StaffDAO {

    public Staff loginStaff(String email, String password) {

        String sql = "SELECT * FROM staff " +
                     "WHERE email = ? AND password = ?";

        try (
            Connection connection = DBConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)
        ) {

            statement.setString(1, email);
            statement.setString(2, password);

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