package net.dungeonrealms.game.quests;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

import net.dungeonrealms.DungeonRealms;

import org.bukkit.Bukkit;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * This stores edittable quest data such as NPCs, Quest Structure, etc.
 * 
 * @author Kneesnap
 * @param ISaveable
 */

public class StoreBase<T extends ISaveable> {

	private List<T> list = new ArrayList<T>();
	private Class<T> createClass;
	private String directory;
	
	public StoreBase(Class<T> createClass, String directory){
		this.directory = directory;
		this.createClass = createClass;
	}
	
	/**
	 * Loads the storage files from disk
	 */
	public void load(){
		this.list.clear();
		File questDir = new File(DungeonRealms.getInstance().getDataFolder() + "/" + this.directory + "/");
		if(!questDir.exists())
			questDir.mkdirs();
		for(File load : questDir.listFiles()){
			try{
				BufferedReader br = new BufferedReader(new FileReader(load));
				T loading = (T) this.createClass.newInstance();
				loading.fromFile(new JsonParser().parse(br).getAsJsonObject());
				list.add(loading);
			}catch(Exception e){
				e.printStackTrace();
				Bukkit.getLogger().warning("Failed to load " + load.getName());
			}
		}
	}
	
	/**
	 * Saves the storage files to disk
	 * 
	 */
	public void save(){
		list.forEach((T save) -> {
			try{
				JsonObject obj = save.toJSON();
				FileWriter file = new FileWriter(getPath(save));
				Gson gson = new GsonBuilder().setPrettyPrinting().create();
				file.write(gson.toJson(obj));
				file.close();
			}catch(Exception e){
				e.printStackTrace();
				Bukkit.getLogger().warning("Failed to save " + save.getFileName());
			}
		});
	}
	
	public List<T> getList(){
		return this.list;
	}
	
	private String getPath(T obj){
		return DungeonRealms.getInstance().getDataFolder() + "/" + this.directory + "/" + obj.getFileName().replaceAll("\\.", "").replaceAll("\\/", "") + ".json";
	}

	public void delete(T obj) {
		File f = new File(getPath(obj));
		if(f.exists())
			f.delete();
	}
}
