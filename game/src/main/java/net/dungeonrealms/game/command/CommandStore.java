package net.dungeonrealms.game.command;


import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.common.game.database.DatabaseAPI;
import net.dungeonrealms.common.game.database.player.rank.Rank;
import net.dungeonrealms.game.anticheat.AntiDuplication;
import net.dungeonrealms.game.handler.MailHandler;
import net.dungeonrealms.game.mechanic.ItemManager;
import net.dungeonrealms.game.profession.Fishing;
import net.dungeonrealms.game.profession.Mining;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by Brad on 24/12/2016.
 */
public class CommandStore extends BaseCommand {
    public CommandStore(String command, String usage, String description) {
        super(command, usage, description);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;

            // Player isn't a developer nor a support agent.
            if (!Rank.isDev(player) && !Rank.isSupport(player))
                return false;

            // Player is a support agent. However, they're not on the support shard.
            if (!Rank.isDev(player) && !DungeonRealms.getInstance().isSupportShard)
                return false;
        } else if (!(sender instanceof ConsoleCommandSender)) {
            // User isn't a player nor the console and therefore we can't use t his command.
            return false;
        }

        // Check whether we have the required number of arguments in order to continue.
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Invalid usage! /store <playername> <storeitem> [...]");
            return false;
        }

        ArrayList<ItemStack> items = new ArrayList<ItemStack>();
        ItemStack thisItem;
        ItemMeta thisItemMeta;
        String playerName = args[0];
        UUID playerUUID;
        String storeItem = args[1].toLowerCase();
        String result = DatabaseAPI.getInstance().getUUIDFromName(playerName);

        // Check to see whether or not the user has an account on Dungeon Realms.
        if (result.equals("")) {
            sender.sendMessage(ChatColor.RED + "Failed to find a user with the name " +
                    ChatColor.BOLD + ChatColor.UNDERLINE + playerName + ChatColor.RED + "!" );
            return false;
        } else {
            playerUUID = UUID.fromString(result);
        }


        // Check which item(s) to issue the user.
        switch (storeItem) {
            case "dev_test":
                thisItem = new ItemStack(Material.BEACON);
                thisItemMeta = thisItem.getItemMeta();
                thisItemMeta.setDisplayName(ChatColor.GOLD + "Developer Test Item");
                thisItem.setItemMeta(thisItemMeta);
                thisItem = AntiDuplication.getInstance().applyAntiDupe(thisItem);
                items.add(thisItem);
                break;
            case "global_buff":
                if (args.length >= 3) {
                    int buffDuration = 1800;
                    int buffBonus = 20;

                    if (args.length >= 4) {
                        buffDuration = Integer.parseInt(args[3]) * 60;
                    }
                    if (args.length >= 5) {
                        buffBonus = Integer.parseInt(args[4]);
                    }

                    switch (args[2].toLowerCase()) {
                        case "loot":
                            items.add(ItemManager.createLootBuff(buffDuration, buffBonus));
                            break;
                        case "profession":
                            items.add(ItemManager.createProfessionBuff(buffDuration, buffBonus));
                            break;
                        case "level":
                            items.add(ItemManager.createLevelBuff(buffDuration, buffBonus));
                            break;
                    }
                } else {
                    sender.sendMessage(ChatColor.RED + "Invalid usage! Missing buff type.");
                    return false;
                }
                break;
            case "scroll":
                if (args.length >= 4) {
                    String scrollType = args[2].toLowerCase();
                    int scrollTier = Integer.parseInt(args[3]);
                    if (scrollTier >= 1 && scrollTier <= 5) {
                        if (scrollType.equals("weapon")) {
                            items.add(ItemManager.createWeaponEnchant(scrollTier));
                        } else if (scrollType.equals("armor") || scrollType.equals("armour")) {
                            items.add(ItemManager.createArmorEnchant(scrollTier));
                        } else if (scrollType.equals("protect") || scrollType.equals("protection")) {
                            items.add(ItemManager.createProtectScroll(scrollTier));
                        } else if (scrollType.equals("mining") || scrollType.equals("pick") || scrollType.equals("pickaxe")) {
                            if (args.length >= 5) {
                                Mining.EnumMiningEnchant enchantType = Mining.EnumMiningEnchant.getEnchant(args[4]);
                                items.add((args.length >= 6 ? Mining.getEnchant(scrollTier, enchantType, Integer.parseInt(args[5])) : Mining.getEnchant(scrollTier, enchantType)));
                            } else {
                                sender.sendMessage(ChatColor.RED + "Invalid usage! Missing enchantment type.");
                            }
                        } else if (scrollType.equals("fishing") || scrollType.equals("fish") || scrollType.equals("rod") || scrollType.equals("fishingrod")) {
                            if (args.length >= 5) {
                                Fishing.FishingRodEnchant enchantType = Fishing.FishingRodEnchant.getEnchant(args[4]);
                                items.add((args.length >= 6 ? Fishing.getEnchant(scrollTier, enchantType, Integer.parseInt(args[5])) : Fishing.getEnchant(scrollTier, enchantType)));
                            } else {
                                sender.sendMessage(ChatColor.RED + "Invalid usage! Missing enchantment type.");
                            }
                        } else {
                            sender.sendMessage(ChatColor.RED + "Invalid usage! Scroll type must be weapon, armor, protect, mining or fishing.");
                        }
                    } else {
                        sender.sendMessage(ChatColor.RED + "Invalid usage! Scroll tier must be between 1-5.");
                    }
                } else {
                    sender.sendMessage(ChatColor.RED + "Invalid usage! Missing scroll type / scroll tier.");
                }
                break;
            case "orb_of_alteration":
                items.add(ItemManager.createOrbofAlteration());
                break;
            case "orb_of_peace":
                items.add(ItemManager.createOrbofPeace());
                break;
            case "orb_of_flight":
                items.add(ItemManager.createOrbofFlight());
                break;
            case "global_messenger":
                items.add(ItemManager.createGlobalMessenger());
                break;
            case "realm_chest":
                items.add(ItemManager.createRealmChest());
                break;
            case "profession":
                if (args.length >= 3) {
                    int tier = 1;

                    if (args.length >= 4) {
                        tier = Integer.parseInt(args[3]);

                        if (tier < 1 || tier > 5) {
                            sender.sendMessage(ChatColor.RED + "Invalid usage! Invalid tier 1-5.");
                            break;
                        }
                    }

                    switch (args[2].toLowerCase()) {
                        case "mining":
                        case "mine":
                        case "pick":
                        case "pickaxe":
                            items.add(ItemManager.createPickaxe(tier));
                            break;
                        case "fishing":
                        case "fish":
                        case "rod":
                        case "fishingrod":
                        case "fishing_rod":
                            items.add(ItemManager.createFishingPole(tier));
                            break;
                        default:
                            sender.sendMessage(ChatColor.RED + "Invalid usage! Invalid profession item.");
                            break;
                    }
                }
                break;
            default:
                sender.sendMessage(ChatColor.RED + "Unrecognised store item " + storeItem + " for " + playerName + ".");
                return false;
        }

        // Check to ensure the package has at least one item.
        if (items.size() == 0) {
            sender.sendMessage(ChatColor.RED + "No items to issue for the item " + storeItem + " to " + playerName + "!");
            return false;
        }

        // Loop through all of the items and send them to the user.
        for (ItemStack item : items) {
            sendItem(playerUUID, item);
        }

        // Success! We have issued all of the item(s) to the player!
        sender.sendMessage(ChatColor.GREEN + "Given " + items.size() + " item(s) to " + playerName + " for " + storeItem + ".");
        return true;
    }

    private boolean sendItem(UUID playerUUID, ItemStack itemStack) {
        return MailHandler.getInstance().sendMailRaw("The Dungeon Realms Team", playerUUID, itemStack);
    }
}