package net.dungeonrealms.game.mechanic;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.game.database.DatabaseAPI;
import net.dungeonrealms.common.game.database.data.EnumData;
import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.game.anticheat.AntiDuplication;
import net.dungeonrealms.game.handler.FriendHandler;
import net.dungeonrealms.game.handler.HealthHandler;
import net.dungeonrealms.game.handler.KarmaHandler;
import net.dungeonrealms.game.mastery.GamePlayer;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.miscellaneous.ItemBuilder;
import net.dungeonrealms.game.miscellaneous.NBTWrapper;
import net.dungeonrealms.game.miscellaneous.RandomHelper;
import net.dungeonrealms.game.miscellaneous.ScrapTier;
import net.dungeonrealms.game.player.inventory.PlayerMenus;
import net.dungeonrealms.game.player.stats.PlayerStats;
import net.dungeonrealms.game.player.stats.StatsManager;
import net.dungeonrealms.game.profession.Fishing;
import net.dungeonrealms.game.profession.Mining;
import net.dungeonrealms.game.quests.Quest;
import net.dungeonrealms.game.quests.QuestPlayerData;
import net.dungeonrealms.game.quests.QuestPlayerData.QuestProgress;
import net.dungeonrealms.game.quests.Quests;
import net.dungeonrealms.game.world.entity.type.mounts.mule.MuleTier;
import net.dungeonrealms.game.world.item.Item;
import net.dungeonrealms.game.world.item.itemgenerator.ItemGenerator;
import net.dungeonrealms.game.world.item.repairing.RepairAPI;
import net.dungeonrealms.game.world.realms.RealmTier;
import net.dungeonrealms.game.world.realms.Realms;
import net.dungeonrealms.game.world.teleportation.TeleportAPI;
import net.dungeonrealms.game.world.teleportation.TeleportLocation;
import net.minecraft.server.v1_9_R2.*;

