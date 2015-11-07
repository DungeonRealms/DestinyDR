package net.dungeonrealms.mechanics;

import net.dungeonrealms.API;
import net.dungeonrealms.anticheat.AntiCheat;
import net.dungeonrealms.handlers.HealthHandler;
import net.dungeonrealms.items.EnumItem;
import net.dungeonrealms.mastery.GamePlayer;
import net.dungeonrealms.miscellaneous.RandomHelper;
import net.dungeonrealms.mongo.DatabaseAPI;
import net.dungeonrealms.mongo.EnumData;
import net.dungeonrealms.profession.Fishing;
import net.dungeonrealms.profession.Mining;
import net.dungeonrealms.stats.PlayerStats;
import net.dungeonrealms.teleportation.TeleportAPI;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import net.minecraft.server.v1_8_R3.NBTTagList;
import net.minecraft.server.v1_8_R3.NBTTagString;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionType;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by Nick on 9/18/2015.
 */
public class ItemManager {
    /**
     * returns hearthstone
     *
     * @param name
     * @param lore
     * @return ItemStack
     * @since 1.0
     */
    public static ItemStack createHearthStone(String name, String[] lore) {
        ItemStack rawStack = new ItemStack(Material.QUARTZ);
        ItemMeta meta = rawStack.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(Arrays.asList(lore));
        rawStack.setItemMeta(meta);
        net.minecraft.server.v1_8_R3.ItemStack nmsStack = CraftItemStack.asNMSCopy(rawStack);
        NBTTagCompound tag = nmsStack.getTag() == null ? new NBTTagCompound() : nmsStack.getTag();
        tag.set("type", new NBTTagString("important"));
        tag.set("usage", new NBTTagString("hearthstone"));
        nmsStack.setTag(tag);
        return CraftItemStack.asBukkitCopy(nmsStack);
    }

    /**
     * Creates a random Teleport book
     *
     * @param name
     * @return ItemStack
     * @since 1.0
     */
    public static ItemStack createRandomTeleportBook(String name) {
        ItemStack rawStack = new ItemStack(Material.BOOK);
        ItemMeta meta = rawStack.getItemMeta();
        meta.setDisplayName(name);
        String teleportLocation = TeleportAPI.getRandomTeleportString();
        meta.setLore(Collections.singletonList(ChatColor.GRAY + "(Right-Click) Teleport to " + teleportLocation));
        rawStack.setItemMeta(meta);
        net.minecraft.server.v1_8_R3.ItemStack nmsStack = CraftItemStack.asNMSCopy(rawStack);
        NBTTagCompound tag = nmsStack.getTag() == null ? new NBTTagCompound() : nmsStack.getTag();
        tag.set("type", new NBTTagString("teleport"));
        tag.set("usage", new NBTTagString(teleportLocation));
        nmsStack.setTag(tag);
        return CraftItemStack.asBukkitCopy(nmsStack);
    }

    /**
     * Creates a scrap piece based on
     * given tier
     *
     * @param tier
     * @return ItemStack
     * @since 1.0
     */
    public static ItemStack createArmorScrap(int tier) {
        ItemStack rawStack = null;
        String name = "";
        switch (tier) {
            case 1:
                rawStack = new ItemStack(Material.LEATHER, 64);
                name = ChatColor.BOLD + "Leather";
                break;
            case 2:
                rawStack = new ItemStack(Material.IRON_FENCE, 64);
                name = ChatColor.GREEN.toString() + ChatColor.BOLD + "Chain";
                break;
            case 3:
                rawStack = new ItemStack(Material.IRON_INGOT, 64);
                name = ChatColor.AQUA.toString() + ChatColor.BOLD + "Iron";
                break;
            case 4:
                rawStack = new ItemStack(Material.DIAMOND, 64);
                name = ChatColor.LIGHT_PURPLE.toString() + ChatColor.BOLD + "Diamond";
                break;
            case 5:
                rawStack = new ItemStack(Material.GOLD_INGOT, 64);
                name = ChatColor.YELLOW.toString() + ChatColor.BOLD + "Gold";
                break;
            default:
                break;
        }
        if (rawStack != null) {
            ItemMeta meta = rawStack.getItemMeta();
            meta.setDisplayName(name + " Scrap");
            meta.setLore(Collections.singletonList(ChatColor.GRAY + "Repairs 3% durability on " + name + ChatColor.GRAY + " items."));
            rawStack.setItemMeta(meta);
            net.minecraft.server.v1_8_R3.ItemStack nmsStack = CraftItemStack.asNMSCopy(rawStack);
            NBTTagCompound tag = nmsStack.getTag() == null ? new NBTTagCompound() : nmsStack.getTag();
            tag.set("type", new NBTTagString("scrap"));
            tag.setInt("itemTier", tier);
            nmsStack.setTag(tag);
            return CraftItemStack.asBukkitCopy(nmsStack);
        }
        return null;
    }

