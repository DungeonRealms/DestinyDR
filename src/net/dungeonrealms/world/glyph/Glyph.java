package net.dungeonrealms.world.glyph;

import net.dungeonrealms.items.DamageMeta;
import net.dungeonrealms.items.Item;
import net.dungeonrealms.items.ItemGenerator;
import net.dungeonrealms.items.armor.Armor;
import net.dungeonrealms.items.armor.ArmorGenerator;
import net.dungeonrealms.items.enchanting.EnchantmentAPI;
import net.dungeonrealms.items.repairing.RepairAPI;
import net.dungeonrealms.miscellaneous.ItemBuilder;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import net.minecraft.server.v1_8_R3.NBTTagInt;
import net.minecraft.server.v1_8_R3.NBTTagString;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Created by Nick on 11/8/2015.
 */
public class Glyph {

    static Glyph instance = null;

    public static Glyph getInstance() {
        if (instance == null) {
            instance = new Glyph();
        }
        return instance;
    }

    /*
    Glyph Anatomy

    glyphTier: (int)1,2,3,4,5
    glyphType: "weapon" or "armor"
    glyphAttributes: "attribute@value,attribute@value,attribute@value,"

     */

    public enum GlyphType {
        WEAPON("weapon"),
        ARMOR("armor"),;

        private String name;

