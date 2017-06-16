package net.dungeonrealms.game.item.items.functional.cluescrolls;

import net.dungeonrealms.common.game.util.ChatColor;
import net.dungeonrealms.game.item.ItemType;
import net.dungeonrealms.game.item.PersistentItem;
import net.dungeonrealms.game.item.items.core.ItemArmor;
import net.dungeonrealms.game.item.items.core.ItemGear;
import net.dungeonrealms.game.item.items.core.ItemPickaxe;
import net.dungeonrealms.game.item.items.core.ItemWeapon;
import net.dungeonrealms.game.item.items.functional.*;
import net.dungeonrealms.game.mechanic.data.MiningTier;
import net.dungeonrealms.game.world.entity.type.monster.DRMonster;
import net.dungeonrealms.game.world.entity.type.pet.EnumPets;
import net.dungeonrealms.game.world.entity.util.EntityAPI;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.entity.Rabbit;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Rar349 on 6/15/2017.
 */
public class ClueUtils {

    public static int getClueScrollSlot(Player player, Clue clue, boolean includeComplete, boolean includesUntranslated) {
        for(int k = 0; k < player.getInventory().getContents().length; k++){
            ItemStack stack = player.getInventory().getContents()[k];
            if(stack == null || !stack.getType().equals(Material.MAP)) continue;
            if(!PersistentItem.isType(stack, ItemType.CLUE_SCROLL)) continue;
            ClueScrollItem item = new ClueScrollItem(stack);
            if(!includeComplete && item.isComplete())continue;
            if(!includesUntranslated && !item.isHasBeenTranslated()) continue;
            if(item.getClue().equals(clue)) return k;
        }

        return -1;
    }

    public static ClueScrollItem getClueScrollItem(Player player, Clue clue, boolean includeComplete, boolean includesUntranslated) {
        int slot = getClueScrollSlot(player, clue, includeComplete, includesUntranslated);
        if(slot == -1) return null;
        return new ClueScrollItem(player.getInventory().getItem(slot));
    }

    public static void handleFishAFish(Player player, ItemStack fish) {
        int slot = getClueScrollSlot(player, Clue.PUFFER_FISH, false, false);
        if(slot == -1) return;
        ItemStack inSlot = player.getInventory().getItem(slot);
        if(inSlot == null) return;
        if(!PersistentItem.isType(inSlot, ItemType.CLUE_SCROLL)) return;
        ClueScrollItem clue = new ClueScrollItem(inSlot);
        if(clue.getClue().equals(Clue.PUFFER_FISH)) {
            clue.setProgress(clue.getProgress() +1);
            player.getInventory().setItem(slot, clue.generateItem());
            handleClueCompleted(player,clue);
        }
    }

    public static void handleUseOrb(Player player, ItemStack oldItem, ItemStack newItem) {
        int slot = getClueScrollSlot(player, Clue.UNDER_WATER_ORB, false, false);
        if(slot == -1) return;
        ItemStack inSlot = player.getInventory().getItem(slot);
        if(inSlot == null) return;
        if(!PersistentItem.isType(inSlot, ItemType.CLUE_SCROLL)) return;

        ClueScrollItem clue = new ClueScrollItem(inSlot);
        if(clue.getClue().equals(Clue.UNDER_WATER_ORB)) {
            boolean isDrowning = player.getRemainingAir() <= 0;
            if(isDrowning) {
                clue.handleCompleted();
                player.getInventory().setItem(slot, clue.generateItem());
                handleClueCompleted(player, clue);
            }
        }
    }

    public static void handleSpawnPet(Player player, EnumPets pet) {
        int slot = getClueScrollSlot(player, Clue.SPAWN_GUARDIAN_UNDER_WATER, false, false);
        if(slot == -1) return;
        ItemStack inSlot = player.getInventory().getItem(slot);
        if(inSlot == null) return;
        if(!PersistentItem.isType(inSlot, ItemType.CLUE_SCROLL)) return;

        ClueScrollItem clue = new ClueScrollItem(inSlot);
        if(clue.getClue().equals(Clue.SPAWN_GUARDIAN_UNDER_WATER)) {
            boolean isInWater = player.getRemainingAir() != player.getMaximumAir();
            if(isInWater && pet.equals(EnumPets.GUARDIAN)) {
                clue.handleCompleted();
                player.getInventory().setItem(slot, clue.generateItem());
                handleClueCompleted(player, clue);
            }
        }
    }

