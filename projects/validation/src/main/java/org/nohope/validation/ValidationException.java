package org.nohope.validation;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
 * @since 9/26/13 11:45 AM
 */
public class ValidationException extends Exception {
    private static final long serialVersionUID = 1L;

    public ValidationException(final String message) {
        super(message);
    }

    public ValidationException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public ValidationException(final Throwable cause) {
        super(cause);
    }
}
