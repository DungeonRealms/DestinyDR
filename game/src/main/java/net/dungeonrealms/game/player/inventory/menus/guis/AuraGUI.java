package net.dungeonrealms.game.player.inventory.menus.guis;

import com.google.common.collect.Lists;
import net.dungeonrealms.game.item.ItemType;
import net.dungeonrealms.game.item.items.core.Aura;
import net.dungeonrealms.game.item.items.functional.ItemLootAura;
import net.dungeonrealms.game.mastery.RomanNumeralUtils;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.player.inventory.menus.GUIItem;
import net.dungeonrealms.game.player.inventory.menus.GUIMenu;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class AuraGUI extends GUIMenu {
    private Aura aura;

    public AuraGUI(Player player, Aura aura) {
        super(player, 9, aura.getName() + "'s " + aura.getType().getName(false) + " Aura");
        this.aura = aura;
    }

    @Override
    protected void setItems() {

        List<String> lore = Lists.newArrayList("", ChatColor.GREEN + "Level: " + RomanNumeralUtils.numeralOf(aura.getLevel()));
        lore.addAll(Utils.getLoreFromLine(String.format(aura.getType().getDescription(), aura.getRadius(), aura.getRadius()), ChatColor.GRAY, "\n"));
        setItem(4, new GUIItem(new ItemStack(Material.DIAMOND))
                .setName(aura.getType().getColor().toString() + aura.getType().getName(false) + " Buff Aura")
                .setLore(lore)
                .setClick(e -> {
                    if (aura.getSecondsRemaining() <= 0) {
                        player.closeInventory();
                        return;
                    }

                    if (aura.getLevel() >= 5) {
                        player.sendMessage(ChatColor.RED + "This Aura is already max level!");
                        player.closeInventory();
                        return;
                    }

                    boolean removed = removeAura(player);
                    if (removed) {
                        Player owner = Bukkit.getPlayer(aura.getName());
                        aura.getSeconds().add(new AtomicInteger(aura.getMaxTime()));

                        if (owner != null)
                            owner.sendMessage(ChatColor.GREEN + player.getName() + " has increased your " + aura.getType().getName(true) + " Buff Aura " + ChatColor.GREEN + "to Level " + RomanNumeralUtils.numeralOf(aura.getLevel()) + "!");

                        player.sendMessage(ChatColor.GREEN + aura.getName() + "'s " + aura.getType().getName(true) + " Buff Aura " + ChatColor.GREEN + "has been increased to Level " + RomanNumeralUtils.numeralOf(aura.getLevel()) + "!");
                        this.setItems();
                        player.updateInventory();
                    } else {
                        player.sendMessage(ChatColor.RED + "You do NOT have any matching Auras in your inventory to level up this Buff Aura!");
                        player.closeInventory();
                    }
                }));

    }


    private boolean removeAura(Player player) {
        for (ItemStack item : player.getInventory()) {
            if (item == null) continue;
            if (item.getType() == Material.DIAMOND && ItemLootAura.isType(item, ItemType.BUFF_AURA)) {
                //Remove?
                ItemLootAura is = (ItemLootAura) ItemLootAura.constructItem(item);
                if (is != null && is.getAuraType() == aura.getType() && aura.getMult() == is.getMultiplier() && aura.getMaxTime() == is.getDuration()) {
                    player.getInventory().removeItem(item);
                    return true;
//                } else if (is != null) {
//                    System.out.println("Aura doesnt match: " + aura.getType() + " : " + is.getAuraType() + " mult: " + aura.getMultiplier() + " is: " + is.getMultiplier());
                }
            }
        }
        return false;
    }
}
