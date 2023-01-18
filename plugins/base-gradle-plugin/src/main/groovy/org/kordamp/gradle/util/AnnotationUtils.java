/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2018-2023 Andres Almiray.
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

import org.gradle.api.Project;
import org.kordamp.gradle.annotations.DependsOn;
import org.kordamp.gradle.annotations.Evicts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Named;
import javax.inject.Qualifier;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Objects.requireNonNull;
import static org.kordamp.gradle.util.ObjectUtils.isSetterMethod;
import static org.kordamp.gradle.util.ObjectUtils.requireState;
import static org.kordamp.gradle.util.StringUtils.getLogicalPropertyName;
import static org.kordamp.gradle.util.StringUtils.getPropertyName;
import static org.kordamp.gradle.util.StringUtils.isBlank;
import static org.kordamp.gradle.util.StringUtils.isNotBlank;
import static org.kordamp.gradle.util.StringUtils.uncapitalize;

/**
 * @author Andres Almiray
 * @since 0.37.0
 */
public class AnnotationUtils {
    private static final Logger LOG = LoggerFactory.getLogger(Project.class);
    private static final String ERROR_CLASS_NULL = "Argument 'class' must not be null";
    private static final String ERROR_METHOD_NULL = "Argument 'method' must not be null";
    private static final String ERROR_SUFFIX_NULL = "Argument 'suffix' must not be null";
    private static final String ERROR_INSTANCE_NULL = "Argument 'instance' must not be null";
    private static final String ERROR_ANNOTATION_NULL = "Argument 'annotation' must not be null";
    private static final String ERROR_ANNOTATION_TYPE_NULL = "Argument 'annotationType' must not be null";
    private static final String ERROR_FIELD_NULL = "Argument 'field' must not be null";
    private static final String ERROR_SETTER_METHOD_NULL = "Argument 'setterMethod' must not be null";
    private static final String VALUE = "value";

    private AnnotationUtils() {
        // prevent instantiation
    }

    @Nonnull
    public static List<Annotation> harvestQualifiers(@Nonnull Class<?> klass) {
        requireNonNull(klass, ERROR_CLASS_NULL);
        List<Annotation> list = new ArrayList<>();
        Annotation[] annotations = klass.getAnnotations();
        for (Annotation annotation : annotations) {
            if (AnnotationUtils.isAnnotatedWith(annotation, Qualifier.class)) {
                // special case for @Named
                if (Named.class.isAssignableFrom(annotation.getClass())) {
                    Named named = (Named) annotation;
                    if (isBlank(named.value())) {
                        list.add(named(getPropertyName(klass)));
                        continue;
                    }
                }
                list.add(annotation);
            }
        }
        return list;
    }

    @Nullable
    public static <A extends Annotation> A findAnnotation(@Nonnull Class<?> klass, @Nonnull Class<A> annotationType) {
        return findAnnotation(klass, annotationType, false);
    }

    @Nullable
    public static <A extends Annotation> A findAnnotation(@Nonnull Class<?> klass, @Nonnull Class<A> annotationType, boolean deep) {
        requireNonNull(klass, ERROR_CLASS_NULL);
        requireNonNull(annotationType, ERROR_ANNOTATION_TYPE_NULL);

        while (klass != null) {
            Annotation annotation = findAnnotation(klass.getAnnotations(), annotationType, deep);
            if (annotation != null) {
                return (A) annotation;
            }
            klass = klass.getSuperclass();
        }
        return null;
    }

    @Nullable
    public static <A extends Annotation> A findAnnotation(@Nonnull Method method, @Nonnull Class<A> annotationType) {
        return findAnnotation(method, annotationType, false);
    }

    @Nullable
    public static <A extends Annotation> A findAnnotation(@Nonnull Method method, @Nonnull Class<A> annotationType, boolean deep) {
        requireNonNull(method, ERROR_METHOD_NULL);
        requireNonNull(annotationType, ERROR_ANNOTATION_TYPE_NULL);

        for (Annotation annotation : method.getAnnotations()) {
            if (annotationType.equals(annotation.annotationType())) {
                return (A) annotation;
            }
            if (deep) {
                A a = findAnnotation(annotation.annotationType().getAnnotations(), annotationType, deep);
                if (a != null) {
                    return a;
                }
            }
        }

        return null;
    }

