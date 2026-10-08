// VIVA GUIDE: Student business adapter around registration/login DAO operations and server input validation.
package com.queue.service;

import com.queue.dao.QueueDAO;
import com.queue.dao.StudentDAO;
import com.queue.model.Queue;
import com.queue.model.Student;

public class StudentService {

    private QueueDAO queueDAO;
    private StudentDAO studentDAO;

    // Initialize this object; inspect arguments/field assignments for the values it carries.
    public StudentService() {
        queueDAO = new QueueDAO();
        studentDAO = new StudentDAO();
    }

    // Student registration
    // Operation registerStudent: follow the inputs, validation and return value in the block below; the file header explains its layer/caller.
    public boolean registerStudent(Student student) {
        return studentDAO.registerStudent(student);
    }

    // Student login
    // Operation loginStudent: follow the inputs, validation and return value in the block below; the file header explains its layer/caller.
    public Student loginStudent(String email, String password) {
        return studentDAO.loginStudent(email, password);
    }

    // Join queue
    // Perform join queue; inspect its conditions, affected rows and return value before describing success.
    public boolean joinQueue(Queue queue) {
        return queueDAO.joinQueue(queue);
    }

    // Get queue status
    // Retrieve queue status for the caller; follow the SQL/service delegation to identify scope and return shape.
    public Queue getQueueStatus(int studentId, int serviceId) {
        return queueDAO.getQueueStatus(studentId, serviceId);
    }

    // Get queue position
    // Retrieve queue position for the caller; follow the SQL/service delegation to identify scope and return shape.
    public int getQueuePosition(int studentId, int serviceId) {
        return queueDAO.getQueuePosition(studentId, serviceId);
    }

    // Get current token
    // Retrieve current token for the caller; follow the SQL/service delegation to identify scope and return shape.
    public int getCurrentToken(int serviceId) {
        return queueDAO.getCurrentToken(serviceId);
    }
}
