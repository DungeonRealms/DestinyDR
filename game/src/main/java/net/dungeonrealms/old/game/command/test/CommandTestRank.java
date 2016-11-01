package net.dungeonrealms.old.game.command.test;

import net.dungeonrealms.common.network.enumeration.EnumShardType;
import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.common.game.database.player.rank.Rank;
import net.dungeonrealms.vgame.Game;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandTestRank extends BaseCommand {

    public CommandTestRank(String command, String usage, String description) {
    	super(command, usage, description);
	}
	
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		Player p = (Player)sender;

		// This command can only be executed from US-0 or if the player is an OP on a live shard.
		if (!(Game.getGame().getGameShard().getShardType() == EnumShardType.MASTER && !p.isOp())) return false;

		p.sendMessage("Developer: " + Rank.isDev(p));
		p.sendMessage("Game Master: " + Rank.isGM(p));
		p.sendMessage("Support Agent: " + Rank.isSupport(p));
		p.sendMessage("Player Moderator: " + Rank.isPMOD(p));
		p.sendMessage("YouTuber: " + Rank.isYouTuber(p));
		p.sendMessage("Subscriber: " + Rank.isSubscriber(p));

		return true;
    }

}
