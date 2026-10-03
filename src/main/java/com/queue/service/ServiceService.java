package com.queue.service;

import com.queue.dao.ServiceDAO;
import com.queue.model.Service;

import java.util.List;

public class ServiceService {

    private ServiceDAO serviceDAO;

    public ServiceService() {
        serviceDAO = new ServiceDAO();
    }

    public List<Service> getAllServices() {
        return serviceDAO.getAllServices();
    }
}