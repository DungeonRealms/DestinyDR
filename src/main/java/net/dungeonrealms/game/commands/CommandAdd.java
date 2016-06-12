package net.dungeonrealms.game.commands;

import net.dungeonrealms.API;
import net.dungeonrealms.game.commands.generic.BasicCommand;
import net.dungeonrealms.game.donate.DonationEffects;
import net.dungeonrealms.game.mastery.GamePlayer;
import net.dungeonrealms.game.mastery.RealmManager;
import net.dungeonrealms.game.mechanics.ItemManager;
import net.dungeonrealms.game.mechanics.ParticleAPI;
import net.dungeonrealms.game.player.banks.BankMechanics;
import net.dungeonrealms.game.player.json.JSONMessage;
import net.dungeonrealms.game.player.rank.Rank;
import net.dungeonrealms.game.world.items.EnumItem;
import net.dungeonrealms.game.world.items.Item;
import net.dungeonrealms.game.world.items.Item.ItemRarity;
import net.dungeonrealms.game.world.items.Item.ItemTier;
import net.dungeonrealms.game.world.items.Item.ItemType;
import net.dungeonrealms.game.world.items.itemgenerator.ItemGenerator;
import net.dungeonrealms.game.world.items.repairing.RepairAPI;
import net.dungeonrealms.game.world.party.Affair;
import net.dungeonrealms.game.world.realms.Instance;
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
import java.util.Random;

/**
 * Created by Nick on 9/17/2015.
 */
public class CommandAdd extends BasicCommand {

    public CommandAdd(String command, String usage, String description) {
        super(command, usage, description);
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
            ItemType type;
            ItemRarity rarity;
            switch (args[0]) {
                case "pcheck":
                    player.sendMessage(ChatColor.GREEN + "There are " + String.valueOf(Affair.getInstance()._parties.size()));
                    break;
                case "check":
                    player.sendMessage("YOUR REALM EXIST? " + String.valueOf(Instance.getInstance().doesRemoteRealmExist(player.getUniqueId().toString())));
                    break;
                case "uuid":
                    player.sendMessage(Bukkit.getPlayer(API.getUUIDFromName(player.getName())).getDisplayName());
                    break;
                case "name":
                    player.sendMessage(API.getNameFromUUID(player.getUniqueId()));
                    break;
                case "uploadrealm":
                    new RealmManager().uploadRealm(player.getUniqueId());
                    break;
                case "realm":
                    Instance.getInstance().openRealm(player);
                    //new RealmManager().downloadRealm(player.getUniqueId());
                    break;
                case "weapon":
                    try {
                        if (args.length == 2) {
                            tier = Integer.parseInt(args[1]);
                            player.getInventory().addItem(new ItemGenerator().setTier(ItemTier.getByTier(tier))
                                    .setType(ItemType.getRandomWeapon()).generateItem().getItem());
                        }
                        else if (args.length == 3) {
                            tier = Integer.parseInt(args[1]);
                            type = ItemType.getByName(args[2]);
                            player.getInventory().addItem(new ItemGenerator().setTier(ItemTier.getByTier(tier))
                                    .setType(type).generateItem().getItem());
                        }
                        else if (args.length == 4) {
                            tier = Integer.parseInt(args[1]);
                            type = ItemType.getByName(args[2]);
                            rarity = ItemRarity.getByName(args[3]);
                            player.getInventory().addItem(new ItemGenerator().setTier(ItemTier.getByTier(tier))
                                    .setType(type).setRarity(rarity).generateItem().getItem());
                        }
                        else {
                            player.getInventory().addItem(
                                    new ItemGenerator().setType(ItemType.getRandomWeapon()).generateItem().getItem());
                        }
                    }
                    catch (NullPointerException ex) {
                        player.sendMessage("Format: /ad weapon [tier] [type] [rarity]. Leave parameter blank to generate a random value.");
                    }
                    break;
                case "armor":
                    try {
                        if (args.length == 2) {
                            tier = Integer.parseInt(args[1]);
                            player.getInventory().addItem(new ItemGenerator().setTier(ItemTier.getByTier(tier))
                                    .setType(ItemType.getRandomArmor()).generateItem().getItem());
                        }
                        else if (args.length == 3) {
                            tier = Integer.parseInt(args[1]);
                            type = ItemType.getByName(args[2]);
                            player.getInventory().addItem(new ItemGenerator().setTier(ItemTier.getByTier(tier))
                                    .setType(type).generateItem().getItem());
                        }
                        else if (args.length == 4) {
                            tier = Integer.parseInt(args[1]);
                            type = ItemType.getByName(args[2]);
                            rarity = ItemRarity.getByName(args[3]);
                            player.getInventory().addItem(new ItemGenerator().setTier(ItemTier.getByTier(tier))
                                    .setType(type).setRarity(rarity).generateItem().getItem());
                        }
                        else {
                            player.getInventory().addItem(
                                    new ItemGenerator().setType(ItemType.getRandomArmor()).generateItem().getItem());
                        }
                    }
                    catch (NullPointerException ex) {
                        player.sendMessage("Format: /ad armor [tier] [type] [rarity]. Leave parameter blank to generate a random value.");
                    }
                    break;
                case "customitem":
                    String name = args[1];
                    player.getInventory().addItem(ItemGenerator.getNamedItem(name));
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
                    player.getInventory().addItem(ItemManager.createItem(EnumItem.RetrainingBook));
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
                case "test":
                    Bukkit.broadcastMessage("Get2" + String.valueOf(RepairAPI.getCustomDurability(player.getEquipment().getItemInMainHand())));
                    break;
                case "orb":
                    player.getInventory().addItem(ItemManager.createOrbofAlteration());
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
                case "pouch":
                    tier = Integer.parseInt(args[1]);
                    player.getInventory().addItem(BankMechanics.getInstance().createGemPouch(tier, 0));
                    break;
                case "votemessage":
                    if (API.getGamePlayer(player) == null) {
                        break;
                    }
                    GamePlayer gamePlayer = API.getGamePlayer(player);
                    int expToLevel = gamePlayer.getEXPNeeded(gamePlayer.getLevel());
                    int expToGive = expToLevel / 20;
                    expToGive += 100;
                    gamePlayer.addExperience(expToGive, false);
                    final JSONMessage normal = new JSONMessage(ChatColor.AQUA + player.getName() + ChatColor.RESET + ChatColor.GRAY + " voted for 15 ECASH & 5% EXP @ vote ", ChatColor.WHITE);
                    normal.addURL(ChatColor.AQUA.toString() + ChatColor.BOLD + ChatColor.UNDERLINE + "HERE", ChatColor.AQUA, "http://minecraftservers.org/server/298658");
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
                case "teleport":
                case "teleports":
                    String[] teleports = new String[] { "Cyrennica", "Harrison_Field", "Dark_Oak", "Trollsbane", "Tripoli", "Gloomy_Hollows", "Crestguard", "Deadpeaks" };
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
                default:
                    player.sendMessage(ChatColor.RED + "Invalid usage! '" + args[0] + "' is not a valid variable.");
                    break;
            }
        }

        return true;
    }
}
