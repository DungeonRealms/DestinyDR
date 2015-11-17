package net.dungeonrealms.json;

import java.util.HashMap;
import java.util.Map.Entry;

import com.google.gson.JsonObject;


/**
 * Created by Chase on Nov 17, 2015
 */
public class JsonBuilder {
	
	private JsonObject json;
	
	public JsonBuilder(){
		initiateData();
	}
	
	public JsonBuilder(String key, Object value){
		initiateData();
		json.addProperty(key, value.toString());
	}
	
	public JsonBuilder(HashMap<String, Object> data){
		initiateData();
		for(Entry<String, Object> x : data.entrySet()){
			json.addProperty(x.getKey(), x.getValue().toString());
		}
	}
	
	private void initiateData(){
		json = new JsonObject();
	}
	
	public JsonBuilder setData(String key, Object value){
		json.addProperty(key, value.toString());
		return this;
	}
	
	public JsonObject getJson(){
		return json;
	}
	
	@Override
	public String toString(){
		return json.toString();
	}
	
}