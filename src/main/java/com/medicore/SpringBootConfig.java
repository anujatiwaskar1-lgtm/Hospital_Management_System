package com.medicore;

import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * ✅ Spring Boot Configuration Class
 *
 * @SpringBootApplication enables:
 *   - @ComponentScan  → finds all @Service, @Repository, @Component classes
 *   - @EnableAutoConfiguration → auto-configures JPA, DataSource, etc.
 *   - @Configuration → marks this as a Spring config class
 *
 * NOTE: There is NO main() here.
 * Spring is started programmatically from MediCoreApp (JavaFX entry point).
 * This avoids having two main() methods and keeps JavaFX in control.
 */
@SpringBootApplication
public class SpringBootConfig {
    // Spring will scan com.medicore.** for all beans
    // Beans found: PatientService, BedService, PatientRepository, BedRepository
}
