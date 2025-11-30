package com.fhtw.genaiworker;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@EnableRabbit
@SpringBootApplication
public class GenAiWorkerApplication {
    public static void main(String[] args) {
        SpringApplication.run(GenAiWorkerApplication.class, args);
    }
}
