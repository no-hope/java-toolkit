package org.nohope.reflection;

import org.apache.commons.lang3.ArrayUtils;
import org.nohope.typetools.StringUtils;

import java.lang.reflect.*;
import java.util.*;

/**
 * Set of introspection utils aimed to reduce problems caused by reflecting
 * native/inherited types.
 * <p/>
 * This class extensively uses "types compatibility" term which means:
 * <p/>
 * Types are compatible if:
 * 1. source type can be auto(un)boxed to target type
 * 2. source type is child of target type
 * 3. source and target are array types then one of these rules should be
 * applied to their component types.
 *
 * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
 * @since 8/12/11 5:42 PM
 */
public final class IntrospectionUtils {
    /**
     * Default stake trace depth for method invocation.
     */
    private static final int DEFAULT_INVOKE_DEPTH = 3;
    /**
     * Method name for constructor (value: "new"). *
     */
    private static final String CONSTRUCTOR = "new";

    /**
     * list of java primitive types.
     */
    private static final List<Class<?>> PRIMITIVES = new ArrayList<>();
    /**
     * lookup map for matching primitive types and their object wrappers.
     */
    private static final Map<Class<?>, Class<?>> PRIMITIVES_TO_WRAPPERS =
            new HashMap<>();

    static {
        PRIMITIVES.add(Byte.TYPE);
        PRIMITIVES.add(Short.TYPE);
        PRIMITIVES.add(Integer.TYPE);
        PRIMITIVES.add(Long.TYPE);
        PRIMITIVES.add(Float.TYPE);
        PRIMITIVES.add(Double.TYPE);
        PRIMITIVES.add(Boolean.TYPE);
        PRIMITIVES.add(Character.TYPE);

        PRIMITIVES_TO_WRAPPERS.put(Byte.TYPE, Byte.class);
        PRIMITIVES_TO_WRAPPERS.put(Short.TYPE, Short.class);
        PRIMITIVES_TO_WRAPPERS.put(Integer.TYPE, Integer.class);
        PRIMITIVES_TO_WRAPPERS.put(Long.TYPE, Long.class);
        PRIMITIVES_TO_WRAPPERS.put(Float.TYPE, Float.class);
        PRIMITIVES_TO_WRAPPERS.put(Double.TYPE, Double.class);
        PRIMITIVES_TO_WRAPPERS.put(Boolean.TYPE, Boolean.class);
        PRIMITIVES_TO_WRAPPERS.put(Character.TYPE, Character.class);
    }

    /**
     * Utility class constructor.
     */
    private IntrospectionUtils() {
    }

    /**
     * @return list of primitive types
     */
    public static List<Class<?>> getPrimitives() {
        return new ArrayList<>(PRIMITIVES);
    }

    /**
     * Returns referenced wrapper for primitive type.
     *
     * @param p class suppose to be a primitive
     * @return wrapper for primitive, {@code null} if passed type is not a
     *         primitive
     */
    public static Class<?> primitiveToWrapper(final Class p) {
        return PRIMITIVES_TO_WRAPPERS.get(p);
    }

    /**
     * Returns boxed version of given primitive type (if actually it is a
     * primitive type).
     *
     * @param type type to translate
     * @return boxed primitive class or class itself if not primitive.
     */
    public static Class<?> tryFromPrimitive(final Class type) {
        if (type == null || !type.isPrimitive()) {
            return type;
        }
        return primitiveToWrapper(type);
    }

    /**
     * Invokes compatible constructor of given type with given constructor
     * arguments.
     *
     * @param type type to construct
     * @param args constructor arguments
     * @param <T>  type
     * @return new instance
     * @throws NoSuchMethodException     if no or more than one compatible
     *                                   constructor found
     * @throws InvocationTargetException on constructor invocation exception
     * @throws IllegalAccessException    on on attempt to invoke
     *                                   protected/private constructor
     * @throws InstantiationException    on constructing exception
     */
    public static <T> T newInstance(final Class<T> type, final Object... args)
            throws NoSuchMethodException, InvocationTargetException,
            IllegalAccessException, InstantiationException {

        final Constructor<T> constructor =
                searchConstructor(type, getClasses(args));
        final Class[] signature = constructor.getParameterTypes();

        try {
            final Object[] params = adaptTo(args, signature);
            constructor.setAccessible(true);
            return constructor.newInstance(params);
        } catch (final ClassCastException e) {
            throw cantInvoke(type, CONSTRUCTOR, signature, args, e);
        }
    }

