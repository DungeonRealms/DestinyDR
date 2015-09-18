package net.dungeonrealms.listeners;

import net.minecraft.server.v1_8_R3.NBTTagCompound;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

/**
 * Created by Nick on 9/17/2015.
 */
public class DamageListener implements Listener {

    /**
     * Listen for the players weapon.
     * @param event
     * @since 1.0
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onEntityByEntityDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player) {
            //Make sure the player is HOLDING something!
            if (((Player) event.getDamager()).getItemInHand() == null) return;
            //Check if the item has NBT, all our custom weapons will have NBT.
            if ((CraftItemStack.asNMSCopy(((Player) event.getDamager()).getItemInHand()).getTag() != null)) {
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
