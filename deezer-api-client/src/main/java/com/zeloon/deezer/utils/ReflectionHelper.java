package com.zeloon.deezer.utils;


import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class ReflectionHelper {
    public static boolean setFinalField(Object object, String field, Object value) {
        try {
            Field f = object.getClass().getField(field);
            boolean accessible = f.isAccessible();

            f.setAccessible(true);

            Field modifiers = Field.class.getDeclaredField("modifiers");
            modifiers.setAccessible(true);
            modifiers.setInt(f, f.getModifiers() & ~Modifier.FINAL);

            f.set(object, value);

            modifiers.setInt(f, f.getModifiers() | Modifier.FINAL);
            modifiers.setAccessible(false);

            f.setAccessible(accessible);
        } catch (NoSuchFieldException e) {
            return false;
        } catch (IllegalAccessException e) {
            return false;
        }
        return true;
    }
}
