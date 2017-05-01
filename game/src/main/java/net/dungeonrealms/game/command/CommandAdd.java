package net.dungeonrealms.game.command;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.common.game.database.player.rank.Rank;
import net.dungeonrealms.game.donation.DonationEffects;
import net.dungeonrealms.game.item.ItemType;
import net.dungeonrealms.game.item.items.core.CombatItem;
import net.dungeonrealms.game.item.items.core.ItemArmor;
import net.dungeonrealms.game.item.items.core.ItemFishingPole;
import net.dungeonrealms.game.item.items.core.ItemPickaxe;
import net.dungeonrealms.game.item.items.core.ItemWeapon;
import net.dungeonrealms.game.item.items.functional.*;
import net.dungeonrealms.game.item.items.functional.ItemHealingFood.EnumHealingFood;
import net.dungeonrealms.game.item.items.functional.ecash.ItemGlobalMessager;
import net.dungeonrealms.game.item.items.functional.ecash.ItemRetrainingBook;
import net.dungeonrealms.game.item.items.functional.PotionItem;
import net.dungeonrealms.game.mastery.GamePlayer;
import net.dungeonrealms.game.mechanic.ItemManager;
import net.dungeonrealms.game.mechanic.ParticleAPI;
import net.dungeonrealms.game.mechanic.data.PotionTier;
import net.dungeonrealms.game.mechanic.data.PouchTier;
import net.dungeonrealms.game.player.json.JSONMessage;
import net.dungeonrealms.game.mechanic.data.ScrapTier;
import net.dungeonrealms.game.world.item.Item.AttributeType;
import net.dungeonrealms.game.world.item.Item.ItemRarity;
import net.dungeonrealms.game.world.item.Item.ItemTier;
import net.dungeonrealms.game.world.item.itemgenerator.ItemGenerator;
import net.dungeonrealms.game.world.spawning.BuffMechanics;
import net.dungeonrealms.game.world.teleportation.TeleportLocation;
import net.minecraft.server.v1_9_R2.NBTTagCompound;
import net.minecraft.server.v1_9_R2.NBTTagString;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.craftbukkit.v1_9_R2.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.text.NumberFormat;
import java.util.*;

import net.dungeonrealms.game.mechanic.data.EnumBuff;
//import net.dungeonrealms.common.game.database.player.rank.NewRank;

/**
 * Created by Nick on 9/17/2015.
 */
public class CommandAdd extends BaseCommand {

    public CommandAdd(String command, String usage, String description, List<String> aliases) {
        super(command, usage, description, aliases);
    }

