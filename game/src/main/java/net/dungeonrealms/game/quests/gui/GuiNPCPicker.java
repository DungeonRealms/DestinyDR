package net.dungeonrealms.game.quests.gui;

import java.util.List;
import java.util.function.Consumer;

import net.dungeonrealms.game.quests.Quest;
import net.dungeonrealms.game.quests.QuestNPC;
import net.dungeonrealms.game.quests.Quests;
import net.md_5.bungee.api.ChatColor;

import org.bukkit.entity.Player;

public class GuiNPCPicker extends GuiBase {
	
	private Quest quest;
	private Consumer<QuestNPC> onPick;
	
	public GuiNPCPicker(Player player, Quest quest, Consumer<QuestNPC> onPick) {
		super(player, "Pick the NPC you'd like to add.", Quests.getInstance().npcStore.getList());
		this.quest = quest;
		this.onPick = onPick;
	}
	
	@Override
	public void createGUI(){
		List<QuestNPC> npcList = Quests.getInstance().npcStore.getList();
		for(int i = 0; i < npcList.size(); i++){
			QuestNPC npc = npcList.get(i);
			this.setSlot(i, Quests.createSkull(npc.getSkinOwner(), ChatColor.GREEN + npc.getName(), new String[] {"Left Click to Add an NPC from the bank."}), (evt) -> this.onPick.accept(npc));
		}
		this.setSlot(this.getSize() - 5, GO_BACK, (evt) -> new GuiQuestEditor(player, quest));
	}
}
