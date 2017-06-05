package net.dungeonrealms.game.mechanic;

import com.google.common.collect.Lists;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.game.handler.HealthHandler;
import net.dungeonrealms.game.handler.KarmaHandler;
import net.dungeonrealms.game.handler.KarmaHandler.EnumPlayerAlignments;
import net.dungeonrealms.game.item.PersistentItem;
import net.dungeonrealms.game.item.items.core.*;
import net.dungeonrealms.game.item.items.functional.PotionItem;
import net.dungeonrealms.game.mastery.MetadataUtils.Metadata;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.mechanic.data.PotionTier;
import net.dungeonrealms.game.mechanic.data.ShardTier;
import net.dungeonrealms.game.quests.Quest;
import net.dungeonrealms.game.quests.QuestPlayerData;
import net.dungeonrealms.game.quests.QuestPlayerData.QuestProgress;
import net.dungeonrealms.game.quests.Quests;
import net.dungeonrealms.game.world.item.Item.ArmorAttributeType;
import net.dungeonrealms.game.world.item.itemgenerator.ItemGenerator;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * ItemManager - Contains basic item utils.
 * <p>
 * Redone by Kneesnap in early April 2017.
 */
public class ItemManager {

    /**
     * Adds a starter kit to the player.
     *
     * @param player
     */
    public static void giveStarter(Player player) {
        giveStarter(player, false);
    }

    private static DecimalFormat df = new DecimalFormat("#.##");

    /**
     * Gives a starter kit to the player.
     *
     * @param player
     * @param isNew
     */
    public static void giveStarter(Player player, boolean isNew) {

        EntityEquipment e = player.getEquipment();
        Map<EquipmentSlot, ItemStack> starter = ItemGenerator.getEliteGear("starter");
        for (EquipmentSlot eq : starter.keySet()) {
            if (eq == EquipmentSlot.HAND)
                continue;
            ItemStack c = GameAPI.getItem(e, eq);
            if (c == null || c.getType() == Material.AIR)
                GameAPI.setItem(player, eq, starter.get(eq));
        }
        player.getInventory().addItem(starter.get(EquipmentSlot.HAND));


        for (int i = 0; i < 3; i++)
            player.getInventory().addItem(new PotionItem(PotionTier.TIER_1).setUntradeable(true).generateItem());

        if (isNew)
            player.getInventory().addItem(new VanillaItem(new ItemStack(Material.BREAD, 3)).setUntradeable(true).generateItem());

        PlayerWrapper.getWrapper(player).calculateAllAttributes();
    }


