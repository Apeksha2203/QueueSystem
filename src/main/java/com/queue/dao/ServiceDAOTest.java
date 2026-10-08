// VIVA GUIDE: Standalone test or diagnostic program. Read fixture setup, assertions and cleanup; do not assume Maven package executes this file.
package com.queue.dao;

import com.queue.model.Service;

import java.util.List;

public class ServiceDAOTest {

    // Standalone entry point: runs this diagnostic/setup/check explicitly; Maven packaging does not automatically execute it.
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