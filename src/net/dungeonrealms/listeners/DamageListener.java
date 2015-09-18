package net.dungeonrealms.listeners;

import net.dungeonrealms.mastery.Utils;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
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
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.inventory.ItemStack;
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
        if (!(event.getEntity() instanceof EnderCrystal)) return;
        event.setCancelled(true);
        net.minecraft.server.v1_8_R3.Entity nmsEntity = ((CraftEntity) event.getEntity()).getHandle();
        NBTTagCompound tag = nmsEntity.getNBTTag();
        if (tag == null) {
            Utils.log.warning("EnderCrystal blew up and isn't a Buff?!? " + event.getEntity().getLocation());
            return;
        }
        //for some reason it's NOT passing through here..
        System.out.println("6");
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
        net.minecraft.server.v1_8_R3.Entity nmsEntity = ((CraftEntity) event.getEntity()).getHandle();
        NBTTagCompound tag = nmsEntity.getNBTTag();
        if (tag == null) return;

        if (tag.getString("type").equalsIgnoreCase("mob")) {
            int level = tag.getInt("level");
            switch (level) {
                case 0:
                    event.getDrops().clear();
                    event.getEntity().getWorld().dropItem(event.getEntity().getLocation(), new ItemStack(Material.BEDROCK, 2));
                    break;
                default:
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
        if (event.getDamager() instanceof Player) {
            //Make sure the player is HOLDING something!
            if (((Player) event.getDamager()).getItemInHand() == null) return;
            //Check if the item has NBT, all our custom weapons will have NBT.
            net.minecraft.server.v1_8_R3.ItemStack nmsItem = (CraftItemStack.asNMSCopy(((Player) event.getDamager()).getItemInHand()));
            if (nmsItem != null && nmsItem.getTag() != null) {
                //Get the NBT of the item the player is holding.
                NBTTagCompound tag = nmsItem.getTag();
                //Check if it's a {WEAPON} the player is hitting with. Once of our custom ones!
                if (!tag.getString("type").equalsIgnoreCase("weapon")) return;
                double damage = tag.getDouble("damage");
                event.setDamage(damage);
            }
        }
    }

}
