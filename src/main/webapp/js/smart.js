// VIVA GUIDE: Retained fixed-slot/JSP browser helpers for older booking and analytics pages. Current student reservations use React, student-api.js and ReservationService.
/**
 * smart.js - Client-side AJAX/fetch functions for Smart Module (Member 4)
 * Handles Slot Loading, Pre-Booking, Cancellation, and Analytics Refresh.
 */

// Helper to get base path
// Named helper getBasePath: read its arguments and return value; callers determine whether it renders UI or performs an action.
function getBasePath() {
    return window.location.pathname.substring(0, window.location.pathname.indexOf('/', 1)) || '';
}

/**
 * Loads available slots for a given service and date via AJAX.
 * Updates the #slotsContainer element in pre-booking.jsp.
 */
// Named helper loadAvailableSlots: read its arguments and return value; callers determine whether it renders UI or performs an action.
function loadAvailableSlots(serviceId, bookingDate) {
    const container = document.getElementById("slotsContainer");
    if (!container) return;

    if (!serviceId || !bookingDate) {
        container.innerHTML = '<span class="text-muted">Please select a service and date to view available time slots.</span>';
        return;
    }

    container.innerHTML = '<div class="spinner-border spinner-border-sm text-primary" role="status"></div> Loading available slots...';

    const url = `${getBasePath()}/api/bookings/available-slots?serviceId=${encodeURIComponent(serviceId)}&date=${encodeURIComponent(bookingDate)}`;

    fetch(url, {
        method: "GET",
        headers: {
            "Accept": "application/json"
        }
    })
    .then(response => {
        if (!response.ok) {
            throw new Error(`HTTP error ${response.status}`);
        }
        return response.json();
    })
    .then(slots => {
        container.innerHTML = "";
        if (!slots || slots.length === 0) {
            container.innerHTML = '<div class="text-danger">No available slots for the selected date. Please choose another date.</div>';
            const submitBtn = document.getElementById("submitBtn");
            if (submitBtn) submitBtn.disabled = true;
            return;
        }

        const btnGroup = document.createElement("div");
        btnGroup.className = "d-flex flex-wrap gap-2";

        slots.forEach(slot => {
            const btn = document.createElement("button");
            btn.type = "button";
            btn.className = "btn btn-outline-primary slot-btn";
            // Format time display (e.g. 09:00:00 -> 09:00)
            const displayTime = slot.substring(0, 5);
            btn.textContent = displayTime;
            btn.onclick = function() {
                if (typeof selectSlot === "function") {
                    selectSlot(slot, btn);
                }
            };
            btnGroup.appendChild(btn);
        });

        container.appendChild(btnGroup);
    })
    .catch(error => {
        console.error("Error loading slots:", error);
        container.innerHTML = `<div class="text-danger">Unable to load slots: ${error.message}</div>`;
    });
}

/**
 * Submits appointment booking request using AJAX.
 */
// Named helper createBooking: read its arguments and return value; callers determine whether it renders UI or performs an action.
function createBooking(studentId, serviceId, bookingDate, bookingTime) {
    const submitBtn = document.getElementById("submitBtn");
    if (submitBtn) {
        submitBtn.disabled = true;
        submitBtn.innerHTML = '<span class="spinner-border spinner-border-sm" role="status"></span> Booking...';
    }

    const params = new URLSearchParams();
    params.append("studentId", studentId);
    params.append("serviceId", serviceId);
    params.append("bookingDate", bookingDate);
    params.append("bookingTime", bookingTime);

    const url = `${getBasePath()}/create-booking`;

    fetch(url, {
        method: "POST",
        headers: {
            "Content-Type": "application/x-www-form-urlencoded",
            "Accept": "application/json"
        },
        body: params.toString()
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            showAlert("success", data.message || "Booking created successfully!");
            setTimeout(() => {
                window.location.href = `${getBasePath()}/booking-status?studentId=${encodeURIComponent(studentId)}`;
            }, 1200);
        } else {
            showAlert("danger", data.message || "Failed to create booking.");
            if (submitBtn) {
                submitBtn.disabled = false;
                submitBtn.textContent = "Book Appointment";
            }
        }
    })
    .catch(error => {
        console.error("Error creating booking:", error);
        showAlert("danger", "An unexpected error occurred: " + error.message);
        if (submitBtn) {
            submitBtn.disabled = false;
            submitBtn.textContent = "Book Appointment";
        }
    });
}

