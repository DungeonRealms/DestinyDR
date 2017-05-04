package net.dungeonrealms.common.game.util;

import com.google.common.collect.Lists;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class StringUtils {

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
