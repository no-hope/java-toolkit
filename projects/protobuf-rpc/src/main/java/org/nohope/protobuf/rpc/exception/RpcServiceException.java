package org.nohope.protobuf.rpc.exception;

import org.nohope.rpc.protocol.RPC;
import com.google.protobuf.ServiceException;

import static org.nohope.rpc.protocol.RPC.Error.Builder;
import static org.nohope.rpc.protocol.RPC.ErrorCode.RPC_ERROR;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
 * @since 8/21/13 4:12 PM
 */
public class RpcServiceException extends ServerSideException {
    private static final long serialVersionUID = 1L;

    public RpcServiceException(final ServiceException t,
                               final RPC.RpcRequest request,
                               final String message) {
        super(t, RPC_ERROR, request, message);
    }

    public RpcServiceException(final Throwable e,
                               final Builder builder,
                               final RPC.RpcRequest request) {
        super(builder, RPC_ERROR, e.getMessage(), request);
        initCause(e);
    }

    public static RpcServiceException wrapExpectedException(final ExpectedServiceException e, final RPC.RpcRequest req) {
        return new RpcServiceException(e, e.getErrorBuilder(), req);
    }
}
