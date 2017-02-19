package net.dungeonrealms.game.quests.gui;

import net.dungeonrealms.game.quests.NPCAnimation;
import net.dungeonrealms.game.quests.QuestNPC;
import net.md_5.bungee.api.ChatColor;

import org.bukkit.Material;
import org.bukkit.entity.Player;

public class GuiNPCSetAnimation extends GuiBase {

	private QuestNPC npc;
	
	public GuiNPCSetAnimation(Player player, QuestNPC npc) {
		super(player, "Select Animation", 1);
		this.npc = npc;
	}
	
	@Override
	public void createGUI(){
		for(int i = 0; i < NPCAnimation.values().length; i++){
			NPCAnimation a = NPCAnimation.values()[i];
			this.setSlot(i, Material.LAPIS_BLOCK, ChatColor.GREEN + a.getDisplayName(), new String[] {"Click here change the NPC animation."}, (evt) -> {
				npc.setAnimation(a);
				player.sendMessage(ChatColor.GREEN + "Animation Updated.");
				new GuiNPCEditor(player, npc);
			});
		}
		
		this.setSlot(8, GO_BACK, (evt) -> new GuiNPCEditor(player, npc));
	}
}
