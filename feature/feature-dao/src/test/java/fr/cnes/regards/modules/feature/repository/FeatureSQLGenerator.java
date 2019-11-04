package fr.cnes.regards.modules.feature.repository;

import org.junit.Ignore;
import org.springframework.test.context.TestPropertySource;

import fr.cnes.regards.framework.jpa.multitenant.test.AbstractScriptGeneratorTest;

//Use following line to launch FLYWAY on public schema (comment it to use HBM2DDL)
@TestPropertySource(properties = { "spring.jpa.properties.hibernate.default_schema=public",
        "regards.jpa.multitenant.migrationTool=FLYWAYDB" })
@Ignore
public class FeatureSQLGenerator extends AbstractScriptGeneratorTest {
}
