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

    private Item.ItemType type;
    private Item.ItemTier tier;
    private Item.ItemRarity rarity;

    private int mobTier = -1;
    private boolean isReroll = false;
    @SuppressWarnings("unused")
    private int pLevel;

    private ItemStack item;
    private ItemStack origItem; // for rerolling

    private boolean isSoulbound;
    private boolean isUntradeable;
    private boolean isPermanentlyUntradeable;

    private static Random rand = new Random();

    public ItemGenerator setType(Item.ItemType type) {
        this.type = type;
        return this;
    }

    public ItemGenerator setTier(Item.ItemTier tier) {
        this.tier = tier;
        return this;
    }

    public ItemGenerator setRarity(Item.ItemRarity rarity) {
        this.rarity = rarity;
        return this;
    }

    public ItemGenerator setMobTier(int mobTier) {
        this.mobTier = mobTier;
        return this;
    }

    public ItemGenerator setReroll(boolean reroll) {
        this.isReroll = reroll;
        return this;
    }

    public ItemGenerator setOrigItem(ItemStack origItem) {
        this.origItem = origItem;
        return this;
    }

    public ItemGenerator setPLevel(int level) {
        this.pLevel = level;
        return this;
    }

    public ItemGenerator setItem(ItemStack item) {
        this.item = item;
        return this;
    }

    public ItemGenerator setSoulbound(boolean soulbound) {
        this.isSoulbound = soulbound;
        return this;
    }

    public ItemGenerator setUntradeable(boolean untradeable) {
        this.isUntradeable = untradeable;
        return this;
    }

    public ItemGenerator setPermanentlyUntradeable(boolean permanentlyUntradeable) {
        this.isPermanentlyUntradeable = permanentlyUntradeable;
        return this;
    }

    /**
     * Generates an item with the given tier, type, and rarity. If the
     * tier, type, and rarity instance variables do not have values,
     * this will generate an item with random tier, type, and rarity.
     *
     * @return - an instance of the current ItemGenerator
     * (call getItem() for the ItemStack)
     * @since 1.0
     */
    @SuppressWarnings("unchecked")
    public ItemGenerator generateItem() {
        Item.ItemTier tier = this.tier;
        Item.ItemType type = this.type;
        Item.ItemRarity rarity = this.rarity;

        if (isReroll && origItem != null && (RepairAPI.isItemArmorOrWeapon(origItem))) {
            net.minecraft.server.v1_9_R2.ItemStack nmsStack = CraftItemStack.asNMSCopy(origItem);
            NBTTagCompound tag = nmsStack.getTag() == null ? new NBTTagCompound() : nmsStack.getTag();
            tier = Item.ItemTier.getByTier(tag.getInt("itemTier"));
            type = Item.ItemType.getById(tag.getInt("itemType"));
            rarity = Item.ItemRarity.getById(tag.getInt("itemRarity"));
            this.isSoulbound = tag.getInt("soulbound") == 1;
        }

        // if no values given, generate a random item
        if (tier == null) tier = Item.ItemTier.values()[rand.nextInt(Item.ItemTier.values().length - 1)];
        if (type == null) type = Item.ItemType.values()[rand.nextInt(Item.ItemType.values().length - 1)];
        if (rarity == null) rarity = Item.ItemRarity.values()[rand.nextInt(Item.ItemRarity.values().length - 1)];

        ItemStack item = isReroll && origItem != null && (RepairAPI.isItemArmorOrWeapon(origItem)) ? origItem : new ItemStack(type.getTier(tier));
        ItemMeta meta = item.getItemMeta().clone();

        if (!isReroll) {
            meta.setLore(new ArrayList<>());
        } else {
            if (Item.ItemType.isWeapon(origItem))
                meta.setLore(meta.getLore().subList(0, 1)); // strips everything except for dmg
            else if (Item.ItemType.isArmor(origItem))
                meta.setLore(meta.getLore().subList(0, 3)); // strips everything except for dps/armor, hp, and energy/hp regen
        }

        final HashMap<ModifierCondition, ItemModifier> conditions = new HashMap<>();

        Collections.shuffle(modifierObjects);

        for (ItemModifier modifier : modifierObjects) {
            if (modifier.canApply(type)) {
                if (isReroll && !modifier.isIncludeOnReroll()) continue;
                ModifierCondition mc = modifier.tryModifier(meta, tier, rarity, type, mobTier);
                if (mc != null) {
                    conditions.put(mc, modifier);
                    ModifierCondition bonus = mc.getBonus();
                    while (bonus != null) {
                        String prefix = modifier.getPrefix(meta);
                        String suffix = modifier.getSuffix(meta);

                        if (bonus.getReplacement() != null && bonus.getReplacement().size() > 0) {
                            ItemModifier replacement = ItemGenerator.modifiers.get(bonus.getReplacement().get(rand.nextInt(bonus.getReplacement().size())));
                            prefix = replacement.getPrefix(meta);
                            suffix = replacement.getSuffix(meta);
                        }

                        bonus.setChosenPrefix(prefix);
                        bonus.setChosenSuffix(suffix);

                        conditions.put(bonus, modifier);

                        bonus = bonus.getBonus();
                    }
                }
            }
        }

        List<ModifierCondition> order = new ArrayList<>();

        for (Object ob : Arrays.asList(conditions.keySet().toArray())) {
            ModifierCondition mc = (ModifierCondition) ob;
            if (!mc.canApply(conditions.keySet())) {
                conditions.remove(mc);
            } else {
                ItemModifier im = conditions.get(mc);

                int belowChance = (mc.getChance() < 0) ? im.getChance() : mc.getChance();

                if (rand.nextInt(100) < belowChance) {
                    order.add(mc);
                } else {
                    conditions.remove(mc);
                }
            }
        }

        for (ItemModifier modifier : conditions.values()) {
            for (ModifierCondition mc : (List<ModifierCondition>) ((ArrayList<ModifierCondition>) order).clone()) {
                if (!(mc.checkCantContain(modifier.getClass()))) {
                    order.remove(mc);
                }
            }
        }

        Collections.sort(order, (mc1, mc2) -> conditions.get(mc1).getOrderPriority() - conditions.get(mc2).getOrderPriority());

        String modName = "";
        String name = tier.getTierColor().toString();
        String[] bonuses = new String[24];
        Arrays.fill(bonuses, "");
        LinkedHashMap<String, Integer> NBTModifiers = new LinkedHashMap<>();

        // name armor with energy or hp/s being rerolled correctly
        if (isReroll && origItem != null && origItem.hasItemMeta() && origItem.getItemMeta().hasLore() && Item.ItemType.isArmor(origItem)) {

            for (String line : origItem.getItemMeta().getLore()) {

                if (!line.contains(":")) continue;

                if (ChatColor.stripColor(line.substring(0, line.indexOf(":"))).equals("ENERGY REGEN")) {
                    bonuses[11] = "ENERGY REGEN";
                } else if (ChatColor.stripColor(line.substring(0, line.indexOf(":"))).equals("HP REGEN")) {
                    bonuses[2] = "HP REGEN";
                }

            }

        }

        // NBT tag write and name the item
        for (ModifierCondition mc : order) {
            ItemModifier im = conditions.get(mc);
            meta = im.applyModifier(mc, meta);

            // write NBT tags
            if (mc.getRange().getModifierType() == ModifierType.TRIPLE || mc.getRange().getModifierType() == ModifierType.RANGE) {
                NBTModifiers.put(im.getNBTName() + "Min", mc.getRange().getValLow());
                NBTModifiers.put(im.getNBTName() + "Max", mc.getRange().getValHigh());
            } else {
                NBTModifiers.put(im.getNBTName(), mc.getRange().getValLow());
            }

            modName = ChatColor.stripColor(mc.getChosenPrefix().substring(0, mc.getChosenPrefix().indexOf(":")));

            // apply the prefixes/suffixes to priority array
            // prefixes need to go before suffixes
            switch (modName) {
                // ARMOR PREFIXES
                case "DODGE":
                    bonuses[0] = "DODGE";
                    break;
                case "REFLECTION":
                    bonuses[1] = "REFLECTION";
                    break;
                case "HP REGEN":
                    bonuses[2] = "HP REGEN";
                    break;
                case "BLOCK":
                    bonuses[3] = "BLOCK";
                    break;
                // WEAPON PREFIXES
                case "PURE DMG":
                    bonuses[4] = "PURE DMG";
                    break;
                case "ACCURACY":
                    bonuses[5] = "ACCURACY";
                    break;
                case "KNOCKBACK":
                    bonuses[6] = "KNOCKBACK";
                    break;
                case "SLOW":
                    bonuses[7] = "SLOW";
                    break;
                case "LIFE STEAL":
                    bonuses[8] = "LIFE STEAL";
                    break;
                case "CRITICAL HIT":
                    bonuses[9] = "CRITICAL HIT";
                    break;
                case "ARMOR PENETRATION":
                    bonuses[10] = "ARMOR PENETRATION";
                    break;
                // ARMOR SUFFIXES
                case "ENERGY REGEN":
                    bonuses[11] = "ENERGY REGEN";
                    break;
                case "FIRE RESISTANCE":
                    bonuses[12] = "FIRE RESISTANCE";
                    break;
                case "ICE RESISTANCE":
                    bonuses[13] = "ICE RESISTANCE";
                    break;
                case "POISON RESISTANCE":
                    bonuses[14] = "POISON RESISTANCE";
                    break;
                case "GEM FIND":
                    bonuses[15] = "GEM FIND";
                    break;
                case "ITEM FIND":
                    bonuses[16] = "ITEM FIND";
                    break;
                case "THORNS":
                    bonuses[17] = "THORNS";
                    break;
                // WEAPON SUFFIXES
                case "BLIND":
                    bonuses[18] = "BLIND";
                    break;
                case "vs. MONSTERS":
                    bonuses[19] = "vs. MONSTERS";
                    break;
                case "vs. PLAYERS":
                    bonuses[20] = "vs. PLAYERS";
                    break;
                case "FIRE DMG":
                    bonuses[21] = "FIRE DMG";
                    break;
                case "ICE DMG":
                    bonuses[22] = "ICE DMG";
                    break;
                case "POISON DMG":
                    bonuses[23] = "POISON DMG";
                    break;
                default:
                    break;
            }
        }

        for (String bonus : bonuses) {
            if (bonus == null) continue;
            // apply the prefixes/suffixes to item name
            switch (bonus) {
                // ARMOR PREFIXES
                case "DODGE":
                    name += "Agile ";
                    break;
                case "REFLECTION":
                    name += "Reflective ";
                    break;
                case "HP REGEN":
                    name += "Mending ";
                    break;
                case "BLOCK":
                    name += "Protective ";
                    break;
                // WEAPON PREFIXES
                case "PURE DMG":
                    name += "Pure ";
                    break;
                case "ACCURACY":
                    name += "Accurate ";
                    break;
                case "KNOCKBACK":
                    name += "Brute ";
                    break;
                case "SLOW":
                    name += "Snaring ";
                    break;
                case "LIFE STEAL":
                    name += "Vampyric ";
                    break;
                case "CRITICAL HIT":
                    name += "Deadly ";
                    break;
                case "ARMOR PENETRATION":
                    name += "Penetrating ";
                    break;
                // ARMOR SUFFIXES
                case "ENERGY REGEN":
                    name += (name.contains(type.getTierName(tier)) ? " of Fortitude" : type.getTierName(tier) + " of Fortitude");
                    break;
                case "FIRE RESISTANCE":
                    name += (name.contains(type.getTierName(tier)) ? " and Fire Resist" : type.getTierName(tier) + " of Fire Resist");
                    break;
                case "ICE RESISTANCE":
                    name += (name.contains(type.getTierName(tier)) ? " and Ice Resist" : type.getTierName(tier) + " of Ice Resist");
                    break;
                case "POISON RESISTANCE":
                    name += (name.contains(type.getTierName(tier)) ? " and Poison Resist" : type.getTierName(tier) + " of Poison Resist");
                    break;
                case "GEM FIND":
                    name += (name.contains(type.getTierName(tier)) ? " Golden" : type.getTierName(tier) + " of Pickpocketing");
                    break;
                case "ITEM FIND":
                    name += (name.contains(type.getTierName(tier)) ? " Treasure" : type.getTierName(tier) + " of Treasure");
                    break;
                case "THORNS":
                    name += (name.contains(type.getTierName(tier)) ? " Spikes" : type.getTierName(tier) + " of Thorns");
                    break;
                // WEAPON SUFFIXES
                case "BLIND":
                    name += type.getTierName(tier) + " of Blindness";
                    break;
                case "vs. MONSTERS":
                    name += (name.contains(type.getTierName(tier)) ? " Slaying" : type.getTierName(tier) + " of Slaying");
                    break;
                case "vs. PLAYERS":
                    name += (name.contains(type.getTierName(tier)) ? " Slaughter" : type.getTierName(tier) + " of Slaughter");
                    break;
                case "FIRE DMG":
                    name += (name.contains(type.getTierName(tier)) ? " Fire" : type.getTierName(tier) + " of Fire");
                    break;
                case "ICE DMG":
                    name += (name.contains(type.getTierName(tier)) ? " Ice" : type.getTierName(tier) + " of Ice");
                    break;
                case "POISON DMG":
                    name += (name.contains(type.getTierName(tier)) ? " Poison" : type.getTierName(tier) + " of Poison");
                    break;
                default:
                    break;
            }
        }

        // if no extra attributes, then make sure the item has the basic name
        if (!(name.contains(type.getTierName(tier)))) name += type.getTierName(tier);

        List<String> lore = meta.getLore();
        // add soulbound lore
        if (isReroll && isSoulbound && origItem.hasItemMeta() && origItem.getItemMeta().hasLore()) {
            for (String line : origItem.getItemMeta().getLore()) {
                if (line.contains(ChatColor.GRAY.toString())) {
                    lore.add(line);
                }
            }
        }

        // add the rarity tag
        lore.add(rarity.getName());

        // add soulbound, untradeable, puntradeable
        if (isSoulbound) {
            lore.add(ChatColor.DARK_RED.toString() + ChatColor.ITALIC + "Soulbound");
        } else if (isUntradeable) {
            lore.add(ChatColor.GRAY + "Untradeable");
        } else if (isPermanentlyUntradeable) {
            lore.add(ChatColor.GRAY + "Permanently Untradeable");
        }

        // add custom EC lore
        if (isReroll && origItem != null && origItem.hasItemMeta() && origItem.getItemMeta().hasLore()) {
            for (String line : origItem.getItemMeta().getLore()) {
                if (line.contains(ChatColor.GOLD.toString()) && line.contains(ChatColor.ITALIC.toString())) {
                    lore.add(line);
                }
            }
        }

        // retain soulbound name
        if (isReroll && isSoulbound) {
            name = origItem.getItemMeta().getDisplayName();
        }

        if (isReroll && EnchantmentAPI.isItemProtected(origItem))
            lore.add(ChatColor.GREEN.toString() + ChatColor.BOLD + "PROTECTED");

        // set the lore!
        meta.setLore(lore);

        if (isReroll && !isSoulbound) {
            int oldEnchantCount = EnchantmentAPI.getEnchantLvl(origItem);
            if (oldEnchantCount > 0) {
                name = ChatColor.RED + "[+" + oldEnchantCount + "] " + ChatColor.RESET + name;
            }
        }
        meta.setDisplayName(name);
        //TODO: Check if it has the E-Cash custom name when it's implemented.
        item.setItemMeta(meta);

        RepairAPI.setCustomItemDurability(item, 1500);

        // set NBT tags
        net.minecraft.server.v1_9_R2.ItemStack nmsStack = CraftItemStack.asNMSCopy(item);
        // NMS stack for writing NBT tags
        NBTTagCompound tag = nmsStack.getTag() == null ? new NBTTagCompound() : nmsStack.getTag();

        tag.set("itemType", new NBTTagInt(type.getId()));
        tag.set("itemRarity", new NBTTagInt(rarity.getId()));
        tag.set("soulbound", new NBTTagInt(isSoulbound ? 1 : 0));
        tag.set("untradeable", new NBTTagInt(isUntradeable ? 1 : 0));
        tag.set("puntradeable", new NBTTagInt(isPermanentlyUntradeable ? 1 : 0));
        tag.set("itemTier", new NBTTagInt(tier.getTierId()));
        if (isReroll && EnchantmentAPI.getEnchantLvl(origItem) > 0) {
            tag.set("enchant", new NBTTagInt(EnchantmentAPI.getEnchantLvl(origItem)));
        }

        if (type.getId() <= 4) {
            tag.set("type", new NBTTagString("weapon"));
        } else {
            tag.set("type", new NBTTagString("armor"));
        }
        
        /*
        The line below removes the weapons attributes.
        E.g. Diamond Sword says, "+7 Attack Damage"
         */
        tag.set("AttributeModifiers", new NBTTagList());

        NBTTagList modifiersList = new NBTTagList();
        if (isReroll) {
            List<String> modifiers = GameAPI.getModifiers(origItem);
            if (Item.ItemType.isWeapon(origItem)) {
                //Stats kept from weapon (Only Damage)
                for (String string : modifiers) {
                    if (string.contains("damage")) {
                        modifiersList.add(new NBTTagString(string));
                    }
                }
            } else if (Item.ItemType.isArmor(origItem)) {
                //Stats kept from armor (Armor/DPS/HP/EnergyRegen/HPRegen)
                modifiers.stream().filter(string -> string.contains("armor") || string.contains("dps") || string.contains("healthPoints") || string.contains("energyRegen") || string.contains("healthRegen")).forEach(string -> {
                    modifiersList.add(new NBTTagString(string));
                });
            }
        }

        for (Map.Entry<String, Integer> entry : NBTModifiers.entrySet()) {
            tag.set(entry.getKey(), new NBTTagInt(entry.getValue()));

            if (!entry.getKey().contains("Max")) {
                if (entry.getKey().contains("Min")) {
                    modifiersList.add(new NBTTagString(entry.getKey().replace("Min", "")));
                    continue;
                }
                modifiersList.add(new NBTTagString(entry.getKey()));
            }
        }

        tag.set("modifiers", modifiersList);

        nmsStack.setTag(tag);

        // apply antidupe
        this.item = AntiDuplication.getInstance().applyAntiDupe(CraftItemStack.asBukkitCopy(nmsStack));

        return this;
    }

    public ItemStack getItem() {
        return item;
    }

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
                    if (Item.ItemType.isWeapon(is)) {
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

                    } else if (Item.ItemType.isArmor(is)) {
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

        tag.set("itemType", new NBTTagInt(Item.ItemType.getTypeFromMaterial(is.getType()).getId()));
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
        if (Item.ItemType.isWeapon(is)) {
            tag.set("type", new NBTTagString("weapon"));
        } else if (Item.ItemType.isArmor(is)) {
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

    /**
     * Generates a helmet, chestplate, leggings, and boots of the specified
     * tier and rarity (set normally via the instance variables).
     *
     * @return - An ItemStack array of the armor set.
     */
    public ItemStack[] getArmorSet() {
        return new ItemStack[]{this.setType(Item.ItemType.BOOTS).setRarity(rarity == null ? GameAPI.getItemRarity(false) : rarity).generateItem().getItem(),
                this.setType(Item.ItemType.LEGGINGS).setRarity(rarity == null ? GameAPI.getItemRarity(false) : rarity).generateItem().getItem(),
                this.setType(Item.ItemType.CHESTPLATE).setRarity(rarity == null ? GameAPI.getItemRarity(false) : rarity).generateItem().getItem(),
                this.setType(Item.ItemType.HELMET).setRarity(rarity == null ? GameAPI.getItemRarity(false) : rarity).generateItem().getItem()};
    }

    public static void loadModifiers() {
        WeaponModifiers wm = new WeaponModifiers();
        wm.new Accuracy();
        wm.new ArmorPenetration();
        wm.new Blind();
        wm.new Critical();
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
        //	/* disabled as of patch 1.9 by Mayley's request
        //wm.new StrDexVitInt();
        wm.new SwordDamage();
        wm.new Versus();

        ArmorModifiers am = new ArmorModifiers();
        am.new Block();
        am.new Dodge();
        am.new EnergyRegen();
        am.new GemFind();
        //am.new HP();
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
