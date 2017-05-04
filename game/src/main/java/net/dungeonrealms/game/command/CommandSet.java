/**
 *
 */
package net.dungeonrealms.game.command;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.common.game.database.DatabaseAPI;
import net.dungeonrealms.common.game.database.data.EnumData;
import net.dungeonrealms.common.game.database.data.EnumOperators;
import net.dungeonrealms.common.game.database.player.rank.Rank;
import net.dungeonrealms.game.achievements.Achievements;
import net.dungeonrealms.game.handler.HealthHandler;
import net.dungeonrealms.game.handler.KarmaHandler;
import net.dungeonrealms.game.item.PersistentItem;
import net.dungeonrealms.game.item.items.core.ItemGear;
import net.dungeonrealms.game.item.items.core.ProfessionItem;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.mechanic.data.ShardTier;
import net.dungeonrealms.game.player.combat.CombatLog;
import net.dungeonrealms.game.world.spawning.SpawningMechanics;
import net.minecraft.server.v1_9_R2.NBTTagCompound;
import net.minecraft.server.v1_9_R2.NBTTagString;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_9_R2.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Chase on Sep 22, 2015
 */
public class CommandSet extends BaseCommand {

    public CommandSet() {
        super("set", "/<command> [args]", "Development command for modifying account variables.");
    }