    @Override
    public boolean onCommand(CommandSender s, Command cmd, String string, String[] args) {
        if (s instanceof ConsoleCommandSender) return false;
        Player player = (Player) s;
        if (!Rank.isGM(player)) {
            return false;
        }

        // Extended Permission Check
        if (!Rank.isHeadGM(player) && !DungeonRealms.getInstance().isGMExtendedPermissions) {
            player.sendMessage(ChatColor.RED + "You don't have permission to execute this command.");
            return false;
        }

        if (args.length > 0) {
            int tier;
            switch (args[0]) {
            	case "save":
            		ItemStack held = player.getEquipment().getItemInMainHand();
            		if (held == null || held.getType() == Material.AIR) {
            			player.sendMessage(ChatColor.RED + "You must be holding an item");
            			return true;
            		}
            		
            		if (args.length == 1) {
            			player.sendMessage(ChatColor.RED + "You must enter an item ID.");
            			return true;
            		}
            		
            		ItemGenerator.saveItem(held, args[1]);
            		player.sendMessage(ChatColor.GREEN + "Saved " + args[1] + ".");
            		break;
            	case "load":
                case "nameditem":
                    if (args.length > 1) {
                        String namedItem = args[1];
                        ItemStack itemStack = ItemGenerator.getNamedItem(namedItem);
                        player.getInventory().addItem(itemStack);
                        if (itemStack == null)
                        	player.sendMessage(ChatColor.RED + "Item not found.");
                    } else {
                        player.sendMessage(ChatColor.RED + "/ad " + args[0] + " <name>");
                    }
                    break;
                case "attributes":
                	player.sendMessage(ChatColor.GREEN + "Player Attributes:");
                	GamePlayer gp = GameAPI.getGamePlayer(player);
                	for (AttributeType at : gp.getAttributes().getAttributes())
                		player.sendMessage(at.getNBTName() + " - " + gp.getAttributes().getAttribute(at).toString());
                	break;
                case "uuid":
                    player.sendMessage(Bukkit.getPlayer(GameAPI.getUUIDFromName(player.getName())).getDisplayName());
                    break;
                case "name":
                    player.sendMessage(GameAPI.getNameFromUUID(player.getUniqueId()));
                    break;
                /*case "myrank":
                    NewRank rank = NewRank.getRank(player);
                	player.sendMessage("Your NewRank: " + rank.getChatPrefix());
                	player.sendMessage(ChatColor.RED + "Expires = " + NewRank.getDaysUntilRankExpiry(player.getUniqueId()));
                	break;
                case "setrank":
                	NewRank newRank = NewRank.valueOf(args[1].toUpperCase());
                	if(newRank == null) {
                		player.sendMessage(ChatColor.RED + "Rank Not Found.");
                		return true;
                	}
                	int expiry = 0;
                	if(args.length > 2)
                		expiry = Integer.parseInt(args[2]);
                	NewRank.setRank(player.getUniqueId(), newRank, expiry);
                	player.sendMessage(ChatColor.GREEN + "Rank Set.");
                	break;*/
                case "armor":
                case "weapon":
                	//TODO: Attribute editor.
                    try {
                    	CombatItem gear = args[0].equals("armor") ? new ItemArmor() : new ItemWeapon();
                        
                        if (args.length >= 2)
                        	gear.setTier(ItemTier.getByTier(Integer.parseInt(args[1])));

                        if (args.length >= 3)
                        	gear.setType(ItemType.valueOf(args[2].toUpperCase()));

                        if (args.length >= 4)
                        	gear.setRarity(ItemRarity.valueOf(args[3].toUpperCase()));
                        
                        player.getInventory().addItem(gear.generateItem());

                    } catch (Exception ex) {
                        player.sendMessage("Format: /ad weapon [tier] [type] [rarity]. Leave parameter blank to generate a random value.");
                    }
                    break;
                case "particle":
                    if (args[1] != null)
                        ParticleAPI.sendParticleToLocation(ParticleAPI.ParticleEffect.getById(Integer.valueOf(args[1])), player.getLocation(), new Random().nextFloat(), new Random().nextFloat(), new Random().nextFloat(), 1F, 250);
                    break;
                case "bank":
                    net.minecraft.server.v1_9_R2.ItemStack nmsBank = CraftItemStack.asNMSCopy(new ItemStack(Material.ENDER_CHEST));
                    NBTTagCompound Banktag = nmsBank.getTag() == null ? new NBTTagCompound() : nmsBank.getTag();
                    Banktag.set("type", new NBTTagString("bank"));
                    nmsBank.setTag(Banktag);
                    player.getInventory().addItem(CraftItemStack.asBukkitCopy(nmsBank));
                    break;
                case "reloadModifiers":
                    ItemGenerator.loadModifiers();
                    break;
                case "trail":
                    if (args[1] != null)
                        DonationEffects.getInstance().PLAYER_PARTICLE_EFFECTS.put(player, ParticleAPI.ParticleEffect.getById(Integer.valueOf(args[1])));
                    break;
                case "gold":
                    DonationEffects.getInstance().PLAYER_GOLD_BLOCK_TRAILS.add(player);
                    break;
                case "pick":
                	int level = args.length == 2 ? Integer.parseInt(args[1]) : 1;
                    player.getInventory().addItem(new ItemPickaxe().setLevel(level).generateItem());
                    break;
                case "rod":
                    level = args.length == 2 ? Integer.parseInt(args[1]) : 1;
                    player.getInventory().addItem(new ItemFishingPole().setLevel(level).generateItem());
                    break;
                case "resetbook":
                    player.getInventory().addItem(new ItemRetrainingBook().generateItem());
                    break;
                case "journal":
                    player.getInventory().addItem(ItemManager.createCharacterJournal(player));
                    break;
                case "scrap":
                    for (int i = 1; i <= 5; i++)
                        player.getInventory().addItem(new ItemScrap(ScrapTier.getScrapTier(i)).generateItem());
                    break;
                case "potion":
                	for (PotionTier p : PotionTier.values()) {
                		PotionItem item = new PotionItem(p);
                		player.getInventory().addItem(item.generateItem());
                		item.setSplash(true);
                		player.getInventory().addItem(item.generateItem());
            		}
                    break;
                case "food":
                    player.setFoodLevel(1);
                    for (EnumHealingFood food : EnumHealingFood.values())
                    	player.getInventory().addItem(new ItemHealingFood(food).generateItem());
                    break;
                case "orb":
                case "orb_of_alteration":
                    player.getInventory().addItem(new ItemOrb().generateItem());
                    break;
                case "orb_of_peace":
                    player.getInventory().addItem(new ItemPeaceOrb().generateItem());
                    break;
                case "orb_of_flight":
                    player.getInventory().addItem(new ItemFlightOrb().generateItem());
                    break;
                case "global_messenger":
                    player.getInventory().addItem(new ItemGlobalMessager().generateItem());
                    break;
                case "buff":
                	BuffMechanics.spawnBuff(player);
                    break;
                case "armorench":
                case "armorenchant":
                    player.getInventory().addItem(new ItemEnchantArmor(ItemTier.getByTier(Integer.parseInt(args[1]))).generateItem());
                    break;
                case "weaponench":
                case "enchant":
                case "weaponenchant":
                    player.getInventory().addItem(new ItemEnchantWeapon(ItemTier.getByTier(Integer.parseInt(args[1]))).generateItem());
                    break;
                case "prot":
                case "protect":
                case "scroll":
                case "protectscroll":
                    player.getInventory().addItem(new ItemProtectionScroll(ItemTier.getByTier(Integer.parseInt(args[1]))).generateItem());
                    break;
                case "realmchest":
                    player.getInventory().addItem(new ItemRealmChest().generateItem());
                    break;
                case "pouch":
                    tier = Integer.parseInt(args[1]);
                    player.getInventory().addItem(new ItemGemPouch(PouchTier.getById(tier)).generateItem());
                    break;
                case "votemessage":
                    if (GameAPI.getGamePlayer(player) == null) {
                        break;
                    }
                    GamePlayer gamePlayer = GameAPI.getGamePlayer(player);
                    int expToLevel = gamePlayer.getEXPNeeded(gamePlayer.getLevel());
                    int expToGive = expToLevel / 20;
                    expToGive += 100;
                    gamePlayer.addExperience(expToGive, false, true);
                    final JSONMessage normal = new JSONMessage(ChatColor.AQUA + player.getName() + ChatColor.RESET + ChatColor.GRAY + " voted for 15 ECASH & 5% EXP @ vote ", ChatColor.WHITE);
                    normal.addURL(ChatColor.AQUA.toString() + ChatColor.BOLD + ChatColor.UNDERLINE + "HERE", ChatColor.AQUA, "http://dungeonrealms.net/vote");
                    for (Player player1 : Bukkit.getOnlinePlayers()) {
                        normal.sendToPlayer(player1);
                    }
                    break;
                case "banknote":
                    int quantity = 1000;
                    if (args.length >= 2) {
                        try {
                            quantity = Integer.parseInt(args[1]);
                            if (quantity <= 0) {
                                player.sendMessage(ChatColor.RED + "Failed to create bank note because " + quantity + " is too small.");
                                break;
                            }
                        } catch (NumberFormatException ex) {
                            player.sendMessage(ChatColor.RED + "Failed to create bank note because " + args[1] + " isn't a valid number.");
                            break;
                        }
                    }
                    player.getInventory().addItem(new ItemGemNote(player.getName(), quantity).generateItem());
                    player.sendMessage(ChatColor.GREEN + "Successfully created a bank note worth " + NumberFormat.getIntegerInstance().format(quantity) + " gems.");
                    break;
                case "teleport":
                case "teleports":
                    if (args.length == 1) {
                        for (TeleportLocation tl : TeleportLocation.values())
                            player.getInventory().addItem(new ItemTeleportBook(tl).generateItem());
                        player.sendMessage(ChatColor.GREEN + "Spawned all teleport books.");
                    } else if (args.length >= 2) {
                        if (args[1].equalsIgnoreCase("random")) {
                            player.getInventory().addItem(new ItemTeleportBook().generateItem());
                            player.sendMessage(ChatColor.GREEN + "Spawned random teleport book.");
                        } else {
                            TeleportLocation tl = TeleportLocation.valueOf(args[1].toUpperCase());
                            if (tl == null) {
                                player.sendMessage(ChatColor.RED + "Location not found.");
                                return true;
                            }
                            player.getInventory().addItem(new ItemTeleportBook(tl).generateItem());
                            player.sendMessage(ChatColor.GREEN + "Spawned " + tl.getDisplayName() + " teleport book.");
                        }
                    }
                    break;
                case "ecash_buff":
                    if (args.length >= 2) {
                        int buffDuration = 1800;
                        int buffBonus = 20;

                        if (args.length >= 3) {
                            buffDuration = Integer.parseInt(args[2]) * 60;
                        }
                        if (args.length >= 4) {
                            buffBonus = Integer.parseInt(args[3]);
                        }

                        player.getInventory().addItem(new ItemBuff(EnumBuff.valueOf(args[1].toUpperCase()), buffDuration, buffBonus).generateItem());
                    } else {
                        player.sendMessage(ChatColor.RED + "Invalid usage! /add ecash_buff <LOOT|PROFESSION|LEVEL>");
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