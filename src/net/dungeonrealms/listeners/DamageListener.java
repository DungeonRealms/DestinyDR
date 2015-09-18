package net.dungeonrealms.listeners;

import net.minecraft.server.v1_8_R3.NBTTagCompound;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Created by Nick on 9/17/2015.
 */
public class DamageListener implements Listener {

    /**
     * This event is to handle mobs, not PLAYERS!
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
                NBTTagCompound tag = CraftItemStack.asNMSCopy(((Player) event.getDamager()).getItemInHand()).getTag();
                //Check if it's a {WEAPON} the player is hitting with. Once of our custom ones!
                if (!tag.getString("type").equalsIgnoreCase("weapon")) return;
                double damage = tag.getDouble("damage");
                event.setDamage(damage);
            }
        }
    }

}
