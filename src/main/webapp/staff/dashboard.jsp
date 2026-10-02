<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">

    <title>Staff Dashboard - Queue System</title>

    <link rel="stylesheet" href="../css/staff.css">
</head>

<body>

    <div class="dashboard">

        <!-- Header -->
        <header class="dashboard-header">
            <div>
                <h1>Staff Dashboard</h1>
                <p>Campus Queue Management System</p>
            </div>

            <div class="staff-info">
                <span>Staff</span>
                <span class="status">AVAILABLE</span>
            </div>
        </header>


        <!-- Dashboard Summary -->
        <section class="summary-cards">

            <div class="card">
                <h3>Current Token</h3>
                <p id="currentToken">--</p>
            </div>

            <div class="card">
                <h3>Waiting Students</h3>
                <p id="waitingCount">0</p>
            </div>

            <div class="card">
                <h3>Completed Today</h3>
                <p id="completedCount">0</p>
            </div>

            <div class="card">
                <h3>Counter Status</h3>
                <p id="counterStatus">Available</p>
            </div>

        </section>


        <!-- Main Content -->
        <main class="dashboard-content">

            <!-- Queue Section -->
            <section class="queue-section">

                <div class="section-header">
                    <div>
                        <h2>Current Queue</h2>
                        <p>Students waiting for service</p>
                    </div>

                    <button id="callNextBtn">
                        Call Next
                    </button>
                </div>


                <table class="queue-table">

                    <thead>
                        <tr>
                            <th>Token</th>
                            <th>Student</th>
                            <th>Service</th>
                            <th>Status</th>
                            <th>Action</th>
                        </tr>
                    </thead>

                    <tbody id="queueTableBody">

                        <tr>
                            <td>A001</td>
                            <td>Student 1</td>
                            <td>Admissions</td>
                            <td>
                                <span class="queue-status waiting">
                                    Waiting
                                </span>
                            </td>
                            <td>
                                <button class="action-btn">
                                    Call
                                </button>
                            </td>
                        </tr>

                        <tr>
                            <td>A002</td>
                            <td>Student 2</td>
                            <td>Admissions</td>
                            <td>
                                <span class="queue-status waiting">
                                    Waiting
                                </span>
                            </td>
                            <td>
                                <button class="action-btn">
                                    Call
                                </button>
                            </td>
                        </tr>

                    </tbody>

                </table>

            </section>


            <!-- Current Service -->
            <section class="service-section">

                <h2>Current Service</h2>

                <div class="current-service">

                    <div>
                        <span>Current Token</span>
                        <strong id="serviceToken">--</strong>
                    </div>

                    <div>
                        <span>Student</span>
                        <strong id="serviceStudent">--</strong>
                    </div>

                    <div class="service-actions">

                        <button id="startBtn">
                            Start Service
                        </button>

                        <button id="completeBtn">
                            Complete
                        </button>

                        <button id="skipBtn">
                            Skip
                        </button>

                    </div>

                </div>

            </section>


            <!-- Availability -->
            <section class="availability-section">

                <h2>Staff Availability</h2>

                <div class="availability-controls">

                    <button id="pauseBtn">
                        Pause
                    </button>

                    <button id="resumeBtn">
                        Resume
                    </button>

                    <button id="checkoutBtn">
                        Check Out
                    </button>

                </div>

            </section>

        </main>

    </div>


    <script src="../js/staff.js"></script>

</body>
</html>