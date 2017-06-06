package net.dungeonrealms.game.player.inventory.menus.guis;

import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.game.mastery.StatBoost;
import net.dungeonrealms.game.mastery.Stats;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.player.chat.Chat;
import net.dungeonrealms.game.player.inventory.menus.GUIItem;
import net.dungeonrealms.game.player.inventory.menus.GUIMenu;
import net.dungeonrealms.game.player.stats.PlayerStats;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Rar349 on 5/17/2017.
 */
public class StatGUI extends GUIMenu {

    private static DecimalFormat df = new DecimalFormat("##.###");

    public StatGUI(Player viewer) {
        super(viewer, 18, "Stat Points");
    }

    @Override
    protected void setItems() {
        PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(player);

        PlayerStats stats = wrapper.getPlayerStats();

        setItem(6, new GUIItem(Material.INK_SACK).setDurability(DyeColor.LIME.getDyeData()).setName(ChatColor.GREEN + "Confirm").setLore(ChatColor.GRAY + "Click to confirm your stat ", ChatColor.GRAY + "point allocation.  If you ", ChatColor.GRAY + "want to undo your changes, ", ChatColor.GRAY + "press escape.").setClick((evt) -> {
            stats.confirmStats();
            player.closeInventory();
        }));

        setItem(15, new GUIItem(Material.ENCHANTED_BOOK).setName(ChatColor.YELLOW + "Stat Point Info").setLore(ChatColor.LIGHT_PURPLE + "Points to Allocate: " + stats.getFreePoints(),
                ChatColor.AQUA + "LCLICK" + ChatColor.GRAY + " to allocate " + ChatColor.AQUA.toString() + ChatColor.UNDERLINE + "1" + ChatColor.GRAY + " point",
                ChatColor.AQUA + "RCLICK" + ChatColor.GRAY + " to unallocate " + ChatColor.AQUA.toString() + ChatColor.UNDERLINE + "1" + ChatColor.GRAY + " point",
                ChatColor.AQUA + "S-LCLICK" + ChatColor.GRAY + " to allocate " + ChatColor.AQUA.toString() + ChatColor.UNDERLINE + "3" + ChatColor.GRAY + " points",
                ChatColor.AQUA + "S-RCLICK" + ChatColor.GRAY + " to unallocate " + ChatColor.AQUA.toString() + ChatColor.UNDERLINE + "3" + ChatColor.GRAY + " points",
                ChatColor.AQUA + "MCLICK" + ChatColor.GRAY + " to allocate " + ChatColor.AQUA.toString() + ChatColor.UNDERLINE + "custom" + ChatColor.GRAY + " points"));

        for(Stats stat : Stats.values()) {
            //Hooks
            int temp = stats.getTempStat(stat);
            boolean buy = temp > 0;

            List<String> hookLore = new ArrayList<>();

            for(StatBoost boost : stat.getStatBoosts()) {
                String prefix = ChatColor.stripColor(boost.getType().getPrefix());
                hookLore.add(ChatColor.GOLD + prefix + ChatColor.AQUA + df.format(stats.getStat(stat) * (boost.getMultiplier() * 100)) + boost.getType().getSuffix() + " " + (buy ? ChatColor.GREEN + "[+" + df.format(temp * (boost.getMultiplier() * 100)) + "]" : ""));
            }

            setItem(stat.getGuiSlot() + 9, new GUIItem(Material.TRIPWIRE_HOOK).setName(ChatColor.RED + stat.getDisplayName() + " Bonuses: " + stats.getStat(stat) + (buy ? ChatColor.GREEN + "[+" + temp + "]" : "")).setLore(hookLore));
            //Maps
            List<String> lore = new ArrayList<>(Arrays.asList(stat.getDescription()));
            Utils.addChatColor(lore,ChatColor.GRAY);
            lore.add(ChatColor.AQUA + "Allocated Points: " + stats.getStat(stat) + (stats.getTempStat(stat) > 0 ? ChatColor.GREEN + " [+" + stats.getTempStat(stat) + "]" : ""));
            lore.add(ChatColor.RED + "Free Points: " + stats.getFreePoints());
            setItem(stat.getGuiSlot(), new GUIItem(Material.EMPTY_MAP).setName(ChatColor.DARK_PURPLE + stat.getDisplayName()).setLore(lore).setClick((evt) -> {
                if (evt.getClick() == ClickType.MIDDLE) {

                    this.setShouldOpenPreviousOnClose(false);
                    player.closeInventory();
                    player.sendMessage(ChatColor.GREEN + "Type a custom allocated amount.");
                    stats.reset = false;
                    int currentFreePoints = stats.getFreePoints();

                    Chat.listenForNumber(player, 0, currentFreePoints, num -> {
                        for (int i = 0; i < num; i++) {
                            stats.allocatePoint(stat);
                        }
                        StatGUI.this.open(player,null);
                    }, () -> {
                        player.sendMessage(ChatColor.RED + "CUSTOM STAT - " + ChatColor.BOLD + "CANCELLED");
                        stats.resetTemp();
                        StatGUI.this.open(player,null);
                    });

                } else {
                    int amount = evt.isShiftClick() ? 3 : 1;
                    for (int i = 0; i < amount; i++) {
                        if (evt.isRightClick())
                            stats.removePoint(stat);
                        if (evt.isLeftClick())
                            stats.allocatePoint(stat);

                    }
                    reconstructGUI(player);
                }
            }));
        }


    }
}
