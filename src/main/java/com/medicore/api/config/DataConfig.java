package com.medicore.api.config;

import org.springframework.context.annotation.Configuration;

/**
 * DataConfig - Data initialization configuration.
 *
 * Automatic data seeding has been disabled intentionally.
 * Tables are created/updated by Hibernate (spring.jpa.hibernate.ddl-auto=update).
 * To seed data, run schema.sql manually via MySQL Workbench or the MySQL CLI:
 *   mysql -u root -p medicore < schema.sql
 */
@Configuration
public class DataConfig {
    // No automatic data loading. All seeding is done manually via MySQL client.
}
