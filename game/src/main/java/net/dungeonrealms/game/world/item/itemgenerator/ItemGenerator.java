package net.dungeonrealms.game.world.item.itemgenerator;

import com.google.common.collect.Lists;
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
import net.dungeonrealms.game.world.item.Item;
import net.dungeonrealms.game.world.item.itemgenerator.engine.ItemModifier;
import net.dungeonrealms.game.world.item.itemgenerator.modifiers.ArmorModifiers;
import net.dungeonrealms.game.world.item.itemgenerator.modifiers.WeaponModifiers;
import net.minecraft.server.v1_9_R2.*;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_9_R2.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.Repairable;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
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
        data = data.replace("\"@#", "<").replace("$?\"", ">");

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

    private static String stripExtension(String str) {
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
        if (!directory.exists()) {
            System.out.println("Nothing to convert!");
            return;
        }
        File[] inDir = directory.listFiles();
        System.out.println("We found " + inDir.length + " items to convert!");
        fileLoop:
        for (File oldFile : inDir) {
            if (oldFile == null || !oldFile.exists()) {
                System.out.println("ItemTemplate File iteration error code 1");
                continue;
            }
            if (oldFile.isDirectory()) {
                System.out.println("ItemTemplate File iteration error code 2");
                continue;
            }
            if (!oldFile.getName().endsWith(".item")) {
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
            NBTTagCompound itemAttributes = new NBTTagCompound();
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
                        modifierName = modifierName.substring(0, modifierName.indexOf(':') + 1);

                        net.dungeonrealms.game.world.item.Item.AttributeType attributeType = net.dungeonrealms.game.world.item.Item.AttributeType.getAttributeTypeFromPrefix(modifierName);

                        if (attributeType == null) {
                            Bukkit.getLogger().info("Null attribute type: " + modifierName + " for " + template_name);
                            continue;
                        }
                        boolean firstRange = true;

                        boolean isRange = false;
                        if (line.contains("(")) {
                            // Number range!
                            NBTTagList list = new NBTTagList();
                            if (line.contains(" - ")) {
                                //Seems like we have some data first?
                                String firstNum = line.split(" - ")[0].trim();
                                if (firstNum.contains(":")) firstNum = firstNum.split(":")[1].trim();
                                if (StringUtils.isNumeric(firstNum)) {
                                    int firstData = Integer.parseInt(firstNum);
                                    Bukkit.getLogger().info("Converted busted line " + line + " from " + template_name);
                                    list.add(new NBTTagString("@#" + 1 + "," + firstData + "$?"));
                                    isRange = true;
                                }
                            }
                            String range = "";
                            for (String s : line.split("\\(")) {
                                if (!s.contains("~")) {
                                    continue;
                                }
                                int lower = Integer.parseInt(s.substring(0, s.indexOf("~")));
                                int upper = Integer.parseInt(s.substring(s.indexOf("~") + 1, s.indexOf(")")));

                                if (!range.isEmpty())
                                    isRange = true;
                                if (!firstRange) {
                                    range += ",";
                                }
                                range += "@#" + lower + "," + upper + "$?";

                                list.add(getData(lower, upper));
                                firstRange = false;
                                foundStats = true;
                            }

                            if (isRange)
                                range = "[" + range + "]";


                            if (isRange)
                                itemAttributes.set(attributeType.getNBTName(), list);
                            else
                                itemAttributes.setString(attributeType.getNBTName(), range);

                            Bukkit.getLogger().info("Loaded attribute: " + attributeType.getNBTName() + " with range: " + range + " for " + template_name);
                            continue;
                        } else if (line.contains("%") || line.contains("+")) {
                            //% or +?
                            String repl = line.replace("%", "").replace("+", "").replace("HP/s", "");

                            String dataValues = repl.split(":")[1].trim();
                            if (dataValues.contains(" - ")) {
                                //Rip.. old armor scuffed values?

                                String[] args = dataValues.split(" - ");
                                int first = Integer.parseInt(args[0]);
                                int second = Integer.parseInt(args[1]);
//                                NBTTagList list = new NBTTagList();
//
//                                list.add(new NBTTagInt(first));
//                                list.add(new NBTTagInt(second));
                                itemAttributes.set(attributeType.getNBTName(), getData(first, second));
                                Bukkit.getLogger().info("Loaded old busted attribute: " + attributeType.getNBTName() + " with old range: " + first + " - " + second + " for " + template_name);
                                continue;
                            }
                            int val = Integer.parseInt(dataValues);

                            Bukkit.getLogger().info("Loaded attribute: " + attributeType.getNBTName() + " with value: " + val + " for " + template_name);
                            itemAttributes.setInt(attributeType.getNBTName(), val);
                            continue;
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

            // check rarity
            net.dungeonrealms.game.world.item.Item.ItemRarity rarity = null;
            for (String line : Lists.newLinkedList(Lists.reverse(item_lore))) {
                for (net.dungeonrealms.game.world.item.Item.ItemRarity itemRarity : net.dungeonrealms.game.world.item.Item.ItemRarity.values()) {
                    if (ChatColor.stripColor(line).equals(ChatColor.stripColor(itemRarity.getName()))) {
                        if (rarity == null)
                            rarity = itemRarity;
                        item_lore.remove(line);
                    }
                }
            }

            if (rarity == null) {
                if (!foundStats) continue fileLoop;
                // Add rarity if needed.
                rarity = net.dungeonrealms.game.world.item.Item.ItemRarity.UNIQUE; // default to unique
//                item_lore.add(rarity.getName());
//                im.setLore(item_lore);
//                is.setItemMeta(im);
            }

            // check soulbound, untradeable, or permanently untradeable
            boolean isSoulbound = false, isUntradeable = false, isPermanentlyUntradeable = false;
            for (String line : Lists.newArrayList(item_lore)) {
                if (line.contains("Soulbound")) {
                    isSoulbound = true;
                    item_lore.remove(line);
                } else if (line.contains("Permanently Untradeable")) {
                    isPermanentlyUntradeable = true;
                    item_lore.remove(line);
                } else if (line.contains("Untradeable")) {
                    isUntradeable = true;
                    item_lore.remove(line);
                }
            }
            ItemMeta im = is.getItemMeta();
            im.setDisplayName(item_name);
            im.setLore(item_lore);
            is.setItemMeta(im);

            // set NBT tags
            net.minecraft.server.v1_9_R2.ItemStack nmsStack = CraftItemStack.asNMSCopy(is);
            // NMS stack for writing NBT tags
            NBTTagCompound tag = nmsStack.getTag() == null ? new NBTTagCompound() : nmsStack.getTag();

            tag.setInt("RepairCost", 1500);
            //guess ill go make a sammi, brb
            //tag.set("type", new NBTTagInt(Item.ItemType.getTypeFromMaterial(is.getType()).getId()));
            tag.setString("itemRarity", rarity.name());
            if (isSoulbound)
                tag.set("soulbound", new NBTTagInt(1));
            if (isUntradeable)
                tag.set("untradeable", new NBTTagInt(1));
            if (isPermanentlyUntradeable)
                tag.set("puntradeable", new NBTTagInt(1));

        /*
        The line below removes the weapons attributes.
        E.g. Diamond Sword says, "+7 Attack Damage"
         */
//            tag.set("AttributeModifiers", new NBTTagList());
            tag.set("itemTier", new NBTTagInt(getTierFromMaterial(is.getType())));
            Item.GeneratedItemType type = Item.GeneratedItemType.getType(is.getType());

            if (type != null) {
                tag.setString("type", type.name().toLowerCase());
            }
            tag.set("itemAttributes", itemAttributes);
//            tag.set("drItemId", new NBTTagString(template_name));

//        tag.a(CraftItemStack.asNMSCopy(is).getTag());
            nmsStack.setTag(tag);

            System.out.println("Successfully converted the old template named '" + template_name + ".item'");
            saveItem(CraftItemStack.asCraftMirror(nmsStack), template_name);

            //return AntiDuplication.getInstance().applyAntiDupe(CraftItemStack.asBukkitCopy(nmsStack));
        }


    }

    private static NBTTagString getData(int min, int max) {
        return new NBTTagString("@#" + min + "," + max + "$?");
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
        if (m.name().toLowerCase().contains("leather")) return 1;
        if (m.name().toLowerCase().contains("chain")) return 2;
        if (m.name().toLowerCase().contains("iron")) return 3;
        if (m.name().toLowerCase().contains("diamond")) return 4;
        if (m.name().toLowerCase().contains("gold")) return 5;
        return 1;
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
