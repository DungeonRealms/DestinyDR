package net.dungeonrealms.game.command.guild;

import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.Constants;
import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.common.game.database.sql.QueryType;
import net.dungeonrealms.common.game.database.sql.SQLDatabaseAPI;
import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.game.guild.GuildMember;
import net.dungeonrealms.game.guild.GuildWrapper;
import net.dungeonrealms.game.guild.database.GuildDatabase;
import net.dungeonrealms.game.player.chat.Chat;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Class written by Rar349 on 4/27/2017
 */


public class CommandGQuit extends BaseCommand {

    public CommandGQuit(String command, String usage, String description, List<String> aliases) {
        super(command, usage, description, aliases);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) return false;

        Player player = (Player) sender;

        PlayerWrapper playerWrapper = PlayerWrapper.getPlayerWrapper(player);
        if (playerWrapper == null) {
            Constants.log.info("Could not load player wrapper for the player " + playerWrapper.getPlayerName() + " ");
            return true;
        }

        GuildWrapper wrapper = GuildDatabase.getAPI().getPlayersGuildWrapper(player.getUniqueId());
        if (wrapper == null) {
            player.sendMessage(ChatColor.RED + "You must be in a guild to use this command!");
            return true;
        }

        GuildMember member = wrapper.getMembers().get(playerWrapper.getAccountID());
        if (member == null) {
            player.sendMessage(ChatColor.RED + "An internal server error occurred.");
            return true;
        }

        boolean isGuildOwner = member.getRank().equals(GuildMember.GuildRanks.OWNER);

        if (isGuildOwner) {
            player.sendMessage("");
            player.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + ChatColor.UNDERLINE.toString() + "WARNING" + ChatColor.RESET + ChatColor.RED.toString() + " If you continue your guild will be disbanded! This is not reversible!");
            player.sendMessage("");
        }

        player.sendMessage("");
        player.sendMessage(ChatColor.GRAY + "Please type " + ChatColor.GREEN + ChatColor.BOLD + "CONFIRM" + ChatColor.GRAY + " to disband your guild!");
        player.sendMessage("");

        Chat.listenForMessage(player, event -> {
            event.setCancelled(true);
            if (event.getMessage().equalsIgnoreCase("confirm")) {
                if(isGuildOwner) {
                    SQLDatabaseAPI.getInstance().executeUpdate((rows) -> {
                        if(rows == null || rows == 0) {
                            player.sendMessage(ChatColor.RED + "Something went wrong when trying to disband your guild");
                            return;
                        }

                        playerWrapper.setGuildID(0);
                        wrapper.sendGuildMessage(ChatColor.RED + "Your guild has been disbanded!", true);
                        GuildDatabase.getAPI().cached_guilds.remove(wrapper.getGuildID());
                        for(GuildMember memberWrap : wrapper.getMembers().values()) {
                            if(memberWrap == null) continue;
                            PlayerWrapper memberWrapper = PlayerWrapper.getPlayerWrapper(memberWrap.getUUID());
                            if(memberWrapper != null) {
                                memberWrapper.setGuildID(0);
                            }
                        }
                        GameAPI.sendNetworkMessage("Guilds", "disband", String.valueOf(wrapper.getGuildID()));
                    }, QueryType.DELETE_GUILD.getQuery(wrapper.getGuildID()), true);
                } else {
                    SQLDatabaseAPI.getInstance().executeUpdate((rows) -> {
                        if(rows == null || rows == 0) {
                            player.sendMessage(ChatColor.RED + "Something went wrong when trying to disband your guild");
                            return;
                        }
                        playerWrapper.setGuildID(0);
                        player.sendMessage(ChatColor.RED + "You have left your guild!");
                        wrapper.removePlayer(playerWrapper.getAccountID());
                        wrapper.sendGuildMessage(ChatColor.DARK_AQUA + player.getName() + ChatColor.GRAY + " has left your guild!");
                    }, QueryType.DELETE_GUILD_MEMBER.getQuery(playerWrapper.getAccountID()), true);
                }
            } else {
                player.sendMessage(ChatColor.RED + "Guild disband cancelled.");
            }
        }, null);
        return true;
    }
}
