package com.queue.controller;

import com.queue.model.Service;
import com.queue.service.ServiceService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

@WebServlet("/api/services")
public class ServiceServlet extends HttpServlet {

    private ServiceService serviceService;

    @Override
    public void init() {
        serviceService = new ServiceService();
    }

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        List<Service> services =
                serviceService.getAllServices();

        StringBuilder json =
                new StringBuilder();

        json.append("{");
        json.append("\"success\":true,");
        json.append("\"message\":\"Services retrieved successfully\",");
        json.append("\"data\":[");

        for (int i = 0; i < services.size(); i++) {

            Service service = services.get(i);

            json.append("{");
            json.append("\"serviceId\":")
                .append(service.getServiceId())
                .append(",");

            json.append("\"serviceName\":\"")
                .append(service.getServiceName())
                .append("\",");

            json.append("\"description\":\"")
                .append(service.getDescription())
                .append("\",");

            json.append("\"averageServiceTime\":")
                .append(service.getAverageServiceTime());

            json.append("}");

            if (i < services.size() - 1) {
                json.append(",");
            }
        }

        json.append("]");
        json.append("}");

        response.getWriter().write(
                json.toString()
        );
    }
}
