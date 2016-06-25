package net.dungeonrealms.game.commands.menualias;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.game.commands.generic.BasicCommand;
import net.dungeonrealms.game.donate.DonationEffects;
import net.dungeonrealms.game.menus.player.Profile;
import net.dungeonrealms.game.mongo.DatabaseAPI;
import net.dungeonrealms.game.mongo.EnumData;
import net.dungeonrealms.game.player.combat.CombatLog;
import net.dungeonrealms.game.player.inventory.PlayerMenus;
import net.dungeonrealms.game.world.entities.utils.EntityAPI;
import net.dungeonrealms.game.world.entities.utils.MountUtils;
import net.minecraft.server.v1_9_R2.Entity;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Created by Kieran Quigley (Proxying) on 29-May-16.
 */
public class CommandMount extends BasicCommand {

    public CommandMount(String command, String usage, String description, List<String> aliases) {
        super(command, usage, description, aliases);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof ConsoleCommandSender) {
            return false;
        }
        Player player = (Player) sender;
        if (args.length == 0) {
            if (EntityAPI.hasMountOut(player.getUniqueId())) {
                Entity entity = EntityAPI.getPlayerMount(player.getUniqueId());
                if (entity.isAlive()) {
                    entity.getBukkitEntity().remove();
                }
                if (DonationEffects.getInstance().ENTITY_PARTICLE_EFFECTS.containsKey(entity)) {
                    DonationEffects.getInstance().ENTITY_PARTICLE_EFFECTS.remove(entity);
                }
                player.sendMessage(ChatColor.AQUA + "Your mount has been dismissed.");
                EntityAPI.removePlayerMountList(player.getUniqueId());
                return true;
            }
            if (CombatLog.isInCombat(player)) {
                player.sendMessage(ChatColor.RED + "You cannot summon a mount while in combat!");
                return true;
            }
            String mountType = (String) DatabaseAPI.getInstance().getData(EnumData.ACTIVE_MOUNT, player.getUniqueId());
            if (mountType == null || mountType.equals("")) {
                player.sendMessage(ChatColor.RED + "You don't have an active mount, please enter the mounts section in your profile to set one.");
                player.closeInventory();
                return true;
            }
            player.sendMessage(ChatColor.GREEN + "Your mount is being summoned into this world!");
            final int[] count = {0};
            Location startingLocation = player.getLocation();
            final boolean[] cancelled = {false};
            int taskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(DungeonRealms.getInstance(), () -> {
                if (!EntityAPI.hasMountOut(player.getUniqueId())) {
                    if (player.getLocation().distanceSquared(startingLocation) <= 4) {
                        if (!CombatLog.isInCombat(player)) {
                            if (!cancelled[0]) {
                                if (count[0] < 3) {
                                    count[0]++;
                                } else {
                                    MountUtils.spawnMount(player.getUniqueId(), mountType, (String) DatabaseAPI.getInstance().getData(EnumData.ACTIVE_MOUNT_SKIN, player.getUniqueId()));
                                }
                            }
                        } else {
                            if (!cancelled[0]) {
                                cancelled[0] = true;
                                count[0] = 0;
                                player.sendMessage(ChatColor.RED + "Combat has cancelled your mount summoning!");
                            }
                        }
                    } else {
                        if (!cancelled[0]) {
                            cancelled[0] = true;
                            count[0] = 0;
                            player.sendMessage(ChatColor.RED + "Movement has cancelled your mount summoning!");
                        }
                    }
                }
            }, 0L, 20L);
            Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> Bukkit.getScheduler().cancelTask(taskID), 65L);
            return true;
        } else if (args.length == 1) {
            if (args[0].equalsIgnoreCase("open") || args[0].equalsIgnoreCase("o") || args[0].equalsIgnoreCase("view") || args[0].equalsIgnoreCase("v")) {
                PlayerMenus.openPlayerMountMenu((Player) sender);
                return true;
            } else if (args[0].equalsIgnoreCase("add") || args[0].equalsIgnoreCase("a") || args[0].equalsIgnoreCase("g") || args[0].equalsIgnoreCase("get")) {
                Profile.addMountItem(player);
                return true;
            } else {
                return true;
            }
        } else {
            return true;
        }
    }
}
