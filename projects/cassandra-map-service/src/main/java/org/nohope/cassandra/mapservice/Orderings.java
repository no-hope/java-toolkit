package org.nohope.cassandra.mapservice;

/**
 * Wrapper over original datastax ordering.
 */
public enum Orderings {
    ASC(false), DESC(true);

    private final boolean ordering;

    Orderings(final boolean ordering) {
        this.ordering = ordering;
    }

    boolean getOrdering() {
        return ordering;
    }
}
