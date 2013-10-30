package org.nohope.jaxb2.plugin;

import com.sun.codemodel.JClass;
import com.sun.codemodel.JNullType;
import com.sun.codemodel.JType;

import java.util.Iterator;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">Ketoth Xupack</a>
 * @since 2013-11-01 16:39
 */
public class Utils {

    private Utils() {
    }

    public static boolean isErasuredAssignable(final JType c1, final JType c2) {
        if (!c1.isReference() || !c2.isReference()) {
            return false;
        }

        final JClass e1 = (JClass) c1.erasure();
        final JClass e2 = (JClass) c2.erasure();

        if (e2 instanceof JNullType) {
            return true;
        }

        if (e1 == e2) {
            return true;
        }

        if (e1 == e1._package().owner().ref(Object.class)) {
            return true;
        }

        final JClass b = e2._extends();
        if (b != null && isErasuredAssignable(e1, b)) {
            return true;
        }

        if (e1.isInterface()) {
            final Iterator<JClass> interfaces = e2._implements();
            while (interfaces.hasNext()) {
                if (isErasuredAssignable(e1, interfaces.next())) {
                    return true;
                }
            }
        }

        return false;
    }
}
