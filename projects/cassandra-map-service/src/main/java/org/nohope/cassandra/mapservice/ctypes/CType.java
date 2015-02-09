package org.nohope.cassandra.mapservice.ctypes;

/**
 * Cassandra data types
 */
public enum CType {
    ASCII("ascii"),
    BIGINT("bigint"),
    BLOB("blob"),
    BOOLEAN("boolean"),
    COUNTER("counter"),
    DECIMAL("decimal"),
    DOUBLE("double"),
    FLOAT("float"),
    INET("inet"),
    INT("int"),
    LIST("list"),
    MAP("map"),
    SET("set"),
    TEXT("text"),
    TIMESTAMP("timestamp"),
    UUID("uuid"),
    TIMEUUID("timeuuid"),
    VARCHAR("varchar"),
    VARINT("varint");

    private final String cassandraType;

    CType(final String cassandraType) {
        this.cassandraType = cassandraType;
    }

    public String getType() {
        return cassandraType;
    }
}
