package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PgApplication {

    public static void main(String[] args) {
        SpringApplication.run(PgApplication.class, args);
        System.out.println("Nam PG");
    }
}
