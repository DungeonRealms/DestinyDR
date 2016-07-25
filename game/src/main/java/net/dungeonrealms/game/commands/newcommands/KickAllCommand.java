package net.dungeonrealms.game.commands.newcommands;

import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.game.commands.BasicCommand;
import net.dungeonrealms.common.game.database.player.rank.Rank;
import net.dungeonrealms.game.world.shops.ShopMechanics;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class KickAllCommand extends BasicCommand {

    public KickAllCommand(String command, String usage, String description) {
        super(command, usage, description);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (!Rank.isDev(player)) return false;
        }

        sender.sendMessage(ChatColor.RED + "Kicking all players..");
        ShopMechanics.deleteAllShops(true);
        GameAPI.logoutAllPlayers();
        return true;
    }

}
