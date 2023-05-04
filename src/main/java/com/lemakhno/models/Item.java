package com.lemakhno.models;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Item {
    Long itemId;
    String sourceIBAN;
    String destinationIBAN;
    Long amount;
    String currencyCode;
    Long contragentId;
    String status;
    String rule;
    Long account;
}
