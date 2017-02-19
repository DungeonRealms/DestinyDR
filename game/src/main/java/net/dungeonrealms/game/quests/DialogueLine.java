package net.dungeonrealms.game.quests;

import java.util.ArrayList;
import java.util.List;

import net.dungeonrealms.game.quests.QuestPlayerData.QuestProgress;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * This class represents each line of dialogue an NPC has,
 * and contains all data such as what actions the NPC will perform.
 * 
 * @author Kneesnap
 */

public class DialogueLine implements ISaveable {
	
	private String dialogueText;
	private List<PotionEffect> potionEffects = new ArrayList<PotionEffect>();
	private Location teleportLocation;
	private List<QuestItem> giveItems = new ArrayList<QuestItem>();
	//Sound Effects
	
	public DialogueLine(JsonObject o){
		this.fromFile(o);
	}
	
	public DialogueLine(){
		
	}
	
	public List<PotionEffect> getPotionEffects(){
		return this.potionEffects;
	}
	
	public void setPotionEffects(List<PotionEffect> potions){
		this.potionEffects = potions;
	}
	
	public String getText(){
		return this.dialogueText;
	}
	
	public void setText(String text){
		this.dialogueText = text;
	}
	
	public Location getTeleportLocation(){
		return this.teleportLocation;
	}
	
	public void setTeleportLocation(Location l){
		this.teleportLocation = l;
	}
	
	/**
	 * This will be called by handleNPCClick to execute the current line of dialogue
	 * 
	 * @param Player
	 * @param Quest Progress
	 * @param NPC
	 * @author Kneesnap
	 */
	public void doDialogue(Player player, QuestProgress qp, QuestStage stage){
		QuestNPC talkingTo = stage.getNPC();
		int itemsFree = 0;
		for(int i = 0; i < player.getInventory().getSize(); i++)
			if(player.getInventory().getItem(i) == null || player.getInventory().getItem(i).getType() == Material.AIR)
				itemsFree++;
		
		if(itemsFree < this.giveItems.size()){
			player.sendMessage(ChatColor.RED + "You must free up some inventory space before talking.");
			return;
		}
		
		for(QuestItem qi : this.giveItems)
			player.getInventory().addItem(qi.createItem(player));
		
		if(!this.giveItems.isEmpty())
			player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1F, 1F);
		
		
		if(this.dialogueText != null && !this.dialogueText.equals("")){
			String message = ChatColor.translateAlternateColorCodes('&', this.dialogueText).replaceAll("%player%", player.getName());
			if(this.dialogueText.startsWith("^")){
				player.sendMessage(ChatColor.GRAY + " * " + message.substring(1) + " * ");
			}else if(this.dialogueText.startsWith("`")){
				player.sendMessage(message.substring(1));
			}else{
				int current = 0;
				for(int i = 0; i < qp.getCurrentLine() + 1; i++)
					if(stage.getDialogue().get(i).getText() != null)
						current++;
				if(talkingTo != null){
					player.sendMessage(ChatColor.GRAY + "" + ChatColor.BOLD + "(" + current + "/" + stage.getTextCount() + ") " + ChatColor.AQUA + talkingTo.getName() + ": " + ChatColor.YELLOW + message);
				}else{
					player.sendMessage(ChatColor.GRAY + "" + ChatColor.BOLD + "(" + current + "/" + stage.getTextCount() + ") " + ChatColor.GREEN + message);
				}
			}
		}
		
		if(this.potionEffects != null && this.potionEffects.size() > 0)
			for(PotionEffect pe : this.potionEffects)
				player.addPotionEffect(pe);
		
		if(this.teleportLocation != null){
			player.teleport(this.teleportLocation);
			player.playSound(player.getLocation(), Sound.BLOCK_LAVA_POP, 1F, 1.333F);
		}
		qp.setCurrentLine(qp.getCurrentLine() + 1);
	}

	@Override
	public void fromFile(JsonObject obj) {
		if(obj.has("location"))
			this.setTeleportLocation(GeneralUtils.jsonToLoc(obj.get("location").getAsJsonObject()));
		
		if(obj.has("dialogue"))
			this.setText(obj.get("dialogue").getAsString());
		
		if(obj.has("effects")){
			for(JsonElement je : obj.get("effects").getAsJsonArray()){
				JsonObject pot = je.getAsJsonObject();
				this.potionEffects.add(new PotionEffect(PotionEffectType.getByName(pot.get("type").getAsString()), pot.get("duration").getAsInt(), pot.get("amplifier").getAsInt()));
			}
		}
		
		if(obj.has("items")){
			for(JsonElement je : obj.get("items").getAsJsonArray()){
				System.out.println(je.getAsJsonObject().toString());
				this.giveItems.add(new QuestItem(je.getAsJsonObject()));
			}
		}
	}

	@Override
	public JsonObject toJSON() {
		JsonObject o = new JsonObject();
		if(this.teleportLocation != null)
			o.add("location", GeneralUtils.locToJson(this.teleportLocation));
		
		if(this.dialogueText != null)
			o.addProperty("dialogue", this.dialogueText);
		
		if(this.potionEffects != null && this.potionEffects.size() > 0) {
			JsonArray ja = new JsonArray();
			for(PotionEffect pe : this.potionEffects){
				JsonObject potion = new JsonObject();
				potion.addProperty("duration", pe.getDuration());
				potion.addProperty("type", pe.getType().getName());
				potion.addProperty("amplifier", pe.getAmplifier());
				ja.add(potion);
			}
			o.add("effects", ja);
		}
		
		if(!this.giveItems.isEmpty()){
			JsonArray ja = new JsonArray();
			for(QuestItem qi : this.giveItems)
				ja.add(qi.toJSON());
			o.add("items", ja);
		}
		
		return o;
	}

	@Override
	public String getFileName() {
		return null;
	}

	public List<QuestItem> getItems() {
		return this.giveItems;
	}
}
