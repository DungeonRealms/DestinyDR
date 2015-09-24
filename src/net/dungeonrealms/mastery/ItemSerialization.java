package net.dungeonrealms.mastery;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.server.v1_8_R3.PacketDataSerializer;

public class ItemSerialization {
	/**
	 * Serializes a list of items to a String
	 * 
	 * @param event
	 * @since 1.0
	 */
	public static String serialize(Collection<ItemStack> items) throws IOException {
		ByteArrayInputStream ret = null;
		if (!items.isEmpty()) {
			ByteBuf buf = Unpooled.buffer();
			PacketDataSerializer serializer = new PacketDataSerializer(buf);
			buf.writeInt(items.size());
			for (ItemStack item : items)
			serializer.a(CraftItemStack.asNMSCopy(item));
			ret = new ByteArrayInputStream(serializer.array());
		}
		return IOUtils.toString(ret, "UTF-8");
	}

	/**
	 * Deserializes a String to a list of items.
	 * 
	 * @param event
	 * @since 1.0
	 */
	public static List<ItemStack> deserialize(String source) throws IOException {
		InputStream input = IOUtils.toInputStream(source, "UTF-8");
		List<ItemStack> items = new ArrayList<>();
		DataInputStream in = new DataInputStream(input);
		ByteBuf buf = Unpooled.buffer();
		buf.writeBytes(IOUtils.toByteArray(in));
		PacketDataSerializer serializer = new PacketDataSerializer(buf);
		try {
			int count = buf.readInt();
			for (int i = 0; i <= count; i++) {
			net.minecraft.server.v1_8_R3.ItemStack item = serializer.i();
			if (item != null) {
				items.add(CraftItemStack.asCraftMirror(item));
			}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (items.size() == 0)
			items.add(new ItemStack(Material.AIR));
		return items;
	}
}
