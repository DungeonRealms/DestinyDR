package net.dungeonrealms.game.listener.inventory;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.database.PlayerToggles.Toggles;
import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.game.affair.Affair;
import net.dungeonrealms.game.affair.party.Party;
import net.dungeonrealms.game.item.items.core.VanillaItem;
import net.dungeonrealms.game.item.items.functional.PotionItem;
import net.dungeonrealms.game.mastery.GamePlayer;
import net.dungeonrealms.game.mechanic.ItemManager;
import net.dungeonrealms.game.mechanic.PlayerManager;
import net.dungeonrealms.game.mechanic.dungeons.DungeonManager;
import net.dungeonrealms.game.miscellaneous.NBTWrapper;
import net.dungeonrealms.game.player.chat.Chat;
import net.dungeonrealms.game.world.item.Item.ItemRarity;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.inventivetalent.glow.GlowAPI;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Kieran on 9/18/2015.
 */
public class ItemListener implements Listener {


    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onItemPickup(PlayerPickupItemEvent event) {
        Player player = event.getPlayer();
        Party p = Affair.getParty(player);
        if (p != null)
            Affair.getInstance().handlePartyPickup(event, p);
    }

    /**
     * Makes Uncommon+ Items glow
     */
    @EventHandler
    public void onItemSpawn(ItemSpawnEvent event) {
        ItemStack type = event.getEntity().getItemStack();
        //Remove those shitty leashes..
        if (type != null && type.getType() == Material.LEASH && !type.hasItemMeta()) {
            event.setCancelled(true);
            event.getEntity().remove();
            return;
        }

        //Dont spawn exploded shit.
        if (DungeonManager.isDungeon(event.getEntity().getWorld())) {
            if (type != null && type.getType().name().contains("REDSTONE")) {
                event.setCancelled(true);
                event.getEntity().remove();
                return;
            }

        }
        this.applyRarityGlow(event.getEntity());
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        int removed = 0;
        StringBuilder removedStr = null;
        for (Entity ent : event.getChunk().getEntities()) {
            if (ent instanceof Item) {
                Item item = (Item) ent;
                if (item.getItemStack() != null && !new NBTWrapper(item.getItemStack()).hasTag("type")) {
                    item.remove();
                    removed++;
                    if (removedStr == null)
                        removedStr = new StringBuilder();

                    removedStr.append(item.getItemStack().getType()).append(", ");
                }
            }
        }

        if (removed > 0)
            Bukkit.getLogger().info("Removed " + removed + " invalid items: " + removedStr.toString() + " from chunk: x" + event.getChunk().getX() + " z" + event.getChunk().getZ());

    }

