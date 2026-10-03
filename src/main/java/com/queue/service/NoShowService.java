package com.queue.service;

import com.queue.model.Booking;
import com.queue.util.DBConnection;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Service class for handling no-show detection and slot release based on a configurable grace period.
 */
public class NoShowService {

    public static final String STATUS_NO_SHOW = "NO_SHOW";
    public static final int DEFAULT_GRACE_PERIOD_MINUTES = 5;

    private int gracePeriodMinutes;

    public NoShowService() {
        this.gracePeriodMinutes = DEFAULT_GRACE_PERIOD_MINUTES;
    }

    public NoShowService(int gracePeriodMinutes) {
        this.gracePeriodMinutes = gracePeriodMinutes > 0 ? gracePeriodMinutes : DEFAULT_GRACE_PERIOD_MINUTES;
    }

    public int getGracePeriodMinutes() {
        return gracePeriodMinutes;
    }

    public void setGracePeriodMinutes(int gracePeriodMinutes) {
        this.gracePeriodMinutes = gracePeriodMinutes > 0 ? gracePeriodMinutes : DEFAULT_GRACE_PERIOD_MINUTES;
    }

    /**
     * Evaluates if a given booking has exceeded its appointment time plus the grace period.
     * Example: Booking Time = 10:30, Grace Period = 5 mins, Current Time > 10:35 -> true
     */
    public boolean isNoShow(Booking booking, int graceMinutes) {
        if (booking == null || !"BOOKED".equalsIgnoreCase(booking.getStatus())) {
            return false;
        }

        Date bookingDate = booking.getBookingDate();
        Time bookingTime = booking.getBookingTime();

        if (bookingDate == null || bookingTime == null) {
            return false;
        }

        LocalDate bDate = bookingDate.toLocalDate();
        LocalDate today = LocalDate.now();

        if (bDate.isBefore(today)) {
            return true;
        } else if (bDate.isEqual(today)) {
            LocalTime bTime = bookingTime.toLocalTime();
            LocalTime cutoffTime = bTime.plusMinutes(graceMinutes);
            return LocalTime.now().isAfter(cutoffTime);
        }

        return false;
    }

    public boolean isNoShow(Booking booking) {
        return isNoShow(booking, this.gracePeriodMinutes);
    }

    /**
     * Scans all active 'BOOKED' records for today (or past dates) that exceeded the grace period,
     * updates their status to 'NO_SHOW', and releases the slot.
     *
     * @return Number of bookings updated to NO_SHOW
     */
    public int checkAndUpdateNoShows() {
        return checkAndUpdateNoShows(this.gracePeriodMinutes);
    }

    /**
     * Scans and marks no-show bookings with a specified grace period.
     */
    public int checkAndUpdateNoShows(int graceMinutes) {
        int updatedCount = 0;

        // SQL finds bookings that are past date, or today and past (booking_time + graceMinutes)
        String selectSql =
                "SELECT booking_id FROM bookings " +
                "WHERE status = 'BOOKED' " +
                "AND (booking_date < CURRENT_DATE " +
                "     OR (booking_date = CURRENT_DATE AND ADDTIME(booking_time, SEC_TO_TIME(? * 60)) < CURRENT_TIME))";

        String updateSql =
                "UPDATE bookings SET status = ? WHERE booking_id = ? AND status = 'BOOKED'";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement selectStatement = connection.prepareStatement(selectSql)) {

            selectStatement.setInt(1, graceMinutes);
            List<Integer> noShowIds = new ArrayList<>();

            try (ResultSet rs = selectStatement.executeQuery()) {
                while (rs.next()) {
                    noShowIds.add(rs.getInt("booking_id"));
                }
            }

            if (!noShowIds.isEmpty()) {
                try (PreparedStatement updateStatement = connection.prepareStatement(updateSql)) {
                    for (int bookingId : noShowIds) {
                        updateStatement.setString(1, STATUS_NO_SHOW);
                        updateStatement.setInt(2, bookingId);
                        updateStatement.addBatch();
                    }
                    int[] results = updateStatement.executeBatch();
                    for (int res : results) {
                        if (res > 0 || res == PreparedStatement.SUCCESS_NO_INFO) {
                            updatedCount++;
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return updatedCount;
    }

    /**
     * Marks a specific booking as NO_SHOW by booking ID.
     */
    public boolean markAsNoShow(int bookingId) {
        String updateSql = "UPDATE bookings SET status = ? WHERE booking_id = ? AND status = 'BOOKED'";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(updateSql)) {

            statement.setString(1, STATUS_NO_SHOW);
            statement.setInt(2, bookingId);

            int rows = statement.executeUpdate();
            return rows > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }
}
