package net.dungeonrealms.listeners;

import net.dungeonrealms.API;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.banks.BankMechanics;
import net.dungeonrealms.combat.CombatLog;
import net.dungeonrealms.handlers.HealthHandler;
import net.dungeonrealms.inventory.PlayerMenus;
import net.dungeonrealms.mastery.GamePlayer;
import net.dungeonrealms.mongo.DatabaseAPI;
import net.dungeonrealms.mongo.EnumData;
import net.dungeonrealms.mongo.EnumOperators;
import net.dungeonrealms.stats.PlayerStats;
import net.dungeonrealms.teleportation.TeleportAPI;
import net.dungeonrealms.teleportation.Teleportation;
import net.minecraft.server.v1_8_R3.NBTTagCompound;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;

import com.minebone.anvilapi.core.AnvilApi;
import com.minebone.anvilapi.nms.anvil.AnvilGUIInterface;
import com.minebone.anvilapi.nms.anvil.AnvilSlot;

/**
 * Created by Kieran on 9/18/2015.
 */
public class ItemListener implements Listener {
    /**
     * Used to stop player from dropping items that are
     * valuable e.g. hearthstone or profile head.
     *
     * @param event
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onItemDrop(PlayerDropItemEvent event) {
        net.minecraft.server.v1_8_R3.ItemStack nmsItem = CraftItemStack.asNMSCopy(event.getItemDrop().getItemStack());
        NBTTagCompound tag = nmsItem.getTag();
        if (tag == null || !tag.getString("type").equalsIgnoreCase("important")) return;
        event.setCancelled(true);
        event.getPlayer().sendMessage(ChatColor.RED + "[WARNING] " + ChatColor.YELLOW + "You can't drop important game items!");
    }

    /**
     * Handles player clicking with a teleportation item
     *
     * @param event
     * @since 1.0
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerUseTeleportItem(PlayerInteractEvent event) {
        if (!(event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) return;
        Player player = event.getPlayer();
        if (player.getItemInHand() == null || player.getItemInHand().getType() != Material.BOOK) return;
        ItemStack itemStack = player.getItemInHand();
        if (!(CombatLog.isInCombat(event.getPlayer()))) {
            if (TeleportAPI.isPlayerCurrentlyTeleporting(player.getUniqueId())) {
                player.sendMessage("You cannot restart a teleport during a cast!");
                return;
            }
            if (TeleportAPI.isTeleportBook(itemStack)) {
                net.minecraft.server.v1_8_R3.ItemStack nmsItem = CraftItemStack.asNMSCopy(itemStack);
                Teleportation.getInstance().teleportPlayer(player.getUniqueId(), Teleportation.EnumTeleportType.TELEPORT_BOOK, nmsItem.getTag());
                if (player.getItemInHand().getAmount() == 1) {
                    player.setItemInHand(new ItemStack(Material.AIR));
                } else {
                    player.getItemInHand().setAmount((player.getItemInHand().getAmount() - 1));
                }
            } else {
                player.sendMessage(ChatColor.RED.toString() + ChatColor.BOLD + "This item cannot be used to Teleport!");
            }
        } else {
            player.sendMessage(
                    ChatColor.GREEN.toString() + ChatColor.BOLD + "TELEPORT " + ChatColor.RED + "You are in combat! " + ChatColor.RED.toString() + "(" + ChatColor.UNDERLINE + CombatLog.COMBAT.get(player.getUniqueId()) + "s" + ChatColor.RED + ")");
        }
    }

    /**
     * Handles player clicking with their profile
     *
     * @param event
     * @since 1.0
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerUseProfileItem(PlayerInteractEvent event) {
        if (!(event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) return;
        Player player = event.getPlayer();
        if (player.getItemInHand() == null || player.getItemInHand().getType() != Material.SKULL_ITEM) return;
        net.minecraft.server.v1_8_R3.ItemStack nmsStack = CraftItemStack.asNMSCopy(player.getItemInHand());
        NBTTagCompound tag = nmsStack.getTag();
        if (tag == null) return;
        if (!(tag.getString("type").equalsIgnoreCase("important")) && !(tag.getString("usage").equalsIgnoreCase("profile"))) return;
        PlayerMenus.openPlayerProfileMenu(player);
    }
    
    
    /**
     * Handles Right Click of Character Journal
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerUseCharacterJournal(PlayerInteractEvent event){
        if (!(event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) return;
        Player p = event.getPlayer();
        if (p.getItemInHand() == null || p.getItemInHand().getType() != Material.WRITTEN_BOOK) return;
        net.minecraft.server.v1_8_R3.ItemStack nmsStack = CraftItemStack.asNMSCopy(p.getItemInHand());
        NBTTagCompound tag = nmsStack.getTag();
        if (tag == null) return;
        if (tag.hasKey("journal") && !(tag.getString("journal").equalsIgnoreCase("true"))) return;
        ItemStack stack = event.getItem();
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

		page1_string = ChatColor.BLACK.toString() + "" + ChatColor.BOLD.toString() + ChatColor.UNDERLINE.toString() + "  Your Character" + "\n"+ new_line
				+ ChatColor.BLACK.toString() + ChatColor.BOLD.toString() + "Alignment: " + pretty_align + "\n"
				+ ChatColor.BLACK.toString() + gp.getPlayerAlignment().description + new_line + ChatColor.BLACK.toString() + "   " + gp.getPlayerCurrentHP()
				+ " / " + gp.getPlayerMaxHP() + "" + ChatColor.BOLD.toString() + " HP" + "\n" + ChatColor.BLACK.toString()
				+ "   " + gp.getStats().getDPS() + "% " +  ChatColor.BOLD.toString() + "DPS" + "\n" + ChatColor.BLACK.toString()
				+ "   " + (HealthHandler.getInstance().getPlayerHPRegenLive(p)) + " " + ChatColor.BOLD.toString() + "HP/s" + "\n"
				+ ChatColor.BLACK.toString() + "   " + "0.00"
				+ "% " + ChatColor.BOLD.toString() + "Energy" + "\n" + ChatColor.BLACK.toString() + "   " + DatabaseAPI.getInstance().getData(EnumData.ECASH, p.getUniqueId()) + ChatColor.BOLD.toString()
				+ " E-CASH";
		
		page2_string = ChatColor.DARK_AQUA.toString() + ChatColor.BOLD + "  ** LEVEL/EXP **\n\n" + ChatColor.BLACK + ChatColor.BOLD
				+ "       LEVEL\n\n" + "          " + ChatColor.BLACK + gp.getLevel() + "\n\n" + ChatColor.BLACK + ChatColor.BOLD
				+ "          XP" + "\n" + ChatColor.BLACK + "       " + gp.getExperience() + " / "
				+ gp.getEXPNeeded(gp.getLevel());
		
        page3_string = ChatColor.BLACK.toString() + ChatColor.BOLD.toString() + "+ " + stats.strPoints  + " Strength"
                + "\n" + ChatColor.BLACK.toString() + "   " + ChatColor.UNDERLINE.toString() + "'The Warrior'"+ "\n"
//                + ChatColor.BLACK.toString() + "+" + df.format("STR * 0.03") + "% Armor" + "\n"
                + ChatColor.BLACK.toString() + "+" + df.format(stats.getBlock()* 100) + "% Block" + "\n"
                + ChatColor.BLACK.toString() + "+" + df.format(stats.getAxeDMG()* 100) + "% Axe DMG" + "\n"
                + ChatColor.BLACK.toString() + "+" + df.format(stats.getPolearmDMG()* 100) + "% Polearm DMG" + "\n" + "\n"
                + ChatColor.BLACK.toString() + ChatColor.BOLD.toString() + "+ " + stats.dexPoints + " Dexterity" + "\n"
                + ChatColor.BLACK.toString() + "   " + ChatColor.UNDERLINE.toString() + "'The Archer'"+ "\n"
                + ChatColor.BLACK.toString() + "+" + df.format(stats.getDodge()* 100) + "% Dodge" + "\n"
                + ChatColor.BLACK.toString() + "+" + df.format(stats.getBowDMG()* 100) + "% Bow DMG" + "\n"
                + ChatColor.BLACK.toString() + "+" + df.format(stats.getCritChance()* 100) + "% Critical Hit" + "\n"
                + ChatColor.BLACK.toString() + "+" + df.format(stats.getArmorPen()* 100) + "% Armor Pen.";

        page4_string = ChatColor.BLACK.toString() + ChatColor.BOLD.toString() + "+ " + stats.vitPoints + " Vitality"
                + "\n" + ChatColor.BLACK.toString() + "   " + ChatColor.UNDERLINE.toString() + "'The Defender'"+ "\n"
                + ChatColor.BLACK.toString() + "+" + df.format(stats.getVitHP()* 100) + "% Health" + "\n"
                + ChatColor.BLACK.toString() + "+" + df.format(stats.getHPRegen()* 100) + "   HP/s" + "\n"
                + ChatColor.BLACK.toString() + "+" + df.format(stats.getSwordDMG()* 100) + "% Sword DMG" + "\n" + "\n"
                + ChatColor.BLACK.toString() + ChatColor.BOLD.toString() + "+ " + stats.intPoints + " Intellect" + "\n"
                + ChatColor.BLACK.toString() + "   " + ChatColor.UNDERLINE.toString() + "'The Mage'"+ "\n" 
                + ChatColor.BLACK.toString() + "+" + df.format(stats.getEnergyRegen()* 100) + "% Energy" + "\n"
                + ChatColor.BLACK.toString() + "+" + df.format(stats.getCritChance()* 100) + "% Critical Hit" + "\n"
                + ChatColor.BLACK.toString() + "+" + df.format(stats.getStaffDMG()* 100) + "% Staff DMG";


        bm.setAuthor("");
        pages.add(page1_string);
        pages.add(page2_string);
        pages.add(page3_string);
        pages.add(page4_string);
        bm.setPages(pages);
        stack.setItemMeta(bm);
        
        p.getInventory().setItem(7, stack);
        p.updateInventory(); 
    }
    
    
    /**
     * Handles player right clicking a stat reset book
     * 
     * @param event
     * @since 1.0
     */
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void useEcashItem(PlayerInteractEvent event) {
    	if(event.getItem() != null){
    	if( event.getItem().getType() == Material.ENCHANTED_BOOK){
    		net.minecraft.server.v1_8_R3.ItemStack nms = CraftItemStack.asNMSCopy(event.getItem());
    		if(nms.hasTag() && nms.getTag().hasKey("type") && nms.getTag().getString("type").equalsIgnoreCase("reset")){
    			AnvilGUIInterface gui = AnvilApi.createNewGUI(event.getPlayer(), e -> {
					if (e.getSlot() == AnvilSlot.OUTPUT) {
						if(e.getName().equalsIgnoreCase("Yes") || e.getName().equalsIgnoreCase("y")){
							if(event.getItem().getAmount() > 1){
								event.getItem().setAmount(event.getItem().getAmount() - 1);
								
							}else
								event.getPlayer().getInventory().remove(event.getItem());
							API.getGamePlayer(event.getPlayer()).getStats().unallocateAllPoints();
							event.getPlayer().sendMessage(ChatColor.YELLOW + "All Stat Points have been unallocated!");
							e.destroy();
						}else{
							e.setWillClose(true);
							e.destroy();
						}
					}
				});
				ItemStack stack = new ItemStack(Material.INK_SACK, 1, DyeColor.LIME.getDyeData());
				ItemMeta meta = stack.getItemMeta();
				meta.setDisplayName("Reset stat points?");
				stack.setItemMeta(meta);
				gui.setSlot(AnvilSlot.INPUT_LEFT, stack);
				event.getPlayer().sendMessage("Opening stat reset confirmation...");
				Bukkit.getScheduler().scheduleAsyncDelayedTask(DungeonRealms.getInstance(), () -> {
				gui.open();
				}, 20 * 5);
    		}
    	}else if(event.getItem().getType() == Material.ENDER_CHEST){
    		net.minecraft.server.v1_8_R3.ItemStack nms = CraftItemStack.asNMSCopy(event.getItem());
    			if(nms.hasTag() && nms.getTag().hasKey("type")){
    				if(nms.getTag().getString("type").equalsIgnoreCase("upgrade")){
    					Player player = event.getPlayer();
    				}
    			}
    		}
    	}
    }
}
