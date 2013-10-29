package org.nohope.protobuf.core.exception;

import com.google.protobuf.ServiceException;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
 * @since 8/22/13 1:40 PM
 */
public class UnexpectedServiceException extends ServiceException {
    private static final long serialVersionUID = 1L;

    public UnexpectedServiceException(final Throwable cause) {
        super(cause);
    }
}
