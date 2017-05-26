package net.dungeonrealms.game.donation.overrides;

import com.codingforcookies.armorequip.ArmorEquipEvent;
import com.codingforcookies.armorequip.ArmorType;
import com.comphenix.protocol.wrappers.EnumWrappers;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

/**
 * Created by Rar349 on 5/25/2017.
 */
public class OverrideListener implements Listener {


    private FakeEquipment equipment;

    public void onEnable() {
        //this.getServer().getPluginManager().registerEvents((Listener)this, (Plugin)this);
        this.equipment = new FakeEquipment(this) {
            @Override
            protected boolean onEquipmentSending(final EquipmentSendingEvent event) {
                if (event.getSlot() != EnumWrappers.ItemSlot.HEAD) {
                    return false;
                }
                final ItemStack clone = event.getEquipment();
                if (clone == null) {
                    return false;
                }
                clone.setType(Material.SAPLING);
                clone.setDurability((short)4);
                event.setEquipment(clone);
                return true;
            }

            @Override
            protected void onEntitySpawn(final Player client, final LivingEntity visibleEntity) {
                super.onEntitySpawn(client, visibleEntity);
            }
        };
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onArmorEquip(final ArmorEquipEvent event) {
        /*if (event.getType() == ArmorType.HELMET && event.getPlayer().isOp()) {
            ((CraftPlayer)event.getPlayer()).getHandle().playerConnection.sendPacket((Packet)new PacketPlayOutEntityEquipment(event.getPlayer().getEntityId(), EnumItemSlot.HEAD, CraftItemStack.asNMSCopy(new ItemStack(Material.SAPLING, 1, (short)4))));
        }*/
    }
}
