import React from 'react';
import { LayoutGrid, Clock, CalendarDays, Settings } from 'lucide-react';
import { useLocation, Link } from 'react-router-dom';

const Sidebar = ({ isDarkMode, toggleTheme }) => {
  const location = useLocation();
  const path = location.pathname;

  const getNavClass = (match) => {
    return `nav-item ${path === match ? 'active' : ''}`;
  };

  return (
    <aside className="sidebar">
      <div className="sidebar-brand text-gradient">CampusQueue</div>
      <nav style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem', marginTop: '2rem', height: '100%' }}>
        <Link to="/dashboard" className={getNavClass('/dashboard')}>
          <LayoutGrid size={24} strokeWidth={path === '/dashboard' ? 2.5 : 2} /> Dashboard
        </Link>
        <Link to="/queue" className={getNavClass('/queue')}>
          <Clock size={24} strokeWidth={path === '/queue' ? 2.5 : 2} /> Active Queue
        </Link>
        <Link to="/bookings" className={getNavClass('/bookings')}>
          <CalendarDays size={24} strokeWidth={path === '/bookings' ? 2.5 : 2} /> Bookings
        </Link>
        <Link to="/settings" className={getNavClass('/settings')} style={{ marginTop: 'auto' }}>
          <Settings size={24} strokeWidth={path === '/settings' ? 2.5 : 2} /> Settings
        </Link>
      </nav>
    </aside>
  );
};

export default Sidebar;
