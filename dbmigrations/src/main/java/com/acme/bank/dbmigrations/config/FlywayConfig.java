package com.acme.bank.dbmigrations.config;

import org.springframework.context.annotation.Configuration;

@Configuration
//@EnableConfigurationProperties({ R2dbcProperties.class, FlywayProperties.class })
public class FlywayConfig {
//    @Bean
//    public Flyway flyway(FlywayProperties flywayProperties, R2dbcProperties r2dbcProperties) {
//        Flyway flyway = Flyway.configure()
//                .dataSource(
//                        flywayProperties.getUrl(),
//                        r2dbcProperties.getUsername(),
//                        r2dbcProperties.getPassword()
//                )
//                .locations(flywayProperties.getLocations().toArray(String[]::new))
//                .baselineOnMigrate(true)
//                .load();
//
//        flyway.migrate();
//
//        return flyway;
//    }
}
