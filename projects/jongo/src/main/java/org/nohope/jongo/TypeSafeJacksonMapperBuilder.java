package org.nohope.jongo;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdKeyDeserializer;
import com.fasterxml.jackson.databind.introspect.VisibilityChecker;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdKeySerializer;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.translate.CharSequenceTranslator;
import org.apache.commons.lang3.text.translate.LookupTranslator;
import org.jongo.Mapper;
import org.jongo.marshall.jackson.JacksonMapper;
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
 * @since 2013-10-14 18:59
 */
public class TypeSafeJacksonMapperBuilder extends JacksonMapper.Builder {
    private static final Logger LOG = LoggerFactory.getLogger(TypeSafeJacksonMapperBuilder.class);
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

    private TypeSafeJacksonMapperBuilder(@Nonnull final ObjectMapper mapper) {
        super(mapper);
    }

    @Nonnull
    public static Mapper buildMapper() {
        return new TypeSafeJacksonMapperBuilder(createPreConfiguredMapper()).build();
    }

    @Nonnull
    public static Mapper buildMapper(@Nonnull final ObjectMapper mapper) {
        return new TypeSafeJacksonMapperBuilder(mapper).build();
    }

    @Nonnull
    public static ObjectMapper createPreConfiguredMapper() {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JodaModule());
        mapper.registerModule(new ColorModule());

        mapper.configure(FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(AUTO_DETECT_GETTERS, false);
        mapper.configure(AUTO_DETECT_SETTERS, false);
        mapper.configure(FAIL_ON_EMPTY_BEANS, false);
        mapper.setSerializationInclusion(NON_NULL);
        mapper.setVisibilityChecker(VisibilityChecker.Std.defaultInstance().withFieldVisibility(ANY));

        mapper.enableDefaultTypingAsProperty(ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.Id.CLASS.getDefaultPropertyName());

        final SimpleModule module = new SimpleModule("jongo", Version.unknownVersion());
        module.addKeySerializer(Object.class, ComplexKeySerializer.S_OBJECT);
        module.addKeyDeserializer(String.class, ComplexKeyDeserializer.S_OBJECT);
        module.addKeyDeserializer(Object.class, ComplexKeyDeserializer.S_OBJECT);

        mapper.registerModule(module);
        return mapper;
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
                jgen.writeFieldName("#" + value.toString());
            } else {
                LOG.warn("Complex key serializer now will call json mapper recursively for value {}@{}; stack was {}"
                        , value
                        , value.getClass().getCanonicalName()
                        , StringUtils.join(Thread.currentThread().getStackTrace(), "\n ... "));
                jgen.writeFieldName("@" + escape(KEY_MAPPER.writeValueAsString(value)));
            }
        }
    }

    private static final class ComplexKeyDeserializer extends StdKeyDeserializer {
        private static final long serialVersionUID = 1L;
        static final ComplexKeyDeserializer S_OBJECT = new ComplexKeyDeserializer(Object.class);

        private ComplexKeyDeserializer(final Class<?> nominalType) {
            super(nominalType);
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
