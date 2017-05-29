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
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_9_R2.inventory.CraftItemStack;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCreativeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Created by Rar349 on 5/25/2017.
 */
public class OverrideListener implements GenericMechanic, Listener {


    private FakeEquipment equipment;

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onArmorEquip(final ArmorEquipEvent event) {
        System.out.println("The armor hat debug send!");
        if (event.getType() == ArmorType.HELMET) {
            System.out.println("The armor hat debug send! 2");
            PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(event.getPlayer());
            if (wrapper == null) return;
            System.out.println("The armor hat debug send! 3");
            CosmeticOverrides hatOverride = wrapper.getActiveHatOverride();
            if (hatOverride == null) return;
            if (event.getNewArmorPiece() == null || event.getNewArmorPiece().getType().equals(Material.AIR)) return;
            System.out.println("The armor hat debug send! 4");
            ItemStack clone = event.getNewArmorPiece().clone();
            if (clone == null) clone = new ItemStack(hatOverride.getItemType());
            ItemMeta meta = clone.getItemMeta();
            meta.setDisplayName(hatOverride.getNameColor() + hatOverride.getDisplayName());
            clone.setType(hatOverride.getItemType());
            clone.setDurability(hatOverride.getDurability());
            clone.setItemMeta(meta);
            System.out.println("The armor hat debug send! 5: " + clone.getType() + " , " + clone.getDurability());
            final ItemStack realClone = clone.clone();
            Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), new Runnable() {
                @Override
                public void run() {
                    ((CraftPlayer) event.getPlayer()).getHandle().playerConnection.sendPacket(new PacketPlayOutEntityEquipment(event.getPlayer().getEntityId(), EnumItemSlot.HEAD, CraftItemStack.asNMSCopy(realClone)));
                }
            });
        }
    }

    @EventHandler
    public void onPlayerClic(InventoryCreativeEvent event) {
        if (event.getSlot() == 5 && event.getClickedInventory().getName().equals("container.inventory")) {
            //Keep this synced...

            Player player = (Player) event.getWhoClicked();
            event.setCancelled(true);
            player.updateInventory();
            player.setItemOnCursor(null);
            player.getEquipment().setHelmet(event.getCursor());
            player.getInventory().addItem(event.getCurrentItem());

//            event.setCursor(event.getCurrentItem());
//            event.setCurrentItem(null);
//            player.setItemOnCursor(event.getCurrentItem());
//            player.updateInventory();
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
                if (event.getVisibleEntity() instanceof Player) {
                    Player player = (Player) event.getVisibleEntity();
                    PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(player);
                    if (wrapper == null) return false;
                    CosmeticOverrides hatOverride = wrapper.getActiveHatOverride();
                    if (hatOverride != null && event.getSlot().equals(EnumWrappers.ItemSlot.HEAD)) {
                        final ItemStack clone = event.getEquipment();
                        if (clone == null) return false;
                        clone.setType(hatOverride.getItemType());
                        clone.setDurability(hatOverride.getDurability());
                        event.setEquipment(clone);
                    }
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

    public static void updatePlayersHatLocally(Player toUpdate) {
        if (toUpdate == null || !toUpdate.isOnline()) return;
        PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(toUpdate);
        if (wrapper == null) return;

        CosmeticOverrides hatOverride = wrapper.getActiveHatOverride();
        if (hatOverride == null) return;
        if (toUpdate.getEquipment().getHelmet() == null || toUpdate.getEquipment().getHelmet().getType() == Material.AIR)
            return;

        ItemStack clone = toUpdate.getEquipment().getHelmet().clone();
        if (clone == null) clone = new ItemStack(hatOverride.getItemType());
        ItemMeta meta = clone.getItemMeta();
        meta.setDisplayName(hatOverride.getNameColor() + hatOverride.getDisplayName());
        clone.setType(hatOverride.getItemType());
        clone.setDurability(hatOverride.getDurability());
        clone.setItemMeta(meta);
    }


    public static void updatePlayersHat(Player toUpdate) {
        if (toUpdate == null || !toUpdate.isOnline()) return;
        PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(toUpdate);
        if (wrapper == null) return;

        CosmeticOverrides hatOverride = wrapper.getActiveHatOverride();
        if (hatOverride == null) return;
        if (toUpdate.getEquipment().getHelmet() == null || toUpdate.getEquipment().getHelmet().getType() == Material.AIR)
            return;

        ItemStack clone = toUpdate.getEquipment().getHelmet().clone();
        if (clone == null) clone = new ItemStack(hatOverride.getItemType());
        ItemMeta meta = clone.getItemMeta();
        meta.setDisplayName(hatOverride.getNameColor() + hatOverride.getDisplayName());
        clone.setType(hatOverride.getItemType());
        clone.setDurability(hatOverride.getDurability());
        clone.setItemMeta(meta);
        for (Entity near : toUpdate.getNearbyEntities(32, 32, 32)) {
            if (!(near instanceof Player)) continue;
            Player nearPlayer = (Player) near;
            ((CraftPlayer) nearPlayer).getHandle().playerConnection.sendPacket(new PacketPlayOutEntityEquipment(toUpdate.getEntityId(), EnumItemSlot.HEAD, CraftItemStack.asNMSCopy(clone)));
        }

        if (toUpdate.getGameMode() != GameMode.CREATIVE)
            ((CraftPlayer) toUpdate).getHandle().playerConnection.sendPacket(new PacketPlayOutEntityEquipment(toUpdate.getEntityId(), EnumItemSlot.HEAD, CraftItemStack.asNMSCopy(clone)));
    }
}

