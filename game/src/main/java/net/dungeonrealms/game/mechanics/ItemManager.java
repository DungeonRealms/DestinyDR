package net.dungeonrealms.game.mechanics;

import net.dungeonrealms.GameAPI;
import net.dungeonrealms.game.database.DatabaseAPI;
import net.dungeonrealms.game.database.type.EnumData;
import net.dungeonrealms.game.handlers.FriendHandler;
import net.dungeonrealms.game.handlers.HealthHandler;
import net.dungeonrealms.game.handlers.KarmaHandler;
import net.dungeonrealms.game.mastery.GamePlayer;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.miscellaneous.ItemBuilder;
import net.dungeonrealms.game.miscellaneous.RandomHelper;
import net.dungeonrealms.game.player.inventory.PlayerMenus;
import net.dungeonrealms.game.player.stats.PlayerStats;
import net.dungeonrealms.game.profession.Fishing;
import net.dungeonrealms.game.profession.Mining;
import net.dungeonrealms.game.world.anticheat.AntiCheat;
import net.dungeonrealms.game.world.entities.types.mounts.mule.MuleTier;
import net.dungeonrealms.game.world.items.Item;
import net.dungeonrealms.game.world.items.itemgenerator.ItemGenerator;
import net.dungeonrealms.game.world.items.repairing.RepairAPI;
import net.dungeonrealms.game.world.realms.Realms;
import net.dungeonrealms.game.world.teleportation.TeleportAPI;
import net.minecraft.server.v1_9_R2.NBTTagCompound;
import net.minecraft.server.v1_9_R2.NBTTagInt;
import net.minecraft.server.v1_9_R2.NBTTagList;
import net.minecraft.server.v1_9_R2.NBTTagString;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_9_R2.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionType;

