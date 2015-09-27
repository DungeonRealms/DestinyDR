package net.dungeonrealms.chat;

import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Nick on 9/26/2015.
 */
public class Chat {

    static Chat instance = null;

    public static Chat getInstance() {
        if (instance == null) {
            instance = new Chat();
        }
        return instance;
    }

    List<String> TERRIBLE_WORDS = Arrays.asList("shit", "nigger", "wynncraft", "dungeonrealms.us", "myspace.com");


    /**
     * Monitor the players primary language also check for bad words.
     *
     * @param event
     * @since 1.0
     */
    public void monitorLanguage(AsyncPlayerChatEvent event) {
    }

}
