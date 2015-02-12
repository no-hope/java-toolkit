package org.nohope.cassandra.mapservice.ctypes;

import com.datastax.driver.core.Row;
import com.google.common.collect.ImmutableSet;
import org.nohope.cassandra.mapservice.columns.CColumn;
import org.nohope.reflection.TypeReference;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.*;


/**
 * Cassandra data types
 */
public abstract class CoreConverter<InternalType> implements Converter<InternalType, InternalType> {
    public static final CoreConverter<String> ASCII = new CoreConverter<String>(TypeDescriptor.ASCII) {
        @Override
        public String readValue(final Row result, final CColumn<String, String> column) {
            return result.getString(column.getName());
        }
    };
    public static final CoreConverter<Long> BIGINT = new CoreConverter<Long>(TypeDescriptor.BIGINT) {
        @Override
        public Long readValue(final Row result, final CColumn<Long, Long> column) {
            return result.getLong(column.getName());
        }
    };
    public static final CoreConverter<ByteBuffer> BLOB = new CoreConverter<ByteBuffer>(TypeDescriptor.BLOB) {
        @Override
        public ByteBuffer readValue(final Row result, final CColumn<ByteBuffer, ByteBuffer> column) {
            return result.getBytes(column.getName());
        }
    };
    public static final CoreConverter<Boolean> BOOLEAN = new CoreConverter<Boolean>(TypeDescriptor.BOOLEAN) {
        @Override
        public Boolean readValue(final Row result, final CColumn<Boolean, Boolean> column) {
            return result.getBool(column.getName());
        }
    };
    public static final CoreConverter<Long> COUNTER = new CoreConverter<Long>(TypeDescriptor.COUNTER) {
        @Override
        public Long readValue(final Row result, final CColumn<Long, Long> column) {
            return result.getLong(column.getName());
        }
    };
    public static final CoreConverter<BigDecimal> DECIMAL = new CoreConverter<BigDecimal>(TypeDescriptor.DECIMAL) {
        @Override
        public BigDecimal readValue(final Row result, final CColumn<BigDecimal, BigDecimal> column) {
            return result.getDecimal(column.getName());
        }
    };
    public static final CoreConverter<Double> DOUBLE = new CoreConverter<Double>(TypeDescriptor.DOUBLE) {
        @Override
        public Double readValue(final Row result, final CColumn<Double, Double> column) {
            return result.getDouble(column.getName());
        }
    };
    public static final CoreConverter<Float> FLOAT = new CoreConverter<Float>(TypeDescriptor.FLOAT) {
        @Override
        public Float readValue(final Row result, final CColumn<Float, Float> column) {
            return result.getFloat(column.getName());
        }
    };
    public static final CoreConverter<InetAddress> INET = new CoreConverter<InetAddress>(TypeDescriptor.INET) {
        @Override
        public InetAddress readValue(final Row result, final CColumn<InetAddress, InetAddress> column) {
            return result.getInet(column.getName());
        }
    };
    public static final CoreConverter<Integer> INT = new CoreConverter<Integer>(TypeDescriptor.INT) {
        @Override
        public Integer readValue(final Row result, final CColumn<Integer, Integer> column) {
            return result.getInt(column.getName());
        }
    };
    public static final CoreConverter<String> TEXT = new CoreConverter<String>(TypeDescriptor.TEXT) {
        @Override
        public String readValue(final Row result, final CColumn<String, String> column) {
            return result.getString(column.getName());
        }
    };
    public static final CoreConverter<Date> TIMESTAMP = new CoreConverter<Date>(TypeDescriptor.TIMESTAMP) {
        @Override
        public Date readValue(final Row result, final CColumn<Date, Date> column) {
            return result.getDate(column.getName());
        }
    };
    public static final CoreConverter<UUID> UUID = new CoreConverter<UUID>(TypeDescriptor.UUID) {
        @Override
        public UUID readValue(final Row result, final CColumn<UUID, UUID> column) {
            return result.getUUID(column.getName());
        }
    };
    public static final CoreConverter<String> VARCHAR = new CoreConverter<String>(TypeDescriptor.VARCHAR) {
        @Override
        public String readValue(final Row result, final CColumn<String, String> column) {
            return result.getString(column.getName());
        }
    };
    public static final CoreConverter<BigInteger> VARINT = new CoreConverter<BigInteger>(TypeDescriptor.VARINT) {
        @Override
        public BigInteger readValue(final Row result, final CColumn<BigInteger, BigInteger> column) {
            return result.getVarint(column.getName());
        }
    };
    public static final CoreConverter<UUID> TIMEUUID = new CoreConverter<UUID>(TypeDescriptor.TIMEUUID) {
        @Override
        public UUID readValue(final Row result, final CColumn<UUID, UUID> column) {
            return result.getUUID(column.getName());
        }
    };

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

    CoreConverter(final TypeDescriptor<InternalType> descriptor) {
        this.descriptor = descriptor;
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
}
