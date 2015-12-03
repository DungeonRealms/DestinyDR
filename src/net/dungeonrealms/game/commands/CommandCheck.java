package net.dungeonrealms.game.commands;

import net.dungeonrealms.game.commands.generic.BasicCommand;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Created by Nick on 12/2/2015.
 */
public class CommandCheck extends BasicCommand {

    public CommandCheck(String command, String usage, String description) {
        super(command, usage, description);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (!(sender instanceof Player)) {
            return false;
        }

        Player player = (Player) sender;

        if(!player.isOp()) {
            return true;
        }

        if (player.getItemInHand() == null) {
            player.sendMessage(ChatColor.RED + "There is nothing in your hand.");
            return true;
        }

        ItemStack inHand = player.getItemInHand();

        NBTTagCompound tag = CraftItemStack.asNMSCopy(inHand).getTag();


        player.sendMessage(ChatColor.RED + "EpochIdentifier: " + tag.getString("u"));

        return false;
    }
}
