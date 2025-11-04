package com.acme.bank.payments;

import io.r2dbc.postgresql.PostgresqlConnectionConfiguration;
import io.r2dbc.postgresql.PostgresqlConnectionFactory;
import io.r2dbc.spi.ConnectionFactory;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@TestConfiguration
public class EmbeddedR2dbcConfiguration {

    @Bean
    @Primary
    public ConnectionFactory r2dbcConnectionFactory(DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            String jdbcUrl = connection.getMetaData().getURL();
            String username = connection.getMetaData().getUserName();

            // Parse JDBC URL: jdbc:postgresql://host:port/database
            String cleanUrl = jdbcUrl.replace("jdbc:postgresql://", "");
            String[] parts = cleanUrl.split("/");
            String[] hostPort = parts[0].split(":");

            String host = hostPort[0];
            int port = Integer.parseInt(hostPort[1]);
            String database = parts[1].split("\\?")[0];

            System.out.println("Configuring R2DBC with embedded PostgreSQL:");
            System.out.println("  Host: " + host);
            System.out.println("  Port: " + port);
            System.out.println("  Database: " + database);
            System.out.println("  Username: " + username);

            return new PostgresqlConnectionFactory(
                    PostgresqlConnectionConfiguration.builder()
                            .host(host)
                            .port(port)
                            .database(database)
                            .username(username)
                            .password("docker")
                            .build()
            );
        } catch (SQLException e) {
            throw new RuntimeException("Failed to configure R2DBC from embedded database", e);
        }
    }
}