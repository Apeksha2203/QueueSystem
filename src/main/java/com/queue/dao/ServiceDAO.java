// VIVA GUIDE: Catalogue SELECT and model mapping. Now throws on failure so the servlet does not fabricate an empty successful result.
package com.queue.dao;

import com.queue.model.Service;
import com.queue.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class ServiceDAO {

    // Reads catalogue rows and maps them to Service objects; failure propagates so the servlet can report HTTP 503 instead of fake empty success.
    public List<Service> getAllServices() {

        List<Service> services = new ArrayList<>();

        String sql = "SELECT * FROM services";

        try (
            Connection connection = DBConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql);
            ResultSet resultSet = statement.executeQuery()
        ) {

            while (resultSet.next()) {

                Service service = new Service();

                service.setServiceId(
                        resultSet.getInt("service_id")
                );

                service.setServiceName(
                        resultSet.getString("service_name")
                );

                service.setDescription(
                        resultSet.getString("description")
                );

                service.setAverageServiceTime(
                        resultSet.getInt("average_service_time")
                );

                services.add(service);
            }

        } catch (Exception e) {
            throw new IllegalStateException("Cannot load service catalogue", e);
        }

        return services;
    }
}
