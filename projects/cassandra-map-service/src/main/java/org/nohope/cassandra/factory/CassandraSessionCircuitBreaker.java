package org.nohope.cassandra.factory;

import com.datastax.driver.core.*;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Counts number of successful and failed requests to cassandra.
 * Later it will shutdown application in case of very high failure rate.
 */
public class CassandraSessionCircuitBreaker implements Session {
    private static final Logger LOG = LoggerFactory.getLogger(CassandraSessionCircuitBreaker.class);

    private final Session decorated;
    private final AtomicInteger numSuccess = new AtomicInteger(0);
    private final AtomicInteger numFailures = new AtomicInteger(0);

    private final Thread circuitBreakerThread = new Thread(new Runnable() {
        @Override
        public void run() {
            try {
                runCircuitBreaker();
            } catch (final InterruptedException e) {
                LOG.warn("circuit breaker interrupted");
            } catch (final Exception e) {
                LOG.error("Unexpected exception, circuit breaker won't print messages any more", e);
            }
        }
    }, "cassandra-circuit-breaker");

    private FutureCallback<Object> statCallback = new FutureCallback<Object>() {
        @Override
        public void onSuccess(final Object result) {
            numSuccess.incrementAndGet();
        }

        @Override
        public void onFailure(final Throwable t) {
            numFailures.incrementAndGet();
        }
    };

    public CassandraSessionCircuitBreaker(final Session decorated) {
        this.decorated = decorated;
        circuitBreakerThread.start();
    }

    @Override
    public String getLoggedKeyspace() {
        try {
            return decorated.getLoggedKeyspace();
        } catch (final RuntimeException e) {
            numFailures.incrementAndGet();
            throw e;
        }
    }

    @Override

    public Session init() {
        try {
            return decorated.init();
        } catch (final RuntimeException e) {
            numFailures.incrementAndGet();
            throw e;
        }
    }

    @Override
    public ResultSet execute(final String query) {
        try {
            return decorated.execute(query);
        } catch (final RuntimeException e) {
            numFailures.incrementAndGet();
            throw e;
        }
    }

    @Override
    public ResultSet execute(final String query, final Object... values) {
        try {
            return decorated.execute(query, values);
        } catch (final RuntimeException e) {
            numFailures.incrementAndGet();
            throw e;
        }
    }

    @Override
    public ResultSet execute(final Statement statement) {
        try {
            final ResultSet result = decorated.execute(statement);
            numSuccess.incrementAndGet();
            return result;
        } catch (final RuntimeException e) {
            numFailures.incrementAndGet();
            throw e;
        }
    }

    @Override
    public ResultSetFuture executeAsync(final String query) {
        try {
            final ResultSetFuture result = decorated.executeAsync(query);
            Futures.addCallback(result, statCallback);
            return result;
        } catch (final RuntimeException e) {
            numFailures.incrementAndGet();
            throw e;
        }
    }

    @Override
    public ResultSetFuture executeAsync(final String query, final Object... values) {
        try {
            final ResultSetFuture result = decorated.executeAsync(query, values);
            Futures.addCallback(result, statCallback);
            return result;
        } catch (final RuntimeException e) {
            numFailures.incrementAndGet();
            throw e;
        }
    }

    @Override
    public ResultSetFuture executeAsync(final Statement statement) {
        try {
            final ResultSetFuture result = decorated.executeAsync(statement);
            Futures.addCallback(result, statCallback);
            return result;
        } catch (final RuntimeException e) {
            numFailures.incrementAndGet();
            throw e;
        }
    }

    @Override
    public PreparedStatement prepare(final String query) {
        try {
            final PreparedStatement result = decorated.prepare(query);
            numSuccess.incrementAndGet();
            return result;
        } catch (final RuntimeException e) {
            numFailures.incrementAndGet();
            throw e;
        }
    }

    @Override
    public PreparedStatement prepare(final RegularStatement statement) {
        try {
            final PreparedStatement result = decorated.prepare(statement);
            numSuccess.incrementAndGet();
            return result;
        } catch (final RuntimeException e) {
            numFailures.incrementAndGet();
            throw e;
        }
    }

    @Override
    public ListenableFuture<PreparedStatement> prepareAsync(final String query) {
        try {
            final ListenableFuture<PreparedStatement> result = decorated.prepareAsync(query);
            Futures.addCallback(result, statCallback);
            return result;
        } catch (final RuntimeException e) {
            numFailures.incrementAndGet();
            throw e;
        }
    }

    @Override
    public ListenableFuture<PreparedStatement> prepareAsync(final RegularStatement statement) {
        try {
            final ListenableFuture<PreparedStatement> result = decorated.prepareAsync(statement);
            Futures.addCallback(result, statCallback);
            return result;
        } catch (final RuntimeException e) {
            numFailures.incrementAndGet();
            throw e;
        }
    }

    @Override
    public CloseFuture closeAsync() {
        try {
            final CloseFuture result = decorated.closeAsync();
            Futures.addCallback(result, statCallback);
            return result;
        } catch (final RuntimeException e) {
            numFailures.incrementAndGet();
            throw e;
        }
    }

    @Override
    public void close() {
        try {
            decorated.close();
            numSuccess.incrementAndGet();
        } catch (final RuntimeException e) {
            numFailures.incrementAndGet();
            throw e;
        }
    }

    @Override
    public boolean isClosed() {
        try {
            final boolean result = decorated.isClosed();
            numSuccess.incrementAndGet();
            return result;
        } catch (final RuntimeException e) {
            numFailures.incrementAndGet();
            throw e;
        }
    }

    @Override
    public Cluster getCluster() {
        try {
            final Cluster result = decorated.getCluster();
            numSuccess.incrementAndGet();
            return result;
        } catch (final RuntimeException e) {
            numFailures.incrementAndGet();
            throw e;
        }
    }

    @Override
    public State getState() {
        try {
            final State result = decorated.getState();
            numSuccess.incrementAndGet();
            return result;
        } catch (final RuntimeException e) {
            numFailures.incrementAndGet();
            throw e;
        }
    }

    private void runCircuitBreaker() throws InterruptedException {
        while (true) {
            TimeUnit.MINUTES.sleep(1);
            final int currNumSuccess = numSuccess.getAndSet(0);
            final int currNumFailures = numFailures.getAndSet(0);
            if ((currNumSuccess + currNumFailures) > 0) {
                final float successRate = (float) currNumSuccess / (currNumSuccess + currNumFailures);
                if (currNumFailures > 0) {
                    LOG.warn("{} succeeded and {} failed requests to cassandra in 1 minute - {}% success", currNumSuccess, currNumFailures, 100 * successRate);
                } else {
                    LOG.info("{} succeeded and {} failed requests to cassandra in 1 minute - {}% success", currNumSuccess, currNumFailures, 100 * successRate);
                }
            }
        }
    }
}
