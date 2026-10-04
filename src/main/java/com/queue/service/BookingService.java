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
import java.util.Arrays;
import java.util.List;

/**
 * Service class for handling appointment pre-bookings.
 */
public class BookingService {

    // Predefined service time slots (30-minute intervals from 09:00 to 16:30)
    public static final List<String> DEFAULT_SLOTS = Arrays.asList(
            "09:00:00", "09:30:00", "10:00:00", "10:30:00",
            "11:00:00", "11:30:00", "12:00:00", "12:30:00",
            "13:30:00", "14:00:00", "14:30:00", "15:00:00",
            "15:30:00", "16:00:00", "16:30:00"
    );

    /**
     * Checks if a particular slot is available for booking.
     * Prevents booking if slot is already occupied or past.
     *
     * @param serviceId   ID of the service
     * @param bookingDate Date of the booking
     * @param bookingTime Time of the booking
     * @return true if available, false otherwise
     */
    public boolean checkSlotAvailability(int serviceId, Date bookingDate, Time bookingTime) {
        // Rule: Prevent booking past time slots
        if (isPastSlot(bookingDate, bookingTime)) {
            return false;
        }

        // Rule: Prevent booking full slots (1 booking per service per slot)
        String countSql =
                "SELECT COUNT(*) AS slot_count FROM bookings " +
                "WHERE service_id = ? AND booking_date = ? AND booking_time = ? " +
                "AND status = 'BOOKED'";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(countSql)) {

            statement.setInt(1, serviceId);
            statement.setDate(2, bookingDate);
            statement.setTime(3, bookingTime);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    int slotCount = resultSet.getInt("slot_count");
                    return slotCount == 0;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Gets all available time slots for a service on a given date.
     */
    public List<String> getAvailableSlots(int serviceId, Date bookingDate) {
        List<String> available = new ArrayList<>();
        List<String> bookedSlots = new ArrayList<>();

        String bookedSql =
                "SELECT booking_time FROM bookings " +
                "WHERE service_id = ? AND booking_date = ? AND status = 'BOOKED'";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(bookedSql)) {

            statement.setInt(1, serviceId);
            statement.setDate(2, bookingDate);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    bookedSlots.add(resultSet.getTime("booking_time").toString());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        for (String slot : DEFAULT_SLOTS) {
            Time time = Time.valueOf(slot);
            if (!isPastSlot(bookingDate, time) && !bookedSlots.contains(slot)) {
                available.add(slot);
            }
        }

        return available;
    }

    /**
     * Creates a new booking with validation rules:
     * 1. Prevent past time slots
     * 2. Prevent duplicate active bookings for the student on the same date & time
     * 3. Prevent booking full slots
     *
     * @param booking Booking object containing studentId, serviceId, bookingDate, bookingTime
     * @return Result message indicating success or failure reason
     */
    public String createBooking(Booking booking) {
        if (booking == null) {
            return "Invalid booking request.";
        }

        Date date = booking.getBookingDate();
        Time time = booking.getBookingTime();

        // 1. Prevent past time slots
        if (isPastSlot(date, time)) {
            return "Cannot book a past date or time slot.";
        }

        // 2. Prevent duplicate bookings for the student at the same time
        if (hasDuplicateBooking(booking.getStudentId(), date, time)) {
            return "You already have an active booking at this date and time.";
        }

        // 3. Prevent booking full slots
        if (!checkSlotAvailability(booking.getServiceId(), date, time)) {
            return "Selected time slot is already fully booked.";
        }

        String insertSql =
                "INSERT INTO bookings (student_id, service_id, booking_date, booking_time, status) " +
                "VALUES (?, ?, ?, ?, 'BOOKED')";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(insertSql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            statement.setInt(1, booking.getStudentId());
            statement.setInt(2, booking.getServiceId());
            statement.setDate(3, date);
            statement.setTime(4, time);

            int rowsAffected = statement.executeUpdate();
            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        booking.setBookingId(generatedKeys.getInt(1));
                    }
                }
                booking.setStatus("BOOKED");
                return "SUCCESS";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Database error while creating booking: " + e.getMessage();
        }

