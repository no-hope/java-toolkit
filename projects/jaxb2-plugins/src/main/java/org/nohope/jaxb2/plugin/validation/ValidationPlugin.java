package org.nohope.jaxb2.plugin.validation;

import com.sun.codemodel.JClass;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JVar;
import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.Plugin;
import com.sun.tools.xjc.model.CClassInfo;
import com.sun.tools.xjc.model.CCustomizable;
import com.sun.tools.xjc.model.CPluginCustomization;
import com.sun.tools.xjc.model.Model;
import com.sun.tools.xjc.outline.ClassOutline;
import com.sun.tools.xjc.outline.Outline;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.jvnet.jaxb2_commons.plugin.AbstractParameterizablePlugin;
import org.jvnet.jaxb2_commons.util.ClassUtils;
import org.nohope.jaxb2.plugin.metadata.MetadataPlugin;
import org.nohope.typetools.TStr;
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
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.jvnet.jaxb2_commons.util.CustomizationUtils.containsCustomization;
import static org.jvnet.jaxb2_commons.util.CustomizationUtils.findCustomization;
import static org.nohope.jaxb2.plugin.validation.Customizations.*;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">Ketoth Xupack</a>
 * @since 2013-10-16 12:13
 */
public class ValidationPlugin extends AbstractParameterizablePlugin {
    private final Map<String, Map.Entry<String, String>> validatorsMapping = new HashMap<>();

    @Override
    public String getOptionName() {
        return "Xvalidation";
    }

    @Override
    public String getUsage() {
        return "TBD";
    }

    private boolean metadataAvailable = false;

    @Override
    protected void init(final Options options) throws Exception {
        super.init(options);
        for (final Plugin activePlugin : options.activePlugins) {
            if (activePlugin instanceof MetadataPlugin) {
                metadataAvailable = true;
                break;
            }
        }
    }

    @Override
    public void postProcessModel(final Model model, final ErrorHandler errorHandler) {
        final boolean containsModelLevel = containsCustomization(model, BINDINGS);

        if (containsModelLevel) {
            final CPluginCustomization customization = findCustomization(model, BINDINGS);
            final NodeList childNodes = customization.element.getChildNodes();
            for (int i = 0; i < childNodes.getLength(); i++) {
                final Node item = childNodes.item(i);
                if (item instanceof Element) {
                    final Element element = validateBindElement((Element) item, true, errorHandler, model);
                    final String complexType = element.getAttribute(BIND_COMPLEX_TYPE);
                    final CClassInfo info = getComplexTypeByName(complexType, model, errorHandler);
                    final Element validationNode = getValidationNode(element, errorHandler, model);
                    addValidator(validationNode, info, errorHandler, model);
                }
            }
        }

        for (final CClassInfo classInfo : model.beans().values()) {
            final boolean containsClassLevel = containsCustomization(classInfo, BIND);

            if (containsClassLevel) {
                final CPluginCustomization customization = findCustomization(classInfo, BIND);
                final Element element = validateBindElement(customization.element, false, errorHandler, classInfo);
                final Element validationNode = getValidationNode(element, errorHandler, classInfo);
                addValidator(validationNode, classInfo, errorHandler, model);
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

    private static Element validateValidatorElement(final Element e,
                                                    final ErrorHandler errorHandler,
                                                    final CCustomizable customizable) {
        final NamedNodeMap attributes = e.getAttributes();
        for (int i = 0; i < attributes.getLength(); i++) {
            final Node item = attributes.item(i);
            final String name = item.getNodeName();
            if (!VALIDATOR_ATTRIBUTE_CLASS.equals(name)
                && !VALIDATOR_ATTRIBUTE_CONTEXT.equals(name)
                && !name.startsWith("xmlns:")) {
                throw fatal("validator element contains unknown '" + name + "' attribute", errorHandler, customizable);
            }
        }

        if (TStr.isEmpty(e.getAttribute(VALIDATOR_ATTRIBUTE_CLASS))
            || TStr.isEmpty(e.getAttribute(VALIDATOR_ATTRIBUTE_CONTEXT))) {
            throw fatal("validator element contains empty '"
                        + VALIDATOR_ATTRIBUTE_CLASS
                        + "' or '"
                        + VALIDATOR_ATTRIBUTE_CONTEXT
                        + "' attribute", errorHandler, customizable);
        }

        return e;
    }

    private void addValidator(final Element validationNode,
                              final CClassInfo info,
                              final ErrorHandler errorHandler,
                              final CCustomizable locator) {

        final Map.Entry<String, String> pair = new ImmutablePair<>(
                validationNode.getAttribute(VALIDATOR_ATTRIBUTE_CLASS),
                validationNode.getAttribute(VALIDATOR_ATTRIBUTE_CONTEXT));

        final Map.Entry<String, String> old = validatorsMapping.put(info.getName(), pair);
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
        return validateValidatorElement(validators.iterator().next(), errorHandler, locator);
    }

    @Override
    protected boolean run(final Outline outline,
                          final Options options) throws Exception {


        for (final ClassOutline classOutline : outline.getClasses()) {
            final JDefinedClass impl = classOutline.implClass;
            final String className = impl.binaryName();
            final JCodeModel model = impl.owner();
            if (validatorsMapping.containsKey(className)) {
                final Map.Entry<String, String> pair = validatorsMapping.get(className);

                final JClass validatorRef = model.ref(pair.getKey());
                final JClass contextRef = model.ref(pair.getValue());

                ClassUtils._implements(impl, model.ref(Validateable.class).narrow(contextRef));
                final JMethod validate = impl.method(JMod.PUBLIC, void.class, "validate");
                validate._throws(ValidationException.class);
                validate.annotate(Override.class);

                final JVar context = validate.param(JMod.FINAL, contextRef, "context");
                validate.body()
                        .add(JExpr._new(validatorRef)
                                  .invoke("validate")
                                  .arg(context)
                                  .arg(metadataAvailable ? JExpr.invoke("getInstanceDescriptor") : JExpr._this()));
            }
        }

        return true;
    }

    @Override
    public Collection<QName> getCustomizationElementNames() {
        return Arrays.asList(BINDINGS, BIND, VALIDATOR);
    }
}