/**
 * Cancels a booking using AJAX.
 */
// Named helper cancelBooking: read its arguments and return value; callers determine whether it renders UI or performs an action.
function cancelBooking(bookingId, studentId) {
    if (!confirm(`Are you sure you want to cancel booking #${bookingId}?`)) {
        return;
    }

    const params = new URLSearchParams();
    params.append("bookingId", bookingId);
    if (studentId) {
        params.append("studentId", studentId);
    }

    const url = `${getBasePath()}/cancel-booking`;

    fetch(url, {
        method: "POST",
        headers: {
            "Content-Type": "application/x-www-form-urlencoded",
            "Accept": "application/json"
        },
        body: params.toString()
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            showAlert("success", data.message || `Booking #${bookingId} cancelled successfully.`);
            setTimeout(() => {
                window.location.reload();
            }, 1000);
        } else {
            showAlert("danger", data.message || "Failed to cancel booking.");
        }
    })
    .catch(error => {
        console.error("Error cancelling booking:", error);
        showAlert("danger", "Error cancelling booking: " + error.message);
    });
}

/**
 * Refreshes real-time analytics data via AJAX.
 */
// Named helper refreshAnalytics: read its arguments and return value; callers determine whether it renders UI or performs an action.
function refreshAnalytics() {
    const url = `${getBasePath()}/analytics?format=json`;

    fetch(url, {
        method: "GET",
        headers: {
            "Accept": "application/json"
        }
    })
    .then(response => {
        if (!response.ok) {
            throw new Error(`HTTP error ${response.status}`);
        }
        return response.json();
    })
    .then(data => {
        // Update Card values
        const elStudentsServed = document.getElementById("valStudentsServed");
        const elAvgService = document.getElementById("valAvgService");
        const elAvgWait = document.getElementById("valAvgWait");
        const elPeakHour = document.getElementById("valPeakHour");
        const elNoShow = document.getElementById("valNoShow");

        if (elStudentsServed) elStudentsServed.textContent = data.studentsServedToday ?? 0;
        if (elAvgService) elAvgService.innerHTML = `${data.averageServiceTime ?? 0.0} <small class="text-muted fs-6">mins</small>`;
        if (elAvgWait) elAvgWait.innerHTML = `${data.averageWaitingTime ?? 0.0} <small class="text-muted fs-6">mins</small>`;
        if (elPeakHour) elPeakHour.textContent = data.peakHour || "N/A";
        if (elNoShow) elNoShow.textContent = data.noShowCount ?? 0;

        // Update Table values
        const tblStudentsServed = document.getElementById("tblStudentsServed");
        const tblAvgService = document.getElementById("tblAvgService");
        const tblAvgWait = document.getElementById("tblAvgWait");
        const tblPeakHour = document.getElementById("tblPeakHour");
        const tblNoShow = document.getElementById("tblNoShow");

        if (tblStudentsServed) tblStudentsServed.textContent = data.studentsServedToday ?? 0;
        if (tblAvgService) tblAvgService.textContent = `${data.averageServiceTime ?? 0.0} mins`;
        if (tblAvgWait) tblAvgWait.textContent = `${data.averageWaitingTime ?? 0.0} mins`;
        if (tblPeakHour) tblPeakHour.textContent = data.peakHour || "N/A";
        if (tblNoShow) tblNoShow.textContent = data.noShowCount ?? 0;
    })
    .catch(error => {
        console.error("Error refreshing analytics:", error);
    });
}

/**
 * Helper to display alerts in #alertPlaceholder
 */
// Named helper showAlert: read its arguments and return value; callers determine whether it renders UI or performs an action.
function showAlert(type, message) {
    const placeholder = document.getElementById("alertPlaceholder");
    if (!placeholder) return;

    placeholder.innerHTML = `
        <div class="alert alert-${type} alert-dismissible fade show" role="alert">
            ${message}
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
        </div>
    `;
}
