package org.nohope.jaxb2.plugin.validation;

import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JVar;
import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.model.CClassInfo;
import com.sun.tools.xjc.model.CCustomizable;
import com.sun.tools.xjc.model.CPluginCustomization;
import com.sun.tools.xjc.model.Model;
import com.sun.tools.xjc.outline.ClassOutline;
import com.sun.tools.xjc.outline.Outline;
import org.jvnet.jaxb2_commons.plugin.AbstractParameterizablePlugin;
import org.jvnet.jaxb2_commons.util.ClassUtils;
import org.jvnet.jaxb2_commons.util.CustomizationUtils;
import org.nohope.IMatcher;
import org.nohope.reflection.IntrospectionUtils;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.annotation.Nonnull;
import javax.xml.namespace.QName;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.nohope.jaxb2.plugin.validation.Customizations.BIND_COMPLEX_TYPE;
import static org.nohope.jaxb2.plugin.validation.Customizations.NAMESPACE_URI;
import static org.nohope.jaxb2.plugin.validation.Customizations.VALIDATOR;
import static org.nohope.reflection.ModifierMatcher.*;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">Ketoth Xupack</a>
 * @since 2013-10-16 12:13
 */
public class ValidationPlugin extends AbstractParameterizablePlugin {
    private final Map<String, StaticValidator> validatorsMapping = new HashMap<>();

    @Override
    public String getOptionName() {
        return "Xvalidation";
    }

    @Override
    public String getUsage() {
        return "TBD";
    }

    @Override
    public void postProcessModel(final Model model,
                                 final ErrorHandler errorHandler) {
        final boolean containsModelLevel =
                CustomizationUtils.containsCustomization(model, Customizations.BINDINGS);

        if (containsModelLevel) {
            final CPluginCustomization customization =
                    CustomizationUtils.findCustomization(model, Customizations.BINDINGS);
            final NodeList childNodes = customization.element.getChildNodes();
            for (int i = 0; i < childNodes.getLength(); i++) {
                final Node item = childNodes.item(i);
                if (item instanceof Element) {
                    final Element element = validateBindElement((Element) item, true, errorHandler, model);
                    final String complexType = element.getAttribute(BIND_COMPLEX_TYPE);
                    final CClassInfo info = getComplexTypeByName(complexType, model, errorHandler);
                    final Element validationNode = getValidationNode(element, errorHandler, model);
                    addValidator(reflectValidator(validationNode, errorHandler, model), info, errorHandler, model);
                }
            }
        }

        for (final CClassInfo classInfo : model.beans().values()) {
            final boolean containsClassLevel =
                    CustomizationUtils.containsCustomization(classInfo, Customizations.BIND);

            if (containsClassLevel) {
                final CPluginCustomization customization =
                        CustomizationUtils.findCustomization(classInfo, Customizations.BIND);

                final Element element = validateBindElement(customization.element, false, errorHandler, classInfo);
                final Element validationNode = getValidationNode(element, errorHandler, classInfo);
                addValidator(reflectValidator(validationNode, errorHandler, model), classInfo, errorHandler, model);
            }
        }
    }

    private static Element validateBindElement(final Element e,
                                               final boolean type,
                                               final ErrorHandler errorHandler,
                                               final CCustomizable customizable) {
        final NamedNodeMap attributes = e.getAttributes();
        for (int i = 0; i < attributes.getLength(); i++) {
            final Node item = attributes.item(i);
            final String name = item.getNodeName();
            if (!BIND_COMPLEX_TYPE.equals(name) && !name.startsWith("xmlns:")) {
                throw fatal("bind element contains unknown '" + name + "' attribute", errorHandler, customizable);
            }
        }

        if (e.hasAttribute(BIND_COMPLEX_TYPE) != type) {
            throw fatal("bind element should "
                        + (type ? "" : "not ") + "contain '" + BIND_COMPLEX_TYPE + "' attribute",
                    errorHandler, customizable);
        }

        return e;
    }

    private void addValidator(final StaticValidator validator,
                              final CClassInfo info,
                              final ErrorHandler errorHandler,
                              final CCustomizable locator) {
        final StaticValidator old = validatorsMapping.put(info.getName(), validator);
        if (old != null) {
            throw fatal("Too many bindings found for xsd type " + info.getTypeName().getLocalPart(),
                    errorHandler, locator);
        }
    }

    private static CClassInfo getComplexTypeByName(@Nonnull final String name,
                                                   final Model model,
                                                   final ErrorHandler errorHandler) {
        final Set<CClassInfo> collected = new HashSet<>();
        for (final CClassInfo info : model.beans().values()) {
            if (info.getTypeName().getLocalPart().equals(name)) {
                collected.add(info);
            }
        }

        if (collected.size() != 1) {
            throw fatal("exactly one complex type should be found for name '"
                        + name
                        + "', but "
                        + collected.size()
                        + " found",
                    errorHandler, model);
        }

        return collected.iterator().next();
    }

