package net.dungeonrealms.game.mechanic.dungeons;

import java.util.List;

import org.bukkit.entity.Player;

public class BanditTrove extends Dungeon {
	public BanditTrove(List<Player> players) {
		super(DungeonType.BANDIT_TROVE, players);
	}
}
