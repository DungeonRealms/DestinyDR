package net.dungeonrealms.game.quests.compass;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.ChatColor;

/**
 * Created by Rar349 on 8/8/2017.
 */
public class CompassNode {

    private final String character;
    @Getter
    @Setter
    private ChatColor color = ChatColor.WHITE;
    @Getter
    @Setter
    private boolean isBold, isUnderlined;


    public CompassNode(String character) {
        this.character = character;
    }

    @Override
    public String toString() {
        return color.toString() + (isBold() ? ChatColor.BOLD.toString() : "") + (isUnderlined() ? ChatColor.UNDERLINE.toString() : "") + character;
    }
}