    @Nullable
    public static <A extends Annotation> A findAnnotation(@Nonnull Annotation[] annotations, @Nonnull Class<A> annotationType) {
        return findAnnotation(annotations, annotationType, false);
    }

    @Nullable
    public static <A extends Annotation> A findAnnotation(@Nonnull Annotation[] annotations, @Nonnull Class<A> annotationType, boolean deep) {
        requireNonNull(annotations, "Argument 'annotations' must not be null");
        requireNonNull(annotationType, ERROR_ANNOTATION_TYPE_NULL);

        for (Annotation annotation : annotations) {
            if (annotationType.isAssignableFrom(annotation.annotationType())) {
                return (A) annotation;
            }
            /*
            if (deep) {
                A a = findAnnotation(annotation.annotationType().getAnnotations(), annotationType, deep);
                if (a != null) {
                    return a;
                }
            }*/
        }
        return null;
    }

    public static boolean isAnnotatedWith(@Nonnull Object instance, @Nonnull Class<? extends Annotation> annotationType) {
        return isAnnotatedWith(requireNonNull(instance, ERROR_INSTANCE_NULL).getClass(), annotationType);
    }

    public static boolean isAnnotatedWith(@Nonnull Method method, @Nonnull Class<? extends Annotation> annotationType) {
        return isAnnotatedWith(method, annotationType, false);
    }

