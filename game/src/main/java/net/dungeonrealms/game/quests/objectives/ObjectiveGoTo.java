package net.dungeonrealms.game.quests.objectives;

import net.dungeonrealms.game.player.chat.Chat;
import net.dungeonrealms.game.quests.GeneralUtils;
import net.dungeonrealms.game.quests.QuestNPC;
import net.dungeonrealms.game.quests.QuestStage;
import net.dungeonrealms.game.quests.Quests;
import net.dungeonrealms.game.quests.gui.GuiBase;
import net.dungeonrealms.game.quests.gui.GuiStageEditor;
import net.md_5.bungee.api.ChatColor;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;

import com.google.gson.JsonObject;

public class ObjectiveGoTo implements QuestObjective {
	
	private String description = "Cyrennica Fountain";
	private Location loc;
	private int radius;
	
	public ObjectiveGoTo(){
		
	}

	@Override
	public boolean isCompleted(Player player, QuestStage stage, QuestNPC currentNPC) {
		if(player == null)
			return false;
		return this.loc != null? this.loc.distance(player.getLocation()) <= radius : true;
	}

	@Override
	public String getName() {
		return "Goto";
	}

	@Override
	public String getTaskDescription(Player player, QuestStage stage) {	
		if(this.loc != null)
			return "Goto " + this.description + " " + Quests.getRegionDirections(this.loc);
		return "Goto Undefined. Funny place I've heard. (Report this to a GM+)";
	}
	
	public Location getLocation(){
		return this.loc;
	}
	
	public int getRadius(){
		return this.radius;
	}

	@Override
	public JsonObject saveJSON() {
		JsonObject obj = new JsonObject();
		obj.add("location", GeneralUtils.locToJson(this.loc));
		obj.addProperty("radius", this.radius);
		obj.addProperty("description", this.description);
		return obj;
	}

	@Override
	public void loadJSON(JsonObject o) {
		if(o.has("location")){
			this.loc = GeneralUtils.jsonToLoc(o.get("location").getAsJsonObject());
			this.radius = o.get("radius").getAsInt();
			this.description = o.get("description").getAsString();
		}
	}

	@Override
	public Material getIcon() {
		return Material.IRON_BARDING;
	}
	
	@Override
	public String[] getDescription(){
		return new String[] {"Player must reach a certain location."};
	}

	@Override
	public GuiBase createEditorGUI(Player player, QuestStage stage) {
		return new GuiGotoEditor(player, stage, this);
	}
	
	
	public class GuiGotoEditor extends GuiBase {
		
		private QuestStage stage;
		private ObjectiveGoTo objective;
		
		public GuiGotoEditor(Player player, QuestStage stage, ObjectiveGoTo objective){
			super(player, "Where should the player go?", InventoryType.HOPPER);
			this.stage = stage;
			this.objective = objective;
			if(this.objective.loc == null){
				this.objective.loc = player.getLocation().clone();
				this.objective.radius = 10;
			}
		}
		
		@Override
		public void createGUI(){
			if(this.objective.loc != null)
				this.setSlot(0, Material.ELYTRA, ChatColor.GOLD + "Teleport", new String[] {"Click here to teleport to the set location."}, (evt) -> {
					player.closeInventory();
					player.teleport(this.objective.loc);
				});
			
			this.setSlot(1, Material.MAP, "Set Description Location", new String[] {"Click here to fill this sentence:", ChatColor.YELLOW + "Goto [Area Name]", "For Example \"Cyrennica Fountain\"", "Current Description: " + ChatColor.YELLOW + this.objective.description}, (e) -> {
				player.sendMessage(ChatColor.YELLOW + "Please fill in the blank.");
				player.sendMessage(ChatColor.RED + "Goto [???] " + Quests.getRegionDirections(this.objective.loc) + ".");
				Chat.listenForMessage(player, (res) -> {
					this.objective.description = res.getMessage();
					player.sendMessage(ChatColor.GREEN + "Description Updated.");
					new GuiGotoEditor(player, stage, objective);
				}, (p) -> new GuiGotoEditor(player, stage, objective));
			});
			
			Location l = this.objective.loc;
			this.setSlot(2, Quests.createSkull("Seska_Rotan", ChatColor.YELLOW + "Set Location", new String[] {"Click here to set the target Location", (l != null ? "Current Location: " + ChatColor.GOLD + Quests.getCoords(l) : "None"), (l != null ? ChatColor.GRAY + "Radius: " + this.objective.radius : "No Radius Set")}), (evt) -> {
				player.sendMessage(ChatColor.YELLOW + "Please walk to the target location then enter the radius players should be detected from.");
				Chat.listenForNumber(player, 0, 50, (num) -> {
					this.objective.loc = player.getLocation().clone();
					this.objective.radius = num;
					player.sendMessage(ChatColor.GREEN + "Trigger set to current location, with radius of " + this.objective.radius);
					new GuiGotoEditor(player, stage, objective);
				}, (p) -> new GuiGotoEditor(player, stage, objective));
			});
			
			this.setSlot(4, GO_BACK, (evt) -> new GuiStageEditor(player, stage));
		}
	}


	@Override
	public void setQuestStage(QuestStage qs) {}
}
