package com.lemakhno.listeners.steps;

import static com.lemakhno.enums.MessageStatus.CONFIRM;
import static com.lemakhno.enums.MessageStatus.NO_OP;
import static com.lemakhno.enums.MessageStatus.TO_DEAD_EXCHANGE;
import static java.util.Objects.isNull;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNumeric;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientResponseException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.lemakhno.exceptions.StopMessageProcessingException;
import com.lemakhno.models.Item;
import com.lemakhno.models.SourceQueueMessage;
import com.lemakhno.models.TargetExchangeMessage;
import com.lemakhno.services.ItemsService;
import com.lemakhno.services.ItemsServiceAvailabilityMonitor;
import com.lemakhno.util.AppUtil;
import com.lemakhno.util.XmlUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class SourceQueueProcessingSteps {

    private final String STATUS_ACTIVE = "active";
    private final String KEYWORD = "rounding";
    private final String C = "C";

    private final ItemsServiceAvailabilityMonitor itemsServiceAvailabilityMonitor;
    private final ItemsService itemsService;
    private final RabbitTemplate targetExchangePublishRabbitTemplate;

    public SourceQueueProcessingSteps(ItemsServiceAvailabilityMonitor itemsServiceAvailabilityMonitor, ItemsService itemsService,
                                        RabbitTemplate targetExchangePublishRabbitTemplate) {
        this.itemsServiceAvailabilityMonitor = itemsServiceAvailabilityMonitor;
        this.itemsService = itemsService;
        this.targetExchangePublishRabbitTemplate = targetExchangePublishRabbitTemplate;
    }

    public SourceQueueMessage parsePayload(String message, String messageId) {
        log.info("messageId={} Got new message: {}", messageId, message);
        SourceQueueMessage sourceQueueMessage;
        try {
            sourceQueueMessage = XmlUtils.unmarshall(message, SourceQueueMessage.class);
        } catch (JsonProcessingException e) {
            log.info("messageId={} Failed to parse payload - {}", messageId, e.getMessage());
            throw new StopMessageProcessingException(TO_DEAD_EXCHANGE);
        }
        return sourceQueueMessage;
    }

    public String getCardGuid(SourceQueueMessage message, String messageId) {
        log.info("messageId={} Checking cardGuid", messageId);
        if (isBlank(message.getCardGuid())) {
            log.info("messageId={} Missing cardGuid", messageId);
            throw new StopMessageProcessingException(CONFIRM);
        }
        return message.getCardGuid();
    }

    public Long getTransactionAmount(SourceQueueMessage message, String messageId) {
        log.info("messageId={} Checking transaction sum", messageId);
        String transactionAmount = message.getTransactionAmount();

        if (isBlank(transactionAmount)) {
            log.info("messageId={} Transaction amount = 0", messageId);
            throw new StopMessageProcessingException(CONFIRM);
        }

        transactionAmount = transactionAmount.trim();

        if (!isNumeric(transactionAmount)) {
            log.info("messageId={} Invalid transaction amount - {}", messageId, transactionAmount);
            throw new StopMessageProcessingException(TO_DEAD_EXCHANGE);
        }

        try {
            return Long.parseLong(transactionAmount);
        } catch (NumberFormatException e) {
            log.info("messageId={} Failed to convert transaction amount to number - {}", messageId, transactionAmount);
            throw new StopMessageProcessingException(TO_DEAD_EXCHANGE);
        }
    }

    public Item getItem(String messageId) {
        try {

            ResponseEntity<List<Item>> response = itemsService.getItems(STATUS_ACTIVE);
            Item item = findSuitableItem(Optional.ofNullable(response.getBody()).orElse(Collections.emptyList()));

            if (isNull(item)) {
                log.info("messageId={} No proper item found", messageId);
                throw new StopMessageProcessingException(CONFIRM);
            }

            boolean isAnyMandatoryFieldEmpty = isBlank(item.getSourceIBAN())
                    || isBlank(item.getDestinationIBAN())
                    || isNull(item.getAmount())
                    || isNull(item.getContragentId())
                    || isBlank(item.getCurrencyCode());

            if (isAnyMandatoryFieldEmpty || item.getAmount() <= 0) {
                log.info("messageId={} Item is not valid", messageId);
                throw new StopMessageProcessingException(TO_DEAD_EXCHANGE);
            }

            return item;

        } catch (RestClientResponseException e) {
            log.info("messageId={} Client or server error while getting items - {}", messageId, e.getMessage());
            itemsServiceAvailabilityMonitor.startMonitor();
            throw new StopMessageProcessingException(NO_OP);
        }
    }

    public Long getOperationRemainder(SourceQueueMessage sourceQueueMessage, String messageId) {
        log.info("messageId={} Calculating operation remainder", messageId);
        String operationData = sourceQueueMessage.getOperationData();
        try {

            if (isBlank(operationData)) return null;
            operationData = operationData.trim();
            String remainderType = getRemainderType(operationData);
            log.info("messageId={} Remainder type: {}", messageId, remainderType);

            Long operationRemainder = getRemainderValue(operationData);
            log.info("messageId={} Operation remainder: {}", messageId, operationRemainder);

            if (C.equals(remainderType) && operationRemainder > 0) {
                return operationRemainder;
            }

            return null;

        } catch (NumberFormatException e) {
            log.info("messageId={} Cound not parse operation remainder to number", messageId);
            throw new StopMessageProcessingException(TO_DEAD_EXCHANGE);
        } catch (IndexOutOfBoundsException e) {
            log.info("messageId={} Tag 'operationData' of SourceQueueMessage is not of proper size", messageId);
            throw new StopMessageProcessingException(TO_DEAD_EXCHANGE);
        }
    }

    public void publishToTargetExchange(TargetExchangeMessage targetExchangeMessage, String messageId) {
        try {
            log.info("messageId={} Publishing message to targetExchange: {}", messageId, AppUtil.prettyPrint(targetExchangeMessage));
            targetExchangePublishRabbitTemplate.convertAndSend(targetExchangeMessage);
            log.info("messageId={} Successfully published to target exchange", messageId);
            throw new StopMessageProcessingException(CONFIRM);
        } catch (AmqpException e) {
            log.info("messageId={} Error while publishing to target exhcange: {}", messageId, e.getMessage());
            throw new StopMessageProcessingException(TO_DEAD_EXCHANGE);
        }
    }

    private String getRemainderType(String operationData) {
        return String.valueOf(operationData.charAt(7));
    }

    private Long getRemainderValue(String operationData) {
        return Long.parseLong(operationData.substring(8, 20));
    }

    private Item findSuitableItem(List<Item> items) {
        return items.stream()
                .filter(item -> KEYWORD.equals(item.getRule()))
                .findFirst()
                .orElse(null);
    }
}
