// VIVA GUIDE: Browser entry point: mounts the React app into the root element and imports styles. React StrictMode helps reveal development side-effect problems.
import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import './index.css'
import App from './App.jsx'

createRoot(document.getElementById('root')).render(
  <StrictMode>
    <App />
  </StrictMode>,
)
