import React, { useEffect, useState } from 'react';
import { Plus, CalendarDays, Clock } from 'lucide-react';
import gsap from 'gsap';

const Bookings = () => {
  const [activeTab, setActiveTab] = useState('Upcoming');

  useEffect(() => {
    gsap.fromTo(".gsap-stagger", { y: 20, autoAlpha: 0 }, { y: 0, autoAlpha: 1, duration: 0.8, stagger: 0.1, ease: "power2.out" });
    gsap.fromTo(".gsap-card", { y: 40, autoAlpha: 0 }, { y: 0, autoAlpha: 1, duration: 0.8, stagger: 0.15, ease: "back.out(1.2)", delay: 0.2 });
  }, [activeTab]);

  return (
    <div style={{ display: 'flex', flexDirection: 'column', width: '100%' }}>
      <header className="header gsap-stagger" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '2rem', flexWrap: 'wrap', gap: '1.5rem' }}>
        <div className="header-greeting">
          <h1 style={{ fontSize: '2.75rem', marginBottom: '0.5rem' }}>My <span className="text-gradient">Bookings</span></h1>
          <p style={{ color: 'var(--text-secondary)', fontSize: '1.1rem' }}>Manage your upcoming appointments and history.</p>
        </div>
        <button style={{ background: 'linear-gradient(135deg, var(--accent-red), var(--accent-orange))', color: 'white', border: 'none', padding: '1rem 2rem', borderRadius: '100px', fontWeight: 600, cursor: 'pointer', display: 'flex', alignItems: 'center', gap: '0.5rem', boxShadow: '0 10px 20px rgba(255, 59, 48, 0.3)', transition: 'var(--transition-bouncy)' }}
          onMouseOver={(e) => { e.currentTarget.style.transform = 'translateY(-3px)'; e.currentTarget.style.boxShadow = '0 15px 25px rgba(255, 59, 48, 0.4)'; }}
          onMouseOut={(e) => { e.currentTarget.style.transform = 'translateY(0)'; e.currentTarget.style.boxShadow = '0 10px 20px rgba(255, 59, 48, 0.3)'; }}
        >
          <Plus size={24} /> New Booking
        </button>
      </header>

      <div className="tabs gsap-stagger" style={{ display: 'flex', gap: '1rem', marginBottom: '2rem', borderBottom: '2px solid var(--glass-border)', paddingBottom: '1rem' }}>
        {['Upcoming', 'Past', 'Cancelled'].map(tab => (
          <div 
            key={tab}
            onClick={() => setActiveTab(tab)}
            style={{ 
              padding: '0.75rem 1.5rem', borderRadius: '100px', cursor: 'pointer', fontWeight: 600, transition: '0.3s',
              background: activeTab === tab ? 'var(--glass-bg)' : 'transparent',
              color: activeTab === tab ? 'var(--accent-orange)' : 'var(--text-secondary)',
              boxShadow: activeTab === tab ? 'var(--glass-shadow)' : 'none'
            }}
          >
            {tab}
          </div>
        ))}
      </div>

      <div className="booking-list" style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
        <div className="booking-card glass-panel gsap-card" style={{ padding: '2rem', display: 'flex', justifyContent: 'space-between', alignItems: 'center', border: '2px solid var(--glass-border)', borderRadius: 'var(--border-radius-lg)', position: 'relative', overflow: 'hidden', flexWrap: 'wrap', gap: '1.5rem' }}>
          <div style={{ position: 'absolute', left: 0, top: 0, bottom: 0, width: '6px', background: 'linear-gradient(to bottom, var(--accent-red), var(--accent-orange))', borderRadius: '6px 0 0 6px' }}></div>
          
          <div className="booking-details">
            <h3 style={{ fontSize: '1.25rem', marginBottom: '0.25rem', color: 'var(--text-primary)' }}>Library Office - Clearance</h3>
            <p style={{ marginTop: '8px', color: 'var(--text-secondary)', fontSize: '0.95rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}><CalendarDays size={16} /> Tomorrow, Oct 6</p>
            <p style={{ color: 'var(--text-secondary)', fontSize: '0.95rem', display: 'flex', alignItems: 'center', gap: '0.5rem', marginTop: '4px' }}><Clock size={16} /> 10:30 AM - 11:00 AM</p>
          </div>
          <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'flex-end', gap: '1rem', flex: '1 1 auto' }}>
            <span style={{ padding: '0.5rem 1rem', borderRadius: '100px', fontWeight: 600, fontSize: '0.85rem', background: 'rgba(52, 199, 89, 0.1)', color: '#34c759' }}>Confirmed</span>
            <div style={{ display: 'flex', gap: '1rem' }}>
              <button style={{ background: 'transparent', border: '2px solid var(--glass-border)', color: 'var(--text-primary)', padding: '0.75rem 1.5rem', borderRadius: '100px', fontWeight: 600, cursor: 'pointer' }}>Reschedule</button>
              <button style={{ background: 'transparent', border: '2px solid rgba(255,59,48,0.3)', color: 'var(--accent-red)', padding: '0.75rem 1.5rem', borderRadius: '100px', fontWeight: 600, cursor: 'pointer' }}>Cancel</button>
            </div>
          </div>
        </div>

        <div className="booking-card glass-panel gsap-card" style={{ padding: '2rem', display: 'flex', justifyContent: 'space-between', alignItems: 'center', border: '2px solid var(--glass-border)', borderRadius: 'var(--border-radius-lg)', position: 'relative', overflow: 'hidden', flexWrap: 'wrap', gap: '1.5rem' }}>
          <div style={{ position: 'absolute', left: 0, top: 0, bottom: 0, width: '6px', background: 'linear-gradient(to bottom, var(--accent-red), var(--accent-orange))', borderRadius: '6px 0 0 6px' }}></div>
          
          <div className="booking-details">
            <h3 style={{ fontSize: '1.25rem', marginBottom: '0.25rem', color: 'var(--text-primary)' }}>Professor Smith - Office Hours</h3>
            <p style={{ marginTop: '8px', color: 'var(--text-secondary)', fontSize: '0.95rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}><CalendarDays size={16} /> Friday, Oct 9</p>
            <p style={{ color: 'var(--text-secondary)', fontSize: '0.95rem', display: 'flex', alignItems: 'center', gap: '0.5rem', marginTop: '4px' }}><Clock size={16} /> 02:15 PM - 02:45 PM</p>
          </div>
          <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'flex-end', gap: '1rem', flex: '1 1 auto' }}>
            <span style={{ padding: '0.5rem 1rem', borderRadius: '100px', fontWeight: 600, fontSize: '0.85rem', background: 'rgba(255, 149, 0, 0.1)', color: 'var(--accent-orange)' }}>Pending Approval</span>
            <div style={{ display: 'flex', gap: '1rem' }}>
              <button style={{ background: 'transparent', border: '2px solid rgba(255,59,48,0.3)', color: 'var(--accent-red)', padding: '0.75rem 1.5rem', borderRadius: '100px', fontWeight: 600, cursor: 'pointer' }}>Cancel</button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Bookings;
