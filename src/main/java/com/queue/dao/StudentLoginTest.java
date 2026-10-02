package com.queue.dao;

import com.queue.model.Student;

public class StudentLoginTest {

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