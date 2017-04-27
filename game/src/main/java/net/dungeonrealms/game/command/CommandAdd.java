package net.dungeonrealms.game.command;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.common.game.database.player.rank.Rank;
import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.game.affair.Affair;
import net.dungeonrealms.game.donation.DonationEffects;
import net.dungeonrealms.game.mastery.GamePlayer;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.mechanic.ItemManager;
import net.dungeonrealms.game.mechanic.ParticleAPI;
import net.dungeonrealms.game.miscellaneous.ItemBuilder;
import net.dungeonrealms.game.miscellaneous.NBTWrapper;
import net.dungeonrealms.game.player.banks.BankMechanics;
import net.dungeonrealms.game.player.json.JSONMessage;
import net.dungeonrealms.game.profession.Fishing;
import net.dungeonrealms.game.profession.Mining;
import net.dungeonrealms.game.world.entity.type.mounts.EnumMountSkins;
import net.dungeonrealms.game.world.entity.type.mounts.EnumMounts;
import net.dungeonrealms.game.world.entity.type.pet.EnumPets;
import net.dungeonrealms.game.world.entity.type.pet.PetData;
import net.dungeonrealms.game.world.entity.util.BuffUtils;
import net.dungeonrealms.game.world.item.Item;
import net.dungeonrealms.game.world.item.itemgenerator.ItemGenerator;
import net.dungeonrealms.game.world.item.repairing.RepairAPI;
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
                    try {
                        ItemGenerator generator = new ItemGenerator();
                        generator.setType(Item.ItemType.getRandomWeapon());
                        if (args.length == 6) {
                            tier = Integer.parseInt(args[1]);
                            Item.ItemType type = Item.ItemType.getByName(args[2]);
                            Item.ItemRarity rarity = Item.ItemRarity.valueOf(args[3].toUpperCase());

                            if (tier != 0 && type != null && rarity != null) {
                                ItemStack item = new ItemGenerator().setTier(Item.ItemTier.getByTier(tier))
                                        .setType(type).setRarity(rarity).generateItem().getItem();

                                int value = Integer.parseInt(args[5]);
                                NBTTagList modifiersList = new NBTTagList();
                                LinkedHashMap<String, Integer> NBTModifiers = new LinkedHashMap<>();
                                NBTModifiers.put(args[4], value);
                                NBTWrapper wrapper = new NBTWrapper(item);

                                if (wrapper.hasTag("modifiers")) {
                                    modifiersList = (NBTTagList) wrapper.get("modifiers");
                                }
                                for (Map.Entry<String, Integer> entry : NBTModifiers.entrySet()) {
                                    wrapper.set(entry.getKey(), new NBTTagInt(entry.getValue()));

                                    if (!entry.getKey().contains("Max")) {
                                        if (entry.getKey().contains("Min")) {
                                            modifiersList.add(new NBTTagString(entry.getKey().replace("Min", "")));
                                            continue;
                                        }

                                        boolean contains = false;
                                        for (int list = 0; list < modifiersList.size(); list++) {
                                            String key = modifiersList.getString(list);
                                            if (key != null && key.equalsIgnoreCase(entry.getKey())) {
                                                contains = true;
                                                break;
                                            }
                                        }
                                        if (!contains)
                                            modifiersList.add(new NBTTagString(entry.getKey()));
                                    }
                                }

                                wrapper.set("modifiers", modifiersList);
                                item = wrapper.build();
                                player.getInventory().addItem(item);
                            } else {
                                player.sendMessage(ChatColor.RED + "Getting random gear..");
                                player.getInventory().addItem(
                                        new ItemGenerator().setType(Item.ItemType.getRandomArmor()).generateItem().getItem());
                            }
                            return true;
                        }
                        if (args.length >= 2)
                            generator.setTier(Item.ItemTier.getByTier(Integer.parseInt(args[1])));

                        if (args.length >= 3)
                            generator.setType(Item.ItemType.getByName(args[2]));

                        if (args.length >= 4)
                            generator.setRarity(Item.ItemRarity.valueOf(args[3].toUpperCase()));
                        player.getInventory().addItem(generator.generateItem().getItem());

                    } catch (Exception ex) {
                        player.sendMessage("Format: /ad weapon [tier] [type] [rarity]. Leave parameter blank to generate a random value.");
                    }
                    break;
                case "armor":
                    try {
                        ItemGenerator generator = new ItemGenerator();
                        generator.setType(Item.ItemType.getRandomArmor());
                        if (args.length == 6) {
                            tier = Integer.parseInt(args[1]);
                            Item.ItemType type = Item.ItemType.getByName(args[2]);
                            Item.ItemRarity rarity = Item.ItemRarity.valueOf(args[3].toUpperCase());

                            if (tier != 0 && type != null && rarity != null) {
                                ItemStack item = new ItemGenerator().setTier(Item.ItemTier.getByTier(tier))
                                        .setType(type).setRarity(rarity).generateItem().getItem();

                                int value = Integer.parseInt(args[5]);
                                NBTTagList modifiersList = new NBTTagList();
                                LinkedHashMap<String, Integer> NBTModifiers = new LinkedHashMap<>();
                                NBTModifiers.put(args[4], value);
                                NBTWrapper wrapper = new NBTWrapper(item);

                                if (wrapper.hasTag("modifiers")) {
                                    modifiersList = (NBTTagList) wrapper.get("modifiers");
                                }
                                for (Map.Entry<String, Integer> entry : NBTModifiers.entrySet()) {
                                    wrapper.set(entry.getKey(), new NBTTagInt(entry.getValue()));

                                    if (!entry.getKey().contains("Max")) {
                                        if (entry.getKey().contains("Min")) {
                                            modifiersList.add(new NBTTagString(entry.getKey().replace("Min", "")));
                                            continue;
                                        }

                                        boolean contains = false;
                                        for (int list = 0; list < modifiersList.size(); list++) {
                                            String key = modifiersList.getString(list);
                                            if (key != null && key.equalsIgnoreCase(entry.getKey())) {
                                                contains = true;
                                                break;
                                            }
                                        }
                                        if (!contains)
                                            modifiersList.add(new NBTTagString(entry.getKey()));
                                    }
                                }

                                wrapper.set("modifiers", modifiersList);
                                item = wrapper.build();
                                player.getInventory().addItem(item);
                            } else {
                                player.sendMessage(ChatColor.RED + "Getting random gear..");
                                player.getInventory().addItem(
                                        new ItemGenerator().setType(Item.ItemType.getRandomArmor()).generateItem().getItem());
                            }
                            return true;
                        }
                        if (args.length >= 2)
                            generator.setTier(Item.ItemTier.getByTier(Integer.parseInt(args[1])));

                        if (args.length >= 3)
                            generator.setType(Item.ItemType.getByName(args[2]));

                        if (args.length >= 4)
                            generator.setRarity(Item.ItemRarity.valueOf(args[3].toUpperCase()));

                        player.getInventory().addItem(generator.generateItem().getItem());

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
                    player.getInventory().addItem(ItemManager.createPickaxe(tier = args.length >= 2 ? Integer.parseInt(args[1]) : 1));
                    break;
                case "rod":
                    int rodTier = 1;
                    if (args.length == 2)
                        rodTier = Integer.parseInt(args[1]);
                    player.getInventory().addItem(ItemManager.createFishingPole(rodTier));
                    break;
                case "resetbook":
                    player.getInventory().addItem(ItemManager.createRetrainingBook());
                    break;
                case "journal":
                    player.getInventory().addItem(ItemManager.createCharacterJournal(player));
                    break;
                case "scrap":
                    for (int i = 1; i <= 5; i++)
                        player.getInventory().addItem(ItemManager.createArmorScrap(i));
                    break;
                case "potion":
                    for (int i = 1; i <= 5; i++)
                        player.getInventory().addItem(ItemManager.createHealthPotion(i, false, false));
                    for (int i = 1; i <= 5; i++)
                        player.getInventory().addItem(ItemManager.createHealthPotion(i, false, true));
                    break;
                case "food":
                    player.setFoodLevel(1);
                    for (int i = 1; i <= 5; i++)
                        for (Item.ItemRarity ir : Item.ItemRarity.values())
                            player.getInventory().addItem(ItemManager.createHealingFood(i, ir));
                    break;
                case "test":
                    Bukkit.broadcastMessage("Get2" + String.valueOf(RepairAPI.getCustomDurability(player.getEquipment().getItemInMainHand())));
                    break;
                case "orb":
                case "orb_of_alteration":
                    player.getInventory().addItem(ItemManager.createOrbofAlteration());
                    break;
                case "orb_of_peace":
                    player.getInventory().addItem(ItemManager.createOrbofPeace(true));
                    break;
                case "orb_of_flight":
                    player.getInventory().addItem(ItemManager.createOrbofFlight(true));
                    break;
                case "global_messenger":
                    player.getInventory().addItem(ItemManager.createGlobalMessenger());
                    break;
                case "buff":
                    BuffUtils.spawnBuff(player.getUniqueId());
                    break;
                case "armorench":
                case "armorenchant":
                    tier = Integer.parseInt(args[1]);
                    player.getInventory().addItem(ItemManager.createArmorEnchant(tier));
                    break;
                case "weaponench":
                case "enchant":
                case "weaponenchant":
                    tier = Integer.parseInt(args[1]);
                    player.getInventory().addItem(ItemManager.createWeaponEnchant(tier));
                    break;
                case "prot":
                case "protect":
                case "scroll":
                case "protectscroll":
                    tier = Integer.parseInt(args[1]);
                    player.getInventory().addItem(ItemManager.createProtectScroll(tier));
                    break;
                case "rodenchant":
                    tier = Integer.parseInt(args[1]);
                    String enchantTypeString = args[2];
                    if (args.length == 4) {
                        int percent = Integer.parseInt(args[3]);
                        Fishing.FishingRodEnchant enchantType = Fishing.FishingRodEnchant.getEnchant(enchantTypeString);
                        player.getInventory().addItem(Fishing.getEnchant(tier, enchantType, percent));

                    } else {
                        Fishing.FishingRodEnchant enchantType = Fishing.FishingRodEnchant.getEnchant(enchantTypeString);
                        player.getInventory().addItem(Fishing.getEnchant(tier, enchantType));
                    }
                    break;
                case "pickenchant":
                    tier = Integer.parseInt(args[1]);
                    enchantTypeString = args[2];
                    if (args.length == 4) {
                        int percent = Integer.parseInt(args[3]);
                        Mining.EnumMiningEnchant enchantType = Mining.EnumMiningEnchant.getEnchant(enchantTypeString);
                        player.getInventory().addItem(Mining.getEnchant(tier, enchantType, percent));

                    } else {
                        Mining.EnumMiningEnchant enchantType = Mining.EnumMiningEnchant.getEnchant(enchantTypeString);
                        player.getInventory().addItem(Mining.getEnchant(tier, enchantType));
                    }
                    break;
                case "realmchest":
                    player.getInventory().addItem(ItemManager.createRealmChest());
                    break;
                case "pouch":
                    tier = Integer.parseInt(args[1]);
                    player.getInventory().addItem(BankMechanics.getInstance().createGemPouch(tier, 0));
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
                    player.getInventory().addItem(BankMechanics.createBankNote(quantity, player));
                    player.sendMessage(ChatColor.GREEN + "Successfully created a bank note worth " + NumberFormat.getIntegerInstance().format(quantity) + " gems.");
                    break;
                case "displayitem":
                    player.getInventory().addItem(new ItemBuilder().setItem(new ItemStack(Material.IRON_BARDING), ChatColor.AQUA + EnumMounts.TIER2_HORSE.getDisplayName(), new String[]{
                            ChatColor.RED + "Speed 140%",
                            ChatColor.RED + "Jump 110%",
                            ChatColor.GRAY.toString() + ChatColor.ITALIC + "A horse fit for a humble squire.",
                            ChatColor.RED.toString() + ChatColor.BOLD + "REQ: " + ChatColor.RESET + ChatColor.GREEN + EnumMounts.TIER1_HORSE.getDisplayName(),
                            ChatColor.GREEN + "Price: " + ChatColor.WHITE + "7000g",
                            ChatColor.GRAY + "Display Item"}).setNBTString("mountType", EnumMounts.TIER2_HORSE.getRawName()).setNBTInt("mountCost", 7000).build());
                    break;
                case "untradable":
                    player.getInventory().addItem(GameAPI.makeItemUntradeable(new ItemBuilder().setItem(new ItemStack(Material.IRON_BARDING), ChatColor.AQUA + EnumMounts.TIER2_HORSE.getDisplayName(), new String[]{
                            ChatColor.RED + "Speed 140%",
                            ChatColor.RED + "Jump 110%",
                            ChatColor.GRAY.toString() + ChatColor.ITALIC + "A horse fit for a humble squire.",
                            ChatColor.RED.toString() + ChatColor.BOLD + "REQ: " + ChatColor.RESET + ChatColor.GREEN + EnumMounts.TIER1_HORSE.getDisplayName(),
                            ChatColor.GREEN + "Price: " + ChatColor.WHITE + "7000g"}).setNBTString("mountType", EnumMounts.TIER2_HORSE.getRawName()).setNBTInt("mountCost", 7000).build()));
                    break;
                case "teleport":
                case "teleports":
                    if (args.length == 1) {
                        for (TeleportLocation tl : TeleportLocation.values())
                            player.getInventory().addItem(ItemManager.createTeleportBook(tl));
                        player.sendMessage(ChatColor.GREEN + "Spawned all teleport books.");
                    } else if (args.length >= 2) {
                        if (args[1].equalsIgnoreCase("random")) {
                            player.getInventory().addItem(ItemManager.createRandomTeleportBook());
                            player.sendMessage(ChatColor.GREEN + "Spawned random teleport book.");
                        } else {
                            TeleportLocation tl = TeleportLocation.valueOf(args[1].toUpperCase());
                            if (tl == null) {
                                player.sendMessage(ChatColor.RED + "Location not found.");
                                return true;
                            }
                            player.getInventory().addItem(ItemManager.createTeleportBook(tl));
                            player.sendMessage(ChatColor.GREEN + "Spawned " + tl.getDisplayName() + " teleport book.");
                        }
                    }
                    break;
                case "everything":
                    // This is a special command for giving YouTubers "everything" & for testing.
                    // Therefore, we want to ensure that the player is an authorized developer.
                    if (!Rank.isDev(player)) {
                        player.sendMessage(ChatColor.RED + "This command can only be executed by a developer.");
                        return false;
                    }

                    // If we don't specify a 2nd argument, we assume we're doing it to ourselves.
                    Player currentProfile = player;
                    if (args.length >= 2) {
                        if (Bukkit.getPlayer(args[1]) != null && Bukkit.getPlayer(args[1]).getDisplayName().equalsIgnoreCase(args[1])) {
                            currentProfile = Bukkit.getPlayer(args[1]);
                        } else {
                            player.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + ChatColor.UNDERLINE + args[1] + ChatColor.RED + " is offline or on another shard.");
                            return false;
                        }
                    }

                    PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(currentProfile);

                    if (wrapper == null) {
                        player.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + ChatColor.UNDERLINE + args[1] + ChatColor.RED + " is offline or not on this shard.");
                        return false;
                    }


                    // Add all pets to the player.
                    Map<EnumPets, PetData> playerPets = wrapper.getPetsUnlocked();
                    for (EnumPets pets : EnumPets.values()) {
                        if (pets == EnumPets.BABY_HORSE) {
                            continue;
                        }
                        if (!playerPets.isEmpty()) {
                            if (playerPets.containsKey(pets)) {
                                continue;
                            }
                        }
                        wrapper.getPetsUnlocked().put(pets, new PetData(null));
                        player.sendMessage(ChatColor.GREEN + "Added the " + ChatColor.BOLD + ChatColor.UNDERLINE + Utils.ucfirst(pets.getRawName()) + ChatColor.GREEN + " pet to " + ChatColor.BOLD + ChatColor.UNDERLINE + currentProfile.getDisplayName() + ChatColor.GREEN + ".");
                    }

                    wrapper.setActivePet(EnumPets.BAT.getRawName());

                    // Add all trails to the player.
                    HashSet<String> playerTrails = wrapper.getTrails();
                    for (ParticleAPI.ParticleEffect trails : ParticleAPI.ParticleEffect.values()) {
                        if (!playerTrails.isEmpty()) {
                            if (playerTrails.contains(trails.getRawName().toUpperCase())) {
                                continue;
                            }
                        }
                        wrapper.getTrails().add(trails.getRawName());
                        player.sendMessage(ChatColor.GREEN + "Added the " + ChatColor.BOLD + ChatColor.UNDERLINE + Utils.ucfirst(trails.getRawName()) + ChatColor.GREEN + " trail to " + ChatColor.BOLD + ChatColor.UNDERLINE + currentProfile.getDisplayName() + ChatColor.GREEN + ".");
                    }

                    wrapper.setActiveTrail(ParticleAPI.ParticleEffect.CRIT.getRawName());


                    // Add all mount skins to the player.
                    HashSet<String> playerMountSkins = wrapper.getMountSkins();
                    for (EnumMountSkins mountSkins : EnumMountSkins.values()) {
                        if (!playerMountSkins.isEmpty()) {
                            if (playerMountSkins.contains(mountSkins.getRawName().toUpperCase())) {
                                continue;
                            }
                        }

                        wrapper.getMountSkins().add(mountSkins.getRawName());
                        player.sendMessage(ChatColor.GREEN + "Added the " + ChatColor.BOLD + ChatColor.UNDERLINE + Utils.ucfirst(mountSkins.getRawName()) + ChatColor.GREEN + " mount skin to " + ChatColor.BOLD + ChatColor.UNDERLINE + currentProfile.getDisplayName() + ChatColor.GREEN + ".");
                    }


                    wrapper.setActiveMountSkin(EnumMountSkins.SKELETON_HORSE.getRawName());

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

                        switch (args[1].toLowerCase()) {
                            case "loot":
                                player.getInventory().addItem(ItemManager.createLootBuff(buffDuration, buffBonus));
                                break;
                            case "profession":
                                player.getInventory().addItem(ItemManager.createProfessionBuff(buffDuration, buffBonus));
                                break;
                            case "level":
                                player.getInventory().addItem(ItemManager.createLevelBuff(buffDuration, buffBonus));
                                break;
                        }
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