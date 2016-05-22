package net.dungeonrealms.game.menus;

import static com.comphenix.protocol.PacketType.Play.Client.CLIENT_COMMAND;
import static com.comphenix.protocol.PacketType.Play.Client.WINDOW_CLICK;

import java.lang.reflect.InvocationTargetException;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.wrappers.EnumWrappers;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.game.mechanics.ItemManager;
import net.dungeonrealms.game.player.inventory.PlayerMenus;
import net.md_5.bungee.api.ChatColor;
import net.minecraft.server.v1_8_R3.NBTTagCompound;

public class Profile implements Listener {
	
    static Logger log = Logger.getLogger("Minecraft");
    
    public void onEnable()
    {
    	log.info("Enabling Profiles");
        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(DungeonRealms.getInstance(),
                CLIENT_COMMAND, WINDOW_CLICK) {
            @Override
            public void onPacketReceiving(PacketEvent event) {
                PacketContainer packet = event.getPacket();
                ProtocolManager pm = ProtocolLibrary.getProtocolManager();
                    Player player = event.getPlayer();
                    if(player.getGameMode() != GameMode.SURVIVAL) return;
                    PacketType type = packet.getType();
                    StructureModifier<Integer> ints = packet.getIntegers();
                    if (type == CLIENT_COMMAND && packet.getClientCommands().read(0) == EnumWrappers.ClientCommand.OPEN_INVENTORY_ACHIEVEMENT) {
                    	try {
                        		ItemStack item = getItem(player, Slot.IN_1);
                        		if(item != null)
                        		{
								pm.sendServerPacket(player, createSlotPacket(0, 1, item));
                        		}
							} catch (InvocationTargetException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
                        }
            }
        });
        Bukkit.getServer().getPluginManager().registerEvents(this, DungeonRealms.getInstance());
		log.info("Enabled Profiles");
    }
    private enum Slot {
        RESULT, IN_1, IN_2, IN_3, IN_4
    }

    private static ItemStack getItem(Player player, Slot slot) {
        return ItemManager.getPlayerProfile(player, ChatColor.WHITE.toString() + ChatColor.BOLD + "Character Profile", new String[]{
ChatColor.GREEN + "Open Profile"});
    }
    
    private static ItemStack getItem(Player player) {
        return ItemManager.getPlayerProfile(player, ChatColor.WHITE.toString() + ChatColor.BOLD + "Character Profile", new String[]{
ChatColor.GREEN + "Open Profile"});
    }

    private PacketContainer createSlotPacket(int windowId, int slot, ItemStack item) {
        PacketContainer packet = new PacketContainer(PacketType.Play.Server.SET_SLOT);
        packet.getIntegers().write(0, windowId).write(1, slot);
        packet.getItemModifier().write(0, item);
        return packet;
    }
    public void onDisable()
    {
    	log.info("Disabled Profiles");
    }
    
    public static void callEvent(Player player)
    {
    	player.closeInventory();
        PlayerMenus.openPlayerProfileMenu(player);
    }
    
    @EventHandler(ignoreCancelled = true)
    void inventoryClick(InventoryClickEvent event) {
		Bukkit.broadcastMessage("HI");
    	if (event.getWhoClicked() instanceof Player)
    	{
    		if(!(event.getInventory() instanceof CraftingInventory))
    		{
    			event.getWhoClicked().closeInventory();
    		}
    	}
    	if (event.getWhoClicked() instanceof Player
                && event.getInventory() instanceof CraftingInventory && event.getInventory().getSize() == 5
                && event.getRawSlot() == 1) {
            Player player = (Player) event.getWhoClicked();
            if(player.getGameMode() != GameMode.SURVIVAL) return;
            if(player.getOpenInventory().getCursor() == getItem(player))
            {
            	event.setCancelled(true);
            }
            Bukkit.getScheduler().runTask(DungeonRealms.getInstance(), () -> {
                try {
                    ProtocolManager pm = ProtocolLibrary.getProtocolManager();
                    pm.sendServerPacket(player, createSlotPacket(-1, -1, player.getOpenInventory().getCursor()));
                    ItemStack item = getItem(player, Slot.IN_1);
                    if (item != null) {
                        //pm.sendServerPacket(player, createSlotPacket(0, 1, item));
                    	callEvent(player);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });
        }
    }
}

