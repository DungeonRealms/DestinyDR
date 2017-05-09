package net.dungeonrealms.common.game.util;

import net.minecraft.server.v1_9_R2.Packet;

import org.bukkit.craftbukkit.v1_9_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;

/**
 * PacketUtils - A utility that allows us to use less version specific code, or at the very least use it all in one place.
 * TODO: Add an easy way to use reflection to send packets.
 * 
 * Created May 5th, 2017.
 * @author Kneesnap
 */
public class PacketUtils {

	/**
	 * Send an NMS Packet to the player.
	 */
	public static void sendPacket(Player player, Packet<?> packet) {
		((CraftPlayer)player).getHandle().playerConnection.sendPacket(packet);
	}
}
