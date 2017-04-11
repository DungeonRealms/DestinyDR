package net.dungeonrealms.game.world.item.itemgenerator;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.game.anticheat.AntiDuplication;
import net.dungeonrealms.game.item.items.core.VanillaItem;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.world.item.itemgenerator.engine.ItemModifier;
import net.dungeonrealms.game.world.item.itemgenerator.modifiers.ArmorModifiers;
import net.dungeonrealms.game.world.item.itemgenerator.modifiers.WeaponModifiers;
import net.minecraft.server.v1_9_R2.MojangsonParser;
import net.minecraft.server.v1_9_R2.NBTTagCompound;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_9_R2.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.*;

public class ItemGenerator {

    public static HashMap<Class<? extends ItemModifier>, ItemModifier> modifiers = new HashMap<>();
    public static List<ItemModifier> modifierObjects = new ArrayList<>();
    
    private static File getFile(String itemName) {
    	return new File("plugins/DungeonRealms/custom_items/" + itemName + ".item");
    }
    
    /**
     * Loads a custom item from disk.
     * @param templateName
     */
    public static ItemStack getNamedItem(String templateName) {
    	File file = getFile(templateName);
        if (!file.exists()) {
            Utils.log.warning("[ItemGenerator] Custom item " + templateName + " not found!");
            return new ItemStack(Material.AIR); // No such custom template!
        }
        try {
        	
        	BufferedReader br = new BufferedReader(new FileReader(file));
        	JsonObject fullObj = new JsonParser().parse(br).getAsJsonObject();
        	ItemStack item = new ItemStack(Material.valueOf(fullObj.get("id").getAsString()), fullObj.get("count").getAsInt(), fullObj.get("damage").getAsShort());
        	
        	if (fullObj.has("tag")) {
        		net.minecraft.server.v1_9_R2.ItemStack nms = CraftItemStack.asNMSCopy(item);
        		NBTTagCompound tag = MojangsonParser.parse(fullObj.get("tag").getAsString());
        		
        		if (tag.hasKey("u"))
        			tag.setString("u", AntiDuplication.createEpoch(item));
        		
        		tag.setString("customId", templateName);
        		nms.setTag(tag);
        		item = CraftItemStack.asBukkitCopy(nms);
        	}
        	
        	return item;
        } catch (Exception e) {
        	e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * Saves a custom item to disk
     * Silently fails if non US0
     */
    public static void saveItem(ItemStack item, String itemName) {
    	if (!DungeonRealms.getInstance().isMasterShard)
    		return;
    	
    	VanillaItem toSave = new VanillaItem(item);
    	
    	JsonObject fullObj = new JsonObject();
    	
    	fullObj.addProperty("count", item.getAmount());
    	fullObj.addProperty("damage", item.getDurability());
    	fullObj.addProperty("id", item.getType().name());
    	fullObj.addProperty("tag", toSave.getTag().toString());
    	
    	try {
			FileWriter file = new FileWriter(getFile(itemName));
			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			file.write(gson.toJson(fullObj));
			file.close();
		} catch(Exception e) {
			e.printStackTrace();
			Bukkit.getLogger().warning("Failed to save " + itemName + ".item");
		}
    }

    public static void loadModifiers() {
        WeaponModifiers wm = new WeaponModifiers();
        wm.new Accuracy();
        wm.new ArmorPenetration();
        wm.new Blind();
        wm.new Critical();
        wm.new Precision();
        wm.new SwordDamage();
        wm.new AxeDamage();
        wm.new StaffDamage();
        wm.new PolearmDamage();
        wm.new BowDamage();
        wm.new Elemental();
        wm.new ElementalBow();
        wm.new Knockback();
        wm.new LifeSteal();
        wm.new Pure();
        wm.new Slow();
        wm.new SwordDamage();
        wm.new Versus();

        ArmorModifiers am = new ArmorModifiers();
        am.new Block();
        am.new Dodge();
        am.new EnergyRegen();
        am.new GemFind();
        am.new ChestplateHP();
        am.new LeggingsHP();
        am.new BootsHP();
        am.new HelmetHP();
        am.new MainDPS();
        am.new OtherDPS();
        am.new HPRegen();
        am.new ItemFind();
        am.new MainArmor();
        am.new HelmetsArmor();
        am.new LeggingArmor();
        am.new BootsArmor();
        am.new Reflection();
        am.new Resistances();
        am.new StrDexVitInt();
        am.new Thorns();
    }
}
