package com.queue.dao;

import com.queue.model.Queue;

public class QueueDAOTest {

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