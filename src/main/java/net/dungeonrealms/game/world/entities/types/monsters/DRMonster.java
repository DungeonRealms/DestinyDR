package net.dungeonrealms.game.world.entities.types.monsters;

import net.dungeonrealms.API;
import net.dungeonrealms.game.miscellaneous.RandomHelper;
import net.dungeonrealms.game.mongo.DatabaseAPI;
import net.dungeonrealms.game.mongo.EnumData;
import net.dungeonrealms.game.player.banks.BankMechanics;
import net.dungeonrealms.game.world.anticheat.AntiCheat;
import net.dungeonrealms.game.world.items.DamageAPI;
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

import java.util.Random;

/**
 * Created by Chase on Oct 21, 2015
 */
public interface DRMonster {

    void onMonsterAttack(Player p);

    void onMonsterDeath(Player killer);

    EnumMonster getEnum();

    default void checkItemDrop(int tier, EnumMonster monster, Entity ent, Player killer) {
        if (ent.getWorld().getName().contains("DUNGEON")) {
            //No normal drops in dungeons.
            return;
        }
        Random random = new Random();
        boolean toggleDebug = (Boolean) DatabaseAPI.getInstance().getData(EnumData.TOGGLE_DEBUG, killer.getUniqueId());
        int killerGemFind = DamageAPI.calculatePlayerStat(killer, Item.ArmorAttributeType.GEM_FIND);
        int killerItemFind = DamageAPI.calculatePlayerStat(killer, Item.ArmorAttributeType.ITEM_FIND);
        Location loc = ent.getLocation();
        World world = ((CraftWorld) loc.getWorld()).getHandle();
        int gemRoll = random.nextInt(99);
        if (gemRoll <= (20 + (20 * killerGemFind / 100))) {
            if (gemRoll > 20) {
                if (toggleDebug) {
                    killer.sendMessage(ChatColor.GREEN + "Your " + killerGemFind + "% Gem Find has resulted in a drop.");
                }
            }
            double gem_drop_amount = 0;
            double drop_multiplier = 1;
            // Elite = 1.5x money chance / item chance.
            if (ent.hasMetadata("elite")) {
                drop_multiplier = 1.5;
            }

            double gold_drop_multiplier = 1;

            switch (tier) {
                case 1:
                    gem_drop_amount = (random.nextInt(8 - 1) + 1) * gold_drop_multiplier;
                    break;
                case 2:
                    gem_drop_amount = (random.nextInt(18 - 2) + 2) * gold_drop_multiplier;
                    break;
                case 3:
                    gem_drop_amount = (random.nextInt(34 - 10) + 10) * gold_drop_multiplier;
                    break;
                case 4:
                    gem_drop_amount = (random.nextInt(64 - 20) + 20) * gold_drop_multiplier;
                    break;
                case 5:
                    gem_drop_amount = (random.nextInt(175 - 75) + 75) * gold_drop_multiplier;
                    break;
            }

            ItemStack item = BankMechanics.gem.clone();
            item.setAmount((int) (gem_drop_amount * drop_multiplier));
            if (item.getAmount() < 1) {
                item.setAmount(1);
            }
            world.getWorld().dropItem(loc.add(0, 1, 0), item);
            if (!ent.hasMetadata("elite")) {
                return;
            }
        }

        int chance = 0;
        switch (tier) {
            case 1:
                chance = 100;
                break;
            case 2:
                chance = 60;
                break;
            case 3:
                chance = 30;
                break;
            case 4:
                chance = 15;
                break;
            case 5:
                chance = 6;
                break;
        }
        //TODO: VERY DANGEROUS CODE. REMOVE BEFORE RELEASE
        if (ent.hasMetadata("elite")) {
            for (ItemStack stack : ((LivingEntity) ent).getEquipment().getArmorContents()) {
                if (stack == null || stack.getType() == Material.AIR || stack.getType() == Material.SKULL || stack.getType() == Material.SKULL_ITEM) {
                    continue;
                }
                world.getWorld().dropItem(loc.add(0, 1, 0), stack);
            }
            ItemStack weapon = ((LivingEntity) ent).getEquipment().getItemInMainHand();
            if (weapon == null || weapon.getType() == Material.AIR) {
                return;
            }
            world.getWorld().dropItem(loc.add(0, 1, 0), weapon);
            return;
        }
        int armorRoll = random.nextInt(1000);
        int drops = 0;
        for (ItemStack stack : ((LivingEntity) ent).getEquipment().getArmorContents()) {
            if (stack == null || stack.getType() == Material.AIR || stack.getType() == Material.SKULL || stack.getType() == Material.SKULL_ITEM) {
                continue;
            }
            if (drops < 1) {
                if (armorRoll <= chance + (chance * killerItemFind / 100)) {
                    if (armorRoll > chance) {
                        if (toggleDebug) {
                            killer.sendMessage(ChatColor.GREEN + "Your " + killerItemFind + "% Item Find has resulted in a drop.");
                        }
                    }
                    RepairAPI.setCustomItemDurability(stack, RandomHelper.getRandomNumberBetween(200, 1000));
                    world.getWorld().dropItem(loc.add(0, 1, 0), stack);
                    drops++;
                }
            }
        }
        if (!ent.hasMetadata("elite")) {
            ItemStack helmet = new ItemGenerator().setTier(Item.ItemTier.getByTier(tier)).setType(Item.ItemType.HELMET).setRarity(API.getItemRarity(false)).generateItem().getItem();
            AntiCheat.getInstance().applyAntiDupe(helmet);
            if (drops < 1) {
                if (armorRoll <= chance + (chance * killerItemFind / 100)) {
                    if (armorRoll > chance) {
                        if (toggleDebug) {
                            killer.sendMessage(ChatColor.GREEN + "Your " + killerItemFind + "% Item Find has resulted in a drop.");
                        }
                    }
                    RepairAPI.setCustomItemDurability(helmet, RandomHelper.getRandomNumberBetween(200, 1000));
                    world.getWorld().dropItem(loc.add(0, 1, 0), helmet);
                    drops++;
                }
            }
        }
        ItemStack weapon = ((LivingEntity) ent).getEquipment().getItemInMainHand();
        if (weapon != null && weapon.getType() != Material.AIR) {
            if (drops < 1) {
                if (armorRoll <= chance + (chance * killerItemFind / 100)) {
                    if (armorRoll > chance) {
                        if (toggleDebug) {
                            killer.sendMessage(ChatColor.GREEN + "Your " + killerItemFind + "% Item Find has resulted in a drop.");
                        }
                    }
                    RepairAPI.setCustomItemDurability(weapon, RandomHelper.getRandomNumberBetween(200, 1000));
                    world.getWorld().dropItem(loc.add(0, 1, 0), weapon);
                }
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
        }*/ //TODO: Decide if we want infinite arrows.
    }
}
