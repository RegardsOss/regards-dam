/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.datasources.utils;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import fr.cnes.regards.framework.multitenant.IRuntimeTenantResolver;
import fr.cnes.regards.framework.multitenant.test.SingleRuntimeTenantResolver;

/**
 *
 * Class PostgreDataSourcePluginTestConfiguration
 *
 * Test Configuration class
 *
 * @author Christophe Mertz
 */
@Configuration
@ComponentScan(basePackages = { "fr.cnes.regards.modules.datasources" })
@PropertySource("classpath:datasource-test.properties")
@EnableJpaRepositories(basePackages = { "fr.cnes.regards.modules.datasources.utils" })
@EnableTransactionManagement
public class PostgreDataSourcePluginTestConfiguration {

    /**
     * The JDBC PostgreSQL driver
     */
    private static final String POSTGRESQL_JDBC_DRIVER = "org.postgresql.Driver";

    @Value("${postgresql.datasource.host}")
    private String dbHost;

    @Value("${postgresql.datasource.port}")
    private String dbPort;

    @Value("${postgresql.datasource.name}")
    private String dbName;

    @Value("${postgresql.datasource.username}")
    private String dbUser;

    @Value("${postgresql.datasource.password}")
    private String dbPassword;

    /**
     *
     * Create the {@link DataSource}
     *
     * @return the {@link DataSource} created
     */
    @Bean
    public DataSource dataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(POSTGRESQL_JDBC_DRIVER);
        dataSource.setUrl(buildUrl());
        dataSource.setUsername(dbUser);
        dataSource.setPassword(dbPassword);
        return dataSource;
    }

    /**
     * Create an {@link EntityManagerFactory}
     *
     * @return the {@link EntityManagerFactory} created
     */
    @Bean
    public EntityManagerFactory entityManagerFactory() {

        final HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        vendorAdapter.setGenerateDdl(true);

        final LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
        factory.setJpaVendorAdapter(vendorAdapter);
        factory.setPackagesToScan("fr.cnes.regards.modules.datasources.utils");
        factory.setDataSource(dataSource());
        factory.afterPropertiesSet();

        return factory.getObject();
    }

    /**
     *
     * Create transaction manager
     *
     * @return
     * @since 1.0-SNAPSHOT
     */
    @Bean
    public PlatformTransactionManager transactionManager() {

        final JpaTransactionManager txManager = new JpaTransactionManager();
        txManager.setEntityManagerFactory(entityManagerFactory());
        return txManager;
    }

    @Bean
    public IRuntimeTenantResolver runtimeTenantResolver() {
        return new SingleRuntimeTenantResolver(null);
    }

    private String buildUrl() {
        return "jdbc:postgresql://" + dbHost + ":" + dbPort + "/" + dbName;
    }

}
