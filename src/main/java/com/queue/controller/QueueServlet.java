package com.queue.controller;

import com.queue.model.Queue;
import com.queue.service.StaffService;

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

    @Override
    public void init() {
        staffService = new StaffService();
    }

    // =========================
    // POST QUEUE OPERATIONS
    // =========================

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        String path = request.getPathInfo();

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