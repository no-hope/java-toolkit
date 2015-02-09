package org.nohope.cassandra.mapservice;

import com.datastax.driver.core.BatchStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.RegularStatement;
import com.datastax.driver.core.Statement;
import org.nohope.cassandra.factory.CassandraFactory;
import org.nohope.cassandra.mapservice.update.CUpdate;

import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Implementation for batch operations in {@link org.nohope.cassandra.mapservice.CMapService mapService}
 */
public final class CBatchImpl implements CBatch {
    private final ConsistencyLevel consistencyLevel;
    private final Collection<Statement> statements = new LinkedList<>();
    private final CMapService cMapService;
    private final CassandraFactory factory;
    private final AtomicBoolean wasApplied = new AtomicBoolean(false);
    private final AtomicBoolean hasCounterOperations = new AtomicBoolean(false);

    /**
     * Instantiates a new C batch impl.
     *
     * @param cMapService      {@link org.nohope.cassandra.mapservice.CMapService}
     * @param factory          {@link org.nohope.cassandra.factory.CassandraFactory}
     * @param consistencyLevel {@link com.datastax.driver.core.ConsistencyLevel}
     */
    public CBatchImpl(final CMapService cMapService,
                      final CassandraFactory factory,
                      final ConsistencyLevel consistencyLevel) {
        this.cMapService = cMapService;
        this.consistencyLevel = consistencyLevel;
        this.factory = factory;
    }

    @Override
    public CBatch remove(final String mapId, final CQuery cQuery) {
        statements.add(cMapService.getMap(mapId).createRemoveOperation(cQuery, consistencyLevel));
        return this;
    }

    @Override
    public CBatch update(final String mapId, final CUpdate update) throws CMapServiceException {
        final CMapSync map = cMapService.getMap(mapId);
        final RegularStatement operation = map.createUpdateOperation(update, consistencyLevel);
        statements.add(operation);
        hasCounterOperations.set(true);
        return this;
    }

    @Override
    public CBatch remove(final CPreparedRemove.PreparedRemoveExecutor removeExecutor) throws CMapServiceException {
        statements.add(removeExecutor.getBound());
        return this;
    }

    @Override
    public CBatch put(final CPreparedPut.PreparedPutExecutor putExecutor) throws CMapServiceException {
        statements.add(putExecutor.getBound());
        return this;
    }

    // TODO: dirty
    @Override
    public CBatch action(final PreparedExecutor executor) throws CMapServiceException {
        if (executor instanceof CPreparedRemove.PreparedRemoveExecutor) {
            return remove((CPreparedRemove.PreparedRemoveExecutor) executor);
        } else if (executor instanceof CPreparedPut.PreparedPutExecutor) {
            return put((CPreparedPut.PreparedPutExecutor) executor);
        }

        throw new IllegalArgumentException(executor.toString());
    }

    @Override
    public CBatch put(final String mapId, final CPutQuery cPutQuery) {
        CMapSync map = cMapService.getMap(mapId);
        final RegularStatement putOperation = map.createPutOperation(cPutQuery, consistencyLevel);
        statements.add(putOperation);
        return this;
    }

    @Override
    public void apply() {
        performCheckBatchBeforeApplying();
        final BatchStatement batch = formProperBatch(BatchStatement.Type.LOGGED);
        for (final Statement statement : batch.getStatements()) {
            statement.enableTracing();
        }

        appliedBatch(batch);
    }

    @Override
    public void applyAsUnlogged() throws CMapServiceException {
        performCheckBatchBeforeApplying();
        final BatchStatement batch = formProperBatch(BatchStatement.Type.UNLOGGED);
        appliedBatch(batch);
    }

    private BatchStatement formProperBatch(final BatchStatement.Type loggingType) {
        final BatchStatement batchStatement;
        if (hasCounterOperations.get()) {
            batchStatement = new BatchStatement(BatchStatement.Type.COUNTER);
        } else {
            batchStatement = new BatchStatement(loggingType);
        }

        batchStatement.addAll(statements);
        return batchStatement;
    }

    private void performCheckBatchBeforeApplying() {
        if (wasApplied.get()) {
            throw new CMapServiceException("Can't apply same batch twice");
        }
        if (statements.isEmpty()) {
            throw new CMapServiceException("No statements for batch operations");
        }
    }

    private void appliedBatch(final BatchStatement batch) {
        wasApplied.set(true);
//        try(final Session session = factory.createSession()) {
//            session.execute(batch);
//        }
        factory.getSession().execute(batch);
    }
}
