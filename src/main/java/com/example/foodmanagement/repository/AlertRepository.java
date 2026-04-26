package com.example.foodmanagement.repository;

import com.example.foodmanagement.model.AlertLog;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class AlertRepository {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<AlertLog> rowMapper = (rs, rowNum) -> {
        AlertLog alert = new AlertLog();
        alert.setId(rs.getLong("id"));
        alert.setFoodItemId(rs.getLong("food_item_id"));
        alert.setAlertType(rs.getString("alert_type"));
        alert.setMessage(rs.getString("message"));
        alert.setStatus(rs.getString("status"));
        alert.setCreatedAt(rs.getString("created_at"));
        return alert;
    };

    public AlertRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<AlertLog> findAll(Long restaurantId, String status) {
        if (status == null || status.isBlank()) {
            return jdbcTemplate.query(
                    "SELECT * FROM restaurant_alert_logs WHERE restaurant_id = ? ORDER BY created_at DESC",
                    rowMapper,
                    restaurantId
            );
        }
        return jdbcTemplate.query(
                "SELECT * FROM restaurant_alert_logs WHERE restaurant_id = ? AND status = ? ORDER BY created_at DESC",
                rowMapper,
                restaurantId,
                status.toUpperCase()
        );
    }

    public int countOpenAlerts(Long restaurantId) {
        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM restaurant_alert_logs
                WHERE restaurant_id = ?
                    AND status = 'OPEN'
                """, Integer.class, restaurantId);
        return count == null ? 0 : count;
    }
}
