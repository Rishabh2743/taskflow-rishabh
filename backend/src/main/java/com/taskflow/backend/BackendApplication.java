package com.taskflow.backend;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.TimeZone;

@SpringBootApplication
public class BackendApplication {

    private static final Logger log = LoggerFactory.getLogger(BackendApplication.class);

    public static void main(String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

        Runtime.getRuntime().addShutdownHook(new Thread(() ->
            log.info("Shutting down TaskFlow API...")
        ));

        SpringApplication.run(BackendApplication.class, args);
        log.info("TaskFlow API started successfully");
    }
}