/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2018-2024 Andres Almiray.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kordamp.gradle.util;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

import static java.util.Objects.requireNonNull;
import static org.kordamp.gradle.util.StringUtils.isBlank;
import static org.kordamp.gradle.util.StringUtils.requireNonBlank;

/**
 * @author Andres Almiray
 * @since 0.37.0
 */
public class ObjectUtils {
    public static final Map<Class<?>, Class<?>> PRIMITIVE_TYPE_COMPATIBLE_CLASSES = new LinkedHashMap<>();
    public static final Map<String, String> PRIMITIVE_TYPE_COMPATIBLE_TYPES = new LinkedHashMap<>();
    private static final String PROPERTY_GET_PREFIX = "get";
    private static final String PROPERTY_IS_PREFIX = "is";
    private static final String PROPERTY_SET_PREFIX = "set";
    private static final Pattern GETTER_PATTERN_1 = Pattern.compile("^get[A-Z][\\w]*$");
    private static final Pattern GETTER_PATTERN_2 = Pattern.compile("^is[A-Z][\\w]*$");
    private static final Pattern SETTER_PATTERN = Pattern.compile("^set[A-Z][\\w]*$");
    private static final String ERROR_METHOD_NAME_BLANK = "Argument 'methodName' must not be blank";
    private static final String ERROR_CLAZZ_NULL = "Argument 'clazz' must not be null";
    private static final String ERROR_PROPERTY_NAME_BLANK = "Argument 'propertyName' must not be blank";
    private static final String ERROR_METHOD_NULL = "Argument 'method' must not be null";
    private static final String MESSAGE = "message";

    static {
        registerPrimitiveClassPair(Boolean.class, boolean.class);
        registerPrimitiveClassPair(Integer.class, int.class);
        registerPrimitiveClassPair(Short.class, short.class);
        registerPrimitiveClassPair(Byte.class, byte.class);
        registerPrimitiveClassPair(Character.class, char.class);
        registerPrimitiveClassPair(Long.class, long.class);
        registerPrimitiveClassPair(Float.class, float.class);
        registerPrimitiveClassPair(Double.class, double.class);
    }

    private ObjectUtils() {
        // prevent instantiation
    }

    /**
     * Just add two entries to the class compatibility map
     *
     * @param left
     * @param right
     */
    private static void registerPrimitiveClassPair(Class<?> left, Class<?> right) {
        PRIMITIVE_TYPE_COMPATIBLE_CLASSES.put(left, right);
        PRIMITIVE_TYPE_COMPATIBLE_CLASSES.put(right, left);
        PRIMITIVE_TYPE_COMPATIBLE_TYPES.put(left.getName(), right.getName());
        PRIMITIVE_TYPE_COMPATIBLE_TYPES.put(right.getName(), left.getName());
    }

    /**
     * Checks that the specified condition is met. This method is designed
     * primarily for doing parameter validation in methods and constructors,
     * as demonstrated below:
     * <blockquote><pre>
     * public Foo(int[] array) {
     *     GriffonClassUtils.requireState(array.length > 0);
     * }
     * </pre></blockquote>
     *
     * @param condition the condition to check
     * @throws IllegalStateException if {@code condition} evaluates to false
     */
    public static void requireState(boolean condition) {
        if (!condition) {
            throw new IllegalStateException();
        }
    }

    /**
     * Checks that the specified condition is met and throws a customized
     * {@link IllegalStateException} if it is. This method is designed primarily
     * for doing parameter validation in methods and constructors with multiple
     * parameters, as demonstrated below:
     * <blockquote><pre>
     * public Foo(int[] array) {
     *     GriffonClassUtils.requireState(array.length > 0, "array must not be empty");
     * }
     * </pre></blockquote>
     *
     * @param condition the condition to check
     * @param message   detail message to be used in the event that a {@code
     *                  IllegalStateException} is thrown
     * @throws IllegalStateException if {@code condition} evaluates to false
     */
    public static void requireState(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }

