package com.fhtw.shreddit.config;

import com.fhtw.shreddit.service.RabbitMQService;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;

@TestConfiguration
@Profile("test")
public class TestRabbitMQConfig {

    @Bean
    @Primary
    public ConnectionFactory connectionFactory() {
        return mock(ConnectionFactory.class);
    }

    @Bean
    @Primary
    public RabbitTemplate rabbitTemplate() {
        return mock(RabbitTemplate.class);
    }

    @Bean
    @Primary
    public RabbitMQService rabbitMQService() {
        RabbitMQService mockService = mock(RabbitMQService.class);
        // Configure the mock to do nothing when sendOcrRequest is called
        doNothing().when(mockService).sendOcrRequest(any());
        return mockService;
    }
}
