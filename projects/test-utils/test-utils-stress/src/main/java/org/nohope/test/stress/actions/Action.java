package org.nohope.test.stress.actions;

import org.nohope.test.stress.MeasureData;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">Ketoth Xupack</a>
 * @since 2013-12-27 23:55
 */
@FunctionalInterface
public interface Action<P extends MeasureData> {
    void doAction(final P p) throws Exception;
}
