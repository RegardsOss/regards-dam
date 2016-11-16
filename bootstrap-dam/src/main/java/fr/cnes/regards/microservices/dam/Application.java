/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.microservices.dam;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import fr.cnes.regards.framework.microservice.annotation.MicroserviceInfo;

/**
 *
 * Spring boot application : scans all core and contrib modules
 *
 * @author msordi
 *
 */
// CHECKSTYLE:OFF
@SpringBootApplication(scanBasePackages = { "fr.cnes.regards.modules", "fr.cnes.regards.contrib" })
@MicroserviceInfo(name = "Data management", version = "1.0-SNAPSHOT")
public class Application { // NOSONAR

    /**
     * Microservice bootstrap method
     *
     * @param pArgs
     *            microservice bootstrap arguments
     */
    public static void main(final String[] pArgs) {
        SpringApplication.run(Application.class, pArgs); // NOSONAR
    }
}
// CHECKSTYLE:ON