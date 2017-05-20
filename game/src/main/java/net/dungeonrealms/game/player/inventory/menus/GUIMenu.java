package net.dungeonrealms.game.player.inventory.menus;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.game.item.items.core.ShopItem;
import net.dungeonrealms.game.mechanic.ItemManager;
import net.dungeonrealms.game.player.inventory.ShopMenu;
import net.minecraft.server.v1_9_R2.ChatMessage;
import net.minecraft.server.v1_9_R2.EntityPlayer;
import net.minecraft.server.v1_9_R2.IChatBaseComponent;
import net.minecraft.server.v1_9_R2.PacketPlayOutOpenWindow;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public abstract class GUIMenu extends ShopMenu {

    @Getter
    private boolean shouldOpenPreviousOnClose;

    @Getter
    protected GUIMenu previousGUI = null;

    public GUIMenu(Player player, int size, String title) {
        this(player, size, title, null);
    }

    public GUIMenu(Player player, int size, String title, GUIMenu previous) {
        super(player, size, title);
        this.previousGUI = previous;
    }

    public GUIMenu setPreviousGUI(GUIMenu toSet) {
        this.previousGUI = toSet;
        return this;
    }

    public GUIMenu open() {
        open(player, null);
        return this;
    }

    public void setItem(int index, @NonNull ShopItem shopItem) {
        this.items.put(index, shopItem);
        this.inventory.setItem(index, shopItem.generateItem());
    }

    public GUIMenu setShouldOpenPreviousOnClose(boolean should) {
        this.shouldOpenPreviousOnClose = should;
        return this;
    }

    public void setItem(int index, @NonNull ItemStack item) {
        GUIItem is = new GUIItem(item);
        this.items.put(index, is);
        this.inventory.setItem(index, is.getItem());
    }

    public GUIItem getItem(int slot) {
        return (GUIItem) this.items.get(slot);
    }

    public void removeItem(int slot) {
        this.items.remove(slot);
        this.inventory.setItem(slot, null);
    }

    public GUIMenu setCloseCallback(Consumer<Player> event) {
        this.closeCallback = event;
        return this;
    }

    public GUIItem getBackButton(String... lore) {
        return new GUIItem(ItemManager.createItem(Material.BARRIER, ChatColor.GREEN + "Back")).setLore(lore)
                .setClick(e -> {
                    if (getPreviousGUI() == null) {
                        player.closeInventory();
                        return;
                    }
                    setShouldOpenPreviousOnClose(false);
                    player.closeInventory();
                    getPreviousGUI().open(player, null);
                });
    }

    @Override
    public void onRemove() {
        if (isShouldOpenPreviousOnClose()) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
                getPreviousGUI().open(player, null);
            });
        }
    }

    public void updateWindowTitle(final Player player, String title) {
        final EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();
        final PacketPlayOutOpenWindow packet = new PacketPlayOutOpenWindow(entityPlayer.activeContainer.windowId, "minecraft:chest", (IChatBaseComponent) new ChatMessage(title, new Object[0]), getSize());
        entityPlayer.playerConnection.sendPacket(packet);
        entityPlayer.updateInventory(entityPlayer.activeContainer);
    }

    public void reconstructGUI(Player player) {
        clear();
        setItems();
        player.updateInventory();
    }
}
