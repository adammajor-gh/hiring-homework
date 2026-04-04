package com.adam.hiring.artemis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

@Service
public class ArtemisService {
    private final JmsTemplate jmsTemplate;
    private static final Logger logger = LoggerFactory.getLogger(ArtemisService.class);

    public ArtemisService(JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
    }

    public void send(String destinationQueue, String message) {
        logger.debug("Sending message to queue: {}", destinationQueue);
        jmsTemplate.convertAndSend(destinationQueue, message);
        logger.info("Message sent to {}: {}", destinationQueue, message);
    }
}
