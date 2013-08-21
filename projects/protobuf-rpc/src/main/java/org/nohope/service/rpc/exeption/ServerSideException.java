package org.nohope.service.rpc.exeption;

import org.nohope.protocol.RPC;

import static org.nohope.protocol.RPC.RpcRequest;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
 * @since 8/21/13 4:15 PM
 */
public class ServerSideException extends Exception {
    private static final long serialVersionUID = 1L;
    private final RPC.ErrorCode code;
    private final RpcRequest request;


    public ServerSideException(final Throwable t,
                               final RPC.ErrorCode code,
                               final RpcRequest request,
                               final String message) {
        this(code, request, message);
        initCause(t);
    }

    public ServerSideException(final RPC.ErrorCode code,
                               final RpcRequest request,
                               final String message) {
        super(message);
        this.request = request;
        this.code = code;
    }

    public RPC.ErrorCode getErrorCode() {
        return code;
    }

    public RpcRequest getRequest() {
        return request;
    }
}
