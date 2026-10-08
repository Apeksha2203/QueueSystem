// VIVA GUIDE: Retained earlier student implementation, outside the current main.jsx -> App.jsx import path. Older student page retained alongside the current App.jsx component implementation. Check imports before treating it as an active production screen.
import React, { useEffect, useState } from 'react';
import { Users, Timer, XCircle } from 'lucide-react';
import { useNavigate, useLocation } from 'react-router-dom';
import gsap from 'gsap';

// Arrow-function helper Queue: keeps this operation reusable at its call sites.
const Queue = () => {
  const navigate = useNavigate();
  const location = useLocation();
  // Component state: the setter updates this value and triggers a render; the initial value is not server authorization.
  const [targetWaitTime] = useState(18);
  // Component state: the setter updates this value and triggers a render; the initial value is not server authorization.
  const [targetPeople] = useState(4);
  // Component state: the setter updates this value and triggers a render; the initial value is not server authorization.
  const [serviceName, setServiceName] = useState("Service Queue");

  // React side effect: inspect dependencies and cleanup to avoid stale requests, duplicate timers or leftover animations.
  useEffect(() => {
    const params = new URLSearchParams(location.search);
    if (params.get('name')) {
      setServiceName(params.get('name'));
    }

    gsap.fromTo(".gsap-zoom", { scale: 0.5, autoAlpha: 0 }, { scale: 1, autoAlpha: 1, duration: 1.2, ease: "elastic.out(1.2, 0.5)" });
    gsap.fromTo(".gsap-fade", { y: 30, autoAlpha: 0 }, { y: 0, autoAlpha: 1, duration: 0.8, stagger: 0.2, ease: "power2.out", delay: 0.3 });
    
    gsap.to(document.getElementById('waitTimeDisplay'), { innerHTML: targetWaitTime, duration: 2, snap: { innerHTML: 1 }, ease: "power1.out" });
    gsap.to(document.getElementById('peopleAheadDisplay'), { innerHTML: targetPeople, duration: 1.5, snap: { innerHTML: 1 }, ease: "power1.out" });
  }, [location.search, targetWaitTime, targetPeople]);

  // Arrow-function helper cancelQueue: keeps this operation reusable at its call sites.
  const cancelQueue = () => {
    if (window.confirm("Are you sure you want to cancel your queue token?")) {
      navigate('/dashboard');
    }
  };

  return (
    <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', width: '100%' }}>
      <div className="service-header gsap-fade" style={{ textAlign: 'center', marginBottom: '3rem', width: '100%' }}>
        <h1 className="text-gradient" style={{ fontSize: '2.5rem' }}>{serviceName}</h1>
        <p style={{ color: 'var(--text-secondary)', fontSize: '1.1rem' }}>Live Queue Tracking</p>
      </div>

      <div style={{ display: 'flex', gap: '3rem', width: '100%', maxWidth: '900px', justifyContent: 'center', alignItems: 'stretch', marginBottom: '3rem', flexWrap: 'wrap' }}>
        <div className="token-circle-wrapper gsap-zoom" style={{ position: 'relative', width: '320px', height: '320px', display: 'flex', justifyContent: 'center', alignItems: 'center', flexShrink: 0 }}>
          <div style={{ position: 'absolute', width: '100%', height: '100%', borderRadius: '50%', border: '4px solid var(--accent-orange)', animation: 'pulse 2s cubic-bezier(0.4, 0, 0.6, 1) infinite', zIndex: 1 }}></div>
          <div style={{ width: '280px', height: '280px', borderRadius: '50%', background: 'linear-gradient(135deg, rgba(255, 59, 48, 0.15), rgba(255, 149, 0, 0.15))', border: '4px solid var(--accent-orange)', backdropFilter: 'blur(20px)', display: 'flex', flexDirection: 'column', justifyContent: 'center', alignItems: 'center', zIndex: 2, boxShadow: '0 0 50px rgba(255, 149, 0, 0.3)' }}>
            <span style={{ fontSize: '1.1rem', color: 'var(--text-secondary)', textTransform: 'uppercase', letterSpacing: '2px', marginBottom: '0.5rem', fontWeight: 700 }}>Your Token</span>
            <span style={{ fontFamily: 'var(--font-heading)', fontSize: '4.5rem', fontWeight: 700, color: 'var(--text-primary)', lineHeight: 1 }}>EX-024</span>
          </div>
        </div>

        <div style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem', flex: 1, justifyContent: 'center', minWidth: '280px' }}>
          <div className="glass-panel gsap-fade" style={{ padding: '2.5rem', display: 'flex', flexDirection: 'column', justifyContent: 'center', borderRadius: 'var(--border-radius-xl)' }}>
            <div style={{ fontSize: '1rem', color: 'var(--text-secondary)', marginBottom: '0.5rem', display: 'flex', alignItems: 'center', gap: '0.5rem', fontWeight: 600 }}>
              <Users size={24} /> People Ahead
            </div>
            <div id="peopleAheadDisplay" style={{ fontFamily: 'var(--font-heading)', fontSize: '3rem', fontWeight: 700, color: 'var(--text-primary)' }}>0</div>
          </div>
          
          <div className="glass-panel gsap-fade" style={{ padding: '2.5rem', display: 'flex', flexDirection: 'column', justifyContent: 'center', borderRadius: 'var(--border-radius-xl)' }}>
            <div style={{ fontSize: '1rem', color: 'var(--text-secondary)', marginBottom: '0.5rem', display: 'flex', alignItems: 'center', gap: '0.5rem', fontWeight: 600 }}>
              <Timer size={24} /> Estimated Wait
            </div>
            <div style={{ fontFamily: 'var(--font-heading)', fontSize: '3rem', fontWeight: 700 }}>
              <span id="waitTimeDisplay" style={{ background: 'linear-gradient(135deg, var(--accent-red), var(--accent-orange))', WebkitBackgroundClip: 'text', WebkitTextFillColor: 'transparent' }}>0</span>
              <span style={{ fontSize: '1.25rem', color: 'var(--text-secondary)', marginLeft: '8px' }}>mins</span>
            </div>
          </div>
        </div>
      </div>

      <div className="gsap-fade" style={{ width: '100%', maxWidth: '900px', display: 'flex', justifyContent: 'center', gap: '1rem' }}>
        <button 
          onClick={cancelQueue}
          style={{ background: 'rgba(255, 59, 48, 0.1)', color: '#FF3B30', border: '2px solid rgba(255, 59, 48, 0.3)', padding: '16px 32px', borderRadius: '100px', fontFamily: 'var(--font-heading)', fontWeight: 700, fontSize: '1.1rem', cursor: 'pointer', transition: 'var(--transition-bouncy)', display: 'flex', alignItems: 'center', gap: '8px' }}
          onMouseOver={(e) => { e.currentTarget.style.background = 'rgba(255, 59, 48, 0.2)'; e.currentTarget.style.transform = 'translateY(-2px)'; }}
          onMouseOut={(e) => { e.currentTarget.style.background = 'rgba(255, 59, 48, 0.1)'; e.currentTarget.style.transform = 'translateY(0)'; }}
        >
          <XCircle size={24} /> Cancel Request
        </button>
      </div>

      <style>{`
        @keyframes pulse {
          0% { transform: scale(1); opacity: 0.8; }
          100% { transform: scale(1.3); opacity: 0; }
        }
      `}</style>
    </div>
  );
};

export default Queue;
