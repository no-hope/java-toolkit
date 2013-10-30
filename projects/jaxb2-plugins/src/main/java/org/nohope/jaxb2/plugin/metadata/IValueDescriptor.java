package org.nohope.jaxb2.plugin.metadata;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">Ketoth Xupack</a>
 * @since 2013-10-30 14:32
 */
public interface IValueDescriptor<T> extends IDescriptor<T> {
    T getValue() throws Exception;
}
