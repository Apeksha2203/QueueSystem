package com.queue.controller;

import com.queue.model.Booking;
import com.queue.service.BookingService;
import com.queue.service.NoShowService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Date;
import java.sql.Time;
import java.util.List;

@WebServlet(urlPatterns = {
        "/create-booking",
        "/cancel-booking",
        "/booking-status",
        "/api/bookings/create",
        "/api/bookings/cancel",
        "/api/bookings/status",
        "/api/bookings/available-slots",
        "/api/bookings/upcoming"
})
public class BookingServlet extends HttpServlet {

    private final BookingService bookingService = new BookingService();
    private final NoShowService noShowService = new NoShowService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Auto-check and release any no-show bookings before handling request
        noShowService.checkAndUpdateNoShows();

        String servletPath = request.getServletPath();
        String action = request.getParameter("action");

        if ("/api/bookings/available-slots".equals(servletPath) || "available-slots".equalsIgnoreCase(action)) {
            handleAvailableSlots(request, response);
        } else if ("/api/bookings/upcoming".equals(servletPath) || "upcoming".equalsIgnoreCase(action)) {
            handleUpcomingBookings(request, response);
        } else if ("/booking-status".equals(servletPath) || "/api/bookings/status".equals(servletPath)) {
            handleBookingStatus(request, response);
        } else {
            // Forward to pre-booking page by default
            request.getRequestDispatcher("/booking/pre-booking.jsp").forward(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Auto-check and release no-shows
        noShowService.checkAndUpdateNoShows();

        String servletPath = request.getServletPath();

        if ("/create-booking".equals(servletPath) || "/api/bookings/create".equals(servletPath)) {
            handleCreateBooking(request, response);
        } else if ("/cancel-booking".equals(servletPath) || "/api/bookings/cancel".equals(servletPath)) {
            handleCancelBooking(request, response);
        } else {
            doGet(request, response);
        }
    }

    private void handleAvailableSlots(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        try {
            int serviceId = Integer.parseInt(request.getParameter("serviceId"));
            String dateStr = request.getParameter("date");
            Date bookingDate = (dateStr != null && !dateStr.trim().isEmpty())
                    ? Date.valueOf(dateStr.trim())
                    : new Date(System.currentTimeMillis());

            List<String> slots = bookingService.getAvailableSlots(serviceId, bookingDate);

            StringBuilder json = new StringBuilder("[");
            for (int i = 0; i < slots.size(); i++) {
                json.append("\"").append(slots.get(i)).append("\"");
                if (i < slots.size() - 1) {
                    json.append(",");
                }
            }
            json.append("]");

            out.print(json.toString());
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
        }
    }

    private void handleCreateBooking(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        boolean isAjax = isAjaxRequest(request);

        try {
            int studentId = Integer.parseInt(request.getParameter("studentId"));
            int serviceId = Integer.parseInt(request.getParameter("serviceId"));
            String dateStr = request.getParameter("bookingDate");
            String timeStr = request.getParameter("bookingTime");

            if (timeStr != null && timeStr.length() == 5) {
                timeStr += ":00"; // format HH:mm to HH:mm:ss
            }

            Booking booking = new Booking();
            booking.setStudentId(studentId);
            booking.setServiceId(serviceId);
            booking.setBookingDate(Date.valueOf(dateStr));
            booking.setBookingTime(Time.valueOf(timeStr));

            String result = bookingService.createBooking(booking);

            if ("SUCCESS".equalsIgnoreCase(result)) {
                if (isAjax) {
                    response.setContentType("application/json");
                    response.getWriter().print("{\"success\":true,\"message\":\"Booking confirmed successfully!\",\"bookingId\":" + booking.getBookingId() + "}");
                } else {
                    request.setAttribute("message", "Booking confirmed successfully! Booking ID: " + booking.getBookingId());
                    request.setAttribute("booking", booking);
                    response.sendRedirect(request.getContextPath() + "/booking-status?studentId=" + studentId);
                }
            } else {
                if (isAjax) {
                    response.setContentType("application/json");
                    response.getWriter().print("{\"success\":false,\"message\":\"" + escapeJson(result) + "\"}");
                } else {
                    request.setAttribute("errorMessage", result);
                    request.getRequestDispatcher("/booking/pre-booking.jsp").forward(request, response);
                }
            }

        } catch (Exception e) {
            if (isAjax) {
                response.setContentType("application/json");
                response.getWriter().print("{\"success\":false,\"message\":\"Invalid input: " + escapeJson(e.getMessage()) + "\"}");
            } else {
                request.setAttribute("errorMessage", "Error processing booking: " + e.getMessage());
                request.getRequestDispatcher("/booking/pre-booking.jsp").forward(request, response);
            }
        }
    }

    private void handleCancelBooking(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        boolean isAjax = isAjaxRequest(request);

        try {
            int bookingId = Integer.parseInt(request.getParameter("bookingId"));
            int studentId = 0;
            if (request.getParameter("studentId") != null && !request.getParameter("studentId").trim().isEmpty()) {
                studentId = Integer.parseInt(request.getParameter("studentId").trim());
            }

            boolean cancelled = bookingService.cancelBooking(bookingId, studentId);

            if (isAjax) {
                response.setContentType("application/json");
                if (cancelled) {
                    response.getWriter().print("{\"success\":true,\"message\":\"Booking #" + bookingId + " has been cancelled successfully.\"}");
                } else {
                    response.getWriter().print("{\"success\":false,\"message\":\"Failed to cancel booking. It may already be cancelled or invalid.\"}");
                }
            } else {
                if (cancelled) {
                    request.setAttribute("message", "Booking #" + bookingId + " has been cancelled.");
                } else {
                    request.setAttribute("errorMessage", "Could not cancel booking #" + bookingId + ".");
                }
                if (studentId > 0) {
                    response.sendRedirect(request.getContextPath() + "/booking-status?studentId=" + studentId);
                } else {
                    response.sendRedirect(request.getContextPath() + "/booking-status");
                }
            }

        } catch (Exception e) {
            if (isAjax) {
                response.setContentType("application/json");
                response.getWriter().print("{\"success\":false,\"message\":\"Error: " + escapeJson(e.getMessage()) + "\"}");
            } else {
                request.setAttribute("errorMessage", "Invalid request: " + e.getMessage());
                request.getRequestDispatcher("/booking/booking-status.jsp").forward(request, response);
            }
        }
    }

    private void handleUpcomingBookings(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        try {
            int serviceId = Integer.parseInt(request.getParameter("serviceId"));
            String dateStr = request.getParameter("date");

            List<Booking> upcoming;
            if (dateStr != null && !dateStr.trim().isEmpty()) {
                Date bookingDate = Date.valueOf(dateStr.trim());
                upcoming = bookingService.getUpcomingBookings(serviceId, bookingDate);
            } else {
                upcoming = bookingService.getUpcomingBookings(serviceId);
            }

            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < upcoming.size(); i++) {
                Booking b = upcoming.get(i);
                sb.append(formatBookingJson(b));
                if (i < upcoming.size() - 1) sb.append(",");
            }
            sb.append("]");

            out.print(sb.toString());
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
        }
    }

