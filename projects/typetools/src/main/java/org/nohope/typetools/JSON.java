package org.nohope.typetools;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import org.nohope.logging.Logger;
import org.nohope.logging.LoggerFactory;

import java.io.IOException;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;
import static com.fasterxml.jackson.databind.SerializationFeature.*;

/**
 * Date: 23.01.12
 * Time: 13:43
 */
public final class JSON {
    private static final Logger LOG = LoggerFactory.getLogger(JSON.class);

    private JSON() {
    }

    private static final ObjectMapper prettyMapper = new ObjectMapper();
    private static final ObjectMapper usualMapper = new ObjectMapper();

    static {
        prettyMapper.registerModule(new JodaModule());
        prettyMapper.configure(INDENT_OUTPUT, true);
        prettyMapper.configure(WRITE_DATES_AS_TIMESTAMPS, false);
        prettyMapper.configure(FAIL_ON_EMPTY_BEANS, false);
        prettyMapper.setSerializationInclusion(NON_EMPTY);

        usualMapper.registerModule(new JodaModule());
        usualMapper.setSerializationInclusion(NON_EMPTY);
    }

    public static String jsonifyPretty(final Object obj,
                                       final String onErrorMessage) {
        try {
            return prettyMapper.writeValueAsString(obj);
        } catch (IOException e) {
            LOG.error(e, "Unable to jsonify object of class {}",
                    obj == null ? null : obj.getClass().getCanonicalName());
            return onErrorMessage;
        }
    }

    public static String jsonifyPretty(final Object obj) {
        return jsonifyPretty(obj, defaultErrorMessage(obj));
    }

    public static String jsonify(final Object obj,
                                 final String onErrorMessage) {
        try {
            return usualMapper.writeValueAsString(obj);
        } catch (IOException e) {
            LOG.error(e, "Unable to jsonify object of class {}",
                    obj == null ? null : obj.getClass());
            return onErrorMessage;
        }
    }

    public static String jsonify(final Object obj) {
        return jsonify(obj, defaultErrorMessage(obj));
    }

    private static String defaultErrorMessage(final Object obj) {
        if (null != obj) {
            return "<? " + obj.getClass().getCanonicalName() + "/>";
        }
        return "<?null />";
    }

    public static <T> T restoreTyped(final Object source, final Class<T> clazz) throws IOException {
        return usualMapper.readValue(usualMapper.writeValueAsBytes(source), clazz);
    }
}
