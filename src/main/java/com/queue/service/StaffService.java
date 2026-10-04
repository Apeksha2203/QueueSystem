package com.queue.service;

import com.queue.dao.StaffDAO;
import com.queue.dao.QueueDAO;
import com.queue.dao.CounterDAO;
import com.queue.model.Staff;
import com.queue.model.Queue;

public class StaffService {

    private StaffDAO staffDAO;
    private QueueDAO queueDAO;
    private CounterDAO counterDAO;

    public StaffService() {
        staffDAO = new StaffDAO();
        queueDAO = new QueueDAO();
        counterDAO = new CounterDAO();
    }

    public Staff login(String email, String password) {
        return staffDAO.loginStaff(email, password);
    }

    public Staff getStaffProfile(int staffId) {
        return staffDAO.getStaffById(staffId);
    }

    public Queue callNextStudent(int serviceId) {
        return queueDAO.callNext(serviceId);
    }

    public Queue callStudent(int staffId, int queueId) {

    Staff staff = staffDAO.getStaffById(staffId);
    Queue queue = queueDAO.getQueueById(queueId);

    if (staff == null || queue == null) {
        return null;
    }

    if (staff.getServiceId() != queue.getServiceId()) {
        return null;
    }

    return queueDAO.callStudent(queueId);
}

    public boolean startService(int staffId, int queueId) {

        Staff staff = staffDAO.getStaffById(staffId);
        Queue queue = queueDAO.getQueueById(queueId);

        if (staff == null || queue == null) {
            return false;
        }

        if (staff.getServiceId() != queue.getServiceId()) {
            return false;
        }

        return queueDAO.startService(queueId);
    }

    public boolean completeService(int staffId, int queueId) {

        Staff staff = staffDAO.getStaffById(staffId);
        Queue queue = queueDAO.getQueueById(queueId);

        if (staff == null || queue == null) {
            return false;
        }

        if (staff.getServiceId() != queue.getServiceId()) {
            return false;
        }

        return queueDAO.completeService(queueId);
    }

    public boolean skipStudent(int staffId, int queueId) {

        Staff staff = staffDAO.getStaffById(staffId);
        Queue queue = queueDAO.getQueueById(queueId);

        if (staff == null || queue == null) {
            return false;
        }

        if (staff.getServiceId() != queue.getServiceId()) {
            return false;
        }

        return queueDAO.skipStudent(queueId);
    }

    public java.util.List<Queue> getActiveQueue(int staffId) {

    Staff staff = staffDAO.getStaffById(staffId);

    if (staff == null) {
        return new java.util.ArrayList<>();
    }

    return queueDAO.getActiveQueue(staff.getServiceId());
}

public boolean updateCounterStatus(int staffId, boolean active) {

    Staff staff = staffDAO.getStaffById(staffId);

    if (staff == null) {
        return false;
    }

    return counterDAO.updateCounterStatus(
        staff.getCounterId(),
        active
    );
}

public java.util.List<Queue> getRecentActivity(int staffId) {

    Staff staff = staffDAO.getStaffById(staffId);

    if (staff == null) {
        return new java.util.ArrayList<>();
    }

    return queueDAO.getRecentActivity(
        staff.getServiceId()
    );
}
}