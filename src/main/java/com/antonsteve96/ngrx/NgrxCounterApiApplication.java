package com.antonsteve96.ngrx;

import com.antonsteve96.ngrx.role.Role;
import com.antonsteve96.ngrx.role.RoleRepository;
import io.github.cdimascio.dotenv.Dotenv;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.nio.file.Paths;
import java.time.LocalDateTime;

@Slf4j
@SpringBootApplication
@EnableJpaAuditing(auditorAwareRef = "applicationAuditAware")
public class NgrxCounterApiApplication {

	/**
	 * Può essere eliminata, solitamente le proprietà sono inserite direttamente negli application.properties
	 * I file dotenv si impiegano per le applicazioni di front-end, dato che per le app Spring Boot vi
	 * sono differenti modalità per la configurazione delle proprietà dell'applicazione in sicurezza
	 */
	public static void main(String[] args) {
		Dotenv dotenv = Dotenv.configure()
				.directory(Paths.get("").toAbsolutePath().toString()) // Percorso della directory
				.filename("environment.env") // Nome del file
				.load();
		dotenv.entries().forEach(entry -> System.setProperty(entry.getKey(), entry.getValue()));


		// Avvio dell'applicazione
		SpringApplication.run(NgrxCounterApiApplication.class, args);
	}

	@Bean
	public CommandLineRunner runner(RoleRepository roleRepository) {
		return args -> {
			if (roleRepository.findByName("USER").isEmpty()) {
				roleRepository.save(Role.builder().createdDate(LocalDateTime.now()).name("USER").build());
				log.info("Role 'USER' was not found, so it was created.");
			} else {
				log.info("Role 'USER' already exists.");
			}
		};
	}

}
