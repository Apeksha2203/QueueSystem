package com.queue.dao;

import com.queue.model.Counter;
import com.queue.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class CounterDAO {

    public List<Counter> getCountersByService(int serviceId) {

        List<Counter> counters = new ArrayList<>();

        String sql = "SELECT * FROM counters " +
                     "WHERE service_id = ? " +
                     "AND is_active = TRUE " +
                     "ORDER BY counter_id";

        try (
            Connection connection = DBConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)
        ) {

            statement.setInt(1, serviceId);

            try (ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {

                    Counter counter = new Counter();

                    counter.setCounterId(
                            resultSet.getInt("counter_id")
                    );

                    counter.setCounterName(
                            resultSet.getString("counter_name")
                    );

                    counter.setServiceId(
                            resultSet.getInt("service_id")
                    );

                    counter.setActive(
                            resultSet.getBoolean("is_active")
                    );

                    counters.add(counter);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return counters;
    }


    public Counter getCounterById(int counterId) {

        String sql = "SELECT * FROM counters " +
                     "WHERE counter_id = ?";

        try (
            Connection connection = DBConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)
        ) {

            statement.setInt(1, counterId);

            try (ResultSet resultSet = statement.executeQuery()) {

                if (resultSet.next()) {

                    Counter counter = new Counter();

                    counter.setCounterId(
                            resultSet.getInt("counter_id")
                    );

                    counter.setCounterName(
                            resultSet.getString("counter_name")
                    );

                    counter.setServiceId(
                            resultSet.getInt("service_id")
                    );

                    counter.setActive(
                            resultSet.getBoolean("is_active")
                    );

                    return counter;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }


    public boolean updateCounterStatus(int counterId,
                                       boolean active) {

        String sql = "UPDATE counters " +
                     "SET is_active = ? " +
                     "WHERE counter_id = ?";

        try (
            Connection connection = DBConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)
        ) {

            statement.setBoolean(1, active);
            statement.setInt(2, counterId);

            return statement.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }
}