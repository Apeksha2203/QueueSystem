// VIVA GUIDE: Retained staff JSP browser behavior. The deployed primary staff interface uses frontend/ React components.
"use strict";


/* =========================================================
   API CONFIGURATION
   ========================================================= */

const API = {

    profile:
        "../api/staff/profile",

    queue:
        "../api/staff/queue",

    callNext:
        "../api/queue/call-next",

    start:
        "../api/queue/start",

    complete:
        "../api/queue/complete",

    skip:
        "../api/queue/skip",

    counterStatus:
        "../api/staff/counter/status",

    analytics:
        "../api/analytics?format=json"
};



/* =========================================================
   STATE
   ========================================================= */

let staffProfile = null;

let queueData = [];

let currentQueueItem = null;

let counterState = "AVAILABLE";

let dashboardInitialized = false;



/* =========================================================
   DOM REFERENCES
   ========================================================= */

const staffName =
    document.getElementById("staffName");

const counterName =
    document.getElementById("counterName");

const counterNameContext =
    document.getElementById("counterNameContext");

const serviceName =
    document.getElementById("serviceName");

const currentDate =
    document.getElementById("currentDate");

const currentTime =
    document.getElementById("currentTime");

const greeting =
    document.getElementById("greeting");

const waitingCount =
    document.getElementById("waitingCount");

const completedCount =
    document.getElementById("completedCount");

const averageWait =
    document.getElementById("averageWait");

const averageService =
    document.getElementById("averageService");

const noShowCount =
    document.getElementById("noShowCount");

const peakHour =
    document.getElementById("peakHour");

const bookingCount =
    document.getElementById("bookingCount");

const queueLength =
    document.getElementById("queueLength");

const queueLengthInsight =
    document.getElementById("queueLengthInsight");

const estimatedWait =
    document.getElementById("estimatedWait");

const queueInsight =
    document.getElementById("queueInsight");

const queueBody =
    document.getElementById("queueBody");

const queueMessage =
    document.getElementById("queueMessage");

const currentToken =
    document.getElementById("currentToken");

const currentStudent =
    document.getElementById("currentStudent");

const currentService =
    document.getElementById("currentService");

const currentExpectedTime =
    document.getElementById("currentExpectedTime");

const serviceState =
    document.getElementById("serviceState");

const startBtn =
    document.getElementById("startBtn");

const completeBtn =
    document.getElementById("completeBtn");

const skipBtn =
    document.getElementById("skipBtn");

const callNextBtn =
    document.getElementById("callNextBtn");

const pauseBtn =
    document.getElementById("pauseBtn");

const resumeBtn =
    document.getElementById("resumeBtn");

const checkoutBtn =
    document.getElementById("checkoutBtn");

const availabilityState =
    document.getElementById("availabilityState");

const activityList =
    document.getElementById("activityList");



/* =========================================================
   GENERAL HELPERS
   ========================================================= */

// Named helper showQueueMessage: read its arguments and return value; callers determine whether it renders UI or performs an action.
function showQueueMessage(message, type = "") {

    queueMessage.textContent = message;

    queueMessage.className =
        "dashboard-message";

    if (type) {
        queueMessage.classList.add(type);
    }
}


// Named helper escapeHtml: read its arguments and return value; callers determine whether it renders UI or performs an action.
function escapeHtml(value) {

    return String(value ?? "")
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#039;");
}


// Named helper formatToken: read its arguments and return value; callers determine whether it renders UI or performs an action.
function formatToken(tokenNumber) {

    if (
        tokenNumber === null ||
        tokenNumber === undefined
    ) {
        return "—";
    }

    return "A" +
        String(tokenNumber).padStart(3, "0");
}


// Named helper formatMinutes: read its arguments and return value; callers determine whether it renders UI or performs an action.
function formatMinutes(value) {

    if (
        value === null ||
        value === undefined ||
        value === ""
    ) {
        return "—";
    }

    const number =
        Number(value);

    if (Number.isNaN(number)) {
        return String(value);
    }

    return `${number.toFixed(1)} min`;
}


// Named helper getServiceNameFromProfile: read its arguments and return value; callers determine whether it renders UI or performs an action.
function getServiceNameFromProfile() {

    if (!staffProfile) {
        return "—";
    }

    return `Service ${staffProfile.serviceId}`;
}