    /**
     * Checks that the specified array is not empty, throwing a
     * {@link IllegalStateException} if it is.
     *
     * @param array the array to check
     * @throws NullPointerException  if {@code array} is null
     * @throws IllegalStateException if {@code array} is empty
     */
    public static byte[] requireNonEmpty(@Nonnull byte[] array) {
        requireNonNull(array);
        requireState(array.length != 0);
        return array;
    }

    /**
     * Checks that the specified array is not empty, throwing a customized
     * {@link IllegalStateException} if it is.
     *
     * @param array   the array to check
     * @param message detail message to be used in the event that a {@code
     *                IllegalStateException} is thrown
     * @throws NullPointerException     if {@code array} is null
     * @throws IllegalArgumentException if {@code message} is {@code blank}
     * @throws IllegalStateException    if {@code array} is empty
     */
    public static byte[] requireNonEmpty(@Nonnull byte[] array, @Nonnull String message) {
        requireNonNull(array);
        requireState(array.length != 0, requireNonBlank(message, MESSAGE));
        return array;
    }

    /**
     * Checks that the specified array is not empty, throwing a
     * {@link IllegalStateException} if it is.
     *
     * @param array the array to check
     * @throws NullPointerException  if {@code array} is null
     * @throws IllegalStateException if {@code array} is empty
     */
    public static short[] requireNonEmpty(@Nonnull short[] array) {
        requireNonNull(array);
        requireState(array.length != 0);
        return array;
    }

    /**
     * Checks that the specified array is not empty, throwing a customized
     * {@link IllegalStateException} if it is.
     *
     * @param array   the array to check
     * @param message detail message to be used in the event that a {@code
     *                IllegalStateException} is thrown
     * @throws NullPointerException     if {@code array} is null
     * @throws IllegalArgumentException if {@code message} is {@code blank}
     * @throws IllegalStateException    if {@code array} is empty
     */
    public static short[] requireNonEmpty(@Nonnull short[] array, @Nonnull String message) {
        requireNonNull(array);
        requireState(array.length != 0, requireNonBlank(message, MESSAGE));
        return array;
    }

    /**
     * Checks that the specified array is not empty, throwing a
     * {@link IllegalStateException} if it is.
     *
     * @param array the array to check
     * @throws NullPointerException  if {@code array} is null
     * @throws IllegalStateException if {@code array} is empty
     */
    public static int[] requireNonEmpty(@Nonnull int[] array) {
        requireNonNull(array);
        requireState(array.length != 0);
        return array;
    }

    /**
     * Checks that the specified array is not empty, throwing a customized
     * {@link IllegalStateException} if it is.
     *
     * @param array   the array to check
     * @param message detail message to be used in the event that a {@code
     *                IllegalStateException} is thrown
     * @throws NullPointerException     if {@code array} is null
     * @throws IllegalArgumentException if {@code message} is {@code blank}
     * @throws IllegalStateException    if {@code array} is empty
     */
    public static int[] requireNonEmpty(@Nonnull int[] array, @Nonnull String message) {
        requireNonNull(array);
        requireState(array.length != 0, requireNonBlank(message, MESSAGE));
        return array;
    }

    /**
     * Checks that the specified array is not empty, throwing a
     * {@link IllegalStateException} if it is.
     *
     * @param array the array to check
     * @throws NullPointerException  if {@code array} is null
     * @throws IllegalStateException if {@code array} is empty
     */
    public static long[] requireNonEmpty(@Nonnull long[] array) {
        requireNonNull(array);
        requireState(array.length != 0);
        return array;
    }

    /**
     * Checks that the specified array is not empty, throwing a customized
     * {@link IllegalStateException} if it is.
     *
     * @param array   the array to check
     * @param message detail message to be used in the event that a {@code
     *                IllegalStateException} is thrown
     * @throws NullPointerException     if {@code array} is null
     * @throws IllegalArgumentException if {@code message} is {@code blank}
     * @throws IllegalStateException    if {@code array} is empty
     */
    public static long[] requireNonEmpty(@Nonnull long[] array, @Nonnull String message) {
        requireNonNull(array);
        requireState(array.length != 0, requireNonBlank(message, MESSAGE));
        return array;
    }

