package com.infamous.call_of_the_wild.common.util;

import com.google.common.collect.Maps;
import com.infamous.call_of_the_wild.AllBarkAllBite;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;

public class ReflectionUtil {
    private static final Map<String, Field> CACHED_FIELDS = Maps.newHashMap();

    private static final Map<String, Method> CACHED_METHODS = Maps.newHashMap();

    @SuppressWarnings("unchecked")
    @Nullable
    public static <T> T callMethod(String methodName, Object obj, Object... args){
        Method method = CACHED_METHODS.computeIfAbsent(methodName, k -> {
            Class<?>[] argClasses = new Class[args.length];
            for(int i = 0; i < args.length; i++){
                argClasses[i] = args[i].getClass();
            }
            return ObfuscationReflectionHelper.findMethod(obj.getClass(), methodName, argClasses);
        });

        try {
            return (T) method.invoke(obj, args);
        } catch (IllegalAccessException | InvocationTargetException e) {
            AllBarkAllBite.LOGGER.error("Reflection error for method name {} called on {} given {}", methodName, obj, Arrays.toString(args));
            return null;
        }
    }

    public static void setField(String fieldName, Class<?> targetClass, Object obj, Object value){
        Field field = CACHED_FIELDS.computeIfAbsent(fieldName, k -> ObfuscationReflectionHelper.findField(targetClass, fieldName));

        try {
            field.set(obj, value);
        } catch (IllegalAccessException e) {
            AllBarkAllBite.LOGGER.error("Reflection error for field name {} modified on {} given {}", fieldName, obj, value);
        }
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public static <T> T getField(String fieldName, Class<?> targetClass, Object obj){
        Field field = CACHED_FIELDS.computeIfAbsent(fieldName, k -> ObfuscationReflectionHelper.findField(targetClass, fieldName));

        try {
            return (T) field.get(obj);
        } catch (IllegalAccessException e) {
            AllBarkAllBite.LOGGER.error("Reflection error for field named {} retrieved on {}", fieldName, obj);
            return null;
        }
    }
}
