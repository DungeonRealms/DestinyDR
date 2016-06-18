package net.dungeonrealms.game.world.loot;

import net.dungeonrealms.API;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.mechanics.ItemManager;
import net.dungeonrealms.game.player.banks.BankMechanics;
import net.dungeonrealms.game.world.loot.types.LootType;
import net.minecraft.server.v1_9_R2.BlockPosition;
import net.minecraft.server.v1_9_R2.Packet;
import net.minecraft.server.v1_9_R2.PacketPlayOutWorldEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_9_R2.CraftServer;
import org.bukkit.craftbukkit.v1_9_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftPlayer;
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
                } else if (API.isOrb(stack)) {
                    stack = ItemManager.createOrbofAlteration();
                } else if (ItemManager.isEnchantScroll(stack)) {
                    int tier = CraftItemStack.asNMSCopy(stack).getTag().getInt("tier");
                    String type = CraftItemStack.asNMSCopy(stack).getTag().getString("type");
                    if (type.equalsIgnoreCase("armorenchant"))
                        stack = ItemManager.createArmorEnchant(tier);
                    else
                        stack = ItemManager.createWeaponEnchant(tier);
                } else if (ItemManager.isProtectScroll(stack)) {
                    int tier = CraftItemStack.asNMSCopy(stack).getTag().getInt("tier");
                    stack = ItemManager.createProtectScroll(tier);
                } else if (BankMechanics.getInstance().isBankNote(stack)) {
                    stack = BankMechanics.createBankNote(CraftItemStack.asNMSCopy(stack).getTag().getInt("worth"));
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
        Bukkit.getWorlds().get(0).playSound(block.getLocation(), Sound.ENTITY_ZOMBIE_BREAK_DOOR_WOOD, 0.5f, 1.2f);
        Packet particles = new PacketPlayOutWorldEvent(2001, new BlockPosition((int) Math.round(block.getLocation().getX()), (int) Math.round(block.getLocation().getY() + 2), (int) Math.round(block.getLocation().getZ())), 25, false);
        ((CraftServer) DungeonRealms.getInstance().getServer()).getServer().getPlayerList().sendPacketNearby(((CraftPlayer) player).getHandle(), block.getLocation().getX(), block.getLocation().getY(), block.getLocation().getZ(), 36, ((CraftWorld) block.getWorld()).getHandle().dimension, particles);
        block.getDrops().clear();
        block.setType(Material.AIR);
        broken = true;
        Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
            setContents();
            block.setType(Material.CHEST);
        }, (long) ((delay + 1200) * LootManager.getDelayMultiplier()));
    }

}
