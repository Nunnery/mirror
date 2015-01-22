/*
 * This file is part of Mirror, licensed under the ISC License.
 *
 * Copyright (c) 2015 Richard Harrah
 *
 * Permission to use, copy, modify, and/or distribute this software for any purpose with or without fee is hereby granted,
 * provided that the above copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES WITH REGARD TO THIS SOFTWARE INCLUDING ALL
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY SPECIAL, DIRECT,
 * INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE OF
 * THIS SOFTWARE.
 */
package me.topplethenun.reflect;

import com.google.common.base.Preconditions;
import org.bukkit.Bukkit;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public final class Mirror {

    private static final Map<String, Class<?>> CACHED_NMS = new HashMap<>();
    private static final Map<String, Class<?>> CACHED_OBC = new HashMap<>();
    private static final Map<Class<?>, Map<String, Map<ArrayWrapper<Class<?>>, Method>>> CACHED_METHODS = new
            HashMap<>();
    private static final Map<Class<?>, Map<String, Field>> CACHED_FIELDS = new HashMap<>();
    private static final Map<Class<?>, Class<?>> CORRESPONDING_TYPES = new HashMap<>();

    static {
        CORRESPONDING_TYPES.put(Integer.class, int.class);
        CORRESPONDING_TYPES.put(Long.class, long.class);
        CORRESPONDING_TYPES.put(Short.class, short.class);
        CORRESPONDING_TYPES.put(Byte.class, byte.class);
        CORRESPONDING_TYPES.put(Double.class, double.class);
        CORRESPONDING_TYPES.put(Float.class, float.class);
        CORRESPONDING_TYPES.put(Character.class, char.class);
        CORRESPONDING_TYPES.put(Boolean.class, boolean.class);
    }

    public static String getVersion() {
        return Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];
    }

    public static Class<?> getClass(String className, ClassType type) {
        Preconditions.checkNotNull(className, "className cannot be null");
        Preconditions.checkNotNull(type, "type cannot be null");
        switch (type) {
            case NMS:
                if (CACHED_NMS.containsKey(className)) {
                    return CACHED_NMS.get(className);
                }
                break;
            case CB:
                if (CACHED_OBC.containsKey(className)) {
                    return CACHED_OBC.get(className);
                }
                break;
            default:
                break;
        }
        Class<?> clazz = null;
        try {
            clazz = Class.forName(type + getVersion() + "." + className);
        } catch (Exception ignored) {
            // do nothing
        }
        if (type == ClassType.NMS) {
            CACHED_NMS.put(className, clazz);
        } else {
            CACHED_OBC.put(className, clazz);
        }
        return clazz;
    }

    public static Method getMethod(Class<?> clazz, String methodName, Class<?>... args) {
        Preconditions.checkNotNull(clazz, "clazz cannot be null");
        Preconditions.checkNotNull(methodName, "methodName cannot be null");
        if (!CACHED_METHODS.containsKey(clazz)) {
            CACHED_METHODS.put(clazz, new HashMap<String, Map<ArrayWrapper<Class<?>>, Method>>());
        }
        Map<String, Map<ArrayWrapper<Class<?>>, Method>> clazzMethods = CACHED_METHODS.get(clazz);
        if (!clazzMethods.containsKey(methodName)) {
            clazzMethods.put(methodName, new HashMap<ArrayWrapper<Class<?>>, Method>());
        }
        Map<ArrayWrapper<Class<?>>, Method> methodMap = clazzMethods.get(methodName);
        ArrayWrapper<Class<?>> wrapped = new ArrayWrapper<>(args);
        if (methodMap.containsKey(wrapped)) {
            return methodMap.get(wrapped);
        }
        for (Method m : clazz.getDeclaredMethods()) {
            if (m.getName().equals(methodName) && Arrays.equals(m.getParameterTypes(), args)) {
                m.setAccessible(true);
                methodMap.put(wrapped, m);
                return m;
            }
        }
        return null;
    }

    public static Field getField(Class<?> clazz, String fieldName) {
        Preconditions.checkNotNull(clazz, "clazz cannot be null");
        Preconditions.checkNotNull(fieldName, "fieldName cannot be null");
        if (!CACHED_FIELDS.containsKey(clazz)) {
            CACHED_FIELDS.put(clazz, new HashMap<String, Field>());
        }
        Map<String, Field> clazzFields = CACHED_FIELDS.get(clazz);
        if (clazzFields.containsKey(fieldName)) {
            return clazzFields.get(fieldName);
        }
        for (Field field : clazz.getDeclaredFields()) {
            if (field.getName().equals(fieldName)) {
                field.setAccessible(true);
                clazzFields.put(fieldName, field);
                return field;
            }
        }
        clazzFields.put(fieldName, null);
        return null;
    }

    private static Class<?> getPrimitiveType(Class<?> clazz) {
        return CORRESPONDING_TYPES.containsKey(clazz) ? CORRESPONDING_TYPES.get(clazz) : clazz;
    }

    private static Class<?>[] toPrimitiveTypeArray(Class<?>[] classes) {
        int size = classes != null ? classes.length : 0;
        Class<?>[] types = new Class<?>[size];
        for (int i = 0; i < size; i++) {
            types[i] = getPrimitiveType(classes[i]);
        }
        return types;
    }

    private static boolean isArrayTypeEqual(Class<?>[] a, Class<?>[] b) {
        if (a.length != b.length) {
            return false;
        }
        for (int i = 0; i < a.length; i++) {
            if (!a[i].equals(b[i]) && !a[i].isAssignableFrom(b[i])) {
                return false;
            }
        }
        return true;
    }

    private Mirror() {
        // do nothing
    }

    public static class ArrayWrapper<E> {
        private E[] elements;

        public ArrayWrapper(E[] elements) {
            this.elements = elements;
        }

        public E[] getElements() {
            return elements;
        }

        public void setElements(E[] elements) {
            this.elements = elements;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof ArrayWrapper)) {
                return false;
            }

            ArrayWrapper that = (ArrayWrapper) o;

            return Arrays.equals(elements, that.elements);
        }

        @Override
        public int hashCode() {
            return elements != null ? Arrays.hashCode(elements) : 0;
        }
    }

    public enum ClassType {
        NMS("net.minecraft.server."),
        CB("org.bukkit.craftbukkit.");

        private final String pack;

        private ClassType(String s) {
            pack = s;
        }

        @Override
        public String toString() {
            return pack;
        }
    }

}
