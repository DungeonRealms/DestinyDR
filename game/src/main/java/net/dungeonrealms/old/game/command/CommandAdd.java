package net.dungeonrealms.old.game.command;

import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.common.game.database.DatabaseAPI;
import net.dungeonrealms.common.game.database.data.EnumData;
import net.dungeonrealms.common.game.database.data.EnumOperators;
import net.dungeonrealms.common.game.database.player.rank.Rank;
import net.dungeonrealms.old.game.party.PartyMechanics;
import net.dungeonrealms.old.game.donation.DonationEffects;
import net.dungeonrealms.old.game.mastery.GamePlayer;
import net.dungeonrealms.old.game.mastery.Utils;
import net.dungeonrealms.old.game.mechanic.ItemManager;
import net.dungeonrealms.old.game.mechanic.ParticleAPI;
import net.dungeonrealms.old.game.miscellaneous.ItemBuilder;
import net.dungeonrealms.old.game.player.banks.BankMechanics;
import net.dungeonrealms.old.game.player.json.JSONMessage;
import net.dungeonrealms.old.game.profession.Fishing;
import net.dungeonrealms.old.game.profession.Mining;
import net.dungeonrealms.old.game.world.entity.type.mounts.EnumMountSkins;
import net.dungeonrealms.old.game.world.entity.type.mounts.EnumMounts;
import net.dungeonrealms.old.game.world.entity.type.pet.EnumPets;
import net.dungeonrealms.old.game.world.entity.util.BuffUtils;
import net.dungeonrealms.old.game.world.item.Item;
import net.dungeonrealms.old.game.world.item.itemgenerator.ItemGenerator;
import net.dungeonrealms.old.game.world.item.repairing.RepairAPI;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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
        if (args.length > 0) {
            int tier;
            Item.ItemType type;
            Item.ItemRarity rarity;
            switch (args[0]) {
                case "pcheck":
                    player.sendMessage(ChatColor.GREEN + "There are " + String.valueOf(PartyMechanics.getInstance()._parties.size()));
                    break;
                case "check":
//                    player.sendMessage("YOUR REALM EXIST? " + String.valueOf(RealmInstance.getInstance().doesRemoteRealmExist(player.getUniqueId().toString())));
                    break;
                case "uuid":
                    player.sendMessage(Bukkit.getPlayer(GameAPI.getUUIDFromName(player.getName())).getDisplayName());
                    break;
                case "name":
                    player.sendMessage(GameAPI.getNameFromUUID(player.getUniqueId()));
                    break;
                case "uploadrealm":
//                    new RealmManager().uploadRealm(player.getUniqueId());
                    break;
                case "realm":
//                    RealmInstance.getInstance().openRealmPortal(player);
                    //new RealmManager().downloadRealm(player.getUniqueId());
                    break;
                case "weapon":
                    try {
                        if (args.length == 2) {
                            tier = Integer.parseInt(args[1]);
                            player.getInventory().addItem(new ItemGenerator().setTier(Item.ItemTier.getByTier(tier))
                                    .setType(Item.ItemType.getRandomWeapon()).generateItem().getItem());
                        } else if (args.length == 3) {
                            tier = Integer.parseInt(args[1]);
                            type = Item.ItemType.getByName(args[2]);
                            player.getInventory().addItem(new ItemGenerator().setTier(Item.ItemTier.getByTier(tier))
                                    .setType(type).generateItem().getItem());
                        } else if (args.length == 4) {
                            tier = Integer.parseInt(args[1]);
                            type = Item.ItemType.getByName(args[2]);
                            rarity = Item.ItemRarity.getByName(args[3]);
                            player.getInventory().addItem(new ItemGenerator().setTier(Item.ItemTier.getByTier(tier))
                                    .setType(type).setRarity(rarity).generateItem().getItem());
                        } else {
                            player.getInventory().addItem(
                                    new ItemGenerator().setType(Item.ItemType.getRandomWeapon()).generateItem().getItem());
                        }
                    } catch (NullPointerException ex) {
                        player.sendMessage("Format: /ad weapon [tier] [type] [rarity]. Leave parameter blank to generate a random value.");
                    }
                    break;
                case "armor":
                    try {
                        if (args.length == 2) {
                            tier = Integer.parseInt(args[1]);
                            player.getInventory().addItem(new ItemGenerator().setTier(Item.ItemTier.getByTier(tier))
                                    .setType(Item.ItemType.getRandomArmor()).generateItem().getItem());
                        } else if (args.length == 3) {
                            tier = Integer.parseInt(args[1]);
                            type = Item.ItemType.getByName(args[2]);
                            player.getInventory().addItem(new ItemGenerator().setTier(Item.ItemTier.getByTier(tier))
                                    .setType(type).generateItem().getItem());
                        } else if (args.length == 4) {
                            tier = Integer.parseInt(args[1]);
                            type = Item.ItemType.getByName(args[2]);
                            rarity = Item.ItemRarity.getByName(args[3]);
                            player.getInventory().addItem(new ItemGenerator().setTier(Item.ItemTier.getByTier(tier))
                                    .setType(type).setRarity(rarity).generateItem().getItem());
                        } else {
                            player.getInventory().addItem(
                                    new ItemGenerator().setType(Item.ItemType.getRandomArmor()).generateItem().getItem());
                        }
                    } catch (NullPointerException ex) {
                        player.sendMessage("Format: /ad armor [tier] [type] [rarity]. Leave parameter blank to generate a random value.");
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
                    tier = 1;
                    if (args.length == 2)
                        tier = Integer.parseInt(args[1]);
                    player.getInventory().addItem(ItemManager.createPickaxe(tier));
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
                    player.getInventory().addItem(ItemManager.createArmorScrap(1));
                    player.getInventory().addItem(ItemManager.createArmorScrap(2));
                    player.getInventory().addItem(ItemManager.createArmorScrap(3));
                    player.getInventory().addItem(ItemManager.createArmorScrap(4));
                    player.getInventory().addItem(ItemManager.createArmorScrap(5));
                    break;
                case "potion":
                    player.getInventory().addItem(ItemManager.createHealthPotion(1, false, false));
                    player.getInventory().addItem(ItemManager.createHealthPotion(2, false, false));
                    player.getInventory().addItem(ItemManager.createHealthPotion(3, false, false));
                    player.getInventory().addItem(ItemManager.createHealthPotion(4, false, false));
                    player.getInventory().addItem(ItemManager.createHealthPotion(5, false, false));
                    player.getInventory().addItem(ItemManager.createHealthPotion(1, false, true));
                    player.getInventory().addItem(ItemManager.createHealthPotion(2, false, true));
                    player.getInventory().addItem(ItemManager.createHealthPotion(3, false, true));
                    player.getInventory().addItem(ItemManager.createHealthPotion(4, false, true));
                    player.getInventory().addItem(ItemManager.createHealthPotion(5, false, true));
                    break;
                case "food":
                    player.setFoodLevel(1);
                    player.getInventory().addItem(ItemManager.createHealingFood(1, Item.ItemRarity.COMMON));
                    player.getInventory().addItem(ItemManager.createHealingFood(1, Item.ItemRarity.RARE));
                    player.getInventory().addItem(ItemManager.createHealingFood(1, Item.ItemRarity.UNIQUE));
                    player.getInventory().addItem(ItemManager.createHealingFood(2, Item.ItemRarity.COMMON));
                    player.getInventory().addItem(ItemManager.createHealingFood(2, Item.ItemRarity.RARE));
                    player.getInventory().addItem(ItemManager.createHealingFood(2, Item.ItemRarity.UNIQUE));
                    player.getInventory().addItem(ItemManager.createHealingFood(3, Item.ItemRarity.COMMON));
                    player.getInventory().addItem(ItemManager.createHealingFood(3, Item.ItemRarity.RARE));
                    player.getInventory().addItem(ItemManager.createHealingFood(3, Item.ItemRarity.UNIQUE));
                    player.getInventory().addItem(ItemManager.createHealingFood(4, Item.ItemRarity.COMMON));
                    player.getInventory().addItem(ItemManager.createHealingFood(4, Item.ItemRarity.RARE));
                    player.getInventory().addItem(ItemManager.createHealingFood(4, Item.ItemRarity.UNIQUE));
                    player.getInventory().addItem(ItemManager.createHealingFood(5, Item.ItemRarity.COMMON));
                    player.getInventory().addItem(ItemManager.createHealingFood(5, Item.ItemRarity.RARE));
                    player.getInventory().addItem(ItemManager.createHealingFood(5, Item.ItemRarity.UNIQUE));
                    break;
                case "skelframe":
                    Bukkit.broadcastMessage("Get2" + String.valueOf(RepairAPI.getCustomDurability(player.getEquipment().getItemInMainHand())));
                    break;
                case "orb":
                case "orb_of_alteration":
                    player.getInventory().addItem(ItemManager.createOrbofAlteration());
                    break;
                case "orb_of_peace":
                    player.getInventory().addItem(ItemManager.createOrbofPeace());
                    break;
                case "orb_of_flight":
                    player.getInventory().addItem(ItemManager.createOrbofFlight());
                    break;
                case "global_messenger":
                    player.getInventory().addItem(ItemManager.createGlobalMessenger());
                    break;
                case "buff":
                    BuffUtils.spawnBuff(player.getUniqueId());
                    break;
                case "armorenchant":
                    tier = Integer.parseInt(args[1]);
                    player.getInventory().addItem(ItemManager.createArmorEnchant(tier));
                    break;
                case "weaponenchant":
                    tier = Integer.parseInt(args[1]);
                    player.getInventory().addItem(ItemManager.createWeaponEnchant(tier));
                    break;
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
                    ItemStack realmChest = ItemManager.createItem(Material.CHEST, ChatColor.GREEN + "Realm Chest", new String[]{ChatColor.GRAY + "This chest can only be placed in realms."});
                    player.getInventory().addItem(realmChest);
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
                    normal.addURL(ChatColor.AQUA.toString() + ChatColor.BOLD + ChatColor.UNDERLINE + "HERE", ChatColor.AQUA, "http://minecraftservers.org/vote/174212");
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
                    player.getInventory().addItem(BankMechanics.createBankNote(quantity));
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
                    String[] teleports = new String[]{"Cyrennica", "Harrison_Field", "Dark_Oak", "Trollsbane", "Tripoli", "Gloomy_Hollows", "Crestguard", "Deadpeaks"};
                    if (args.length == 1) {
                        for (String tp : teleports) {
                            player.getInventory().addItem(ItemManager.createTeleportBook(tp));
                        }
                        player.sendMessage(ChatColor.GREEN + "Spawned all teleport books.");
                    } else if (args.length >= 2) {
                        if (args[1].equalsIgnoreCase("random")) {
                            player.getInventory().addItem(ItemManager.createRandomTeleportBook());
                            player.sendMessage(ChatColor.GREEN + "Spawned random teleport book.");
                        } else {
                            for (String tp : teleports) {
                                if (tp.equalsIgnoreCase(args[1])) {
                                    player.getInventory().addItem(ItemManager.createTeleportBook(tp));
                                    player.sendMessage(ChatColor.GREEN + "Spawned " + tp + " teleport book.");
                                    return true;
                                }
                            }
                            player.sendMessage(ChatColor.RED + "The requested location (" + args[1] + ") is not a valid teleport location.");
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
                            if (playerPets.contains(pets.getRawName().toUpperCase())) {
                                continue;
                            }
                            boolean hasPet = false;
                            for (String playerPet : playerPets) {
                                if (playerPet.contains("@") && playerPet.split("@")[0].equals(pets.getRawName())) {
                                    hasPet = true;
                                    break;
                                }
                            }
                            if (hasPet) continue;
                        }
                        DatabaseAPI.getInstance().update(currentProfile.getUniqueId(), EnumOperators.$PUSH, EnumData.PETS, pets.getRawName(), true);
                        player.sendMessage(ChatColor.GREEN + "Added the " + ChatColor.BOLD + ChatColor.UNDERLINE + Utils.ucfirst(pets.getRawName()) + ChatColor.GREEN + " pet to " + ChatColor.BOLD + ChatColor.UNDERLINE + currentProfile.getDisplayName() + ChatColor.GREEN + ".");
                    }

                    DatabaseAPI.getInstance().update(currentProfile.getUniqueId(), EnumOperators.$SET, EnumData.ACTIVE_PET, EnumPets.BAT.getRawName(), true);

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
                            if (playerMountSkins.contains(mountSkins.getRawName().toUpperCase())) {
                                continue;
                            }
                        }
                        DatabaseAPI.getInstance().update(currentProfile.getUniqueId(), EnumOperators.$PUSH, EnumData.MOUNT_SKINS, mountSkins.getRawName(), true);
                        player.sendMessage(ChatColor.GREEN + "Added the " + ChatColor.BOLD + ChatColor.UNDERLINE + Utils.ucfirst(mountSkins.getRawName()) + ChatColor.GREEN + " mount skin to " + ChatColor.BOLD + ChatColor.UNDERLINE + currentProfile.getDisplayName() + ChatColor.GREEN + ".");
                    }

                    DatabaseAPI.getInstance().update(currentProfile.getUniqueId(), EnumOperators.$SET, EnumData.ACTIVE_MOUNT_SKIN, EnumMountSkins.SKELETON_HORSE.getRawName(), true);

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
