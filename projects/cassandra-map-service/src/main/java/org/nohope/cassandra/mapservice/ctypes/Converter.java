package org.nohope.cassandra.mapservice.ctypes;

import com.datastax.driver.core.Row;
import org.nohope.cassandra.mapservice.columns.CColumn;
import org.nohope.reflection.TypeReference;

public interface Converter<CassandraType, JavaType> {
    JavaType readValue(Row result, CColumn<JavaType, CassandraType> name);
    CassandraType asCassandraValue(JavaType value);
    JavaType asJavaValue(CassandraType value);
    TypeDescriptor<CassandraType> getCassandraType();
    TypeReference<JavaType> getJavaType();
}
