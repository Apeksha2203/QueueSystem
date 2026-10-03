package com.queue.controller;

import com.queue.service.AnalyticsService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

@WebServlet(urlPatterns = {"/analytics", "/api/analytics"})
public class AnalyticsServlet extends HttpServlet {

    private final AnalyticsService analyticsService = new AnalyticsService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Map<String, Object> data = analyticsService.getAnalyticsSummary();

        String format = request.getParameter("format");
        String acceptHeader = request.getHeader("Accept");
        String requestedWith = request.getHeader("X-Requested-With");

        boolean isJson = "json".equalsIgnoreCase(format)
                || "/api/analytics".equals(request.getServletPath())
                || "XMLHttpRequest".equalsIgnoreCase(requestedWith)
                || (acceptHeader != null && acceptHeader.contains("application/json"));

        if (isJson) {
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");

            PrintWriter out = response.getWriter();
            out.print("{" +
                    "\"studentsServedToday\":" + data.get("studentsServedToday") + "," +
                    "\"averageServiceTime\":" + data.get("averageServiceTime") + "," +
                    "\"averageWaitingTime\":" + data.get("averageWaitingTime") + "," +
                    "\"peakHour\":\"" + data.get("peakHour") + "\"," +
                    "\"noShowCount\":" + data.get("noShowCount") +
                    "}");
        } else {
            request.setAttribute("studentsServedToday", data.get("studentsServedToday"));
            request.setAttribute("averageServiceTime", data.get("averageServiceTime"));
            request.setAttribute("averageWaitingTime", data.get("averageWaitingTime"));
            request.setAttribute("peakHour", data.get("peakHour"));
            request.setAttribute("noShowCount", data.get("noShowCount"));

            request.getRequestDispatcher("/booking/analytics.jsp").forward(request, response);
        }
    }
}