    /**
     * Checks that the specified array is not empty, throwing a
     * {@link IllegalStateException} if it is.
     *
     * @param array the array to check
     * @throws NullPointerException  if {@code array} is null
     * @throws IllegalStateException if {@code array} is empty
     */
    public static float[] requireNonEmpty(@Nonnull float[] array) {
        requireNonNull(array);
        requireState(array.length != 0);
        return array;
    }

    /**
     * Checks that the specified array is not empty, throwing a customized
     * {@link IllegalStateException} if it is.
     *
     * @param array   the array to check
     * @param message detail message to be used in the event that a {@code
     *                IllegalStateException} is thrown
     * @throws NullPointerException     if {@code array} is null
     * @throws IllegalArgumentException if {@code message} is {@code blank}
     * @throws IllegalStateException    if {@code array} is empty
     */
    public static float[] requireNonEmpty(@Nonnull float[] array, @Nonnull String message) {
        requireNonNull(array);
        requireState(array.length != 0, requireNonBlank(message, MESSAGE));
        return array;
    }

    /**
     * Checks that the specified array is not empty, throwing a
     * {@link IllegalStateException} if it is.
     *
     * @param array the array to check
     * @throws NullPointerException  if {@code array} is null
     * @throws IllegalStateException if {@code array} is empty
     */
    public static double[] requireNonEmpty(@Nonnull double[] array) {
        requireNonNull(array);
        requireState(array.length != 0);
        return array;
    }

    /**
     * Checks that the specified array is not empty, throwing a customized
     * {@link IllegalStateException} if it is.
     *
     * @param array   the array to check
     * @param message detail message to be used in the event that a {@code
     *                IllegalStateException} is thrown
     * @throws NullPointerException     if {@code array} is null
     * @throws IllegalArgumentException if {@code message} is {@code blank}
     * @throws IllegalStateException    if {@code array} is empty
     */
    public static double[] requireNonEmpty(@Nonnull double[] array, @Nonnull String message) {
        requireNonNull(array);
        requireState(array.length != 0, requireNonBlank(message, MESSAGE));
        return array;
    }

    /**
     * Checks that the specified array is not empty, throwing a
     * {@link IllegalStateException} if it is.
     *
     * @param array the array to check
     * @throws NullPointerException  if {@code array} is null
     * @throws IllegalStateException if {@code array} is empty
     */
    public static char[] requireNonEmpty(@Nonnull char[] array) {
        requireNonNull(array);
        requireState(array.length != 0);
        return array;
    }

    /**
     * Checks that the specified array is not empty, throwing a customized
     * {@link IllegalStateException} if it is.
     *
     * @param array   the array to check
     * @param message detail message to be used in the event that a {@code
     *                IllegalStateException} is thrown
     * @throws NullPointerException     if {@code array} is null
     * @throws IllegalArgumentException if {@code message} is {@code blank}
     * @throws IllegalStateException    if {@code array} is empty
     */
    public static char[] requireNonEmpty(@Nonnull char[] array, @Nonnull String message) {
        requireNonNull(array);
        requireState(array.length != 0, requireNonBlank(message, MESSAGE));
        return array;
    }

    /**
     * Checks that the specified array is not empty, throwing a
     * {@link IllegalStateException} if it is.
     *
     * @param array the array to check
     * @throws NullPointerException  if {@code array} is null
     * @throws IllegalStateException if {@code array} is empty
     */
    public static boolean[] requireNonEmpty(@Nonnull boolean[] array) {
        requireNonNull(array);
        requireState(array.length != 0);
        return array;
    }

    /**
     * Checks that the specified array is not empty, throwing a customized
     * {@link IllegalStateException} if it is.
     *
     * @param array   the array to check
     * @param message detail message to be used in the event that a {@code
     *                IllegalStateException} is thrown
     * @throws NullPointerException     if {@code array} is null
     * @throws IllegalArgumentException if {@code message} is {@code blank}
     * @throws IllegalStateException    if {@code array} is empty
     */
    public static boolean[] requireNonEmpty(@Nonnull boolean[] array, @Nonnull String message) {
        requireNonNull(array);
        requireState(array.length != 0, requireNonBlank(message, MESSAGE));
        return array;
    }

