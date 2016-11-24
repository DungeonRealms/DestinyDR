package net.dungeonrealms.vgame.goal.achievement.gui;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.dungeonrealms.common.frontend.menu.construct.BasicGUI;
import net.dungeonrealms.common.frontend.menu.construct.action.GUIAction;
import net.dungeonrealms.vgame.goal.achievement.EnumAchievementGroup;
import net.dungeonrealms.vgame.player.GamePlayer;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Giovanni on 22-11-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class AchievementGUI extends BasicGUI
{
    public AchievementGUI(GamePlayer gamePlayer)
    {
        super(null, "Achievements", 18);

        // Calculate achievement group count
        int combatAchievements = 0;
        int explorerAchievements = 0;

        for (EnumAchievementGroup achievementGroup : EnumAchievementGroup.values())
        {
            ItemStack itemStack = new ItemStack(achievementGroup.getMaterial());
            ItemMeta itemMeta = itemStack.getItemMeta();
            itemMeta.setDisplayName(ChatColor.GOLD.toString() + ChatColor.BOLD + achievementGroup.getName());
            List<String> simpleLore = Lists.newArrayList();
            if (achievementGroup == EnumAchievementGroup.COMBAT)
            {
                simpleLore.add("&eOwned: &a" + combatAchievements + "&8/&c" + achievementGroup.getSize());
            } else if (achievementGroup == EnumAchievementGroup.EXPLORER)
            {
                simpleLore.add("&eOwned: &a" + explorerAchievements + "&8/&c" + achievementGroup.getSize());
            }
            simpleLore.addAll(Arrays.asList("", "&aClick to view"));

            List<String> endLore = Lists.newArrayList();
            endLore.addAll(simpleLore.stream().map(string -> ChatColor.translateAlternateColorCodes('&', string)).collect(Collectors.toList()));
            itemMeta.setLore(endLore);
            itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemStack.setItemMeta(itemMeta);

            this.addAction(new GUIAction(itemStack)
            {
                @Override
                public void perform(Player player, InventoryClickEvent event)
                {
                    if(event.getCurrentItem() == this.getItemStack())
                    {
                        if(this.getItemStack().hasItemMeta())
                        {
                        }
                    }
                }
            }, achievementGroup.getSlot());
        }
    }
}
