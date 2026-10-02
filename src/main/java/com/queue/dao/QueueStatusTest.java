package com.queue.dao;

import com.queue.model.Queue;

public class QueueStatusTest {

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
