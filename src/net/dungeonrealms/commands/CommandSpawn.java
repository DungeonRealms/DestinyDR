package net.dungeonrealms.commands;

import net.dungeonrealms.mastery.NBTUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.potion.PotionEffectType;

/**
 * Created by Nick on 9/17/2015.
 */
public class CommandSpawn implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender s, Command cmd, String string, String[] args) {
        if (s instanceof ConsoleCommandSender) return false;
        Player player = (Player) s;
        if (args.length > 0) {
            switch (args[0]) {
                case "wolf":
                    Wolf w = (Wolf) Bukkit.getWorld(player.getWorld().getName()).spawnEntity(player.getLocation(), EntityType.WOLF);
                    NBTUtils.nullifyAI(w);
                    break;
                case "buff":
                    EnderCrystal ec = (EnderCrystal) Bukkit.getWorld(player.getWorld().getName()).spawnEntity(player.getLocation(), EntityType.ENDER_CRYSTAL);
                    NBTUtils.buffEntity(ec, PotionEffectType.REGENERATION, 6, 20 * 10);
                    break;
                default:
            }
        }
        return true;
    }
}
