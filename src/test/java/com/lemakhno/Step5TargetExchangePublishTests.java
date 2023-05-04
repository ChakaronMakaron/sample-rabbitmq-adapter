package com.lemakhno;

import static com.lemakhno.enums.MessageStatus.CONFIRM;
import static com.lemakhno.enums.MessageStatus.TO_DEAD_EXCHANGE;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import com.lemakhno.enums.MessageStatus;
import com.lemakhno.exceptions.StopMessageProcessingException;
import com.lemakhno.listeners.steps.SourceQueueProcessingSteps;
import com.lemakhno.models.TargetExchangeMessage;
import com.lemakhno.services.ItemsService;
import com.lemakhno.services.ItemsServiceAvailabilityMonitor;

public class Step5TargetExchangePublishTests {

    @Test
    public void publishFailureTest() {

        RabbitTemplate rabbitTemplate = Mockito.mock(RabbitTemplate.class);
        Mockito.doThrow(AmqpException.class).when(rabbitTemplate).convertAndSend(Mockito.any());
        ItemsServiceAvailabilityMonitor itemsServiceAvailabilityMonitor = Mockito.mock(ItemsServiceAvailabilityMonitor.class);
        ItemsService itemsService = Mockito.mock(ItemsService.class);
        SourceQueueProcessingSteps sourceQueueProcessingSteps = new SourceQueueProcessingSteps(itemsServiceAvailabilityMonitor, itemsService, rabbitTemplate);

        MessageStatus status = null;
        try {
            sourceQueueProcessingSteps.publishToTargetExchange(new TargetExchangeMessage(), "123");
        } catch (StopMessageProcessingException e) {
            status = e.getMessageStatus();
        }
        assertEquals(TO_DEAD_EXCHANGE, status);
    }

    @Test
    public void publishSuccessTest() {

        RabbitTemplate rabbitTemplate = Mockito.mock(RabbitTemplate.class);
        ItemsServiceAvailabilityMonitor itemsServiceAvailabilityMonitor = Mockito.mock(ItemsServiceAvailabilityMonitor.class);
        ItemsService itemsService = Mockito.mock(ItemsService.class);
        SourceQueueProcessingSteps sourceQueueProcessingSteps = new SourceQueueProcessingSteps(itemsServiceAvailabilityMonitor, itemsService, rabbitTemplate);

        MessageStatus status = null;
        try {
            sourceQueueProcessingSteps.publishToTargetExchange(new TargetExchangeMessage(), "123");
        } catch (StopMessageProcessingException e) {
            status = e.getMessageStatus();
        }
        assertEquals(CONFIRM, status);
    }
}
