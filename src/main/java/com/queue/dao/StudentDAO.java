package com.queue.dao;

import com.queue.model.Student;
import com.queue.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class StudentDAO {

    public boolean registerStudent(Student student) {

        String sql = "INSERT INTO students " +
                     "(student_name, email, phone, password) " +
                     "VALUES (?, ?, ?, ?)";

        try (
            Connection connection = DBConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)
        ) {

            statement.setString(1, student.getStudentName());
            statement.setString(2, student.getEmail());
            statement.setString(3, student.getPhone());
            statement.setString(4, student.getPassword());

            int rowsInserted = statement.executeUpdate();

            return rowsInserted > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    public Student loginStudent(String email, String password) {

        String sql = "SELECT * FROM students " +
                     "WHERE email = ? AND password = ?";

        try (
            Connection connection = DBConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)
        ) {

            statement.setString(1, email);
            statement.setString(2, password);

            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {

                Student student = new Student();

                student.setStudentId(resultSet.getInt("student_id"));
                student.setStudentName(resultSet.getString("student_name"));
                student.setEmail(resultSet.getString("email"));
                student.setPhone(resultSet.getString("phone"));
                student.setPassword(resultSet.getString("password"));

                return student;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}