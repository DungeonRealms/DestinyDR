package net.dungeonrealms.game.world.entities.types.monsters;

import net.dungeonrealms.API;
import net.dungeonrealms.game.donate.DonationEffects;
import net.dungeonrealms.game.enchantments.EnchantmentAPI;
import net.dungeonrealms.game.mastery.GamePlayer;
import net.dungeonrealms.game.mechanics.ItemManager;
import net.dungeonrealms.game.miscellaneous.RandomHelper;
import net.dungeonrealms.game.mongo.DatabaseAPI;
import net.dungeonrealms.game.mongo.EnumData;
import net.dungeonrealms.game.player.banks.BankMechanics;
import net.dungeonrealms.game.world.anticheat.AntiCheat;
import net.dungeonrealms.game.world.items.Item;
import net.dungeonrealms.game.world.items.itemgenerator.ItemGenerator;
import net.dungeonrealms.game.world.items.repairing.RepairAPI;
import net.minecraft.server.v1_9_R2.World;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_9_R2.CraftWorld;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

/**
 * Created by Chase on Oct 21, 2015
 */
public interface DRMonster {

    void onMonsterAttack(Player p);

    void onMonsterDeath(Player killer);

    EnumMonster getEnum();

    Map<String, Integer[]> getAttributes();

    default void checkItemDrop(int tier, EnumMonster monster, Entity ent, Player killer) {
        if (ent.getWorld().getName().contains("DUNGEON")) {
            //No normal drops in dungeons.
            return;
        }
        if (ent.hasMetadata("boss")) {
            //Boss will handle this.
            return;
        }
        if (ent.hasMetadata("combatlog")) {
            //combat log npcs have special drop mechanics
            return;
        }
        Random random = new Random();
        GamePlayer gp = API.getGamePlayer(killer);
        boolean toggleDebug = (Boolean) DatabaseAPI.getInstance().getData(EnumData.TOGGLE_DEBUG, killer.getUniqueId());
        double gold_drop_multiplier = (gp.getRangedAttributeVal(Item.ArmorAttributeType.GEM_FIND)[1] + 100.) / 100.;
        int killerItemFind = gp.getRangedAttributeVal(Item.ArmorAttributeType.ITEM_FIND)[1];
        Location loc = ent.getLocation();
        World world = ((CraftWorld) loc.getWorld()).getHandle();

        int gemRoll = random.nextInt(100);
        int gemChance = 0;
        int chance = 0;
        switch (tier) {
            case 1:
                gemChance = 50;
                chance = ent.hasMetadata("elite") ? 1000 : 120; // 100%, 12%
                break;
            case 2:
                gemChance = 40;
                chance = ent.hasMetadata("elite") ? 500 : 50; // 50%, 5%
                break;
            case 3:
                gemChance = 30;
                chance = ent.hasMetadata("elite") ? 100 : 30; // 10%, 3%
                break;
            case 4:
                gemChance = 20;
                chance = ent.hasMetadata("elite") ? 20 : 5; // 20%, 1%
                break;
            case 5:
                gemChance = 35;
                chance = ent.hasMetadata("elite") ? 10 : 2; // 1%, 0.2%
                break;
        }
        if (DonationEffects.getInstance().isLootBuffActive()) {
            chance *= 1.2;
        }

        if (gemRoll < (gemChance * gold_drop_multiplier)) {
            if (gemRoll >= gemChance) {
                if (toggleDebug) {
                    killer.sendMessage(ChatColor.GREEN + "Your " + gp.getRangedAttributeVal(Item.ArmorAttributeType.GEM_FIND)[1] + "% Gem Find has resulted in a drop.");
                }
            }
            double gem_drop_amount = 0;
            double drop_multiplier = 1;
            // Elite = 1.5x money chance / item chance.
            if (ent.hasMetadata("elite")) {
                drop_multiplier = 1.5;
            }

            switch (tier) {
                case 1:
                    gem_drop_amount = (random.nextInt(3 - 1) + 1) * gold_drop_multiplier;
                    break;
                case 2:
                    gem_drop_amount = (random.nextInt(12 - 2) + 2) * gold_drop_multiplier;
                    break;
                case 3:
                    gem_drop_amount = (random.nextInt(30 - 10) + 10) * gold_drop_multiplier;
                    break;
                case 4:
                    gem_drop_amount = (random.nextInt(50 - 20) + 20) * gold_drop_multiplier;
                    break;
                case 5:
                    gem_drop_amount = (random.nextInt(200 - 75) + 75) * gold_drop_multiplier;
                    break;
            }

            ItemStack item = BankMechanics.gem.clone();
            item.setAmount((int) (gem_drop_amount * drop_multiplier));
            if (item.getAmount() < 1) {
                item.setAmount(1);
            }
            world.getWorld().dropItem(loc.add(0, 1, 0), item);
        }

        int armorRoll = random.nextInt(1000);
        List<ItemStack> toDrop = new ArrayList<>();
        for (ItemStack stack : ((LivingEntity) ent).getEquipment().getArmorContents()) {
            if (stack == null || stack.getType() == Material.AIR || stack.getType() == Material.SKULL || stack.getType() == Material.SKULL_ITEM) {
                continue;
            }
            toDrop.add(stack);
        }
        if (!ent.hasMetadata("elite")) {
            ItemStack helmet = new ItemGenerator().setTier(Item.ItemTier.getByTier(tier)).setType(Item.ItemType.HELMET).setRarity(API.getItemRarity(false)).generateItem().getItem();
            AntiCheat.getInstance().applyAntiDupe(helmet);
            toDrop.add(helmet);
        }
        ItemStack weapon = ((LivingEntity) ent).getEquipment().getItemInMainHand();
        if (weapon != null && weapon.getType() != Material.AIR) {
            toDrop.add(weapon);
        }
        //Random drop choice, as opposed dropping in the same order (boots>legs>chest>head)
        Collections.shuffle(toDrop);
        if (armorRoll < chance + (chance * killerItemFind / 100)) {
            if (armorRoll >= chance) {
                if (toggleDebug) {
                    killer.sendMessage(ChatColor.GREEN + "Your " + killerItemFind + "% Item Find has resulted in a drop.");
                }
            }
            ItemStack drop = toDrop.get(random.nextInt(toDrop.size()));
            RepairAPI.setCustomItemDurability(drop, RandomHelper.getRandomNumberBetween(200, 1500));
            if (drop != null && drop.getType() != Material.AIR)
                EnchantmentAPI.removeGlow(drop);
            world.getWorld().dropItem(loc.add(0, 1, 0), drop);
        }
        int scrollDrop = random.nextInt(100);
        int scrollDropChance;
        switch (tier) {
            case 1:
            case 2:
            case 3:
                scrollDropChance = 2;
                break;
            case 4:
            case 5:
                scrollDropChance = 1;
                break;
            default:
                scrollDropChance = 1;
                break;
        }
        if (scrollDropChance >= scrollDrop) {
            ItemStack teleport = null;
            switch (tier) {
                case 1:
                    if (random.nextInt(2) == 0) {
                        teleport = ItemManager.createTeleportBook("Cyrennica");
                    } else {
                        teleport = ItemManager.createTeleportBook("Harrison_Field");
                    }
                    break;
                case 2:
                    int type = random.nextInt(5);
                    switch (type) {
                        case 0:
                            teleport = ItemManager.createTeleportBook("Cyrennica");
                            break;
                        case 1:
                            teleport = ItemManager.createTeleportBook("Harrison_Field");
                            break;
                        case 2:
                            teleport = ItemManager.createTeleportBook("Dark_Oak");
                            break;
                        case 3:
                            teleport = ItemManager.createTeleportBook("Trollsbane");
                            break;
                        case 4:
                            teleport = ItemManager.createTeleportBook("Tripoli");
                            break;
                    }
                    break;
                case 3:
                    type = random.nextInt(5);
                    switch (type) {
                        case 0:
                            teleport = ItemManager.createTeleportBook("Cyrennica");
                            break;
                        case 1:
                            teleport = ItemManager.createTeleportBook("Dark_Oak");
                            break;
                        case 2:
                            teleport = ItemManager.createTeleportBook("Trollsbane");
                            break;
                        case 3:
                            teleport = ItemManager.createTeleportBook("Gloomy_Hollows");
                            break;
                        case 4:
                            teleport = ItemManager.createTeleportBook("Crestguard");
                            break;
                    }
                    break;
                case 4:
                    if (random.nextInt(2) == 0) {
                        teleport = ItemManager.createTeleportBook("Deadpeaks");
                    } else {
                        teleport = ItemManager.createTeleportBook("Gloomy_Hollows");
                    }
                    break;
                case 5:
                    if (random.nextInt(2) == 0) {
                        teleport = ItemManager.createTeleportBook("Deadpeaks");
                    } else {
                        teleport = ItemManager.createTeleportBook("Gloomy_Hollows");
                    }
                    break;
                default:
                    break;
            }
            if (teleport != null) {
                ent.getWorld().dropItem(ent.getLocation().add(0, 1, 0), teleport);
            }
        }
        /*if (weapon.getType() == Material.BOW) {
            int arrowRoll = random.nextInt(99);
            if (arrowRoll <= (25 + (25 * killerItemFind / 100))) {
                if (arrowRoll > 25) {
                    if (toggleDebug) {
                        killer.sendMessage(ChatColor.GREEN + "Your " + killerItemFind + "% Item Find has resulted in a drop.");
                    }
                }
                ItemStack item = new ItemStack(Material.ARROW);
                int amount = (tier * 2);
                item.setAmount(amount);
                world.getWorld().dropItem(loc.add(0, 1, 0), item);
            }
        }*/ // arrows are no longer needed (uncomment if we ever add them back)
    }
}
