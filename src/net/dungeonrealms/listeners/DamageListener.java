package net.dungeonrealms.listeners;

import net.dungeonrealms.energy.EnergyHandler;
import net.dungeonrealms.entities.utils.EntityAPI;
import net.dungeonrealms.items.Attribute;
import net.dungeonrealms.items.DamageAPI;
import net.dungeonrealms.mastery.MetadataUtils;
import net.dungeonrealms.mechanics.ParticleAPI;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.EntityEquipment;
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
        event.blockList().clear();
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
     * Used for calculating damage based on player weapon
     * @param event
     * @since 1.0
     */
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = false)
    public void onPlayerHitEntity(EntityDamageByEntityEvent event) {
        if ((!(event.getDamager() instanceof Player)) && ((event.getDamager().getType() != EntityType.ARROW) && (event.getDamager().getType() != EntityType.WITHER_SKULL))) return;
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
            if (attacker.hasPotionEffect(PotionEffectType.SLOW_DIGGING) || EnergyHandler.getPlayerCurrentEnergy(attacker.getUniqueId()) <= 0) {
                event.setCancelled(true);
                attacker.playSound(attacker.getLocation(), Sound.WOLF_PANT, 12F, 1.5F);
                try {
                    ParticleAPI.sendParticleToLocation(ParticleAPI.ParticleEffect.CRIT, event.getEntity().getLocation().add(0,1,0), new Random().nextFloat(), new Random().nextFloat(), new Random().nextFloat(), 0.75F, 40);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                return;
            }
            EnergyHandler.removeEnergyFromPlayerAndUpdate(attacker.getUniqueId(), EnergyHandler.getWeaponSwingEnergyCost(attacker.getItemInHand()));
            attacker.getItemInHand().setDurability(((short) -1));
            finalDamage = DamageAPI.calculateWeaponDamage(attacker, event.getEntity(), tag);
        } else if (event.getDamager().getType() == EntityType.ARROW) {
            Arrow attackingArrow = (Arrow) event.getDamager();
            if (!(attackingArrow.getShooter() instanceof Player)) return;
            if (attackingArrow.getShooter() != null && attackingArrow.getShooter() instanceof Player) {
                finalDamage = DamageAPI.calculateProjectileDamage((Player)attackingArrow.getShooter(), event.getEntity(), attackingArrow);
            }
        } else if (event.getDamager().getType() == EntityType.WITHER_SKULL) {
            WitherSkull staffProjectile = (WitherSkull) event.getDamager();
            if (!(staffProjectile.getShooter() instanceof Player)) return;
            if (staffProjectile.getShooter() != null && staffProjectile.getShooter() instanceof Player) {
                finalDamage = DamageAPI.calculateProjectileDamage((Player)staffProjectile.getShooter(), event.getEntity(), staffProjectile);
            }
        }
        event.setDamage(finalDamage);
    }

    /**
     * Listen for the monsters hitting a player
     * Used for calculating damage based on mob weapon
     * @param event
     * @since 1.0
     */
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = false)
    public void onMonsterHitPlayer(EntityDamageByEntityEvent event) {
        if ((!(event.getDamager() instanceof Monster)) && ((event.getDamager().getType() != EntityType.ARROW) && (event.getDamager().getType() != EntityType.WITHER_SKULL))) return;
        if (!(event.getEntity() instanceof Player)) return;
        double finalDamage = 0;
        if (event.getDamager() instanceof Monster) {
            Monster attacker = (Monster) event.getDamager();
            EntityEquipment attackerEquipment = attacker.getEquipment();
            if (attackerEquipment.getItemInHand() == null) return;
            attackerEquipment.getItemInHand().setDurability(((short) -1));
            //Check if the item has NBT, all our custom weapons will have NBT.
            net.minecraft.server.v1_8_R3.ItemStack nmsItem = (CraftItemStack.asNMSCopy(attackerEquipment.getItemInHand()));
            if (nmsItem == null || nmsItem.getTag() == null) {
                Bukkit.broadcastMessage("MOB " + event.getDamager() + " does not have one of our custom weapons. CHASE!!!!!!");
                return;
            }
            //Get the NBT of the item the mob is holding.
            NBTTagCompound tag = nmsItem.getTag();
            //Check if it's a {WEAPON} the mob is hitting with. Once of our custom ones!
            if (!tag.getString("type").equalsIgnoreCase("weapon")) return;
            finalDamage = DamageAPI.calculateWeaponDamage(attacker, event.getEntity(), tag);
        } else if (event.getDamager().getType() == EntityType.ARROW) {
            Arrow attackingArrow = (Arrow) event.getDamager();
            if (!(attackingArrow.getShooter() instanceof Monster)) return;
            finalDamage = DamageAPI.calculateProjectileDamage((LivingEntity)attackingArrow.getShooter(), event.getEntity(), attackingArrow);
        } else if (event.getDamager().getType() == EntityType.WITHER_SKULL) {
            WitherSkull staffProjectile = (WitherSkull) event.getDamager();
            if (!(staffProjectile.getShooter() instanceof Monster)) return;
            finalDamage = DamageAPI.calculateProjectileDamage((LivingEntity)staffProjectile.getShooter(), event.getEntity(), staffProjectile);
        }
        event.setDamage(finalDamage);
    }


    /**
     * Reduces damage after it is set previously based on the defenders armor
     * @param event
     * @since 1.0
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = false)
    public void onArmorReduceBaseDamage(EntityDamageByEntityEvent event) {
        if ((!(event.getDamager() instanceof LivingEntity)) && ((event.getDamager().getType() != EntityType.ARROW) && (event.getDamager().getType() != EntityType.WITHER_SKULL)))
            return;
        if (!(event.getEntity() instanceof LivingEntity)) return;
        double armourReducedDamage = 0;
        LivingEntity defender = (LivingEntity) event.getEntity();
        EntityEquipment defenderEquipment = defender.getEquipment();
        if (defenderEquipment.getArmorContents() == null) return;
        ItemStack[] defenderArmor = defenderEquipment.getArmorContents();
        defenderArmor[0].setDurability((short) -1);
        defenderArmor[1].setDurability((short) -1);
        defenderArmor[2].setDurability((short) -1);
        defenderArmor[3].setDurability((short) -1);
        if (event.getDamager() instanceof LivingEntity) {
            LivingEntity attacker = (LivingEntity) event.getDamager();
            armourReducedDamage = DamageAPI.calculateArmorReduction(attacker, defender, defenderArmor);
        } else if (event.getDamager().getType() == EntityType.ARROW) {
            Arrow attackingArrow = (Arrow) event.getDamager();
            if (!(attackingArrow.getShooter() instanceof LivingEntity)) return;
            if (attackingArrow.getShooter() instanceof Monster) {
                armourReducedDamage = DamageAPI.calculateArmorReduction(attackingArrow, defender, defenderArmor);
            }
        } else if (event.getDamager().getType() == EntityType.WITHER_SKULL) {
            WitherSkull staffProjectile = (WitherSkull) event.getDamager();
            if (!(staffProjectile.getShooter() instanceof LivingEntity)) return;
            if (staffProjectile.getShooter() instanceof Monster) {
                armourReducedDamage = DamageAPI.calculateArmorReduction(staffProjectile, defender, defenderArmor);
            }
        }
        if (armourReducedDamage == -1) {
            //The defender dodged the attack
            event.setDamage(0);
            LivingEntity leDefender = (LivingEntity) event.getEntity();
            if (leDefender.hasPotionEffect(PotionEffectType.SLOW)) {
                leDefender.removePotionEffect(PotionEffectType.SLOW);
            }
            if (leDefender.hasPotionEffect(PotionEffectType.POISON)) {
                leDefender.removePotionEffect(PotionEffectType.POISON);
            }
            try {
                ParticleAPI.sendParticleToLocation(ParticleAPI.ParticleEffect.CLOUD, defender.getLocation(),
                        new Random().nextFloat(), new Random().nextFloat(), new Random().nextFloat(), 0.5F, 20);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else if (event.getDamage() - armourReducedDamage == 0 || armourReducedDamage == -2) {
            event.setDamage(0);
            LivingEntity leDefender = (LivingEntity) event.getEntity();
            if (leDefender.hasPotionEffect(PotionEffectType.SLOW)) {
                leDefender.removePotionEffect(PotionEffectType.SLOW);
            }
            if (leDefender.hasPotionEffect(PotionEffectType.POISON)) {
                leDefender.removePotionEffect(PotionEffectType.POISON);
            }
            try {
                ParticleAPI.sendParticleToLocation(ParticleAPI.ParticleEffect.RED_DUST, defender.getLocation(),
                        new Random().nextFloat(), new Random().nextFloat(), new Random().nextFloat(), 0.5F, 20);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else {
            event.setDamage(event.getDamage() - armourReducedDamage);
        }
    }

    /**
     * Listen for Players [NOT DISPENSERS/MOBS] firing projectiles
     * Used to apply metadata from the nbt data of the bow in the entities hand
     * @param event
     * @since 1.0
     */
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = false)
    public void onLivingEntityFireProjectile(ProjectileLaunchEvent event) {
        if (!(event.getEntity().getShooter() instanceof Player) && ((event.getEntityType() != EntityType.ARROW) && (event.getEntityType() != EntityType.WITHER_SKULL))) return;
        LivingEntity shooter = (LivingEntity) event.getEntity().getShooter();
        EntityEquipment entityEquipment = shooter.getEquipment();
        if (entityEquipment.getItemInHand() == null) return;
        entityEquipment.getItemInHand().setDurability((short) -1);
        //Check if the item has NBT, all our custom weapons will have NBT.
        net.minecraft.server.v1_8_R3.ItemStack nmsItem = (CraftItemStack.asNMSCopy(entityEquipment.getItemInHand()));
        if (nmsItem == null || nmsItem.getTag() == null) return;
        //Get the NBT of the item the player is holding.
        int weaponTier = new Attribute(entityEquipment.getItemInHand()).getItemTier().getId();
        Player player = (Player) shooter;
        if (player.hasPotionEffect(PotionEffectType.SLOW_DIGGING) || EnergyHandler.getPlayerCurrentEnergy(player.getUniqueId()) <= 0) {
            event.setCancelled(true);
            event.getEntity().remove();
            player.playSound(shooter.getLocation(), Sound.WOLF_PANT, 12F, 1.5F);
            try {
                ParticleAPI.sendParticleToLocation(ParticleAPI.ParticleEffect.CRIT, event.getEntity().getLocation().add(0,1,0), new Random().nextFloat(), new Random().nextFloat(), new Random().nextFloat(), 0.75F, 40);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return;
        }
        EnergyHandler.removeEnergyFromPlayerAndUpdate(player.getUniqueId(), EnergyHandler.getWeaponSwingEnergyCost(player.getItemInHand()));
        NBTTagCompound tag = nmsItem.getTag();
        MetadataUtils.registerProjectileMetadata(tag, event.getEntity(), weaponTier);
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
        if (event.getDamager() instanceof Player) {
            Player player = (Player) event.getDamager();
            player.getItemInHand().setDurability((short) -1);
        }
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
        if (!(event.getEntity().hasMetadata("type"))) return;
        String metaValue = event.getEntity().getMetadata("type").get(0).asString().toLowerCase();
        switch (metaValue) {
            case "pet":
                event.setCancelled(true);
                event.getEntity().setFireTicks(0);
                break;
            case "mount":
                event.setCancelled(true);
                event.getEntity().setFireTicks(0);
                break;
            default:
        }
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

    /**
     * Listen for Monsters dying
     * handles their item drops based
     * on their tier and killers luck chance
     *
     * @param event
     * @since 1.0
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onMonsterDeathDropItems(EntityDeathEvent event) {
        if (!(event.getEntity().hasMetadata("type"))) return;
        if (!(event.getEntity() instanceof Monster)) return;
        if (event.getEntity().getKiller() == null) return;
        Player killer = event.getEntity().getKiller();
        int playerLuck = DamageAPI.calculatePlayerLuck(killer);
        int mobTier = event.getEntity().getMetadata("tier").get(0).asInt();
        if (new Random().nextInt(99) > ((20 - mobTier) + playerLuck)) {
            event.getDrops().clear();
        }
    }
}
