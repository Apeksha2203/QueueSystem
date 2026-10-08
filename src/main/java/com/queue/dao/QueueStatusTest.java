// VIVA GUIDE: Standalone test or diagnostic program. Read fixture setup, assertions and cleanup; do not assume Maven package executes this file.
package com.queue.dao;

import com.queue.model.Queue;

public class QueueStatusTest {

    // Standalone entry point: runs this diagnostic/setup/check explicitly; Maven packaging does not automatically execute it.
    public static void main(String[] args) {

        QueueDAO queueDAO = new QueueDAO();

        // Test Student 1 and Service 1
        Queue queue = queueDAO.getQueueStatus(1, 1);

        if (queue != null) {

            System.out.println("Queue status found!");
            System.out.println("Queue ID: " + queue.getQueueId());
            System.out.println("Student ID: " + queue.getStudentId());
            System.out.println("Service ID: " + queue.getServiceId());
            System.out.println("Token Number: " + queue.getTokenNumber());
            System.out.println("Status: " + queue.getStatus());

        } else {

            System.out.println("Queue record not found!");
        }
    }
}
