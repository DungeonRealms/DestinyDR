package net.dungeonrealms.game.donation.overrides;

import com.codingforcookies.armorequip.ArmorEquipEvent;
import com.codingforcookies.armorequip.ArmorType;
import com.comphenix.protocol.wrappers.EnumWrappers;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.game.mechanic.generic.EnumPriority;
import net.dungeonrealms.game.mechanic.generic.GenericMechanic;
import net.minecraft.server.v1_9_R2.EnumItemSlot;
import net.minecraft.server.v1_9_R2.PacketPlayOutEntityEquipment;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_9_R2.inventory.CraftItemStack;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

/**
 * Created by Rar349 on 5/25/2017.
 */
public class OverrideListener implements GenericMechanic, Listener {


    private FakeEquipment equipment;

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onArmorEquip(final ArmorEquipEvent event) {
        if (event.getType() == ArmorType.HELMET && event.getPlayer().isOp()) {
            ((CraftPlayer) event.getPlayer()).getHandle().playerConnection.sendPacket(new PacketPlayOutEntityEquipment(event.getPlayer().getEntityId(), EnumItemSlot.HEAD, CraftItemStack.asNMSCopy(new ItemStack(Material.SAPLING, 1, (short) 4))));
        }
    }

    @Override
    public EnumPriority startPriority() {
        return EnumPriority.PRIESTS;
    }

    @Override
    public void startInitialization() {
        this.equipment = new FakeEquipment(DungeonRealms.getInstance()) {
            @Override
            protected boolean onEquipmentSending(final EquipmentSendingEvent event) {
                if(event.getVisibleEntity() instanceof Player) {
                    Player player = (Player) event.getVisibleEntity();
                    PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(player);
                    if(wrapper == null) return false;
                    final ItemStack clone = event.getEquipment();
                    if (clone == null) {
                        return false;
                    }
                    clone.setType(Material.SAPLING);
                    clone.setDurability((short) 4);
                    event.setEquipment(clone);
                }
                if (event.getSlot() != EnumWrappers.ItemSlot.HEAD) {
                    return false;
                }
                return true;
            }

            @Override
            protected void onEntitySpawn(final Player client, final LivingEntity visibleEntity) {
                super.onEntitySpawn(client, visibleEntity);
            }
        };
    }

    @Override
    public void stopInvocation() {
        this.equipment.close();
    }
}
