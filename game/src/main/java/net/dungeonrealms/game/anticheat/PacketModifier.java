package net.dungeonrealms.game.anticheat;

import java.util.Arrays;
import java.util.List;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.game.mechanic.generic.EnumPriority;
import net.dungeonrealms.game.mechanic.generic.GenericMechanic;
import net.dungeonrealms.game.world.item.Item.ItemRarity;
import net.md_5.bungee.api.ChatColor;
import net.minecraft.server.v1_9_R2.NBTTagCompound;

import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_9_R2.inventory.CraftItemStack;
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
	private List<String> ALLOWED_TAGS = Arrays.asList("display", "pages", "generation", "SkullOwner", "AttributeModifiers", "ench", "Unbreakable", "HideFlags", "CanDestroy", "PickupDelay", "CanPlaceOn");
	
	@Override
	public void startInitialization() {
		//This makes it so players can't tell which mobs will drop what items, or learn what items another player is using.
		listener = new PacketAdapter(DungeonRealms.getInstance(), PacketType.Play.Server.ENTITY_EQUIPMENT) {
	    	@Override
	    	public void onPacketSending(PacketEvent event) {
	    		PacketContainer packet = event.getPacket();
	    		ItemStack original = packet.getItemModifier().read(0);
	    		if(original == null || original.getType() == Material.AIR)
	    			return;
	    		//Remove all data the client doesn't need to see.
	    		ItemStack item = stripNBT(original);
	    		ItemMeta meta = item.getItemMeta();
	    		if(meta.hasLore())
	    			meta.setLore(Arrays.asList(ItemRarity.UNIQUE.getName()));
	    		if(meta.hasDisplayName())
	    			meta.setDisplayName(ChatColor.GOLD + "Legendary Snapper..?");
	    		if(GameAPI.isArmor(item) || GameAPI.isWeapon(item))
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
	
	public ItemStack stripNBT(ItemStack item){
		net.minecraft.server.v1_9_R2.ItemStack stripped = CraftItemStack.asNMSCopy(item.clone());
		if(stripped.hasTag()){
			NBTTagCompound tag = new NBTTagCompound();
			for(String key : stripped.getTag().c())
				if(ALLOWED_TAGS.contains(key))
					tag.set(key, stripped.getTag().get(key));
			stripped.setTag(tag);
		}
		return CraftItemStack.asBukkitCopy(stripped);
	}
}
