package org.nohope.jaxb2.plugin.metadata;

import org.nohope.reflection.TypeReference;

import javax.annotation.Nullable;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">Ketoth Xupack</a>
 * @since 2013-10-30 16:25
 */
public class ValueDescriptor<T>
        extends Descriptor<T>
        implements IValueDescriptor<T> {

    private final IValueGetter<T> getter;

    public ValueDescriptor(
            @Nullable final ValueDescriptor<?> parent,
            @Nullable final String name, final TypeReference<T> type,
            final IValueGetter<T> getter) {
        super(parent, name, type);
        this.getter = getter;
    }

    @Override
    public IValueDescriptor<?> getChild(final String name) {
        throw new IllegalArgumentException("No children found");
    }

    @Override
    public T getValue() throws CallException {
        try {
            return getter.get();
        } catch (final Exception e) {
            if (e instanceof CallException) {
                throw (CallException) e;
            }
            throw new CallException(this, "Error while getting value of descriptor " + this, e);
        }
    }
}
