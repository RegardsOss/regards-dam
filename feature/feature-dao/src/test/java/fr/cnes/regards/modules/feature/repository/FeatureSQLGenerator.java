package fr.cnes.regards.modules.feature.repository;

import org.junit.Ignore;

import fr.cnes.regards.framework.jpa.multitenant.test.AbstractScriptGeneratorTest;

//Use following line to launch FLYWAY on public schema (comment it to use HBM2DDL)
//@TestPropertySource(properties = { "spring.jpa.properties.hibernate.default_schema=public",
//     "regards.jpa.multitenant.migrationTool=HBM2DDL" })
@Ignore
// @TestPropertySource(properties = { "spring.jpa.properties.hibernate.default_schema:feature" })
public class FeatureSQLGenerator extends AbstractScriptGeneratorTest {
}

