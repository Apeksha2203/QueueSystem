import React from 'react';
import Sidebar from './Sidebar';
import BottomNav from './BottomNav';
import '../styles/premium-theme.css'; 

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
