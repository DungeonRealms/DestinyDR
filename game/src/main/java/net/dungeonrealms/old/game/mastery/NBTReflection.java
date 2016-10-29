package net.dungeonrealms.old.game.mastery;

import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

/**
 * Created by Kieran Quigley (Proxying) on 27-May-16.
 */
public class NBTReflection {

    private static Class getCraftItemStack() {
        String version = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];
        try {
            return Class.forName("org.bukkit.craftbukkit." + version + ".inventory.CraftItemStack");
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    private static Object getNewNBTTag() {
        String version = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];
        try {
            Class c = Class.forName("net.minecraft.server." + version + ".NBTTagCompound");
            return c.newInstance();
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    private static Object setNBTTag(Object NBTTag, Object NMSItem) {
        try {
            java.lang.reflect.Method method;
            method = NMSItem.getClass().getMethod("setTag", NBTTag.getClass());
            method.invoke(NMSItem, NBTTag);
            return NMSItem;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    private static Object getNMSItemStack(ItemStack item) {
        Class cis = getCraftItemStack();
        java.lang.reflect.Method method;
        try {
            assert cis != null;
            method = cis.getMethod("asNMSCopy", ItemStack.class);
            return method.invoke(cis, item);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static ItemStack getBukkitItemStack(Object item) {
        Class cis = getCraftItemStack();
        java.lang.reflect.Method method;
        try {
            assert cis != null;
            method = cis.getMethod("asCraftMirror", item.getClass());
            Object answer = method.invoke(cis, item);
            return (ItemStack) answer;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static Object getNBTTagCompound(Object nmsItem) {
        Class c = nmsItem.getClass();
        java.lang.reflect.Method method;
        try {
            method = c.getMethod("getTag");
            return method.invoke(nmsItem);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static ItemStack setString(ItemStack item, String key, String Text) {
        Object nmsItem = getNMSItemStack(item);
        if (nmsItem == null) {
            return null;
        }
        Object nbtTag = getNBTTagCompound(nmsItem);
        if (nbtTag == null) {
            nbtTag = getNewNBTTag();
        }
        java.lang.reflect.Method method;
        try {
            assert nbtTag != null;
            method = nbtTag.getClass().getMethod("setString", String.class, String.class);
            method.invoke(nbtTag, key, Text);
            nmsItem = setNBTTag(nbtTag, nmsItem);
            return getBukkitItemStack(nmsItem);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return item;
    }

    public static String getString(ItemStack item, String key) {
        Object nmsItem = getNMSItemStack(item);
        if (nmsItem == null) {
            return null;
        }
        Object nbtTag = getNBTTagCompound(nmsItem);
        if (nbtTag == null) {
            nbtTag = getNewNBTTag();
        }
        java.lang.reflect.Method method;
        try {
            assert nbtTag != null;
            method = nbtTag.getClass().getMethod("getString", String.class);
            return (String) method.invoke(nbtTag, key);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static ItemStack setInt(ItemStack item, String key, Integer i) {
        Object nmsItem = getNMSItemStack(item);
        if (nmsItem == null) {
            return null;
        }
        Object nbtTag = getNBTTagCompound(nmsItem);
        if (nbtTag == null) {
            nbtTag = getNewNBTTag();
        }
        java.lang.reflect.Method method;
        try {
            assert nbtTag != null;
            method = nbtTag.getClass().getMethod("setInt", String.class, int.class);
            method.invoke(nbtTag, key, i);
            nmsItem = setNBTTag(nbtTag, nmsItem);
            return getBukkitItemStack(nmsItem);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return item;
    }

    public static Integer getInt(ItemStack item, String key) {
        Object nmsItem = getNMSItemStack(item);
        if (nmsItem == null) {
            return null;
        }
        Object nbtTag = getNBTTagCompound(nmsItem);
        if (nbtTag == null) {
            nbtTag = getNewNBTTag();
        }
        java.lang.reflect.Method method;
        try {
            assert nbtTag != null;
            method = nbtTag.getClass().getMethod("getInt", String.class);
            return (Integer) method.invoke(nbtTag, key);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static ItemStack setDouble(ItemStack item, String key, Double d) {
        Object nmsItem = getNMSItemStack(item);
        if (nmsItem == null) {
            return null;
        }
        Object nbtTag = getNBTTagCompound(nmsItem);
        if (nbtTag == null) {
            nbtTag = getNewNBTTag();
        }
        java.lang.reflect.Method method;
        try {
            assert nbtTag != null;
            method = nbtTag.getClass().getMethod("setDouble", String.class, double.class);
            method.invoke(nbtTag, key, d);
            nmsItem = setNBTTag(nbtTag, nmsItem);
            return getBukkitItemStack(nmsItem);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return item;
    }

    public static Double getDouble(ItemStack item, String key) {
        Object nmsItem = getNMSItemStack(item);
        if (nmsItem == null) {
            return null;
        }
        Object nbtTag = getNBTTagCompound(nmsItem);
        if (nbtTag == null) {
            nbtTag = getNewNBTTag();
        }
        java.lang.reflect.Method method;
        try {
            assert nbtTag != null;
            method = nbtTag.getClass().getMethod("getDouble", String.class);
            return (Double) method.invoke(nbtTag, key);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static ItemStack setBoolean(ItemStack item, String key, Boolean d) {
        Object nmsItem = getNMSItemStack(item);
        if (nmsItem == null) {
            return null;
        }
        Object nbtTag = getNBTTagCompound(nmsItem);
        if (nbtTag == null) {
            nbtTag = getNewNBTTag();
        }
        java.lang.reflect.Method method;
        try {
            assert nbtTag != null;
            method = nbtTag.getClass().getMethod("setBoolean", String.class, boolean.class);
            method.invoke(nbtTag, key, d);
            nmsItem = setNBTTag(nbtTag, nmsItem);
            return getBukkitItemStack(nmsItem);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return item;
    }

    public static Boolean getBoolean(ItemStack item, String key) {
        Object nmsItem = getNMSItemStack(item);
        if (nmsItem == null) {
            return null;
        }
        Object nbtTag = getNBTTagCompound(nmsItem);
        if (nbtTag == null) {
            nbtTag = getNewNBTTag();
        }
        java.lang.reflect.Method method;
        try {
            assert nbtTag != null;
            method = nbtTag.getClass().getMethod("getBoolean", String.class);
            return (Boolean) method.invoke(nbtTag, key);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static Boolean hasKey(ItemStack item, String key) {
        Object nmsItem = getNMSItemStack(item);
        if (nmsItem == null) {
            return null;
        }
        Object nbtTag = getNBTTagCompound(nmsItem);
        if (nbtTag == null) {
            nbtTag = getNewNBTTag();
        }
        java.lang.reflect.Method method;
        try {
            assert nbtTag != null;
            method = nbtTag.getClass().getMethod("hasKey", String.class);
            return (Boolean) method.invoke(nbtTag, key);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }


}
