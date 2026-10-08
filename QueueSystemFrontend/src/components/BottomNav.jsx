// VIVA GUIDE: Retained earlier student implementation, outside the current main.jsx -> App.jsx import path. Reusable UI presentation component; check its imports/callers to establish whether the current App uses it.
import React from 'react';
import { LayoutGrid, Clock, CalendarDays, Settings } from 'lucide-react';
import { useLocation, Link } from 'react-router-dom';

// Retained student mobile navigation; current App.jsx defines its active mobile navigation inline.
const BottomNav = () => {
  const location = useLocation();
  const path = location.pathname;

  // Arrow-function helper getNavClass: keeps this operation reusable at its call sites.
  const getNavClass = (match) => {
    return `bottom-nav-item ${path === match ? 'active' : ''}`;
  };

  return (
    <nav className="bottom-nav">
      <Link to="/dashboard" className={getNavClass('/dashboard')}>
        <LayoutGrid size={24} strokeWidth={path === '/dashboard' ? 2.5 : 2} />
        <span>Home</span>
      </Link>
      <Link to="/queue" className={getNavClass('/queue')}>
        <Clock size={24} strokeWidth={path === '/queue' ? 2.5 : 2} />
        <span>Queue</span>
      </Link>
      <Link to="/bookings" className={getNavClass('/bookings')}>
        <CalendarDays size={24} strokeWidth={path === '/bookings' ? 2.5 : 2} />
        <span>Bookings</span>
      </Link>
      <Link to="/settings" className={getNavClass('/settings')}>
        <Settings size={24} strokeWidth={path === '/settings' ? 2.5 : 2} />
        <span>Settings</span>
      </Link>
    </nav>
  );
};

export default BottomNav;