        GlyphType(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public static GlyphType getByString(String name) {
            for (GlyphType gt : values()) {
                if (gt.getName().equals(name)) {
                    return gt;
                }
            }
            return null;
        }
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

    public ItemStack getBaseGlyph(String name, int tier, GlyphType glyphType) {
        ItemStack glyph = new ItemStack(381);

        net.minecraft.server.v1_8_R3.ItemStack nmsStack = CraftItemStack.asNMSCopy(glyph);
        NBTTagCompound tag = nmsStack.getTag() == null ? new NBTTagCompound() : nmsStack.getTag();

        tag.set("glyphTier", new NBTTagInt(tier));
        tag.set("glyphType", new NBTTagString(glyphType.getName()));


        int randomizedTier = 0;

        switch (tier) {
            case 1:
                randomizedTier = 1;
                break;
            case 2:
                randomizedTier = (new Random().nextInt(2) + 1);
                break;
            case 3:
                randomizedTier = (new Random().nextInt(3) + 1);
                break;
            case 4:
                randomizedTier = (new Random().nextInt(4) + 1);
                break;
            case 5:
                randomizedTier = (new Random().nextInt(4) + 1);
                break;
            default:
                randomizedTier = (new Random().nextInt(4) + 1);
        }

        List<String> lore = new ArrayList<>();

        switch (glyphType) {
            case ARMOR:
                StringBuilder armorAttribute = new StringBuilder();
                for (Armor.ArmorAttributeType type : getGlyphArmorAttributes(randomizedTier)) {
                    //Get the value
                    int value = new DamageMeta().nextArmor(Armor.ArmorTier.getByTier(randomizedTier), Armor.ArmorModifier.getById((randomizedTier - 1)), Armor.ArmorAttributeType.getByString(type.getNBTName()));
                    armorAttribute.append(type.getNBTName()).append("@").append(value).append(",");

                    //Make sure percentages aren't over 10
                    if (Arrays.asList(new String[]{
                            "armor",
                            "energyRegen",
                            "block",
                            "luck",
                            "thorns",
                            "dodge",
                            "damage"
                    }).contains(type.getNBTName())) {
                        if (value > 10) {
                            value = 8;
                        }
                    }

                    lore.add("  • " + ChatColor.RED + String.valueOf(value) + " " + ChatColor.RESET + type.getName());
                }
                tag.set("glyphAttributes", new NBTTagString(armorAttribute.toString()));
                break;
            case WEAPON:
                StringBuilder weaponAttribute = new StringBuilder();
                for (Item.AttributeType weaponAttributeType : getGlyphWeaponAttributes(randomizedTier)) {
                    if (((Item.AttributeType) weaponAttributeType) == null) continue;
                    Item.AttributeType type = ((Item.AttributeType) weaponAttributeType);
                    //Get the value
                    int value = new DamageMeta().nextWeapon(Item.ItemTier.getByTier(randomizedTier), Item.ItemModifier.getById((randomizedTier - 1)), Item.AttributeType.getByString(type.getNBTName()));
                    weaponAttribute.append(type.getNBTName()).append("@").append(value).append(",");

                    //Make sure percentages aren't over 10
                    if (Arrays.asList(new String[]{
                            "criticalHit",
                            "vsMonsters",
                            "vsPlayers",
                            "lifesteal",
                            "accuracy"
                    }).contains(type.getNBTName())) {
                        if (value > 10) {
                            value = 8;
                        }
                    }

                    lore.add("  • " + ChatColor.RED + String.valueOf(value) + " " + ChatColor.RESET + type.getName());
                }
                tag.set("glyphAttributes", new NBTTagString(weaponAttribute.toString()));
                break;
        }

        nmsStack.setTag(tag);

        ItemStack item = CraftItemStack.asBukkitCopy(nmsStack);
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(name);

        lore.add(ChatColor.BLUE + "Caution -");
        lore.add(ChatColor.RED + "  • " + ChatColor.GOLD + "Binds when equip");
        lore.add(ChatColor.RED + "  • " + ChatColor.GOLD + "Can only be applied to " + glyphType.getName());

        meta.setLore(lore);
        item.setItemMeta(meta);

        return item;
    }

    ArrayList<Item.AttributeType> getGlyphWeaponAttributes(int amountOfAttributes) {
        ArrayList<Item.AttributeType> _tempWeapon = new ArrayList<>();
        for (int i = 0; i < amountOfAttributes; i++) {
            Item.AttributeType attributeType = ItemGenerator.getRandomItemAttribute();
            if (!_tempWeapon.contains(attributeType)) {
                _tempWeapon.add(attributeType);
            } else {
                --i;
            }
        }
        return _tempWeapon;
    }

    ArrayList<Armor.ArmorAttributeType> getGlyphArmorAttributes(int amountOfAttributes) {
        ArrayList<Armor.ArmorAttributeType> _tempArmor = new ArrayList<>();
        for (int i = 0; i < amountOfAttributes; i++) {
            Armor.ArmorAttributeType attributeType = ArmorGenerator.getRandomItemAttribute();
            if (!_tempArmor.contains(attributeType)) {
                _tempArmor.add(attributeType);
            } else {
                --i;
            }
        }
        return _tempArmor;
    }

    public boolean isGlyph(ItemStack item) {
        net.minecraft.server.v1_8_R3.ItemStack nmsStack = CraftItemStack.asNMSCopy(item);
        return nmsStack.hasTag() && nmsStack.getTag().hasKey("glyphTier");
    }

    public int getGlyphTier(ItemStack item) {
        if (isGlyph(item)) {
            return CraftItemStack.asNMSCopy(item).getTag().getInt("glyphTier");
        }
        return 1;
    }

    /*
    Glyph Anatomy

    glyphTier: (int)1,2,3,4,5
    glyphType: "weapon" or "armor"
    glyphAttributes: "attribute@value,attribute@value,attribute@value,"

     */

    public void applyGlyph(Player player, InventoryClickEvent event, ItemStack glyph, ItemStack item) {
        if (!isGlyph(glyph)) return;

        event.setCancelled(true);

        net.minecraft.server.v1_8_R3.ItemStack nmsStack = CraftItemStack.asNMSCopy(item);

        NBTTagCompound glyphTag = CraftItemStack.asNMSCopy(glyph).getTag();
        NBTTagCompound itemTag = nmsStack.getTag();

        GlyphType type = GlyphType.getByString(glyphTag.getString("glyphType"));
        int tier = glyphTag.getInt("glyphTier");
        int itemTier = RepairAPI.getArmorOrWeaponTier(item);

        if (tier > itemTier) {
            player.sendMessage(ChatColor.RED + "Your armor/weapon tier appears to be higher than the tier..");
            return;
        }

        if (Boolean.valueOf(itemTag.getString("bound"))) {
            player.sendMessage(ChatColor.RED + "This weapon is already bound!");
            return;
        }


        String[] attributeList = glyphTag.getString("glyphAttributes").split(",");


        if (EnchantmentAPI.isItemWeapon(item)) {
            /*
            Item is Weapon.
             */
            if (type == GlyphType.ARMOR) {
                player.sendMessage(ChatColor.RED + "This Glyph isn't for weapons!");
                return;
            }

            event.setCursor(new ItemStack(Material.AIR));

            for (String s : attributeList) {
                String attribute = s.split("@")[0];
                int value = Integer.valueOf(s.split("@")[1]);

                if (itemTag.hasKey(attribute)) {
                    itemTag.setInt(attribute, itemTag.getInt(attribute) + value);
                } else {
                    itemTag.setInt(attribute, value);
                }
            }
        } else {
            /*
            Item is Armor
             */
            if (type == GlyphType.WEAPON) {
                player.sendMessage(ChatColor.RED + "This Glyph isn't for armor!");
                return;
            }

            event.setCursor(new ItemStack(Material.AIR));

            for (String s : attributeList) {
                String attribute = s.split("@")[0];
                int value = Integer.valueOf(s.split("@")[1]);

                if (itemTag.hasKey(attribute)) {
                    itemTag.setInt(attribute, itemTag.getInt(attribute) + value);
                } else {
                    itemTag.setInt(attribute, value);
                }
            }
        }

        itemTag.set("bound", new NBTTagString("true"));
        nmsStack.setTag(itemTag);

        ItemStack stack = CraftItemStack.asBukkitCopy(nmsStack);
        ItemMeta meta = stack.getItemMeta();
        List<String> lore = meta.getLore();

        lore.add(" ");
        lore.add(ChatColor.BLUE + "Glyph Attributes -");

        for (String s : attributeList) {
            String attribute = s.split("@")[0];
            int value = Integer.valueOf(s.split("@")[1]);
            String output = attribute.substring(0, 1).toUpperCase() + attribute.substring(1);
            lore.add(ChatColor.GREEN + "  • " + ChatColor.RED + value + " " + ChatColor.GOLD + output);
        }

        meta.setLore(lore);
        stack.setItemMeta(meta);

        event.setCurrentItem(new ItemStack(Material.AIR));

        player.getInventory().addItem(stack);
    }

}
