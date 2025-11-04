package com.acme.bank.payments.infrastructure;

import io.r2dbc.postgresql.PostgresqlConnectionConfiguration;
import io.r2dbc.postgresql.PostgresqlConnectionFactory;
import io.r2dbc.spi.ConnectionFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.flyway.FlywayProperties;
import org.springframework.boot.autoconfigure.r2dbc.R2dbcProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@TestConfiguration
public class PostgresContainerConfiguration {
    @Bean
    public PostgreSQLContainer<?> postgresContainer() {
        var container = new PostgreSQLContainer<>("postgres:13.2")
                .withUsername("admin")
                .withPassword("password")
                .withExposedPorts(5432)
                .waitingFor(Wait.forListeningPort());

        container.start();

        var jdbcUrl = container.getJdbcUrl();
        var r2dbcUrl = jdbcUrl.replace("jdbc:postgresql://", "r2dbc:postgresql://");

        System.setProperty("spring.r2dbc.url", r2dbcUrl);
        System.setProperty("spring.r2dbc.username", container.getUsername());
        System.setProperty("spring.r2dbc.password", container.getPassword());

        System.setProperty("flyway.user", container.getUsername());
        System.setProperty("flyway.password", container.getPassword());
        System.setProperty("flyway.schemas", "public");
        System.setProperty("flyway.url", jdbcUrl);
        System.setProperty("flyway.locations", "classpath:db/migrations");

        return container;
    }

    @Bean
    public DataSource flywayDataSource(FlywayProperties flywayProperties, R2dbcProperties r2dbcProperties) {
        var jdbcUrl = flywayProperties.getUrl();
        var r2dbcUrl = r2dbcProperties.getUrl();

        if (jdbcUrl == null || jdbcUrl.startsWith("r2dbc:")) {
            jdbcUrl = r2dbcUrl.replace("r2dbc:", "jdbc:");
        }

        return DataSourceBuilder.create()
                .url(jdbcUrl)
                .username(r2dbcProperties.getUsername())
                .password(r2dbcProperties.getPassword())
                .build();
    }

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
