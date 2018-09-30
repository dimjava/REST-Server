package com.reshigo.config;

import com.reshigo.controller.orders.OrdersMaturityTaskMaster;
import com.reshigo.service.scheduled.ScheduledTasks;
import org.apache.commons.dbcp2.BasicDataSource;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.orm.hibernate4.HibernateTransactionManager;
import org.springframework.orm.hibernate4.LocalSessionFactoryBean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Properties;

@Configuration
@EnableTransactionManagement
@EnableScheduling
@EnableAsync
@ComponentScan(value = {"com.reshigo.dao.**", "com.reshigo.service.**"} )
public class DBConfig {

    Logger logger = LoggerFactory.getLogger(DBConfig.class);

    @Bean
    Properties dbProperties() throws IOException {
        Properties properties = new Properties();

        InputStream is = getClass().getClassLoader().getResourceAsStream("database.properties");
        properties.load(is);

        return properties;
    }


    @Bean
    public Properties commonProperties() throws IOException {
        Properties properties = new Properties();

        InputStream is = getClass().getClassLoader().getResourceAsStream("common.properties");
        properties.load(is);

        return properties;
    }

    @Bean @Autowired
    public BasicDataSource dataSource(@Qualifier(value = "dbProperties") Properties properties) throws SQLException {
        BasicDataSource bs = new BasicDataSource();

        String prefix = "release.";

        if (properties.getProperty("test").equals("1")) {
            prefix = "debug.";
            logger.debug("Using debug database. URL: {}", properties.getProperty(prefix + "url"));
        } else {
            logger.debug("Using release database. URL: {}", properties.getProperty(prefix + "url"));
        }

        bs.setUrl(properties.getProperty(prefix + "url"));
        bs.setUsername(properties.getProperty(prefix + "name"));
        bs.setPassword(properties.getProperty(prefix + "password"));

        bs.setValidationQuery("select 1");
        bs.setDriver(new com.microsoft.sqlserver.jdbc.SQLServerDriver());

        return bs;
    }

    @Bean
    public LocalSessionFactoryBean sessionFactory(DataSource dataSource) {
        LocalSessionFactoryBean sfb = new LocalSessionFactoryBean();

        sfb.setDataSource(dataSource);
        sfb.setPackagesToScan("com.reshigo.model.entity.**");

        Properties properties = new Properties();

        if (System.getProperty("debug") == null || System.getProperty("debug").equals("true")) {
            properties.setProperty("hibernate.show_sql", "false");
        }

        properties.setProperty("hibernate.dialect", "org.hibernate.dialect.SQLServer2008Dialect");

        properties.setProperty("hibernate.hbm2ddl.auto", "update");
        sfb.setHibernateProperties(properties);

        return sfb;
    }

    @Bean
    @Autowired
    public HibernateTransactionManager hibernateTransactionManager(DataSource dataSource, SessionFactory sessionFactory) {
        HibernateTransactionManager tm = new HibernateTransactionManager();
        tm.setDataSource(dataSource);
        tm.setSessionFactory(sessionFactory);

        return tm;
    }

    @Bean @Autowired
    @Scope("prototype")
    public OrdersMaturityTaskMaster ordersMaturityTaskMaster(ScheduledTasks st) {
        return new OrdersMaturityTaskMaster(st);
    }
}
