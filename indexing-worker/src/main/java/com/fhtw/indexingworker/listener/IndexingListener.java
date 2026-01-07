package com.fhtw.indexingworker.listener;

import com.fhtw.indexingworker.dto.IndexingRequestDto;
import com.fhtw.indexingworker.service.IndexingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
public class IndexingListener {
    private static final Logger log = LoggerFactory.getLogger(IndexingListener.class);

    private final IndexingService indexingService;

    public IndexingListener(IndexingService indexingService) {
        this.indexingService = indexingService;
    }

    @RabbitListener(queues = "${rabbitmq.queue.indexing}")
    public void onMessage(@Payload IndexingRequestDto requestDto) {
        if (requestDto == null) {
            log.warn("INDEX: received null message");
            return;
        }
        try {
            indexingService.indexDocument(requestDto);
        } catch (Exception e) {
            log.error("INDEX: failed to handle indexing request {}", requestDto, e);
        }
    }
}
