// VIVA GUIDE: Thin catalogue adapter that delegates to ServiceDAO.
package com.queue.service;

import com.queue.dao.ServiceDAO;
import com.queue.model.Service;

import java.util.List;

public class ServiceService {

    private ServiceDAO serviceDAO;

    // Initialize this object; inspect arguments/field assignments for the values it carries.
    public ServiceService() {
        serviceDAO = new ServiceDAO();
    }

    // Retrieve all services for the caller; follow the SQL/service delegation to identify scope and return shape.
    public List<Service> getAllServices() {
        return serviceDAO.getAllServices();
    }
}
