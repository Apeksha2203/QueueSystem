// VIVA GUIDE: Staff HTTP adapter for login, profile, service queue, counter availability, summary and activity. Study service scoping and remaining legacy branches.
package com.queue.controller;

import com.queue.model.Staff;
import com.queue.service.StaffService;
import com.queue.model.Booking;
import com.queue.service.AnalyticsService;
import com.queue.service.BookingService;

import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebServlet("/api/staff/*")
public class StaffServlet extends HttpServlet {

    private StaffService staffService;
    private AnalyticsService analyticsService;
private BookingService bookingService;

    @Override
// Initialize servlet dependencies once when Tomcat creates the servlet.
public void init() {
    staffService = new StaffService();
    analyticsService = new AnalyticsService();
    bookingService = new BookingService();
}

    // =========================
    // POST /api/staff/login
    // =========================

    @Override
    // Handle HTTP POST requests for this servlet mapping; validate input/session before returning HTML or JSON.
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        String path = request.getPathInfo();

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        if ("/logout".equals(path)) {
            HttpSession current=request.getSession(false);
            if(current!=null)current.invalidate();
            response.getWriter().write("{\"success\":true,\"data\":null}");return;
        }
        if ("/login".equals(path)) {

            String email = request.getParameter("email");
            String password = request.getParameter("password");

            if (email == null || password == null ||
                email.isBlank() || password.isBlank()) {

                response.getWriter().write(
                    "{\"success\":false," +
                    "\"message\":\"Email and password are required\"," +
                    "\"data\":null}"
                );

                return;
            }

            Staff staff = staffService.login(email, password);

            if (staff != null) {

                HttpSession session = request.getSession();
                // Rotate the session identifier after authentication to reduce session-fixation risk.
                request.changeSessionId();
                session.removeAttribute("studentId");
                session.setAttribute("staffId", staff.getStaffId());

                String json =
                    "{"
                    + "\"success\":true,"
                    + "\"message\":\"Staff login successful\","
                    + "\"data\":{"
                    + "\"staffId\":" + staff.getStaffId() + ","
                    + "\"staffName\":\"" + staff.getStaffName() + "\","
                    + "\"email\":\"" + staff.getEmail() + "\","
                    + "\"serviceId\":" + staff.getServiceId() + ","
                    + "\"counterId\":" + staff.getCounterId()
                    + "}"
                    + "}";

                response.getWriter().write(json);

            } else {

                response.getWriter().write(
                    "{\"success\":false," +
                    "\"message\":\"Invalid email or password\"," +
                    "\"data\":null}"
                );
            }

            return;
        }

        if ("/counter/status".equals(path)) {

    HttpSession session = request.getSession(false);

    if (session == null ||
        session.getAttribute("staffId") == null) {
        response.setStatus(401);

        response.getWriter().write(
            "{\"success\":false," +
            "\"message\":\"Staff is not logged in\"," +
            "\"data\":null}"
        );

        return;
    }

    String status =
        request.getParameter("status");

    if (status == null || status.isBlank()) {

        response.getWriter().write(
            "{\"success\":false," +
            "\"message\":\"Status is required\"," +
            "\"data\":null}"
        );

        return;
    }

    status = status.toUpperCase();

    if (!status.equals("AVAILABLE") &&
        !status.equals("PAUSED") &&
        !status.equals("CHECKED_OUT")) {

        response.getWriter().write(
            "{\"success\":false," +
            "\"message\":\"Invalid status. Use AVAILABLE, PAUSED or CHECKED_OUT\"," +
            "\"data\":null}"
        );

        return;
    }

    int staffId =
        (Integer) session.getAttribute("staffId");

    boolean updated;
    try { updated=new com.queue.service.ReservationService().availability(staffId,status); }
    catch(Exception error){response.setStatus(409);response.getWriter().write(com.queue.util.Json.encode(java.util.Map.of("success",false,"message",error instanceof IllegalArgumentException?error.getMessage():"Could not update counter availability.")));return;}    if (!updated) {

        response.getWriter().write(
            "{\"success\":false," +
            "\"message\":\"Failed to update counter status\"," +
            "\"data\":null}"
        );

        return;
    }

    java.util.Map<String, Object> details =
        staffService.getCounterDetails(staffId);

    String json =
        "{"
        + "\"success\":true,"
        + "\"message\":\"Counter status updated\","
        + "\"data\":{"
        + "\"counterId\":" + details.get("counterId") + ","
        + "\"counterName\":\"" + details.get("counterName") + "\","
        + "\"serviceId\":" + details.get("serviceId") + ","
        + "\"serviceName\":\"" + details.get("serviceName") + "\","
        + "\"status\":\"" + status + "\""
        + "}"
        + "}";

    response.getWriter().write(json);

    return;
}

