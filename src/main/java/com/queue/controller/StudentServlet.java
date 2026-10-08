// VIVA GUIDE: Older student HTTP surface. Compare with current StudentPortalServlet before claiming this is the active UI route.
package com.queue.controller;

import com.queue.model.Student;
import com.queue.service.StudentService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/api/students/*")
public class StudentServlet extends HttpServlet {

    private StudentService studentService;

    @Override
    // Initialize servlet dependencies once when Tomcat creates the servlet.
    public void init() {
        studentService = new StudentService();
    }

    @Override
    // Handle HTTP POST requests for this servlet mapping; validate input/session before returning HTML or JSON.
    protected void doPost(HttpServletRequest request,
                           HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String path = request.getPathInfo();

        // POST /api/students/register
        if ("/register".equals(path)) {

            String studentName =
                    request.getParameter("studentName");

            String email =
                    request.getParameter("email");

            String phone =
                    request.getParameter("phone");

            String password =
                    request.getParameter("password");

            if (studentName == null ||
                email == null ||
                password == null) {

                writeError(
                    response,
                    "studentName, email and password are required"
                );

                return;
            }

            Student student = new Student();

            student.setStudentName(studentName);
            student.setEmail(email);
            student.setPhone(phone);
            student.setPassword(password);

            boolean success =
                    studentService.registerStudent(student);

            if (success) {

                response.getWriter().write(
                    "{"
                    + "\"success\":true,"
                    + "\"message\":\"Student registered successfully\""
                    + "}"
                );

            } else {

                writeError(
                    response,
                    "Unable to register student. Email may already exist."
                );
            }

            return;
        }

        // POST /api/students/login
        if ("/login".equals(path)) {

            String email =
                    request.getParameter("email");

            String password =
                    request.getParameter("password");

            if (email == null ||
                password == null) {

                writeError(
                    response,
                    "email and password are required"
                );

                return;
            }

            Student student =
                    studentService.loginStudent(
                        email,
                        password
                    );

            if (student != null) {

                response.getWriter().write(
                    "{"
                    + "\"success\":true,"
                    + "\"message\":\"Student login successful\","
                    + "\"data\":{"
                    + "\"studentId\":" + student.getStudentId() + ","
                    + "\"studentName\":\"" + student.getStudentName() + "\","
                    + "\"email\":\"" + student.getEmail() + "\""
                    + "}"
                    + "}"
                );

            } else {

                writeError(
                    response,
                    "Invalid email or password"
                );
            }

            return;
        }

        writeError(
            response,
            "POST operation not available for this endpoint"
        );
    }

    // Operation writeError: follow the inputs, validation and return value in the block below; the file header explains its layer/caller.
    private void writeError(HttpServletResponse response,
                            String message)
            throws IOException {

        response.getWriter().write(
            "{"
            + "\"success\":false,"
            + "\"message\":\"" + message + "\""
            + "}"
        );
    }
}
