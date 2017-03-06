package net.dungeonrealms.game.anticheat;

import java.util.Arrays;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.game.mechanic.generic.EnumPriority;
import net.dungeonrealms.game.mechanic.generic.GenericMechanic;
import net.dungeonrealms.game.world.item.Item.ItemRarity;
import net.md_5.bungee.api.ChatColor;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketListener;

public class PacketModifier implements GenericMechanic {
	
	private PacketListener listener;
	
	@Override
	public void startInitialization() {
		//This makes it so players can't tell which mobs will drop what items, or learn what items another player is using.
		listener = new PacketAdapter(DungeonRealms.getInstance(), PacketType.Play.Server.ENTITY_EQUIPMENT) {
	    	@Override
	    	public void onPacketSending(PacketEvent event) {
	    		PacketContainer packet = event.getPacket();
	    		ItemStack item = packet.getItemModifier().read(0);
	    		if(item == null || item.getType() == Material.AIR)
	    			return;
	    		item = item.clone();
	    		ItemMeta meta = item.getItemMeta();
	    		if(meta.hasLore())
	    			meta.setLore(Arrays.asList(ItemRarity.UNIQUE.getName()));
	    		if(meta.hasDisplayName())
	    			meta.setDisplayName(ChatColor.GOLD + "Legendary Snapper..?");
	    		item.setDurability((short)0);
	    		item.setItemMeta(meta);
	    		packet.getItemModifier().write(0, item);
	    	}
	    };
	    ProtocolLibrary.getProtocolManager().addPacketListener(listener);
	}

	@Override
	public void stopInvocation() {
		ProtocolLibrary.getProtocolManager().removePacketListener(listener);
	}
	
	@Override
	public EnumPriority startPriority() {
		return EnumPriority.CARDINALS;
	}
}
