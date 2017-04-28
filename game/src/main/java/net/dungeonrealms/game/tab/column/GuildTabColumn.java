package net.dungeonrealms.game.tab.column;

import codecrafter47.bungeetablistplus.api.bukkit.Variable;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.common.Tuple;
import net.dungeonrealms.common.game.database.player.PlayerToken;
import net.dungeonrealms.common.network.ShardInfo;
import net.dungeonrealms.common.network.bungeecord.BungeeServerTracker;
import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.game.guild.GuildMember;
import net.dungeonrealms.game.guild.GuildWrapper;
import net.dungeonrealms.game.guild.database.GuildDatabase;
import net.dungeonrealms.game.tab.Column;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Class written by Rar349 on 4/27/2017
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
                        GuildWrapper hisGuild = GuildDatabase.getAPI().getPlayersGuildWrapper(player.getUniqueId());
                        if (hisGuild == null) {
                            if (cursor == 0) return ChatColor.GRAY.toString() + "Please visit the Guild";
                            if (cursor == 1) return ChatColor.GRAY.toString() + "Registrar to start a guild!";
                            return "";
                        }

                        PlayerWrapper playerWrapper = PlayerWrapper.getPlayerWrapper(player);
                        if(playerWrapper == null) return "ERROR";

                        GuildMember hisMember = hisGuild.getMembers().get(playerWrapper.getAccountID());
                        if(hisMember == null) {
                            return "ERROR 2";
                        }

                        if(!hisMember.isAccepted()) {
                            if (cursor == 0) return ChatColor.GRAY.toString() + "Pending invite from " + ChatColor.DARK_AQUA + hisGuild.getDisplayName();
                            if (cursor == 1) return ChatColor.GRAY.toString() + "/gaccept to join";
                            if(cursor == 2) return ChatColor.GRAY.toString() + "/gdeny to deny";
                            return "";
                        }


                        List<String> lines = new ArrayList<>();

                        for (GuildMember members : hisGuild.getMembers().values()) {
                            if (members == null || !members.isAccepted()) continue;
                            UUID memberID = members.getUUID();
                            if (memberID == null) continue;
                            if (player.getUniqueId().equals(memberID)) continue;
                            Player guildPlayer = Bukkit.getPlayer(memberID);
                            ShardInfo shard = null;
                            if (guildPlayer == null) {
                                Optional<Tuple<PlayerToken, ShardInfo>> curInfo = BungeeServerTracker.grabPlayerInfo(memberID);
                                if (!curInfo.isPresent()) continue;

                                PlayerToken playerInfo = curInfo.get().a();
                                if (playerInfo == null) continue;

                                shard = curInfo.get().b();
                            } else {
                                shard = DungeonRealms.getShard();
                            }

                            String prefix = "";

                            if (members.getRank().equals(GuildMember.GuildRanks.OWNER))
                                prefix += ChatColor.DARK_AQUA + "♛ " + ChatColor.GRAY;
                            else if (members.getRank().equals(GuildMember.GuildRanks.OFFICER))
                                prefix += ChatColor.DARK_AQUA + "* " + ChatColor.GRAY;

                            lines.add(getFormat(prefix + members.getPlayerName(), shard));
                        }

                        if (lines.isEmpty()) {
                            if (cursor == 0) {
                                return ChatColor.RED + "No guild members online!";
                            }
                            else return "";
                        }

                        try {
                            if (lines.get(cursor) == null)
                                return "";
                        } catch (Exception ignored) {
                            return "";
                        }

                        return lines.get(cursor);
                    }
                });
            }
        } catch (NullPointerException ignored) {
            ignored.printStackTrace();
        }
        return this;
    }
}