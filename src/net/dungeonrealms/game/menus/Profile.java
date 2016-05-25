package net.dungeonrealms.game.menus;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
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
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;

import java.util.logging.Logger;

import static com.comphenix.protocol.PacketType.Play.Client.CLIENT_COMMAND;
import static com.comphenix.protocol.PacketType.Play.Client.WINDOW_CLICK;

public class Profile implements Listener {

    static Logger log = Logger.getLogger("Minecraft");

    public void onEnable() {
        log.info("Enabling Profiles");
        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(DungeonRealms.getInstance(), CLIENT_COMMAND, WINDOW_CLICK) {
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
        });
        Bukkit.getServer().getPluginManager().registerEvents(this, DungeonRealms.getInstance());
        log.info("Enabled Profiles");
    }

    public static ItemStack getItem(Player player) {
        return ItemManager.getPlayerProfile(player, ChatColor.WHITE.toString() + ChatColor.BOLD + "Character Profile", new String[]{
                ChatColor.GREEN + "Open Profile"});
    }

    public void onDisable() {
        log.info("Disabled Profiles");
    }

    private static void callEvent(Player player) {
        player.closeInventory();
        PlayerMenus.openPlayerProfileMenu(player);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void inventoryClick(InventoryClickEvent event) {
        if (event.getInventory() instanceof CraftingInventory && event.getInventory().getSize() == 5 && event.getRawSlot() == 1) {
            if (event.getWhoClicked().getGameMode() == GameMode.CREATIVE) return;
            event.setCancelled(true);
            callEvent((Player) event.getWhoClicked());
        }
    }
}

