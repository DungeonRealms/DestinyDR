package net.dungeonrealms.game.quests;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.dungeonrealms.game.quests.objectives.QuestObjective;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Created 1/10/2017
 * This is a temporary storage system for player quest data, as testing this is easier on my local PC than on us0.
 * This will be deleted and move to DR Database code later.
 * 
 * @author Kneesnap
 */

public class QuestPlayerData {
	private transient Player player;
	private ArrayList<QuestProgress> qip = new ArrayList<QuestProgress>();
	
	public QuestPlayerData(Player player){
		this.player = player;
	}
	
	public QuestPlayerData(Player player, JsonArray arr){
		this(player);
		this.fromJson(arr);
	}
	
	public JsonArray toJSON(){
		JsonArray newArray = new JsonArray();
		for(QuestProgress qp : this.qip)
			newArray.add(qp.toJSON());
		return newArray;
	}
	
	private void fromJson(JsonArray arr){
		for(JsonElement je : arr)
			this.qip.add(new QuestProgress(je.getAsJsonObject()));
	}
	
	public void triggerObjectives(Class<? extends QuestObjective> cls){
		for(Quest quest : this.getCurrentQuests()){
        	if(!this.isDoingQuest(quest))
        		continue;
			QuestProgress qp = this.getQuestProgress(quest);
			QuestStage stage = qp.getCurrentStage().getPrevious();
			if(stage == null)
				continue;
			QuestObjective qo = stage.getObjective();
        	if(qo != null && cls.isInstance(qo) && qp.getCurrentLine() == 0)
        		quest.advanceQuest(player, true);
        }
	}
	
	/**
	 * Returns the player's current progress in a quest
	 * 
	 * @param Quest
	 * @return Player's Quest Progress
	 */
	public QuestProgress getQuestProgress(Quest quest){
		for(QuestProgress q : this.qip)
			if(q.getQuest() == quest)
				return q;
		return null;
	}
	/**
	 * Returns if a player is doing a specified quest
	 * 
	 * @param Quest
	 * @return Doing Quest
	 */
	public boolean isDoingQuest(Quest q){
		QuestProgress qp = this.getQuestProgress(q);
		return qp != null && qp.isDoingQuest();
	}
	
	/**
	 * Marks the player as having started the supplied quest.
	 * Returns whether the player was already doing the quest.
	 * 
	 * @param Quest
	 * @return Success?
	 */
	public boolean startQuest(Quest q){
		if(!isDoingQuest(q)){
			QuestProgress qp = this.getQuestProgress(q);
			if(qp == null)
				qp = new QuestProgress(q);
			qp.startQuest();
			if(this.getQuestProgress(q) == null)
				this.qip.add(qp);
			return true;
		}
		return false;
	}
	
	public void completeQuest(Quest q){
		if(isDoingQuest(q)){
			this.getQuestProgress(q).completeQuest();
		}
	}
	
	public List<Quest> getCurrentQuests(){
		List<Quest> list = new ArrayList<Quest>();
		for(QuestProgress q : this.qip)
			if(q.isDoingQuest())
				list.add(q.getQuest());
		return list;
	}
	
	public Player getPlayer(){
		return this.player;
	}
	
	public class QuestProgress {
		private int questScrollDelay;
		
		private boolean doingQuest;
		private int currentStage;
		private int currentLine;
		//Reset every stage. Contains a counter used by quest objectives, for instance the kill counter in the kill objective.
		private int currentProgress;
		private long lastCompleted;
		private Quest quest;
		
		public QuestProgress(Quest quest){
			this.quest = quest;
			this.currentStage = 0;
			this.currentLine = 0;
			this.lastCompleted = 0;
			this.doingQuest = false;
			this.questScrollDelay = 0;
		}
		
		public QuestProgress(JsonObject obj){
			this.fromJson(obj);
		}
		
		private void fromJson(JsonObject obj){
			String questName = obj.get("quest").getAsString();
			for(Quest q : Quests.getInstance().questStore.getList())
				if(q.getQuestName().equals(questName))
					this.quest = q;
			this.currentStage = obj.get("stage").getAsInt();
			this.currentLine = obj.get("line").getAsInt();
			this.lastCompleted = obj.get("completed").getAsLong();
			this.doingQuest = obj.get("doingQuest").getAsBoolean();
			this.currentProgress = obj.get("objectiveProgress").getAsInt();
		}
		
		public JsonObject toJSON(){
			JsonObject obj = new JsonObject();
			obj.addProperty("quest", this.quest.getQuestName());
			obj.addProperty("stage", this.currentStage);
			obj.addProperty("line", this.currentLine);
			obj.addProperty("completed", this.lastCompleted);
			obj.addProperty("doingQuest", this.doingQuest);
			obj.addProperty("objectiveProgress", this.currentProgress);
			return obj;
		}
		
		public Quest getQuest(){
			return this.quest;
		}
		
		public void setCurrentStage(int stage){
			this.currentStage = stage;
			this.currentLine = 0;
			this.currentProgress = 0;
		}
		
		public int getObjectiveCounter(){
			return this.currentProgress;
		}
		
		public void setObjectiveCounter(int i){
			this.currentProgress = i;
		}
		
		public QuestStage getCurrentStage(){
			return this.getQuest().getStageList().get(this.currentStage);
		}
		
		public void setCurrentLine(int line){
			this.currentLine = line;
		}
		
		public int getCurrentLine(){
			return this.currentLine;
		}
		
		public long getLastCompletionTime(){
			return this.lastCompleted;
		}
		
		public void startQuest(){
			this.doingQuest = true;
			this.currentLine = 0;
			this.currentStage = 0;
			this.currentProgress = 0;
		}
		
		public void completeQuest(){
			this.lastCompleted = new Date().getTime();
			this.doingQuest = false;
			this.currentLine = 0;
			this.currentStage = 0;
			this.currentProgress = 0;
		}
		
		public boolean isDoingQuest(){
			return this.doingQuest;
		}

		public void clearQuestDelay() {
			if(this.questScrollDelay != 0)
				Bukkit.getScheduler().cancelTask(this.questScrollDelay);
		}
		
		public void setQuestDelay(int num){
			this.questScrollDelay = num;
		}

		public int getStageIndex() {
			return this.currentStage;
		}
	}
}
