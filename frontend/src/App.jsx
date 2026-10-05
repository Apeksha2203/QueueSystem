import {
    BrowserRouter,
    Routes,
    Route,
    Navigate,
    useNavigate
} from "react-router-dom";

import { useState } from "react";

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


    function handleLogout() {
        onLogout();
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
                                onLogout={() => setStaff(null)}
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