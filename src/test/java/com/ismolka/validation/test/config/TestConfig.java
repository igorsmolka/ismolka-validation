package com.ismolka.validation.test.config;

import com.ismolka.validation.factory.TransactionManagerConstraintValidatorFactoryBean;
import liquibase.integration.spring.SpringLiquibase;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.TransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import javax.sql.DataSource;
import java.util.Locale;
import java.util.Objects;

@Configuration
@EnableTransactionManagement
@ComponentScan("com.ismolka.validation.test")
public class TestConfig {

    private static final String CHANGELOG_PATH = "classpath:liquibase/db.changelog-master.xml";


    @Bean
    public MessageSource validationMessageSource() {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasename("validation_error_messages");
        messageSource.setDefaultEncoding("UTF-8");
        messageSource.setDefaultLocale(Locale.getDefault());
        return messageSource;
    }

    @Bean
    public JpaVendorAdapter jpaVendorAdapter() {
        HibernateJpaVendorAdapter hibernateJpaVendorAdapter = new HibernateJpaVendorAdapter();
        hibernateJpaVendorAdapter.setGenerateDdl(false);
        hibernateJpaVendorAdapter.setShowSql(true);

        return hibernateJpaVendorAdapter;
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource) {
        LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();

        factory.setDataSource(dataSource);
        factory.setPackagesToScan(
                "com.ismolka.validation.test.model"
        );
        factory.setJpaVendorAdapter(jpaVendorAdapter());
        factory.setPersistenceUnitName("default");

        return factory;
    }

    @Bean
    public TransactionManager transactionManager(LocalContainerEntityManagerFactoryBean entityManagerFactory) {
        return new JpaTransactionManager(Objects.requireNonNull(entityManagerFactory.getObject()));
    }

    @Bean
    public SpringLiquibase springLiquibase(DataSource dataSource) {
        SpringLiquibase springLiquibase = new SpringLiquibase();
        springLiquibase.setChangeLog(CHANGELOG_PATH);
        springLiquibase.setDataSource(dataSource);

        return springLiquibase;
    }

    @Bean
    public TransactionManagerConstraintValidatorFactoryBean transactionManagerConstraintValidatorFactoryBean(AutowireCapableBeanFactory beanFactory, TransactionManager transactionManager) {
        TransactionManagerConstraintValidatorFactoryBean transactionManagerConstraintValidatorFactoryBean = new TransactionManagerConstraintValidatorFactoryBean(beanFactory);
        transactionManagerConstraintValidatorFactoryBean.setJpaTransactionManager((JpaTransactionManager) transactionManager);

        return transactionManagerConstraintValidatorFactoryBean;
    }

    @Bean
    public LocalValidatorFactoryBean validator(TransactionManagerConstraintValidatorFactoryBean transactionManagerConstraintValidatorFactoryBean) {
        LocalValidatorFactoryBean localValidatorFactoryBean = new LocalValidatorFactoryBean();
        localValidatorFactoryBean.setValidationMessageSource(validationMessageSource());
        localValidatorFactoryBean.setConstraintValidatorFactory(transactionManagerConstraintValidatorFactoryBean);

        return localValidatorFactoryBean;
    }
}
