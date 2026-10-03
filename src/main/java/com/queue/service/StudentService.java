package com.queue.service;

import com.queue.dao.QueueDAO;
import com.queue.model.Queue;

public class StudentService {

    private QueueDAO queueDAO;

    public StudentService() {
        queueDAO = new QueueDAO();
    }

    public boolean joinQueue(Queue queue) {
        return queueDAO.joinQueue(queue);
    }

    public Queue getQueueStatus(int studentId, int serviceId) {
        return queueDAO.getQueueStatus(studentId, serviceId);
    }

    public int getQueuePosition(int studentId, int serviceId) {
        return queueDAO.getQueuePosition(studentId, serviceId);
    }

    public int getCurrentToken(int serviceId) {
        return queueDAO.getCurrentToken(serviceId);
    }
}
