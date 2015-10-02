package net.dungeonrealms.mechanics;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by Kieran on 9/27/2015.
 * Based from some Bukkit API from 2014.
 */
public class ReflectionAPI {

    /**
     * Finds a declared field of a specific type in the target class. With a specific index.
     *
     * @param target    the target class to retrieve the fields from
     * @param fieldType the field type that you want to search for
     * @param index     the field index starting in the specified order
     * @return the field
     */
    public static Field findField(Class<?> target, Class<?> fieldType, int index) {
        return findField(target, fieldType, index, false);
    }

    /**
     * Finds a declared field of a specific type in the target class. With a specific index.
     *
     * @param target    the target class to retrieve the fields from
     * @param fieldType the field type that you want to search for
     * @param index     the field index starting in the specified order
     * @param reverse   whether you want to reverse the order of the fields
     * @return the field
     */
    public static Field findField(Class<?> target, Class<?> fieldType, int index, boolean reverse) {
        List<Field> fields = new ArrayList<>();
        fields.addAll(Arrays.asList(target.getDeclaredFields()));
        if (reverse) {
            Collections.reverse(fields);
        }
        for (Field field : fields.toArray(new Field[fields.size()])) {
            field.setAccessible(true);
            if (field.getType().isAssignableFrom(fieldType)) {
                if (index == 0) {
                    return field;
                }
                index--;
            }
        }
        return null;
    }

    /**
     * Finds all the declared fields of a specific type in the target class.
     *
     * @param target    the target class to retrieve the fields from
     * @param fieldType the field type that you want to search for
     * @return the fields
     */
    public static Field[] findFields(Class<?> target, Class<?> fieldType) {
        return findFields(target, fieldType, 0);
    }

    /**
     * Finds all the declared fields of a specific type in the target class.
     *
     * @param target    the target class to retrieve the fields from
     * @param fieldType the field type that you want to search for
     * @param depth     the depth you want to check for underlying classes their fields
     * @return the fields
     */
    public static Field[] findFields(Class<?> target, Class<?> fieldType, int depth) {
        List<Field> list = new ArrayList<>();
        while (target != null && target != Object.class) {
            for (Field field : target.getDeclaredFields()) {
                field.setAccessible(true);
                if (field.getType().isAssignableFrom(fieldType)) {
                    list.add(field);
                }
            }
            target = target.getSuperclass();
            if (depth != -1 && depth-- == 0) {
                break;
            }
        }
        return list.toArray(new Field[list.size()]);
    }

    /**
     * Finds the objects of all declared fields with a specific type in the
     * target class.
     *
     * @param target       the target class to retrieve the fields from
     * @param fieldType    the field type that you want to search for
     * @param targetObject the target object you want to retrieve the object of
     * @return the objects
     */
    public static Object[] findFieldsAndGet(Class<?> target, Class<?> fieldType, Object targetObject) {
        return findFieldsAndGet(target, fieldType, targetObject, 0);
    }

    /**
     * Finds the objects of all declared fields with a specific type in the
     * target class.
     *
     * @param target       the target class to retrieve the fields from
     * @param fieldType    the field type that you want to search for
     * @param targetObject the target object you want to retrieve the object of
     * @param depth        the depth you want to check for underlying classes their fields
     * @return the objects
     */
    public static Object[] findFieldsAndGet(Class<?> target, Class<?> fieldType, Object targetObject, int depth) {
        List<Object> list = new ArrayList<>();
        while (target != null && target != Object.class) {
            for (Field field : target.getDeclaredFields()) {
                field.setAccessible(true);
                if (field.getType().isAssignableFrom(fieldType)) {
                    try {
                        list.add(field.get(targetObject));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            target = target.getSuperclass();
            if (depth != -1 && depth-- == 0) {
                break;
            }
        }
        return list.toArray(new Object[list.size()]);
    }

}
