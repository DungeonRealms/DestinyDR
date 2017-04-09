package net.dungeonrealms.game.world.loot;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.game.item.items.functional.ItemEnchantArmor;
import net.dungeonrealms.game.item.items.functional.ItemEnchantWeapon;
import net.dungeonrealms.game.item.items.functional.ItemOrb;
import net.dungeonrealms.game.item.items.functional.ItemProtectionScroll;
import net.dungeonrealms.game.mastery.GamePlayer;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.mechanic.ItemManager;
import net.dungeonrealms.game.player.banks.BankMechanics;
import net.dungeonrealms.game.world.loot.types.LootType;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_9_R2.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Random;

/**
 * Created by Chase on Oct 9, 2015
 */
public class LootSpawner {

    public long delay = 100;
    public Location location;
    public Block block;
    public Inventory inv;
    public boolean broken;
    public LootType lootType;

    public LootSpawner(Block chest, long delay, LootType lootType) {
        this.location = chest.getLocation();
        this.delay = delay;
        this.lootType = lootType;
        block = chest;
        block.setType(Material.CHEST);
        inv = Bukkit.createInventory(null, 27, "Loot");
        setContents();
        broken = false;
    }

    /**
     * Sets the loot in the chest based on tier.
     */
    private void setContents() {
        HashMap<ItemStack, Double> loot = lootType.getLoot();
        if (loot.isEmpty()) {
            Utils.log.info("LOOT EMPTY RETURNNING...");
            return;
        }
        int count = 0;
        for (ItemStack stack : loot.keySet()) {
            if (loot == null || !loot.containsKey(stack))
                continue;
            double spawn_chance = loot.get(stack);
            double do_i_spawn = new Random().nextInt(1000);
            if (spawn_chance < 1) {
                spawn_chance = 1;
            }
//			Utils.log.info(spawn_chance + " > " + do_i_spawn + " " + stack.getType());
            if (spawn_chance >= do_i_spawn) {
                if (stack.getType() == Material.IRON_SWORD) {
//					int tier = CraftItemStack.asNMSCopy(stack).getTag().getInt("itemTier");
//					stack = LootManager.generateRandomTierItem(tier);
                    continue;
                } else if (GameAPI.isOrb(stack)) {
                    stack = new ItemOrb().createItem();
                } else if (ItemManager.isEnchantScroll(stack)) {
                    int tier = CraftItemStack.asNMSCopy(stack).getTag().getInt("tier");
                    String type = CraftItemStack.asNMSCopy(stack).getTag().getString("type");
                    if (type.equalsIgnoreCase("armorenchant"))
                        stack = new ItemEnchantArmor(tier).createItem();
                    else
                        stack = new ItemEnchantWeapon(tier).createItem();
                } else if (new ItemProtectionScroll().isInstanceOf(stack)) {
                    int tier = CraftItemStack.asNMSCopy(stack).getTag().getInt("tier");
                    stack = new ItemProtectionScroll(tier).createItem();
                } else if (BankMechanics.isBankNote(stack)) {
                    stack = BankMechanics.createBankNote(CraftItemStack.asNMSCopy(stack).getTag().getInt("worth"), "");
                }

                count++;
                inv.addItem(stack);
            }
        }

        if (count == 0) {
            setContents();
//			inv.addItem(ItemManager.createHealthPotion(1, false, false));
        }

    }

    /**
     * Checking if the inventory is empty, then break the chest.
     */
    public void update(Player player) {
        if (inv.getContents().length > 0) {
            for (ItemStack stack : inv.getContents()) {
                if (stack != null) {
                    if (stack.getType() != (Material.AIR)) {
                        return;
                    }
                }
            }
        }
        GamePlayer gamePlayer = GameAPI.getGamePlayer(player);
        if (gamePlayer == null) return;
        gamePlayer.getPlayerStatistics().setLootChestsOpened(gamePlayer.getPlayerStatistics().getLootChestsOpened() + 1);
        for (int i = 0; i < 6; i++) {
            player.getWorld().playEffect(block.getLocation().add(i, 0.5, i), Effect.TILE_BREAK, 25, 12);
            player.getWorld().playEffect(block.getLocation().add(i, 0.35, i), Effect.TILE_BREAK, 25, 12);
            player.getWorld().playEffect(block.getLocation().add(i, 0.2, i), Effect.TILE_BREAK, 25, 12);
        }
        player.playSound(block.getLocation(), Sound.ENTITY_ZOMBIE_BREAK_DOOR_WOOD, 0.5f, 1.2f);
        block.getDrops().clear();
        block.setType(Material.AIR);
        broken = true;
        Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
            setContents();
            block.setType(Material.CHEST);
        }, (long) (1200 + delay + (delay * LootManager.getDelayMultiplier())));
    }

}