        response.getWriter().write(
            "{\"success\":false," +
            "\"message\":\"Invalid staff endpoint\"," +
            "\"data\":null}"
        );
    }

    // =========================
    // GET /api/staff/profile
    // =========================

    @Override
    // Handle HTTP GET requests for this servlet mapping; validate input/session before returning HTML or JSON.
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        String path = request.getPathInfo();
        if ("/queue".equals(path)) {
            response.setContentType("application/json");response.setCharacterEncoding("UTF-8");
            HttpSession actor=request.getSession(false);
            if(actor==null || !(actor.getAttribute("staffId") instanceof Integer)){response.setStatus(401);response.getWriter().write("{\"success\":false,\"message\":\"Staff is not logged in\"}");return;}
            try {response.getWriter().write(com.queue.util.Json.encode(java.util.Map.of("success",true,"data",new com.queue.service.ReservationService().staffQueue((Integer)actor.getAttribute("staffId")))));}
            catch(Exception error){response.setStatus(503);response.getWriter().write("{\"success\":false,\"message\":\"Queue unavailable\"}");}
            return;
        }

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        if ("/profile".equals(path)) {

            HttpSession session = request.getSession(false);

            if (session == null ||
                session.getAttribute("staffId") == null) {
        response.setStatus(401);

                response.getWriter().write(
                    "{\"success\":false," +
                    "\"message\":\"Staff is not logged in\"," +
                    "\"data\":null}"
                );

                return;
            }

            int staffId =
                (Integer) session.getAttribute("staffId");

            Staff staff =
                staffService.getStaffProfile(staffId);

            if (staff == null) {

                response.getWriter().write(
                    "{\"success\":false," +
                    "\"message\":\"Staff profile not found\"," +
                    "\"data\":null}"
                );

                return;
            }

            String json =
                "{"
                + "\"success\":true,"
                + "\"message\":\"Staff profile retrieved\","
                + "\"data\":{"
                + "\"staffId\":" + staff.getStaffId() + ","
                + "\"staffName\":\"" + staff.getStaffName() + "\","
                + "\"email\":\"" + staff.getEmail() + "\","
                + "\"serviceId\":" + staff.getServiceId() + ","
                + "\"counterId\":" + staff.getCounterId()
                + "}"
                + "}";

            response.getWriter().write(json);

            return;
        }

        if ("/counter/status".equals(path)) {

    HttpSession session = request.getSession(false);

    if (session == null ||
        session.getAttribute("staffId") == null) {
        response.setStatus(401);

        response.getWriter().write(
            "{\"success\":false," +
            "\"message\":\"Staff is not logged in\"," +
            "\"data\":null}"
        );

        return;
    }

    int staffId =
        (Integer) session.getAttribute("staffId");

    java.util.Map<String, Object> details =
        staffService.getCounterDetails(staffId);

    if (details.isEmpty()) {

        response.getWriter().write(
            "{\"success\":false," +
            "\"message\":\"Counter details not found\"," +
            "\"data\":null}"
        );

        return;
    }

    String status;
    try { status=new com.queue.service.ReservationService().availability(((Number)details.get("counterId")).intValue()); }
    catch(Exception error){response.setStatus(503);response.getWriter().write("{\"success\":false,\"message\":\"Counter status unavailable\"}");return;}    String json =
        "{"
        + "\"success\":true,"
        + "\"message\":\"Counter status retrieved\","
        + "\"data\":{"
        + "\"counterId\":" + details.get("counterId") + ","
        + "\"counterName\":\"" + details.get("counterName") + "\","
        + "\"serviceId\":" + details.get("serviceId") + ","
        + "\"serviceName\":\"" + details.get("serviceName") + "\","
        + "\"status\":\"" + status + "\""
        + "}"
        + "}";

    response.getWriter().write(json);

    return;
}

        if ("/dashboard-summary".equals(path)) {

    HttpSession session = request.getSession(false);

    if (session == null ||
        session.getAttribute("staffId") == null) {
        response.setStatus(401);

        response.getWriter().write(
            "{\"success\":false," +
            "\"message\":\"Staff is not logged in\"," +
            "\"data\":null}"
        );

        return;
    }

    int staffId =
        (Integer) session.getAttribute("staffId");

    Staff staff =
        staffService.getStaffProfile(staffId);

    if (staff == null) {

        response.getWriter().write(
            "{\"success\":false," +
            "\"message\":\"Staff profile not found\"," +
            "\"data\":null}"
        );

        return;
    }

    // Get active queue for staff's service
    List<com.queue.model.Queue> activeQueue =
        staffService.getActiveQueue(staffId);

    int studentsWaiting = 0;

    for (com.queue.model.Queue queue : activeQueue) {

        if ("WAITING".equals(queue.getStatus())) {
            studentsWaiting++;
        }
    }

    java.util.Map<String,Object> metrics;
    try {metrics=analyticsService.getServiceDaySummary(staff.getServiceId());}
    catch(java.sql.SQLException error){response.setStatus(503);response.getWriter().write("{\"success\":false,\"message\":\"Analytics temporarily unavailable\"}");return;}
    int servedToday=((Number)metrics.get("servedToday")).intValue();
    Object averageWaitMinutes=metrics.get("averageWaitMinutes");
    Object averageServiceMinutes=metrics.get("averageServiceMinutes");
    int noShows=((Number)metrics.get("noShowsToday")).intValue();
    String peakHour=(String)metrics.get("peakHour");
    // Get upcoming bookings for staff's service
    List<java.util.Map<String, Object>> upcomingBookings =
    bookingService.getUpcomingBookingsWithDetails(
        staff.getServiceId()
    );

    String json =
        "{"
        + "\"success\":true,"
        + "\"message\":\"Dashboard summary fetched successfully\","
        + "\"data\":{"
        + "\"studentsWaiting\":" + studentsWaiting + ","
        + "\"servedToday\":" + servedToday + ","
        + "\"averageWaitMinutes\":" + averageWaitMinutes + ","
        + "\"averageServiceMinutes\":" + averageServiceMinutes + ","
        + "\"noShowsToday\":" + noShows + ","
        + "\"peakHour\":\"" + peakHour + "\","
        + "\"upcomingBookings\":" + studentsWaiting
        + "}"
        + "}";

    response.getWriter().write(json);

    return;
}
if ("/recent-activity".equals(path)) {

    HttpSession session = request.getSession(false);

    if (session == null ||
        session.getAttribute("staffId") == null) {
        response.setStatus(401);

        response.getWriter().write(
            "{\"success\":false," +
            "\"message\":\"Staff is not logged in\"," +
            "\"data\":null}"
        );

        return;
    }

    int staffId =
        (Integer) session.getAttribute("staffId");

    Staff staff =
        staffService.getStaffProfile(staffId);

    if (staff == null) {

        response.getWriter().write(
            "{\"success\":false," +
            "\"message\":\"Staff profile not found\"," +
            "\"data\":null}"
        );

        return;
    }

    List<com.queue.model.Queue> activityList =
        staffService.getRecentActivity(staffId);

    StringBuilder json =
        new StringBuilder();

    json.append("{")
        .append("\"success\":true,")
        .append("\"message\":\"Recent activity retrieved\",")
        .append("\"data\":[");

    boolean firstActivity = true;

    for (com.queue.model.Queue queue : activityList) {

        if (queue.getCalledAt() != null) {

            if (!firstActivity) {
                json.append(",");
            }

            json.append("{")
                .append("\"tokenNumber\":")
                .append(queue.getTokenNumber())
                .append(",")
                .append("\"action\":\"CALLED\",")
                .append("\"timestamp\":\"")
                .append(queue.getCalledAt())
                .append("\"")
                .append("}");

            firstActivity = false;
        }

        if (queue.getStartedAt() != null) {

            if (!firstActivity) {
                json.append(",");
            }

            json.append("{")
                .append("\"tokenNumber\":")
                .append(queue.getTokenNumber())
                .append(",")
                .append("\"action\":\"SERVICE_STARTED\",")
                .append("\"timestamp\":\"")
                .append(queue.getStartedAt())
                .append("\"")
                .append("}");

            firstActivity = false;
        }

        if (queue.getCompletedAt() != null) {

            if (!firstActivity) {
                json.append(",");
            }

            String action =
                "SKIPPED".equals(queue.getStatus())
                    ? "SKIPPED"
                    : queue.getStatus();

            json.append("{")
                .append("\"tokenNumber\":")
                .append(queue.getTokenNumber())
                .append(",")
                .append("\"action\":\"")
                .append(action)
                .append("\",")
                .append("\"timestamp\":\"")
                .append(queue.getCompletedAt())
                .append("\"")
                .append("}");

            firstActivity = false;
        }
    }

    json.append("]}");

    response.getWriter().write(
        json.toString()
    );

    return;
}


        if ("/queue".equals(path)) {

    HttpSession session = request.getSession(false);

    if (session == null ||
        session.getAttribute("staffId") == null) {
        response.setStatus(401);

        response.getWriter().write(
            "{\"success\":false," +
            "\"message\":\"Staff is not logged in\"," +
            "\"data\":null}"
        );

        return;
    }

    int staffId =
        (Integer) session.getAttribute("staffId");

    java.util.List<com.queue.model.Queue> queueList =
        staffService.getActiveQueue(staffId);

    StringBuilder json =
        new StringBuilder();

    json.append("{")
        .append("\"success\":true,")
        .append("\"message\":\"Active queue retrieved\",")
        .append("\"data\":[");

    for (int i = 0; i < queueList.size(); i++) {

        com.queue.model.Queue queue =
            queueList.get(i);

        if (i > 0) {
            json.append(",");
        }

        json.append("{")
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
            .append(queue.getStatus())
            .append("\"")
            .append("}");
    }

    json.append("]}");

    response.getWriter().write(json.toString());

    return;
}

        response.getWriter().write(
            "{\"success\":false," +
            "\"message\":\"Invalid staff endpoint\"," +
            "\"data\":null}"
        );
    }
}
