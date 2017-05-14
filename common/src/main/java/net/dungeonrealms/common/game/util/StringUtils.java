package net.dungeonrealms.common.game.util;

import com.google.common.collect.Lists;

import java.util.*;

import org.bukkit.Bukkit;

public class StringUtils {

	public static <T extends Enum<T>> String serializeEnumList(List<T> values) {
	    if(values == null)return "";
		String s = "";
		for (T val : values)
			s += val.name() + ",";
		return s;
	}

    public static String serializeEnumNumberMap(Map<Enum,Number> values) {
        if(values == null || values.isEmpty())return "";
        String s = "";
        for (Map.Entry<Enum,Number> val : values.entrySet()) {
            s += val.getKey().toString() + ";" + val.getValue().toString();
            s += ",";
        }
        return s;
    }

    public static <T extends Enum<T>> String serializeEnumList(Set<T> values) {
        if (values == null) return "";
        String s = "";
        for (T val : values)
            s += val.name() + ",";
        return s;

    }

    public static <T extends Enum<T>, E extends Number> Map<T,E> deserializeNumberMap(String s, Class<T> c, Class<E> numClass) {
        Map<T,E> map = new HashMap<>();
        if(s == null || s.isEmpty())return map;
        for (String str : s.split(",")) {
            try {
                if(!str.isEmpty()) {
                    String[] components = str.split(";");
                    String key = components[0];
                    String value = components[1];
                    Number valNum = getNumberWrapperFromString(value, numClass);
                    if(valNum == null) continue;
                    map.put((T) c.getMethod("valueOf", String.class).invoke(null, key), (E)valNum);
                }
            } catch (Exception e) {
                e.printStackTrace();
                Bukkit.getLogger().warning("Failed to deserialize " + str + " as " + c.getSimpleName() + ".");
            }
        }
        return map;
    }

    private static <E extends Number> Number getNumberWrapperFromString(String numString, Class<E> format) {
	    try {
	        if(format.equals(Integer.class))return new Integer(numString);
            if(format.equals(Long.class))return new Long(numString);
            if(format.equals(Float.class))return new Float(numString);
            if(format.equals(Double.class))return new Double(numString);
        } catch(Exception e) {
	        e.printStackTrace();
        }
        return null;
    }


    @SuppressWarnings("unchecked")
	public static <T extends Enum<T>> List<T> deserializeEnumList(String s, Class<T> c) {
		List<T> list = new ArrayList<>();
		if(s == null)return list;
		for (String str : s.split(",")) {
			try {
				if (!str.equals(""))
					list.add((T) c.getMethod("valueOf", String.class).invoke(null, str));
			} catch (Exception e) {
				e.printStackTrace();
				Bukkit.getLogger().warning("Failed to deserialize " + str + " as " + c.getSimpleName() + ".");
			}
		}
		return list;
	}

    @SuppressWarnings("unchecked")
    public static <T extends Enum<T>> Set<T> deserializeEnumListToSet(String s, Class<T> c) {
        Set<T> list = new HashSet<T>();
        if(s == null)return list;
        for (String str : s.split(",")) {
            try {
                if (!str.equals(""))
                    list.add((T) c.getMethod("valueOf", String.class).invoke(null, str));
            } catch (Exception e) {
                e.printStackTrace();
                Bukkit.getLogger().warning("Failed to deserialize " + str + " as " + c.getSimpleName() + ".");
            }
        }
        return list;
    }

    public static List<String> deserializeList(String string, String delimeter){
        List<String> retr = Lists.newArrayList();
        if(string == null || string.isEmpty())return retr;
        for(String s : string.split(delimeter)){
            if(s == null || s.isEmpty())continue;
            retr.add(s);
        }
        return retr;
    }

    public static String serializeList(Set<String> string, String delimeter, boolean addQuotes){
        if(addQuotes) {
            String str = serializeList(string, delimeter);
            if (str == null || str.isEmpty()) return null;

            return "'" + str + "'";
        }
        return serializeList(string, delimeter);
    }

    public static String serializeList(List<String> string, String delimeter){
        if(string == null || string.isEmpty())return null;
        StringBuilder builder = new StringBuilder();
        for(String s : string)
        	builder.append(s).append(delimeter);

        String retr = builder.toString();
        if (retr.endsWith(delimeter))
            return retr.substring(0, retr.length() - delimeter.length());

        return builder.toString();
    }

    public static String serializeList(Set<String> string, String delimeter){
        if(string == null || string.isEmpty())return null;
        StringBuilder builder = new StringBuilder();
        for(String s : string) builder.append(s).append(delimeter);

        return builder.toString();
    }


    public static HashSet<String> deserializeSet(String string, String delimeter){
        HashSet<String> retr = new HashSet<>();
        if(string == null)return retr;
        for(String s : string.split(delimeter)){
            if(s == null)continue;
            retr.add(s);
        }
        return retr;
    }
}
