package org.nohope.protobuf.core.exception;

import com.google.protobuf.Descriptors;
import com.google.protobuf.ExtensionRegistry;
import com.google.protobuf.InvalidProtocolBufferException;
import org.junit.Test;
import org.nohope.protobuf.core.MessageUtils;
import org.nohope.protocol.Ordinar;
import org.nohope.protocol.Repeated;
import org.nohope.rpc.protocol.RPC;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.nohope.rpc.protocol.RPC.ErrorCode.BAD_REQUEST_DATA;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">Ketoth Xupack</a>
 * @since 2013-10-22 22:56
 */
public class ExpectedServiceExceptionTest {

    @Test
    public void extension() {
        for (final Ordinar.OrdinarEnum value : Ordinar.OrdinarEnum.values()) {
            final ExpectedServiceException built = new ExpectedServiceException
                    .Builder(new IllegalStateException())
                    .addExtension(Ordinar.ordinarExtension, value)
                    .build();

            final RPC.Error error = cloned(Ordinar.getDescriptor(),
                    built.getErrorBuilder()
                         .setErrorCode(BAD_REQUEST_DATA)
                         .setErrorMessage("test message")
                         .build());

            final Ordinar.OrdinarEnum extension = error.getExtension(Ordinar.ordinarExtension);
            assertEquals(value, extension);
        }
    }

    @Test
    public void repeatedExtension() {
        final ExpectedServiceException built = new ExpectedServiceException
                .Builder(new IllegalStateException())
                .addListExtension(Repeated.enumExtension,
                        Repeated.TestEnum.KIND3,
                        Repeated.TestEnum.KIND1,
                        Repeated.TestEnum.KIND2)
                .build();

        final RPC.Error error = cloned(Repeated.getDescriptor(),
                built.getErrorBuilder()
                     .setErrorCode(BAD_REQUEST_DATA)
                     .setErrorMessage("test message")
                     .build());

        final List<Repeated.TestEnum> extensions =
                error.getExtension(Repeated.enumExtension);

        assertFalse(extensions.isEmpty());
        assertEquals(3, extensions.size());
        assertEquals(Repeated.TestEnum.KIND3, extensions.get(0));
        assertEquals(Repeated.TestEnum.KIND1, extensions.get(1));
        assertEquals(Repeated.TestEnum.KIND2, extensions.get(2));
    }

    private static RPC.Error cloned(final Descriptors.FileDescriptor d, final RPC.Error error) {
        final ExtensionRegistry extensionRegistry = MessageUtils.getExtensionRegistry(d);

        try {
            final RPC.Error.Builder builder =
                    RPC.Error
                       .getDefaultInstance()
                       .newBuilderForType()
                       .mergeFrom(error.toByteString(), extensionRegistry);
            return builder.build();
        } catch (InvalidProtocolBufferException e) {
            throw new IllegalStateException(e);
        }
    }
}
