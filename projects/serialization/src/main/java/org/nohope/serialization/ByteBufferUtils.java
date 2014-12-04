package org.nohope.serialization;

import java.nio.ByteBuffer;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
 */
public final class ByteBufferUtils {
    private ByteBufferUtils() {
    }

    public static byte[] continuousArray(final ByteBuffer buffer) {
        final byte[] payload = new byte[buffer.remaining()];
        buffer.get(payload);
        return payload;
    }
}
