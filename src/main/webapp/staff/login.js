// VIVA GUIDE: Retained JSP staff-login browser helper; distinguish it from frontend/src/pages/StaffLogin.jsx.
const loginForm = document.getElementById("loginForm");
const loginButton = document.getElementById("loginButton");
const loginMessage = document.getElementById("loginMessage");
const passwordInput = document.getElementById("password");
const togglePassword = document.getElementById("togglePassword");


// Show / hide password
togglePassword.addEventListener("click", function () {

    if (passwordInput.type === "password") {
        passwordInput.type = "text";
        togglePassword.textContent = "Hide";
    } else {
        passwordInput.type = "password";
        togglePassword.textContent = "Show";
    }

});


// Login
loginForm.addEventListener("submit", async function (event) {

    event.preventDefault();

    const email = document.getElementById("email").value.trim();
    const password = passwordInput.value;

    loginMessage.textContent = "";
    loginButton.disabled = true;
    loginButton.textContent = "Logging in...";

    try {

        const response = await fetch("../api/staff/login", {
            method: "POST",

            headers: {
                "Content-Type": "application/x-www-form-urlencoded"
            },

            credentials: "same-origin",

            body: new URLSearchParams({
                email: email,
                password: password
            })
        });

        const result = await response.json();

        console.log("Login response:", result);

        if (result.success) {

            loginMessage.textContent = "Login successful.";

            window.location.href = "dashboard.jsp";

        } else {

            loginMessage.textContent =
                result.message || "Invalid email or password.";

            loginButton.disabled = false;
            loginButton.textContent = "Login";
        }

    } catch (error) {

        console.error("Login error:", error);

        loginMessage.textContent =
            "Unable to connect to the server.";

        loginButton.disabled = false;
        loginButton.textContent = "Login";
    }

});
