package org.nohope.jaxb2.plugin.metadata;

import javax.annotation.Nonnull;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">Ketoth Xupack</a>
 * @since 2013-12-23 12:59
 */
public final class MetadataUtils {

    private MetadataUtils() {
    }

    /**
     * Returns value of call chain applied to given object.
     */
    @SuppressWarnings("unchecked")
    public static <V> V getValue(@Nonnull final IMetadataHolder<?> object,
                                 @Nonnull final IDescriptor<V> descriptor)
            throws CallException {
        IValueDescriptor<?> current = object.getInstanceDescriptor();
        for (final IDescriptor<?> d : descriptor) {
            if (d.getParent() == null) {
                continue;
            }
            if (current == null) {
                throw new CallException(descriptor,
                        "Unexpected null value in call chain",
                        new NullPointerException());
            }
            current = current.getChild(d.getName());
            if (current.getParent() == null) {
                break;
            }
        }

        return (V) current.getValue();
    }
}
