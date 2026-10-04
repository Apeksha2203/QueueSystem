package com.queue.service;

import com.queue.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

/**
 * Service class for computing system analytics and reporting data using SQL aggregation.
 */
public class AnalyticsService {

    /**
     * Retrieves the total count of students successfully served today.
     * Uses SQL aggregation: COUNT(*) on completed queue records for CURRENT_DATE.
     */
    public int getStudentsServedToday() {
        String sql =
                "SELECT COUNT(*) AS count FROM queue " +
                "WHERE status = 'COMPLETED' " +
                "AND DATE(COALESCE(completed_at, joined_at)) = CURRENT_DATE";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            if (resultSet.next()) {
                return resultSet.getInt("count");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Retrieves the average service time in minutes across all completed queue tickets.
     * Uses SQL aggregation: AVG(TIMESTAMPDIFF(MINUTE, started_at, completed_at)).
     */
    public double getAverageServiceTime() {
        String sql =
                "SELECT AVG(TIMESTAMPDIFF(MINUTE, started_at, completed_at)) AS avg_service " +
                "FROM queue " +
                "WHERE status = 'COMPLETED' " +
                "AND started_at IS NOT NULL AND completed_at IS NOT NULL";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            if (resultSet.next()) {
                double avg = resultSet.getDouble("avg_service");
                return resultSet.wasNull() ? 0.0 : Math.round(avg * 10.0) / 10.0;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    /**
     * Retrieves the average waiting time in minutes for students in the queue.
     * Uses SQL aggregation: AVG(TIMESTAMPDIFF(MINUTE, joined_at, COALESCE(started_at, called_at))).
     */
    public double getAverageWaitingTime() {
        String sql =
                "SELECT AVG(TIMESTAMPDIFF(MINUTE, joined_at, COALESCE(started_at, called_at, completed_at))) AS avg_wait " +
                "FROM queue " +
                "WHERE joined_at IS NOT NULL " +
                "AND (started_at IS NOT NULL OR called_at IS NOT NULL OR completed_at IS NOT NULL)";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            if (resultSet.next()) {
                double avg = resultSet.getDouble("avg_wait");
                return resultSet.wasNull() ? 0.0 : Math.round(avg * 10.0) / 10.0;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    /**
     * Calculates the peak traffic hour based on historical queue joins.
     * Uses SQL aggregation: GROUP BY HOUR(joined_at) ORDER BY count DESC LIMIT 1.
     *
     * @return Formatted string representation of peak hour (e.g. "10:00 - 11:00") or "N/A"
     */
    public String getPeakHour() {
        String sql =
                "SELECT HOUR(joined_at) AS hr, COUNT(*) AS queue_count " +
                "FROM queue " +
                "WHERE joined_at IS NOT NULL " +
                "GROUP BY HOUR(joined_at) " +
                "ORDER BY queue_count DESC LIMIT 1";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            if (resultSet.next()) {
                int hour = resultSet.getInt("hr");
                int nextHour = (hour + 1) % 24;
                return String.format("%02d:00 - %02d:00", hour, nextHour);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "N/A";
    }

    /**
     * Calculates the total count of no-shows from bookings and skipped tickets from queue.
     * Uses SQL aggregation.
     */

    /**
 * Calculates the count of no-shows and skipped tickets strictly for TODAY.
 */
public int getNoShowsToday() {
    String sql =
            "SELECT " +
            "(SELECT COUNT(*) FROM bookings WHERE status = 'NO_SHOW' AND booking_date = CURRENT_DATE) + " +
            "(SELECT COUNT(*) FROM queue WHERE status = 'SKIPPED' AND DATE(joined_at) = CURRENT_DATE) AS no_shows_today";

    try (Connection connection = DBConnection.getConnection();
         PreparedStatement statement = connection.prepareStatement(sql);
         ResultSet resultSet = statement.executeQuery()) {

        if (resultSet.next()) {
            return resultSet.getInt("no_shows_today");
        }
    } catch (Exception e) {
        e.printStackTrace();
    }

    return 0;
}

/**
 * Calculates the count of no-shows and skipped tickets for a specific service for TODAY.
 */
public int getNoShowsToday(int serviceId) {
    String sql =
            "SELECT " +
            "(SELECT COUNT(*) FROM bookings WHERE status = 'NO_SHOW' AND service_id = ? AND booking_date = CURRENT_DATE) + " +
            "(SELECT COUNT(*) FROM queue WHERE status = 'SKIPPED' AND service_id = ? AND DATE(joined_at) = CURRENT_DATE) AS no_shows_today";

    try (Connection connection = DBConnection.getConnection();
         PreparedStatement statement = connection.prepareStatement(sql)) {

        statement.setInt(1, serviceId);
        statement.setInt(2, serviceId);

        try (ResultSet resultSet = statement.executeQuery()) {
            if (resultSet.next()) {
                return resultSet.getInt("no_shows_today");
            }
        }
    } catch (Exception e) {
        e.printStackTrace();
    }

    return 0;
}
    public int getNoShowCount() {
        String sql =
                "SELECT " +
                "(SELECT COUNT(*) FROM bookings WHERE status = 'NO_SHOW') + " +
                "(SELECT COUNT(*) FROM queue WHERE status = 'SKIPPED') AS total_no_shows";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            if (resultSet.next()) {
                return resultSet.getInt("total_no_shows");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Returns a consolidated map of all analytics metrics for view and API rendering.
     */
    public Map<String, Object> getAnalyticsSummary() {
        Map<String, Object> data = new HashMap<>();
        data.put("studentsServedToday", getStudentsServedToday());
        data.put("averageServiceTime", getAverageServiceTime());
        data.put("averageWaitingTime", getAverageWaitingTime());
        data.put("peakHour", getPeakHour());
        data.put("noShowCount", getNoShowCount());
        return data;
    }
}
