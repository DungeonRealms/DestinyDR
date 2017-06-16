package net.dungeonrealms.game.item.items.functional.cluescrolls;

import net.citizensnpcs.api.event.NPCClickEvent;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.npc.entity.EntityHumanNPC;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.game.item.ItemType;
import net.dungeonrealms.game.item.PersistentItem;
import net.dungeonrealms.game.mechanic.ItemManager;
import net.dungeonrealms.game.mechanic.PlayerManager;
import net.dungeonrealms.game.player.banks.BankMechanics;
import net.dungeonrealms.game.player.banks.Storage;
import net.dungeonrealms.game.world.entity.util.MountUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.server.MapInitializeEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

/**
 * Created by Rar349 on 6/14/2017.
 */
public class ClueListener implements Listener {


    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        updateClueScrolls(event.getPlayer());
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if(event.getDamager() instanceof Player) {
            ClueUtils.handleMobHit((Player)event.getDamager(), event.getEntity());
        }
    }

    @EventHandler
    public void onPlayerInteractNPC(PlayerInteractAtEntityEvent event) {
        if(!(event.getRightClicked() instanceof EntityHumanNPC.PlayerNPC)) return;
        EntityHumanNPC.PlayerNPC npc = (EntityHumanNPC.PlayerNPC)event.getRightClicked();
        Player player = event.getPlayer();
        if(player.hasMetadata("npcClick")) {
            long time = player.getMetadata("npcClick").get(0).asLong();
            if(System.currentTimeMillis() - time < 500) return;
        }
        player.setMetadata("npcClick", new FixedMetadataValue(DungeonRealms.getInstance(), System.currentTimeMillis()));
        if(npc.getName().equalsIgnoreCase("fisherman")) {
            ItemStack stack = player.getEquipment().getItemInMainHand();
            if(stack == null || !stack.getType().equals(Material.MAP)) return;
            if(!PersistentItem.isType(stack, ItemType.CLUE_SCROLL)) return;
            ClueScrollItem item = new ClueScrollItem(stack);
            if(!item.getClueType().equals(ClueScrollType.FISHING)) {
                npcChatPlayer(player, npc, "I do not understand this at all!");
                npcChatPlayer(player, npc, "Maybe someone else can help you!");
                return;
            }
            if(item.isComplete()) {
                item.getClueType().getRewards().giveReward(player, item.getDifficulty(), npc.getName());
                return;
            }
            if(item.isHasBeenTranslated()) {
                npcChatPlayer(player, npc, "This is already translated!");
                return;
            }
            item.setTranslated(true);
            player.getEquipment().setItemInMainHand(item.generateItem());
            npcChatPlayer(player,npc, "There you go mate! Goodluck on your Treasure Scroll!");
        } else if(npc.getName().equalsIgnoreCase("miner")) {
            ItemStack stack = player.getEquipment().getItemInMainHand();
            if(stack == null || !stack.getType().equals(Material.MAP)) return;
            if(!PersistentItem.isType(stack, ItemType.CLUE_SCROLL)) return;
            ClueScrollItem item = new ClueScrollItem(stack);
            if(!item.getClueType().equals(ClueScrollType.MINING)) {
                npcChatPlayer(player, npc, "I do not understand this at all!");
                npcChatPlayer(player, npc, "Maybe someone else can help you!");
                return;
            }
            if(item.isComplete()) {
                item.getClueType().getRewards().giveReward(player, item.getDifficulty(), npc.getName());
                return;
            }
            if(item.isHasBeenTranslated()) {
                npcChatPlayer(player, npc, "This is already translated!");
                return;
            }
            item.setTranslated(true);
            player.getEquipment().setItemInMainHand(item.generateItem());
            npcChatPlayer(player,npc, "There you go mate! Goodluck on your Treasure Scroll!");
        } else if(npc.getName().equalsIgnoreCase("isaam")) {
            ItemStack stack = player.getEquipment().getItemInMainHand();
            if(stack == null || !stack.getType().equals(Material.MAP)) return;
            if(!PersistentItem.isType(stack, ItemType.CLUE_SCROLL)) return;
            ClueScrollItem item = new ClueScrollItem(stack);
            if(!item.getClueType().equals(ClueScrollType.COMBAT)) {
                npcChatPlayer(player, npc, "I do not understand this at all!");
                npcChatPlayer(player, npc, "Maybe someone else can help you!");
                return;
            }
            if(item.isComplete()) {
                item.getClueType().getRewards().giveReward(player, item.getDifficulty(), npc.getName());
                return;
            }
            if(item.isHasBeenTranslated()) {
                npcChatPlayer(player, npc, "This is already translated!");
                return;
            }
            item.setTranslated(true);
            player.getEquipment().setItemInMainHand(item.generateItem());
            npcChatPlayer(player,npc, "There you go warrior! Goodluck on your Treasure Scroll!");
        }
    }

    public void npcChatPlayer(Player sending, EntityHumanNPC.PlayerNPC citizen, String message) {
        sending.sendMessage(ChatColor.AQUA + citizen.getName() + ": " + ChatColor.YELLOW + message);
    }

    public void updateClueScrolls(Player player) {
        PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(player);
        if(!PlayerManager.hasItem(player, ItemType.CLUE_SCROLL)) return;
        for(int k = 0; k < player.getInventory().getContents().length; k++){
            ItemStack stack = player.getInventory().getContents()[k];
            if(stack == null || !stack.getType().equals(Material.MAP)) continue;
            if(!PersistentItem.isType(stack, ItemType.CLUE_SCROLL)) continue;
            ClueScrollItem item = new ClueScrollItem(stack);
            player.getInventory().setItem(k, item.generateItem());
        }
        if(wrapper == null) return;
        Storage storage = BankMechanics.getStorage(player);
        if(storage != null) {
            if(storage.inv != null) {
                for(int k = 0; k < storage.inv.getContents().length; k++) {
                    ItemStack stack = storage.inv.getContents()[k];
                    if(stack == null || !stack.getType().equals(Material.MAP));
                    if(!PersistentItem.isType(stack, ItemType.CLUE_SCROLL)) continue;
                    ClueScrollItem item = new ClueScrollItem(stack);
                    storage.inv.getContents()[k] = item.generateItem();
                }
            }
            if(storage.collection_bin != null) {
                for(int k = 0; k < storage.collection_bin.getContents().length; k++) {
                    ItemStack stack = storage.collection_bin.getContents()[k];
                    if(stack == null || !stack.getType().equals(Material.MAP));
                    if(!PersistentItem.isType(stack, ItemType.CLUE_SCROLL)) continue;
                    ClueScrollItem item = new ClueScrollItem(stack);
                    storage.collection_bin.getContents()[k] = item.generateItem();
                }
            }
            if(MountUtils.hasInventory(player)) {
                Inventory mule = MountUtils.getInventory(player);
                for(int k = 0; k < mule.getContents().length; k++) {
                    ItemStack stack = mule.getContents()[k];
                    if(stack == null || !stack.getType().equals(Material.MAP));
                    if(!PersistentItem.isType(stack, ItemType.CLUE_SCROLL)) continue;
                    ClueScrollItem item = new ClueScrollItem(stack);
                    mule.getContents()[k] = item.generateItem();
                }
            }
        }
    }
}