// Named helper readJsonResponse: read its arguments and return value; callers determine whether it renders UI or performs an action.
async function readJsonResponse(response) {

    const text =
        await response.text();

    let data;

    try {

        data =
            JSON.parse(text);

    } catch (error) {

        throw new Error(
            `Invalid server response (HTTP ${response.status}).`
        );
    }


    if (!response.ok) {

        throw new Error(
            data.message ||
            `Request failed with HTTP ${response.status}.`
        );
    }


    if (
        data &&
        data.success === false
    ) {

        throw new Error(
            data.message ||
            "The server rejected the request."
        );
    }


    return data;
}


// Send a cookie-backed staff request and reject failed/non-JSON responses before screens consume the payload.
async function apiRequest(
    url,
    options = {}
) {

    const response =
        await fetch(
            url,
            {
                ...options,

                credentials:
                    "same-origin",

                cache:
                    "no-store"
            }
        );

    return readJsonResponse(response);
}


// Named helper postForm: read its arguments and return value; callers determine whether it renders UI or performs an action.
async function postForm(
    url,
    values = {}
) {

    const formData =
        new URLSearchParams();


    Object.entries(values).forEach(
        ([key, value]) => {

            formData.append(
                key,
                value
            );
        }
    );


    return apiRequest(
        url,
        {
            method: "POST",

            headers: {
                "Content-Type":
                    "application/x-www-form-urlencoded;charset=UTF-8",

                "Accept":
                    "application/json"
            },

            body:
                formData.toString()
        }
    );
}



/* =========================================================
   DATE / TIME
   ========================================================= */

// Named helper updateDateTime: read its arguments and return value; callers determine whether it renders UI or performs an action.
function updateDateTime() {

    const now =
        new Date();


    if (currentDate) {

        currentDate.textContent =
            now.toLocaleDateString(
                "en-IN",
                {
                    weekday: "long",
                    day: "numeric",
                    month: "long",
                    year: "numeric"
                }
            );
    }


    if (currentTime) {

        currentTime.textContent =
            now.toLocaleTimeString(
                "en-IN",
                {
                    hour: "2-digit",
                    minute: "2-digit",
                    second: "2-digit"
                }
            );
    }


    if (greeting) {

        const hour =
            now.getHours();


        let text = "Good Evening";


        if (hour < 12) {
            text = "Good Morning";
        } else if (hour < 17) {
            text = "Good Afternoon";
        }


        greeting.textContent =
            text;
    }
}


updateDateTime();

setInterval(
    updateDateTime,
    1000
);



/* =========================================================
   STAFF PROFILE
   ========================================================= */

// Named helper loadProfile: read its arguments and return value; callers determine whether it renders UI or performs an action.
async function loadProfile() {

    const result =
        await apiRequest(
            API.profile,
            {
                method: "GET",

                headers: {
                    "Accept":
                        "application/json"
                }
            }
        );


    staffProfile =
        result.data;


    if (!staffProfile) {

        throw new Error(
            "Staff profile was not returned."
        );
    }


    if (staffName) {

        staffName.textContent =
            staffProfile.staffName ||
            "Staff Member";
    }


    const counterText =
        staffProfile.counterId !== undefined
            ? `Counter ${String(
                staffProfile.counterId
            ).padStart(2, "0")}`
            : "Counter";


    if (counterName) {

        counterName.textContent =
            counterText;
    }


    if (counterNameContext) {

        counterNameContext.textContent =
            counterText;
    }


    if (serviceName) {

        serviceName.textContent =
            getServiceNameFromProfile();
    }
}



/* =========================================================
   ACTIVE QUEUE
   ========================================================= */

// Named helper loadQueue: read its arguments and return value; callers determine whether it renders UI or performs an action.
async function loadQueue() {

    const result =
        await apiRequest(
            API.queue,
            {
                method: "GET",

                headers: {
                    "Accept":
                        "application/json"
                }
            }
        );


    queueData =
        Array.isArray(result.data)
            ? result.data
            : [];


    renderQueue();

    updateQueueSummary();

    restoreCurrentServiceFromQueue();
}


