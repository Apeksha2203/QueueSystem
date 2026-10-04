<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Queue Analytics - Campus Queue System</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <style>
        body {
            background-color: #f8f9fa;
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
        }
        .stat-card {
            border-radius: 8px;
            border: 1px solid #e3e6f0;
            background-color: #fff;
            padding: 20px;
            box-shadow: 0 4px 6px rgba(0,0,0,0.04);
            transition: transform 0.2s ease;
        }
        .stat-card:hover {
            transform: translateY(-2px);
        }
        .stat-title {
            font-size: 0.85rem;
            text-transform: uppercase;
            letter-spacing: 0.05em;
            color: #6c757d;
            font-weight: 600;
        }
        .stat-value {
            font-size: 1.8rem;
            font-weight: 700;
            color: #2c3e50;
            margin-top: 5px;
        }
    </style>
</head>
<body>
<div class="container py-5">
    <div class="d-flex justify-content-between align-items-center mb-4">
        <div>
            <h2 class="text-primary mb-1">Queue & Service Analytics</h2>
            <p class="text-muted mb-0">Real-time aggregated performance metrics</p>
        </div>
        <div>
            <button class="btn btn-outline-primary" onclick="refreshAnalytics()">
                🔄 Refresh Analytics
            </button>
        </div>
    </div>

    <!-- Analytics Cards -->
    <div class="row g-4 mb-4">
        <div class="col-md-4 col-sm-6">
            <div class="stat-card border-start border-primary border-4">
                <div class="stat-title">Students Served Today</div>
                <div class="stat-value" id="valStudentsServed">
                    <%= request.getAttribute("studentsServedToday") != null ? request.getAttribute("studentsServedToday") : 0 %>
                </div>
            </div>
        </div>

        <div class="col-md-4 col-sm-6">
            <div class="stat-card border-start border-success border-4">
                <div class="stat-title">Average Service Time</div>
                <div class="stat-value" id="valAvgService">
                    <%= request.getAttribute("averageServiceTime") != null ? request.getAttribute("averageServiceTime") : 0.0 %> <small class="text-muted fs-6">mins</small>
                </div>
            </div>
        </div>

        <div class="col-md-4 col-sm-6">
            <div class="stat-card border-start border-info border-4">
                <div class="stat-title">Average Waiting Time</div>
                <div class="stat-value" id="valAvgWait">
                    <%= request.getAttribute("averageWaitingTime") != null ? request.getAttribute("averageWaitingTime") : 0.0 %> <small class="text-muted fs-6">mins</small>
                </div>
            </div>
        </div>

        <div class="col-md-6 col-sm-6">
            <div class="stat-card border-start border-warning border-4">
                <div class="stat-title">Peak Traffic Hour</div>
                <div class="stat-value" id="valPeakHour">
                    <%= request.getAttribute("peakHour") != null ? request.getAttribute("peakHour") : "N/A" %>
                </div>
            </div>
        </div>

        <div class="col-md-6 col-sm-6">
            <div class="stat-card border-start border-danger border-4">
                <div class="stat-title">No-Show & Skipped Count</div>
                <div class="stat-value" id="valNoShow">
                    <%= request.getAttribute("noShowCount") != null ? request.getAttribute("noShowCount") : 0 %>
                </div>
            </div>
        </div>
    </div>

    <!-- Summary Details Table -->
    <div class="card p-4">
        <h5 class="card-title text-secondary mb-3">Metrics Overview Table</h5>
        <div class="table-responsive">
            <table class="table table-bordered table-striped align-middle mb-0">
                <thead class="table-light">
                    <tr>
                        <th>Metric</th>
                        <th>Current Value</th>
                        <th>Calculation Method</th>
                    </tr>
                </thead>
                <tbody>
                    <tr>
                        <td><strong>Students Served Today</strong></td>
                        <td id="tblStudentsServed"><%= request.getAttribute("studentsServedToday") != null ? request.getAttribute("studentsServedToday") : 0 %></td>
                        <td>SQL Count of queue records with status 'COMPLETED' for today</td>
                    </tr>
                    <tr>
                        <td><strong>Average Service Time</strong></td>
                        <td id="tblAvgService"><%= request.getAttribute("averageServiceTime") != null ? request.getAttribute("averageServiceTime") : 0.0 %> mins</td>
                        <td>SQL Average duration between started_at and completed_at</td>
                    </tr>
                    <tr>
                        <td><strong>Average Waiting Time</strong></td>
                        <td id="tblAvgWait"><%= request.getAttribute("averageWaitingTime") != null ? request.getAttribute("averageWaitingTime") : 0.0 %> mins</td>
                        <td>SQL Average duration from joined_at until service starts/called</td>
                    </tr>
                    <tr>
                        <td><strong>Peak Hour</strong></td>
                        <td id="tblPeakHour"><%= request.getAttribute("peakHour") != null ? request.getAttribute("peakHour") : "N/A" %></td>
                        <td>Hour with the highest total ticket creation count</td>
                    </tr>
                    <tr>
                        <td><strong>Total No-Shows</strong></td>
                        <td id="tblNoShow"><%= request.getAttribute("noShowCount") != null ? request.getAttribute("noShowCount") : 0 %></td>
                        <td>Aggregated count of expired booking slots and skipped queue tokens</td>
                    </tr>
                </tbody>
            </table>
        </div>
        <div class="mt-4 text-center">
            <a href="<%= request.getContextPath() %>/create-booking" class="btn btn-outline-secondary me-2">Pre-Booking</a>
            <a href="<%= request.getContextPath() %>/booking-status" class="btn btn-outline-secondary">Booking Status</a>
        </div>
    </div>
</div>

<script src="<%= request.getContextPath() %>/js/smart.js"></script>
</body>
</html>
