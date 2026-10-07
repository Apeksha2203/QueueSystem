import { useCallback, useEffect, useState } from "react";

export async function studentRequest(path, payload, signal) {
  const response = await fetch(`/api/student${path}`, {
    method: payload ? "POST" : "GET",
    credentials: "include",
    cache: "no-store",
    signal: signal || AbortSignal.timeout(12000),
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

export function useStudentBackend() {
  const [profile, setProfile] = useState(null);
  const [ready, setReady] = useState(false);
  const [catalog, setCatalog] = useState([]);
  const [tickets, setTickets] = useState([]);
  const [bookings, setBookings] = useState([]);
  const [error, setError] = useState("");
  const [busy, setBusy] = useState(false);
  const refresh = useCallback(async () => {
    try {
      const [overview, appointments] = await Promise.all([studentRequest("/overview"), studentRequest("/bookings")]);
      setCatalog(overview.services); setTickets(overview.tickets); setBookings(appointments); setError("");
    } catch (failure) {
      setError(failure.message);
      if (failure.status === 401) { setProfile(null); setTickets([]); setBookings([]); setCatalog([]); }
      throw failure;
    }
  }, []);
  useEffect(() => {
    const controller = new AbortController();
    studentRequest("/session", undefined, controller.signal).then(setProfile).catch((failure) => {
      if (failure.name !== "AbortError") setError(failure.message);
    }).finally(() => setReady(true));
    return () => controller.abort();
  }, []);
  useEffect(() => {
    if (!profile) return;
    refresh().catch(() => {});
    const timer = setInterval(() => { if (!document.hidden) refresh().catch(() => {}); }, 8000);
    const refreshWhenVisible = () => { if (!document.hidden) refresh().catch(() => {}); };
    window.addEventListener("focus", refreshWhenVisible);
    document.addEventListener("visibilitychange", refreshWhenVisible);
    return () => {
      clearInterval(timer);
      window.removeEventListener("focus", refreshWhenVisible);
      document.removeEventListener("visibilitychange", refreshWhenVisible);
    };
  }, [profile, refresh]);
  async function action(path, payload) {
    setBusy(true); setError("");
    try {
      const data = await studentRequest(path, payload);
      if (path === "/login" || path === "/register") setProfile(data);
      else if (path === "/logout") { setProfile(null); setCatalog([]); setTickets([]); setBookings([]); }
      else await refresh();
      return data;
    } catch (failure) { setError(failure.message); throw failure; }
    finally { setBusy(false); }
  }
  return { profile, ready, catalog, tickets, bookings, error, busy, refresh, action };
}
