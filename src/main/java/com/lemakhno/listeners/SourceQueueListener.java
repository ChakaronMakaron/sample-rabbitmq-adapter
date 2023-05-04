package com.lemakhno.listeners;

import static com.lemakhno.config.RabbitConfig.LISTENER_NAME;

import java.io.IOException;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import com.lemakhno.enums.MessageStatus;
import com.lemakhno.exceptions.StopMessageProcessingException;
import com.lemakhno.listeners.processors.SourceQueueMessageProcessor;
import com.rabbitmq.client.Channel;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class SourceQueueListener {

    SourceQueueMessageProcessor sourceQueueMessageProcessor;

    @RabbitListener(queues = "#{configProperties.getRabbitSourceQueue()}", id = LISTENER_NAME)
    public void handleMessage(Message message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
        String messageId = RandomStringUtils.randomAlphanumeric(20);
        try {
            sourceQueueMessageProcessor.processMessage(message.getBody(), messageId);
        } catch (StopMessageProcessingException e) {
            resolveStopStatus(e.getMessageStatus(), messageId, channel, tag);
        } catch (Exception e) {
            log.info("messageId={} Unexpected error: {}", messageId, e.toString());
        }
    }

    private void resolveStopStatus(MessageStatus messageStatus, String messageId, Channel channel, long tag) throws IOException {
        if (messageStatus.equals(MessageStatus.CONFIRM)) {
            log.info("messageId={} Successfully processed", messageId);
            channel.basicAck(tag, false);
        }
        else if (messageStatus.equals(MessageStatus.TO_DEAD_EXCHANGE)) {
            log.info("messageId={} Sent to dead exchange", messageId);
            channel.basicNack(tag, false, false);
        }
    }
}
