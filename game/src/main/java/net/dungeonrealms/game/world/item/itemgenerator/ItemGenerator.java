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
import net.dungeonrealms.game.world.item.itemgenerator.engine.ItemModifier;
import net.dungeonrealms.game.world.item.itemgenerator.modifiers.ArmorModifiers;
import net.dungeonrealms.game.world.item.itemgenerator.modifiers.WeaponModifiers;
import net.minecraft.server.v1_9_R2.*;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_9_R2.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ItemGenerator {

    public static HashMap<Class<? extends ItemModifier>, ItemModifier> modifiers = new HashMap<>();
    public static List<ItemModifier> modifierObjects = new ArrayList<>();

    private static File getFile(String itemName) {
        return new File(GameAPI.getDataFolder() + "/custom_items/" + Utils.sanitizeFileName(itemName) + ".item");
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
     * Loads a custom item from disk.
     *
     * @param templateName
     */
    public static ItemStack getNamedItem(String templateName) {
        File file = getFile(templateName);
        if (!file.exists()) {
            Utils.log.warning("[ItemGenerator] Custom item " + templateName + " not found!");
            return new ItemStack(Material.AIR); // No such custom template!
        }

        try {
            BufferedReader br = new BufferedReader(new FileReader(file)); //Read from file
            return createItem(new JsonParser().parse(br).getAsJsonObject(), templateName); //Create Item
        } catch (IOException e) {
            e.printStackTrace();
            Utils.log.info("[ItemGenerator] Failed to load " + templateName + ".");
        }

        return new ItemStack(Material.AIR);
    }

    /**
     * Converts an item into a json object for saving.
     *
     * @param item
     */
    public static JsonObject toJson(ItemStack item) {
        VanillaItem toSave = new VanillaItem(item);
        JsonObject fullObj = new JsonObject();

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
        if (!DungeonRealms.isMaster())
            return;

        VanillaItem vi = new VanillaItem(item);
        vi.removeTag("display"); // Just takes extra space and can make editting the tag more annoying..
        item = vi.generateItem();

        try {
            FileWriter file = new FileWriter(getFile(itemName));
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            file.write(gson.toJson(toJson(item)));
            file.close();
        } catch (Exception e) {
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
