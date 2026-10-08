// VIVA GUIDE: Student SQL storage/login operations, password verification and hash migration. Read PreparedStatement bindings and generated IDs.
package com.queue.dao;

import com.queue.model.Student;
import com.queue.util.Passwords;
import com.queue.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class StudentDAO {

    // Inserts validated student fields with a password hash using bound SQL parameters.
    public boolean registerStudent(Student student) {
        com.queue.util.StudentValidation.registration(student);

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
            statement.setString(4, Passwords.hash(student.getPassword()));

            int rowsInserted = statement.executeUpdate();

            return rowsInserted > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    // Finds the account by email, verifies the password and migrates a successfully authenticated legacy password to a hash.
    public Student loginStudent(String email, String password) {

        String sql = "SELECT * FROM students " +
                     "WHERE email = ?";

        try (
            Connection connection = DBConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)
        ) {

            statement.setString(1, email);


            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next() && Passwords.matches(password, resultSet.getString("password"))) {

                Student student = new Student();

                student.setStudentId(resultSet.getInt("student_id"));
                student.setStudentName(resultSet.getString("student_name"));
                student.setEmail(resultSet.getString("email"));
                student.setPhone(resultSet.getString("phone"));
                student.setPassword(resultSet.getString("password"));

                if (!student.getPassword().startsWith("pbkdf2$")) {
                    try (PreparedStatement migrate = connection.prepareStatement("UPDATE students SET password = ? WHERE student_id = ? AND password = ?")) {
                        migrate.setString(1, Passwords.hash(password)); migrate.setInt(2, student.getStudentId()); migrate.setString(3, student.getPassword()); migrate.executeUpdate();
                    }
                }
                return student;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
