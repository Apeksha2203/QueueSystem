<%-- VIVA GUIDE: Retained server-rendered JSP page. Tomcat compiles JSP into a servlet; the current primary student/staff dashboards use React instead. --%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.queue.dao.ServiceDAO" %>
<%@ page import="com.queue.model.Service" %>
<%@ page import="java.util.List" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Pre-Booking - Campus Queue System</title>
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
        .slot-btn {
            margin: 4px;
            min-width: 90px;
        }
        .slot-btn.selected {
            background-color: #0d6efd;
            color: white;
        }
    </style>
</head>
<body>
<div class="container py-5">
    <div class="row justify-content-center">
        <div class="col-md-7">
            <div class="card p-4">
                <h3 class="mb-4 text-primary text-center">Campus Service Pre-Booking</h3>

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

                <form id="bookingForm" action="<%= request.getContextPath() %>/create-booking" method="POST">
                    <div class="mb-3">
                        <label for="studentId" class="form-label">Student ID</label>
                        <input type="number" class="form-control" id="studentId" name="studentId" required placeholder="e.g. 101">
                    </div>

                    <div class="mb-3">
                        <label for="serviceId" class="form-label">Select Service</label>
                        <select class="form-select" id="serviceId" name="serviceId" required onchange="onServiceOrDateChange()">
                            <option value="">-- Choose a Service --</option>
                            <%
                                ServiceDAO serviceDAO = new ServiceDAO();
                                List<Service> serviceList = serviceDAO.getAllServices();
                                if (serviceList != null && !serviceList.isEmpty()) {
                                    for (Service s : serviceList) {
                            %>
                                <option value="<%= s.getServiceId() %>">
                                    <%= s.getServiceName() %> (Avg: <%= s.getAverageServiceTime() %> mins)
                                </option>
                            <%
                                    }
                                } else {
                            %>
                                <option value="1">General Inquiries</option>
                                <option value="2">Fee Payment</option>
                                <option value="3">Document Verification</option>
                            <%
                                }
                            %>
                        </select>
                    </div>

                    <div class="mb-3">
                        <label for="bookingDate" class="form-label">Booking Date</label>
                        <input type="date" class="form-control" id="bookingDate" name="bookingDate" required onchange="onServiceOrDateChange()">
                    </div>

                    <div class="mb-3">
                        <label class="form-label">Available Time Slots</label>
                        <div id="slotsContainer" class="p-3 border rounded bg-white text-muted">
                            Please select a service and date to view available time slots.
                        </div>
                        <input type="hidden" id="bookingTime" name="bookingTime" required>
                    </div>

                    <div class="d-grid gap-2">
                        <button type="button" class="btn btn-primary btn-lg" id="submitBtn" onclick="submitBooking()" disabled>Book Appointment</button>
                    </div>
                </form>

                <div class="text-center mt-4">
                    <a href="<%= request.getContextPath() %>/booking-status" class="text-decoration-none">View My Bookings</a> |
                    <a href="<%= request.getContextPath() %>/analytics" class="text-decoration-none">View Queue Analytics</a>
                </div>
            </div>
        </div>
    </div>
</div>

<script src="<%= request.getContextPath() %>/js/smart.js"></script>
<script>
    // Initialize date picker with today's date as min
    document.addEventListener("DOMContentLoaded", function () {
        const today = new Date().toISOString().split('T')[0];
        const dateInput = document.getElementById("bookingDate");
        dateInput.min = today;
        dateInput.value = today;
        onServiceOrDateChange();
    });

    function onServiceOrDateChange() {
        const serviceId = document.getElementById("serviceId").value;
        const bookingDate = document.getElementById("bookingDate").value;

        if (serviceId && bookingDate) {
            loadAvailableSlots(serviceId, bookingDate);
        } else {
            document.getElementById("slotsContainer").innerHTML = "Please select a service and date to view available time slots.";
            document.getElementById("submitBtn").disabled = true;
        }
    }

    function selectSlot(timeSlot, btnElement) {
        document.getElementById("bookingTime").value = timeSlot;
        const allSlotBtns = document.querySelectorAll(".slot-btn");
        allSlotBtns.forEach(btn => btn.classList.remove("selected", "btn-primary"));
        allSlotBtns.forEach(btn => btn.classList.add("btn-outline-primary"));

        btnElement.classList.remove("btn-outline-primary");
        btnElement.classList.add("btn-primary", "selected");
        document.getElementById("submitBtn").disabled = false;
    }

    function submitBooking() {
        const studentId = document.getElementById("studentId").value;
        const serviceId = document.getElementById("serviceId").value;
        const bookingDate = document.getElementById("bookingDate").value;
        const bookingTime = document.getElementById("bookingTime").value;

        if (!studentId || !serviceId || !bookingDate || !bookingTime) {
            alert("Please complete all required fields and select a slot.");
            return;
        }

        createBooking(studentId, serviceId, bookingDate, bookingTime);
    }
</script>
</body>
</html>
