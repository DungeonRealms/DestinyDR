package net.dungeonrealms.game.player.inventory.menus;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.game.player.inventory.ShopMenu;
import net.dungeonrealms.game.player.inventory.ShopMenuListener;
import net.minecraft.server.v1_9_R2.ChatMessage;
import net.minecraft.server.v1_9_R2.EntityPlayer;
import net.minecraft.server.v1_9_R2.IChatBaseComponent;
import net.minecraft.server.v1_9_R2.PacketPlayOutOpenWindow;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public abstract class GUIMenu extends ShopMenu {

    @Getter
    @Setter
    private boolean shouldOpenPreviousOnClose;

    public GUIMenu(Player player, int size, String title) {
        super(player, size, title);
        shouldOpenPreviousOnClose = false;
    }


    @Override
    protected abstract void setItems();

    public void setItem(int index, @Nullable GUIItem shopItem) {
        this.items.put(index, shopItem);
        this.inventory.setItem(index, shopItem.getItem());
    }

    public void setItem(int index, @NonNull ItemStack item) {
        GUIItem is = new GUIItem(item);
        this.items.put(index, is);
        this.inventory.setItem(index, is.getItem());
    }

    public GUIItem getItem(int slot){
        return (GUIItem) this.items.get(slot);
    }

    public void removeItem(int slot){
        this.items.remove(slot);
        this.inventory.setItem(slot, null);
    }
    public GUIMenu setCloseCallback(Consumer<Player> event) {
        this.closeCallback = event;
        return this;
    }

    public void clear(){
        this.inventory.clear();
        this.items.clear();
    }
    public void open(Player player, InventoryAction action) {
        if (player == null)
            return;
        this.player = player;


        ShopMenu menu = ShopMenuListener.getMenus().get(player);
        if (menu != null) {
            if (menu.getTitle() != null && menu.getTitle().equals(getTitle()) && menu.getSize() == getSize()) {
                setItems();
                return;
            }

            //Delay the next inventory click by 1.
            if (action != null && action.name().startsWith("PICKUP_")) {
                //CAnt close the inventory on a pickup_all action etc otherwise throws exceptions.
                Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
                    this.setItems();
                    player.closeInventory();
                    reopenWithDelay(player);
                });
                return;
            }
            this.setItems();
            reopenWithDelay(player);
            return;
        }

        this.setItems();
        player.openInventory(getInventory());
        ShopMenuListener.getMenus().put(player, this);
    }

    public GUIMenu getPreviousGUI() {
        return null;
    }

    @Override
    public void onRemove() {
        Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
            if(isShouldOpenPreviousOnClose()) getPreviousGUI().open(player,null);
        });
    }

    public void updateWindowTitle(final Player player, String title) {
        final EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();
        final PacketPlayOutOpenWindow packet = new PacketPlayOutOpenWindow(entityPlayer.activeContainer.windowId, "minecraft:chest", (IChatBaseComponent) new ChatMessage(title, new Object[0]), getSize());
        entityPlayer.playerConnection.sendPacket(packet);
        entityPlayer.updateInventory(entityPlayer.activeContainer);
    }

}
