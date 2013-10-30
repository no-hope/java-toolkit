package org.nohope.jaxb2.plugin.metadata;

import com.sun.istack.Nullable;
import org.nohope.reflection.TypeReference;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">Ketoth Xupack</a>
 * @since 2013-10-30 16:25
 */
public class AbstractValueDescriptor<T>
        extends AbstractDescriptor<T>
        implements IValueDescriptor<T> {

    private final IValueGetter<T> getter;

    public AbstractValueDescriptor(
            @Nullable final AbstractValueDescriptor<?> parent,
            @Nullable final String name, final TypeReference<T> type,
            final IValueGetter<T> getter) {
        super(parent, name, type);
        this.getter = getter;
    }

    @Override
    public T getValue() throws Exception {
        try {
            return getter.get();
        } catch (Exception e) {
            if (e instanceof CallException) {
                throw e;
            }
            throw new CallException(this, "Error while getting value of descriptor " + this , e);
        }
    }
}
