package org.nohope.cassandra.mapservice.ctypes;

import com.datastax.driver.core.Row;
import org.nohope.reflection.TypeReference;

public interface Converter<CassandraType, JavaType> {
    JavaType readValue(Row result, String name);

    CassandraType asCassandraValue(JavaType value);
    JavaType asJavaValue(CassandraType value);

    TypeDescriptor<CassandraType> getCassandraType();
    TypeReference<JavaType> getJavaType();
}
