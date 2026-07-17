package com.fruitlink;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * FruitLink Enterprise ERP – Backend
 * Java 21 (Virtual Threads) + Spring Boot 3
 * Prepared by: MK Web Tech
 */
@SpringBootApplication
@EnableScheduling
@EnableAsync
@EnableCaching
public class FruitlinkBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(FruitlinkBackendApplication.class, args);
    }

}
