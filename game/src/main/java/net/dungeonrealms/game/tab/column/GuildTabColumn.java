package net.dungeonrealms.game.tab.column;

import codecrafter47.bungeetablistplus.api.bukkit.Variable;
import net.dungeonrealms.game.guild.GuildDatabaseAPI;
import net.dungeonrealms.game.guild.GuildMechanics;
import net.dungeonrealms.game.guild.database.GuildDatabase;
import net.dungeonrealms.game.tab.Column;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Class written by APOLLOSOFTWARE.IO on 8/5/2016
 */
public class GuildTabColumn extends Column {

    @Override
    public Column register() {
        for (int i = 0; i < 18; i++) {
            int cursor = i;
            variablesToRegister.add(new Variable("guild." + cursor) {
                @Override
                public String getReplacement(Player player) {
                    if (GuildDatabaseAPI.get().isGuildNull(player.getUniqueId())) {
                        switch (cursor) {
                            case 0:
                                return ChatColor.GRAY.toString() + "Please visit the Guild";
                            case 1:
                                return ChatColor.GRAY.toString() + "Registrar to start a guild!";

                        }
                        return "";
                    } else {
                        if (!GuildDatabase.getAPI().isGuildCached(GuildDatabaseAPI.get().getGuildOf(player.getUniqueId())))
                            return "";
                    }

                    List<String> guildMembers = new ArrayList<>();

                    GuildMechanics.getInstance().getAllOnlineGuildMembers(GuildDatabaseAPI.get().getGuildOf(player.getUniqueId()))
                            .forEach(uuid -> guildMembers.add(Bukkit.getPlayer(uuid).getName()));

                    if (guildMembers.isEmpty()) if (cursor == 0)
                        return ChatColor.RED + "No friends online";
                    else return "";

                    try {
                        if (guildMembers.get(cursor) == null)
                            return "";
                    } catch (Exception ignored) {
                        return "";
                    }

                    return ChatColor.GREEN + guildMembers.get(cursor);
                }
            });
        }
        return this;
    }
}