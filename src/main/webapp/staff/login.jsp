<%-- VIVA GUIDE: Retained server-rendered JSP page. Tomcat compiles JSP into a servlet; the current primary student/staff dashboards use React instead. --%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<!DOCTYPE html>
<html lang="en">

<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">

    <title>Staff Login - Campus Queue</title>

    <link rel="stylesheet" href="../css/staff.css">
</head>

<body class="login-page">

<div class="login-container">

    <div class="login-card">

        <div class="login-logo">CQ</div>

        <h1>Campus Queue</h1>

        <p class="login-subtitle">
            Staff Management Portal
        </p>

        <form id="loginForm">

            <div class="form-group">
                <label for="email">Staff Email</label>

                <input
                    type="email"
                    id="email"
                    name="email"
                    placeholder="Enter your staff email"
                    autocomplete="username"
                    required
                >
            </div>

            <div class="form-group">

                <label for="password">Password</label>

                <div class="password-wrapper">

                    <input
                        type="password"
                        id="password"
                        name="password"
                        placeholder="Enter your staff password"
                        autocomplete="current-password"
                        required
                    >

                    <button
                        type="button"
                        id="togglePassword"
                        class="password-toggle"
                    >
                        Show
                    </button>

                </div>

            </div>

            <div
                id="loginMessage"
                class="login-message"
                aria-live="polite"
            ></div>

            <button
                type="submit"
                id="loginButton"
                class="login-btn"
            >
                Login
            </button>

        </form>

        <p class="login-footer">
            Campus Queue Management System
        </p>

    </div>

</div>

<script src="login.js"></script>

</body>
</html>
