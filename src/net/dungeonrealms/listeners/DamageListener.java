package net.dungeonrealms.listeners;

import net.dungeonrealms.entities.utils.EntityAPI;
import net.dungeonrealms.items.Attribute;
import net.dungeonrealms.items.DamageAPI;
import net.dungeonrealms.mastery.MetadataUtils;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

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
        if (!(event.getEntity().hasMetadata("type"))) return;
        if (event.getEntity().getMetadata("type").get(0).asString().equalsIgnoreCase("buff")) {
            event.setCancelled(true);
            int radius = event.getEntity().getMetadata("radius").get(0).asInt();
            int duration = event.getEntity().getMetadata("duration").get(0).asInt();
            PotionEffectType effectType = PotionEffectType.getByName(event.getEntity().getMetadata("effectType").get(0).asString());
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
    }

    /**
     * Listen for the players weapon hitting an entity
     * @param event
     * @since 1.0
     */
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = false)
    public void onPlayerHitEntity(EntityDamageByEntityEvent event) {
        if ((!(event.getDamager() instanceof Player)) && (event.getDamager().getType() != EntityType.ARROW)) return;
        //Make sure the player is HOLDING something!
        double finalDamage = 0;
        if (event.getDamager() instanceof Player) {
            Player attacker = (Player) event.getDamager();
            if (attacker.getItemInHand() == null) return;
            //Check if the item has NBT, all our custom weapons will have NBT.
            net.minecraft.server.v1_8_R3.ItemStack nmsItem = (CraftItemStack.asNMSCopy(attacker.getItemInHand()));
            if (nmsItem == null || nmsItem.getTag() == null) return;
            //Get the NBT of the item the player is holding.
            NBTTagCompound tag = nmsItem.getTag();
            //Check if it's a {WEAPON} the player is hitting with. Once of our custom ones!
            if (!tag.getString("type").equalsIgnoreCase("weapon")) return;
            finalDamage = DamageAPI.calculateWeaponDamage(attacker, event.getEntity(), tag);
        } else {
            Arrow attackingArrow = (Arrow) event.getDamager();
            if (!(((Arrow) event.getDamager()).getShooter() instanceof Player)) return;
            if (attackingArrow.getShooter() != null && attackingArrow.getShooter() instanceof Player) {
                finalDamage = DamageAPI.calculateProjectileDamage((Player)attackingArrow.getShooter(), event.getEntity(), attackingArrow);
            }
        }
        event.setDamage(finalDamage);
    }

    /**
     * Listen for the monsters hitting a player
     * @param event
     * @since 1.0
     */
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = false)
    public void onMonsterHitPlayer(EntityDamageByEntityEvent event) {
        if ((!(event.getDamager() instanceof Monster)) && (event.getDamager().getType() != EntityType.ARROW)) return;
        if (!(event.getEntity() instanceof Player)) return;
        //Make sure the player is HOLDING something!
        double finalDamage = 0;
        if (event.getDamager() instanceof Monster) {
            Monster attacker = (Monster) event.getDamager();
            EntityEquipment attackerEquipment = attacker.getEquipment();
            if (attackerEquipment.getItemInHand() == null) return;
            //Check if the item has NBT, all our custom weapons will have NBT.
            net.minecraft.server.v1_8_R3.ItemStack nmsItem = (CraftItemStack.asNMSCopy(attackerEquipment.getItemInHand()));
            if (nmsItem == null || nmsItem.getTag() == null) {
                Bukkit.broadcastMessage("MOB " + event.getDamager() + " does not have one of our custom weapons. CHASE!!!!!!");
                return;
            }
            //Get the NBT of the item the player is holding.
            NBTTagCompound tag = nmsItem.getTag();
            //Check if it's a {WEAPON} the player is hitting with. Once of our custom ones!
            if (!tag.getString("type").equalsIgnoreCase("weapon")) return;
            finalDamage = DamageAPI.calculateWeaponDamage(attacker, event.getEntity(), tag);
        } else {
            Arrow attackingArrow = (Arrow) event.getDamager();
            if (!(((Arrow) event.getDamager()).getShooter() instanceof Monster)) return;
            if (attackingArrow.getShooter() != null && attackingArrow.getShooter() instanceof Player) {
                finalDamage = DamageAPI.calculateProjectileDamage((LivingEntity)attackingArrow.getShooter(), event.getEntity(), attackingArrow);
            }
        }
        event.setDamage(finalDamage);
    }


    /**
     * Test to check EventPriorities
     * @param event
     * @since 1.0
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = false)
    public void onPlayerMeleeHitEntityREDUCETEST(EntityDamageByEntityEvent event) {
        if ((!(event.getDamager() instanceof Player)) && (event.getDamager().getType() != EntityType.ARROW)) return;
        Bukkit.broadcastMessage("Previous Damage " + String.valueOf(event.getDamage()));

        event.setDamage(event.getDamage() / 2);
        Bukkit.broadcastMessage("Armor Reduced Damage " + String.valueOf(event.getDamage()));
    }

    /**
     * Test to check EventPriorities
     * @param event
     * @since 1.0
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = false)
    public void onMobMeleeHitPlayerREDUCETEST(EntityDamageByEntityEvent event) {
        if ((!(event.getDamager() instanceof Monster)) && (event.getDamager().getType() != EntityType.ARROW)) return;
        if (!(event.getEntity() instanceof Player)) return;
        Bukkit.broadcastMessage("Previous Damage " + String.valueOf(event.getDamage()));

        event.setDamage(event.getDamage() / 2);
        Bukkit.broadcastMessage("Armor Reduced Damage " + String.valueOf(event.getDamage()));
    }

    /**
     * Listen for Living Entities (Mobs/Players etc) [NOT DISPENSERS] firing projectiles
     * Used to apply metadata from the nbt data of the bow in the entities hand
     * @param event
     * @since 1.0
     */
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = false)
    public void onLivingEntityFireProjectile(ProjectileLaunchEvent event) {
        if (!(event.getEntity().getShooter() instanceof LivingEntity) &&(event.getEntityType() != EntityType.ARROW)) return;
        LivingEntity shooter = (LivingEntity) event.getEntity().getShooter();
        EntityEquipment entityEquipment = shooter.getEquipment();
        if (entityEquipment.getItemInHand() == null) return;
        //Check if the item has NBT, all our custom weapons will have NBT.
        net.minecraft.server.v1_8_R3.ItemStack nmsItem = (CraftItemStack.asNMSCopy(entityEquipment.getItemInHand()));
        if (nmsItem == null || nmsItem.getTag() == null) return;
        //Get the NBT of the item the player is holding.
        int weaponTier = new Attribute(entityEquipment.getItemInHand()).getItemTier().getId();
        NBTTagCompound tag = nmsItem.getTag();
        MetadataUtils.registerProjectileMetadata(tag, event.getEntity(), weaponTier);
        Bukkit.broadcastMessage("Projectile Meta Registered");
    }

    /**
     * Listen for Pets Damage.
     * <p>
     * E.g. I can't attack Xwaffle's Wolf it's a pet!
     *
     * @param event
     * @since 1.0
     */
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void petDamageListener(EntityDamageByEntityEvent event) {
        if (!(event.getEntity().hasMetadata("type"))) return;
        if (event.getEntity() instanceof Player) return;
        String metaValue = event.getEntity().getMetadata("type").get(0).asString().toLowerCase();
        switch (metaValue) {
            case "pet":
                event.setCancelled(true);
                break;
            case "mount":
                event.setCancelled(true);
                break;
            default:
        }
    }


    /**
     * Listen for Entities being damaged by non Entities.
     * Mainly used for damage cancelling
     *
     * @param event
     * @since 1.0
     */
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void onEntityDamaged(EntityDamageEvent event) {
        //if (event.getEntity() instanceof Player) return;
        if (event.getCause() == DamageCause.CONTACT || event.getCause() == DamageCause.CONTACT || event.getCause() == DamageCause.DROWNING
                || event.getCause() == DamageCause.FALL || event.getCause() == DamageCause.LAVA || event.getCause() == DamageCause.FIRE
                || event.getCause() == DamageCause.ENTITY_EXPLOSION || event.getCause() == DamageCause.BLOCK_EXPLOSION) {
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
        if (EntityAPI.hasPetOut(event.getEntity().getUniqueId())) {
            net.minecraft.server.v1_8_R3.Entity pet = EntityAPI.getPlayerPet(event.getEntity().getUniqueId());
            if (!pet.getBukkitEntity().isDead()) { //Safety check
                pet.getBukkitEntity().remove();
            }
            EntityAPI.removePlayerPetList(event.getEntity().getUniqueId());
            event.getEntity().sendMessage("For it's own safety, your pet has returned to its home.");
        }

        if (EntityAPI.hasMountOut(event.getEntity().getUniqueId())) {
            net.minecraft.server.v1_8_R3.Entity mount = EntityAPI.getPlayerMount(event.getEntity().getUniqueId());
            if (mount.isAlive()) {
                mount.getBukkitEntity().remove();
            }
            EntityAPI.getPlayerMount(event.getEntity().getUniqueId());
            event.getEntity().sendMessage("For it's own safety, your mount has returned to the stable.");
        }
    }
}
