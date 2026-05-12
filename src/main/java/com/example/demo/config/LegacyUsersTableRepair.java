package com.example.demo.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Aligns legacy {@code users} tables with JPA: drops orphan columns left from older Spring Security
 * or Hibernate naming ({@code password}, {@code username}) when {@code password_hash} / {@code email}
 * are the columns this app uses.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class LegacyUsersTableRepair implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(LegacyUsersTableRepair.class);

    private final DataSource dataSource;

    public LegacyUsersTableRepair(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        try (Connection c = dataSource.getConnection()) {
            repairPasswordColumn(c);
            repairUsernameColumn(c);
        }
    }

    private void repairPasswordColumn(Connection c) throws Exception {
        int legacy = columnCount(c, "password");
        if (legacy == 0) {
            return;
        }
        int hash = columnCount(c, "password_hash");
        try (Statement st = c.createStatement()) {
            if (hash > 0) {
                st.executeUpdate("ALTER TABLE users DROP COLUMN `password`");
                log.info("Dropped legacy users.password (password_hash is in use).");
            } else {
                st.executeUpdate(
                        "ALTER TABLE users CHANGE COLUMN `password` password_hash VARCHAR(255) NOT NULL");
                log.info("Renamed users.password to password_hash.");
            }
        }
    }

    private void repairUsernameColumn(Connection c) throws Exception {
        if (columnCount(c, "username") == 0) {
            return;
        }
        if (columnCount(c, "email") == 0) {
            log.warn("users.username exists but users.email is missing; leaving username column unchanged.");
            return;
        }
        try (Statement st = c.createStatement()) {
            st.executeUpdate("ALTER TABLE users DROP COLUMN `username`");
            log.info("Dropped legacy users.username (this app uses email for login).");
        }
    }

    private static int columnCount(Connection c, String columnName) throws Exception {
        String sql = """
                SELECT COUNT(*) FROM information_schema.COLUMNS
                WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'users' AND COLUMN_NAME = ?
                """;
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, columnName);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }
}
