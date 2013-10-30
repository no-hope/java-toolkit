package org.nohope.jaxb2.plugin.metadata;

import org.nohope.reflection.TypeReference;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">Ketoth Xupack</a>
 * @since 2013-10-30 16:10
 */
public interface IDescriptor<T> extends Iterable<IDescriptor<?>> {
    IDescriptor<?> getParent();
    String getName();
    TypeReference<T> getType();
}
