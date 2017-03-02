package net.dungeonrealms.game.quests.objectives;

import java.util.ArrayList;
import java.util.List;

import net.dungeonrealms.game.quests.QuestStage;

import org.bukkit.Bukkit;

import com.google.gson.JsonObject;

public class QuestObjectiveList {

	private static List<Class<? extends QuestObjective>> objectiveList = new ArrayList<Class<? extends QuestObjective>>();
	
	static {
		objectiveList.add(ObjectiveGather.class);
		objectiveList.add(ObjectiveGoTo.class);
		objectiveList.add(ObjectiveNextNPC.class);
		objectiveList.add(ObjectiveKill.class);
		objectiveList.add(ObjectiveCreateShop.class);
		objectiveList.add(ObjectiveOpenRealm.class);
		objectiveList.add(ObjectiveUseAnvil.class);
		objectiveList.add(ObjectiveUseHearthStone.class);
		objectiveList.add(ObjectiveOpenJournal.class);
		objectiveList.add(ObjectiveOpenProfile.class);
		objectiveList.add(ObjectiveNone.class);
	}
	
	public static QuestObjective createObjective(JsonObject data, QuestStage stage){
		String type = data.get("type").getAsString();
		for(Class<? extends QuestObjective> cls : objectiveList){
			try{
				QuestObjective qo = cls.newInstance();
				if(qo.getName().equals(type)){
					qo.loadJSON(data);
					qo.setQuestStage(stage);
					return qo;
				}
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		Bukkit.getLogger().warning("Failed to find Quest Objective " + type + ".");
		return null;
	}
	
	public static List<Class<? extends QuestObjective>> getObjectives(){
		return objectiveList;
	}
}
