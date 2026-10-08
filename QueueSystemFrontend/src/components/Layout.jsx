// VIVA GUIDE: Retained earlier student implementation, outside the current main.jsx -> App.jsx import path. Reusable UI presentation component; check its imports/callers to establish whether the current App uses it.
import React from 'react';
import Sidebar from './Sidebar';
import BottomNav from './BottomNav';
import '../styles/premium-theme.css'; 

// Retained wrapper that imports premium-theme.css and older navigation components.
const Layout = ({ children }) => {
  return (
    <>
      <div className="dashboard-layout">
        <Sidebar />
        
        <main className="main-content">
          {children}
        </main>
      </div>

      <BottomNav />
    </>
  );
};

export default Layout;
