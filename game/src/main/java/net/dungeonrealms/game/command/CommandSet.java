package net.dungeonrealms.game.command;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.common.game.database.player.rank.Rank;
import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.game.guild.GuildWrapper;
import net.dungeonrealms.game.guild.database.GuildDatabase;
import net.dungeonrealms.game.handler.HealthHandler;
import net.dungeonrealms.game.handler.KarmaHandler.EnumPlayerAlignments;
import net.dungeonrealms.game.item.PersistentItem;
import net.dungeonrealms.game.item.items.core.ItemGear;
import net.dungeonrealms.game.item.items.core.ProfessionItem;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.mechanic.data.ShardTier;
import net.dungeonrealms.game.player.combat.CombatLog;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

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
        String[] ignoreExtendedPermissions = new String[]{"health", "hp"};
        if (!Rank.isHeadGM(player) && !Arrays.asList(ignoreExtendedPermissions).contains(args[0])
                && !DungeonRealms.getInstance().isGMExtendedPermissions) {
            player.sendMessage(ChatColor.RED + "You don't have permission to execute this command.");
            return false;
        }

        PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(player);
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
                    PlayerWrapper.getPlayerWrapper(p.getUniqueId(), false, true, wrapp -> wrapp.setLevel(lvl));
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
                wrapper.setGems(gems);
//                DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$SET, EnumData.GEMS, gems, true);
                s.sendMessage("Gems set to " + gems);
                break;
            case "invlevel":
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Invalid usage! /set invlevel <level>");
                    break;
                }
                int invlvl = Integer.parseInt(args[1]);
                wrapper.setBankLevel(invlvl);
//                DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$SET, EnumData.INVENTORY_LEVEL, invlvl, false);
                break;
            case "portal_keys":
            	PlayerWrapper pw = PlayerWrapper.getPlayerWrapper(player);
            	for (ShardTier tier : ShardTier.values())
            		pw.setPortalShards(tier, 1500);
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

                PlayerWrapper foundWrapper = PlayerWrapper.getPlayerWrapper(p);
                if (foundWrapper != null) {
                    foundWrapper.setShopOpened(false);
                    if (p != null)
                        p.sendMessage(ChatColor.GRAY + "Fixed your shop");
                }
                break;
            case "shoplvl":
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Invalid usage! /set shoplvl <level>");
                    break;
                }
                invlvl = Integer.parseInt(args[1]);
                wrapper.setShopLevel(invlvl);
//                DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$SET, EnumData.SHOPLEVEL, invlvl, false);
                break;
            case "chaotic":
            case "neutral":
            case "lawful":
                Player target = null;
                if (args.length > 1)
                    target = Bukkit.getPlayer(args[1]);
                if (target == null)
                    target = player;

                PlayerWrapper.getWrapper(target).setAlignment(EnumPlayerAlignments.valueOf(args[0].toUpperCase()));
                player.sendMessage(ChatColor.GREEN + "Set " + target.getName() + "'s alignment to " + args[0] + ".");
                break;
            case "g":
                GuildWrapper guildWrapper = GuildDatabase.getAPI().getPlayersGuildWrapper(player.getUniqueId());
                if(guildWrapper == null) {
                    player.sendMessage(ChatColor.RED + "You are not in a guild!");
                    break;
                }
                guildWrapper.removePlayer(player.getUniqueId());
                wrapper.setGuildID(0);
            break;
            case "combatoff":
                if (Bukkit.getPlayer(args[1]) != null) {
                    CombatLog.removeFromCombat(Bukkit.getPlayer(args[1]));
                } else {
                    player.sendMessage(ChatColor.RED + args[1] + " not found on this shard.");
                }
                break;
            case "pvpoff":
                if (Bukkit.getPlayer(args[1]) != null) {
                    CombatLog.removeFromPVP(Bukkit.getPlayer(args[1]));
                } else {
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

                wrapper = PlayerWrapper.getPlayerWrapper(player);
                wrapper.setEcash(ecash);
//                DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$SET, EnumData.ECASH, ecash, false);
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
            default:
                player.sendMessage(ChatColor.RED + "Invalid usage! '" + args[0] + "' is not a valid variable.");
                break;
        }

        return true;
    }
}
