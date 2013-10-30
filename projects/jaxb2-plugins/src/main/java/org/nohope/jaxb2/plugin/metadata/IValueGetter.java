package org.nohope.jaxb2.plugin.metadata;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">Ketoth Xupack</a>
 * @since 2013-10-30 15:00
 */
public interface IValueGetter<R> {
    R get() throws Exception;
}
