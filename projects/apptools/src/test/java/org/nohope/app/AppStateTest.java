package org.nohope.app;

import org.nohope.test.EnumTestSupport;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">Ketoth Xupack</a>
 * @since 2013-10-10 23:57
 */
public class AppStateTest extends EnumTestSupport<AppState> {
    @Override
    protected Class<AppState> getEnumClass() {
        return AppState.class;
    }
}
