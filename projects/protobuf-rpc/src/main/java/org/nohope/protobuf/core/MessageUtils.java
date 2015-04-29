package org.nohope.protobuf.core;

import com.google.protobuf.Descriptors.FileDescriptor;
import com.google.protobuf.ExtensionRegistry;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.Message.Builder;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">Ketoth Xupack</a>
 * @since 2013-12-02 20:30
 */
public final class MessageUtils {
    private MessageUtils() {
    }

    public static ExtensionRegistry getExtensionRegistry(final FileDescriptor fileDescriptor) {
        final ExtensionRegistry extensionRegistry = ExtensionRegistry.newInstance();
        fileDescriptor.getExtensions().forEach(extensionRegistry::add);
        return extensionRegistry;
    }

    @SuppressWarnings("unchecked")
    public static <T extends Message> T repairedMessage(final T message,
                                                        final ExtensionRegistry registry)
            throws InvalidProtocolBufferException {
        if (message == null) {
            return null;
        }

        final Builder builder = message.newBuilderForType();
        return (T) builder.mergeFrom(message.toByteString(), registry).build();
    }
}
