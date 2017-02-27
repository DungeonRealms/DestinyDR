package net.dungeonrealms.game.quests.listeners;

import net.dungeonrealms.game.quests.Quest;
import net.dungeonrealms.game.quests.Quests;
import net.dungeonrealms.game.quests.objectives.ObjectiveKill;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;

public class KillObjectiveListener implements Listener {
	@EventHandler
	public void onPlayerInteractEntity(PlayerInteractAtEntityEvent evt){
		for(Quest quest : Quests.getInstance().questStore.getList())
			quest.getStageList().stream().filter(stage -> stage.getObjective() instanceof ObjectiveKill)
				.forEach(stage -> ((ObjectiveKill)stage.getObjective()).handleEntityInteract(evt));
	}
}
