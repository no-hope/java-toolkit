package org.nohope.typetools;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.VisibilityChecker;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import org.nohope.logging.Logger;
import org.nohope.logging.LoggerFactory;
import org.nohope.typetools.json.ColorModule;

import java.io.IOException;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
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
        prettyMapper.registerModule(new ColorModule());
        prettyMapper.configure(INDENT_OUTPUT, true);
        prettyMapper.configure(WRITE_DATES_AS_TIMESTAMPS, false);
        prettyMapper.configure(FAIL_ON_EMPTY_BEANS, false);
        prettyMapper.setSerializationInclusion(NON_EMPTY);
        prettyMapper.enableDefaultTypingAsProperty(ObjectMapper.DefaultTyping.NON_FINAL, "@class");
        prettyMapper.setVisibilityChecker(VisibilityChecker.Std.defaultInstance().withFieldVisibility(ANY));

        usualMapper.registerModule(new JodaModule());
        usualMapper.registerModule(new ColorModule());
        usualMapper.setSerializationInclusion(NON_EMPTY);
        usualMapper.enableDefaultTypingAsProperty(ObjectMapper.DefaultTyping.NON_FINAL, "@class");
        usualMapper.setVisibilityChecker(VisibilityChecker.Std.defaultInstance().withFieldVisibility(ANY));
    }

    public static String pretty(final Object obj) {
        return pretty(obj, defaultErrorMessage(obj));
    }

    public static String jsonify(final Object obj) {
        return jsonify(obj, defaultErrorMessage(obj));
    }

    /**
     * Serializes given object with Jackson, then deserializes into class required.
     * Not so fast, so shouldn't be used for deep copying.
     *
     * @param source
     * @param clazz
     * @param <T>
     * @return
     * @throws IOException
     */
    public static <T> T copyAs(final Object source, final Class<T> clazz) throws IOException {
        final byte [] marshalled = usualMapper.writeValueAsBytes(source);
        return usualMapper.readValue(marshalled, clazz);
    }

    private static String jsonify(final Object obj,
                                 final String onErrorMessage) {
        return jsonifyWith(usualMapper, obj, onErrorMessage);
    }

    private static String pretty(final Object obj,
                         final String onErrorMessage) {
        return jsonifyWith(prettyMapper, obj, onErrorMessage);
    }

    private static String jsonifyWith(final ObjectMapper mapper, final Object obj, final String onErrorMessage) {
        try {
            return mapper.writeValueAsString(obj);
        } catch (Throwable e) {
            LOG.error(e, "Unable to jsonify object of class {}",
                    obj == null ? null : obj.getClass());
            return onErrorMessage;
        }
    }

    private static String defaultErrorMessage(final Object obj) {
        if (null != obj) {
            return "<? " + obj.getClass().getCanonicalName() + "/>";
        }
        return "<?null />";
    }
}
