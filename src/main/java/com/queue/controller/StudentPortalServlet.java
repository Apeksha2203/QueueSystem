// VIVA GUIDE: Current student HTTP adapter. Routes login/register/session/overview/preview/reserve/cancel; obtains identity from the server session.
package com.queue.controller;

import com.queue.dao.CounterDAO;
import com.queue.dao.ServiceDAO;
import com.queue.model.Booking;
import com.queue.model.Service;
import com.queue.model.Student;
import com.queue.service.BookingService;
import com.queue.service.NoShowService;
import com.queue.service.StudentService;
import com.queue.service.WaitingTimeService;
import com.queue.util.DBConnection;
import com.queue.util.Json;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.Time;
import java.util.*;

/** Student session and presentation adapter. Business rules remain in the team's services. */
@WebServlet("/api/student/*")
public class StudentPortalServlet extends HttpServlet {
    private final com.queue.service.ReservationService reservations = new com.queue.service.ReservationService();
    private final StudentService students = new StudentService();
    private final ServiceDAO services = new ServiceDAO();
    private final BookingService bookings = new BookingService();
    private final WaitingTimeService waiting = new WaitingTimeService();
    private final CounterDAO counters = new CounterDAO();

    // Handle HTTP GET requests for this servlet mapping; validate input/session before returning HTML or JSON.
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException { handle(req, res); }
    // Handle HTTP POST requests for this servlet mapping; validate input/session before returning HTML or JSON.
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException { handle(req, res); }

    // Dispatches the student route, validates inputs and derives personal identity from the server session before accessing reservations.
    // The servlet is the HTTP boundary; ReservationService owns transactional queue rules.
    private void handle(HttpServletRequest req, HttpServletResponse res) throws IOException {
        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");
        res.setHeader("Cache-Control", "no-store");
        req.setCharacterEncoding("UTF-8");
        String path = Objects.toString(req.getPathInfo(), "");
        boolean post = "POST".equals(req.getMethod());
        if (post && !"XMLHttpRequest".equals(req.getHeader("X-Requested-With"))) {
            fail(res, 403, "Use the student application to submit this request."); return;
        }
        try {
            if (!post && "/session".equals(path)) {
                HttpSession session = req.getSession(false);
                ok(res, session == null ? null : session.getAttribute("student")); return;
            }
            if (post && "/logout".equals(path)) {
                HttpSession session = req.getSession(false);
                if (session != null) session.invalidate();
                ok(res, null); return;
            }
            // Legacy login/register/join DAOs hide database exceptions; probe only those paths.
            // Reservation reads/writes propagate SQL errors themselves and do not need an extra cloud connection.
            if (post && Set.of("/login","/register","/queue/join").contains(path)) try (Connection connection = DBConnection.getConnection()) {
                if (!connection.isValid(3)) throw new java.sql.SQLException("Database unavailable");
            }
            if (post && ("/login".equals(path) || "/register".equals(path))) {
                String email = required(req, "email");
                String password = required(req, "password");
                if ("/register".equals(path)) {
                    Student account = new Student();
                    account.setStudentName(required(req, "studentName"));
                    account.setEmail(email); account.setPassword(password); account.setPhone(req.getParameter("phone"));
                    if (!students.registerStudent(account)) { fail(res, 409, "Registration failed. That email may already be registered."); return; }
                }
                Student student = students.loginStudent(email, password);
                if (student == null) { fail(res, 401, "Invalid email or password."); return; }
                HttpSession previous = req.getSession(false);
                if (previous != null) previous.invalidate();
                HttpSession session = req.getSession(true);
                Map<String,Object> profile = Map.of("studentId", student.getStudentId(), "name", student.getStudentName(), "email", student.getEmail());
                session.setAttribute("studentId", student.getStudentId());
                session.setAttribute("student", profile); session.setMaxInactiveInterval(1800);
                ok(res, profile); return;
            }
            HttpSession session = req.getSession(false);
            if (session == null || !(session.getAttribute("studentId") instanceof Integer)) {
                fail(res, 401, "Please sign in to your student account."); return;
            }
            int studentId = (Integer) session.getAttribute("studentId");
            if (!post && "/overview".equals(path)) {
                ok(res, reservations.studentOverview(studentId)); return;
            }
            if (post && "/queue/join".equals(path)) {
                int serviceId = Integer.parseInt(required(req, "serviceId"));
                requireService(serviceId);
                com.queue.model.Queue previous = students.getQueueStatus(studentId, serviceId);
                if (previous != null && Set.of("WAITING", "CALLED", "SERVING").contains(previous.getStatus())) {
                    fail(res, 409, "You already have an active ticket for this service."); return;
                }
                com.queue.model.Queue entry = new com.queue.model.Queue();
                entry.setStudentId(studentId); entry.setServiceId(serviceId);
                if (!students.joinQueue(entry)) { fail(res, 409, "Unable to join the queue. Please try again."); return; }
                ok(res, Map.of("queueId", entry.getQueueId())); return;
            }
            if (!post && "/bookings".equals(path)) { ok(res,reservations.visits(studentId));return; }
            if (!post && "/bookings/preview".equals(path)) { int id=Integer.parseInt(required(req,"serviceId"));ok(res,reservations.preview(studentId,id));return; }
            if (post && "/bookings/create".equals(path)) {
                if(req.getParameter("bookingDate")!=null && !required(req,"bookingDate").equals(com.queue.service.ServiceHours.now().toLocalDate().toString()))throw new IllegalArgumentException("Only today's reservations are allowed.");
                ok(res,reservations.reserve(studentId,Integer.parseInt(required(req,"serviceId"))));return;
            }
            if (post && "/bookings/cancel".equals(path)) {
                if(!reservations.cancel(studentId,Integer.parseInt(required(req,"bookingId")))){fail(res,409,"Only your waiting reservation for today can be cancelled.");return;}
                ok(res,null);return;
            }
            if ("/bookings/slots".equals(path)) { fail(res,410,"Time slots have been replaced by today's queue reservations.");return; }            fail(res, 404, "Student operation not supported.");
        } catch (IllegalArgumentException error) { fail(res, 400, error.getMessage()==null?"Please supply valid required fields.":error.getMessage()); }
        catch (java.sql.SQLException error) { fail(res, 503, "The database is unavailable. Please try again shortly."); }
        catch (IllegalStateException error) { fail(res, 503, "The operation could not be saved. Please try again shortly."); }
    }
    // Rejects service identifiers not found in the current catalogue.
    private void requireService(int id) {
        if (services.getAllServices().stream().noneMatch(service -> service.getServiceId() == id))
            throw new IllegalArgumentException("Unknown service");
    }
    // Rejects absent/blank request input before the caller parses or uses it.
    private String required(HttpServletRequest req, String name) {
        String value = req.getParameter(name);
        if (value == null || value.isBlank()) throw new IllegalArgumentException(name);
        return value;
    }
    // Writes the student API success/data envelope using the JSON serializer.
    private void ok(HttpServletResponse res, Object data) throws IOException {
        Map<String,Object> body = new LinkedHashMap<>(); body.put("success", true); body.put("data", data);
        res.getWriter().write(Json.encode(body));
    }
    // Sets the HTTP failure status and writes a success:false message envelope.
    private void fail(HttpServletResponse res, int status, String message) throws IOException {
        res.setStatus(status); res.getWriter().write(Json.encode(Map.of("success", false, "message", message)));
    }
}