import java.text.DecimalFormat;
import java.util.*;

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
        net.minecraft.server.v1_9_R2.ItemStack nmsStack = CraftItemStack.asNMSCopy(rawStack);
        NBTTagCompound tag = nmsStack.getTag() == null ? new NBTTagCompound() : nmsStack.getTag();
        tag.set("type", new NBTTagString("important"));
        tag.set("type", new NBTTagString("important"));
        tag.set("destroy", new NBTTagString("yes"));
        nmsStack.setTag(tag);
        return AntiCheat.getInstance().applyAntiDupe(CraftItemStack.asBukkitCopy(nmsStack));
    }

    public static ItemStack createOrbofAlteration() {
        ItemStack rawStack = createItem(Material.MAGMA_CREAM, ChatColor.LIGHT_PURPLE.toString() + "Orb of Alteration", new String[]{(ChatColor.GRAY.toString() + "Randomizes bonus stats of selected equipment")});
        net.minecraft.server.v1_9_R2.ItemStack nms = CraftItemStack.asNMSCopy(rawStack);
        nms.getTag().setString("type", "orb");
        return CraftItemStack.asBukkitCopy(nms);
    }

    public static ItemStack createOrbofFlight() {
        ItemStack orbOfFlight = createItem(Material.FIREWORK_CHARGE, ChatColor.AQUA.toString() + "Orb of Flight",
                Arrays.asList((ChatColor.GRAY.toString() + "Enables " + ChatColor.UNDERLINE + "FLYING" + ChatColor.GRAY + " in realm for the owner "),
                        (ChatColor.GRAY.toString() + "and all builders for 30 minute(s)."),
                        (ChatColor.RED.toString() + ChatColor.BOLD.toString() + "REQ:" + ChatColor.RED.toString() + " Active Orb of Peace")).toArray(new String[3]));

        net.minecraft.server.v1_9_R2.ItemStack nms = CraftItemStack.asNMSCopy(orbOfFlight);
        nms.getTag().setString("orb", "flight");
        return CraftItemStack.asBukkitCopy(nms);
    }

    public static ItemStack createOrbofPeace() {
        ItemStack orbOfPeace = createItem(Material.ENDER_PEARL, ChatColor.LIGHT_PURPLE.toString() + "Orb of Peace",
                new String[]{(ChatColor.GRAY.toString() + "Set realm to " + ChatColor.UNDERLINE + "SAFE ZONE" + ChatColor.GRAY + " for 1 hour(s).")});
        net.minecraft.server.v1_9_R2.ItemStack nms = CraftItemStack.asNMSCopy(orbOfPeace);
        nms.getTag().setString("orb", "peace");
        return CraftItemStack.asBukkitCopy(nms);
    }

    public static ItemStack createWeaponEnchant(int tier) {
        String material = getWeaponMatString(tier);
        ItemStack rawStack = createItem(Material.EMPTY_MAP, ChatColor.WHITE.toString() + ChatColor.BOLD.toString() + "Scroll: " + GameAPI.getTierColor(tier) + "Enchant " + material + " Weapon", new String[]{ChatColor.RED + "+5% DMG", ChatColor.GRAY.toString() + ChatColor.ITALIC.toString() + "Weapon will VANISH if enchant above +3 FAILS."});
        net.minecraft.server.v1_9_R2.ItemStack nms = CraftItemStack.asNMSCopy(rawStack);
        nms.getTag().setString("type", "weaponenchant");
        nms.getTag().setInt("tier", tier);
        return CraftItemStack.asBukkitCopy(nms);
    }

    public static ItemStack createArmorEnchant(int tier) {
        String material = getArmorMatString(tier);
        ItemStack rawStack = createItem(Material.EMPTY_MAP, ChatColor.WHITE.toString() + ChatColor.BOLD.toString() + "Scroll: " + GameAPI.getTierColor(tier) + "Enchant " + material + " Armor", new String[]{ChatColor.RED + "+5% HP", ChatColor.RED + "+5% HP REGEN", ChatColor.GRAY.toString() + ChatColor.ITALIC + "    - OR -", ChatColor.RED + "+1% ENERGY REGEN", ChatColor.GRAY.toString() + ChatColor.ITALIC.toString() + "Armor will VANISH if enchant above +3 FAILS."});
        net.minecraft.server.v1_9_R2.ItemStack nms = CraftItemStack.asNMSCopy(rawStack);
        nms.getTag().setString("type", "armorenchant");
        nms.getTag().setInt("tier", tier);
        return CraftItemStack.asBukkitCopy(nms);
    }

    /**
     * @param tier
     * @return
     */
    private static String getArmorMatString(int tier) {
        switch (tier) {
            case 1:
                return "Leather";
            case 2:
                return "Chainmail";
            case 3:
                return "Iron";
            case 4:
                return "Diamond";
            case 5:
                return "Gold";
        }
        return null;
    }

    private static String getWeaponMatString(int tier) {
        switch (tier) {
            case 1:
                return "Wooden";
            case 2:
                return "Stone";
            case 3:
                return "Iron";
            case 4:
                return "Diamond";
            case 5:
                return "Gold";
        }
        return null;
    }

    public static ItemStack createProtectScroll(int tier) {
        String material = getArmorMatString(tier);
        ItemStack rawStack = createItem(Material.EMPTY_MAP, ChatColor.WHITE.toString() + ChatColor.BOLD.toString() + "White Scroll: " + GameAPI.getTierColor(tier) + "Protect " + material + " Equipment", new String[]{
                ChatColor.GRAY + ChatColor.ITALIC.toString() + "Apply to any T" + tier + " item to " + ChatColor.UNDERLINE + "prevent" + ChatColor.GRAY + ChatColor.ITALIC.toString() + " it", ChatColor.GRAY + ChatColor.ITALIC.toString() + "from being destroyed if the next", ChatColor.GRAY + ChatColor.ITALIC.toString() + "enchantment scroll (up to +8) fails."});
        net.minecraft.server.v1_9_R2.ItemStack nms = CraftItemStack.asNMSCopy(rawStack);
        nms.getTag().setString("type", "protection");
        nms.getTag().setInt("tier", tier);
        return CraftItemStack.asBukkitCopy(nms);
    }

    /**
     * Creates a random Teleport book
     *
     * @return ItemStack
     * @since 1.0
     */
    public static ItemStack createRandomTeleportBook() {
        ItemStack rawStack = new ItemStack(Material.BOOK);
        ItemMeta meta = rawStack.getItemMeta();
        String teleportLocation = TeleportAPI.getRandomTeleportString();
        String displayName = TeleportAPI.getDisplayNameOfLocation(teleportLocation);
        meta.setDisplayName(ChatColor.WHITE.toString() + ChatColor.BOLD + "Teleport: " + ChatColor.WHITE + teleportLocation.replace("_", " "));
        meta.setLore(Collections.singletonList(ChatColor.GRAY + "(Right-Click) Teleport to " + displayName));
        rawStack.setItemMeta(meta);
        net.minecraft.server.v1_9_R2.ItemStack nmsStack = CraftItemStack.asNMSCopy(rawStack);
        NBTTagCompound tag = nmsStack.getTag() == null ? new NBTTagCompound() : nmsStack.getTag();
        tag.set("type", new NBTTagString("teleport"));
        tag.set("usage", new NBTTagString(teleportLocation));
        nmsStack.setTag(tag);
        return CraftItemStack.asBukkitCopy(nmsStack);
    }

    /**
     * Creates a Teleport book using location for a String
     *
     * @return ItemStack
     * @since 1.0
     */
    public static ItemStack createTeleportBook(String location) {
        ItemStack rawStack = new ItemStack(Material.BOOK);
        ItemMeta meta = rawStack.getItemMeta();
        String displayName = TeleportAPI.getDisplayNameOfLocation(location);
        meta.setDisplayName(ChatColor.WHITE.toString() + ChatColor.BOLD + "Teleport: " + ChatColor.WHITE + location.replace("_", " "));
        meta.setLore(Collections.singletonList(ChatColor.GRAY + "(Right-Click) Teleport to " + displayName));
        rawStack.setItemMeta(meta);
        net.minecraft.server.v1_9_R2.ItemStack nmsStack = CraftItemStack.asNMSCopy(rawStack);
        NBTTagCompound tag = nmsStack.getTag() == null ? new NBTTagCompound() : nmsStack.getTag();
        tag.set("type", new NBTTagString("teleport"));
        tag.set("usage", new NBTTagString(location));
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
                name = ChatColor.WHITE + "Leather";
                break;
            case 2:
                rawStack = new ItemStack(Material.IRON_FENCE, 64);
                name = ChatColor.GREEN + "Chain";
                break;
            case 3:
                rawStack = new ItemStack(Material.INK_SACK, 64, (short) 7);
                name = ChatColor.AQUA + "Iron";
                break;
            case 4:
                rawStack = new ItemStack(Material.INK_SACK, 64, DyeColor.LIGHT_BLUE.getDyeData());
                name = ChatColor.LIGHT_PURPLE + "Diamond";
                break;
            case 5:
                rawStack = new ItemStack(Material.INK_SACK, 64, DyeColor.YELLOW.getDyeData());
                name = ChatColor.YELLOW + "Gold";
                break;
            default:
                break;
        }
        if (rawStack != null) {
            ItemMeta meta = rawStack.getItemMeta();
            meta.setDisplayName(name + " Scrap");
            meta.setLore(Collections.singletonList(ChatColor.GRAY + "Repairs 3% durability on " + name + ChatColor.GRAY + " equipment."));
            rawStack.setItemMeta(meta);
            net.minecraft.server.v1_9_R2.ItemStack nmsStack = CraftItemStack.asNMSCopy(rawStack);
            NBTTagCompound tag = nmsStack.getTag() == null ? new NBTTagCompound() : nmsStack.getTag();
            tag.set("type", new NBTTagString("scrap"));
            tag.setInt("itemTier", tier);
            nmsStack.setTag(tag);
            return CraftItemStack.asBukkitCopy(nmsStack);
        }
        return null;
    }

    public static ItemStack getPlayerMuleItem(MuleTier tier) {
        ItemStack stack = PlayerMenus.editItem(new ItemStack(Material.LEASH), tier.getName(), new String[]{
                ChatColor.RED + "Storage Size: " + tier.getSize() + " Items", ChatColor.RED + "An old worn mule."
        });
        net.minecraft.server.v1_9_R2.ItemStack nmsStack = CraftItemStack.asNMSCopy(stack);
        NBTTagCompound tag = nmsStack.getTag() == null ? new NBTTagCompound() : nmsStack.getTag();
        tag.set("type", new NBTTagString("important"));
        tag.set("destroy", new NBTTagString("yes"));
        tag.setInt("muleTier", tier.getTier());
        tag.setString("usage", "mule");
        tag.setString("mule", "true");
        nmsStack.setTag(tag);
        return CraftItemStack.asBukkitCopy(nmsStack);
    }


    public static ItemStack createRealmPortalRune(UUID uuid) {
        int realmTier = Realms.getInstance().getRealmTier(uuid);
        int realmDimensions = Realms.getInstance().getRealmDimensions(realmTier);


        return new ItemBuilder().setItem(createItem(Material.NETHER_STAR, ChatColor.LIGHT_PURPLE.toString() + ChatColor.BOLD + "Realm Portal Rune",
                new String[]{ChatColor.GRAY + "Tier: " + realmTier + "/7" + " [" + realmDimensions + "x" + realmDimensions + "x" + realmDimensions + "]"
                        , ChatColor.LIGHT_PURPLE + "Right Click: " + ChatColor.GRAY + "Open Portal",
                        ChatColor.LIGHT_PURPLE + "Left Click: " + ChatColor.GRAY + "Realm Shop",
                        ChatColor.LIGHT_PURPLE + "Sneak-Right Click: " + ChatColor.GRAY + "Upgrade Realm",
                        ChatColor.LIGHT_PURPLE + "Sneak-Left Click: " + ChatColor.GRAY + "Add Builder",})).setNBTString("type", "important")
                .setNBTString("subtype", "nondrop").setNBTString("realmPortalRune", "true").build();
    }

    public static ItemStack createMuleUpgrade(int tier) {
        ItemStack is = null;
        if (tier == 2)
            is = new ItemBuilder().setItem(createItem(Material.CHEST, ChatColor.AQUA + "Adventurer's Storage Mule Chest", new String[]{
                    ChatColor.RED + "18 Max Storage Size", ChatColor.GRAY + "Apply to your " + ChatColor.GREEN + "Old Storage Mule" + ChatColor.GRAY + " to expand its inventory!"}))
//                    .addLore(ChatColor.WHITE + "5000" + ChatColor.AQUA + " Portal Key Shards")
                    .setNBTInt("muleLevel", 2).setNBTString("type", "important").setNBTString("usage", "muleUpgrade").setNBTString("destroy", "yes").build();
        else if (tier == 3)
            is = new ItemBuilder().setItem(createItem(Material.CHEST, ChatColor.AQUA + "Royal Storage Mule Chest", new String[]{
                    ChatColor.RED + "27 Max Storage Size", ChatColor.GRAY + "Apply to your " + ChatColor.AQUA + "Adventurer's Storage Mule", ChatColor.GRAY + "to further expand its inventory!"}))
//                    .addLore(ChatColor.WHITE + "8000" + ChatColor.LIGHT_PURPLE + " Portal Key Shards")
                    .setNBTInt("muleLevel", 3).setNBTString("type", "important").setNBTString("usage", "muleUpgrade").setNBTString("destroy", "yes").build();

        return is;
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
                    healAmount = RandomHelper.getRandomNumberBetween(10, 50);
                } else {
                    healAmount = 40;
                }
                break;
            case 2:
                if (!isSplashPotion) {
                    name = ChatColor.GREEN + "Inferior Elixir of Singular Healing";
                } else {
                    name = ChatColor.GREEN + "Inferior Elixir of Splash Healing";
                }
                if (!fromShop) {
                    healAmount = RandomHelper.getRandomNumberBetween(60, 120);
                } else {
                    healAmount = 90;
                }
                break;
            case 3:
                if (!isSplashPotion) {
                    name = ChatColor.AQUA + "Modest Elixir of Singular Healing";
                } else {
                    name = ChatColor.AQUA + "Modest Elixir of Splash Healing";
                }
                if (!fromShop) {
                    healAmount = RandomHelper.getRandomNumberBetween(250, 400);
                } else {
                    healAmount = 350;
                }
                break;
            case 4:
                if (!isSplashPotion) {
                    name = ChatColor.LIGHT_PURPLE + "Superior Elixir of Singular Healing";
                } else {
                    name = ChatColor.LIGHT_PURPLE + "Superior Elixir of Splash Healing";
                }
                if (!fromShop) {
                    healAmount = RandomHelper.getRandomNumberBetween(700, 950);
                } else {
                    healAmount = 850;
                }
                break;
            case 5:
                if (!isSplashPotion) {
                    name = ChatColor.YELLOW + "Legendary Elixir of Singular Healing";
                } else {
                    name = ChatColor.YELLOW + "Legendary Elixir of Splash Healing";
                }
                if (!fromShop) {
                    healAmount = RandomHelper.getRandomNumberBetween(1600, 2100);
                } else {
                    healAmount = 1900;
                }
                break;
            default:
                break;
        }
        if (!fromShop) {
            healAmount = (((healAmount + 5) / 10) * 10);
        }
        if (!isSplashPotion) {
            Potion potion = new Potion(PotionType.WATER, 1);
            potion.setSplash(false);
            ItemStack rawStack = potion.toItemStack(1);
            switch (tier) {
                case 1:
                    new Potion(PotionType.REGEN).apply(rawStack);
                    break;
                case 2:
                    new Potion(PotionType.INSTANT_HEAL).apply(rawStack);
                    break;
                case 3:
                    new Potion(PotionType.STRENGTH).apply(rawStack);
                    break;
                case 4:
                    new Potion(PotionType.INSTANT_DAMAGE).apply(rawStack);
                    break;
                case 5:
                    new Potion(PotionType.FIRE_RESISTANCE).apply(rawStack);
                    break;
                default:
                    break;

            }
            PotionMeta potionMeta = (PotionMeta) rawStack.getItemMeta();
            potionMeta.setDisplayName(name);
            potionMeta.setLore(Collections.singletonList(ChatColor.GRAY + "An Elixir that heals for " + ChatColor.RED + ChatColor.BOLD + healAmount + ChatColor.GRAY + "HP."));
            rawStack.setItemMeta(potionMeta);
            net.minecraft.server.v1_9_R2.ItemStack nmsStack = CraftItemStack.asNMSCopy(rawStack);
            NBTTagCompound tag = nmsStack.getTag() == null ? new NBTTagCompound() : nmsStack.getTag();
            tag.set("type", new NBTTagString("healthPotion"));
            tag.setInt("itemTier", tier);
            tag.setInt("healAmount", healAmount);
            nmsStack.setTag(tag);
            return AntiCheat.getInstance().applyAntiDupe(CraftItemStack.asBukkitCopy(nmsStack));
        } else {
            healAmount *= 0.65;
            Potion potion = new Potion(PotionType.WATER, 1);
            potion.setSplash(true);
            ItemStack rawStack = potion.toItemStack(1);
            switch (tier) {
                case 1:
                    new Potion(PotionType.REGEN).apply(rawStack);
                    break;
                case 2:
                    new Potion(PotionType.INSTANT_HEAL).apply(rawStack);
                    break;
                case 3:
                    new Potion(PotionType.STRENGTH).apply(rawStack);
                    break;
                case 4:
                    new Potion(PotionType.INSTANT_DAMAGE).apply(rawStack);
                    break;
                case 5:
                    new Potion(PotionType.FIRE_RESISTANCE).apply(rawStack);
                    break;
                default:
                    break;

            }
            PotionMeta potionMeta = (PotionMeta) rawStack.getItemMeta();
            potionMeta.setDisplayName(name);
            potionMeta.setLore(Collections.singletonList(ChatColor.GRAY + "An Elixir that heals for " + ChatColor.RED + ChatColor.BOLD + healAmount + ChatColor.GRAY + "HP in a " + ChatColor.RED + ChatColor.BOLD + "4x4" + ChatColor.GRAY + " Area."));
            rawStack.setItemMeta(potionMeta);
            net.minecraft.server.v1_9_R2.ItemStack nmsStack = CraftItemStack.asNMSCopy(rawStack);
            NBTTagCompound tag = nmsStack.getTag() == null ? new NBTTagCompound() : nmsStack.getTag();
            tag.set("type", new NBTTagString("splashHealthPotion"));
            tag.setInt("itemTier", tier);
            tag.setInt("healAmount", healAmount);
            nmsStack.setTag(tag);
            return AntiCheat.getInstance().applyAntiDupe(CraftItemStack.asBukkitCopy(nmsStack));
        }
    }

    public static ItemStack createHealingFood(int tier, Item.ItemRarity rarity) {
        ItemStack rawStack = null;
        String name = "";
        String description = "";
        int healAmount = 0;
        switch (tier) {
            case 1:
                switch (rarity) {
                    case COMMON:
                        name = ChatColor.WHITE + "Plowed Potato";
                        description = ChatColor.GRAY + "The staple crop of Andulucia.";
                        healAmount = 9;
                        rawStack = new ItemStack(Material.POTATO_ITEM, 1);
                        break;
                    case RARE:
                        name = ChatColor.WHITE + "Loaded Potato Skin";
                        description = ChatColor.GRAY + "Extremely Tasty.";
                        healAmount = 16;
                        rawStack = new ItemStack(Material.BAKED_POTATO, 1);
                        break;
                    case UNIQUE:
                        name = ChatColor.WHITE + "Fresh Apple";
                        description = ChatColor.GRAY + "Fresh from the local Apple Tree.";
                        healAmount = 25;
                        rawStack = new ItemStack(Material.APPLE, 1);
                        break;
                }
                break;
            case 2:
                switch (rarity) {
                    case COMMON:
                        name = ChatColor.GREEN + "Uncooked Chicken";
                        description = ChatColor.GRAY + "This may or may not be safe to eat...";
                        healAmount = 42;
                        rawStack = new ItemStack(Material.RAW_CHICKEN, 1);
                        break;
                    case RARE:
                        name = ChatColor.GREEN + "Roast Chicken";
                        description = ChatColor.GRAY + "Warm and toasty. Delicious too.";
                        healAmount = 55;
                        rawStack = new ItemStack(Material.COOKED_CHICKEN, 1);
                        break;
                    case UNIQUE:
                        name = ChatColor.GREEN + "Pumpkin Pie";
                        description = ChatColor.GRAY + "The spookiest meal you'll ever eat.";
                        healAmount = 70;
                        rawStack = new ItemStack(Material.PUMPKIN_PIE, 1);
                        break;
                }
                break;
            case 3:
                switch (rarity) {
                    case COMMON:
                        name = ChatColor.AQUA + "Salted Pork";
                        description = ChatColor.GRAY + "Bringing in the bacon.";
                        healAmount = 90;
                        rawStack = new ItemStack(Material.PORK, 1);
                        break;
                    case RARE:
                        name = ChatColor.AQUA + "Seasoned Pork";
                        description = ChatColor.GRAY + "Bacon. Except tastier (is that possible?).";
                        healAmount = 150;
                        rawStack = new ItemStack(Material.GRILLED_PORK, 1);
                        break;
                    case UNIQUE:
                        name = ChatColor.AQUA + "Mushroom Soup";
                        description = ChatColor.GRAY + "I hope these are the correct mushrooms.";
                        healAmount = 190;
                        rawStack = new ItemStack(Material.MUSHROOM_SOUP, 1);
                        break;
                }
                break;
            case 4:
                switch (rarity) {
                    case COMMON:
                        name = ChatColor.LIGHT_PURPLE + "Frozen Steak";
                        description = ChatColor.GRAY + "Stop complaining. Your dog would love this.";
                        healAmount = 300;
                        rawStack = new ItemStack(Material.RAW_BEEF, 1);
                        break;
                    case RARE:
                        name = ChatColor.LIGHT_PURPLE + "Rare Sizzling Steak";
                        description = ChatColor.GRAY + "Real men take their steaks rare.";
                        healAmount = 400;
                        rawStack = new ItemStack(Material.COOKED_BEEF, 1);
                        break;
                    case UNIQUE:
                        name = ChatColor.LIGHT_PURPLE + "Grilled Rabbit";
                        description = ChatColor.GRAY + "Aww, look at the cute little bunny.";
                        healAmount = 500;
                        rawStack = new ItemStack(Material.COOKED_MUTTON, 1);
                        break;
                }
                break;
            case 5:
                switch (rarity) {
                    case COMMON:
                        name = ChatColor.YELLOW + "King's Apple";
                        description = ChatColor.GRAY + "A meal fit for a King.";
                        healAmount = 700;
                        rawStack = new ItemStack(Material.GOLDEN_APPLE, 1, (short) 0);
                        break;
                    case RARE:
                        name = ChatColor.YELLOW + "Enchanted King's Apple";
                        description = ChatColor.GRAY + "A powerful King's battle snack.";
                        healAmount = 1000;
                        rawStack = new ItemStack(Material.GOLDEN_APPLE, 1, (short) 1);
                        break;
                    case UNIQUE:
                        name = ChatColor.YELLOW + "Golden Carrot";
                        description = ChatColor.GRAY + "Now this is just a waste of useful gold ore.";
                        healAmount = 1350;
                        rawStack = new ItemStack(Material.GOLDEN_CARROT, 1, (short) 0);
                        break;
                }
                break;
        }
        if (rawStack != null) {
            ItemMeta meta = rawStack.getItemMeta();
            meta.setDisplayName(name);
            List<String> itemLore = new ArrayList<>();
            itemLore.add(ChatColor.RED + "+" + ChatColor.BOLD + healAmount + "HP/s" + ChatColor.RED + " for " + ChatColor.BOLD + "15 " + ChatColor.RED + "Seconds.");
            itemLore.add(ChatColor.RED.toString() + ChatColor.BOLD + "Sprinting will cancel the effect!");
            itemLore.add(description);
            itemLore.add(rarity.getName());
            meta.setLore(itemLore);
            rawStack.setItemMeta(meta);
            net.minecraft.server.v1_9_R2.ItemStack nmsStack = CraftItemStack.asNMSCopy(rawStack);
            NBTTagCompound tag = nmsStack.getTag() == null ? new NBTTagCompound() : nmsStack.getTag();
            tag.set("type", new NBTTagString("healingFood"));
            tag.setInt("itemTier", tier);
            tag.set("itemRarity", new NBTTagInt(rarity.getId()));
            tag.setInt("healAmount", healAmount);
            tag.set("AttributeModifiers", new NBTTagList());
            nmsStack.setTag(tag);
            return CraftItemStack.asBukkitCopy(nmsStack);
        }
        return null;
    }

    /**
     * Adds a starter kit to the player.
     *
     * @param player
     */
    public static void giveStarter(Player player) {
        player.getInventory().addItem(new ItemBuilder().setItem(ItemManager.createHealthPotion(1, false, false))
                .setNBTString("subtype", "starter").addLore(ChatColor.GRAY + "Untradeable").build());
        player.getInventory().addItem(new ItemBuilder().setItem(ItemManager.createHealthPotion(1, false, false))
                .setNBTString("subtype", "starter").addLore(ChatColor.GRAY + "Untradeable").build());
        player.getInventory().addItem(new ItemBuilder().setItem(ItemManager.createHealthPotion(1, false, false))
                .setNBTString("subtype", "starter").addLore(ChatColor.GRAY + "Untradeable").build());
        player.getInventory().addItem(new ItemBuilder().setItem(new ItemStack(Material.BREAD, 3)).setNBTString
                ("subtype", "starter").addLore(ChatColor.GRAY + "Untradeable").build());

        if (Utils.randInt(0, 1) == 1) {
            player.getInventory().addItem(ItemGenerator.getNamedItem("training_sword"));
        } else {
            player.getInventory().addItem(ItemGenerator.getNamedItem("training_axe"));
        }

        player.getInventory().addItem(ItemGenerator.getNamedItem("traininghelm"));
        player.getInventory().addItem(ItemGenerator.getNamedItem("trainingchest"));
        player.getInventory().addItem(ItemGenerator.getNamedItem("traininglegs"));
        player.getInventory().addItem(ItemGenerator.getNamedItem("trainingboots"));
    }


    /**
     * Creates a pickaxe based on the given tier
     *
     * @param tier
     * @return ItemStack
     * @since 1.0
     */
    public static ItemStack createPickaxe(int tier) {
        ItemStack rawStack = null;
        String name = "";
        ArrayList<String> lore = new ArrayList<>();

        String expBar = ChatColor.RED + "||||||||||||||||||||" + "||||||||||||||||||||" + "||||||||||";
        int lvl = Mining.getTierLvl(tier);
        lore.add(ChatColor.GRAY.toString() + "Level: " + GameAPI.getTierColor(tier) + lvl);
        lore.add(ChatColor.GRAY.toString() + 0 + ChatColor.GRAY.toString() + " / " + ChatColor.GRAY + Mining.getEXPNeeded(lvl));
        lore.add(ChatColor.GRAY.toString() + "EXP: " + expBar);
        switch (tier) {
            case 1:
                rawStack = new ItemStack(Material.WOOD_PICKAXE);
                name = ChatColor.WHITE + "Novice Pickaxe";
                lore.add(ChatColor.GRAY.toString() + ChatColor.ITALIC + "A pickaxe made out of sturdy wood.");
                break;
            case 2:
                rawStack = new ItemStack(Material.STONE_PICKAXE);
                name = ChatColor.GREEN.toString() + "Apprentice Pickaxe";
                lore.add(ChatColor.GRAY.toString() + ChatColor.ITALIC + "A pickaxe made out of cave stone.");
                break;
            case 3:
                rawStack = new ItemStack(Material.IRON_PICKAXE);
                name = ChatColor.AQUA.toString() + "Expert Pickaxe";
                lore.add(ChatColor.GRAY.toString() + ChatColor.ITALIC + "A pickaxe made out of forged iron.");
                break;
            case 4:
                rawStack = new ItemStack(Material.DIAMOND_PICKAXE);
                name = ChatColor.LIGHT_PURPLE.toString() + "Supreme Pickaxe";
                lore.add(ChatColor.GRAY.toString() + ChatColor.ITALIC + "A pickaxe made out of hardened diamond.");
                break;
            case 5:
                rawStack = new ItemStack(Material.GOLD_PICKAXE);
                name = ChatColor.YELLOW.toString() + "Master Pickaxe";
                lore.add(ChatColor.GRAY.toString() + ChatColor.ITALIC + "A pickaxe made out of reinforced gold.");
                break;
            default:
                break;
        }
        if (rawStack != null) {
            ItemMeta meta = rawStack.getItemMeta();
            meta.setDisplayName(name);
            meta.setLore(lore);
            rawStack.setItemMeta(meta);
            RepairAPI.setCustomItemDurability(rawStack, 1500);
            net.minecraft.server.v1_9_R2.ItemStack nmsStack = CraftItemStack.asNMSCopy(rawStack);
            NBTTagCompound tag = nmsStack.getTag() == null ? new NBTTagCompound() : nmsStack.getTag();
            tag.set("type", new NBTTagString("pick"));
            tag.setInt("itemTier", tier);
            tag.setInt("XP", 0);
            tag.setInt("maxXP", Mining.getEXPNeeded(lvl));
            tag.setInt("level", lvl);
            tag.set("AttributeModifiers", new NBTTagList());
            nmsStack.setTag(tag);
            return AntiCheat.getInstance().applyAntiDupe(CraftItemStack.asBukkitCopy(nmsStack));
        }
        return null;
    }

    public static ItemStack createRetrainingBook() {
        ItemStack stack;
        net.minecraft.server.v1_9_R2.ItemStack nms;
        NBTTagCompound tag = new NBTTagCompound();
        stack = createItem(Material.ENCHANTED_BOOK, ChatColor.GREEN + "Retraining Book", new String[]{ChatColor.GRAY + "Right click to reset your stat", ChatColor.GRAY + "allocated points to free points.", ChatColor.DARK_GRAY + "One time use."});
        nms = CraftItemStack.asNMSCopy(stack);
        tag.setString("retrainingBook", "true");
        nms.setTag(tag);
        return AntiCheat.getInstance().applyAntiDupe(CraftItemStack.asBukkitCopy(nms));
    }

    public static ItemStack createGlobalMessenger() {
        ItemStack rawStack = new ItemStack(Material.FIREWORK);
        ItemMeta meta = rawStack.getItemMeta();
        meta.setDisplayName(ChatColor.GOLD + "Global Messenger");
        meta.setLore(Arrays.asList(ChatColor.GOLD + "Uses: " + ChatColor.GRAY + "1", ChatColor.GRAY + "Sends a message to all players on " + ChatColor.UNDERLINE + "ALL SHARDS.", ChatColor.GRAY + "Permanent Untradeable"));
        rawStack.setItemMeta(meta);
        net.minecraft.server.v1_9_R2.ItemStack nmsStack = CraftItemStack.asNMSCopy(rawStack);
        NBTTagCompound tag = nmsStack.getTag() == null ? new NBTTagCompound() : nmsStack.getTag();
        tag.set("globalMessenger", new NBTTagString("true"));
        nmsStack.setTag(tag);

        return CraftItemStack.asBukkitCopy(nmsStack);
    }


    public static ItemStack createFishingPole(int tier) {
        ItemStack rawStack = null;
        String name = "";
        ArrayList<String> lore = new ArrayList<>();
        rawStack = new ItemStack(Material.FISHING_ROD, 1);

        ItemMeta meta = rawStack.getItemMeta();
        meta.addEnchant(Enchantment.LURE, 3, false);
        String expBar = ChatColor.RED + "||||||||||||||||||||" + "||||||||||||||||||||" + "||||||||||";
        int lvl = Fishing.getTierLvl(tier);
        lore.add(ChatColor.GRAY.toString() + "Level: " + GameAPI.getTierColor(tier) + lvl);
        lore.add(ChatColor.GRAY.toString() + 0 + ChatColor.GRAY.toString() + " / " + ChatColor.GRAY + Mining.getEXPNeeded(lvl));
        lore.add(ChatColor.GRAY.toString() + "EXP: " + expBar);

        switch (tier) {
            case 1:
                name = ChatColor.WHITE + "Basic Fishingrod";
                lore.add(ChatColor.GRAY.toString() + ChatColor.ITALIC + "A fishing rod made of wood and thread.");
                break;
            case 2:
                name = ChatColor.GREEN.toString() + "Advanced Fishingrod";
                lore.add(ChatColor.GRAY.toString() + ChatColor.ITALIC + "A fishing rod made of oak wood and thread.");
                break;
            case 3:
                name = ChatColor.AQUA.toString() + "Expert Fishingrod";
                lore.add(ChatColor.GRAY.toString() + ChatColor.ITALIC + "A fishing rod made of ancient oak wood and spider silk.");
                break;
            case 4:
                name = ChatColor.LIGHT_PURPLE.toString() + "Supreme Fishingrod";
                lore.add(ChatColor.GRAY.toString() + ChatColor.ITALIC + "A fishing rod made of jungle bamboo and spider silk.");
                break;
            case 5:
                name = ChatColor.YELLOW.toString() + "Master Fishingrod";
                lore.add(ChatColor.GRAY.toString() + ChatColor.ITALIC + "A fishing rod made of rich mahogany and enchanted silk");
                break;
            default:
                break;
        }
        meta.setDisplayName(name);
        meta.setLore(lore);
        rawStack.setItemMeta(meta);
        rawStack.addEnchantment(Enchantment.LURE, 3);
        RepairAPI.setCustomItemDurability(rawStack, 1500);
        net.minecraft.server.v1_9_R2.ItemStack nmsStack = CraftItemStack.asNMSCopy(rawStack);
        NBTTagCompound tag = nmsStack.getTag() == null ? new NBTTagCompound() : nmsStack.getTag();
        tag.setString("type", "rod");
        tag.setInt("itemTier", tier);
        tag.setInt("level", lvl);
        tag.setInt("XP", 0);
        tag.setInt("maxXP", Fishing.getEXPNeeded(lvl));
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
        ItemStack stack = createItem(Material.WRITTEN_BOOK, ChatColor.GREEN.toString() + ChatColor.BOLD + "Character Journal", new String[]{ChatColor.GREEN + "Left Click: " + ChatColor.GRAY + "Invite to Party", ChatColor.GREEN + "Sneak-Right Click: " + ChatColor.GRAY + "Setup Shop"});
        BookMeta bm = (BookMeta) stack.getItemMeta();
        List<String> pages = new ArrayList<>();
        String page1_string;
        String page2_string;
        String page3_string;
        String page4_string;
        String new_line = "\n" + ChatColor.WHITE.toString() + "`" + "\n";
        GamePlayer gp = GameAPI.getGamePlayer(p);
        if(gp == null)
            return stack;
        KarmaHandler.EnumPlayerAlignments playerAlignment = gp.getPlayerAlignment();
        String pretty_align = (playerAlignment == KarmaHandler.EnumPlayerAlignments.LAWFUL ? ChatColor.DARK_GREEN.toString() :
                playerAlignment.getAlignmentColor()) + ChatColor.UNDERLINE.toString() + playerAlignment.name();
        DecimalFormat df = new DecimalFormat("#.##");
        PlayerStats stats = gp.getStats();

        if (pretty_align.contains("CHAOTIC")) {
            String time = String.valueOf(KarmaHandler.getInstance().getAlignmentTime(p));
            page1_string = ChatColor.BLACK.toString() + "" + ChatColor.BOLD.toString() + ChatColor.UNDERLINE.toString() + "  Your Character" + "\n"
                    + ChatColor.BLACK.toString() + ChatColor.BOLD.toString() + "Alignment: " + pretty_align + "\n" + ChatColor.RED.toString() + ChatColor.BOLD + time + "s.." + new_line
                    + ChatColor.BLACK.toString() + playerAlignment.description + new_line + ChatColor.BLACK.toString() + "   " + gp.getPlayerCurrentHP()
                    + " / " + gp.getPlayerMaxHP() + "" + ChatColor.BOLD.toString() + " HP" + "\n" + ChatColor.BLACK.toString()
                    + "   " + Math.round(gp.getStats().getDPS()) + "% " + ChatColor.BOLD.toString() + "DPS" + "\n" + ChatColor.BLACK.toString()
                    + "   " + (HealthHandler.getInstance().getPlayerHPRegenLive(p)) + " " + ChatColor.BOLD.toString() + "HP/s" + "\n" + ChatColor.BLACK.toString()
                    + "   " + gp.getStaticAttributeVal(Item.ArmorAttributeType.ENERGY_REGEN) + "% " + ChatColor.BOLD.toString() + "Energy/s" + "\n" + ChatColor.BLACK.toString()
                    + "   " + DatabaseAPI.getInstance().getData(EnumData.ECASH, p.getUniqueId()) + ChatColor.BOLD.toString() + " E-CASH" + "\n" + ChatColor.BLACK.toString()
                    + "   " + gp.getPlayerGemFind() + ChatColor.BOLD.toString() + " GEM FIND" + "\n" + ChatColor.BLACK.toString()
                    + "   " + gp.getPlayerItemFind() + ChatColor.BOLD.toString() + " ITEM FIND";
        } else {
            page1_string = ChatColor.BLACK.toString() + "" + ChatColor.BOLD.toString() + ChatColor.UNDERLINE.toString() + "  Your Character" + "\n" + new_line
                    + ChatColor.BLACK.toString() + ChatColor.BOLD.toString() + "Alignment: " + pretty_align + new_line
                    + ChatColor.BLACK.toString() + playerAlignment.description + new_line + ChatColor.BLACK.toString() + "   " + gp.getPlayerCurrentHP()
                    + " / " + gp.getPlayerMaxHP() + "" + ChatColor.BOLD.toString() + " HP" + "\n" + ChatColor.BLACK.toString()
                    + "   " + Math.round(gp.getStats().getDPS()) + "% " + ChatColor.BOLD.toString() + "DPS" + "\n" + ChatColor.BLACK.toString()
                    + "   " + (HealthHandler.getInstance().getPlayerHPRegenLive(p) + gp.getStats().getHPRegen()) + " " + ChatColor.BOLD.toString() + "HP/s" + "\n" + ChatColor.BLACK.toString()
                    + "   " + gp.getStaticAttributeVal(Item.ArmorAttributeType.ENERGY_REGEN) + "% " + ChatColor.BOLD.toString() + "Energy/s" + "\n" + ChatColor.BLACK.toString()
                    + "   " + DatabaseAPI.getInstance().getData(EnumData.ECASH, p.getUniqueId()) + ChatColor.BOLD.toString() + " E-CASH" + "\n" + ChatColor.BLACK.toString()
                    + "   " + gp.getPlayerGemFind() + ChatColor.BOLD.toString() + " GEM FIND" + "\n" + ChatColor.BLACK.toString()
                    + "   " + gp.getPlayerItemFind() + ChatColor.BOLD.toString() + " ITEM FIND";
        }
        page2_string = ChatColor.DARK_AQUA.toString() + ChatColor.BOLD + "  ** LEVEL/EXP **\n\n" + ChatColor.BLACK + ChatColor.BOLD
                + "       LEVEL\n" + "          " + ChatColor.BLACK + gp.getLevel() + "\n\n" + ChatColor.BLACK + ChatColor.BOLD
                + "          XP" + "\n" + ChatColor.BLACK + "       " + (int) gp.getExperience() + "/"
                + gp.getEXPNeeded(gp.getLevel());
        page3_string = ChatColor.BLACK.toString() + ChatColor.BOLD.toString() + "+ " + gp.getStaticAttributeVal(Item.ArmorAttributeType.STRENGTH) + " Strength"
                + "\n" + ChatColor.BLACK.toString() + "   " + ChatColor.UNDERLINE.toString() + "'The Warrior'" + "\n"
//                + ChatColor.BLACK.toString() + "+" + df.format("STR * 0.03") + "% Armor" + "\n"
                + ChatColor.BLACK.toString() + "+" + df.format(gp.getAttributeBonusesFromStats().get(Item.ArmorAttributeType.getByNBTName("armor"))) + "% ARMOR" + "\n"
                + ChatColor.BLACK.toString() + "+" + df.format(gp.getAttributeBonusesFromStats().get(Item.ArmorAttributeType.getByNBTName("block"))) + "% BLOCK" + "\n"
                + ChatColor.BLACK.toString() + "+" + df.format(gp.getStaticAttributeVal(Item.ArmorAttributeType.STRENGTH) * 0.015) + "% AXE DMG" + "\n"
                + ChatColor.BLACK.toString() + "+" + df.format(gp.getStaticAttributeVal(Item.ArmorAttributeType.STRENGTH) * 0.023) + "% Polearm DMG" + "\n" + "\n"
                + ChatColor.BLACK.toString() + ChatColor.BOLD.toString() + "+ " + gp.getStaticAttributeVal(Item.ArmorAttributeType.DEXTERITY) + " Dexterity" + "\n"
                + ChatColor.BLACK.toString() + "   " + ChatColor.UNDERLINE.toString() + "'The Archer'" + "\n"
                + ChatColor.BLACK.toString() + "+" + df.format(gp.getAttributeBonusesFromStats().get(Item.ArmorAttributeType.getByNBTName("dps"))) + "% DPS" + "\n"
                + ChatColor.BLACK.toString() + "+" + df.format(gp.getAttributeBonusesFromStats().get(Item.ArmorAttributeType.getByNBTName("dodge"))) + "% DODGE" + "\n"
                + ChatColor.BLACK.toString() + "+" + df.format(gp.getAttributeBonusesFromStats().get(Item.WeaponAttributeType.getByNBTName("armorPenetration"))) + "% ARMOR PEN" + "\n"
                + ChatColor.BLACK.toString() + "+" + df.format(gp.getStaticAttributeVal(Item.ArmorAttributeType.DEXTERITY) * 0.015) + "% BOW DMG";
        page4_string = ChatColor.BLACK.toString() + ChatColor.BOLD.toString() + "+ " + gp.getStaticAttributeVal(Item.ArmorAttributeType.VITALITY) + " Vitality"
                + "\n" + ChatColor.BLACK.toString() + "   " + ChatColor.UNDERLINE.toString() + "'The Defender'" + "\n"
                + ChatColor.BLACK.toString() + "+" + df.format(gp.getStaticAttributeVal(Item.ArmorAttributeType.VITALITY) * 0.034) + "% Health" + "\n"
                + ChatColor.BLACK.toString() + "+" + df.format(gp.getStaticAttributeVal(Item.ArmorAttributeType.getByNBTName("healthRegen"))) + "   HP/s" + "\n"
                + ChatColor.BLACK.toString() + "+" + df.format(gp.getStaticAttributeVal(Item.ArmorAttributeType.VITALITY) * 0.01) + "% Sword DMG" + "\n" + "\n"
                + ChatColor.BLACK.toString() + ChatColor.BOLD.toString() + "+ " + gp.getStaticAttributeVal(Item.ArmorAttributeType.INTELLECT) + " Intellect" + "\n"
                + ChatColor.BLACK.toString() + "   " + ChatColor.UNDERLINE.toString() + "'The Mage'" + "\n"
                + ChatColor.BLACK.toString() + "+" + df.format(gp.getAttributeBonusesFromStats().get(Item.ArmorAttributeType.getByNBTName("energyRegen"))) + "% Energy" + "\n"
                + ChatColor.BLACK.toString() + "+" + df.format(gp.getAttributeBonusesFromStats().get(Item.WeaponAttributeType.getByNBTName("criticalHit"))) + "% Critical Hit" + "\n"
                + ChatColor.BLACK.toString() + "+" + df.format(gp.getStaticAttributeVal(Item.ArmorAttributeType.INTELLECT) * 0.02) + "% Staff DMG";

        String page5_string = (ChatColor.BLACK.toString() + "" + ChatColor.BOLD.toString() + ChatColor.UNDERLINE.toString() + "   Command Guide  " + new_line
                + ChatColor.BLACK.toString() + ChatColor.BOLD.toString() + "@<PLAYER> <MSG>" + "\n" + ChatColor.BLACK.toString() + "Sends a PM." + new_line
                + ChatColor.BLACK.toString() + ChatColor.BOLD.toString() + "/pinvite"
                + "\n"
                + ChatColor.BLACK.toString()
                + "Invite to party"
                + new_line + ChatColor.BLACK + ChatColor.BOLD.toString() + "/premove " + "\n" + ChatColor.BLACK.toString()
                + "Kick player from party" + new_line + ChatColor.BLACK + ChatColor.BOLD.toString() + "/pleave " + "\n"
                + ChatColor.BLACK.toString() + "Leave your party" + new_line + ChatColor.BLACK.toString() + ChatColor.BOLD.toString());


        String page6_string = (ChatColor.BLACK.toString() + ChatColor.BOLD.toString() + "/roll " + "\n" + ChatColor.BLACK.toString() + "Rolls a random number."
                + new_line + ChatColor.BLACK.toString() + ChatColor.BOLD.toString() + "/logout " + "\n" + ChatColor.BLACK.toString()
                + "Safetly logs out your character."
                + new_line + ChatColor.BLACK.toString() + ChatColor.BOLD.toString() + "/stats" + "\n" + ChatColor.BLACK.toString() + "Set Attributes"
                + new_line + ChatColor.BLACK.toString() + ChatColor.BOLD.toString() + "/toggles" + "\n" + ChatColor.BLACK.toString() + "Open Toggles Menu");


        ArrayList<String> friendsList = FriendHandler.getInstance().getFriendsList(p.getUniqueId());

        bm.setAuthor("King Bulwar");
        pages.add(page1_string);
        pages.add(page2_string);
        pages.add(page3_string);
        pages.add(page4_string);
        pages.add(page5_string);
        pages.add(page6_string);
        int count = 0;
        String nextLine = "\n";
        String friendsPage_string = (ChatColor.BLACK.toString() + "" + ChatColor.BOLD.toString() + ChatColor.UNDERLINE.toString() + "   Friends List  " + new_line);
        for (String uuidString : friendsList) {
            UUID uuid = UUID.fromString(uuidString);
            String playerName = DatabaseAPI.getInstance().getOfflineName(uuid);
            String shard = DatabaseAPI.getInstance().getFormattedShardName(uuid);
            boolean isOnline = Boolean.valueOf(String.valueOf(DatabaseAPI.getInstance().getData(EnumData.IS_PLAYING, uuid)));
            long currentTime = System.currentTimeMillis();
            long endTime = Long.valueOf(String.valueOf(DatabaseAPI.getInstance().getData(EnumData.LAST_LOGOUT, uuid)));
            long millis = currentTime - endTime;
            long second = (millis / 1000) % 60;
            long minute = (millis / (1000 * 60)) % 60;
            long hour = (millis / (1000 * 60 * 60)) % 24;
            String time = "";

            if (hour > 0) {
                time += hour + "h " + minute + "m " + second + "s ";
            } else if (minute > 0) {
                time += minute + "m " + second + "s ";

            } else {
                time += second + "s ";
            }
            if (hour > 99)
                time = "Many moons.";
            time += nextLine;

            if (playerName.length() >= 15)
                playerName = playerName.substring(0, 15);
            friendsPage_string += (isOnline ? ChatColor.GREEN + ChatColor.BOLD.toString() + "O" : ChatColor.DARK_RED + ChatColor.BOLD.toString() + "O") + ChatColor.BLACK + ChatColor.BOLD.toString() + " " + playerName + nextLine;
            friendsPage_string += (isOnline ? ChatColor.BLACK + "Shard: " + ChatColor.BOLD + shard + nextLine : ChatColor.BLACK + "Last On: " + time);


            count++;
            if (count == 5 || uuidString.equalsIgnoreCase(friendsList.get(friendsList.size() - 1))) {
                count = 0;
                pages.add(friendsPage_string);
                friendsPage_string = (ChatColor.BLACK.toString() + "" + ChatColor.BOLD.toString() + ChatColor.UNDERLINE.toString() + "   Friends List  " + new_line);
                if (uuidString.equalsIgnoreCase(friendsList.get(friendsList.size() - 1)))
                    break;
            }
        }


        bm.setPages(pages);
        stack.setItemMeta(bm);
        net.minecraft.server.v1_9_R2.ItemStack nms = CraftItemStack.asNMSCopy(stack);
        NBTTagCompound tag = nms.getTag() == null ? new NBTTagCompound() : nms.getTag();
        tag.setString("type", "important");
        tag.setString("journal", "true");
        tag.setString("subtype", "nondrop");
        nms.setTag(tag);
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
        net.minecraft.server.v1_9_R2.ItemStack nmsStack = CraftItemStack.asNMSCopy(skull);
        NBTTagCompound tag = nmsStack.getTag() == null ? new NBTTagCompound() : nmsStack.getTag();
        tag.set("type", new NBTTagString("important"));
        tag.set("usage", new NBTTagString("profile"));
        tag.set("destroy", new NBTTagString("yes"));
        nmsStack.setTag(tag);
        return CraftItemStack.asBukkitCopy(nmsStack);
    }

    public static ItemStack getPlayerHearthstone(Player player) {
        ItemStack stack = PlayerMenus.editItem(new ItemStack(Material.QUARTZ), ChatColor.GREEN + "Hearthstone", new String[]{
                ChatColor.DARK_GRAY + "Home location",
                "",
                ChatColor.GRAY + "Use: Returns you to " + ChatColor.YELLOW + TeleportAPI.getLocationFromDatabase(player.getUniqueId()),
                "",
                ChatColor.YELLOW + "Speak to an Innkeeper to change location."
        });
        net.minecraft.server.v1_9_R2.ItemStack nmsStack = CraftItemStack.asNMSCopy(stack);
        NBTTagCompound tag = nmsStack.getTag() == null ? new NBTTagCompound() : nmsStack.getTag();
        tag.set("type", new NBTTagString("important"));
        tag.set("usage", new NBTTagString("hearthstone"));
        tag.set("destroy", new NBTTagString("yes"));
        nmsStack.setTag(tag);
        return CraftItemStack.asBukkitCopy(nmsStack);
    }


    public static ItemStack getPlayerMountItem() {
        ItemStack stack = PlayerMenus.editItem(new ItemStack(Material.SADDLE), ChatColor.GREEN + "Mount", new String[]{
                ChatColor.DARK_GRAY + "Summons your active Mount.",
        });
        net.minecraft.server.v1_9_R2.ItemStack nmsStack = CraftItemStack.asNMSCopy(stack);
        NBTTagCompound tag = nmsStack.getTag() == null ? new NBTTagCompound() : nmsStack.getTag();
        tag.set("type", new NBTTagString("important"));
        tag.set("mount", new NBTTagString("true"));
        tag.set("usage", new NBTTagString("mount"));
        tag.set("destroy", new NBTTagString("yes"));
        nmsStack.setTag(tag);
        return CraftItemStack.asBukkitCopy(nmsStack);
    }

    public static ItemStack getPlayerPetItem() {
        ItemStack stack = PlayerMenus.editItem(new ItemStack(Material.NAME_TAG), ChatColor.GREEN + "Pet", new String[]{
                ChatColor.DARK_GRAY + "Summons your active Pet.",
        });
        net.minecraft.server.v1_9_R2.ItemStack nmsStack = CraftItemStack.asNMSCopy(stack);
        NBTTagCompound tag = nmsStack.getTag() == null ? new NBTTagCompound() : nmsStack.getTag();
        tag.set("type", new NBTTagString("important"));
        tag.set("pet", new NBTTagString("true"));
        tag.set("usage", new NBTTagString("pet"));
        tag.set("destroy", new NBTTagString("yes"));
        nmsStack.setTag(tag);
        return CraftItemStack.asBukkitCopy(nmsStack);
    }

    public static ItemStack getPlayerTrailItem() {
        ItemStack stack = PlayerMenus.editItem(new ItemStack(Material.EYE_OF_ENDER), ChatColor.GREEN + "Effect", new String[]{
                ChatColor.DARK_GRAY + "Equips your active Effect.",
        });
        net.minecraft.server.v1_9_R2.ItemStack nmsStack = CraftItemStack.asNMSCopy(stack);
        NBTTagCompound tag = nmsStack.getTag() == null ? new NBTTagCompound() : nmsStack.getTag();
        tag.set("type", new NBTTagString("important"));
        tag.set("trail", new NBTTagString("true"));
        tag.set("usage", new NBTTagString("trail"));
        tag.set("destroy", new NBTTagString("yes"));
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

    /**
     * @param stack
     * @return
     */
    public static boolean isEnchantScroll(ItemStack stack) {
        net.minecraft.server.v1_9_R2.ItemStack nms = CraftItemStack.asNMSCopy(stack);
        return stack.getType() == Material.EMPTY_MAP && nms.getTag() != null && nms.getTag().hasKey("type") && (nms.getTag().getString("type").equalsIgnoreCase("armorenchant") || nms.getTag().getString("type").equalsIgnoreCase("weaponenchant"));
    }

    /**
     * @param stack
     * @return
     */
    public static boolean isProtectScroll(ItemStack stack) {
        net.minecraft.server.v1_9_R2.ItemStack nms = CraftItemStack.asNMSCopy(stack);
        return stack.getType() == Material.EMPTY_MAP && nms.getTag() != null && nms.getTag().hasKey("type") && nms.getTag().getString("type").equalsIgnoreCase("protection");
    }

    public static ItemStack makeSoulBound(ItemStack is) {
        ItemMeta im = is.getItemMeta();
        List<String> lore = new ArrayList<>();
        if (im.hasLore()) {
            lore = im.getLore();
        }
        lore.add(ChatColor.DARK_RED.toString() + ChatColor.ITALIC + "Soulbound");
        im.setLore(lore);
        is.setItemMeta(im);
        net.minecraft.server.v1_9_R2.ItemStack nmsItem = CraftItemStack.asNMSCopy(is);
        if (nmsItem == null || nmsItem.getTag() == null) return is;
        NBTTagCompound nbtTagCompound = nmsItem.getTag();
        nbtTagCompound.setInt("soulbound", 1);
        nmsItem.setTag(nbtTagCompound);

        return CraftItemStack.asBukkitCopy(nmsItem);
    }
}