    public static void handleTreasureFindFishing(Player player, ItemStack treasure) {
        for(int slot = 0; slot < player.getInventory().getContents().length; slot++) {
            ItemStack inSlot = player.getInventory().getItem(slot);
            if(inSlot == null) continue;
            if(!PersistentItem.isType(inSlot, ItemType.CLUE_SCROLL)) continue;
            if(!ItemArmor.isArmor(treasure)) continue;
            ItemArmor armor = new ItemArmor(treasure);
            ClueScrollItem clue = new ClueScrollItem(inSlot);
            if(clue.getClue().equals(Clue.FISH_GEAR)) {
                int tierRequired = clue.getRolledRanges()[0];
                if(armor.getTier().getId() == tierRequired) {
                    clue.handleCompleted();
                    player.getInventory().setItem(slot, clue.generateItem());
                    handleClueCompleted(player, clue);
                    return; //only do 1 clue at a time.
                }
            }
        }
    }

    public static void handleTreasureFindMining(Player player, ItemStack treasure, MiningTier oreTier) {
        for(int slot = 0; slot < player.getInventory().getContents().length; slot++) {
            ItemStack inSlot = player.getInventory().getItem(slot);
            if(inSlot == null) continue;
            if(!PersistentItem.isType(inSlot, ItemType.CLUE_SCROLL)) continue;
            ClueScrollItem clue = new ClueScrollItem(inSlot);
            if(clue.getClue().equals(Clue.TREASURE_FIND)) {
                int oreTierRequired = clue.getRolledRanges()[0];
                if(oreTier.getTier() == oreTierRequired) {
                    clue.setProgress(clue.getProgress() +1);
                    player.getInventory().setItem(slot, clue.generateItem());
                    handleClueCompleted(player,clue);
                    return; //only do 1 clue at a time.
                }
            }
        }
    }

    public static void handleMobHit(Player player, Entity mob) {
        if(!(mob instanceof Rabbit)) return;
        if(player.getEquipment().getItemInMainHand() == null) return;
        if(!PersistentItem.isType(player.getEquipment().getItemInMainHand(), ItemType.PICKAXE)) return;
        ItemPickaxe pickaxe = new ItemPickaxe(player.getEquipment().getItemInMainHand());
        for(int slot = 0; slot < player.getInventory().getContents().length; slot++) {
            ItemStack inSlot = player.getInventory().getItem(slot);
            if(inSlot == null) continue;
            if(!PersistentItem.isType(inSlot, ItemType.CLUE_SCROLL)) continue;
            ClueScrollItem clue = new ClueScrollItem(inSlot);
            if(clue.getClue().equals(Clue.HIT_A_RABBIT)) {
                int pickaxeTierRequired = clue.getRolledRanges()[0];
                if(pickaxe.getTier().getId() == pickaxeTierRequired) {
                    clue.handleCompleted();
                    player.getInventory().setItem(slot, clue.generateItem());
                    handleClueCompleted(player,clue);
                    return; //only do 1 clue at a time.
                }
            }
        }
    }

