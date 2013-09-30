package org.nohope.jaxb2.plugin.metadata;

import org.nohope.reflection.TypeReference;

/**
 * Bean descriptor.
 *
 * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
 * @since 9/30/13 3:02 PM
 */
public interface IDescriptor<T> {
    TypeReference<T> getFieldType();
    CallChain getCallChain();
}
