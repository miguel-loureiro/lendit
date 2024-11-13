package com.ims;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@SpringBootApplication
public class ImsApplication {

	public static void main(String[] args) {
		ConfigurableApplicationContext context = SpringApplication.run(ImsApplication.class, args);

		// H2 database connection details
		String url = "jdbc:h2:mem:testdb";
		String user = "sa";
		String password = "";

		// Attempt to connect to the database
		try (Connection connection = DriverManager.getConnection(url, user, password)) {
			System.out.println("Connected to database!");

			// Optional: Execute a simple query to test the connection
			var statement = connection.createStatement();
			var resultSet = statement.executeQuery("SELECT 1");

			if (resultSet.next()) {
				System.out.println("Query executed successfully: " + resultSet.getInt(1));
			}
		} catch (SQLException e) {
			System.err.println("Connection failed: " + e.getMessage());
		}
    }
}
