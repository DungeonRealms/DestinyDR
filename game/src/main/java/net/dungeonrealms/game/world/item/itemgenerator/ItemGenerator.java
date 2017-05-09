package net.dungeonrealms.game.world.item.itemgenerator;

import com.google.common.collect.Lists;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.game.anticheat.AntiDuplication;
import net.dungeonrealms.game.enchantments.EnchantmentAPI;
import net.dungeonrealms.game.item.ItemType;
import net.dungeonrealms.game.item.PersistentItem;
import net.dungeonrealms.game.item.items.core.*;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.world.item.Item;
import net.dungeonrealms.game.world.item.itemgenerator.engine.ItemModifier;
import net.dungeonrealms.game.world.item.itemgenerator.modifiers.ArmorModifiers;
import net.dungeonrealms.game.world.item.itemgenerator.modifiers.WeaponModifiers;
import net.minecraft.server.v1_9_R2.*;
import net.minecraft.server.v1_9_R2.NBTBase.NBTNumber;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_9_R2.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.Repairable;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

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
     * @param fullObj
     * @param template
     */
    public static ItemStack createItem(JsonObject fullObj, String template) {
    	ItemStack item = new ItemStack(Material.valueOf(fullObj.get("id").getAsString()), fullObj.get("count").getAsInt(), fullObj.get("damage").getAsShort());
    	try {
    		if (fullObj.has("tag")) {
    			net.minecraft.server.v1_9_R2.ItemStack nms = CraftItemStack.asNMSCopy(item);
    			NBTTagCompound tag = MojangsonParser.parse(fullObj.get("tag").getAsString());

    			// Apply Antidupe.
    			if (tag.hasKey("u"))
    				tag.setString("u", AntiDuplication.createEpoch(item));

    			// Apply Custom ID
    			if (template != null)
    				tag.setString("customId", template);

    			applyRNG(tag, true);
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
     * Applies the RNG to the ranges in an NBT tag.
     * Does not apply to child tag compounds.
     */
    public static void applyRNG(NBTTagCompound tag) {
    	applyRNG(tag, false);
    }
    
    /**
     * Applies RNG to the ranges in an NBT tag.
     * Applies to child components.
     */
    public static void applyRNG(NBTTagCompound tag, boolean deep) {
    	System.out.println("Trying to apply RNG to " + tag.toString());
    	for (String key : tag.c()) {
    		NBTBase base = tag.get(key);
    		
    		System.out.println("Trying to generate from " + base.toString());
    		if (base instanceof NBTTagList || base instanceof NBTTagIntArray) {
    			tag.set(key, applyRNG(base));
    		} else if (base instanceof NBTTagCompound && deep) {
    			applyRNG((NBTTagCompound) base, true);
    		}
    	}
    }
    
    /**
     * Loads a data range from an nbt int array or an nbt list.
     */
    private static NBTBase applyRNG(NBTBase data) {
    	System.out.println("Trying to load " + data.toString());
    	
    	if (data instanceof NBTTagIntArray) {
    		int[] d = ((NBTTagIntArray)data).c();
    		return new NBTTagInt( d.length > 1 ? Utils.randInt(d[0], d[1]) : d[0]);
    	} else if (data instanceof NBTTagList) {
    		NBTTagList list = (NBTTagList) data;
    		if (list.h(0) instanceof NBTNumber) {
    			return new NBTTagInt(list.size() > 1 ? Utils.randInt(list.c(0), list.c(1)) : list.c(0));
    		} else if (list.h(0) instanceof NBTTagList || list.h(0) instanceof NBTTagIntArray){
    			NBTTagList newData = new NBTTagList();
    			newData.add(applyRNG(list.h(0)));
    			newData.add(applyRNG(list.h(1)));
    			return newData;
    		}
    	}
    	
    	return data;
    }
    
    /**
     * Saves a custom item to disk
     * Silently fails if non US0
     */
    public static void saveItem(ItemStack item, String itemName) {
    	if (!DungeonRealms.isMaster())
    		return;
    	
    	try {
			FileWriter file = new FileWriter(getFile(itemName));
			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			file.write(gson.toJson(toJson(item)));
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

    private static String stripExtension (String str) {
        // Handle null case specially.

        if (str == null) return null;

        // Get position of last '.'.

        int pos = str.lastIndexOf(".");

        // If there wasn't any '.' just return the string as is.

        if (pos == -1) return str;

        // Otherwise return the string, up to the dot.

        return str.substring(0, pos);
    }

    public static void convertOldItemTemplates() {
        System.out.println("Attemtping to convert old file templates!");
        File directory = new File("plugins/DungeonRealms/custom_items_old");
        if(directory == null || !directory.exists()) {
            System.out.println("Nothing to convert!");
            return;
        }
        File[] inDir = directory.listFiles();
        System.out.println("We found " + inDir.length + " items to convert!");
        fileLoop: for(File oldFile : inDir) {
            if(oldFile == null || !oldFile.exists()) {
                System.out.println("ItemTemplate File iteration error code 1");
                continue;
            }
            if(oldFile.isDirectory()) {
                System.out.println("ItemTemplate File iteration error code 2");
                continue;
            }
            if(!oldFile.getName().endsWith(".item"))  {
                System.out.println("ItemTemplate File iteration error code 3");
                continue;
            }

            String template_name = stripExtension(oldFile.getName());

            int item_id = -1;
            String item_name = "";
            List<String> item_lore = new ArrayList<>();
            LinkedHashMap<String, NBTTagInt> NBTModifiers = new LinkedHashMap<>();
            ItemStack is = null;

            BufferedReader reader = null;
            boolean foundStats = false;
            try {
                reader = new BufferedReader(new FileReader(oldFile));

                String line;

                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("item_name=")) {
                        line = ChatColor.translateAlternateColorCodes('&', line);

                        item_name = line.substring(line.indexOf("=") + 1, line.length());
                    } else if (line.startsWith("item_id=")) {
                        item_id = Integer.parseInt(line.substring(line.indexOf("=") + 1, line.length()));
                        is = new ItemStack(Material.getMaterial(item_id));
                    } else if (line.contains(":")) {
                        if (is == null) {
                            Utils.log.warning("[ItemGenerator] Missing item id from item " + template_name + "!");
                            continue fileLoop;
                        }

                        // It's lore!
                        line = ChatColor.translateAlternateColorCodes('&', line);
                        //line = ChatColor.stripColor(line);

                        String modifierName = ChatColor.stripColor(line);
                        modifierName = modifierName.substring(0, modifierName.indexOf(':'));

                        if (line.contains("(")) {
                            // Number range!
                            String line_copy = line;
                            for (String s : line_copy.split("\\(")) {
                                if (!(s.contains("~"))) {
                                    continue;
                                }
                                int lower = Integer.parseInt(s.substring(0, s.indexOf("~")));
                                int upper = Integer.parseInt(s.substring(s.indexOf("~") + 1, s.indexOf(")")));

                                foundStats = true;
                                int val = ThreadLocalRandom.current().nextInt((upper - lower)) + lower;
                                if (line.contains("+") || line.contains("-")) {
                                    line = line.replace("(" + lower + "~" + upper + ")", String.valueOf(val));
                                } else {
                                    if (!line.contains("-")) {
                                        line = line.replace("(" + lower + "~" + upper + ")", "+" + String.valueOf(val));
                                    }
                                }
                            }
                        }

                        // set NBT tags
                        if (net.dungeonrealms.game.item.items.core.ItemWeapon.isWeapon(is)) {
                            Item.WeaponAttributeType attribute = Item.WeaponAttributeType.getByName(modifierName);
                            if (attribute == null) {
                                Utils.log.warning("[ItemGenerator] Invalid modifier " + modifierName + " for item " + template_name + "!");
                                continue fileLoop;
                            }

                            if (line.contains("-")) { // range
                                String lowVal = line.split("-")[0];
                                String highVal = line.split("-")[1];

                                int lowInt = Integer.parseInt(lowVal.replaceAll("\\D", ""));
                                int highInt = Integer.parseInt(highVal.replaceAll("\\D", ""));

                                NBTModifiers.put(attribute.getNBTName() + "Min", new NBTTagInt(lowInt));
                                NBTModifiers.put(attribute.getNBTName() + "Max", new NBTTagInt(highInt));
                                foundStats = true;
                            } else { // static val
                                int val = Integer.parseInt(line.replaceAll("\\D", ""));

                                NBTModifiers.put(attribute.getNBTName(), new NBTTagInt(val));
                                foundStats = true;
                            }

                        } else if (net.dungeonrealms.game.item.items.core.ItemArmor.isArmor(is)) {
                            Item.ArmorAttributeType attribute = Item.ArmorAttributeType.getByName(modifierName);

                            if (Item.ArmorAttributeType.getByName(modifierName) == null) {
                                Utils.log.warning("[ItemGenerator] Invalid modifier " + modifierName + " for item " + template_name + "!");
                                continue fileLoop;
                            }

                            if (line.contains("-")) { // range
                                String lowVal = line.split("-")[0];
                                String highVal = line.split("-")[1];

                                int lowInt = Integer.parseInt(lowVal.replaceAll("\\D", ""));
                                int highInt = Integer.parseInt(highVal.replaceAll("\\D", ""));

                                NBTModifiers.put(attribute.getNBTName() + "Min", new NBTTagInt(lowInt));
                                NBTModifiers.put(attribute.getNBTName() + "Max", new NBTTagInt(highInt));
                                foundStats = true;
                            } else { // static val
                                int val = Integer.parseInt(line.replaceAll("\\D", ""));
                                NBTModifiers.put(attribute.getNBTName(), new NBTTagInt(val));
                                foundStats = true;
                            }
                        }

                        item_lore.add(line);
                    } else {
                        item_lore.add(ChatColor.translateAlternateColorCodes('&', line));
                    }
                }
            } catch (Exception e) {
                Utils.log.info("Template error - " + template_name);
                e.printStackTrace();
            } finally {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (is == null) {
                Utils.log.warning("[ItemGenerator] Missing item id from item " + template_name + "!");
                return;
            }


            ItemMeta im = is.getItemMeta();
            im.setDisplayName(item_name);
            im.setLore(item_lore);
            is.setItemMeta(im);

            // check rarity
            Item.ItemRarity rarity = null;
            for (String line : Lists.reverse(item_lore)) {
                for (Item.ItemRarity itemRarity : Item.ItemRarity.values()) {
                    if (ChatColor.stripColor(line).equals(ChatColor.stripColor(itemRarity.getName()))) {
                        rarity = itemRarity;
                        break;
                    }
                }
                if (rarity != null) break;
            }

            if (rarity == null) {
                if(!foundStats)continue fileLoop;
                // Add rarity if needed.
                rarity = Item.ItemRarity.UNIQUE; // default to unique
                item_lore.add(rarity.getName());
                im.setLore(item_lore);
                is.setItemMeta(im);
                setCustomItemDurability(is, 1500);
            }

            setCustomItemDurability(is, 1500);

            // check soulbound, untradeable, or permanently untradeable
            boolean isSoulbound = false, isUntradeable = false, isPermanentlyUntradeable = false;
            for (String line : Lists.reverse(item_lore)) {
                if (line.contains("Soulbound")) {
                    isSoulbound = true;
                    break; // an item can only be one of the three
                } else if (line.contains("Permanently Untradeable")) {
                    isPermanentlyUntradeable = true;
                    break;
                } else if (line.contains("Untradeable")) {
                    isUntradeable = true;
                    break;
                }
            }

            // set NBT tags
            net.minecraft.server.v1_9_R2.ItemStack nmsStack = CraftItemStack.asNMSCopy(is);
            // NMS stack for writing NBT tags
            NBTTagCompound tag = nmsStack.getTag() == null ? new NBTTagCompound() : nmsStack.getTag();

            //guess ill go make a sammi, brb
            //tag.set("type", new NBTTagInt(Item.ItemType.getTypeFromMaterial(is.getType()).getId()));
            tag.set("itemRarity", new NBTTagInt(rarity.getId()));
            tag.set("soulbound", new NBTTagInt(isSoulbound ? 1 : 0));
            tag.set("untradeable", new NBTTagInt(isUntradeable ? 1 : 0));
            tag.set("puntradeable", new NBTTagInt(isPermanentlyUntradeable ? 1 : 0));

        /*
        The line below removes the weapons attributes.
        E.g. Diamond Sword says, "+7 Attack Damage"
         */
            tag.set("AttributeModifiers", new NBTTagList());
            tag.set("itemTier", new NBTTagInt(getTierFromMaterial(is.getType())));

            // set item type
            if (net.dungeonrealms.game.item.items.core.ItemWeapon.isWeapon(is)) {
                tag.set("type", new NBTTagString("weapon"));
            } else if (net.dungeonrealms.game.item.items.core.ItemArmor.isArmor(is)) {
                tag.set("type", new NBTTagString("armor"));
            }

            NBTTagList modifiersList = new NBTTagList();

            for (Map.Entry<String, NBTTagInt> entry : NBTModifiers.entrySet()) {
                tag.set(entry.getKey(), entry.getValue());

                if (!entry.getKey().contains("Max")) {
                    if (entry.getKey().contains("Min")) {
                        modifiersList.add(new NBTTagString(entry.getKey().replace("Min", "")));
                        continue;
                    }
                    modifiersList.add(new NBTTagString(entry.getKey()));
                }
            }

            tag.set("modifiers", modifiersList);
            tag.set("drItemId", new NBTTagString(template_name));

//        tag.a(CraftItemStack.asNMSCopy(is).getTag());
            nmsStack.setTag(tag);

            System.out.println("Successfully converted the old template named '" + template_name + ".item'");
            saveItem(is,template_name);

            //return AntiDuplication.getInstance().applyAntiDupe(CraftItemStack.asBukkitCopy(nmsStack));
        }


    }

    public static void setCustomItemDurability(ItemStack itemStack, double durability) {
        try {
            Repairable repairable = (Repairable) itemStack.getItemMeta();
            repairable.setRepairCost((int) durability);
            itemStack.setItemMeta((ItemMeta) repairable);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static int getTierFromMaterial(Material m) {
        if(m.name().toLowerCase().contains("leather")) return 1;
        if(m.name().toLowerCase().contains("chain")) return 2;
        if(m.name().toLowerCase().contains("iron")) return 3;
        if(m.name().toLowerCase().contains("diamond")) return 4;
        if(m.name().toLowerCase().contains("gold")) return 5;
        return 1;
    }
}
