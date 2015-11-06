package net.dungeonrealms.miscellaneous;

import net.dungeonrealms.anticheat.AntiCheat;
import net.dungeonrealms.items.DamageMeta;
import net.dungeonrealms.items.Item;
import net.dungeonrealms.items.ItemGenerator;
import net.dungeonrealms.items.armor.Armor;
import net.dungeonrealms.items.armor.ArmorGenerator;
import net.dungeonrealms.items.repairing.RepairAPI;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Nick on 11/4/2015.
 */
public class Glyph {

    static Glyph instance = null;

    public static Glyph getInstance() {
        if (instance == null) {
            instance = new Glyph();
        }
        return instance;
    }

    /**
     * @return
     * @apiNote returns a random scroll.
     */
    public ItemStack nextWeaponGlyph() {
        return getWeaponGlyph("Glyph of Burick", ((new Random().nextInt(5)) + 1));
    }

    /**
     * @return
     */
    public ItemStack nextArmorGlyph() {
        return getWeaponGlyph("Glyph of Burick", ((new Random().nextInt(5)) + 1));
    }

    public ItemStack nextArmorGlyph(String name, int tier) {
        return getWeaponGlyph(name, tier);
    }

    public ItemStack nextWeaponGlyph(String name, int tier) {
        return getWeaponGlyph(name, tier);
    }

    public void applyGlyph(Player player, ItemStack scroll, ItemStack item) {
        if (!isGlyph(scroll)) return;

        NBTTagCompound itemTag = CraftItemStack.asNMSCopy(item).getTag();
        NBTTagCompound scrollTag = CraftItemStack.asNMSCopy(scroll).getTag();

        int scrollLevel = scrollTag.getInt("tier");

        if (itemTag.hasKey("itemTier")) {

            int itemLevel = RepairAPI.getArmorOrWeaponTier(item);

            if (itemLevel != scrollLevel) {
                player.sendMessage(ChatColor.RED + "Tier mismatch! You cannot apply a tier: " + scrollLevel + " scroll to a tier: " + itemLevel + " item!");
                return;
            }

            if (Boolean.valueOf(itemTag.getString("bound"))) {
                player.sendMessage(ChatColor.RED + "This weapon is already bound! You must acquire a unbinding scroll before applying another scroll!");
                return;
            }

            String[] rawAttributeList = scrollTag.getString("attributes").split(",");

            for (String ra : rawAttributeList) {
                if (!itemTag.hasKey(ra.split("@")[0])) {
                    player.sendMessage(ChatColor.RED + "Attribute mismatch, your item appears to be missing some attributes that the scroll offers! You cannot scroll this item!");
                    return;
                }
            }

            player.getInventory().remove(item);
            player.getInventory().remove(scroll);

            for (String ra : rawAttributeList) {
                String NBTName = ra.split("@")[0];
                Integer NBTValue = Integer.valueOf(ra.split("@")[1]);

                System.out.println(itemTag.getInt(NBTName));
                System.out.println(itemTag.getInt(NBTName) + NBTValue);

                itemTag.set(NBTName, new NBTTagInt(itemTag.getInt(NBTName) + NBTValue));
            }

            itemTag.set("bound", new NBTTagString("true"));

            net.minecraft.server.v1_8_R3.ItemStack nmsStack = CraftItemStack.asNMSCopy(item);
            nmsStack.setTag(itemTag);
            ItemStack armorItem = CraftItemStack.asBukkitCopy(nmsStack);
            ItemMeta meta = armorItem.getItemMeta();
            List<String> lore = meta.getLore();
            lore.add(" ");
            lore.add(ChatColor.BLUE + "Glyph Attributes -");

            for (String ra : rawAttributeList) {
                String NBTName = ra.split("@")[0];
                Integer NBTValue = Integer.valueOf(ra.split("@")[1]);

                if (Arrays.asList(new String[]{
                        "criticalHit",
                        "vsMonsters",
                        "vsPlayers",
                        "lifesteal",
                        "accuracy"
                }).contains(NBTValue)) {
                    if (NBTValue > 10) {
                        NBTValue = 13;
                    }
                }

                itemTag.set(NBTName, new NBTTagInt(itemTag.getInt(NBTName) + NBTValue));
                lore.add(ChatColor.GREEN + "  • " + ChatColor.RED + NBTValue + " " + ChatColor.GOLD + Item.AttributeType.getByString(NBTName).getName());
            }

            meta.setLore(lore);
            armorItem.setItemMeta(meta);

            player.getInventory().addItem(armorItem);

        } else if (itemTag.hasKey("armorTier")) {

            int armorLevel = RepairAPI.getArmorOrWeaponTier(item);

            if (Boolean.valueOf(itemTag.getString("bound"))) {
                player.sendMessage(ChatColor.RED + "You cannot bind two glyphs to a singular armor piece.");
                return;
            }

            if (armorLevel != scrollLevel) {
                player.sendMessage(ChatColor.RED + "Tier mismatch! You cannot apply a tier: " + scrollLevel + " glyph to a tier: " + armorLevel + " armor!");
                return;
            }

            String[] rawAttributeList = scrollTag.getString("attributes").split(",");

            for (String ra : rawAttributeList) {
                if (!itemTag.hasKey(ra.split("@")[0])) {
                    player.sendMessage(ChatColor.RED + "AttributeMismatchException! Your glyph and the armor set contain different attributes!");
                    return;
                }
            }

            player.getInventory().remove(item);
            player.getInventory().remove(scroll);

            itemTag.set("bound", new NBTTagString("true"));

            net.minecraft.server.v1_8_R3.ItemStack nmsStack = CraftItemStack.asNMSCopy(item);
            nmsStack.setTag(itemTag);
            ItemStack armorItem = CraftItemStack.asBukkitCopy(nmsStack);
            ItemMeta meta = armorItem.getItemMeta();
            List<String> lore = meta.getLore();
            lore.add(" ");
            lore.add(ChatColor.BLUE + "Glyph Attributes -");

            for (String ra : rawAttributeList) {
                String NBTName = ra.split("@")[0];
                Integer NBTValue = Integer.valueOf(ra.split("@")[1]);

                if (Arrays.asList(new String[]{
                        "armor",
                        "energyRegen",
                        "block",
                        "luck",
                        "thorns",
                        "dodge",
                        "damage"
                }).contains(NBTValue)) {
                    if (NBTValue > 10) {
                        NBTValue = 13;
                    }
                }

                itemTag.set(NBTName, new NBTTagInt(itemTag.getInt(NBTName) + NBTValue));
                lore.add(ChatColor.GREEN + "  • " + ChatColor.RED + NBTValue + " " + ChatColor.GOLD + Armor.ArmorAttributeType.getByString(NBTName).getName());
            }

            meta.setLore(lore);
            armorItem.setItemMeta(meta);

            player.getInventory().addItem(armorItem);

        } else {
            player.sendMessage(ChatColor.RED + "Error: Something weird happen! Don't report this because it wont be looked at!");
        }


    }

