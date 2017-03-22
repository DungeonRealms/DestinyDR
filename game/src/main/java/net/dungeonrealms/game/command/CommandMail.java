package net.dungeonrealms.game.command;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.common.game.database.DatabaseAPI;
import net.dungeonrealms.game.handler.MailHandler;
import net.dungeonrealms.game.player.inventory.PlayerMenus;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * Created by Nick on 10/15/2015.
 */
public class CommandMail extends BaseCommand {

    public CommandMail(String command, String usage, String description) {
        super(command, usage, description);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String string, String[] args) {
        if (!(sender instanceof Player)) return false;
        Player player = (Player) sender;

        if (args.length == 2 && args[0].equals("send") && (DungeonRealms.getInstance().isMasterShard || DungeonRealms.getInstance().isSupportShard)) {
            if (player.getEquipment().getItemInMainHand() != null && player.getEquipment().getItemInMainHand().getType() != Material.AIR) {
                UUID toUUID;

                // Check that the user we're attempting to send it to is an actual Dungeon Realms player.
                String result = DatabaseAPI.getInstance().getUUIDFromName(args[1]);
                if (result.equals("")) {
                    // Yikes! They're not a player, prompt the user and return false.
                    player.sendMessage(ChatColor.RED + "This player does not exist.");
                    return false;
                } else {
                    // Success! The user is a valid player.
                    toUUID = UUID.fromString(result);
                }

                if (MailHandler.getInstance().sendMailRaw("The Dungeon Realms Team", toUUID, player.getEquipment().getItemInMainHand())) {
                    player.getEquipment().setItemInMainHand(null);
                }
            }
            return true;
        }

        // Open the player's mailbox inventory.
        PlayerMenus.openMailInventory(player);
        return true;
    }
}