    /**
     * Creates a character journal for a player.
     * This should ONLY be called when force opening this book for the player.
     * In other words, use new ItemPlayerJournal(Player).generateItem() instead of this.
     * We can save space / cpu power by only generating this when the player opens it.
     */
    public static ItemStack createCharacterJournal(Player p) {
        ItemStack stack = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta bm = (BookMeta) stack.getItemMeta();
        List<String> pages = new ArrayList<>();
        String page1_string;
        String questPage_string;
        String page2_string;
        String page3_string;
        String page4_string;
        String stat_page_string;
        String stat_page2_string;
        String new_line = "\n" + ChatColor.BLACK.toString() + " " + "\n";
        PlayerWrapper pw = PlayerWrapper.getWrapper(p);

        EnumPlayerAlignments playerAlignment = pw.getAlignment();
        String pretty_align = (playerAlignment == KarmaHandler.EnumPlayerAlignments.LAWFUL ? ChatColor.DARK_GREEN.toString() :
                playerAlignment.getAlignmentColor()) + ChatColor.UNDERLINE.toString() + playerAlignment.name();

        if (TutorialIsland.onTutorialIsland(p.getLocation())) {
            page1_string = ChatColor.BLACK.toString() + "" + ChatColor.BOLD.toString() + ChatColor.UNDERLINE.toString() + "  Your Character  \n\n"
                    + ChatColor.BLACK.toString() + ChatColor.BOLD.toString() + "/skip to skip the tutorial!" + new_line;
        } else if (playerAlignment != EnumPlayerAlignments.LAWFUL) {
            String time = String.valueOf(pw.getAlignmentTime());
            page1_string = ChatColor.BLACK.toString() + "" + ChatColor.BOLD.toString() + ChatColor.UNDERLINE.toString() + "  Your Character  \n\n"
                    + ChatColor.BLACK.toString() + ChatColor.BOLD.toString() + "Alignment: " + pretty_align + "\n" + playerAlignment.getAlignmentColor().toString() + ChatColor.BOLD + time + "s.." + new_line;
        } else {
            page1_string = ChatColor.BLACK.toString() + "" + ChatColor.BOLD.toString() + ChatColor.UNDERLINE.toString() + "  Your Character  " + "\n\n"
                    + ChatColor.BLACK.toString() + ChatColor.BOLD.toString() + "Alignment: " + pretty_align + new_line;
        }

        page1_string += ""
                + ChatColor.BLACK.toString() + playerAlignment.getDescription() + new_line + ChatColor.BLACK + "   " + HealthHandler.getHP(p)
                + " / " + HealthHandler.getMaxHP(p) + "" + ChatColor.BOLD + " HP" + "\n" + ChatColor.BLACK
                + "   " + pw.getAttribute(ArmorAttributeType.DAMAGE) + "% " + ChatColor.BOLD + "DPS" + "\n" + ChatColor.BLACK
                + "   " + (HealthHandler.getRegen(p) + pw.getPlayerStats().getHPRegen()) + " " + ChatColor.BOLD + "HP/s" + "\n" + ChatColor.BLACK
                + "   " + pw.getAttributes().getAttribute(ArmorAttributeType.ENERGY_REGEN).toString() + "% " + ChatColor.BOLD.toString() + "Energy/s" + "\n" + ChatColor.BLACK
                + "   " + pw.getEcash() + ChatColor.BOLD + " E-CASH" + "\n" + ChatColor.BLACK
                + "   " + pw.getAttributes().getAttribute(ArmorAttributeType.GEM_FIND).getValue() + ChatColor.BOLD + " GEM FIND" + "\n" + ChatColor.BLACK
                + "   " + pw.getAttributes().getAttribute(ArmorAttributeType.ITEM_FIND).getValue() + ChatColor.BOLD + " ITEM FIND";

        /*page1_string = ChatColor.BLACK.toString() + "" + ChatColor.BOLD.toString() + ChatColor.UNDERLINE.toString() + "  Your Character" + "   " + "\n"
                + ChatColor.BLACK.toString() + ChatColor.BOLD.toString() + "Alignment: " + pretty_align + align_expire_message + "\n"
                + ChatColor.BLACK.toString() + align_descrip + new_line + ChatColor.BLACK.toString() + "   " + HealthMechanics.getPlayerHP(p.getName())
                + " / " + HealthMechanics.health_data.get(p.getName()) + "" + ChatColor.BOLD.toString() + " HP" + "\n" + ChatColor.BLACK.toString()
                + "   " + ItemMechanics.armor_data.get(p.getName()).get(0) + " - " + ItemMechanics.armor_data.get(p.getName()).get(1) + "% "
                + ChatColor.BOLD.toString() + "Armor" + "\n" + ChatColor.BLACK.toString() + "   " + ItemMechanics.dmg_data.get(p.getName()).get(0)
                + " - " + ItemMechanics.dmg_data.get(p.getName()).get(1) + "% " + ChatColor.BOLD.toString() + "DPS" + "\n" + ChatColor.BLACK.toString()
                + "   " + (HealthMechanics.health_regen_data.get((p.getName())) + 5) + " " + ChatColor.BOLD.toString() + "HP/s" + "\n"
                + ChatColor.BLACK.toString() + "   " + df.format((((double) FatigueMechanics.energy_regen_data.get((p.getName()))) * 100.0D) + 90.0D)
                + "% " + ChatColor.BOLD.toString() + "Energy" + "\n" + ChatColor.BLACK.toString() + "   " + gold_find + "x "
                + ChatColor.BOLD.toString() + "Gem Find" + "\n" + ChatColor.BLACK.toString() + "   " + item_find + "x " + ChatColor.BOLD.toString()
                + "Item Find" + money_space + ChatColor.BLACK.toString() + "" + Hive.player_ecash.get(p.getName()) + " " + ChatColor.BOLD.toString()
                + "E-CASH";
        page2_string = ChatColor.DARK_AQUA.toString() + ChatColor.BOLD + "  ** LEVEL/EXP **\n\n" + ChatColor.BLACK + ChatColor.BOLD
                + "       LEVEL\n\n" + "         " + ChatColor.BLACK + LevelMechanics.getPlayerLevel(p) + "\n\n" + ChatColor.BLACK + ChatColor.BOLD
                + "          XP" + "\n" + ChatColor.BLACK + "       " + LevelMechanics.getPlayerData(p).getXP() + " / "
                + LevelMechanics.getPlayerData(p).getEXPNeeded(LevelMechanics.getPlayerLevel(p));
        page3_string = "\n" + ChatColor.BLACK.toString() + "   " + ItemMechanics.fire_res_data.get(p.getName()) + "% " + ChatColor.BOLD.toString()
                + "Fire Resist" + "\n" + ChatColor.BLACK.toString() + "   " + ItemMechanics.ice_res_data.get(p.getName()) + "% "
                + ChatColor.BOLD.toString() + "Ice Resist" + "\n" + ChatColor.BLACK.toString() + "   " + ItemMechanics.poison_res_data.get(p.getName())
                + "% " + ChatColor.BOLD.toString() + "Poison Resist" + new_line + ChatColor.BLACK.toString() + "Monsters that deal" + "\n"
                + "elemental damage will" + "\n" + "ignore 80% of your" + "\n" + "ARMOR." + new_line + ChatColor.BLACK.toString()
                + "Fire, Ice, and Poison" + "\n" + "resistances will take" + "\n" + "the place of your" + "\n" + "ARMOR vs. elements.";

        int str_val = ItemMechanics.str_data.get(p.getName());
        int dex_val = ItemMechanics.dex_data.get(p.getName());
        int vit_val = ItemMechanics.vit_data.get(p.getName());
        int int_val = ItemMechanics.int_data.get(p.getName());

        page4_string = ChatColor.BLACK.toString() + ChatColor.BOLD.toString() + "+ " + str_val + " Strength"
                + "\n" + ChatColor.BLACK.toString() + "   " + ChatColor.UNDERLINE.toString() + "'The Warrior'"
                + ChatColor.BLACK.toString() + "+" + df.format(str_val * 0.03) + "% Armor" + "\n"
                + ChatColor.BLACK.toString() + "+" + df.format(str_val * 0.017) + "% Block" + "\n"
                + ChatColor.BLACK.toString() + "+" + df.format(str_val * 0.015) + "% Axe DMG" + "\n"
                + ChatColor.BLACK.toString() + "+" + df.format(str_val * 0.023) + "% Polearm DMG" + "\n" + "\n"
                + ChatColor.BLACK.toString() + ChatColor.BOLD.toString() + "+ " + dex_val + " Dexterity" + "\n"
                + ChatColor.BLACK.toString() + "   " + ChatColor.UNDERLINE.toString() + "'The Archer'"
                + ChatColor.BLACK.toString() + "+" + df.format(dex_val * 0.03) + "% Dodge" + "\n"
                + ChatColor.BLACK.toString() + "+" + df.format(dex_val * 0.015) + "% Bow DMG" + "\n"
                + ChatColor.BLACK.toString() + "+" + df.format(dex_val * 0.005) + "% Critical Hit" + "\n"
                + ChatColor.BLACK.toString() + "+" + df.format(dex_val * 0.009) + "% Armor Pen.";

        page5_string = ChatColor.BLACK.toString() + ChatColor.BOLD.toString() + "+ " + vit_val + " Vitality"
                + "\n" + ChatColor.BLACK.toString() + "   " + ChatColor.UNDERLINE.toString() + "'The Defender'"
                + ChatColor.BLACK.toString() + "+" + df.format(vit_val * 0.034) + "% Health" + "\n"
                + ChatColor.BLACK.toString() + "+" + df.format(vit_val * 0.3) + "   HP/s" + "\n"
                + ChatColor.BLACK.toString() + "+" + df.format(vit_val * 0.04) + "% Ele Resist" + "\n"
                + ChatColor.BLACK.toString() + "+" + df.format(vit_val * 0.01) + "% Sword DMG" + "\n" + "\n"
                + ChatColor.BLACK.toString() + ChatColor.BOLD.toString() + "+ " + int_val + " Intellect" + "\n"
                + ChatColor.BLACK.toString() + "   " + ChatColor.UNDERLINE.toString() + "'The Mage'" + new_line
                + ChatColor.BLACK.toString() + "+" + df.format(int_val * 0.015) + "% Energy" + "\n"
                + ChatColor.BLACK.toString() + "+" + df.format(int_val * 0.05) + "% Ele Damage" + "\n"
                + ChatColor.BLACK.toString() + "+" + df.format(int_val * 0.025) + "% Critical Hit" + "\n"
                + ChatColor.BLACK.toString() + "+" + df.format(int_val * 0.02) + "% Staff DMG";*/

        questPage_string = ChatColor.BLACK + "" + ChatColor.BOLD + ChatColor.UNDERLINE + "  Quest Progress  \n\n";
        int quests = 0;

        if (Quests.isEnabled()) {
            QuestPlayerData data = Quests.getInstance().playerDataMap.get(p);
            if (data != null) {
                //TODO: Multi page support. (Does vanilla do this automatically?)
                for (Quest doing : data.getCurrentQuests()) {
                    quests++;
                    QuestProgress qp = data.getQuestProgress(doing);

                    questPage_string += ChatColor.BLACK + doing.getQuestName() + "> " + ChatColor.GREEN;
                    if (qp.getCurrentStage().getPrevious() == null) {
                        questPage_string += "Start by talking to " + qp.getCurrentStage().getNPC().getName();
                    } else {
                        questPage_string += qp.getCurrentStage().getPrevious().getObjective().getTaskDescription(p, qp.getCurrentStage());
                    }
                    questPage_string += "\n\n";
                }
            }
        }

        page2_string = ChatColor.DARK_AQUA.toString() + ChatColor.BOLD + "  ** LEVEL/EXP **\n\n" + ChatColor.BLACK + ChatColor.BOLD
                + "       LEVEL\n" + "          " + ChatColor.BLACK + pw.getLevel() + "\n\n" + ChatColor.BLACK + ChatColor.BOLD
                + "          XP" + "\n" + ChatColor.BLACK + "       " + pw.getExperience() + "/" + pw.getEXPNeeded();


        int str_val = pw.getAttributes().getAttribute(ArmorAttributeType.STRENGTH).getValue();
        int dex_val = pw.getAttributes().getAttribute(ArmorAttributeType.DEXTERITY).getValue();

        int vit_val = pw.getAttributes().getAttribute(ArmorAttributeType.VITALITY).getValue();
        int int_val = pw.getAttributes().getAttribute(ArmorAttributeType.INTELLECT).getValue();
        //stat page

        stat_page_string = ChatColor.BLACK.toString() + ChatColor.BOLD.toString() + "+ " + str_val + " Strength"
                + "\n" + ChatColor.BLACK.toString() + "   " + ChatColor.UNDERLINE.toString() + "'The Warrior'\n"
                + ChatColor.BLACK.toString() + "+" + df.format(str_val * 0.03) + "% Armor" + "\n"
                + ChatColor.BLACK.toString() + "+" + df.format(str_val * 0.017) + "% Block" + "\n"
                + ChatColor.BLACK.toString() + "+" + df.format(str_val * 0.015) + "% Axe DMG" + "\n"
                + ChatColor.BLACK.toString() + "+" + df.format(str_val * 0.023) + "% Polearm DMG" + "\n" + "\n"
                + ChatColor.BLACK.toString() + ChatColor.BOLD.toString() + "+ " + dex_val + " Dexterity" + "\n"
                + ChatColor.BLACK.toString() + "   " + ChatColor.UNDERLINE.toString() + "'The Archer'\n"
                + ChatColor.BLACK.toString() + "+" + df.format(dex_val * 0.017f) + "% Dodge" + "\n"
                + ChatColor.BLACK.toString() + "+" + df.format(dex_val * 0.015) + "% Bow DMG" + "\n"
                + ChatColor.BLACK.toString() + "+" + df.format(dex_val * 0.03) + "% DPS" + "\n"
                + ChatColor.BLACK.toString() + "+" + df.format(dex_val * 0.02) + "% Armor Pen.";

        stat_page2_string = ChatColor.BLACK.toString() + ChatColor.BOLD.toString() + "+ " + vit_val + " Vitality"
                + "\n" + ChatColor.BLACK.toString() + "   " + ChatColor.UNDERLINE.toString() + "'The Defender'\n"
                + ChatColor.BLACK.toString() + "+" + df.format(vit_val * 0.034) + "% Health" + "\n"
                + ChatColor.BLACK.toString() + "+" + df.format(vit_val * 0.3) + " HP/s" + "\n"
                + ChatColor.BLACK.toString() + "+" + df.format(vit_val * 0.04) + "% Ele Resist" + "\n"
                + ChatColor.BLACK.toString() + "+" + df.format(vit_val * 0.01) + "% Sword DMG" + "\n" + "\n"
                + ChatColor.BLACK.toString() + ChatColor.BOLD.toString() + "+ " + int_val + " Intellect" + "\n"
                + ChatColor.BLACK.toString() + "   " + ChatColor.UNDERLINE.toString() + "'The Mage'" + new_line
                + ChatColor.BLACK.toString() + "+" + df.format(int_val * 0.015) + "% Energy" + "\n"
                + ChatColor.BLACK.toString() + "+" + df.format(int_val * 0.05) + "% Ele Damage" + "\n"
                + ChatColor.BLACK.toString() + "+" + df.format(int_val * 0.025) + "% Critical DMG" + "\n"
                + ChatColor.BLACK.toString() + "+" + df.format(int_val * 0.02) + "% Staff DMG";
        //  PORTAL SHARD PAGE  //
        String portalShardPage = ChatColor.BLACK.toString() + ChatColor.BOLD.toString() + "Portal Key Shards" + "\n" + ChatColor.BLACK.toString()
                + ChatColor.ITALIC.toString() + "A sharded fragment from the great portal of Maltai that may be exchanged at the Dungeoneer for epic equipment.\n";

        for (ShardTier tier : ShardTier.values())
            portalShardPage += "\n" + (tier.getColor() != ChatColor.WHITE ? tier.getColor() : ChatColor.DARK_GRAY) + "Portal Shards: " + ChatColor.BLACK + pw.getPortalShards(tier);

        //  COMMAND PAGE  //
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
                + new_line + ChatColor.BLACK.toString() + ChatColor.BOLD.toString() + "/trade "
                + "\n" + ChatColor.BLACK.toString() + "Allows you to trade with a person near you."
                + new_line + ChatColor.BLACK.toString() + ChatColor.BOLD.toString() + "/vote "
                + "\n" + ChatColor.BLACK.toString() + "Allows you to vote for eCash!"
        );


