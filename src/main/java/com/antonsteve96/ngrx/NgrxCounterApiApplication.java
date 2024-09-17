package com.antonsteve96.ngrx;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.nio.file.Paths;

@SpringBootApplication
public class NgrxCounterApiApplication {

	public static void main(String[] args) {
		Dotenv dotenv = Dotenv.configure()
				.directory(Paths.get("").toAbsolutePath().toString()) // Percorso della directory
				.filename("environment.env") // Nome del file
				.load();
		dotenv.entries().forEach(entry -> System.setProperty(entry.getKey(), entry.getValue()));


		// Avvio dell'applicazione
		SpringApplication.run(NgrxCounterApiApplication.class, args);
	}

}
