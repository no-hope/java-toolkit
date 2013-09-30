package org.nohope.jaxb2.plugin.metadata;

import com.sun.codemodel.JAnnotationUse;
import com.sun.codemodel.JAnnotationValue;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JFormatter;
import com.sun.codemodel.JInvocation;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JType;
import com.sun.codemodel.JVar;
import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.outline.ClassOutline;
import com.sun.tools.xjc.outline.Outline;
import org.jvnet.jaxb2_commons.plugin.AbstractParameterizablePlugin;
import org.jvnet.jaxb2_commons.util.ClassUtils;
import org.nohope.reflection.TypeReference;
import org.xml.sax.ErrorHandler;

import javax.xml.bind.annotation.XmlElement;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Map;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
 * @since 9/30/13 3:44 PM
 */
public class MetadataPlugin extends AbstractParameterizablePlugin {

    @Override
    public String getOptionName() {
        return "Xmetadata";
    }

    @Override
    public String getUsage() {
        return "TBD";
    }

    @Override
    public boolean run(final Outline outline,
                       final Options opt,
                       final ErrorHandler errorHandler) {

        final Collection<? extends ClassOutline> classes = outline.getClasses();
        for (final ClassOutline classOutline : classes) {
            ClassUtils._implements(classOutline.implClass,
                    classOutline.implClass.owner().ref(IMetadataHolder.class));
        }

        for (final ClassOutline classOutline : classes) {
            processClassOutline(classOutline);
        }

        return true;
    }

    protected static void processClassOutline(final ClassOutline classOutline) {
        final JDefinedClass theClass = classOutline.implClass;
        generateDescriptor(theClass);
    }

    private static void generateDescriptor(final JDefinedClass theClass) {
        final JCodeModel codeModel = theClass.owner();
        final JDefinedClass descriptor;
        final JDefinedClass descriptorInterface;

        try {
            descriptorInterface =
                    theClass._interface("IDescriptor")
                            ._extends(codeModel.ref(IDescriptor.class).narrow(theClass))
                            ;


            descriptor =
                    theClass._class(JMod.PUBLIC | JMod.STATIC, "Descriptor")
                            ._extends(codeModel.ref(SimpleDescriptor.class).narrow(theClass))
                            ._implements(descriptorInterface)
                            ;

        } catch (JClassAlreadyExistsException e) {
            throw new IllegalStateException(e);
        }

        theClass.method(JMod.PUBLIC | JMod.STATIC, descriptorInterface, "descriptor")
                .body()
                ._return(JExpr._new(descriptor));

        final JInvocation thisTypeRef =
                JExpr._new(codeModel.anonymousClass(codeModel.ref(TypeReference.class).narrow(theClass)));

        // default constructor
        descriptor.constructor(JMod.PROTECTED)
                  .body()
                  .invoke("super")
                  .arg(thisTypeRef);

        final JMethod constructor2 = descriptor.constructor(JMod.PROTECTED);
        final JVar chain = constructor2.param(JMod.FINAL, codeModel.ref(CallChain.class), "chain");

        constructor2.body().invoke("super").arg(thisTypeRef).arg(chain);

        for (final JMethod method : theClass.methods()) {
            final String name = method.name();

            final String fieldMetaName = getAssociatedFieldName(theClass, method);
            if (fieldMetaName == null) {
                continue;
            }

            final JType methodType = method.type();
            final JClass concreteType;
            final JClass abstractType;
            final JInvocation expression;

            if (methodType.isReference() && codeModel.ref(IMetadataHolder.class).isAssignableFrom((JClass) methodType)) {
                final JClass castedType = (JClass) methodType;
                concreteType = codeModel.directClass(castedType.name() + ".Descriptor");
                abstractType = codeModel.directClass(castedType.name() + ".IDescriptor");
                expression = JExpr._new(concreteType);
            } else {
                final JClass returnClass = methodType.isPrimitive() ? methodType.boxify() : (JClass) methodType;
                concreteType = codeModel.ref(SimpleDescriptor.class).narrow(returnClass);
                abstractType = codeModel.ref(IDescriptor.class).narrow(returnClass);

                final JDefinedClass returnTypeRef =
                        codeModel.anonymousClass(
                                codeModel.ref(TypeReference.class)
                                         .narrow(returnClass));

                final JClass java7concreteClass = codeModel.ref(SimpleDescriptor.class).narrow(new JClass[]{});
                expression = JExpr._new(java7concreteClass).arg(JExpr._new(returnTypeRef));
            }

            descriptorInterface.method(JMod.NONE, abstractType, name);

            final JMethod descriptorMethod = descriptor.method(JMod.PUBLIC, concreteType, name);
            final JInvocation addToChain =
                    JExpr._this()
                         .invoke("getCallChain")
                         .invoke("add")
                         .arg(JExpr._this())
                         .arg(fieldMetaName);

            descriptorMethod.javadoc().add("This method reflects '" + fieldMetaName + "' field metadata.\n");
            descriptorMethod.javadoc().addReturn().add(concreteType.erasure().fullName());
            descriptorMethod.javadoc().add("@see");
            descriptorMethod.javadoc().add(IDescriptor.class.getCanonicalName());

            descriptorMethod.annotate(Override.class);
            descriptorMethod.body()._return(expression.arg(addToChain));
        }
    }

    private static String getAssociatedFieldName(final JDefinedClass theClass, final JMethod method) {
        final JCodeModel codeModel = theClass.owner();
        final String name = method.name();

        if (!name.startsWith("get")) {
            return null;
        }

        final String string = name.substring(3, name.length());
        final String fieldName =
                Character.toLowerCase(string.charAt(0))
                + (string.length() > 1 ? string.substring(1) : "");

        final JFieldVar field = theClass.fields().get(fieldName);
        if (field == null) {
            return null;
        }

        for (final JAnnotationUse annotation : field.annotations()) {
            if (annotation.getAnnotationClass().isAssignableFrom(codeModel.ref(XmlElement.class))) {
                final Map<String,JAnnotationValue> members = annotation.getAnnotationMembers();
                if (!members.containsKey("name")) {
                    continue;
                }

                final JAnnotationValue annotationFieldName = members.get("name");

                final StringWriter writer = new StringWriter();
                final JFormatter f = new JFormatter(writer);
                annotationFieldName.generate(f);

                final String value = writer.toString();
                final String actualName = value.substring(1, value.length() - 1);

                if ("".equals(actualName)) {
                    throw new IllegalStateException();
                }

                if ("##default".equals(actualName)) {
                    return fieldName;
                }

                return actualName;
            }
        }

        return fieldName;
    }
}

