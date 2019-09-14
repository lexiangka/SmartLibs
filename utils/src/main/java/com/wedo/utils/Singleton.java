package com.wedo.utils;


import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("all")
public class Singleton {

    private static Map<Class<? extends Singleton>, Singleton> INSTANCES_MAP =
            new HashMap<Class<? extends Singleton>, Singleton>();

    protected Singleton() {
    }

    public synchronized static <E extends Singleton> Singleton getInstance(Class<E> instanceClass) {
        if (INSTANCES_MAP.containsKey(instanceClass)) {
            return (E) INSTANCES_MAP.get(instanceClass);
        } else {
            try {
                E instance = instanceClass.newInstance();
                INSTANCES_MAP.put(instanceClass, instance);
                return instance;
            } catch (Exception e) {
                return null;
            }
        }
    }
}