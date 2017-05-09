package net.dungeonrealms.game.command;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.common.game.database.player.Rank;
import net.dungeonrealms.common.game.database.sql.SQLDatabaseAPI;
import net.dungeonrealms.game.anticheat.AntiDuplication;
import net.dungeonrealms.game.item.items.core.ItemFishingPole;
import net.dungeonrealms.game.item.items.core.ItemPickaxe;
import net.dungeonrealms.game.item.items.functional.*;
import net.dungeonrealms.game.item.items.functional.ecash.*;
import net.dungeonrealms.game.world.item.Item.ItemTier;
import net.dungeonrealms.game.world.item.Item.PickaxeAttributeType;
import net.dungeonrealms.game.world.item.Item.FishingAttributeType;

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

import net.dungeonrealms.game.mechanic.data.EnumBuff;

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
            if (!Rank.isDev(player) && !DungeonRealms.isSupport())
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

        String storeItem = args[1].toLowerCase();
        String playerName = args[0];
        SQLDatabaseAPI.getInstance().getUUIDFromName(playerName, false, uuid -> {
            ArrayList<ItemStack> items = new ArrayList<ItemStack>();
            ItemStack thisItem;
            ItemMeta thisItemMeta;
            if(uuid == null){
                sender.sendMessage(ChatColor.RED + "Failed to find a user with the name " +
                        ChatColor.BOLD + ChatColor.UNDERLINE + playerName + ChatColor.RED + "!");
                return;
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
                        
                        items.add(new ItemBuff(EnumBuff.valueOf(args[2].toUpperCase()), buffDuration, buffBonus).generateItem());
                    } else {
                    	sender.sendMessage(ChatColor.RED + "Not enough args.");
                    }
                    break;
                case "scroll":
                	if (args.length >= 4) {
                		String scrollType = args[2].toLowerCase();
                		int scrollTier = Integer.parseInt(args[3]);
                		if (scrollTier >= 1 && scrollTier <= 5) {
                			ItemTier tier = ItemTier.getByTier(scrollTier);
                			if (scrollType.equals("weapon")) {
                				items.add(new ItemEnchantWeapon(tier).generateItem());
                			} else if (scrollType.equals("armor") || scrollType.equals("armour")) {
                				items.add(new ItemEnchantArmor(tier).generateItem());
                			} else if (scrollType.equals("protect") || scrollType.equals("protection")) {
                				items.add(new ItemProtectionScroll(tier).generateItem());
                			} else if (scrollType.equals("mining") || scrollType.equals("pick") || scrollType.equals("pickaxe")) {
                				if (args.length >= 5) {
                					items.add(new ItemEnchantPickaxe(PickaxeAttributeType.valueOf(args[4].toUpperCase())).generateItem());
                				} else {
                					sender.sendMessage(ChatColor.RED + "Invalid usage! Missing enchantment type.");
                				}
                			} else if (scrollType.equals("fishing") || scrollType.equals("fish") || scrollType.equals("rod") || scrollType.equals("fishingrod")) {
                            if (args.length >= 5) {
                            	items.add(new ItemEnchantFishingRod(FishingAttributeType.valueOf(args[4].toUpperCase())).generateItem());
                            } else {
                            	sender.sendMessage(ChatColor.RED + "Invalid usage! Missing enchantment type.");
                            }
                        } else {
                            sender.sendMessage(ChatColor.RED + "Invalid usage! Scroll type must be weapon, armor, protect, mining or fishing.");
                        }
                    } else {
                        sender.sendMessage(ChatColor.RED + "Invalid tier.");
                    }
                }
                break;
            case "orb_of_alteration":
                items.add(new ItemOrb().generateItem());
                break;
            case "orb_of_peace":
                items.add(new ItemPeaceOrb().generateItem());
                break;
            case "orb_of_flight":
                items.add(new ItemFlightOrb().generateItem());
                break;
            case "global_messenger":
                items.add(new ItemGlobalMessager().generateItem());
                break;
            case "realm_chest":
                items.add(new ItemRealmChest().generateItem());
                break;
            case "profession":
                if (args.length >= 3) {
                    int tier = 1;
                    switch (args[2].toLowerCase()) {
                        case "mining":
                        case "mine":
                        case "pick":
                        case "pickaxe":
                        	items.add(new ItemPickaxe(tier * 20).generateItem());
                            break;
                        case "fishing":
                        case "fish":
                        case "rod":
                        case "fishingrod":
                        case "fishing_rod":
                        	items.add(new ItemFishingPole(tier * 20).generateItem());
                            break;
                        default:
                            sender.sendMessage(ChatColor.RED + "Invalid usage! Invalid profession item.");
                            break;
                    }
                }
                break;
            default:
                sender.sendMessage(ChatColor.RED + "Unrecognised store item " + storeItem + " for " + playerName + ".");
            }

            // Loop through all of the items and send them to the user.
            for (ItemStack item : items)
                sendItem(uuid, item);

            // Success! We have issued all of the item(s) to the player!
            sender.sendMessage(ChatColor.GREEN + "Given " + items.size() + " item(s) to " + playerName + " for " + storeItem + ".");
        });
        return true;
    }

    private boolean sendItem(UUID playerUUID, ItemStack itemStack) {
        return false;
        //TODO: FIX
//        return MailHandler.getInstance().sendMailRaw("The Dungeon Realms Team", playerUUID, itemStack);
    }
}