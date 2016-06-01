package net.dungeonrealms.game.player.json;



import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.json.simple.JSONArray;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import net.minecraft.server.v1_8_R3.IChatBaseComponent.ChatSerializer;
import net.minecraft.server.v1_8_R3.PacketPlayOutChat;

/**
 * Created by Chase on Nov 17, 2015
 */
public class JSONMessage {
	
	private JsonObject json;
	
	public JSONMessage() {
		initiateData();
	}
	
	public JSONMessage(String text) {
		initiateData();
	}
	
	public JSONMessage(String text, ChatColor color) {
		initiateData();
		json.addProperty("text", text);
		json.addProperty("color", color.name().toLowerCase());
	}
	
	private void initiateData(){
		json = new JsonObject();
		json.add("extra", new JsonArray());
	}
	
	private JsonArray getExtra() {
		if(!json.has("extra")) json.add("extra", new JsonArray());
		return (JsonArray) json.get("extra");
	}
	
	public void addText(String text) {
		addText(text, ChatColor.WHITE);
	}
	
	public void addText(String text, ChatColor color) {
		JsonObject data = new JsonObject();
		data.addProperty("text", text);
		data.addProperty("color", color.name().toLowerCase());
		getExtra().add(data);
	}
	
	public void addInsertionText(String text, ChatColor color, String insertion){
		JsonObject o = new JsonObject();
		o.addProperty("text", text);
		o.addProperty("color", color.name().toLowerCase());
		o.addProperty("insertion", insertion);
		getExtra().add(o);
	}

	public void addURL(String text, ChatColor color, String url) {
		JsonObject o = new JsonObject();
		o.addProperty("text", text);
		o.addProperty("color", color.name().toLowerCase());
		
		JsonObject u = new JsonObject();
		u.addProperty("action", "open_url");
		u.addProperty("value", url);
		
		o.add("clickEvent", u);
		getExtra().add(o);
	}
	
	public void addItem(ItemStack item, String text) {
		addItem(item, text, ChatColor.WHITE);
	}
	
	@SuppressWarnings("deprecation")
	public void addItem(ItemStack item, String text, ChatColor color) {
		if(item == null) return;
		
		JsonObject o = new JsonObject();
		o.addProperty("text", text);
		o.addProperty("color", color.name().toLowerCase());
		
		JsonObject a = new JsonObject();
		a.addProperty("action", "show_item");

		JsonObject i = new JsonObject();
		i.addProperty("id", item.getTypeId());
		i.addProperty("Damage", item.getDurability());
		
		if(item.getItemMeta() != null && (item.getItemMeta().getDisplayName() != null || (item.getItemMeta().getLore() != null && item.getItemMeta().getLore().size() > 0))) {
			JsonObject x = new JsonObject();
			JsonObject v = new JsonObject();
			
			ItemMeta m = item.getItemMeta();
			if(m.getDisplayName() != null) v.addProperty("Name", m.getDisplayName());
			if(m.getLore() != null)	v.addProperty("Lore", "%LORE%");
			
			x.add("display", v);
			i.add("tag", x);
		}
		
		String is = i.toString();
		is = is.replace("\"", "");
		
		if(is.contains("%LORE%")){
			String lore = JSONArray.toJSONString(item.getItemMeta().getLore());
			lore = lore.replace(":", "|");
			lore = lore.replace("\\", "");
			is = is.replace("%LORE%", lore);
		}
		
		a.addProperty("value", is);
		o.add("hoverEvent", a);
		getExtra().add(o);
	}
	
	public void addSuggestCommand(String text, ChatColor color, String cmd) {
		JsonObject o = new JsonObject();
		o.addProperty("text", text);
		o.addProperty("color", color.name().toLowerCase());
		
		JsonObject u = new JsonObject();
		u.addProperty("action", "suggest_command");
		u.addProperty("value", cmd);
		
		o.add("clickEvent", u);
		getExtra().add(o);
	}
	
	public void addRunCommand(String text, ChatColor color, String cmd){
		JsonObject o = new JsonObject();
		o.addProperty("text", text);
		o.addProperty("color", color.name().toLowerCase());
		
		JsonObject u = new JsonObject();
		u.addProperty("action", "run_command");
		u.addProperty("value", cmd);
		
		o.add("clickEvent", u);
		getExtra().add(o);
	}
	
	@Override
	public String toString(){
		return json.toString();
	}
	
	public void sendToPlayer(Player p){
		((CraftPlayer) p).getHandle().playerConnection.sendPacket(new PacketPlayOutChat(ChatSerializer.a(json.toString())));
	}
	
	public void setColor(ChatColor color){
		json.addProperty("color", color.name().toLowerCase());
	}
	
	public void setText(String text){
		json.addProperty("text", text);
	}
	
}