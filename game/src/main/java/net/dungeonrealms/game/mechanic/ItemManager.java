package net.dungeonrealms.game.mechanic;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.game.database.DatabaseAPI;
import net.dungeonrealms.common.game.database.data.EnumData;
import net.dungeonrealms.game.handler.HealthHandler;
import net.dungeonrealms.game.handler.KarmaHandler;
import net.dungeonrealms.game.item.PersistentItem;
import net.dungeonrealms.game.item.items.core.CombatItem;
import net.dungeonrealms.game.item.items.core.ItemGeneric;
import net.dungeonrealms.game.item.items.core.ItemWeapon;
import net.dungeonrealms.game.item.items.core.ItemArmor;
import net.dungeonrealms.game.item.items.core.VanillaItem;
import net.dungeonrealms.game.item.items.functional.ItemScrap;
import net.dungeonrealms.game.item.items.functional.PotionItem;
import net.dungeonrealms.game.mastery.GamePlayer;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.mechanic.data.PotionTier;
import net.dungeonrealms.game.mechanic.data.ScrapTier;
import net.dungeonrealms.game.mechanic.data.ShardTier;
import net.dungeonrealms.game.miscellaneous.ItemBuilder;
import net.dungeonrealms.game.quests.Quest;
import net.dungeonrealms.game.quests.QuestPlayerData;
import net.dungeonrealms.game.quests.QuestPlayerData.QuestProgress;
import net.dungeonrealms.game.quests.Quests;
import net.dungeonrealms.game.world.item.Item.ArmorAttributeType;
import net.dungeonrealms.game.world.item.itemgenerator.ItemGenerator;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.*;

/**
 * ItemManager - Contains basic item utils.
 * 
 * Redone by Kneesnap in early April 2017.
 */
public class ItemManager {
	
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

        // Give T1 potions.
        for(int i = 0; i < 3; i++)
        	player.getInventory().addItem(new PotionItem(PotionTier.TIER_1).setUntradeable(true).generateItem());
        
        if(isNew)
        	player.getInventory().addItem(new VanillaItem(new ItemStack(Material.BREAD, 3)).setUntradeable(true).generateItem());

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
        
        GameAPI.getGamePlayer(player).calculateAllAttributes();
        HealthHandler.updatePlayerHP(player);
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
        //ItemStack orb = ItemManager.createEventOrbofAlteration();
        //orb.setAmount(64);
        //player.getInventory().addItem(orb, orb);

        // Add Scrap
        player.getInventory().addItem(new ItemScrap(ScrapTier.TIER5).generateItem());

        // Add Food
        player.getInventory().addItem(new ItemStack(Material.GOLDEN_CARROT, 64));

        // Add T5 potions
        for (int i = 0; i < 25; i++)
        	player.getInventory().addItem(new PotionItem(PotionTier.TIER_5).generateItem());
        
        GameAPI.getGamePlayer(player).calculateAllAttributes();
        HealthHandler.updatePlayerHP(player);
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
        String new_line = "\n" + ChatColor.BLACK.toString() + " " + "\n";
        GamePlayer gp = GameAPI.getGamePlayer(p);
        if (gp == null)
            return stack;
        KarmaHandler.EnumPlayerAlignments playerAlignment = gp.getPlayerAlignment();
        String pretty_align = (playerAlignment == KarmaHandler.EnumPlayerAlignments.LAWFUL ? ChatColor.DARK_GREEN.toString() :
                playerAlignment.getAlignmentColor()) + ChatColor.UNDERLINE.toString() + playerAlignment.name();

        if (pretty_align.contains("CHAOTIC") || pretty_align.contains("NEUTRAL")) {
            String time = String.valueOf(KarmaHandler.getInstance().getAlignmentTime(p));
            page1_string = ChatColor.BLACK.toString() + "" + ChatColor.BOLD.toString() + ChatColor.UNDERLINE.toString() + "  Your Character  \n\n"
                    + ChatColor.BLACK.toString() + ChatColor.BOLD.toString() + "Alignment: " + pretty_align + "\n" + playerAlignment.getAlignmentColor().toString() + ChatColor.BOLD + time + "s.." + new_line;
        } else {
            page1_string = ChatColor.BLACK.toString() + "" + ChatColor.BOLD.toString() + ChatColor.UNDERLINE.toString() + "  Your Character  " + "\n\n"
                    + ChatColor.BLACK.toString() + ChatColor.BOLD.toString() + "Alignment: " + pretty_align + new_line;
        }
        
        page1_string += ""
        		+ ChatColor.BLACK.toString() + playerAlignment.description + new_line + ChatColor.BLACK.toString() + "   " + gp.getHP()
        		+ " / " + gp.getHP() + "" + ChatColor.BOLD.toString() + " HP" + "\n" + ChatColor.BLACK.toString()
                + "   " + gp.getStats().getDPS() + "% " + ChatColor.BOLD.toString() + "DPS" + "\n" + ChatColor.BLACK.toString()
        		+ "   " + (HealthHandler.getPlayerHPRegen(p) + gp.getStats().getHPRegen()) + " " + ChatColor.BOLD.toString() + "HP/s" + "\n" + ChatColor.BLACK.toString()
                + "   " + gp.getAttributes().getAttribute(ArmorAttributeType.ENERGY_REGEN).toString() + "% " + ChatColor.BOLD.toString() + "Energy/s" + "\n" + ChatColor.BLACK.toString()
                + "   " + DatabaseAPI.getInstance().getData(EnumData.ECASH, p.getUniqueId()) + ChatColor.BOLD.toString() + " E-CASH" + "\n" + ChatColor.BLACK.toString()
        		+ "   " + gp.getAttributes().getAttribute(ArmorAttributeType.GEM_FIND).getValue() + ChatColor.BOLD.toString() + " GEM FIND" + "\n" + ChatColor.BLACK.toString()
                + "   " + gp.getAttributes().getAttribute(ArmorAttributeType.ITEM_FIND).getValue() + ChatColor.BOLD.toString() + " ITEM FIND";
        
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
                + ChatColor.ITALIC.toString() + "A sharded fragment from the great portal of Maltai that may be exchanged at the Dungeoneer for epic equipment.";
        
        for (ShardTier tier : ShardTier.values())
        	portalShardPage += (tier.getColor() != ChatColor.WHITE ? tier.getColor() : ChatColor.DARK_GRAY) + "Portal Shards: " + ChatColor.BLACK + DatabaseAPI.getInstance().getData(tier.getShardData(), p.getUniqueId());
        
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

        bm.setPages(pages);
        stack.setItemMeta(bm);
        return stack;
    }
    
    public static void whitelistItemDrop(Player player, org.bukkit.entity.Item item) {
        if (player == null) return;
        item.setMetadata("whitelist", new FixedMetadataValue(DungeonRealms.getInstance(), player.getName()));
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
		return (ItemGeneric)PersistentItem.constructItem(item);
	}
	
	public static CombatItem createRandomCombatItem() {
		return new Random().nextBoolean() ? new ItemWeapon() : new ItemArmor();
	}

	public static ItemStack createItem(Material mat, String name, String[] lore) {
		ItemStack stack = new ItemStack(mat);
		ItemMeta meta = stack.getItemMeta();
		meta.setDisplayName(name);
		List<String> l = new ArrayList<>();
		for(String s : lore)
			l.add(ChatColor.GRAY + s);
		meta.setLore(l);
		stack.setItemMeta(meta);
		return stack;
	}
}
