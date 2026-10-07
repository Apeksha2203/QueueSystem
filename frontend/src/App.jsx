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


function LoginPage({ onLogin }) {
    const navigate = useNavigate();

    function handleLogin(staffData) {
        onLogin(staffData);
        navigate("/dashboard");
    }

    return (
        <StaffLogin onLogin={handleLogin} />
    );
}


function DashboardPage({ staff, onLogout }) {

    const navigate = useNavigate();

    // This controls which section of the staff dashboard is displayed
    const [page, setPage] = useState("dashboard");


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


function App() {

    const [staff, setStaff] = useState(null);
    const [ready,setReady]=useState(false);
    const [error,setError]=useState('');
    const restore=useCallback(()=>{setError('');api.profile().then(result=>setStaff(result.data)).catch(err=>{if(err.status!==401)setError(err.message);}).finally(()=>setReady(true));},[]);
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
