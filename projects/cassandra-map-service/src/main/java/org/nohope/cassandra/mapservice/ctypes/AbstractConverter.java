package org.nohope.cassandra.mapservice.ctypes;

import com.datastax.driver.core.Row;
import org.nohope.reflection.TypeReference;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">Ketoth Xupack</a>
 * @since 2015-02-11 15:09
 */
public abstract class AbstractConverter<CassandraType, JavaType> implements Converter<CassandraType, JavaType> {
    private final TypeReference<JavaType> type;
    private final CoreConverter<CassandraType> coreType;

    protected AbstractConverter(final Class<JavaType> clazz,
                                final CoreConverter<CassandraType> coreType) {
        this(TypeReference.erasure(clazz), coreType);
    }

    protected AbstractConverter(final TypeReference<JavaType> type,
                                final CoreConverter<CassandraType> coreType) {
        this.type = type;
        this.coreType = coreType;
    }

    @Override
    public final TypeDescriptor<CassandraType> getCassandraType() {
        return coreType.getCassandraType();
    }

    @Override
    public final TypeReference<JavaType> getJavaType() {
        return type;
    }

    @Override
    public final JavaType readValue(final Row result, final String name) {
        return asJavaValue(coreType.readValue(result, name));
    }
}
