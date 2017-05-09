package net.dungeonrealms.common.game.util;

import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;

public class StringUtils {

	public static <T extends Enum<T>> String serializeEnumList(List<T> values) {
		String s = "";
		for (T val : values)
			s += val.name() + ",";
		return s;
	}

	@SuppressWarnings("unchecked")
	public static <T extends Enum<T>> List<T> deserializeEnumList(String s, Class<T> c) {
		List<T> list = new ArrayList<>();
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
