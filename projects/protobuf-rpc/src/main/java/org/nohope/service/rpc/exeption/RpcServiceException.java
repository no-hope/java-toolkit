package org.nohope.service.rpc.exeption;

import org.nohope.protocol.RPC;
import com.google.protobuf.ServiceException;

import static org.nohope.protocol.RPC.ErrorCode.RPC_ERROR;

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
}
