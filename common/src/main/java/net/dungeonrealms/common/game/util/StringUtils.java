package net.dungeonrealms.common.game.util;

import com.google.common.collect.Lists;

import java.util.List;

public class StringUtils {

    public static List<String> deserializeList(String string, String delimeter){
        List<String> retr = Lists.newArrayList();
        if(string == null)return retr;
        for(String s : string.split(delimeter)){
            if(s == null)continue;
            retr.add(s);
        }
        return retr;
    }
}
