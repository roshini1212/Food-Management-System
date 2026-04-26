package com.example.foodmanagement.config;

import jakarta.annotation.PostConstruct;
import javax.sql.DataSource;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

@Component
public class DatabaseInitializer {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseInitializer.class);
    private final DataSource dataSource;

    public DatabaseInitializer(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @PostConstruct
    public void initialize() throws IOException, SQLException {
        String schemaScript = new String(
                new ClassPathResource("schema.sql").getInputStream().readAllBytes(),
                StandardCharsets.UTF_8
        );

        String[] statements = Arrays.stream(schemaScript.split("-- statement-break"))
                .map(String::trim)
                .filter(statement -> !statement.isBlank())
                .toArray(String[]::new);

        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            for (String sql : statements) {
                statement.execute(sql);
            }
        }

        logger.info("Database schema and triggers initialized");
    }
}
