package net.dungeonrealms.game.guild.banner.menus;

import net.dungeonrealms.game.gui.GUIButtonClickEvent;
import net.dungeonrealms.game.gui.VolatileGUI;
import net.dungeonrealms.game.gui.item.GUIButton;
import net.dungeonrealms.game.gui.item.GUIDisplayer;
import net.dungeonrealms.game.guild.banner.AbstractMenu;
import net.dungeonrealms.game.guild.banner.menus.selectors.ColorSelectorMenu;
import net.dungeonrealms.game.guild.banner.menus.selectors.PatternSelectorMenu;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;

import java.util.Arrays;
import java.util.Collections;
import java.util.function.Consumer;

/**
 * Class written by APOLLOSOFTWARE.IO on 6/11/2016
 */


public class BannerCreatorMenu extends AbstractMenu implements VolatileGUI {
    protected Consumer<ItemStack> onFinish;

    public BannerCreatorMenu(Player player, Consumer<ItemStack> onFinish) {
        super("Create a banner for your guild!", 45, player.getUniqueId());
        this.onFinish = onFinish;

        GUIDisplayer banner = new GUIDisplayer(Material.BANNER);

        BannerMeta bannerMeta = (BannerMeta) banner.getItemStack().getItemMeta();
        bannerMeta.setBaseColor(DyeColor.WHITE);

        banner.setDisplayName(ChatColor.AQUA + "Preview");
        banner.setLore(Arrays.asList(" ", "&7This is how your banner will appear."));

        set(4, banner);

        GUIButton setBaseColor = new GUIButton(Material.INK_SACK, (byte) 1) {
            @Override
            public void action(GUIButtonClickEvent event) throws Exception {

                new ColorSelectorMenu(event.getWhoClicked(), color -> {
                    bannerMeta.setBaseColor(color);
                    banner.getItemStack().setItemMeta(bannerMeta);
                    player.openInventory(inventory);

                }).open(event.getWhoClicked());

            }
        };

        setBaseColor.setDisplayName(ChatColor.GREEN + "Set base color.");
        setBaseColor.setLore(Collections.singletonList("&7Click here to set your desired base color."));
        set(20, setBaseColor);

        GUIButton addPattern = new GUIButton(Material.WORKBENCH) {
            @Override
            public void action(GUIButtonClickEvent event) throws Exception {
                new PatternSelectorMenu(event.getWhoClicked(), pattern -> {
                    bannerMeta.addPattern(pattern);
                    banner.getItemStack().setItemMeta(bannerMeta);
                    player.openInventory(inventory);

                }).open(event.getWhoClicked());
            }
        };

        addPattern.setDisplayName(ChatColor.GREEN + "Add a pattern.");
        addPattern.setLore(Collections.singletonList("&7Click here to add pattern to your banner."));
        set(22, addPattern);


        GUIButton reset = new GUIButton(Material.PAPER) {
            @Override
            public void action(GUIButtonClickEvent event) throws Exception {
                for (int i = 0; i < bannerMeta.getPatterns().size(); i++)
                    bannerMeta.removePattern(i);

                bannerMeta.setBaseColor(DyeColor.WHITE);
                banner.getItemStack().setItemMeta(bannerMeta);

            }
        };

        reset.setDisplayName(ChatColor.RED + "Reset.");
        reset.setLore(Collections.singletonList("&7Click here to start over again."));
        set(24, reset);


        GUIButton createBanner = new GUIButton(Material.INK_SACK, (byte) 10) {
            @Override
            public void action(GUIButtonClickEvent event) throws Exception {
                final ItemStack finalBanner = banner.getItemStack();
                event.getWhoClicked().closeInventory();


            }
        };

        createBanner.setDisplayName(ChatColor.GREEN + "Create Banner");
        createBanner.setLore(Arrays.asList("&7Click here to start over again."));
        set(40, createBanner);

        fillEmptySpaces(getSpaceFillerItem());
    }

    @Override
    public void open(Player player) {
        player.openInventory(inventory);
    }

    @Override
    public void onDestroy(Event event) {
        onFinish.accept(null);
    }


}
