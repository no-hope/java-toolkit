package org.nohope.jaxb2.plugin.metadata;

/**
 * Marker interface for models containing metadata-driven descriptors.
 *
 * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
 * @since 9/30/13 3:19 PM
 */
public interface IMetadataHolder<D extends IValueDescriptor<?>> {
    D getInstanceDescriptor();
}
