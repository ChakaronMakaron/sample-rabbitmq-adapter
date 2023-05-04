package com.lemakhno.listeners.processors;

import java.nio.charset.StandardCharsets;

import org.springframework.stereotype.Component;

import com.lemakhno.listeners.steps.SourceQueueProcessingSteps;
import com.lemakhno.models.Item;
import com.lemakhno.models.SourceQueueMessage;
import com.lemakhno.models.TargetExchangeMessage;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Component
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class SourceQueueMessageProcessor {

    SourceQueueProcessingSteps sourceQueueProcessingSteps;

    public void processMessage(byte[] payload, String messageId) {
        SourceQueueMessage message = sourceQueueProcessingSteps.parsePayload(new String(payload, StandardCharsets.UTF_8), messageId);
        String cardGuid = sourceQueueProcessingSteps.getCardGuid(message, messageId);
        Long transactionAmount = sourceQueueProcessingSteps.getTransactionAmount(message, messageId);
        Item item = sourceQueueProcessingSteps.getItem(messageId);
        Long operationRemainder = sourceQueueProcessingSteps.getOperationRemainder(message, messageId);

        TargetExchangeMessage targetExchangeMessage = TargetExchangeMessage.builder()
                .cardGuid(cardGuid)
                .trnAmount(transactionAmount)
                .sourceIBAN(item.getSourceIBAN())
                .destinationIBAN(item.getDestinationIBAN())
                .amount(operationRemainder)
                .messageId(messageId)
                .build();

        sourceQueueProcessingSteps.publishToTargetExchange(targetExchangeMessage, messageId);
    }
}