    /**
     * Creates a potion based on the
     * given tier
     *
     * @param tier
     * @return ItemStack
     * @since 1.0
     */
    public static ItemStack createHealthPotion(int tier, boolean fromShop, boolean isSplashPotion) {
        String name = "";
        int healAmount = 0;
        switch (tier) {
            case 1:
                if (!isSplashPotion) {
                    name = ChatColor.WHITE + "Poor Elixir of Singular Healing";
                } else {
                    name = ChatColor.WHITE + "Poor Elixir of Splash Healing";
                }
                if (!fromShop) {
                    healAmount = RandomHelper.getRandomNumberBetween(30, 100);
                } else {
                    healAmount = 75;
                }
                break;
            case 2:
                if (!isSplashPotion) {
                    name = ChatColor.GREEN + "Inferior Elixir of Singular Healing";
                } else {
                    name = ChatColor.GREEN + "Inferior Elixir of Splash Healing";
                }
                if (!fromShop) {
                    healAmount = RandomHelper.getRandomNumberBetween(270, 375);
                } else {
                    healAmount = 325;
                }
                break;
            case 3:
                if (!isSplashPotion) {
                    name = ChatColor.AQUA + "Modest Elixir of Singular Healing";
                } else {
                    name = ChatColor.AQUA + "Modest Elixir of Splash Healing";
                }
                if (!fromShop) {
                    healAmount = RandomHelper.getRandomNumberBetween(800, 1000);
                } else {
                    healAmount = 900;
                }
                break;
            case 4:
                if (!isSplashPotion) {
                    name = ChatColor.LIGHT_PURPLE + "Superior Elixir of Singular Healing";
                } else {
                    name = ChatColor.LIGHT_PURPLE + "Superior Elixir of Splash Healing";
                }
                if (!fromShop) {
                    healAmount = RandomHelper.getRandomNumberBetween(2000, 2400);
                } else {
                    healAmount = 2250;
                }
                break;
            case 5:
                if (!isSplashPotion) {
                    name = ChatColor.YELLOW + "Legendary Elixir of Singular Healing";
                } else {
                    name = ChatColor.YELLOW + "Legendary Elixir of Splash Healing";
                }
                if (!fromShop) {
                    healAmount = RandomHelper.getRandomNumberBetween(3700, 4300);
                } else {
                    healAmount = 4000;
                }
                break;
            default:
                break;
        }
        if (!fromShop) {
            healAmount = (((healAmount + 5) / 10) * 10);
        }
        if (!isSplashPotion) {
            ItemStack rawStack = new ItemStack(Material.POTION, 1, (short) 5);
            PotionMeta potionMeta = (PotionMeta) rawStack.getItemMeta();
            potionMeta.setDisplayName(name);
            potionMeta.setLore(Collections.singletonList(ChatColor.GRAY + "An Elixir that heals for " + ChatColor.RED + ChatColor.BOLD + healAmount + ChatColor.GRAY + "HP."));
            rawStack.setItemMeta(potionMeta);
            net.minecraft.server.v1_8_R3.ItemStack nmsStack = CraftItemStack.asNMSCopy(rawStack);
            NBTTagCompound tag = nmsStack.getTag() == null ? new NBTTagCompound() : nmsStack.getTag();
            tag.set("type", new NBTTagString("healthPotion"));
            tag.setInt("itemTier", tier);
            tag.setInt("healAmount", healAmount);
            nmsStack.setTag(tag);
            return AntiCheat.getInstance().applyAntiDupe(CraftItemStack.asBukkitCopy(nmsStack));
        } else {
            Potion potion = new Potion(PotionType.INSTANT_HEAL, 1);
            potion.setSplash(true);
            ItemStack rawStack = potion.toItemStack(1);
            PotionMeta potionMeta = (PotionMeta) rawStack.getItemMeta();
            potionMeta.setDisplayName(name);
            potionMeta.setLore(Collections.singletonList(ChatColor.GRAY + "An Elixir that heals for " + ChatColor.RED + ChatColor.BOLD + healAmount + ChatColor.GRAY + "HP in a " + ChatColor.RED + ChatColor.BOLD + "4x4" + ChatColor.GRAY + " Area."));
            rawStack.setItemMeta(potionMeta);
            net.minecraft.server.v1_8_R3.ItemStack nmsStack = CraftItemStack.asNMSCopy(rawStack);
            NBTTagCompound tag = nmsStack.getTag() == null ? new NBTTagCompound() : nmsStack.getTag();
            tag.set("type", new NBTTagString("splashHealthPotion"));
            tag.setInt("itemTier", tier);
            tag.setInt("healAmount", healAmount);
            nmsStack.setTag(tag);
            return AntiCheat.getInstance().applyAntiDupe(CraftItemStack.asBukkitCopy(nmsStack));
        }
    }

