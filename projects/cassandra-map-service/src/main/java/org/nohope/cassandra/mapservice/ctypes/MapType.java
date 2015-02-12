package org.nohope.cassandra.mapservice.ctypes;

import com.datastax.driver.core.Row;
import org.nohope.cassandra.mapservice.columns.CColumn;
import org.nohope.reflection.TypeReference;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 */
class MapType<CK, CV, JK, JV> implements Converter<Map<CK, CV>, Map<JK, JV>> {
    private final Converter<CK, JK> keyConverter;
    private final Converter<CV, JV> valueConverter;

    MapType(final Converter<CK, JK> keyConverter,
            final Converter<CV, JV> valueConverter) {
        this.keyConverter = keyConverter;
        this.valueConverter = valueConverter;
    }

    @Override
    public Map<CK, CV> asCassandraValue(final Map<JK, JV> value) {
        final Map<CK, CV> map = new LinkedHashMap<>();
        for (final Map.Entry<JK, JV> e : value.entrySet()) {
            map.put(keyConverter.asCassandraValue(e.getKey()),
                    valueConverter.asCassandraValue(e.getValue()));
        }
        return map;
    }

    @Override
    public Map<JK, JV> asJavaValue(final Map<CK, CV> value) {
        final Map<JK, JV> map = new LinkedHashMap<>();
        for (final Map.Entry<CK, CV> e : value.entrySet()) {
            map.put(keyConverter.asJavaValue(e.getKey()),
                    valueConverter.asJavaValue(e.getValue()));
        }
        return map;
    }

    @Override
    public Map<JK, JV> readValue(final Row result, final CColumn<Map<JK, JV>, Map<CK, CV>> name) {
        final Map<CK, CV> map = result.getMap(name.getName(),
                keyConverter.getCassandraType().getReference().getTypeClass(),
                valueConverter.getCassandraType().getReference().getTypeClass()
        );
        return asJavaValue(map);
    }

    @Override
    public TypeDescriptor<Map<CK, CV>> getCassandraType() {
        return TypeDescriptor.map(
                keyConverter.getCassandraType(),
                valueConverter.getCassandraType());
    }

    @Override
    public TypeReference<Map<JK, JV>> getJavaType() {
        return new TypeReference<Map<JK, JV>>() {};
    }
}
