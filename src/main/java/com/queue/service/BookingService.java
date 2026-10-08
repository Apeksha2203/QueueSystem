// VIVA GUIDE: Retained legacy fixed-slot booking logic. Do not present it as the current same-day reservation algorithm.
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
    // Operation checkSlotAvailability: follow the inputs, validation and return value in the block below; the file header explains its layer/caller.
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
    // Retrieve available slots for the caller; follow the SQL/service delegation to identify scope and return shape.
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
    // Perform create booking; inspect its conditions, affected rows and return value before describing success.
    public String createBooking(Booking booking) {
        if (booking == null || booking.getStudentId() <= 0 || booking.getServiceId() <= 0) return "Invalid booking request.";
        Date date=booking.getBookingDate(); Time time=booking.getBookingTime();
        if (isPastSlot(date,time)) return "Cannot book a past date or time slot.";
        if (!DEFAULT_SLOTS.contains(time.toString())) return "Invalid time slot.";
        try (Connection connection=DBConnection.getConnection()) {
            connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            connection.setAutoCommit(false);
            try {
                // Always student then service: serializes cross-service duplicates and service capacity.
                if (!com.queue.util.TransactionLocks.student(connection,booking.getStudentId()) ||
                    !com.queue.util.TransactionLocks.service(connection,booking.getServiceId())) {
                    connection.rollback();return "Invalid student or service.";
                }
                try (PreparedStatement check=connection.prepareStatement("SELECT booking_id FROM bookings WHERE student_id=? AND booking_date=? AND booking_time=? AND status='BOOKED' LIMIT 1")) {
                    check.setInt(1,booking.getStudentId());check.setDate(2,date);check.setTime(3,time);
                    try(ResultSet rows=check.executeQuery()) { if(rows.next()) { connection.rollback();return "You already have an active booking at this date and time."; } }
                }
                try (PreparedStatement check=connection.prepareStatement("SELECT booking_id FROM bookings WHERE service_id=? AND booking_date=? AND booking_time=? AND status='BOOKED' LIMIT 1")) {
                    check.setInt(1,booking.getServiceId());check.setDate(2,date);check.setTime(3,time);
                    try(ResultSet rows=check.executeQuery()) { if(rows.next()) { connection.rollback();return "Selected time slot is already fully booked."; } }
                }
                if (isPastSlot(date,time)) { connection.rollback();return "Cannot book a past date or time slot."; }
                try(PreparedStatement insert=connection.prepareStatement("INSERT INTO bookings(student_id,service_id,booking_date,booking_time,status) VALUES (?,?,?,?,'BOOKED')",java.sql.Statement.RETURN_GENERATED_KEYS)) {
                    insert.setInt(1,booking.getStudentId());insert.setInt(2,booking.getServiceId());insert.setDate(3,date);insert.setTime(4,time);insert.executeUpdate();
                    try(ResultSet keys=insert.getGeneratedKeys()) { keys.next();booking.setBookingId(keys.getInt(1)); }
                }
                connection.commit();booking.setStatus("BOOKED");return "SUCCESS";
            } catch(Exception error) { connection.rollback();throw error; }
        } catch(Exception error) { throw new IllegalStateException("Unable to create booking.",error); }
    }
    /**
     * Cancels an existing booking if its status is 'BOOKED'.
     *
     * @param bookingId ID of the booking
     * @param studentId ID of the student owning the booking (0 to skip student check)
     * @return true if successfully cancelled, false otherwise
     */
    // Perform cancel booking; inspect its conditions, affected rows and return value before describing success.
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
    // Perform cancel booking; inspect its conditions, affected rows and return value before describing success.
    public boolean cancelBooking(int bookingId) {
        return cancelBooking(bookingId, 0);
    }

    /**
     * Retrieves all bookings for a given student.
     *
     * @param studentId ID of the student
     * @return List of Booking objects
     */
    // Retrieve student bookings for the caller; follow the SQL/service delegation to identify scope and return shape.
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
    // Retrieve upcoming bookings for the caller; follow the SQL/service delegation to identify scope and return shape.
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
    // Retrieve upcoming bookings for the caller; follow the SQL/service delegation to identify scope and return shape.
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
 * Retrieves upcoming bookings with student and service details.
 * Uses SQL JOIN to avoid N+1 queries.
 *
 * @param serviceId ID of the service
 * @return List of maps containing booking, student, and service details
 */
public List<java.util.Map<String, Object>> getUpcomingBookingsWithDetails(int serviceId) {
    List<java.util.Map<String, Object>> list = new ArrayList<>();

    String sql =
            "SELECT b.booking_id, b.student_id, b.service_id, b.booking_date, b.booking_time, " +
            "       b.status, b.created_at, st.student_name, st.email, st.phone, sv.service_name " +
            "FROM bookings b " +
            "JOIN students st ON b.student_id = st.student_id " +
            "JOIN services sv ON b.service_id = sv.service_id " +
            "WHERE b.service_id = ? AND b.status = 'BOOKED' " +
            "AND (b.booking_date > CURRENT_DATE " +
            "     OR (b.booking_date = CURRENT_DATE AND b.booking_time >= CURRENT_TIME)) " +
            "ORDER BY b.booking_date ASC, b.booking_time ASC";

    try (Connection connection = DBConnection.getConnection();
         PreparedStatement statement = connection.prepareStatement(sql)) {

        statement.setInt(1, serviceId);

        try (ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
                java.util.Map<String, Object> map = new java.util.HashMap<>();

                map.put("bookingId", rs.getInt("booking_id"));
                map.put("studentId", rs.getInt("student_id"));
                map.put("studentName", rs.getString("student_name"));
                map.put("email", rs.getString("email"));
                map.put("phone", rs.getString("phone"));
                map.put("serviceId", rs.getInt("service_id"));
                map.put("serviceName", rs.getString("service_name"));
                map.put("bookingDate", rs.getDate("booking_date"));
                map.put("bookingTime", rs.getTime("booking_time"));
                map.put("status", rs.getString("status"));
                map.put("createdAt", rs.getTimestamp("created_at"));

                list.add(map);
            }
        }

    } catch (Exception e) {
        e.printStackTrace();
    }

    return list;
}

    /**
     * Retrieves a single booking by booking ID.
     */
    // Retrieve booking by id for the caller; follow the SQL/service delegation to identify scope and return shape.
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
    // Operation hasDuplicateBooking: follow the inputs, validation and return value in the block below; the file header explains its layer/caller.
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
    // Operation isPastSlot: follow the inputs, validation and return value in the block below; the file header explains its layer/caller.
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
