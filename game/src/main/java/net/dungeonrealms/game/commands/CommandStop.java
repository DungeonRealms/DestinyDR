package net.dungeonrealms.game.commands;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.game.database.DatabaseDriver;
import net.dungeonrealms.game.database.player.Rank;
import net.dungeonrealms.game.mastery.AsyncUtils;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.player.combat.CombatLog;
import net.dungeonrealms.game.player.combat.CombatLogger;
import net.dungeonrealms.game.world.realms.Realms;
import net.dungeonrealms.game.world.shops.ShopMechanics;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Created by Chase on Nov 6, 2015
 */
public class CommandStop extends BasicCommand {
    public CommandStop(String command, String usage, String description, List<String> aliases) {
        super(command, usage, description, aliases);
    }

    @Override
    public boolean onCommand(CommandSender s, Command cmd, String string, String[] args) {
        if (s instanceof Player) {
            Player player = (Player) s;
            if (!Rank.isDev(player)) {
                return false;
            }
        }

        if (Realms.getInstance().realmsAreUpgrading()) {
            s.sendMessage(ChatColor.RED + "Realms are still being upgraded!");
            return true;
        }

        boolean stoppingAll = false;
        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("all")) {
                stoppingAll = true;
            }
        }

        DungeonRealms.getInstance().getLogger().info("DRStop called.");
        Bukkit.getServer().setWhitelist(true);
        DungeonRealms.getInstance().setFinishedSetup(false);
        DungeonRealms.getInstance().saveConfig();
        CombatLog.getInstance().getCOMBAT_LOGGERS().values().forEach(CombatLogger::handleTimeOut);
        Bukkit.getScheduler().cancelAllTasks();
        GameAPI.logoutAllPlayers(true, stoppingAll);
        ShopMechanics.deleteAllShops(true);
        DungeonRealms.getInstance().mm.stopInvocation();
        AsyncUtils.pool.shutdown();

        Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
            DungeonRealms.getInstance().mm.stopInvocation();
            Utils.log.info("DungeonRealms onDisable() ... SHUTTING DOWN in 5s");
            DatabaseDriver.mongoClient.close();
        }, 200);
        Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), Bukkit::shutdown, 15 * 20L);
        return false;
    }
}
