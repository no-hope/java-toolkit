package org.nohope.jaxb2.plugin.metadata;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">Ketoth Xupack</a>
 * @since 2013-10-30 17:57
 */
public class PasstroughGetter<R> implements IValueGetter<R> {
    private final R value;

    public PasstroughGetter(final R value) {
        this.value = value;
    }

    @Override
    public R get() throws Exception {
        return value;
    }
}
