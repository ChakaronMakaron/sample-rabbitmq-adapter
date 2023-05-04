package com.lemakhno.models;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SourceQueueMessage {
    String type;
    String transactionAmount;
    String operationData;
    String cardGuid;
    String hexData;
}
