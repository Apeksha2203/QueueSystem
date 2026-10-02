package com.queue.dao;

import com.queue.model.Student;

public class StudentDAOTest {

    public static void main(String[] args) {

        Student student = new Student();

        student.setStudentName("Test Student");
        student.setEmail("teststudent@gmail.com");
        student.setPhone("9876543210");
        student.setPassword("test123");

        StudentDAO studentDAO = new StudentDAO();

        boolean result = studentDAO.registerStudent(student);

        if (result) {
            System.out.println("Student registered successfully!");
        } else {
            System.out.println("Student registration failed!");
        }
    }
}