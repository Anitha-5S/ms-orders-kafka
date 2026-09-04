package com.csquare.lc.ms.orders.kafka.configuration;


import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.HashMap;

@Configuration
@EnableJpaRepositories(
        basePackages = {"com.csquare.lc.ms.orders.lib.repos"},
        entityManagerFactoryRef = "postgresEntityManager",
        transactionManagerRef = "postgresTransactionManager")
public class PostgresDBConfig {

    @Autowired
    private Environment env;
    @Value("${postgres.datasource.hikari.poolName}")
    private String poolName;
    @Value("${postgres.datasource.hikari.minimumIdle}")
    private int minimumIdle;
    @Value("${postgres.datasource.hikari.idleTimeout}")
    private long idleTimeout;
    @Value("${postgres.datasource.hikari.maxLifetime}")
    private long maxLifetime;
    @Value("${postgres.datasource.hikari.maximumPoolSize}")
    private int maximumPoolSize;
    @Value("${postgres.datasource.hikari.connectionTimeout}")
    private long connectionTimeout;

    @Bean
    @Primary
    public DataSource postgreDataSource() {

        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(env.getProperty("postgres.datasource.url"));
        hikariConfig.setUsername(env.getProperty("postgres.datasource.username"));
        hikariConfig.setPassword(env.getProperty("postgres.datasource.password"));

        hikariConfig.setMaximumPoolSize(maximumPoolSize);
        hikariConfig.setConnectionTestQuery("SELECT 1");
        hikariConfig.setPoolName(poolName);
        hikariConfig.setIdleTimeout(idleTimeout);
        hikariConfig.setMaxLifetime(maxLifetime);
        hikariConfig.setMinimumIdle(minimumIdle);
        hikariConfig.setConnectionTimeout(connectionTimeout);

        return new HikariDataSource(hikariConfig);
    }

    @Bean(name = "postgresEntityManager")
    @Primary
    public LocalContainerEntityManagerFactoryBean postgresEntityManager() {

        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(postgreDataSource());
        em.setPackagesToScan("com.csquare.lc.ms.orders.lib.model");
        em.setPersistenceUnitName("postgresql");

        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);
        HashMap<String, Object> properties = new HashMap<>();
        properties.put("hibernate.hbm2ddl.auto", env.getProperty("hibernate.hbm2ddl.auto"));
        properties.put("hibernate.dialect", env.getProperty("postgres.hibernate.dialect"));
        properties.put("hibernate.jdbc.lob.non_contextual_creation", env.getProperty("hibernate.jdbc.lob.non_contextual_creation"));
        properties.put("hibernate.enable_lazy_load_no_trans", true);
        properties.put("hibernate.event.merge.entity_copy_observer", "allow");
        em.setJpaPropertyMap(properties);
        return em;
    }


    @Bean
    @Primary
    public PlatformTransactionManager postgresTransactionManager() {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(postgresEntityManager().getObject());
        return transactionManager;
    }
}