        return "Failed to create booking.";
    }

    /**
     * Cancels an existing booking if its status is 'BOOKED'.
     *
     * @param bookingId ID of the booking
     * @param studentId ID of the student owning the booking (0 to skip student check)
     * @return true if successfully cancelled, false otherwise
     */
    public boolean cancelBooking(int bookingId, int studentId) {
        String updateSql;
        if (studentId > 0) {
            updateSql = "UPDATE bookings SET status = 'CANCELLED' WHERE booking_id = ? AND student_id = ? AND status = 'BOOKED'";
        } else {
            updateSql = "UPDATE bookings SET status = 'CANCELLED' WHERE booking_id = ? AND status = 'BOOKED'";
        }

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(updateSql)) {

            statement.setInt(1, bookingId);
            if (studentId > 0) {
                statement.setInt(2, studentId);
            }

            int rows = statement.executeUpdate();
            return rows > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Cancels an existing booking by booking ID.
     */
    public boolean cancelBooking(int bookingId) {
        return cancelBooking(bookingId, 0);
    }

    /**
     * Retrieves all bookings for a given student.
     *
     * @param studentId ID of the student
     * @return List of Booking objects
     */
    public List<Booking> getStudentBookings(int studentId) {
        List<Booking> bookings = new ArrayList<>();
        String sql = "SELECT * FROM bookings WHERE student_id = ? ORDER BY booking_date DESC, booking_time DESC";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, studentId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Booking b = new Booking();
                    b.setBookingId(resultSet.getInt("booking_id"));
                    b.setStudentId(resultSet.getInt("student_id"));
                    b.setServiceId(resultSet.getInt("service_id"));
                    b.setBookingDate(resultSet.getDate("booking_date"));
                    b.setBookingTime(resultSet.getTime("booking_time"));
                    b.setStatus(resultSet.getString("status"));
                    b.setCreatedAt(resultSet.getTimestamp("created_at"));
                    bookings.add(b);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return bookings;
    }

    /**
     * Retrieves all upcoming active bookings for a specific service (status = 'BOOKED' for today and future).
     *
     * @param serviceId ID of the service
     * @return List of upcoming Booking objects ordered by date and time
     */
    public List<Booking> getUpcomingBookings(int serviceId) {
        List<Booking> bookings = new ArrayList<>();
        String sql =
                "SELECT * FROM bookings " +
                "WHERE service_id = ? AND status = 'BOOKED' " +
                "AND (booking_date > CURRENT_DATE " +
                "     OR (booking_date = CURRENT_DATE AND booking_time >= CURRENT_TIME)) " +
                "ORDER BY booking_date ASC, booking_time ASC";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, serviceId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Booking b = new Booking();
                    b.setBookingId(resultSet.getInt("booking_id"));
                    b.setStudentId(resultSet.getInt("student_id"));
                    b.setServiceId(resultSet.getInt("service_id"));
                    b.setBookingDate(resultSet.getDate("booking_date"));
                    b.setBookingTime(resultSet.getTime("booking_time"));
                    b.setStatus(resultSet.getString("status"));
                    b.setCreatedAt(resultSet.getTimestamp("created_at"));
                    bookings.add(b);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return bookings;
    }

    /**
     * Retrieves upcoming bookings for a specific service on a specific date.
     *
     * @param serviceId   ID of the service
     * @param bookingDate Specific date to filter
     * @return List of Booking objects
     */
    public List<Booking> getUpcomingBookings(int serviceId, Date bookingDate) {
        List<Booking> bookings = new ArrayList<>();
        String sql =
                "SELECT * FROM bookings " +
                "WHERE service_id = ? AND booking_date = ? AND status = 'BOOKED' " +
                "ORDER BY booking_time ASC";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, serviceId);
            statement.setDate(2, bookingDate);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Booking b = new Booking();
                    b.setBookingId(resultSet.getInt("booking_id"));
                    b.setStudentId(resultSet.getInt("student_id"));
                    b.setServiceId(resultSet.getInt("service_id"));
                    b.setBookingDate(resultSet.getDate("booking_date"));
                    b.setBookingTime(resultSet.getTime("booking_time"));
                    b.setStatus(resultSet.getString("status"));
                    b.setCreatedAt(resultSet.getTimestamp("created_at"));
                    bookings.add(b);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return bookings;
    }

    /**
     * Retrieves a single booking by booking ID.
     */
    public Booking getBookingById(int bookingId) {
        String sql = "SELECT * FROM bookings WHERE booking_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, bookingId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    Booking b = new Booking();
                    b.setBookingId(resultSet.getInt("booking_id"));
                    b.setStudentId(resultSet.getInt("student_id"));
                    b.setServiceId(resultSet.getInt("service_id"));
                    b.setBookingDate(resultSet.getDate("booking_date"));
                    b.setBookingTime(resultSet.getTime("booking_time"));
                    b.setStatus(resultSet.getString("status"));
                    b.setCreatedAt(resultSet.getTimestamp("created_at"));
                    return b;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Checks if a student already has an active booking at the same date and time.
     */
    public boolean hasDuplicateBooking(int studentId, Date bookingDate, Time bookingTime) {
        String checkSql =
                "SELECT COUNT(*) AS cnt FROM bookings " +
                "WHERE student_id = ? AND booking_date = ? AND booking_time = ? AND status = 'BOOKED'";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(checkSql)) {

            statement.setInt(1, studentId);
            statement.setDate(2, bookingDate);
            statement.setTime(3, bookingTime);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("cnt") > 0;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Checks if a date and time slot is in the past.
     */
    private boolean isPastSlot(Date bookingDate, Time bookingTime) {
        if (bookingDate == null || bookingTime == null) {
            return true;
        }

        LocalDate slotDate = bookingDate.toLocalDate();
        LocalDate today = LocalDate.now();

        if (slotDate.isBefore(today)) {
            return true;
        } else if (slotDate.isEqual(today)) {
            LocalTime slotTime = bookingTime.toLocalTime();
            LocalTime now = LocalTime.now();
            return slotTime.isBefore(now);
        }

        return false;
    }
}