    /**
     * Checks that the specified array is not empty, throwing a
     * {@link IllegalStateException} if it is.
     *
     * @param array the array to check
     * @throws NullPointerException  if {@code array} is null
     * @throws IllegalStateException if {@code array} is empty
     */
    public static <E> E[] requireNonEmpty(@Nonnull E[] array) {
        requireNonNull(array);
        requireState(array.length != 0);
        return array;
    }

    /**
     * Checks that the specified array is not empty, throwing a customized
     * {@link IllegalStateException} if it is.
     *
     * @param array   the array to check
     * @param message detail message to be used in the event that a {@code
     *                IllegalStateException} is thrown
     * @throws NullPointerException     if {@code array} is null
     * @throws IllegalArgumentException if {@code message} is {@code blank}
     * @throws IllegalStateException    if {@code array} is empty
     */
    public static <E> E[] requireNonEmpty(@Nonnull E[] array, @Nonnull String message) {
        requireNonNull(array);
        requireState(array.length != 0, requireNonBlank(message, MESSAGE));
        return array;
    }

    /**
     * Checks that the specified collection is not empty, throwing a
     * {@link IllegalStateException} if it is.
     *
     * @param collection the collection to check
     * @throws NullPointerException  if {@code collection} is null
     * @throws IllegalStateException if {@code collection} is empty
     */
    public static Collection<?> requireNonEmpty(@Nonnull Collection<?> collection) {
        requireNonNull(collection);
        requireState(!collection.isEmpty());
        return collection;
    }

    /**
     * Checks that the specified collection is not empty, throwing a customized
     * {@link IllegalStateException} if it is.
     *
     * @param collection the collection to check
     * @param message    detail message to be used in the event that a {@code
     *                   IllegalStateException} is thrown
     * @throws NullPointerException     if {@code collection} is null
     * @throws IllegalArgumentException if {@code message} is {@code blank}
     * @throws IllegalStateException    if {@code collection} is empty
     */
    public static Collection<?> requireNonEmpty(@Nonnull Collection<?> collection, @Nonnull String message) {
        requireNonNull(collection);
        requireState(!collection.isEmpty(), requireNonBlank(message, MESSAGE));
        return collection;
    }

    /**
     * Checks that the specified map is not empty, throwing a
     * {@link IllegalStateException} if it is.
     *
     * @param map the map to check
     * @throws NullPointerException  if {@code map} is null
     * @throws IllegalStateException if {@code map} is empty
     */
    public static Map<?, ?> requireNonEmpty(@Nonnull Map<?, ?> map) {
        requireNonNull(map);
        requireState(!map.isEmpty());
        return map;
    }

    /**
     * Checks that the specified map is not empty, throwing a customized
     * {@link IllegalStateException} if it is.
     *
     * @param map     the map to check
     * @param message detail message to be used in the event that a {@code
     *                IllegalStateException} is thrown
     * @throws NullPointerException     if {@code map} is null
     * @throws IllegalArgumentException if {@code message} is {@code blank}
     * @throws IllegalStateException    if {@code map} is empty
     */
    public static Map<?, ?> requireNonEmpty(@Nonnull Map<?, ?> map, @Nonnull String message) {
        requireNonNull(map);
        requireState(!map.isEmpty(), requireNonBlank(message, MESSAGE));
        return map;
    }

    /**
     * Finds out if the given {@code Method} is a getter method.
     * <p>
     * <pre>
     * // assuming getMethod() returns an appropriate Method reference
     * isGetterMethod(getMethod("getFoo"))       = true
     * isGetterMethod(getMethod("getfoo") )      = false
     * isGetterMethod(getMethod("mvcGroupInit")) = false
     * isGetterMethod(getMethod("isFoo"))        = true
     * isGetterMethod(getMethod("island"))       = false
     * </pre>
     *
     * @param method a Method reference
     * @return true if the method is a getter, false otherwise.
     */
    public static boolean isGetterMethod(@Nonnull Method method) {
        return isGetterMethod(method, false);
    }

