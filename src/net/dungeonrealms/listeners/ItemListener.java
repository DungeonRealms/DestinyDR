package net.dungeonrealms.listeners;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.combat.CombatLog;
import net.dungeonrealms.inventory.GUI;
import net.dungeonrealms.inventory.Menu;
import net.dungeonrealms.mastery.Utils;
import net.dungeonrealms.mechanics.ItemManager;
import net.dungeonrealms.teleportation.TeleportAPI;
import net.dungeonrealms.teleportation.Teleportation;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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
        Player player = event.getPlayer();
        if (!(event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) return;
        if (player.getItemInHand() == null || player.getItemInHand().getType() != Material.QUARTZ && player.getItemInHand().getType() != Material.BOOK)
            return;
        ItemStack itemStack = player.getItemInHand();
        if (!(CombatLog.isInCombat(event.getPlayer()))) {
            if (TeleportAPI.isPlayerCurrentlyTeleporting(player.getUniqueId())) {
                player.sendMessage("You cannot restart a teleport during a cast!");
                return;
            }
            if (TeleportAPI.isHearthstone(itemStack)) {
                if (TeleportAPI.canUseHearthstone(player.getUniqueId())) {
                    net.minecraft.server.v1_8_R3.ItemStack nmsItem = CraftItemStack.asNMSCopy(itemStack);
                    Teleportation.teleportPlayer(event.getPlayer().getUniqueId(), Teleportation.EnumTeleportType.HEARTHSTONE, nmsItem.getTag());
                } else {
                    player.sendMessage(
                            ChatColor.GREEN.toString() + ChatColor.BOLD + "HEARTHSTONE " + ChatColor.RED + "[Usage Exhausted] " + ChatColor.RED.toString() + "(" + ChatColor.UNDERLINE + TeleportAPI.getPlayerHearthstoneCD(player.getUniqueId()) + "s" + ChatColor.RED + ")");
                }
            } else if (TeleportAPI.isTeleportBook(itemStack)) {
                net.minecraft.server.v1_8_R3.ItemStack nmsItem = CraftItemStack.asNMSCopy(itemStack);
                Teleportation.teleportPlayer(player.getUniqueId(), Teleportation.EnumTeleportType.TELEPORT_BOOK, nmsItem.getTag());
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
        Player player = event.getPlayer();
        if (!(event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) return;
        if (player.getItemInHand() == null || player.getItemInHand().getType() != Material.SKULL_ITEM) return;
        GUI profileMain = new GUI("Profile", 27, guievent ->{
        	int slot = guievent.getPosition();
        	ItemStack stack = guievent.getInventory().getItem(slot);
        	switch(slot){
                case 22:
        	       if (!(CombatLog.isInCombat(event.getPlayer()))) {
                       if (TeleportAPI.isPlayerCurrentlyTeleporting(player.getUniqueId())) {
                           player.sendMessage("You cannot restart a teleport during a cast!");
                           return;
                       }
                       if (TeleportAPI.canUseHearthstone(player.getUniqueId())) {
                           net.minecraft.server.v1_8_R3.ItemStack nmsItem = CraftItemStack.asNMSCopy(stack);
                           Teleportation.teleportPlayer(event.getPlayer().getUniqueId(), Teleportation.EnumTeleportType.HEARTHSTONE, nmsItem.getTag());
                           break;
                       } else {
                           player.sendMessage(ChatColor.GREEN.toString() + ChatColor.BOLD + "HEARTHSTONE " + ChatColor.RED + "[Usage Exhausted] " + ChatColor.RED.toString() + "(" + ChatColor.UNDERLINE + TeleportAPI.getPlayerHearthstoneCD(player.getUniqueId()) + "s" + ChatColor.RED + ")");
                           break;
                       }
                   } else {
                       player.sendMessage(ChatColor.GREEN.toString() + ChatColor.BOLD + "TELEPORT " + ChatColor.RED + "You are in combat! " + ChatColor.RED.toString() + "(" + ChatColor.UNDERLINE + CombatLog.COMBAT.get(player) + "s" + ChatColor.RED + ")");
                       break;
                   }
                case 5:
                    Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> Menu.openMobParticleMenu(player), 5L);
                    break;
                case 6:
                    Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> Menu.openPlayerParticleMenu(player), 5L);
                    break;
                case 7:
                    Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> Menu.openPlayerMountMenu(player), 5L);
                    break;
                case 8:
                    Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> Menu.openPlayerPetMenu(player), 5L);
                    break;
                default:
                    break;
            }
        }, DungeonRealms.getInstance()).setOption(4, Utils.getPlayerHead(player), ChatColor.GREEN + player.getName() + "'s Profile", new String[]{
                ChatColor.DARK_GRAY + "Player Profile"
        }).setOption(0, new ItemStack(Material.EXP_BOTTLE), ChatColor.GREEN + "Attributes", new String[]{
                ChatColor.DARK_GRAY + "Player Attributes",
                "",
                ChatColor.GRAY + "As you play throughout Dungeon Realms,",
                ChatColor.GRAY + "your player will acquire attribute points.",
                ChatColor.GRAY + "With Attribute points you can improve",
                ChatColor.GRAY + "several of many individual character",
                ChatColor.GRAY + "skills!",
                "",
                ChatColor.YELLOW + "Click to view Player Attributes!"
        }).setOption(5, new ItemStack(Material.SKULL_ITEM), ChatColor.GREEN + "Mob Trails", new String[]{
                ChatColor.DARK_GRAY + "Mob Trails",
                "",
                ChatColor.GRAY + "Want your companion to look awesome?",
                ChatColor.GRAY + "Get yourself some Mob Trails!",
                "",
                ChatColor.YELLOW + "Click to view Mob Trails!"
        }).setOption(6, new ItemStack(Material.EYE_OF_ENDER), ChatColor.GREEN + "Player Trails", new String[]{
                ChatColor.DARK_GRAY + "Player Trails",
                "",
                ChatColor.GRAY + "Want to be the envy of your friends?",
                ChatColor.GRAY + "Get yourself a Particle Trail!",
                "",
                ChatColor.YELLOW + "Click to view Player Trails!"
        }).setOption(7, new ItemStack(Material.SADDLE), ChatColor.GREEN + "Mounts", new String[]{
                ChatColor.DARK_GRAY + "Player Mounts",
                "",
                ChatColor.GRAY + "Want to travel in style?",
                ChatColor.GRAY + "Mounts are the solution!",
                "",
                ChatColor.YELLOW + "Click to view Player Mounts!"
        }).setOption(8, new ItemStack(Material.NAME_TAG), ChatColor.GREEN + "Pets", new String[]{
                ChatColor.DARK_GRAY + "Player Pets",
                "",
                ChatColor.GRAY + "Want a friendly companion",
                ChatColor.GRAY + "to bring along on your",
                ChatColor.GRAY + "adventures? Pets are the",
                ChatColor.GRAY + "solution!",
                "",
                ChatColor.YELLOW + "Click to view Player Pets!"
        }).setOption(18, new ItemStack(Material.EMERALD), ChatColor.GREEN + "Donations", new String[]{
                ChatColor.DARK_GRAY + "Micro Transactions",
                "",
                ChatColor.GRAY + "Want to access to more awesomeness?",
                ChatColor.GRAY + "Consider donating to support Dungeon Realms",
                ChatColor.GRAY + "in return you'll receive several in-game",
                ChatColor.GRAY + "perks!",
                "",
                ChatColor.YELLOW + "Click to view Micro Transactions!"
        }).setOption(22, ItemManager.createHearthStone("HearthStone", new String[]{
                ChatColor.AQUA + "Teleport home to " + ChatColor.YELLOW + ChatColor.BOLD + TeleportAPI.getLocationFromDatabase(player.getUniqueId()).toUpperCase() + ChatColor.RESET + ChatColor.AQUA + "!",
        }), "HearthStone", new String[]{
                ChatColor.AQUA + "Teleport home to " + ChatColor.YELLOW + ChatColor.BOLD + TeleportAPI.getLocationFromDatabase(player.getUniqueId()).toUpperCase() + ChatColor.RESET + ChatColor.AQUA + "!",
        }).setOption(26, new ItemStack(Material.REDSTONE_COMPARATOR), ChatColor.GREEN + "Settings & Preferences", new String[]{
                        ChatColor.DARK_GRAY + "Settings & Preferences",
                        "",
                        ChatColor.GRAY + "Help us help you! By adjusting",
                        ChatColor.GRAY + "your player settings & preferences",
                        ChatColor.GRAY + "we can optimize your gaming experience",
                        ChatColor.GRAY + "on this RPG!",
                        "",
                        ChatColor.YELLOW + "Click to view Settings & Preferences!"
                });

        profileMain.open(player);
    }
}
