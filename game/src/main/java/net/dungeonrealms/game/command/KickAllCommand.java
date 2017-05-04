package net.dungeonrealms.game.command;

import lombok.Cleanup;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.common.game.database.player.rank.Rank;
import net.dungeonrealms.game.world.shops.ShopMechanics;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.PreparedStatement;

public class KickAllCommand extends BaseCommand {

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
        try {
            @Cleanup PreparedStatement statement = ShopMechanics.deleteAllShops(true);
            statement.executeBatch();
        } catch (Exception e) {
            e.printStackTrace();
        }
        GameAPI.logoutAllPlayers();
        return true;
    }

}
