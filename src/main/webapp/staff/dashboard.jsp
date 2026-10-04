<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<!DOCTYPE html>
<html lang="en">

<head>

    <meta charset="UTF-8">

    <meta
        name="viewport"
        content="width=device-width, initial-scale=1.0"
    >

    <title>Staff Dashboard - Campus Queue</title>

    <link
        rel="stylesheet"
        href="../css/staff.css"
    >

</head>


<body>

<div class="dashboard">


    <!-- HEADER -->

    <header class="dashboard-header">

        <div class="brand-section">

            <h1>Campus Queue</h1>

            <p>Staff Operations Console</p>

        </div>


        <div class="staff-info">

            <div>

                <strong id="staffName">
                    Loading...
                </strong>

                <p id="counterName">
                    Counter
                </p>

            </div>

            <span
                id="onlineStatus"
                class="status"
            >
                ONLINE
            </span>

        </div>

    </header>



    <!-- PAGE INTRO -->

    <section class="page-intro">

        <div>

            <h2 id="greeting">
                Welcome
            </h2>

            <p>
                Manage your queue and service operations.
            </p>

        </div>


        <div class="date-time">

            <strong id="currentDate">
                —
            </strong>

            <span id="currentTime">
                —
            </span>

        </div>

    </section>



    <!-- SERVICE CONTEXT -->

    <section class="service-context">

        <div>

            <span class="context-label">
                CURRENT SERVICE
            </span>

            <strong id="serviceName">
                —
            </strong>

        </div>


        <div>

            <span class="context-label">
                COUNTER
            </span>

            <strong id="counterNameContext">
                —
            </strong>

        </div>

    </section>



    <!-- SUMMARY CARDS -->

    <section class="summary-cards">

        <div class="card">
            <h3>Students Waiting</h3>
            <p id="waitingCount">—</p>
        </div>


        <div class="card">
            <h3>Served Today</h3>
            <p id="completedCount">—</p>
        </div>


        <div class="card">
            <h3>Average Wait</h3>
            <p id="averageWait">—</p>
        </div>


        <div class="card">
            <h3>Avg Service Time</h3>
            <p id="averageService">—</p>
        </div>


        <div class="card">
            <h3>No-Shows Today</h3>
            <p id="noShowCount">—</p>
        </div>


        <div class="card">
            <h3>Peak Hour</h3>
            <p id="peakHour">—</p>
        </div>


        <div class="card">
            <h3>Upcoming Bookings</h3>
            <p id="bookingCount">—</p>
        </div>


        <div class="card">
            <h3>Active Queue</h3>
            <p id="queueLength">—</p>
        </div>

    </section>



    <main class="dashboard-content">


        <!-- LIVE QUEUE -->

        <section class="queue-section">

            <div class="section-header">

                <div>

                    <h2>Live Queue</h2>

                    <p>
                        Students currently waiting for service
                    </p>

                </div>


                <button
                    type="button"
                    id="callNextBtn"
                >
                    Call Next
                </button>

            </div>


            <div
                id="queueMessage"
                class="dashboard-message"
            ></div>


            <div class="queue-table-container">

                <table class="queue-table">

                    <thead>

                    <tr>

                        <th>Token</th>

                        <th>Student ID</th>

                        <th>Service ID</th>

                        <th>Status</th>

                        <th>Action</th>

                    </tr>

                    </thead>


                    <tbody id="queueBody">

                    <tr>

                        <td
                            colspan="5"
                            class="loading-cell"
                        >
                            Loading queue...
                        </td>

                    </tr>

                    </tbody>

                </table>

            </div>

        </section>



        <!-- CURRENT SERVICE -->

        <section class="service-section">

            <div class="section-header">

                <div>

                    <h2>Current Service</h2>

                    <p>
                        Manage the student currently being served
                    </p>

                </div>

            </div>


            <div class="current-service">

                <div>

                    <span>Token</span>

                    <strong id="currentToken">
                        —
                    </strong>

                </div>


                <div>

                    <span>Student ID</span>

                    <strong id="currentStudent">
                        —
                    </strong>

                </div>


                <div>

                    <span>Service</span>

                    <strong id="currentService">
                        —
                    </strong>

                </div>


                <div>

                    <span>Expected Time</span>

                    <strong id="currentExpectedTime">
                        —
                    </strong>

                </div>


                <div>

                    <span>State</span>

                    <strong
                        id="serviceState"
                        class="service-state idle"
                    >
                        IDLE
                    </strong>

                </div>


                <div class="service-actions">

                    <button
                        type="button"
                        id="startBtn"
                        disabled
                    >
                        Start Service
                    </button>


                    <button
                        type="button"
                        id="completeBtn"
                        disabled
                    >
                        Complete
                    </button>


                    <button
                        type="button"
                        id="skipBtn"
                        disabled
                    >
                        Skip
                    </button>

                </div>

            </div>

        </section>



        <!-- QUEUE INSIGHT -->

        <section class="service-section">

            <div class="section-header">

                <div>

                    <h2>Queue Insight</h2>

                    <p>
                        Current queue conditions
                    </p>

                </div>

            </div>


            <div class="current-service">

                <div>

                    <span>Queue Length</span>

                    <strong id="queueLengthInsight">
                        —
                    </strong>

                </div>


                <div>

                    <span>Estimated Wait</span>

                    <strong id="estimatedWait">
                        —
                    </strong>

                </div>


                <div class="insight-wide">

                    <span>Insight</span>

                    <strong id="queueInsight">
                        Waiting for live queue data...
                    </strong>

                </div>

            </div>

        </section>



        <!-- STAFF AVAILABILITY -->

        <section class="availability-section">

            <div class="section-header">

                <div>

                    <h2>Staff Availability</h2>

                    <p>
                        Manage your counter availability
                    </p>

                </div>


                <span
                    id="availabilityState"
                    class="availability-badge available"
                >
                    AVAILABLE
                </span>

            </div>


            <div class="availability-controls">

                <button
                    type="button"
                    id="pauseBtn"
                >
                    Pause
                </button>


                <button
                    type="button"
                    id="resumeBtn"
                    disabled
                >
                    Resume
                </button>


                <button
                    type="button"
                    id="checkoutBtn"
                >
                    Check Out
                </button>

            </div>

        </section>



        <!-- RECENT ACTIVITY -->

        <section class="service-section">

            <div class="section-header">

                <div>

                    <h2>Recent Activity</h2>

                    <p>
                        Activity during this dashboard session
                    </p>

                </div>

            </div>


            <div
                id="activityList"
                class="activity-list"
            >

                <div class="activity-item">

                    <span class="activity-dot"></span>

                    <div>

                        <strong>
                            Dashboard loaded
                        </strong>

                        <span>
                            Waiting for live activity
                        </span>

                    </div>

                </div>

            </div>

        </section>


    </main>



    <footer class="dashboard-footer">

        <p>
            Campus Queue Management System
        </p>

        <p>
            Staff Operations Dashboard
        </p>

    </footer>


</div>


<script src="../js/staff.js"></script>

</body>

</html>