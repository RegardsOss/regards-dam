/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.microservices.dam;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import fr.cnes.regards.microservices.core.information.MicroserviceInfo;
import springfox.documentation.builders.ApiInfoBuilder;

/**
 * 
 * Start microservice dam
 * @author TODO
 *
 */
@SpringBootApplication(scanBasePackages = { "fr.cnes.regards.modules", "fr.cnes.regards.microservices.core" })
@MicroserviceInfo(name = "dam", version = "1.0-SNAPSHOT")
public class Application {

    @Bean
    public ApiInfoBuilder apiInfoBuilder() {
        return new ApiInfoBuilder().title("dam API").description("API for dam REGARDS Microservice")
                .license("Apache License Version 2.0").version("1.0-SNAPSHOT");
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
