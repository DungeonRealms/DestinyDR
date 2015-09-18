package net.dungeonrealms.listeners;

import net.dungeonrealms.entities.Entities;
import net.dungeonrealms.entities.utils.EntityStats;
import net.dungeonrealms.mastery.NMSUtils;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Random;

/**
 * Created by Nick on 9/17/2015.
 */
public class DamageListener implements Listener {

    /**
     * This event listens for EnderCrystal explosions.
     * Which are buffs.. with the correct nbt at least.
     *
     * @param event
     * @since 1.0
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onBuffExplode(EntityExplodeEvent event) {
        if (!(event.getEntity() instanceof EnderCrystal)) return;
        event.setCancelled(true);
        net.minecraft.server.v1_8_R3.Entity nmsEntity = ((CraftEntity) event.getEntity()).getHandle();
        NBTTagCompound tag = nmsEntity.getNBTTag();
        if (!tag.getString("type").equalsIgnoreCase("buff")) return;
        int radius = tag.getInt("radius");
        int duration = tag.getInt("duration");
        PotionEffectType effectType = PotionEffectType.getByName(tag.getString("effectType"));
        for (Entity e : event.getEntity().getNearbyEntities(radius, radius, radius)) {
            if (!(e instanceof Player)) continue;
            ((Player) e).addPotionEffect(new PotionEffect(effectType, duration, 2));
            e.sendMessage(new String[]{
                    "",
                    ChatColor.BLUE + "[BUFF] " + ChatColor.YELLOW + "You have received the " + ChatColor.UNDERLINE + effectType.getName() + ChatColor.YELLOW + " buff!",
                    ""
            });
        }
    }

    /**
     * This event is to handle mobs, not PLAYERS!
     * <p>
     * THIS METHOD IS FOR TESTING, NOT SURE IF EXTENDING
     * THE ENTITY YOU CAN PUT ITS DROPS INSIDE. XWAFFLE. ;-)
     *
     * @param event
     * @since 1.0
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onMobDeath(EntityDeathEvent event) {
        if (event.getEntity() instanceof Player) return;
        if (!(event.getEntity().hasMetadata("type"))) return;
        String metaValue = event.getEntity().getMetadata("type").get(0).asString();
        if (metaValue.equalsIgnoreCase("hostile")) {
            int tier = event.getEntity().getMetadata("tier").get(0).asInt();
            switch (tier) {
                case 1:
                    event.getDrops().clear();
                    event.getEntity().getWorld().dropItemNaturally(event.getEntity().getLocation(), new ItemStack(Material.BEDROCK, 2));
                    break;
                case 2:
                    event.getDrops().clear();
                    event.getEntity().getWorld().dropItemNaturally(event.getEntity().getLocation(), new ItemStack(Material.INK_SACK, 2));
                default:
                    Bukkit.broadcastMessage("THIS MOB HAS NO TIER CODED NUBS");
            }
        }

    }

    /**
     * Listen for the players weapon.
     *
     * @param event
     * @since 1.0
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onPlayerStrikeWithWeapon(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) return;
        //Make sure the player is HOLDING something!
        if (((Player) event.getDamager()).getItemInHand() == null) return;
        //Check if the item has NBT, all our custom weapons will have NBT.
        net.minecraft.server.v1_8_R3.ItemStack nmsItem = (CraftItemStack.asNMSCopy(((Player) event.getDamager()).getItemInHand()));
        if (nmsItem == null || nmsItem.getTag() == null) return;
        //Get the NBT of the item the player is holding.
        NBTTagCompound tag = nmsItem.getTag();
        //Check if it's a {WEAPON} the player is hitting with. Once of our custom ones!
        if (!tag.getString("type").equalsIgnoreCase("weapon")) return;
        double damage = tag.getDouble("damage");
        event.setDamage(damage);
    }

    /**
     * Listen for Entities being damaged by another Entity.
     * NOT TO BE USED FOR PLAYERS
     * Mainly used for friendly mobs and damage cancelling
     *
     * @param event
     * @since 1.0
     */
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void onEntityDamagedByEntity(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player) return;
        if (!(event.getEntity().hasMetadata("type"))) return;
        String metaValue = event.getEntity().getMetadata("type").get(0).asString().toLowerCase();
        switch (metaValue) {
            case "pet":
                event.setCancelled(true);
                event.getDamager().sendMessage("You cannot damage players pets!");
                break;
            case "mount":
                event.setCancelled(true);
                event.getDamager().sendMessage("You cannot damage players mounts!");
                break;
            default:
        }
    }


    /**
     * Listen for Entities being damaged by non Entities.
     * NOT TO BE USED FOR PLAYERS
     * Mainly used for damage cancelling
     *
     * @param event
     * @since 1.0
     */
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void onEntityDamaged(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) return;
        if (event.getCause() == DamageCause.CONTACT || event.getCause() == DamageCause.CONTACT || event.getCause() == DamageCause.DROWNING || event.getCause() == DamageCause.FALL
                || event.getCause() == DamageCause.LAVA || event.getCause() == DamageCause.FIRE) {
            event.setCancelled(true);
            event.getEntity().setFireTicks(0);
        }
    }

    /**
     * Listen for Players dying
     * NOT TO BE USED FOR NON-PLAYERS
     *
     * @param event
     * @since 1.0
     */
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void onPlayerDeath(EntityDeathEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        if (Entities.PLAYER_PETS.containsKey(event.getEntity().getUniqueId())) {
            net.minecraft.server.v1_8_R3.Entity pet = Entities.PLAYER_PETS.get(event.getEntity().getUniqueId());
            if (!pet.getBukkitEntity().isDead()) { //Safety check
                pet.getBukkitEntity().remove();
            }
            Entities.PLAYER_PETS.remove(event.getEntity().getUniqueId());
            event.getEntity().sendMessage("For it's own safety, your pet has returned to its home.");
        }

        if (Entities.PLAYER_MOUNTS.containsKey(event.getEntity().getUniqueId())) {
            net.minecraft.server.v1_8_R3.Entity mount = Entities.PLAYER_MOUNTS.get(event.getEntity().getUniqueId());
            if (mount.isAlive()) {
                mount.getBukkitEntity().remove();
            }
            Entities.PLAYER_MOUNTS.remove(event.getEntity().getUniqueId());
            event.getEntity().sendMessage("For it's own safety, your mount has returned to the stable.");
        }
    }

    /**
     * Listen for monsters damaging players
     * NOT TO BE USED FOR PLAYERS
     *
     * @param event
     * @since 1.0
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onMonsterDamagePlayer(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        int finalDamage = 0;
        Player p = (Player) event.getEntity();
        net.minecraft.server.v1_8_R3.Entity nmsMonster = NMSUtils.getNMSEntity(event.getDamager());
        if (event.getDamager().hasMetadata("type") && event.getDamager().getMetadata("type").get(0).asString().equalsIgnoreCase("hostile")) {
            EntityStats.Stats stats = EntityStats.getMonsterStats(nmsMonster);
            Random random = new Random();
            if (random.nextBoolean()) {
                finalDamage = stats.atk + random.nextInt(10);
            } else {
                finalDamage = stats.atk - random.nextInt(10);
            }
            p.sendMessage(finalDamage + "dealt to you");
        }
    }
}
