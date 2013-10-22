package org.nohope.protobuf.core.exception;

import org.junit.Test;
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
    public void repeatedExtension() {
        final ExpectedServiceException built = new ExpectedServiceException
                .Builder(new IllegalStateException())
                .addListExtension(Repeated.enumExtension,
                        Repeated.TestEnum.KIND3,
                        Repeated.TestEnum.KIND1,
                        Repeated.TestEnum.KIND2)
                .build();

        final RPC.Error error =
                built.getErrorBuilder()
                     .setErrorCode(BAD_REQUEST_DATA)
                     .setErrorMessage("test message")
                     .build();

        final List<Repeated.TestEnum> extensions =
                error.getExtension(Repeated.enumExtension);

        assertFalse(extensions.isEmpty());
        assertEquals(3, extensions.size());
        assertEquals(Repeated.TestEnum.KIND3, extensions.get(0));
        assertEquals(Repeated.TestEnum.KIND1, extensions.get(1));
        assertEquals(Repeated.TestEnum.KIND2, extensions.get(2));
    }
}
