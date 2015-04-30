package org.nohope.jaxb2.plugin.metadata;

import com.sun.codemodel.*;
import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.model.Aspect;
import com.sun.tools.xjc.model.CClassInfo;
import com.sun.tools.xjc.model.CTypeInfo;
import com.sun.tools.xjc.outline.ClassOutline;
import com.sun.tools.xjc.outline.FieldOutline;
import com.sun.tools.xjc.outline.Outline;
import org.jvnet.jaxb2_commons.plugin.AbstractParameterizablePlugin;
import org.jvnet.jaxb2_commons.util.ClassUtils;
import org.nohope.jaxb2.plugin.Utils;
import org.nohope.reflection.TypeReference;
import org.xml.sax.ErrorHandler;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import java.io.StringWriter;
import java.util.Collection;
import java.util.HashMap;
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
            final Map<String, JClass> collectionTypes = new HashMap<>();
            for (final FieldOutline field : classOutline.getDeclaredFields()) {
                if (field.getPropertyInfo().isCollection()) {
                    final Collection<? extends CTypeInfo> references = field.getPropertyInfo().ref();
                    if (references.size() == 1) { // FIXME: more than one?
                        final CTypeInfo ref = references.iterator().next();
                        if (ref instanceof CClassInfo) {
                            final CClassInfo collectionType = (CClassInfo) ref;
                            collectionTypes.put(field.getPropertyInfo().getName(false),
                                    collectionType.getType().toType(outline, Aspect.IMPLEMENTATION));
                        }
                    }
                }
            }

            final JDefinedClass c = classOutline.implClass;
            for (final Map.Entry<String, JClass> entry : collectionTypes.entrySet()) {
                c.fields()
                 .get(entry.getKey())
                 .annotate(CollectionType.class)
                 .param("value", entry.getValue());
            }

            final JCodeModel cm = c.owner();
            ClassUtils._implements(c, cm.ref(IMetadataHolder.class)
                                        .narrow(cm.directClass(c.name() + ".IInstanceDescriptor")));
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

        final JDefinedClass classLevelDescriptorInterface;
        final JDefinedClass classLevelDescriptor;

        final JDefinedClass instanceLevelDescriptorInterface;
        final JDefinedClass instanceLevelDescriptor;

        final JClass abstractDescriptor = codeModel.ref(Descriptor.class);
        final JClass abstractValueDescriptor = codeModel.ref(ValueDescriptor.class);
        final JClass descriptorInterface = codeModel.ref(IDescriptor.class);
        final JClass valueDescriptor = codeModel.ref(IValueDescriptor.class);

        try {
            // class-level descriptors
            classLevelDescriptorInterface =
                    theClass._interface("IClassDescriptor")
                            ._extends(descriptorInterface.narrow(theClass))
                            ;

            classLevelDescriptor =
                    theClass._class(JMod.PUBLIC | JMod.STATIC, "ClassDescriptor")
                            ._extends(abstractDescriptor.narrow(theClass))
                            ._implements(classLevelDescriptorInterface)
                            ;

            // class-level descriptors
            instanceLevelDescriptorInterface =
                    theClass._interface("IInstanceDescriptor")
                            ._extends(classLevelDescriptorInterface)
                            ._implements(valueDescriptor.narrow(theClass))
                            ;

            instanceLevelDescriptor =
                    theClass._class(JMod.PUBLIC | JMod.STATIC, "InstanceDescriptor")
                            ._extends(abstractValueDescriptor.narrow(theClass))
                            ._implements(instanceLevelDescriptorInterface)
                            ;

        } catch (JClassAlreadyExistsException e) {
            throw new IllegalStateException(e);
        }

        // public getter methods

        theClass.method(JMod.PUBLIC | JMod.STATIC, classLevelDescriptorInterface, "getClassDescriptor")
                .body()
                ._return(JExpr._new(classLevelDescriptor)
                              .arg(JExpr._null())
                              .arg(JExpr._null())
                );

        final JMethod instancedDescriptorGetter =
                theClass.method(JMod.PUBLIC, instanceLevelDescriptorInterface, "getInstanceDescriptor");
        instancedDescriptorGetter
                .body()
                ._return(JExpr._new(instanceLevelDescriptor)
                              .arg(JExpr._null())
                              .arg(JExpr._null())
                              .arg(JExpr._new(codeModel.ref(PasstroughGetter.class).narrow(theClass)).arg(JExpr._this())));
        instancedDescriptorGetter.annotate(Override.class);
        instancedDescriptorGetter.annotate(XmlTransient.class);

        final JClass typeReference = codeModel.ref(TypeReference.class);
        final JInvocation thisTypeRef = JExpr._new(codeModel.anonymousClass(typeReference.narrow(theClass)));

        // class-level descriptor constructor
        {
            final JClass parentDescriptor = abstractDescriptor.narrow(codeModel.wildcard());
            final JMethod classLevelDescriptorConstructor = classLevelDescriptor.constructor(JMod.PROTECTED);
            final JVar parentVar = classLevelDescriptorConstructor.param(JMod.FINAL, parentDescriptor, "parent");
            final JVar nameVar = classLevelDescriptorConstructor.param(JMod.FINAL, String.class, "name");
            classLevelDescriptorConstructor
                    .body()
                    .invoke("super")
                    .arg(parentVar)
                    .arg(nameVar)
                    .arg(thisTypeRef);
        }

        // instance-level descriptor constructor
        {
            final JClass parentDescriptor = abstractValueDescriptor.narrow(codeModel.directClass("?"));
            final JClass getter = codeModel.ref(IValueGetter.class).narrow(theClass);

            final JMethod instanceLevelDescriptorConstructor = instanceLevelDescriptor.constructor(JMod.PROTECTED);
            final JVar parentVar = instanceLevelDescriptorConstructor.param(JMod.FINAL, parentDescriptor, "parent");
            final JVar nameVar = instanceLevelDescriptorConstructor.param(JMod.FINAL, String.class, "name");
            final JVar getterVar = instanceLevelDescriptorConstructor.param(JMod.FINAL, getter, "getter");
            instanceLevelDescriptorConstructor
                    .body()
                    .invoke("super")
                    .arg(parentVar)
                    .arg(nameVar)
                    .arg(thisTypeRef)
                    .arg(getterVar)
            ;
        }


        final Map<String, JMethod> instanceMethods = new HashMap<>();
        final Map<String, JMethod> classMethods = new HashMap<>();

        // FIXME: iterate over fields
        for (final JMethod method : theClass.methods()) {
            final String name = method.name();

            final String fieldMetaName = getAssociatedFieldName(theClass, method);
            if (fieldMetaName == null) {
                continue;
            }

            final JType methodType = method.type();
            final JClass concreteClassLevelReturnType;
            final JClass abstractClassLevelReturnType;
            final JClass abstractInstanceLevelReturnType;
            final JClass concreteInstanceLevelReturnType;
            final JInvocation classLevelMethodExpression;
            final JInvocation instanceLevelMethodExpression;

            final JDefinedClass valueGetter =
                    codeModel.anonymousClass(codeModel.ref(IValueGetter.class).narrow(methodType));
            valueGetter.method(JMod.PUBLIC, methodType.boxify(), "get")
                       ._throws(codeModel.ref(Exception.class))
                       .body()
                       ._return(JExpr.invoke("getValue").invoke(name));

            if (Utils.isErasuredAssignable(codeModel.ref(IMetadataHolder.class), methodType)) {
                final JClass castedType = (JClass) methodType;

                concreteClassLevelReturnType = codeModel.directClass(castedType.name() + ".ClassDescriptor");
                abstractClassLevelReturnType = codeModel.directClass(castedType.name() + ".IClassDescriptor");
                classLevelMethodExpression =
                        JExpr._new(concreteClassLevelReturnType)
                             .arg(JExpr._this())
                             .arg(fieldMetaName)
                             ;

                abstractInstanceLevelReturnType = codeModel.directClass(castedType.name() + ".IInstanceDescriptor");
                concreteInstanceLevelReturnType = codeModel.directClass(castedType.name() + ".InstanceDescriptor");
                instanceLevelMethodExpression =
                        JExpr._new(concreteInstanceLevelReturnType)
                             .arg(JExpr._this())
                             .arg(fieldMetaName)
                             .arg(JExpr._new(valueGetter))
                             ;
            } else {
                final JClass returnClass = methodType.isPrimitive() ? methodType.boxify() : (JClass) methodType;
                abstractClassLevelReturnType = descriptorInterface.narrow(returnClass);
                concreteClassLevelReturnType = abstractDescriptor.narrow(returnClass);

                final JDefinedClass returnTypeRef = codeModel.anonymousClass(typeReference.narrow(returnClass));
                final JClass java7concreteClass = abstractDescriptor.narrow(new JClass[]{});

                classLevelMethodExpression = JExpr._new(java7concreteClass)
                                  .arg(JExpr._this())
                                  .arg(fieldMetaName)
                                  .arg(JExpr._new(returnTypeRef))
                                  ;

                abstractInstanceLevelReturnType = valueDescriptor.narrow(returnClass);
                concreteInstanceLevelReturnType = abstractValueDescriptor.narrow(returnClass);
                instanceLevelMethodExpression =
                        JExpr._new(concreteInstanceLevelReturnType)
                             .arg(JExpr._this())
                             .arg(fieldMetaName)
                             .arg(JExpr._new(returnTypeRef))
                             .arg(JExpr._new(valueGetter))
                             ;
            }

            classLevelDescriptorInterface.method(JMod.NONE, abstractClassLevelReturnType, name);
            instanceLevelDescriptorInterface.method(JMod.NONE, abstractInstanceLevelReturnType, name)
                                            .annotate(Override.class);

            final JMethod classLevelDescriptorMethod =
                    classLevelDescriptor.method(JMod.PUBLIC, concreteClassLevelReturnType, name);

            classLevelDescriptorMethod.javadoc().add("This method reflects '" + fieldMetaName + "' field metadata.\n");
            classLevelDescriptorMethod.javadoc().addReturn().add(concreteClassLevelReturnType.erasure().fullName());
            classLevelDescriptorMethod.javadoc().add("@see");
            classLevelDescriptorMethod.javadoc().add(IDescriptor.class.getCanonicalName());

            classLevelDescriptorMethod.annotate(Override.class);
            classLevelDescriptorMethod.body()._return(classLevelMethodExpression);

            final JMethod instanceDescriptorMethod =
                    instanceLevelDescriptor.method(JMod.PUBLIC, abstractInstanceLevelReturnType, name);
            instanceDescriptorMethod.annotate(Override.class);
            instanceDescriptorMethod.body()._return(instanceLevelMethodExpression);

            instanceMethods.put(fieldMetaName, instanceDescriptorMethod);
            classMethods.put(fieldMetaName, classLevelDescriptorMethod);
        }

        {
            final JMethod instancedDescriptorChildGetter =
                    instanceLevelDescriptor.method(JMod.PUBLIC,
                            codeModel.ref(IValueDescriptor.class).narrow(codeModel.wildcard()),
                            "getChild");
            //instancedDescriptorChildGetter._throws(CallException.class);
            final JVar name =
                    instancedDescriptorChildGetter.param(String.class, "name");

            final JSwitch _switch =
                    instancedDescriptorChildGetter.body()
                                                  ._switch(name);

            for (final Map.Entry<String, JMethod> e : instanceMethods.entrySet()) {
                _switch._case(JExpr.lit(e.getKey()))
                       .body()
                       ._return(JExpr.invoke(e.getValue()));
            }

            _switch._default()
                   .body()
                   ._throw(JExpr._new(codeModel.ref(IllegalArgumentException.class))
                                .arg(JExpr.lit("No requested child found")));
            instancedDescriptorChildGetter.annotate(Override.class);
        }

        {
            final JMethod instancedDescriptorChildGetter =
                    classLevelDescriptor.method(JMod.PUBLIC,
                            codeModel.ref(IDescriptor.class).narrow(codeModel.wildcard()),
                            "getChild");
            //instancedDescriptorChildGetter._throws(CallException.class);
            final JVar name =
                    instancedDescriptorChildGetter.param(String.class, "name");

            final JSwitch _switch =
                    instancedDescriptorChildGetter.body()._switch(name);

            for (final Map.Entry<String, JMethod> e : classMethods.entrySet()) {
                _switch._case(JExpr.lit(e.getKey()))
                       .body()
                       ._return(JExpr.invoke(e.getValue()));
            }

            _switch._default()
                   .body()
                   ._throw(JExpr._new(codeModel.ref(IllegalArgumentException.class))
                                .arg(JExpr.lit("No requested child found")));
            instancedDescriptorChildGetter.annotate(Override.class);
        }
    }

    private static String getAssociatedFieldName(final JDefinedClass theClass, final JMethod method) {
        final JCodeModel codeModel = theClass.owner();
        final String name = method.name();

        final boolean isGetMethod = name.startsWith("get");
        if (!isGetMethod && !name.startsWith("is")) {
            return null;
        }

        final String string = name.substring(isGetMethod ? 3 : 2, name.length());
        final String fieldName =
                Character.toLowerCase(string.charAt(0))
                + (string.length() > 1 ? string.substring(1) : "");

        final JFieldVar field = theClass.fields().get(fieldName);
        if (field == null) {
            return null;
        }

        for (final JAnnotationUse annotation : field.annotations()) {
            if (annotation.getAnnotationClass().isAssignableFrom(codeModel.ref(XmlElement.class))) {
                final Map<String, JAnnotationValue> members = annotation.getAnnotationMembers();
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
