package org.nohope.cassandra.mapservice;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.nohope.cassandra.factory.CassandraFactory;
import org.nohope.cassandra.factory.ITHelpers;
import org.nohope.cassandra.mapservice.columns.CColumn;
import org.nohope.cassandra.mapservice.ctypes.custom.SerializableType;
import org.nohope.cassandra.util.RowNotFoundException;

import java.io.Serializable;
import java.nio.ByteBuffer;

import static org.junit.Assert.assertEquals;

/**
 */
public class CMapNonTrivialTypeIT {
    private static final CColumn<Dwarf, ByteBuffer> DWARF_COL =
            CColumn.of("dwarf", SerializableType.kryo(Dwarf.class));
    private static final TableScheme SCHEME = new CMapBuilder("dwarfs")
            .addColumn(DWARF_COL)
            .end()
            .setPartition(DWARF_COL)
            .withoutClustering().buildScheme();

    private static final CQuery QUERY = new CQuery(DWARF_COL);

    private CMapSync testMap;
    private CassandraFactory cassandraFactory;

    @Before
    public void setUp() {
        cassandraFactory = ITHelpers.cassandraFactory();
        testMap = new CMapSync(SCHEME, cassandraFactory);
    }

    @After
    public void tearDown() {
        ITHelpers.destroy(cassandraFactory);
    }

    @Test
    public void nonTrivialTypePutAndGetTest() throws RowNotFoundException {
        final Dwarf gloin = new Dwarf("Gloin", "Groin");
        final ValueTuple valueToPut = ValueTuple.of(DWARF_COL, gloin);

        testMap.put(new CPutQuery(valueToPut));

        final ValueTuple returnValue = testMap.getOne(QUERY);
        assertEquals(returnValue.get(DWARF_COL), gloin);
    }

    @Test
    public void nonTrivialTypeTypedGetTest() throws RowNotFoundException {
        final Dwarf gloin = new Dwarf("Gloin", "Groin");
        final ValueTuple valueToPut = ValueTuple.of(DWARF_COL, gloin);

        testMap.put(new CPutQuery(valueToPut));

        final ValueTuple returnValue = testMap.getOne(QUERY);
        assertEquals(returnValue.get(DWARF_COL), gloin);
    }

    private static class Dwarf implements Serializable {
        private static final long serialVersionUID = 8955293282893726628L;
        private final String name;
        private final String sonOf;

        private Dwarf(final String name, final String sonOf) {
            this.name = name;
            this.sonOf = sonOf;
        }

        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (getClass() != o.getClass()) {
                return false;
            }

            final Dwarf gnome = (Dwarf) o;
            return name.equals(gnome.name) && sonOf.equals(gnome.sonOf);
        }

        public int hashCode() {
            int result;
            result = ((name != null) ? name.hashCode() : 0);
            result = (31 * result) + ((sonOf != null) ? sonOf.hashCode() : 0);
            return result;
        }
    }
}