// Named helper renderQueue: read its arguments and return value; callers determine whether it renders UI or performs an action.
function renderQueue() {

    if (!queueBody) {
        return;
    }


    if (queueData.length === 0) {

        queueBody.innerHTML = `
            <tr>
                <td
                    colspan="5"
                    class="empty-cell"
                >
                    No students are currently in the queue.
                </td>
            </tr>
        `;

        return;
    }


    queueBody.innerHTML =
        queueData.map(
            item => {

                const token =
                    formatToken(
                        item.tokenNumber
                    );


                const status =
                    String(
                        item.status || ""
                    ).toUpperCase();


                const waiting =
                    status === "WAITING";


                const active =
                    currentQueueItem &&
                    Number(
                        currentQueueItem.queueId
                    ) === Number(
                        item.queueId
                    );


                return `
                    <tr
                        class="${active ? "active-row" : ""}"
                        data-queue-id="${escapeHtml(item.queueId)}"
                    >

                        <td>
                            <strong>${escapeHtml(token)}</strong>
                        </td>

                        <td>
                            Student #${escapeHtml(item.studentId)}
                        </td>

                        <td>
                            Service #${escapeHtml(item.serviceId)}
                        </td>

                        <td>
                            <span
                                class="queue-status ${status.toLowerCase()}"
                            >
                                ${escapeHtml(status)}
                            </span>
                        </td>

                        <td>

                            ${
                                waiting
                                    ? `
                                        <button
                                            type="button"
                                            class="row-call-btn"
                                            data-queue-id="${escapeHtml(item.queueId)}"
                                        >
                                            Call
                                        </button>
                                      `
                                    : `
                                        <span class="no-action">
                                            —
                                        </span>
                                      `
                            }

                        </td>

                    </tr>
                `;
            }
        ).join("");
}



/* =========================================================
   QUEUE SUMMARY
   ========================================================= */

// Named helper updateQueueSummary: read its arguments and return value; callers determine whether it renders UI or performs an action.
function updateQueueSummary() {

    const waitingStudents =
        queueData.filter(
            item =>
                String(
                    item.status || ""
                ).toUpperCase() === "WAITING"
        );


    const count =
        waitingStudents.length;


    if (waitingCount) {
        waitingCount.textContent =
            count;
    }


    if (queueLength) {
        queueLength.textContent =
            queueData.length;
    }


    if (queueLengthInsight) {
        queueLengthInsight.textContent =
            count;
    }


    if (queueInsight) {

        if (count === 0) {

            queueInsight.textContent =
                "No students are currently waiting.";

        } else if (count <= 3) {

            queueInsight.textContent =
                "Queue is currently light.";

        } else if (count <= 7) {

            queueInsight.textContent =
                "Moderate queue. Continue normal service.";

        } else {

            queueInsight.textContent =
                "Queue is busy. Additional counter capacity may help.";
        }
    }
}



/* =========================================================
   CURRENT SERVICE
   ========================================================= */

// Named helper restoreCurrentServiceFromQueue: read its arguments and return value; callers determine whether it renders UI or performs an action.
function restoreCurrentServiceFromQueue() {

    const active =
        queueData.find(
            item => {

                const status =
                    String(
                        item.status || ""
                    ).toUpperCase();

                return (
                    status === "CALLED" ||
                    status === "SERVING"
                );
            }
        );


    if (active) {

        currentQueueItem =
            active;

        updateCurrentService();

    } else {

        clearCurrentService();
    }
}


// Named helper updateCurrentService: read its arguments and return value; callers determine whether it renders UI or performs an action.
function updateCurrentService() {

    if (!currentQueueItem) {
        clearCurrentService();
        return;
    }


    const status =
        String(
            currentQueueItem.status || ""
        ).toUpperCase();


    if (currentToken) {

        currentToken.textContent =
            formatToken(
                currentQueueItem.tokenNumber
            );
    }


    if (currentStudent) {

        currentStudent.textContent =
            `#${currentQueueItem.studentId}`;
    }


    if (currentService) {

        currentService.textContent =
            `Service ${currentQueueItem.serviceId}`;
    }


    if (currentExpectedTime) {

        currentExpectedTime.textContent =
            "—";
    }


    if (serviceState) {

        serviceState.textContent =
            status || "IDLE";

        serviceState.className =
            "service-state " +
            status.toLowerCase();
    }


    if (startBtn) {

        startBtn.disabled =
            status !== "CALLED" ||
            counterState !== "AVAILABLE";
    }


    if (completeBtn) {

        completeBtn.disabled =
            status !== "SERVING";
    }


    if (skipBtn) {

        skipBtn.disabled =
            !(
                status === "CALLED" ||
                status === "SERVING"
            );
    }
}


