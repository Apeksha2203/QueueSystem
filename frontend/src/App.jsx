// VIVA GUIDE: Staff BrowserRouter, restored login session, navigation and dashboard composition.
import {
    BrowserRouter,
    Routes,
    Route,
    Navigate,
    useNavigate
} from "react-router-dom";

import { useState, useEffect, useCallback } from "react";
import { api } from "./services/api";

import StaffLogin from "./pages/StaffLogin";
import StaffDashboard from "./pages/StaffDashboard";


// Wrap the staff login form and navigate to the dashboard after a successful server response.
function LoginPage({ onLogin }) {
    const navigate = useNavigate();

    // Store the authenticated staff profile and navigate to the protected dashboard.
    function handleLogin(staffData) {
        onLogin(staffData);
        navigate("/dashboard");
    }

    return (
        <StaffLogin onLogin={handleLogin} />
    );
}


// Pass the selected console section and staff profile into the shared staff dashboard.
function DashboardPage({ staff, onLogout }) {

    const navigate = useNavigate();

    // This controls which section of the staff dashboard is displayed
    // Selected staff-console section passed into StaffDashboard.
    const [page, setPage] = useState("dashboard");


    // Wait for server logout before navigating back to the staff login screen.
    async function handleLogout() {
        await onLogout();
        navigate("/");
    }


    return (
        <StaffDashboard
            staff={staff}
            onLogout={handleLogout}
            page={page}
            onNavigate={setPage}
        />
    );
}


// Mount the application-level router and screen composition; consult the imports to distinguish active components from retained pages.
function App() {

    // Staff profile restored from the backend session; service/counter identifiers come from the server.
    const [staff, setStaff] = useState(null);
    // Initial session lookup has finished; prevents rendering a login screen before restoration completes.
    const [ready,setReady]=useState(false);
    // User-facing request/form error; empty text means there is no current error banner.
    const [error,setError]=useState('');
    const restore=useCallback(()=>{setError('');api.profile().then(result=>setStaff(result.data)).catch(err=>{if(err.status!==401)setError(err.message);}).finally(()=>setReady(true));},[]);
    // React side effect: inspect dependencies and cleanup to avoid stale requests, duplicate timers or leftover animations.
    useEffect(()=>{restore();},[restore]);
    const logout=useCallback(async()=>{try{await api.logout();setStaff(null);}catch(err){setError(err.message);}},[]);
    if(!ready)return <div className="empty-state">Checking staff session…</div>;
    if(error)return <div className="error-banner" role="alert">{error}<button onClick={restore}>Retry</button></div>;


    return (
        <BrowserRouter>

            <Routes>

                <Route
                    path="/"
                    element={
                        <LoginPage
                            onLogin={setStaff}
                        />
                    }
                />

                <Route
                    path="/dashboard"
                    element={
                        staff ? (
                            <DashboardPage
                                staff={staff}
                                onLogout={logout}
                            />
                        ) : (
                            <Navigate
                                to="/"
                                replace
                            />
                        )
                    }
                />

            </Routes>

        </BrowserRouter>
    );
}


export default App;
