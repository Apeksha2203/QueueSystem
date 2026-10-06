import { useEffect, useLayoutEffect, useRef, useState } from "react";
import {
  HashRouter,
  NavLink,
  Link,
  Route,
  Routes,
  Navigate,
  useLocation,
  useNavigate,
} from "react-router-dom";
import {
  ArrowUpRight,
  ArrowRight,
  MapPin,
  Plus,
  Sun,
  Moon,
  X,
  Check,
  LayoutGrid,
  Ticket as TicketIcon,
  CalendarDays,
  Settings as SettingsIcon,
  LogOut,
  Coins,
  FileText,
  Library,
  Users,
} from "lucide-react";
import gsap from "gsap";
import { studentRequest, useStudentBackend } from "./student-api.js";
import { queueLayout } from "./queue-layout.js";
import "./index.css";
import "./styles/original-identity.css";

const nav = [
  ["/dashboard", LayoutGrid, "Overview"],
  ["/queue", TicketIcon, "My queue"],
  ["/bookings", CalendarDays, "Bookings"],
  ["/settings", SettingsIcon, "Settings"],
];
function Brand() {
  return (
    <Link className="brand" to="/dashboard">
      <span className="brand-mark">
        c<span>q</span>
      </span>
      <span>
        campusqueue
        <span className="brand-caption">LESS WAIT. MORE CAMPUS.</span>
      </span>
    </Link>
  );
}
function Theme({ dark, toggle }) {
  return (
    <button
      className="icon-button"
      onClick={toggle}
      aria-label={`Switch to ${dark ? "light" : "dark"} theme`}
    >
      {dark ? <Sun size={19} /> : <Moon size={19} />}
    </button>
  );
}
function PageHeading({ eyebrow, title, description, children }) {
  return (
    <header className="page-heading">
      <div>
        <p className="eyebrow">{eyebrow}</p>
        <h1>{title}</h1>
        <p className="muted">{description}</p>
      </div>
      {children}
    </header>
  );
}
function Ticket({ ticket, compact = false }) {
  return (
    <article className={`ticket ${compact ? "compact-ticket" : ""}`}>
      <div className="ticket-top">
        <span className="eyebrow">YOUR CAMPUS PASS</span>
        <TicketIcon size={22} />
      </div>
      <div className="ticket-body">
        <p>{ticket.serviceName}</p>
        <div className="token">{ticket.token}</div>
        <span className="ticket-status">
          <span className="status-dot" />{" "}
          {ticket.status === "CALLED" || ticket.status === "SERVING"
            ? "Your turn — head to the counter"
            : "You’re in. Make yourself at home."}
        </span>
      </div>
      <div className="ticket-tear" />
      <div className="ticket-bottom">
        <div>
          <span className="eyebrow">DESTINATION</span>
          <strong>{ticket.place}</strong>
        </div>
        <div>
          <span className="eyebrow">COUNTER</span>
          <strong>Service desk</strong>
        </div>
      </div>
      <div className="ticket-footer">
        <div className="barcode" aria-hidden="true" />
        <span className="mono">{ticket.token} / STUDENT COPY</span>
      </div>
    </article>
  );
}
function VirtualQueue({ ticket }) {
  const scene = useRef(null);
  const hasPosition = useRef(false);
  const [paused, setPaused] = useState(false);
  const layout = queueLayout(ticket?.ahead ?? 4);
  const { ahead, grouped, hidden, people, you: position } = layout;
  useLayoutEffect(() => {
    const marker = scene.current?.querySelector(".virtual-you");
    if (!marker) return;
    if (!hasPosition.current) {
      gsap.set(marker, { x: position });
      hasPosition.current = true;
      return;
    }
    const tween = gsap.to(marker, {
      x: position,
      duration: matchMedia("(prefers-reduced-motion: reduce)").matches ? 0 : 0.7,
      ease: "power3.inOut",
    });
    return () => tween.kill();
  }, [position]);
  return (
    <div className={`virtual-queue ${paused ? "is-paused" : ""}`} ref={scene}>
      <div className="virtual-caption">
        <span className="eyebrow">A LITTLE CLOSER, EVERY TURN</span>
        <button type="button" className="virtual-pause" aria-pressed={paused} onClick={() => setPaused(!paused)}>
          {paused ? "Play motion" : "Pause motion"}
        </button>
      </div>
      <svg viewBox="0 0 550 225" role="img" aria-label={`Illustrated virtual queue: ${ahead} people ahead, between you and the service desk on the right. ${ticket ? "Position follows your ticket." : "Example queue."}`}>
        <ellipse cx="270" cy="176" rx="238" ry="21" fill="var(--soft)" />
        <path className="virtual-flow" d="M40 175 H398" fill="none" stroke="var(--orange)" strokeWidth="2" strokeDasharray="3 10" opacity=".45" />
        <path d="M381 169 L389 175 L381 181" fill="none" stroke="var(--orange)" strokeWidth="2" />
        {grouped && <g className="virtual-group">
          <path d="M97 167 l7 -9 M97 176 l7 -9 M192 167 l7 -9 M192 176 l7 -9" stroke="var(--orange)" strokeWidth="2" fill="none" />
          <rect x="111" y="78" width="75" height="79" rx="18" fill="var(--soft)" stroke="var(--line)" strokeWidth="2" />
          <g fill="var(--ink)" opacity=".45"><circle cx="137" cy="96" r="5"/><circle cx="148" cy="92" r="5"/><circle cx="159" cy="96" r="5"/></g>
          <text x="148" y="123" textAnchor="middle" fill="var(--orange)" fontSize={hidden > 999 ? 18 : 23} fontWeight="700">+{hidden}</text>
          <text x="148" y="141" textAnchor="middle" fill="var(--muted)" fontSize="9">MORE AHEAD</text>
        </g>}
        {people.map((x, i) => (
          <g key={i} transform={`translate(${x},0)`} className="virtual-person" style={{ "--motion-delay": `${i * -0.4}s` }}>
            <ellipse cx="0" cy="167" rx="19" ry="5" fill="var(--ink)" opacity=".07" />
            <g className="virtual-body">
              <circle cy="91" r="12" fill="var(--ink)" opacity=".55" />
              <path d="M-15 119 Q-15 108 0 108 Q15 108 15 119 L12 141 H-12 Z" fill={i % 2 ? "#b38a42" : "#8073b8"} opacity=".65" />
              <path d="M-8 140 L-11 161 M8 140 L11 161" fill="none" stroke="var(--ink)" strokeWidth="6" strokeLinecap="round" opacity=".55" />
              <path d="M12 116 L23 127" fill="none" stroke="var(--ink)" strokeWidth="5" strokeLinecap="round" opacity=".5" />
            </g>
          </g>
        ))}
        <g className="virtual-you">
          <ellipse cy="167" rx="21" ry="6" fill="var(--orange)" opacity=".18" />
          <g className="virtual-body">
            <rect x="-26" y="42" width="52" height="23" rx="11" fill="var(--orange)" />
            <text y="57" textAnchor="middle" fill="white" fontSize="10" fontWeight="700">YOU</text>
            <circle cy="91" r="13" fill="var(--ink)" />
            <path d="M-17 119 Q-17 107 0 107 Q17 107 17 119 L13 141 H-13 Z" fill="var(--orange)" />
            <path d="M-8 141 L-11 161 M8 141 L11 161" fill="none" stroke="var(--ink)" strokeWidth="7" strokeLinecap="round" />
            <path d="M14 118 L26 129" fill="none" stroke="var(--ink)" strokeWidth="5" strokeLinecap="round" />
            <rect x="20" y="118" width="16" height="21" rx="3" transform="rotate(-12 28 129)" fill="var(--surface)" stroke="var(--orange)" strokeWidth="2" />
            <path d="M24 124 H31 M24 128 H30" stroke="var(--orange)" strokeWidth="1.5" />
          </g>
        </g>
        <g>
          <rect x="422" y="43" width="79" height="31" rx="8" fill="var(--ink)" />
          <text x="461" y="63" textAnchor="middle" fill="var(--surface)" fontSize="10" fontWeight="600">SERVICE DESK</text>
          <circle cx="462" cy="96" r="11" fill="var(--ink)" opacity=".7" />
          <path d="M447 122 Q447 109 462 109 Q477 109 477 122" fill="var(--orange)" opacity=".6" />
          <rect x="411" y="124" width="102" height="45" rx="7" fill="var(--surface)" stroke="var(--line)" strokeWidth="2" />
          <path d="M405 124 H519" stroke="var(--orange)" strokeWidth="5" strokeLinecap="round" />
          <rect x="424" y="101" width="25" height="20" rx="3" fill="var(--ink)" />
          <path d="M434 120 V124" stroke="var(--ink)" strokeWidth="3" />
          <circle className="virtual-beacon" cx="496" cy="144" r="4" fill="var(--orange)" />
        </g>
        <text x="40" y="211" fill="var(--muted)" fontSize="10" fontFamily="monospace">{ticket ? `${ahead} AHEAD / YOUR PLACE IN LINE` : "ILLUSTRATED QUEUE / TAKE YOUR TIME"}</text>
      </svg>
      <p className="virtual-note" aria-live="polite">{grouped ? `Grouped view · ${people.length} people shown + ${hidden} grouped ahead. The group shrinks as staff serve the queue.` : ticket ? "Your place moves toward the desk when your queue advances." : "A place in line. A little freedom in your day."}</p>
    </div>
  );
}
function Overview({ ticket, profile, join, catalog, busy }) {
  return (
    <>
      <PageHeading
        eyebrow="YOUR DAY, ON YOUR TERMS"
        title={
          <>
            Hey, {profile.name.split(" ")[0]}.{" "}
            <span className="serif">Go live a little.</span>
          </>
        }
        description="Handle the essentials. Get back to everything else."
      />
      <div className="overview-grid">
        <section className="feature-panel">
          <div className="section-label">
            <span className="eyebrow">
              {ticket ? "YOUR NEXT STOP" : "CAMPUS, WITHOUT THE QUEUES"}
            </span>
            <span className="tag">
              {ticket ? "Active ticket" : "Student edition"}
            </span>
          </div>
          <h2>
            {ticket ? (
              <>
                Your place is saved.
                <br />
                <span className="serif">Your time is yours.</span>
              </>
            ) : (
              <>
                Less standing around.
                <br />
                <span className="serif">More getting around.</span>
              </>
            )}
          </h2>
          <p>
            {ticket
              ? `${ticket.ahead} people ahead · ${ticket.wait === null ? "Estimate unavailable" : `About ${ticket.wait} minutes to go.`}`
              : "Pick a service below, take a digital ticket, and spend the wait wherever you like."}
          </p>
          {ticket ? (
            <Link className="button primary" to="/queue">
              View my ticket <ArrowUpRight size={19} />
            </Link>
          ) : (
            <button
              className="button primary"
              onClick={() =>
                document.getElementById("services").scrollIntoView({
                  behavior: matchMedia("(prefers-reduced-motion: reduce)")
                    .matches
                    ? "instant"
                    : "smooth",
                })
              }
            >
              Find your next stop <ArrowRight size={19} />
            </button>
          )}
          <VirtualQueue ticket={ticket} />
        </section>
        <aside className="day-panel">
          <p className="eyebrow">A LITTLE ROOM IN YOUR DAY</p>
          <div className="time-art">
            Make
            <br />
            <span className="serif">time.</span>
            <span className="asterisk">✳</span>
          </div>
          <p>
            For the coffee.
            <br />
            For the conversation.
            <br />
            For whatever comes next.
          </p>
          <div className="day-bottom">
            <span className="mono">CAMPUSQUEUE / 01</span>
            <ArrowUpRight size={28} />
          </div>
        </aside>
      </div>
      <section id="services" className="services-section">
        <div className="section-heading">
          <div>
            <p className="eyebrow">CHOOSE YOUR DESTINATION</p>
            <h2>What brings you here?</h2>
          </div>
          <span className="muted small">
            Service duration estimates
          </span>
        </div>
        <div className="service-list">
          {!catalog.length && <p className="muted">No campus services have been configured yet. Your campus team will add them to the database.</p>}
          {catalog.map((s) => {
            const ServiceIcon =
              {
                "Fee Payment": Coins,
                Registration: FileText,
                "Library Services": Library,
                "General Inquiry": Users,
                "General Inquiries": Users,
                "Document Verification": FileText,
              }[s.serviceName] || MapPin;
            return (
              <button
                className="service-row"
                key={s.serviceId}
                disabled={busy}
                onClick={() => join(s)}
              >
                <span
                  className="service-code"
                  style={{ "--service-color": s.color }}
                >
                  <ServiceIcon size={27} aria-hidden="true" />
                </span>
                <span className="service-copy">
                  <strong>{s.serviceName}</strong>
                  <span>{s.description}</span>
                </span>
                <span className="service-place">
                  <MapPin size={14} />
                  {s.serviceName}
                </span>
                <span className="service-wait">
                  <strong>{s.averageServiceTime}</strong>
                  <span>min / service</span>
                </span>
                <span className="row-arrow">
                  <ArrowUpRight size={21} />
                </span>
                <span className="sr-only">Join service queue</span>
              </button>
            );
          })}
        </div>
      </section>
      <footer className="page-footer">
        <span>Good things happen outside the queue.</span>
        <span className="mono">TAKE A NUMBER. TAKE YOUR TIME.</span>
      </footer>
    </>
  );
}
function QueuePage({ ticket, tickets, selectTicket, refresh }) {
  if (!ticket) return <><PageHeading eyebrow="YOUR PLACE IN LINE" title={<>A ticket to <span className="serif">more time.</span></>} description="You don’t have an active ticket yet." /><div className="empty-state"><TicketIcon size={55}/><h2>Where are we headed?</h2><p>Choose a service to join its queue.</p><Link to="/dashboard" className="button primary">Explore services <ArrowRight size={18}/></Link></div></>;
  const called = ticket.status === "CALLED";
  const serving = ticket.status === "SERVING";
  return <>
    <PageHeading eyebrow="YOUR PLACE IN LINE" title={<>You’re in. <span className="serif">Breathe easy.</span></>} description="Your ticket and position, updated from the service queue." />
    {tickets.length > 1 && <div className="tabs" aria-label="Your active service queues">{tickets.map((entry) => <button className={entry.queueId === ticket.queueId ? "active" : ""} key={entry.queueId} onClick={() => selectTicket(entry.queueId)}>{entry.serviceName}</button>)}</div>}
    <div className="queue-grid"><Ticket ticket={ticket}/><div className="queue-info">
      <section className="queue-progress"><div className="section-label"><p className="eyebrow">THE ROAD TO YOUR TURN</p><span className="tag"><span className="status-dot"/>{serving ? "Being served" : called ? "Called" : "Waiting"}</span></div>
        <div className="wait-big">{serving ? "You’re up." : called ? "It’s time." : ticket.wait === null ? <span>Estimate unavailable</span> : <>{ticket.wait}<span> min / service</span></>}</div>
        <p className="muted" aria-live="polite">{serving ? "Your service is in progress." : called ? "Staff have called your token. Please head to the service desk." : `${ticket.ahead} people ahead. ${ticket.ahead === 0 ? "You’re first in line; wait for staff to call your token." : "We’ll update your position as staff serve the queue."}`}</p>
        <VirtualQueue ticket={ticket}/>
        <div className="queue-legend"><span>Now serving: {ticket.currentToken || "No token called"}</span><span>{ticket.serviceName}</span></div>
      </section><section className="bring-panel"><span className="eyebrow">BEFORE YOU HEAD OVER</span><h3>Keep your student ID handy.</h3><p className="muted">The queue is shared across this service’s counters. Staff will direct you to the right desk.</p></section>
      <div className="queue-actions"><button className="button secondary" onClick={() => refresh().catch(() => {})}>Refresh position <ArrowRight size={16}/></button></div>
    </div></div>
  </>;
}
function Modal({ title, close, children }) {
  const ref = useRef(null);
  useEffect(() => {
    const prior = document.activeElement;
    const dialog = ref.current;
    dialog.showModal();
    return () => {
      dialog.close();
      prior?.focus();
    };
  }, []);
  return (
    <dialog ref={ref} className="modal" onCancel={close}>
      <div className="section-heading">
        <h2>{title}</h2>
        <button
          className="icon-button"
          onClick={close}
          aria-label="Close dialog"
        >
          <X size={20} />
        </button>
      </div>
      {children}
    </dialog>
  );
}
function BookingForm({ catalog, backend, close }) {
  const [serviceId, setServiceId] = useState(String(catalog[0]?.serviceId || ""));
  const [date, setDate] = useState(new Date().toLocaleDateString("en-CA"));
  const [slots, setSlots] = useState([]);
  const [slot, setSlot] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  useEffect(() => {
    if (!serviceId || !date) return;
    const controller = new AbortController();
    setLoading(true); setSlot(""); setSlots([]); setError("");
    studentRequest(`/bookings/slots?${new URLSearchParams({ serviceId, date })}`, undefined, controller.signal)
      .then(setSlots).catch((failure) => { if (failure.name !== "AbortError") setError(failure.message); })
      .finally(() => { if (!controller.signal.aborted) setLoading(false); });
    return () => controller.abort();
  }, [serviceId, date]);
  return <Modal title="Plan a campus visit" close={close}><form className="form" onSubmit={async (event) => {
    event.preventDefault(); setError("");
    try { await backend.action("/bookings/create", { serviceId, bookingDate: date, bookingTime: slot }); close(); }
    catch (failure) { setError(failure.message); }
  }}>
    <label>Service<select value={serviceId} onChange={(event) => setServiceId(event.target.value)} required>{catalog.map((service) => <option value={service.serviceId} key={service.serviceId}>{service.serviceName}</option>)}</select></label>
    <label>Date<input type="date" min={new Date().toLocaleDateString("en-CA")} value={date} onChange={(event) => setDate(event.target.value)} required/></label>
    <label>Available time slot<select value={slot} onChange={(event) => setSlot(event.target.value)} required disabled={loading || !slots.length}><option value="">{loading ? "Loading slots…" : slots.length ? "Choose a time" : "No available slots"}</option>{slots.map((time) => <option value={time} key={time}>{time.slice(0,5)}</option>)}</select></label>
    <p className="muted small">One booking per service per slot. Please arrive on time; the no-show grace period is 5 minutes.</p>
    {error && <p role="alert" className="danger">{error}</p>}
    <button className="button primary" disabled={!slot || backend.busy}>{backend.busy ? "Saving…" : "Book appointment"}<Check size={18}/></button>
  </form></Modal>;
}
function BookingsPage({ backend }) {
  const [tab, setTab] = useState("Upcoming");
  const [editing, setEditing] = useState(false);
  const [cancel, setCancel] = useState(null);
  const filtered = backend.bookings.filter((booking) => tab === "Cancelled" ? booking.status === "CANCELLED" : tab === "Past" ? booking.status !== "CANCELLED" && (booking.status !== "BOOKED" || new Date(`${booking.date}T${booking.time}`) < new Date()) : booking.status === "BOOKED" && new Date(`${booking.date}T${booking.time}`) >= new Date());
  const serviceName = (id) => backend.catalog.find((service) => service.serviceId === id)?.serviceName || `Service ${id}`;
  return <>
    <PageHeading eyebrow="MAKE ROOM FOR WHAT’S NEXT" title={<>A little <span className="serif">forward planning.</span></>} description="Your campus appointments, all in one place."><button className="button primary" disabled={!backend.catalog.length || backend.busy} onClick={() => setEditing(true)}><Plus size={18}/>New booking</button></PageHeading>
    <div className="tabs" role="tablist" aria-label="Booking status">{["Upcoming", "Past", "Cancelled"].map((name) => <button role="tab" aria-selected={tab === name} className={tab === name ? "active" : ""} key={name} onClick={() => setTab(name)}>{name}</button>)}</div>
    {!filtered.length ? <div className="empty-state"><CalendarDays size={45}/><h2>No {tab.toLowerCase()} bookings.</h2><p>Your saved appointments will appear here.</p></div> : <div className="agenda">{filtered.map((booking) => <article className="agenda-row" key={booking.id}><div className="agenda-date"><span>{new Date(`${booking.date}T12:00`).toLocaleDateString("en", {month:"short"})}</span><strong>{booking.date.slice(-2)}</strong></div><div className="agenda-detail"><span className="mono">{booking.time.slice(0,5)}</span><h3>{serviceName(booking.serviceId)}</h3><p className="muted">Campus appointment <span className="tag">{booking.status.replaceAll("_", " ")}</span></p></div>{booking.status === "BOOKED" && <div className="agenda-actions"><button disabled={backend.busy} className="text-button danger" onClick={() => setCancel(booking)}>Cancel</button></div>}</article>)}</div>}
    {editing && <BookingForm catalog={backend.catalog} backend={backend} close={() => setEditing(false)}/>}
    {cancel && <Modal title="Cancel this appointment?" close={() => setCancel(null)}><p>{serviceName(cancel.serviceId)} · {cancel.date} at {cancel.time.slice(0,5)}</p><button className="button primary" disabled={backend.busy} onClick={async () => { try { await backend.action("/bookings/cancel", {bookingId:cancel.id}); setCancel(null); } catch {} }}>Cancel appointment</button></Modal>}
  </>;
}
function SettingsPage({ profile, dark, toggle, logout, busy }) {
  return <><PageHeading eyebrow="THE PERSONAL DETAILS" title={<>Make yourself <span className="serif">at home.</span></>} description="Your account and appearance preferences."/>
    <div className="settings-grid"><section className="settings-panel"><div className="section-heading"><h2>Your student profile</h2><span className="avatar">{profile.name[0]}</span></div><div className="form"><label>Full name<input value={profile.name} readOnly/></label><label>Email address<input value={profile.email} readOnly/></label><label>Student ID<input value={profile.studentId} readOnly/></label><p className="muted small">Your registered campus account.</p></div></section>
    <section className="settings-panel"><h2>The way you like it</h2><div className="preference"><div><h3>Dark appearance</h3><p className="muted small">A quieter palette for after hours.</p></div><button role="switch" aria-checked={dark} aria-label="Dark appearance" className={`toggle ${dark ? "on" : ""}`} onClick={toggle}><span/></button></div><button disabled={busy} className="text-button danger signout" onClick={logout}><LogOut size={16}/>Sign out</button></section></div>
  </>;
}
function LoginPage({ dark, toggle, backend }) {
  const [register, setRegister] = useState(false);
  const navigate = useNavigate();
  return (
    <div className="login-page">
      <section className="login-story">
        <Brand />
        <div className="login-message">
          <p className="eyebrow">YOUR CAMPUS. YOUR PACE.</p>
          <h1>
            No more
            <br />
            standing in
            <br />
            <span className="serif">lines.</span>
          </h1>
          <p>
            Take a number. Skip the standing around.
            <br />
            There’s a whole campus out there.
          </p>
          <div
            className="login-floats"
            aria-label="Illustrative queue information"
          >
            <div className="floating-stat now-serving">
              <span className="eyebrow">YOUR PLACE, KEPT</span>
              <strong>A digital ticket</strong>
            </div>
            <div className="floating-stat estimated-wait">
              <span className="eyebrow">MORE TIME FOR</span>
              <strong>Your campus day</strong>
            </div>
          </div>
        </div>
        <span className="mono small">A BETTER WAY TO WAIT / CAMPUSQUEUE</span>
      </section>
      <section className="login-form-side">
        <div className="login-theme">
          <Theme dark={dark} toggle={toggle} />
        </div>
        <div className="auth-form">
          <p className="eyebrow">LET’S GET YOU GOING</p>
          <h2>{register ? "Find your place." : "Good to see you."}</h2>
          <p className="muted">
            {register
              ? "Create your campus student account."
              : "Step into a calmer campus day."}
          </p>
          <form
            className="form"
            onSubmit={async (e) => {
              e.preventDefault();
              const data = new FormData(e.currentTarget);
              try {
                await backend.action(register ? "/register" : "/login", {
                  studentName: data.get("name") || "",
                  email: data.get("email"), password: data.get("password"),
                });
                navigate("/dashboard");
              } catch {}
            }}
          >
            {register && (
              <label>
                Your name
                <input
                  name="name"
                  autoComplete="name"
                  placeholder="Full name"
                  required
                />
              </label>
            )}
            <label>
              College email
              <input
                name="email"
                type="email"
                autoComplete="email"
                placeholder="you@college.edu"
                required
              />
            </label>
            <label>
              Password
              <input
                name="password"
                type="password"
                autoComplete={register ? "new-password" : "current-password"}
                placeholder="Your password"
                required
                minLength={4}
              />
            </label>
            <button className="button primary" disabled={backend.busy}>{backend.busy ? "Please wait…" : register ? "Create account" : "Sign in"}
              <ArrowRight size={18} />
            </button>
          </form>
          <p className="auth-switch">
            {register ? "Already have a profile?" : "New around here?"}{" "}
            <button
              className="text-button"
              onClick={() => setRegister(!register)}
            >
              {register ? "Sign in" : "Get started"}
            </button>
          </p>
          <p className="demo-note">Sign in with your registered campus account.</p>
          {backend.error && <p className="danger" role="alert">{backend.error}</p>}
        </div>
        <span className="login-foot">Less wait. More campus.</span>
      </section>
    </div>
  );
}
function Experience() {
  const location = useLocation();
  const navigate = useNavigate();
  const root = useRef(null);
  const backend = useStudentBackend();
  const [selectedTicket, selectTicket] = useState(null);
  const profile = backend.profile;
  const ticket = backend.tickets.find((entry) => entry.queueId === selectedTicket) || backend.tickets[0] || null;
  const [dark, setDark] = useState(localStorage.getItem("theme") === "dark");

  useEffect(() => {
    document.documentElement.dataset.theme = dark ? "dark" : "light";
    localStorage.setItem("theme", dark ? "dark" : "light");
  }, [dark]);
  useEffect(() => {
    document.title = `${nav.find((n) => n[0] === location.pathname)?.[2] || "Welcome"} | CampusQueue`;
    if (matchMedia("(prefers-reduced-motion: reduce)").matches) return;
    const ctx = gsap.context(() => {
      const panels = root.current.querySelectorAll(
        ".page-heading, .overview-grid, .services-section, .queue-grid, .settings-grid, .auth-form, .login-message",
      );
      if (panels.length)
        gsap.fromTo(
          panels,
          { y: 18, opacity: 0 },
          {
            y: 0,
            opacity: 1,
            duration: 0.55,
            stagger: 0.07,
            ease: "power3.out",
          },
        );
      const routes = root.current.querySelectorAll(".map-route");
      if (routes.length)
        gsap.fromTo(
          routes,
          { strokeDasharray: 600, strokeDashoffset: 600 },
          { strokeDashoffset: 0, duration: 1.4, ease: "power2.out" },
        );
    }, root);
    return () => ctx.revert();
  }, [location.pathname]);
  async function create(service) {
    const current = backend.tickets.find((entry) => entry.serviceId === service.serviceId);
    if (current) { selectTicket(current.queueId); navigate("/queue"); return; }
    try {
      const result = await backend.action("/queue/join", { serviceId: service.serviceId });
      selectTicket(result.queueId); navigate("/queue");
    } catch {}
  }
  const toggle = () => setDark(!dark);
  if (!backend.ready) return <div ref={root} className="empty-state" role="status">Connecting to your campus…</div>;
  if (!profile || location.pathname === "/login")
    return (
      <div ref={root}>
        <LoginPage dark={dark} toggle={toggle} backend={backend} />
      </div>
    );
  return (
    <div ref={root} className="app-shell">
      <aside className="sidebar">
        <Brand />
        <div className="sidebar-label eyebrow">THE STUDENT SPACE</div>
        <nav aria-label="Main navigation">
          {nav.map(([path, Icon, label], i) => (
            <NavLink key={path} to={path}>
              <Icon size={19} />
              <span>{label}</span>
              <span className="nav-number">0{i + 1}</span>
            </NavLink>
          ))}
        </nav>
        <div className="sidebar-bottom">
          <div className="sidebar-note">
            <span className="asterisk">✳</span>
            <p>
              A whole campus.
              <br />
              <span className="serif">Beyond the queue.</span>
            </p>
          </div>
          <div className="student-profile">
            <span className="avatar">{profile.name[0]}</span>
            <span>
              <strong>{profile.name}</strong>
              <small>Student</small>
            </span>
          </div>
        </div>
      </aside>
      <div className="main-shell">
        <div className="topbar">
          <span className="topbar-brand">campusqueue</span>
          <span className="breadcrumb">
            STUDENT SPACE <span>/</span>{" "}
            {nav.find((n) => n[0] === location.pathname)?.[2]?.toUpperCase()}
          </span>
          <div className="topbar-right">
            <span className="mono date-label">
              {new Date().toLocaleDateString("en-IN", {
                day: "2-digit",
                month: "short",
                year: "numeric",
              })}
            </span>
            <Theme dark={dark} toggle={toggle} />
          </div>
        </div>
        <main>
          {backend.error && <div className="backend-error" role="alert">{backend.error}<button className="text-button" onClick={() => backend.refresh().catch(() => {})}>Try again</button></div>}
          <Routes>
            <Route
              path="/dashboard"
              element={
                <Overview
                  ticket={ticket}
                  profile={profile}
                  catalog={backend.catalog}
                  busy={backend.busy}
                  join={create}
                />
              }
            />
            <Route
              path="/queue"
              element={<QueuePage ticket={ticket} tickets={backend.tickets} selectTicket={selectTicket} refresh={backend.refresh} />}
            />
            <Route path="/bookings" element={<BookingsPage backend={backend} />} />
            <Route
              path="/settings"
              element={
                <SettingsPage
                  profile={profile}
                  busy={backend.busy}
                  logout={() => backend.action("/logout", {}).then(() => navigate("/login")).catch(() => {})}
                  dark={dark}
                  toggle={toggle}
                />
              }
            />
            <Route path="*" element={<Navigate to="/dashboard" replace />} />
          </Routes>
        </main>
      </div>
      <nav className="bottom-nav" aria-label="Mobile navigation">
        {nav.map(([path, Icon, label]) => (
          <NavLink key={path} to={path}>
            <Icon size={20} />
            <span>{label}</span>
          </NavLink>
        ))}
      </nav>
    </div>
  );
}
export default function App() {
  return (
    <HashRouter>
      <Experience />
    </HashRouter>
  );
}
