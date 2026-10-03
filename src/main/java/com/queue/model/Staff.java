package com.queue.model;

public class Staff {

    private int staffId;
    private String staffName;
    private String email;
    private String password;
    private int serviceId;
    private int counterId;

    public Staff() {
    }

    public Staff(int staffId, String staffName, String email,
                 String password, int serviceId, int counterId) {
        this.staffId = staffId;
        this.staffName = staffName;
        this.email = email;
        this.password = password;
        this.serviceId = serviceId;
        this.counterId = counterId;
    }

    public int getStaffId() {
        return staffId;
    }

    public void setStaffId(int staffId) {
        this.staffId = staffId;
    }

    public String getStaffName() {
        return staffName;
    }

    public void setStaffName(String staffName) {
        this.staffName = staffName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getServiceId() {
        return serviceId;
    }

    public void setServiceId(int serviceId) {
        this.serviceId = serviceId;
    }

    public int getCounterId() {
        return counterId;
    }

    public void setCounterId(int counterId) {
        this.counterId = counterId;
    }
}