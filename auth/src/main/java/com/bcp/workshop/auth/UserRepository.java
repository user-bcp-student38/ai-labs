package com.bcp.workshop.auth;

import io.quarkus.elytron.security.common.BcryptUtil;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Persistencia de usuarios del workshop. Las contraseñas se guardan como hash
 * BCrypt (nunca en texto plano) y toda consulta usa {@link PreparedStatement}
 * con parámetros, nunca concatenación de strings.
 */
@ApplicationScoped
public class UserRepository {
    private static final Logger LOG = Logger.getLogger(UserRepository.class);
    private static final String DB_URL = System.getenv().getOrDefault("AUTH_DB_URL", "jdbc:sqlite:./data/workshop.db");

    @PostConstruct
    void initialize() {
        try {
            Files.createDirectories(Path.of("data"));
            try (Connection connection = DriverManager.getConnection(DB_URL)) {
                connection.createStatement().executeUpdate(
                        "CREATE TABLE IF NOT EXISTS legacy_users ("
                                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                                + "username TEXT UNIQUE, "
                                + "password_hash TEXT NOT NULL, "
                                + "display_name TEXT NOT NULL, "
                                + "role TEXT NOT NULL)");
                seedUser(connection, "student", "Workshop123!", "Workshop Student", "STUDENT");
                seedUser(connection, "trainer", "Trainer123!", "Workshop Trainer", "TRAINER");
            }
        } catch (SQLException | java.io.IOException error) {
            LOG.error("No se pudo inicializar la base de datos de usuarios", error);
        }
    }

    private void seedUser(Connection connection, String username, String plainPassword, String displayName, String role) throws SQLException {
        String sql = "INSERT OR IGNORE INTO legacy_users(username,password_hash,display_name,role) VALUES (?,?,?,?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            statement.setString(2, BcryptUtil.bcryptHash(plainPassword));
            statement.setString(3, displayName);
            statement.setString(4, role);
            statement.executeUpdate();
        }
    }

    public UserRecord findByUsername(String username) {
        String sql = "SELECT id, username, password_hash, display_name, role FROM legacy_users WHERE username = ?";
        try (Connection connection = DriverManager.getConnection(DB_URL);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return new UserRecord(
                            result.getLong("id"),
                            result.getString("username"),
                            result.getString("password_hash"),
                            result.getString("display_name"),
                            result.getString("role"));
                }
            }
        } catch (SQLException error) {
            LOG.error("No se pudo consultar el usuario " + username, error);
        }
        return null;
    }
}
