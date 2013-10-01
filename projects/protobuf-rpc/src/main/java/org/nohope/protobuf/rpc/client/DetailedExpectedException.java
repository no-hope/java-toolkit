package org.nohope.protobuf.rpc.client;

import org.nohope.rpc.protocol.RPC;
import com.google.protobuf.GeneratedMessage;
import com.google.protobuf.ServiceException;

import javax.annotation.Nullable;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
 * @since 2013-10-01 17:34
 */
public class DetailedExpectedException extends ServiceException {
    private static final long serialVersionUID = 1L;

    private final RPC.Error error;

    public DetailedExpectedException(final RPC.Error error) {
        super(new Throwable());
        this.error = error;
    }

    @Nullable
    public <T> T getDetailedReason(final GeneratedMessage.GeneratedExtension<RPC.Error, T> extension) {
        return error.getExtension(extension);
    }

    public RPC.Error getError() {
        return error;
    }
}
