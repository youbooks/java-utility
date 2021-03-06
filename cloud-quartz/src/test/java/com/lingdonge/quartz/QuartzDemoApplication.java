package com.lingdonge.quartz;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;


@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class})
public class QuartzDemoApplication implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(QuartzDemoApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(QuartzDemoApplication.class, args);

        logger.info("Start QuartzDemoApplication");

    }

    @Override
    public void run(String... strings) {


    }

}
