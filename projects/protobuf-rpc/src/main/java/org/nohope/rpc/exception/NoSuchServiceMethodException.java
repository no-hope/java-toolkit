package org.nohope.rpc.exception;

import org.nohope.rpc.protocol.RPC;

import static org.nohope.rpc.protocol.RPC.ErrorCode.METHOD_NOT_FOUND;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
 * @since 8/21/13 4:12 PM
 */
public class NoSuchServiceMethodException extends ServerSideException {
    private static final long serialVersionUID = 1L;

    public NoSuchServiceMethodException(final RPC.RpcRequest request, final String message) {
        super(METHOD_NOT_FOUND, request, message);
    }
}
