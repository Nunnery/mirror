/**
 * The MIT License
 * Copyright (c) 2015 Teal Cube Games
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.tealcube.minecraft.bukkit.mirror;

import com.google.common.base.Preconditions;
import org.bukkit.Bukkit;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
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
            case MINECRAFT_SERVER:
                if (CACHED_NMS.containsKey(className)) {
                    return CACHED_NMS.get(className);
                }
                break;
            case CRAFTBUKKIT:
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
        if (type == ClassType.MINECRAFT_SERVER) {
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
        Class<?>[] primArgs = toPrimitiveTypeArray(args);
        for (Method m : clazz.getDeclaredMethods()) {
            Class<?>[] primParam = toPrimitiveTypeArray(m.getParameterTypes());
            if (m.getName().equals(methodName) && isArrayTypeEqual(primArgs, primParam)) {
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

}
