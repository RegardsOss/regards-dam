/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.microservices.dam;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import fr.cnes.regards.microservices.core.annotation.MicroserviceInfo;
import springfox.documentation.builders.ApiInfoBuilder;

/**
 *
 * Start microservice dam
 *
 * @author msordi
 *
 */
@SpringBootApplication(scanBasePackages = { "fr.cnes.regards.modules", "fr.cnes.regards.microservices.core",
        "fr.cnes.regards.security.utils" })
@MicroserviceInfo(name = "dam", version = "1.0-SNAPSHOT")
public class Application {

    /**
     * Initialize swagger builder
     *
     * @return Swagger API builder
     */
    @Bean
    public ApiInfoBuilder apiInfoBuilder() {
        return new ApiInfoBuilder().title("dam API").description("API for dam REGARDS Microservice")
                .license("Apache License Version 2.0").version("1.0-SNAPSHOT");
    }

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
