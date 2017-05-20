package net.dungeonrealms.game.world.item.itemgenerator;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.game.anticheat.AntiDuplication;
import net.dungeonrealms.game.item.PersistentItem;
import net.dungeonrealms.game.item.items.core.VanillaItem;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.world.entity.type.monster.type.EnumNamedElite;
import net.dungeonrealms.game.world.item.itemgenerator.engine.ItemModifier;
import net.dungeonrealms.game.world.item.itemgenerator.modifiers.ArmorModifiers;
import net.dungeonrealms.game.world.item.itemgenerator.modifiers.WeaponModifiers;
import net.minecraft.server.v1_9_R2.*;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_9_R2.inventory.CraftItemStack;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ItemGenerator {

    public static HashMap<Class<? extends ItemModifier>, ItemModifier> modifiers = new HashMap<>();
    public static List<ItemModifier> modifierObjects = new ArrayList<>();

    private static File getFile(String itemName, String ext) {
        return new File(GameAPI.getDataFolder() + "/custom_items/" + Utils.sanitizeFileName(itemName) + "." + ext);
    }

    public static ItemStack createItem(JsonObject fullObj) {
        return createItem(fullObj, null);
    }

    /**
     * Creates an ItemStack from a JsonObject, with a set custom ID.
     * The custom Id is usually used so quests can identify items.
     *
     * @param fullObj
     * @param template
     */
    public static ItemStack createItem(JsonObject fullObj, String template) {
        ItemStack item = new ItemStack(Material.valueOf(fullObj.get("id").getAsString()), fullObj.get("count").getAsInt(), fullObj.get("damage").getAsShort());
        try {
            if (fullObj.has("tag")) {
                net.minecraft.server.v1_9_R2.ItemStack nms = CraftItemStack.asNMSCopy(item);
                NBTTagCompound tag = loadNBT(fullObj.get("tag").getAsString());

                // Apply Antidupe.
                if (tag.hasKey("u"))
                    tag.setString("u", AntiDuplication.createEpoch(item));

                //Dont show thanks..
                tag.set("AttributeModifiers", new NBTTagList());

                // Apply Custom ID
                if (template != null)
                    tag.setString("customId", template);

                // Create Item
                nms.setTag(tag);
                item = CraftItemStack.asBukkitCopy(nms);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Utils.log.info("NBT Json is formatted incorrectly. (ID = " + template + ")");
        }

        return PersistentItem.constructItem(item).generateItem();
    }

    /**
     * Loads an NBTTagCompound from a string.
     * Applies RNG, so don't use this on anything a user can control.
     */
    public static NBTTagCompound loadNBT(String data) throws MojangsonParseException {
    	
        //Format: <x,y> Generates a random value between <x,y>
        Matcher rangeMatch = Pattern.compile("<\\d+,\\d+>").matcher(data);
        while (rangeMatch.find()) {
            String found = rangeMatch.group();
            String[] range = found.substring(1, found.length() - 1).split(",");
            data = data.replace(found, Utils.randInt(Integer.parseInt(range[0]), Integer.parseInt(range[1])) + "");
        }

        return MojangsonParser.parse(data);
    }
    
    /**
     * Load the elite gear for a given entity type.
     * @param type
     */
    public static Map<EquipmentSlot, ItemStack> getEliteGear(EnumNamedElite type) {
    	return getEliteGear(type.name().toLowerCase());
    }
    
    /**
     * Load the elite gear from a file name.
     * @param eliteName
     * @return
     */
    public static Map<EquipmentSlot, ItemStack> getEliteGear(String eliteName) {
    	Map<EquipmentSlot, ItemStack> map = new HashMap<>();
    	JsonObject obj = readJSON(eliteName, "elite");
    	for (EquipmentSlot s : EquipmentSlot.values()) {
    		if (obj.has(s.name()))
    			map.put(s, createItem(obj.get(s.name()).getAsJsonObject()));
    	}
    	return map;
    }
    
    /**
     * Save elite gear to disk.
     * @param e
     * @param fileName
     */
    public static void saveEliteGear(EntityEquipment e, String fileName) {
    	JsonObject entire = new JsonObject();
    	for (EquipmentSlot slot : EquipmentSlot.values()) {
    		ItemStack i = GameAPI.getItem(e, slot);
    		if (i != null && i.getType() != Material.AIR)
    			entire.add(slot.name(), toJson(i));
    	}
    	saveJSON(fileName, "elite", entire);
    }

    /**
     * Loads a custom item from disk.
     *
     * @param templateName
     */
    public static ItemStack getNamedItem(String templateName) {
        File file = getFile(templateName, "item");
        if (!file.exists()) {
            Utils.log.warning("[ItemGenerator] Custom item " + templateName + " not found!");
            return new ItemStack(Material.AIR); // No such custom template!
        }

        JsonObject obj = readJSON(templateName, "item");
        return obj != null ? createItem(obj, templateName) : new ItemStack(Material.AIR);
    }

    /**
     * Converts an item into a json object for saving.
     *
     * @param item
     */
    public static JsonObject toJson(ItemStack item) {
        VanillaItem toSave = new VanillaItem(item);
        JsonObject fullObj = new JsonObject();

        toSave.removeTag("display"); // Display is only used to show to the player, it'll get regenerated on load.
        toSave.removeTag("customId"); // Gets overriden.
        if (toSave.hasTag("u"))
        	toSave.setTagBool("u", true); // Takes up extra space, and will get regened anyways.
        
        // Save Data.
        fullObj.addProperty("count", item.getAmount());
        fullObj.addProperty("damage", item.getDurability());
        fullObj.addProperty("id", item.getType().name());
        fullObj.addProperty("tag", toSave.getTag().toString());

        return fullObj;
    }

    /**
     * Saves a custom item to disk
     * Silently fails if non US0
     */
    public static void saveItem(ItemStack item, String itemName) {
        if (DungeonRealms.isMaster())
            saveJSON(itemName, "item", toJson(item));
    }
    
    private static JsonObject readJSON(String fileName, String ext) {
    	try {
            BufferedReader br = new BufferedReader(new FileReader(getFile(fileName, ext))); //Read from file
            return new JsonParser().parse(br).getAsJsonObject();
        } catch (IOException e) {
            e.printStackTrace();
            Utils.log.info("[ItemGenerator] Failed to load " + fileName + "." + ext);
            return null;
        }
    }
    
    private static void saveJSON(String fileName, String ext, JsonObject object) {
    	try {
            FileWriter file = new FileWriter(getFile(fileName, ext));
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            file.write(gson.toJson(object));
            file.close();
        } catch (Exception e) {
            e.printStackTrace();
            Bukkit.getLogger().warning("Failed to save " + fileName + "." + ext);
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
        //So they can get 2 ty.
        am.new StrDexVitInt();
        am.new StrDexVitInt();
        am.new Thorns();
    }
}
