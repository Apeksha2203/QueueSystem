// VIVA GUIDE: Java data model: fields, getters and setters transport structured values between SQL mapping and application logic. It does not itself enforce all business rules.
package com.queue.model;

public class Service {

    private int serviceId;
    private String serviceName;
    private String description;
    private int averageServiceTime;

    // Initialize this object; inspect arguments/field assignments for the values it carries.
    public Service() {
    }

    // Initialize this object; inspect arguments/field assignments for the values it carries.
    public Service(int serviceId, String serviceName,
                   String description, int averageServiceTime) {
        this.serviceId = serviceId;
        this.serviceName = serviceName;
        this.description = description;
        this.averageServiceTime = averageServiceTime;
    }

    // Return the stored service id field; this accessor does not query the database.
    public int getServiceId() {
        return serviceId;
    }

    // Update the service id field on this object; persistence happens separately in a DAO/service.
    public void setServiceId(int serviceId) {
        this.serviceId = serviceId;
    }

    // Return the stored service name field; this accessor does not query the database.
    public String getServiceName() {
        return serviceName;
    }

    // Update the service name field on this object; persistence happens separately in a DAO/service.
    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    // Return the stored description field; this accessor does not query the database.
    public String getDescription() {
        return description;
    }

    // Update the description field on this object; persistence happens separately in a DAO/service.
    public void setDescription(String description) {
        this.description = description;
    }

    // Return the stored average service time field; this accessor does not query the database.
    public int getAverageServiceTime() {
        return averageServiceTime;
    }

    // Update the average service time field on this object; persistence happens separately in a DAO/service.
    public void setAverageServiceTime(int averageServiceTime) {
        this.averageServiceTime = averageServiceTime;
    }
}