// Named helper clearCurrentService: read its arguments and return value; callers determine whether it renders UI or performs an action.
function clearCurrentService() {

    currentQueueItem =
        null;


    if (currentToken) {
        currentToken.textContent = "—";
    }


    if (currentStudent) {
        currentStudent.textContent = "—";
    }


    if (currentService) {
        currentService.textContent = "—";
    }


    if (currentExpectedTime) {
        currentExpectedTime.textContent = "—";
    }


    if (serviceState) {

        serviceState.textContent =
            "IDLE";

        serviceState.className =
            "service-state idle";
    }


    if (startBtn) {
        startBtn.disabled = true;
    }


    if (completeBtn) {
        completeBtn.disabled = true;
    }


    if (skipBtn) {
        skipBtn.disabled = true;
    }
}



/* =========================================================
   CALL NEXT
   ========================================================= */

// Named helper callNextStudent: read its arguments and return value; callers determine whether it renders UI or performs an action.
async function callNextStudent() {

    if (
        counterState !== "AVAILABLE"
    ) {

        showQueueMessage(
            "The counter is not available.",
            "error"
        );

        return;
    }


    if (currentQueueItem) {

        showQueueMessage(
            "Complete or skip the current student first.",
            "error"
        );

        return;
    }


    try {

        callNextBtn.disabled = true;

        showQueueMessage(
            "Calling next student..."
        );


        const result =
            await postForm(
                API.callNext
            );


        if (!result.data) {

            throw new Error(
                result.message ||
                "No student was called."
            );
        }


        currentQueueItem =
            result.data;


        addActivity(
            "Student called",
            `Token ${formatToken(
                currentQueueItem.tokenNumber
            )}`
        );


        await loadQueue();


        /*
         * If the queue endpoint has not yet reflected the
         * new status, retain the Call Next response.
         */
        if (!currentQueueItem) {

            currentQueueItem =
                result.data;
        }


        updateCurrentService();


        showQueueMessage(
            result.message ||
            "Next student called.",
            "success"
        );


    } catch (error) {

        console.error(
            "Call Next error:",
            error
        );


        showQueueMessage(
            error.message ||
            "Unable to call the next student.",
            "error"
        );

    } finally {

        callNextBtn.disabled =
            counterState !== "AVAILABLE";
    }
}



/* =========================================================
   INDIVIDUAL CALL BUTTON
   ========================================================= */

// Named helper handleRowCall: read its arguments and return value; callers determine whether it renders UI or performs an action.
async function handleRowCall(queueId) {

    /*
     * The backend currently exposes Call Next,
     * not a call-by-queueId endpoint.
     *
     * Therefore the row's Call button uses
     * the same Call Next operation.
     */

    if (
        currentQueueItem ||
        counterState !== "AVAILABLE"
    ) {

        showQueueMessage(
            "Finish the current service before calling another student.",
            "error"
        );

        return;
    }


    await callNextStudent();
}



/* =========================================================
   START SERVICE
   ========================================================= */

// Named helper startService: read its arguments and return value; callers determine whether it renders UI or performs an action.
async function startService() {

    if (!currentQueueItem) {
        return;
    }


    try {

        startBtn.disabled = true;


        await postForm(
            API.start,
            {
                queueId:
                    currentQueueItem.queueId
            }
        );


        addActivity(
            "Service started",
            `Token ${formatToken(
                currentQueueItem.tokenNumber
            )}`
        );


        await loadQueue();


        showQueueMessage(
            "Service started successfully.",
            "success"
        );


    } catch (error) {

        console.error(
            "Start service error:",
            error
        );


        showQueueMessage(
            error.message ||
            "Unable to start service.",
            "error"
        );


        updateCurrentService();
    }
}



/* =========================================================
   COMPLETE SERVICE
   ========================================================= */

// Named helper completeService: read its arguments and return value; callers determine whether it renders UI or performs an action.
async function completeService() {

    if (!currentQueueItem) {
        return;
    }


    const queueId =
        currentQueueItem.queueId;


    const token =
        formatToken(
            currentQueueItem.tokenNumber
        );


    try {

        completeBtn.disabled = true;


        await postForm(
            API.complete,
            {
                queueId:
                    queueId
            }
        );


        addActivity(
            "Service completed",
            `Token ${token}`
        );


        clearCurrentService();


        await loadQueue();

        await loadAnalytics();


        showQueueMessage(
            "Service completed successfully.",
            "success"
        );


    } catch (error) {

        console.error(
            "Complete service error:",
            error
        );


        showQueueMessage(
            error.message ||
            "Unable to complete service.",
            "error"
        );


        updateCurrentService();
    }
}



/* =========================================================
   SKIP
   ========================================================= */

