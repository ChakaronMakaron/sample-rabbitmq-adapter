package com.lemakhno.util;

import com.fasterxml.jackson.databind.ObjectMapper;

public class AppUtil {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public static String prettyPrint(Object obj) {
        try {
            return OBJECT_MAPPER
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public static String hexToAscii(String hexString) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < hexString.length(); i += 2) {
            result.append((char) Byte.parseByte(hexString.substring(i, i + 2), 16));
        }
        return result.toString();
    }
}
