package org.nohope.cassandra.mapservice.columns;

/**
 */
public interface CCollection<V> {
    String insertToCollection(Iterable<V> values);

    String updateCollectionWith(Iterable<V> valuesToInsert);

    String collectionType();
}