// Named helper skipService: read its arguments and return value; callers determine whether it renders UI or performs an action.
async function skipService() {

    if (!currentQueueItem) {
        return;
    }


    const confirmed =
        window.confirm(
            "Are you sure you want to skip this student?"
        );


    if (!confirmed) {
        return;
    }


    const queueId =
        currentQueueItem.queueId;


    const token =
        formatToken(
            currentQueueItem.tokenNumber
        );


    try {

        skipBtn.disabled = true;


        await postForm(
            API.skip,
            {
                queueId:
                    queueId
            }
        );


        addActivity(
            "Student skipped",
            `Token ${token}`
        );


        clearCurrentService();


        await loadQueue();

        await loadAnalytics();


        showQueueMessage(
            "Student skipped successfully.",
            "success"
        );


    } catch (error) {

        console.error(
            "Skip service error:",
            error
        );


        showQueueMessage(
            error.message ||
            "Unable to skip student.",
            "error"
        );


        updateCurrentService();
    }
}



/* =========================================================
   COUNTER STATUS
   ========================================================= */

// Named helper updateCounterStatus: read its arguments and return value; callers determine whether it renders UI or performs an action.
async function updateCounterStatus(
    active
) {

    try {

        await postForm(
            API.counterStatus,
            {
                active:
                    String(active)
            }
        );


        if (active) {

            counterState =
                "AVAILABLE";

            updateAvailabilityUI();

            addActivity(
                "Counter resumed",
                "Counter is available"
            );

        } else {

            counterState =
                "PAUSED";

            updateAvailabilityUI();

            addActivity(
                "Counter paused",
                "Counter is unavailable"
            );
        }


        updateCurrentService();


    } catch (error) {

        console.error(
            "Counter status error:",
            error
        );


        showQueueMessage(
            error.message ||
            "Unable to update counter status.",
            "error"
        );
    }
}


// Named helper updateAvailabilityUI: read its arguments and return value; callers determine whether it renders UI or performs an action.
function updateAvailabilityUI() {

    if (!availabilityState) {
        return;
    }


    availabilityState.textContent =
        counterState;


    availabilityState.className =
        "availability-badge " +
        counterState.toLowerCase();


    if (pauseBtn) {

        pauseBtn.disabled =
            counterState !== "AVAILABLE";
    }


    if (resumeBtn) {

        resumeBtn.disabled =
            counterState !== "PAUSED";
    }


    if (checkoutBtn) {

        checkoutBtn.disabled =
            counterState === "CHECKED OUT";
    }


    if (callNextBtn) {

        callNextBtn.disabled =
            counterState !== "AVAILABLE";
    }
}


// Named helper pauseCounter: read its arguments and return value; callers determine whether it renders UI or performs an action.
async function pauseCounter() {

    if (currentQueueItem) {

        showQueueMessage(
            "Complete or skip the current student before pausing.",
            "error"
        );

        return;
    }


    await updateCounterStatus(false);
}


// Named helper resumeCounter: read its arguments and return value; callers determine whether it renders UI or performs an action.
async function resumeCounter() {

    if (
        counterState !== "PAUSED"
    ) {
        return;
    }


    await updateCounterStatus(true);
}


// Named helper checkoutCounter: read its arguments and return value; callers determine whether it renders UI or performs an action.
async function checkoutCounter() {

    if (currentQueueItem) {

        showQueueMessage(
            "Complete or skip the current student before checking out.",
            "error"
        );

        return;
    }


    const confirmed =
        window.confirm(
            "Are you sure you want to check out from this counter?"
        );


    if (!confirmed) {
        return;
    }


    try {

        await updateCounterStatus(false);


        counterState =
            "CHECKED OUT";


        updateAvailabilityUI();


        if (callNextBtn) {
            callNextBtn.disabled = true;
        }


        addActivity(
            "Counter checked out",
            "Staff session remains open"
        );


    } catch (error) {

        console.error(
            "Checkout error:",
            error
        );
    }
}



/* =========================================================
   ANALYTICS
   ========================================================= */

