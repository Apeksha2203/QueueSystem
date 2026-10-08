// VIVA GUIDE: Java data model: fields, getters and setters transport structured values between SQL mapping and application logic. It does not itself enforce all business rules.
package com.queue.model;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;

public class Booking {

    private int bookingId;
    private int studentId;
    private int serviceId;
    private Date bookingDate;
    private Time bookingTime;
    private String status;
    private Timestamp createdAt;

    // Initialize this object; inspect arguments/field assignments for the values it carries.
    public Booking() {
    }

    // Initialize this object; inspect arguments/field assignments for the values it carries.
    public Booking(int bookingId, int studentId, int serviceId,
                   Date bookingDate, Time bookingTime,
                   String status, Timestamp createdAt) {

        this.bookingId = bookingId;
        this.studentId = studentId;
        this.serviceId = serviceId;
        this.bookingDate = bookingDate;
        this.bookingTime = bookingTime;
        this.status = status;
        this.createdAt = createdAt;
    }

    // Return the stored booking id field; this accessor does not query the database.
    public int getBookingId() {
        return bookingId;
    }

    // Update the booking id field on this object; persistence happens separately in a DAO/service.
    public void setBookingId(int bookingId) {
        this.bookingId = bookingId;
    }

    // Return the stored student id field; this accessor does not query the database.
    public int getStudentId() {
        return studentId;
    }

    // Update the student id field on this object; persistence happens separately in a DAO/service.
    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    // Return the stored service id field; this accessor does not query the database.
    public int getServiceId() {
        return serviceId;
    }

    // Update the service id field on this object; persistence happens separately in a DAO/service.
    public void setServiceId(int serviceId) {
        this.serviceId = serviceId;
    }

    // Return the stored booking date field; this accessor does not query the database.
    public Date getBookingDate() {
        return bookingDate;
    }

    // Update the booking date field on this object; persistence happens separately in a DAO/service.
    public void setBookingDate(Date bookingDate) {
        this.bookingDate = bookingDate;
    }

    // Return the stored booking time field; this accessor does not query the database.
    public Time getBookingTime() {
        return bookingTime;
    }

    // Update the booking time field on this object; persistence happens separately in a DAO/service.
    public void setBookingTime(Time bookingTime) {
        this.bookingTime = bookingTime;
    }

    // Return the stored status field; this accessor does not query the database.
    public String getStatus() {
        return status;
    }

    // Update the status field on this object; persistence happens separately in a DAO/service.
    public void setStatus(String status) {
        this.status = status;
    }

    // Return the stored created at field; this accessor does not query the database.
    public Timestamp getCreatedAt() {
        return createdAt;
    }

    // Update the created at field on this object; persistence happens separately in a DAO/service.
    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}