    public static void handleMobKilled(Player player, DRMonster killed) {
            for (int slot = 0; slot < player.getInventory().getContents().length; slot++) {
                ItemStack inSlot = player.getInventory().getItem(slot);
                if (inSlot == null) continue;
                if (!PersistentItem.isType(inSlot, ItemType.CLUE_SCROLL)) continue;
                ClueScrollItem clue = new ClueScrollItem(inSlot);
                if (player.getEquipment().getItemInMainHand() != null && PersistentItem.isType(player.getEquipment().getItemInMainHand(), ItemType.PICKAXE) && clue.getClue().equals(Clue.KILL_WITH_PICK)) {
                    ItemPickaxe pickaxe = new ItemPickaxe(player.getEquipment().getItemInMainHand());
                    int monsterTierRequired = clue.getRolledRanges()[0];
                    int pickaxeTierRequired = clue.getRolledRanges()[1];
                    if (pickaxe.getTier().getId() != pickaxeTierRequired) continue;
                    if (killed.getTier() != monsterTierRequired) continue;
                    clue.handleCompleted();
                    player.getInventory().setItem(slot, clue.generateItem());
                    handleClueCompleted(player, clue);
                    return; //only do 1 clue at a time.
                } else if(clue.getClue().equals(Clue.KILL_AN_ELITE) && EntityAPI.isElite(killed.getBukkit())) {
                    clue.handleCompleted();
                    player.getInventory().setItem(slot, clue.generateItem());
                    handleClueCompleted(player, clue);
                } else if(clue.getClue().equals(Clue.KILL_TIERED_ELITE) && EntityAPI.isElite(killed.getBukkit())) {
                    int eliteTierRequired = clue.getRolledRanges()[0];
                    if(killed.getTier() != eliteTierRequired) continue;
                    clue.handleCompleted();
                    player.getInventory().setItem(slot, clue.generateItem());
                    handleClueCompleted(player, clue);
                } else if(clue.getClue().equals(Clue.KILL_NUM_TIERED_ELITE) && EntityAPI.isElite(killed.getBukkit())) {
                    int eliteTierRequired = clue.getRolledRanges()[1];
                    if(killed.getTier() != eliteTierRequired) continue;
                    clue.setProgress(clue.getProgress() +1);
                    player.getInventory().setItem(slot, clue.generateItem());
                    handleClueCompleted(player, clue);
                } else if(player.getEquipment().getItemInMainHand() != null && ItemWeapon.isWeapon(player.getEquipment().getItemInMainHand())) {
                    ItemWeapon weapon = new ItemWeapon(player.getEquipment().getItemInMainHand());
                    if(clue.getClue().equals(Clue.KILL_MOBS)) {
                        int weaponTierRequired = clue.getRolledRanges()[2];
                        int mobTierRequired = clue.getRolledRanges()[1];
                        if(killed.getTier() != mobTierRequired) continue;
                        if(weapon.getTier().getId() != weaponTierRequired) continue;
                        clue.setProgress(clue.getProgress() +1);
                        player.getInventory().setItem(slot, clue.generateItem());
                        handleClueCompleted(player,clue);
                    }
                }
            }
    }

    public static void handlePlayerKilled(Player killer, Player died) {
        if(killer.getEquipment().getItemInMainHand() == null) return;
        if(!ItemWeapon.isWeapon(killer.getEquipment().getItemInMainHand())) return;
        ItemWeapon weapon = new ItemWeapon(killer.getEquipment().getItemInMainHand());
        for(int slot = 0; slot < killer.getInventory().getContents().length; slot++) {
            ItemStack inSlot = killer.getInventory().getItem(slot);
            if(inSlot == null) continue;
            if(!PersistentItem.isType(inSlot, ItemType.CLUE_SCROLL)) continue;
            ClueScrollItem clue = new ClueScrollItem(inSlot);
            if(clue.getClue().equals(Clue.KILL_A_PLAYER)) {
                int weaponTierRequired = clue.getRolledRanges()[0];
                if(weapon.getTier().getId() == weaponTierRequired) {
                    clue.handleCompleted();
                    killer.getInventory().setItem(slot, clue.generateItem());
                    handleClueCompleted(killer,clue);
                    return; //only do 1 clue at a time.
                }
            }
        }
    }

