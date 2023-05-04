package com.lemakhno;

import static com.lemakhno.enums.MessageStatus.CONFIRM;
import static com.lemakhno.enums.MessageStatus.TO_DEAD_EXCHANGE;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import com.lemakhno.enums.MessageStatus;
import com.lemakhno.exceptions.StopMessageProcessingException;
import com.lemakhno.listeners.steps.SourceQueueProcessingSteps;
import com.lemakhno.models.SourceQueueMessage;
import com.lemakhno.properties.ConfigProperties;
import com.lemakhno.services.ItemsService;
import com.lemakhno.services.ItemsServiceAvailabilityMonitor;
import com.lemakhno.util.TestUtil;

public class Step3TransactionAmountTests {

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
    public void noTagTest() {
        SourceQueueMessage sourceQueueMessage = TestUtil.getSourceQueueMessage();
        sourceQueueMessage.setTransactionAmount(null);
        MessageStatus status = null;
        try {
            sourceQueueProcessingSteps.getTransactionAmount(sourceQueueMessage, "123");
        } catch (StopMessageProcessingException e) {
            status = e.getMessageStatus();
        }
        assertEquals(CONFIRM, status);
    }

    @Test
    public void withTagTest() {
        SourceQueueMessage sourceQueueMessage = TestUtil.getSourceQueueMessage();
        sourceQueueMessage.setTransactionAmount("000000010035");
        Long trnAmount = sourceQueueProcessingSteps.getTransactionAmount(sourceQueueMessage, "123");
        assertEquals(trnAmount, 10035);
    }

    @Test
    public void amountDoesNotStartWithZeroTest() {
        SourceQueueMessage sourceQueueMessage = TestUtil.getSourceQueueMessage();
        sourceQueueMessage.setTransactionAmount("111111110035");
        MessageStatus status = null;
        try {
            sourceQueueProcessingSteps.getTransactionAmount(sourceQueueMessage, "123");
        } catch (StopMessageProcessingException e) {
            status = e.getMessageStatus();
        }
        assertEquals(CONFIRM, status);
    }

    @Test
    public void invalidTagValueTest() {
        SourceQueueMessage sourceQueueMessage = TestUtil.getSourceQueueMessage();
        sourceQueueMessage.setTransactionAmount("jiu913131fd");
        MessageStatus status = null;
        try {
            sourceQueueProcessingSteps.getTransactionAmount(sourceQueueMessage, "123");
        } catch (StopMessageProcessingException e) {
            status = e.getMessageStatus();
        }
        assertEquals(TO_DEAD_EXCHANGE, status);
    }
}