// Named helper loadAnalytics: read its arguments and return value; callers determine whether it renders UI or performs an action.
async function loadAnalytics() {

    try {

        const result =
            await apiRequest(
                API.analytics,
                {
                    method: "GET",

                    headers: {
                        "Accept":
                            "application/json"
                    }
                }
            );


        const data =
            result.data || result;


        if (
            completedCount &&
            data.studentsServedToday !== undefined
        ) {

            completedCount.textContent =
                data.studentsServedToday;
        }


        if (
            averageWait &&
            data.averageWaitingTime !== undefined
        ) {

            averageWait.textContent =
                formatMinutes(
                    data.averageWaitingTime
                );
        }


        if (
            averageService &&
            data.averageServiceTime !== undefined
        ) {

            averageService.textContent =
                formatMinutes(
                    data.averageServiceTime
                );
        }


        if (
            noShowCount &&
            data.noShowCount !== undefined
        ) {

            noShowCount.textContent =
                data.noShowCount;
        }


        if (
            peakHour &&
            data.peakHour !== undefined
        ) {

            peakHour.textContent =
                data.peakHour || "N/A";
        }


        /*
         * The backend analytics gives the historical/
         * average waiting time. It is NOT recalculated here.
         */
        if (
            estimatedWait &&
            data.averageWaitingTime !== undefined
        ) {

            estimatedWait.textContent =
                formatMinutes(
                    data.averageWaitingTime
                );
        }


    } catch (error) {

        console.error(
            "Analytics error:",
            error
        );


        /*
         * Do not replace unavailable backend data
         * with fake values.
         */
    }
}



/* =========================================================
   UPCOMING BOOKINGS
   ========================================================= */

// Named helper keepBookingsUnavailable: read its arguments and return value; callers determine whether it renders UI or performs an action.
function keepBookingsUnavailable() {

    /*
     * There is currently no established staff-specific
     * upcoming-bookings endpoint in the backend contract.
     *
     * Therefore this remains "—" instead of using
     * fabricated data.
     */

    if (bookingCount) {

        bookingCount.textContent =
            "—";
    }
}



/* =========================================================
   RECENT ACTIVITY
   ========================================================= */

// Named helper addActivity: read its arguments and return value; callers determine whether it renders UI or performs an action.
function addActivity(
    title,
    description
) {

    if (!activityList) {
        return;
    }


    const item =
        document.createElement("div");


    item.className =
        "activity-item";


    item.innerHTML = `
        <span class="activity-dot"></span>

        <div>

            <strong>
                ${escapeHtml(title)}
            </strong>

            <span>
                ${escapeHtml(description)}
            </span>

        </div>
    `;


    activityList.prepend(item);


    /*
     * Keep the dashboard readable.
     */
    while (
        activityList.children.length > 6
    ) {

        activityList.removeChild(
            activityList.lastElementChild
        );
    }
}



/* =========================================================
   EVENT LISTENERS
   ========================================================= */

if (callNextBtn) {

    callNextBtn.addEventListener(
        "click",
        callNextStudent
    );
}


if (startBtn) {

    startBtn.addEventListener(
        "click",
        startService
    );
}


if (completeBtn) {

    completeBtn.addEventListener(
        "click",
        completeService
    );
}


if (skipBtn) {

    skipBtn.addEventListener(
        "click",
        skipService
    );
}


if (pauseBtn) {

    pauseBtn.addEventListener(
        "click",
        pauseCounter
    );
}


if (resumeBtn) {

    resumeBtn.addEventListener(
        "click",
        resumeCounter
    );
}


if (checkoutBtn) {

    checkoutBtn.addEventListener(
        "click",
        checkoutCounter
    );
}


if (queueBody) {

    queueBody.addEventListener(
        "click",
        function (event) {

            const button =
                event.target.closest(
                    ".row-call-btn"
                );


            if (!button) {
                return;
            }


            const queueId =
                button.dataset.queueId;


            handleRowCall(queueId);
        }
    );
}



/* =========================================================
   INITIALIZATION
   ========================================================= */

// Named helper initializeDashboard: read its arguments and return value; callers determine whether it renders UI or performs an action.
async function initializeDashboard() {

    if (dashboardInitialized) {
        return;
    }


    dashboardInitialized =
        true;


    try {

        await loadProfile();

        await Promise.all([
            loadQueue(),
            loadAnalytics()
        ]);


        keepBookingsUnavailable();


        counterState =
            "AVAILABLE";


        updateAvailabilityUI();


        console.log(
            "Staff dashboard initialized successfully."
        );


    } catch (error) {

        console.error(
            "Dashboard initialization error:",
            error
        );


        showQueueMessage(
            error.message ||
            "Unable to load staff dashboard data.",
            "error"
        );


        /*
         * This usually means the staff session
         * is missing or expired.
         */
    }
}


initializeDashboard();
