package net.dungeonrealms.game.quests.listeners;


import net.citizensnpcs.api.event.NPCSpawnEvent;
import net.dungeonrealms.game.quests.Quest;
import net.dungeonrealms.game.quests.QuestNPC;
import net.dungeonrealms.game.quests.Quests;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.inventory.EquipmentSlot;

/**
 * Handles all player and npc quest events.
 * 
 * @author Kneesnap
 */
public class NPCListener implements Listener {
	
	@EventHandler
	public void onPlayerInteractWithEntity(PlayerInteractAtEntityEvent evt){
		if(evt.getHand() == EquipmentSlot.OFF_HAND) return;
		for(Quest q : Quests.getInstance().questStore.getList())
			if(q.isQuestNPC(evt.getRightClicked()))
				q.handleNPCClick(q.getQuestNPC(evt.getRightClicked()), evt.getPlayer());
	}
	
	@EventHandler
	public void onNPCSpawn(NPCSpawnEvent event){
		for(Quest q : Quests.getInstance().questStore.getList()){
			if(q.isQuestNPC(event.getNPC().getEntity())){
				QuestNPC npc = q.getQuestNPC(event.getNPC().getEntity());
				if(npc.getSkinOwner() != null)
					npc.setSkin(npc.getSkinOwner());
				npc.setAnimation(npc.getAnimation());
			}
		}
	}

	@EventHandler
	public void onEntityDamage(EntityDamageEvent event) {
		for(Quest q : Quests.getInstance().questStore.getList())
			if(q.isQuestNPC(event.getEntity()))
				event.setCancelled(true);
	}
}
