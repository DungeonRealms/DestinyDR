package net.dungeonrealms.game.tab.column;

import codecrafter47.bungeetablistplus.api.bukkit.Variable;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.common.Tuple;
import net.dungeonrealms.common.game.database.player.PlayerToken;
import net.dungeonrealms.common.network.ShardInfo;
import net.dungeonrealms.common.network.bungeecord.BungeeServerTracker;
import net.dungeonrealms.game.guild.database.GuildDatabase;
import net.dungeonrealms.game.tab.Column;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Class written by APOLLOSOFTWARE.IO on 8/5/2016
 */
public class GuildTabColumn extends Column {

    @Override
    public Column register() {
        try {
            for (int i = 0; i < 18; i++) {
                int cursor = i;
                variablesToRegister.add(new Variable("guild." + cursor) {
                    @Override
                    public String getReplacement(Player player) {
//                        if (GuildDatabaseAPI.get().isGuildNull(player.getUniqueId())) {
//                            switch (cursor) {
//                                case 0:
//                                    return ChatColor.GRAY.toString() + "Please visit the Guild";
//                                case 1:
//                                    return ChatColor.GRAY.toString() + "Registrar to start a guild!";
//
//                            }
//                            return "";
//                        } else {
//                            if (!GuildDatabase.getAPI().isGuildCached(GuildDatabaseAPI.get().getGuildOf(player.getUniqueId())))
//                                return "";
//                        }

//                        List<String> guildMembers = new CopyOnWriteArrayList<>();
//
//                        String guild = GuildDatabaseAPI.get().getGuildOf(player.getUniqueId());
//
//                        new ArrayList<>(GuildDatabaseAPI.get().getAllOfGuild(guild)).stream()
//                                .filter(uuid -> !player.getUniqueId().equals(uuid))
//                                .forEach(uuid -> {
//                                    String playerName = null;
//                                    ShardInfo shard = null;
//
//                                    if (Bukkit.getPlayer(uuid) == null) {
//                                        Optional<Tuple<PlayerToken, ShardInfo>> curInfo = BungeeServerTracker.grabPlayerInfo(uuid);
//                                        if (!curInfo.isPresent()) return;
//
//                                        PlayerToken playerInfo = curInfo.get().a();
//                                        if (playerInfo == null) return;
//
//                                        playerName = playerInfo.getName();
//                                        shard = curInfo.get().b();
//
//                                    } else {
//                                        shard = DungeonRealms.getShard();
//                                        playerName = Bukkit.getPlayer(uuid).getName();
//                                    }
//
//                                    String prefix = "";
//
//                                    if (GuildDatabaseAPI.get().isOwner(uuid, guild))
//                                        prefix += ChatColor.DARK_AQUA + "♛ " + ChatColor.GRAY;
//                                    else if (GuildDatabaseAPI.get().isOfficer(uuid, guild))
//                                        prefix += ChatColor.DARK_AQUA + "* " + ChatColor.GRAY;
//
//                                    guildMembers.add(getFormat(prefix + playerName, shard));
//                                });
//
//                        if (guildMembers.isEmpty()) if (cursor == 0)
//                            return ChatColor.RED + "No guild members online!";
//                        else return "";
//
//                        try {
//                            if (guildMembers.get(cursor) == null)
//                                return "";
//                        } catch (Exception ignored) {
//                            return "";
//                        }
//
//                        return guildMembers.get(cursor);
                        return "BRANDON PLS FIX!!!";
                    }
                });
            }
        } catch (NullPointerException ignored) {

        }
        return this;
    }
}