package com.lemakhno.models;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TargetExchangeMessage {
    String messageId;
    String destinationIBAN;
    String sourceIBAN;
    Long amount;
    Long trnAmount;
    String cardGuid;
}
