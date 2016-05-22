package net.dungeonrealms.game.menus;

import static com.comphenix.protocol.PacketType.Play.Client.CLIENT_COMMAND;
import static com.comphenix.protocol.PacketType.Play.Client.WINDOW_CLICK;


import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketListener;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.wrappers.EnumWrappers;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.game.player.combat.CombatLog;
import net.dungeonrealms.game.player.inventory.PlayerMenus;
import net.dungeonrealms.game.world.teleportation.TeleportAPI;
import net.dungeonrealms.game.world.teleportation.Teleportation;
import net.md_5.bungee.api.ChatColor;

public class HearthStone implements Listener {
	
    static Logger log = Logger.getLogger("Minecraft");
    private static PacketListener listener;

    public void onEnable()
    {
    	log.info("Enabling Hearthstone Mechanics Extension");
    	listener = new PacketAdapter(DungeonRealms.getInstance(),
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
                        		ItemStack item = getItem(player, Slot.IN_2);
                        		if(item != null)
                        		{
								pm.sendServerPacket(player, createSlotPacket(0, 2, item));
                        		}
							} catch (InvocationTargetException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
                        }
            }
        };
        ProtocolLibrary.getProtocolManager().addPacketListener(listener);
        Bukkit.getServer().getPluginManager().registerEvents(this, DungeonRealms.getInstance());
		log.info("Enabled Hearthstone Mechanics Extension");
    }
    
    public void onDisable()
    {
    	ProtocolLibrary.getProtocolManager().removePacketListener(listener);
		HandlerList.unregisterAll(this);
    	log.info("Disabled Hearthstone Mechanics Expansion");
    }
    
    private enum Slot {
        RESULT, IN_1, IN_2, IN_3, IN_4
    }
    public static ItemStack createCustomItem(ItemStack is, String name, List<String> lore) {
        ItemMeta im = is.getItemMeta();
        if (name != null) {
            im.setDisplayName(name);
        }
        if (lore != null) {
            im.setLore(lore);
        }
        is.setItemMeta(im);
        return is;
    }
    private static ItemStack getItem(Player player, Slot slot) {
    	return PlayerMenus.editItem(new ItemStack(Material.QUARTZ), ChatColor.GREEN + "Hearthstone", new String[]{
                ChatColor.DARK_GRAY + "Home location",
                "",
                ChatColor.GRAY + "Use: Returns you to ",
                ChatColor.YELLOW + TeleportAPI.getLocationFromDatabase(player.getUniqueId()),
                "",
                ChatColor.YELLOW + "Speak to an Innkeeper to change location."
        });
    }
    
    private static ItemStack getItem(Player player) {
    	return PlayerMenus.editItem(new ItemStack(Material.QUARTZ), ChatColor.GREEN + "Hearthstone", new String[]{
                ChatColor.DARK_GRAY + "Home location",
                "",
                ChatColor.GRAY + "Use: Returns you to ",
                ChatColor.YELLOW + TeleportAPI.getLocationFromDatabase(player.getUniqueId()),
                "",
                ChatColor.YELLOW + "Speak to an Innkeeper to change location."
        });
    }

    private PacketContainer createSlotPacket(int windowId, int slot, ItemStack item) {
        PacketContainer packet = new PacketContainer(PacketType.Play.Server.SET_SLOT);
        packet.getIntegers().write(0, windowId).write(1, slot);
        packet.getItemModifier().write(0, item);
        return packet;
    }
    
    public static void callEvent(Player player)
    {
    	player.closeInventory();
        if (!(CombatLog.isInCombat(player))) {
            if (TeleportAPI.isPlayerCurrentlyTeleporting(player.getUniqueId())) {
                player.sendMessage("You cannot restart a teleport during a cast!");
                return;
            }
            if (TeleportAPI.canUseHearthstone(player.getUniqueId())) {
                net.minecraft.server.v1_8_R3.ItemStack nmsItem = CraftItemStack.asNMSCopy(getItem(player));
                Teleportation.getInstance().teleportPlayer(player.getUniqueId(), Teleportation.EnumTeleportType.HEARTHSTONE, nmsItem.getTag());
            } else {
                player.sendMessage(ChatColor.RED + "You currently cannot use your Hearthstone because of Alignment, World or Cooldown issues!" + " (" + TeleportAPI.getPlayerHearthstoneCD(player.getUniqueId()) + "s)");
            }
        } else {
            player.sendMessage(ChatColor.GREEN.toString() + ChatColor.BOLD + "TELEPORT " + ChatColor.RED + "You are in combat! " + ChatColor.RED.toString() + "(" + ChatColor.UNDERLINE + CombatLog.COMBAT.get(player) + "s" + ChatColor.RED + ")");
        }
    }
    
    @EventHandler(ignoreCancelled = true)
    void inventoryClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player
                && event.getInventory() instanceof CraftingInventory && event.getInventory().getSize() == 5
                && event.getRawSlot() == 2) {
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
                    ItemStack item = getItem(player, Slot.IN_2);
                    if (item != null) {
                    	callEvent(player);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });
        }
    }

}

