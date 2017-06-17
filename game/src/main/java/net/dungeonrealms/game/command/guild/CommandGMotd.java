package net.dungeonrealms.game.command.guild;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.common.game.database.player.Rank;
import net.dungeonrealms.common.game.database.sql.SQLDatabaseAPI;
import net.dungeonrealms.game.guild.GuildMechanics;
import net.dungeonrealms.game.guild.GuildWrapper;
import net.dungeonrealms.game.guild.database.GuildDatabase;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Class written by APOLLOSOFTWARE.IO on 6/2/2016
 */

public class CommandGMotd extends BaseCommand {

    public CommandGMotd(String command, String usage, String description) {
        super(command, usage, description);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) return false;

        Player player = (Player) sender;

        GuildWrapper wrapper = GuildDatabase.getAPI().getPlayersGuildWrapper(player.getUniqueId());
        if(wrapper == null){
            player.sendMessage(ChatColor.RED + "You must be in a " + ChatColor.BOLD + "GUILD" + ChatColor.RED + " to view " + ChatColor.BOLD + "/gmotd.");
            return true;
        }
        String guildTag = wrapper.getTag();

        if (args.length == 0) {
            GuildMechanics.getInstance().showMotd(player, guildTag, wrapper.getMotd());
            return true;
        }

        if (args.length >= 1) {
            if(!wrapper.isOwner(player.getUniqueId()) && !Rank.isGM(player)){
                player.sendMessage(ChatColor.RED + "You must be the " + ChatColor.BOLD + "GUILD OWNER" + ChatColor.RED + " to use " + ChatColor.BOLD + "/gmotd <motd>.");
                return true;
            }
            StringBuilder newMotd = new StringBuilder(args[0]);
            for (int arg = 1; arg < args.length; arg++) newMotd.append(" ").append(args[arg]);

            if (newMotd.toString().contains("$")) {
                player.sendMessage(ChatColor.RED + "MOTD contains illegal characters.");
                return true;
            }

            String motd = SQLDatabaseAPI.filterSQLInjection(newMotd.toString());
            wrapper.setMotd(motd);
//            GuildDatabaseAPI.get().setMotdOf(guildName, newMotd.toString());
//            GameAPI.updateGuildData(guildName);
            GameAPI.sendNetworkMessage("Guilds", "setmotd", DungeonRealms.getShard().getPseudoName(),wrapper.getGuildID() + "", player.getName(), motd);

            player.sendMessage(ChatColor.GRAY + "You have updated the guild " + ChatColor.BOLD.toString() + ChatColor.DARK_AQUA + "MOTD" + ChatColor.GRAY + " to:");
            GuildMechanics.getInstance().showMotd(player, guildTag, wrapper.getMotd());
            return true;
        }
        return false;
    }
}
