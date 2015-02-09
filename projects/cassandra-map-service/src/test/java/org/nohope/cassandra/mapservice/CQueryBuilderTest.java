package org.nohope.cassandra.mapservice;

public final class CQueryBuilderTest {
//    private static final Collection<CFilter> FILTERS = new ArrayList<>();
//    private static final ColumnsSet COLUMNS = new ColumnsSet("xxx", "yyy");
//    private static final List<COrdering> ORDERINGS = new ArrayList<>();
//    private static final List<COrdering> EMPTY_ORDERINGS = new ArrayList<>();
//
//    static {
//        FILTERS.add(CFilters.eq("aaa", "bbb"));
//        FILTERS.add(CFilters.eq("ccc", "ddd"));
//    }
//
//    static {
//        ORDERINGS.add(new COrdering("xxx", Orderings.ASC));
//    }
//
//    @Test
//    public void testContract() {
//        final CQuery q1 = new CQuery(COLUMNS, FILTERS, true, ORDERINGS, false);
//        final CQuery q2 = new CQuery(COLUMNS, FILTERS, true, ORDERINGS, false);
//        ContractUtils.assertStrongEquality(q1, q2);
//    }
//
//    @Test
//    public void testCOrderingsContract() {
//        final COrdering o1 = new COrdering("xxx", Orderings.ASC);
//        final COrdering o2 = new COrdering("xxx", Orderings.ASC);
//        ContractUtils.assertStrongEquality(o1, o2);
//    }
//
//    @Test
//    public void testOrderings() {
//        final COrdering o1 = new COrdering("xxx", Orderings.ASC);
//
//        assertNotEquals(o1.isDesc(), Orderings.ASC.getOrdering());
//
//        assertEquals(o1.getColumnName(), "xxx");
//    }
//
//    @Test
//    public void testOf(){
//
//        final CQuery queryToExpect = new CQuery(new ColumnsSet("xxx", "yyy"), true);
//
//        final CQuery query = CQueryBuilder
//                .createQuery()
//                .of("xxx", "yyy")
//                .end();
//
//
//        assertEquals(query, queryToExpect);
//    }
//
//    @Test
//    public void testFiltersGetter(){
//        final CQuery queryToExpect = new CQuery(COLUMNS, FILTERS, false, EMPTY_ORDERINGS, false);
//
//        assertEquals(FILTERS, queryToExpect.getFilters());
//    }
//
//    @Test
//    public void testColumnsSetGetter(){
//        final CQuery queryToExpect = new CQuery(COLUMNS, FILTERS, false, EMPTY_ORDERINGS, false);
//
//        assertEquals(COLUMNS.getColumns().size(), queryToExpect.getExpectedColumnsCollection().getColumns().size());
//        assertTrue(COLUMNS.getColumns().containsAll(queryToExpect.getExpectedColumnsCollection().getColumns()));
//    }
//
//    @Test void testWithFilters() {
//        final CQuery queryToExpect = new CQuery(COLUMNS, FILTERS, false, EMPTY_ORDERINGS, false);
//
//        final CQuery query = CQueryBuilder
//                .createQuery()
//                .of("xxx", "yyy")
//                .withFilters(FILTERS)
//                .end();
//
//        assertEquals(query, queryToExpect);
//    }
//
//
//    @Test
//    public void testOfColumnSet() {
//        final CQuery queryToExpect = new CQuery(new ColumnsSet("xxx", "yyy"), true);
//
//        final CQuery query = CQueryBuilder
//                .createQuery()
//                .of(COLUMNS)
//                .end();
//
//        assertEquals(query, queryToExpect);
//    }
//
//    @Test
//    public void testEQGetFilters() {
//        final CQuery queryToExpect = new CQuery(COLUMNS, FILTERS, false, EMPTY_ORDERINGS, false);
//
//        final CQuery query = CQueryBuilder
//                .createQuery()
//                .of(COLUMNS)
//                .addFilters()
//                .eq("aaa", "bbb")
//                .eq("ccc", "ddd")
//                .noMoreFilters()
//                .end();
//
//        assertEquals(query, queryToExpect);
//    }
//
//    @Test
//    public void testGTGetFilters() {
//        final Collection<CFilter> filters = new ArrayList<>();
//        filters.add(CFilters.gt("xxx", 4));
//
//        final CQuery queryToExpect = new CQuery(COLUMNS, filters, false, EMPTY_ORDERINGS, false);
//
//        final CQuery query = CQueryBuilder
//                .createQuery()
//                .of(COLUMNS)
//                .addFilters()
//                .gt("xxx", 4)
//                .noMoreFilters()
//                .end();
//
//        assertEquals(query, queryToExpect);
//    }
//
//    @Test
//    public void testGTEGetFilters() {
//        final Collection<CFilter> filters = new ArrayList<>();
//        filters.add(CFilters.gte("xxx", 4));
//
//        final CQuery queryToExpect = new CQuery(COLUMNS, filters, false, EMPTY_ORDERINGS, false);
//
//        final CQuery query = CQueryBuilder
//                .createQuery()
//                .of(COLUMNS)
//                .addFilters()
//                .gte("xxx", 4)
//                .noMoreFilters()
//                .end();
//
//        assertEquals(query, queryToExpect);
//    }
//
//    @Test
//    public void testLTGetFilters() {
//        final Collection<CFilter> filters = new ArrayList<>();
//        filters.add(CFilters.lt("xxx", 4));
//
//        final CQuery queryToExpect = new CQuery(COLUMNS, filters, false, EMPTY_ORDERINGS, false);
//        final CQuery query = CQueryBuilder
//                .createQuery()
//                .of(COLUMNS)
//                .addFilters()
//                .lt("xxx", 4)
//                .noMoreFilters()
//                .end();
//
//        assertEquals(query, queryToExpect);
//    }
//
//    @Test
//    public void testLTEGetFilters() {
//        final Collection<CFilter> filters = new ArrayList<>();
//        filters.add(CFilters.lte("xxx", 4));
//
//        final CQuery queryToExpect = new CQuery(COLUMNS, filters, false, EMPTY_ORDERINGS, false);
//        final CQuery query = CQueryBuilder
//                .createQuery()
//                .of(COLUMNS)
//                .addFilters()
//                .lte("xxx", 4)
//                .noMoreFilters()
//                .end();
//
//        assertEquals(query, queryToExpect);
//    }
//
//    @Test
//    public void allowFiltering() {
//        final CQuery queryToExpect = new CQuery(COLUMNS, FILTERS, true, EMPTY_ORDERINGS, false);
//
//        final CQuery query = CQueryBuilder
//                .createQuery()
//                .of("xxx", "yyy")
//                .withFilters(FILTERS)
//                .allowFiltering()
//                .end();
//
//        assertEquals(query, queryToExpect);
//        assertTrue(query.isAllowFiltering());
//    }
//
//    @Test
//    public void allowOrderingBy() throws CQueryException {
//        final CQuery queryToExpect = new CQuery(COLUMNS, FILTERS, false, ORDERINGS, false);
//
//        final CQuery query = CQueryBuilder
//                .createQuery()
//                .of("xxx", "yyy")
//                .withFilters(FILTERS)
//                .orderingBy("xxx", Orderings.ASC)
//                .end();
//
//        assertEquals(query, queryToExpect);
//        assertEquals(query.getOrderBy(), ORDERINGS);
//    }
//
//    @Test
//    public void cOrderingToProperCassandraOrderingsConversionTest() throws CQueryException, NoSuchFieldException, IllegalAccessException {
//        final Map<String, Boolean> expectedOrderingsNames = new HashMap<>();
//        expectedOrderingsNames.put("xxx", true); // equivalent to QueryBuilder.asc("xxx")
//        expectedOrderingsNames.put("yyy", false); // equivalent to QueryBuilder.desc("yyy")
//
//        final CQuery queryASC = CQueryBuilder
//                .createQuery()
//                .of("xxx", "yyy")
//                .withFilters(FILTERS)
//                .orderingBy("xxx", Orderings.ASC)
//                .end();
//
//
//        final Ordering[] cassandraASCOrdering = queryASC.getOrderingAsCassandraOrderings();
//
//       assertEquals(cassandraASCOrdering.length, 1);
//       assertFieldsWithReflection(expectedOrderingsNames, cassandraASCOrdering[0]);
//
//        final CQuery queryDESC = CQueryBuilder
//                .createQuery()
//                .of("xxx", "yyy")
//                .withFilters(FILTERS)
//                .orderingBy("yyy", Orderings.DESC)
//                .end();
//
//        final Ordering[] cassandraOrdering = queryDESC.getOrderingAsCassandraOrderings();
//
//        assertEquals(cassandraOrdering.length, 1);
//        assertFieldsWithReflection(expectedOrderingsNames, cassandraOrdering[0]);
//    }
//
//    private static void assertFieldsWithReflection(
//            final Map<String, Boolean> expectedOrderingsNames,
//            final Ordering ordering) throws NoSuchFieldException, IllegalAccessException {
//        final Field nameField = ordering.getClass().getDeclaredField("name");
//        final Field descField = ordering.getClass().getDeclaredField("isDesc");
//        nameField.setAccessible(true);
//        descField.setAccessible(true);
//        final String name = (String) nameField.get(ordering);
//        if(expectedOrderingsNames.containsKey(name)) {
//             final Boolean isDeskValue = (Boolean) descField.get(ordering);
//            assertEquals(expectedOrderingsNames.get(name), isDeskValue);
//        }
//    }
}
