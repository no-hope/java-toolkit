package org.nohope.jaxb2.plugin.validation;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">Ketoth Xupack</a>
 * @since 2013-10-16 17:15
 */
public interface Validateable<C extends StaticValidationContext> {
    void validate(final C context) throws ValidationException;
}
