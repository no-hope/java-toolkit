package org.nohope.jaxb2.plugin.metadata;

import org.nohope.reflection.TypeReference;

import javax.annotation.Nonnull;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
 * @since 9/30/13 3:04 PM
 */
public class SimpleDescriptor<T> implements IDescriptor<T> {
    private final CallChain chain;
    private final TypeReference<T> clazz;

    public SimpleDescriptor(@Nonnull final TypeReference<T> clazz, @Nonnull final CallChain chain) {
        this.chain = chain;
        this.clazz = clazz;
    }

    public SimpleDescriptor(@Nonnull final TypeReference<T> clazz) {
        this(clazz, new CallChain());
    }

    @Nonnull
    @Override
    public TypeReference<T> getFieldType() {
        return clazz;
    }

    @Nonnull
    @Override
    public CallChain getCallChain() {
        return chain;
    }
}
