package net.dungeonrealms.old.game.tab;

import codecrafter47.bungeetablistplus.api.bukkit.Variable;
import lombok.Getter;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.common.network.ShardInfo;
import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.List;

/**
 * Class written by APOLLOSOFTWARE.IO on 8/4/2016
 */

public abstract class Column {

    @Getter
    protected List<Variable> variablesToRegister = new ArrayList<>();


    /**
     * Create all variables associated with this colman
     *
     * @return Column instance
     */
    public abstract Column register();


    protected static String getFormat(String displayName, ShardInfo shard) {
        if (DungeonRealms.getShard().equals(shard)) {
            // THIS WILL INDICATE THAT PLAYER IS IN CURRENT SHARD //
            return ChatColor.GREEN + " ⦿ " + ChatColor.GRAY + displayName;
        } else {
            return ChatColor.GOLD + "(" + shard.getShardID() + ") " + ChatColor.GRAY + displayName;
        }

    }

}
