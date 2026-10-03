package com.queue.model;

public class Counter {

    private int counterId;
    private String counterName;
    private int serviceId;
    private boolean active;

    public Counter() {
    }

    public Counter(int counterId, String counterName,
                   int serviceId, boolean active) {
        this.counterId = counterId;
        this.counterName = counterName;
        this.serviceId = serviceId;
        this.active = active;
    }

    public int getCounterId() {
        return counterId;
    }

    public void setCounterId(int counterId) {
        this.counterId = counterId;
    }

    public String getCounterName() {
        return counterName;
    }

    public void setCounterName(String counterName) {
        this.counterName = counterName;
    }

    public int getServiceId() {
        return serviceId;
    }

    public void setServiceId(int serviceId) {
        this.serviceId = serviceId;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}