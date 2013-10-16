package org.nohope.protobuf.core.exception;

import com.google.protobuf.ServiceException;
import org.junit.Test;
import org.nohope.rpc.protocol.RPC;

import java.util.concurrent.TimeoutException;

import static org.junit.Assert.assertSame;
import static org.nohope.test.SerializationUtils.cloneJava;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">Ketoth Xupack</a>
 * @since 2013-10-16 12:29
 */
public class ExceptionTest {
    @Test
    public void serialization() {
        cloneJava(new RpcException(
                new IllegalStateException(),
                RPC.RpcRequest.getDefaultInstance(),
                "test"
        ));
        cloneJava(new RpcException(
                RPC.RpcRequest.getDefaultInstance(),
                "test"
        ));

        cloneJava(new RpcServiceException(
                new ServiceException("xxx"),
                RPC.RpcRequest.getDefaultInstance(),
                "test"
        ));

        final TimeoutException te = new TimeoutException();
        final RpcTimeoutException rte = new RpcTimeoutException(te);
        assertSame(te, rte.getCause());
        cloneJava(rte);

        cloneJava(new NoSuchServiceException(RPC.RpcRequest.getDefaultInstance(), "test"));
        cloneJava(new NoSuchServiceMethodException(RPC.RpcRequest.getDefaultInstance(), "test"));
        cloneJava(new InvalidRpcRequestException(
                new IllegalStateException(),
                RPC.RpcRequest.getDefaultInstance(),
                "test"));
    }
}
