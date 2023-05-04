package com.lemakhno.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

public class XmlUtils {

    private static final XmlMapper XML_MAPPER = new XmlMapper();

    public static <T> T unmarshall(String str, Class<T> cls) throws JsonProcessingException {
        return XML_MAPPER.readValue(str, cls);
    }
}