    private void handleBookingStatus(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String studentIdParam = request.getParameter("studentId");
        String bookingIdParam = request.getParameter("bookingId");
        boolean isAjax = isAjaxRequest(request);

        List<Booking> bookings = null;
        Booking singleBooking = null;

        if (studentIdParam != null && !studentIdParam.trim().isEmpty()) {
            try {
                int studentId = Integer.parseInt(studentIdParam.trim());
                bookings = bookingService.getStudentBookings(studentId);
                request.setAttribute("studentId", studentId);
            } catch (NumberFormatException ignored) {}
        } else if (bookingIdParam != null && !bookingIdParam.trim().isEmpty()) {
            try {
                int bookingId = Integer.parseInt(bookingIdParam.trim());
                singleBooking = bookingService.getBookingById(bookingId);
                request.setAttribute("bookingId", bookingId);
            } catch (NumberFormatException ignored) {}
        }

        if (isAjax) {
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            PrintWriter out = response.getWriter();

            if (bookings != null) {
                StringBuilder sb = new StringBuilder("[");
                for (int i = 0; i < bookings.size(); i++) {
                    Booking b = bookings.get(i);
                    sb.append(formatBookingJson(b));
                    if (i < bookings.size() - 1) sb.append(",");
                }
                sb.append("]");
                out.print(sb.toString());
            } else if (singleBooking != null) {
                out.print(formatBookingJson(singleBooking));
            } else {
                out.print("[]");
            }
        } else {
            request.setAttribute("bookings", bookings);
            request.setAttribute("singleBooking", singleBooking);
            request.getRequestDispatcher("/booking/booking-status.jsp").forward(request, response);
        }
    }

    private boolean isAjaxRequest(HttpServletRequest request) {
        String requestedWith = request.getHeader("X-Requested-With");
        String acceptHeader = request.getHeader("Accept");
        String format = request.getParameter("format");
        return "XMLHttpRequest".equalsIgnoreCase(requestedWith)
                || (acceptHeader != null && acceptHeader.contains("application/json"))
                || "json".equalsIgnoreCase(format);
    }

    private String formatBookingJson(Booking b) {
        return "{" +
                "\"bookingId\":" + b.getBookingId() + "," +
                "\"studentId\":" + b.getStudentId() + "," +
                "\"serviceId\":" + b.getServiceId() + "," +
                "\"bookingDate\":\"" + (b.getBookingDate() != null ? b.getBookingDate().toString() : "") + "\"," +
                "\"bookingTime\":\"" + (b.getBookingTime() != null ? b.getBookingTime().toString() : "") + "\"," +
                "\"status\":\"" + (b.getStatus() != null ? b.getStatus() : "") + "\"" +
                "}";
    }

    private String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
    }
}
