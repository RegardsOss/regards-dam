/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.microservices.dam;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;

import fr.cnes.regards.framework.microservice.annotation.MicroserviceInfo;
import fr.cnes.regards.framework.security.utils.endpoint.IInstanceAdminAccessVoter;
import fr.cnes.regards.framework.security.utils.endpoint.IProjectAdminAccessVoter;
import fr.cnes.regards.framework.security.utils.endpoint.IRoleSysAccessVoter;

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
@EnableAsync
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

    /**
     *
     * Specific method authorization voter to accept systems roles for all endpoints.
     *
     * @return IRoleSysAccessVoter
     * @since 1.0-SNAPSHOT
     */
    @Bean
    public IRoleSysAccessVoter roleSysVoter() {
        return new AcceptRoleSysVoter();
    }

    /**
     *
     * Specific method authorization voter to accept instance admin user for all endpoints.
     *
     * @return IRoleSysAccessVoter
     * @since 1.0-SNAPSHOT
     */
    @Bean
    public IInstanceAdminAccessVoter instanceAdminVoter(
            @Value("${regards.accounts.root.user.login}") final String pInstanceUser) {
        return new AcceptInstanceAdminVoter(pInstanceUser);
    }

    @Bean
    public IProjectAdminAccessVoter projectAdminVoter() {
        return new AcceptProjectAdminVoter();
    }
}
// CHECKSTYLE:ON