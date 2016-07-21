/**
 *
 */
package net.dungeonrealms.game.commands;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.game.achievements.Achievements;
import net.dungeonrealms.game.database.DatabaseAPI;
import net.dungeonrealms.game.database.player.Rank;
import net.dungeonrealms.game.database.type.EnumData;
import net.dungeonrealms.game.database.type.EnumOperators;
import net.dungeonrealms.game.handlers.HealthHandler;
import net.dungeonrealms.game.handlers.KarmaHandler;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.player.combat.CombatLog;
import net.dungeonrealms.game.profession.Mining;
import net.dungeonrealms.game.world.loot.LootManager;
import net.dungeonrealms.game.world.spawning.BaseMobSpawner;
import net.dungeonrealms.game.world.spawning.SpawningMechanics;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;

/**
 * Created by Chase on Sep 22, 2015
 */
public class CommandSet extends BasicCommand {

    public CommandSet(String command, String usage, String description) {
        super(command, usage, description);
    }

    @Override
    public boolean onCommand(CommandSender s, Command cmd, String string, String[] args) {
        if (!(s instanceof Player))
            return false;
        Player player = (Player) s;
        if (!Rank.isGM(player)) {
            return false;
        }
        if (args.length > 0) {
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
                        DatabaseAPI.getInstance().update(p.getUniqueId(), EnumOperators.$SET, EnumData.LEVEL, lvl, true, true);
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
                    DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$SET, EnumData.GEMS, gems, true, false);
                    s.sendMessage("Gems set to " + gems);
                    break;
                case "invlevel":
                    if (args.length < 2) {
                        player.sendMessage(ChatColor.RED + "Invalid usage! /set invlevel <level>");
                        break;
                    }
                    int invlvl = Integer.parseInt(args[1]);
                    DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$SET, EnumData.INVENTORY_LEVEL, invlvl, true, false);
                    break;
                case "portalKeys":
                    DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$INC, EnumData.PORTAL_SHARDS_T1, 1500, false, false);
                    DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$INC, EnumData.PORTAL_SHARDS_T2, 1500, false, false);
                    DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$INC, EnumData.PORTAL_SHARDS_T3, 1500, false, false);
                    DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$INC, EnumData.PORTAL_SHARDS_T4, 1500, false, false);
                    DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$INC, EnumData.PORTAL_SHARDS_T5, 1500, true, false);
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
                    player.sendMessage("Line " + (SpawningMechanics.SPAWNER_CONFIG.size() + 2) + " added " + args[1] + " tier " + tier);
                    SpawningMechanics.SPAWNER_CONFIG.add(text);
                    DungeonRealms.getInstance().getConfig().set("spawners", SpawningMechanics.SPAWNER_CONFIG);
                    DungeonRealms.getInstance().saveConfig();
                    SpawningMechanics.loadSpawner(text);
                    break;
                case "loot":
                    if (args.length < 2) {
                        player.sendMessage(ChatColor.RED + "Invalid usage! /set loot <tier>");
                    }
                    int lootTier = Integer.parseInt(args[1]);
                    String data = player.getLocation().getX() + "," + player.getLocation().getY() + "," + player.getLocation().getZ() + ":" + lootTier;
                    LootManager.SPAWNER_CONFIG.add(data);
                    DungeonRealms.getInstance().getConfig().set("loot", LootManager.SPAWNER_CONFIG);
                    player.getWorld().getBlockAt(player.getLocation()).setType(Material.SPONGE);
                    player.sendMessage((LootManager.LOOT_SPAWNERS.size() + 1) + " loot spawner placed");
                    break;
                case "kill":
                    player.getWorld().getLivingEntities().forEach(org.bukkit.entity.Entity::remove);
                    SpawningMechanics.getALLSPAWNERS().forEach(BaseMobSpawner::kill);
                    break;
                case "pick":
                    Mining.lvlUp(Mining.getPickTier(player.getEquipment().getItemInMainHand()), player);
                    player.updateInventory();
                    break;
                case "shopoff":
                    if (args.length < 2) {
                        player.sendMessage(ChatColor.RED + "Invalid usage! /set shopoff <name>");
                        break;
                    }
                    playerName = args[1];
                    p = Bukkit.getPlayer(playerName);
                    DatabaseAPI.getInstance().update(p.getUniqueId(), EnumOperators.$SET, EnumData.HASSHOP, false, true, false);

                    if (p != null)
                        p.sendMessage(ChatColor.GRAY + "Fixed your shop");
                    break;
                case "shoplvl":
                    if (args.length < 2) {
                        player.sendMessage(ChatColor.RED + "Invalid usage! /set shoplvl <level>");
                        break;
                    }
                    invlvl = Integer.parseInt(args[1]);
                    DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$SET, EnumData.SHOPLEVEL, invlvl, true, false);
                    break;
                case "chaotic":
                    KarmaHandler.getInstance().setPlayerAlignment(player, KarmaHandler.EnumPlayerAlignments.CHAOTIC, null, false);
                    break;
                case "neutral":
                    KarmaHandler.getInstance().setPlayerAlignment(player, KarmaHandler.EnumPlayerAlignments.NEUTRAL, null, false);
                    break;
                case "lawful":
                    KarmaHandler.getInstance().setPlayerAlignment(player, KarmaHandler.EnumPlayerAlignments.LAWFUL, null, false);
                    break;
                case "g":
                    DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$SET, EnumData.GUILD, "", true, true);
                    break;
                case "combatoff":
                    if (Bukkit.getPlayer(args[1]) != null) {
                        CombatLog.removeFromCombat(Bukkit.getPlayer(args[1]));
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

                    DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$SET, EnumData.ECASH, ecash, true, false);
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
                        HealthHandler.getInstance().setPlayerMaxHPLive(player, hp);
                        HealthHandler.getInstance().setPlayerHPLive(player, hp);
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
                            DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$SET, EnumData.ACHIEVEMENTS, new ArrayList<String>(), true, true);
                        }
                        player.sendMessage(ChatColor.GREEN + "Finished " + args[1].toLowerCase() + "ing all achievements.");
                    } else {
                        for (Achievements.EnumAchievements playerAchievements : Achievements.EnumAchievements.values()) {
                            if (playerAchievements.getMongoName().equalsIgnoreCase("achievement." + args[2])) {
                                if (args[1].equalsIgnoreCase("unlock")) {
                                    Achievements.getInstance().giveAchievement(player.getUniqueId(), playerAchievements);
                                } else {
                                    DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$PULL, EnumData.ACHIEVEMENTS, playerAchievements.getMongoName(), true, true);
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
        }
        return true;
    }
}
