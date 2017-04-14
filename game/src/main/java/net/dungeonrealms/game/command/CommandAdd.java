package net.dungeonrealms.game.command;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.common.game.database.DatabaseAPI;
import net.dungeonrealms.common.game.database.data.EnumData;
import net.dungeonrealms.common.game.database.data.EnumOperators;
import net.dungeonrealms.common.game.database.player.rank.Rank;
import net.dungeonrealms.game.affair.Affair;
import net.dungeonrealms.game.donation.DonationEffects;
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
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.mechanic.ItemManager;
import net.dungeonrealms.game.mechanic.ParticleAPI;
import net.dungeonrealms.game.mechanic.data.PotionTier;
import net.dungeonrealms.game.mechanic.data.PouchTier;
import net.dungeonrealms.game.miscellaneous.ItemBuilder;
import net.dungeonrealms.game.miscellaneous.NBTWrapper;
import net.dungeonrealms.game.player.banks.BankMechanics;
import net.dungeonrealms.game.player.json.JSONMessage;
import net.dungeonrealms.game.profession.Fishing;
import net.dungeonrealms.game.profession.Mining;
import net.dungeonrealms.game.world.entity.type.mounts.EnumMountSkins;
import net.dungeonrealms.game.world.entity.type.mounts.EnumMounts;
import net.dungeonrealms.game.mechanic.data.ScrapTier;
import net.dungeonrealms.game.world.entity.type.pet.EnumPets;
import net.dungeonrealms.game.world.entity.util.BuffUtils;
import net.dungeonrealms.game.world.item.Item;
import net.dungeonrealms.game.world.item.Item.AttributeType;
import net.dungeonrealms.game.world.item.Item.GeneratedItemType;
import net.dungeonrealms.game.world.item.Item.ItemRarity;
import net.dungeonrealms.game.world.item.Item.ItemTier;
import net.dungeonrealms.game.world.item.itemgenerator.ItemGenerator;
import net.dungeonrealms.game.world.teleportation.TeleportLocation;
import net.minecraft.server.v1_9_R2.NBTTagCompound;
import net.minecraft.server.v1_9_R2.NBTTagInt;
import net.minecraft.server.v1_9_R2.NBTTagList;
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
                case "nameditem":
                    if (args.length == 2) {
                        String namedItem = null;
                        try {
                            namedItem = args[1];
                        } catch (Exception e) {
                            player.sendMessage(ChatColor.RED + "Argument 2 must be a string");
                            e.printStackTrace();
                        }
                        if (namedItem != null) {
                            ItemStack itemStack = ItemGenerator.getNamedItem(namedItem);
                            if (itemStack != null) {
                                player.getInventory().addItem(itemStack);
                            }
                        }
                    } else {
                        player.sendMessage(ChatColor.RED + "/ad nameitem <name>");
                    }
                    break;
                case "attributes":
                	player.sendMessage(ChatColor.GREEN + "Player Attributes:");
                	GamePlayer gp = GameAPI.getGamePlayer(player);
                	for (AttributeType at : gp.getAttributes().getAttributes())
                		player.sendMessage(at.getNBTName() + " - " + gp.getAttributes().getAttribute(at).toString());
                	break;
                case "pcheck":
                    player.sendMessage(ChatColor.GREEN + "There are " + String.valueOf(Affair.getInstance()._parties.size()));
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
                case "weapon":
                	//TODO: Attribute editor.
                    try {
                    	ItemWeapon generator = new ItemWeapon();
                        generator.setType(GeneratedItemType.getRandomWeapon());
                        
                        if (args.length >= 2)
                            generator.setTier(Item.ItemTier.getByTier(Integer.parseInt(args[1])));

                        if (args.length >= 3)
                            generator.setType(GeneratedItemType.getByName(args[2]));

                        if (args.length >= 4)
                            generator.setRarity(Item.ItemRarity.valueOf(args[3].toUpperCase()));
                        
                        player.getInventory().addItem(generator.generateItem());

                    } catch (Exception ex) {
                        player.sendMessage("Format: /ad weapon [tier] [type] [rarity]. Leave parameter blank to generate a random value.");
                    }
                    break;
                case "armor":
                	//TODO: Attribute Editor.
                    try {
                    	ItemArmor generator = new ItemArmor();
                    	generator.setGeneratedItemType(GeneratedItemType.getRandomArmor());
                    	
                        if (args.length >= 2)
                            generator.setTier(Item.ItemTier.getByTier(Integer.parseInt(args[1])));
                        
                        if (args.length >= 3)
                            generator.setGeneratedItemType(GeneratedItemType.getByName(args[2]));
                        
                        if (args.length >= 4)
                            generator.setRarity(Item.ItemRarity.valueOf(args[3].toUpperCase()));
                        
                        player.getInventory().addItem(generator.generateItem());
                        
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        player.sendMessage("Format: /ad weapon [tier] [type] [rarity]. Leave parameter blank to generate a random value.");
                    }
                    break;
                case "customitem":
                    String name = args[1];
                    player.getInventory().addItem(ItemGenerator.getNamedItem(name));
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
                    BuffUtils.spawnBuff(player.getUniqueId());
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
                case "everything":
                    // This is a special command for giving YouTubers "everything" & for testing.
                    // Therefore, we want to ensure that the player is an authorized developer.
                    if (!Rank.isDev(player)) {
                        player.sendMessage(ChatColor.RED + "This command can only be executed by a a developer.");
                        return false;
                    }

                    // If we don't specify a 2nd argument, we assume we're doing it to ourselves.
                    Player currentProfile = player;
                    if (args.length >= 2) {
                        if (Bukkit.getPlayer(args[1]) != null && Bukkit.getPlayer(args[1]).getDisplayName().equalsIgnoreCase(args[1])) {
                            currentProfile = Bukkit.getPlayer(args[1]);
                        } else {
                            player.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + ChatColor.UNDERLINE + args[1] + ChatColor.RED + " is offline.");
                            return false;
                        }
                    }

                    // Add all pets to the player.
                    List<String> playerPets = (ArrayList<String>) DatabaseAPI.getInstance().getData(EnumData.PETS, currentProfile.getUniqueId());
                    for (EnumPets pets : EnumPets.values()) {
                        if (pets == EnumPets.BABY_HORSE) {
                            continue;
                        }
                        if (!playerPets.isEmpty()) {
                            if (playerPets.contains(pets.getName().toUpperCase())) {
                                continue;
                            }
                            boolean hasPet = false;
                            for (String playerPet : playerPets) {
                                if (playerPet.contains("@") && playerPet.split("@")[0].equals(pets.getName())) {
                                    hasPet = true;
                                    break;
                                }
                            }
                            if (hasPet) continue;
                        }
                        DatabaseAPI.getInstance().update(currentProfile.getUniqueId(), EnumOperators.$PUSH, EnumData.PETS, pets.getName(), true);
                        player.sendMessage(ChatColor.GREEN + "Added the " + ChatColor.BOLD + ChatColor.UNDERLINE + Utils.ucfirst(pets.getName()) + ChatColor.GREEN + " pet to " + ChatColor.BOLD + ChatColor.UNDERLINE + currentProfile.getDisplayName() + ChatColor.GREEN + ".");
                    }

                    DatabaseAPI.getInstance().update(currentProfile.getUniqueId(), EnumOperators.$SET, EnumData.ACTIVE_PET, EnumPets.BAT.getName(), true);

                    // Add all trails to the player.
                    List<String> playerTrails = (ArrayList<String>) DatabaseAPI.getInstance().getData(EnumData.PARTICLES, currentProfile.getUniqueId());
                    for (ParticleAPI.ParticleEffect trails : ParticleAPI.ParticleEffect.values()) {
                        if (!playerTrails.isEmpty()) {
                            if (playerTrails.contains(trails.getRawName().toUpperCase())) {
                                continue;
                            }
                        }
                        DatabaseAPI.getInstance().update(currentProfile.getUniqueId(), EnumOperators.$PUSH, EnumData.PARTICLES, trails.getRawName(), true);
                        player.sendMessage(ChatColor.GREEN + "Added the " + ChatColor.BOLD + ChatColor.UNDERLINE + Utils.ucfirst(trails.getRawName()) + ChatColor.GREEN + " trail to " + ChatColor.BOLD + ChatColor.UNDERLINE + currentProfile.getDisplayName() + ChatColor.GREEN + ".");
                    }

                    DatabaseAPI.getInstance().update(currentProfile.getUniqueId(), EnumOperators.$SET, EnumData.ACTIVE_TRAIL, ParticleAPI.ParticleEffect.CRIT.getRawName(), true);

                    // Add all mount skins to the player.
                    List<String> playerMountSkins = (ArrayList<String>) DatabaseAPI.getInstance().getData(EnumData.MOUNT_SKINS, currentProfile.getUniqueId());
                    for (EnumMountSkins mountSkins : EnumMountSkins.values()) {
                        if (!playerMountSkins.isEmpty()) {
                            if (playerMountSkins.contains(mountSkins.getName().toUpperCase())) {
                                continue;
                            }
                        }
                        DatabaseAPI.getInstance().update(currentProfile.getUniqueId(), EnumOperators.$PUSH, EnumData.MOUNT_SKINS, mountSkins.getName(), true);
                        player.sendMessage(ChatColor.GREEN + "Added the " + ChatColor.BOLD + ChatColor.UNDERLINE + Utils.ucfirst(mountSkins.getName()) + ChatColor.GREEN + " mount skin to " + ChatColor.BOLD + ChatColor.UNDERLINE + currentProfile.getDisplayName() + ChatColor.GREEN + ".");
                    }

                    DatabaseAPI.getInstance().update(currentProfile.getUniqueId(), EnumOperators.$SET, EnumData.ACTIVE_MOUNT_SKIN, EnumMountSkins.SKELETON_HORSE.getName(), true);

                    player.sendMessage(ChatColor.GREEN + ChatColor.BOLD.toString() + ChatColor.UNDERLINE + currentProfile.getDisplayName() + ChatColor.GREEN + " has received everything.");
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