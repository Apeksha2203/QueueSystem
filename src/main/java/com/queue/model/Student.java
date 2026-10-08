// VIVA GUIDE: Java data model: fields, getters and setters transport structured values between SQL mapping and application logic. It does not itself enforce all business rules.
package com.queue.model;

public class Student {

    private int studentId;
    private String studentName;
    private String email;
    private String phone;
    private String password;

    // Initialize this object; inspect arguments/field assignments for the values it carries.
    public Student() {
    }

    // Initialize this object; inspect arguments/field assignments for the values it carries.
    public Student(int studentId, String studentName, String email,
                   String phone, String password) {
        this.studentId = studentId;
        this.studentName = studentName;
        this.email = email;
        this.phone = phone;
        this.password = password;
    }

    // Return the stored student id field; this accessor does not query the database.
    public int getStudentId() {
        return studentId;
    }

    // Update the student id field on this object; persistence happens separately in a DAO/service.
    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    // Return the stored student name field; this accessor does not query the database.
    public String getStudentName() {
        return studentName;
    }

    // Update the student name field on this object; persistence happens separately in a DAO/service.
    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    // Return the stored email field; this accessor does not query the database.
    public String getEmail() {
        return email;
    }

    // Update the email field on this object; persistence happens separately in a DAO/service.
    public void setEmail(String email) {
        this.email = email;
    }

    // Return the stored phone field; this accessor does not query the database.
    public String getPhone() {
        return phone;
    }

    // Update the phone field on this object; persistence happens separately in a DAO/service.
    public void setPhone(String phone) {
        this.phone = phone;
    }

    // Return the stored password field; this accessor does not query the database.
    public String getPassword() {
        return password;
    }

    // Update the password field on this object; persistence happens separately in a DAO/service.
    public void setPassword(String password) {
        this.password = password;
    }
}
