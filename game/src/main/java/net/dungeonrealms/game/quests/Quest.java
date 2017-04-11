package net.dungeonrealms.game.quests;

import java.util.ArrayList;
import java.util.List;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.game.mastery.GamePlayer;
import net.dungeonrealms.game.mechanic.ItemManager;
import net.dungeonrealms.game.player.banks.BankMechanics;
import net.dungeonrealms.game.quests.QuestPlayerData.QuestProgress;
import net.dungeonrealms.game.quests.objectives.QuestObjective;
import net.md_5.bungee.api.ChatColor;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Builder;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class Quest implements ISaveable {
	
	private String questName;
	private List<QuestStage> stageList = new ArrayList<QuestStage>();
	private int levelRequirement = 1;
	private int gemReward = 0;
	private int xpReward = 0;
	private QuestInterval interval;
	
	public Quest(){
		
	}
	
	public Quest(JsonObject o){
		this.fromFile(o);
	}
	
	public Quest(String questName){
		this.questName = questName;
		this.interval = QuestInterval.ONCE;
	}
	
	public String getQuestName(){
		return this.questName;
	}
	
	public void setQuestName(String name){
		this.questName = name;
	}
	
	public List<QuestStage> getStageList(){
		return this.stageList;
	}
	
	public void setStageList(List<QuestStage> stages){
		this.stageList = stages;
	}

	public boolean isQuestNPC(Entity ent) {
		return this.getQuestNPC(ent) != null;
	}
	
	public QuestNPC getQuestNPC(Entity ent){
		for(QuestStage stage : this.stageList)
			if(stage.getNPC() != null && stage.getNPC().getName().equals(ent.getName()))
				return stage.getNPC();
		return null;
	}
	
	public QuestInterval getQuestInterval(){
		return this.interval;
	}
	
	public void setQuestInterval(QuestInterval qi){
		this.interval = qi;
	}
	
	public int getLevelRequirement(){
		return this.levelRequirement;
	}
	
	public void setLevelRequirement(int l){
		this.levelRequirement = l;
	}
	
	public void setXPReward(int xp){
		this.xpReward = xp;
	}
	
	public int getXPReward(){
		return this.xpReward;
	}
	
	public void setGemReward(int gem){
		this.gemReward = gem;
	}
	
	public int getGemReward(){
		return this.gemReward;
	}
	
	public boolean canStartQuest(Player player){
		GamePlayer gamePlayer = GameAPI.getGamePlayer(player);
		QuestPlayerData data = Quests.getInstance().playerDataMap.get(player);
		if (gamePlayer == null || data == null)
			return false;
		
		if(data.isDoingQuest(this))
			return false;
		
		if (this.getLevelRequirement() > gamePlayer.getLevel()){
			player.sendMessage(ChatColor.RED + "You must be level " + ChatColor.UNDERLINE + this.getLevelRequirement() + ChatColor.RED + " to do this quest.");
			return false;
		}
		
		if(data.getQuestProgress(this) != null && !this.interval.hasCooldownEnded(player, this)){
			if(this.interval == QuestInterval.ONCE){
				player.sendMessage(ChatColor.RED + "This quest is only completable " + ChatColor.UNDERLINE + "once" + ChatColor.RED + ".");
			}else{
				player.sendMessage(ChatColor.RED + "You must wait " + ChatColor.BOLD + GameAPI.formatTime(-this.interval.getCooldown(player, this)) + ChatColor.RED + " before doing this quest again.");
			}
			return false;
		}
		
		return true;
	}
	
	/**
	 * Handles when a player clicks a Quest NPC.
	 * @param Player
	 */
	public void handleNPCClick(QuestNPC npc, Player player){
		QuestPlayerData data = Quests.getInstance().playerDataMap.get(player);
		if(data == null){
			player.sendMessage(ChatColor.RED + "Please wait for your data to load.");
			return;
		}
		
		QuestProgress qp = data.getQuestProgress(this);
		QuestStage stage = this.getStageList().get(0);
		
		if((qp == null || !qp.isDoingQuest())){
			if(npc == this.getStageList().get(0).getNPC()){
				startQuest(player, data);
				qp = data.getQuestProgress(this);
			}
		}else{
			stage = qp.getCurrentStage();
		}
		
		if(npc == stage.getNPC()){
			advanceQuest(player);
		}else
			player.sendMessage(ChatColor.AQUA + npc.getName() + ": "+ ChatColor.YELLOW + npc.getIdleMessage());
	}
	
	public void advanceQuest(Player player){
		this.advanceQuest(player, false);
	}
	
	/**
	 * This should be activated to start or advance the dialogue with an NPC
	 * 
	 * @param Player    The player interacting with an NPC
	 * @author Kneesnap
	 */
	public void advanceQuest(Player player, boolean force){
		QuestPlayerData data = Quests.getInstance().playerDataMap.get(player);
		
		QuestProgress qp = data.getQuestProgress(this);
		QuestStage stage = qp.getCurrentStage();
		if(!qp.isDoingQuest())
			return;
		QuestStage previous = stage.getPrevious();
		//Check if the quest should actually be advanced (Only check if it's the first dialogue line as some Objectives will take actions when isComplete is run, such as take items, resulting in the next stage not being able to run.)
		if(stage.getObjective() != null && qp.getCurrentLine() == 0 && (!force && (previous != null && previous.getObjective() != null && !previous.getObjective().isCompleted(player, stage, stage.getNPC())))){
			player.sendMessage(ChatColor.AQUA + stage.getNPC().getName() + ": " + ChatColor.YELLOW + "Please " + previous.getObjective().getTaskDescription(player, stage) + ".");
			return;
		}
		
		//  Advance the Dialogue  //
		if(qp.getCurrentLine() >= stage.getDialogue().size()){
			
			if(previous != null && previous.getObjective() != null)
				previous.getObjective().onEnd(player);
			
			if(qp.getStageIndex() >= this.getStageList().size() - 1) {
				completeQuest(player, data);
			}else{
				if(stage.getObjective() != null){
					player.sendMessage(ChatColor.WHITE + "" + ChatColor.BOLD + "Next Objective> " + ChatColor.AQUA + stage.getObjective().getTaskDescription(player, stage.getNext()) + ".");
					player.playSound(player.getLocation(), Sound.BLOCK_BREWING_STAND_BREW, 0.75F, 1.75F);
				}
				if(stage.getObjective() != null)
					stage.getObjective().onStart(player);
				qp.setCurrentStage(qp.getStageIndex() + 1);
				Quests.getInstance().updateActionBar(player);
			}
		}else{
			stage.getDialogue().get(qp.getCurrentLine()).doDialogue(player, qp, stage);
			final QuestStage currentStage = stage;
			qp.clearQuestDelay();
			qp.setQuestDelay(Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
				if(qp.getCurrentStage() == currentStage && qp.isDoingQuest() && player.isOnline())
					advanceQuest(player);
			}, 40));
		}
	}
	
	private void completeQuest(Player player, QuestPlayerData data){
		if(!data.isDoingQuest(this))
			return;
		
		if(this.gemReward > 0 && player.getInventory().firstEmpty() == -1){
			player.sendMessage(ChatColor.RED + "Please free up some inventory space first.");
			return;
		}
		
		player.sendMessage(ChatColor.WHITE + "" + ChatColor.BOLD + "Quest Complete> " + ChatColor.AQUA + this.getQuestName());
		data.completeQuest(this);

		//if (this.getQuestName().equalsIgnoreCase("Tutorial Island")) {
		//	ItemManager.giveStarter(player, true);  //This replaces any armor they're wearing already, plus they get this when they login.
		//}
		GamePlayer gamePlayer = GameAPI.getGamePlayer(player);
		
		player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
		Builder effect = FireworkEffect.builder();
		effect.withColor(Color.NAVY);
		effect.with(Type.BALL);
		spawnFirework(player.getLocation(), effect.build());
		
		if(this.gemReward > 0){
			player.sendMessage(ChatColor.GREEN + "You acquired " + this.gemReward + " gems!");
			player.getInventory().addItem(BankMechanics.createBankNote(this.gemReward, this.getQuestName()));
		}
		
		//This delay is purely for "cosmetic" purposes.
		Bukkit.getScheduler().runTaskLater(DungeonRealms.getInstance(),
				() -> gamePlayer.addExperience(this.getXPReward(), false, true), 40);
	}
	
	private void startQuest(Player player, QuestPlayerData data){
		if(!this.canStartQuest(player))
			return;
		player.sendMessage(ChatColor.WHITE + "" + ChatColor.BOLD + "Quest Started> " + ChatColor.AQUA + this.getQuestName());
		player.playSound(player.getLocation(), Sound.BLOCK_NOTE_PLING, 1F, 2F);
		data.startQuest(this);
	}
	
	private void spawnFirework(Location l, FireworkEffect... effects){
		Firework fw = (Firework) l.getWorld().spawnEntity(l, EntityType.FIREWORK);
        FireworkMeta fwm = fw.getFireworkMeta();
        fwm.setPower(0);
        fwm.addEffects(effects);
        fw.setFireworkMeta(fwm);
	}

	@Override
	public void fromFile(JsonObject obj) {
		this.interval = QuestInterval.valueOf(obj.get("interval").getAsString());
		this.questName = obj.get("questName").getAsString();
		
		if(obj.has("levelRequirement"))
			this.levelRequirement = obj.get("levelRequirement").getAsInt();
		if(obj.has("xpReward"))
			this.xpReward = obj.get("xpReward").getAsInt();
		if(obj.has("gemReward"))
			this.gemReward = obj.get("gemReward").getAsInt();
		
		for(JsonElement o : obj.get("stages").getAsJsonArray()){
			QuestStage qs = new QuestStage(o.getAsJsonObject());
			qs.setQuest(this);
			this.stageList.add(qs);
		}
	}

	@Override
	public JsonObject toJSON() {
		JsonObject obj =  new JsonObject();
		obj.addProperty("interval", this.interval.name());
		obj.addProperty("questName", this.getQuestName());
		obj.addProperty("levelRequirement", this.levelRequirement);
		obj.addProperty("xpReward", this.xpReward);
		obj.addProperty("gemReward", this.gemReward);
		JsonArray stages = new JsonArray();
		for(QuestStage stage : this.stageList)
			stages.add(stage.toJSON());
		obj.add("stages", stages);
		return obj;
	}

	@Override
	public String getFileName() {
		return this.getQuestName();
	}
	
	public void updateStages() {
		for(QuestStage stage : this.stageList) {
			QuestObjective obj = stage.getObjective();
			if(obj != null)
				obj.setQuestStage(stage);
		}
	}
}
