package net.dungeonrealms.listeners;

import net.dungeonrealms.entities.utils.EntityAPI;
import net.dungeonrealms.entities.utils.EntityStats;
import net.dungeonrealms.items.Attribute;
import net.dungeonrealms.mastery.NMSUtils;
import net.dungeonrealms.mechanics.ParticleAPI;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
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
     * Listen for the players weapon.
     * MELEE ONLY
     *
     * @param event
     * @since 1.0
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onPlayerMeleeHitEntity(EntityDamageByEntityEvent event) {
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
        if (((Player) event.getDamager()).getItemInHand().getType() ==  Material.BOW) {
            return;
        }
        Entity entityDamged = event.getEntity();
        ItemStack ourItem = ((Player) event.getDamager()).getItemInHand();
        int weaponTier = new Attribute(ourItem).getItemTier().getId();
        double damage = tag.getDouble("damage");
        Bukkit.broadcastMessage("BaseDMG: " + damage);
        boolean isHitCrit = false;
        if (entityDamged instanceof Player) {
            if (tag.getInt("vsPlayers") != 0) {
                damage += tag.getInt("vsPlayers");
                Bukkit.broadcastMessage("VSMONSTER: " + damage);
                //THIS IS DR'S FORMULA. I'M NOT SURE WHY THEY ALWAYS DIVIDE BY 100 SURELY YOU'D JUST SET THE DATA TO 80 INSTEAD OF 800 ETC.
                //TODO: PROBABLY CHANGE
            }
        } else {
            if (entityDamged.getMetadata("type").get(0).asString().equalsIgnoreCase("hostile")) {
                Bukkit.broadcastMessage("IsMonster");
                if (tag.getInt("vsMonsters") != 0) {
                    damage += tag.getInt("vsMonsters");
                    Bukkit.broadcastMessage("VSMONSTER: " + damage);
                    //THIS IS DR'S FORMULA. I'M NOT SURE WHY THEY ALWAYS DIVIDE BY 100 SURELY YOU'D JUST SET THE DATA TO 80 INSTEAD OF 800 ETC.
                    // TODO: PROBABLY CHANGE
                }
            }
        }

        //TODO: THIS WAS BEING USED IN DR BUT THE TIER OF THE ITEM WAS HARDCODED TO 0. WHY? NO CLUE. SHOULD WE KEEP OR REMOVE?
        if (tag.getInt("fireDamage") != 0) {
            switch (weaponTier) {
                case 0:
                    entityDamged.setFireTicks(15);
                    break;
                case 1:
                    entityDamged.setFireTicks(25);
                    break;
                case 2:
                    entityDamged.setFireTicks(30);
                    break;
                case 3:
                    entityDamged.setFireTicks(35);
                    break;
                case 4:
                    entityDamged.setFireTicks(40);
                    break;
            }
            damage += tag.getInt("fireDamage");
            Bukkit.broadcastMessage("FIRE: " + damage);
        }

        LivingEntity le = (LivingEntity) entityDamged;
        if (tag.getInt("iceDamage") != 0) {
            switch (weaponTier) {
                case 0:
                    le.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 30, 0));
                    break;
                case 1:
                    le.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 40, 0));
                    break;
                case 2:
                    le.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 50, 0));
                    break;
                case 3:
                    le.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 40, 1));
                    break;
                case 4:
                    le.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 50, 1));
                    break;
            }
            damage += tag.getInt("iceDamage");
            Bukkit.broadcastMessage("ICE: " + damage);
        }

        if (tag.getInt("poisonDamage") != 0) {
            switch (weaponTier) {
                case 0:
                    le.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 30, 0));
                    break;
                case 1:
                    le.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 40, 0));
                    break;
                case 2:
                    le.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 50, 0));
                    break;
                case 3:
                    le.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 40, 1));
                    break;
                case 4:
                    le.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 50, 1));
                    break;
            }
            damage += tag.getInt("poisonDamage");
            Bukkit.broadcastMessage("POISON: " + damage);
        }

        if (tag.getInt("criticalHit") != 0) {
            try {
                ParticleAPI.sendParticleToLocation(ParticleAPI.ParticleEffect.MAGIC_CRIT, entityDamged.getLocation(),
                        new Random().nextFloat(), new Random().nextFloat(), new Random().nextFloat(), 1F, 50);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            isHitCrit = true;
        }

        if (tag.getInt("lifesteal") != 0) {
            //TODO: LIFESTEAL WHEN WE HAVE OUR CUSTOM HP SHIT DONE
        }

        if (tag.getInt("blind") != 0) {
            //TODO: BLIND. NOT SURE IF WE WANT THIS. PRETTY RETARDED
        }

        if (((Player) event.getDamager()).hasPotionEffect(PotionEffectType.INCREASE_DAMAGE)) {
            int potionTier = 0;
            for (PotionEffect potionEffect : ((Player) event.getDamager()).getActivePotionEffects()) {
                if (potionEffect.getType() == PotionEffectType.INCREASE_DAMAGE) {
                    potionTier = potionEffect.getAmplifier();
                    break;
                }
            }
            switch (potionTier) {
                case 0:
                    damage *= 1.1;
                    break;
                case 1:
                    damage *= 1.3;
                    break;
                case 2:
                    damage *= 1.5;
                    break;
            }
        }
        if (isHitCrit) {
            damage = damage * 1.5;
            Bukkit.broadcastMessage("Crit: " + damage);
        }
        Bukkit.broadcastMessage("Final Attack damage: " + damage);
        event.setDamage(damage);
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
