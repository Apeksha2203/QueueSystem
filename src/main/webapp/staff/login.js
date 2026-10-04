const loginForm = document.getElementById("loginForm");
const loginMessage = document.getElementById("loginMessage");
const passwordInput = document.getElementById("password");
const togglePassword = document.getElementById("togglePassword");

function showMessage(message, type) {
    loginMessage.textContent = message;
    loginMessage.className = "login-message " + type;
}

loginForm.addEventListener("submit", async function (event) {

    event.preventDefault();

    const email =
        document.getElementById("email").value.trim();

    const password =
        passwordInput.value;

    if (!email || !password) {
        showMessage(
            "Please enter your email and password.",
            "error"
        );
        return;
    }

    const loginButton =
        loginForm.querySelector(".login-btn");

    loginButton.disabled = true;
    loginButton.textContent = "Logging in...";

    showMessage("", "");

    try {

        const formData =
            new URLSearchParams();

        formData.append("email", email);
        formData.append("password", password);

        const response = await fetch(
            "../api/staff/login",
            {
                method: "POST",

                headers: {
                    "Content-Type":
                        "application/x-www-form-urlencoded;charset=UTF-8",
                    "Accept":
                        "application/json"
                },

                body: formData.toString(),

                credentials: "same-origin",

                cache: "no-store"
            }
        );

        const text =
            await response.text();

        console.log(
            "Login HTTP status:",
            response.status
        );

        console.log(
            "Login response:",
            text
        );

        let result;

        try {
            result = JSON.parse(text);
        } catch (error) {

            throw new Error(
                "Server returned an invalid login response."
            );
        }

        if (!response.ok) {

            throw new Error(
                result.message ||
                "Login request failed."
            );
        }

        if (!result.success) {

            showMessage(
                result.message ||
                "Invalid email or password.",
                "error"
            );

            return;
        }

        /*
         * Only redirect after the backend confirms
         * successful authentication.
         */

        showMessage(
            "Login successful. Opening dashboard...",
            "success"
        );

        window.location.replace(
            "dashboard.jsp"
        );

    } catch (error) {

        console.error(
            "Login error:",
            error
        );

        showMessage(
            error.message ||
            "Unable to connect to the server.",
            "error"
        );

    } finally {

        loginButton.disabled = false;
        loginButton.textContent = "Login";
    }
});


if (togglePassword) {

    togglePassword.addEventListener(
        "click",
        function () {

            const isPassword =
                passwordInput.type === "password";

            passwordInput.type =
                isPassword
                    ? "text"
                    : "password";

            togglePassword.textContent =
                isPassword
                    ? "Hide"
                    : "Show";
        }
    );
}