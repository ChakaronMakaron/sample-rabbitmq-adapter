package com.lemakhno;

import static com.lemakhno.enums.MessageStatus.TO_DEAD_EXCHANGE;
import static com.lemakhno.util.TestUtil.readFile;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import com.lemakhno.enums.MessageStatus;
import com.lemakhno.exceptions.StopMessageProcessingException;
import com.lemakhno.listeners.steps.SourceQueueProcessingSteps;
import com.lemakhno.services.ItemsService;
import com.lemakhno.services.ItemsServiceAvailabilityMonitor;

public class Step1ParsePayloadTests {

    @Test
    public void malformedQueueMessageTest() {
        ItemsServiceAvailabilityMonitor itemsServiceAvailabilityMonitor = Mockito.mock(ItemsServiceAvailabilityMonitor.class);
        ItemsService itemsService = Mockito.mock(ItemsService.class);
        RabbitTemplate rabbitTemplate = Mockito.mock(RabbitTemplate.class);
        SourceQueueProcessingSteps sourceQueueProcessingSteps = new SourceQueueProcessingSteps(itemsServiceAvailabilityMonitor, itemsService, rabbitTemplate);

        MessageStatus status = null;
        try {
            sourceQueueProcessingSteps.parsePayload(readFile("src/test/resources/malformedQueueMessage.xml"), "123");
        } catch (StopMessageProcessingException e) {
            status = e.getMessageStatus();
        }
        assertEquals(TO_DEAD_EXCHANGE, status);
    }
}
