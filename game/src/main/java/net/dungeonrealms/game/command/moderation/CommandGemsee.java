package net.dungeonrealms.game.command.moderation;

import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.common.game.database.player.Rank;
import net.dungeonrealms.common.game.database.sql.SQLDatabaseAPI;
import net.dungeonrealms.database.PlayerWrapper;

import net.dungeonrealms.game.player.inventory.menus.guis.support.CharacterSelectionGUI;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;

/**
 * Created by Brad on 25/12/2016.
 */
public class CommandGemsee extends BaseCommand {
    public CommandGemsee() {
        super("gemsee", "/<command> <player>", "View a player's gems.", Collections.singletonList("mgs"));
    }

    @Override
    public boolean onCommand(CommandSender s, Command cmd, String string, String[] args) {
        if (s instanceof ConsoleCommandSender) return false;
        Player sender = (Player) s;

        if (!Rank.isGM(sender)) return false;

        if (args.length == 0) {
            s.sendMessage(usage);
            return true;
        }

        String playerName = args[0];

        SQLDatabaseAPI.getInstance().getUUIDFromName(playerName, false, (uuid) -> {
            if(uuid == null) {
                sender.sendMessage(ChatColor.RED + "This player has never logged into Dungeon Realms");
                return;
            }

            Integer accountID = SQLDatabaseAPI.getInstance().getAccountIdFromUUID(uuid);
            if(accountID == null) {
                sender.sendMessage(ChatColor.RED + "This player has never logged in with Dungeon Realms");
                return;
            }

            new CharacterSelectionGUI(sender, accountID, (charID) -> {

                PlayerWrapper.getPlayerWrapper(uuid, charID,false, false, (wrapper) -> {
                    if (wrapper == null) {
                        sender.sendMessage(ChatColor.RED + "Something went wrong");
                        return;
                    }

                    sender.sendMessage(ChatColor.YELLOW + playerName + " balance: " + ChatColor.AQUA + wrapper.getGems());
                });
            }).open(sender,null);
        });
        return false;
    }
}
