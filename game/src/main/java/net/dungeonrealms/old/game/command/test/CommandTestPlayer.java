package net.dungeonrealms.old.game.command.test;

import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.old.game.soundtrack.EnumSong;
import net.dungeonrealms.old.game.soundtrack.SongPlayer;
import net.dungeonrealms.old.game.soundtrack.Soundtrack;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;

/**
 * Class written by APOLLOSOFTWARE.IO on 8/2/2016
 */
public class CommandTestPlayer extends BaseCommand {

    public CommandTestPlayer(String command, String usage, String description) {
        super(command, usage, description);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.isOp() && !(sender instanceof ConsoleCommandSender)) return false;

        SongPlayer player = Soundtrack.getInstance().getPlayer(EnumSong.TEST);

        if (player.isPlaying())
            player.setPlaying(false);

        Bukkit.getOnlinePlayers().forEach(player::addPlayer);
        player.setPlaying(true);
        return false;
    }


}