import org.apache.commons.lang.time.DurationFormatUtils;
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
import org.bukkit.metadata.FixedMetadataValue;
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
        return AntiDuplication.getInstance().applyAntiDupe(CraftItemStack.asBukkitCopy(nmsStack));
    }

    public static ItemStack addPartyMemberSoulboundBypass(ItemStack item, int timeframe, List<Player> partyMembers) {
        StringBuilder serialized = new StringBuilder();
        partyMembers.forEach((pl) -> serialized.append(pl.getName()).append(","));

        NBTWrapper wrapper = new NBTWrapper(item);

        wrapper.setLong("soulboundAllowed", System.currentTimeMillis() + (timeframe * 1000));
        wrapper.setString("soulboundBypass", serialized.toString());

        return wrapper.build();
    }

    public static ItemStack createRealmChest() {
        return createItem(Material.CHEST, ChatColor.GREEN + "Realm Chest", new String[]{ChatColor.GRAY + "This chest can only be placed in realms."});
    }

    public static ItemStack createOrbofAlteration() {
        ItemStack rawStack = createItem(Material.MAGMA_CREAM, ChatColor.LIGHT_PURPLE.toString() + "Orb of Alteration", new String[]{(ChatColor.GRAY.toString() + "Randomizes bonus stats of selected equipment")});
        net.minecraft.server.v1_9_R2.ItemStack nms = CraftItemStack.asNMSCopy(rawStack);
        nms.getTag().setString("type", "orb");
        return AntiDuplication.getInstance().applyAntiDupe(CraftItemStack.asBukkitCopy(nms));
    }

    public static ItemStack createEventOrbofAlteration() {
        if (!DungeonRealms.getInstance().isEventShard)
            return new ItemStack(Material.AIR);

        ItemStack rawStack = createItem(Material.MAGMA_CREAM, ChatColor.LIGHT_PURPLE.toString() + "Orb of Alteration", new String[]{
                ChatColor.GRAY.toString() + "Randomizes bonus stats of selected equipment",
                ChatColor.GRAY.toString() + "Event Item"
        });

        net.minecraft.server.v1_9_R2.ItemStack nms = CraftItemStack.asNMSCopy(rawStack);
        nms.getTag().setString("type", "orb");
        return CraftItemStack.asBukkitCopy(nms);
    }

    public static ItemStack createOrbofFlight(boolean applyAntiDupe) {
        ItemStack orbOfFlight = createItem(Material.FIREWORK_CHARGE, ChatColor.AQUA.toString() + "Orb of Flight",
                Arrays.asList((ChatColor.GRAY.toString() + "Enables " + ChatColor.UNDERLINE + "FLYING" + ChatColor.GRAY + " in realm for the owner "),
                        (ChatColor.GRAY.toString() + "and all builders for 30 minute(s)."),
                        (ChatColor.RED.toString() + ChatColor.BOLD.toString() + "REQ:" + ChatColor.RED.toString() + " Active Orb of Peace")).toArray(new String[3]));

        net.minecraft.server.v1_9_R2.ItemStack nms = CraftItemStack.asNMSCopy(orbOfFlight);
        nms.getTag().setString("orb", "flight");
        return applyAntiDupe ? AntiDuplication.getInstance().applyAntiDupe(CraftItemStack.asBukkitCopy(nms)) : CraftItemStack.asBukkitCopy(nms);
    }

    public static ItemStack createOrbofPeace(boolean applyAntiDupe) {
        ItemStack orbOfPeace = createItem(Material.ENDER_PEARL, ChatColor.LIGHT_PURPLE.toString() + "Orb of Peace",
                new String[]{(ChatColor.GRAY.toString() + "Set realm to " + ChatColor.UNDERLINE + "SAFE ZONE" + ChatColor.GRAY + " for 1 hour(s).")});
        net.minecraft.server.v1_9_R2.ItemStack nms = CraftItemStack.asNMSCopy(orbOfPeace);
        nms.getTag().setString("orb", "peace");
        return applyAntiDupe ? AntiDuplication.getInstance().applyAntiDupe(CraftItemStack.asBukkitCopy(nms)) : CraftItemStack.asBukkitCopy(nms);
    }

    public static ItemStack createWeaponEnchant(int tier) {
        String material = getWeaponMatString(tier);
        ItemStack rawStack = createItem(Material.EMPTY_MAP, ChatColor.WHITE.toString() + ChatColor.BOLD.toString() + "Scroll: " + GameAPI.getTierColor(tier) + "Enchant " + material + " Weapon", new String[]{ChatColor.RED + "+5% DMG", ChatColor.GRAY.toString() + ChatColor.ITALIC.toString() + "Weapon will VANISH if enchant above +3 FAILS."});
        net.minecraft.server.v1_9_R2.ItemStack nms = CraftItemStack.asNMSCopy(rawStack);
        nms.getTag().setString("type", "weaponenchant");
        nms.getTag().setInt("tier", tier);
        return AntiDuplication.getInstance().applyAntiDupe(CraftItemStack.asBukkitCopy(nms));
    }

    public static ItemStack createArmorEnchant(int tier) {
        String material = getArmorMatString(tier);
        ItemStack rawStack = createItem(Material.EMPTY_MAP, ChatColor.WHITE.toString() + ChatColor.BOLD.toString() + "Scroll: " + GameAPI.getTierColor(tier) + "Enchant " + material + " Armor", new String[]{ChatColor.RED + "+5% HP", ChatColor.RED + "+5% HP REGEN", ChatColor.GRAY.toString() + ChatColor.ITALIC + "    - OR -", ChatColor.RED + "+1% ENERGY REGEN", ChatColor.GRAY.toString() + ChatColor.ITALIC.toString() + "Armor will VANISH if enchant above +3 FAILS."});
        net.minecraft.server.v1_9_R2.ItemStack nms = CraftItemStack.asNMSCopy(rawStack);
        nms.getTag().setString("type", "armorenchant");
        nms.getTag().setInt("tier", tier);
        return AntiDuplication.getInstance().applyAntiDupe(CraftItemStack.asBukkitCopy(nms));
    }

    public static ItemStack createLootBuff(int duration, int bonusAmount) {
        String formattedTime = DurationFormatUtils.formatDurationWords(duration * 1000, true, true);
        ItemStack lootBuff = new ItemBuilder().setItem(Material.DIAMOND, (short) 0, ChatColor.GOLD.toString() + "Global Loot Buff", new
                String[]{ChatColor.GOLD.toString() + "Duration: " + ChatColor.GRAY + formattedTime, ChatColor.GOLD
                .toString() + "Uses: " + ChatColor.GRAY + "1", ChatColor.GRAY.toString() + ChatColor.ITALIC +
                "Increases all loot drop chances for everyone", ChatColor.GRAY.toString() + ChatColor.ITALIC + "by " +
                bonusAmount + "% across " + ChatColor.UNDERLINE + "ALL SHARDS."})
                .setNBTString("buff", "loot").setNBTInt("duration", duration).setNBTInt
                        ("bonusAmount", bonusAmount).setNBTString("description", "loot drop chances").build();
        // apply antidupe to make unstackable
        return AntiDuplication.getInstance().applyAntiDupe(GameAPI.makePermanentlyUntradeable(lootBuff));
    }

    public static ItemStack createProfessionBuff(int duration, int bonusAmount) {
        String formattedTime = DurationFormatUtils.formatDurationWords(duration * 1000, true, true);
        ItemStack professionBuff = new ItemBuilder().setItem(Material.GOLDEN_CARROT, (short) 0, ChatColor.GOLD.toString() + "Global Profession Buff", new
                String[]{ChatColor.GOLD.toString() + "Duration: " + ChatColor.GRAY + formattedTime, ChatColor.GOLD
                .toString() + "Uses: " + ChatColor.GRAY + "1", ChatColor.GRAY.toString() + ChatColor.ITALIC +
                "Increases all experience gained from professions for everyone", ChatColor.GRAY.toString() + ChatColor.ITALIC + "by " +
                bonusAmount + "% across " + ChatColor.UNDERLINE + "ALL SHARDS."})
                .setNBTString("buff", "profession").setNBTInt("duration", duration)
                .setNBTInt("bonusAmount", bonusAmount).setNBTString("description", "experience gained from " +
                        "professions").build();
        // apply antidupe to make unstackable
        return AntiDuplication.getInstance().applyAntiDupe(GameAPI.makePermanentlyUntradeable(professionBuff));
    }

    public static ItemStack createLevelBuff(int duration, int bonusAmount) {
        String formattedTime = DurationFormatUtils.formatDurationWords(duration * 1000, true, true);
        ItemStack levelBuff = new ItemBuilder().setItem(Material.EXP_BOTTLE, (short) 0, ChatColor.GOLD.toString() + "Global Level EXP Buff", new
                String[]{ChatColor.GOLD.toString() + "Duration: " + ChatColor.GRAY + formattedTime, ChatColor.GOLD
                .toString() + "Uses: " + ChatColor.GRAY + "1", ChatColor.GRAY.toString() + ChatColor.ITALIC +
                "Increases all experience gained from mobs for everyone", ChatColor.GRAY.toString() + ChatColor.ITALIC + "by " +
                bonusAmount + "% across " + ChatColor.UNDERLINE + "ALL SHARDS."})
                .setNBTString("buff", "level").setNBTInt("duration", duration).setNBTInt
                        ("bonusAmount", bonusAmount).setNBTString("description", "character experience gained").build();
        // apply antidupe to make unstackable
        return AntiDuplication.getInstance().applyAntiDupe(GameAPI.makePermanentlyUntradeable(levelBuff));
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
        return AntiDuplication.getInstance().applyAntiDupe(CraftItemStack.asBukkitCopy(nms));
    }

    /**
     * Creates a random Teleport book
     *
     * @return ItemStack
     * @since 1.0
     */
    public static ItemStack createRandomTeleportBook() {
    	List<TeleportLocation> teleportable = new ArrayList<TeleportLocation>();
    	for(TeleportLocation tl : TeleportLocation.values())
    		if(tl.canBeABook())
    			teleportable.add(tl);
    	return createTeleportBook(teleportable.get(Utils.randInt(0, teleportable.size() - 1)));
    }

    /**
     * Creates a Teleport book using location for a String
     *
     * @return ItemStack
     * @since 1.0
     */
    public static ItemStack createTeleportBook(TeleportLocation teleportLocation) {
    	ItemStack rawStack = new ItemStack(Material.BOOK);
        ItemMeta meta = rawStack.getItemMeta();
        meta.setDisplayName(ChatColor.WHITE.toString() + ChatColor.BOLD + "Teleport: " + ChatColor.WHITE + teleportLocation.getDisplayName());
        //TODO: Make this check chaotic, not just if it's deadpeaks?
        meta.setLore(Collections.singletonList(ChatColor.GRAY + "(Right-Click) Teleport to " + teleportLocation.getDisplayName()
        		+ ( teleportLocation == TeleportLocation.DEADPEAKS ? ChatColor.RED + " WARNING: CHAOTIC ZONE" : "") ));
        rawStack.setItemMeta(meta);
        net.minecraft.server.v1_9_R2.ItemStack nmsStack = CraftItemStack.asNMSCopy(rawStack);
        NBTTagCompound tag = nmsStack.getTag() == null ? new NBTTagCompound() : nmsStack.getTag();
        tag.set("type", new NBTTagString("teleport"));
        tag.set("usage", new NBTTagString(teleportLocation.name()));
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

        ScrapTier scrapTier = ScrapTier.getScrapTier(tier);

        if (scrapTier == null) return null;

        ItemStack rawStack = scrapTier.getRawStack();
        ItemMeta meta = rawStack.getItemMeta();
        meta.setDisplayName(scrapTier.getName() + " Scrap");
        meta.setLore(Collections.singletonList(ChatColor.GRAY + "Repairs 3% durability on " + scrapTier.getName() + ChatColor.GRAY + " equipment."));
        rawStack.setItemMeta(meta);
        net.minecraft.server.v1_9_R2.ItemStack nmsStack = CraftItemStack.asNMSCopy(rawStack);
        NBTTagCompound tag = nmsStack.getTag() == null ? new NBTTagCompound() : nmsStack.getTag();
        tag.set("type", new NBTTagString("scrap"));
        tag.setInt("itemTier", tier);
        nmsStack.setTag(tag);
        return CraftItemStack.asBukkitCopy(nmsStack);
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
        RealmTier realmTier = Realms.getRealmTier(uuid);
        
        return new ItemBuilder().setItem(createItem(Material.NETHER_STAR, ChatColor.LIGHT_PURPLE.toString() + ChatColor.BOLD + "Realm Portal Rune",
                new String[]{ChatColor.GRAY + "Tier: " + realmTier.getTier() + "/" + RealmTier.values().length + " [" + realmTier.getDimensions() + "x" + realmTier.getDimensions() + "x" + realmTier.getDimensions() + "]"
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
            return AntiDuplication.getInstance().applyAntiDupe(CraftItemStack.asBukkitCopy(nmsStack));
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
            return CraftItemStack.asBukkitCopy(nmsStack);
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
            return AntiDuplication.getInstance().applyAntiDupe(CraftItemStack.asBukkitCopy(nmsStack));
        }
        return null;
    }
    /**
     * Adds a starter kit to the player.
     *
     * @param player
     */
    public static void giveStarter(Player player){
    	giveStarter(player, false);
    }

    /**
     * Adds a starter kit to the player.
     *
     * @param player
     * @param isNew
     */
    public static void giveStarter(Player player, boolean isNew) {
        if (DungeonRealms.getInstance().isEventShard) {
            giveEventStarter(player);
            return;
        }

        player.getInventory().addItem(new ItemBuilder().setItem(ItemManager.createHealthPotion(1, false, false))
                .setNBTString("subtype", "starter").addLore(ChatColor.GRAY + "Untradeable").build());
        player.getInventory().addItem(new ItemBuilder().setItem(ItemManager.createHealthPotion(1, false, false))
                .setNBTString("subtype", "starter").addLore(ChatColor.GRAY + "Untradeable").build());
        player.getInventory().addItem(new ItemBuilder().setItem(ItemManager.createHealthPotion(1, false, false))
                .setNBTString("subtype", "starter").addLore(ChatColor.GRAY + "Untradeable").build());
        
        if(isNew)
        	player.getInventory().addItem(new ItemBuilder().setItem(new ItemStack(Material.BREAD, 3)).setNBTString
                ("subtype", "starter").addLore(ChatColor.GRAY + "Untradeable").build());

        if (Utils.randInt(0, 1) == 1) {
            ItemStack fixedSword = ItemGenerator.getNamedItem("training_sword");
            player.getInventory().addItem(new ItemBuilder().setItem(fixedSword).setNBTString("dataType", "starterSet").build());
        } else {
            ItemStack fixedAxe = ItemGenerator.getNamedItem("training_axe");
            player.getInventory().addItem(new ItemBuilder().setItem(fixedAxe).setNBTString("dataType", "starterSet").build());
        }

        ItemStack fixedHelmet = ItemGenerator.getNamedItem("traininghelm");
        player.getInventory().setHelmet(new ItemBuilder().setItem(fixedHelmet).setNBTString("dataType", "starterSet").build());

        ItemStack fixedChestplate = ItemGenerator.getNamedItem("trainingchest");
        player.getInventory().setChestplate(new ItemBuilder().setItem(fixedChestplate).setNBTString("dataType", "starterSet").build());

        ItemStack fixedLeggings = ItemGenerator.getNamedItem("traininglegs");
        player.getInventory().setLeggings(new ItemBuilder().setItem(fixedLeggings).setNBTString("dataType", "starterSet").build());

        ItemStack fixedBoots = ItemGenerator.getNamedItem("trainingboots");
        player.getInventory().setBoots(new ItemBuilder().setItem(fixedBoots).setNBTString("dataType", "starterSet").build());
        
        GameAPI.calculateAllAttributes(player);
        HealthHandler.getInstance().updatePlayerHP(player);
    }

    /**
     * Give the event starter kit to a player.
     *
     * @param player
     */
    public static void giveEventStarter(Player player) {
        // Sanity check to prevent accidentally issuing an event starter on a non-event shard.
        if (!DungeonRealms.getInstance().isEventShard)
            return;

        // Give the player their gear.
        player.getInventory().addItem(ItemGenerator.getNamedItem("eventpvpsword"));
        player.getInventory().setHelmet(ItemGenerator.getNamedItem("eventpvphelmet"));
        player.getInventory().setChestplate(ItemGenerator.getNamedItem("eventpvpchestplate"));
        player.getInventory().setLeggings(ItemGenerator.getNamedItem("eventpvpleggings"));
        player.getInventory().setBoots(ItemGenerator.getNamedItem("eventpvpboots"));

        // Add 128 event orbs.
        ItemStack orb = ItemManager.createEventOrbofAlteration();
        orb.setAmount(64);
        player.getInventory().addItem(orb, orb);

        // Add Scrap
        player.getInventory().addItem(ItemManager.createArmorScrap(5));

        // Add Food
        player.getInventory().addItem(new ItemStack(Material.GOLDEN_CARROT, 64));

        // Add T5 potions
        for (int i = 0; i < 25; i++)
            player.getInventory().addItem(ItemManager.createHealthPotion(5, false, false));
        
        GameAPI.calculateAllAttributes(player);
        HealthHandler.getInstance().updatePlayerHP(player);
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
            return AntiDuplication.getInstance().applyAntiDupe(CraftItemStack.asBukkitCopy(nmsStack));
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
        tag.setInt("untradeable", 1);
        nms.setTag(tag);
        return AntiDuplication.getInstance().applyAntiDupe(CraftItemStack.asBukkitCopy(nms));
    }

    public static ItemStack createGlobalMessenger() {
        ItemStack rawStack = new ItemStack(Material.FIREWORK);
        ItemMeta meta = rawStack.getItemMeta();
        meta.setDisplayName(ChatColor.GOLD + "Global Messenger");
        meta.setLore(Arrays.asList(ChatColor.GOLD + "Uses: " + ChatColor.GRAY + "1", ChatColor.GRAY + "Sends a message to all players on " + ChatColor.UNDERLINE + "ALL SHARDS."));
        rawStack.setItemMeta(meta);
        net.minecraft.server.v1_9_R2.ItemStack nmsStack = CraftItemStack.asNMSCopy(rawStack);
        NBTTagCompound tag = nmsStack.getTag() == null ? new NBTTagCompound() : nmsStack.getTag();
        tag.set("globalMessenger", new NBTTagString("true"));
        nmsStack.setTag(tag);

        return AntiDuplication.getInstance().applyAntiDupe(
        		GameAPI.makePermanentlyUntradeable(
        				CraftItemStack.asBukkitCopy(nmsStack)));
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
                name = ChatColor.WHITE + "Basic Fishing Rod";
                lore.add(ChatColor.GRAY.toString() + ChatColor.ITALIC + "A fishing rod made of wood and thread.");
                break;
            case 2:
                name = ChatColor.GREEN.toString() + "Advanced Fishing Rod";
                lore.add(ChatColor.GRAY.toString() + ChatColor.ITALIC + "A fishing rod made of oak wood and thread.");
                break;
            case 3:
                name = ChatColor.AQUA.toString() + "Expert Fishing Rod";
                lore.add(ChatColor.GRAY.toString() + ChatColor.ITALIC + "A fishing rod made of ancient oak wood and spider silk.");
                break;
            case 4:
                name = ChatColor.LIGHT_PURPLE.toString() + "Supreme Fishing Rod";
                lore.add(ChatColor.GRAY.toString() + ChatColor.ITALIC + "A fishing rod made of jungle bamboo and spider silk.");
                break;
            case 5:
                name = ChatColor.YELLOW.toString() + "Master Fishing Rod";
                lore.add(ChatColor.GRAY.toString() + ChatColor.ITALIC + "A fishing rod made of rich mahogany and enchanted silk.");
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
        return AntiDuplication.getInstance().applyAntiDupe(CraftItemStack.asBukkitCopy(nmsStack));
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
        String questPage_string;
        String page2_string;
        String page3_string;
        String page4_string;
        String new_line = "\n" + ChatColor.BLACK.toString() + " " + "\n";
        GamePlayer gp = GameAPI.getGamePlayer(p);
        if (gp == null)
            return stack;
        KarmaHandler.EnumPlayerAlignments playerAlignment = gp.getPlayerAlignment();
        String pretty_align = (playerAlignment == KarmaHandler.EnumPlayerAlignments.LAWFUL ? ChatColor.DARK_GREEN.toString() :
                playerAlignment.getAlignmentColor()) + ChatColor.UNDERLINE.toString() + playerAlignment.name();
        DecimalFormat df = new DecimalFormat("#.##");
        PlayerStats stats = StatsManager.getPlayerStats(p);

        PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(p);

        if (pretty_align.contains("CHAOTIC") || pretty_align.contains("NEUTRAL")) {
            String time = String.valueOf(KarmaHandler.getInstance().getAlignmentTime(p));
            page1_string = ChatColor.BLACK.toString() + "" + ChatColor.BOLD.toString() + ChatColor.UNDERLINE.toString() + "  Your Character  \n\n"
                    + ChatColor.BLACK.toString() + ChatColor.BOLD.toString() + "Alignment: " + pretty_align + "\n" + playerAlignment.getAlignmentColor().toString() + ChatColor.BOLD + time + "s.." + new_line
                    + ChatColor.BLACK.toString() + playerAlignment.description + new_line + ChatColor.BLACK.toString() + "   " + gp.getPlayerCurrentHP()
                    + " / " + gp.getPlayerMaxHP() + "" + ChatColor.BOLD.toString() + " HP" + "\n" + ChatColor.BLACK.toString()
                    + "   " + stats.getDPS() + "% " + ChatColor.BOLD.toString() + "DPS" + "\n" + ChatColor.BLACK.toString()
                    + "   " + (HealthHandler.getInstance().getPlayerHPRegenLive(p)) + " " + ChatColor.BOLD.toString() + "HP/s" + "\n" + ChatColor.BLACK.toString()
                    + "   " + gp.getStaticAttributeVal(Item.ArmorAttributeType.ENERGY_REGEN) + "% " + ChatColor.BOLD.toString() + "Energy/s" + "\n" + ChatColor.BLACK.toString()
                    + "   " + wrapper.getEcash() + ChatColor.BOLD.toString() + " E-CASH" + "\n" + ChatColor.BLACK.toString()
                    + "   " + gp.getPlayerGemFind() + ChatColor.BOLD.toString() + " GEM FIND" + "\n" + ChatColor.BLACK.toString()
                    + "   " + gp.getPlayerItemFind() + ChatColor.BOLD.toString() + " ITEM FIND";
        } else {
            page1_string = ChatColor.BLACK.toString() + "" + ChatColor.BOLD.toString() + ChatColor.UNDERLINE.toString() + "  Your Character  " + "\n\n"
                    + ChatColor.BLACK.toString() + ChatColor.BOLD.toString() + "Alignment: " + pretty_align + new_line
                    + ChatColor.BLACK.toString() + playerAlignment.description + new_line + ChatColor.BLACK.toString() + "   " + gp.getPlayerCurrentHP()
                    + " / " + gp.getPlayerMaxHP() + "" + ChatColor.BOLD.toString() + " HP" + "\n" + ChatColor.BLACK.toString()
                    + "   " + stats.getDPS() + "% " + ChatColor.BOLD.toString() + "DPS" + "\n" + ChatColor.BLACK.toString()
                    + "   " + (HealthHandler.getInstance().getPlayerHPRegenLive(p) + stats.getHPRegen()) + " " + ChatColor.BOLD.toString() + "HP/s" + "\n" + ChatColor.BLACK.toString()
                    + "   " + gp.getStaticAttributeVal(Item.ArmorAttributeType.ENERGY_REGEN) + "% " + ChatColor.BOLD.toString() + "Energy/s" + "\n" + ChatColor.BLACK.toString()
                    + "   " + wrapper.getEcash() + ChatColor.BOLD.toString() + " E-CASH" + "\n" + ChatColor.BLACK.toString()
                    + "   " + gp.getPlayerGemFind() + ChatColor.BOLD.toString() + " GEM FIND" + "\n" + ChatColor.BLACK.toString()
                    + "   " + gp.getPlayerItemFind() + ChatColor.BOLD.toString() + " ITEM FIND";
        }
        
        questPage_string = ChatColor.BLACK + "" + ChatColor.BOLD + ChatColor.UNDERLINE + "  Quest Progress  \n\n";
        int quests = 0;
        
        if(Quests.isEnabled()){
        	QuestPlayerData data = Quests.getInstance().playerDataMap.get(p);
        	if(data != null){
        		//TODO: Multi page support. (Does vanilla do this automatically?)
        		for(Quest doing : data.getCurrentQuests()){
        			quests++;
        			QuestProgress qp = data.getQuestProgress(doing);
        			
        			questPage_string += ChatColor.BLACK + doing.getQuestName() + "> " + ChatColor.GREEN;
        			if(qp.getCurrentStage().getPrevious() == null){
        				questPage_string += "Start by talking to " + qp.getCurrentStage().getNPC().getName();
        			}else{
        				questPage_string += qp.getCurrentStage().getPrevious().getObjective().getTaskDescription(p, qp.getCurrentStage());
        			}
        			questPage_string += "\n\n";
        		}
        	}
        }
        
        page2_string = ChatColor.DARK_AQUA.toString() + ChatColor.BOLD + "  ** LEVEL/EXP **\n\n" + ChatColor.BLACK + ChatColor.BOLD
                + "       LEVEL\n" + "          " + ChatColor.BLACK + gp.getLevel() + "\n\n" + ChatColor.BLACK + ChatColor.BOLD
                + "          XP" + "\n" + ChatColor.BLACK + "       " + gp.getExperience() + "/"
                + gp.getEXPNeeded(gp.getLevel());


        String portalShardPage = ChatColor.BLACK.toString() + ChatColor.BOLD.toString() + "Portal Key Shards" + "\n" + ChatColor.BLACK.toString()
                + ChatColor.ITALIC.toString()
                + "A sharded fragment from the great portal of Maltai that may be exchanged at the Dungeoneer for epic equipment." + new_line
                + ChatColor.DARK_GRAY.toString() + "Portal Shards: " + ChatColor.BLACK + wrapper.getPortalShardsT1() + "\n"
                + ChatColor.GREEN.toString() + "Portal Shards: " + ChatColor.BLACK + wrapper.getPortalShardsT2() + "\n"
                + ChatColor.AQUA.toString() + "Portal Shards: " + ChatColor.BLACK + wrapper.getPortalShardsT3() + "\n"
                + ChatColor.LIGHT_PURPLE.toString() + "Portal Shards: " + ChatColor.BLACK + wrapper.getPortalShardsT4()
                + "\n" + ChatColor.GOLD.toString() + "Portal Shards: " + ChatColor.BLACK + wrapper.getPortalShardsT5();

        page3_string = (ChatColor.BLACK.toString() + "" + ChatColor.BOLD.toString() + ChatColor.UNDERLINE.toString() + "   Command Guide  " + new_line
                + ChatColor.BLACK.toString() + ChatColor.BOLD.toString() + "/msg" + "\n" + ChatColor.BLACK.toString() + "Sends a PM." + new_line
                + ChatColor.BLACK.toString() + ChatColor.BOLD.toString() + "/ask" + "\n" + ChatColor.BLACK.toString() + "Ask any questions." + new_line
                + ChatColor.BLACK.toString() + ChatColor.BOLD.toString() + "/shard" + "\n" + ChatColor.BLACK.toString() + "Switch your current session." + new_line
                + ChatColor.BLACK.toString() + ChatColor.BOLD.toString() + "/pinvite"
                + "\n"
                + ChatColor.BLACK.toString()
                + "Invite to party");


        page4_string = (ChatColor.BLACK + ChatColor.BOLD.toString() + "/premove " + "\n" + ChatColor.BLACK.toString()
                + "Kick player from party" + new_line + ChatColor.BLACK + ChatColor.BOLD.toString() + "/pleave " + "\n"
                + ChatColor.BLACK.toString() + "Leave your party"
                + new_line + ChatColor.BLACK.toString() + ChatColor.BOLD.toString() + "/roll "
                + "\n" + ChatColor.BLACK.toString() + "Rolls a random number."
        );


        String page5_string = (ChatColor.BLACK.toString() + ChatColor.BOLD.toString() + "/stats" + "\n" + ChatColor.BLACK.toString() + "Set Attributes"
                + new_line + ChatColor.BLACK.toString() + ChatColor.BOLD.toString() + "/toggles" + "\n" + ChatColor.BLACK.toString() + "Open Toggles Menu");


        bm.setAuthor("King Bulwar");
        pages.add(page1_string);
        if(quests > 0)
        	pages.add(questPage_string);
        pages.add(page2_string);
        pages.add(portalShardPage);
        pages.add(page3_string);
        pages.add(page4_string);
        pages.add(page5_string);

//        int count = 0;
//        String nextLine = "\n";
////        String friendsPage_string = (ChatColor.BLACK.toString() + "" + ChatColor.BOLD.toString() + ChatColor.UNDERLINE.toString() + "   Friends List  " + new_line);
////
////        for (String uuidString : friendsList) {
////            UUID uuid = UUID.fromString(uuidString);
////            String playerName = DatabaseAPI.getInstance().getOfflineName(uuid);
////            String shard = DatabaseAPI.getInstance().getFormattedShardName(uuid);
////            boolean isOnline = Boolean.valueOf(String.valueOf(DatabaseAPI.getInstance().getData(EnumData.IS_PLAYING, uuid)));
////            long currentTime = System.currentTimeMillis();
////            long endTime = Long.valueOf(String.valueOf(DatabaseAPI.getInstance().getData(EnumData.LAST_LOGOUT, uuid)));
////            long millis = currentTime - endTime;
////            long second = (millis / 1000) % 60;
////            long minute = (millis / (1000 * 60)) % 60;
////            long hour = (millis / (1000 * 60 * 60)) % 24;
////            String time = "";
////
////            if (hour > 0) {
////                time += hour + "h " + minute + "m " + second + "s ";
////            } else if (minute > 0) {
////                time += minute + "m " + second + "s ";
////
////            } else {
////                time += second + "s ";
////            }
////            if (hour > 99)
////                time = "Many moons.";
////            time += nextLine;
////
////            if (playerName.length() >= 15)
////                playerName = playerName.substring(0, 15);
////            friendsPage_string += (isOnline ? ChatColor.GREEN + ChatColor.BOLD.toString() + "O" : ChatColor.DARK_RED + ChatColor.BOLD.toString() + "O") + ChatColor.BLACK + ChatColor.BOLD.toString() + " " + playerName + nextLine;
////            friendsPage_string += (isOnline ? ChatColor.BLACK + "Shard: " + ChatColor.BOLD + shard + nextLine : ChatColor.BLACK + "Last On: " + time);
////
////
////            count++;
////            if (count == 5 || uuidString.equalsIgnoreCase(friendsList.get(friendsList.size() - 1))) {
////                count = 0;
////                pages.add(friendsPage_string);
////                friendsPage_string = (ChatColor.BLACK.toString() + "" + ChatColor.BOLD.toString() + ChatColor.UNDERLINE.toString() + "   Friends List  " + new_line);
////                if (uuidString.equalsIgnoreCase(friendsList.get(friendsList.size() - 1)))
////                    break;
////            }
////        }


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
    public static boolean isTeleportBook(ItemStack stack) {
        net.minecraft.server.v1_9_R2.ItemStack nms = CraftItemStack.asNMSCopy(stack);
        return nms.getTag() != null && nms.getTag().hasKey("type") && (nms.getTag().getString("type").equalsIgnoreCase("teleport"));
    }


    /**
     * @param stack
     * @return
     */
    public static boolean isProtectScroll(ItemStack stack) {
        net.minecraft.server.v1_9_R2.ItemStack nms = CraftItemStack.asNMSCopy(stack);
        return stack.getType() == Material.EMPTY_MAP && nms.getTag() != null && nms.getTag().hasKey("type") && nms.getTag().getString("type").equalsIgnoreCase("protection");
    }

    /**
     * @param stack
     * @return
     */
    public static boolean isScrap(ItemStack stack) {
        net.minecraft.server.v1_9_R2.ItemStack nms = CraftItemStack.asNMSCopy(stack);
        return nms.getTag() != null && nms.getTag().hasKey("type") && nms.getTag().getString("type").equalsIgnoreCase("scrap");
    }


    /**
     * @param stack
     * @return
     */
    public static boolean isPotion(ItemStack stack) {
        net.minecraft.server.v1_9_R2.ItemStack nms = CraftItemStack.asNMSCopy(stack);
        return nms.getTag() != null && nms.getTag().hasKey("type") && nms.getTag().getString("type").toLowerCase().contains("potion");
    }

    public static ItemStack makeSoulBound(ItemStack is) {
        ItemMeta im = is.getItemMeta();
        List<String> lore = new ArrayList<>();
        if (im.hasLore()) {
            lore = im.getLore();
        }
        boolean contained = false;
        for (String line : lore) {
            if (line.contains("Soulbound") && line.contains(ChatColor.DARK_RED.toString())) {
                contained = true;
            }
        }

        //Just to avoid the ugly looking drops.
        if (!contained)
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

    public static void whitelistItemDrop(Player player, org.bukkit.entity.Item item) {
        if (player == null) return;
        item.setMetadata("whitelist", new FixedMetadataValue(DungeonRealms.getInstance(), player.getName()));
    }
}
