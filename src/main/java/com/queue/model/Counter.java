// VIVA GUIDE: Java data model: fields, getters and setters transport structured values between SQL mapping and application logic. It does not itself enforce all business rules.
package com.queue.model;

public class Counter {

    private int counterId;
    private String counterName;
    private int serviceId;
    private boolean active;

    // Initialize this object; inspect arguments/field assignments for the values it carries.
    public Counter() {
    }

    // Initialize this object; inspect arguments/field assignments for the values it carries.
    public Counter(int counterId, String counterName,
                   int serviceId, boolean active) {
        this.counterId = counterId;
        this.counterName = counterName;
        this.serviceId = serviceId;
        this.active = active;
    }

    // Return the stored counter id field; this accessor does not query the database.
    public int getCounterId() {
        return counterId;
    }

    // Update the counter id field on this object; persistence happens separately in a DAO/service.
    public void setCounterId(int counterId) {
        this.counterId = counterId;
    }

    // Return the stored counter name field; this accessor does not query the database.
    public String getCounterName() {
        return counterName;
    }

    // Update the counter name field on this object; persistence happens separately in a DAO/service.
    public void setCounterName(String counterName) {
        this.counterName = counterName;
    }

    // Return the stored service id field; this accessor does not query the database.
    public int getServiceId() {
        return serviceId;
    }

    // Update the service id field on this object; persistence happens separately in a DAO/service.
    public void setServiceId(int serviceId) {
        this.serviceId = serviceId;
    }

    // Return the stored active field; this accessor does not query the database.
    public boolean isActive() {
        return active;
    }

    // Update the active field on this object; persistence happens separately in a DAO/service.
    public void setActive(boolean active) {
        this.active = active;
    }
}
