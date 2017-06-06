package net.dungeonrealms.game.item.items.functional;

import net.dungeonrealms.game.item.ItemType;
import net.dungeonrealms.game.item.ItemUsage;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class ItemQuestCompass extends FunctionalItem{
    public ItemQuestCompass() {
        super(ItemType.QUEST_COMPASS);
    }
    public ItemQuestCompass(ItemStack item) {
        this();
    }

    @Override
    protected ItemStack getStack() {
        return new ItemStack(Material.COMPASS, 1);
    }

    @Override
    protected String getDisplayName() {
        return ChatColor.AQUA + ChatColor.BOLD.toString() + "Quest Objective";
    }

    @Override
    protected String[] getLore() {
        return new String[] {
                ChatColor.GRAY + "This compass points to your",
                ChatColor.GRAY + "current quest objective!"};
    }

    @Override
    protected ItemUsage[] getUsage() {
        return new ItemUsage[0];
    }
}
