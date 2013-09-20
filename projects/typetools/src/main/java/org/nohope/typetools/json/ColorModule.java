package org.nohope.typetools.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.awt.*;
import java.io.IOException;

/**
 * Date: 11/8/12
 * Time: 3:08 PM
 */
public class ColorModule extends SimpleModule {
    private static final long serialVersionUID = 1L;

    public ColorModule() {
        // first deserializers
        addDeserializer(Color.class, new ColorDeserializer());

        // then serializers:
        addSerializer(Color.class, new ColorSerializer());
    }

    private static class ColorSerializer extends StdSerializer<Color> {
        protected ColorSerializer() {
            super(Color.class);
        }

        @Override
        public void serialize(final Color value, final JsonGenerator jgen, final SerializerProvider provider) throws IOException {
            jgen.writeStartArray();
            for (final float v:value.getRGBComponents(null)) {
                jgen.writeNumber(v);
            }
            jgen.writeEndArray();
        }

        @Override
        public JsonNode getSchema(final SerializerProvider provider, final java.lang.reflect.Type typeHint)
        {
            return createSchemaNode("array", true);
        }

        @Override
        public void serializeWithType(final Color value, final JsonGenerator jgen, final SerializerProvider provider, final TypeSerializer typeSer) throws IOException {
            typeSer.writeTypePrefixForScalar(value, jgen);
            serialize(value, jgen, provider);
            typeSer.writeTypeSuffixForScalar(value, jgen);
        }
    }

    private static class ColorDeserializer extends StdScalarDeserializer<Color> {
        private static final long serialVersionUID = 1L;

        protected ColorDeserializer() {
            super(Color.class);
        }

        @Override
        public Color deserialize(final JsonParser jp, final DeserializationContext ctxt) throws IOException {
            if (jp.isExpectedStartArrayToken()) {
                jp.nextToken(); // VALUE_NUMBER_INT
                final float r = jp.getFloatValue();

                jp.nextToken(); // VALUE_NUMBER_INT
                final float g = jp.getFloatValue();

                jp.nextToken(); // VALUE_NUMBER_INT
                final float b = jp.getFloatValue();

                jp.nextToken(); // VALUE_NUMBER_INT
                final float a = jp.getFloatValue();

                if (jp.nextToken() != JsonToken.END_ARRAY) {
                    throw ctxt.wrongTokenException(jp, JsonToken.END_ARRAY, "after Color ints");
                }
                return new Color(r, g, b, a);
            }
            throw ctxt.wrongTokenException(jp, JsonToken.END_ARRAY, "expected for Color");
        }

        @Override
        public Object deserializeWithType(final JsonParser jp, final DeserializationContext ctxt, final TypeDeserializer typeDeserializer) throws IOException {
            return typeDeserializer.deserializeTypedFromAny(jp, ctxt);
        }
    }
}
