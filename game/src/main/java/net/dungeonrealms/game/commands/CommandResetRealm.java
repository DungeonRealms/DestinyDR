package net.dungeonrealms.game.commands;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.common.game.commands.BasicCommand;
import net.dungeonrealms.common.game.database.DatabaseAPI;
import net.dungeonrealms.common.game.database.data.EnumData;
import net.dungeonrealms.common.game.database.player.rank.Rank;
import net.dungeonrealms.game.player.chat.Chat;
import net.dungeonrealms.game.world.realms.Realms;
import net.dungeonrealms.game.world.realms.instance.obj.RealmStatus;
import net.dungeonrealms.game.world.realms.instance.obj.RealmToken;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.IOException;

/**
 * Class written by APOLLOSOFTWARE.IO on 6/23/2016
 */

public class CommandResetRealm extends BasicCommand {

    public CommandResetRealm(String command, String usage, String description) {
        super(command, usage, description);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) return false;

        Player player = (Player) sender;

        long lastReset = (long) DatabaseAPI.getInstance().getData(EnumData.REALM_LAST_RESET, player.getUniqueId());

        if (lastReset != 0 && !Rank.isGM(player)) {
            player.sendMessage(ChatColor.RED + "You may only reset your realm " + ChatColor.UNDERLINE + "ONCE" + ChatColor.RED + " per hour.");
            return true;
        }


        if (Realms.getInstance().isRealmCached(player.getUniqueId())) {
            RealmToken realm = Realms.getInstance().getRealm(player.getUniqueId());

            if (realm.getStatus() != RealmStatus.OPENED && realm.getStatus() != RealmStatus.CLOSED) {
                player.sendMessage(Realms.getInstance().getRealmStatusMessage(realm.getStatus()));
                return true;
            }
        }

        player.playSound(player.getLocation(), Sound.ENTITY_WITHER_SPAWN, 1, 1);
        player.sendMessage(ChatColor.GRAY + "Are you sure you want to RESET your realm  - This cannot be undone. " + "(" + ChatColor.GREEN.toString() + ChatColor.BOLD + "Y" + ChatColor.GRAY + " / " + ChatColor.RED.toString() + ChatColor.BOLD + "N" + ChatColor.GRAY + ")");
        player.sendMessage(ChatColor.RED.toString() + ChatColor.BOLD.toString() + "DISCLAIMER: " + ChatColor.GRAY + "You will not receive " + ChatColor.UNDERLINE + "ANY" + ChatColor.GRAY + " of your resources in your realm back. Your realm will be " + ChatColor.BOLD + "PERMANENTLY DELETED" + ChatColor.GRAY + ". Your realm upgrades will also be removed.");

        Chat.listenForMessage(player, confirmation -> {
            if (!confirmation.getMessage().equalsIgnoreCase("y") || confirmation.getMessage().equalsIgnoreCase("n") || confirmation.getMessage().equalsIgnoreCase("cancel")) {
                player.sendMessage(ChatColor.RED + "/resetrealm - " + ChatColor.BOLD + "CANCELLED");
                return;
            }

            // Run sync cuz listen for chat is async -_-
            Bukkit.getScheduler().runTask(DungeonRealms.getInstance(),
                    () -> Realms.getInstance().loadRealm(player, () -> {
                                player.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "Resetting your realm ...");

                                try {
                                    Realms.getInstance().resetRealm(player);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    player.sendMessage(ChatColor.RED + "We failed to reset your realm!");
                                }

                            }
                    ));
        }, null);

        return true;
    }
}
