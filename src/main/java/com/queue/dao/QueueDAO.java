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

        String tokenSql =
                "SELECT COALESCE(MAX(token_number), 0) + 1 " +
                "AS next_token FROM queue " +
                "WHERE service_id = ?";

        String insertSql =
                "INSERT INTO queue " +
                "(student_id, service_id, token_number, status) " +
                "VALUES (?, ?, ?, 'WAITING')";

        try (Connection connection = DBConnection.getConnection()) {

            int nextToken;

            try (PreparedStatement statement =
                         connection.prepareStatement(tokenSql)) {

                statement.setInt(1, queue.getServiceId());

                try (ResultSet resultSet = statement.executeQuery()) {

                    resultSet.next();
                    nextToken = resultSet.getInt("next_token");
                }
            }

            try (PreparedStatement statement =
                         connection.prepareStatement(insertSql)) {

                statement.setInt(1, queue.getStudentId());
                statement.setInt(2, queue.getServiceId());
                statement.setInt(3, nextToken);

                int rowsInserted = statement.executeUpdate();

                if (rowsInserted > 0) {

                    queue.setTokenNumber(nextToken);
                    queue.setStatus("WAITING");

                    return true;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }


    public Queue getQueueStatus(int studentId, int serviceId) {

        String sql =
                "SELECT * FROM queue " +
                "WHERE student_id = ? AND service_id = ? " +
                "ORDER BY queue_id DESC LIMIT 1";

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


    public int getQueuePosition(int studentId, int serviceId) {

        String tokenSql =
                "SELECT token_number FROM queue " +
                "WHERE student_id = ? " +
                "AND service_id = ? " +
                "AND status IN ('WAITING', 'CALLED', 'SERVING') " +
                "ORDER BY queue_id DESC LIMIT 1";

        String positionSql =
                "SELECT COUNT(*) AS position " +
                "FROM queue " +
                "WHERE service_id = ? " +
                "AND token_number <= ? " +
                "AND status IN ('WAITING', 'CALLED', 'SERVING')";

        try (Connection connection = DBConnection.getConnection()) {

            int studentToken;

            try (PreparedStatement statement =
                         connection.prepareStatement(tokenSql)) {

                statement.setInt(1, studentId);
                statement.setInt(2, serviceId);

                try (ResultSet resultSet = statement.executeQuery()) {

                    if (!resultSet.next()) {
                        return -1;
                    }

                    studentToken =
                            resultSet.getInt("token_number");
                }
            }

            try (PreparedStatement statement =
                         connection.prepareStatement(positionSql)) {

                statement.setInt(1, serviceId);
                statement.setInt(2, studentToken);

                try (ResultSet resultSet = statement.executeQuery()) {

                    if (resultSet.next()) {
                        return resultSet.getInt("position");
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return -1;
    }


    public int getCurrentToken(int serviceId) {

        String sql =
                "SELECT token_number FROM queue " +
                "WHERE service_id = ? " +
                "AND status IN ('CALLED', 'SERVING') " +
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
            "SELECT * FROM queue " +
            "WHERE service_id = ? " +
            "AND status IN ('WAITING', 'CALLED', 'SERVING') " +
            "ORDER BY token_number";

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

    public Queue callNext(int serviceId) {

        String findSql =
                "SELECT * FROM queue " +
                "WHERE service_id = ? " +
                "AND status = 'WAITING' " +
                "ORDER BY token_number ASC " +
                "LIMIT 1";

        String updateSql =
                "UPDATE queue " +
                "SET status = 'CALLED', called_at = CURRENT_TIMESTAMP " +
                "WHERE queue_id = ?";

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
                "SELECT * FROM queue " +
                "WHERE queue_id = ?";

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