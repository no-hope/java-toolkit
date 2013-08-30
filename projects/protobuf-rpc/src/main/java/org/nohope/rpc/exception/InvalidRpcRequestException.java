package org.nohope.rpc.exception;

import org.nohope.rpc.protocol.RPC;

import static org.nohope.rpc.protocol.RPC.ErrorCode.INVALID_REQUEST_PROTO;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
 * @since 8/21/13 4:12 PM
 */
public class InvalidRpcRequestException extends ServerSideException {
    private static final long serialVersionUID = 1L;

    public InvalidRpcRequestException(final Throwable e, final RPC.RpcRequest request, final String message) {
        super(e, INVALID_REQUEST_PROTO, request, message);
    }
}
