// VIVA GUIDE: Java data model: fields, getters and setters transport structured values between SQL mapping and application logic. It does not itself enforce all business rules.
package com.queue.model;

import java.sql.Timestamp;

public class Queue {

    private int queueId;
    private int studentId;
    private int serviceId;
    private int tokenNumber;
    private String status;
    private Timestamp joinedAt;
    private Timestamp calledAt;
    private Timestamp startedAt;
    private Timestamp completedAt;

    // Initialize this object; inspect arguments/field assignments for the values it carries.
    public Queue() {
    }

    // Initialize this object; inspect arguments/field assignments for the values it carries.
    public Queue(int queueId, int studentId, int serviceId,
                 int tokenNumber, String status,
                 Timestamp joinedAt, Timestamp calledAt,
                 Timestamp startedAt, Timestamp completedAt) {

        this.queueId = queueId;
        this.studentId = studentId;
        this.serviceId = serviceId;
        this.tokenNumber = tokenNumber;
        this.status = status;
        this.joinedAt = joinedAt;
        this.calledAt = calledAt;
        this.startedAt = startedAt;
        this.completedAt = completedAt;
    }

    // Return the stored queue id field; this accessor does not query the database.
    public int getQueueId() {
        return queueId;
    }

    // Update the queue id field on this object; persistence happens separately in a DAO/service.
    public void setQueueId(int queueId) {
        this.queueId = queueId;
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

    // Return the stored token number field; this accessor does not query the database.
    public int getTokenNumber() {
        return tokenNumber;
    }

    // Update the token number field on this object; persistence happens separately in a DAO/service.
    public void setTokenNumber(int tokenNumber) {
        this.tokenNumber = tokenNumber;
    }

    // Return the stored status field; this accessor does not query the database.
    public String getStatus() {
        return status;
    }

    // Update the status field on this object; persistence happens separately in a DAO/service.
    public void setStatus(String status) {
        this.status = status;
    }

    // Return the stored joined at field; this accessor does not query the database.
    public Timestamp getJoinedAt() {
        return joinedAt;
    }

    // Update the joined at field on this object; persistence happens separately in a DAO/service.
    public void setJoinedAt(Timestamp joinedAt) {
        this.joinedAt = joinedAt;
    }

    // Return the stored called at field; this accessor does not query the database.
    public Timestamp getCalledAt() {
        return calledAt;
    }

    // Update the called at field on this object; persistence happens separately in a DAO/service.
    public void setCalledAt(Timestamp calledAt) {
        this.calledAt = calledAt;
    }

    // Return the stored started at field; this accessor does not query the database.
    public Timestamp getStartedAt() {
        return startedAt;
    }

    // Update the started at field on this object; persistence happens separately in a DAO/service.
    public void setStartedAt(Timestamp startedAt) {
        this.startedAt = startedAt;
    }

    // Return the stored completed at field; this accessor does not query the database.
    public Timestamp getCompletedAt() {
        return completedAt;
    }

    // Update the completed at field on this object; persistence happens separately in a DAO/service.
    public void setCompletedAt(Timestamp completedAt) {
        this.completedAt = completedAt;
    }
}