    /**
     * Finds out if the given {@code Method} is a getter method.
     * <p>
     * <pre>
     * // assuming getMethod() returns an appropriate Method reference
     * isGetterMethod(getMethod("getFoo"))       = true
     * isGetterMethod(getMethod("getfoo") )      = false
     * isGetterMethod(getMethod("mvcGroupInit")) = false
     * isGetterMethod(getMethod("isFoo"))        = true
     * isGetterMethod(getMethod("island"))       = false
     * </pre>
     *
     * @param method a Method reference
     * @return true if the method is a getter, false otherwise.
     */
    public static boolean isGetterMethod(@Nonnull Method method, boolean removeAbstractModifier) {
        requireNonNull(method, ERROR_METHOD_NULL);
        return isGetterMethod(MethodDescriptor.forMethod(method, removeAbstractModifier));
    }

    /**
     * Finds out if the given {@code MetaMethod} is a getter method.
     * <p>
     * <pre>
     * // assuming getMethod() returns an appropriate MethodDescriptor reference
     * isGetterMethod(getMethod("getFoo"))       = true
     * isGetterMethod(getMethod("getfoo") )      = false
     * isGetterMethod(getMethod("mvcGroupInit")) = false
     * isGetterMethod(getMethod("isFoo"))        = true
     * isGetterMethod(getMethod("island"))       = false
     * </pre>
     *
     * @param method a MethodDescriptor reference
     * @return true if the method is a getter, false otherwise.
     */
    public static boolean isGetterMethod(@Nonnull MethodDescriptor method) {
        requireNonNull(method, ERROR_METHOD_NULL);
        return isInstanceMethod(method) &&
            (GETTER_PATTERN_1.matcher(method.getName()).matches() || GETTER_PATTERN_2.matcher(method.getName()).matches());
    }

    /**
     * Finds out if the given {@code Method} is a setter method.
     * <p>
     * <pre>
     * // assuming getMethod() returns an appropriate Method reference
     * isGetterMethod(getMethod("setFoo"))       = true
     * isGetterMethod(getMethod("setfoo"))       = false
     * isGetterMethod(getMethod("mvcGroupInit")) = false
     * </pre>
     *
     * @param method a Method reference
     * @return true if the method is a setter, false otherwise.
     */
    public static boolean isSetterMethod(@Nonnull Method method) {
        return isSetterMethod(method, false);
    }

    /**
     * Finds out if the given {@code Method} is a setter method.
     * <p>
     * <pre>
     * // assuming getMethod() returns an appropriate Method reference
     * isGetterMethod(getMethod("setFoo"))       = true
     * isGetterMethod(getMethod("setfoo"))       = false
     * isGetterMethod(getMethod("mvcGroupInit")) = false
     * </pre>
     *
     * @param method a Method reference
     * @return true if the method is a setter, false otherwise.
     */
    public static boolean isSetterMethod(@Nonnull Method method, boolean removeAbstractModifier) {
        requireNonNull(method, ERROR_METHOD_NULL);
        return isSetterMethod(MethodDescriptor.forMethod(method, removeAbstractModifier));
    }

    /**
     * Finds out if the given {@code MethodDescriptor} is a setter method.
     * <p>
     * <pre>
     * // assuming getMethod() returns an appropriate MethodDescriptor reference
     * isGetterMethod(getMethod("setFoo"))       = true
     * isGetterMethod(getMethod("setfoo"))       = false
     * isGetterMethod(getMethod("mvcGroupInit")) = false
     * </pre>
     *
     * @param method a MethodDescriptor reference
     * @return true if the method is a setter, false otherwise.
     */
    public static boolean isSetterMethod(@Nonnull MethodDescriptor method) {
        requireNonNull(method, ERROR_METHOD_NULL);
        return isInstanceMethod(method) && SETTER_PATTERN.matcher(method.getName()).matches();
    }