    public static boolean isAnnotatedWith(@Nonnull Method method, @Nonnull Class<? extends Annotation> annotationType, boolean deep) {
        requireNonNull(method, ERROR_METHOD_NULL);
        requireNonNull(annotationType, ERROR_ANNOTATION_TYPE_NULL);

        for (Annotation annotation : method.getAnnotations()) {
            if (annotationType.equals(annotation.annotationType())) {
                return true;
            }
            if (deep) {
                if (isAnnotatedWith(annotation, annotationType)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isAnnotatedWith(@Nonnull Annotation annotation, @Nonnull Class<? extends Annotation> annotationType) {
        return isAnnotatedWith(annotation, annotationType, false);
    }

    public static boolean isAnnotatedWith(@Nonnull Annotation annotation, @Nonnull Class<? extends Annotation> annotationType, boolean deep) {
        requireNonNull(annotation, ERROR_ANNOTATION_NULL);
        requireNonNull(annotationType, ERROR_ANNOTATION_TYPE_NULL);

        for (Annotation a : annotation.annotationType().getAnnotations()) {
            if (annotationType.equals(a.annotationType())) {
                return true;
            }

            if (deep) {
                if (isAnnotatedWith(a, annotationType)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isAnnotatedWith(@Nonnull Class<?> clazz, @Nonnull Class<? extends Annotation> annotationType) {
        requireNonNull(clazz, ERROR_CLASS_NULL);
        requireNonNull(annotationType, ERROR_ANNOTATION_TYPE_NULL);

        while (clazz != null) {
            for (Annotation annotation : clazz.getAnnotations()) {
                if (annotationType.equals(annotation.annotationType())) {
                    return true;
                }
            }
            for (Class<?> iface : clazz.getInterfaces()) {
                if (isAnnotatedWith(iface, annotationType)) {
                    return true;
                }
            }

            clazz = clazz.getSuperclass();
        }
        return false;
    }

    @Nonnull
    public static <T> T requireAnnotation(@Nonnull T instance, @Nonnull Class<? extends Annotation> annotationType) {
        if (!isAnnotatedWith(instance, annotationType)) {
            throw new IllegalArgumentException("Instance of " + instance.getClass() + " is not annotated with " + annotationType.getName());
        }
        return instance;
    }

    @Nonnull
    public static <T> Class<T> requireAnnotation(@Nonnull Class<T> klass, @Nonnull Class<? extends Annotation> annotationType) {
        if (!isAnnotatedWith(klass, annotationType)) {
            throw new IllegalArgumentException("Class " + klass.getName() + " is not annotated with " + annotationType.getName());
        }
        return klass;
    }

    @Nonnull
    public static String[] getDependsOn(@Nonnull Object instance) {
        requireNonNull(instance, ERROR_INSTANCE_NULL);

        DependsOn dependsOn = instance.getClass().getAnnotation(DependsOn.class);
        return dependsOn != null ? dependsOn.value() : new String[0];
    }

    @Nonnull
    public static String getEvicts(@Nonnull Object instance) {
        requireNonNull(instance, ERROR_INSTANCE_NULL);

        Evicts evicts = instance.getClass().getAnnotation(Evicts.class);
        return evicts != null ? evicts.value() : "";
    }

    @Nonnull
    public static String nameFor(@Nonnull Class<?> klass) {
        return nameFor(klass, false);
    }

    @Nonnull
    public static String nameFor(@Nonnull Class<?> klass, boolean simple) {
        requireNonNull(klass, ERROR_CLASS_NULL);

        Named annotation = klass.getAnnotation(Named.class);
        if (annotation != null && isNotBlank(annotation.value())) {
            return annotation.value();
        } else {
            return simple ? klass.getSimpleName() : klass.getName();
        }
    }

    @Nonnull
    public static String nameFor(@Nonnull Object instance) {
        return nameFor(instance, false);
    }

    @Nonnull
    public static String nameFor(@Nonnull Object instance, boolean simple) {
        requireNonNull(instance, ERROR_INSTANCE_NULL);

        Named annotation = instance.getClass().getAnnotation(Named.class);
        if (annotation != null && isNotBlank(annotation.value())) {
            return annotation.value();
        } else {
            return simple ? instance.getClass().getSimpleName() : instance.getClass().getName();
        }
    }

    @Nonnull
    public static String nameFor(@Nonnull Field field) {
        return nameFor(field, false);
    }

    @Nonnull
    public static String nameFor(@Nonnull Field field, boolean simple) {
        requireNonNull(field, ERROR_FIELD_NULL);

        Named annotation = field.getAnnotation(Named.class);
        if (annotation != null && isNotBlank(annotation.value())) {
            return annotation.value();
        } else {
            return simple ? field.getType().getSimpleName() : field.getType().getName();
        }
    }

    @Nonnull
    public static String[] namesFor(@Nonnull Field field) {
        requireNonNull(field, ERROR_FIELD_NULL);

        List<String> names = new ArrayList<>();
        Named annotation = field.getAnnotation(Named.class);
        if (annotation != null && isNotBlank(annotation.value())) {
            names.add(annotation.value());
        } else {
            names.add(field.getName());
        }
        names.add(field.getType().getName());
        return names.toArray(new String[names.size()]);
    }

    @Nonnull
    public static String nameFor(@Nonnull Method setterMethod) {
        requireNonNull(setterMethod, ERROR_SETTER_METHOD_NULL);

        Class<?>[] parameterTypes = setterMethod.getParameterTypes();
        requireState(parameterTypes != null && parameterTypes.length > 0, "Argument 'setterMethod' must have at least one parameter. " + MethodDescriptor.forMethod(setterMethod));

        Named annotation = findAnnotation(annotationsOfMethodParameter(setterMethod, 0), Named.class);
        if (annotation != null && isNotBlank(annotation.value())) {
            return annotation.value();
        } else {
            return parameterTypes[0].getName();
        }
    }

    @Nonnull
    public static String[] namesFor(@Nonnull Method setterMethod) {
        requireNonNull(setterMethod, ERROR_SETTER_METHOD_NULL);

        Class<?>[] parameterTypes = setterMethod.getParameterTypes();
        requireState(parameterTypes != null && parameterTypes.length > 0, "Argument 'setterMethod' must have at least one parameter. " + MethodDescriptor.forMethod(setterMethod));


        List<String> names = new ArrayList<>();
        Named annotation = findAnnotation(annotationsOfMethodParameter(setterMethod, 0), Named.class);
        if (annotation != null && isNotBlank(annotation.value())) {
            names.add(annotation.value());
        } else {
            if (isSetterMethod(setterMethod)) {
                names.add(uncapitalize(setterMethod.getName().substring(3)));
            } else {
                names.add(uncapitalize(setterMethod.getName()));
            }
        }
        names.add(parameterTypes[0].getName());
        return names.toArray(new String[names.size()]);
    }

    @Nonnull
    public static Class<?> parameterTypeAt(@Nonnull Method method, int index) {
        requireNonNull(method, "Argument 'method' must not be null");

        Class<?>[] parameterTypes = method.getParameterTypes();
        requireState(parameterTypes != null && parameterTypes.length >= index, "Index " + index + " is out of bounds");

        return parameterTypes[index];
    }

    @Nonnull
    public static Annotation[] annotationsOfMethodParameter(@Nonnull Method method, int index) {
        parameterTypeAt(method, index);
        return method.getParameterAnnotations()[index];
    }

    @Nonnull
    public static <T> Map<String, T> mapInstancesByName(@Nonnull Collection<T> instances, @Nonnull String suffix) {
        Map<String, T> map = new LinkedHashMap<>();

        for (T instance : instances) {
            map.put(getLogicalPropertyName(nameFor(instance), suffix), instance);
        }

        return map;
    }

    @Nonnull
    public static <T> Map<String, T> mapInstancesByName(@Nonnull String context, @Nonnull Collection<T> instances, @Nonnull String suffix, @Nonnull String type) {
        Map<String, T> map = new LinkedHashMap<>();

        for (T instance : instances) {
            String currentEvicts = getEvicts(instance);
            String name = getLogicalPropertyName(nameFor(instance), suffix);
            Object evictedInstance = isBlank(currentEvicts) ? null : map.get(currentEvicts);

            if (evictedInstance != null) {
                String evictedEvicts = getEvicts(evictedInstance);
                if (isNotBlank(evictedEvicts)) {
                    throw new IllegalArgumentException(type + " " + name + " has an eviction conflict between " + instance + " and " + evictedInstance);
                } else {
                    name = currentEvicts;
                    LOG.info("[" + context + "] {} {} with instance {} evicted by {}", type, name, evictedInstance, instance);
                    map.put(name, instance);
                }
            } else {
                name = isBlank(currentEvicts) ? name : currentEvicts;
                Object previousInstance = map.get(name);
                if (previousInstance != null) {
                    if (isBlank(getEvicts(previousInstance))) {
                        throw new IllegalArgumentException(type + " " + name + " neither " + instance + " nor " + previousInstance + " is marked with @Evict");
                    } else {
                        LOG.info("[" + context + "] {} {} with instance {} evicted by {}", type, name, instance, previousInstance);
                    }
                } else {
                    map.put(name, instance);
                }
            }
        }

        return map;
    }

    @Nonnull
    public static <T> Map<String, T> sortByDependencies(@Nonnull String context, @Nonnull Collection<T> instances, @Nonnull String suffix, @Nonnull String type) {
        return sortByDependencies(context, instances, suffix, type, Collections.emptyList());
    }

    @Nonnull
    public static <T> Map<String, T> sortByDependencies(@Nonnull String context, @Nonnull Collection<T> instances, @Nonnull String suffix, @Nonnull String type, @Nonnull List<String> order) {
        requireNonNull(instances, "Argument 'instances' must not be null");
        requireNonNull(suffix, ERROR_SUFFIX_NULL);
        requireNonNull(type, "Argument 'type' must not be null");
        requireNonNull(order, "Argument 'order' must not be null");

        Map<String, T> instancesByName = mapInstancesByName(context, instances, suffix, type);

        Map<String, T> map = new LinkedHashMap<>(instancesByName);

        if (!order.isEmpty()) {
            Map<String, T> tmp1 = new LinkedHashMap<>(instancesByName);
            Map<String, T> tmp2 = new LinkedHashMap<>();
            //noinspection ConstantConditions
            for (String name : order) {
                if (tmp1.containsKey(name)) {
                    tmp2.put(name, tmp1.remove(name));
                }
            }
            tmp2.putAll(tmp1);
            map.clear();
            map.putAll(tmp2);
        }

        List<T> sorted = new ArrayList<>();
        Set<String> instanceDeps = new LinkedHashSet<>();

        while (!map.isEmpty()) {
            int processed = 0;

            LOG.info("[" + context + "] Current {} order is {}", type, instancesByName.keySet());

            for (Iterator<Map.Entry<String, T>> iter = map.entrySet().iterator(); iter.hasNext(); ) {
                Map.Entry<String, T> entry = iter.next();
                String instanceName = entry.getKey();
                String[] dependsOn = getDependsOn(entry.getValue());

                LOG.debug("[" + context + "] Processing {} '{}'", type, instanceName);
                LOG.debug("[" + context + "]   depends on '{}'", Arrays.toString(dependsOn));

                if (dependsOn.length != 0) {
                    LOG.debug("[" + context + "]   checking {} '{}' dependencies ({})", type, instanceName, dependsOn.length);

                    boolean failedDep = false;
                    for (String dep : dependsOn) {
                        LOG.debug("[" + context + "]   checking {} '{}' dependency {}", type, instanceName, dep);
                        if (!instanceDeps.contains(dep)) {
                            // dep not in the list yet, we need to skip adding this to the list for now
                            LOG.debug("[" + context + "]   skipped {} '{}', since dependency '{}' not yet added", type, instanceName, dep);
                            failedDep = true;
                            break;
                        } else {
                            LOG.debug("[" + context + "]   {} '{}' dependency '{}' already added", type, instanceName, dep);
                        }
                    }

                    if (failedDep) {
                        // move on to next dependency
                        continue;
                    }
                }

                LOG.debug("[" + context + "]   adding {} '{}', since all dependencies have been added", type, instanceName);
                sorted.add(entry.getValue());
                instanceDeps.add(instanceName);
                iter.remove();
                processed++;
            }

            if (processed == 0) {
                // we have a cyclical dependency, warn the user and load in the order they appeared originally
                LOG.debug("[" + context + "]   unresolved {} dependencies detected", type);
                for (Map.Entry<String, T> entry : map.entrySet()) {
                    String instanceName = entry.getKey();
                    String[] dependsOn = getDependsOn(entry.getValue());

                    // display this as a cyclical dep
                    LOG.debug("[" + context + "]   {} {} ", type, instanceName);
                    if (dependsOn.length != 0) {
                        for (String dep : dependsOn) {
                            LOG.debug("    depends on {}", dep);
                        }
                    } else {
                        // we should only have items left in the list with deps, so this should never happen
                        // but a wise man once said...check for true, false and otherwise...just in case
                        LOG.debug("[" + context + "]   problem while resolving dependencies.");
                        LOG.debug("[" + context + "]   unable to resolve dependency hierarchy.");
                    }
                    if (!instanceDeps.contains(instanceName)) {
                        LOG.debug("[" + context + "]   adding {} '{}' at end", type, instanceName);
                        sorted.add(entry.getValue());
                        instanceDeps.add(instanceName);
                    }
                }
                break;
                // if we have processed all the instances, we are done
            } else if (sorted.size() == instancesByName.size()) {
                LOG.info("[" + context + "] {} dependency ordering complete", type);
                break;
            }
        }

        instancesByName = mapInstancesByName(sorted, suffix);
        LOG.info("[" + context + "] Computed {} order is {}", type, instancesByName.keySet());

        return instancesByName;
    }

    @Nonnull
    public static Named named(@Nonnull String name) {
        return new NamedImpl(requireNonNull(name, "Argument 'name' must not be null"));
    }

    @SuppressWarnings("ClassExplicitlyAnnotation")
    private static class NamedImpl implements Named, Serializable {
        private static final long serialVersionUID = 0;
        private final String value;

        public NamedImpl(String value) {
            this.value = requireNonNull(value, VALUE);
        }

        public String value() {
            return this.value;
        }

        public int hashCode() {
            // This is specified in java.lang.Annotation.
            return (127 * VALUE.hashCode()) ^ value.hashCode();
        }

        public boolean equals(Object o) {
            if (!(o instanceof Named)) {
                return false;
            }

            Named other = (Named) o;
            return value.equals(other.value());
        }

        public String toString() {
            return "@" + Named.class.getName() + "(value=" + value + ")";
        }

        public Class<? extends Annotation> annotationType() {
            return Named.class;
        }
    }
}
