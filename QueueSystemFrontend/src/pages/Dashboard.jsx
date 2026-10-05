import React, { useState, useEffect } from 'react';
import { ChevronRight, Users, Coins, MapPin, Library, FileText, Clock } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import gsap from 'gsap';

// A mapping to get nice icons based on service names
const iconMap = {
  "Fee Payment": <Coins size={36} />,
  "Registration": <FileText size={36} />,
  "General Inquiry": <Users size={36} />,
  "Library Services": <Library size={36} />
};

const Dashboard = () => {
  const [services, setServices] = useState([]);
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();

  useEffect(() => {
    // Fetch services from our Tomcat backend via Vite proxy
    fetch('/api/services')
      .then(res => res.json())
      .then(data => {
        if (data.success && data.data && data.data.length > 0) {
          setServices(data.data);
        } else {
          // Fallback dummy data if db is empty
          setServices([
            { serviceId: 1, serviceName: 'Fee Payment', description: 'Pay tuition, hostel and other fees', averageServiceTime: 12 },
            { serviceId: 2, serviceName: 'Registration', description: 'Course and semester registration', averageServiceTime: 20 },
            { serviceId: 3, serviceName: 'Library Services', description: 'Book issue, return and fines', averageServiceTime: 5 },
            { serviceId: 4, serviceName: 'General Inquiry', description: 'Ask questions regarding administration', averageServiceTime: 8 }
          ]);
        }
        setLoading(false);
      })
      .catch(err => {
        console.error("Failed to fetch services:", err);
        // Fallback dummy data if db is empty
        setServices([
          { serviceId: 1, serviceName: 'Fee Payment', description: 'Pay tuition, hostel and other fees', averageServiceTime: 12 },
          { serviceId: 2, serviceName: 'Registration', description: 'Course and semester registration', averageServiceTime: 20 },
          { serviceId: 3, serviceName: 'Library Services', description: 'Book issue, return and fines', averageServiceTime: 5 },
          { serviceId: 4, serviceName: 'General Inquiry', description: 'Ask questions regarding administration', averageServiceTime: 8 }
        ]);
        setLoading(false);
      });
  }, []);

  useEffect(() => {
    if (!loading && services.length > 0) {
      gsap.fromTo('.service-card', 
        { y: 40, opacity: 0 },
        { y: 0, opacity: 1, duration: 0.6, stagger: 0.1, ease: 'back.out(1.2)' }
      );
    }
  }, [loading, services]);

  const handleServiceClick = (service) => {
    navigate(`/queue?serviceId=${service.serviceId}&name=${encodeURIComponent(service.serviceName)}`);
  };

  return (
    <div>
      <header className="header">
        <div className="header-greeting">
          <h1>Welcome, Student 👋</h1>
          <p>What service do you need today?</p>
        </div>
      </header>

      {loading ? (
        <div style={{ display: 'flex', justifyContent: 'center', padding: '3rem' }}>
          <p>Loading services...</p>
        </div>
      ) : (
        <div className="service-grid">
          {services.map((service) => (
            <div 
              key={service.serviceId} 
              className="service-card"
              onClick={() => handleServiceClick(service)}
            >
              <div className="service-icon">
                {iconMap[service.serviceName] || <MapPin size={36} />}
              </div>
              
              <div className="service-info">
                <h3>{service.serviceName}</h3>
                <p>{service.description}</p>
              </div>

              <div className="service-meta">
                <div className="wait-time">
                  <Clock size={20} strokeWidth={2.5} />
                  ~ {service.averageServiceTime} mins
                </div>
                <div style={{ color: 'var(--accent-orange)' }}>
                  <ChevronRight size={24} strokeWidth={2.5} />
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

export default Dashboard;