    private void applyRarityGlow(Item entity) {
        ItemStack item = entity.getItemStack();
        if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasLore())
            return;
        List<String> lore = item.getItemMeta().getLore();
        for (ItemRarity rarity : ItemRarity.values()) {
            for (String s : lore) {
                if (s.contains(rarity.getName())) {
                    Bukkit.getScheduler().runTaskAsynchronously(DungeonRealms.getInstance(), () -> {
                        //Filter out players who have toggle glow off.
                        List<Player> sendTo = GameAPI.getNearbyPlayersAsync(entity.getLocation(), 10).stream().filter(p -> {
                            PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(p);
                            return wrapper != null && wrapper.getToggles().getState(Toggles.GLOW);
                        }).collect(Collectors.toList());
                        //Set the item as glowing.
                        GlowAPI.setGlowing(entity, GlowAPI.Color.valueOf(rarity.getColor().name()), sendTo);
                    });
                    return;
                }
            }
        }
    }

    /**
     * Used to handle dropping a soulbound, untradeable, or
     * permanently untradeable item.
     *
     * @param event
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onItemDrop(PlayerDropItemEvent event) {
        if (event.isCancelled()) return;
        Player p = event.getPlayer();
        GamePlayer gp = GameAPI.getGamePlayer(p);
        if (gp != null && !gp.isAbleToDrop())
            return;
        ItemStack item = event.getItemDrop().getItemStack();

        //  SOULBOUND ITEM DESTRUCTION PROMPT  //
        if (ItemManager.isItemSoulbound(item)) {
            //Don't cancel this event inside of here. It keeps the item in the inventory.
            event.getItemDrop().remove();
            p.sendMessage(ChatColor.RED + "Are you sure you want to " + ChatColor.UNDERLINE + "destroy" + ChatColor.RED + " this Soulbound item? ");
            p.sendMessage(ChatColor.GRAY + "Type " + ChatColor.GREEN + ChatColor.BOLD + "Y" + ChatColor.GRAY + " or " + ChatColor.DARK_RED + ChatColor.BOLD + "N" + ChatColor.GRAY + " to confirm.");
            p.playSound(p.getLocation(), Sound.BLOCK_LAVA_EXTINGUISH, 1, 1.2F);

            if (p.getItemOnCursor().equals(item))
                p.setItemOnCursor(null);

            Chat.promptPlayerConfirmation(p, () -> {
                p.sendMessage(ChatColor.RED + "Item " + (item.hasItemMeta() && item.getItemMeta().hasDisplayName() ? item.getItemMeta().getDisplayName() + " " : "") + ChatColor.RED + "has been " + ChatColor.UNDERLINE + "destroyed.");
            }, () -> {
                p.sendMessage(ChatColor.RED + "Soulbound item destruction " + ChatColor.UNDERLINE + "CANCELLED");
                GameAPI.giveOrDropItem(p, item.clone());
            });

            return;
        }

        //  PREVENT DROPPING PERMANENTLY UNTRADEABLE ITEMS  //
        if (ItemManager.isItemPermanentlyUntradeable(item)) {
            event.setCancelled(true);
            event.getItemDrop().remove();
            event.getPlayer().sendMessage(ChatColor.GRAY + "This item is " + ChatColor.UNDERLINE + "not" + ChatColor.GRAY + " droppable.");
            return;
        }

        //  PREVENT DROPPING UNTRADEABLE ITEMS  //
        if (!ItemManager.isItemTradeable(item)) {
            event.getItemDrop().remove();
            p.sendMessage(ChatColor.GRAY + "This item was " + ChatColor.ITALIC + "untradeable" + ChatColor.GRAY + ", " + "so it has " + ChatColor.UNDERLINE + "vanished.");
            p.playSound(p.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.6F, 0.2F);
            return;
        }

        PlayerManager.checkInventory(event.getPlayer());

        //  SILENTLY REMOVE UNDROPPABLE ITEMS  //
        if (!ItemManager.isItemDroppable(item)) {
            event.getItemDrop().remove();
            event.setCancelled(true);
            return;
        }
    }

    @EventHandler //Prevents dropping undroppable items and having the pop back up in your inventory.
    public void onInventoryClick(InventoryClickEvent evt) {
        if (evt.getAction() != InventoryAction.DROP_ALL_SLOT && evt.getAction() != InventoryAction.DROP_ONE_SLOT)
            return;

        if (!(new VanillaItem(evt.getCurrentItem()).isUndroppable()))
            return;

        evt.setCancelled(true);
    }

    /**
     * This is a prank for the people who are supposedly using vanilla potions in pvp.
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    private void playPotionPrank(PlayerInteractEvent evt) {
        ItemStack item = evt.getItem();
        Player player = evt.getPlayer();
        if (item == null || item.getType() == Material.AIR || PotionItem.isPotion(item) || (item.getType() != Material.POTION && item.getType() != Material.SPLASH_POTION))
            return;
        evt.setCancelled(true);
        player.getInventory().remove(item);

        player.sendMessage(ChatColor.GRAY + " *Glug* *Glug* *Glug*");
        player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_DRINK, 1, 1);

        Bukkit.getScheduler().runTaskLater(DungeonRealms.getInstance(), () -> {
            player.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 375, 2));
            player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 350, 2));
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 350, 2));
        }, 5);

        for (int second = 0; second < 15; second++) {
            for (int i = 0; i < 4; i++) {
                final int in = i;
                Bukkit.getScheduler().runTaskLater(DungeonRealms.getInstance(), () -> {
                    if (player.isOnline())
                        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_DEATH, 0.17f + 0.33f * in, 0.25f * in + 0.25f);
                }, (second * 20) + 1 + i * 5);
            }
        }

        Bukkit.getScheduler().runTaskLater(DungeonRealms.getInstance(), () -> {
            player.sendMessage(ChatColor.DARK_GREEN + "" + ChatColor.ITALIC + "Regret shoots through you as vomit pours from your mouth.");
        }, 16);
    }
}
