// VIVA GUIDE: Retained earlier student implementation, outside the current main.jsx -> App.jsx import path. Older student page retained alongside the current App.jsx component implementation. Check imports before treating it as an active production screen.
import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import gsap from 'gsap';
import { ArrowRight, Moon, Sun } from 'lucide-react';

// Arrow-function helper Login: keeps this operation reusable at its call sites.
const Login = () => {
  // Component state: the setter updates this value and triggers a render; the initial value is not server authorization.
  const [isLogin, setIsLogin] = useState(true);
  // Component state: the setter updates this value and triggers a render; the initial value is not server authorization.
  const [isDarkMode, setIsDarkMode] = useState(
    document.documentElement.getAttribute('data-theme') === 'dark' || localStorage.getItem('theme') === 'dark'
  );
  const navigate = useNavigate();

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
    // Initial mount animations
    gsap.fromTo(".gsap-hero h1", { y: 30, autoAlpha: 0 }, { y: 0, autoAlpha: 1, duration: 1, ease: "power3.out" });
    gsap.fromTo(".gsap-hero p", { y: 20, autoAlpha: 0 }, { y: 0, autoAlpha: 1, duration: 1, delay: 0.2, ease: "power3.out" });
    gsap.fromTo(".gsap-float", { y: 60, autoAlpha: 0 }, { y: 0, autoAlpha: 1, duration: 1.2, stagger: 0.2, ease: "back.out(1.5)" });
    gsap.fromTo(".gsap-auth", { x: 30, autoAlpha: 0 }, { x: 0, autoAlpha: 1, duration: 1, delay: 0.3, ease: "power2.out" });
  }, []);

  // When isLogin changes, animate the new form in
  // React side effect: inspect dependencies and cleanup to avoid stale requests, duplicate timers or leftover animations.
  useEffect(() => {
    const activeForm = isLogin ? ".login-form" : ".register-form";
    gsap.fromTo(activeForm, { opacity: 0, y: 10 }, { opacity: 1, y: 0, duration: 0.4, ease: "power2.out" });
  }, [isLogin]);

  // Arrow-function helper switchForm: keeps this operation reusable at its call sites.
  const switchForm = () => {
    const currentForm = isLogin ? ".login-form" : ".register-form";
    
    // Animate the current form out
    gsap.to(currentForm, {
      opacity: 0, y: -10, duration: 0.3,
      onComplete: () => {
        // Once out animation completes, swap the state
        // The useEffect above will handle animating the new form in
        setIsLogin(!isLogin);
      }
    });
  };

  // Arrow-function helper handleAuth: keeps this operation reusable at its call sites.
  const handleAuth = (e) => {
    e.preventDefault();
    navigate('/dashboard');
  };

  return (
    <div className="login-wrapper" style={{ display: 'flex', minHeight: '100vh', width: '100vw' }}>
      <button className="theme-toggle" onClick={toggleTheme} title="Toggle Theme" style={{ zIndex: 200 }}>
        {isDarkMode ? <Sun size={24} /> : <Moon size={24} />}
      </button>

      <div className="login-left" style={{ flex: 1, padding: '5rem', position: 'relative', display: 'flex', flexDirection: 'column', justifyContent: 'center' }}>
        <div className="hero-text gsap-hero">
          <h1 className="text-gradient" style={{ fontSize: '4.5rem', lineHeight: 1.05, marginBottom: '1.5rem', letterSpacing: '-0.04em' }}>
            No more<br />standing in<br />lines.
          </h1>
          <p style={{ fontSize: '1.15rem', color: 'var(--text-secondary)', maxWidth: '440px', lineHeight: 1.6, marginBottom: '2rem' }}>
            Join queues digitally, track your live position, and arrive exactly when it's your turn.
          </p>
          <button 
            className="mobile-scroll-btn"
            onClick={() => document.getElementById('auth-section').scrollIntoView({ behavior: 'smooth' })}
          >
            Get Started
          </button>
        </div>
        
        <div className="float-card glass-panel fc-1 gsap-float" style={{ position: 'absolute', right: '5%', top: '25%', transform: 'rotate(8deg)', padding: '1.5rem', width: '220px', opacity: 0 }}>
          <span className="token-label" style={{ fontSize: '0.75rem', color: 'var(--text-secondary)', textTransform: 'uppercase', letterSpacing: '1px' }}>Now Serving</span>
          <span className="token-badge" style={{ fontSize: '1.75rem', fontWeight: 500, display: 'block' }}>EXAM-23</span>
        </div>
        
        <div className="float-card glass-panel fc-2 gsap-float" style={{ position: 'absolute', left: '55%', bottom: '15%', transform: 'rotate(-6deg)', padding: '1.5rem', width: '220px', opacity: 0 }}>
          <span className="token-label" style={{ fontSize: '0.75rem', color: 'var(--text-secondary)', textTransform: 'uppercase', letterSpacing: '1px' }}>Estimated Wait</span>
          <span className="token-badge" style={{ fontSize: '1.75rem', fontWeight: 500, display: 'block' }}>12 mins</span>
        </div>
      </div>

      <div id="auth-section" className="login-right" style={{ flex: 1, padding: '2rem', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
        <div className="auth-card glass-panel gsap-auth" style={{ width: '100%', maxWidth: '440px', padding: '3rem', opacity: 0 }}>
          
          {isLogin ? (
            <div className="login-form">
              <div style={{ textAlign: 'center', marginBottom: '2.5rem' }}>
                <h2 style={{ fontSize: '2rem', marginBottom: '0.5rem' }}>Welcome back</h2>
                <p style={{ color: 'var(--text-secondary)' }}>Enter your details to access your dashboard.</p>
              </div>
              <form onSubmit={handleAuth}>
                <div style={{ marginBottom: '1.25rem' }}>
                  <label style={{ display: 'block', marginBottom: '0.5rem', fontSize: '0.9rem', color: 'var(--text-secondary)' }}>Email Address</label>
                  <input type="email" style={{ width: '100%', padding: '1rem', background: 'var(--glass-bg)', border: '1px solid var(--glass-border)', borderRadius: '12px', color: 'var(--text-primary)' }} required placeholder="student@college.edu" />
                </div>
                <div style={{ marginBottom: '1.5rem' }}>
                  <label style={{ display: 'block', marginBottom: '0.5rem', fontSize: '0.9rem', color: 'var(--text-secondary)' }}>Password</label>
                  <input type="password" style={{ width: '100%', padding: '1rem', background: 'var(--glass-bg)', border: '1px solid var(--glass-border)', borderRadius: '12px', color: 'var(--text-primary)' }} required placeholder="••••••••" />
                </div>
                <button type="submit" style={{ width: '100%', padding: '1rem', background: 'var(--accent-orange)', border: 'none', borderRadius: '12px', color: '#fff', fontSize: '1rem', fontWeight: 600, display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '0.5rem', cursor: 'pointer' }}>
                  Sign In <ArrowRight size={20} />
                </button>
              </form>
              <div style={{ textAlign: 'center', marginTop: '2rem', fontSize: '0.85rem', color: 'var(--text-secondary)' }}>
                Don't have an account? <span onClick={switchForm} style={{ color: 'var(--text-primary)', fontWeight: 600, cursor: 'pointer' }}>Sign up</span>
              </div>
            </div>
          ) : (
            <div className="register-form">
              <div style={{ textAlign: 'center', marginBottom: '2.5rem' }}>
                <h2 style={{ fontSize: '2rem', marginBottom: '0.5rem' }}>Create Account</h2>
                <p style={{ color: 'var(--text-secondary)' }}>Join the digital queue system today.</p>
              </div>
              <form onSubmit={handleAuth}>
                <div style={{ marginBottom: '1.25rem' }}>
                  <label style={{ display: 'block', marginBottom: '0.5rem', fontSize: '0.9rem', color: 'var(--text-secondary)' }}>Full Name</label>
                  <input type="text" style={{ width: '100%', padding: '1rem', background: 'var(--glass-bg)', border: '1px solid var(--glass-border)', borderRadius: '12px', color: 'var(--text-primary)' }} required placeholder="John Doe" />
                </div>
                <div style={{ marginBottom: '1.25rem' }}>
                  <label style={{ display: 'block', marginBottom: '0.5rem', fontSize: '0.9rem', color: 'var(--text-secondary)' }}>Email Address</label>
                  <input type="email" style={{ width: '100%', padding: '1rem', background: 'var(--glass-bg)', border: '1px solid var(--glass-border)', borderRadius: '12px', color: 'var(--text-primary)' }} required placeholder="student@college.edu" />
                </div>
                <div style={{ marginBottom: '1.5rem' }}>
                  <label style={{ display: 'block', marginBottom: '0.5rem', fontSize: '0.9rem', color: 'var(--text-secondary)' }}>Password</label>
                  <input type="password" style={{ width: '100%', padding: '1rem', background: 'var(--glass-bg)', border: '1px solid var(--glass-border)', borderRadius: '12px', color: 'var(--text-primary)' }} required placeholder="••••••••" />
                </div>
                <button type="submit" style={{ width: '100%', padding: '1rem', background: 'var(--accent-orange)', border: 'none', borderRadius: '12px', color: '#fff', fontSize: '1rem', fontWeight: 600, display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '0.5rem', cursor: 'pointer' }}>
                  Create Account <ArrowRight size={20} />
                </button>
              </form>
              <div style={{ textAlign: 'center', marginTop: '2rem', fontSize: '0.85rem', color: 'var(--text-secondary)' }}>
                Already have an account? <span onClick={switchForm} style={{ color: 'var(--text-primary)', fontWeight: 600, cursor: 'pointer' }}>Sign in</span>
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default Login;
