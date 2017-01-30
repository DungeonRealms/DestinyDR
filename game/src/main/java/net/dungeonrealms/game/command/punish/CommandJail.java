package net.dungeonrealms.game.command.punish;

import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.common.game.database.player.rank.Rank;
import net.dungeonrealms.game.mastery.GamePlayer;
import net.dungeonrealms.game.world.entity.EntityMechanics;
import net.dungeonrealms.game.world.entity.util.EntityAPI;
import net.dungeonrealms.game.world.realms.Realms;
import net.minecraft.server.v1_9_R2.Entity;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Class written by APOLLOSOFTWARE.IO on 7/25/2016
 */
public class CommandJail extends BaseCommand {


    public CommandJail(String command, String usage, String description) {
        super(command, usage, description);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (!Rank.isTrialGM(player)) return false;
        }

        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Invalid usage! /jail <player>");
            return true;
        }

        Player player = Bukkit.getPlayer(args[0]);

        if (player == null) {
            sender.sendMessage(ChatColor.RED + args[0] + " isn't online.");
            return true;
        }

        GamePlayer gamePlayer = GameAPI.getGamePlayer(player);

        if (gamePlayer == null) {
            sender.sendMessage(ChatColor.RED + args[0] + " hasn't played Dungeon Realms before.");
            return true;
        }


        if (gamePlayer.isJailed()) {
            sender.sendMessage(ChatColor.RED + args[0] + " is already jailed.");
            return true;
        }

        sender.sendMessage(ChatColor.GREEN + "You have jailed " + args[0] + ".");
        player.sendMessage(ChatColor.RED + "You have been jailed.");
        player.teleport(new Location(Bukkit.getWorlds().get(0), -225, 81, 403));
        player.playSound(player.getLocation(), Sound.ENTITY_WITHER_SPAWN, 1, 1);

        if (EntityAPI.hasPetOut(player.getUniqueId())) {
            Entity pet = EntityMechanics.PLAYER_PETS.get(player.getUniqueId());
            pet.dead = true;
            EntityAPI.removePlayerPetList(player.getUniqueId());
        }
        if (EntityAPI.hasMountOut(player.getUniqueId())) {
            Entity mount = EntityMechanics.PLAYER_MOUNTS.get(player.getUniqueId());
            mount.dead = true;
            EntityAPI.removePlayerMountList(player.getUniqueId());
        }

        if (Realms.getInstance().isRealmPortalOpen(player.getUniqueId()))
            Realms.getInstance().closeRealmPortal(player.getUniqueId(), true, ChatColor.RED + "The owner of this realm has LOGGED OUT.");


        gamePlayer.setJailed(true);
        return false;
    }
}
