package com.lemakhno.util;

import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;

import org.springframework.util.StreamUtils;

import com.lemakhno.models.Item;
import com.lemakhno.models.SourceQueueMessage;

public class TestUtil {

    public static String readFile(String path) {
        try (FileInputStream fis = new FileInputStream(path)) {
            return StreamUtils.copyToString(fis, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static SourceQueueMessage getSourceQueueMessage() {
        SourceQueueMessage sourceQueueMessage = new SourceQueueMessage();
        sourceQueueMessage.setType("1234567");
        sourceQueueMessage.setOperationData("12345678901234567890");
        sourceQueueMessage.setCardGuid("ABCDEFGHIJABCDEFGHIJABCDEFGHIJAB");
        sourceQueueMessage.setHexData("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF" +
                "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF" +
                "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF" +
                "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF");
        return sourceQueueMessage;
    }

    public static Item getItem() {
        Item item = new Item();
        item.setItemId(1L);
        item.setSourceIBAN("UA123456789012345678901234567");
        item.setDestinationIBAN("UA123456789012345678901234567");
        item.setAmount(10000L);
        item.setCurrencyCode("UAH");
        item.setContragentId(2L);
        item.setStatus("active");
        item.setRule("rule");
        item.setAccount(12345678901234L);
        return item;
    }
}
