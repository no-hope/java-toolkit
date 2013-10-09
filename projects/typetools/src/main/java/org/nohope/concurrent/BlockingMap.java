package org.nohope.concurrent;

import javax.annotation.concurrent.ThreadSafe;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">Ketoth Xupack</a>
 * @since 6/10/11 4:56 PM
 */
@ThreadSafe
public final class BlockingMap<K, V> extends AbstractBlockingMap<K, V> {
    @Override
    protected IObjectSynchronizer<V> makeSynchronizer() {
        return new LatchSynchronizer<>();
    }
}
