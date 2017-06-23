package net.dungeonrealms.game.mechanic.dot;

import lombok.Getter;
import net.dungeonrealms.common.game.util.ChatColor;

/**
 * Created by Rar349 on 6/6/2017.
 */
@Getter
public enum DotType {

    FIRE(false, ChatColor.RED.toString(), ChatColor.GOLD + "♨"),
    HEALING(true, ChatColor.DARK_PURPLE.toString(), ChatColor.YELLOW + "☕"),
    POISON(false, ChatColor.DARK_GREEN.toString(), ChatColor.DARK_GREEN + "☠");

    private boolean isHeal;
    private String prefix;
    private String suffix;
    DotType(boolean isHeal, String prefix, String suffix) {
        this.isHeal = isHeal;
        this.prefix = prefix;
        this.suffix = suffix;
    }

    public char getSign() {
        return isHeal() ? '+' : '-';
    }
}
