package com.fhtw.ocrworker.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableRabbit
public class RabbitConfig {

    @Value("${rabbitmq.queue.ocr}")
    private String ocrQueueName;

    @Value("${rabbitmq.queue.genai}")
    private String genAiQueueName;

    @Value("${rabbitmq.queue.indexing}")
    private String indexingQueueName;

    @Bean
    public Queue ocrQueue() {
        return new Queue(ocrQueueName, true);
    }

    @Bean
    public Queue genAiQueue() {
        return new Queue(genAiQueueName, true);
    }

    @Bean
    public Queue indexingQueue() {
        return new Queue(indexingQueueName, true);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}
