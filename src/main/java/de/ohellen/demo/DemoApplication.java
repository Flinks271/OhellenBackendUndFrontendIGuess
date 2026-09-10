package de.ohellen.demo;

import java.io.IOException;
import java.util.Properties;

import org.flywaydb.core.Flyway;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DemoApplication {

	public static void main(String[] args) {
		migrateDatabase();
		SpringApplication.run(DemoApplication.class, args);
	}

	public static void migrateDatabase() {
        System.out.println("Migrating database...");

        try {
            Properties props = new Properties();
            props.load(DemoApplication.class.getClassLoader()
                .getResourceAsStream("application.properties"));

            String dbUrl = props.getProperty("spring.datasource.url");
            String dbUsername = props.getProperty("spring.datasource.username");
            String dbPassword = props.getProperty("spring.datasource.password");

            System.out.println(dbUrl + dbUsername + dbPassword);

            Flyway flyway = Flyway.configure()
                .dataSource(dbUrl, dbUsername, dbPassword)
                .locations("classpath:db/migration")
                .load();

            flyway.migrate();
        } catch (IOException e) {
            System.err.println("Failed to load properties: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

}
