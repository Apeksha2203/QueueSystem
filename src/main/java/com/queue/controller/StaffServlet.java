package com.queue.controller;

import java.io.IOException;
import java.util.List;

import com.queue.model.Queue;
import com.queue.model.Staff;
import com.queue.service.StaffService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/api/staff/*")
public class StaffServlet extends HttpServlet {

    private StaffService staffService;

    @Override
    public void init() {
        staffService = new StaffService();
    }

    // =========================================================
    // POST /api/staff/*
    // =========================================================
    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String path = request.getPathInfo();

        // -----------------------------------------------------
        // POST /api/staff/login
        // -----------------------------------------------------
        if ("/login".equals(path)) {

            String email = request.getParameter("email");
            String password = request.getParameter("password");

            if (email == null || password == null
                    || email.isBlank() || password.isBlank()) {

                writeResponse(
                        response,
                        false,
                        "Email and password are required",
                        null
                );

                return;
            }

            email = email.trim();

            Staff staff = staffService.login(email, password);

            if (staff == null) {

                writeResponse(
                        response,
                        false,
                        "Invalid email or password",
                        null
                );

                return;
            }

            /*
             * IMPORTANT:
             * Remove any old session and create a fresh session
             * after successful authentication.
             */
            HttpSession oldSession = request.getSession(false);

            if (oldSession != null) {
                oldSession.invalidate();
            }

            HttpSession session = request.getSession(true);

            session.setAttribute(
                    "staffId",
                    staff.getStaffId()
            );

            session.setMaxInactiveInterval(30 * 60);

            String data
                    = "{"
                    + "\"staffId\":" + staff.getStaffId() + ","
                    + "\"staffName\":\"" + escapeJson(staff.getStaffName()) + "\","
                    + "\"email\":\"" + escapeJson(staff.getEmail()) + "\","
                    + "\"serviceId\":" + staff.getServiceId() + ","
                    + "\"counterId\":" + staff.getCounterId()
                    + "}";

            writeResponse(
                    response,
                    true,
                    "Staff login successful",
                    data
            );

            return;
        }

        // -----------------------------------------------------
        // POST /api/staff/counter/status
        // -----------------------------------------------------
        if ("/counter/status".equals(path)) {

            HttpSession session = request.getSession(false);

            if (session == null
                    || session.getAttribute("staffId") == null) {

                writeResponse(
                        response,
                        false,
                        "Staff is not logged in",
                        null
                );

                return;
            }

            String activeParameter
                    = request.getParameter("active");

            if (activeParameter == null) {

                writeResponse(
                        response,
                        false,
                        "Active status is required",
                        null
                );

                return;
            }

            boolean active
                    = Boolean.parseBoolean(activeParameter);

            int staffId
                    = (Integer) session.getAttribute("staffId");

            boolean updated
                    = staffService.updateCounterStatus(
                            staffId,
                            active
                    );

            if (!updated) {

                writeResponse(
                        response,
                        false,
                        "Failed to update counter status",
                        null
                );

                return;
            }

            String data
                    = "{\"active\":" + active + "}";

            writeResponse(
                    response,
                    true,
                    "Counter status updated",
                    data
            );

            return;
        }

        writeResponse(
                response,
                false,
                "Invalid staff endpoint",
                null
        );
    }

    // =========================================================
    // GET /api/staff/*
    // =========================================================
    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String path = request.getPathInfo();

        // -----------------------------------------------------
        // GET /api/staff/profile
        // -----------------------------------------------------
        if ("/profile".equals(path)) {

            HttpSession session
                    = request.getSession(false);

            if (session == null
                    || session.getAttribute("staffId") == null) {

                writeResponse(
                        response,
                        false,
                        "Staff is not logged in",
                        null
                );

                return;
            }

            int staffId
                    = (Integer) session.getAttribute("staffId");

            Staff staff
                    = staffService.getStaffProfile(staffId);

            if (staff == null) {

                writeResponse(
                        response,
                        false,
                        "Staff profile not found",
                        null
                );

                return;
            }

            String data
                    = "{"
                    + "\"staffId\":" + staff.getStaffId() + ","
                    + "\"staffName\":\"" + escapeJson(staff.getStaffName()) + "\","
                    + "\"email\":\"" + escapeJson(staff.getEmail()) + "\","
                    + "\"serviceId\":" + staff.getServiceId() + ","
                    + "\"counterId\":" + staff.getCounterId()
                    + "}";

            writeResponse(
                    response,
                    true,
                    "Staff profile retrieved",
                    data
            );

            return;
        }

        // -----------------------------------------------------
        // GET /api/staff/queue
        // -----------------------------------------------------
        if ("/queue".equals(path)) {

            HttpSession session
                    = request.getSession(false);

            if (session == null
                    || session.getAttribute("staffId") == null) {

                writeResponse(
                        response,
                        false,
                        "Staff is not logged in",
                        null
                );

                return;
            }

            int staffId
                    = (Integer) session.getAttribute("staffId");

            List<Queue> queueList
                    = staffService.getActiveQueue(staffId);

            StringBuilder data
                    = new StringBuilder("[");

            for (int i = 0; i < queueList.size(); i++) {

                Queue queue = queueList.get(i);

                if (i > 0) {
                    data.append(",");
                }

                data.append("{")
                        .append("\"queueId\":")
                        .append(queue.getQueueId())
                        .append(",")
                        .append("\"studentId\":")
                        .append(queue.getStudentId())
                        .append(",")
                        .append("\"serviceId\":")
                        .append(queue.getServiceId())
                        .append(",")
                        .append("\"tokenNumber\":")
                        .append(queue.getTokenNumber())
                        .append(",")
                        .append("\"status\":\"")
                        .append(escapeJson(queue.getStatus()))
                        .append("\"")
                        .append("}");
            }

            data.append("]");

            writeResponse(
                    response,
                    true,
                    "Active queue retrieved",
                    data.toString()
            );

            return;
        }

        writeResponse(
                response,
                false,
                "Invalid staff endpoint",
                null
        );
    }

    // =========================================================
    // JSON RESPONSE HELPERS
    // =========================================================
    private void writeResponse(
            HttpServletResponse response,
            boolean success,
            String message,
            String data)
            throws IOException {

        StringBuilder json
                = new StringBuilder();

        json.append("{")
                .append("\"success\":")
                .append(success)
                .append(",")
                .append("\"message\":\"")
                .append(escapeJson(message))
                .append("\",")
                .append("\"data\":");

        if (data == null) {
            json.append("null");
        } else {
            json.append(data);
        }

        json.append("}");

        response.getWriter().write(
                json.toString()
        );
    }

    private String escapeJson(String value) {

        if (value == null) {
            return "";
        }

        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "\\r")
                .replace("\n", "\\n");
    }
}
