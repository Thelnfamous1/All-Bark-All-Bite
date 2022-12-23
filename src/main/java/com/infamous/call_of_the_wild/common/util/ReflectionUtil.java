package com.infamous.call_of_the_wild.common.util;

import com.google.common.collect.Maps;
import com.infamous.call_of_the_wild.CallOfTheWild;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;

public class ReflectionUtil {

    private static final Map<String, Method> CACHED_METHODS = Maps.newHashMap();

    @SuppressWarnings("unchecked")
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
            CallOfTheWild.LOGGER.error("Reflection error for method name {} called on {} given {}", methodName, obj, Arrays.toString(args));
            throw new RuntimeException(e);
        }
    }
}
