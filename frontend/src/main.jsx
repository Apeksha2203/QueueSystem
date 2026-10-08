// VIVA GUIDE: Browser entry point: mounts the React app into the root element and imports styles. React StrictMode helps reveal development side-effect problems.
import React from 'react'
import { createRoot } from 'react-dom/client'
import App from './App'
import './style.css'
createRoot(document.getElementById('root')).render(<App />)
