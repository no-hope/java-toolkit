package org.nohope.rpc.rpc.exception;

import org.nohope.rpc.protocol.RPC;

import static org.nohope.rpc.protocol.RPC.RpcRequest;
import static org.nohope.rpc.protocol.RPC.Error;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
 * @since 8/21/13 4:15 PM
 */
public class ServerSideException extends Exception {
    private static final long serialVersionUID = 1L;
    private final RpcRequest request;
    private final Error error;


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
        this(Error.newBuilder()
                  .setErrorCode(code)
                  .setErrorMessage(message)
                  .build(), request);
    }

    protected ServerSideException(final Error.Builder builder,
                                  final RPC.ErrorCode code,
                                  final String message,
                                  final RpcRequest request) {
        this(builder.setErrorMessage(message).setErrorCode(code).build(), request);
    }

    public ServerSideException(final Error error,
                               final RpcRequest request) {
        super(error.getErrorMessage());
        this.request = request;
        this.error = error;
    }


    public Error getError() {
        return error;
    }

    public RpcRequest getRequest() {
        return request;
    }
}
