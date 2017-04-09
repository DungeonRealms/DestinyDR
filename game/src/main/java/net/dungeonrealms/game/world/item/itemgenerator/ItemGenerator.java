package net.dungeonrealms.game.world.item.itemgenerator;

import com.google.common.collect.Lists;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.game.anticheat.AntiDuplication;
import net.dungeonrealms.game.enchantments.EnchantmentAPI;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.world.item.Item;
import net.dungeonrealms.game.world.item.itemgenerator.engine.ItemModifier;
import net.dungeonrealms.game.world.item.itemgenerator.engine.ModifierCondition;
import net.dungeonrealms.game.world.item.itemgenerator.engine.ModifierType;
import net.dungeonrealms.game.world.item.itemgenerator.modifiers.ArmorModifiers;
import net.dungeonrealms.game.world.item.itemgenerator.modifiers.WeaponModifiers;
import net.dungeonrealms.game.world.item.repairing.RepairAPI;
import net.minecraft.server.v1_9_R2.NBTTagCompound;
import net.minecraft.server.v1_9_R2.NBTTagInt;
import net.minecraft.server.v1_9_R2.NBTTagList;
import net.minecraft.server.v1_9_R2.NBTTagString;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_9_R2.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class ItemGenerator {

    public static HashMap<Class<? extends ItemModifier>, ItemModifier> modifiers = new HashMap<>();
    public static List<ItemModifier> modifierObjects = new ArrayList<>();
    /**
     * Gets the custom named item located in the custom_items directory.
     *
     * @param template_name - the name of the item
     * @return
     */
    public static ItemStack getNamedItem(String template_name) {
        File template = new File("plugins/DungeonRealms/custom_items/" + template_name + ".item");
        if (!(template.exists())) {
            Utils.log.warning("[ItemGenerator] Custom item " + template_name + " not found!");
            return null; // No such custom template!
        }

        int item_id = -1;
        String item_name = "";
        List<String> item_lore = new ArrayList<>();
        LinkedHashMap<String, NBTTagInt> NBTModifiers = new LinkedHashMap<>();
        ItemStack is = null;

        BufferedReader reader = null;
        boolean foundStats = false;
        try {
            reader = new BufferedReader(new FileReader(template));

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
                        return null;
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
                            int val = rand.nextInt((upper - lower)) + lower;
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
                    if (Item.GeneratedItemType.isWeapon(is)) {
                        Item.WeaponAttributeType attribute = Item.WeaponAttributeType.getByName(modifierName);
                        if (attribute == null) {
                            Utils.log.warning("[ItemGenerator] Invalid modifier " + modifierName + " for item " + template_name + "!");
                            return null;
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

                    } else if (Item.GeneratedItemType.isArmor(is)) {
                        Item.ArmorAttributeType attribute = Item.ArmorAttributeType.getByName(modifierName);

                        if (Item.ArmorAttributeType.getByName(modifierName) == null) {
                            Utils.log.warning("[ItemGenerator] Invalid modifier " + modifierName + " for item " + template_name + "!");
                            return null;
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
            return null;
        }

//        boolean hasSoulboundTag = false;
//        for (String line : item_lore) {
//            if (line.contains("Soulbound")) {
//                hasSoulboundTag = true;
//                break;
//            }
//        }
//        if (!hasSoulboundTag) {
//            item_lore.add(ChatColor.DARK_RED + "Soulbound");
//        }

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
            if(!foundStats)return is;
            // Add rarity if needed.
            rarity = Item.ItemRarity.UNIQUE; // default to unique
            item_lore.add(rarity.getName());
            im.setLore(item_lore);
            is.setItemMeta(im);
            RepairAPI.setCustomItemDurability(is, 1500);
        }

        RepairAPI.setCustomItemDurability(is, 1500);

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

        tag.set("GeneratedItemType", new NBTTagInt(Item.GeneratedItemType.getTypeFromMaterial(is.getType()).getId()));
        tag.set("itemRarity", new NBTTagInt(rarity.getId()));
        tag.set("soulbound", new NBTTagInt(isSoulbound ? 1 : 0));
        tag.set("untradeable", new NBTTagInt(isUntradeable ? 1 : 0));
        tag.set("puntradeable", new NBTTagInt(isPermanentlyUntradeable ? 1 : 0));

        /*
        The line below removes the weapons attributes.
        E.g. Diamond Sword says, "+7 Attack Damage"
         */
        tag.set("AttributeModifiers", new NBTTagList());
        tag.set("itemTier", new NBTTagInt(Item.getTierFromMaterial(is.getType()).getTierId()));

        // set item type
        if (Item.GeneratedItemType.isWeapon(is)) {
            tag.set("type", new NBTTagString("weapon"));
        } else if (Item.GeneratedItemType.isArmor(is)) {
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

        return AntiDuplication.getInstance().applyAntiDupe(CraftItemStack.asBukkitCopy(nmsStack));
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
