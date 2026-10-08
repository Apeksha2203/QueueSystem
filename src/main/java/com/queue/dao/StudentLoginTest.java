// VIVA GUIDE: Standalone test or diagnostic program. Read fixture setup, assertions and cleanup; do not assume Maven package executes this file.
package com.queue.dao;

import com.queue.model.Student;

public class StudentLoginTest {

    // Standalone entry point: runs this diagnostic/setup/check explicitly; Maven packaging does not automatically execute it.
    public static void main(String[] args) {

        StudentDAO studentDAO = new StudentDAO();

        Student student = studentDAO.loginStudent(
                "teststudent@gmail.com",
                "test123"
        );

        if (student != null) {

            System.out.println("Login successful!");
            System.out.println("Student ID: " + student.getStudentId());
            System.out.println("Name: " + student.getStudentName());
            System.out.println("Email: " + student.getEmail());

        } else {

            System.out.println("Invalid email or password!");
        }
    }
}