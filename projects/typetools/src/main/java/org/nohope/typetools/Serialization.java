package org.nohope.util.typetools;

import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;

import static org.codehaus.jackson.map.SerializationConfig.Feature.INDENT_OUTPUT;
import static org.codehaus.jackson.map.SerializationConfig.Feature.WRITE_DATES_AS_TIMESTAMPS;
import static org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion.NON_EMPTY;

/**
 * Date: 23.01.12
 * Time: 13:43
 */
public final class Serialization {
    private Serialization() {
    }

    public static String serializeForPrettyPrint(final Object obj,
                                                 final String onErrorMessage) {
        try {
            final ObjectMapper mapper = new ObjectMapper();
            mapper.configure(INDENT_OUTPUT, true);
            mapper.configure(WRITE_DATES_AS_TIMESTAMPS, false);
            mapper.setSerializationInclusion(NON_EMPTY);

            return mapper.writeValueAsString(obj);
        } catch (IOException e) {
            return onErrorMessage;
        }
    }

    public static String serialize(final Object obj,
                                   final String onErrorMessage) {
        try {
            final ObjectMapper mapper = new ObjectMapper();
            mapper.setSerializationInclusion(NON_EMPTY);
            return mapper.writeValueAsString(obj);
        } catch (IOException e) {
            return onErrorMessage;
        }
    }
}
