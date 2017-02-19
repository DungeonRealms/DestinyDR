package net.dungeonrealms.game.quests;

import net.dungeonrealms.GameAPI;
import net.dungeonrealms.game.mechanic.ItemManager;
import net.dungeonrealms.game.world.item.itemgenerator.ItemGenerator;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.google.gson.JsonObject;

public class QuestItem implements ISaveable {
	
	private String drItemName;
	
	private Material itemMaterial;
	private int itemAmount;
	private short itemMeta;
	private String displayName;
	private boolean isSoulBound;
	
	public QuestItem(ItemStack i){
		this.loadItem(i);
	}
	
	public QuestItem(JsonObject obj){
		this.fromFile(obj);
	}
	
	/**
	 * Converts this Quest Item into an actual itemStack.
	 */
	public ItemStack createItem(Player player){
		ItemStack created;
		if(!this.isDRItem()){
			created = new ItemStack(this.itemMaterial, this.itemAmount);
			ItemMeta meta = created.getItemMeta();
			if(this.displayName != null)
				meta.setDisplayName(this.displayName);
			created.setItemMeta(meta);
			created.setDurability(this.itemMeta);
		}else{
			created = ItemGenerator.getNamedItem(this.drItemName);
		}
		if(this.isSoulBound)
			created = ItemManager.makeSoulBound(created);
		return created;
	}
	
	/**
	 * Checks whether an itemstack matches this quest item.
	 */
	public boolean doesItemMatch(ItemStack item){
		if(this.isSoulBound != GameAPI.isItemSoulbound(item))
			return false;
		if(this.isDRItem())
			return this.drItemName.equals(GameAPI.getCustomID(item));
		if(item == null)
			return false;
		if(item.getType() != this.itemMaterial)
			return false;
		if(item.getAmount() < this.itemAmount)
			return false;
		if(this.displayName != null && !item.getItemMeta().getDisplayName().equals(this.displayName))
			return false;
		return true;
	}

	@Override
	public void fromFile(JsonObject obj) {
		
		if(obj.has("soulbound"))
			this.isSoulBound = obj.get("soulbound").getAsBoolean();
		
		if(obj.has("drItemName")){
			this.drItemName = obj.get("drItemName").getAsString();
			return;
		}
		
		if(obj.has("displayName"))
			this.displayName = obj.get("displayName").getAsString();
		if(obj.has("amount"))
			this.itemAmount = obj.get("amount").getAsInt();
		if(obj.has("type"))
			this.itemMaterial = Material.valueOf(obj.get("type").getAsString());
		if(obj.has("meta"))
			this.itemMeta = obj.get("meta").getAsShort();
	}

	@Override
	public JsonObject toJSON() {
		JsonObject obj = new JsonObject();
		obj.addProperty("soulbound", this.isSoulBound);
		if(this.isDRItem()){
			obj.addProperty("drItemName", this.drItemName);
			return obj;
		}
		obj.addProperty("displayName", this.displayName);
		obj.addProperty("amount", this.itemAmount);
		obj.addProperty("type", this.itemMaterial.name());
		obj.addProperty("meta", this.itemMeta);
		return obj;
	}

	@Override
	public String getFileName() {
		return null;
	}

	public int getAmount() {
		return this.itemAmount;
	}
	
	public void setAmount(int a){
		this.itemAmount = a;
	}
	
	public Material getType(){
		return this.itemMaterial;
	}

	public void setType(Material mat) {
		this.itemMaterial = mat;
	}
	
	public String getDisplayName(){
		return this.displayName;
	}
	
	public void setDisplayName(String name){
		this.displayName = name;
	}
	
	/**
	 * Take an ItemStack and load it into this quest item.
	 * @param Item to load from
	 */
	public void loadItem(ItemStack i) {
		String drItemName = GameAPI.getCustomID(i);
		if(drItemName != null){
			this.drItemName = drItemName;
			return;
		}
		this.itemMaterial = i.getType();
		this.itemAmount = i.getAmount();
		this.itemMeta = i.getDurability();
		this.displayName = i.getItemMeta().getDisplayName();
		if(this.displayName == null)
			this.displayName = this.itemMaterial.name();
		this.isSoulBound = GameAPI.isItemSoulbound(i);
	}
	
	public boolean isSoulbound(){
		return this.isSoulBound;
	}
	
	public void setSoulbound(boolean questItem){
		this.isSoulBound = questItem;
	}

	public boolean isDRItem() {
		return this.drItemName != null;
	}
	
	public String getDRItemName(){
		return this.drItemName;
	}
	
	public void setDRItemName(String dr){
		this.drItemName = dr;
	}
}
