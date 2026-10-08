// VIVA GUIDE: Student fetch wrapper and shared backend hook. Includes credentials, error handling, polling and focused-tab refresh.
import { useCallback, useEffect, useRef, useState } from "react";

// Send the student HTTP request with cookies, parse JSON and raise errors; payload presence selects POST versus GET.
export async function studentRequest(path, payload, signal, base = "/api/student") {
  const response = await fetch(`${base}${path}`, {
    method: payload ? "POST" : "GET",
    credentials: "include",
    cache: "no-store",
    // The free cloud backend can take longer to wake up; do not mistake a slow response for an empty catalogue.
    signal: signal || AbortSignal.timeout(60000),
    headers: { Accept: "application/json", "X-Requested-With": "XMLHttpRequest" },
    body: payload ? new URLSearchParams(payload) : undefined,
  });
  let body;
  try { body = await response.json(); }
  catch { throw new Error("The Java backend is not responding. Please try again shortly."); }
  if (!response.ok || !body.success) {
    const error = new Error(body.message || "The request could not be completed.");
    error.status = response.status;
    throw error;
  }
  return body.data;
}

// Share session, services, tickets, bookings, errors and busy state across screens; fetch updates cause React renders.
export function useStudentBackend() {
  // Authenticated student details returned by the session/login endpoint.
  const [profile, setProfile] = useState(null);
  // Initial session lookup has finished; prevents rendering a login screen before restoration completes.
  const [ready, setReady] = useState(false);
  // Service catalogue returned by the backend, used by service cards and booking options.
  const [catalog, setCatalog] = useState([]);
  // Active student tickets returned by the overview endpoint, including status and queue estimates.
  const [tickets, setTickets] = useState([]);
  // Reservation history used by Upcoming, Past and Cancelled filters.
  const [bookings, setBookings] = useState([]);
  // User-facing request/form error; empty text means there is no current error banner.
  const [error, setError] = useState("");
  // An action is pending; disable related controls to reduce repeated submissions.
  const [busy, setBusy] = useState(false);
  const [catalogLoaded, setCatalogLoaded] = useState(false);
  const refreshing = useRef(null);
  const refresh = useCallback(async () => {
    // Reuse an outstanding refresh rather than overlapping slow database requests every eight seconds.
    if (refreshing.current) return refreshing.current;
    const work = (async () => {
      // Show the inexpensive public catalogue immediately, independently of personal history/queue estimates.
      const results = await Promise.allSettled([
        studentRequest("/services", undefined, undefined, "/api").then(services => {
          setCatalog(services); setCatalogLoaded(true);
        }),
        studentRequest("/overview").then(overview => {
          setCatalog(overview.services); setCatalogLoaded(true); setTickets(overview.tickets);
        }),
        studentRequest("/bookings").then(setBookings),
      ]);
      const failures = results.filter(result => result.status === "rejected").map(result => result.reason);
      if (failures.some(failure => failure.status === 401)) {
        setProfile(null); setTickets([]); setBookings([]); setCatalog([]); setCatalogLoaded(false);
      }
      // A successful overview can replace a failed public-catalogue request; personal errors stay explicit.
      const failure = failures.find(item => item.status === 401)
        || (results[1].status === "rejected" ? results[1].reason : null)
        || (results[2].status === "rejected" ? results[2].reason : null);
      setError(failure?.message || "");
      if (failure) throw failure;
    })();
    refreshing.current = work;
    try { return await work; }
    finally { refreshing.current = null; }
  }, []);
  // Restore the cookie-backed session once; abort the request when the effect is cleaned up.
  useEffect(() => {
    const controller = new AbortController();
    studentRequest("/session", undefined, controller.signal).then(setProfile).catch((failure) => {
      if (failure.name !== "AbortError") setError(failure.message);
    }).finally(() => setReady(true));
    // Abort the in-flight request when this effect is cleaned up.
    return () => controller.abort();
  }, []);
  // While logged in, refresh every 8 seconds only in visible tabs and refresh on focus/visibility changes.
  useEffect(() => {
    if (!profile) return;
    refresh().catch(() => {});
    // Polling refreshes the server view periodically; the effect cleanup must clear this timer.
    const timer = setInterval(() => { if (!document.hidden) refresh().catch(() => {}); }, 8000);
    // Catch up with server changes when the user returns to this tab.
    const refreshWhenVisible = () => { if (!document.hidden) refresh().catch(() => {}); };
    window.addEventListener("focus", refreshWhenVisible);
    document.addEventListener("visibilitychange", refreshWhenVisible);
    return () => {
      clearInterval(timer);
      window.removeEventListener("focus", refreshWhenVisible);
      document.removeEventListener("visibilitychange", refreshWhenVisible);
    };
  }, [profile, refresh]);
  // Run an authenticated action, update login/logout state or refresh records, and clear busy state even on failure.
  async function action(path, payload) {
    setBusy(true); setError("");
    try {
      const data = await studentRequest(path, payload);
      if (path === "/login" || path === "/register") setProfile(data);
      else if (path === "/logout") { setProfile(null); setCatalog([]); setTickets([]); setBookings([]); setCatalogLoaded(false); }
      // A saved action remains successful even if its subsequent display refresh fails; never invite duplicate writes.
      else await refresh().catch(() => {});
      return data;
    } catch (failure) { setError(failure.message); throw failure; }
    finally { setBusy(false); }
  }
  return { profile, ready, catalog, catalogLoaded, tickets, bookings, error, busy, refresh, action };
}
