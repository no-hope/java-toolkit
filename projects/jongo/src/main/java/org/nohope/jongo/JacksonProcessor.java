/*
 * Copyright (C) 2011 Benoit GUEROUT <bguerout at gmail dot com> and Yves AMSELLEM <amsellem dot yves at gmail dot com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.nohope.jongo;

import com.fasterxml.jackson.core.JsonGenerationException;
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
import org.jongo.marshall.Marshaller;
import org.jongo.marshall.MarshallingException;
import org.jongo.marshall.Unmarshaller;
import org.nohope.logging.Logger;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static com.fasterxml.jackson.databind.MapperFeature.AUTO_DETECT_GETTERS;
import static com.fasterxml.jackson.databind.MapperFeature.AUTO_DETECT_SETTERS;

public class JacksonProcessor implements Unmarshaller, Marshaller {
    private static final Logger LOG = org.nohope.logging.LoggerFactory.getLogger(JacksonProcessor.class);

    private final ObjectMapper mapper;

    private JacksonProcessor(final ObjectMapper mapper) {
        this.mapper = mapper;
    }

    public JacksonProcessor() {
        this(createPreConfiguredMapper());
    }

    @Override
    public <T> T unmarshall(final String json, final Class<T> clazz) throws MarshallingException {
        try {
            return mapper.readValue(json, clazz);
        } catch (Exception e) {
            final String message = String.format("Unable to unmarshall from json: %s to %s", json, clazz);
            throw new MarshallingException(message, e);
        }
    }

    @Override
    public <T> String marshall(final T obj) throws MarshallingException {
        try {
            final Writer writer = new StringWriter();
            mapper.writeValue(writer, obj);
            return writer.toString();
        } catch (Exception e) {
            final String message = String.format("Unable to marshall json from: %s", obj);
            throw new MarshallingException(message, e);
        }
    }

    private static ObjectMapper createPreConfiguredMapper() {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JodaModule());
        mapper.configure(FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(AUTO_DETECT_GETTERS, false);
        mapper.configure(AUTO_DETECT_SETTERS, false);
        mapper.setSerializationInclusion(NON_NULL);
        mapper.setVisibilityChecker(VisibilityChecker.Std.defaultInstance().withFieldVisibility(ANY));

        mapper.enableDefaultTypingAsProperty(ObjectMapper.DefaultTyping.NON_FINAL, "@class");

        final SimpleModule module = new SimpleModule("jongo", Version.unknownVersion());
        module.addKeySerializer(Object.class, ComplexKeySerializer.sObject);
        module.addKeyDeserializer(Object.class, ComplexKeyDeserializer.sObject);

        //addBSONTypeSerializers(module);

        mapper.registerModule(module);
        return mapper;
    }

    private static final CharSequenceTranslator ESCAPE_AT =
            new LookupTranslator(
                    new String[][]{
                            {"@", "\\@"},
                            {"#", "\\#"},
                    });
    private static final CharSequenceTranslator UNESCAPE_AT =
            new LookupTranslator(
                    new String[][]{
                            {"\\@", "@"},
                            {"\\#", "#"},
                    });

    private static final ObjectMapper keyMapper = createPreConfiguredMapper();

    static final class ComplexKeySerializer extends StdKeySerializer {
        static final ComplexKeySerializer sObject = new ComplexKeySerializer();

        private ComplexKeySerializer() {
            super();
        }

        @Override
        public void serialize(final Object value, final JsonGenerator jgen, final SerializerProvider provider) throws IOException, JsonGenerationException {
            if (value.getClass().isAssignableFrom(String.class)) {
                final String out = ESCAPE_AT.translate(value.toString());
                jgen.writeFieldName(out);
            } else if (value instanceof Integer) {
                jgen.writeFieldName("#" + value.toString());
            } else {
                LOG.warn("Complex key serializer now will call json mapper recursively for value {}@{}; stack was {}"
                        , value
                        , value.getClass().getCanonicalName()
                        , StringUtils.join(Thread.currentThread().getStackTrace(), "\n ... "));
                jgen.writeFieldName("@" + ESCAPE_AT.translate(keyMapper.writeValueAsString(value)));
            }

        }
    }

    private static class ComplexKeyDeserializer extends StdKeyDeserializer {
        static final ComplexKeyDeserializer sObject = new ComplexKeyDeserializer(Object.class);

        private ComplexKeyDeserializer(final Class<?> nominalType) {
            super(nominalType);
        }

        @Override
        public Object _parse(final String key, final DeserializationContext ctxt) throws IOException {
            if (key.startsWith("#")) {
                return Integer.parseInt(key.substring(1));
            } if (key.startsWith("@")) {
                LOG.warn("Complex key deserializer now will call json mapper recursively for key {}; stack was {}"
                        , key
                        , StringUtils.join(Thread.currentThread().getStackTrace(), "\n ... "));
                final String marshalled = UNESCAPE_AT.translate(key.substring(1));
                return keyMapper.readValue(marshalled, Object.class);
            } else {
                return UNESCAPE_AT.translate(key);
            }
        }
    }
}
