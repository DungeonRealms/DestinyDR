package net.dungeonrealms.game.item.items.functional.cluescrolls;

import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.game.util.ChatColor;
import net.dungeonrealms.game.item.items.functional.accessories.Trinket;
import net.dungeonrealms.game.item.items.functional.accessories.TrinketItem;
import net.dungeonrealms.game.item.items.functional.accessories.TrinketType;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.player.json.JSONMessage;
import net.dungeonrealms.game.world.item.Item;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by Rar349 on 6/15/2017.
 */
public class TrinketClueReward implements AbstractClueReward {

    private List<Trinket> possibleRewards;
    private TrinketType type;
    private HashMap<ClueDifficulty, NavigableMap<Double, Trinket>> rewardTrees = new HashMap<>();
    private HashMap<ClueDifficulty, Double> totals = new HashMap<>();

    public TrinketClueReward(TrinketType type) {
        this.type = type;
        possibleRewards = type.getAccessory();
    }

    /*public TrinketClueReward(Trinket... trinkets) {
        possibleRewards = Arrays.asList(trinkets);
    }*/

    @Override
    public void giveReward(Player player, ClueDifficulty difficulty, String npcName) {
        player.getEquipment().setItemInMainHand(null);
        Trinket reward = getRandomTrinket(difficulty);
        ItemStack rewardStack = new TrinketItem(type, reward).generateItem();
        ItemMeta meta = rewardStack.getItemMeta();
        List<String> hoveredChat = new ArrayList<>();
        hoveredChat.add(Utils.getItemName(rewardStack));

        if (meta.hasLore())
            hoveredChat.addAll(meta.getLore());

        final JSONMessage normal = new JSONMessage(org.bukkit.ChatColor.GREEN + "The " + npcName + " holds out his hand and gives you: ", org.bukkit.ChatColor.DARK_GREEN);
        normal.addHoverText(hoveredChat, org.bukkit.ChatColor.BOLD + org.bukkit.ChatColor.UNDERLINE.toString() + "SHOW");

        normal.sendToPlayer(player);
        GameAPI.giveOrDropItem(player,rewardStack);

        if(reward.getItemRarity().equals(Item.ItemRarity.UNIQUE) || reward.getItemRarity().equals(Item.ItemRarity.RARE)) {
            for(Player near : GameAPI.getNearbyPlayers(player.getLocation(),64))near.playSound(near.getLocation(), Sound.ENTITY_WITHER_SPAWN, 1f, 1.6f);
            player.playSound(player.getLocation(), Sound.ENTITY_WITHER_SPAWN, 1f, 1.6f);
        }

        player.updateInventory();
    }

    public Trinket getRandomTrinket(ClueDifficulty difficulty) {
        NavigableMap<Double, Trinket> table = rewardTrees.get(difficulty);
        Double totalObj = totals.get(difficulty);
        if(totalObj == null) totalObj = new Double(0);
        double total = totalObj;
        if (table == null) {
            total = 0;
            table = new TreeMap<>();

            for (Trinket trinket : possibleRewards) {
                total += trinket.getChance();
                total += getIncreasedChance(difficulty, trinket);
                table.put(total, trinket);
            }

            rewardTrees.put(difficulty, table);
            totals.put(difficulty, total);
        }

        return table.ceilingEntry(ThreadLocalRandom.current().nextDouble() * total).getValue();
    }

    public int getIncreasedChance(ClueDifficulty difficulty, Trinket trink) {
        if(trink.getItemRarity().equals(Item.ItemRarity.RARE)) {
            if(difficulty.equals(ClueDifficulty.HARD)) return 2;
            if(difficulty.equals(ClueDifficulty.MEDIUM)) return 1;
        }

        if(trink.getItemRarity().equals(Item.ItemRarity.UNCOMMON)) {
            if(difficulty.equals(ClueDifficulty.HARD)) return 3;
            if(difficulty.equals(ClueDifficulty.MEDIUM)) return 2;
        }

        return 0;
    }

}
