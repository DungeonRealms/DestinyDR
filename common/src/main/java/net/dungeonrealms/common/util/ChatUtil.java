package net.dungeonrealms.common.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

public class ChatUtil {

    public static List<String> bannedWords = new ArrayList<>(Arrays.asList("shit", "fuck", "cunt", "bitch", "whore",
            "slut", "wank", "asshole", "cock",
            "dick", "clit", "homo", "fag", "faggot", "queer", "nigger", "n1gger", "n1gg3r", "nigga", "dike", "dyke", "retard", " " +
                    "motherfucker", "vagina", "boob", "pussy", "rape", "gay", "penis",
            "cunt", "titty", "anus", " faggot", "blowjob", "handjob", "bast", "minecade", "@ss", "mystic " +
                    "runes", "mysticrunes", "f@g", "d1ck", "titanrift", "wynncraft", "titan rift", "jigga", "atherialrunes", "atherial",
            "autism", "autismrealms", "jiggaboo", "hitler", "jews", "titanrift", "fucked", "mckillzone",
            "MysticRunes.net", "play.wynncraft.com", "mineca.de", "play.atherialrunes.net", "autismrealms.us", "play.mckillzone.net", "niger", "kys"));

    private static String toCensor(int characters) {
        String result = "";
        for (int i = 0; i < characters; i++)
            result = result.concat("*");
        return result;
    }


    public static String checkForBannedWords(String msg) {
        String result = msg;
        result = result.replace("ð", "");


        for (String word : bannedWords) result = replaceOperation(result, word);

        StringTokenizer st = new StringTokenizer(result);
        String string = "";

        while (st.hasMoreTokens()) {
            String token = st.nextToken();

            for (String word : bannedWords)
                if (token.contains(word)) {
                    List<Integer> positions = new ArrayList<>();

                    for (int i = 0; i < token.length(); i++)
                        if (Character.isUpperCase(token.charAt(i))) positions.add(i);

                    if (token.toLowerCase().contains(word.toLowerCase())) {
                        token = token.toLowerCase().replaceAll(word.toLowerCase(), " " + toCensor(word.length()));
                    }

                    for (int i : positions)
                        if (i < token.length()) Character.toUpperCase(token.charAt(i));
                }
            string += token + " ";
        }
        return string.trim();
    }

    public static boolean containsBannedWords(String msg) {
        String result = msg;
        result = result.replace("ð", "");

        StringTokenizer st = new StringTokenizer(result);

        while (st.hasMoreTokens()) {
            String token = st.nextToken();

            for (String word : bannedWords)
                if (token.toLowerCase().contains(word.toLowerCase())) {
                    return true;
                }
        }
        return false;
    }


    private static String replaceOperation(String source, String search) {
        int length = search.length();
        if (length < 2) return source;

        // - Ignore the same character mutliple times in a row
        // - Ignore any non-alphabetic characters
        // - Ignore any digits and whitespaces between characters
        StringBuilder sb = new StringBuilder(4 * length - 3);
        for (int i = 0; i < length - 1; i++) {
            sb.append("([\\W\\d]*").append(Pattern.quote("" + search.charAt(i))).append(")+");
        }
        sb.append("([\\W\\d\\s]*)+");
        sb.append(search.charAt(length - 1));

        String temp = source.replaceAll("(?i)" + sb.toString(), search).trim();
        int wordCount = temp.split("\\s").length;

        String replace = source;

        if (wordCount <= 2) {
            replace = " " + source;
        }

        return replace.replaceAll("(?i)" + sb.toString(), " " + search).trim();
    }

    public static boolean containsIllegal(String s) {
        //return s.matches("\\p{L}+") || s.matches("\\w+");
        //Probably have an array of allowed characters aswell.
        return !s.replace(" ", "").matches("[\\w\\Q!\"#$%&'()*çáéíóúâêôãõàüñ¿¡+,-./:;<=>?@[\\]^_`{|}~\\E]+");
    }
}
