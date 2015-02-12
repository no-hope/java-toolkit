package org.nohope.cassandra.mapservice;

import com.google.common.base.Optional;

import javax.annotation.concurrent.Immutable;

/**
 * Wrapper for {@link org.nohope.cassandra.mapservice.ValueTuple ValueTuple} with options. <br>
 * <ol>
 * Examples of usage:
 * <li> Create simple put query
 * <pre>
 * CPutQuery query =
 *  CQueryBuilder.createPutQuery()
 *      .addValueTuple(ValueTuple.of("column1", "column2"))
 *      .end()
 *     </pre>
 * </li>
 * <li> Create put query with TTL in seconds
 * <pre>
 * CPutQuery query =
 *  CQueryBuilder.createPutQuery()
 *      .addValueTuple(ValueTuple.of("column1", "column2"))
 *      .withTTL(1000)
 *      </pre>
 * </li>
 *
 * <li> Create put query with TTL using joda.time.Duration
 * <pre>
 * CPutQuery query =
 *  CQueryBuilder.createPutQuery()
 *      .addValueTuple(ValueTuple.of("column1", "column2"))
 *      .withTTL(Duration.millis(111))
 *      </pre>
 * </li>
 *
 * </ol>
 */
@Immutable
public final class CPutQuery {
    private final Optional<ValueTuple> valueTuple;

    private final Optional<Integer> ttl;
    private final Optional<Long> timestamp;

    public CPutQuery(final ValueTuple valueTuple) {
        this(Optional.of(valueTuple), Optional.<Integer> absent());
    }

    public CPutQuery(final Optional<ValueTuple> valueTuple) {
        this(valueTuple, Optional.<Integer> absent());
    }

    public CPutQuery(final ValueTuple valueTuple,
                     final Optional<Integer> ttl) {
        this(Optional.of(valueTuple), ttl);
    }

    public CPutQuery(final Optional<ValueTuple> valueTuple,
                     final Optional<Integer> ttl) {
        this.valueTuple = valueTuple;
        this.ttl = ttl;
        this.timestamp = Optional.absent();
    }

    public Optional<ValueTuple> getValueTuple() {
        if (valueTuple.isPresent()) {
            return Optional.of(valueTuple.get());
        } else {
            return Optional.absent();
        }
    }

    public Optional<Integer> getTTL() {
        return ttl;
    }

    public Optional<Long> getTimestamp() {
        return timestamp;
    }
}
