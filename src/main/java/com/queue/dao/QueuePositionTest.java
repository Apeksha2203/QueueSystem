package com.queue.dao;

public class QueuePositionTest {

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