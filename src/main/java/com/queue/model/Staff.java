// VIVA GUIDE: Java data model: fields, getters and setters transport structured values between SQL mapping and application logic. It does not itself enforce all business rules.
package com.queue.model;

public class Staff {

    private int staffId;
    private String staffName;
    private String email;
    private String password;
    private int serviceId;
    private int counterId;

    // Initialize this object; inspect arguments/field assignments for the values it carries.
    public Staff() {
    }

    // Initialize this object; inspect arguments/field assignments for the values it carries.
    public Staff(int staffId, String staffName, String email,
                 String password, int serviceId, int counterId) {
        this.staffId = staffId;
        this.staffName = staffName;
        this.email = email;
        this.password = password;
        this.serviceId = serviceId;
        this.counterId = counterId;
    }

    // Return the stored staff id field; this accessor does not query the database.
    public int getStaffId() {
        return staffId;
    }

    // Update the staff id field on this object; persistence happens separately in a DAO/service.
    public void setStaffId(int staffId) {
        this.staffId = staffId;
    }

    // Return the stored staff name field; this accessor does not query the database.
    public String getStaffName() {
        return staffName;
    }

    // Update the staff name field on this object; persistence happens separately in a DAO/service.
    public void setStaffName(String staffName) {
        this.staffName = staffName;
    }

    // Return the stored email field; this accessor does not query the database.
    public String getEmail() {
        return email;
    }

    // Update the email field on this object; persistence happens separately in a DAO/service.
    public void setEmail(String email) {
        this.email = email;
    }

    // Return the stored password field; this accessor does not query the database.
    public String getPassword() {
        return password;
    }

    // Update the password field on this object; persistence happens separately in a DAO/service.
    public void setPassword(String password) {
        this.password = password;
    }

    // Return the stored service id field; this accessor does not query the database.
    public int getServiceId() {
        return serviceId;
    }

    // Update the service id field on this object; persistence happens separately in a DAO/service.
    public void setServiceId(int serviceId) {
        this.serviceId = serviceId;
    }

    // Return the stored counter id field; this accessor does not query the database.
    public int getCounterId() {
        return counterId;
    }

    // Update the counter id field on this object; persistence happens separately in a DAO/service.
    public void setCounterId(int counterId) {
        this.counterId = counterId;
    }
}
