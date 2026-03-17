package org.example.internal_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;

@EnableRetry
@SpringBootApplication
public class InternalApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(InternalApiApplication.class, args);
    }

}
