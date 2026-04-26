package com.example.foodmanagement.repository;

import com.example.foodmanagement.model.FoodItem;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class FoodItemRepository {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<FoodItem> rowMapper = (rs, rowNum) -> {
        FoodItem item = new FoodItem();
        item.setId(rs.getLong("id"));
        item.setName(rs.getString("name"));
        item.setCategory(rs.getString("category"));
        item.setSupplierId(rs.getLong("supplier_id"));
        item.setSupplierName(rs.getString("supplier_name"));
        item.setQuantity(rs.getInt("quantity"));
        item.setReorderLevel(rs.getInt("reorder_level"));
        item.setUnit(rs.getString("unit"));
        item.setExpiryDate(rs.getString("expiry_date"));
        item.setBatchCode(rs.getString("batch_code"));
        item.setCreatedAt(rs.getString("created_at"));
        return item;
    };

    public FoodItemRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<FoodItem> findAll(Long restaurantId) {
        return jdbcTemplate.query("""
                SELECT f.*, s.name AS supplier_name
                FROM restaurant_food_items f
                JOIN restaurant_suppliers s ON s.id = f.supplier_id
                WHERE f.restaurant_id = ?
                ORDER BY f.expiry_date ASC
                """, rowMapper, restaurantId);
    }

    public Optional<FoodItem> findById(Long restaurantId, Long id) {
        List<FoodItem> items = jdbcTemplate.query("""
                SELECT f.*, s.name AS supplier_name
                FROM restaurant_food_items f
                JOIN restaurant_suppliers s ON s.id = f.supplier_id
                WHERE f.restaurant_id = ? AND f.id = ?
                """, rowMapper, restaurantId, id);
        return items.stream().findFirst();
    }

    public FoodItem save(Long restaurantId, FoodItem item) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement("""
                    INSERT INTO restaurant_food_items(restaurant_id, name, category, supplier_id, quantity, reorder_level, unit, expiry_date, batch_code)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """, Statement.RETURN_GENERATED_KEYS);
            bind(restaurantId, item, statement);
            return statement;
        }, keyHolder);

        item.setId(keyHolder.getKey().longValue());
        return item;
    }

    public boolean update(Long restaurantId, Long id, FoodItem item) {
        int rows = jdbcTemplate.update("""
                UPDATE restaurant_food_items
                SET name = ?, category = ?, supplier_id = ?, quantity = ?, reorder_level = ?, unit = ?, expiry_date = ?, batch_code = ?
                WHERE restaurant_id = ? AND id = ?
                """,
                item.getName(),
                item.getCategory(),
                item.getSupplierId(),
                item.getQuantity(),
                item.getReorderLevel(),
                item.getUnit(),
                item.getExpiryDate(),
                item.getBatchCode(),
                restaurantId,
                id
        );
        return rows > 0;
    }

    public boolean delete(Long restaurantId, Long id) {
        return jdbcTemplate.update(
                "DELETE FROM restaurant_food_items WHERE restaurant_id = ? AND id = ?",
                restaurantId,
                id
        ) > 0;
    }

    public int count(Long restaurantId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM restaurant_food_items WHERE restaurant_id = ?",
                Integer.class,
                restaurantId
        );
        return count == null ? 0 : count;
    }

    public int countExpiringSoon(Long restaurantId) {
        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM restaurant_food_items
                WHERE restaurant_id = ?
                    AND date(expiry_date) <= date('now', '+7 day')
                """, Integer.class, restaurantId);
        return count == null ? 0 : count;
    }

    public int countLowStock(Long restaurantId) {
        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM restaurant_food_items
                WHERE restaurant_id = ?
                    AND quantity <= reorder_level
                """, Integer.class, restaurantId);
        return count == null ? 0 : count;
    }

    private void bind(Long restaurantId, FoodItem item, PreparedStatement statement) throws java.sql.SQLException {
        statement.setLong(1, restaurantId);
        statement.setString(2, item.getName());
        statement.setString(3, item.getCategory());
        statement.setLong(4, item.getSupplierId());
        statement.setInt(5, item.getQuantity());
        statement.setInt(6, item.getReorderLevel());
        statement.setString(7, item.getUnit());
        statement.setString(8, item.getExpiryDate());
        statement.setString(9, item.getBatchCode());
    }
}
