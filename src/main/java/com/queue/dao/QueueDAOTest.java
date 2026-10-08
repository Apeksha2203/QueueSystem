// VIVA GUIDE: Standalone test or diagnostic program. Read fixture setup, assertions and cleanup; do not assume Maven package executes this file.
package com.queue.dao;

import com.queue.model.Queue;

public class QueueDAOTest {

    // Standalone entry point: runs this diagnostic/setup/check explicitly; Maven packaging does not automatically execute it.
    public static void main(String[] args) {

        Queue queue = new Queue();

        // Test Student
        queue.setStudentId(1);

        // Examination Section
        queue.setServiceId(1);

        QueueDAO queueDAO = new QueueDAO();

        boolean result = queueDAO.joinQueue(queue);

        if (result) {

            System.out.println("Student joined the queue successfully!");
            System.out.println("Student ID: " + queue.getStudentId());
            System.out.println("Service ID: " + queue.getServiceId());
            System.out.println("Token Number: " + queue.getTokenNumber());
            System.out.println("Status: " + queue.getStatus());

        } else {

            System.out.println("Failed to join queue!");
        }
    }
}