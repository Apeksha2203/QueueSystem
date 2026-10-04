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
public void init() {
    staffService = new StaffService();
    analyticsService = new AnalyticsService();
    bookingService = new BookingService();
}

    // =========================
    // POST /api/staff/login
    // =========================

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        String path = request.getPathInfo();

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

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

        response.getWriter().write(
            "{\"success\":false," +
            "\"message\":\"Staff is not logged in\"," +
            "\"data\":null}"
        );

        return;
    }

    String activeParameter =
        request.getParameter("active");

    if (activeParameter == null) {

        response.getWriter().write(
            "{\"success\":false," +
            "\"message\":\"Active status is required\"," +
            "\"data\":null}"
        );

        return;
    }

    boolean active =
        Boolean.parseBoolean(activeParameter);

    int staffId =
        (Integer) session.getAttribute("staffId");

    boolean updated =
        staffService.updateCounterStatus(
            staffId,
            active
        );

    if (updated) {

        response.getWriter().write(
            "{\"success\":true," +
            "\"message\":\"Counter status updated\"," +
            "\"data\":{\"active\":" +
            active +
            "}}"
        );

    } else {

        response.getWriter().write(
            "{\"success\":false," +
            "\"message\":\"Failed to update counter status\"," +
            "\"data\":null}"
        );
    }

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
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        String path = request.getPathInfo();

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        if ("/profile".equals(path)) {

            HttpSession session = request.getSession(false);

            if (session == null ||
                session.getAttribute("staffId") == null) {

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

        if ("/dashboard-summary".equals(path)) {

    HttpSession session = request.getSession(false);

    if (session == null ||
        session.getAttribute("staffId") == null) {

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

    // Get analytics
    int servedToday =
        analyticsService.getStudentsServedToday();

    double averageWaitMinutes =
        analyticsService.getAverageWaitingTime();

    double averageServiceMinutes =
        analyticsService.getAverageServiceTime();

    int noShows =
        analyticsService.getNoShowCount();

    String peakHour =
        analyticsService.getPeakHour();

    // Get upcoming bookings for staff's service
    List<Booking> upcomingBookings =
        bookingService.getUpcomingBookings(
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
        + "\"upcomingBookings\":" + upcomingBookings.size()
        + "}"
        + "}";

    response.getWriter().write(json);

    return;
}
if ("/recent-activity".equals(path)) {

    HttpSession session = request.getSession(false);

    if (session == null ||
        session.getAttribute("staffId") == null) {

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
                    : "COMPLETED";

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