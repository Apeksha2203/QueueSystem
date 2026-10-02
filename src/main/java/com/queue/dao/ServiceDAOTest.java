package com.queue.dao;

import com.queue.model.Service;

import java.util.List;

public class ServiceDAOTest {

    public static void main(String[] args) {

        ServiceDAO serviceDAO = new ServiceDAO();

        List<Service> services = serviceDAO.getAllServices();

        System.out.println("Available Services:");

        for (Service service : services) {

            System.out.println(
                    service.getServiceId() + " - " +
                    service.getServiceName() + " - " +
                    service.getDescription() + " - " +
                    service.getAverageServiceTime() + " minutes"
            );
        }
    }
}