package org.nohope.typetools;

import org.nohope.test.UtilitiesTestSupport;

/**
 * Date: 07.08.12
 * Time: 17:26
 */
public class TTimeSingletonTest extends UtilitiesTestSupport {
    @Override
    protected Class<?> getUtilityClass() {
        return TTime.LazyDataTypeFactorySingleton.class;
    }
}