    /**
     * Finds out if the given {@code Method} is an instance method, i.e,
     * it is public and non-static.
     *
     * @param method a Method reference
     * @return true if the method is an instance method, false otherwise.
     */
    public static boolean isInstanceMethod(@Nonnull Method method) {
        return isInstanceMethod(method, false);
    }

    /**
     * Finds out if the given {@code Method} is an instance method, i.e,
     * it is public and non-static.
     *
     * @param method a Method reference
     * @return true if the method is an instance method, false otherwise.
     */
    public static boolean isInstanceMethod(@Nonnull Method method, boolean removeAbstractModifier) {
        requireNonNull(method, ERROR_METHOD_NULL);
        return isInstanceMethod(MethodDescriptor.forMethod(method, removeAbstractModifier));
    }

    /**
     * Finds out if the given {@code MethodDescriptor} is an instance method, i.e,
     * it is public and non-static.
     *
     * @param method a MethodDescriptor reference
     * @return true if the method is an instance method, false otherwise.
     */
    public static boolean isInstanceMethod(@Nonnull MethodDescriptor method) {
        requireNonNull(method, ERROR_METHOD_NULL);
        int modifiers = method.getModifiers();
        return Modifier.isPublic(modifiers) &&
            !Modifier.isAbstract(modifiers) &&
            !Modifier.isStatic(modifiers);
    }

    /**
     * Detect if left and right types are matching types. In particular,
     * test if one is a primitive type and the other is the corresponding
     * Java wrapper type. Primitive and wrapper classes may be passed to
     * either arguments.
     *
     * @param leftType
     * @param rightType
     * @return true if one of the classes is a native type and the other the object representation
     * of the same native type
     */
    public static boolean isMatchBetweenPrimitiveAndWrapperTypes(@Nonnull Class<?> leftType, @Nonnull Class<?> rightType) {
        requireNonNull(leftType, "Left type is null!");
        requireNonNull(rightType, "Right type is null!");
        return isMatchBetweenPrimitiveAndWrapperTypes(leftType.getName(), rightType.getName());
    }

    /**
     * Detect if left and right types are matching types. In particular,
     * test if one is a primitive type and the other is the corresponding
     * Java wrapper type. Primitive and wrapper classes may be passed to
     * either arguments.
     *
     * @param leftType
     * @param rightType
     * @return true if one of the classes is a native type and the other the object representation
     * of the same native type
     */
    public static boolean isMatchBetweenPrimitiveAndWrapperTypes(@Nonnull String leftType, @Nonnull String rightType) {
        requireNonBlank(leftType, "Left type is null!");
        requireNonBlank(rightType, "Right type is null!");
        String r = PRIMITIVE_TYPE_COMPATIBLE_TYPES.get(leftType);
        return r != null && r.equals(rightType);
    }

    @Nullable
    @SuppressWarnings("ConstantConditions")
    private static Method findDeclaredMethod(@Nonnull Class<?> clazz, @Nonnull String methodName, Class[] parameterTypes) {
        requireNonNull(clazz, ERROR_CLAZZ_NULL);
        requireNonBlank(methodName, ERROR_METHOD_NAME_BLANK);
        while (clazz != null) {
            try {
                Method method = clazz.getDeclaredMethod(methodName, parameterTypes);
                if (method != null) {
                    return method;
                }
            } catch (NoSuchMethodException | SecurityException e) {
                // skip
            }
            clazz = clazz.getSuperclass();
        }

        return null;
    }

    /**
     * Calculate the name for a getter method to retrieve the specified property
     *
     * @param propertyName the name of the property
     * @return The name for the getter method for this property, if it were to exist, i.e. getConstraints
     */
    @Nonnull
    public static String getGetterName(@Nonnull String propertyName) {
        requireNonBlank(propertyName, ERROR_PROPERTY_NAME_BLANK);
        return PROPERTY_GET_PREFIX + Character.toUpperCase(propertyName.charAt(0))
            + propertyName.substring(1);
    }

