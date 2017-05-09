package net.dungeonrealms.game.player.menu;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketListener;
import com.comphenix.protocol.wrappers.EnumWrappers;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.game.item.ItemType;
import net.dungeonrealms.game.item.items.functional.ItemHearthstone;
import net.dungeonrealms.game.item.items.functional.ItemPlayerProfile;
import net.dungeonrealms.game.item.items.functional.ecash.ItemMount;
import net.dungeonrealms.game.item.items.functional.ecash.ItemMuleMount;
import net.dungeonrealms.game.item.items.functional.ecash.ItemParticleTrail;
import net.dungeonrealms.game.item.items.functional.ecash.ItemPet;
import net.dungeonrealms.game.mechanic.PlayerManager;
import net.md_5.bungee.api.ChatColor;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.CraftingInventory;
import static com.comphenix.protocol.PacketType.Play.Client.CLIENT_COMMAND;

public class CraftingMenu implements Listener {

    private static PacketListener listener;

    public void onEnable() {
        listener = new PacketAdapter(DungeonRealms.getInstance(), CLIENT_COMMAND) {
            @Override
            public void onPacketReceiving(PacketEvent event) {
                PacketContainer packet = event.getPacket();
                Player player = event.getPlayer();
                if (player.getGameMode() != GameMode.SURVIVAL) return;
                PacketType type = packet.getType();
                if (type == CLIENT_COMMAND && packet.getClientCommands().read(0) == EnumWrappers.ClientCommand.OPEN_INVENTORY_ACHIEVEMENT) {
                    if (player.getOpenInventory().getTopInventory() instanceof CraftingInventory) {
                    	player.getOpenInventory().getTopInventory().setItem(1, new ItemPlayerProfile(player).generateItem());
                        player.getOpenInventory().getTopInventory().setItem(2, new ItemHearthstone(player).generateItem());
                    }
                    GameAPI.runAsSpectators(player, (spectator) -> {
                    	spectator.sendMessage(ChatColor.YELLOW + player.getName() + " opened their inventory.");
                    	Bukkit.getScheduler().runTask(DungeonRealms.getInstance(), () -> spectator.openInventory(player.getInventory()));
                    });
                }
            }
        };
        ProtocolLibrary.getProtocolManager().addPacketListener(listener);
        Bukkit.getServer().getPluginManager().registerEvents(this, DungeonRealms.getInstance());
    }

    public void onDisable() {
        ProtocolLibrary.getProtocolManager().removePacketListener(listener);
        HandlerList.unregisterAll(this);
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCraftingInventoryClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        if (player.getOpenInventory().getTopInventory() instanceof CraftingInventory) {
            player.getOpenInventory().getTopInventory().setItem(1, null);
            player.getOpenInventory().getTopInventory().setItem(2, null);
        }
    }
    
    public static void addMountItem(Player player) {
    	if (PlayerManager.hasItem(player.getInventory(), ItemType.MOUNT))
    		return;
        player.getInventory().addItem(new ItemMount().generateItem());
    }

    public static void addPetItem(Player player) {
    	if (PlayerManager.hasItem(player.getInventory(), ItemType.PET))
    		return;
        player.getInventory().addItem(new ItemPet().generateItem());
    }

    public static void addMuleItem(Player player) {
        if (PlayerManager.hasItem(player.getInventory(), ItemType.MULE))
        	return;
        player.getInventory().addItem(new ItemMuleMount(player).generateItem());
    }

    public static void addTrailItem(Player player) {
    	if (PlayerManager.hasItem(player.getInventory(), ItemType.PARTICLE_TRAIL))
    		return;
    	
    	player.getInventory().addItem(new ItemParticleTrail().generateItem());
    }

}

