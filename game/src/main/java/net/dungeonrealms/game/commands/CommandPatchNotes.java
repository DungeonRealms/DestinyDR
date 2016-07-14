package net.dungeonrealms.game.commands;

import io.netty.buffer.Unpooled;
import net.dungeonrealms.tool.PatchTools;
import net.minecraft.server.v1_9_R2.EnumHand;
import net.minecraft.server.v1_9_R2.PacketDataSerializer;
import net.minecraft.server.v1_9_R2.PacketPlayOutCustomPayload;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * Class written by APOLLOSOFTWARE.IO on 7/10/2016
 */
public class CommandPatchNotes extends BasicCommand {

    public CommandPatchNotes(String command, String usage, String description, List<String> aliases) {
        super(command, usage, description, aliases);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) return true;

        Player player = (Player) sender;

        final ItemStack savedItem = player.getInventory().getItemInMainHand();

        player.getInventory().setItemInMainHand(new ItemStack(PatchTools.getInstance().getPatchBook()));
        PacketDataSerializer packetdataserializer = new PacketDataSerializer(Unpooled.buffer());

        packetdataserializer.a(EnumHand.MAIN_HAND);
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(new PacketPlayOutCustomPayload("MC|BOpen", packetdataserializer));

        player.getInventory().setItemInMainHand(savedItem);
        return true;
    }
}
