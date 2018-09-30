package com.reshigo.model.entity;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

/**
 * Created by dmitry103 on 25/06/17.
 */
public class CustomSerializer extends StdSerializer<byte[]> {

    public CustomSerializer() {
        super((Class<byte[]>) null);
    }

    public CustomSerializer(Class<byte[]> t) {
        super(t);
    }

    public CustomSerializer(JavaType type) {
        super(type);
    }

    public CustomSerializer(Class<?> t, boolean dummy) {
        super(t, dummy);
    }

    @Override
    public void serialize(byte[] bytes, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException, JsonGenerationException {
        jsonGenerator.writeString(new String(bytes));
    }
}