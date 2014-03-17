package org.nohope.typetools;

import org.nohope.test.UtilitiesTestSupport;

/**
 * Date: 07.08.12
 * Time: 17:26
 */
public class TTimeSingletonTest extends UtilitiesTestSupport<TTime.LazyDataTypeFactorySingleton> {
    @Override
    protected Class<TTime.LazyDataTypeFactorySingleton> getUtilityClass() {
        return TTime.LazyDataTypeFactorySingleton.class;
    }
}
