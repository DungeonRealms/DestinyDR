package net.dungeonrealms.common.util;

import lombok.Getter;
import net.dungeonrealms.common.game.database.player.PlayerRank;
import net.dungeonrealms.common.game.database.player.Rank;
import org.bukkit.entity.Player;

/**
 * Created by Rar349 on 6/21/2017.
 */
@Getter
public enum CharacterType {

    DEFAULT("DEFAULT", "Default",1, PlayerRank.DEFAULT),
    SUB("SUB", "Sub",1, PlayerRank.SUB),
    SUB_PLUS("SUB_PLUS", "Sub+",1, PlayerRank.SUB_PLUS),
    SUB_PLUS_PLUS("SUB_PLUS_PLUS", "Sub++",0, PlayerRank.SUB_PLUS_PLUS),
    GM("GM","Game Master",1, PlayerRank.GM),
    PURCHASED("PURCHASED", "Purchased",0, PlayerRank.DEFAULT);

    String internalName;
    String displayName;
    int defaultSlots;
    PlayerRank rank;
    CharacterType(String internalName, String displayName, int defaultSlots, PlayerRank rank) {
        this.internalName = internalName;
        this.displayName = displayName;
        this.defaultSlots = defaultSlots;
        this.rank = rank;
    }

    public static CharacterType getCharacterType(String internal) {
        for(CharacterType type : CharacterType.values()) {
            if(type.getInternalName().equalsIgnoreCase(internal)) return type;
        }

        return null;
    }

    public static int getDefaultSlots(Player player) {
        int toReturn = 0;
        PlayerRank hisRank = Rank.getPlayerRank(player.getUniqueId());
        for(CharacterType type : CharacterType.values()) {
            if(!hisRank.isAtLeast(type.getRank())) continue;
            toReturn += type.getDefaultSlots();
        }

        return toReturn;
    }
}
