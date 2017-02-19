package net.dungeonrealms.game.quests;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import com.google.gson.JsonObject;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.npc.skin.SkinnableEntity;
import net.citizensnpcs.util.NMS;

public class QuestNPC implements ISaveable{
	
	private String npcName;
	private Location npcLocation;
	private String skinOwner;
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
				se.setSkinName(username);
			}else{
				Bukkit.getLogger().warning("Tried to set Skin Data for non-player NPC " + this.getName() + "!");
			}
		}
		this.skinOwner = username;
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
			this.setSkin(obj.get("skinOwner").getAsString());
		
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
				if(this.skinOwner != null)
					this.setSkin(this.skinOwner);
			}
		}
	}
	
	@Override
	public JsonObject toJSON() {
		JsonObject obj = new JsonObject();
		obj.addProperty("name", this.getName());
		obj.add("location", GeneralUtils.locToJson(this.npcLocation));
		if(this.skinOwner != null)
			obj.addProperty("skinOwner", this.skinOwner);
		obj.addProperty("animation", this.animation.name());
		obj.addProperty("idleMessage", this.idleMessage);
		
		return obj;
	}

	@Override
	public String getFileName() {
		return this.getName();
	}
}	
