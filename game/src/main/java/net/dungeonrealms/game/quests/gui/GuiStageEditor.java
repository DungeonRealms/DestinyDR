package net.dungeonrealms.game.quests.gui;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

import net.dungeonrealms.game.quests.QuestStage;
import net.dungeonrealms.game.quests.Quests;
import net.md_5.bungee.api.ChatColor;

public class GuiStageEditor extends GuiBase {
	
	private QuestStage stage;
	
	public GuiStageEditor(Player player, QuestStage stage){
		super(player, "Stage Editor", InventoryType.HOPPER);
		this.stage = stage;
	}
	
	@Override
	public void createGUI(){
		ItemStack i;
		if(this.stage.getNPC() != null){
			i = Quests.createSkull(this.stage.getNPC().getName(), ChatColor.YELLOW + "Select NPC", new String[] {"Click here to select another NPC.", "Current NPC: " + ChatColor.YELLOW + this.stage.getNPC().getName()});
		}else{
			i = createItem(Material.EGG, 1, (short)0, ChatColor.GREEN + "Select NPC", new String[] {"Click here to add an NPC."});
		}
		this.setSlot(0, i, (evt) -> new GuiNPCPicker(player, stage.getQuest(), (pickedNPC) -> {
			this.stage.setNPC(pickedNPC);
			new GuiStageEditor(player, this.stage);
		}));
			
		this.setSlot(1, Material.BOOK, ChatColor.GOLD + "Edit Dialogue", new String[] {"Click here to edit Dialogue and NPC Actions."}, (evt) -> {
			new GuiDialogueSelector(player, stage);
		});
			
		this.setSlot(2, stage.getObjective() != null ? stage.getObjective().getIcon() : Material.EYE_OF_ENDER, ChatColor.LIGHT_PURPLE + "Select Objective", new String[] {"Click here to edit the required objective.", "Objective: " + ChatColor.LIGHT_PURPLE + (stage.getObjective() != null ? stage.getObjective().getTaskDescription(null, stage.getNext()) : "None")}, (evt) -> {
			new GuiObjectiveSelector(player, stage);
		});
		
		this.setSlot(4, GO_BACK, (evt) -> new GuiStageSelector(player, stage.getQuest()));
	}
}
