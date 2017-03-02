package net.dungeonrealms.game.quests.gui;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import net.dungeonrealms.game.quests.QuestStage;
import net.dungeonrealms.game.quests.objectives.QuestObjective;
import net.dungeonrealms.game.quests.objectives.QuestObjectiveList;

public class GuiObjectiveSelector extends GuiBase {
	
	private QuestStage stage;
	
	public GuiObjectiveSelector(Player player, QuestStage stage){
		super(player, "Objective Selector", QuestObjectiveList.getObjectives(), 0);
		this.stage = stage;
	}
	
	@Override
	public void createGUI(){
		for(int i = 0; i < QuestObjectiveList.getObjectives().size(); i++){
			try{
				QuestObjective qo = QuestObjectiveList.getObjectives().get(i).newInstance();
				this.setSlot(i, qo.getIcon(), ChatColor.LIGHT_PURPLE + qo.getName(), qo.getDescription(), (evt) -> {
					if(this.stage.getObjective() != null && qo.getClass() == this.stage.getObjective().getClass()){
						this.stage.getObjective().createEditorGUI(this.player, this.stage);
					}else{
						this.player.sendMessage(ChatColor.GREEN + "Objective Created!");
						this.stage.setNextObjective(qo);
						qo.createEditorGUI(this.player, this.stage);
					}
				});
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		
		this.setSlot(this.getSize() - 1, GO_BACK, (evt) -> new GuiStageEditor(player, stage));
	}
}
