package org.nohope.protobuf.rpc.exception;

import org.nohope.rpc.protocol.RPC;

import static org.nohope.rpc.protocol.RPC.ErrorCode.RPC_FAILED;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
 * @since 8/21/13 4:12 PM
 */
public class RpcException extends ServerSideException {
    private static final long serialVersionUID = 1L;

    public RpcException(final Throwable e,
                        final RPC.RpcRequest request,
                        final String message) {
        super(e, RPC_FAILED, request, message);
    }

    public RpcException(final RPC.RpcRequest request,
                        final String message) {
        super(RPC_FAILED, request, message);
    }
}
