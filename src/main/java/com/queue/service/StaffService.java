// VIVA GUIDE: Staff business adapter. Resolves login/profile and validates account/counter service assignments.
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

    // Initialize this object; inspect arguments/field assignments for the values it carries.
    public StaffService() {
        staffDAO = new StaffDAO();
        queueDAO = new QueueDAO();
        counterDAO = new CounterDAO();
    }

    // Operation login: follow the inputs, validation and return value in the block below; the file header explains its layer/caller.
    public Staff login(String email, String password) {
        return staffDAO.loginStaff(email, password);
    }

    // Retrieve staff profile for the caller; follow the SQL/service delegation to identify scope and return shape.
    public Staff getStaffProfile(int staffId) {
        return staffDAO.getStaffById(staffId);
    }

    // Perform call next student; inspect its conditions, affected rows and return value before describing success.
    public Queue callNextStudent(int serviceId) {
        return queueDAO.callNext(serviceId);
    }

    // Perform call student; inspect its conditions, affected rows and return value before describing success.
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

    // Perform start service; inspect its conditions, affected rows and return value before describing success.
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

    // Perform complete service; inspect its conditions, affected rows and return value before describing success.
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

    // Perform skip student; inspect its conditions, affected rows and return value before describing success.
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

    // Retrieve active queue for the caller; follow the SQL/service delegation to identify scope and return shape.
    public java.util.List<Queue> getActiveQueue(int staffId) {

    Staff staff = staffDAO.getStaffById(staffId);

    if (staff == null) {
        return new java.util.ArrayList<>();
    }

    return queueDAO.getActiveQueue(staff.getServiceId());
}

// Perform update counter status; inspect its conditions, affected rows and return value before describing success.
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

public java.util.Map<String, Object> getCounterDetails(int staffId) {

    Staff staff = staffDAO.getStaffById(staffId);

    if (staff == null) {
        return new java.util.HashMap<>();
    }

    return counterDAO.getCounterDetails(
        staff.getCounterId()
    );
}

// Retrieve recent activity for the caller; follow the SQL/service delegation to identify scope and return shape.
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
