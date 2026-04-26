package com.example.foodmanagement.repository;

import com.example.foodmanagement.model.RestaurantAccount;
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
public class RestaurantAccountRepository {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<RestaurantAccount> rowMapper = (rs, rowNum) -> {
        RestaurantAccount account = new RestaurantAccount();
        account.setId(rs.getLong("id"));
        account.setRestaurantName(rs.getString("restaurant_name"));
        account.setOwnerName(rs.getString("owner_name"));
        account.setEmail(rs.getString("email"));
        account.setPasswordHash(rs.getString("password_hash"));
        account.setCreatedAt(rs.getString("created_at"));
        return account;
    };

    public RestaurantAccountRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<RestaurantAccount> findByEmail(String email) {
        List<RestaurantAccount> results = jdbcTemplate.query(
                "SELECT * FROM restaurant_accounts WHERE lower(email) = lower(?)",
                rowMapper,
                email
        );
        return results.stream().findFirst();
    }

    public RestaurantAccount save(RestaurantAccount account) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(
                    """
                    INSERT INTO restaurant_accounts(restaurant_name, owner_name, email, password_hash)
                    VALUES (?, ?, ?, ?)
                    """,
                    Statement.RETURN_GENERATED_KEYS
            );
            statement.setString(1, account.getRestaurantName());
            statement.setString(2, account.getOwnerName());
            statement.setString(3, account.getEmail());
            statement.setString(4, account.getPasswordHash());
            return statement;
        }, keyHolder);

        account.setId(keyHolder.getKey().longValue());
        return account;
    }
}
