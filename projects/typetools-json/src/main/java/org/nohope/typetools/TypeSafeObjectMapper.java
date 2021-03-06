package org.nohope.typetools;

import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectMapper.DefaultTyping;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdKeyDeserializer;
import com.fasterxml.jackson.databind.introspect.VisibilityChecker.Std;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdKeySerializer;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.translate.CharSequenceTranslator;
import org.apache.commons.lang3.text.translate.LookupTranslator;
import org.nohope.logging.Logger;
import org.nohope.logging.LoggerFactory;
import org.nohope.typetools.json.ColorModule;

import javax.annotation.Nonnull;
import java.io.IOException;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static com.fasterxml.jackson.databind.MapperFeature.AUTO_DETECT_GETTERS;
import static com.fasterxml.jackson.databind.MapperFeature.AUTO_DETECT_SETTERS;
import static com.fasterxml.jackson.databind.SerializationFeature.FAIL_ON_EMPTY_BEANS;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">Ketoth Xupack</a>
 * @since 2014-03-14 21:41
 */
public class TypeSafeObjectMapper {
    private static final Logger LOG = LoggerFactory.getLogger(TypeSafeObjectMapper.class);
    private static final ObjectMapper KEY_MAPPER = createPreConfiguredMapper();

    private static final CharSequenceTranslator ESCAPE_AT =
            new LookupTranslator(
                    new String[][]{
                            {"@", "\\@"},
                            {"#", "\\#"},
                            {".", "_"},
                            {"_", "\\_"},
                    });

    private static final CharSequenceTranslator UNESCAPE_AT =
            new LookupTranslator(
                    new String[][]{
                            {"\\@", "@"},
                            {"\\#", "#"},
                            {"_", "."},
                            {"\\_", "_"},
                    });

    private TypeSafeObjectMapper() {
    }

    /**
     * @param mapper object mapper to configure
     * @param override {@code false} if no modification should be made with given mapper
     * @return reconfigured object mapper
     */
    @Nonnull
    public static ObjectMapper configureMapper(final ObjectMapper mapper, final boolean override) {
        final ObjectMapper copy = override ? mapper : mapper.copy();
        copy.registerModule(new JodaModule());
        copy.registerModule(new ColorModule());

        copy.configure(FAIL_ON_UNKNOWN_PROPERTIES, false);
        copy.configure(AUTO_DETECT_GETTERS, false);
        copy.configure(AUTO_DETECT_SETTERS, false);
        copy.configure(FAIL_ON_EMPTY_BEANS, false);
        copy.setSerializationInclusion(NON_NULL);
        copy.setVisibilityChecker(Std.defaultInstance().withFieldVisibility(ANY));

        copy.enableDefaultTypingAsProperty(DefaultTyping.NON_FINAL,
                Id.CLASS.getDefaultPropertyName());

        final SimpleModule module = new SimpleModule("typesafe-jongo-module", Version.unknownVersion());
        module.addKeySerializer(Object.class, ComplexKeySerializer.S_OBJECT);
        module.addKeyDeserializer(String.class, ComplexKeyDeserializer.S_OBJECT);
        module.addKeyDeserializer(Object.class, ComplexKeyDeserializer.S_OBJECT);

        copy.registerModule(module);
        return copy;
    }

    @Nonnull
    public static ObjectMapper createPreConfiguredMapper() {
        return configureMapper(new ObjectMapper(), true);
    }

    public static String escape(final String name) {
        return ESCAPE_AT.translate(name);
    }

    public static String unescape(final String name) {
        return UNESCAPE_AT.translate(name);
    }

    private static final class ComplexKeySerializer extends StdKeySerializer {
        static final ComplexKeySerializer S_OBJECT = new ComplexKeySerializer();

        private ComplexKeySerializer() {
            super();
        }

        @Override
        public void serialize(final Object value, final JsonGenerator jgen,
                              final SerializerProvider provider)
                throws IOException {
            //System.err.println("Saving "+value);
            if (value instanceof String) {
                final String out = escape(value.toString());
                jgen.writeFieldName(out);
            } else if (value instanceof Integer) {
                jgen.writeFieldName('#' + value.toString());
            } else {
                LOG.warn("Complex key serializer now will call json mapper recursively for value {}@{}; stack was {}"
                        , value
                        , value.getClass().getCanonicalName()
                        , StringUtils.join(Thread.currentThread().getStackTrace(), "\n ... "));
                jgen.writeFieldName('@' + escape(KEY_MAPPER.writeValueAsString(value)));
            }
        }
    }

    private static final class ComplexKeyDeserializer extends StdKeyDeserializer {
        private static final long serialVersionUID = 1L;
        static final ComplexKeyDeserializer S_OBJECT = new ComplexKeyDeserializer(Object.class);

        private ComplexKeyDeserializer(final Class<?> nominalType) {
            super(-1, nominalType);
        }

        @Override
        //CHECKSTYLE:OFF
        public Object _parse(final String key, final DeserializationContext ctxt) throws IOException {
            //CHECKSTYLE:ON
            //System.out.println("Restoring "+key);
            if (key.startsWith("#")) {
                return Integer.parseInt(key.substring(1));
            } if (key.startsWith("@")) {
                LOG.warn("Complex key deserializer now will call json mapper recursively for key {}; stack was {}"
                        , key
                        , StringUtils.join(Thread.currentThread().getStackTrace(), "\n ... "));
                final String marshalled = unescape(key.substring(1));
                return KEY_MAPPER.readValue(marshalled, Object.class);
            } else {
                return unescape(key);
            }
        }
    }
}