    /**
     * Creates a pickaxe based on
     * given tier
     *
     * @param tier
     * @return ItemStack
     * @since 1.0
     */
    public static ItemStack createPickaxe(int tier) {
        ItemStack rawStack = null;
        String name = "";
        switch (tier) {
            case 1:
                rawStack = new ItemStack(Material.WOOD_PICKAXE);
                name = ChatColor.BOLD + "Weak Pick";
                break;
            case 2:
                rawStack = new ItemStack(Material.STONE_PICKAXE);
                name = ChatColor.GREEN.toString() + ChatColor.BOLD + "Basic Pick";
                break;
            case 3:
                rawStack = new ItemStack(Material.IRON_PICKAXE);
                name = ChatColor.AQUA.toString() + ChatColor.BOLD + "Intermediate Pick";
                break;
            case 4:
                rawStack = new ItemStack(Material.DIAMOND_PICKAXE);
                name = ChatColor.LIGHT_PURPLE.toString() + ChatColor.BOLD + "Strong Pick";
                break;
            case 5:
                rawStack = new ItemStack(Material.GOLD_PICKAXE);
                name = ChatColor.YELLOW.toString() + ChatColor.BOLD + "Master Pick";
                break;
            default:
                break;
        }
        if (rawStack != null) {
            ItemMeta meta = rawStack.getItemMeta();
            meta.setDisplayName(name);
            meta.setLore(Collections.singletonList(0 + "/" + Mining.getMaxXP(tier)));
            rawStack.setItemMeta(meta);
            net.minecraft.server.v1_8_R3.ItemStack nmsStack = CraftItemStack.asNMSCopy(rawStack);
            NBTTagCompound tag = nmsStack.getTag() == null ? new NBTTagCompound() : nmsStack.getTag();
            tag.set("type", new NBTTagString("pick"));
            tag.setInt("itemTier", tier);
            tag.setInt("XP", 0);
            tag.setInt("maxXP", Mining.getMaxXP(tier));
            tag.set("AttributeModifiers", new NBTTagList());
            nmsStack.setTag(tag);
            return AntiCheat.getInstance().applyAntiDupe(CraftItemStack.asBukkitCopy(nmsStack));
        }
        return null;
    }

