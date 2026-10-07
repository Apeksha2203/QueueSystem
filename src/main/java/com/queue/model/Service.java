package com.queue.model;

public class Service {

    private int serviceId;
    private String serviceName;
    private String description;
    private int averageServiceTime;

    public Service() {
    }

    public Service(int serviceId, String serviceName,
                   String description, int averageServiceTime) {
        this.serviceId = serviceId;
        this.serviceName = serviceName;
        this.description = description;
        this.averageServiceTime = averageServiceTime;
    }

    public int getServiceId() {
        return serviceId;
    }

    public void setServiceId(int serviceId) {
        this.serviceId = serviceId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getAverageServiceTime() {
        return averageServiceTime;
    }

    public void setAverageServiceTime(int averageServiceTime) {
        this.averageServiceTime = averageServiceTime;
    }
}
