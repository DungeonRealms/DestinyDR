package net.dungeonrealms.game.command.moderation;

import com.google.common.collect.Lists;

import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.Constants;
import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.common.game.database.player.rank.Rank;
import net.dungeonrealms.common.game.database.sql.SQLDatabaseAPI;
import net.dungeonrealms.common.network.bungeecord.BungeeUtils;
import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.database.punishment.PunishAPI;
import net.dungeonrealms.database.punishment.PunishType;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;

public class CommandWipe extends BaseCommand {
    public CommandWipe() {
        super("wipe", "/<command>", "/<command> <player>", "", Lists.newArrayList("wipeplayer", "pwipe"));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (!Rank.isHeadGM(player)) {
                return true;
            }
        }
        if (args.length == 1) {

            SQLDatabaseAPI.getInstance().getUUIDFromName(args[0], false, uuid -> {

                PlayerWrapper.getPlayerWrapper(uuid, false, true, wrapper -> {
                    BungeeUtils.sendNetworkMessage("BungeeCord", "KickPlayer", wrapper.getUsername(), "Your player data is being reset, please join back.");
//                    PunishAPI.kick(wrapper.getUsername(), "Your player data is being reset, please join back.", null);
                    GameAPI.sendNetworkMessage("WipePlayer", String.valueOf(wrapper.getAccountID()), uuid.toString());
                    if (wrapper.getUsername() != null){
                        SQLDatabaseAPI.getInstance().executeUpdate(updates -> {
                            Constants.log.info("Deleted selected_character_id for " + wrapper.getAccountID());
                            sender.sendMessage(ChatColor.RED + "You have WIPED " + wrapper.getUsername() + "'s user and character data.");
                        }, "UPDATE users SET selected_character_id = 0, ecash = 0, last_free_ecash = 0, currentShard = null, is_online = 0, currencyTab = " + (wrapper.getCurrencyTab() != null && wrapper.getCurrencyTab().hasAccess ? "'0:0:0:0:0'" : "null") + ", mounts = null WHERE account_id = '" + wrapper.getAccountID() + "';");
                        wrapper.loadAllPunishments(true, punishments -> {
                            GameAPI.sendNetworkMessage("GMMessage", sender.getName() + " has WIPED " + wrapper.getUsername() + "'s character data from the database. A-ID: (" + wrapper.getAccountID() + ") C-ID: (" + wrapper.getCharacterID() + ") " + punishments.getPunishCount(PunishType.MUTE) + " Mutes, " + punishments.getPunishCount(PunishType.BAN) + " Bans");
                        });
                    }
                });

            });
            return true;
        }

        sender.sendMessage(ChatColor.RED + "/wipe <player> - Wipe all player information from the database (Does not include punishments)");
        return false;
    }
}
