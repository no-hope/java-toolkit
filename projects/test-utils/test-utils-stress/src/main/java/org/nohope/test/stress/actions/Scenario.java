package org.nohope.test.stress.actions;

import org.nohope.test.stress.AbstractMeasureData;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">Ketoth Xupack</a>
 * @since 2013-12-27 23:55
 */
@FunctionalInterface
public interface Scenario<P extends AbstractMeasureData> {
    void doAction(final P p) throws Exception;
}
