package org.nohope.test.stress;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">Ketoth Xupack</a>
 * @since 2013-12-27 23:55
 */
public abstract class AbstractAction<P extends MeasureData> {
    protected abstract void doAction(final P p) throws Exception;
}
