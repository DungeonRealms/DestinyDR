package net.dungeonrealms.listeners;

import net.dungeonrealms.API;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.handlers.EnergyHandler;
import net.minecraft.server.v1_8_R3.EntityExperienceOrb;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerToggleSprintEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Random;

/**
 * Created by Kieran on 9/24/2015.
 */
public class EnergyListener implements Listener {

    /**
     * Checks for players starving
     * adds hunger potion effect and metadata
     * to be used at a later time
     *
     * @param event
     * @since 1.0
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerStarveDamage(EntityDamageEvent event) {
        if (event.getCause() != EntityDamageEvent.DamageCause.STARVATION) return;
        if (!(API.isPlayer(event.getEntity()))) return;
        event.setCancelled(true);
        event.setDamage(0);
        Player player = (Player) event.getEntity();
        if (!(player.hasPotionEffect(PotionEffectType.HUNGER))) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, 50, 0));
            if (!(player.hasMetadata("starving"))) {
                player.setMetadata("starving", new FixedMetadataValue(DungeonRealms.getInstance(), "true"));
                player.sendMessage(ChatColor.RED.toString() + ChatColor.BOLD + "**STARVING**");
            }
        }
    }

    /**
     * Checks for players starting sprinting or stopping sprinting
     * adds/removes correct metadata, applies initial energy reduction
     * cancels if player can't sprint
     *
     * @param event
     * @since 1.0
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerToggleSprint(PlayerToggleSprintEvent event) {
        if (!(event.getPlayer().isSprinting())) {
            if (!(event.getPlayer().hasMetadata("starving"))) {
                EnergyHandler.removeEnergyFromPlayerAndUpdate(event.getPlayer().getUniqueId(), 0.15F);
                event.getPlayer().setMetadata("sprinting", new FixedMetadataValue(DungeonRealms.getInstance(), "true"));
            } else {
                event.setCancelled(true);
                event.getPlayer().setSprinting(false);
                return;
            }
        }
        if (event.getPlayer().isSprinting()) {
            event.getPlayer().removeMetadata("sprinting", DungeonRealms.getInstance());
        }
    }

    /**
     * Checks players food level changes
     * removes hunger potion effect and metadata
     * to be used at a later time
     *
     * @param event
     * @since 1.0
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerFoodLevelChange(FoodLevelChangeEvent event) {
        if (!(API.isPlayer(event.getEntity()))) return;
        Player player = (Player) event.getEntity();
        if (event.getFoodLevel() < player.getFoodLevel()) {
            if (new Random().nextInt(4) >= 1) {
                event.setCancelled(true);
                return;
            }
        }
        if (event.getFoodLevel() > 0 && player.hasMetadata("starving")) {
            player.removeMetadata("starving", DungeonRealms.getInstance());
            player.removePotionEffect(PotionEffectType.HUNGER);
        }
    }

    /**
     * Cancels the base MC Exp change
     * as we use exp for energy
     *
     * @param event
     * @since 1.0
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerMCExpChange(PlayerExpChangeEvent event) {
        event.setAmount(0);
    }

    /**
     * Safety check, cancels mobs/players
     * dropping MC exp
     *
     * @param event
     * @since 1.0
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntityDeathDropExp(EntityDeathEvent event) {
        event.setDroppedExp(0);
    }

    /**
     * Safety check, cancels players
     * picking up items
     *
     * @param event
     * @since 1.0
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onItemPickup(PlayerPickupItemEvent event) {
        if (!(event.getItem() instanceof ExperienceOrb) && (!(event.getItem() instanceof EntityExperienceOrb))) return;
        event.setCancelled(true);
    }

    /**
     * Handles players swinging their fists/items
     * removes energy or cancels the event if they can't
     *
     * @param event
     * @since 1.0
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerAnimation(PlayerAnimationEvent event) {
        Player player = event.getPlayer();
        ItemStack playerWeapon = player.getItemInHand();

        if (playerWeapon == null) {
            playerWeapon = new ItemStack(Material.AIR);
        }
        float energyToRemove = EnergyHandler.getWeaponSwingEnergyCost(playerWeapon);
        if (playerWeapon.getType() == Material.BOW) {
            energyToRemove += 0.15F;
        }

        if (player.hasPotionEffect(PotionEffectType.SLOW_DIGGING) || EnergyHandler.getPlayerCurrentEnergy(player.getUniqueId()) <= 0) {
            event.setCancelled(true);
        }

        EnergyHandler.removeEnergyFromPlayerAndUpdate(player.getUniqueId(), energyToRemove);
    }

    /**
     * Handles players deaths, removing metadata
     * and potion effects
     *
     * @param event
     * @since 1.0
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        if (!player.hasPotionEffect(PotionEffectType.HUNGER) && !player.hasPotionEffect(PotionEffectType.SLOW_DIGGING) && !player.hasMetadata("starving") && !player.hasMetadata("sprinting"))
            return;
        if (player.hasPotionEffect(PotionEffectType.HUNGER)) {
            player.removePotionEffect(PotionEffectType.HUNGER);
        }
        if (player.hasMetadata("starving")) {
            player.removeMetadata("starving", DungeonRealms.getInstance());
        }
        if (player.hasMetadata("sprinting")) {
            player.removeMetadata("sprinting", DungeonRealms.getInstance());
        }
        if (player.hasPotionEffect(PotionEffectType.SLOW_DIGGING)) {
            player.removePotionEffect(PotionEffectType.SLOW_DIGGING);
        }
    }
}
