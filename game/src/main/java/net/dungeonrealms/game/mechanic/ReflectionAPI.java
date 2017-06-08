package net.dungeonrealms.game.mechanic;

import lombok.SneakyThrows;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

/**
 */
public class ReflectionAPI {

    //Caching these found fields is much faster then having reflection do it everytime.
    private static Map<Class<?>, Map<String, Field>> cachedFields = new HashMap<>();

    public static Field getDeclaredField(Class<?> target, String fieldName) {
        Map<String, Field> stored = cachedFields.computeIfAbsent(target, m -> new HashMap<>());
        Field found = stored.get(fieldName);
        if (found != null) return found;

        try {
            Field field = target.getDeclaredField(fieldName);
            field.setAccessible(true);
            stored.put(fieldName, field);
            return field;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public static Method getDeclaredMethod(Class<?> target, String fieldName, Class<?>... params) {
        try {
            Method field = target.getDeclaredMethod(fieldName, params);
            field.setAccessible(true);
            return field;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

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

    @SneakyThrows
    public static void setField(String fieldName, Class<?> clazz, Object instance, Object value) {
        Field field = getDeclaredField(clazz, fieldName);
        if (field != null)
            field.set(instance, value);
    }

    @SneakyThrows
    public static void setField(String fieldName, Object instance, Object value) {
        Field field = getDeclaredField(instance.getClass(), fieldName);
        if (field != null)
            field.set(instance, value);
    }

    @SneakyThrows
    public static Object getObjectFromField(String fieldName, Class<?> from, Object instance) {
        Field field = getDeclaredField(from, fieldName);
        if (field != null) {
            return field.get(instance);
        }
        return null;
    }
    @SneakyThrows
    public static Object getObjectFromField(String fieldName, Object instance) {
        return getObjectFromField(fieldName, instance.getClass(), instance);
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
