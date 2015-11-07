package org.nohope.cassandra.mapservice.ctypes;

import com.datastax.driver.core.DataType;
import org.nohope.reflection.TypeReference;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.*;

import static com.datastax.driver.core.DataType.*;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">Ketoth Xupack</a>
 * @since 2015-02-11 05:20
 */
public final class TypeDescriptor<Type> {
    public static final TypeDescriptor<String> ASCII = of(ascii(), String.class);
    public static final TypeDescriptor<Long> BIGINT = of(bigint(), Long.class);
    public static final TypeDescriptor<ByteBuffer> BLOB = of(blob(), ByteBuffer.class);
    public static final TypeDescriptor<Boolean> BOOLEAN = of(cboolean(), Boolean.class);
    public static final TypeDescriptor<Long> COUNTER = of(counter(), Long.class);
    public static final TypeDescriptor<BigDecimal> DECIMAL = of(decimal(), BigDecimal.class);
    public static final TypeDescriptor<Double> DOUBLE = of(cdouble(), Double.class);
    public static final TypeDescriptor<Float> FLOAT = of(cfloat(), Float.class);
    public static final TypeDescriptor<InetAddress> INET = of(inet(), InetAddress.class);
    public static final TypeDescriptor<Integer> INT = of(cint(), Integer.class);
    public static final TypeDescriptor<String> TEXT = of(text(), String.class);
    public static final TypeDescriptor<Date> TIMESTAMP = of(timestamp(), Date.class);
    public static final TypeDescriptor<java.util.UUID> UUID = of(uuid(), UUID.class);
    public static final TypeDescriptor<String> VARCHAR = of(varchar(), String.class);
    public static final TypeDescriptor<BigInteger> VARINT = of(varint(), BigInteger.class);
    public static final TypeDescriptor<UUID> TIMEUUID = of(timeuuid(), UUID.class);

    private final DataType dataType;
    private final TypeReference<Type> reference;

    private TypeDescriptor(@Nonnull final DataType dataType,
                           @Nonnull final TypeReference<Type> reference) {
        this.dataType = dataType;
        this.reference = reference;
        if (!reference.getTypeClass().equals(dataType.asJavaClass())) {
            throw new IllegalStateException(
                    "Cassandra type " + dataType
                    + " is mapped to " + dataType.asJavaClass()
                    + ", but " + reference.getTypeClass() + " was passed");
        }
    }

    public static <C> TypeDescriptor<List<C>> list(final TypeDescriptor<C> elementType) {
        return of(DataType.list(elementType.dataType), new TypeReference<List<C>>() {});
    }

    public static <C> TypeDescriptor<Set<C>> set(final TypeDescriptor<C> elementType) {
        return of(DataType.set(elementType.dataType), new TypeReference<Set<C>>() {});
    }

    public static <K, V> TypeDescriptor<Map<K, V>> map(final TypeDescriptor<K> keyType,
                                                       final TypeDescriptor<V> valueType) {
        return of(DataType.map(keyType.dataType, valueType.dataType), new TypeReference<Map<K, V>>() {});
    }

    private static <T> TypeDescriptor<T> of(@Nonnull final DataType dataType,
                                            @Nonnull final TypeReference<T> reference) {
        return new TypeDescriptor<>(dataType, reference);
    }

    private static <T> TypeDescriptor<T> of(@Nonnull final DataType dataType,
                                            @Nonnull final Class<T> reference) {
        return new TypeDescriptor<>(dataType, TypeReference.erasure(reference));
    }

    public DataType getDataType() {
        return dataType;
    }

    public TypeReference<Type> getReference() {
        return reference;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if ((o == null) || (getClass() != o.getClass())) {
            return false;
        }

        final TypeDescriptor<?> that = (TypeDescriptor<?>) o;
        return (dataType == that.dataType)
            && reference.equals(that.reference);
    }

    public String getTypeName() {
        return dataType.toString();
    }

    @Override
    public int hashCode() {
        int result = dataType.hashCode();
        result = (31 * result) + reference.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "TypeDescriptor{" +
               "dataType=" + dataType +
               ", reference=" + reference +
               '}';
    }
}