    @Override
    public boolean onCommand(CommandSender s, Command cmd, String string, String[] args) {
        if (!(s instanceof Player))
            return false;

        Player player = (Player) s;
        if (!Rank.isGM(player))
            return false;

        if (args.length == 0)
            return false;

        // Extended Permission Check
        String[] ignoreExtendedPermissions = new String[] { "health", "hp" };
        if (!Rank.isHeadGM(player) && !Arrays.asList(ignoreExtendedPermissions).contains(args[0])
                && !DungeonRealms.getInstance().isGMExtendedPermissions) {
            player.sendMessage(ChatColor.RED + "You don't have permission to execute this command.");
            return false;
        }

        switch (args[0]) {
            case "level":
                if (args.length < 3) {
                    player.sendMessage(ChatColor.RED + "Invalid usage! /set level <name> <level>");
                    break;
                }
                String playerName = args[1];
                Player p = Bukkit.getPlayer(playerName);
                if (p != null) {
                    int lvl = Integer.parseInt(args[2]);
                    if (lvl < 1 || lvl > 100) {
                        player.sendMessage(ChatColor.RED + "Invalid player level (1 - 100).");
                        break;
                    }
                    GameAPI.getGamePlayer(p).updateLevel(lvl, false, true);
                    DatabaseAPI.getInstance().update(p.getUniqueId(), EnumOperators.$SET, EnumData.LEVEL, lvl, true);
                    Utils.sendCenteredMessage(player, ChatColor.YELLOW + "Level of " + ChatColor.GREEN + p.getName() + ChatColor.YELLOW + " set to: " + ChatColor.LIGHT_PURPLE + lvl);
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_PLING, 1f, 63f);
                }
                break;
            case "gems":
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Invalid usage! /set gems <quantity>");
                    break;
                }
                int gems = Integer.parseInt(args[1]);
                DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$SET, EnumData.GEMS, gems, true);
                s.sendMessage("Gems set to " + gems);
                break;
            case "invlevel":
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Invalid usage! /set invlevel <level>");
                    break;
                }
                int invlvl = Integer.parseInt(args[1]);
                DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$SET, EnumData.INVENTORY_LEVEL, invlvl, false);
                break;
            case "portal_keys":
            	for (ShardTier tier : ShardTier.values())
            		DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$INC, tier.getShardData(), 1500, false);
                break;
            case "durability":
            	ItemStack i = player.getInventory().getItemInMainHand();
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Invalid usage! /set durability #");
                    break;
                } else if (!ItemGear.isCustomTool(i)) {
                    player.sendMessage(ChatColor.RED + "Error! This item is not repairable!");
                } else {
                	ItemGear gear = (ItemGear)PersistentItem.constructItem(i);
                	gear.damageItem(player, Integer.parseInt(args[1]));
                	player.sendMessage(ChatColor.GREEN + "Damaged!");
                }
                break;
            case "spawner":
                if (args.length < 4) {
                    player.sendMessage("/set spawner monster tier (* on monster for elite chance), (MOBS TO SPAWN x2)");
                    player.sendMessage("/set spawner goblin 2 2(spawns 4)");
                    return false;
                }
                int tier = 0;
                int spawnAmount = 0;
                String range = "-";
                try {
                    tier = Integer.parseInt(args[2]);
                    spawnAmount = Integer.parseInt(args[3]);
                    if (args.length == 5)
                        range = args[4];
                } catch (Exception exc) {
                    return false;
                }
                String text = (player.getLocation().getX() + "," + player.getLocation().getY() + ","
                        + player.getLocation().getZ() + "=" + args[1] + ":" + tier + ";" + spawnAmount);
                SpawningMechanics.getSpawners().add(SpawningMechanics.loadSpawner(text));
                SpawningMechanics.saveConfig();
                player.sendMessage("Line " + (SpawningMechanics.getSpawners().size() + 2) + " added " + args[1] + " tier " + tier);
                break;
            case "pick":
            case "rod":
            	ItemStack held = player.getEquipment().getItemInMainHand();
                if (!ProfessionItem.isProfessionItem(held)) {
                	player.sendMessage("This is not a profession item.");
                	return true;
                }
                ProfessionItem pr = (ProfessionItem)PersistentItem.constructItem(held);
                pr.levelUp(player);
                player.getEquipment().setItemInMainHand(pr.generateItem());
                break;
            case "shopoff":
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Invalid usage! /set shopoff <name>");
                    break;
                }
                playerName = args[1];
                p = Bukkit.getPlayer(playerName);
                DatabaseAPI.getInstance().update(p.getUniqueId(), EnumOperators.$SET, EnumData.HASSHOP, false, false);


                if (p != null)
                    p.sendMessage(ChatColor.GRAY + "Fixed your shop");
                break;
            case "shoplvl":
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Invalid usage! /set shoplvl <level>");
                    break;
                }
                invlvl = Integer.parseInt(args[1]);
                DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$SET, EnumData.SHOPLEVEL, invlvl, false);
                break;
            case "chaotic":
            case "neutral":
            case "lawful":
            	Player target = null;
            	if(args.length > 1)
            		target = Bukkit.getPlayer(args[1]);
            	if(target == null)
            		target = player;
            	
                KarmaHandler.getInstance().setPlayerAlignment(target, KarmaHandler.EnumPlayerAlignments.valueOf(args[0].toUpperCase()), null, false);
                player.sendMessage(ChatColor.GREEN + "Set " + target.getName() + "'s alignment to " + args[0] + ".");
                break;
            case "g":
                DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$SET, EnumData.GUILD, "", true);
                break;
            case "combatoff":
                if (Bukkit.getPlayer(args[1]) != null) {
                    CombatLog.removeFromCombat(Bukkit.getPlayer(args[1]));
                }
                else {
                    player.sendMessage(ChatColor.RED + args[1] + " not found on this shard.");
                }
                break;
            case "pvpoff":
                if (Bukkit.getPlayer(args[1]) != null) {
                    CombatLog.removeFromPVP(Bukkit.getPlayer(args[1]));
                }
                else {
                    player.sendMessage(ChatColor.RED + args[1] + " not found on this shard.");
                }
                break;
            case "ecash":
                int ecash = 0;
                if (args.length > 1) {
                    try {
                        ecash = Integer.parseInt(args[1]);
                        if (ecash < 0) {
                            player.sendMessage(ChatColor.RED + "Failed to set E-Cash value because " + ecash + " is too small.");
                            break;
                        }
                    } catch (NumberFormatException ex) {
                        player.sendMessage(ChatColor.RED + "Failed to set E-Cash value because " + args[1] + " isn't a valid number.");
                        break;
                    }
                }

                DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$SET, EnumData.ECASH, ecash, false);
                player.sendMessage(ChatColor.GREEN + "Successfully set your E-Cash value to: " + ecash + ".");
                break;
            case "hp":
            case "health":
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Invalid usage! /set health <#>");
                    break;
                }
                int hp = Integer.parseInt(args[1]);
                if (hp > 0) {
                	HealthHandler.initHP(player, hp);
                    player.sendMessage(ChatColor.GREEN + "Set health to " + hp + ".");
                } else {
                    player.sendMessage(ChatColor.RED + "Unable to set health to " + hp + ", value is  too low.");
                }
                break;
            case "achievement":
            case "achievements":
                if (!Rank.isDev(player)) {
                    player.sendMessage(ChatColor.RED + "You must be a " + ChatColor.BOLD + ChatColor.UNDERLINE + "DEVELOPER" + ChatColor.RED + " to modify this.");
                    return false;
                }
                if (args.length < 3 || (!args[1].equalsIgnoreCase("unlock") && !args[1].equalsIgnoreCase("lock"))) {
                    player.sendMessage(ChatColor.RED + "Invalid usage! /set achievement <lock|unlock> <id|*>");
                    break;
                }
                if (args[2].equalsIgnoreCase("all") || args[2].equals("*")) {
                    if (args[1].equalsIgnoreCase("unlock")) {
                        for (Achievements.EnumAchievements playerAchievements : Achievements.EnumAchievements.values()) {
                            Achievements.getInstance().giveAchievement(player.getUniqueId(), playerAchievements);
                        }
                    } else {
                        DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$SET, EnumData.ACHIEVEMENTS, new ArrayList<String>(), true);
                    }
                    player.sendMessage(ChatColor.GREEN + "Finished " + args[1].toLowerCase() + "ing all achievements.");
                } else {
                    for (Achievements.EnumAchievements playerAchievements : Achievements.EnumAchievements.values()) {
                        if (playerAchievements.getMongoName().equalsIgnoreCase("achievement." + args[2])) {
                            if (args[1].equalsIgnoreCase("unlock")) {
                                Achievements.getInstance().giveAchievement(player.getUniqueId(), playerAchievements);
                            } else {
                                DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$PULL, EnumData.ACHIEVEMENTS, playerAchievements.getMongoName(), true);
                            }
                            player.sendMessage(ChatColor.GREEN + "Successfully " + args[1].toLowerCase() + "ed the achievement: " + args[2].toLowerCase());
                            return true;
                        }
                    }
                    player.sendMessage(ChatColor.RED + "Failed to " + args[1].toLowerCase() + " achievements because " + args[2].toLowerCase() + " is invalid.");
                }
                break;
            default:
                player.sendMessage(ChatColor.RED + "Invalid usage! '" + args[0] + "' is not a valid variable.");
                break;
        }

        return true;
    }
}
