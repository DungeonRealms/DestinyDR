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
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
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
        return getWeaponGlyph("Glyph of Burick", ((new Random().nextInt(4)) + 1));
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

    public ItemStack nextStarGlyph() {
        return new ItemBuilder().setItem(new ItemStack(381), ChatColor.GREEN + "Star Unbinding Glyph", new String[]{
                ChatColor.GRAY + "Left click while holding to",
                ChatColor.GRAY + "unbind the current glyph!"
        }).setNBTString("star", "true").build();
    }

    public ItemStack nextBlankGlyph(int tier) {
        /*
        t1 white x
        t2 green x
        t3 aqua x
        t4 light_purple x
        t5 yellow
         */

        switch (tier) {
            case 1:
                return new ItemBuilder().setItem(new ItemStack(381), ChatColor.WHITE + "Unknown Glyph", new String[]{
                }).setNBTString("glyph", "true").setNBTInt("tier", tier).build();
            case 2:
                return new ItemBuilder().setItem(new ItemStack(381), ChatColor.GREEN + "Unknown Glyph", new String[]{
                }).setNBTString("glyph", "true").setNBTInt("tier", tier).build();
            case 3:
                return new ItemBuilder().setItem(new ItemStack(381), ChatColor.AQUA + "Unknown Glyph", new String[]{
                }).setNBTString("glyph", "true").setNBTInt("tier", tier).build();
            case 4:
                return new ItemBuilder().setItem(new ItemStack(381), ChatColor.LIGHT_PURPLE + "Unknown Glyph", new String[]{
                }).setNBTString("glyph", "true").setNBTInt("tier", tier).build();
            case 5:
                return new ItemBuilder().setItem(new ItemStack(381), ChatColor.YELLOW + "Unknown Glyph", new String[]{
                }).setNBTString("glyph", "true").setNBTInt("tier", tier).build();
            default:
                return null;
        }
    }

    public void starGlyph(InventoryClickEvent event, Player player, ItemStack item, ItemStack star) {
        if (!isStar(star)) return;

        event.setCancelled(true);

        event.setCursor(new ItemStack(Material.AIR));

        net.minecraft.server.v1_8_R3.ItemStack nmsStack = CraftItemStack.asNMSCopy(item);

        NBTTagCompound itemTag = nmsStack.getTag();

        String glyphAttributes = itemTag.getString("glyphAttributes");

        String[] rawAttributeList = glyphAttributes.split(",");


        for (String ra : rawAttributeList) {
            if (ra.split("@")[0] == null) continue;
            if (ra.split("@")[1] == null) continue;
            String NBTName = ra.split("@")[0];
            Integer NBTValue = Integer.valueOf(ra.split("@")[1]);
            System.out.println("Removed: " + NBTName + " with " + NBTValue);
            itemTag.set(NBTName, new NBTTagInt(itemTag.getInt(NBTName) - NBTValue));
        }

        nmsStack.setTag(itemTag);

        ItemStack bukkitItem = CraftItemStack.asBukkitCopy(nmsStack);
        ItemMeta meta = bukkitItem.getItemMeta();

        // Glyph •


        //TODO: Xwaffle please fix this, make everything under ALSO make sure to remove the glyph after its done.

        bukkitItem.setItemMeta(meta);

        player.getInventory().addItem(bukkitItem);

    }

    public void applyGlyph(InventoryClickEvent event, Player player, ItemStack scroll, ItemStack item) {
        if (!isGlyph(scroll)) return;

        event.setCancelled(true);
        event.setCursor(new ItemStack(Material.AIR));

        NBTTagCompound itemTag = CraftItemStack.asNMSCopy(item).getTag();
        NBTTagCompound scrollTag = CraftItemStack.asNMSCopy(scroll).getTag();

        int scrollLevel = scrollTag.getInt("tier");

        /**
         * APPLY GLYPH TO WEAPON! -----------------------------------------------------------------------------
         */

        if (itemTag.hasKey("itemTier")) {

            int itemLevel = RepairAPI.getArmorOrWeaponTier(item);

            if (itemLevel <= scrollLevel) {
                player.sendMessage(ChatColor.RED + "Your scroll level is lower than your item level.");
                player.playSound(player.getLocation(), Sound.ANVIL_BREAK, 1f, 63f);
                return;
            }

            if (Boolean.valueOf(itemTag.getString("bound"))) {
                player.sendMessage(ChatColor.RED + "This weapon is already bound! You must acquire a unbinding star before applying another scroll!");
                player.playSound(player.getLocation(), Sound.ANVIL_BREAK, 1f, 63f);
                return;
            }

            String[] rawAttributeList = scrollTag.getString("attributes").split(",");

            for (String ra : rawAttributeList) {
                if (!itemTag.hasKey(ra.split("@")[0])) {
                    player.sendMessage(ChatColor.RED + "Attribute mismatch, your item appears to be missing some attributes that the scroll offers! You cannot scroll this item!");
                    player.playSound(player.getLocation(), Sound.ANVIL_BREAK, 1f, 63f);
                    return;
                }
            }

            event.setCursor(new ItemStack(Material.AIR));

            player.getInventory().remove(item);

            for (String ra : rawAttributeList) {
                String NBTName = ra.split("@")[0];
                Integer NBTValue = Integer.valueOf(ra.split("@")[1]);

                System.out.println(itemTag.getInt(NBTName));
                System.out.println(itemTag.getInt(NBTName) + NBTValue);

                itemTag.set(NBTName, new NBTTagInt(itemTag.getInt(NBTName) + NBTValue));
            }

            itemTag.set("glyphAttributes", new NBTTagString(scrollTag.getString("attributes")));

            itemTag.set("bound", new NBTTagString("true"));

            net.minecraft.server.v1_8_R3.ItemStack nmsStack = CraftItemStack.asNMSCopy(item);
            nmsStack.setTag(itemTag);
            ItemStack armorItem = CraftItemStack.asBukkitCopy(nmsStack);
            ItemMeta meta = armorItem.getItemMeta();
            List<String> lore = meta.getLore();
            lore.add(" ");
            lore.add(ChatColor.BLUE + "Glyph Attributes -");

            String glyphAttributes = "";

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
                        lore.add(ChatColor.GREEN + "  • " + ChatColor.RED + NBTValue + "%" + " " + ChatColor.GOLD + Item.AttributeType.getByString(NBTName).getName());
                    }
                } else {
                    lore.add(ChatColor.GREEN + "  • " + ChatColor.RED + NBTValue + " " + ChatColor.GOLD + Item.AttributeType.getByString(NBTName).getName());
                }

                glyphAttributes += NBTName + "@" + NBTValue + ",";
                itemTag.set(NBTName, new NBTTagInt(itemTag.getInt(NBTName) + NBTValue));

            }

            itemTag.set("glyph", new NBTTagString(glyphAttributes));

            meta.setLore(lore);
            armorItem.setItemMeta(meta);

            player.getInventory().addItem(armorItem);

        }

        /**
         * APPLY GLYPH TO ARMOR! -----------------------------------------------------------------------------
         */

        else if (itemTag.hasKey("armorTier")) {

            int armorLevel = RepairAPI.getArmorOrWeaponTier(item);

            if (Boolean.valueOf(itemTag.getString("bound"))) {
                player.sendMessage(ChatColor.RED + "This armor is already bound! You must apply an unbinding star to remove it's current glyph!");
                player.playSound(player.getLocation(), Sound.ANVIL_BREAK, 1f, 63f);
                return;
            }

            if (armorLevel <= scrollLevel) {
                player.sendMessage(ChatColor.RED + "Tier mismatch, your scroll tier is lower than your armor tier!");
                player.playSound(player.getLocation(), Sound.ANVIL_BREAK, 1f, 63f);
                return;
            }

            String[] rawAttributeList = scrollTag.getString("attributes").split(",");

            for (String ra : rawAttributeList) {
                if (!itemTag.hasKey(ra.split("@")[0])) {
                    player.sendMessage(ChatColor.RED + "AttributeMismatchException! Your glyph and the armor set contain different attributes!");
                    player.playSound(player.getLocation(), Sound.ANVIL_BREAK, 1f, 63f);
                    return;
                }
            }

            player.getInventory().remove(item);
            event.setCursor(new ItemStack(Material.AIR));

            itemTag.set("glyphAttributes", new NBTTagString(scrollTag.getString("attributes")));

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
                        lore.add(ChatColor.GREEN + "  • " + ChatColor.RED + NBTValue + "%" + " " + ChatColor.GOLD + Armor.ArmorAttributeType.getByString(NBTName).getName());
                    }
                } else {
                    lore.add(ChatColor.GREEN + "  • " + ChatColor.RED + NBTValue + " " + ChatColor.GOLD + Armor.ArmorAttributeType.getByString(NBTName).getName());
                }
                itemTag.set(NBTName, new NBTTagInt(itemTag.getInt(NBTName) + NBTValue));
            }

            meta.setLore(lore);
            armorItem.setItemMeta(meta);

            player.getInventory().addItem(armorItem);

        } else {
            player.sendMessage(ChatColor.RED + "Error: Something weird happen! Don't report this because it wont be looked at!");
        }


    }

    public boolean isGlyph(ItemStack item) {
        net.minecraft.server.v1_8_R3.ItemStack nmsGlyph = CraftItemStack.asNMSCopy(item);
        return nmsGlyph.getTag() != null && nmsGlyph.hasTag() && nmsGlyph.getTag().hasKey("glyph");
    }

    public boolean isStar(ItemStack item) {
        net.minecraft.server.v1_8_R3.ItemStack nmsStar = CraftItemStack.asNMSCopy(item);
        return nmsStar.getTag() != null && nmsStar.hasTag() && nmsStar.getTag().hasKey("star");
    }

    public int getGlyphTier(ItemStack item) {
        net.minecraft.server.v1_8_R3.ItemStack nmsGlyph = CraftItemStack.asNMSCopy(item);
        if (nmsGlyph.getTag() == null) {
            return 0;
        }
        if (!(nmsGlyph.getTag().hasKey("tier"))) {
            return 0;
        }
        return nmsGlyph.getTag().getInt("tier");
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
                attributes = getArmorGlyphAttributes(tier, (new Random().nextInt(3) + 1));
                meta.setDisplayName(ChatColor.YELLOW + name);
                break;
            default:
                attributes = getArmorGlyphAttributes(tier, (new Random().nextInt(3) + 1));
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
                attributes = getWeaponGlyphAttributes(tier, (new Random().nextInt(3) + 1));
                meta.setDisplayName(ChatColor.YELLOW + name);
                break;
            default:
                attributes = getWeaponGlyphAttributes(tier, (new Random().nextInt(3) + 1));
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


        Armor.ArmorTier armorTier = Armor.ArmorTier.getByTier(tier);
        Armor.ArmorModifier armorModifier = Armor.ArmorModifier.getById(tier - 1);

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


        Item.ItemTier itemTier = Item.ItemTier.getByTier(tier - 1);
        Item.ItemModifier itemModifier = Item.ItemModifier.getById(tier - 1);

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
