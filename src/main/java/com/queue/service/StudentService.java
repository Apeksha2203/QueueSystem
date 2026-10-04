package com.queue.service;

import com.queue.dao.QueueDAO;
import com.queue.dao.StudentDAO;
import com.queue.model.Queue;
import com.queue.model.Student;

public class StudentService {

    private QueueDAO queueDAO;
    private StudentDAO studentDAO;

    public StudentService() {
        queueDAO = new QueueDAO();
        studentDAO = new StudentDAO();
    }

    // Student registration
    public boolean registerStudent(Student student) {
        return studentDAO.registerStudent(student);
    }

    // Student login
    public Student loginStudent(String email, String password) {
        return studentDAO.loginStudent(email, password);
    }

    // Join queue
    public boolean joinQueue(Queue queue) {
        return queueDAO.joinQueue(queue);
    }

    // Get queue status
    public Queue getQueueStatus(int studentId, int serviceId) {
        return queueDAO.getQueueStatus(studentId, serviceId);
    }

    // Get queue position
    public int getQueuePosition(int studentId, int serviceId) {
        return queueDAO.getQueuePosition(studentId, serviceId);
    }

    // Get current token
    public int getCurrentToken(int serviceId) {
        return queueDAO.getCurrentToken(serviceId);
    }
}