    public static ItemStack createItem(EnumItem enumItem) {
        ItemStack stack = null;
        net.minecraft.server.v1_8_R3.ItemStack nms = null;
        switch (enumItem) {
            case StorageExpansion:
                stack = createItem(Material.TRAPPED_CHEST, ChatColor.GREEN + "Storage Expansion", new String[]{ChatColor.GRAY + "Increase storage space by 1 row." + ChatColor.RED + ChatColor.BOLD + "Max of 6"});
                nms = CraftItemStack.asNMSCopy(stack);
                nms.getTag().setString("type", "upgrade");
                break;
            case RepairHammer:
                stack = createItem(Material.ANVIL, ChatColor.GREEN + "Repair Hammer", new String[]{ChatColor.GRAY + "Increase storage space by 1 row." + ChatColor.RED + ChatColor.BOLD + "Max of 6"});
                nms = CraftItemStack.asNMSCopy(stack);
                nms.getTag().setString("type", "repair");
                break;
            case RetrainingBook:
                stack = createItem(Material.ENCHANTED_BOOK, ChatColor.GREEN + "Retraining Book", new String[]{ChatColor.GRAY + "Right click to reset your stat", ChatColor.GRAY + "allocated points to free points."});
                nms = CraftItemStack.asNMSCopy(stack);
                nms.getTag().setString("type", "reset");
                break;
            case MedalOfGathering:
                stack = createItem(Material.YELLOW_FLOWER, ChatColor.GREEN + "Medal of Gathering", new String[]{ChatColor.GRAY + "Increase storage space by 1 row." + ChatColor.RED + ChatColor.BOLD + "Max of 6"});
                nms = CraftItemStack.asNMSCopy(stack);
                nms.getTag().setString("type", "gathering");
                break;
            case CharacterJournal:

                break;
        }
        return AntiCheat.getInstance().applyAntiDupe(CraftItemStack.asBukkitCopy(nms));
    }


    public static ItemStack createFishingPole(int tier) {
        ItemStack rawStack = new ItemStack(Material.FISHING_ROD);
        String name = "";
        switch (tier) {
            case 1:
                name = ChatColor.BOLD + "Weak Rod";
                break;
            case 2:
                name = ChatColor.GREEN.toString() + ChatColor.BOLD + "Basic Rod";
                break;
            case 3:
                name = ChatColor.AQUA.toString() + ChatColor.BOLD + "Intermediate Rod";
                break;
            case 4:
                name = ChatColor.LIGHT_PURPLE.toString() + ChatColor.BOLD + "Strong Rod";
                break;
            case 5:
                name = ChatColor.YELLOW.toString() + ChatColor.BOLD + "Master Rod";
                break;
            default:
                break;
        }
        ItemMeta meta = rawStack.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(Collections.singletonList(0 + "/" + Fishing.getMaxXP(tier)));
        rawStack.setItemMeta(meta);
        net.minecraft.server.v1_8_R3.ItemStack nmsStack = CraftItemStack.asNMSCopy(rawStack);
        NBTTagCompound tag = nmsStack.getTag() == null ? new NBTTagCompound() : nmsStack.getTag();
        tag.set("type", new NBTTagString("rod"));
        tag.setInt("itemTier", tier);
        tag.setInt("XP", 0);
        tag.setInt("maxXP", Fishing.getMaxXP(tier));
        nmsStack.setTag(tag);
        return AntiCheat.getInstance().applyAntiDupe(CraftItemStack.asBukkitCopy(nmsStack));
    }

    /**
     * @param m
     * @param name
     * @param lore
     * @return ItemStack
     */
    public static ItemStack createItem(Material m, String name, String[] lore) {
        ItemStack is = new ItemStack(m, 1);
        ItemMeta meta = is.getItemMeta();
        meta.setDisplayName(name);
        if (lore != null)
            meta.setLore(Arrays.asList(lore));
        is.setItemMeta(meta);
        return is;
    }

