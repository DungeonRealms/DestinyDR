package net.dungeonrealms.game.menus;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketListener;
import com.comphenix.protocol.wrappers.EnumWrappers;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.game.mechanics.ItemManager;
import net.dungeonrealms.game.player.inventory.PlayerMenus;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;

import static com.comphenix.protocol.PacketType.Play.Client.CLIENT_COMMAND;
import static com.comphenix.protocol.PacketType.Play.Client.WINDOW_CLICK;

public class Profile implements Listener {

    private static PacketListener listener;

    public void onEnable() {
        listener = new PacketAdapter(DungeonRealms.getInstance(), CLIENT_COMMAND, WINDOW_CLICK) {
            @Override
            public void onPacketReceiving(PacketEvent event) {
                PacketContainer packet = event.getPacket();
                Player player = event.getPlayer();
                if (player.getGameMode() == GameMode.CREATIVE) return;
                PacketType type = packet.getType();
                if (type == CLIENT_COMMAND && packet.getClientCommands().read(0) == EnumWrappers.ClientCommand.OPEN_INVENTORY_ACHIEVEMENT) {
                    player.getOpenInventory().getTopInventory().setItem(1, getItem(player));
                }
            }
        };
        ProtocolLibrary.getProtocolManager().addPacketListener(listener);
        Bukkit.getServer().getPluginManager().registerEvents(this, DungeonRealms.getInstance());
    }

    public static ItemStack getItem(Player player) {
        return ItemManager.getPlayerProfile(player, ChatColor.WHITE.toString() + ChatColor.BOLD + "Character Profile", new String[]{ChatColor.GREEN + "Open Profile"});
    }

    public void onDisable() {
        ProtocolLibrary.getProtocolManager().removePacketListener(listener);
        HandlerList.unregisterAll(this);
    }

    private static void callEvent(Player player) {
        player.closeInventory();
        PlayerMenus.openPlayerProfileMenu(player);
    }

    private static void addMountItem(Player player) {
        player.getInventory().addItem(ItemManager.getPlayerMountItem());
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void inventoryClick(InventoryClickEvent event) {
        if (event.getInventory() instanceof CraftingInventory && event.getInventory().getSize() == 5 && event.getRawSlot() == 1) {
            if (event.getWhoClicked().getGameMode() == GameMode.CREATIVE) return;
            event.setCancelled(true);
            callEvent((Player) event.getWhoClicked());
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onInventoryDrag(InventoryClickEvent event) {
        if (event.getInventory().getTitle().equals("Profile")) {
            if (event.getClick() == ClickType.MIDDLE) {
                if (event.getRawSlot() == 7) {
                    event.setCancelled(true);
                    addMountItem((Player) event.getWhoClicked());
                }
            }
        }
    }
}

