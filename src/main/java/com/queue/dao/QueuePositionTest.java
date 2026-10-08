// VIVA GUIDE: Standalone test or diagnostic program. Read fixture setup, assertions and cleanup; do not assume Maven package executes this file.
package com.queue.dao;

public class QueuePositionTest {

    // Standalone entry point: runs this diagnostic/setup/check explicitly; Maven packaging does not automatically execute it.
    public static void main(String[] args) {

        QueueDAO queueDAO = new QueueDAO();

        // Test Student 1 and Service 1
        int position = queueDAO.getQueuePosition(1, 1);

        if (position != -1) {

            System.out.println("Queue position found!");
            System.out.println("Student ID: 1");
            System.out.println("Service ID: 1");
            System.out.println("Queue Position: " + position);

        } else {

            System.out.println("Student is not currently in the queue!");
        }
    }
}