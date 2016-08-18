package net.dungeonrealms.game.tab.column;

import codecrafter47.bungeetablistplus.api.bukkit.Variable;
import net.dungeonrealms.common.Tuple;
import net.dungeonrealms.common.network.ShardInfo;
import net.dungeonrealms.common.network.bungeecord.BungeeServerTracker;
import net.dungeonrealms.common.network.ping.PingResponse;
import net.dungeonrealms.game.guild.GuildDatabaseAPI;
import net.dungeonrealms.game.guild.database.GuildDatabase;
import net.dungeonrealms.game.tab.Column;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

                    String guild = GuildDatabaseAPI.get().getGuildOf(player.getUniqueId());

                    GuildDatabaseAPI.get().getAllOfGuild(guild).stream()
                            .filter(uuid -> !player.getUniqueId().equals(uuid))
                            .forEach(uuid -> {
                                String prefix = "";

                                if (GuildDatabaseAPI.get().isOwner(uuid, guild))
                                    prefix += ChatColor.GRAY + "[" + ChatColor.DARK_AQUA + "Owner" + ChatColor.GRAY + "]";
                                else if (GuildDatabaseAPI.get().isOfficer(uuid, guild))
                                    prefix += ChatColor.GRAY + "[" + ChatColor.DARK_AQUA + "Officer" + ChatColor.GRAY + "]";


                                Optional<Tuple<PingResponse.PlayerInfo, ShardInfo>> curInfo = BungeeServerTracker.grabPlayerInfo(uuid);

                                if (curInfo.isPresent())
                                    guildMembers.add(ChatColor.GOLD + curInfo.get().b().getShardID() + " " + prefix + ChatColor.GREEN + curInfo.get().a().getName());
                            });

                    if (guildMembers.isEmpty()) if (cursor == 0)
                        return ChatColor.RED + "No guild members on this shard";
                    else return "";

                    try {
                        if (guildMembers.get(cursor) == null)
                            return "";
                    } catch (Exception ignored) {
                        return "";
                    }

                    return guildMembers.get(cursor);
                }
            });
        }
        return this;
    }
}