    /**
     * Retrieves the name of a setter for the specified property name
     *
     * @param propertyName The property name
     * @return The setter equivalent
     */
    @Nonnull
    public static String getSetterName(@Nonnull String propertyName) {
        requireNonBlank(propertyName, ERROR_PROPERTY_NAME_BLANK);
        return PROPERTY_SET_PREFIX + propertyName.substring(0, 1).toUpperCase() + propertyName.substring(1);
    }

    /**
     * Returns true if the name of the method specified and the number of arguments make it a javabean property
     *
     * @param name True if its a Javabean property
     * @param args The arguments
     * @return True if it is a javabean property method
     */
    @SuppressWarnings("ConstantConditions")
    public static boolean isGetter(@Nullable String name, @Nullable Class[] args) {
        if (isBlank(name) || args == null) {
            return false;
        }
        if (args.length != 0) {
            return false;
        }

        if (name.startsWith(PROPERTY_GET_PREFIX)) {
            name = name.substring(3);
            return name.length() > 0 && Character.isUpperCase(name.charAt(0));
        } else if (name.startsWith(PROPERTY_IS_PREFIX)) {
            name = name.substring(2);
            return name.length() > 0 && Character.isUpperCase(name.charAt(0));
        }
        return false;
    }

    /**
     * Returns a property name equivalent for the given getter name or null if it is not a getter
     *
     * @param getterName The getter name
     * @return The property name equivalent
     */
    @Nullable
    @SuppressWarnings("ConstantConditions")
    public static String getPropertyForGetter(@Nullable String getterName) {
        if (isBlank(getterName)) {
            return null;
        }

        if (getterName.startsWith(PROPERTY_GET_PREFIX)) {
            String prop = getterName.substring(3);
            return convertPropertyName(prop);
        } else if (getterName.startsWith(PROPERTY_IS_PREFIX)) {
            String prop = getterName.substring(2);
            return convertPropertyName(prop);
        }
        return null;
    }

    @Nonnull
    private static String convertPropertyName(@Nonnull String prop) {
        if (Character.isUpperCase(prop.charAt(0)) && Character.isUpperCase(prop.charAt(1))) {
            return prop;
        } else if (Character.isDigit(prop.charAt(0))) {
            return prop;
        } else {
            return Character.toLowerCase(prop.charAt(0)) + prop.substring(1);
        }
    }

    /**
     * Returns a property name equivalent for the given setter name or null if it is not a getter
     *
     * @param setterName The setter name
     * @return The property name equivalent
     */
    @Nullable
    @SuppressWarnings("ConstantConditions")
    public static String getPropertyForSetter(@Nullable String setterName) {
        if (isBlank(setterName)) {
            return null;
        }

        if (setterName.startsWith(PROPERTY_SET_PREFIX)) {
            String prop = setterName.substring(3);
            return convertPropertyName(prop);
        }
        return null;
    }

    @SuppressWarnings("ConstantConditions")
    public static boolean isSetter(@Nullable String name, @Nullable Class[] args) {
        if (isBlank(name) || args == null) {
            return false;
        }

        if (name.startsWith(PROPERTY_SET_PREFIX)) {
            if (args.length != 1) {
                return false;
            }
            name = name.substring(3);
            return name.length() > 0 && Character.isUpperCase(name.charAt(0));
        }

        return false;
    }

    /**
     * Returns true if the specified clazz parameter is either the same as, or is a superclass or super interface
     * of, the specified type parameter. Converts primitive types to compatible class automatically.
     *
     * @param clazz
     * @param type
     * @return True if the class is a taglib
     * @see Class#isAssignableFrom(Class)
     */
    public static boolean isAssignableOrConvertibleFrom(@Nullable Class<?> clazz, @Nullable Class<?> type) {
        if (type == null || clazz == null) {
            return false;
        } else if (type.isPrimitive()) {
            // convert primitive type to compatible class
            Class<?> primitiveClass = PRIMITIVE_TYPE_COMPATIBLE_CLASSES.get(type);
            return primitiveClass != null && clazz.isAssignableFrom(primitiveClass);
        } else {
            return clazz.isAssignableFrom(type);
        }
    }
}
