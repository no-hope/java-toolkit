package org.nohope.jaxb2.plugin.validation;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">Ketoth Xupack</a>
 * @since 2013-10-16 12:14
 */
public class ValidationException extends Exception {
    private static final long serialVersionUID = 1L;

    public ValidationException() {
        super();
    }

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