        String page5_string = (ChatColor.BLACK.toString() + ChatColor.BOLD.toString() + "/stats" + "\n" + ChatColor.BLACK.toString() + "Set Attributes"
                + new_line + ChatColor.BLACK.toString() + ChatColor.BOLD.toString() + "/toggles" + "\n" + ChatColor.BLACK.toString() + "Open Toggles Menu" +
                new_line + ChatColor.BLACK.toString() + ChatColor.BOLD.toString() + "/mailbox" + "\n" + ChatColor.BLACK.toString() + "Open Your mailbox to view your pending purchases!"
                + new_line + ChatColor.BLACK.toString() + ChatColor.BOLD.toString() + "/unlocks" + "\n" + ChatColor.BLACK.toString() + "Open your unlocks menu!");

        String page6_string = (ChatColor.BLACK.toString() + ChatColor.BOLD.toString() + ChatColor.UNDERLINE.toString() + "  Party Commands  " + new_line
                + ChatColor.BLACK.toString() + ChatColor.BOLD.toString() + "/pinvite" + "\n" + ChatColor.BLACK.toString() + "Invite someone to your party" +
                new_line + ChatColor.BLACK.toString() + ChatColor.BOLD.toString() + "/pdeny" + "\n" + ChatColor.BLACK.toString() + "Deny your pending party invitation"
                + new_line + ChatColor.BLACK.toString() + ChatColor.BOLD.toString() + "/paccept" + "\n" + ChatColor.BLACK.toString() + "Accept your pending party invitation");