    /**
     * Invokes compatible method of given instance with given name and
     * parameters.
     *
     * @param instance   target object object
     * @param methodName name of method
     * @param args       method arguments
     * @return method invocation result
     * @throws NoSuchMethodException     if no or more than one compatible
     *                                   method found
     * @throws InvocationTargetException on method invocation exception
     * @throws IllegalAccessException    on on attempt to invoke
     *                                   protected/private method
     */
    public static Object invoke(final Object instance,
                                final String methodName,
                                final Object... args)
            throws NoSuchMethodException, InvocationTargetException,
            IllegalAccessException {

        final Method method =
                searchMethod(instance, methodName, getClasses(args));
        final Class[] sig = method.getParameterTypes();

        try {
            final Object[] params = adaptTo(args, sig);
            method.setAccessible(true);
            return method.invoke(instance, params);
        } catch (final ClassCastException e) {
            throw cantInvoke(instance.getClass(), methodName, sig, args, e);
        }
    }

    /**
     * Checks if target class is assignable from source class in terms of
     * auto(un)boxing. if given classes are array types then recursively checks
     * if their component types are assignable from each other.
     * <p/>
     * Note: assignable means inheritance:
     * <pre>
     *   target
     *     ^
     *     |
     *   source
     * </pre>
     *
     * @param target target class
     * @param source source class
     * @return {@code true} if target is assignable from source
     */
    public static boolean isAssignable(final Class target, final Class source) {
        if (target == null || source == null) {
            throw new IllegalArgumentException("classes");
        }

        if (target.isArray() && source.isArray()) {
            return isAssignable(target.getComponentType(),
                    source.getComponentType());
        }
        return tryFromPrimitive(target).isAssignableFrom(tryFromPrimitive(source));
    }

