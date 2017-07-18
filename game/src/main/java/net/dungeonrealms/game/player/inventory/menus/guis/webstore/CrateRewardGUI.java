package net.dungeonrealms.game.player.inventory.menus.guis.webstore;

import com.google.common.collect.Lists;
import net.dungeonrealms.game.player.inventory.menus.GUIItem;
import net.dungeonrealms.game.player.inventory.menus.GUIMenu;
import net.dungeonrealms.game.player.inventory.menus.guis.webstore.crates.AbstractCrateReward;
import net.dungeonrealms.game.player.inventory.menus.guis.webstore.crates.Crates;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Rar349 on 7/10/2017.
 */
public class CrateRewardGUI extends GUIMenu {

    private Crates crate;
    public CrateRewardGUI(Player player, Crates crate) {
        super(player, fitSize(crate.getCommonRewards().length + crate.getUncommonRewards().length + crate.getRareRewards().length + crate.getVeryRareRewards().length + crate.getInsaneRewards().length), crate.getDisplayName() + " rewards");
        this.crate = crate;
    }

    @Override
    protected void setItems() {

        int slot = 0;
        for(AbstractCrateReward reward : crate.getInsaneRewards()) {
            ItemStack displayItem = reward.getDisplayMaterial().toItemStack();
            displayItem.setAmount(1);
            ItemMeta meta = displayItem.getItemMeta();
            meta.spigot().setUnbreakable(true);
            meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_UNBREAKABLE);
            displayItem.setItemMeta(meta);
            List<String> lore = Lists.newArrayList(reward.getDisplayLore());
            lore.add(" ");
            lore.add(ChatColor.DARK_RED + "Insane");
            setItem(slot++, new GUIItem(displayItem).setName(ChatColor.DARK_RED + ChatColor.BOLD.toString() + ChatColor.stripColor(reward.getDisplayName())).setLore(lore).setClick((evt) -> {

            }));
        }

        for(AbstractCrateReward reward : crate.getVeryRareRewards()) {
            ItemStack displayItem = reward.getDisplayMaterial().toItemStack();
            displayItem.setAmount(1);
            List<String> lore = Lists.newArrayList(reward.getDisplayLore());
            lore.add(" ");
            lore.add(ChatColor.RED + "Very Rare");
            setItem(slot++, new GUIItem(displayItem).setName(ChatColor.RED + ChatColor.BOLD.toString() + ChatColor.stripColor(reward.getDisplayName())).setLore(lore).setClick((evt) -> {

            }));
        }

        for(AbstractCrateReward reward : crate.getRareRewards()) {
            ItemStack displayItem = reward.getDisplayMaterial().toItemStack();
            displayItem.setAmount(1);
            List<String> lore = Lists.newArrayList(reward.getDisplayLore());
            lore.add(" ");
            lore.add(ChatColor.GOLD + "Rare");
            setItem(slot++, new GUIItem(displayItem).setName(ChatColor.GOLD + ChatColor.BOLD.toString() + ChatColor.stripColor(reward.getDisplayName())).setLore(lore).setClick((evt) -> {

            }));
        }

        for(AbstractCrateReward reward : crate.getUncommonRewards()) {
            ItemStack displayItem = reward.getDisplayMaterial().toItemStack();
            displayItem.setAmount(1);
            List<String> lore = Lists.newArrayList(reward.getDisplayLore());
            lore.add(" ");
            lore.add(ChatColor.YELLOW + "Uncommon");
            setItem(slot++, new GUIItem(displayItem).setName(ChatColor.YELLOW + ChatColor.BOLD.toString() + ChatColor.stripColor(reward.getDisplayName())).setLore(lore).setClick((evt) -> {

            }));
        }

        for(AbstractCrateReward reward : crate.getCommonRewards()) {
            ItemStack displayItem = reward.getDisplayMaterial().toItemStack();
            displayItem.setAmount(1);
            List<String> lore = Lists.newArrayList(reward.getDisplayLore());
            lore.add(" ");
            lore.add(ChatColor.GREEN + "Common");
            setItem(slot++, new GUIItem(displayItem).setName(ChatColor.GREEN + ChatColor.BOLD.toString() + ChatColor.stripColor(reward.getDisplayName())).setLore(lore).setClick((evt) -> {

            }));
        }
    }
}
