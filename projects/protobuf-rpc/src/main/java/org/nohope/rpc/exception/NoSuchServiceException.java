package org.nohope.rpc.exception;

import org.nohope.rpc.protocol.RPC;

import static org.nohope.rpc.protocol.RPC.ErrorCode.SERVICE_NOT_FOUND;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
 * @since 8/21/13 4:11 PM
 */
public class NoSuchServiceException extends ServerSideException {
    private static final long serialVersionUID = 1L;

    public NoSuchServiceException(final RPC.RpcRequest request, final String message) {
        super(SERVICE_NOT_FOUND, request, message);
    }
}
