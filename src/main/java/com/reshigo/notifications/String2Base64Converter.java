package com.reshigo.notifications;

import javax.persistence.AttributeConverter;

/**
 * Created by dmitry103 on 29/06/17.
 */
public class String2Base64Converter implements AttributeConverter<String, String> {
    @Override
    public String convertToDatabaseColumn(String s) {
        if (s == null) {
            return null;
        }

        return org.apache.commons.codec.binary.Base64.encodeBase64String(s.getBytes());
    }

    @Override
    public String convertToEntityAttribute(String s) {
        if (s == null) {
            return null;
        }

        return new String(org.apache.commons.codec.binary.Base64.decodeBase64(s));
    }
}