    @SuppressWarnings("unchecked")
    public static StaticValidator reflectValidator(final Element node,
                                                   final ErrorHandler errorHandler,
                                                   final CCustomizable locator) {

        final String validatorFQDN = node.getAttribute("class");
        try {
            final Class<?> clazz = Class.forName(validatorFQDN);
            if (!IntrospectionUtils.instanceOf(clazz, StaticValidator.class)) {
                throw fatal("Validator "
                            + validatorFQDN
                            + " must implement "
                            + StaticValidator.class.getCanonicalName()
                            + " interface",
                        errorHandler, locator);
            }

            final Class<? extends StaticValidator> validatorClass =
                    (Class<? extends StaticValidator>) clazz;

            final Set<Constructor<? extends StaticValidator>> constructors =
                    IntrospectionUtils.searchConstructors(validatorClass,
                            new IMatcher<Constructor<? extends StaticValidator>>() {
                                @Override
                                public boolean matches(final Constructor<? extends StaticValidator> obj) {
                                    return obj.getTypeParameters().length == 0
                                           && PUBLIC.matches(obj.getModifiers());
                                }
                            });

            if (constructors.size() != 1) {
                throw fatal("No public default constructor found for validator " + validatorFQDN,
                        errorHandler, locator);
            }

            final StaticValidator validatorObject = constructors.iterator().next().newInstance();
            final Class contextClass = validatorObject.getContextClass().getTypeClass();
            if (contextClass.isLocalClass() || or(STATIC, ABSTRACT).matches(contextClass.getModifiers())) {
                throw fatal("Validation context "
                            + contextClass.getCanonicalName()
                            + " defined in "
                            + validatorFQDN
                            + " must be public and non-local",
                        errorHandler, locator);
            }

            return validatorObject;
        } catch (ClassNotFoundException e) {
            throw fatal("unable to locate validator " + validatorFQDN, e, errorHandler, locator);
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw fatal("unable to instantiate validator " + validatorFQDN, e, errorHandler, locator);
        }
    }

    private static RuntimeException fatal(final String message,
                                          final Exception ex,
                                          final ErrorHandler errorHandler,
                                          final CCustomizable locator) {
        try {
            final SAXParseException exception = new SAXParseException(message, locator.getLocator(), ex);
            errorHandler.fatalError(exception);
            return new IllegalStateException(exception);
        } catch (final SAXException e) {
            return new IllegalStateException(e);
        }
    }

    private static RuntimeException fatal(final String message,
                                          final ErrorHandler errorHandler,
                                          final CCustomizable locator) {
        try {
            final SAXParseException exception = new SAXParseException(message, locator.getLocator());
            errorHandler.fatalError(exception);
            return new IllegalStateException(exception);
        } catch (final SAXException e) {
            return new IllegalStateException(e);
        }
    }

    private static Element getValidationNode(final Element customizationElement,
                                             final ErrorHandler errorHandler,
                                             final CCustomizable locator) {
        final Set<Element> validators = new HashSet<>();
        final NodeList childNodes = customizationElement.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            final Node node = childNodes.item(i);
            if (node instanceof Text) {
                continue;
            }

            if (!NAMESPACE_URI.equals(node.getNamespaceURI())
                || !VALIDATOR.getLocalPart().equals(node.getLocalName())) {
                throw fatal("unsupported bind child node found: " + node.getNodeName(), errorHandler, locator);
            }

            if (!(node instanceof Element)) {
                throw fatal("validator element has unknown type " + node.getNodeType(), errorHandler, locator);
            }

            validators.add(((Element) node));
        }

        if (validators.size() != 1) {
            throw fatal("bind node must contains exactly one validator child node", errorHandler, locator);
        }

        // TODO: test class attribute exists
        return validators.iterator().next();
    }

    @Override
    protected boolean run(final Outline outline,
                          final Options options) throws Exception {
        for (final ClassOutline classOutline : outline.getClasses()) {
            final JDefinedClass impl = classOutline.implClass;
            final String className = impl.binaryName();
            if (validatorsMapping.containsKey(className)) {
                final StaticValidator validator = validatorsMapping.get(className);

                final Class contextClass = validator.getContextClass().getTypeClass();
                final JCodeModel model = impl.owner();
                ClassUtils._implements(impl, model.ref(Validateable.class).narrow(model.ref(contextClass)));

                final JMethod validate = impl.method(JMod.PUBLIC, void.class, "validate");
                validate._throws(ValidationException.class);
                validate.annotate(Override.class);

                final JVar context = validate.param(JMod.FINAL, contextClass, "context");
                validate.body()
                        .add(JExpr._new(
                                model.ref(validator.getClass())
                        ).invoke("validate").arg(context).arg(JExpr._this())
                );
            }
        }

        return true;
    }

    @Override
    public Collection<QName> getCustomizationElementNames() {
        return Arrays.asList(Customizations.BINDINGS,
                Customizations.BIND,
                VALIDATOR);
    }
}
