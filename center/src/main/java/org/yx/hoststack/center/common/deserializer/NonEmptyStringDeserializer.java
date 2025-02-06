package org.yx.hoststack.center.common.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import org.yx.lib.utils.util.StringUtil;

import java.io.IOException;

public class NonEmptyStringDeserializer extends JsonDeserializer<String> {
    private final String defaultValue;

    public NonEmptyStringDeserializer(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    @Override
    public String deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String value = p.getValueAsString();
        return StringUtil.isBlank(value) ? defaultValue : value;
    }
}