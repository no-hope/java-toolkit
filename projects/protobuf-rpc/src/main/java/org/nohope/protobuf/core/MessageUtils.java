package org.nohope.protobuf.core;

import com.google.protobuf.Descriptors;
import com.google.protobuf.ExtensionRegistry;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">Ketoth Xupack</a>
 * @since 2013-12-02 20:30
 */
public class MessageUtils {
    private MessageUtils() {
    }

    public static ExtensionRegistry getExtensionRegistry(final Descriptors.FileDescriptor fileDescriptor) {
        final ExtensionRegistry extensionRegistry = ExtensionRegistry.newInstance();
        for (final Descriptors.FieldDescriptor descriptor : fileDescriptor.getExtensions()) {
            extensionRegistry.add(descriptor);
        }
        return extensionRegistry;
    }

    @SuppressWarnings("unchecked")
    public static <T extends Message> T repairedMessage(final T message,
                                                        final ExtensionRegistry registry)
            throws InvalidProtocolBufferException {
        if (message == null) {
            return null;
        }

        final Message.Builder builder = message.newBuilderForType();
        return (T) builder.mergeFrom(message.toByteString(), registry).build();
    }
}
