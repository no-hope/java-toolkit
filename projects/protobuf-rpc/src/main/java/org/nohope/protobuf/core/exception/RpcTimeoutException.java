package org.nohope.protobuf.core.exception;

import com.google.protobuf.ServiceException;

import java.util.concurrent.TimeoutException;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
 * @since 2013-10-01 19:17
 */
public class RpcTimeoutException extends ServiceException {
    private static final long serialVersionUID = 1L;

    public RpcTimeoutException(final TimeoutException cause) {
        super(cause);
    }

    @Override
    public synchronized TimeoutException getCause() {
        return (TimeoutException) super.getCause();
    }
}
