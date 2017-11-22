package net.dungeonrealms.game.command;

import net.dungeonrealms.common.game.command.BaseCommand;

import net.dungeonrealms.game.donation.DonationEffects;
import net.dungeonrealms.game.mechanic.data.EnumBuff;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandBuffTime extends BaseCommand{

    public CommandBuffTime() {
        super("bufftime", "/<command>", "Shows the remaining time on a buff.");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if ((sender instanceof Player)) {
            Player player = (Player) sender;
            DonationEffects de = DonationEffects.getInstance();
            boolean hasBuff = false;
            for (EnumBuff buffType : EnumBuff.values()) {
                if (de.hasBuff(buffType)) {
                    hasBuff = true;
                   de.doLogin(player);
                }
            }
            if (!hasBuff){
                player.sendMessage(ChatColor.DARK_RED + "" + ChatColor.BOLD + "There is no active buff!");
            }
        }
        return true;
    }
}
