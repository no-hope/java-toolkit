package org.nohope.cassandra.mapservice.ctypes;

import com.datastax.driver.core.Row;
import com.google.common.collect.ImmutableSet;
import org.nohope.reflection.TypeReference;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.function.BiFunction;

/**
 * Cassandra data types
 */
public final class CoreConverter<InternalType> implements Converter<InternalType, InternalType> {
    public static final CoreConverter<String> ASCII = of(TypeDescriptor.ASCII, Row::getString);
    public static final CoreConverter<Long> BIGINT = of(TypeDescriptor.BIGINT, Row::getLong);
    public static final CoreConverter<ByteBuffer> BLOB = of(TypeDescriptor.BLOB, Row::getBytes);
    public static final CoreConverter<Boolean> BOOLEAN = of(TypeDescriptor.BOOLEAN, Row::getBool);
    public static final CoreConverter<Long> COUNTER = of(TypeDescriptor.COUNTER, Row::getLong);
    public static final CoreConverter<BigDecimal> DECIMAL = of(TypeDescriptor.DECIMAL, Row::getDecimal);
    public static final CoreConverter<Double> DOUBLE = of(TypeDescriptor.DOUBLE, Row::getDouble);
    public static final CoreConverter<Float> FLOAT = of(TypeDescriptor.FLOAT, Row::getFloat);
    public static final CoreConverter<InetAddress> INET = of(TypeDescriptor.INET, Row::getInet);
    public static final CoreConverter<Integer> INT = of(TypeDescriptor.INT, Row::getInt);
    public static final CoreConverter<String> TEXT = of(TypeDescriptor.TEXT, Row::getString);
    public static final CoreConverter<Date> TIMESTAMP = of(TypeDescriptor.TIMESTAMP, Row::getDate);
    public static final CoreConverter<UUID> UUID = of(TypeDescriptor.UUID, Row::getUUID);
    public static final CoreConverter<String> VARCHAR = of(TypeDescriptor.VARCHAR, Row::getString);
    public static final CoreConverter<BigInteger> VARINT = of(TypeDescriptor.VARINT, Row::getVarint);
    public static final CoreConverter<UUID> TIMEUUID = of(TypeDescriptor.TIMEUUID, Row::getUUID);

    public static <C, J> Converter<List<C>, List<J>> list(final Converter<C, J> elementConverter) {
        return new ListType<>(elementConverter);
    }

    public static <C, J> Converter<Set<C>, Set<J>> set(final Converter<C, J> elementConverter) {
        return new SetType<>(elementConverter);
    }

    public static <CK, CV, JK, JV> Converter<Map<CK, CV>, Map<JK, JV>> map(final Converter<CK, JK> keyConverter,
                                                                           final Converter<CV, JV> valueConverter) {
        return new MapType<>(keyConverter, valueConverter);
    }

    private final TypeDescriptor<InternalType> descriptor;
    private final BiFunction<Row, String, InternalType> function;

    CoreConverter(final TypeDescriptor<InternalType> descriptor,
                  final BiFunction<Row, String, InternalType> function) {
        this.descriptor = descriptor;
        this.function = function;
    }

    private static <T> CoreConverter<T> of(final TypeDescriptor<T> descriptor,
                                           final BiFunction<Row, String, T> function) {
        return new CoreConverter<>(descriptor, function);
    }

    public static Set<CoreConverter<?>> getSupportedTypes() {
        return ImmutableSet.of(
                ASCII, BIGINT, BLOB, BOOLEAN, COUNTER,
                DECIMAL, DOUBLE, FLOAT, INET, INT,
                TEXT, TIMESTAMP, UUID, VARCHAR, VARINT,
                TIMEUUID/*, LIST, SET, MAP, TUPLE*/
        );
    }

    @Override
    public InternalType asCassandraValue(final InternalType value) {
        return value;
    }

    @Override
    public InternalType asJavaValue(final InternalType value) {
        return value;
    }

    @Override
    public TypeDescriptor<InternalType> getCassandraType() {
        return descriptor;
    }

    @Override
    public TypeReference<InternalType> getJavaType() {
        return descriptor.getReference();
    }

    @Override
    public InternalType readValue(final Row result, final String name) {
        return function.apply(result, name);
    }
}
