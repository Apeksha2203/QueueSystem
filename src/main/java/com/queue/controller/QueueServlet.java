// VIVA GUIDE: Queue action HTTP adapter. Authenticates staff and delegates actions into ReservationService; retained routes require care.
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
    // Initialize servlet dependencies once when Tomcat creates the servlet.
    public void init() {
    staffService = new StaffService();
    studentService = new StudentService();
}

    // =========================
    // POST QUEUE OPERATIONS
    // =========================

    @Override
    // Handle HTTP POST requests for this servlet mapping; validate input/session before returning HTML or JSON.
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        String path = request.getPathInfo();

        // The public skip endpoint is retired (410); confirmed missed calls use the current no-show rule.
        if ("/skip".equals(path)) {response.setStatus(410);response.setContentType("application/json");response.getWriter().write("{\"success\":false,\"message\":\"Skip is retired. Use confirmed missed call for absent students.\"}");return;}
        if (java.util.Set.of("/call-next","/call","/start","/complete","/skip","/no-show").contains(java.util.Objects.toString(path,""))) {
            response.setContentType("application/json");response.setCharacterEncoding("UTF-8");
            HttpSession actor=request.getSession(false);
            if(actor==null || !(actor.getAttribute("staffId") instanceof Integer)) {
                response.setStatus(401);writeError(response,"Staff is not logged in");return;
            }
            try {
                Object result=new com.queue.service.ReservationService().operate((Integer)actor.getAttribute("staffId"),path.substring(1),getQueueId(request));
                if(result==null){response.setStatus(409);writeError(response,"No students are currently waiting");return;}
                response.getWriter().write(com.queue.util.Json.encode(java.util.Map.of("success",true,"data",result)));return;
            }catch(IllegalArgumentException error){response.setStatus(409);writeError(response,error.getMessage());return;}
            catch(java.sql.SQLException error){response.setStatus(503);writeError(response,"The operation could not be saved. Please try again.");return;}
        }

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
// CALL SPECIFIC STUDENT
// POST /api/queue/call
// =========================

if ("/call".equals(path)) {

    int queueId = getQueueId(request);

    HttpSession session = request.getSession(false);

    if (session == null ||
        session.getAttribute("staffId") == null) {

        writeError(response, "Staff is not logged in");
        return;
    }

    int staffId =
            (Integer) session.getAttribute("staffId");

    if (queueId <= 0) {

        writeError(
            response,
            "Valid queueId is required"
        );

        return;
    }

    Queue queue =
            staffService.callStudent(
                staffId,
                queueId
            );

    if (queue == null) {

        writeError(
            response,
            "Unable to call student"
        );

        return;
    }

    String json =
            "{"
            + "\"success\":true,"
            + "\"message\":\"Student called successfully\","
            + "\"data\":{"
            + "\"queueId\":" + queue.getQueueId() + ","
            + "\"studentId\":" + queue.getStudentId() + ","
            + "\"serviceId\":" + queue.getServiceId() + ","
            + "\"tokenNumber\":" + queue.getTokenNumber() + ","
            + "\"status\":\"" + queue.getStatus() + "\""
            + "}"
            + "}";

    response.getWriter().write(json);
    return;
}

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
// Handle HTTP GET requests for this servlet mapping; validate input/session before returning HTML or JSON.
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

    // Retrieve queue id for the caller; follow the SQL/service delegation to identify scope and return shape.
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

    // Operation writeSuccess: follow the inputs, validation and return value in the block below; the file header explains its layer/caller.
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

    // Operation writeError: follow the inputs, validation and return value in the block below; the file header explains its layer/caller.
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
