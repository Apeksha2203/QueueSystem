// VIVA GUIDE: Request filter protecting older endpoints using authenticated ownership/service checks. Not a substitute for current service validation.
package com.queue.filter;

import com.queue.service.BookingService;
import com.queue.util.Json;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

/** Protect the existing personal student routes as well as the new portal. */
@WebFilter(urlPatterns = {"/api/queue/*", "/api/bookings/*", "/create-booking", "/cancel-booking", "/booking-status"})
public class StudentOwnershipFilter implements Filter {
    // Checks authenticated identity against claimed IDs/booking ownership on protected older routes before invoking the next filter/servlet.
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        String route = req.getServletPath() + (req.getPathInfo() == null ? "" : req.getPathInfo());
        if ("/api/bookings/upcoming".equals(route)) {
            HttpSession staffSession = req.getSession(false);
            if (staffSession == null || staffSession.getAttribute("staffId") == null) {
                reject(res, 401, "Staff sign-in is required."); return;
            }
        }
        boolean personal = Set.of("/api/queue/join", "/api/queue/status", "/api/queue/position",
            "/api/bookings/create", "/api/bookings/cancel", "/api/bookings/status",
            "/create-booking", "/cancel-booking", "/booking-status").contains(route);
        if (!personal) { chain.doFilter(request, response); return; }
        HttpSession session = req.getSession(false);
        if (session == null || !(session.getAttribute("studentId") instanceof Integer)) {
            reject(res, 401, "Student sign-in is required."); return;
        }
        int studentId = (Integer) session.getAttribute("studentId");
        try {
            String claimedId = req.getParameter("studentId");
            if (claimedId == null || Integer.parseInt(claimedId) != studentId) {
                reject(res, 403, "This request does not belong to your student account."); return;
            }
            String bookingId = req.getParameter("bookingId");
            if (bookingId != null) {
                var booking = new BookingService().getBookingById(Integer.parseInt(bookingId));
                if (booking == null || booking.getStudentId() != studentId) {
                    reject(res, 403, "This booking does not belong to your account."); return;
                }
            }
        } catch (NumberFormatException error) { reject(res, 400, "Invalid account or booking identifier."); return; }
        chain.doFilter(request, response);
    }
    // Stops the request with the chosen HTTP status and a JSON error envelope.
    private void reject(HttpServletResponse res, int status, String message) throws IOException {
        res.setStatus(status); res.setContentType("application/json"); res.setCharacterEncoding("UTF-8");
        res.getWriter().write(Json.encode(Map.of("success", false, "message", message)));
    }
}
