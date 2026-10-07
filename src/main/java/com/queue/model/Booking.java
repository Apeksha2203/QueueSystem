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

    public Booking() {
    }

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

    public int getBookingId() {
        return bookingId;
    }

    public void setBookingId(int bookingId) {
        this.bookingId = bookingId;
    }

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public int getServiceId() {
        return serviceId;
    }

    public void setServiceId(int serviceId) {
        this.serviceId = serviceId;
    }

    public Date getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(Date bookingDate) {
        this.bookingDate = bookingDate;
    }

    public Time getBookingTime() {
        return bookingTime;
    }

    public void setBookingTime(Time bookingTime) {
        this.bookingTime = bookingTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}
