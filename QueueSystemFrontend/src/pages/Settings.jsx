// VIVA GUIDE: Retained earlier student implementation, outside the current main.jsx -> App.jsx import path. Older student page retained alongside the current App.jsx component implementation. Check imports before treating it as an active production screen.
import React, { useEffect, useState } from 'react';
import { User, Bell, LogOut, Moon } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import gsap from 'gsap';

// Arrow-function helper Settings: keeps this operation reusable at its call sites.
const Settings = () => {
  const navigate = useNavigate();
  // Component state: the setter updates this value and triggers a render; the initial value is not server authorization.
  const [isDarkMode, setIsDarkMode] = useState(
    document.documentElement.getAttribute('data-theme') === 'dark' || localStorage.getItem('theme') === 'dark'
  );

  // Arrow-function helper toggleTheme: keeps this operation reusable at its call sites.
  const toggleTheme = () => {
    const nextMode = !isDarkMode;
    setIsDarkMode(nextMode);
    if (nextMode) {
      document.documentElement.setAttribute('data-theme', 'dark');
      localStorage.setItem('theme', 'dark');
    } else {
      document.documentElement.setAttribute('data-theme', 'light');
      localStorage.setItem('theme', 'light');
    }
  };

  // React side effect: inspect dependencies and cleanup to avoid stale requests, duplicate timers or leftover animations.
  useEffect(() => {
    gsap.fromTo(".gsap-stagger", { y: 20, autoAlpha: 0 }, { y: 0, autoAlpha: 1, duration: 0.8, stagger: 0.1, ease: "power2.out" });
    gsap.fromTo(".gsap-card", { y: 40, autoAlpha: 0 }, { y: 0, autoAlpha: 1, duration: 0.8, stagger: 0.15, ease: "back.out(1.2)", delay: 0.2 });
  }, []);

  return (
    <div style={{ display: 'flex', flexDirection: 'column', width: '100%' }}>
      <header className="header gsap-stagger" style={{ marginBottom: '3rem' }}>
        <h1 style={{ fontSize: '2.75rem', marginBottom: '0.5rem' }}>Account <span className="text-gradient">Settings</span></h1>
        <p style={{ color: 'var(--text-secondary)', fontSize: '1.1rem' }}>Manage your profile, preferences, and notifications.</p>
      </header>

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(320px, 1fr))', gap: '2rem' }}>
        
        <div className="glass-panel gsap-card" style={{ padding: '2.5rem', display: 'flex', flexDirection: 'column', gap: '2rem', border: '2px solid var(--glass-border)', borderRadius: 'var(--border-radius-xl)' }}>
          <h2 style={{ fontSize: '1.5rem', color: 'var(--text-primary)', fontFamily: 'var(--font-heading)', display: 'flex', alignItems: 'center', gap: '0.75rem', borderBottom: '2px solid var(--glass-border)', paddingBottom: '1rem' }}>
            <User size={24} /> Personal Profile
          </h2>
          
          <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
            <label style={{ fontWeight: 600, color: 'var(--text-secondary)', fontSize: '0.95rem' }}>Full Name</label>
            <input type="text" defaultValue="Student User" placeholder="Your Name" style={{ padding: '1rem 1.25rem', borderRadius: '12px', border: '2px solid var(--glass-border)', background: 'rgba(255, 255, 255, 0.05)', color: 'var(--text-primary)', fontFamily: 'inherit', fontSize: '1rem', transition: 'var(--transition-bouncy)' }} />
          </div>
          
          <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
            <label style={{ fontWeight: 600, color: 'var(--text-secondary)', fontSize: '0.95rem' }}>Email Address</label>
            <input type="email" defaultValue="student@college.edu" placeholder="you@college.edu" style={{ padding: '1rem 1.25rem', borderRadius: '12px', border: '2px solid var(--glass-border)', background: 'rgba(255, 255, 255, 0.05)', color: 'var(--text-primary)', fontFamily: 'inherit', fontSize: '1rem', transition: 'var(--transition-bouncy)' }} />
          </div>
          
          <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
            <label style={{ fontWeight: 600, color: 'var(--text-secondary)', fontSize: '0.95rem' }}>Student ID / Enrollment No.</label>
            <input type="text" defaultValue="CS2026-001" readOnly style={{ padding: '1rem 1.25rem', borderRadius: '12px', border: '2px solid var(--glass-border)', background: 'rgba(255, 255, 255, 0.05)', color: 'var(--text-primary)', fontFamily: 'inherit', fontSize: '1rem', opacity: 0.7, cursor: 'not-allowed' }} />
          </div>

          <button style={{ background: 'linear-gradient(135deg, var(--accent-red), var(--accent-orange))', color: 'white', border: 'none', padding: '1rem', borderRadius: '12px', fontWeight: 600, cursor: 'pointer', transition: 'var(--transition-bouncy)', boxShadow: '0 10px 20px rgba(255, 59, 48, 0.3)', fontSize: '1.1rem', marginTop: '1rem' }}
            onMouseOver={(e) => { e.currentTarget.style.transform = 'translateY(-3px)'; e.currentTarget.style.boxShadow = '0 15px 25px rgba(255, 59, 48, 0.4)'; }}
            onMouseOut={(e) => { e.currentTarget.style.transform = 'translateY(0)'; e.currentTarget.style.boxShadow = '0 10px 20px rgba(255, 59, 48, 0.3)'; }}
          >
            Save Changes
          </button>
        </div>

        <div className="glass-panel gsap-card" style={{ padding: '2.5rem', display: 'flex', flexDirection: 'column', gap: '2rem', border: '2px solid var(--glass-border)', borderRadius: 'var(--border-radius-xl)' }}>
          <h2 style={{ fontSize: '1.5rem', color: 'var(--text-primary)', fontFamily: 'var(--font-heading)', display: 'flex', alignItems: 'center', gap: '0.75rem', borderBottom: '2px solid var(--glass-border)', paddingBottom: '1rem' }}>
            <Bell size={24} /> Preferences
          </h2>
          
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <div>
              <h3 style={{ fontSize: '1.1rem' }}>Push Notifications</h3>
              <p style={{ color: 'var(--text-secondary)', fontSize: '0.9rem', marginTop: '4px' }}>Get alerted when your turn is near.</p>
            </div>
            <label className="switch">
              <input type="checkbox" defaultChecked />
              <span className="slider"></span>
            </label>
          </div>

          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginTop: '1rem' }}>
            <div>
              <h3 style={{ fontSize: '1.1rem' }}>Email Alerts</h3>
              <p style={{ color: 'var(--text-secondary)', fontSize: '0.9rem', marginTop: '4px' }}>Receive booking confirmations via email.</p>
            </div>
            <label className="switch">
              <input type="checkbox" defaultChecked />
              <span className="slider"></span>
            </label>
          </div>

          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginTop: '1rem', paddingBottom: '1.5rem', borderBottom: '1px solid var(--glass-border)' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
              <div>
                <h3 style={{ fontSize: '1.1rem', display: 'flex', alignItems: 'center', gap: '6px' }}><Moon size={18} /> Dark Theme</h3>
                <p style={{ color: 'var(--text-secondary)', fontSize: '0.9rem', marginTop: '4px' }}>Toggle between light and dark modes.</p>
              </div>
            </div>
            <label className="switch">
              <input type="checkbox" checked={isDarkMode} onChange={toggleTheme} />
              <span className="slider"></span>
            </label>
          </div>
          
          <button 
            onClick={() => navigate('/login')}
            style={{ background: 'rgba(255, 59, 48, 0.1)', color: 'var(--accent-red)', border: '2px solid rgba(255, 59, 48, 0.3)', padding: '1rem', borderRadius: '12px', fontWeight: 600, cursor: 'pointer', transition: 'var(--transition-bouncy)', display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '0.5rem', width: '100%', marginTop: 'auto' }}
            onMouseOver={(e) => { e.currentTarget.style.background = 'var(--accent-red)'; e.currentTarget.style.color = 'white'; }}
            onMouseOut={(e) => { e.currentTarget.style.background = 'rgba(255, 59, 48, 0.1)'; e.currentTarget.style.color = 'var(--accent-red)'; }}
          >
            <LogOut size={24} /> Sign Out Securely
          </button>
        </div>
      </div>
      <style>{`
        .switch { position: relative; display: inline-block; width: 50px; height: 28px; }
        .switch input { opacity: 0; width: 0; height: 0; }
        .slider { position: absolute; cursor: pointer; top: 0; left: 0; right: 0; bottom: 0; background-color: var(--glass-border); transition: .4s; border-radius: 34px; }
        .slider:before { position: absolute; content: ""; height: 20px; width: 20px; left: 4px; bottom: 4px; background-color: white; transition: .4s; border-radius: 50%; }
        input:checked + .slider { background: linear-gradient(135deg, var(--accent-red), var(--accent-orange)); }
        input:checked + .slider:before { transform: translateX(22px); }
      `}</style>
    </div>
  );
};

export default Settings;
