package org.nohope.cassandra.mapservice;

import org.nohope.cassandra.mapservice.update.CUpdate;

/**
 * Interface for Batch operations in {@link org.nohope.cassandra.mapservice.CMapService mapService}
 * <ol>
 * <b>Example:</b> <br>
 * <p/>
 * <li>Remove example:
 * <pre>
 * {@link org.nohope.cassandra.mapservice.CQuery CQuery} removeQuery = {@link org.nohope.cassandra.mapservice.CQueryBuilder CQueryBuilder}
 *      .createRemoveQuery()
 *      .addFilters()
 *      .eq(SOME_SMART_NAME_FOR_COLUMN, "xxx")
 *      .eq(ANOTHER_SMART_NAME_FOR_COLUMN, "yyy")
 *      .noMoreFilters()
 *      .end();
 *
 * CBatch batch = {@link org.nohope.cassandra.mapservice.CMapService service}.batch();
 * batch.remove(TABLE_NAME, removeQuery);
 * batch.apply();
 *  </pre>
 * </li>
 * <li>Put example:
 * <pre>
 *     {@link org.nohope.cassandra.mapservice.ValueTuple ValueTuple} valueToPut = ValueTuple.of(NAME_COL, "xxx")
 *               .with(KINGDOM_COL, "yyy");
 *      CBatch batch = {@link org.nohope.cassandra.mapservice.CMapService service}.batch();
 *      batch.put(RING_OF_POWER_TABLE, new CPutQuery(valueToPut));
 *      batch.apply();
 *  </pre>
 * </li>
 *
 *
 *
 *
 * </ol>
 */
public interface CBatch {

    /**
     * Remove batch query.
     *
     * @param mapId  name of existing in {@link org.nohope.cassandra.mapservice.CMapService mapService} map
     * @param cQuery {@link org.nohope.cassandra.mapservice.CQuery query}
     * @return batch object
     * @throws CMapServiceException the c map service exception
     */
    CBatch remove(String mapId, CQuery cQuery) throws CMapServiceException;

    CBatch put(String mapId, CPutQuery cPutQuery) throws CMapServiceException;

    /**
     * Update batch. Applies as count.
     *
     * @param mapId  name of existing in {@link org.nohope.cassandra.mapservice.CMapService mapService} map
     * @param update {@link org.nohope.cassandra.mapservice.update.CUpdate CUpdate}
     * @return batch object
     * @throws CMapServiceException the c map service exception
     */
    CBatch update(String mapId, CUpdate update) throws CMapServiceException;

    /**
     * Prepared remove batch. {@link org.nohope.cassandra.mapservice.CPreparedRemove CPreparedRemove}
     *
     * @param removeExecutor {@link org.nohope.cassandra.mapservice.CPreparedRemove.PreparedRemoveExecutor}
     * @return batch object
     * @throws CMapServiceException the c map service exception
     */
    CBatch remove(CPreparedRemove.PreparedRemoveExecutor removeExecutor) throws CMapServiceException;

    /**
     * Prepared put batch. {@link org.nohope.cassandra.mapservice.CPreparedPut CPreparedPut}
     *
     * @param putExecutor {@link org.nohope.cassandra.mapservice.CPreparedPut}
     * @return batch object
     * @throws CMapServiceException the c map service exception
     */
    CBatch put(CPreparedPut.PreparedPutExecutor putExecutor) throws CMapServiceException;

    /**
     * Prepared batch operation, automatically selecting.
     * {@link org.nohope.cassandra.mapservice.CBatch#put(org.nohope.cassandra.mapservice.CPreparedPut.PreparedPutExecutor)}
     * or
     * {@link org.nohope.cassandra.mapservice.CBatch#remove(org.nohope.cassandra.mapservice.CPreparedRemove.PreparedRemoveExecutor)}
     *
     * @param executor {@link org.nohope.cassandra.mapservice.PreparedExecutor}
     * @return batch object
     * @throws CMapServiceException the c map service exception
     */
    CBatch action(PreparedExecutor executor) throws CMapServiceException;

    void apply() throws CMapServiceException;

    /**
     * Apply batch as unlogged.
     *
     * @throws CMapServiceException the c map service exception
     */
    void applyAsUnlogged() throws CMapServiceException;
}
