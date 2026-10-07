package com.queue.dao;

import com.queue.model.Queue;
import com.queue.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class QueueDAO {

    // =========================
    // STUDENT QUEUE OPERATIONS
    // =========================

    public boolean joinQueue(Queue queue) {
        try {
            java.util.Map<String,Object> result=new com.queue.service.ReservationService().reserve(queue.getStudentId(),queue.getServiceId());
            queue.setQueueId(((Number)result.get("queueId")).intValue());queue.setTokenNumber(Integer.parseInt(result.get("token").toString()));queue.setStatus("WAITING");return true;
        } catch (IllegalArgumentException conflict) { return false; }
        catch (java.sql.SQLException error) { throw new IllegalStateException("Unable to reserve a place.",error); }
    }
    public Queue getQueueStatus(int studentId, int serviceId) {

        String sql =
                "SELECT queue.* FROM queue JOIN queue_reservations r ON r.queue_id=queue.queue_id " +
                "WHERE student_id = ? AND service_id = ? AND r.visit_date >= DATE(CONVERT_TZ(UTC_TIMESTAMP(), '+00:00', '+05:30')) " +
                "ORDER BY queue.queue_id DESC LIMIT 1";

        try (
            Connection connection = DBConnection.getConnection();
            PreparedStatement statement =
                    connection.prepareStatement(sql)
        ) {

            statement.setInt(1, studentId);
            statement.setInt(2, serviceId);

            try (ResultSet resultSet = statement.executeQuery()) {

                if (resultSet.next()) {

                    return mapQueue(resultSet);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }


    public int getQueuePosition(int studentId,int serviceId) {
        try { java.util.Map<String,Object> preview=new com.queue.service.ReservationService().preview(studentId,serviceId);return preview.get("queueId")==null?-1:((Number)preview.get("position")).intValue(); }
        catch(Exception error) { throw new IllegalStateException("Unable to read queue position.",error); }
    }
    public int getCurrentToken(int serviceId) {

        String sql =
                "SELECT token_number FROM queue JOIN queue_reservations r ON r.queue_id=queue.queue_id " +
                "WHERE service_id = ? " +
                "AND status IN ('CALLED', 'SERVING') AND r.visit_date=DATE(CONVERT_TZ(UTC_TIMESTAMP(), '+00:00', '+05:30')) " +
                "ORDER BY token_number DESC LIMIT 1";

        try (
            Connection connection = DBConnection.getConnection();
            PreparedStatement statement =
                    connection.prepareStatement(sql)
        ) {

            statement.setInt(1, serviceId);

            try (ResultSet resultSet = statement.executeQuery()) {

                if (resultSet.next()) {
                    return resultSet.getInt("token_number");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }

    public java.util.List<Queue> getActiveQueue(int serviceId) {

    java.util.List<Queue> queueList =
            new java.util.ArrayList<>();

    String sql =
            "SELECT queue.* FROM queue JOIN queue_reservations r ON r.queue_id=queue.queue_id " +
            "WHERE service_id = ? " +
            "AND status IN ('WAITING', 'CALLED', 'SERVING') AND r.visit_date = DATE(CONVERT_TZ(UTC_TIMESTAMP(), '+00:00', '+05:30')) " +
            "ORDER BY CASE WHEN status IN ('CALLED','SERVING') THEN 0 ELSE 1 END,r.queue_order,queue.queue_id";

    try (
        Connection connection = DBConnection.getConnection();
        PreparedStatement statement =
                connection.prepareStatement(sql)
    ) {

        statement.setInt(1, serviceId);

        try (ResultSet resultSet =
                     statement.executeQuery()) {

            while (resultSet.next()) {
                queueList.add(mapQueue(resultSet));
            }
        }

    } catch (Exception e) {
        e.printStackTrace();
    }

    return queueList;
}


    // =========================
    // STAFF QUEUE OPERATIONS
    // =========================

    public Queue callStudent(int queueId) {

    String sql =
            "UPDATE queue " +
            "SET status = 'CALLED', " +
            "called_at = CURRENT_TIMESTAMP " +
            "WHERE queue_id = ? " +
            "AND status = 'WAITING'";

    try (
        Connection connection = DBConnection.getConnection();
        PreparedStatement statement =
                connection.prepareStatement(sql)
    ) {

        statement.setInt(1, queueId);

        int updated = statement.executeUpdate();

        if (updated > 0) {
            return getQueueById(connection, queueId);
        }

    } catch (Exception e) {
        e.printStackTrace();
    }

    return null;
}

    public Queue callNext(int serviceId) {

        String findSql =
                "SELECT queue.* FROM queue JOIN queue_reservations r ON r.queue_id=queue.queue_id " +
                "WHERE service_id = ? " +
                "AND status = 'WAITING' " +
                "ORDER BY token_number ASC " +
                "LIMIT 1 FOR UPDATE";

        String updateSql =
                "UPDATE queue " +
                "SET status = 'CALLED', called_at = CURRENT_TIMESTAMP " +
                "WHERE queue_id = ? AND status = 'WAITING'";

        try (Connection connection = DBConnection.getConnection()) {

            connection.setAutoCommit(false);

            try (
                PreparedStatement findStatement =
                        connection.prepareStatement(findSql);
                PreparedStatement updateStatement =
                        connection.prepareStatement(updateSql)
            ) {

                findStatement.setInt(1, serviceId);

                try (ResultSet resultSet =
                             findStatement.executeQuery()) {

                    if (!resultSet.next()) {
                        connection.rollback();
                        return null;
                    }

                    int queueId =
                            resultSet.getInt("queue_id");

                    updateStatement.setInt(1, queueId);

                    int updated =
                            updateStatement.executeUpdate();

                    if (updated == 0) {
                        connection.rollback();
                        return null;
                    }

                    connection.commit();

                    return getQueueById(connection, queueId);
                }

            } catch (Exception e) {

                connection.rollback();
                throw e;

            } finally {

                connection.setAutoCommit(true);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }


    public boolean startService(int queueId) {

        String sql =
                "UPDATE queue " +
                "SET status = 'SERVING', " +
                "started_at = CURRENT_TIMESTAMP " +
                "WHERE queue_id = ? " +
                "AND status = 'CALLED'";

        try (
            Connection connection = DBConnection.getConnection();
            PreparedStatement statement =
                    connection.prepareStatement(sql)
        ) {

            statement.setInt(1, queueId);

            return statement.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }


    public boolean completeService(int queueId) {

        String sql =
                "UPDATE queue " +
                "SET status = 'COMPLETED', " +
                "completed_at = CURRENT_TIMESTAMP " +
                "WHERE queue_id = ? " +
                "AND status = 'SERVING'";

        try (
            Connection connection = DBConnection.getConnection();
            PreparedStatement statement =
                    connection.prepareStatement(sql)
        ) {

            statement.setInt(1, queueId);

            return statement.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }


    public boolean skipStudent(int queueId) {

        String sql =
                "UPDATE queue " +
                "SET status = 'SKIPPED', " +
                "completed_at = CURRENT_TIMESTAMP " +
                "WHERE queue_id = ? " +
                "AND status IN ('CALLED', 'SERVING')";

        try (
            Connection connection = DBConnection.getConnection();
            PreparedStatement statement =
                    connection.prepareStatement(sql)
        ) {

            statement.setInt(1, queueId);

            return statement.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public java.util.List<Queue> getRecentActivity(int serviceId) {

    java.util.List<Queue> activityList =
            new java.util.ArrayList<>();

    String sql =
            "SELECT queue.* FROM queue JOIN queue_reservations r ON r.queue_id=queue.queue_id " +
            "WHERE service_id = ? " +
            "AND (" +
            "called_at IS NOT NULL " +
            "OR started_at IS NOT NULL " +
            "OR completed_at IS NOT NULL" +
            ") " +
            "ORDER BY " +
            "COALESCE(completed_at, started_at, called_at) DESC " +
            "LIMIT 10";

    try (
        Connection connection = DBConnection.getConnection();
        PreparedStatement statement =
                connection.prepareStatement(sql)
    ) {

        statement.setInt(1, serviceId);

        try (ResultSet resultSet =
                     statement.executeQuery()) {

            while (resultSet.next()) {

                activityList.add(
                    mapQueue(resultSet)
                );
            }
        }

    } catch (Exception e) {
        e.printStackTrace();
    }

    return activityList;
}


    // =========================
    // HELPER METHODS
    // =========================

    public Queue getQueueById(int queueId) {

    try (
        Connection connection = DBConnection.getConnection()
    ) {
        return getQueueById(connection, queueId);

    } catch (Exception e) {
        e.printStackTrace();
    }

    return null;
}

    private Queue getQueueById(Connection connection,
                               int queueId) throws Exception {

        String sql =
                "SELECT queue.* FROM queue JOIN queue_reservations r ON r.queue_id=queue.queue_id " +
                "WHERE queue.queue_id = ?";

        try (
            PreparedStatement statement =
                    connection.prepareStatement(sql)
        ) {

            statement.setInt(1, queueId);

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                if (resultSet.next()) {
                    return mapQueue(resultSet);
                }
            }
        }

        return null;
    }


    private Queue mapQueue(ResultSet resultSet)
            throws Exception {

        Queue queue = new Queue();

        queue.setQueueId(
                resultSet.getInt("queue_id")
        );

        queue.setStudentId(
                resultSet.getInt("student_id")
        );

        queue.setServiceId(
                resultSet.getInt("service_id")
        );

        queue.setTokenNumber(
                resultSet.getInt("token_number")
        );

        queue.setStatus(
                resultSet.getString("status")
        );

        queue.setJoinedAt(
                resultSet.getTimestamp("joined_at")
        );

        queue.setCalledAt(
                resultSet.getTimestamp("called_at")
        );

        queue.setStartedAt(
                resultSet.getTimestamp("started_at")
        );

        queue.setCompletedAt(
                resultSet.getTimestamp("completed_at")
        );

        return queue;
    }
}