    /**
     * Checks if given arrays of types are compatible.
     *
     * @param targets array of types
     * @param sources array of types
     * @return {@code true} if types are compatible
     */
    public static boolean areTypesCompatible(final Class[] targets,
                                             final Class[] sources) {
        // check if types are "varargs-compatible"
        if (sources.length != targets.length) {
            return false;
        }

        for (int i = 0; i < targets.length; i++) {
            // if we got null here then types are definitely compatible
            // (if target is not a primitive)
            if (sources[i] == null) {
                if (targets[i].isPrimitive()) {
                    return false;
                }
                continue;
            }

            if (!isAssignable(targets[i], sources[i])) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if types compatible in term of varargs.
     *
     * @param targets target types
     * @param sources source types
     * @return {@code true} if types are vararg-compatible
     */
    public static boolean areTypesVarargCompatible(final Class[] targets,
                                                   final Class[] sources) {
        if (!isVarargs(targets)) {
            return areTypesCompatible(targets, sources);
        }

        final Class[] flat = flattenVarargs(targets);
        final int flatSize = flat.length;
        final int srcSize = sources.length;

        // last vararg can be omitted
        if (srcSize == flatSize - 1) {
            return areTypesCompatible(ArrayUtils.subarray(flat, 0, flatSize - 1), sources);
        }
        // not a vararg
        if (srcSize == flatSize) {
            return areTypesCompatible(flat, sources);
        }
        // vararg should be assembled
        if (srcSize > flatSize) {
            final Class vararg = flat[flatSize - 1];
            for (int i = flatSize; i < srcSize; i++) {
                if (!isAssignable(vararg, sources[i])) {
                    return false;
                }
            }

            return areTypesCompatible(flat, ArrayUtils.subarray(sources, 0, flatSize));
        }

        return false;
    }

    /**
     * Searches for constructor of given type compatible with given signature.
     *
     * @param type      type
     * @param signature signature
     * @param <T>       type of object for search
     * @return constructor compatible with given signature
     * @throws NoSuchMethodException if no or more than one constructor found
     */
    @SuppressWarnings("unchecked")
    private static <T> Constructor<T> searchConstructor(
            final Class<T> type, final Class[] signature)
            throws NoSuchMethodException {

        final Constructor[] constructors = type.getDeclaredConstructors();
        Constructor found = null;
        Constructor vararg = null;
        int varargsFound = 0;

        for (final Constructor constructor : constructors) {
            final Class[] types = constructor.getParameterTypes();

            // Check for signature types compatibility
            if (areTypesCompatible(types, signature)) {
                if (found != null) {
                    // we got one compatible constructor already...
                    throw tooMuch(type, CONSTRUCTOR, signature);
                }
                found = constructor;
            } else if (areTypesVarargCompatible(types, signature)) {
                vararg = constructor;
                varargsFound++;
            }
        }

        // there is no such constructor at all...
        if (found == null) {
            if (varargsFound > 1) {
                throw tooMuch(type, CONSTRUCTOR, signature);
            }
            if (varargsFound == 1) {
                return vararg;
            }
            throw notFound(type, CONSTRUCTOR, signature);
        }

        // this _should_ be an Constructor<T> huh?
        return (Constructor<T>) found;
    }

    /**
     * Searches for method of given instance with given name and compatible
     * signature.
     *
     * @param instance   instance
     * @param methodName method name
     * @param signature  method signature
     * @return constructor compatible with given signature
     * @throws NoSuchMethodException if no or more than one method found
     */
    public static Method searchMethod(final Object instance,
                                      final String methodName,
                                      final Class... signature)
            throws NoSuchMethodException {

        final Class type;
        if (instance instanceof Class) {
            type = (Class) instance;
        } else {
            type = instance.getClass();
        }

        final List<Method> mth = new ArrayList<>();
        mth.addAll(Arrays.asList(type.getDeclaredMethods()));

        // FIXME
        for (final Method m : type.getMethods()) {
            if (!mth.contains(m)) {
                mth.add(m);
            }
        }

        final Method[] methods = mth.toArray(new Method[mth.size()]);

        Method found = null;
        Method vararg = null;
        int varargsFound = 0;

        for (final Method method : methods) {
            final Class[] types = method.getParameterTypes();

            // wrong method name...
            if (!methodName.equals(method.getName())) {
                continue;
            }

            // Check for signature types compatibility
            if (areTypesCompatible(types, signature)) {
                if (found != null) {
                    // we got one compatible method already...
                    throw tooMuch(type, methodName, signature);
                }
                found = method;
            } else if (areTypesVarargCompatible(types, signature)) {
                vararg = method;
                varargsFound++;
            }
        }

        // there is no such method at all...
        if (found == null) {
            if (varargsFound > 1) {
                throw tooMuch(type, methodName, signature);
            }
            if (varargsFound == 1) {
                return vararg;
            }
            throw notFound(type, methodName, signature);
        }

        return found;
    }

    /**
     * Shrinks array component type to common parent type of all array elements.
     *
     * @param array given array
     * @return casted array
     */
    public static Object shrinkType(final Object[] array) {
        if (array.length == 0) {
            return array;
        }

        int firstNotNull = -1;
        for (int i = 0; i < array.length; i++) {
            if (null != array[i]) {
                firstNotNull = i;
                break;
            }
        }

        // array of nulls
        if (firstNotNull == -1) {
            return array;
        }

        Class<?> common = array[firstNotNull].getClass();
        for (int i = firstNotNull; i < array.length; i++) {
            final Object element = array[i];

            if (element != null) {
                // can't be null
                common = findCommonParent(element.getClass(), common);
            }
        }

        return shrinkTypeTo(array, common);
    }

    /**
     * Finds superclass common for passed classes.
     * <p/>
     * Note: this method passes through interface classes.
     *
     * @param c1 first class
     * @param c2 second class
     * @return common parent class if exists {@code null} otherwise
     */
    public static Class<?> findCommonParent(final Class<?> c1,
                                            final Class<?> c2) {
        if (c1 == null) {
            return c2;
        }
        if (c2 == null) {
            return c1;
        }

        if (isAssignable(c1, c2)) {
            return c1;
        }
        if (isAssignable(c2, c1)) {
            return c2;
        }

        Class c1Parent = tryFromPrimitive(c1).getSuperclass();
        while (c1Parent != null && !c2.isInterface()) {
            if (isAssignable(c1Parent, c2)) {
                return c1Parent;
            }
            c1Parent = c1Parent.getSuperclass();
        }

        // c1 or c2 is an interface (which not yet supported)
        return null;
    }

    /**
     * Casts array object to a given type.
     * <p/>
     * i.e:
     * <pre>
     *      shrinkTypeTo(Object[] {1, 2, 3}, int.class) --> int[] {1, 2, 3}
     * </pre>
     *
     * @param source array to be casted
     * @param clazz  desired type
     * @param <T>    desired type
     * @return casted array
     * @throws IllegalArgumentException if source object is not an array
     * @throws ClassCastException       on array storing exception
     */
    public static <T> Object shrinkTypeTo(final Object source,
                                          final Class<T> clazz) {
        if (source == null || !source.getClass().isArray()) {
            throw new IllegalArgumentException("array expected");
        }

        if (source.getClass().getComponentType() == clazz) {
            return source;
        }

        final int arrayLength = Array.getLength(source);
        final Object result = Array.newInstance(clazz, arrayLength);
        // zero length array
        if (arrayLength == 0) {
            if (isAssignable(source.getClass().getComponentType(), clazz)) {
                return result;
            }
            throw arrayCastError(source, clazz);
        }


        for (int i = 0; i < arrayLength; i++) {
            Object origin = Array.get(source, i);

            try {
                // in case if we got multidimensional array
                if (origin != null
                        && origin.getClass().isArray()
                        && clazz.isArray()) {
                    origin = shrinkTypeTo(origin, clazz.getComponentType());
                }
                Array.set(result, i, origin);
            } catch (IllegalArgumentException e) {
                throw arrayCastError(origin, clazz, e);
            }
        }
        return result;
    }

    /**
     * Shrinks component type of given array to given type.
     *
     * @param source array to be casted
     * @param clazz  desired type
     * @param <T>    desired type
     * @return casted array
     */
    public static <T> Object shrinkTypeTo(final Object[] source,
                                          final Class<T> clazz) {
        return shrinkTypeTo((Object) source, clazz);
    }

    /**
     * Creates object array from given object. If object is of array type
     * then shrinking it to {@link Object} type, else wraps it with new
     * {@link Object[]} instance.
     *
     * @param source object
     * @return array of objects
     * @see #shrinkTypeTo(Object, Class)
     */
    public static Object[] toObjArray(final Object source) {
        if (source == null) {
            return null;
        }
        if (!source.getClass().isArray()) {
            return new Object[]{source};
        }
        return (Object[]) shrinkTypeTo(source, Object.class);
    }

    /**
     * @return name of method which called this method
     */
    public static String reflectSelfName() {
        return getMethodName(0);
    }

    /**
     * @return name of method which called method invoked this method
     */
    public static String reflectCallerName() {
        return getMethodName(1);
    }

    /**
     * Transforms list of objects to list of their canonical names.
     *
     * @param arguments list of objects
     * @return canonical names for given object types
     */
    public static String[] getClassNames(final Object... arguments) {
        return getClassNames(getClasses(arguments));
    }

    public static String getCanonicalClassName(final Object obj) {
        return obj == null ? null : obj.getClass().getCanonicalName();
    }

    /**
     * Transforms list of classes to list of their canonical names.
     *
     * @param arguments list of classes
     * @return canonical names for given classes
     */
    private static String[] getClassNames(final Class... arguments) {
        final String[] names = new String[arguments.length];
        {
            int i = 0;
            for (final Class clazz : arguments) {
                names[i++] = (clazz == null) ? null : clazz.getCanonicalName();
            }
        }

        return names;
    }

    /**
     * Returns list of types of given list of objects.
     *
     * @param arguments array of object
     * @return array of classes of given objects
     */
    private static Class[] getClasses(final Object... arguments) {
        final Class[] signature = new Class[arguments.length];
        {
            int i = 0;
            for (final Object argument : arguments) {
                signature[i++] = (argument == null)
                        ? null
                        : argument.getClass();
            }
        }

        return signature;
    }

    /**
     * Returns class for given type.
     *
     * @param type type
     * @return class object
     */
    public static Class<?> getClass(final Type type) {
        if (type instanceof Class) {
            return (Class) type;
        }
        if (type instanceof ParameterizedType) {
            return getClass(((ParameterizedType) type).getRawType());
        }
        if (type instanceof GenericArrayType) {
            final Type componentType = ((GenericArrayType) type)
                    .getGenericComponentType();
            final Class<?> componentClass = getClass(componentType);
            if (componentClass != null) {
                return Array.newInstance(componentClass, 0).getClass();
            }
        }
        return null;
    }

    /**
     * Returns component type of last class in given signature of classes.
     *
     * @param signature signature of types
     * @return vararg component type
     */
    private static Class getVarargComponentType(final Class[] signature) {
        return signature[signature.length - 1].getComponentType();
    }

    /**
     * Casts list of objects to a given list of types.
     * Node: types of objects should be already compatible with given list of
     * types.
     *
     * @param objects list of objects to cast to
     * @param types   corresponding types
     * @return list of objects casted to given list of types
     */
    static Object[] adaptTo(final Object[] objects, final Class[] types) {
        final int argsLength = objects.length;
        final int typesLength = types.length;

        final List<Object> result = new ArrayList<>();
        if (argsLength == typesLength) {
            for (int i = 0; i < argsLength; i++) {
                final Class type = types[i];
                final Object object = objects[i];

                if (type.isArray() && object != null) {
                    final Class<?> component = type.getComponentType();
                    result.add(shrinkTypeTo(object, component));
                    continue;
                }
                result.add(objects[i]);
            }
        } else if (isVarargs(types)) {
            final Class<?> clazz = getVarargComponentType(types);

            // vararg should be defaulted
            if (argsLength == typesLength - 1) {
                return adaptTo(ArrayUtils.add(objects,
                        Array.newInstance(clazz, 0)), types);
            }
            // aggregate last arguments
            if (argsLength > typesLength) {
                final int varargIndex = typesLength - 1;

                final Object[] newParams =
                        ArrayUtils.subarray(objects, 0, varargIndex);
                final Object[] varargRest =
                        ArrayUtils.subarray(objects, varargIndex,
                                argsLength);
                return adaptTo(ArrayUtils.add(newParams, varargRest), types);
            }
        }
        return result.toArray();
    }

    /**
     * Tricky method to get object wrapper for primitive type.
     *
     * @param clazz class
     * @param <T>   type
     * @return referenced wrapper class for all primitive types passe
     */
    static <T> Class autoBox(final Class<T> clazz) {
        return Array.get(Array.newInstance(clazz, 1), 0).getClass();
    }

    /**
     * Constructs array type of given type.
     *
     * @param clazz type
     * @param depth resulted array dimension
     * @param <T>   type
     * @return array type of given type with passed dimension
     */
    static <T> Class<?> toArrayType(final Class<T> clazz, final int depth) {
        Class result = clazz;
        for (int i = 0; i < depth; i++) {
            result = Array.newInstance(result, 0).getClass();
        }
        return result;
    }

    /**
     * Checks if given signature is possible vararg-like signature.
     *
     * @param signature array of classes
     * @return {@code true} if last element is type of array
     */
    private static boolean isVarargs(final Class[] signature) {
        final int length = signature.length;
        return length > 0 && signature[length - 1].isArray();
    }

    /**
     * Make last argument argument of vararg signature "flat".
     *
     * @param signature array of classes
     * @return new signature
     */
    static Class[] flattenVarargs(final Class[] signature) {
        if (isVarargs(signature)) {
            final Class[] result = signature.clone();
            final int length = signature.length;
            result[length - 1] = signature[length - 1].getComponentType();
            return result;
        }
        return signature;
    }

    /**
     * Returns method name in current thread stack.
     *
     * @param stackDepthShift position in stack to get deeper called method
     * @return method name
     */
    private static String getMethodName(final int stackDepthShift) {
        final StackTraceElement[] currStack =
                Thread.currentThread().getStackTrace();
        // Find caller function name
        return currStack[DEFAULT_INVOKE_DEPTH + stackDepthShift]
                .getMethodName();
    }

    /**
     * Helper function for constructing exception.
     *
     * @param message    exceptional message
     * @param type       type affected
     * @param methodName method
     * @param signature  method signature
     * @return constructed exception
     */
    private static NoSuchMethodException abort(final String message,
                                               final Class type,
                                               final String methodName,
                                               final Class[] signature) {
        return new NoSuchMethodException(String.format(message,
                type.getCanonicalName(), methodName,
                StringUtils.join(getClassNames(signature))));
    }

    /**
     * Constructs exception for too much found method.
     *
     * @param type       type affected
     * @param methodName method
     * @param signature  method signature
     * @return constructed exception
     */
    private static NoSuchMethodException tooMuch(
            final Class type, final String methodName,
            final Class[] signature) {
        return abort("More than one method %s#%s found conforms signature [%s]",
                type, methodName, signature);
    }

    /**
     * Constructs exception for not found exception.
     *
     * @param type       type affected
     * @param methodName method name
     * @param signature  method signature
     * @return constructed exception
     */
    private static NoSuchMethodException notFound(
            final Class type, final String methodName,
            final Class[] signature) {
        return abort("No methods %s#%s found to conform signature [%s]",
                type, methodName, signature);
    }

    /**
     * Constructs {@link ClassCastException} for array casting error case.
     *
     * @param elem  element of array caused cast exception
     * @param clazz type of array
     * @param cause original exception
     * @return {@link ClassCastException} instance
     */
    private static ClassCastException arrayCastError(final Object elem,
                                                     final Class clazz,
                                                     final Throwable cause) {
        final ClassCastException ex = new ClassCastException(String.format(
                "Unexpected value %s (%s) for array of type %s"
                , elem
                , elem == null ? "unknown" : elem.getClass().getCanonicalName()
                , clazz.getCanonicalName()));
        ex.initCause(cause);
        throw ex;
    }

    /**
     * Constructs {@link ClassCastException} for array casting error case.
     *
     * @param src   source array
     * @param clazz type of destination array
     * @return {@link ClassCastException} instance
     */
    private static ClassCastException arrayCastError(final Object src,
                                                     final Class clazz) {
        throw new ClassCastException(String.format(
                "Incompatible types found - source %s destination %s[]"
                , src.getClass().getCanonicalName()
                , clazz.getCanonicalName()));
    }

    /**
     * Constructs {@link NoSuchMethodException} in case when found method
     * can't be invoked.
     *
     * @param type       type of object
     * @param methodName method name failed invocation
     * @param signature  method signature
     * @param args       arguments was passed to method
     * @param cause      original exception
     * @return {@link NoSuchMethodException} instance
     */
    private static NoSuchMethodException cantInvoke(final Class type,
                                                    final String methodName,
                                                    final Class[] signature,
                                                    final Object[] args,
                                                    final Throwable cause) {
        final NoSuchMethodException e = new NoSuchMethodException(String.format(
                "Unable to invoke method %s#%s(%s) with parameters [%s]"
                , type.getCanonicalName()
                , methodName
                , StringUtils.join(getClassNames(signature))
                , StringUtils.join(args)));

        return (NoSuchMethodException) e.initCause(cause);
    }
}
