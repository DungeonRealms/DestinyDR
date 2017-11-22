package net.dungeonrealms.game.command;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.common.game.database.player.Rank;
import net.dungeonrealms.game.listener.MainListener;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;

public class CommandRoomba extends BaseCommand {

    public CommandRoomba() {
        super("roomba", "/<command>", "Remove all commands on ground.");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player;
        if (sender instanceof Player ) {
            player = (Player) sender;
            if(!Rank.isHeadGM(player)){return false;}
            GameAPI.roomba(GameAPI.getGamePlayer(player).getPlayer().getWorld().getLoadedChunks());
            player.sendMessage("Item(s) removed.");
        }
        return true;
    }

}
