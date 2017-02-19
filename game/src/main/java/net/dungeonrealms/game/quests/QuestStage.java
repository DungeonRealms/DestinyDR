package net.dungeonrealms.game.quests;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.dungeonrealms.game.quests.objectives.ObjectiveGoTo;
import net.dungeonrealms.game.quests.objectives.ObjectiveNextNPC;
import net.dungeonrealms.game.quests.objectives.QuestObjective;
import net.dungeonrealms.game.quests.objectives.QuestObjectiveList;

public class QuestStage implements ISaveable {
	
	private Quest belongsTo;
	private QuestNPC questNPC;
	private QuestObjective requiredObjective;
	private List<DialogueLine> dialogueLines = new ArrayList<DialogueLine>();
	
	public QuestStage(){
		
	}
	
	public QuestStage(JsonObject obj){
		this.fromFile(obj);
	}
	
	public QuestStage(Quest quest){
		this.setQuest(quest);
	}
	
	public QuestNPC getNPC(){
		return this.questNPC;
	}
	
	public void setNPC(QuestNPC npc){
		this.questNPC = npc;
	}
	
	public List<DialogueLine> getDialogue(){
		return this.dialogueLines;
	}
	
	public void addDialogue(DialogueLine data){
		this.dialogueLines.add(data);
	}
	
	public void setDialogue(List<DialogueLine> data){
		this.dialogueLines = data;
	}
	
	public QuestObjective getObjective() {
		if(this.requiredObjective == null)
			this.setNextObjective(new ObjectiveNextNPC());
		return this.requiredObjective;
	}

	public void setNextObjective(QuestObjective qo) {
		this.requiredObjective = qo;
	}
	
	public Quest getQuest(){
		return this.belongsTo;
	}
	
	public void setQuest(Quest q){
		this.belongsTo = q;
	}
	
	public Trigger getStageTrigger(){
		if(this.getPrevious() == null)
			return Trigger.NPC;
		if(this.getNPC() != null)
			return Trigger.NPC;
		if(this.getPrevious().getObjective() instanceof ObjectiveNextNPC)
			return Trigger.NPC;
		if(this.getPrevious().getObjective() instanceof ObjectiveGoTo)
			return Trigger.LOCATION;
		return Trigger.NONE;
	}
	
	public long getTextCount(){
		return this.dialogueLines.stream().filter(dl -> dl.getText() != null).count();
	}
	
	public Location getTriggerLocation(){
		if(this.getStageTrigger() == Trigger.LOCATION)
			return ((ObjectiveGoTo)this.getPrevious().getObjective()).getLocation();
		return null;
	}
	
	public int getTriggerRadius(){
		if(this.getStageTrigger() == Trigger.LOCATION)
			return ((ObjectiveGoTo)this.getPrevious().getObjective()).getRadius();
		return 0;
	}
	
	public QuestStage getPrevious(){
		int index = this.getQuest().getStageList().indexOf(this);
		if(index <= 0)
			return null;
		return this.getQuest().getStageList().get(index - 1);
	}
	
	public QuestStage getNext(){
		int index = this.getQuest().getStageList().indexOf(this);
		if(index >= this.getQuest().getStageList().size() - 1)
			return null;
		return this.getQuest().getStageList().get(index + 1);
	}
	
	@Override
	public void fromFile(JsonObject obj) {
		if(obj.has("objective"))
			this.requiredObjective = QuestObjectiveList.createObjective(obj.get("objective").getAsJsonObject(), this);
		
		if(obj.has("dialogue"))
			for(JsonElement je : obj.get("dialogue").getAsJsonArray())
				if(je instanceof JsonObject)
					this.dialogueLines.add(new DialogueLine((JsonObject)je));
		
		if(obj.has("npc"))
			this.questNPC = Quests.getInstance().getNPCByName(obj.get("npc").getAsString());
	}
	
	@Override
	public JsonObject toJSON() {
		JsonObject obj = new JsonObject();
		if(this.getObjective() != null){
			JsonObject objectiveData = this.getObjective().saveJSON();
			objectiveData.addProperty("type", this.getObjective().getName());
			obj.add("objective", objectiveData);
		}
		
		JsonArray ja = new JsonArray();
		for(DialogueLine dl : this.dialogueLines)
			ja.add(dl.toJSON());
		obj.add("dialogue", ja);
		
		if(this.questNPC !=  null)
			obj.addProperty("npc", this.questNPC.getName());
		return obj;
	}
	
	@Override
	public String getFileName() {
		return null;
	}
}
