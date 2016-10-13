/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.microservices.dam;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import fr.cnes.regards.microservices.core.annotation.MicroserviceInfo;

/**
 *
 * Start microservice dam
 *
 * @author msordi
 *
 */
@SpringBootApplication(scanBasePackages = { "fr.cnes.regards" })
@MicroserviceInfo(name = "dam", version = "1.0-SNAPSHOT")
public class Application {

    /**
     * Microservice starter method
     *
     * @param pArgs
     *            microservice start arguments
     */
    public static void main(final String[] pArgs) {
        SpringApplication.run(Application.class, pArgs); // NOSONAR
    }

}
