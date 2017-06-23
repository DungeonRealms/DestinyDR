package net.dungeonrealms.game.command.moderation;

import com.google.common.collect.Lists;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.Constants;
import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.common.game.database.player.PlayerRank;
import net.dungeonrealms.common.game.database.player.Rank;
import net.dungeonrealms.common.game.database.sql.SQLDatabaseAPI;
import net.dungeonrealms.common.network.bungeecord.BungeeUtils;
import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.database.punishment.PunishType;
import net.dungeonrealms.game.player.inventory.menus.guis.support.CharacterSelectionGUI;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandLock extends BaseCommand {
    public CommandLock() {
        super("lock", "/<command>", "/<command> <player>", "", Lists.newArrayList("wipeplayer", "pwipe"));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if(!(sender instanceof Player)) return true;

            Player player = (Player) sender;
            if (!Rank.isHeadGM(player)) {
                return true;
            }

        if (args.length == 1) {


            SQLDatabaseAPI.getInstance().getUUIDFromName(args[0], false, uuid -> {

                Integer accountID = SQLDatabaseAPI.getInstance().getAccountIdFromUUID(uuid);

                if (accountID == null) {
                    sender.sendMessage(ChatColor.RED.toString() + ChatColor.BOLD + args[0] + ChatColor.RED + " does not exist in our database.");
                    return;
                }
                new CharacterSelectionGUI(player, accountID, (charID) -> {

                    PlayerWrapper.getPlayerWrapper(uuid, charID,false, true, wrapper -> {
                        BungeeUtils.sendNetworkMessage("BungeeCord", "KickPlayer", wrapper.getUsername(), "Your player data is being reset, please join back.");
                        if (wrapper.getUsername() != null) {

                                SQLDatabaseAPI.getInstance().executeUpdate(updates2 -> {
                                    Constants.log.info("Swapping character locked for " + charID);
                                    sender.sendMessage(ChatColor.RED + "You have LOCKED " + wrapper.getUsername() + "'s character");
                                }, "UPDATE characters SET isLocked = !isLocked WHERE character_id = '" + charID + "';");
                            wrapper.loadAllPunishments(true, punishments -> {
                                GameAPI.sendStaffMessage(PlayerRank.GM, sender.getName() + " has WIPED " + wrapper.getUsername() + "'s character data from the database. A-ID: (" + wrapper.getAccountID() + ") C-ID: (" + wrapper.getCharacterID() + ") " + punishments.getPunishCount(PunishType.MUTE) + " Mutes, " + punishments.getPunishCount(PunishType.BAN) + " Bans");
                            });
                        }
                    });

                });
            });
            return true;
        }

        sender.sendMessage(ChatColor.RED + "/lock <player> - Lock a players character!");
        return false;
    }
}
