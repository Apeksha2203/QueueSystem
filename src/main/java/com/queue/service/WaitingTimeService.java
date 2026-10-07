package com.queue.service;

import com.queue.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Service to calculate estimated waiting time based on historical service performance,
 * queue length, and active counter staff.
 */
public class WaitingTimeService {

    public static final String SERVICE_UNAVAILABLE = "SERVICE CURRENTLY UNAVAILABLE";

    /**
     * Calculates estimated waiting time formatted as a string.
     * Formula: estimatedWait = (studentsAhead * averageServiceTime) / activeStaff
     *
     * @param serviceId     ID of the service
     * @param studentsAhead Number of students ahead in queue
     * @param activeStaff   Number of active staff currently serving
     * @return String description of estimated wait time or SERVICE CURRENTLY UNAVAILABLE
     */
    public String calculateEstimatedWaitingTime(int serviceId, int studentsAhead, int activeStaff) {
        if (activeStaff <= 0) {
            return SERVICE_UNAVAILABLE;
        }

        if (studentsAhead < 0) {
            studentsAhead = 0;
        }

        double averageServiceTime = getAverageServiceTime(serviceId);
        int estimatedMinutes = (int) Math.round((studentsAhead * averageServiceTime) / activeStaff);

        if (estimatedMinutes <= 0) {
            return "Less than a minute";
        } else if (estimatedMinutes == 1) {
            return "1 minute";
        } else {
            return estimatedMinutes + " minutes";
        }
    }

    /**
     * Calculates estimated waiting time in minutes as an integer.
     * Returns -1 if activeStaff is 0 or negative.
     */
    public int calculateEstimatedWaitMinutes(int serviceId, int studentsAhead, int activeStaff) {
        if (activeStaff <= 0) {
            return -1;
        }

        if (studentsAhead < 0) {
            studentsAhead = 0;
        }

        double averageServiceTime = getAverageServiceTime(serviceId);
        return (int) Math.round((studentsAhead * averageServiceTime) / activeStaff);
    }

    /**
     * Retrieves average service time in minutes.
     * First checks historical completed records from the queue table.
     * If no completed records exist, falls back to the configured service table average.
     */
    public double getAverageServiceTime(int serviceId) {
        String historicalSql="SELECT started_at,completed_at,processing_seconds FROM queue WHERE service_id=? AND status='COMPLETED' AND started_at IS NOT NULL AND completed_at>started_at ORDER BY completed_at DESC,queue_id DESC LIMIT 20";
        try(Connection c=DBConnection.getConnection();PreparedStatement p=c.prepareStatement(historicalSql)) {
            p.setInt(1,serviceId);try(ResultSet r=p.executeQuery()) {
                double total=0;int count=0;
                while(r.next()){double minutes=r.getObject(3)!=null?r.getDouble(3)/60.0:ServiceHours.processingMinutes(r.getTimestamp(1).toLocalDateTime(),r.getTimestamp(2).toLocalDateTime());if(minutes>0){total+=minutes;count++;}}
                if(count>0)return Math.max(1.0/60.0,total/count);
            }
        }catch(Exception error){throw new IllegalStateException("Service estimate unavailable",error);}
        // Fallback to average_service_time defined in services table
        String serviceConfigSql =
                "SELECT average_service_time FROM services WHERE service_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(serviceConfigSql)) {

            statement.setInt(1, serviceId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    int configAvg = resultSet.getInt("average_service_time");
                    if (configAvg > 0) {
                        return configAvg;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Default standard fallback if service not found or 0
        return 10.0;
    }
}
