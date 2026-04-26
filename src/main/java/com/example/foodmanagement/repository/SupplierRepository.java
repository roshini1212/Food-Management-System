package com.example.foodmanagement.repository;

import com.example.foodmanagement.model.Supplier;
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
public class SupplierRepository {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<Supplier> rowMapper = (rs, rowNum) -> {
        Supplier supplier = new Supplier();
        supplier.setId(rs.getLong("id"));
        supplier.setName(rs.getString("name"));
        supplier.setContactPerson(rs.getString("contact_person"));
        supplier.setEmail(rs.getString("email"));
        supplier.setPhone(rs.getString("phone"));
        supplier.setCreatedAt(rs.getString("created_at"));
        return supplier;
    };

    public SupplierRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Supplier> findAll(Long restaurantId) {
        return jdbcTemplate.query(
                "SELECT * FROM restaurant_suppliers WHERE restaurant_id = ? ORDER BY name",
                rowMapper,
                restaurantId
        );
    }

    public Optional<Supplier> findById(Long restaurantId, Long id) {
        List<Supplier> results = jdbcTemplate.query(
                "SELECT * FROM restaurant_suppliers WHERE restaurant_id = ? AND id = ?",
                rowMapper,
                restaurantId,
                id
        );
        return results.stream().findFirst();
    }

    public Supplier save(Long restaurantId, Supplier supplier) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(
                    """
                    INSERT INTO restaurant_suppliers(restaurant_id, name, contact_person, email, phone)
                    VALUES (?, ?, ?, ?, ?)
                    """,
                    Statement.RETURN_GENERATED_KEYS
            );
            statement.setLong(1, restaurantId);
            statement.setString(2, supplier.getName());
            statement.setString(3, supplier.getContactPerson());
            statement.setString(4, supplier.getEmail());
            statement.setString(5, supplier.getPhone());
            return statement;
        }, keyHolder);

        supplier.setId(keyHolder.getKey().longValue());
        return supplier;
    }

    public int count(Long restaurantId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM restaurant_suppliers WHERE restaurant_id = ?",
                Integer.class,
                restaurantId
        );
        return count == null ? 0 : count;
    }
}
