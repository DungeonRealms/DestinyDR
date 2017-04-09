package net.dungeonrealms.game.quests;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.scoreboard.Team;
import org.inventivetalent.glow.GlowAPI;

import com.google.gson.JsonObject;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.npc.skin.SkinnableEntity;
import net.citizensnpcs.util.NMS;

public class QuestNPC implements ISaveable{
	
	private String npcName;
	private Location npcLocation;
	private String skinOwner;
	private String skinValue;
	private String skinSignature;
	private NPCAnimation animation;
	private NPC entity;
	private String idleMessage;
	private String lastSkin = "";
	
	public QuestNPC(){
		
	}
	
	public QuestNPC(String name, Location loc){
		this.setName(name);
		this.setAnimation(NPCAnimation.Nothing);
		this.setLocation(loc);
		this.spawnEntity();
		this.setIdleMessage("Hey.");
	}
	
	public QuestNPC(String name){
		this.setName(name);
	}
	
	public QuestNPC(JsonObject o){
		this.fromFile(o);
	}
	
	public NPCAnimation getAnimation(){
		return this.animation;
	}
	
	public void setAnimation(NPCAnimation anim){
		if(isLoaded()){
			switch(anim){
				case Nothing:
					this.entity.getDefaultGoalController().clear();
					this.entity.removeTrait(NPCTaskFaceNearestPlayer.class);
					break;
				case Nearest:
					this.entity.addTrait(new NPCTaskFaceNearestPlayer());
					break;
				default:
					Bukkit.getLogger().warning("Don't know how to handle the animation type " + anim.name());
			}
		}
		this.animation = anim;
	}
	
	public boolean isLoaded(){
		return this.entity != null && this.entity.isSpawned();
	}
	
	public NPC getNPCEntity(){
		return this.entity;
	}
	
	public Location getLocation(){
		return this.npcLocation;
	}
	
	public void setLocation(Location location){
		if(isLoaded())
			this.getNPCEntity().teleport(location, TeleportCause.COMMAND);
		this.npcLocation = location;
	}
	
	public String getName(){
		return this.npcName;
	}
	
	public void setName(String name){
		if(isLoaded())
			this.getNPCEntity().setName(name);
		this.npcName = name;
	}
	
	public String getSkinOwner(){
		return this.skinOwner;
	}
	
	public String getIdleMessage(){
		return this.idleMessage;
	}
	
	public void setIdleMessage(String message){
		this.idleMessage = message;
	}
	
	public void setSkin(String username){
		if(isLoaded() && !this.lastSkin.equals(username)){
			SkinnableEntity se = NMS.getSkinnable(this.getNPCEntity().getEntity());
			this.lastSkin = username;
			if(se != null){
				this.skinValue = null;
				this.skinSignature = null;
				se.setSkinName(username);
			}else{
				Bukkit.getLogger().warning("Tried to set Skin Data for non-player NPC " + this.getName() + "!");
			}
		}
		this.skinOwner = username;
	}
	
	public void setGlowing(Player player, ChatColor color) {
		if(!isLoaded())
			return;
		
		String teamName = this.getNPCEntity().data().get(NPC.SCOREBOARD_FAKE_TEAM_NAME_METADATA);
		if(teamName == null)
			return;
		
		//System.out.println("Team = " + teamName);
		Team team = Bukkit.getScoreboardManager().getMainScoreboard().getTeam(teamName);
		//System.out.println(team != null ? "Team Found" : "Team Not Found");
		if(team != null)
			team.setPrefix(color.toString());
		
		GlowAPI.setGlowing(this.getNPCEntity().getEntity(), GlowAPI.Color.valueOf(color.name()), player);
	}
	
	public void setSkin(String value, String signature){
		this.skinValue = value;
		this.skinSignature = signature;
		if(isLoaded()){
			this.getNPCEntity().data().set(NPC.PLAYER_SKIN_TEXTURE_PROPERTIES_METADATA, value);
			this.getNPCEntity().data().set(NPC.PLAYER_SKIN_TEXTURE_PROPERTIES_SIGN_METADATA, signature);
		}
	}

	@Override
	public void fromFile(JsonObject obj) {
		this.setName(obj.get("name").getAsString());
		System.out.println("Loading " + this.getName());
		if(obj.has("location"))
			this.setLocation(GeneralUtils.jsonToLoc(obj.get("location").getAsJsonObject()));

		if(obj.has("animation"))
			this.setAnimation(NPCAnimation.valueOf(obj.get("animation").getAsString()));
		
		if(obj.has("idleMessage"))
			this.setIdleMessage(obj.get("idleMessage").getAsString());
		
		if(obj.has("skinOwner"))
			this.skinOwner = obj.get("skinOwner").getAsString();
		
		if(obj.has("skinValue") && obj.has("skinSignature"))
			this.setSkin(obj.get("skinValue").getAsString(), obj.get("skinSignature").getAsString());
		
		this.spawnEntity();
	}
	
	public void spawnEntity(){
		if(!isLoaded()){
			this.entity = CitizensAPI.getNPCRegistry().createNPC(EntityType.PLAYER, this.getName());
			this.entity.data().set(NPC.SHOULD_SAVE_METADATA, false);
			this.entity.setProtected(true);
			this.entity.spawn(this.npcLocation);
			if(Bukkit.getOnlinePlayers().size() > 0){
				this.setAnimation(this.animation);
				if(this.skinSignature != null && this.skinValue != null)
					this.setSkin(this.skinValue, this.skinSignature);
			}
		}
	}
	
	@Override
	public JsonObject toJSON() {
		if(this.skinValue == null && this.isLoaded() && this.getNPCEntity().data().has(NPC.PLAYER_SKIN_TEXTURE_PROPERTIES_METADATA)){
			this.skinValue = this.getNPCEntity().data().get(NPC.PLAYER_SKIN_TEXTURE_PROPERTIES_METADATA);
			this.skinSignature = this.getNPCEntity().data().get(NPC.PLAYER_SKIN_TEXTURE_PROPERTIES_SIGN_METADATA);
		}
		JsonObject obj = new JsonObject();
		obj.addProperty("name", this.getName());
		obj.add("location", GeneralUtils.locToJson(this.npcLocation));
		if(this.skinOwner != null)
			obj.addProperty("skinOwner", this.skinOwner);
		if(this.skinSignature != null)
			obj.addProperty("skinSignature", this.skinSignature);
		if(this.skinValue != null)
			obj.addProperty("skinValue", this.skinValue);
		obj.addProperty("animation", this.animation.name());
		obj.addProperty("idleMessage", this.idleMessage);
		
		return obj;
	}

	@Override
	public String getFileName() {
		return this.getName();
	}
}	
