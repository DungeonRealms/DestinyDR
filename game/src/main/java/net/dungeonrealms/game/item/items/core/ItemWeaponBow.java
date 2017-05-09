package net.dungeonrealms.game.item.items.core;

import net.dungeonrealms.game.item.ItemType;
import net.dungeonrealms.game.mechanic.data.ScrapTier;
import net.dungeonrealms.game.world.item.DamageAPI;
import net.minecraft.server.v1_9_R2.DataWatcher;
import net.minecraft.server.v1_9_R2.DataWatcherObject;
import net.minecraft.server.v1_9_R2.DataWatcherRegistry;
import net.minecraft.server.v1_9_R2.PacketPlayOutEntityMetadata;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * A simple Bow Item
 * @author Kneesnap
 */
public class ItemWeaponBow extends ItemWeaponRanged {
	
	public ItemWeaponBow() {
		super(ItemType.BOW);
	}
	
	public ItemWeaponBow(ItemStack item) {
		super(item);
	}
	
	public static boolean isBow(ItemStack item) {
		return isType(item, ItemType.BOW);
	}

	@Override
	public int getShootDelay() {
		return 650;
	}

	@Override
	public Sound getShootSound() {
		return Sound.ENTITY_ARROW_SHOOT;
	}

	@Override
	public void fireProjectile(Player player, boolean takeDurability) {
		//Unsure what this packet does. I think it makes the player's bow firing silent to other players.
		//However, I am not entirely sure because this the event is cancelled that sound shouldn't play.
		//Will look into later.
		DataWatcher watcher = new DataWatcher(((CraftPlayer) player).getHandle());
        watcher.register(new DataWatcherObject<>(5, DataWatcherRegistry.a), (byte) 1);
        for (Player player1 : Bukkit.getOnlinePlayers())
            if (player != player1)
                ((CraftPlayer) player1).getHandle().playerConnection.sendPacket(new PacketPlayOutEntityMetadata(((CraftPlayer) player).getHandle().getId(), watcher, true));
        
        DamageAPI.fireBowProjectile(player, this, takeDurability);
	}
	
	@Override
	public int getRepairParticle(ScrapTier tier) {
		return 5;
	}
}