    /**
     * @param m
     * @param name
     * @param lore
     * @return ItemStack
     */
    public static ItemStack createItemWithData(Material m, String name, String[] lore, short i) {
        ItemStack is = new ItemStack(m, 1, i);
        ItemMeta meta = is.getItemMeta();
        meta.setDisplayName(name);
        if (lore != null)
            meta.setLore(Arrays.asList(lore));
        is.setItemMeta(meta);
        return is;
    }

    /**
     * Creates a Character Journal for p
     *
     * @return ItemStack
     */
    public static ItemStack createCharacterJournal(Player p) {
        ItemStack stack = createItem(Material.WRITTEN_BOOK, ChatColor.YELLOW.toString() + "Character Journal", new String[]{"Right Click to see stuff"});
        BookMeta bm = (BookMeta) stack.getItemMeta();
        List<String> pages = new ArrayList<String>();
        String page1_string = "";
        String page2_string = "";
        String page3_string = "";
        String page4_string = "";
        String new_line = "\n" + ChatColor.WHITE.toString() + "`" + "\n";
        GamePlayer gp = API.getGamePlayer(p);
        String pretty_align = gp.getPlayerAlignment().getAlignmentColor() + gp.getPlayerAlignment().name();
        DecimalFormat df = new DecimalFormat("#.##");
        PlayerStats stats = gp.getStats();

        page1_string = ChatColor.BLACK.toString() + "" + ChatColor.BOLD.toString() + ChatColor.UNDERLINE.toString() + "  Your Character" + "\n" + new_line
                + ChatColor.BLACK.toString() + ChatColor.BOLD.toString() + "Alignment: " + pretty_align + "\n"
                + ChatColor.BLACK.toString() + gp.getPlayerAlignment().description + new_line + ChatColor.BLACK.toString() + "   " + gp.getPlayerCurrentHP()
                + " / " + gp.getPlayerMaxHP() + "" + ChatColor.BOLD.toString() + " HP" + "\n" + ChatColor.BLACK.toString()
                + "   " + gp.getStats().getDPS() + "% " + ChatColor.BOLD.toString() + "DPS" + "\n" + ChatColor.BLACK.toString()
                + "   " + (HealthHandler.getInstance().getPlayerHPRegenLive(p)) + " " + ChatColor.BOLD.toString() + "HP/s" + "\n"
                + ChatColor.BLACK.toString() + "   " + "0.00"
                + "% " + ChatColor.BOLD.toString() + "Energy" + "\n" + ChatColor.BLACK.toString() + "   " + DatabaseAPI.getInstance().getData(EnumData.ECASH, p.getUniqueId()) + ChatColor.BOLD.toString()
                + " E-CASH";

        page2_string = ChatColor.DARK_AQUA.toString() + ChatColor.BOLD + "  ** LEVEL/EXP **\n\n" + ChatColor.BLACK + ChatColor.BOLD
                + "       LEVEL\n" + "          " + ChatColor.BLACK + gp.getLevel() + "\n\n" + ChatColor.BLACK + ChatColor.BOLD
                + "          XP" + "\n" + ChatColor.BLACK + "       " + (int) gp.getExperience() + "/"
                + gp.getEXPNeeded(gp.getLevel());

        page3_string = ChatColor.BLACK.toString() + ChatColor.BOLD.toString() + "+ " + stats.strPoints + " Strength"
                + "\n" + ChatColor.BLACK.toString() + "   " + ChatColor.UNDERLINE.toString() + "'The Warrior'" + "\n"
//                + ChatColor.BLACK.toString() + "+" + df.format("STR * 0.03") + "% Armor" + "\n"
                + ChatColor.BLACK.toString() + "+" + df.format(stats.getBlock() * 100) + "% Block" + "\n"
                + ChatColor.BLACK.toString() + "+" + df.format(stats.getAxeDMG() * 100) + "% Axe DMG" + "\n"
                + ChatColor.BLACK.toString() + "+" + df.format(stats.getPolearmDMG() * 100) + "% Polearm DMG" + "\n" + "\n"
                + ChatColor.BLACK.toString() + ChatColor.BOLD.toString() + "+ " + stats.dexPoints + " Dexterity" + "\n"
                + ChatColor.BLACK.toString() + "   " + ChatColor.UNDERLINE.toString() + "'The Archer'" + "\n"
                + ChatColor.BLACK.toString() + "+" + df.format(stats.getDodge() * 100) + "% Dodge" + "\n"
                + ChatColor.BLACK.toString() + "+" + df.format(stats.getBowDMG() * 100) + "% Bow DMG" + "\n"
                + ChatColor.BLACK.toString() + "+" + df.format(stats.getCritChance() * 100) + "% Critical Hit" + "\n"
                + ChatColor.BLACK.toString() + "+" + df.format(stats.getArmorPen() * 100) + "% Armor Pen.";

        page4_string = ChatColor.BLACK.toString() + ChatColor.BOLD.toString() + "+ " + stats.vitPoints + " Vitality"
                + "\n" + ChatColor.BLACK.toString() + "   " + ChatColor.UNDERLINE.toString() + "'The Defender'" + "\n"
                + ChatColor.BLACK.toString() + "+" + df.format(stats.getVitHP() * 100) + "% Health" + "\n"
                + ChatColor.BLACK.toString() + "+" + df.format(stats.getHPRegen() * 100) + "   HP/s" + "\n"
                + ChatColor.BLACK.toString() + "+" + df.format(stats.getSwordDMG() * 100) + "% Sword DMG" + "\n" + "\n"
                + ChatColor.BLACK.toString() + ChatColor.BOLD.toString() + "+ " + stats.intPoints + " Intellect" + "\n"
                + ChatColor.BLACK.toString() + "   " + ChatColor.UNDERLINE.toString() + "'The Mage'" + "\n"
                + ChatColor.BLACK.toString() + "+" + df.format(stats.getEnergyRegen() * 100) + "% Energy" + "\n"
                + ChatColor.BLACK.toString() + "+" + df.format(stats.getCritChance() * 100) + "% Critical Hit" + "\n"
                + ChatColor.BLACK.toString() + "+" + df.format(stats.getStaffDMG() * 100) + "% Staff DMG";


        bm.setAuthor("");
        pages.add(page1_string);
        pages.add(page2_string);
        pages.add(page3_string);
        pages.add(page4_string);
        bm.setPages(pages);
        stack.setItemMeta(bm);
        net.minecraft.server.v1_8_R3.ItemStack nms = CraftItemStack.asNMSCopy(stack);
        nms.getTag().setString("type", "important");
        nms.getTag().setString("journal", "true");
        return CraftItemStack.asBukkitCopy(nms);
    }


