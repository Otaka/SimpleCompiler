package com.simplecompiler.interpreter;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author sad
 */
public class NativeFunctionManager {

    private Map<String, Function> functionsMap = new HashMap<>();
    private Set<Class> processedClasses = new HashSet<>();

    public void registerFunctions(Object objectWithFunctions) {
        Class clazz = objectWithFunctions.getClass();
        while (clazz != null) {
            if (clazz == Object.class) {
                break;
            }

            if (processedClasses.contains(clazz)) {
                throw new IllegalArgumentException("You already register functions from class " + clazz.getSimpleName());
            }

            processedClasses.add(clazz);

            for (Method method : clazz.getDeclaredMethods()) {
                if (method.getAnnotation(NativeFunction.class) != null) {
                    method.setAccessible(true);
                    Function function = new Function();
                    function.name = method.getAnnotation(NativeFunction.class).name();
                    function.ownerObject = objectWithFunctions;
                    if ((method.getModifiers() & Modifier.STATIC) > 0) {
                        function.ownerObject = null;
                    }
                    function.method = method;
                    if (functionsMap.containsKey(function.name)) {
                        throw new IllegalArgumentException("You already registered native function [" + function.name + "]");
                    }
                    functionsMap.put(function.name, function);
                }
            }

            clazz = clazz.getSuperclass();
        }
    }

    public Function findFunction(String name) {
        return functionsMap.get(name);
    }

    public static class Function {

        public String name;
        public Method method;
        public Object ownerObject;
    }
}