    boolean isGlyph(ItemStack item) {
        net.minecraft.server.v1_8_R3.ItemStack nmsGlyph = CraftItemStack.asNMSCopy(item);
        return nmsGlyph.hasTag() && nmsGlyph.getTag().hasKey("glyph");
    }

    public ItemStack getArmorGylph(String name, int tier) {

        ItemStack stack = new ItemStack(381);
        ItemMeta meta = stack.getItemMeta();

        /*
        t1 white x
        t2 green x
        t3 aqua x
        t4 light_purple x
        t5 yellow
         */

        List<String> lore = new ArrayList<>();

        Map<Armor.ArmorAttributeType, Integer> attributes = null;

        switch (tier) {
            case 1:
                attributes = getArmorGlyphAttributes(tier, 1);
                meta.setDisplayName(ChatColor.WHITE + name);
                break;
            case 2:
                attributes = getArmorGlyphAttributes(tier, 1);
                meta.setDisplayName(ChatColor.GREEN + name);
                break;
            case 3:
                attributes = getArmorGlyphAttributes(tier, (new Random().nextInt(2) + 1));
                meta.setDisplayName(ChatColor.AQUA + name);
                break;
            case 4:
                attributes = getArmorGlyphAttributes(tier, (new Random().nextInt(3) + 1));
                meta.setDisplayName(ChatColor.LIGHT_PURPLE + name);
                break;
            case 5:
                attributes = getArmorGlyphAttributes(tier, (new Random().nextInt(4) + 1));
                meta.setDisplayName(ChatColor.YELLOW + name);
                break;
            default:
                attributes = getArmorGlyphAttributes(tier, (new Random().nextInt(4) + 1));
                meta.setDisplayName(ChatColor.RED + "ERROR " + name);
        }

        lore.add(ChatColor.BLUE + "Attributes -");
        lore.addAll(attributes.entrySet().stream().map(entry -> ChatColor.AQUA + "  •" + ChatColor.GREEN + " Improves " + ChatColor.GOLD + entry.getKey().getName() + " " + ChatColor.GREEN + "by " + ChatColor.RED + entry.getValue()).collect(Collectors.toList()));


        lore.add(ChatColor.BLUE + "Caution -");
        lore.add(ChatColor.RED + "  •" + ChatColor.YELLOW + " Binds on equip");
        lore.add(ChatColor.RED + "  •" + ChatColor.YELLOW + " Can only be applied to a armor.");

        meta.setLore(lore);
        stack.setItemMeta(meta);

        net.minecraft.server.v1_8_R3.ItemStack nmsStack = CraftItemStack.asNMSCopy(stack);

        NBTTagCompound tag = nmsStack.getTag() == null ? new NBTTagCompound() : nmsStack.getTag();

        tag.set("AttributeModifiers", new NBTTagList());
        tag.set("glyph", new NBTTagByte((byte) 1));

        StringBuilder attributeList = new StringBuilder();

        for (Map.Entry<Armor.ArmorAttributeType, Integer> entry : attributes.entrySet()) {
            tag.set(entry.getKey().getNBTName(), new NBTTagInt(entry.getValue()));
            attributeList.append(entry.getKey().getNBTName()).append("@" + entry.getValue()).append(",");
        }

        tag.set("attributes", new NBTTagString(attributeList.toString()));
        tag.set("tier", new NBTTagInt(tier));

        nmsStack.setTag(tag);


        return AntiCheat.getInstance().applyAntiDupe(CraftItemStack.asBukkitCopy(nmsStack));
    }

