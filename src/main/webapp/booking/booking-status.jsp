<%-- VIVA GUIDE: Retained server-rendered JSP page. Tomcat compiles JSP into a servlet; the current primary student/staff dashboards use React instead. --%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.queue.model.Booking" %>
<%@ page import="java.util.List" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Booking Status - Campus Queue System</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <style>
        body {
            background-color: #f8f9fa;
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
        }
        .card {
            border-radius: 8px;
            box-shadow: 0 4px 6px rgba(0, 0, 0, 0.05);
        }
    </style>
</head>
<body>
<div class="container py-5">
    <div class="row justify-content-center">
        <div class="col-md-10">
            <div class="card p-4">
                <div class="d-flex justify-content-between align-items-center mb-4">
                    <h3 class="text-primary mb-0">Booking Status & History</h3>
                    <a href="<%= request.getContextPath() %>/create-booking" class="btn btn-outline-primary btn-sm">+ New Booking</a>
                </div>

                <!-- Search by Student ID or Booking ID -->
                <form class="row g-3 mb-4" method="GET" action="<%= request.getContextPath() %>/booking-status">
                    <div class="col-md-4">
                        <input type="number" class="form-control" name="studentId" placeholder="Search by Student ID"
                               value="<%= request.getAttribute("studentId") != null ? request.getAttribute("studentId") : (request.getParameter("studentId") != null ? request.getParameter("studentId") : "") %>">
                    </div>
                    <div class="col-md-4">
                        <input type="number" class="form-control" name="bookingId" placeholder="Or by Booking ID"
                               value="<%= request.getAttribute("bookingId") != null ? request.getAttribute("bookingId") : (request.getParameter("bookingId") != null ? request.getParameter("bookingId") : "") %>">
                    </div>
                    <div class="col-md-4">
                        <button type="submit" class="btn btn-secondary w-100">Find Bookings</button>
                    </div>
                </form>

                <% if (request.getAttribute("errorMessage") != null) { %>
                    <div class="alert alert-danger" role="alert">
                        <%= request.getAttribute("errorMessage") %>
                    </div>
                <% } %>

                <% if (request.getAttribute("message") != null) { %>
                    <div class="alert alert-success" role="alert">
                        <%= request.getAttribute("message") %>
                    </div>
                <% } %>

                <div id="alertPlaceholder"></div>

                <%
                    List<Booking> bookings = (List<Booking>) request.getAttribute("bookings");
                    Booking singleBooking = (Booking) request.getAttribute("singleBooking");
                %>

                <div class="table-responsive">
                    <table class="table table-hover table-bordered align-middle">
                        <thead class="table-light">
                        <tr>
                            <th>Booking ID</th>
                            <th>Student ID</th>
                            <th>Service ID</th>
                            <th>Date</th>
                            <th>Time</th>
                            <th>Status</th>
                            <th>Action</th>
                        </tr>
                        </thead>
                        <tbody>
                        <%
                            if (bookings != null && !bookings.isEmpty()) {
                                for (Booking b : bookings) {
                        %>
                        <tr>
                            <td><strong>#<%= b.getBookingId() %></strong></td>
                            <td><%= b.getStudentId() %></td>
                            <td>Service <%= b.getServiceId() %></td>
                            <td><%= b.getBookingDate() %></td>
                            <td><%= b.getBookingTime() %></td>
                            <td>
                                <% if ("BOOKED".equalsIgnoreCase(b.getStatus())) { %>
                                    <span class="badge bg-success">BOOKED</span>
                                <% } else if ("CANCELLED".equalsIgnoreCase(b.getStatus())) { %>
                                    <span class="badge bg-secondary">CANCELLED</span>
                                <% } else if ("NO_SHOW".equalsIgnoreCase(b.getStatus())) { %>
                                    <span class="badge bg-danger">NO_SHOW</span>
                                <% } else { %>
                                    <span class="badge bg-info"><%= b.getStatus() %></span>
                                <% } %>
                            </td>
                            <td>
                                <% if ("BOOKED".equalsIgnoreCase(b.getStatus())) { %>
                                    <button class="btn btn-outline-danger btn-sm"
                                            onclick="cancelBooking(<%= b.getBookingId() %>, <%= b.getStudentId() %>)">
                                        Cancel
                                    </button>
                                <% } else { %>
                                    <span class="text-muted small">N/A</span>
                                <% } %>
                            </td>
                        </tr>
                        <%
                                }
                            } else if (singleBooking != null) {
                        %>
                        <tr>
                            <td><strong>#<%= singleBooking.getBookingId() %></strong></td>
                            <td><%= singleBooking.getStudentId() %></td>
                            <td>Service <%= singleBooking.getServiceId() %></td>
                            <td><%= singleBooking.getBookingDate() %></td>
                            <td><%= singleBooking.getBookingTime() %></td>
                            <td>
                                <% if ("BOOKED".equalsIgnoreCase(singleBooking.getStatus())) { %>
                                    <span class="badge bg-success">BOOKED</span>
                                <% } else if ("CANCELLED".equalsIgnoreCase(singleBooking.getStatus())) { %>
                                    <span class="badge bg-secondary">CANCELLED</span>
                                <% } else if ("NO_SHOW".equalsIgnoreCase(singleBooking.getStatus())) { %>
                                    <span class="badge bg-danger">NO_SHOW</span>
                                <% } else { %>
                                    <span class="badge bg-info"><%= singleBooking.getStatus() %></span>
                                <% } %>
                            </td>
                            <td>
                                <% if ("BOOKED".equalsIgnoreCase(singleBooking.getStatus())) { %>
                                    <button class="btn btn-outline-danger btn-sm"
                                            onclick="cancelBooking(<%= singleBooking.getBookingId() %>, <%= singleBooking.getStudentId() %>)">
                                        Cancel
                                    </button>
                                <% } else { %>
                                    <span class="text-muted small">N/A</span>
                                <% } %>
                            </td>
                        </tr>
                        <%
                            } else {
                        %>
                        <tr>
                            <td colspan="7" class="text-center text-muted py-4">
                                No bookings found. Enter a Student ID or Booking ID to search, or create a new booking.
                            </td>
                        </tr>
                        <%
                            }
                        %>
                        </tbody>
                    </table>
                </div>

                <div class="text-center mt-3">
                    <a href="<%= request.getContextPath() %>/create-booking" class="text-decoration-none me-3">Book New Appointment</a>
                    <a href="<%= request.getContextPath() %>/analytics" class="text-decoration-none">Queue Analytics</a>
                </div>
            </div>
        </div>
    </div>
</div>

<script src="<%= request.getContextPath() %>/js/smart.js"></script>
</body>
</html>
