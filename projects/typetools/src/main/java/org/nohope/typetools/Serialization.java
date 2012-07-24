package org.nohope.typetools;

import org.codehaus.jackson.map.ObjectMapper;
import org.nohope.logging.Logger;
import org.nohope.logging.LoggerFactory;

import java.io.IOException;

import static org.codehaus.jackson.map.SerializationConfig.Feature.INDENT_OUTPUT;
import static org.codehaus.jackson.map.SerializationConfig.Feature.WRITE_DATES_AS_TIMESTAMPS;
import static org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion.NON_EMPTY;

/**
 * Date: 23.01.12
 * Time: 13:43
 */
public final class Serialization {
    private static final Logger LOG = LoggerFactory.getLogger(Serialization.class);

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
            LOG.debug(e, "Unable to serialize object of class {}",
                    obj == null ? null : obj.getClass());
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
            LOG.debug(e, "Unable to serialize object of class {}",
                    obj == null ? null : obj.getClass());
            return onErrorMessage;
        }
    }
}
