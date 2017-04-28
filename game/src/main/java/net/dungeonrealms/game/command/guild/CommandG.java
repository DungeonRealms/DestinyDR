package net.dungeonrealms.game.command.guild;

import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.Constants;
import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.game.guild.GuildMechanics;
import net.dungeonrealms.game.guild.GuildMember;
import net.dungeonrealms.game.guild.GuildWrapper;
import net.dungeonrealms.game.guild.database.GuildDatabase;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;

/**
 * Class written by APOLLOSOFTWARE.IO on 6/16/2016
 */
public class CommandG extends BaseCommand {

    public CommandG(String command, String usage, String description) {
        super(command, usage, description);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) return false;

        Player player = (Player) sender;

        PlayerWrapper playerWrapper = PlayerWrapper.getPlayerWrapper(player);
        if(playerWrapper == null) {
            Constants.log.info("Null playerwrapper for player " + player.getName() + " on command G");
            return true;
        }

        GuildWrapper wrapper = GuildDatabase.getAPI().getPlayersGuildWrapper(player.getUniqueId());

        if (wrapper == null) {
            player.sendMessage(ChatColor.RED + "You must be in a " + ChatColor.BOLD + "GUILD" + ChatColor.RED + " to use " + ChatColor.BOLD + "GUILD CHAT.");
            player.sendMessage(ChatColor.GRAY + "Use /gl (or tab) to speak in global.");
            return true;
        }

        if (args.length == 0) {
            GuildMechanics.getInstance().toggleGuildChat(player);
            return true;
        }

        GuildMember member = wrapper.getMembers().get(playerWrapper.getAccountID());
        if(member == null) {
            Constants.log.info("Null guild member object on command G for player " + player.getName() + " for the guild " + wrapper.getName());
            return true;
        }




        if(!member.isAccepted()) return true;

        String tag = wrapper.getTag();
        String format = ChatColor.DARK_AQUA.toString() + "<" + ChatColor.BOLD + tag + ChatColor.DARK_AQUA + ">" + ChatColor.GRAY + " " + player.getName() + ": " + ChatColor.GRAY;
        StringBuilder msg = new StringBuilder(format);
        for (int arg = 0; arg < args.length; arg++) msg.append(" ").append(args[arg]);
        wrapper.sendGuildMessage(msg.toString(), false);
//        GuildMechanics.getInstance().sendMessageToGuild(guildName, msg.toString());
        return false;
    }

}
