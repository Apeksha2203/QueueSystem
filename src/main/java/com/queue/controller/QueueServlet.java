package com.queue.controller;

import com.queue.model.Queue;
import com.queue.service.StaffService;
import com.queue.service.StudentService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebServlet("/api/queue/*")
public class QueueServlet extends HttpServlet {

    private StaffService staffService;
    private StudentService studentService;

    @Override
    public void init() {
    staffService = new StaffService();
    studentService = new StudentService();
}

    // =========================
    // POST QUEUE OPERATIONS
    // =========================

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        String path = request.getPathInfo();

        if ("/join".equals(path)) {

    String studentIdParameter =
            request.getParameter("studentId");

    String serviceIdParameter =
            request.getParameter("serviceId");

    if (studentIdParameter == null ||
        serviceIdParameter == null) {

        writeError(
            response,
            "studentId and serviceId are required"
        );

        return;
    }

    try {

        int studentId =
                Integer.parseInt(studentIdParameter);

        int serviceId =
                Integer.parseInt(serviceIdParameter);

        Queue queue = new Queue();

        queue.setStudentId(studentId);
        queue.setServiceId(serviceId);

        boolean success =
                studentService.joinQueue(queue);

        if (success) {

            response.getWriter().write(
                "{"
                + "\"success\":true,"
                + "\"message\":\"Joined queue successfully\","
                + "\"data\":{"
                + "\"queueId\":" + queue.getQueueId() + ","
                + "\"studentId\":" + queue.getStudentId() + ","
                + "\"serviceId\":" + queue.getServiceId() + ","
                + "\"tokenNumber\":" + queue.getTokenNumber() + ","
                + "\"status\":\"" + queue.getStatus() + "\""
                + "}"
                + "}"
            );

        } else {

            writeError(
                response,
                "Unable to join queue"
            );
        }

    } catch (NumberFormatException e) {

        writeError(
            response,
            "studentId and serviceId must be valid numbers"
        );
    }

    return;
}

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // =========================
        // CALL NEXT
        // POST /api/queue/call-next
        // =========================

        if ("/call-next".equals(path)) {

            HttpSession session = request.getSession(false);

            if (session == null ||
                session.getAttribute("staffId") == null) {

                writeError(response, "Staff is not logged in");
                return;
            }

            int staffId =
                    (Integer) session.getAttribute("staffId");

            com.queue.dao.StaffDAO staffDAO =
                    new com.queue.dao.StaffDAO();

            com.queue.model.Staff staff =
                    staffDAO.getStaffById(staffId);

            if (staff == null) {
                writeError(response, "Staff profile not found");
                return;
            }

            Queue queue =
                    staffService.callNextStudent(
                            staff.getServiceId());

            if (queue == null) {

                writeError(
                    response,
                    "No students are currently waiting"
                );

                return;
            }

            String json =
                    "{"
                    + "\"success\":true,"
                    + "\"message\":\"Next student called\","
                    + "\"data\":{"
                    + "\"queueId\":" + queue.getQueueId() + ","
                    + "\"studentId\":" + queue.getStudentId() + ","
                    + "\"serviceId\":" + queue.getServiceId() + ","
                    + "\"tokenNumber\":" + queue.getTokenNumber() + ","
                    + "\"status\":\"" + queue.getStatus() + "\","
                    + "\"counterId\":" + staff.getCounterId()
                    + "}"
                    + "}";

            response.getWriter().write(json);
            return;
        }

        // =========================
        // START SERVICE
        // POST /api/queue/start
        // =========================

        if ("/start".equals(path)) {

            int queueId = getQueueId(request);
            HttpSession session = request.getSession(false);

if (session == null ||
    session.getAttribute("staffId") == null) {
    writeError(response, "Staff is not logged in");
    return;
}

int staffId = (Integer) session.getAttribute("staffId");

            if (queueId <= 0) {
                writeError(response, "Valid queueId is required");
                return;
            }

            boolean success =
        staffService.startService(staffId, queueId);

            if (success) {

                writeSuccess(
                    response,
                    "Service started"
                );

            } else {

                writeError(
                    response,
                    "Unable to start service"
                );
            }

            return;
        }

        // =========================
        // COMPLETE SERVICE
        // POST /api/queue/complete
        // =========================

        if ("/complete".equals(path)) {

            int queueId = getQueueId(request);
            HttpSession session = request.getSession(false);

if (session == null ||
    session.getAttribute("staffId") == null) {
    writeError(response, "Staff is not logged in");
    return;
}

int staffId = (Integer) session.getAttribute("staffId");

            if (queueId <= 0) {
                writeError(response, "Valid queueId is required");
                return;
            }

            boolean success =
        staffService.completeService(staffId, queueId);
            if (success) {

                writeSuccess(
                    response,
                    "Service completed"
                );

            } else {

                writeError(
                    response,
                    "Unable to complete service"
                );
            }

            return;
        }

        // =========================
        // SKIP STUDENT
        // POST /api/queue/skip
        // =========================

