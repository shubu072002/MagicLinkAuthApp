package com.magicauth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MagicAuthApplication {
    public static void main(String[] args) {
        SpringApplication.run(MagicAuthApplication.class, args);
    }
}