        String page7_string = (ChatColor.BLACK.toString() + ChatColor.BOLD.toString() + "/djoin" + "\n" + ChatColor.BLACK.toString() + "Join your parties Dungeon"
                + new_line + ChatColor.BLACK.toString() + ChatColor.BOLD.toString() + "/dleave" + "\n" + ChatColor.BLACK.toString() + "Leave your parties Dungeon");

        bm.setAuthor("King Bulwar");
        pages.add(page1_string);
        if (quests > 0)
            pages.add(questPage_string);
        pages.add(page2_string);
        pages.add(stat_page_string);
        pages.add(stat_page2_string);
        pages.add(portalShardPage);
        pages.add(page3_string);
        pages.add(page4_string);
        pages.add(page5_string);
        pages.add(page6_string);
        pages.add(page7_string);

        bm.setPages(pages);
        stack.setItemMeta(bm);
        return stack;
    }

    public static void whitelistItemDrop(Player player, Location drop, ItemStack item) {
        whitelistItemDrop(player, drop.getWorld().dropItem(drop.clone().add(0, 1, 0), item));
    }

    public static org.bukkit.entity.Item whitelistItemDrop(Player player, org.bukkit.entity.Item item) {
        if (player != null)
            Metadata.WHITELIST.set(item, player.getName());
        return item;
    }

    /**
     * Is this item marked as droppable, and tradeable?
     */
    public static boolean isItemDroppable(ItemStack item) {
        return !get(item).isUndroppable() && isItemTradeable(item);
    }

    /**
     * Make an item undroppable.
     */
    public static ItemStack makeItemUndroppable(ItemStack item) {
        VanillaItem vanilla = new VanillaItem(item);
        vanilla.setUndroppable(true);
        return vanilla.generateItem();
    }

    /**
     * Is this item marked as tradeable and not soulbound or permanently untradeable?
     */
    public static boolean isItemTradeable(ItemStack item) {
        ItemGeneric ig = get(item);
        return !(ig.isUntradeable() || ig.isSoulbound() || ig.isPermanentUntradeable());
    }

    /**
     * Make an item untradeable.
     */
    public static ItemStack makeItemUntradeable(ItemStack item) {
        VanillaItem vanilla = new VanillaItem(item);
        vanilla.setUntradeable(true);
        return vanilla.generateItem();
    }

    public static boolean isDungeonItem(ItemStack item) {
        return get(item).isDungeon();
    }

    /**
     * Is this item soulbound?
     */
    public static boolean isItemSoulbound(ItemStack item) {
        return get(item).isSoulbound();
    }

    /**
     * Make an item soulbound.
     */
    public static ItemStack makeItemSoulbound(ItemStack item) {
        VanillaItem vanilla = new VanillaItem(item);
        vanilla.setSoulbound(true);
        return vanilla.generateItem();
    }

    /**
     * Is this item permanently untradeable?
     */
    public static boolean isItemPermanentlyUntradeable(ItemStack item) {
        return get(item).isPermanentUntradeable();
    }

    /**
     * Make an item permanent untradeable.
     */
    public static ItemStack makeItemPermenantUntradeable(ItemStack item) {
        VanillaItem vanilla = new VanillaItem(item);
        vanilla.setPermUntradeable(true);
        return vanilla.generateItem();
    }

    private static ItemGeneric get(ItemStack item) {
        return (ItemGeneric) PersistentItem.constructItem(item);
    }

    public static CombatItem createRandomCombatItem() {
        return ThreadLocalRandom.current().nextBoolean() ? new ItemWeapon() : new ItemArmor();
    }

    public static ItemStack createItem(Material mat, short data, String name) {
        ItemStack stack = new ItemStack(mat, 1, data);
        ItemMeta meta = stack.getItemMeta();
        meta.setDisplayName(name);
        stack.setItemMeta(meta);
        return stack;
    }

    public static ItemStack createItem(Material mat, String name, String... lore) {
        ItemStack stack = new ItemStack(mat);
        ItemMeta meta = stack.getItemMeta();
        meta.setDisplayName(name);
        List<String> l = new ArrayList<>();
        if (lore != null)
            for (String s : lore)
                l.add(ChatColor.GRAY + s);
        meta.setLore(l);
        stack.setItemMeta(meta);
        return stack;
    }

    public static ItemStack createItem(Material mat, String name, short data, String... lore) {
        ItemStack stack = new ItemStack(mat, 1, data);
        ItemMeta meta = stack.getItemMeta();
        meta.setDisplayName(name);
        if (lore != null && lore.length > 0)
            meta.setLore(Lists.newArrayList(lore));
        stack.setItemMeta(meta);
        return stack;
    }
}