    /**
     * returns playerProfile
     *
     * @param player
     * @param displayName
     * @param lore
     * @return ItemStack
     * @since 1.0
     */
    public static ItemStack getPlayerProfile(Player player, String displayName, String[] lore) {
        ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
        SkullMeta meta = (SkullMeta) skull.getItemMeta();
        meta.setOwner(player.getName());
        meta.setDisplayName(displayName);
        meta.setLore(Arrays.asList(lore));
        skull.setItemMeta(meta);
        net.minecraft.server.v1_8_R3.ItemStack nmsStack = CraftItemStack.asNMSCopy(skull);
        NBTTagCompound tag = nmsStack.getTag() == null ? new NBTTagCompound() : nmsStack.getTag();
        tag.set("type", new NBTTagString("important"));
        tag.set("usage", new NBTTagString("profile"));
        nmsStack.setTag(tag);
        return CraftItemStack.asBukkitCopy(nmsStack);
    }

    /**
     * Remove the cost of gems from itemstacks lore.
     *
     * @param stack
     */
    public static ItemStack removePrice(ItemStack stack) {
        ItemStack item = stack.clone();
        ItemMeta meta = stack.getItemMeta();
        if (meta != null && meta.hasLore()) {
            List<String> lore = meta.getLore();
            for (int i = 0; i < lore.size(); i++) {
                String line = lore.get(i);
                if (line.contains("Price") || line.contains("Gems")) {
                    lore.remove(i);
                    break;
                }
            }
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }
}
