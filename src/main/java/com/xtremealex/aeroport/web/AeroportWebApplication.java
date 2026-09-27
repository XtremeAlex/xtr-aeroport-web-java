package com.xtremealex.aeroport.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Frontend sottile della suite Aeroport.
 * Non accede ad alcun database: consuma esclusivamente le API di xtr-aeroport-api.
 */
@SpringBootApplication
public class AeroportWebApplication {

    public static void main(String[] args) {
        SpringApplication.run(AeroportWebApplication.class, args);
    }
}
