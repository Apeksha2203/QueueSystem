const API_BASE = "/api";

async function apiRequest(url, options = {}) {
    const response = await fetch(`${API_BASE}${url}`, {
        credentials: "include",
        ...options,
    });

    const text = await response.text();

    let data;

    try {
        data = JSON.parse(text);
    } catch {
        throw new Error(
            `Server returned an invalid response (${response.status}).`
        );
    }

    if (!response.ok || data.success === false) {
        const error=new Error(data.message || "API request failed."); error.status=response.status; throw error;
    }

    return data;
}

function formBody(values) {
    return new URLSearchParams(values);
}

export const api = {
    logout: () => apiRequest("/staff/logout", {method:"POST"}),
    login: (email, password) =>
        apiRequest("/staff/login", {
            method: "POST",
            headers: {
                "Content-Type": "application/x-www-form-urlencoded",
            },
            body: formBody({ email, password }),
        }),

    profile: () =>
        apiRequest("/staff/profile"),

    summary: () =>
        apiRequest("/staff/dashboard-summary"),

    queue: () =>
        apiRequest("/staff/queue"),

    recentActivity: () =>
        apiRequest("/staff/recent-activity"),

    counterStatus: () =>
        apiRequest("/staff/counter/status"),

    setCounterStatus: (status) =>
        apiRequest("/staff/counter/status", {
            method: "POST",
            headers: {
                "Content-Type": "application/x-www-form-urlencoded",
            },
            body: formBody({ status }),
        }),

    callNext: () =>
        apiRequest("/queue/call-next", {
            method: "POST",
        }),

    callStudent: (queueId) =>
        apiRequest("/queue/call", {
            method: "POST",
            headers: {
                "Content-Type": "application/x-www-form-urlencoded",
            },
            body: formBody({ queueId }),
        }),

    startService: (queueId) =>
        queueAction("/start", queueId),

    completeService: (queueId) =>
        queueAction("/complete", queueId),

    noShow: (queueId) => queueAction("/no-show", queueId),
    skipStudent: (queueId) =>
        queueAction("/skip", queueId),
};

function queueAction(path, queueId) {
    return apiRequest(`/queue${path}`, {
        method: "POST",
        headers: {
            "Content-Type": "application/x-www-form-urlencoded",
        },
        body: formBody({ queueId }),
    });
}
