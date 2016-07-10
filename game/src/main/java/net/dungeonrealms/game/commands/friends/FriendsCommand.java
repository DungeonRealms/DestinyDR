package net.dungeonrealms.game.commands.friends;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.dungeonrealms.game.commands.generic.BasicCommand;
import net.dungeonrealms.game.mechanics.ItemManager;
import net.dungeonrealms.tool.PatchTools;
import net.minecraft.server.v1_9_R2.EnumHand;
import net.minecraft.server.v1_9_R2.PacketDataSerializer;
import net.minecraft.server.v1_9_R2.PacketPlayOutCustomPayload;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * Created by chase on 7/7/2016.
 */
public class FriendsCommand extends BasicCommand {

    public FriendsCommand(String command, String usage, String description, List<String> aliases) {
        super(command, usage, description, aliases);
    }

    @Override
    public boolean onCommand(CommandSender s, Command cmd, String string, String[] args) {
        if (s instanceof ConsoleCommandSender) return false;
        Player player = (Player) s;
        ItemStack book = ItemManager.createCharacterJournal(player);
        final ItemStack savedItem = player.getInventory().getItemInMainHand();
        player.getInventory().setItemInMainHand(book);
        PacketDataSerializer packetdataserializer = new PacketDataSerializer(Unpooled.buffer());
        packetdataserializer.a(EnumHand.MAIN_HAND);
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(new PacketPlayOutCustomPayload("MC|BOpen", packetdataserializer));
        player.getInventory().setItemInMainHand(savedItem);
        return false;
    }
}