    public ItemStack getWeaponGlyph(String name, int tier) {

        ItemStack stack = new ItemStack(381);
        ItemMeta meta = stack.getItemMeta();

        /*
        t1 white x
        t2 green x
        t3 aqua x
        t4 light_purple x
        t5 yellow
         */

        List<String> lore = new ArrayList<>();

        Map<Item.AttributeType, Integer> attributes = null;

        switch (tier) {
            case 1:
                attributes = getWeaponGlyphAttributes(tier, 1);
                meta.setDisplayName(ChatColor.WHITE + name);
                break;
            case 2:
                attributes = getWeaponGlyphAttributes(tier, 1);
                meta.setDisplayName(ChatColor.GREEN + name);
                break;
            case 3:
                attributes = getWeaponGlyphAttributes(tier, (new Random().nextInt(2) + 1));
                meta.setDisplayName(ChatColor.AQUA + name);
                break;
            case 4:
                attributes = getWeaponGlyphAttributes(tier, (new Random().nextInt(3) + 1));
                meta.setDisplayName(ChatColor.LIGHT_PURPLE + name);
                break;
            case 5:
                attributes = getWeaponGlyphAttributes(tier, (new Random().nextInt(4) + 1));
                meta.setDisplayName(ChatColor.YELLOW + name);
                break;
            default:
                attributes = getWeaponGlyphAttributes(tier, (new Random().nextInt(4) + 1));
                meta.setDisplayName(ChatColor.RED + "ERROR " + name);
        }

        lore.add(ChatColor.BLUE + "Attributes -");
        lore.addAll(attributes.entrySet().stream().map(entry -> ChatColor.AQUA + "  •" + ChatColor.GREEN + " Improves " + ChatColor.GOLD + entry.getKey().getName() + " " + ChatColor.GREEN + "by " + ChatColor.RED + entry.getValue()).collect(Collectors.toList()));


        lore.add(ChatColor.BLUE + "Caution -");
        lore.add(ChatColor.RED + "  •" + ChatColor.YELLOW + " Binds on equip");
        lore.add(ChatColor.RED + "  •" + ChatColor.YELLOW + " Can only be applied to a weapon.");

        meta.setLore(lore);
        stack.setItemMeta(meta);

        net.minecraft.server.v1_8_R3.ItemStack nmsStack = CraftItemStack.asNMSCopy(stack);

        NBTTagCompound tag = nmsStack.getTag() == null ? new NBTTagCompound() : nmsStack.getTag();

        tag.set("AttributeModifiers", new NBTTagList());
        tag.set("glyph", new NBTTagByte((byte) 1));

        StringBuilder attributeList = new StringBuilder();

        for (Map.Entry<Item.AttributeType, Integer> entry : attributes.entrySet()) {
            tag.set(entry.getKey().getNBTName(), new NBTTagInt(entry.getValue()));
            attributeList.append(entry.getKey().getNBTName()).append("@" + entry.getValue()).append(",");
        }

        tag.set("attributes", new NBTTagString(attributeList.toString()));
        tag.set("tier", new NBTTagInt(tier));

        nmsStack.setTag(tag);


        return AntiCheat.getInstance().applyAntiDupe(CraftItemStack.asBukkitCopy(nmsStack));
    }

    public Map<Armor.ArmorAttributeType, Integer> getArmorGlyphAttributes(int tier, int amount) {
        Map<Armor.ArmorAttributeType, Integer> _temp = new HashMap<>();


        Armor.ArmorTier armorTier = Armor.ArmorTier.getById(tier);
        Armor.ArmorModifier armorModifier = Armor.ArmorModifier.getById(tier);

        for (int i = 0; i < amount; i++) {
            Armor.ArmorAttributeType armorAttribute = ArmorGenerator.getRandomItemAttribute();

            int increase = new DamageMeta().nextArmor(armorTier, armorModifier, armorAttribute);
            if (!_temp.containsKey(armorAttribute)) {
                _temp.put(armorAttribute, increase);
            } else {
                i--;
            }
        }


        return _temp;
    }


    public Map<Item.AttributeType, Integer> getWeaponGlyphAttributes(int tier, int amount) {
        Map<Item.AttributeType, Integer> _temp = new HashMap<>();


        Item.ItemTier itemTier = Item.ItemTier.getById(tier);
        Item.ItemModifier itemModifier = Item.ItemModifier.getById(tier);

        for (int i = 0; i < amount; i++) {
            Item.AttributeType itemAttribute = ItemGenerator.getRandomItemAttribute();
            int increase = new DamageMeta().nextWeapon(itemTier, itemModifier, itemAttribute);
            if (!_temp.containsKey(itemAttribute)) {
                _temp.put(itemAttribute, increase);
            } else {
                i--;
            }
        }


        return _temp;
    }


}
