package com.lemakhno;

import com.lemakhno.enums.MessageStatus;
import com.lemakhno.exceptions.StopMessageProcessingException;
import com.lemakhno.listeners.steps.SourceQueueProcessingSteps;
import com.lemakhno.models.SourceQueueMessage;
import com.lemakhno.properties.ConfigProperties;
import com.lemakhno.services.ItemsService;
import com.lemakhno.services.ItemsServiceAvailabilityMonitor;
import com.lemakhno.util.TestUtil;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import static com.lemakhno.enums.MessageStatus.CONFIRM;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class Step2CardGuidTests {

    @Mock
    private ConfigProperties configProperties;
    @Mock
    private ItemsServiceAvailabilityMonitor itemsServiceAvailabilityMonitor;
    @Mock
    private ItemsService itemsService;
    @Mock
    private RabbitTemplate rabbitTemplate;
    @InjectMocks
    private SourceQueueProcessingSteps sourceQueueProcessingSteps;

    @BeforeEach
    public void beforeEach() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void noCardGuidTest() {
        SourceQueueMessage sourceQueueMessage = TestUtil.getSourceQueueMessage();
        sourceQueueMessage.setCardGuid(null);
        MessageStatus status = null;
        try {
            sourceQueueProcessingSteps.getCardGuid(sourceQueueMessage, "123");
        } catch (StopMessageProcessingException e) {
            status = e.getMessageStatus();
        }
        assertEquals(CONFIRM, status);
    }

    @Test
    public void withCardGuidTest() {
        SourceQueueMessage sourceQueueMessage = TestUtil.getSourceQueueMessage();
        sourceQueueMessage.setCardGuid("0000000000000000000000000000");
        String cardGuid = sourceQueueProcessingSteps.getCardGuid(sourceQueueMessage, "123");
        assertEquals(sourceQueueMessage.getCardGuid(), cardGuid);
    }
}
