package com.shop.catalog;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class CatalogApplication {

    private static final Logger log = LoggerFactory.getLogger(CatalogApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(CatalogApplication.class, args);
    }

    @Bean
    CommandLineRunner startupLogger() {
        return args -> log.info("shop-catalog service started and ready to serve requests");
    }
}
