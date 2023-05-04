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

import static com.lemakhno.enums.MessageStatus.TO_DEAD_EXCHANGE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class Step4OperationRemainderTests {

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
    public void emptyTagTest() {
        SourceQueueMessage sourceQueueMessage = TestUtil.getSourceQueueMessage();
        sourceQueueMessage.setOperationData("   ");
        Long operationRemainder = sourceQueueProcessingSteps.getOperationRemainder(sourceQueueMessage, "123");
        assertNull(operationRemainder);
    }

    @Test
    public void invalidRemainderTypeTest() {
        SourceQueueMessage sourceQueueMessage = TestUtil.getSourceQueueMessage();
        sourceQueueMessage.setOperationData("00000000000000000000000000");
        Long operationRemainder = sourceQueueProcessingSteps.getOperationRemainder(sourceQueueMessage, "123");
        assertNull(operationRemainder);
    }

    @Test
    public void tagValueTooShortTest() {
        SourceQueueMessage sourceQueueMessage = TestUtil.getSourceQueueMessage();
        sourceQueueMessage.setOperationData("2002980A00965");
        MessageStatus status = null;
        try {
            sourceQueueProcessingSteps.getOperationRemainder(sourceQueueMessage, "123");
        } catch (StopMessageProcessingException e) {
            status = e.getMessageStatus();
        }
        assertEquals(TO_DEAD_EXCHANGE, status);
    }

    @Test
    public void zeroOperationRemainderTest() {
        SourceQueueMessage sourceQueueMessage = TestUtil.getSourceQueueMessage();
        sourceQueueMessage.setOperationData("2002980A000000000000");
        Long operationRemainder = sourceQueueProcessingSteps.getOperationRemainder(sourceQueueMessage, "123");
        assertNull(operationRemainder);
    }
}