    public static void handleOreMined(Player player, MiningTier oreTier) {
        for(int slot = 0; slot < player.getInventory().getContents().length; slot++) {
            ItemStack inSlot = player.getInventory().getItem(slot);
            if(inSlot == null) continue;
            if(!PersistentItem.isType(inSlot, ItemType.CLUE_SCROLL)) continue;
            ClueScrollItem clue = new ClueScrollItem(inSlot);
            if(clue.getClue().equals(Clue.MINE_ORE)) {
                int oreTierRequired = clue.getRolledRanges()[1];
                if(oreTier.getTier() != oreTierRequired) continue;
                clue.setProgress(clue.getProgress() +1);
                player.getInventory().setItem(slot, clue.generateItem());
                handleClueCompleted(player,clue);
                return; //only do 1 clue at a time.
            } else if(clue.getClue().equals(Clue.MINE_ON_HORSE)) {
                if(player.getVehicle() == null || !(player.getVehicle() instanceof Horse)) continue;
                int oreTierRequired = clue.getRolledRanges()[0];
                if(oreTier.getTier() != oreTierRequired) continue;
                clue.handleCompleted();
                player.getInventory().setItem(slot, clue.generateItem());
                handleClueCompleted(player,clue);
                return; //only do 1 clue at a time.
            }
        }
    }

    public static void handleAddEnchantToItem(Player player, ItemEnchantScroll scroll, ItemStack upgrading) {
        if(scroll instanceof ItemEnchantFishingRod) {
            int slot = getClueScrollSlot(player, Clue.USE_FISHING_ENCHANT, false, false);
            if (slot == -1) return;
            ItemStack inSlot = player.getInventory().getItem(slot);
            if (inSlot == null) return;
            if (!PersistentItem.isType(inSlot, ItemType.CLUE_SCROLL)) return;

            ClueScrollItem clue = new ClueScrollItem(inSlot);
            if (clue.getClue().equals(Clue.USE_FISHING_ENCHANT)) {
                    clue.handleCompleted();
                    player.getInventory().setItem(slot, clue.generateItem());
                    handleClueCompleted(player, clue);
            }
        } else if(scroll instanceof ItemEnchantPickaxe) {
            int slot = getClueScrollSlot(player, Clue.USE_PICK_ENCHANT, false, false);
            if (slot == -1) return;
            ItemStack inSlot = player.getInventory().getItem(slot);
            if (inSlot == null) return;
            if (!PersistentItem.isType(inSlot, ItemType.CLUE_SCROLL)) return;

            ClueScrollItem clue = new ClueScrollItem(inSlot);
            if (clue.getClue().equals(Clue.USE_PICK_ENCHANT)) {
                clue.handleCompleted();
                player.getInventory().setItem(slot, clue.generateItem());
                handleClueCompleted(player, clue);
            }
        } else if(scroll instanceof ItemEnchantArmor) {
            int slot = getClueScrollSlot(player, Clue.USE_ARMOR_ENCHANT, false, false);
            if (slot == -1) return;
            ItemStack inSlot = player.getInventory().getItem(slot);
            if (inSlot == null) return;
            if (!PersistentItem.isType(inSlot, ItemType.CLUE_SCROLL)) return;

            ClueScrollItem clue = new ClueScrollItem(inSlot);
            if (clue.getClue().equals(Clue.USE_ARMOR_ENCHANT)) {
                clue.handleCompleted();
                player.getInventory().setItem(slot, clue.generateItem());
                handleClueCompleted(player, clue);
            }
        } else if(scroll instanceof ItemEnchantWeapon) {
            int slot = getClueScrollSlot(player, Clue.USE_WEAPON_ENCHANT, false, false);
            if (slot == -1) return;
            ItemStack inSlot = player.getInventory().getItem(slot);
            if (inSlot == null) return;
            if (!PersistentItem.isType(inSlot, ItemType.CLUE_SCROLL)) return;

            ClueScrollItem clue = new ClueScrollItem(inSlot);
            if (clue.getClue().equals(Clue.USE_WEAPON_ENCHANT)) {
                clue.handleCompleted();
                player.getInventory().setItem(slot, clue.generateItem());
                handleClueCompleted(player, clue);
            }
        }
    }

    public static void handleClueCompleted(Player player, ClueScrollItem item) {
        if(!item.isComplete()) return;
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f,1f);
        player.sendMessage("");
        player.sendMessage(ChatColor.GREEN + ChatColor.BOLD.toString() + "TREASURE SCROLL COMPLETE");
        player.sendMessage(ChatColor.GRAY + item.getClueType().getWhoForReward());
    }

}
