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

    public Queue() {
    }

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

    public int getQueueId() {
        return queueId;
    }

    public void setQueueId(int queueId) {
        this.queueId = queueId;
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

    public int getTokenNumber() {
        return tokenNumber;
    }

    public void setTokenNumber(int tokenNumber) {
        this.tokenNumber = tokenNumber;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Timestamp getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(Timestamp joinedAt) {
        this.joinedAt = joinedAt;
    }

    public Timestamp getCalledAt() {
        return calledAt;
    }

    public void setCalledAt(Timestamp calledAt) {
        this.calledAt = calledAt;
    }

    public Timestamp getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Timestamp startedAt) {
        this.startedAt = startedAt;
    }

    public Timestamp getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Timestamp completedAt) {
        this.completedAt = completedAt;
    }
}
