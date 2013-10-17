package org.nohope.jaxb2.plugin.validation;

import javax.xml.namespace.QName;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">Ketoth Xupack</a>
 * @since 2013-10-16 13:59
 */
public class Customizations {
    public static final String NAMESPACE_URI = "http://no-hope.org/jaxb2/validation";
    public static final String BIND_COMPLEX_TYPE = "type";

    public static final String VALIDATOR_ATTRIBUTE_CLASS = "class";
    public static final String VALIDATOR_ATTRIBUTE_CONTEXT = "context";

    public static final QName BIND = new QName(NAMESPACE_URI, "bind");
    public static final QName BINDINGS = new QName(NAMESPACE_URI, "bindings");
    public static final QName VALIDATOR = new QName(NAMESPACE_URI, "validator");

    private Customizations() {
    }
}