        if ("/skip".equals(path)) {

            int queueId = getQueueId(request);
            HttpSession session = request.getSession(false);

if (session == null ||
    session.getAttribute("staffId") == null) {
    writeError(response, "Staff is not logged in");
    return;
}

int staffId = (Integer) session.getAttribute("staffId");

            if (queueId <= 0) {
                writeError(response, "Valid queueId is required");
                return;
            }

            boolean success =
        staffService.skipStudent(staffId, queueId);

            if (success) {

                writeSuccess(
                    response,
                    "Student skipped"
                );

            } else {

                writeError(
                    response,
                    "Unable to skip student"
                );
            }

            return;
        }

        writeError(response, "Invalid queue endpoint");
    }

    // =========================
    // GET QUEUE STATUS
    // =========================

@Override
protected void doGet(HttpServletRequest request,
                     HttpServletResponse response)
        throws ServletException, IOException {

    response.setContentType("application/json");
    response.setCharacterEncoding("UTF-8");

    String path = request.getPathInfo();

    // GET /api/queue/status
    if ("/status".equals(path)) {

        String studentIdParameter =
                request.getParameter("studentId");

        String serviceIdParameter =
                request.getParameter("serviceId");

        if (studentIdParameter == null ||
            serviceIdParameter == null) {

            writeError(
                response,
                "studentId and serviceId are required"
            );

            return;
        }

        try {

            int studentId =
                    Integer.parseInt(studentIdParameter);

            int serviceId =
                    Integer.parseInt(serviceIdParameter);

            Queue queue =
                    studentService.getQueueStatus(
                        studentId,
                        serviceId
                    );

            if (queue != null) {

                response.getWriter().write(
                    "{"
                    + "\"success\":true,"
                    + "\"message\":\"Queue status retrieved successfully\","
                    + "\"data\":{"
                    + "\"queueId\":" + queue.getQueueId() + ","
                    + "\"studentId\":" + queue.getStudentId() + ","
                    + "\"serviceId\":" + queue.getServiceId() + ","
                    + "\"tokenNumber\":" + queue.getTokenNumber() + ","
                    + "\"status\":\"" + queue.getStatus() + "\""
                    + "}"
                    + "}"
                );

            } else {

                writeError(
                    response,
                    "No queue entry found"
                );
            }

        } catch (NumberFormatException e) {

            writeError(
                response,
                "studentId and serviceId must be valid numbers"
            );
        }

        return;
    }

    // GET /api/queue/position
    if ("/position".equals(path)) {

        String positionStudentIdParameter =
                request.getParameter("studentId");

        String positionServiceIdParameter =
                request.getParameter("serviceId");

        if (positionStudentIdParameter == null ||
            positionServiceIdParameter == null) {

            writeError(
                response,
                "studentId and serviceId are required"
            );

            return;
        }

        try {

            int studentId =
                    Integer.parseInt(
                        positionStudentIdParameter
                    );

            int serviceId =
                    Integer.parseInt(
                        positionServiceIdParameter
                    );

            int position =
                    studentService.getQueuePosition(
                        studentId,
                        serviceId
                    );

            response.getWriter().write(
                "{"
                + "\"success\":true,"
                + "\"message\":\"Queue position retrieved successfully\","
                + "\"data\":{"
                + "\"studentId\":" + studentId + ","
                + "\"serviceId\":" + serviceId + ","
                + "\"position\":" + position
                + "}"
                + "}"
            );

        } catch (NumberFormatException e) {

            writeError(
                response,
                "studentId and serviceId must be valid numbers"
            );
        }

        return;
    }

    // GET /api/queue/current-token
    if ("/current-token".equals(path)) {

        String currentTokenServiceIdParameter =
                request.getParameter("serviceId");

        if (currentTokenServiceIdParameter == null) {

            writeError(
                response,
                "serviceId is required"
            );

            return;
        }

        try {

            int serviceId =
                    Integer.parseInt(
                        currentTokenServiceIdParameter
                    );

            int currentToken =
                    studentService.getCurrentToken(
                        serviceId
                    );

            response.getWriter().write(
                "{"
                + "\"success\":true,"
                + "\"message\":\"Current token retrieved successfully\","
                + "\"data\":{"
                + "\"serviceId\":" + serviceId + ","
                + "\"currentToken\":" + currentToken
                + "}"
                + "}"
            );

        } catch (NumberFormatException e) {

            writeError(
                response,
                "serviceId must be a valid number"
            );
        }

        return;
    }

    writeError(
        response,
        "GET operation not available for this endpoint"
    );
}

    // =========================
    // GET QUEUE ID
    // =========================

    private int getQueueId(HttpServletRequest request) {

        String queueIdParameter =
                request.getParameter("queueId");

        if (queueIdParameter == null ||
            queueIdParameter.isBlank()) {

            return -1;
        }

        try {

            return Integer.parseInt(queueIdParameter);

        } catch (NumberFormatException e) {

            return -1;
        }
    }

    // =========================
    // SUCCESS RESPONSE
    // =========================

    private void writeSuccess(
            HttpServletResponse response,
            String message)
            throws IOException {

        response.getWriter().write(
            "{"
            + "\"success\":true,"
            + "\"message\":\"" + message + "\","
            + "\"data\":null"
            + "}"
        );
    }

    // =========================
    // ERROR RESPONSE
    // =========================

    private void writeError(
            HttpServletResponse response,
            String message)
            throws IOException {

        response.getWriter().write(
            "{"
            + "\"success\":false,"
            + "\"message\":\"" + message + "\","
            + "\"data\":null"
            + "}"
        );
    }
}